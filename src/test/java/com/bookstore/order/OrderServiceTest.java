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
import com.bookstore.order.dto.PlaceOrderRequest;
import com.bookstore.support.TestEntities;
import com.bookstore.user.CurrentUserService;
import com.bookstore.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for checkout, cancellation, stock, and ownership logic
 * (FR-05 to FR-09; BR-04/05/06/07; DM-02/03/06/08).
 *
 * A real OrderMapper and SimulatedPaymentService are used (pure, deterministic);
 * all repository/user collaborators are mocked.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private BookRepository bookRepository;
    @Mock private CurrentUserService currentUserService;

    private final SimulatedPaymentService paymentService = new SimulatedPaymentService();
    private final OrderMapper mapper = new OrderMapper();
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Address address;
    private Book book;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, cartRepository, addressRepository,
                bookRepository, currentUserService, paymentService, mapper);

        user = TestEntities.user(1L, "demo@bookstore.com");
        cart = TestEntities.cart(10L, user);
        address = TestEntities.address(1L, user, "Austin");
        book = TestEntities.book(2L, "Clean Code", "Robert C. Martin", "42.50", 8);

        lenient().when(currentUserService.requireCurrentUser()).thenReturn(user);
        // save returns the same order instance
        lenient().when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private void seedCartWith(CartItem... items) {
        for (CartItem i : items) {
            cart.getItems().add(i);
        }
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
    }

    // ---------------------------------------------------------------- checkout

    @Test
    @DisplayName("Checkout creates a CONFIRMED order, locks price, decrements stock, clears cart")
    void checkoutHappyPath() {
        CartItem item = TestEntities.cartItem(55L, cart, book, 2);
        seedCartWith(item);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        OrderResponse response = orderService.placeOrder(
                new PlaceOrderRequest(1L, PaymentMethod.CREDIT_CARD));

        // Order confirmed, total = 42.50 * 2 = 85.00
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("85.00");
        assertThat(response.getSimulatedPaymentReference()).startsWith("SIM-");
        // Unit price locked at purchase (BR-06)
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getUnitPrice()).isEqualByComparingTo("42.50");
        // Address snapshot (DM-03)
        assertThat(response.getShippingAddress().getCity()).isEqualTo("Austin");
        // Stock decremented 8 -> 6 (BR-05)
        assertThat(book.getStockQuantity()).isEqualTo(6);
        verify(bookRepository).save(book);
        // Cart cleared (DM-06)
        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Checkout with an empty cart throws a business-rule error (422)")
    void checkoutEmptyCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart)); // no items

        assertThatThrownBy(() -> orderService.placeOrder(
                new PlaceOrderRequest(1L, PaymentMethod.CREDIT_CARD)))
                .isInstanceOf(BusinessRuleException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Checkout with an unknown address throws NOT_FOUND (404)")
    void checkoutUnknownAddress() {
        CartItem item = TestEntities.cartItem(55L, cart, book, 1);
        seedCartWith(item);
        when(addressRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(
                new PlaceOrderRequest(999L, PaymentMethod.CREDIT_CARD)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Checkout with an address owned by another customer is rejected (422)")
    void checkoutAddressNotOwned() {
        CartItem item = TestEntities.cartItem(55L, cart, book, 1);
        seedCartWith(item);
        Address foreign = TestEntities.address(5L, TestEntities.user(2L, "other@bookstore.com"), "Dallas");
        when(addressRepository.findById(5L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> orderService.placeOrder(
                new PlaceOrderRequest(5L, PaymentMethod.CREDIT_CARD)))
                .isInstanceOf(BusinessRuleException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Checkout with insufficient stock throws INSUFFICIENT_STOCK (422), no order persisted")
    void checkoutInsufficientStock() {
        book.setStockQuantity(1);
        CartItem item = TestEntities.cartItem(55L, cart, book, 5);
        seedCartWith(item);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> orderService.placeOrder(
                new PlaceOrderRequest(1L, PaymentMethod.CREDIT_CARD)))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).getCode())
                        .isEqualTo("INSUFFICIENT_STOCK"));
        verify(orderRepository, never()).save(any());
        assertThat(book.getStockQuantity()).isEqualTo(1); // unchanged
    }

    @Test
    @DisplayName("Checkout fails with PAYMENT_DECLINED (422) when payment is declined; no order persisted")
    void checkoutPaymentDeclined() {
        // A free book (price 0.00) makes the total 0.00, which the simulator declines.
        Book freeBook = TestEntities.book(6L, "Freebie", "Nobody", "0.00", 5);
        CartItem item = TestEntities.cartItem(60L, cart, freeBook, 1);
        seedCartWith(item);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> orderService.placeOrder(
                new PlaceOrderRequest(1L, PaymentMethod.CREDIT_CARD)))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).getCode())
                        .isEqualTo("PAYMENT_DECLINED"));
        verify(orderRepository, never()).save(any());
        assertThat(freeBook.getStockQuantity()).isEqualTo(5); // stock untouched
    }

    // ------------------------------------------------------------ cancellation

    @Test
    @DisplayName("Cancelling a recent CONFIRMED order restores stock and sets CANCELLED")
    void cancelWithinWindow() {
        Order order = confirmedOrder(book, 2, OffsetDateTime.now().minusHours(1));
        book.setStockQuantity(6);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.cancelOrder(100L);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        // Stock restored 6 -> 8
        assertThat(book.getStockQuantity()).isEqualTo(8);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("Cancelling after the 48-hour window throws CANCELLATION_WINDOW_EXPIRED (422)")
    void cancelAfterWindow() {
        Order order = confirmedOrder(book, 2, OffsetDateTime.now().minusHours(49));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(100L))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).getCode())
                        .isEqualTo("CANCELLATION_WINDOW_EXPIRED"));
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cancelling an already-cancelled order throws ORDER_ALREADY_CANCELLED (422)")
    void cancelAlreadyCancelled() {
        Order order = confirmedOrder(book, 2, OffsetDateTime.now().minusHours(1));
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(100L))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).getCode())
                        .isEqualTo("ORDER_ALREADY_CANCELLED"));
    }

    @Test
    @DisplayName("Cancelling an unknown order throws NOT_FOUND (404)")
    void cancelUnknownOrder() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Cancelling another customer's order is denied (403)")
    void cancelAnotherUsersOrder() {
        Order order = confirmedOrder(book, 2, OffsetDateTime.now().minusHours(1));
        order.setUser(TestEntities.user(2L, "other@bookstore.com"));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(100L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Fetching another customer's order detail is denied (403)")
    void getAnotherUsersOrder() {
        Order order = confirmedOrder(book, 1, OffsetDateTime.now());
        order.setUser(TestEntities.user(2L, "other@bookstore.com"));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrder(100L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // --------------------------------------------------------------- helpers

    private Order confirmedOrder(Book book, int qty, OffsetDateTime createdAt) {
        Order order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(book.getPrice().multiply(BigDecimal.valueOf(qty)));
        order.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        order.setSimulatedPaymentReference("SIM-TESTREF1");
        order.setSnapRecipient("Demo Customer");
        order.setSnapStreetLine1("123 Main Street");
        order.setSnapCity("Austin");
        order.setSnapState("TX");
        order.setSnapPostalCode("78701");
        order.setSnapCountry("US");
        order.setCreatedAt(createdAt);
        order.setUpdatedAt(createdAt);

        OrderItem oi = new OrderItem();
        oi.setId(200L);
        oi.setOrder(order);
        oi.setBook(book);
        oi.setQuantity(qty);
        oi.setUnitPrice(book.getPrice());
        order.setItems(List.of(oi));
        return order;
    }
}
