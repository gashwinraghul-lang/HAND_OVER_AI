You are building "HandOver.AI", an intelligent knowledge handover web platform for a national-level hackathon (WE CODE CODERS FORGE48). Work in Agent mode. Start by producing a short written plan (folder structure, data model, API endpoints, security design, and build order) and wait for it to be accepted before writing code. Then build in the order given at the end. Prioritise a working, demo-ready core over breadth.

## Product summary
HandOver.AI helps a company keep project knowledge when an employee leaves or changes roles. It connects to the tools where work happens through the user's own API keys, collects work progress, analyses it with AI, and combines it with uploaded documents and meeting notes. Every AI claim carries a source citation, missing or conflicting evidence is flagged, a manager must approve the result, and then the platform produces a role-appropriate handover report (PDF) and an onboarding plan for the incoming employee.

Tagline: "Preserve Knowledge. Empower People. Ensure Continuity."
Footer line: "People may leave, but project knowledge should never be lost."

## Tech stack (use exactly this)
- Frontend: HTML5, CSS3, vanilla JavaScript, Bootstrap 5, Chart.js. Three.js (or a lightweight equivalent) for the 3D animated backgrounds. face-api.js (or a browser-side face-recognition library) for face login. No React or other frameworks.
- Backend: Java 17+, Spring Boot (Spring Web, Spring Security, Spring Data JPA, Hibernate), Maven.
- Database: MySQL 8, schema name `handover_ai`.
- Document parsing: Apache PDFBox (PDF), Apache POI (DOCX), plain read for TXT.
- PDF report generation: a Java PDF library (e.g. OpenPDF).
- AI layer: an AI service class behind an interface so the LLM provider can be swapped. Read the LLM key from an environment variable. Implement RAG: chunk the data, create embeddings, retrieve only authorised chunks, and answer with citations.
- MOCK AI mode (config flag) that returns realistic, pre-written analysis and answers with citations, so the demo works with no API keys. The UI must clearly label whether AI is live or simulated.

## Project structure
Backend (package `com.handoverai`): `config`, `security`, `controller`, `service`, `repository`, `entity`, `dto`, `ai`, `file`, `connector`, `report`, `audit`. Frontend: `static/` with `css/`, `js/`, `assets/` (3D scene scripts), and one HTML page per screen.

## Authentication, account creation and face login (required before anything opens)
- The website opens to a login/signup page. No dashboard, project, document or API content is reachable without an authenticated session. Every route except the login, signup and static assets is protected on the backend, and unauthenticated requests are redirected to login.
- Sign up: name, work email, password (minimum 12 characters, BCrypt or Argon2 hash), and email verification (a simulated verification link logged to the console is fine for the demo). Public signup always creates an Employee account. Admin and Manager accounts are created only by an existing admin.
- Password login with rate-limited attempts and account lockout after repeated failures.
- Face recognition as a second factor, enrolled during signup or from the profile page:
  - The user gives explicit consent before the camera is used, and can delete their face data at any time.
  - Capture several frames and require a basic liveness check (e.g. a blink or head-turn prompt) to reduce photo spoofing.
  - Store only a face embedding (a numeric vector), encrypted at rest, never the photo itself.
  - Face login matches the embedding against that user's stored vector only. It never searches other users.
  - Face recognition is used with the password, not instead of it. Password remains the fallback when the camera is unavailable or the match fails. Admin accounts must use password plus face plus email verification.
  - Note in the UI and README that face recognition needs HTTPS or localhost for camera access.
- Logout invalidates the session server-side. Session cookies: HttpOnly, Secure, SameSite. CSRF protection enabled. Session rotation and expiry.
- Only the logged-in user's own account and data are displayed. Permission checks are enforced in the backend services, not by hiding UI elements.

## Connected work tracking through API keys
- Users add their own API keys or tokens on a "Connections" page for the services where their project work lives. Start with these connectors, each behind a common connector interface: GitHub (repos, commits, pull requests, issues), Trello or Jira (boards, tasks, status, assignee, due dates), and Google Drive or a generic file/document source (documents for ingestion). Add Slack or email (meeting notes) only if time allows.
- Use read-only scopes wherever the service allows them. Show the requested permissions to the user before they save a key.
- API keys are encrypted at rest with AES-GCM using a key from the environment, shown in the UI only as a masked value (last 4 characters), never returned to the browser in full, never logged, and never included in AI prompts.
- Users can test, rotate, revoke or delete a key at any time. Deleting a key stops future syncs.
- A sync job (scheduled, plus a "Sync now" button) pulls progress data from each connected service, normalises it into tasks, decisions, owners, deadlines and status changes, and stores the source link for each item.
- Analysis: after each sync, the AI analyses the collected progress and produces: completed work, work in progress, blocked items, unowned tasks, overdue deadlines, risks, dependencies, and knowledge gaps. Every result links back to its source item (repo, card, file, page or section).
- Results feed the Handover Readiness Score, the handover report, and the onboarding plan.

## Project chatbox (scoped assistant)
- A chat panel on each project page answers the user's doubts using only that project's analysed data and authorised documents.
- Every answer includes citations (source, page or section, or the connected item link). If the evidence is missing, the answer says so and flags the gap.
- If the question is not related to the project or its data, the assistant replies with exactly: "its irrelevant". It does not answer general questions, even if it knows the answer.
- If the question is about the project but the data is not in the system, it says the information is missing and suggests which connector or document would provide it.
- Users can flag any answer as wrong. The flag is logged for the manager to review.

## Database tables
users, user_face_profiles (encrypted embeddings), roles, projects, project_members, connections (encrypted keys, service, status, last sync), sync_runs, documents, document_chunks, tasks, decisions, risks, knowledge_gaps, analysis_results, chat_messages, handover_reports, report_approvals, access_items (accounts, licences, client files), audit_logs, onboarding_plans (30/60/90), exit_interview_notes.

## Roles and permissions
- Admin: manages users and projects. Requires password, face and email verification.
- Manager: creates projects, reviews, corrects, approves or rejects handover reports.
- Outgoing Employee: connects their own tools, uploads documents, answers exit interview questions, sees their own projects.
- Incoming Employee: sees only the approved handover and onboarding plan for their assigned project.
Project-level access limits which documents, connections and chat data a user can see.

## Trust layer
- Every AI claim displays its source (document name, page, section, or connected item link).
- Uncertain or missing information is flagged (amber "Missing evidence" tags). Conflicts show both values with their sources.
- AI output is never shared until a manager approves it. Show a clear "Pending manager approval" state.
- Users can correct or reject any AI item, and the correction is recorded in the version history.

## Demo data (seed automatically on first run)
Create a synthetic, anonymised project "VPS Infrastructure Renewal" with:
- Document "VPS Renewal SOP.pdf": renewal deadline 30 June 2025, payment via "GlobalPay", access shared with the infra team after renewal (page 3, section 2.1).
- Document "Team Meeting Notes.txt": deadline listed as 25 June 2025, so a conflict is detected.
- Mock connector data: two GitHub issues and one Trello card, one overdue, one with no owner, one completed.
- Two tasks, one decision, one risk, and one missing item (no renewal receipt uploaded).
- Readiness score 78%, with 22% information missing and three items needing attention.
- Demo users: an admin, a manager, an outgoing employee, and an incoming employee, each with a demo password and a demo face profile for the face-login demo.
Use no real personal data or payment details. Mask the account ID in the UI.

## Theme and frontend design
- 3D animated background: a lightweight Three.js scene (for example, floating translucent knowledge nodes linked by lines that slowly drift) used on the landing and login pages. On the dashboard, use a subtle version or a static gradient so the content stays readable. Provide a reduced-motion option and a toggle to turn the animation off.
- Colour theme: deep navy or teal primary, soft glass-style cards, amber for warnings, green for approved, red for overdue. Support light and dark mode.
- Dashboard: clean and uncluttered. Top row: readiness score, pending approvals, open risks, connected services. Then the project list, a progress chart (Chart.js), recent AI findings, and the chat panel. Use plain language, large clear labels, and no more than one primary action per section.
- Landing page: hero with the tagline and "Get started" and "Log in" buttons; the problem section (no documentation, sudden departure, undocumented access, client relationship gaps, complex custom projects, delayed recruitment, no overlap period, no mentorship, unverified AI information); the five-step workflow (Connect, Reconstruct, Verify, Review, Handover); a trust and security section.
- Fully responsive for mobile. Accessible: proper labels, sufficient contrast, keyboard navigation, and screen-reader text for the 3D background.

## Non-functional requirements
- No secrets in the repository. Provide `.env.example` listing every variable (database, LLM key, connector encryption key, mock-mode flag).
- README with: setup steps, running MySQL (Docker Compose), switching between mock and live AI, adding connector keys, demo credentials, face-login setup, and a short architecture overview.
- Unit tests for: permission checks, the readiness score calculation, the "its irrelevant" chatbox rule, API key masking, and encryption round-trip.
- The app starts with one command after the database is running (`mvn spring-boot:run`).

## Build order
1. Project skeleton, Maven, MySQL with Docker Compose, `.env.example`.
2. Entities, repositories, and demo data seeder.
3. Signup, email verification, password login, lockout, and route protection (login page is the entry point).
4. Face enrolment and face login as a second factor, with consent and liveness check.
5. Projects, roles, and document upload with parsing and chunking.
6. Connections page: encrypted API keys, masking, test, revoke, and the connector interface with GitHub, Trello or Jira, and Google Drive.
7. Sync job and AI analysis (mock first, then live behind the same interface).
8. Project chatbox with citations and the "its irrelevant" rule.
9. Readiness score, manager review workflow, and PDF report after approval.
10. Incoming employee view.
11. 3D theme, dashboard polish, landing page, and README.
12. P2 features as time allows: exit interview assistant, access inventory, 30/60/90 plan, analytics.

After each step, run the app or tests and fix errors before moving on. At the end, give me a summary of what works, what is mocked, what needs real API keys, and what is not built yet.