# Phase 7 — Capstone Demonstration Guide

**Phase:** 7 — Final Review, GitHub, and Demonstration Readiness
**Scope:** Approved MVP only. No new functionality.

A focused ~8–12 minute walkthrough for the capstone recording. It shows the
AI-assisted process, the running application against PostgreSQL, the full MVP
customer journey, test evidence, and the repository.

---

## 0. Before Recording (setup, off-camera)

```bash
# Terminal A — ensure PostgreSQL is running and the demo DB is at a clean baseline
export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH"
psql -d ebookstore -f src/main/resources/db/reset.sql
psql -d ebookstore -f src/main/resources/db/data.sql
```

Have ready:
- Terminal A: `psql` connected to `ebookstore` (for showing persistence).
- Terminal B: project root (for running the app, tests, curl).
- Editor/IDE open on the repo.
- Postman with the collection + environment imported (optional).
- The GitHub repository page in a browser.

Clean baseline: 1 demo user, 6 books (Clean Code stock 8), 1 address, 0 orders.

---

## 1. Brief Project Structure (~30s)

Show the repo tree in the editor:
- `src/main/java/com/bookstore/` organized by capability (auth, catalogue, cart,
  address, order, security, common).
- `docs/` with one artifact per phase.

Say: *"A modular monolith — Controller → Service → Repository → PostgreSQL — with
DTOs separated from JPA entities."*

## 2. Requirements / Architecture (~45s)

Open:
- `docs/requirements/requirements.md` — functional requirements.
- `docs/data-model/02-data-model.md` — the ER model.
- `docs/api-design/openapi.yaml` — the approved API contract.

Say: *"Each of these was produced and reviewed before any code was written, using
AWS Kiro: Requirements → Data Model → OpenAPI → Implementation."*

## 3. PostgreSQL Running Locally (~30s)

In Terminal A:

```sql
\dt
SELECT id, title, price, stock_quantity FROM books ORDER BY id;
```

Show 9 tables and the 6 seeded books.

## 4. Spring Boot Application Startup (~45s)

In Terminal B (uses the local run helper or explicit env vars):

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export DB_URL="jdbc:postgresql://localhost:5432/ebookstore"
export DB_USERNAME="ebookstore"
export DB_PASSWORD="<your local password>"
export JWT_SECRET="<your base64 secret>"
java -jar target/ebookstore-1.0.0-SNAPSHOT.jar
```

Point out `Started EbookstoreApplication` and `Tomcat started on port 8080`.
Say: *"It starts with `ddl-auto=validate`, so a clean start proves the JPA mappings
match the PostgreSQL schema."*

> The following steps use `curl` in a third terminal (or Postman). Set
> `BASE=http://localhost:8080/api`.

## 5. Login (~30s)

```bash
BASE=http://localhost:8080/api
TOKEN=$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
  -d '{"email":"demo@bookstore.com","password":"demo1234"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')
echo "token length: ${#TOKEN}"
```

Say: *"Login returns a JWT; every protected call sends it as a Bearer token."*

## 6. Browse / Search Books (~45s)

```bash
curl -s "$BASE/categories"
curl -s "$BASE/books?size=3"
curl -s "$BASE/books?title=clean"       # search by title
curl -s "$BASE/books?authorName=asimov" # search by author
curl -s "$BASE/books/2"                 # book detail (Clean Code)
```

## 7. Add Book to Cart (~30s)

```bash
curl -s -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -X POST "$BASE/cart/items" -d '{"bookId":2,"quantity":2}'
```

Show `totalAmount: 85.00`.

## 8. View / Update Cart (~30s)

```bash
curl -s -H "Authorization: Bearer $TOKEN" "$BASE/cart"           # note the itemId (1)
curl -s -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -X PUT "$BASE/cart/items/1" -d '{"quantity":2}'
```

## 9. View / Select Address (~20s)

```bash
curl -s -H "Authorization: Bearer $TOKEN" "$BASE/addresses"      # note the id (1)
```

## 10. Checkout with Simulated Payment (~45s)

```bash
curl -s -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -X POST "$BASE/orders" -d '{"addressId":1,"paymentMethod":"CREDIT_CARD"}'
```

## 11. Purchase Confirmation (~20s)

Point out in the response: `status: CONFIRMED`, `totalAmount: 85.00`,
`simulatedPaymentReference: SIM-...`, the `shippingAddress` snapshot, and
`items[].unitPrice: 42.50` (price locked at purchase).

Say: *"Payment is simulated — no real processor, and no card data is ever accepted
or stored."*

## 12. Order History (~20s)

```bash
curl -s -H "Authorization: Bearer $TOKEN" "$BASE/orders"
```

## 13. Cancel the Order (~30s)

```bash
# use the order id returned at checkout (e.g. 1)
curl -s -H "Authorization: Bearer $TOKEN" -X POST "$BASE/orders/1/cancel"
```

Show `status: CANCELLED`.

## 14. Show PostgreSQL Persistence (~45s)

In Terminal A, demonstrate the side effects were persisted:

```sql
-- Clean Code stock: 8 -> 6 at checkout, restored to 8 after cancel
SELECT id, title, stock_quantity FROM books WHERE id = 2;
-- Order recorded with snapshot + locked price
SELECT id, status, total_amount, snap_city FROM orders;
SELECT order_id, quantity, unit_price FROM order_items;
-- Cart cleared after checkout
SELECT count(*) AS cart_items FROM cart_items;
```

## 15. Automated Test Result (~45s)

In Terminal B:

```bash
mvn clean test
```

Point out: **BUILD SUCCESS**, **Tests run: 57, Failures: 0, Errors: 0**, and that the
integration tests run against **real PostgreSQL** (`ebookstore_test`), not H2.
Optionally open `docs/06-test-results.md`.

## 16. OpenAPI & Postman (~30s)

- Open `docs/api-design/openapi.yaml` — the contract the implementation matches.
- Open Postman with `docs/api-testing/` imported; run a couple of requests
  (Login + Checkout) and the negative tests folder to show structured errors.

## 17. GitHub Repository (~20s)

Show the GitHub repo page — code, `docs/`, and the per-phase history
(`git log --oneline`).

---

## Negative Scenarios (optional, if time allows)

```bash
# Invalid login -> 401 INVALID_CREDENTIALS
curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
  -d '{"email":"demo@bookstore.com","password":"wrong"}'
# No token -> 401 UNAUTHORIZED
curl -s "$BASE/cart"
# Insufficient stock -> 422 INSUFFICIENT_STOCK
curl -s -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -X POST "$BASE/cart/items" -d '{"bookId":6,"quantity":99}'
```

---

## After Recording

Reset the demo database so the next run starts clean:

```bash
psql -d ebookstore -f src/main/resources/db/reset.sql
psql -d ebookstore -f src/main/resources/db/data.sql
```
