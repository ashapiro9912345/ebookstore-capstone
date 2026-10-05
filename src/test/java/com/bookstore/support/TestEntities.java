package com.bookstore.support;

import com.bookstore.address.Address;
import com.bookstore.catalogue.Book;
import com.bookstore.catalogue.Category;
import com.bookstore.cart.Cart;
import com.bookstore.cart.CartItem;
import com.bookstore.user.User;

import java.math.BigDecimal;

/**
 * Small factory helpers for building in-memory entities in unit tests.
 * Keeps the Mockito-based service tests readable and consistent.
 */
public final class TestEntities {

    private TestEntities() {}

    public static User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setFullName("Test User " + id);
        u.setEmail(email);
        u.setPasswordHash("$2a$10$hashhashhashhashhashhashhashhashhashhashhashhashhashhh");
        return u;
    }

    public static Category category(Long id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        return c;
    }

    public static Book book(Long id, String title, String author, String price, int stock) {
        Book b = new Book();
        b.setId(id);
        b.setTitle(title);
        b.setAuthor(author);
        b.setPrice(new BigDecimal(price));
        b.setStockQuantity(stock);
        b.setDeliveryEstimateDays(3);
        b.setCategory(category(1L, "Technology"));
        return b;
    }

    public static Cart cart(Long id, User owner) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setUser(owner);
        return cart;
    }

    public static CartItem cartItem(Long id, Cart cart, Book book, int quantity) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setCart(cart);
        item.setBook(book);
        item.setQuantity(quantity);
        return item;
    }

    public static Address address(Long id, User owner, String city) {
        Address a = new Address();
        a.setId(id);
        a.setUser(owner);
        a.setRecipientName("Recipient " + id);
        a.setStreetLine1("1 Test Street");
        a.setStreetLine2(null);
        a.setCity(city);
        a.setState("TX");
        a.setPostalCode("78701");
        a.setCountry("US");
        return a;
    }
}
