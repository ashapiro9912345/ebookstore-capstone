package com.bookstore.cart;

import com.bookstore.cart.dto.CartItemResponse;
import com.bookstore.cart.dto.CartResponse;
import com.bookstore.catalogue.Book;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Hand-written mapper from Cart/CartItem entities to response DTOs.
 *
 * Computes lineTotal (price × quantity) and the cart totalAmount using BigDecimal.
 */
@Component
public class CartMapper {

    public CartItemResponse toItemResponse(CartItem item) {
        Book book = item.getBook();
        BigDecimal lineTotal = book.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                item.getQuantity(),
                lineTotal);
    }

    public CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, totalAmount);
    }
}
