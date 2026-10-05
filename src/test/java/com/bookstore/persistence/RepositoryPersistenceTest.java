package com.bookstore.persistence;

import com.bookstore.catalogue.Book;
import com.bookstore.catalogue.BookRepository;
import com.bookstore.catalogue.Brand;
import com.bookstore.catalogue.BrandRepository;
import com.bookstore.catalogue.Category;
import com.bookstore.catalogue.CategoryRepository;
import com.bookstore.cart.Cart;
import com.bookstore.cart.CartItem;
import com.bookstore.cart.CartItemRepository;
import com.bookstore.cart.CartRepository;
import com.bookstore.user.User;
import com.bookstore.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository/persistence integration tests running against the real local
 * PostgreSQL `ebookstore_test` database (Replace.NONE — not H2).
 *
 * Validates that the custom queries, constraints, and mappings behave against
 * PostgreSQL — in particular the case-insensitive search (which previously
 * failed with lower(bytea) when parameters were untyped) and the
 * unique (cart_id, book_id) constraint (DM-07).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RepositoryPersistenceTest {

    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private com.bookstore.order.OrderRepository orderRepository;
    @Autowired private com.bookstore.address.AddressRepository addressRepository;
    @Autowired private jakarta.persistence.EntityManager entityManager;

    /**
     * Start each test from an empty schema. @DataJpaTest wraps the test in a
     * transaction that is rolled back afterwards, so this cleanup (and all test
     * writes) never persist. It removes any rows committed by other
     * (non-transactional) integration tests that ran earlier against the same
     * local database, keeping these tests order-independent. A single native
     * TRUNCATE ... CASCADE handles all FK dependencies in one step.
     */
    @org.junit.jupiter.api.BeforeEach
    void cleanDatabase() {
        entityManager.createNativeQuery(
                "TRUNCATE TABLE order_items, orders, cart_items, carts, addresses, "
                        + "books, brands, categories, users RESTART IDENTITY CASCADE")
                .executeUpdate();
    }

    private Category newCategory(String name) {
        Category c = new Category();
        c.setName(name);
        return categoryRepository.save(c);
    }

    private Book newBook(String title, String author, String price, int stock, Category category) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setPrice(new BigDecimal(price));
        b.setStockQuantity(stock);
        b.setDeliveryEstimateDays(3);
        b.setCategory(category);
        return bookRepository.save(b);
    }

    @Test
    @DisplayName("Book search with null filters returns all books (no lower(bytea) error on PostgreSQL)")
    void searchWithNullFiltersReturnsAll() {
        Category tech = newCategory("Technology");
        newBook("Clean Code", "Robert C. Martin", "42.50", 8, tech);
        newBook("Dune", "Frank Herbert", "18.99", 20, tech);

        Page<Book> result = bookRepository.search(null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Book search is case-insensitive partial match on title")
    void searchByTitleCaseInsensitive() {
        Category tech = newCategory("Technology");
        newBook("Clean Code", "Robert C. Martin", "42.50", 8, tech);
        newBook("Dune", "Frank Herbert", "18.99", 20, tech);

        Page<Book> result = bookRepository.search("CLEAN", null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("Book price persists as NUMERIC(10,2) and reads back with 2 decimals")
    void priceScalePersists() {
        Category tech = newCategory("Technology");
        Book saved = newBook("Clean Code", "Robert C. Martin", "42.50", 8, tech);

        Book reloaded = bookRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getPrice()).isEqualByComparingTo("42.50");
        assertThat(reloaded.getPrice().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Duplicate (cart_id, book_id) violates the unique constraint (DM-07)")
    void duplicateCartItemRejected() {
        User user = new User();
        user.setFullName("Dup Tester");
        user.setEmail("dup@bookstore.com");
        user.setPasswordHash("hash");
        user = userRepository.save(user);

        Category tech = newCategory("Technology");
        Book book = newBook("Clean Code", "Robert C. Martin", "42.50", 8, tech);

        Cart cart = new Cart();
        cart.setUser(user);
        cart = cartRepository.save(cart);

        CartItem first = new CartItem();
        first.setCart(cart);
        first.setBook(book);
        first.setQuantity(1);
        cartItemRepository.saveAndFlush(first);

        CartItem duplicate = new CartItem();
        duplicate.setCart(cart);
        duplicate.setBook(book);
        duplicate.setQuantity(2);

        assertThatThrownBy(() -> cartItemRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
