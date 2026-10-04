# Prompt 1 — Requirements Analysis

Act as a Software Architect and Senior Backend Developer.

Review the provided AI Specialist Capstone instructions, bookstore wireframes, UI screens, and architecture diagram.

Use the existing Kiro project instructions as the governing standards.

Your task is to reverse-engineer and document the backend requirements for the bookstore application.

## Do Not Generate Yet

Do not generate:
- Application code
- SQL
- Database schemas
- OpenAPI specifications
- API endpoint definitions
- Docker or AWS infrastructure

This phase is requirements analysis only.

## Produce

1. Functional requirements
2. Non-functional requirements
3. User roles
4. User stories
5. Business capabilities
6. Logical domain boundaries
7. Minimal backend modules
8. Logical API domains
9. Core data entities
10. Data ownership matrix
11. Business rules
12. Assumptions and ambiguities
13. MVP scope
14. Secondary / optional scope
15. Out-of-scope items
16. Requirements traceability to the source wireframes/instructions

## Scope Principle

Prioritize the minimum end-to-end customer journey:

Browse books → Select book → Add to cart → Select delivery address → Checkout → Simulated payment → Order confirmation → Order history → Cancel order within 48 hours

Treat recommendations, Buy Again, gift points, reviews, wishlist, and related-product enhancements as secondary unless needed for the core demonstration.

Do not silently invent requirements.

When something is unclear:
- identify the ambiguity,
- recommend the simplest reasonable interpretation,
- keep the project aligned to capstone completion.

## Output

Create/update:

`requirements.md`

End with:

**Status: Draft — Awaiting Requirements Review**

Do not proceed to data modeling.
