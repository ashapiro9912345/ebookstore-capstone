# Phase 8 — AI Review Submission: 3-Minute Video Script

**Purpose:** A tight, time-boxed storyboard for the ~3-minute AI-review recording.
**Emphasis:** Lead with the working application; mention the AWS Kiro agentic process briefly.
**Rubric covered:** Agentic IDE usage · Core deliverables (API design) · Working application · Best practices · (optional) Deployability.

> Target spoken length: ~400 words (~3 minutes at a natural pace). Read the
> **Narration** column aloud top-to-bottom; do the **On-screen** action as you speak.

---

## Before Recording (off-camera, pre-staged)

Do all of this before you hit record so the on-camera part fits in 3 minutes:

```bash
# Terminal A — PostgreSQL running; reset the demo DB to a clean, known baseline
export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH"
psql -d ebookstore -f src/main/resources/db/reset.sql
psql -d ebookstore -f src/main/resources/db/data.sql

# Terminal B — build once and start the app (leave it running)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export DB_URL="jdbc:postgresql://localhost:5432/ebookstore"
export DB_USERNAME="ebookstore"
export DB_PASSWORD="<your local password>"
export JWT_SECRET="<your base64 32-byte secret>"
mvn -q clean package
java -jar target/ebookstore-1.0.0-SNAPSHOT.jar   # wait for "Started EbookstoreApplication"
```

Have ready, visible and arranged:
- **Terminal A** — `psql` connected to `ebookstore` (for the persistence proof).
- **Terminal B** — app already running (so you don't spend airtime on startup).
- **Terminal C** — project root, with `BASE=http://localhost:8080/api` already set, for the live curl calls.
- **Editor** — repo open; `docs/api-design/openapi.yaml` and `docs/prompts/` ready in tabs.
- **Browser** — the (now public) GitHub repo page.

Clean baseline after reset: 1 demo user, 3 categories, 3 brands, 6 books (Clean Code = book id 2, price 42.50, stock 8), 1 address (id 1), 0 orders.

```bash
# Terminal C — set once, off-camera
BASE=http://localhost:8080/api
```

---

## The Script (on-camera, ~3:00)

| Time | On-screen | Narration |
|------|-----------|-----------|
| **0:00–0:20** | Editor showing the repo root + README title. | "This is the backend for an e-commerce bookstore, built entirely with an agentic IDE — AWS Kiro. The stack is Java 21, Spring Boot 3, and PostgreSQL. I drove it phase by phase: requirements, data model, API, implementation, and tests." |
| **0:20–0:35** | Expand `src/main/java/com/bookstore/` then the `docs/` folder. | "It's a modular monolith — Controller to Service to Repository to PostgreSQL — with DTOs kept separate from the JPA entities. Every phase has a documented artifact under `docs/`." |
| **0:35–0:50** | Terminal B visible (app running); switch to Terminal C. Run the login curl. | "Let me show it running against PostgreSQL. First I log in — the demo customer — and get back a JWT." |
| **0:50–1:05** | Run the two search calls. | "The catalogue is public. I can search books by title — 'clean' finds Clean Code — and by author — 'asimov' finds Foundation." |
| **1:05–1:25** | Run add-to-cart, then checkout. | "Now a protected flow with the Bearer token: I add two copies of Clean Code to the cart, then check out against address one with a simulated card payment." |
| **1:25–1:45** | Point at the checkout JSON response. | "The order comes back CONFIRMED — total 85.00, a simulated payment reference, an address snapshot, and the unit price locked at 42.50. No real processor, and no card data is ever stored." |
| **1:45–1:55** | Run the cancel curl. | "I cancel the order within the 48-hour window — status goes to CANCELLED." |
| **1:55–2:15** | Switch to Terminal A (`psql`); run the stock query. | "And this is the real proof: in PostgreSQL, Clean Code's stock went 8 to 6 at checkout, then back to 8 after the cancellation. The side effects are actually persisted." |
| **2:15–2:35** | Editor → `docs/api-design/openapi.yaml`; scroll the paths. | "The core deliverable is the API contract — this OpenAPI spec defines all 14 endpoints. The implementation was built to match it, and I verified conformance across every endpoint." |
| **2:35–2:50** | Terminal: show `mvn test` summary (pre-run or run live if time). | "On best practices: 57 automated tests pass against real PostgreSQL — no H2 substitution — plus JWT and BCrypt security, structured error responses, and no secrets in the repo." |
| **2:50–3:00** | Editor → `docs/prompts/`; then browser → GitHub repo. | "And it was all built this way with Kiro — one prompt per phase, each artifact reviewed before the next. The full code and docs are in the GitHub repo linked below." |

---

## Exact Commands (Terminal C — condensed happy path)

```bash
# Login — capture the JWT
TOKEN=$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
  -d '{"email":"demo@bookstore.com","password":"demo1234"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')

# Search (title, then author)
curl -s "$BASE/books?title=clean"
curl -s "$BASE/books?authorName=asimov"

# Add 2x Clean Code (book id 2) to the cart
curl -s -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -X POST "$BASE/cart/items" -d '{"bookId":2,"quantity":2}'

# Checkout against address 1 with a simulated card payment
curl -s -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -X POST "$BASE/orders" -d '{"addressId":1,"paymentMethod":"CREDIT_CARD"}'

# Cancel the order (use the id returned at checkout, e.g. 1)
curl -s -H "Authorization: Bearer $TOKEN" -X POST "$BASE/orders/1/cancel"
```

```sql
-- Terminal A (psql): the persistence proof — stock 8 -> 6 at checkout, 6 -> 8 after cancel
SELECT id, title, stock_quantity FROM books WHERE id = 2;
```

```bash
# Optional, if you want tests on-camera (pre-run off-camera to save time):
mvn clean test   # BUILD SUCCESS — Tests run: 57, Failures: 0, Errors: 0
```

---

## Segment → Rubric Mapping

| Segment | Rubric criterion |
|---------|------------------|
| 0:00–0:20 Hook / stack | Agentic IDE usage (framing) |
| 0:20–0:35 Repo + architecture | Best practices |
| 0:35–2:15 Live demo + persistence proof | **Working application** |
| 2:15–2:35 OpenAPI contract | **Core deliverables (API design)** |
| 2:35–2:50 Tests + security + no secrets | Best practices |
| 2:50–3:00 `docs/prompts/` + repo link | **Agentic IDE usage** |
| (Pre-staged local run via documented env vars) | Deployability (optional) |

---

## Delivery Tips

- Rehearse once with a timer. If you run long, drop the author search (1:05) and the on-camera `mvn test` (pre-run it and just show the summary line).
- Keep the checkout JSON on screen long enough to point at `status`, `totalAmount`, and `simulatedPaymentReference`.
- The persistence query (1:55) is the single most convincing moment — don't rush it.
- Reset the demo DB again after recording (`reset.sql` + `data.sql`) if you plan a second take.
