# Documentation.md

This file is the live status log for the project.

It records what Codex did, why it did it, what is currently complete, what failed, what was assumed, and what should happen next.

## Current Status

Status:

- [ ] Not started
- [x] In progress
- [ ] Blocked
- [ ] Completed

Current milestone:

Milestone 0 complete; ready for Milestone 1 minimal running skeleton

Last updated:

2026-05-09

## Project Summary

Brief summary of what this project is building:

This repository is now scoped to build Yuelu Traffic, a deployable Android-based crowdsourced traffic safety and incident reporting app for the Central South University and Lushan South Road area. The app will support map-based road condition reports, community validation, reputation and leaderboard mechanics, a privacy-aware accident board, and administrator moderation.

## Technology Stack

Selected stack:

- Android language: Kotlin
- Android UI: Jetpack Compose with Material 3
- Android architecture: MVVM with ViewModel, StateFlow, repositories, Retrofit/OkHttp, DataStore, and an AMap-backed map adapter
- Backend language: Java 21
- Backend framework: Spring Boot REST API
- Database: PostgreSQL with Flyway migrations
- Testing: Android JVM unit tests, backend JUnit/Spring tests, API/privacy contract checks, and emulator/manual checks when UI exists
- Build tool: Gradle for Android and backend projects
- Deployment: Docker Compose for local PostgreSQL/backend setup, then JVM-capable server or cloud deployment

Reason for selection:

This stack keeps the Android client native, keeps the backend conservative and deployable, uses persistent relational storage, and supports a China-area map provider while allowing fake map adapters for tests and credential-free local validation.

## Milestone Progress

| Milestone | Status | Notes |
|---|---|---|
| Milestone 0 — Project Understanding and Setup | Completed | Technical stack, architecture, module boundaries, data model draft, API draft, and validation strategy recorded in `docs/Architecture.md`. |
| Milestone 1 — Minimal Running Skeleton | Next | Initialize Android and backend skeletons only; no P0 feature implementation beyond bootstrapping. |
| Milestone 2 — Core P0 Feature 1 | Not started | |
| Milestone 3 — Core P0 Feature 2 | Not started | |
| Milestone 4 — Core P0 Feature 3 | Not started | |
| Milestone 5 — Integration and Error Handling | Not started | |
| Milestone 6 — Tests and Quality Check | Not started | |
| Milestone 7 — Final Documentation and Delivery | Not started | |

## Work Log

Use the following template for every meaningful work session or milestone update.

### Entry Template

Date:

Milestone:

Files changed:

Work completed:

Commands run:

Results:

Failures:

Fixes:

Decisions:

Next step:

---

### 2026-05-09 — Workbench Setup

Date: 2026-05-09

Milestone: Workbench setup before project milestones

Files changed:

- `AGENTS.md`
- `Prompt.md`
- `Plan.md`
- `Implement.md`
- `Documentation.md`
- `README.md`
- `src/`
- `tests/`
- `scripts/`
- `docs/`

Work completed:

- Created the general-purpose Codex project development workbench.
- Added control files for requirements, planning, autonomous execution, and live documentation.
- Created generic source, test, script, and documentation directories.

Commands run:

- `Get-ChildItem -Force`
- `git rev-parse --is-inside-work-tree`
- `New-Item -ItemType Directory -Force -Path src,tests,scripts,docs`

Results:

- Repository workbench files and directories were created.
- Current directory is not a git repository.

Failures:

- None.

Fixes:

- None.

Decisions:

- The workbench is technology-neutral and does not implement business functionality.
- Control files are written for long-term Codex-guided project execution.

Next step:

- Fill in `Prompt.md` with the real project requirements, then ask Codex to start executing according to `Implement.md`.

---

### 2026-05-09 - Yuelu Traffic Requirements Finalized

Date: 2026-05-09

Milestone: Requirements definition before implementation milestones

Files changed:

- `Prompt.md`
- `Documentation.md`

Work completed:

- Replaced the placeholder `Prompt.md` with the approved Yuelu Traffic requirements.
- Scoped the project as a deployable Android app with backend API, persistent database, map-based traffic reports, reputation mechanics, leaderboard and titles, accident board, privacy-aware contact exchange, and admin moderation.
- Recorded the legal and safety boundary that the app must be a lawful traffic safety and public road-condition reporting tool, not a tool for evading law enforcement.

Commands run:

- `Get-Content -Raw Prompt.md`
- `Get-Content -Raw Plan.md`
- `Get-Content -Raw Implement.md`
- `Get-Content -Raw Documentation.md`

Results:

- `Prompt.md` is now sufficiently defined for Milestone 0 planning.
- No implementation code was written.

Failures:

- None.

Fixes:

- None.

Decisions:

- Accept the user's clarified positioning: keep traffic management / public enforcement presence as a report type only within a lawful safety and public-information framing.
- Put QQ group and campus channel ingestion in P2 with consent and privacy constraints.
- Keep shake-to-match and image watermarking outside MVP.

Assumptions:

- Student number input is a lightweight internal identifier and not formal university identity verification.
- The project should target real deployment, but exact Android, backend, database, and map SDK choices will be made in Milestone 0.

Next step:

- Start Milestone 0 by selecting a minimal deployable technology stack and adapting validation commands to the selected Android/backend architecture.

---

### 2026-05-09 - Milestone 0 Technical Architecture

Date: 2026-05-09

Milestone: Milestone 0 - Project Understanding and Setup

Files changed:

- `docs/Architecture.md`
- `Documentation.md`

Work completed:

- Selected the MVP technology stack: Kotlin Android app, Jetpack Compose UI, Java 21 Spring Boot backend, PostgreSQL persistence, Flyway migrations, Gradle builds, and AMap behind a map-provider adapter.
- Defined planned repository layout for `android/`, `backend/`, `docs/`, `docs/api/`, `scripts/`, and cross-project test assets.
- Split backend responsibilities into auth, users, reports, feedback, confidence, reputation, accidents, contacts, moderation, location, admin, and common infrastructure modules.
- Split Android responsibilities into app shell, auth, map, reports, accidents, leaderboard, profile, admin, network, domain, storage, and design modules.
- Drafted relational data models for users, traffic reports, feedback, reputation events, accident posts, contact exchange requests, protected contact offers, moderation flags, admin actions, and user restrictions.
- Drafted REST API endpoints for auth, current user, leaderboard, reports, feedback, accident board, contact exchange, and admin moderation.
- Defined validation strategy for skeleton builds, backend tests, Android tests, API/privacy contract checks, manual map checks, and end-to-end MVP workflow validation.
- Deferred actual Android/backend project initialization and all business code to Milestone 1 or later.

Commands run:

- `Get-Content -Raw Prompt.md`
- `Get-Content -Raw Plan.md`
- `Get-Content -Raw Implement.md`
- `Get-Content -Raw Documentation.md`
- `Get-Content -Raw AGENTS.md`
- `Get-Content -Raw README.md`
- `Get-ChildItem -Force`
- `Get-ChildItem -Recurse -File -Force | Select-Object -ExpandProperty FullName`
- `git status --short`
- `git branch --show-current`
- `git diff -- Prompt.md`
- `git diff -- Documentation.md`
- `Select-String -Path 'docs\Architecture.md' -Pattern 'Selected Technology Stack|Module Boundaries|Data Model Draft|API Draft|Validation Strategy'`
- `Get-ChildItem -Force 'android','backend' -ErrorAction SilentlyContinue`
- `git diff --stat`
- `git add Prompt.md Documentation.md docs/Architecture.md`
- `git commit -m "Complete milestone 0 architecture design"`

Results:

- Required control files were read.
- Existing repository structure was inspected before edits.
- Existing uncommitted changes in `Prompt.md` and `Documentation.md` were preserved.
- Milestone 0 architecture design is documented in `docs/Architecture.md`.
- Document section check found stack selection, module boundaries, data model draft, API draft, and validation strategy.
- No `android/` or `backend/` project directories were created during this design-only milestone.
- No business code was written.
- Milestone 0 commit was created and amended with message `Complete milestone 0 architecture design`.

Failures:

- None.

Fixes:

- None.

Decisions:

- Use Android-only role-gated admin screens for P0 unless a later requirement explicitly needs a web dashboard.
- Use PostgreSQL latitude/longitude columns and pilot-area bounding-box validation for MVP; defer PostGIS until larger geospatial needs justify it.
- Use AMap as the production map provider for the China-area pilot, wrapped behind a map adapter so tests can run without map credentials.
- Hash student numbers on the backend with a server-side pepper and never expose raw student numbers publicly.
- Store contact values only for mutual-confirmation accident exchange and never return them from public APIs.

Assumptions:

- AMap credentials may not be available in local development, so a fake/static map provider must be available for tests.
- P0 admin functionality can be delivered inside the Android app with role-protected backend APIs.
- Backend deployment can start with Docker Compose locally and a JVM-capable server or cloud instance for production instructions.

Next step:

- Start Milestone 1 by initializing the minimal Android and backend skeletons with build/test commands, without implementing P0 business workflows yet.

---

## Decisions

| Date | Decision | Reason |
|---|---|---|
| 2026-05-09 | Use a technology-neutral workbench structure. | The repository must support many project types instead of one fixed stack. |
| 2026-05-09 | Start with `Prompt.md` as the requirements source of truth. | Future implementation needs a clear scope and acceptance criteria before code changes. |
| 2026-05-09 | Use milestone-based execution. | Small validated stages reduce drift and make progress auditable. |
| 2026-05-09 | Position Yuelu Traffic as a lawful traffic safety and road-condition reporting app. | The product must not help users evade law enforcement or violate traffic rules. |
| 2026-05-09 | Use Kotlin, Jetpack Compose, Java 21 Spring Boot, PostgreSQL, Flyway, and Gradle for MVP implementation. | This is a conservative deployable Android/backend stack that fits the P0 scope without adding a separate web frontend. |
| 2026-05-09 | Wrap AMap behind a map-provider adapter. | The pilot area is in China, but local tests should not require map credentials. |
| 2026-05-09 | Keep P0 admin functionality inside the Android app unless requirements change. | Avoids adding a web frontend stack before it is necessary. |

## Assumptions

| Date | Assumption | Reason | Risk |
|---|---|---|---|
| 2026-05-09 | No concrete business feature should be implemented during workbench setup. | The current task only asks for the generic control system. | Future project work cannot begin until `Prompt.md` is filled in. |
| 2026-05-09 | Empty directories are acceptable as placeholders for future work. | The requested structure includes `src/`, `tests/`, `scripts/`, and `docs/`. | Some tools may ignore empty directories if the project is later committed without placeholder files. |
| 2026-05-09 | Student number login is a lightweight app identifier, not formal university identity verification. | The user requested student number input but specified it is only for distinguishing users. | Additional privacy and storage decisions are still needed during implementation planning. |
| 2026-05-09 | AMap SDK credentials may be unavailable in local development. | Map SDK services commonly require keys, but Milestone 1 should still be buildable. | A fake map adapter and manual production map check are needed. |
| 2026-05-09 | PostgreSQL without PostGIS is enough for MVP. | The pilot area is small and bounding-box filtering is sufficient for first implementation. | Larger coverage or radius queries may later require PostGIS migration. |

## Validation History

| Date | Command / Check | Result | Notes |
|---|---|---|---|
| 2026-05-09 | `Get-ChildItem -Force` | Passed | Confirmed initial directory state and created workbench directories. |
| 2026-05-09 | `git rev-parse --is-inside-work-tree` | Not a git repository | No milestone commit was created. |
| 2026-05-09 | `gh auth status` | Not available | GitHub CLI is not installed in this environment. |
| 2026-05-09 | `git init` | Passed | Initialized local Git repository. |
| 2026-05-09 | `git branch -M main` | Passed | Renamed default branch to `main`. |
| 2026-05-09 | `git commit -m "Initialize Codex project workbench"` | Passed | Created initial local commit `ce9fbdf`. |
| 2026-05-09 | GitHub connector repository listing | No repositories returned | No target GitHub repository could be inferred automatically. |
| 2026-05-09 | `git remote add origin https://github.com/SynapseOperator/project-root.git` | Passed | Configured GitHub repository as `origin`. |
| 2026-05-09 | `git push -u origin main` | Passed | Pushed local `main` branch and set upstream tracking. |
| 2026-05-09 | Milestone 0 document review | Passed | `docs/Architecture.md` records stack selection, module boundaries, data model, API draft, and validation strategy. No code validation was required. |
| 2026-05-09 | `git commit -m "Complete milestone 0 architecture design"` | Passed | Created and amended the Milestone 0 commit. |

## Known Issues

| Issue | Severity | Status | Notes |
|---|---|---|---|
| `Prompt.md` is not filled with a concrete project yet. | Medium | Resolved | `Prompt.md` now defines Yuelu Traffic requirements. |
| GitHub remote is not configured. | Low | Resolved | `origin` is configured as `https://github.com/SynapseOperator/project-root.git`. |

## Final Handoff Notes

To be completed at the end of the actual project.

### 2026-05-09 - GitHub Repository Preparation

Date: 2026-05-09

Milestone: Repository setup for GitHub handoff

Files changed:

- `.gitignore`
- `src/.gitkeep`
- `tests/.gitkeep`
- `scripts/.gitkeep`
- `docs/.gitkeep`
- `Documentation.md`

Work completed:

- Prepared the workbench files for an initial Git commit.
- Added a general-purpose `.gitignore` for common editor, OS, log, environment, Python, and Node artifacts.
- Added `.gitkeep` placeholders so the intended empty workbench directories are preserved by Git.
- Checked whether GitHub CLI is available for direct GitHub repository creation or push.

Commands run:

- `git rev-parse --is-inside-work-tree`
- `git status --short`
- `Get-ChildItem -Force`
- `gh auth status`
- `git --version`
- `git config --get user.name`
- `git config --get user.email`

Results:

- The directory was not a Git repository before this session.
- Git is installed and has user identity configured.
- GitHub CLI is not installed, so automatic GitHub repository creation or push through `gh` is unavailable.
- The directory is now initialized as a local Git repository on branch `main`.
- The initial workbench commit was created as `ce9fbdf`.
- The GitHub connector returned no repositories, so a target GitHub repository could not be inferred.

Failures:

- `gh auth status` failed because `gh` is not installed.

Fixes:

- Prepared the local repository for a normal Git remote push instead.

Decisions:

- Keep this as a technology-neutral workbench because `Prompt.md` still has no concrete project requirements.
- Do not select a stack or implement business functionality as part of GitHub setup.

Assumptions:

- The requested GitHub handoff means preserving the current workbench as a Git-tracked project and preparing it to be pushed to a target GitHub repository.

Next step:

- Add a GitHub remote with the target repository URL and push the initial commit.

### 2026-05-09 - Local Git Repository Initialized

Date: 2026-05-09

Milestone: Repository setup for GitHub handoff

Files changed:

- `.git/` local repository metadata
- `Documentation.md`

Work completed:

- Initialized `D:\project-root` as a local Git repository.
- Renamed the default branch to `main`.
- Created the first commit containing the workbench control files, README, `.gitignore`, and placeholder directories.
- Checked for a configured remote and available GitHub repositories.

Commands run:

- `git init`
- `git branch -M main`
- `git add .`
- `git commit -m "Initialize Codex project workbench"`
- `git status --short`
- `git log --oneline -1`
- `git remote -v`
- GitHub connector repository listing

Results:

- Local commit created: `ce9fbdf Initialize Codex project workbench`.
- Working tree was clean after the initial commit.
- No Git remote is configured yet.
- GitHub connector did not expose a repository list, so the target remote must be supplied manually.

Failures:

- None for local Git initialization.

Fixes:

- None.

Decisions:

- Use `main` as the default branch name for GitHub compatibility.
- Stop before pushing because no target GitHub repository URL is known.

Assumptions:

- The target GitHub repository already exists or will be created by the user before pushing.

Next step:

- Provide the target repository URL, then run `git remote add origin <repo-url>` and `git push -u origin main`.

### 2026-05-09 - GitHub Remote Pushed

Date: 2026-05-09

Milestone: Repository setup for GitHub handoff

Files changed:

- `.git/config` local repository metadata
- `Documentation.md`

Work completed:

- Added the GitHub repository as the `origin` remote.
- Pushed the local `main` branch to GitHub.
- Set local `main` to track `origin/main`.

Commands run:

- `git status --short`
- `git remote -v`
- `git branch --show-current`
- `git remote add origin https://github.com/SynapseOperator/project-root.git`
- `git push -u origin main`

Results:

- Push succeeded.
- Remote URL: `https://github.com/SynapseOperator/project-root.git`
- Branch tracking: `main` tracks `origin/main`.

Failures:

- None.

Fixes:

- None.

Decisions:

- Use the user-provided GitHub repository as the canonical remote.

Assumptions:

- The user-provided GitHub repository is the intended destination for this workbench.

Next step:

- Fill in `Prompt.md` with concrete project requirements before starting implementation milestones.
