package com.bookstore.cart;

import com.bookstore.cart.dto.AddToCartRequest;
import com.bookstore.cart.dto.CartResponse;
import com.bookstore.cart.dto.UpdateCartItemRequest;
import com.bookstore.catalogue.Book;
import com.bookstore.catalogue.BookRepository;
import com.bookstore.common.exception.BusinessRuleException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.user.CurrentUserService;
import com.bookstore.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shopping cart service (FR-03).
 *
 * Rules enforced here:
 * - One reusable cart per customer; created automatically on first use (DM-06, BR-02).
 * - No duplicate books in a cart; adding an existing book increments quantity (DM-07, BR-03).
 * - Requested quantity must not exceed available book stock (BR-03).
 * - All operations are scoped to the authenticated customer's own cart.
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final CurrentUserService currentUserService;
    private final CartMapper mapper;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       BookRepository bookRepository,
                       CurrentUserService currentUserService,
                       CartMapper mapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.currentUserService = currentUserService;
        this.mapper = mapper;
    }

    @Transactional
    public CartResponse getCart() {
        Cart cart = getOrCreateCart();
        return mapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(AddToCartRequest request) {
        Cart cart = getOrCreateCart();

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", request.getBookId()));

        CartItem existing = cartItemRepository
                .findByCartIdAndBookId(cart.getId(), book.getId())
                .orElse(null);

        int newQuantity = (existing != null)
                ? existing.getQuantity() + request.getQuantity()
                : request.getQuantity();

        requireSufficientStock(book, newQuantity);

        if (existing != null) {
            existing.setQuantity(newQuantity);
            cartItemRepository.save(existing);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setBook(book);
            item.setQuantity(request.getQuantity());
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        touchCart(cart);
        return mapper.toCartResponse(reloadCart(cart.getId()));
    }

    @Transactional
    public CartResponse updateItem(Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart();

        CartItem item = findItemInCart(cart, itemId);
        requireSufficientStock(item.getBook(), request.getQuantity());

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        touchCart(cart);
        return mapper.toCartResponse(reloadCart(cart.getId()));
    }

    @Transactional
    public CartResponse removeItem(Long itemId) {
        Cart cart = getOrCreateCart();

        CartItem item = findItemInCart(cart, itemId);
        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        touchCart(cart);
        return mapper.toCartResponse(reloadCart(cart.getId()));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Cart getOrCreateCart() {
        User user = currentUserService.requireCurrentUser();
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    private Cart reloadCart(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));
    }

    private CartItem findItemInCart(Cart cart, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));
        // Ensure the item belongs to the authenticated customer's cart
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Cart item", itemId);
        }
        return item;
    }

    private void requireSufficientStock(Book book, int requestedQuantity) {
        if (requestedQuantity > book.getStockQuantity()) {
            throw new BusinessRuleException(
                    "INSUFFICIENT_STOCK",
                    "Insufficient stock for '" + book.getTitle()
                            + "'. Requested: " + requestedQuantity
                            + ", available: " + book.getStockQuantity());
        }
    }

    /** Trigger the cart's @PreUpdate so updated_at reflects the item change. */
    private void touchCart(Cart cart) {
        cart.setUpdatedAt(cart.getUpdatedAt());
        cartRepository.save(cart);
    }
}
