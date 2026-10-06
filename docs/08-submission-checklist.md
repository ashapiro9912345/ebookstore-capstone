# Phase 8 — Pre-Submission Checklist

Run these yourself before recording and submitting. Nothing here is automated —
making the repo public, pushing, and recording are your calls on your account.

Repo: `https://github.com/ashapiro9912345/ebookstore-capstone`

---

## 1. Final secrets re-check (do this BEFORE going public)

Flipping a repo to public exposes its full history, so verify there are no real
secrets first.

```bash
# Scan tracked source for credential-like strings — expect only labeled dev/demo placeholders
git grep -niE "password|secret|token|apikey|api_key" -- src/main

# Confirm the local-only / sensitive files are ignored (each should print a .gitignore rule)
git check-ignore -v run-local.sh \
  src/main/resources/application-local.properties \
  target/ebookstore-1.0.0-SNAPSHOT.jar

# Sanity check: these should NOT be tracked (no output = good)
git ls-files | grep -E "application-local\.properties|run-local\.sh|\.DS_Store|target/" || echo "clean: none tracked"
```

What's acceptable to see: the dev-default `JWT_SECRET` placeholder in
`application.properties` (clearly labeled, overridden by env), test-only values in
`application-test.properties`, and the demo account `demo@bookstore.com / demo1234`
documented as local/demo-only. Anything that looks like a real password or key must
be removed from history before going public.

---

## 2. Confirm everything is committed and pushed

Current state (verified): branch `main`, `HEAD` already at `origin/main`. There are a
few **untracked** items in the working tree that are Postman app scaffolding, not part
of the submission:

```
.postman/        postman/        capture
```

Decide what to do with them:

```bash
git status --short            # review untracked items
git log --oneline -5          # confirm the Phase 7 "Capstone MVP Complete" commit is present

# Option A — leave them untracked (they won't affect the public repo). Nothing to do.

# Option B — if you WANT them in the repo, add explicitly and commit:
git add .postman postman capture
git commit -m "Add Postman workspace scaffolding"
git push                      # pushes to origin/main
```

If you also want the new Phase 8 submission docs in the repo:

```bash
git add docs/08-video-script.md docs/08-submission-text.md docs/08-submission-checklist.md
git commit -m "Phase 8 — AI review submission: video script, text response, checklist"
git push
```

> Avoid `git add -A` / `git add .` so you don't accidentally stage the untracked
> Postman dirs or anything unintended. Stage files by name.

---

## 3. Make the GitHub repository public

Pick one. **Do this only after step 1 passes.**

**Option A — GitHub CLI (if `gh` is installed and authenticated):**

```bash
gh repo edit ashapiro9912345/ebookstore-capstone \
  --visibility public --accept-visibility-change-consequences
```

**Option B — GitHub web UI:**

1. Open `https://github.com/ashapiro9912345/ebookstore-capstone/settings`
2. Scroll to **Danger Zone** → **Change repository visibility** → **Change to public**
3. Confirm by typing the repository name.

Verify it's public (should return without auth):

```bash
curl -s -o /dev/null -w "%{http_code}\n" \
  https://github.com/ashapiro9912345/ebookstore-capstone
# 200 = reachable publicly
```

---

## 4. Verify build and run (for a clean recording)

```bash
# Build + run the full test suite — expect BUILD SUCCESS, 57 tests, 0 failures
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn clean package                     # produces target/ebookstore-1.0.0-SNAPSHOT.jar

# Reset the demo DB to a known baseline (1 user, 6 books, Clean Code id 2 stock 8, 0 orders)
export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH"
psql -d ebookstore -f src/main/resources/db/reset.sql
psql -d ebookstore -f src/main/resources/db/data.sql

# Start the app (leave running during the recording)
export DB_URL="jdbc:postgresql://localhost:5432/ebookstore"
export DB_USERNAME="ebookstore"
export DB_PASSWORD="<your local password>"
export JWT_SECRET="<your base64 32-byte secret>"
java -jar target/ebookstore-1.0.0-SNAPSHOT.jar   # wait for "Started EbookstoreApplication"
```

Smoke-test one call before recording:

```bash
BASE=http://localhost:8080/api
curl -s "$BASE/books?title=clean"     # should return Clean Code
```

> Tests need a local `ebookstore_test` database (see `docs/05-local-run-guide.md`
> and `docs/06-test-results.md`). If you only want to demo (not run tests on-camera),
> packaging with `-DskipTests` is fine for producing the jar quickly.

---

## 5. Record and submit

- [ ] Record the ~3-minute video following `docs/08-video-script.md` (app already running from step 4).
- [ ] Reset the demo DB afterward if you need a second take (`reset.sql` + `data.sql`).
- [ ] Paste your public repo URL into the submission's repo-link field.
- [ ] Copy the body of `docs/08-submission-text.md` (between the BEGIN/END markers), replace `GITHUB_REPO_URL`, and paste it into the text field (confirmed under 4000 chars).
- [ ] Attach the video file.

---

## Quick reference

| Item | Value |
|------|-------|
| Repo | `https://github.com/ashapiro9912345/ebookstore-capstone` |
| Branch | `main` (HEAD pushed to `origin/main`) |
| API base URL | `http://localhost:8080/api` |
| Demo account | `demo@bookstore.com` / `demo1234` (local/demo only) |
| Persistence proof | Clean Code (book id 2) stock 8 → 6 at checkout, 6 → 8 after cancel |
| Tests | `mvn clean test` → 57 passing, real PostgreSQL (no H2) |
