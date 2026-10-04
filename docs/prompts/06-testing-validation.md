# Prompt 6 — Test APIs and Validate the MVP

Act as a Senior QA Automation Engineer and Spring Boot Developer.

Use:
- `requirements.md`
- `openapi.yaml`
- the running Spring Boot application
- PostgreSQL

Validate the complete approved MVP.

## Automated Tests

Create and execute appropriate tests for:

1. Service business logic
2. Controller/API behavior
3. Persistence against PostgreSQL where practical
4. Authentication/security behavior
5. Validation failures
6. Not-found scenarios
7. Cart workflow
8. Checkout workflow
9. Simulated payment success/failure behavior
10. Order creation
11. Order history
12. Order cancellation inside and outside the 48-hour window
13. Stock validation and stock decrement

Use:

- JUnit 5
- Spring Boot Test
- Mockito where appropriate

Use PostgreSQL for persistence/integration validation. Do not quietly substitute H2 for database behavior that needs to be verified against PostgreSQL.

## API Contract Validation

Verify that implemented endpoints match `openapi.yaml` for:

- paths
- HTTP methods
- request bodies
- response bodies
- status codes
- validation
- error responses

Fix implementation/specification inconsistencies intentionally and document any approved contract change.

## Manual/API Client Testing

Create a practical API test sequence for Postman or Insomnia covering:

Browse books → Select book → Add to cart → Select/add address → Checkout → Simulated payment → Confirmation → Order history → Cancel order

Include important negative cases.

Verify persistent operations are actually saved and retrieved from PostgreSQL.

## Output

Create/update:

`06-test-results.md`

Include:
- tests executed
- pass/fail summary
- defects found
- fixes made
- remaining known issues
- OpenAPI conformance result
- end-to-end MVP result

Also provide an importable Postman collection if practical, or a clearly documented Insomnia/Postman request sequence.

End with:

**Status: MVP Validated — Ready for Final Review**
