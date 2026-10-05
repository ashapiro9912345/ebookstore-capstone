# API Test Collection (Postman / Insomnia)

Importable API test assets for the E-Bookstore MVP.

## Files

- `ebookstore.postman_collection.json` — the request collection (happy path + negative tests, with inline test assertions).
- `ebookstore.postman_environment.json` — environment variables (base URL, demo credentials, and auto-captured `token`/`orderId`/etc.).

Both import directly into Postman. Insomnia can import the Postman collection as well.

## Prerequisites

1. The application is running locally. See `docs/05-local-run-guide.md`.
   Base URL: `http://localhost:8080/api`.
2. The demo data is loaded (`db/data.sql`), which creates the demo customer
   `demo@bookstore.com` / `demo1234`.

> The demo credentials are a **local demonstration account** only — not a real
> secret. No tokens, passwords, or card data are hard-coded anywhere real.

## How to run (Postman)

1. Import both JSON files.
2. Select the **E-Bookstore Local** environment.
3. Run the folders in order (or use the Collection Runner):
   1. **1. Auth → Login** — authenticates and stores the JWT in `{{token}}`.
   2. **2. Catalogue** — list categories, list/search books, book detail
      (captures a `{{bookId}}`).
   3. **3. Cart** — get cart, add item (captures `{{cartItemId}}`), update, remove.
   4. **4. Addresses** — list (captures `{{addressId}}`) and create.
   5. **5. Orders** — checkout (captures `{{orderId}}`), history, detail, cancel.
   6. **6. Negative tests** — invalid login, missing token, bad payload,
      unknown book, insufficient stock, unknown address, unknown order.

Each request has test scripts asserting the expected status code and, for errors,
the structured `code` field.

## Token handling

The **Login** request's test script runs:

```js
pm.environment.set('token', pm.response.json().token);
```

Every protected request sends `Authorization: Bearer {{token}}`, so you never
copy the token by hand. Re-run **Login** if the token expires (24h default).

## Manual request sequence (fallback)

If you prefer raw requests (curl / Insomnia), the end-to-end sequence is:

| # | Method | Path | Auth | Body |
|---|--------|------|------|------|
| 1 | POST | `/auth/login` | no | `{"email":"demo@bookstore.com","password":"demo1234"}` → copy `token` |
| 2 | GET | `/categories` | no | — |
| 3 | GET | `/books?title=clean` | no | — |
| 4 | GET | `/books/2` | no | — |
| 5 | POST | `/cart/items` | Bearer | `{"bookId":2,"quantity":2}` |
| 6 | GET | `/cart` | Bearer | — (note the `itemId`) |
| 7 | PUT | `/cart/items/{itemId}` | Bearer | `{"quantity":2}` |
| 8 | GET | `/addresses` | Bearer | — (note the `id`) |
| 9 | POST | `/orders` | Bearer | `{"addressId":1,"paymentMethod":"CREDIT_CARD"}` → `201 CONFIRMED` |
| 10 | GET | `/orders` | Bearer | — |
| 11 | GET | `/orders/{orderId}` | Bearer | — |
| 12 | POST | `/orders/{orderId}/cancel` | Bearer | — → `200 CANCELLED` |

Key negative checks:

| Case | Request | Expected |
|------|---------|----------|
| Invalid login | POST `/auth/login` wrong password | `401 INVALID_CREDENTIALS` |
| Missing token | GET `/cart` no header | `401 UNAUTHORIZED` |
| Invalid payload | POST `/auth/login` bad email | `400 VALIDATION_ERROR` |
| Unknown book | GET `/books/999999` | `404 NOT_FOUND` |
| Insufficient stock | POST `/cart/items` qty 100000 | `422 INSUFFICIENT_STOCK` |
| Unknown address | POST `/orders` addressId 999999 | `404 NOT_FOUND` |
| Unknown order | POST `/orders/999999/cancel` | `404 NOT_FOUND` |
