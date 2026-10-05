package com.bookstore.cart;

import com.bookstore.cart.dto.AddToCartRequest;
import com.bookstore.cart.dto.CartResponse;
import com.bookstore.cart.dto.UpdateCartItemRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Shopping cart endpoints (authenticated; own cart only).
 *
 * Implements:
 *   GET    /api/cart                 — get the current cart
 *   POST   /api/cart/items           — add a book (increments if present)
 *   PUT    /api/cart/items/{itemId}  — update item quantity
 *   DELETE /api/cart/items/{itemId}  — remove an item
 *
 * All operations return the full updated cart (200 OK).
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addItem(request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(itemId));
    }
}
