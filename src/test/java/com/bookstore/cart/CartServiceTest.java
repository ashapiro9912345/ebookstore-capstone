package com.bookstore.cart;

import com.bookstore.cart.dto.AddToCartRequest;
import com.bookstore.cart.dto.CartResponse;
import com.bookstore.cart.dto.UpdateCartItemRequest;
import com.bookstore.catalogue.Book;
import com.bookstore.catalogue.BookRepository;
import com.bookstore.common.exception.BusinessRuleException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.support.TestEntities;
import com.bookstore.user.CurrentUserService;
import com.bookstore.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for cart business logic (FR-03, DM-06, DM-07, BR-03).
 * All collaborators are mocked; no database involved.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private BookRepository bookRepository;
    @Mock private CurrentUserService currentUserService;

    private final CartMapper mapper = new CartMapper();
    private CartService cartService;

    private User user;
    private Cart cart;
    private Book book;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, cartItemRepository,
                bookRepository, currentUserService, mapper);
        user = TestEntities.user(1L, "demo@bookstore.com");
        cart = TestEntities.cart(10L, user);
        book = TestEntities.book(2L, "Clean Code", "Robert C. Martin", "42.50", 8);

        lenient().when(currentUserService.requireCurrentUser()).thenReturn(user);
        lenient().when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        lenient().when(cartRepository.findById(10L)).thenReturn(Optional.of(cart));
    }

    @Test
    @DisplayName("Adding a new book creates a cart item with the requested quantity")
    void addNewItem() {
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(cartItemRepository.findByCartIdAndBookId(10L, 2L)).thenReturn(Optional.empty());

        CartResponse response = cartService.addItem(new AddToCartRequest(2L, 2));

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(2);
        assertThat(captor.getValue().getBook()).isEqualTo(book);
        assertThat(response.getCartId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Re-adding an existing book increments the quantity (DM-07) rather than duplicating")
    void reAddIncrementsQuantity() {
        CartItem existing = TestEntities.cartItem(55L, cart, book, 3);
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(cartItemRepository.findByCartIdAndBookId(10L, 2L)).thenReturn(Optional.of(existing));

        cartService.addItem(new AddToCartRequest(2L, 2));

        // existing 3 + requested 2 = 5
        assertThat(existing.getQuantity()).isEqualTo(5);
        verify(cartItemRepository).save(existing);
    }

    @Test
    @DisplayName("Adding beyond available stock throws INSUFFICIENT_STOCK (422)")
    void addInsufficientStock() {
        book.setStockQuantity(1);
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(cartItemRepository.findByCartIdAndBookId(10L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(new AddToCartRequest(2L, 5)))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).getCode())
                        .isEqualTo("INSUFFICIENT_STOCK"));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Adding an unknown book throws NOT_FOUND (404)")
    void addUnknownBook() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(new AddToCartRequest(999L, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Updating a cart item changes its quantity")
    void updateItemQuantity() {
        CartItem existing = TestEntities.cartItem(55L, cart, book, 1);
        when(cartItemRepository.findById(55L)).thenReturn(Optional.of(existing));

        cartService.updateItem(55L, new UpdateCartItemRequest(4));

        assertThat(existing.getQuantity()).isEqualTo(4);
        verify(cartItemRepository).save(existing);
    }

    @Test
    @DisplayName("Updating a cart item beyond stock throws INSUFFICIENT_STOCK (422)")
    void updateItemInsufficientStock() {
        book.setStockQuantity(2);
        CartItem existing = TestEntities.cartItem(55L, cart, book, 1);
        when(cartItemRepository.findById(55L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> cartService.updateItem(55L, new UpdateCartItemRequest(10)))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).getCode())
                        .isEqualTo("INSUFFICIENT_STOCK"));
    }

    @Test
    @DisplayName("Removing a cart item deletes it from the cart")
    void removeItem() {
        CartItem existing = TestEntities.cartItem(55L, cart, book, 1);
        cart.getItems().add(existing);
        when(cartItemRepository.findById(55L)).thenReturn(Optional.of(existing));

        cartService.removeItem(55L);

        verify(cartItemRepository).delete(existing);
        assertThat(cart.getItems()).doesNotContain(existing);
    }

    @Test
    @DisplayName("Operating on a cart item that belongs to another cart throws NOT_FOUND (404)")
    void itemFromAnotherCartNotFound() {
        Cart otherCart = TestEntities.cart(99L, TestEntities.user(2L, "other@bookstore.com"));
        CartItem foreignItem = TestEntities.cartItem(77L, otherCart, book, 1);
        when(cartItemRepository.findById(77L)).thenReturn(Optional.of(foreignItem));

        assertThatThrownBy(() -> cartService.updateItem(77L, new UpdateCartItemRequest(1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Getting a cart for a first-time user creates one automatically (DM-06)")
    void getCartCreatesWhenMissing() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        Cart created = TestEntities.cart(20L, user);
        when(cartRepository.save(any(Cart.class))).thenReturn(created);

        CartResponse response = cartService.getCart();

        assertThat(response.getCartId()).isEqualTo(20L);
        verify(cartRepository).save(any(Cart.class));
    }
}
