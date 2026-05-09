# Yuelu Traffic

Yuelu Traffic is an Android-based crowdsourced traffic safety and incident reporting app for the Central South University and Lushan South Road pilot area.

The project is being implemented milestone by milestone from `Prompt.md`, `Plan.md`, and `Implement.md`. Current code contains the first runnable skeleton: a Spring Boot backend module and a Jetpack Compose Android module.

## Current Modules

- `backend/` - Java 21 Spring Boot REST API.
- `android/` - Kotlin Jetpack Compose Android app.
- `docs/` - architecture and project notes.
- `scripts/` - reserved for helper and validation scripts.

## Prerequisites

- JDK 21. Android Studio's bundled JBR 21 is sufficient.
- Android SDK with API 35 installed.
- Gradle wrapper from this repository.

If Java or Android SDK are not on `PATH`, set them for the current PowerShell session before running Gradle:

```powershell
$env:JAVA_HOME="D:\Android Studio\jbr"
$env:ANDROID_HOME="D:\AndroidDev\AndroidSDK"
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
```

Adjust those paths for your machine.

## Run and Validate the Skeleton

From the repository root:

```powershell
.\gradlew.bat :backend:test
.\gradlew.bat :backend:bootJar
.\gradlew.bat :android:testDebugUnitTest
.\gradlew.bat :android:assembleDebug
```

Run the backend locally:

```powershell
.\gradlew.bat :backend:bootRun
```

Then check:

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/health
```

The Android debug APK is generated at:

```text
android/build/outputs/apk/debug/android-debug.apk
```

## Control Files

### `AGENTS.md`

Global behavior rules for Codex in this repository.

It defines Codex's role, required reading, file priority, working principles, code quality rules, validation rules, documentation rules, git rules, prohibited actions, and final reporting format.

### `Prompt.md`

The project requirements source of truth.

Fill this file before implementation begins. It describes what to build, who it is for, P0/P1/P2 requirements, non-goals, constraints, inputs, outputs, data needs, dependencies, acceptance criteria, and the final Done When checklist.

### `Plan.md`

The milestone plan and validation checklist.

It tells Codex what order to work in and how each stage should be validated. The default milestones start with project understanding, then a minimal running skeleton, then P0 features, integration, tests, and final documentation.

### `Implement.md`

Codex's autonomous execution manual.

It defines how Codex should read the control files, find the current milestone, implement only the required work, run validation, fix failures, update `Documentation.md`, and continue until completion or a real blocker.

### `Documentation.md`

The live project log.

It records current status, selected stack, milestone progress, work log entries, decisions, assumptions, validation history, known issues, and handoff notes.

### `src/`

Legacy workbench placeholder. Business code now lives in `android/` and `backend/`.

### `tests/`

Default location for tests.

### `scripts/`

Default location for helper scripts, setup scripts, validation scripts, or data-processing scripts.

### `docs/`

Default location for additional project documentation, design notes, diagrams, reports, or generated documentation.

## How to Fill `Prompt.md`

Before asking Codex to implement a project, edit `Prompt.md`.

At minimum, fill in:

1. Project name
2. One-sentence goal
3. Background
4. Target users
5. Core problem
6. Project type
7. P0 requirements
8. Non-goals
9. Technical constraints
10. Inputs and outputs
11. Data requirements
12. External dependencies
13. Acceptance criteria
14. Done When checklist

Keep P0 small and concrete. P0 should describe the minimum useful version, not the final dream version.

Good P0 examples:

- "A user can upload a CSV and see a summary table."
- "The API exposes `POST /items` and `GET /items` with validation."
- "The Android app opens to a main screen and saves one local note."
- "The ML baseline trains on a small sample and prints accuracy."

Weak P0 examples:

- "Make it smart."
- "Support everything."
- "Build a complete production system."

## How to Ask Codex to Start

After `Prompt.md` is filled, ask Codex something like:

```text
Please read AGENTS.md, Prompt.md, Plan.md, Implement.md, and Documentation.md, then start executing the project according to Implement.md from the first unfinished milestone.
```

Codex should then:

1. Read the control files.
2. Identify the current unfinished milestone.
3. Implement only that milestone.
4. Run validation.
5. Update `Documentation.md`.
6. Continue to the next milestone unless blocked.

## Recommended Development Flow

1. Fill `Prompt.md`.
2. Ask Codex to run Milestone 0.
3. Review the selected stack and assumptions in `Documentation.md`.
4. Ask Codex to continue to Milestone 1.
5. Review the minimal running skeleton.
6. Let Codex implement P0 features one milestone at a time.
7. Validate after each milestone.
8. Keep P1 and P2 features out until P0 is stable.
9. Finish with Milestone 7 documentation and delivery.

## Adapting Common Project Types

### Android App

Use `Prompt.md` to specify target SDK, UI requirements, device requirements, storage needs, and main user flows. Validation usually includes `./gradlew test` and `./gradlew assembleDebug`.

### Web Frontend

Specify framework preference, pages, components, browser support, styling constraints, and expected user flows. Validation often includes `npm test`, `npm run lint`, `npm run build`, and a visual check.

### Full-Stack Web App

Specify frontend, backend, API boundaries, database, authentication needs, and deployment expectations. Validation should include backend tests, frontend build, and at least one end-to-end manual workflow.

### Backend API Service

Specify endpoints, request and response formats, validation rules, persistence, authentication, and error behavior. Validation should include automated tests and manual API calls when useful.

### Python Tool

Specify command-line usage, inputs, outputs, file formats, and error cases. Validation usually includes `python -m pytest` and sample command execution.

### Desktop App

Specify operating system targets, UI framework preference, packaging expectations, file access, and main workflows. Validation should include launch steps and manual workflow checks.

### Database Project

Specify database engine, schema, seed data, queries, migrations, and expected outputs. Validation should run SQL scripts and record results.

### Data Analysis Project

Specify datasets, analysis questions, output charts or tables, reproducibility needs, and expected report artifacts. Validation should run the analysis path on sample data.

### ML / NLP Baseline

Specify dataset, model family, metrics, baseline method, training constraints, and expected artifacts. Validation should run on a small sample before larger runs.

### Course Assignment or Research Code

Specify grading requirements, deliverables, allowed libraries, report format, and reproducibility expectations. Validation should match the assignment or paper requirements as closely as possible.

## Notes and Constraints

- This workbench does not choose a technology stack until `Prompt.md` describes the actual project.
- Do not add business functionality before requirements are filled in.
- Keep the first implementation minimal.
- Every milestone should have a validation method.
- Important changes must be recorded in `Documentation.md`.
- If Codex makes an assumption, it must record that assumption.
- If Codex is blocked, it must stop, record the reason, list attempted fixes, and suggest the next smallest action.
- Avoid complex dependencies and infrastructure unless the project explicitly needs them.
