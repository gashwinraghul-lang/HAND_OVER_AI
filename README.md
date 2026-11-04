# HandOver.AI

**Preserve Knowledge. Empower People. Ensure Continuity.**

HandOver.AI is a knowledge-handover workspace for teams losing a project owner. It connects to the tools where work happens, ingests project documents, reconstructs what is done / blocked / missing with citations, requires manager approval, and produces a handover report plus a 30/60/90 onboarding plan.

> People may leave, but project knowledge should never be lost.

---

## 1. What is in the demo

- Login, signup, email-verification simulation, rate limiting and account lockout.
- Face recognition as a **second factor** with password, explicit consent, liveness (blink), encrypted embeddings and deletion.
- Role-aware dashboard with project list, readiness score, progress chart, findings and chat.
- Seeded project **VPS Infrastructure Renewal** (readiness 78%, 22% missing, 3 items needing attention).
- Document upload + PDF/DOCX/TXT parsing and chunking.
- Connector page with encrypted keys, masked last-four display, test, rotate, revoke and sync.
- Project chatbox with source citations and the exact off-topic response `its irrelevant`.
- Manager review / approval workflow and downloadable PDF handover report.
- Incoming-employee view with approved report, access inventory and 30/60/90 plan.
- Three.js knowledge-node background with reduced-motion support and a pause toggle.

## 2. Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Java 17, Spring Boot 3.2, Spring Web, Spring Security, Spring Data JPA / Hibernate |
| Database | H2 file database (default, zero setup) or MySQL 8 |
| Frontend | HTML5, CSS3, vanilla JavaScript, Bootstrap 5, Chart.js, Three.js, face-api.js |
| Documents | Apache PDFBox, Apache POI, plain-text reader |
| PDF reports | OpenPDF |
| AI | `AIService` interface with offline `MockAIService` and OpenAI-compatible `LiveAIService` |
| Tests | JUnit 5, Mockito, Spring Boot Test |

---

## 3. Quick start (no database install)

Prerequisites: **JDK 17+** and Maven (or the included wrapper).

```powershell
# Windows
.\mvnw.cmd spring-boot:run
```

```bash
# macOS / Linux (Maven wrapper if available)
./mvnw spring-boot:run
# or: mvn spring-boot:run
```

Open <http://localhost:8080>. The app starts with the embedded H2 file database at `data/handover_ai.mv.db`; demo data is seeded automatically on the first run.

Stop the app with `Ctrl+C`. Delete the `data/` directory if you want a clean re-seed.

### Demo credentials

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@handoverai.com` | `Admin@123456789` |
| Manager | `manager@handoverai.com` | `Manager@12345` |
| Outgoing employee | `outgoing@handoverai.com` | `Outgoing@12345` |
| Incoming employee | `incoming@handoverai.com` | `Incoming@12345` |

Demo project id is **1** ("VPS Infrastructure Renewal").

---

## 4. Run with MySQL 8 (Docker Compose)

```bash
docker compose up -d
```

Then set the database variables (PowerShell example):

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/handover_ai?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:SPRING_DATASOURCE_DRIVER="com.mysql.cj.jdbc.Driver"
$env:SPRING_DATASOURCE_USERNAME="handover_user"
$env:SPRING_DATASOURCE_PASSWORD="handover_pass"
$env:SPRING_JPA_DATABASE_PLATFORM="org.hibernate.dialect.MySQLDialect"
.\mvnw.cmd spring-boot:run
```

The Compose file creates database `handover_ai`, user `handover_user` / `handover_pass`, and root password `rootpassword` on port `3306`.

Copy `.env.example` to `.env` for a complete list of variables. The Spring app reads environment variables; it does not load `.env` automatically, so export them in your shell or IDE run configuration.

---

## 5. Mock AI vs live AI

Mock mode is the default and needs no API key:

```env
MOCK_AI_MODE=true
```

It returns realistic, pre-written answers, conflicts, missing-evidence flags and citations. The UI reads `GET /api/system/status` and labels the mode as **SIMULATED**.

Live mode uses any OpenAI-compatible chat-completions endpoint:

```env
MOCK_AI_MODE=false
LLM_API_KEY=your_real_key
LLM_API_ENDPOINT=https://api.openai.com/v1/chat/completions
LLM_MODEL=gpt-4o
```

`LiveAIService` sends only the authorised project context (chunks, tasks, decisions, risks), keeps the off-topic rule, and returns citations from the same retrieved evidence. No API key is included in prompts or returned to the browser.

---

## 6. Connector keys

1. Sign in and open **Connections**.
2. Choose a project and service (GitHub, Trello, Google Drive).
3. Review the requested scopes, paste a token/API key, and save.
4. The backend validates the format, encrypts the key with AES-GCM, and stores only the ciphertext plus the last four characters.
5. Use **Test**, **Rotate**, **Sync now**, or **Revoke**.

The seeded GitHub/Trello connectors are deliberately simulated for the hackathon demo. Real GitHub/Trello/Drive HTTP calls can be added behind the existing `Connector` interface without changing the UI or database.

---

## 7. Face login

Face recognition is **never a replacement for the password**. The flow is:

1. Email + password.
2. Camera capture of a 128-value face descriptor.
3. Blink-based liveness check.
4. The descriptor is compared only against that user's stored vector (Euclidean distance <= 0.60).

Enrol or delete face data from **Profile**. Enrolment requires explicit consent, captures several frames, averages the descriptor, and stores only the AES-GCM-encrypted embedding - never the photo.

Camera access requires **HTTPS or localhost**. For a camera-free demo, the login page has **Use seeded demo face (no camera)**: it sends a synthetic all-zero descriptor that matches the seeded demo profiles, plus the password.

Admin accounts use password + face + verified email. Face login can be disabled with `FACE_LOGIN_ENABLED=false`.

---

## 8. Security design

- BCrypt password hashes (strength 12); minimum 12-character passwords.
- Session-based authentication with HttpOnly cookies, configurable SameSite/Secure flags, server-side logout and session invalidation.
- CSRF protection enabled; the frontend fetches `/api/auth/csrf` and sends `X-XSRF-TOKEN`.
- Project access is checked in backend services (`ProjectService.assertProjectAccess`) for project details, documents, connections, chat, reports and onboarding views - not only hidden in the UI.
- Report approval is restricted to `ROLE_MANAGER` and `ROLE_ADMIN`.
- API keys and face embeddings are AES-GCM encrypted at rest; keys are returned only as `...last4`.
- Junction/entity JSON is guarded with `@JsonIgnore` so secrets and lazy proxies never leak.
- Login attempts are rate-limited and accounts lock temporarily after repeated failures.
- No secrets are committed; see `.env.example`.

---

## 9. Project structure

```text
src/main/java/com/handoverai
├── ai/           AIService, MockAIService, LiveAIService
├── config/       SecurityConfig, DemoDataSeeder
├── connector/    Connector interface + GitHub/Trello/Drive implementations
├── controller/   Auth, Project, Document, Connection, Chat, Report, Onboarding, System
├── dto/          Request/response records
├── entity/       JPA entities (users, projects, reports, connections, ...)
├── file/         PDF/DOCX/TXT parsing
├── report/       OpenPDF report generation
├── repository/   Spring Data repositories
├── security/     EncryptionUtil, FaceAuthService, rate limiting
└── service/      Project, Document, Connection, Chat, Report services

src/main/resources/static
├── index.html, login.html, signup.html
├── dashboard.html, project.html, connections.html, profile.html
├── css/style.css
└── js/app.js, js/bg3d.js, js/chart-config.js
```

### Main API endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/auth/csrf` | Bootstrap CSRF token |
| POST | `/api/auth/signup` | Create employee account |
| POST | `/api/auth/login` | Password login |
| POST | `/api/auth/face/enroll` | Enrol encrypted face embedding (consent required) |
| POST | `/api/auth/face/verify` | Password + face second factor |
| DELETE | `/api/auth/face` | Delete face data |
| GET | `/api/projects` | Projects visible to the signed-in user |
| GET | `/api/projects/{id}` | Project workspace data |
| POST | `/api/projects/{id}/chat` | Project-scoped chat with citations |
| GET/POST | `/api/projects/{id}/documents` | List / upload documents |
| GET/POST | `/api/projects/{id}/connections` | List masked keys / add a connector |
| POST | `/api/projects/{id}/connections/{connId}/test` | Test connector |
| POST | `/api/projects/{id}/connections/{connId}/rotate` | Rotate key |
| POST | `/api/projects/{id}/connections/sync` | Sync and analyse |
| POST | `/api/projects/{id}/reports/generate` | Generate pending report |
| POST | `/api/projects/{id}/reports/{reportId}/approve` | Manager/admin approval |
| GET | `/api/projects/{id}/reports/{reportId}/download` | Download PDF |
| GET | `/api/projects/{id}/incoming-view` | Approved handover + onboarding plan |

---

## 10. Tests

```powershell
.\mvnw.cmd test
```

Covered:

- Project permission checks (admin, member, creator, non-member, unknown user).
- Readiness score and missing-percentage calculation.
- Off-topic chatbox rule returning exactly `its irrelevant`, plus citations for project questions.
- API key masking (`...last4`) and no ciphertext leakage from the connections API.
- AES-GCM encryption round-trip and random IV behaviour.

---

## 11. Known limitations

- GitHub/Trello/Google Drive connectors use seeded/simulated data; they do not call live vendor APIs yet.
- Email verification is simulated in the console and auto-verifies demo signups.
- Live AI requires a real OpenAI-compatible key; mock mode is the default.
- The face matcher is a client-side descriptor + server-side Euclidean comparison suitable for a demo, not a certified biometric product.
- The seeded synthetic face profile is for camera-free demos only. A real enrolment replaces it.

---

© 2026 HandOver.AI. People may leave, but project knowledge should never be lost.
