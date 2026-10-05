# Phase 5 — Local Run Guide (PostgreSQL)

**Source prompt:** `docs/prompts/05-postgresql-local-run.md`
**Authoritative artifacts:** `requirements.md`, `02-data-model.md`, `03-api-design.md`, `openapi.yaml`, `04-implementation-notes.md`
**Phase:** 5 — PostgreSQL Configuration and Local Run

This guide explains how to run the E-Bookstore backend locally against a real
PostgreSQL database. It is local-first: no Docker, cloud, or external services.

---

## 1. Prerequisites

| Tool | Version used | Notes |
|------|--------------|-------|
| JDK | Temurin 21 | `java -version` must report 21 |
| Maven | 3.10 | or the system Maven on PATH |
| PostgreSQL | 16 (16.15) | local server, no container |

Install on macOS (Homebrew):

```bash
brew install --cask temurin@21      # JDK 21
brew install maven                  # Maven
brew install postgresql@16          # PostgreSQL 16
brew services start postgresql@16   # start the local server
```

Point your shell at JDK 21 for build/run commands:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

If `psql` is not on your PATH:

```bash
export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH"
```

---

## 2. Database, Schema, and Credentials

- **Database name:** `ebookstore`
- **Application role:** `ebookstore`
- **Schema:** default `public` schema; 9 tables defined in `src/main/resources/db/schema.sql`.

### Required environment variables

Credentials are never hard-coded or committed. The application reads them from the
environment (with local-only development defaults in `application.properties`):

| Variable | Example (local dev) | Purpose |
|----------|---------------------|---------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/ebookstore` | JDBC URL |
| `DB_USERNAME` | `ebookstore` | DB role |
| `DB_PASSWORD` | *(your local password)* | DB password — **set this yourself** |
| `JWT_SECRET` | *(Base64 32-byte secret)* | JWT signing key |
| `JWT_EXPIRATION_MS` | `86400000` | token lifetime (optional) |

> The password shown in examples is a local development value chosen at setup time.
> Do not reuse it anywhere real, and never commit it.

---

## 3. One-time Database Setup

Create the role and database (replace the password with your own local value):

```bash
export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH"
export DB_PASSWORD='choose_a_local_password'

psql -d postgres -v ON_ERROR_STOP=1 <<SQL
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'ebookstore') THEN
      CREATE ROLE ebookstore LOGIN PASSWORD '${DB_PASSWORD}';
   END IF;
END
$$;
SQL

# Create the database owned by the ebookstore role (skips if it already exists)
psql -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='ebookstore'" | grep -q 1 \
  || createdb -O ebookstore ebookstore
```

---

## 4. Schema Initialization

**Approach:** a plain SQL DDL script, applied with `psql`. This is the simplest
reliable method for the capstone and keeps the schema under version control.

- `spring.jpa.hibernate.ddl-auto=validate` — Hibernate does **not** create or alter
  tables. The schema must exist first, and Hibernate validates the entity mappings
  against it at startup. This is deliberate: it guarantees the running schema matches
  the approved data model.
- No Flyway/Liquibase is introduced (unnecessary complexity for a single local MVP).

Apply the schema (as the application role):

```bash
export PGPASSWORD="$DB_PASSWORD"
psql -h localhost -U ebookstore -d ebookstore -v ON_ERROR_STOP=1 \
     -f src/main/resources/db/schema.sql
```

The script is idempotent (`CREATE TABLE IF NOT EXISTS`, `CREATE INDEX IF NOT EXISTS`),
so it is safe to re-run.

---

## 5. Demo-Data Loading

**Approach:** a seed SQL script (`src/main/resources/db/data.sql`) applied with `psql`.
It is idempotent (`ON CONFLICT DO NOTHING` / guarded inserts).

```bash
export PGPASSWORD="$DB_PASSWORD"
psql -h localhost -U ebookstore -d ebookstore -v ON_ERROR_STOP=1 \
     -f src/main/resources/db/data.sql
```

Loaded data:

- **3 categories:** Technology, Science Fiction, Business
- **3 brands:** Addison-Wesley, O'Reilly Media, Penguin Books
- **6 books** with stock quantities and delivery estimates (one deliberately has no brand)
- **1 demo customer** — `demo@bookstore.com` / `demo1234`
  (stored only as a BCrypt hash; never plaintext — NFR-03)
- **1 delivery address** for the demo customer

> The demo password hash in `data.sql` was generated with the application's own
> `BCryptPasswordEncoder`, so login works out of the box.

---

## 6. Build

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn -DskipTests clean package
```

Produces `target/ebookstore-1.0.0-SNAPSHOT.jar`. Result: **BUILD SUCCESS**.

---

## 7. Run

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export DB_URL="jdbc:postgresql://localhost:5432/ebookstore"
export DB_USERNAME="ebookstore"
export DB_PASSWORD="your_local_password"
export JWT_SECRET="your_base64_32_byte_secret"

java -jar target/ebookstore-1.0.0-SNAPSHOT.jar
```

The API base URL is `http://localhost:8080/api`.

---

## 8. Successful Startup Evidence

Key log lines from a successful run against PostgreSQL:

```
HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@...
HikariPool-1 - Start completed.
Initialized JPA EntityManagerFactory for persistence unit 'default'
Tomcat started on port 8080 (http) with context path '/'
Started EbookstoreApplication in ~3 s
```

With `ddl-auto=validate`, reaching "Started EbookstoreApplication" proves that every
JPA entity mapping validated cleanly against the PostgreSQL schema.

---

## 9. Persistence Smoke Test (via the API)

Login, browse, and run the full checkout write-path:

```bash
BASE=http://localhost:8080/api

# Public catalogue reads
curl -s "$BASE/categories"
curl -s "$BASE/books?size=3"
curl -s "$BASE/books?title=clean"       # search filter

# Login (DB read + BCrypt verification -> JWT)
TOKEN=$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
  -d '{"email":"demo@bookstore.com","password":"demo1234"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')

# Authenticated writes
curl -s -H "Authorization: Bearer $TOKEN" "$BASE/addresses"
curl -s -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -X POST "$BASE/cart/items" -d '{"bookId":2,"quantity":2}'
curl -s -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -X POST "$BASE/orders" -d '{"addressId":1,"paymentMethod":"CREDIT_CARD"}'
curl -s -H "Authorization: Bearer $TOKEN" "$BASE/orders"
```

### Observed results

| Step | Result |
|------|--------|
| `GET /categories` | `200` — 3 categories returned |
| `GET /books` | `200` — paginated list, `totalElements: 6` |
| `GET /books?title=clean` | `200` — search returns "Clean Code" only |
| `POST /auth/login` | `200` — JWT + user summary (demo auth works) |
| `GET /addresses` | `200` — seeded address returned |
| `POST /cart/items` | `200` — cart updated, `lineTotal` / `totalAmount` computed |
| `POST /orders` | `201` — `CONFIRMED` order, `SIM-...` reference, address snapshot |
| `GET /orders` | `200` — order read back from the database |

### Verified side effects (direct DB query)

| Check | Expected | Actual |
|-------|----------|--------|
| `books.stock_quantity` for the ordered book | decremented by 2 (8 → 6) | **6** |
| `orders` row | 1 CONFIRMED, total 85.00, snapshot = Austin | ✓ |
| `order_items` row | qty 2, `unit_price` locked at 42.50 (BR-06) | ✓ |
| `cart_items` after checkout | 0 (cart cleared, DM-06) | **0** |

This confirms the application reads from and writes to PostgreSQL correctly, and that
checkout, stock decrement, address snapshotting, price locking, and cart clearing all
persist as designed.

---

## 10. Problems Encountered and Resolutions

### 10.1 `GET /books` returned 500 — `function lower(bytea) does not exist`

**Symptom:** the first catalogue listing call failed with HTTP 500. The log showed
`PSQLException: ERROR: function lower(bytea) does not exist`.

**Root cause:** an **implementation defect**, not a schema or design problem. In
`BookRepository.search`, when an optional filter parameter (`title`/`authorName`) was
bound as `null`, PostgreSQL could not infer the parameter's type and defaulted it to
`bytea`, so `LOWER(?)` resolved to the non-existent `lower(bytea)`.

**Resolution (implementation fix, no design change):** cast the string parameters in the
JPQL so their SQL type is explicit:

```jpql
WHERE (CAST(:title AS string) IS NULL
       OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%')))
  AND (CAST(:authorName AS string) IS NULL
       OR LOWER(b.author) LIKE LOWER(CONCAT('%', CAST(:authorName AS string), '%')))
```

After rebuilding, `GET /books` and `GET /books?title=...` both return `200`. The approved
OpenAPI contract and data model were unchanged.

### 10.2 Security warning about `UserDetailsService`

**Symptom:** a startup `WARN` — "UserDetailsService beans will not be used for
username/password login" — because `SecurityConfig` registers an explicit
`AuthenticationProvider` bean.

**Assessment:** harmless. The `DaoAuthenticationProvider` already has the
`UserDetailsService` set explicitly, and the JWT filter uses it directly. Login was
verified to return `200` with a valid token. No change required.

---

## 11. Phase 5 Completion Report

- **Database configuration:** PASS — PostgreSQL 16 running locally; `ebookstore`
  role and database created; credentials via environment variables only.
- **Schema:** PASS — 9 tables created from `schema.sql`, matching the approved data
  model; Hibernate `validate` passes against the live schema.
- **Demo data:** PASS — categories, brands, books (with stock), demo customer, and a
  delivery address loaded via `data.sql`.
- **Application startup:** PASS — starts in ~3 s with no errors; Tomcat on port 8080.
- **Persistence validation:** PASS — demo login works; catalogue reads work; a full
  checkout persisted an order, decremented stock, locked unit price, snapshotted the
  address, and cleared the cart.
- **Maven build:** PASS — `clean package` succeeds.
- **Remaining blockers:** none.

One implementation defect (the `lower(bytea)` search query) was found and fixed; no
approved design decision was changed.

---

**Status: Local Application Running — Ready for API Testing**
