# Prompt 2 — PostgreSQL Data Model Design

Act as a Senior Data Architect and Spring Boot backend architect.

Use the approved `requirements.md` as the authoritative requirements baseline.

Design the simplest PostgreSQL relational data model that fully supports the approved MVP.

## Do Not Generate Yet

Do not generate:
- SQL DDL
- JPA entities
- repositories
- REST APIs
- OpenAPI
- application code
- Docker or AWS infrastructure

This phase is data-model design only.

## Design

1. Identify required MVP entities
2. Define attributes and PostgreSQL data types
3. Recommend a consistent primary-key strategy
4. Define relationships and cardinalities
5. Define foreign keys and delete behavior
6. Preserve historical order item prices and quantities
7. Preserve the delivery address used by an order
8. Support one active cart per customer
9. Prevent duplicate book rows within the same cart
10. Support simple stock quantity behavior
11. Define the minimum order lifecycle/statuses
12. Identify meaningful database constraints
13. Recommend only justified indexes
14. Use minimal audit timestamps such as `created_at` and `updated_at`
15. Produce a Mermaid ER diagram
16. Produce a table-level data dictionary
17. Trace tables/entities back to approved requirements

## Resolve These Ambiguities

Recommend the simplest MVP approach for:

- Brand versus publisher
- Whether simulated payment needs its own persisted table
- Order address snapshotting
- Historical order pricing
- Authentication-related customer data without choosing the authentication mechanism yet

Do not store real card numbers, CVVs, expiration dates, or real payment credentials.

Avoid over-normalization and enterprise-only features.

## Output

Create:

`02-data-model.md`

End with:

**Status: Draft — Awaiting Data Model Review**

Do not proceed to SQL or APIs.
