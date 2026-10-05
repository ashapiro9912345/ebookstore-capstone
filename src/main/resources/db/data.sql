-- ============================================================================
-- E-Bookstore — demonstration seed data
--
-- Loads just enough data to exercise the MVP workflow:
--   categories, brands, books (with stock), one demo customer, one address.
--
-- Idempotent: ON CONFLICT DO NOTHING on natural keys so re-running is safe.
--
-- Demo customer:
--   email:    demo@bookstore.com
--   password: demo1234   (stored only as a BCrypt hash below — never plaintext)
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Categories
-- ---------------------------------------------------------------------------
INSERT INTO categories (name, description) VALUES
    ('Technology',       'Software, programming, and computing'),
    ('Science Fiction',  'Speculative fiction exploring futuristic concepts'),
    ('Business',         'Management, entrepreneurship, and economics')
ON CONFLICT (name) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Brands / publishers
-- ---------------------------------------------------------------------------
INSERT INTO brands (name) VALUES
    ('Addison-Wesley'),
    ('O''Reilly Media'),
    ('Penguin Books')
ON CONFLICT (name) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Books (reference categories/brands by name so IDs need not be known)
-- ---------------------------------------------------------------------------
INSERT INTO books (title, author, isbn, description, price, stock_quantity, delivery_estimate_days, category_id, brand_id)
SELECT 'The Pragmatic Programmer', 'David Thomas, Andrew Hunt', '9780135957059',
       'A guide to pragmatic programming practices', 39.99, 15, 3,
       (SELECT id FROM categories WHERE name = 'Technology'),
       (SELECT id FROM brands     WHERE name = 'Addison-Wesley')
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780135957059');

INSERT INTO books (title, author, isbn, description, price, stock_quantity, delivery_estimate_days, category_id, brand_id)
SELECT 'Clean Code', 'Robert C. Martin', '9780132350884',
       'A handbook of agile software craftsmanship', 42.50, 8, 4,
       (SELECT id FROM categories WHERE name = 'Technology'),
       (SELECT id FROM brands     WHERE name = 'Addison-Wesley')
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780132350884');

INSERT INTO books (title, author, isbn, description, price, stock_quantity, delivery_estimate_days, category_id, brand_id)
SELECT 'Designing Data-Intensive Applications', 'Martin Kleppmann', '9781449373320',
       'The big ideas behind reliable, scalable, maintainable systems', 55.00, 12, 5,
       (SELECT id FROM categories WHERE name = 'Technology'),
       (SELECT id FROM brands     WHERE name = 'O''Reilly Media')
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9781449373320');

INSERT INTO books (title, author, isbn, description, price, stock_quantity, delivery_estimate_days, category_id, brand_id)
SELECT 'Dune', 'Frank Herbert', '9780441013593',
       'A landmark of science fiction set on the desert planet Arrakis', 18.99, 20, 2,
       (SELECT id FROM categories WHERE name = 'Science Fiction'),
       (SELECT id FROM brands     WHERE name = 'Penguin Books')
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780441013593');

INSERT INTO books (title, author, isbn, description, price, stock_quantity, delivery_estimate_days, category_id, brand_id)
SELECT 'The Lean Startup', 'Eric Ries', '9780307887894',
       'How constant innovation creates radically successful businesses', 24.00, 10, 4,
       (SELECT id FROM categories WHERE name = 'Business'),
       (SELECT id FROM brands     WHERE name = 'Penguin Books')
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780307887894');

-- A book with no brand to exercise the nullable brand path
INSERT INTO books (title, author, isbn, description, price, stock_quantity, delivery_estimate_days, category_id, brand_id)
SELECT 'Foundation', 'Isaac Asimov', '9780553293357',
       'The first novel in the Foundation series', 16.50, 5, 2,
       (SELECT id FROM categories WHERE name = 'Science Fiction'),
       NULL
WHERE NOT EXISTS (SELECT 1 FROM books WHERE isbn = '9780553293357');

-- ---------------------------------------------------------------------------
-- Demo customer
-- password "demo1234" as a BCrypt hash (NFR-03: never plaintext)
-- ---------------------------------------------------------------------------
INSERT INTO users (full_name, email, password_hash)
VALUES ('Demo Customer', 'demo@bookstore.com',
        '$2a$10$kK1ZfmOq8okabkdnXSAMk.gZJyz2feFdVb6v.YaPxAPeBJtBtc7Ri')
ON CONFLICT (email) DO NOTHING;

-- ---------------------------------------------------------------------------
-- One delivery address for the demo customer
-- ---------------------------------------------------------------------------
INSERT INTO addresses (user_id, recipient_name, street_line1, street_line2, city, state, postal_code, country)
SELECT u.id, 'Demo Customer', '123 Main Street', 'Apt 4B', 'Austin', 'TX', '78701', 'US'
FROM users u
WHERE u.email = 'demo@bookstore.com'
  AND NOT EXISTS (
      SELECT 1 FROM addresses a
      WHERE a.user_id = u.id AND a.street_line1 = '123 Main Street' AND a.postal_code = '78701'
  );
