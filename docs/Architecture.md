# Yuelu Traffic Architecture

Status: Milestone 0 technical design. No business code has been implemented.

## Scope Boundary

The MVP is an Android app plus a deployable backend and persistent database for the Central South University and Lushan South Road pilot area.

P0 includes:

- Lightweight student-number entry as an internal identifier, not formal identity verification.
- Map-centered traffic report display and submission.
- Report feedback, confidence scoring, default expiration, and community validity control.
- User reputation, points, leaderboard, and title display without exposing private identifiers.
- Accident board with mutual-confirmation contact exchange.
- Administrator moderation for reports, abuse, accident posts, and posting bans.

P0 excludes route planning, enforcement evasion guidance, unapproved external chat ingestion, citywide coverage, payment, complex ML, push notifications, and image evidence.

## Selected Technology Stack

### Android Client

- Language: Kotlin.
- UI: Jetpack Compose with Material 3.
- Architecture: MVVM with ViewModel, StateFlow, repository interfaces, and small domain use cases where logic is shared across screens.
- Navigation: Jetpack Navigation Compose.
- Networking: Retrofit plus OkHttp.
- JSON: kotlinx.serialization or Moshi; choose one during skeleton setup and use it consistently.
- Local storage: Jetpack DataStore for auth token, privacy acknowledgment, and lightweight settings.
- Map SDK: AMap Android SDK as the default production provider because the pilot area is in China. Wrap it behind a `MapProvider` interface so tests and local development can use a fake/static map when credentials are unavailable.
- Testing: JVM unit tests for view models and domain logic; Android instrumented tests only where device behavior is required.

### Backend Service

- Language: Java 21.
- Framework: Spring Boot.
- API style: REST JSON under `/api/v1`.
- Validation: Jakarta Bean Validation on request DTOs.
- Persistence: Spring Data JPA for ordinary relational entities.
- Migrations: Flyway SQL migrations.
- Auth: Lightweight student login issues app tokens for ordinary users; separate role-protected admin access.
- Scheduled work: Spring scheduled jobs for time-based report expiry and reputation maintenance.
- API documentation: OpenAPI generated from Spring controllers or maintained as an explicit contract during implementation.
- Testing: JUnit, Spring Boot tests, repository tests against PostgreSQL-compatible test database where practical.

### Database and Deployment

- Database: PostgreSQL.
- Geospatial approach for MVP: store latitude and longitude as numeric columns with ordinary indexes and pilot-area bounding-box checks. Defer PostGIS until radius queries or larger coverage justify it.
- Local deployment: Docker Compose for PostgreSQL and backend runtime once the skeleton exists.
- Production deployment target: any JVM-capable server or cloud instance with PostgreSQL. Secrets must come from environment variables, never repository files.

### Why This Stack

- Kotlin and Compose are the native direction for modern Android UI.
- Java plus Spring Boot keeps the backend conservative, well-documented, and deployable without introducing a web frontend framework for P0.
- PostgreSQL is sufficient for persistent relational data, moderation audit trails, and later geospatial growth.
- AMap is a practical map provider for the target geography, while the adapter boundary keeps credentials out of tests.

## Top-Level Repository Layout

Planned structure for Milestone 1:

```text
android/                 Android client project
backend/                 Spring Boot backend project
docs/                    Architecture, API drafts, design notes
docs/api/                OpenAPI draft or generated contract
scripts/                 Helper and validation scripts
tests/                   Cross-project or contract test assets if needed
```

The existing `src/` placeholder should remain unused unless a later decision creates shared tooling. Business code should live under `android/` and `backend/`.

## Backend Module Boundaries

- `auth`: student-number normalization, hashing, token issuing, role checks.
- `users`: user profile, reputation summary, points, title, posting restriction state.
- `reports`: traffic report creation, listing, detail, status, default expiration.
- `feedback`: confirm, expired, false, and malicious feedback with one-feedback-per-user-per-report rules.
- `confidence`: deterministic confidence calculation from report type, submitter reputation, feedback weights, and expiration state.
- `reputation`: points, reputation events, title changes, and posting-ban triggers.
- `accidents`: accident post creation, browsing, status management.
- `contacts`: mutual-confirmation contact exchange and protected contact disclosure.
- `moderation`: admin review queue, hide/remove actions, user bans, accident abuse handling.
- `location`: pilot-area boundary validation and location helper functions.
- `admin`: admin-only API surface and audit action recording.
- `common`: error responses, pagination, time handling, request validation, logging filters.

## Android Module Boundaries

- `app`: composition root, dependency wiring, navigation graph.
- `auth`: student-number entry, privacy notice, local token state.
- `map`: map screen, marker rendering, map-provider adapter, pilot-area view state.
- `reports`: report submission, report detail, feedback actions.
- `accidents`: accident board list, accident detail, contact exchange flow.
- `leaderboard`: rankings, points, titles.
- `profile`: current user profile, reputation, restrictions.
- `admin`: role-gated moderation screens for P0 admin workflows.
- `network`: Retrofit services, DTO mapping, auth interceptor.
- `domain`: small shared client-side models and view-state reducers.
- `storage`: DataStore-backed settings and token persistence.
- `design`: reusable Compose components and theme.

## Data Model Draft

Use UUID primary keys and `created_at` / `updated_at` timestamps unless noted.

### `users`

- `id`
- `student_number_hash`, unique, never returned publicly
- `public_code`, non-sensitive display handle such as `User-7F3A`
- `role`: `USER`, `ADMIN`
- `reputation_score`
- `points`
- `title_code`
- `posting_ban_until`, nullable
- `created_at`
- `updated_at`

### `traffic_reports`

- `id`
- `type`: `TRAFFIC_MANAGEMENT`, `CONSTRUCTION`, `CONGESTION`, `ROAD_CONTROL`, `ACCIDENT_OR_HAZARD`
- `latitude`
- `longitude`
- `location_label`, nullable
- `description`, nullable
- `submitted_at`
- `default_expires_at`
- `status`: `ACTIVE`, `EXPIRED`, `HIDDEN`, `REMOVED`, `UNDER_REVIEW`
- `confidence_score`
- `submitter_id`
- `hidden_reason`, nullable
- `created_at`
- `updated_at`

### `report_feedback`

- `id`
- `report_id`
- `user_id`
- `feedback_type`: `CONFIRM_VALID`, `MARK_EXPIRED`, `REPORT_FALSE`, `REPORT_MALICIOUS`
- `weight_snapshot`
- `created_at`

Constraints:

- Unique `(report_id, user_id)` for current MVP. If feedback changes are later allowed, store history separately.

### `reputation_events`

- `id`
- `user_id`
- `source_type`
- `source_id`
- `points_delta`
- `reputation_delta`
- `reason_code`
- `created_at`

### `accident_posts`

- `id`
- `latitude`
- `longitude`
- `location_label`, nullable
- `occurred_at`
- `description`
- `created_by_user_id`
- `status`: `OPEN`, `MATCHED`, `CLOSED`, `HIDDEN`
- `created_at`
- `updated_at`

### `contact_exchange_requests`

- `id`
- `accident_id`
- `requesting_user_id`
- `target_user_id`, nullable until a target is selected
- `status`: `PENDING`, `MUTUALLY_CONFIRMED`, `REJECTED`, `EXPIRED`
- `requester_confirmed_at`, nullable
- `target_confirmed_at`, nullable
- `created_at`
- `updated_at`

### `accident_contact_offers`

- `id`
- `exchange_request_id`
- `user_id`
- `contact_type`: `PHONE`, `WECHAT`, `QQ`, `OTHER`
- `encrypted_contact_value`
- `created_at`

Contact values are never returned by public accident APIs. They are returned only to the two exchange participants after the related request reaches `MUTUALLY_CONFIRMED`.

### `moderation_flags`

- `id`
- `target_type`: `TRAFFIC_REPORT`, `ACCIDENT_POST`, `USER`
- `target_id`
- `created_by_user_id`
- `reason_type`
- `details`, nullable
- `status`: `OPEN`, `RESOLVED`, `DISMISSED`
- `created_at`
- `updated_at`

### `admin_actions`

- `id`
- `admin_user_id`
- `target_type`
- `target_id`
- `action_type`
- `reason`
- `created_at`

### `user_restrictions`

- `id`
- `user_id`
- `restriction_type`: `POSTING_BAN`
- `reason`
- `starts_at`
- `ends_at`
- `created_by_admin_id`, nullable for automated restrictions
- `created_at`

## API Draft

All endpoints use JSON. Public list responses must omit `student_number_hash`, raw student numbers, and contact values.

### Auth and Current User

- `POST /api/v1/auth/student`
  - Request: `studentNumber`, `privacyAcknowledged`
  - Response: access token, user summary, whether the user is new
- `GET /api/v1/me`
  - Response: current user summary, reputation, points, title, posting restriction
- `GET /api/v1/leaderboard`
  - Response: ranked public user summaries without student numbers

### Traffic Reports

- `GET /api/v1/reports?bbox=&status=ACTIVE&type=`
  - Returns active map reports for the requested bounds.
- `POST /api/v1/reports`
  - Creates a report with type, coordinates, optional label, optional description, and submit time.
- `GET /api/v1/reports/{reportId}`
  - Returns report detail, confidence, status, and available feedback actions.
- `POST /api/v1/reports/{reportId}/feedback`
  - Request: feedback type.
  - Updates confidence, moderation flags, reputation events, and possibly report status.

### Accident Board

- `GET /api/v1/accidents?bbox=&status=OPEN`
  - Returns accident post summaries without contact details.
- `POST /api/v1/accidents`
  - Creates an accident post.
- `GET /api/v1/accidents/{accidentId}`
  - Returns accident detail without contact details.
- `POST /api/v1/accidents/{accidentId}/contact-requests`
  - Starts a mutual contact exchange request.
- `POST /api/v1/contact-requests/{requestId}/confirm`
  - Confirms one side of contact exchange.
- `GET /api/v1/contact-requests/{requestId}`
  - Returns request status and contact values only when mutual confirmation is complete.

### Admin

- `GET /api/v1/admin/review-queue`
  - Lists reports, accident posts, users, and flags needing review.
- `POST /api/v1/admin/reports/{reportId}/moderate`
  - Hide, remove, expire, restore, or mark under review.
- `POST /api/v1/admin/accidents/{accidentId}/moderate`
  - Hide, close, restore, or mark under review.
- `POST /api/v1/admin/users/{userId}/restrictions`
  - Adds or updates a posting ban.
- `GET /api/v1/admin/actions`
  - Audit log for admin actions.

No API endpoint should provide route recommendations, enforcement avoidance instructions, or bypass tactics.

## Core Business Rules Draft

### Student Identifier

- Normalize student number on the backend.
- Hash with a server-side pepper from environment configuration.
- Store only the hash.
- Return only `public_code` to public clients.
- Do not log raw student numbers.

### Report Expiration

- `CONGESTION`: default expiry after 30 minutes.
- `TRAFFIC_MANAGEMENT`: default expiry after 6 hours.
- Other types: choose conservative defaults during implementation and record them before coding.
- Community feedback can downgrade or hide a report before time expiry.
- Scheduled expiry changes only status and confidence; it should not delete audit history.

### Confidence

Initial confidence depends on report type and submitter reputation. Feedback adjusts confidence using `weight_snapshot` captured from the feedback user's reputation at feedback time.

Suggested starting formula for implementation:

```text
confidence = base_by_type
  + submitter_reputation_factor
  + weighted_confirm_count
  - weighted_expired_count
  - weighted_false_or_malicious_count
  - time_decay
```

Clamp confidence to a documented range such as `0..100`.

### Reputation and Points

- Useful reports and valid confirmations add points.
- Confirmed false or malicious reports reduce reputation.
- Repeated malicious reports can create a `POSTING_BAN` restriction.
- Keep reputation changes in `reputation_events` so moderation decisions are auditable.

### Privacy and Contact Exchange

- Accident contact details are optional and hidden by default.
- Contact values are visible only to both participants after mutual confirmation.
- Admins can moderate accident posts and abuse flags, but routine public APIs never disclose contact values.
- Prefer application-level encryption for stored contact values once a contact field is implemented.

## Validation Strategy

### Milestone 0 Validation

- Confirm `Prompt.md`, `Plan.md`, `Implement.md`, and `Documentation.md` were read.
- Confirm this architecture document records stack selection, module boundaries, data model, API draft, and validation plan.
- No business-code build is required for Milestone 0.

### Milestone 1 Skeleton Validation

Planned commands after skeleton creation:

```powershell
cd D:\project-root\backend
.\gradlew.bat test
.\gradlew.bat bootJar
```

```powershell
cd D:\project-root\android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

If Android SDK or map credentials are unavailable, document the limitation and validate a fake map adapter plus unit tests.

### Backend Validation

- Controller tests for request validation and public response redaction.
- Service tests for confidence, expiry, voting uniqueness, reputation, and posting-ban rules.
- Repository or integration tests for Flyway migrations and PostgreSQL persistence.
- Admin endpoint tests for role protection and audit logging.

### Android Validation

- ViewModel tests for auth, report list, report submission, feedback, accident contact exchange, leaderboard, and admin states.
- UI smoke checks on emulator or physical device once screens exist.
- Manual map check for marker display when a valid map SDK key is available.

### Contract and Privacy Validation

- Keep OpenAPI or DTO tests aligned between Android and backend.
- Add tests that public report, accident, and leaderboard responses do not expose student numbers or contact values.
- Add a text scan before release for prohibited user-facing phrasing connected to law-enforcement evasion.

### End-to-End MVP Validation

- User enters student number and sees privacy notice.
- User views pilot-area map.
- User submits each P0 report type.
- Report appears as a marker and detail.
- Other users confirm or mark expired/false/malicious.
- Confidence, reputation, points, and leaderboard change.
- Accident post is created and browsed.
- Contact details remain hidden until both sides confirm.
- Admin hides or removes an invalid report and applies a posting ban.

## Key Risks and Mitigations

- Map SDK credentials may be unavailable locally. Mitigation: keep a fake map provider for tests and document manual production checks.
- Student-number-only login is weak authentication. Mitigation: state clearly that it is a lightweight identifier, protect admin separately, and avoid public exposure.
- Contact data is sensitive. Mitigation: store minimum data, hide by default, encrypt stored values, and test redaction.
- Public traffic management reports are safety-sensitive. Mitigation: avoid route planning or evasion language and keep the feature framed as public road-condition reporting.
- Admin UI could expand scope. Mitigation: use role-gated Android admin screens for P0 and defer a web dashboard unless explicitly requested.

## Open Decisions Before Milestone 1

- Exact minimum Android SDK and Gradle plugin versions.
- Exact JSON library for Android and backend DTOs.
- Backend deployment target for first deployable instructions.
- Whether P0 admin screens are Android-only or whether a minimal backend-served admin page is required.
- Default expiration durations for `CONSTRUCTION`, `ROAD_CONTROL`, and `ACCIDENT_OR_HAZARD`.
