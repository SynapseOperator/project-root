# Yuelu Traffic

Yuelu Traffic is an Android and Spring Boot project for crowdsourced traffic safety and road-condition reporting around Central South University and Lushan South Road.

The current Android app is a Simplified Chinese, map-first Compose experience. Core traffic workflows now connect to the backend: student-number login, current user session, traffic report list, report creation, report detail refresh, and report feedback. Accident board, leaderboard, and admin surfaces remain local/demo in this phase and are labeled that way in the app.

## Repository Layout

- `android/` - Kotlin Jetpack Compose Android app.
- `backend/` - Java 21 Spring Boot REST API.
- `backend/src/main/resources/db/migration/` - Flyway database migrations.
- `docker-compose.yml` - PostgreSQL plus backend deployment path.
- `scripts/` - validation helpers.
- `docs/Architecture.md` - design notes and API outline.
- `docs/prompts/` - archived prompt snapshots.

## Current Android Surface

- Chinese login screen with privacy acknowledgement.
- Backend-connected login through `/api/v1/auth/student`.
- Backend-connected current user refresh through `/api/v1/me`.
- Map-first home page with a polished credential-free Compose mock map.
- Chinese bottom navigation: 地图, 上报, 事故栏, 我的.
- Backend-connected traffic report list, detail refresh, creation, and feedback.
- Clear Chinese backend-unavailable and local-demo states.
- Local/demo accident board with mutual contact confirmation copy.
- Local/demo profile, leaderboard, and admin panel with visible scope boundaries.

The production map SDK is intentionally deferred. The app builds without map credentials.

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
$env:JAVA_HOME="D:\Android Studio\jbr"
.\gradlew.bat check
powershell -ExecutionPolicy Bypass -File .\scripts\check_android_chinese_text.ps1
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
$env:JAVA_HOME="D:\Android Studio\jbr"
.\gradlew.bat :backend:bootRun
```

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/health
```

## Run Android Locally

Build the debug APK:

```powershell
$env:JAVA_HOME="D:\Android Studio\jbr"
.\gradlew.bat :android:assembleDebug
```

The Android app defaults to:

```text
http://10.0.2.2:8080
```

That address lets an Android emulator reach the backend running on the host machine. A physical device may need a LAN IP or a different `API_BASE_URL` build configuration.

Manual emulator or phone validation was not available in the latest local run because `adb devices` reported no connected devices.

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
    "locationLabel":"麓山南路中南大学门口",
    "description":"车流缓慢，请耐心通行。",
    "initialCredibility":50
  }'
```

List nearby reports:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/v1/reports?minLat=28.12&minLng=112.88&maxLat=28.22&maxLng=113.00"
```

Submit feedback:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/reports/$($report.id)/feedback" `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json" `
  -Body '{"feedbackType":"CONFIRM_VALID"}'
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
- The app is framed as traffic safety and public road-condition reporting, not as routing or enforcement-avoidance assistance.
- Run `scripts/check_safety_text.ps1` and `scripts/check_android_chinese_text.ps1` before release-facing text changes.

## Known Limitations

- Production map SDK integration is deferred; the current Android map is a credential-free Compose mock map.
- Accident board, leaderboard, and admin backend integration are deferred from this phase's P0 scope.
- Android emulator or physical-device manual testing was not available in the latest validation run.
- Docker is not installed in the current environment, so `docker compose up --build` was documented but not locally executed.
- Contact values are protected from public APIs, but production-grade field encryption should be added before real deployment.
