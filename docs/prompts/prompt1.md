# Prompt 1 — Analyze Bookstore Wireframes and Define Requirements

Act as a Software Architect and Senior Backend Developer.

Review the provided AI Specialist Capstone instructions, bookstore wireframes, UI screens, and architecture diagram.

Your task is to reverse-engineer and document the backend requirements for the bookstore application.

## Important Constraints

Do **not** generate:
- Application code
- SQL
- Database schemas
- OpenAPI specifications
- API endpoint definitions
- AWS infrastructure
- Docker/container configuration

This step is requirements analysis only.

Follow the existing Kiro project instructions as the governing technical and architectural standards.

The implementation will ultimately use:

- Java 21
- Spring Boot 3.x
- Maven
- PostgreSQL
- Spring Data JPA / Hibernate
- REST APIs
- OpenAPI 3.x
- JUnit 5 / Spring Boot Test
- Local execution
- Git/GitHub
- Postman or Insomnia for API testing

Do not introduce microservices, cloud hosting, containers, Kubernetes, or additional infrastructure.

## Produce the Following

### 1. Functional Requirements

Identify the backend functionality required to support the provided bookstore screens and customer journeys.

For each requirement, include:
- Requirement ID
- Description
- Related user action or screen
- Priority: MVP or Secondary

### 2. Non-Functional Requirements

Identify only the non-functional requirements reasonably necessary for this capstone, including:

- Maintainability
- Validation
- Security
- Data integrity
- Error handling
- Performance at a reasonable demonstration scale
- Testability

Do not invent enterprise-scale availability, scalability, or infrastructure requirements that are not supported by the capstone.

### 3. User Roles

Identify only user roles supported or clearly implied by the provided requirements and screens.

Do not invent administrative or operational roles unless the source material requires them.

### 4. User Stories

Create concise user stories for the identified functionality.

Use the format:

"As a [role], I want to [action] so that [business outcome]."

Group user stories by business capability.

### 5. Business Capabilities

Identify the major bookstore capabilities, such as:

- Product/catalog browsing
- Categories
- Shopping cart
- User/customer information
- Delivery address
- Checkout
- Payment
- Orders
- Order history
- Purchase confirmation

Only include additional capabilities when supported by the provided capstone materials.

### 6. Domain Boundaries

Identify logical domain boundaries for organizing the backend.

Keep the solution as a modular monolithic Spring Boot application.

Domain boundaries are for code organization and responsibility separation only. Do not propose microservices.

### 7. Backend Modules

Recommend a minimal set of backend modules or packages necessary to support the MVP.

Explain the responsibility of each module briefly.

### 8. API Domains

Identify the logical API resource areas that will eventually require REST APIs.

Do not design URI paths, HTTP methods, request DTOs, or responses yet.

### 9. Core Data Entities

Identify the data entities implied by the requirements.

For each entity provide:
- Entity name
- Business purpose
- Important information it is expected to contain
- Likely relationships to other entities

Do not design database tables or columns yet.

### 10. Data Ownership Matrix

Create a simple matrix showing which logical domain owns each core entity.

Keep ownership simple and appropriate for a modular monolith.

### 11. Business Rules

Identify explicit or strongly implied business rules from the provided material.

Clearly distinguish between:

- Explicit requirements
- Reasonable assumptions
- Ambiguous requirements requiring a decision

Do not silently invent business rules.

### 12. Assumptions and Ambiguities

Create a separate section containing:

- Assumptions required to proceed
- Missing information
- Conflicting requirements
- Requirements that need interpretation

For each ambiguity, recommend the simplest implementation appropriate for completing the capstone.

### 13. MVP Scope

Identify the **minimum functionality necessary to successfully complete and demonstrate the bookstore capstone**.

Prioritize this core customer journey:

Browse books → Select book → Add to cart → Select delivery address → Checkout → Simulated payment → Order confirmation → Order history

### 14. Secondary / Optional Scope

Identify functionality shown or implied by the materials that can reasonably be deferred until after the MVP.

Examples may include:

- Recommendations
- Buy Again
- Gift points
- Related products
- Wishlist
- Reviews

Include them only when supported by the provided material.

### 15. Out-of-Scope Items

Explicitly identify functionality that should not be implemented during the initial MVP unless required by the capstone.

Focus on avoiding unnecessary complexity.

### 16. Requirements Traceability

Where practical, indicate which wireframe, screen, or capstone requirement supports each major requirement.

This should demonstrate that the implementation is derived from the provided project material rather than invented.

## Working Principle

Optimize for:

**Correctness → Simplicity → Traceability → Completion**

The purpose of this step is to establish an approved requirements baseline before designing the PostgreSQL data model or REST API.

If something is unclear, document the ambiguity and recommend the simplest reasonable interpretation.

Do not proceed beyond requirements analysis.

Output the result as a structured Markdown requirements document.