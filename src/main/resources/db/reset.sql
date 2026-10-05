-- ============================================================================
-- E-Bookstore — demo/test database reset
--
-- Purpose: return the database to a KNOWN, REPEATABLE starting state so that
-- tests and the end-to-end API journey can be reproduced deterministically.
--
-- What it does:
--   1. Removes all transactional + demo rows (orders, carts, addresses, users,
--      books, brands, categories).
--   2. Restarts identity sequences so IDs are predictable (users.id = 1,
--      books.id = 1..6, address.id = 1, etc.).
--
-- After running this script, re-apply db/data.sql to reload the demo data.
--
-- Safe to run repeatedly. Local demo/test use only.
-- ============================================================================

-- TRUNCATE with CASCADE clears dependent tables (order_items, cart_items)
-- automatically. RESTART IDENTITY resets the surrogate-key sequences so the
-- reloaded demo data always gets the same IDs.
TRUNCATE TABLE
    order_items,
    orders,
    cart_items,
    carts,
    addresses,
    books,
    brands,
    categories,
    users
RESTART IDENTITY CASCADE;
