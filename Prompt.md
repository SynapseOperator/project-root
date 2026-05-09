# Prompt.md

This file is the active source of truth for current project requirements.

## Version Note

This is the active prompt for the next Yuelu Traffic phase.

Previous prompts must be archived under `docs/prompts/` for history only. The root `Prompt.md` is the only active requirement source.

## 1. Project Name

Yuelu Traffic

The app display name remains `Yuelu Traffic`.

## 2. Current Product Phase

The Android app has passed phone-side functional testing. The current phase is to move from a polished Chinese prototype with partial backend integration to a more complete, deployable Android product.

This phase focuses on:

1. Full Android backend API integration.
2. Persistent login and session handling.
3. Production map SDK integration.
4. More polished UI and interaction details.
5. Release-preparation work such as app icon, launch screen, privacy page, screenshots, and phone/backend setup documentation.

## 3. One-Sentence Goal

Upgrade Yuelu Traffic into a backend-connected, map-based Android app where login, traffic reports, accident board, leaderboard, profile, and admin workflows all use real backend APIs, while continuing to improve the Chinese campus-style UI and preparing the app for realistic deployment testing.

## 4. Current Known State

Already working:

- Android app builds and runs on phone.
- Simplified Chinese map-first UI exists.
- Core traffic workflows are backend-connected:
  - Student-number login.
  - Current user session.
  - Traffic report list.
  - Traffic report creation.
  - Traffic report detail refresh.
  - Traffic report feedback.
- Backend APIs exist for auth, reports, accidents, leaderboard, and admin moderation.
- Accident board, leaderboard, and admin pages currently remain local/demo or partially connected.
- Current map is still a Compose mock map.
- Production map SDK is not yet integrated.
- Docker/PostgreSQL runtime validation and real deployment validation remain incomplete.

## 5. Product Direction

The product should remain:

- Simplified Chinese first.
- Map-first.
- Clear, fast, and useful.
- Visually polished and campus-friendly.
- Slightly playful in low-risk places.
- Serious in privacy, accident, moderation, safety, and legal contexts.

Playful copy may be used for light UI moments, for example:

- 路况有点寄
- 堵麻了
- 这条消息还活着吗？
- 已失效，散了散了
- 上报一下，给同学们续一秒

Do not use playful language for injury, privacy confirmation, contact exchange, bans, admin actions, or legal/safety notices.

## 6. Visual Design Direction

Continue the current direction, but improve polish:

- Main palette: campus green with clean high-contrast surfaces.
- Accent colors: warning yellow, signal red, construction orange, road-control purple, info blue.
- UI should feel like a useful map tool with student-flavored personality.
- Improve hierarchy, spacing, icon usage, bottom sheets, cards, empty states, loading states, and error states.
- Strengthen visual design for:
  - Map markers and marker detail sheet.
  - Accident board cards.
  - Leaderboard ranking surface.
  - Admin moderation dashboard.
  - Profile page.
  - Backend/offline state messaging.
- Optional mascot `MQ` may appear in empty states, tips, and success states, but must not distract from core workflows.

## 7. Project Type and Stack

Project type:

- [x] Android app
- [x] Backend API service
- [x] Database project
- [x] Small complete software engineering project

Current stack:

- Android: Kotlin + Jetpack Compose + Material 3.
- Backend: Java 21 + Spring Boot REST API.
- Database: PostgreSQL target with Flyway migrations.
- Build: Gradle.
- Deployment path: Docker Compose / server-hosted backend.

## 8. P0 Functional Requirements

### 8.1 Full Backend Integration

Android P0 must connect these flows to real backend APIs:

1. Login and current user.
   - Student-number login uses backend auth API.
   - App retrieves current user from backend.
   - App does not expose raw student number.

2. Persistent session.
   - Store auth token locally.
   - Reopen app into signed-in state when token is valid.
   - Support logout.
   - If token is invalid or expired, return to login with a clear Chinese message.

3. Traffic reports.
   - List reports from backend.
   - Create reports through backend.
   - Refresh report detail from backend.
   - Submit feedback through backend.
   - Reflect backend confidence/status values in UI.

4. Accident board.
   - Accident list uses backend.
   - Accident creation uses backend.
   - Contact request uses backend.
   - Contact confirmation uses backend.
   - Contact details remain hidden until mutual confirmation.
   - P0 may use the existing backend protection model; production-grade field encryption can be P1.

5. Leaderboard and profile.
   - Leaderboard uses backend data.
   - Profile shows backend user points, reputation, title, public code, and restriction state where available.
   - Student number must never be shown publicly.

6. Admin.
   - Admin page remains inside the Android app.
   - Admin entry is visible only for admin/demo-admin users.
   - Admin can review backend data.
   - Admin can hide reports.
   - Admin can hide accident posts.
   - Admin can apply or clear user posting restrictions.
   - Admin actions use serious Chinese copy.

### 8.2 Backend Connectivity Modes

The app should support these environments:

1. Default development mode:
   - Android emulator connects to host backend through `10.0.2.2`.
   - Physical phone can connect to backend through LAN IP configuration.

2. Optional deployment mode:
   - App can point to a public backend URL.

3. Configuration:
   - Backend base URL must be configurable.
   - Do not hardcode one machine-specific IP as the only option.
   - README must explain emulator, LAN phone, and deployed-backend setup.

### 8.3 Map SDK Integration

This phase should integrate a production Android map SDK.

Requirements:

- Use the provided Android map SDK key through local configuration.
- Do not commit the raw SDK key into the repository.
- Prefer a provider abstraction so the app can fall back to the existing Compose mock map if SDK setup fails.
- Map should center around Central South University and Lushan South Road.
- Backend reports should appear as markers on the real map.
- Marker type and confidence should be visually distinct.
- Tapping a marker opens the report detail sheet.
- The app must still build in environments without the map key, using mock map fallback.

Implementation note:

- Treat the provided key as local/development credential material.
- Store via `local.properties`, environment variable, ignored resource file, or equivalent secure local config.
- Include setup instructions in README.
- Follow the current official map SDK documentation for key configuration, manifest metadata, lifecycle handling, and privacy compliance requirements.

### 8.4 UI Polish

Improve UI beyond the current version:

- Map home:
  - Better marker styling.
  - Better report summary card.
  - Better detail bottom sheet.
  - Clear loading/offline/empty states.
  - Filter chips for report types.

- Report submission:
  - More refined type selector.
  - Better success feedback.
  - Better validation messages.
  - Faster return to map after submission.

- Accident board:
  - More polished cards.
  - Clear status chips.
  - Clear privacy messaging.
  - Better contact confirmation flow.

- Leaderboard:
  - Real rankings from backend.
  - Better ranking visual hierarchy.
  - Fun but safe titles such as 路况情报员、校园雷达、不堵车研究员.

- Admin:
  - Dashboard-like stats.
  - Cleaner review queues.
  - Clear destructive-action confirmation.
  - Serious moderation language.

- Profile:
  - Public code.
  - Points/reputation/title.
  - Login state.
  - Logout.
  - Backend endpoint/status info for debugging.

### 8.5 Release Preparation

P0 should include:

- App icon.
- Launch screen / splash screen.
- In-app privacy and safety page.
- README phone-backend connection guide.
- README map SDK key setup guide.
- Manual test checklist for phone or emulator.
- Updated screenshots or screenshot instructions if screenshots cannot be captured locally.

## 9. P1 Requirements

1. Production-grade accident contact encryption.
   - Encrypt contact values at rest.
   - Keep contact values out of public APIs.
   - Document retention and deletion assumptions.

2. PostgreSQL / Docker runtime validation.
   - Run backend against PostgreSQL through Docker Compose where possible.
   - Record results in `Documentation.md`.
   - If Docker is unavailable, document manual validation steps.

3. Stronger end-to-end testing.
   - Phone/emulator test against live backend.
   - Validate login, map report list, create report, feedback, accident creation, contact confirmation, leaderboard, and admin action.

4. Improved offline/degraded mode.
   - Cached last successful report list.
   - Clear offline state.
   - Retry action.

5. More refined MQ mascot usage.
   - Empty state.
   - Success message.
   - Onboarding helper.
   - No mascot in serious flows unless restrained.

## 10. P2 Requirements

1. Push notifications for high-confidence nearby road-condition events.
2. Image evidence and watermarking.
3. QQ group / campus channel import with documented permission.
4. Web admin dashboard.
5. Advanced map features:
   - Search.
   - Locate me.
   - Report clustering.
   - Custom map style.
6. Release packaging improvements:
   - Signed release build.
   - Versioning.
   - Crash/error reporting if appropriate.

## 11. Non-Goals

This phase must not include:

1. Any feature that helps users evade law enforcement.
2. Route planning around traffic management or enforcement presence.
3. Public display of student numbers.
4. Public display of contact information before mutual confirmation.
5. Unapproved scraping of private groups or channels.
6. Paid map SDK purchase work unless explicitly required later.
7. Replacing the Android app with a web frontend.
8. Rewriting the backend from scratch.
9. Adding large UI frameworks outside the current Compose stack.
10. Committing SDK keys, backend secrets, signing credentials, or other private credentials to Git.

## 12. Safety and Privacy Requirements

- The app is a traffic safety and public road-condition reporting tool.
- User-facing copy must avoid unlawful evasion framing.
- Student numbers must remain private.
- Public identity must use public user code.
- Contact information must remain hidden until mutual confirmation.
- Admin actions must be auditable where backend support exists.
- SDK key and backend secrets must not be committed to Git.
- Map SDK privacy/compliance text must be reflected in the privacy page if required by the SDK.

## 13. Acceptance Criteria

This phase is acceptable when:

1. Android app builds successfully.
2. Backend tests pass.
3. Android unit tests pass.
4. Safety text scan passes.
5. Chinese text scan passes.
6. Android login persists session across app restart.
7. Logout works.
8. Backend base URL is configurable.
9. App can connect to backend in emulator mode.
10. App can be configured for phone-over-LAN backend access.
11. Traffic report list, create, detail, and feedback use backend.
12. Accident board list/create/contact request/contact confirmation use backend.
13. Leaderboard uses backend.
14. Profile uses backend user data.
15. Admin review/moderation actions use backend.
16. Map SDK view works when key is configured.
17. Mock map fallback works when key is unavailable.
18. Backend report markers render on the map.
19. Marker tap opens report detail.
20. UI remains fully Simplified Chinese for user-facing screens.
21. No obvious garbled text remains.
22. App icon and launch screen exist.
23. In-app privacy/safety page exists.
24. README explains backend setup, phone connection, emulator connection, and map key setup.
25. `Documentation.md` records completed work, validation commands, failures, fixes, and remaining risks.

## 14. Done When Checklist

- [ ] Previous active prompt is archived under `docs/prompts/`.
- [ ] Root `Prompt.md` contains this active requirement set.
- [ ] Login/session/logout are backend-connected and persistent.
- [ ] Traffic reports are backend-connected.
- [ ] Accident board is backend-connected.
- [ ] Leaderboard is backend-connected.
- [ ] Profile is backend-connected.
- [ ] Admin moderation is backend-connected.
- [ ] Map SDK is integrated with secure local key configuration.
- [ ] Mock map fallback remains available.
- [ ] UI polish improvements are applied to map, report detail, accident board, leaderboard, admin, and profile.
- [ ] App icon exists.
- [ ] Launch screen exists.
- [ ] Privacy/safety page exists.
- [ ] README is updated.
- [ ] Phone/emulator manual test checklist exists.
- [ ] Android build passes.
- [ ] Backend tests pass.
- [ ] Android tests pass.
- [ ] Safety and Chinese text scans pass.
- [ ] `Documentation.md` is updated.
