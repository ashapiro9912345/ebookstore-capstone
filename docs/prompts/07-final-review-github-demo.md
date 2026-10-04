# Prompt 7 — Final Capstone Review, GitHub, and Demo Readiness

Act as a Principal Software Architect performing the final capstone review.

Review the complete bookstore project against:

- original capstone instructions
- approved `requirements.md`
- approved data model
- `openapi.yaml`
- Spring Boot implementation
- PostgreSQL configuration
- automated/manual test results

The goal is capstone completion, not production hardening.

## Review

Evaluate:

1. Required bookstore functionality is implemented
2. MVP customer journey works end to end
3. PostgreSQL persistence is verified
4. OpenAPI matches implementation
5. Tests cover important success/failure scenarios
6. No real payment credentials are stored
7. No passwords/secrets are committed
8. Code follows the intended Controller → Service → Repository architecture
9. DTOs remain separate from persistence entities
10. Secondary features did not unnecessarily delay MVP completion
11. README/setup instructions are sufficient for another reviewer to run the project
12. Git repository is clean and appropriate for GitHub

## Fix Only Completion-Blocking Issues

If you identify issues:
- classify them as Blocker, Important, or Optional
- fix Blockers
- fix Important issues only when low-risk and necessary for capstone quality
- record Optional improvements without expanding scope

Do not introduce Docker, AWS hosting, microservices, Kubernetes, CI/CD, or production infrastructure during this final review unless explicitly requested.

## GitHub Readiness

Prepare:

- `.gitignore`
- final `README.md`
- clear local setup/run instructions
- API documentation reference
- test instructions
- sensible commit guidance
- recommended feature branch / PR description

Do not commit secrets.

## Demo Readiness

Create a concise demo sequence covering:

1. Project objective
2. How AWS Kiro was used
3. Requirements analysis
4. Data model
5. OpenAPI contract
6. Spring Boot architecture
7. PostgreSQL persistence
8. End-to-end API demonstration
9. Test results
10. GitHub repository / PR

Create a short checklist for recording the final capstone video.

## Output

Create:

`07-final-review.md`

Include:
- completion checklist
- blockers, if any
- final MVP status
- GitHub readiness
- demo/video checklist

The final line should be one of:

**CAPSTONE READY FOR SUBMISSION**

or

**CAPSTONE NOT READY — BLOCKERS REMAIN**

Do not mark the project ready if a required MVP workflow is not working.
