# E-Bookstore Backend Requirements

**Source:** AI Specialist Capstone Instructions (AI Specialist - Cloud FullStack - Capstone instructions.pptx.pdf)  
**Governing Standards:** Kiro Project Instructions  
**Phase:** 1 — Requirements Analysis  

---

## 1. Functional Requirements

### FR-01 — User Authentication
- FR-01.1 A customer can register a new account with name, email, and password.
- FR-01.2 A customer can log in using email and password.
- FR-01.3 A customer can log out.
- FR-01.4 Authenticated sessions must be maintained for the duration of a shopping workflow.

### FR-02 — Product Catalogue
- FR-02.1 The system shall display a landing/home page showing available books.
- FR-02.2 Books are organised into categories; the customer can browse by category.
- FR-02.3 Each book listing shall show title, author, price, and tentative delivery date.
- FR-02.4 The customer can browse books by brand/publisher.
- FR-02.5 The customer can select an individual book to view its detail.
- FR-02.6 The customer can search and filter the product catalogue.

### FR-03 — Shopping Cart
- FR-03.1 An authenticated customer can add one or more books to a cart.
- FR-03.2 The customer can view the contents of their cart.
- FR-03.3 The customer can update item quantities in the cart.
- FR-03.4 The customer can remove items from the cart.
- FR-03.5 The cart persists between sessions for the same authenticated customer.

### FR-04 — Delivery Address
- FR-04.1 The customer can add one or more delivery addresses to their profile.
- FR-04.2 During checkout the customer selects which address the order is shipped to.
- FR-04.3 The customer can edit or delete their saved addresses.

### FR-05 — Checkout
- FR-05.1 The customer initiates checkout from the cart.
- FR-05.2 Checkout requires a selected delivery address.
- FR-05.3 Checkout presents the order total before payment.

### FR-06 — Simulated Payment
- FR-06.1 The system shall present a payment form supporting Credit Card and Debit Card options (simulated).
- FR-06.2 No real payment processor shall be connected.
- FR-06.3 No actual card numbers, CVVs, or banking credentials shall be stored.
- FR-06.4 Payment simulation shall produce either a success or failure response.
- FR-06.5 On simulated payment success the order is confirmed and persisted.
- FR-06.6 On simulated payment failure the order is not created and the customer is notified.

### FR-07 — Order Confirmation
- FR-07.1 On successful payment the customer receives an order confirmation containing order ID, items purchased, delivery address, and total amount.
- FR-07.2 A confirmation record is persisted in the database.

### FR-08 — Order History
- FR-08.1 An authenticated customer can view a list of their past orders.
- FR-08.2 Each order entry shows order ID, date, items, status, and total.
- FR-08.3 The customer can view the detail of an individual order.

### FR-09 — Order Cancellation
- FR-09.1 The customer can cancel an order within 48 hours of placing it.
- FR-09.2 Orders that are older than 48 hours cannot be cancelled.
- FR-09.3 When an order is cancelled its status is updated to CANCELLED.

---

## 2. Non-Functional Requirements

| ID     | Category        | Requirement                                                                                      |
|--------|-----------------|--------------------------------------------------------------------------------------------------|
| NFR-01 | Technology      | Java 21, Spring Boot 3.x, Maven, PostgreSQL, Spring Data JPA/Hibernate.                          |
| NFR-02 | API Style       | RESTful, resource-oriented. OpenAPI 3.x specification is the contract.                           |
| NFR-03 | Security        | Passwords must be stored hashed (BCrypt). Credentials must not appear in source code.            |
| NFR-04 | Configuration   | Database credentials and sensitive values via environment variables or application config files. |
| NFR-05 | Validation      | All API inputs must be validated; meaningful error responses returned with appropriate HTTP codes.|
| NFR-06 | Data Integrity  | Appropriate primary keys, foreign keys, constraints, and indexes applied to the database schema. |
| NFR-07 | Testability     | Unit tests, service-layer tests, and repository integration tests covering happy and error paths. |
| NFR-08 | Portability     | Application runs locally; no cloud, container, or Kubernetes infrastructure required.            |
| NFR-09 | Version Control | Source committed to GitHub; no secrets committed.                                                |

---

## 3. User Roles

| Role      | Description                                                                                     |
|-----------|-------------------------------------------------------------------------------------------------|
| Customer  | A registered and authenticated end-user who browses, purchases, and manages orders.             |
| Anonymous | An unauthenticated visitor; may view the catalogue but cannot add to cart or check out.          |
| (Admin)   | Out of scope for MVP. No admin management interface is required for the capstone.               |

---

## 4. User Stories

### Authentication
- US-01 As a visitor I want to register an account so that I can purchase books.
- US-02 As a registered customer I want to log in so that I can access my cart and order history.
- US-03 As a logged-in customer I want to log out to secure my account.

### Catalogue & Browsing
- US-04 As a customer I want to see a list of available books on the home page so I can start browsing.
- US-05 As a customer I want to browse books by category so I can find books relevant to my interests.
- US-06 As a customer I want to browse books by brand/publisher.
- US-07 As a customer I want to search for books by keyword so I can find specific titles quickly.
- US-08 As a customer I want to view a book's detail page so I can see full information before buying.
- US-09 As a customer I want to see a tentative delivery date on a product so I know when to expect it.

### Cart
- US-10 As a customer I want to add a book to my cart so I can purchase it later.
- US-11 As a customer I want to view my cart so I can review items before checkout.
- US-12 As a customer I want to update item quantities in my cart.
- US-13 As a customer I want to remove items from my cart.

### Checkout & Payment
- US-14 As a customer I want to select a delivery address during checkout.
- US-15 As a customer I want to complete a simulated payment so I can place an order.
- US-16 As a customer I want to receive an order confirmation after successful payment.
- US-17 As a customer I want to be notified if payment fails so I can try again.

### Order Management
- US-18 As a customer I want to view my order history so I can track past purchases.
- US-19 As a customer I want to view the details of a specific order.
- US-20 As a customer I want to cancel an order within 48 hours if I change my mind.

---

## 5. Business Capabilities

| Capability ID | Capability Name          | Description                                                                         |
|---------------|--------------------------|-------------------------------------------------------------------------------------|
| BC-01         | Identity & Access        | Registration, login, logout, session management.                                    |
| BC-02         | Product Management       | Catalogue of books with categories and brands.                                      |
| BC-03         | Cart Management          | Persistent cart per customer with CRUD operations.                                  |
| BC-04         | Address Management       | Customer delivery addresses stored and selected at checkout.                        |
| BC-05         | Order Processing         | Checkout flow: address selection → payment simulation → order creation.             |
| BC-06         | Payment Simulation       | Simulated card payment producing success/failure without real processing.           |
| BC-07         | Order History & Detail   | Retrieval of past orders and their details.                                         |
| BC-08         | Order Cancellation       | Time-limited cancellation (within 48 hours).                                        |

---

## 6. Logical Domain Boundaries

| Domain       | Responsibility                                                      |
|--------------|---------------------------------------------------------------------|
| Users        | Registration, authentication, profile data.                         |
| Catalogue    | Books, categories, brands/publishers.                               |
| Cart         | Active cart and line items per customer.                            |
| Addresses    | Saved delivery addresses per customer.                              |
| Orders       | Order lifecycle: created, confirmed, cancelled.                     |
| Payments     | Simulated payment transactions linked to orders.                    |

---

## 7. Minimal Backend Modules

```
com.bookstore
├── user          (registration, login, profile)
├── catalogue     (books, categories, brands)
├── cart          (cart, cart items)
├── address       (delivery addresses)
├── order         (orders, order items, order status)
├── payment       (simulated payment processing)
└── common        (exception handling, validation, DTOs, security config)
```

---

## 8. Logical API Domains

| API Domain  | Base Path         | Responsibility                                          |
|-------------|-------------------|---------------------------------------------------------|
| Auth        | `/api/auth`       | Register, login, logout.                                |
| Books       | `/api/books`      | List, search, filter, detail.                           |
| Categories  | `/api/categories` | List categories; books by category.                     |
| Cart        | `/api/cart`       | View, add, update, remove cart items.                   |
| Addresses   | `/api/addresses`  | List, add, update, delete delivery addresses.           |
| Orders      | `/api/orders`     | Place order, view history, view detail, cancel.         |
| Payments    | `/api/payments`   | Initiate and confirm simulated payment.                 |

---

## 9. Core Data Entities

| Entity          | Key Attributes (conceptual)                                                                      |
|-----------------|--------------------------------------------------------------------------------------------------|
| User            | id, name, email, password (hashed), role, created_at                                             |
| Category        | id, name, description                                                                            |
| Brand           | id, name (publisher name)                                                                        |
| Book            | id, title, author, isbn, description, price, stock_quantity, tentative_delivery_date (representation TBD), category_id, brand_id, created_at |
| Cart            | id, user_id, created_at, updated_at                                                              |
| CartItem        | id, cart_id, book_id, quantity                                                                   |
| Address         | id, user_id, recipient_name, street_line1, street_line2, city, state, postal_code, country, is_default |
| Order           | id, user_id, address_id, status, total_amount, created_at, updated_at                           |
| OrderItem       | id, order_id, book_id, quantity, unit_price                                                      |
| Payment         | id, order_id, payment_method (representation TBD), status, simulated_reference, created_at                           |

---

## 10. Data Ownership Matrix

| Entity     | Owned By Domain | Created By        | Read By               | Updated By       | Deleted By       |
|------------|-----------------|-------------------|-----------------------|------------------|------------------|
| User       | Users           | User (register)   | Auth, User            | User             | —                |
| Category   | Catalogue       | (initial data load) | Catalogue, Cart, Order| —                | —                |
| Brand      | Catalogue       | (initial data load) | Catalogue             | —                | —                |
| Book       | Catalogue       | (initial data load) | Catalogue, Cart, Order| —                | —                |
| Cart       | Cart            | Cart (auto)       | Cart                  | Cart             | Cart (on order)  |
| CartItem   | Cart            | Cart              | Cart                  | Cart             | Cart             |
| Address    | Addresses       | User              | Addresses, Orders     | User             | User             |
| Order      | Orders          | Orders (checkout) | Orders, User          | Orders           | —                |
| OrderItem  | Orders          | Orders (checkout) | Orders                | —                | —                |
| Payment    | Payments        | Payments          | Payments, Orders      | Payments         | —                |

---

## 11. Business Rules

| ID    | Rule                                                                                                           |
|-------|----------------------------------------------------------------------------------------------------------------|
| BR-01 | A cart belongs to exactly one authenticated user.                                                              |
| BR-02 | A user may only have one active cart at a time.                                                                |
| BR-03 | A book can only be added to a cart if it is in stock (stock_quantity > 0).                                     |
| BR-04 | An order is only created if the simulated payment succeeds.                                                    |
| BR-05 | An order requires a delivery address selected at checkout.                                                     |
| BR-06 | OrderItem records capture the book price at time of purchase (price must not change retroactively).            |
| BR-07 | Orders may be cancelled only if the order was placed within the last 48 hours and status is not CANCELLED.     |
| BR-08 | Payment details (card numbers, CVVs) must never be persisted; only a simulated reference code is stored.       |
| BR-09 | On successful checkout, the cart is cleared (or deactivated).                                                  |
| BR-10 | Email addresses must be unique across users.                                                                   |

---

## 12. Assumptions and Ambiguities

| ID    | Topic                   | Ambiguity / Assumption                                                                                          |
|-------|-------------------------|-----------------------------------------------------------------------------------------------------------------|
| AM-01 | Authentication Mechanism | The capstone document requires login and session maintenance but does not specify the authentication mechanism (e.g. JWT, HTTP session, OAuth2). **Open architectural decision — to be resolved during API/implementation design.** |
| AM-02 | Buy Again               | "User Browses Order History with Buy Again feature" appears in the customer journey list. **Assumption:** Treat this as secondary/optional. It is not required for the end-to-end MVP demo.         |
| AM-03 | Recommendations         | "Recommends items based on Order History" appears in the journey. **Assumption:** Treat as secondary/out of scope for MVP. No recommendation engine will be built.                                  |
| AM-04 | Gift Points             | "Redeem gift points" appears in the payment screen. **Assumption:** Treat as secondary/out of scope. The payment simulation will not include a points system.                                       |
| AM-05 | Related Products        | "Related products appear for selection." **Assumption:** Treat as secondary. The core product detail endpoint will not return related product recommendations.                                      |
| AM-06 | Delivery Date           | The wireframe says products are "tagged with tentative delivery date." The requirement is to expose a tentative delivery date per book. **How this is stored and computed is an open design decision — to be resolved during data-model design.** |
| AM-07 | Admin / Seeding         | No admin UI is described. No admin CRUD API is required for MVP. **The strategy for loading catalogue data (books, categories, brands) is an open design decision — to be resolved during data-model design.** |
| AM-08 | Stock Management        | No explicit stock replenishment flow described. **Assumption:** Books carry a stock quantity. Decrement on order; return stock on cancellation.                                                     |
| AM-09 | Address on Order        | The delivery address is selected at checkout. **Assumption:** A snapshot of address fields is not stored on the order; instead the address foreign key is stored. If the user later edits the address, the historical reference stays accurate (address is not deleted, only soft-deletable). |
| AM-10 | Payment Method          | The capstone slide shows "Credit card, Debit card" as payment options. No actual card details may be stored. **How the chosen payment method type and simulated transaction are represented in the data model is an open design decision — to be resolved during data-model design.** |
| AM-11 | User Roles              | No admin or moderator role is shown. **Assumption:** Single customer role for MVP.                                                                                                                 |

---

## 13. MVP Scope

The minimum viable product covers the complete end-to-end customer journey:

1. Register / Login
2. Browse catalogue (home, by category, by brand, search)
3. View product detail
4. Add to cart / manage cart
5. Select delivery address
6. Checkout with simulated payment
7. Receive order confirmation
8. View order history
9. View order detail
10. Cancel order within 48 hours

---

## 14. Secondary / Optional Scope

These items appear in the capstone document but are not required for a successful MVP demonstration:

- **Buy Again** — re-add items from order history to the cart
- **Recommendations** — books recommended based on order history
- **Gift Points** — earn and redeem points at checkout
- **Related Products** — similar books displayed on a product detail page
- **Admin CRUD** — management console for books/categories/users

---

## 15. Out-of-Scope Items

- Cloud deployment (AWS, IBM Cloud, etc.)
- Containerisation (Docker, Kubernetes)
- Microservices architecture
- Real payment processing (Stripe, PayPal, etc.)
- Email notifications
- Frontend / UI implementation
- Wishlists, reviews, ratings
- Multi-currency support
- Inventory replenishment workflows

---

## 16. Requirements Traceability

| Requirement | Source Reference                                                                 |
|-------------|----------------------------------------------------------------------------------|
| FR-01 (Auth)         | Slide: "Login Page / User Authentication"                               |
| FR-02 (Catalogue)    | Slides: "Catalogue", "Select Category", "Browse brands", "Select product", "Access Product catalogue for each category" |
| FR-03 (Cart)         | Slides: "Add the product(s) to basket", Shopping Cart wireframe         |
| FR-04 (Address)      | Slide: "Select the address for delivery"                                |
| FR-05 (Checkout)     | Slides: "Payment & Purchase confirmation", checkout flow                |
| FR-06 (Payment)      | Slides: "Initiate payment with the right option", "Payment Screen"      |
| FR-07 (Confirmation) | Slide: "Confirmation on purchase", "Payment & Purchase"                 |
| FR-08 (Order History)| Slide: "User Browses Order History"                                     |
| FR-09 (Cancellation) | Slide: "Cancel Order Within 48 hrs"                                     |
| NFR-01 (Tech Stack)  | Capstone doc: "Technology Components and Workflow" — PostgreSQL, Spring Boot |
| NFR-03 (Passwords)   | Project instructions: "Do not place database credentials in source code"|
| BR-08 (No card data) | Project instructions: "Do not store actual credit card numbers, CVVs"   |

---

**Status: Reviewed — Ready for Data Model Design**
