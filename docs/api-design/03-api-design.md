# E-Bookstore REST API Design

**Sources:**
- `docs/requirements/requirements.md` (Status: Reviewed — Ready for Data Model Design)
- `docs/data-model/02-data-model.md` (Status: Reviewed — Ready for API Design)

**Governing Standards:** Kiro Project Instructions  
**Phase:** 3 — API Design

---

## 1. Design Principles

- Resource-oriented RESTful design. URIs identify resources; HTTP methods express intent.
- All request and response bodies are `application/json`.
- All protected endpoints require a JWT Bearer token in the `Authorization` header. This is the first point in the project where the authentication mechanism is selected (AM-01 deferred from requirements). JWT is chosen because it is stateless, fits REST naturally, and is the standard approach for Spring Boot REST APIs.
- Validation errors return `400 Bad Request` with a structured error body.
- Authorisation failures return `401 Unauthorized` (missing/invalid token) or `403 Forbidden` (valid token, insufficient rights).
- Not-found errors return `404 Not Found`.
- Business rule violations return `422 Unprocessable Entity`.
- Unexpected server failures return `500 Internal Server Error`.
- Monetary values (`price`, `unitPrice`, `lineTotal`, `totalAmount`) are represented as JSON numbers. The Java implementation uses `BigDecimal`; PostgreSQL stores them as `NUMERIC(10,2)`. No floating-point types are used in persistence.
- Timestamps are ISO 8601 UTC strings (e.g. `"2026-10-04T14:30:00Z"`).

---

## 2. Architectural Decisions

### Authentication Mechanism

**Decision:** JWT (JSON Web Token) Bearer authentication.

**Rationale:** A stateless JWT approach suits a REST API — no server-side session state is required, the token is presented on each request, and Spring Security's built-in support makes it straightforward to implement. The token is issued on login and must be included in the `Authorization: Bearer <token>` header on all protected endpoints.

**Scope of this phase:** The OpenAPI specification defines the `bearerAuth` security scheme and marks which endpoints require it. The specific JWT library and configuration choices are implementation decisions for Phase 4.

### Demo Customer Account

Customer self-registration (`POST /auth/register`) is classified as Secondary scope in the approved requirements. For the MVP demonstration, a pre-configured demo customer account is loaded during local database setup. Testers and reviewers log in using `POST /auth/login` with the demo credentials. No registration endpoint is included in the MVP contract.

---

## 3. Base URL

```
/api
```

All paths below are relative to `/api`.

---

## 4. Resource Definitions

### 4.1 Authentication — `/auth`

| # | Purpose | Method | URI | Auth Required |
|---|---------|--------|-----|---------------|
| 1 | Log in and receive a JWT | POST | `/auth/login` | No |

---

#### POST /auth/login

**Purpose:** Authenticate a customer and return a JWT (FR-01.2, US-02).

**Request body:**

| Field | Type | Constraints |
|-------|------|-------------|
| `email` | string | Required, valid email format |
| `password` | string | Required |

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Authentication successful; returns JWT and user summary |
| 400 Bad Request | Validation failure |
| 401 Unauthorized | Invalid credentials |

**Response body (200):** `AuthResponse` — contains `token` (string) and `user` (UserResponse)

---

### 4.2 Categories — `/categories`

| # | Purpose | Method | URI | Auth Required |
|---|---------|--------|-----|---------------|
| 2 | List all categories | GET | `/categories` | No |

---

#### GET /categories

**Purpose:** Retrieve the full list of book categories for browse/filter UI (FR-02.2, US-05).

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Returns array of categories |

**Response body (200):** Array of `CategoryResponse`

---

### 4.3 Books — `/books`

| # | Purpose | Method | URI | Auth Required |
|---|---------|--------|-----|---------------|
| 3 | List / search / filter books | GET | `/books` | No |
| 4 | Get a single book | GET | `/books/{bookId}` | No |

---

#### GET /books

**Purpose:** Browse the catalogue; supports search and filter (FR-02.1, FR-02.2, FR-02.4, FR-02.6, US-04–US-08).

**Query parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `title` | string | Case-insensitive partial match on title |
| `authorName` | string | Case-insensitive partial match on author |
| `categoryId` | integer | Filter by category |
| `brandId` | integer | Filter by brand/publisher |
| `page` | integer | Page number, 0-based, default 0 |
| `size` | integer | Page size, default 20, max 100 |

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Paginated list of books |
| 400 Bad Request | Invalid query parameter |

**Response body (200):** `PagedBooksResponse` — contains `content` (array of `BookSummaryResponse`), `page`, `size`, `totalElements`, `totalPages`

---

#### GET /books/{bookId}

**Purpose:** Retrieve full detail for a single book (FR-02.5, US-08, US-09).

**Path parameters:** `bookId` — integer

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Book detail |
| 404 Not Found | Book not found |

**Response body (200):** `BookDetailResponse` — includes all fields plus `deliveryEstimateDays` (used by consumer to compute tentative delivery date)

---

### 4.4 Cart — `/cart`

One cart per authenticated customer. The cart is created automatically on first use.

| # | Purpose | Method | URI | Auth Required |
|---|---------|--------|-----|---------------|
| 5 | Get the current customer's cart | GET | `/cart` | Yes |
| 6 | Add a book to the cart | POST | `/cart/items` | Yes |
| 7 | Update quantity of a cart item | PUT | `/cart/items/{itemId}` | Yes |
| 8 | Remove a book from the cart | DELETE | `/cart/items/{itemId}` | Yes |

---

#### GET /cart

**Purpose:** Retrieve the authenticated customer's cart and its items (FR-03.2, US-11).

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Cart with items (empty items array if cart has no items) |
| 401 Unauthorized | Not authenticated |

**Response body (200):** `CartResponse`

---

#### POST /cart/items

**Purpose:** Add a book to the cart. If the book is already in the cart, increments the quantity (FR-03.1, US-10, BR-03, DM-07).

**Request body:**

| Field | Type | Constraints |
|-------|------|-------------|
| `bookId` | integer | Required |
| `quantity` | integer | Required, min 1 |

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Cart updated; returns the full updated cart |
| 400 Bad Request | Validation failure |
| 401 Unauthorized | Not authenticated |
| 404 Not Found | Book not found |
| 422 Unprocessable Entity | Insufficient stock (BR-03) |

**Response body (200):** `CartResponse`

---

#### PUT /cart/items/{itemId}

**Purpose:** Update the quantity of an existing cart item (FR-03.3, US-12).

**Path parameters:** `itemId` — integer

**Request body:**

| Field | Type | Constraints |
|-------|------|-------------|
| `quantity` | integer | Required, min 1 |

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Cart updated; returns the full updated cart |
| 400 Bad Request | Validation failure |
| 401 Unauthorized | Not authenticated |
| 404 Not Found | Cart item not found |
| 422 Unprocessable Entity | Insufficient stock |

**Response body (200):** `CartResponse`

---

#### DELETE /cart/items/{itemId}

**Purpose:** Remove a book from the cart (FR-03.4, US-13).

**Path parameters:** `itemId` — integer

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Item removed; returns the updated cart |
| 401 Unauthorized | Not authenticated |
| 404 Not Found | Cart item not found |

**Response body (200):** `CartResponse`

---

### 4.5 Addresses — `/addresses`

Address update and delete are classified as Secondary scope. The MVP supports adding and listing addresses only.

| # | Purpose | Method | URI | Auth Required |
|---|---------|--------|-----|---------------|
| 9 | List the customer's saved addresses | GET | `/addresses` | Yes |
| 10 | Add a new delivery address | POST | `/addresses` | Yes |

---

#### GET /addresses

**Purpose:** List all saved delivery addresses for the authenticated customer (FR-04.1, US-14).

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Array of addresses (may be empty) |
| 401 Unauthorized | Not authenticated |

**Response body (200):** Array of `AddressResponse`

---

#### POST /addresses

**Purpose:** Add a new delivery address (FR-04.1, US-14).

**Request body:** `AddressRequest` — all address fields.

| Field | Type | Constraints |
|-------|------|-------------|
| `recipientName` | string | Required, 1–255 chars |
| `streetLine1` | string | Required, 1–255 chars |
| `streetLine2` | string | Optional, max 255 chars |
| `city` | string | Required, 1–100 chars |
| `state` | string | Required, 1–100 chars |
| `postalCode` | string | Required, 1–20 chars |
| `country` | string | Required, 1–100 chars |

**Responses:**

| Status | Meaning |
|--------|---------|
| 201 Created | Address created; returns the new address |
| 400 Bad Request | Validation failure |
| 401 Unauthorized | Not authenticated |

**Response body (201):** `AddressResponse`

---

### 4.6 Orders — `/orders`

Checkout is modelled as order creation. `POST /orders` accepts the delivery address and payment method, runs the simulated payment, and — on success — creates and returns the confirmed order. No separate checkout or payment endpoint is needed (DM-02).

Order cancellation uses `POST /orders/{orderId}/cancel`. Orders are historical records and are never represented as deleted resources.

| # | Purpose | Method | URI | Auth Required |
|---|---------|--------|-----|---------------|
| 11 | Place an order (checkout) | POST | `/orders` | Yes |
| 12 | List the customer's order history | GET | `/orders` | Yes |
| 13 | Get a single order | GET | `/orders/{orderId}` | Yes |
| 14 | Cancel an order | POST | `/orders/{orderId}/cancel` | Yes |

---

#### POST /orders

**Purpose:** Checkout — simulate payment and create a confirmed order from the customer's cart (FR-05, FR-06, FR-07, US-15, US-16, BR-04, BR-05, BR-09, DM-08).

**Request body:**

| Field | Type | Constraints |
|-------|------|-------------|
| `addressId` | integer | Required; must belong to the authenticated customer |
| `paymentMethod` | string | Required; `CREDIT_CARD` or `DEBIT_CARD` |

**Responses:**

| Status | Meaning |
|--------|---------|
| 201 Created | Order confirmed; returns the full order |
| 400 Bad Request | Validation failure |
| 401 Unauthorized | Not authenticated |
| 404 Not Found | Address not found |
| 422 Unprocessable Entity | Cart is empty; address belongs to another user; simulated payment declined; insufficient stock |

**Response body (201):** `OrderResponse`

**Response body on simulated payment failure (422):** `ErrorResponse` with `code: "PAYMENT_DECLINED"`

---

#### GET /orders

**Purpose:** Retrieve the authenticated customer's order history, newest first (FR-08.1, FR-08.2, US-18).

**Query parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | integer | 0-based, default 0 |
| `size` | integer | Default 20, max 100 |

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Paginated list of orders |
| 401 Unauthorized | Not authenticated |

**Response body (200):** `PagedOrdersResponse`

---

#### GET /orders/{orderId}

**Purpose:** Retrieve the full detail of a single order (FR-08.3, US-19).

**Path parameters:** `orderId` — integer

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Order detail |
| 401 Unauthorized | Not authenticated |
| 403 Forbidden | Order belongs to a different user |
| 404 Not Found | Order not found |

**Response body (200):** `OrderResponse`

---

#### POST /orders/{orderId}/cancel

**Purpose:** Cancel a confirmed order within the 48-hour window (FR-09, US-20, BR-07).

Orders are historical records. This operation changes status from `CONFIRMED` to `CANCELLED`; it does not delete the order resource.

**Path parameters:** `orderId` — integer

**Preconditions (all must be true):**
- The order belongs to the authenticated customer.
- The order status is `CONFIRMED`.
- The order was created within the last 48 hours.

**Post-conditions on success:**
- Order status set to `CANCELLED`.
- Stock quantities restored for all cancelled items.

**Responses:**

| Status | Meaning |
|--------|---------|
| 200 OK | Order cancelled; returns the updated order |
| 401 Unauthorized | Not authenticated |
| 403 Forbidden | Order belongs to a different user |
| 404 Not Found | Order not found |
| 422 Unprocessable Entity | Order is already cancelled, or the 48-hour cancellation window has passed |

**Response body (200):** `OrderResponse`

---

## 5. Response Schema Definitions

### UserResponse
```
id, email, fullName, createdAt
```

### AuthResponse
```
token (JWT string), user (UserResponse)
```

### CategoryResponse
```
id, name, description
```

### BrandResponse
```
id, name
```

### BookSummaryResponse
```
id, title, author, price (number), stockQuantity, deliveryEstimateDays,
categoryId, categoryName, brandId, brandName
```

### BookDetailResponse
```
id, title, author, isbn, description, price (number), stockQuantity, deliveryEstimateDays,
category (CategoryResponse), brand (BrandResponse)
```

### CartItemResponse
```
itemId, bookId, title, author, price (number), quantity, lineTotal (number)
```

### CartResponse
```
cartId, items (array of CartItemResponse), totalAmount (number)
```

### AddressResponse
```
id, recipientName, streetLine1, streetLine2, city, state, postalCode, country, createdAt
```

### OrderItemResponse
```
bookId, title, author, quantity, unitPrice (number), lineTotal (number)
```

### OrderResponse
```
id, status, totalAmount (number), paymentMethod, simulatedPaymentReference,
shippingAddress (ShippingAddressSnapshot), items (array of OrderItemResponse),
createdAt, updatedAt
```

### ShippingAddressSnapshot
```
recipientName, streetLine1, streetLine2, city, state, postalCode, country
```

### PagedBooksResponse
```
content (array of BookSummaryResponse), page, size, totalElements, totalPages
```

### PagedOrdersResponse
```
content (array of OrderResponse), page, size, totalElements, totalPages
```

### ErrorResponse
```
code (string), message (string), details (array of strings, optional)
```

---

## 6. Security Boundaries

| Endpoint group | Anonymous | Authenticated |
|----------------|-----------|---------------|
| POST /auth/login | ✓ | ✓ |
| GET /categories | ✓ | ✓ |
| GET /books, GET /books/{id} | ✓ | ✓ |
| GET /cart, POST /cart/items, PUT /cart/items/{id}, DELETE /cart/items/{id} | ✗ | ✓ (own cart only) |
| GET /addresses, POST /addresses | ✗ | ✓ (own addresses only) |
| POST /orders, GET /orders, GET /orders/{id}, POST /orders/{id}/cancel | ✗ | ✓ (own orders only) |

---

## 7. Standard HTTP Status Codes Used

| Code | Meaning |
|------|---------|
| 200 OK | Successful read or update |
| 201 Created | Successful resource creation |
| 400 Bad Request | Input validation failure |
| 401 Unauthorized | Missing, expired, or invalid JWT |
| 403 Forbidden | Authenticated but accessing another user's resource |
| 404 Not Found | Resource does not exist |
| 422 Unprocessable Entity | Business rule violation (out of stock, cancellation window expired, payment declined, empty cart) |
| 500 Internal Server Error | Unexpected server error |

Note: `204 No Content` and `409 Conflict` are no longer used in the MVP contract after removing address delete and customer registration.

---

## 8. Secondary Scope — Not in MVP Contract

The following capabilities are classified as Secondary scope and are excluded from the MVP OpenAPI contract. They may be added in a later phase if explicitly requested.

| Capability | Excluded Endpoint(s) |
|------------|----------------------|
| Customer self-registration | `POST /auth/register` |
| Address update | `PUT /addresses/{addressId}` |
| Address deletion | `DELETE /addresses/{addressId}` |
| Buy Again | — |
| Recommendations | — |
| Gift Points | — |
| Related Products | — |

---

**Status: Reviewed — Ready for Spring Boot Implementation**
