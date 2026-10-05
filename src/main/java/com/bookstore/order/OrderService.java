package com.bookstore.order;

import com.bookstore.address.Address;
import com.bookstore.address.AddressRepository;
import com.bookstore.catalogue.Book;
import com.bookstore.catalogue.BookRepository;
import com.bookstore.cart.Cart;
import com.bookstore.cart.CartItem;
import com.bookstore.cart.CartRepository;
import com.bookstore.common.exception.BusinessRuleException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.PagedOrdersResponse;
import com.bookstore.order.dto.PlaceOrderRequest;
import com.bookstore.user.CurrentUserService;
import com.bookstore.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order service — checkout, history, detail, and cancellation (FR-05 to FR-09).
 *
 * Checkout flow (POST /orders), executed atomically within one transaction:
 *   1. Load the authenticated customer's cart; reject if empty (BR-04).
 *   2. Verify the selected address belongs to the customer.
 *   3. Re-validate stock for every line against current book stock (BR-03).
 *   4. Compute the order total from current book prices.
 *   5. Run the simulated payment; on decline, abort with 422 (no order persisted) (FR-06.6, DM-02).
 *   6. On approval, create the CONFIRMED order with an address snapshot (DM-03),
 *      locking unit prices at purchase time (BR-06).
 *   7. Decrement book stock (BR-05).
 *   8. Clear the cart items; the cart row is reused (DM-06).
 *
 * Cancellation (POST /orders/{id}/cancel):
 *   - Allowed only within 48 hours of creation (BR-07) and only when CONFIRMED.
 *   - Sets status to CANCELLED and restores stock for all items.
 */
@Service
public class OrderService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final long CANCELLATION_WINDOW_HOURS = 48;

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final BookRepository bookRepository;
    private final CurrentUserService currentUserService;
    private final SimulatedPaymentService paymentService;
    private final OrderMapper mapper;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        AddressRepository addressRepository,
                        BookRepository bookRepository,
                        CurrentUserService currentUserService,
                        SimulatedPaymentService paymentService,
                        OrderMapper mapper) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.bookRepository = bookRepository;
        this.currentUserService = currentUserService;
        this.paymentService = paymentService;
        this.mapper = mapper;
    }

    // -------------------------------------------------------------------------
    // Checkout
    // -------------------------------------------------------------------------

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        User user = currentUserService.requireCurrentUser();

        // 1. Load cart; reject if empty
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessRuleException(
                        "CART_EMPTY", "Cannot place an order with an empty cart"));
        List<CartItem> cartItems = cart.getItems();
        if (cartItems.isEmpty()) {
            throw new BusinessRuleException(
                    "CART_EMPTY", "Cannot place an order with an empty cart");
        }

        // 2. Verify the address belongs to the customer
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", request.getAddressId()));
        if (!address.getUser().getId().equals(user.getId())) {
            // Per the approved OpenAPI contract, a checkout against an address
            // owned by another customer is reported as a 422 business-rule
            // violation with code FORBIDDEN (see openapi.yaml /orders 422 example).
            throw new BusinessRuleException(
                    "FORBIDDEN", "The specified address does not belong to the current customer");
        }

        // 3 & 4. Validate stock and compute total from current prices
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Book book = item.getBook();
            if (item.getQuantity() > book.getStockQuantity()) {
                throw new BusinessRuleException(
                        "INSUFFICIENT_STOCK",
                        "Insufficient stock for '" + book.getTitle()
                                + "'. Requested: " + item.getQuantity()
                                + ", available: " + book.getStockQuantity());
            }
            total = total.add(book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 5. Simulated payment — abort (no persistence) on decline
        SimulatedPaymentService.PaymentResult payment =
                paymentService.charge(total, request.getPaymentMethod());
        if (!payment.approved()) {
            throw new BusinessRuleException(
                    "PAYMENT_DECLINED", "Simulated payment was declined");
        }

        // 6. Build the confirmed order with address snapshot and locked prices
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(total);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setSimulatedPaymentReference(payment.reference());
        order.setAddress(address);
        order.setSnapRecipient(address.getRecipientName());
        order.setSnapStreetLine1(address.getStreetLine1());
        order.setSnapStreetLine2(address.getStreetLine2());
        order.setSnapCity(address.getCity());
        order.setSnapState(address.getState());
        order.setSnapPostalCode(address.getPostalCode());
        order.setSnapCountry(address.getCountry());

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            Book book = item.getBook();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(book.getPrice()); // lock price at purchase (BR-06)
            orderItems.add(orderItem);

            // 7. Decrement stock (BR-05)
            book.setStockQuantity(book.getStockQuantity() - item.getQuantity());
            bookRepository.save(book);
        }
        order.setItems(orderItems);

        Order saved = orderRepository.save(order);

        // 8. Clear the cart items; reuse the cart row (DM-06)
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // History & detail
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PagedOrdersResponse listOrders(int page, int size) {
        User user = currentUserService.requireCurrentUser();

        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(
                safePage, safeSize, Sort.by("createdAt").descending());

        Page<Order> result = orderRepository.findByUserId(user.getId(), pageable);

        List<OrderResponse> content = result.getContent()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return new PagedOrdersResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return mapper.toResponse(loadOwnedOrder(orderId));
    }

    // -------------------------------------------------------------------------
    // Cancellation
    // -------------------------------------------------------------------------

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = loadOwnedOrder(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "ORDER_ALREADY_CANCELLED", "Order is already cancelled");
        }

        OffsetDateTime deadline = order.getCreatedAt().plusHours(CANCELLATION_WINDOW_HOURS);
        if (OffsetDateTime.now().isAfter(deadline)) {
            throw new BusinessRuleException(
                    "CANCELLATION_WINDOW_EXPIRED",
                    "The 48-hour cancellation window has passed");
        }

        // Restore stock for every item
        for (OrderItem item : order.getItems()) {
            Book book = item.getBook();
            book.setStockQuantity(book.getStockQuantity() + item.getQuantity());
            bookRepository.save(book);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        return mapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Loads an order and enforces ownership.
     * Returns 404 if the order does not exist; 403 if it belongs to another user.
     */
    private Order loadOwnedOrder(Long orderId) {
        User user = currentUserService.requireCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Order belongs to a different user");
        }
        return order;
    }
}
