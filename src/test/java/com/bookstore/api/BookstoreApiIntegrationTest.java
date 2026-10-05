package com.bookstore.api;

import com.bookstore.cart.Cart;
import com.bookstore.cart.CartItem;
import com.bookstore.catalogue.Book;
import com.bookstore.order.Order;
import com.bookstore.order.OrderItem;
import com.bookstore.order.OrderStatus;
import com.bookstore.order.PaymentMethod;
import com.bookstore.support.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack API integration tests against the live Spring context and the
 * local PostgreSQL `ebookstore_test` database.
 *
 * These exercise the real controller → service → repository → PostgreSQL path,
 * including JWT security, the structured error contract, and persisted side
 * effects (stock decrement/restore, cart clearing, order creation).
 */
class BookstoreApiIntegrationTest extends IntegrationTestBase {

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    private MockMvc mvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(context)
                    .apply(springSecurity())
                    .build();
        }
        return mockMvc;
    }

    // Logs in and returns a Bearer token value.
    private String login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}""".formatted(email, password);
        MvcResult result = mvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    // =======================================================================
    // Authentication
    // =======================================================================

    @Test
    @DisplayName("Login with valid credentials returns 200 + JWT + user summary")
    void loginSuccess() throws Exception {
        mvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}""".formatted(DEMO_EMAIL, DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(DEMO_EMAIL))
                .andExpect(jsonPath("$.user.fullName").value("Demo Customer"));
    }

    @Test
    @DisplayName("Login with a wrong password returns 401 INVALID_CREDENTIALS")
    void loginInvalidPassword() throws Exception {
        mvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"wrong"}""".formatted(DEMO_EMAIL)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Login with a malformed email returns 400 VALIDATION_ERROR")
    void loginValidationError() throws Exception {
        mvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray());
    }

    // =======================================================================
    // Catalogue (public)
    // =======================================================================

    @Test
    @DisplayName("GET /categories returns the 3 seeded categories (public, no token)")
    void listCategories() throws Exception {
        mvc().perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("GET /books returns a paginated catalogue of all 6 books")
    void listBooks() throws Exception {
        mvc().perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /books?title=clean filters to Clean Code only")
    void searchBooksByTitle() throws Exception {
        mvc().perform(get("/api/books").param("title", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /books?authorName=asimov filters by author")
    void searchBooksByAuthor() throws Exception {
        mvc().perform(get("/api/books").param("authorName", "asimov"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Foundation"));
    }

    @Test
    @DisplayName("GET /books/{id} returns full detail including category")
    void getBookDetail() throws Exception {
        mvc().perform(get("/api/books/{id}", cleanCode.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.price").value(42.50))
                .andExpect(jsonPath("$.category.name").value("Technology"));
    }

    @Test
    @DisplayName("GET /books/{id} for a book with no brand returns null brand")
    void getBookDetailNoBrand() throws Exception {
        mvc().perform(get("/api/books/{id}", foundation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Foundation"))
                .andExpect(jsonPath("$.brand").doesNotExist());
    }

    @Test
    @DisplayName("GET /books/{id} for an unknown id returns 404 NOT_FOUND")
    void getUnknownBook() throws Exception {
        mvc().perform(get("/api/books/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // =======================================================================
    // Security boundaries
    // =======================================================================

    @Test
    @DisplayName("Protected endpoint without a token returns 401 UNAUTHORIZED (structured)")
    void protectedWithoutToken() throws Exception {
        mvc().perform(get("/api/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Protected endpoint with an invalid token returns 401 UNAUTHORIZED")
    void protectedWithInvalidToken() throws Exception {
        mvc().perform(get("/api/cart").header("Authorization", "Bearer not.a.valid.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    // =======================================================================
    // Cart workflow
    // =======================================================================

    @Test
    @DisplayName("Full cart workflow: empty → add → re-add (increment) → update → remove")
    void cartWorkflow() throws Exception {
        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        Long bookId = cleanCode.getId();

        // Empty cart
        mvc().perform(get("/api/cart").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0));

        // Add 1
        mvc().perform(post("/api/cart/items").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":%d,"quantity":1}""".formatted(bookId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.totalAmount").value(42.50));

        // Re-add 2 of the same book → quantity becomes 3 (DM-07), still one line
        mvc().perform(post("/api/cart/items").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":%d,"quantity":2}""".formatted(bookId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.totalAmount").value(127.50));

        // Find the item id and update quantity to 2
        MvcResult cartResult = mvc().perform(get("/api/cart").header("Authorization", bearer(token)))
                .andReturn();
        long itemId = objectMapper.readTree(cartResult.getResponse().getContentAsString())
                .get("items").get(0).get("itemId").asLong();

        mvc().perform(put("/api/cart/items/{id}", itemId).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":2}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(85.00));

        // Remove the item → empty cart
        mvc().perform(delete("/api/cart/items/{id}", itemId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    @DisplayName("Adding more than available stock returns 422 INSUFFICIENT_STOCK")
    void addItemInsufficientStock() throws Exception {
        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        // Foundation has stock 5
        mvc().perform(post("/api/cart/items").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":%d,"quantity":99}""".formatted(foundation.getId())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }

    @Test
    @DisplayName("Adding an unknown book to the cart returns 404 NOT_FOUND")
    void addUnknownBookToCart() throws Exception {
        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        mvc().perform(post("/api/cart/items").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":999999,"quantity":1}"""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("Add-to-cart with quantity 0 fails validation (400)")
    void addItemValidationError() throws Exception {
        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        mvc().perform(post("/api/cart/items").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":%d,"quantity":0}""".formatted(cleanCode.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // =======================================================================
    // Addresses
    // =======================================================================

    @Test
    @DisplayName("GET /addresses lists the customer's saved addresses")
    void listAddresses() throws Exception {
        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        mvc().perform(get("/api/addresses").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].city").value("Austin"));
    }

    @Test
    @DisplayName("POST /addresses creates a new address (201) and persists it")
    void createAddress() throws Exception {
        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        mvc().perform(post("/api/addresses").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientName":"Jane Smith","streetLine1":"456 Oak Ave",
                                 "city":"Dallas","state":"TX","postalCode":"75201","country":"US"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.city").value("Dallas"));

        assertThat(addressRepository.findByUserId(demoUser.getId())).hasSize(2);
    }

    @Test
    @DisplayName("POST /addresses with a missing required field fails validation (400)")
    void createAddressValidationError() throws Exception {
        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        mvc().perform(post("/api/addresses").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientName":"","streetLine1":"456 Oak Ave",
                                 "city":"Dallas","state":"TX","postalCode":"75201","country":"US"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // =======================================================================
    // End-to-end checkout journey + persistence side effects
    // =======================================================================

    @Test
    @DisplayName("E2E: login → add to cart → checkout → confirmation → history → detail → cancel")
    void endToEndJourney() throws Exception {
        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        Long bookId = cleanCode.getId();
        int startStock = cleanCode.getStockQuantity(); // 8

        // Add 2 x Clean Code
        mvc().perform(post("/api/cart/items").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":%d,"quantity":2}""".formatted(bookId)))
                .andExpect(status().isOk());

        // Checkout → 201 CONFIRMED
        MvcResult orderResult = mvc().perform(post("/api/orders").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"addressId":%d,"paymentMethod":"CREDIT_CARD"}""".formatted(demoAddress.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(85.00))
                .andExpect(jsonPath("$.simulatedPaymentReference").value(org.hamcrest.Matchers.startsWith("SIM-")))
                .andExpect(jsonPath("$.shippingAddress.city").value("Austin"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(42.50))
                .andReturn();
        long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Persistence side effects: stock decremented, cart cleared
        assertThat(bookRepository.findById(bookId).orElseThrow().getStockQuantity())
                .isEqualTo(startStock - 2);
        Cart cart = cartRepository.findByUserId(demoUser.getId()).orElseThrow();
        assertThat(cartItemRepository.findByCartIdAndBookId(cart.getId(), bookId)).isEmpty();
        Order persisted = orderRepository.findById(orderId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        // Verify the locked unit price persisted to order_items (BR-06)
        java.math.BigDecimal persistedUnitPrice = jdbcTemplate.queryForObject(
                "SELECT unit_price FROM order_items WHERE order_id = ?",
                java.math.BigDecimal.class, orderId);
        assertThat(persistedUnitPrice).isEqualByComparingTo("42.50");

        // Order history
        mvc().perform(get("/api/orders").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value((int) orderId));

        // Order detail
        mvc().perform(get("/api/orders/{id}", orderId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) orderId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Cancel within 48h → stock restored
        mvc().perform(post("/api/orders/{id}/cancel", orderId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        assertThat(bookRepository.findById(bookId).orElseThrow().getStockQuantity())
                .isEqualTo(startStock); // restored to 8
    }

    @Test
    @DisplayName("Checkout with an empty cart returns 422")
    void checkoutEmptyCart() throws Exception {
        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        mvc().perform(post("/api/orders").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"addressId":%d,"paymentMethod":"CREDIT_CARD"}""".formatted(demoAddress.getId())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CART_EMPTY"));
    }

    @Test
    @DisplayName("Checkout against an unknown address returns 404 NOT_FOUND")
    void checkoutUnknownAddress() throws Exception {
        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        // Put something in the cart first so empty-cart isn't the failing rule
        mvc().perform(post("/api/cart/items").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":%d,"quantity":1}""".formatted(cleanCode.getId())))
                .andExpect(status().isOk());

        mvc().perform(post("/api/orders").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"addressId":999999,"paymentMethod":"CREDIT_CARD"}"""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("Checkout using another customer's address is rejected (422)")
    void checkoutForeignAddress() throws Exception {
        // Create an address for the other user
        var foreign = new com.bookstore.address.Address();
        foreign.setUser(otherUser);
        foreign.setRecipientName("Other");
        foreign.setStreetLine1("9 Other St");
        foreign.setCity("Houston");
        foreign.setState("TX");
        foreign.setPostalCode("77001");
        foreign.setCountry("US");
        foreign = addressRepository.save(foreign);

        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        mvc().perform(post("/api/cart/items").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":%d,"quantity":1}""".formatted(cleanCode.getId())))
                .andExpect(status().isOk());

        mvc().perform(post("/api/orders").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"addressId":%d,"paymentMethod":"CREDIT_CARD"}""".formatted(foreign.getId())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // =======================================================================
    // Order ownership / cancellation negative cases (persisted orders)
    // =======================================================================

    @Test
    @DisplayName("Accessing another customer's order returns 403 FORBIDDEN")
    void accessAnotherCustomersOrder() throws Exception {
        Order order = persistConfirmedOrder(otherUser, cleanCode, 1, OffsetDateTime.now());

        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        mvc().perform(get("/api/orders/{id}", order.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("Cancelling an unknown order returns 404 NOT_FOUND")
    void cancelUnknownOrder() throws Exception {
        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        mvc().perform(post("/api/orders/{id}/cancel", 999999).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("Cancelling an already-cancelled order returns 422 ORDER_ALREADY_CANCELLED")
    void cancelAlreadyCancelled() throws Exception {
        Order order = persistConfirmedOrder(demoUser, cleanCode, 1, OffsetDateTime.now());
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        mvc().perform(post("/api/orders/{id}/cancel", order.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ORDER_ALREADY_CANCELLED"));
    }

    @Test
    @DisplayName("Cancelling an order older than 48h returns 422 CANCELLATION_WINDOW_EXPIRED")
    void cancelExpiredWindow() throws Exception {
        Order order = persistConfirmedOrder(demoUser, cleanCode, 1, OffsetDateTime.now().minusHours(49));

        String token = login(DEMO_EMAIL, DEMO_PASSWORD);
        mvc().perform(post("/api/orders/{id}/cancel", order.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CANCELLATION_WINDOW_EXPIRED"));
    }

    // --- helper to persist a confirmed order directly (bypasses checkout) ---

    private Order persistConfirmedOrder(com.bookstore.user.User owner, Book book,
                                        int qty, OffsetDateTime createdAt) {
        Order order = new Order();
        order.setUser(owner);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(book.getPrice().multiply(java.math.BigDecimal.valueOf(qty)));
        order.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        order.setSimulatedPaymentReference("SIM-SEEDED01");
        order.setSnapRecipient("Recipient");
        order.setSnapStreetLine1("1 St");
        order.setSnapCity("Austin");
        order.setSnapState("TX");
        order.setSnapPostalCode("78701");
        order.setSnapCountry("US");

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setBook(book);
        item.setQuantity(qty);
        item.setUnitPrice(book.getPrice());
        order.setItems(List.of(item));

        Order saved = orderRepository.saveAndFlush(order);
        // created_at is set by @PrePersist and is non-updatable via JPA, so set
        // the desired timestamp directly in the DB for cancellation-window tests.
        jdbcTemplate.update("UPDATE orders SET created_at = ? WHERE id = ?",
                java.sql.Timestamp.from(createdAt.toInstant()), saved.getId());
        return orderRepository.findById(saved.getId()).orElseThrow();
    }
}
