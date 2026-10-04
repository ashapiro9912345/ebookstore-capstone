# Prompt 5 — Configure PostgreSQL and Run Locally

Act as a Spring Boot and PostgreSQL Developer.

Use the implemented Spring Boot application and the approved data model.

Prepare the application to run locally against PostgreSQL.

## Requirements

Use PostgreSQL as the application database.

Do not use H2 or MySQL as the primary runtime database.

## Configure

1. PostgreSQL database/schema expectations
2. Spring datasource configuration
3. JPA/Hibernate settings required for local execution
4. Environment-variable placeholders for database credentials
5. Application name and server port
6. Database initialization/migration approach using the simplest reliable method
7. Seed/demo data needed to exercise the bookstore workflow

If migration scripts are useful, generate only what is needed for this project.

Do not introduce Docker solely to run PostgreSQL.

## Demo Data

Create enough data to demonstrate:

- categories
- books
- stock quantities
- at least one customer suitable for testing login
- delivery address data where appropriate

Never commit real secrets.

## Local Validation

Build and run the project using Maven.

Verify:

- application builds successfully
- application starts without errors
- PostgreSQL connection succeeds
- schema/tables are created or migrated correctly
- seed data loads successfully
- application can read/write PostgreSQL

Fix local startup/configuration errors you encounter.

## Output

Create/update:

`05-local-run-guide.md`

Include:
- PostgreSQL prerequisites
- required environment variables
- database creation/setup commands where needed
- Maven build command
- Maven run command
- expected successful startup indicators
- troubleshooting notes for any issue actually encountered

End with:

**Status: Local Application Running — Ready for API Testing**
