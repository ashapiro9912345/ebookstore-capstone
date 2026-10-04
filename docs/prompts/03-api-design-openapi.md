# Prompt 3 — API Design and OpenAPI Specification

Act as a Principal REST API Architect.

Use only the approved:
- `requirements.md`
- `02-data-model.md`

Design the REST API contract for the approved MVP and generate the OpenAPI specification.

## Scope

Include only APIs required for the approved MVP, such as:

- Authentication/login
- Books/catalog
- Categories
- Search/filtering required by the MVP
- Cart
- Delivery addresses
- Checkout
- Simulated payment behavior
- Orders
- Order history
- Order cancellation

Do not automatically add secondary features.

## First: API Design

Before writing OpenAPI, define:

- Resource
- Purpose
- HTTP method
- URI
- Request model
- Response model
- Validation rules
- Expected HTTP status codes
- Error cases
- Authentication requirement

Use RESTful, resource-oriented conventions.

Use standard HTTP methods appropriately:
- GET
- POST
- PUT/PATCH where justified
- DELETE where justified

## Then: Generate OpenAPI

Generate a valid OpenAPI 3.x specification with:

- Tags
- Paths
- Request schemas
- Response schemas
- Reusable components
- Validation constraints
- Standard structured error responses
- Authentication/security definition selected using the simplest appropriate Spring Security approach
- Pagination only where it is useful
- Filtering/search parameters only where required

Do not introduce enterprise API patterns that the MVP does not need.

## Review the Contract

Before finalizing, review the generated API for:

- consistency with requirements
- consistency with the data model
- REST naming consistency
- correct status codes
- validation coverage
- error handling
- security boundaries
- accidental secondary-scope features

Fix issues before producing the final specification.

## Output

Create:

`03-api-design.md`

and:

`openapi.yaml`

End the design document with:

**Status: Draft — Awaiting OpenAPI Review**

Do not generate Spring Boot code yet.
