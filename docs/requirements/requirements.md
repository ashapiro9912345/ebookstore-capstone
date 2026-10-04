# E-Bookstore Backend — Requirements Document

**Project:** AI Specialist Capstone — E-Commerce Bookstore Backend  
**Version:** 1.0  
**Date:** 2026-10-04  
**Status:** Draft — Awaiting Review  
**Source Material:** AI Specialist - Cloud FullStack - Capstone instructions.pptx.pdf (Slides 3–12)

---

## Table of Contents

1. [Functional Requirements](#1-functional-requirements)
2. [Non-Functional Requirements](#2-non-functional-requirements)
3. [User Roles](#3-user-roles)
4. [User Stories](#4-user-stories)
5. [Business Capabilities](#5-business-capabilities)
6. [Domain Boundaries](#6-domain-boundaries)
7. [Backend Modules](#7-backend-modules)
8. [API Domains](#8-api-domains)
9. [Core Data Entities](#9-core-data-entities)
10. [Data Ownership Matrix](#10-data-ownership-matrix)
11. [Business Rules](#11-business-rules)
12. [Assumptions and Ambiguities](#12-assumptions-and-ambiguities)
13. [MVP Scope](#13-mvp-scope)
14. [Secondary / Optional Scope](#14-secondary--optional-scope)
15. [Out-of-Scope Items](#15-out-of-scope-items)
16. [Requirements Traceability](#16-requirements-traceability)

---

## 1. Functional Requirements

### Authentication & User Management

| ID | Description | Related Screen / Action | Priority |
|----|-------------|------------------------|----------|
| FR-AUTH-01 | The system shall allow a customer to register a new account with name, email, and password. | Login Page (Slide 3, Step 1–2) | MVP |
| FR-AUTH-02 | The system shall authenticate a customer using email and password, returning a session token. | User Authentication (Slide 3, Step 2) | MVP |
| FR-AUTH-03 | The system shall protect all non-public endpoints so that only authenticated customers can access them. | All post-login screens | MVP |
| FR-AUTH-04 | The system shall allow a customer to log out, invalidating their session. | Implied by authenticated session | MVP |

### Product Catalogue & Browsing

| ID | Description | Related Screen / Action | Priority |
|----|-------------|------------------------|----------|
| FR-CAT-01 | The system shall maintain a catalogue of books available for purchase. | Landing Page (Slide 5); Catalogue Page (Slide 6) | MVP |
| FR-CAT-02 | The system shall organise books into categories. | Catalogue — Select Category (Slide 6, Step 5) | MVP |
| FR-CAT-03 | The system shall allow a customer to retrieve all books for a given category. | Catalogue — Category filter (Slide 6) | MVP |
| FR-CAT-04 | The system shall allow a customer to search or browse books by brand/publisher. | Catalogue — Browse Brands (Slide 6, Step 6) | MVP |
| FR-CAT-05 | The system shall return a detailed view of a single book, including title, author, price, description, and tentative delivery date. | Product Detail (Slide 6, Step 7) | MVP |
| FR-CAT-06 | The system shall allow a customer to search for books by keyword (title, author, or category). | Landing Page / Catalogue (Slide 5–6) | MVP |
| FR-CAT-07 | The system shall return related products for a selected book. | Product Detail — Related Products (Slide 6, Step 7) | Secondary |

### Shopping Cart

| ID | Description | Related Screen / Action | Priority |
|----|-------------|------------------------|----------|
| FR-CART-01 | The system shall allow an authenticated customer to add one or more books to their shopping cart. | Shopping Cart (Slide 7, Step 8) | MVP |
| FR-CART-02 | The system shall allow a customer to view the contents of their cart, including items, quantities, and total price. | Shopping Cart (Slide 7) | MVP |
| FR-CART-03 | The system shall allow a customer to update the quantity of an item in the cart. | Shopping Cart (Slide 7) | MVP |
| FR-CART-04 | The system shall allow a customer to remove an item from the cart. | Shopping Cart (Slide 7) | MVP |
| FR-CART-05 | The system shall maintain a persistent cart for the authenticated customer across sessions. | Shopping Cart (Slide 7) | MVP |

### Delivery Address

| ID | Description | Related Screen / Action | Priority |
|----|-------------|------------------------|----------|
| FR-ADDR-01 | The system shall allow a customer to add one or more delivery addresses to their account. | Payment & Purchase — Address (Slide 8, Step 9) | MVP |
| FR-ADDR-02 | The system shall allow a customer to select a saved delivery address during checkout. | Payment & Purchase — Address (Slide 8, Step 9) | MVP |
| FR-ADDR-03 | The system shall allow a customer to edit or delete a saved delivery address. | Implied by address management (Slide 8) | MVP |

### Checkout & Payment

| ID | Description | Related Screen / Action | Priority |
|----|-------------|------------------------|----------|
| FR-PAY-01 | The system shall allow a customer to initiate checkout from their cart. | Payment & Purchase (Slide 8) | MVP |
| FR-PAY-02 | The system shall allow a customer to select a payment method (credit card or debit card). | Payment Screen (Slide 9, Step 10) | MVP |
| FR-PAY-03 | The system shall simulate payment processing and return a success or failure result. | Payment Screen (Slide 9) | MVP |
| FR-PAY-04 | The system shall NOT store actual card numbers, CVVs, or sensitive payment credentials. | Payment Screen (Slide 9) | MVP |
| FR-PAY-05 | The system shall allow a customer to apply gift/loyalty points to reduce the order total. | Payment — Redeem Gift Points (Slide 8, Step 10) | Secondary |

### Order Management

| ID | Description | Related Screen / Action | Priority |
|----|-------------|------------------------|----------|
| FR-ORD-01 | The system shall create an order record upon successful simulated payment. | Purchase Confirmation (Slide 10, Step 11–12) | MVP |
| FR-ORD-02 | The system shall return a purchase confirmation to the customer after a successful order. | Purchase Confirmation (Slide 10, Step 12) | MVP |
| FR-ORD-03 | The system shall allow a customer to view their order history. | Order History (Slide 6, Step 6) | MVP |
| FR-ORD-04 | The system shall allow a customer to cancel an order within 48 hours of placement. | Cancel Order (Slide 3, Step 12) | MVP |
| FR-ORD-05 | The system shall store the delivery address selected at checkout with each order. | Order record (Slide 8) | MVP |
| FR-ORD-06 | The system shall store the payment method type (not credentials) used for each order. | Order record (Slide 9) | MVP |

---

## 2. Non-Functional Requirements

| ID | Category | Description |
|----|----------|-------------|
| NFR-01 | Maintainability | The application shall use a standard layered architecture: Controller → Service → Repository, keeping layers independently testable. |
| NFR-02 | Maintainability | DTOs shall be kept separate from JPA persistence entities to avoid leaking database structure into the API layer. |
| NFR-03 | Validation | All incoming API request payloads shall be validated using Bean Validation (Jakarta Validation). Invalid requests shall return HTTP 400 with a meaningful error message. |
| NFR-04 | Security | Passwords shall be stored as hashed values (e.g., BCrypt). Plain-text passwords shall never be stored or logged. |
| NFR-05 | Security | Sensitive payment information (card numbers, CVV) shall not be stored or logged by the application. |
| NFR-06 | Security | API endpoints that operate on customer data shall verify that the authenticated user is the owner of that data. |
| NFR-07 | Data Integrity | Foreign key relationships shall be enforced at the database level where appropriate. |
| NFR-08 | Data Integrity | Order records shall be immutable after creation, except for the cancellation status field. |
| NFR-09 | Error Handling | The application shall return structured JSON error responses with an appropriate HTTP status code and a human-readable message for all error conditions. |
| NFR-10 | Error Handling | The application shall not expose internal stack traces or database errors in API responses. |
| NFR-11 | Performance | The application shall perform adequately for demonstration-scale data (hundreds of books, tens of users, hundreds of orders). No enterprise-scale throughput requirements apply. |
| NFR-12 | Testability | Core business logic in service classes shall be unit-testable without a live database. |
| NFR-13 | Testability | Repository interactions shall be tested against a real PostgreSQL instance or an equivalent integration test configuration. |
| NFR-14 | Configuration | Database credentials and sensitive configuration shall be externalised via environment variables or `application.properties` / `application.yml`. No credentials shall appear in source code or be committed to Git. |

---

## 3. User Roles

| Role | Description | Source |
|------|-------------|--------|
| **Customer** | A registered, authenticated user who can browse books, manage a cart, place orders, and view order history. | Slides 3–10 — all customer journeys |
| **Guest / Anonymous** | An unauthenticated visitor who can view the landing page and browse the catalogue. Login is required before adding to cart or checking out. | Slide 5 — Landing Page (implied) |

> No administrative, staff, or inventory-management roles are present in the provided capstone materials. An admin role is therefore out of scope for the MVP.

---

## 4. User Stories

### Authentication & Registration

- As a **guest**, I want to register an account so that I can make purchases on the bookstore.
- As a **guest**, I want to log in with my email and password so that I can access my cart and order history.
- As a **customer**, I want to log out so that my account is secured on a shared device.

### Product Browsing

- As a **guest or customer**, I want to see a landing page showing available books so that I can get an overview of the store's catalogue.
- As a **guest or customer**, I want to browse books by category so that I can find books relevant to my interests.
- As a **guest or customer**, I want to filter books by brand/publisher so that I can narrow my search.
- As a **guest or customer**, I want to search for books by keyword so that I can quickly find a specific title or author.
- As a **guest or customer**, I want to view a book's detail page including its price, description, and tentative delivery date so that I can make an informed purchase decision.

### Shopping Cart

- As a **customer**, I want to add a book to my shopping cart so that I can purchase it later.
- As a **customer**, I want to view the contents of my cart with a running total so that I know what I am about to buy.
- As a **customer**, I want to update the quantity of a book in my cart so that I can adjust my order before checkout.
- As a **customer**, I want to remove a book from my cart so that I can change my mind without starting over.

### Checkout & Payment

- As a **customer**, I want to select a saved delivery address during checkout so that I know where my books will be sent.
- As a **customer**, I want to add a new delivery address so that I can ship to a new location.
- As a **customer**, I want to choose a payment method (credit or debit card) so that I can complete my purchase.
- As a **customer**, I want to receive confirmation that my payment was accepted so that I know my order was placed successfully.

### Orders & History

- As a **customer**, I want to view my order history so that I can see past purchases.
- As a **customer**, I want to see the details of a specific order so that I can review what I bought and the status.
- As a **customer**, I want to cancel an order within 48 hours so that I can change my mind after placing it.

### Secondary Stories

- As a **customer**, I want to see books related to the one I am viewing so that I can discover similar titles.
- As a **customer**, I want to see recommendations based on my order history so that I can find books I might enjoy.
- As a **customer**, I want to redeem gift/loyalty points at checkout so that I can get a discount on my purchase.
- As a **customer**, I want to re-order a previously purchased book from my order history so that I can quickly buy it again.

---

## 5. Business Capabilities

| # | Capability | Description |
|---|-----------|-------------|
| 1 | **User Registration & Authentication** | Register, authenticate, and manage customer sessions. |
| 2 | **Product Catalogue** | Maintain the book inventory; expose books with details, categories, and brands. |
| 3 | **Category Management** | Organise books into browsable categories. |
| 4 | **Product Search & Browse** | Support keyword search and filtered browsing by category and brand. |
| 5 | **Shopping Cart** | Maintain a per-customer cart with add/update/remove operations. |
| 6 | **Delivery Address Management** | Allow customers to store and select delivery addresses. |
| 7 | **Checkout** | Orchestrate the end-to-end flow from cart to confirmed order. |
| 8 | **Simulated Payment** | Accept payment method selection and simulate processing. |
| 9 | **Order Management** | Create, store, and retrieve orders; support order cancellation. |
| 10 | **Order History** | Provide customers access to their historical orders. |
| 11 | **Purchase Confirmation** | Return a confirmation response and record after a successful checkout. |

---

## 6. Domain Boundaries

The application is a **modular monolith**. Domain boundaries define code organisation and responsibility, not deployment units.

| Domain | Responsibility |
|--------|---------------|
| **identity** | Customer registration, authentication, and session/token management. |
| **catalog** | Books, categories, brands, product search, and related products. |
| **cart** | Shopping cart state for authenticated customers. |
| **checkout** | Orchestration of the checkout flow: address selection, payment simulation, and order creation. |
| **order** | Order lifecycle: creation, status tracking, history retrieval, and cancellation. |
| **address** | Delivery address storage and management for customers. |
| **payment** | Simulated payment processing; payment method recording (no real credentials). |

---

## 7. Backend Modules

| Module / Package | Responsibility |
|-----------------|----------------|
| `identity` | Handles `Customer` registration and login. Issues and validates authentication tokens. Owns the `Customer` entity. |
| `catalog` | Manages `Book`, `Category`, and `Brand` entities. Provides search, filtering, and product detail APIs. |
| `cart` | Maintains `Cart` and `CartItem` state. Handles add, update, remove operations per customer. |
| `address` | Manages `DeliveryAddress` records owned by the customer. |
| `checkout` | Orchestrates the checkout sequence: validate cart → select address → process simulated payment → create order. Depends on `cart`, `address`, `payment`, and `order`. |
| `payment` | Contains simulated payment logic. Records payment method type on the order. Does not store card credentials. |
| `order` | Owns `Order` and `OrderItem` entities. Provides order creation, retrieval, history listing, and cancellation. |
| `common` | Shared utilities: exception handling, base DTOs, API response wrappers, validation helpers. |

---

## 8. API Domains

The following logical resource areas will require REST APIs. Paths, HTTP methods, and DTOs are not defined here.

| API Domain | Description |
|-----------|-------------|
| **Authentication** | Register, login, logout. |
| **Customers** | Customer profile retrieval and update. |
| **Books** | List books, search books, get book detail. |
| **Categories** | List all categories, get books by category. |
| **Brands** | List brands, get books by brand. |
| **Cart** | Get cart, add item, update item quantity, remove item, clear cart. |
| **Addresses** | List addresses, add address, update address, delete address. |
| **Checkout** | Initiate checkout, confirm payment, place order. |
| **Orders** | Get order history, get order detail, cancel order. |

---

## 9. Core Data Entities

### Customer
- **Business purpose:** Represents a registered user of the bookstore.
- **Key information:** Name, email address, hashed password, account creation date, account status.
- **Relationships:** Has many `DeliveryAddress`, has one `Cart`, has many `Order`.

### Book
- **Business purpose:** Represents a purchasable book in the catalogue.
- **Key information:** Title, author, description, price, cover image URL, stock availability, tentative delivery estimate, publication date.
- **Relationships:** Belongs to one `Category`; belongs to one `Brand`; appears in many `CartItem` and `OrderItem`.

### Category
- **Business purpose:** Groups books into browsable subject areas (e.g., Fiction, Technology, Science).
- **Key information:** Name, description.
- **Relationships:** Has many `Book`.

### Brand (Publisher)
- **Business purpose:** Represents the publisher or brand of a book.
- **Key information:** Name, description.
- **Relationships:** Has many `Book`.

### Cart
- **Business purpose:** Holds the books a customer intends to purchase before checkout.
- **Key information:** Associated customer, creation/update timestamp.
- **Relationships:** Belongs to one `Customer`; has many `CartItem`.

### CartItem
- **Business purpose:** Represents a single book and its quantity within a cart.
- **Key information:** Reference to book, quantity, unit price at time of addition.
- **Relationships:** Belongs to one `Cart`; references one `Book`.

### DeliveryAddress
- **Business purpose:** A physical address stored by a customer for use during checkout.
- **Key information:** Recipient name, street address, city, state/province, postal code, country, whether it is the default address.
- **Relationships:** Belongs to one `Customer`; referenced by `Order`.

### Order
- **Business purpose:** A confirmed purchase record created after successful checkout.
- **Key information:** Order reference number, order date, status (PENDING, CONFIRMED, CANCELLED), total amount, payment method type, delivery address snapshot.
- **Relationships:** Belongs to one `Customer`; has many `OrderItem`; references one `DeliveryAddress`.

### OrderItem
- **Business purpose:** Records the specific books and quantities in a confirmed order.
- **Key information:** Reference to book, quantity, unit price at time of purchase, subtotal.
- **Relationships:** Belongs to one `Order`; references one `Book`.

### Payment (record)
- **Business purpose:** Records the simulated payment event associated with an order.
- **Key information:** Payment method type (CREDIT_CARD, DEBIT_CARD), simulated status (SUCCESS, FAILURE), timestamp.
- **Relationships:** Associated with one `Order`. Does NOT store card number or CVV.

---

## 10. Data Ownership Matrix

| Entity | Owning Domain | Notes |
|--------|--------------|-------|
| `Customer` | `identity` | Core user account. |
| `Book` | `catalog` | Product data. |
| `Category` | `catalog` | Product taxonomy. |
| `Brand` | `catalog` | Publisher/brand data. |
| `Cart` | `cart` | Per-customer transient basket. |
| `CartItem` | `cart` | Child of Cart. |
| `DeliveryAddress` | `address` | Customer-owned address records. |
| `Order` | `order` | Confirmed purchase record. |
| `OrderItem` | `order` | Child of Order. |
| `Payment` | `payment` | Simulated payment record; linked to Order. |

---

## 11. Business Rules

### Explicit Requirements (stated in capstone materials)

| ID | Rule | Source |
|----|------|--------|
| BR-01 | A customer must be authenticated before adding items to a cart or checking out. | Slide 3 — User Authentication (Step 2) |
| BR-02 | A customer can cancel an order within 48 hours of placement. Orders cannot be cancelled after that window. | Slide 3 — Step 12 |
| BR-03 | Payment is simulated. No real payment processor shall be connected. No card credentials shall be stored. | Capstone instructions |
| BR-04 | A delivery address must be selected before payment can be initiated. | Slide 8 — Step 9 |
| BR-05 | The system must display a tentative delivery date on product detail. | Slide 6 — Step 7 |
| BR-06 | An order confirmation must be displayed after successful payment. | Slide 10 — Step 12 |

### Reasonable Assumptions

| ID | Assumption | Rationale |
|----|-----------|-----------|
| BR-A01 | A book must be in stock (quantity > 0) to be added to the cart. | Standard e-commerce expectation not contradicted by the materials. |
| BR-A02 | Prices are stored in a fixed decimal format (e.g., two decimal places) to avoid floating-point errors. | Data integrity best practice for financial values. |
| BR-A03 | The price captured in `CartItem` and `OrderItem` at the time of addition/purchase is the authoritative price for that line. Catalogue price changes do not retroactively affect existing carts or orders. | Protects order integrity. |
| BR-A04 | A customer can have only one active (non-checked-out) cart at a time. | Simplest model consistent with the wireframe. |
| BR-A05 | An order transitions to CONFIRMED status on successful payment and to CANCELLED status on cancellation. | Minimal state machine required by the cancellation rule. |
| BR-A06 | The tentative delivery date is a calculated or stored estimate on the Book entity, not dynamically computed per order at this stage. | Simplest implementation for capstone. |
| BR-A07 | Guest browsing (catalogue and product detail) is permitted without authentication. | Implied by the Landing Page being publicly visible. |

### Ambiguous Requirements Requiring a Decision

See Section 12.

---

## 12. Assumptions and Ambiguities

| ID | Topic | Ambiguity | Recommended Simplest Approach |
|----|-------|-----------|-------------------------------|
| AMB-01 | Authentication mechanism | The materials do not specify whether to use JWT, session cookies, or Spring Security basic auth. | Use JWT (stateless Bearer token) via Spring Security. This is standard for REST APIs and straightforward to test with Postman/Insomnia. |
| AMB-02 | Gift/loyalty points | Gift points are mentioned (Slide 8) but no rules are given for earning, balance management, or redemption calculation. | Treat as Secondary scope. If implemented, model as a simple integer point balance on the Customer; redemption applies a fixed point-to-currency conversion defined in configuration. |
| AMB-03 | Order cancellation eligibility | "Within 48 hours" is stated, but it is unclear whether the clock starts from order placement or payment confirmation. | Start the cancellation window from the order creation timestamp for simplicity. |
| AMB-04 | Stock management | No inventory replenishment, reservation, or oversell prevention rules are described. | Track a simple integer `stockQuantity` on Book. Decrement on order confirmation. Block cart addition if stock is 0. No reservation during cart hold. |
| AMB-05 | Buy Again | Mentioned on Slide 3 (Step 6) and Slide 6 but not shown as a standalone screen. | Implement as a Secondary feature: an API endpoint that re-adds a previous order's items to the current cart. |
| AMB-06 | Recommendations | Mentioned on Slide 3 (Step 6) but no algorithm or rule is described. | Treat as Secondary scope. Simplest implementation: return books from the same category as those most frequently ordered by the customer. |
| AMB-07 | Related products | Mentioned on Slide 6 (Step 7) but no rule for determining relatedness is given. | Treat as Secondary scope. Simplest implementation: return other books in the same category. |
| AMB-08 | Multiple delivery addresses | The wireframe implies address selection but does not specify a maximum. | Allow multiple addresses per customer with a flag for the default address. |
| AMB-09 | User profile management | Registration and login are shown, but no profile editing screen exists. | Implement a basic GET /customers/me endpoint returning profile data. Update endpoint is Secondary. |
| AMB-10 | Book data seeding | No mechanism for adding books to the catalogue is shown (no admin screen). | Seed initial book data via a SQL data script or Spring Boot `data.sql` for demonstration purposes. An admin API is out of scope for MVP. |
| AMB-11 | Delivery date | A tentative delivery date is shown on the product card but no calculation rule is given. | Store a `deliveryEstimateDays` integer on the Book. The API returns it as-is. |

---

## 13. MVP Scope

The minimum functionality required to complete and demonstrate the capstone end-to-end.

**Core customer journey:**

> Browse books → Select book → Add to cart → Select delivery address → Checkout → Simulated payment → Order confirmation → View order history → Cancel order (within 48 hrs)

### MVP Features

| Area | Included |
|------|---------|
| Customer registration and login (JWT) | ✅ |
| Landing page / book listing | ✅ |
| Book catalogue with category and brand browsing | ✅ |
| Keyword search for books | ✅ |
| Book detail page (including tentative delivery date) | ✅ |
| Shopping cart (add, view, update, remove) | ✅ |
| Delivery address management (add, list, select) | ✅ |
| Checkout flow (cart → address → simulated payment → order) | ✅ |
| Simulated payment (credit/debit, no real credentials stored) | ✅ |
| Purchase confirmation | ✅ |
| Order history (list) | ✅ |
| Order detail (single order) | ✅ |
| Order cancellation within 48 hours | ✅ |

---

## 14. Secondary / Optional Scope

Features present in the capstone materials that are reasonable to defer until after MVP is complete and demonstrated.

| Feature | Source | Notes |
|---------|--------|-------|
| Related products on product detail | Slide 6 (Step 7) | Return books from the same category. Simple query. |
| Buy Again from order history | Slide 3 (Step 6), Slide 6 | Re-add a past order's items to cart. |
| Recommendations based on order history | Slide 3 (Step 6) | Return books from categories the customer has previously purchased. |
| Gift / loyalty points redemption | Slide 8 (Step 10) | Requires point balance management and redemption logic. |
| Customer profile update | Implied | Update name, email, or password. |
| Delivery address update and delete | Implied | Extension of MVP address management. |

---

## 15. Out-of-Scope Items

The following shall not be implemented during the initial MVP unless explicitly requested.

| Item | Reason |
|------|--------|
| Admin / back-office API | No admin screens in capstone materials. Book data will be seeded. |
| Real payment processor integration | Explicitly prohibited by capstone instructions. |
| Storage of card numbers or CVV | Explicitly prohibited by capstone instructions. |
| Wishlist / save for later | Not present in capstone materials. |
| Product reviews and ratings | Not present in capstone materials. |
| Email notifications | Not present in capstone materials. No email infrastructure is specified. |
| AWS cloud hosting | Explicitly excluded by capstone constraints. |
| Docker / container deployment | Explicitly excluded by capstone constraints. |
| Microservices architecture | Explicitly excluded by capstone constraints. |
| Kubernetes | Explicitly excluded by capstone constraints. |
| Frontend / UI implementation | Capstone requires backend only. |
| Inventory replenishment / procurement | Not present in capstone materials. |

---

## 16. Requirements Traceability

| Requirement ID(s) | Capability | Source — Slide / Step |
|-------------------|-----------|----------------------|
| FR-AUTH-01, FR-AUTH-02 | User Authentication | Slide 3, Steps 1–2; Slide 5 (Login Page) |
| FR-AUTH-03, FR-AUTH-04 | Security / Session | Slide 3 Step 2 (implied); Capstone instructions |
| FR-CAT-01 through FR-CAT-06 | Product Catalogue & Search | Slide 5 (Landing), Slide 6 (Catalogue), Slide 3 Steps 5–7 |
| FR-CAT-07 | Related Products | Slide 6, Step 7 |
| FR-CART-01 through FR-CART-05 | Shopping Cart | Slide 7, Step 8 |
| FR-ADDR-01 through FR-ADDR-03 | Delivery Address | Slide 8, Step 9 |
| FR-PAY-01 through FR-PAY-04 | Checkout & Simulated Payment | Slide 8, Slide 9, Steps 10–11 |
| FR-PAY-05 | Gift Points Redemption | Slide 8, Step 10 |
| FR-ORD-01 through FR-ORD-06 | Order Management & Confirmation | Slide 10, Steps 11–12; Slide 3 Step 12 (cancel) |
| BR-02 | Order Cancellation (48 hrs) | Slide 3, Step 12 |
| BR-05 | Tentative Delivery Date | Slide 6, Step 7 |
| NFR-04, NFR-05 | Security / No credential storage | Capstone instructions (Payment Rules) |
| NFR-14 | Configuration / No secrets in code | Capstone instructions (Database Rules) |

---

*End of Requirements Document*

*This document represents the requirements analysis phase only. No application code, SQL, database schema, OpenAPI specification, or API endpoint definitions have been produced at this stage.*
