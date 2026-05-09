# Yuelu Traffic

Yuelu Traffic is an Android-based crowdsourced traffic safety and incident reporting project for the Central South University and Lushan South Road pilot area.

The repository contains:

- `android/` - Kotlin Jetpack Compose Android app.
- `backend/` - Java 21 Spring Boot REST API.
- `backend/src/main/resources/db/migration/` - Flyway database migrations.
- `docker-compose.yml` - PostgreSQL plus backend deployment path.
- `scripts/` - validation helpers.
- `docs/Architecture.md` - design notes and API outline.

## Implemented MVP Surface

- Lightweight student-number login with explicit privacy acknowledgement.
- Backend hashing of student numbers; public APIs return only non-sensitive public user codes.
- Traffic report create/list/detail APIs with pilot-area validation.
- MVP report types: traffic management presence, construction, congestion, road control, and accident or abnormal road condition.
- Default expiration: congestion 30 minutes, traffic management 6 hours, construction 12 hours, road control 12 hours, accident/hazard 4 hours.
- Community feedback for confirm-valid and no-longer-valid states.
- Confidence, points, reputation, title, leaderboard, and posting restriction behavior.
- Accident board APIs and mutual-confirmation contact exchange.
- Admin APIs for review queue, report moderation, accident moderation, and user posting restrictions.
- Android local MVP UI for login/privacy notice, map-style report markers, report submission/feedback, leaderboard, accident board, contact confirmation, and admin actions.

## Prerequisites

- JDK 21. Android Studio's bundled JBR 21 works.
- Android SDK with API 35 installed.
- PowerShell on Windows.
- Docker Desktop or compatible Docker Engine only if running the PostgreSQL Compose stack.

If Java or Android SDK are not on `PATH`, set them for the current PowerShell session:

```powershell
$env:JAVA_HOME="D:\Android Studio\jbr"
$env:ANDROID_HOME="D:\AndroidDev\AndroidSDK"
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
```

Adjust paths for your machine.

## Validate

From the repository root:

```powershell
.\gradlew.bat check
powershell -ExecutionPolicy Bypass -File .\scripts\check_safety_text.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\check_no_todos.ps1
.\gradlew.bat :backend:bootJar
.\gradlew.bat :android:assembleDebug
```

The Android debug APK is generated at:

```text
android/build/outputs/apk/debug/android-debug.apk
```

## Run Backend Locally

The default backend profile uses a local H2 file database for credential-free development:

```powershell
.\gradlew.bat :backend:bootRun
```

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/health
```

## Backend API Workflow

Create or reuse a student-number-based app user. The backend stores a salted hash and does not return the raw student number.

```powershell
$login = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/v1/auth/student `
  -ContentType "application/json" `
  -Body '{"studentNumber":"20260001","privacyAcknowledged":true}'

$token = $login.accessToken
```

Create a traffic report:

```powershell
$report = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/v1/reports `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json" `
  -Body '{
    "type":"CONGESTION",
    "latitude":28.1703,
    "longitude":112.9388,
    "locationLabel":"Lushan South Road",
    "description":"Northbound slow traffic",
    "initialCredibility":50
  }'
```

Evaluate the report:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/reports/$($report.id)/feedback" `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json" `
  -Body '{"feedbackType":"CONFIRM_VALID"}'
```

Create an accident-board post and contact request:

```powershell
$accident = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/v1/accidents `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json" `
  -Body '{
    "latitude":28.1703,
    "longitude":112.9388,
    "locationLabel":"Lushan South Road",
    "description":"Minor incident, looking for the other involved party"
  }'

$exchange = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/accidents/$($accident.id)/contact-requests" `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json" `
  -Body '{"contactType":"WECHAT","contactValue":"private-contact"}'
```

Contact values are returned only after the other involved user confirms the exchange.

## Admin Workflow

Admin users are configured with `YUELU_ADMIN_STUDENT_NUMBERS`. Local development enables `ADMIN-DEMO` by default.

```powershell
$admin = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/v1/auth/student `
  -ContentType "application/json" `
  -Body '{"studentNumber":"ADMIN-DEMO","privacyAcknowledged":true}'

Invoke-RestMethod `
  -Uri http://localhost:8080/api/v1/admin/review-queue `
  -Headers @{ Authorization = "Bearer $($admin.accessToken)" }
```

## PostgreSQL Deployment Path

Copy `.env.example` to `.env`, replace the placeholder values, then run:

```powershell
docker compose up --build
```

The Compose stack starts PostgreSQL and the backend. Flyway migrations run at backend startup.

## Safety and Privacy Notes

- Student numbers are normalized and hashed on the backend.
- Public responses use `publicCode` and do not include raw student numbers or student-number hashes.
- Accident contact values are omitted from public accident APIs and hidden until mutual confirmation.
- The app is framed as traffic safety and public road-condition reporting. It must not provide routing, tactics, or instructions for unlawful traffic behavior.
- Run `scripts/check_safety_text.ps1` before release-facing text changes.

## Known Limitations

- The Android UI currently uses local in-app state for MVP workflow demonstration; backend APIs are implemented and tested separately.
- The Android map is a credential-free Compose map-style panel, not a production AMap SDK view yet.
- Android emulator or physical-device manual testing was not available in this session; validation used unit tests, Android lint, and debug APK build.
- Docker is not installed in the current environment, so `docker compose up --build` was documented but not locally executed.
- Contact values are protected from public APIs, but production-grade field encryption should be added before real deployment.
