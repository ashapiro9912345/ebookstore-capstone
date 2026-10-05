package com.bookstore.support;

import com.bookstore.address.Address;
import com.bookstore.address.AddressRepository;
import com.bookstore.catalogue.Book;
import com.bookstore.catalogue.BookRepository;
import com.bookstore.catalogue.Brand;
import com.bookstore.catalogue.BrandRepository;
import com.bookstore.catalogue.Category;
import com.bookstore.catalogue.CategoryRepository;
import com.bookstore.cart.CartItemRepository;
import com.bookstore.cart.CartRepository;
import com.bookstore.order.OrderRepository;
import com.bookstore.user.User;
import com.bookstore.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

/**
 * Base class for full-stack integration tests.
 *
 * Runs the real Spring context against the LOCAL PostgreSQL `ebookstore_test`
 * database (never H2 — AutoConfigureTestDatabase.Replace.NONE keeps the
 * configured datasource). Each test starts from a clean, deterministic seed:
 * the data is wiped and re-created in {@link #seedDatabase()} before every test,
 * giving a repeatable starting state.
 *
 * Seeded data mirrors the demo seed used in Phase 5:
 *   - demo customer: demo@bookstore.com / demo1234
 *   - a second customer: other@bookstore.com / other1234 (for ownership tests)
 *   - 6 books across 3 categories / 3 brands (one book has no brand)
 *   - one saved address for the demo customer
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    public static final String DEMO_EMAIL = "demo@bookstore.com";
    public static final String DEMO_PASSWORD = "demo1234";
    public static final String OTHER_EMAIL = "other@bookstore.com";
    public static final String OTHER_PASSWORD = "other1234";

    @Autowired protected UserRepository userRepository;
    @Autowired protected CategoryRepository categoryRepository;
    @Autowired protected BrandRepository brandRepository;
    @Autowired protected BookRepository bookRepository;
    @Autowired protected AddressRepository addressRepository;
    @Autowired protected CartRepository cartRepository;
    @Autowired protected CartItemRepository cartItemRepository;
    @Autowired protected OrderRepository orderRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    // Convenience handles populated by the seed
    protected User demoUser;
    protected User otherUser;
    protected Address demoAddress;
    protected Book cleanCode;   // price 42.50, stock 8
    protected Book foundation;  // price 16.50, stock 5, no brand

    @BeforeEach
    void seedDatabase() {
        // Clean in FK-safe order
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        bookRepository.deleteAll();
        brandRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        Category tech = category("Technology", "Software, programming, and computing");
        Category scifi = category("Science Fiction", "Speculative fiction");
        Category business = category("Business", "Management and economics");

        Brand aw = brand("Addison-Wesley");
        Brand oreilly = brand("O'Reilly Media");
        Brand penguin = brand("Penguin Books");

        book("The Pragmatic Programmer", "David Thomas, Andrew Hunt", "9780135957059",
                "39.99", 15, 3, tech, aw);
        cleanCode = book("Clean Code", "Robert C. Martin", "9780132350884",
                "42.50", 8, 4, tech, aw);
        book("Designing Data-Intensive Applications", "Martin Kleppmann", "9781449373320",
                "55.00", 12, 5, tech, oreilly);
        book("Dune", "Frank Herbert", "9780441013593",
                "18.99", 20, 2, scifi, penguin);
        book("The Lean Startup", "Eric Ries", "9780307887894",
                "24.00", 10, 4, business, penguin);
        foundation = book("Foundation", "Isaac Asimov", "9780553293357",
                "16.50", 5, 2, scifi, null);

        demoUser = user("Demo Customer", DEMO_EMAIL, DEMO_PASSWORD);
        otherUser = user("Other Customer", OTHER_EMAIL, OTHER_PASSWORD);

        demoAddress = address(demoUser, "Demo Customer", "123 Main Street", "Apt 4B",
                "Austin", "TX", "78701", "US");
    }

    // --- seed helpers ---

    private Category category(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        return categoryRepository.save(c);
    }

    private Brand brand(String name) {
        Brand b = new Brand();
        b.setName(name);
        return brandRepository.save(b);
    }

    private Book book(String title, String author, String isbn, String price,
                      int stock, int deliveryDays, Category category, Brand brand) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setIsbn(isbn);
        b.setDescription(title + " description");
        b.setPrice(new BigDecimal(price));
        b.setStockQuantity(stock);
        b.setDeliveryEstimateDays(deliveryDays);
        b.setCategory(category);
        b.setBrand(brand);
        return bookRepository.save(b);
    }

    private User user(String fullName, String email, String rawPassword) {
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(u);
    }

    private Address address(User owner, String recipient, String line1, String line2,
                            String city, String state, String postal, String country) {
        Address a = new Address();
        a.setUser(owner);
        a.setRecipientName(recipient);
        a.setStreetLine1(line1);
        a.setStreetLine2(line2);
        a.setCity(city);
        a.setState(state);
        a.setPostalCode(postal);
        a.setCountry(country);
        return addressRepository.save(a);
    }
}
