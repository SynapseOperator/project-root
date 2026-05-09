# Prompt.md

This file is the active source of truth for current project requirements.

## Version Note

This is the current active product prompt for the next development phase of Yuelu Traffic.

Previous requirement snapshots should be archived under `docs/prompts/` for history only. Archived prompts are not active requirements unless explicitly copied back into this root `Prompt.md`.

## 1. Project Name

Yuelu Traffic

App display name remains:

Yuelu Traffic

## 2. Current Product Phase

The app already builds and can run on an Android phone. The next phase is a product polish and usability upgrade focused on:

1. Full Simplified Chinese localization.
2. A complete Android UI redesign.
3. A map-first user experience.
4. Connecting core Android flows to the existing backend APIs.
5. Preserving all existing safety, privacy, and lawful traffic-information boundaries.

This phase should improve the existing product without expanding into unrelated new features.

## 3. One-Sentence Goal

Upgrade Yuelu Traffic into a Simplified Chinese, visually polished, map-first Android app for Central South University and Lushan South Road users, with a clear, useful, slightly playful campus-style interface and backend-connected core traffic report workflows.

## 4. Background

Yuelu Traffic currently has implemented backend APIs and a runnable Android app, but the Android UI is still a rough local-state prototype. Current problems include:

- User-facing text is mostly English.
- Some text appears garbled.
- The UI looks too plain and unpolished for real users.
- Android screens are not yet structured like a real product.
- Android core traffic flows are not yet connected to backend APIs.
- The current map is a simulated Compose panel, not a production map SDK view.

This phase should make the app feel usable, clear, and interesting for Chinese university students while keeping serious flows such as accidents, privacy, and admin actions appropriately restrained.

## 5. Target Users

Primary users:

- Students and daily commuters around Central South University and Lushan South Road.

Secondary users:

- Users who need to report traffic conditions.
- Users who need to check nearby road conditions quickly.
- Users involved in minor traffic incidents.
- Administrators reviewing abuse reports and invalid content.

User environment:

- Android phone.
- Chinese-language interface.
- Map-first interaction.
- Short-session mobile usage while walking, commuting, or checking road conditions.

## 6. Product Personality and UX Direction

The product should be:

- Clear and fast to understand.
- Visually polished and modern.
- Slightly playful, with a university-student sense of humor.
- Useful before it is decorative.
- Serious where privacy, accidents, moderation, or safety are involved.

Acceptable playful copy examples:

- "路况有点寄"
- "堵麻了"
- "这条消息还活着吗？"
- "已失效，散了散了"
- "上报一下，给同学们续一秒"

Tone limits:

- Accident, contact exchange, privacy, admin moderation, and legal/safety notices must use clear and respectful language.
- The app must not joke about injury, harassment, illegal behavior, or evading law enforcement.
- The app must not provide routing, tactics, or instructions for unlawful traffic behavior.

## 7. Visual Design Direction

Recommended initial visual direction:

- Main palette: fresh campus green with high-contrast black/white text.
- Accent colors: warning yellow, signal red, road-control purple, construction orange, info blue.
- Style: clean map utility plus playful campus details.
- Visual density: map-first and scan-friendly, not a marketing landing page.
- Components: Material 3 / Jetpack Compose design system.
- Corners: moderate rounded corners, not overly soft.
- Typography: clear Chinese mobile typography, no oversized decorative text inside dense tools.
- Icons: use meaningful icons for report types and actions when possible.

Optional mascot:

- A small mascot named `MQ`.
- MQ may appear in empty states, tips, onboarding, or success messages.
- MQ is optional and must not block core functionality.
- MQ should not appear in accident privacy confirmation or admin abuse workflows unless the tone remains appropriate.

## 8. Project Type

- [x] Android app
- [x] Backend API service
- [x] Database project
- [x] Small complete software engineering project
- [ ] Web frontend
- [ ] Desktop app
- [ ] Machine learning / NLP experiment

Current stack:

- Android: Kotlin + Jetpack Compose + Material 3.
- Backend: Java 21 + Spring Boot REST API.
- Database: PostgreSQL target with Flyway migrations; local development may use existing test/development configuration.
- Build: Gradle.

## 9. Current Known Product State

The app currently has:

- Android project that builds and runs.
- Backend APIs for login, traffic reports, feedback, accident board, leaderboard, and admin moderation.
- Android local-state MVP screens for login, map-style report markers, report submission, feedback, leaderboard, accident board, contact confirmation, and admin actions.

Known limitations to address or preserve:

- Android UI currently uses local in-app state for MVP demonstration.
- Android core traffic flows are not fully connected to backend APIs.
- The current map is a credential-free simulated Compose panel.
- Production map SDK integration is deferred to a later phase.
- Accident board and admin backend integration can be deferred after core traffic API connection.

## 10. Functional Requirements

### P0 - Must Have

P0 defines the required scope for this phase.

1. Full Simplified Chinese localization.
   - All Android user-facing text must be Simplified Chinese.
   - This includes page titles, buttons, labels, placeholders, helper text, status text, empty states, error messages, report type names, feedback actions, leaderboard text, accident board text, admin text, and privacy notices.
   - Remove or fix all garbled characters.
   - Avoid hardcoded English user-facing strings in Compose UI.
   - Keep technical identifiers, package names, API paths, and internal code names in English where appropriate.

2. Preserve app name.
   - The app display name remains `Yuelu Traffic`.
   - Chinese text can explain the product, but the displayed product name should not be renamed.

3. Rebuild Android UI into a formal multi-page structure.
   Required pages or surfaces:
   - Login / student number entry page.
   - Map home page.
   - Traffic report submission page.
   - Traffic report detail dialog or detail sheet.
   - Accident board page.
   - My/Profile page.
   - Leaderboard page.
   - Admin page or admin section.
   - Bottom navigation shell.

4. Map-first home screen.
   - After login, the first screen should be the map home page.
   - The map area must be the dominant visual element.
   - Report markers should be visually distinct by type and confidence.
   - Since map SDK integration is deferred, this phase may use a polished Compose mock map panel.
   - The mock map must look intentional and product-quality, not like a placeholder rectangle.

5. Bottom navigation.
   Required main tabs:
   - 地图
   - 上报
   - 事故栏
   - 我的

   Additional navigation:
   - 排行榜 can live under 我的 or be reachable from the map/profile surface.
   - 管理员入口 should be hidden under 我的 and visible only for admin users or demo admin state.

6. Redesign priority pages.
   If time is constrained, implement visual redesign in this order:
   1. Login page.
   2. Map home page.
   3. Traffic report submission page.
   4. Traffic report detail dialog/sheet.
   5. Accident board.
   6. My/Profile and leaderboard.
   7. Admin page.

7. Backend connection for core P0 traffic workflows.
   Android P0 must connect to backend APIs for:
   - Student-number login.
   - Current user/session display.
   - Traffic report list.
   - Traffic report creation.
   - Traffic report feedback: confirm valid / mark expired.

   Android P0 does not need to connect these workflows yet:
   - Accident board backend integration.
   - Admin backend integration.
   - Leaderboard backend integration, unless already easy and low-risk.

8. Keep local/demo fallbacks where useful.
   - If backend is unavailable, the app should show a clear Chinese error or offline/demo message.
   - The app should not silently pretend local data is live backend data.
   - Mock data may remain for visual demonstration, but it must be clearly separated from backend-connected state.

9. Traffic report types must be shown in Chinese.
   Required report types:
   - 交通管理提示
   - 施工
   - 拥堵
   - 道路封闭/管制
   - 事故/异常路况

10. Traffic report detail must be understandable.
    Detail view should show:
    - Report type.
    - Location label.
    - Submission time or relative time.
    - Description if present.
    - Confidence score or readable confidence level.
    - Status.
    - Confirm count and expired count if available.
    - Actions: "还有效", "已失效".
    - Clear copy explaining that community feedback affects report visibility.

11. Traffic report submission must be fast.
    Submission page should include:
    - Report type picker.
    - Location input or current selected location display.
    - Optional description.
    - Submit button.
    - Friendly success state.
    - Validation error state in Chinese.

12. Login page must be redesigned.
    Login page should include:
    - App name: Yuelu Traffic.
    - Short Chinese product description.
    - Student number input.
    - Privacy acknowledgement checkbox.
    - Clear statement that student number is used only to distinguish users and is not public identity verification.
    - Enter button.
    - Polished visual style matching the new design direction.

13. Accident board must be visually improved.
    - Accident board can remain local/demo data in P0.
    - It must be fully Chinese.
    - Contact privacy must remain explicit.
    - Contact details must not be shown by default.
    - Mutual confirmation language must be clear and serious.

14. Admin page must be visually improved.
    - Admin page can remain local/demo data in P0.
    - It must be fully Chinese.
    - It should show moderation stats and actions in a clearer layout.
    - Abuse, hiding, and restriction actions must use serious wording.

15. My/Profile page.
    The profile page should show:
    - Public user code, not raw student number.
    - Reputation/points/title if available.
    - Leaderboard entry.
    - Admin entry if applicable.
    - App safety/privacy notes.

16. Leaderboard.
    - Leaderboard must use public display codes, not student numbers.
    - It should include playful but not abusive title language.
    - Example titles may include "路况情报员", "校园雷达", "不堵车研究员".

17. Safety and legal boundary must be preserved.
    - The app may show public road-condition and traffic-management reports.
    - The app must not provide advice, route planning, tactics, or wording intended to help users evade law enforcement.
    - User-facing text must avoid "规避交警", "躲避执法", "逃避处罚", or similar framing.

18. Validation.
    - Android unit tests must pass.
    - Backend tests must pass if backend integration code changes.
    - Android debug APK must build.
    - Safety text scan must pass.
    - A Chinese text check or review must confirm no obvious English user-facing strings remain in the main Android UI.
    - Manual phone/emulator visual check should be performed if a device is available.

### P1 - Should Have

1. Connect additional Android flows to backend:
   - Accident board list/create/contact request.
   - Leaderboard.
   - Admin review actions.
   - User restrictions.

2. Add improved UI states:
   - Loading.
   - Empty.
   - Error.
   - Offline/backend unavailable.
   - Success.
   - Disabled/restricted posting.

3. Improve map interaction.
   - Marker tap opens detail sheet.
   - Filter chips for report types.
   - Confidence legend.
   - "附近有点动静" style summary card.

4. Add MQ mascot lightly.
   - Empty states.
   - Successful submission.
   - Login helper text.
   - No use in serious accident/contact/privacy warnings unless restrained.

5. Update README user-facing sections in Chinese or bilingual form.
   - Keep technical setup commands clear.
   - Add screenshots or visual notes if available.

6. Add screenshot-based UI documentation.
   - Capture redesigned login page.
   - Capture redesigned map home.
   - Capture report submission and detail sheet.
   - Capture accident board and profile/admin if implemented.

### P2 - Nice to Have

1. Production map SDK integration.
   - Deferred to next phase.
   - Prefer a provider abstraction so AMap/Baidu/Tencent can be swapped.
   - Requires developer account, Android key, package name, SHA1, SDK privacy compliance text, and testing on real device.
   - App must still build without real map credentials.

2. More advanced playful visual identity.
   - MQ mascot illustrations.
   - Custom report icons.
   - Light animation.
   - Campus-themed badges.

3. Push notifications.
   - Only for high-confidence nearby safety/road-condition events.
   - Must not encourage unlawful traffic behavior.

4. Image evidence and watermarking.
   - Deferred.
   - Must include privacy warnings before upload.

5. QQ group / campus channel import.
   - Deferred.
   - Only with documented permission and traffic-related content filtering.

## 11. Non-Goals

This phase must not include:

1. Production map SDK integration as a required deliverable.
2. Paid SDK purchase or commercial map authorization work.
3. New route planning or navigation behavior.
4. Any feature that helps users evade law enforcement.
5. Full accident board backend integration as a P0 requirement.
6. Full admin backend integration as a P0 requirement.
7. Image upload.
8. Push notifications.
9. Major backend schema redesign unless required for Android API integration.
10. A separate web admin dashboard.
11. Changing the app name away from `Yuelu Traffic`.

## 12. Technical Constraints

- Android must remain Kotlin + Jetpack Compose.
- Use Material 3 where practical.
- Existing backend APIs should be reused rather than redesigned.
- Avoid adding large UI frameworks.
- Prefer a small design system inside the Android module:
  - Colors
  - Typography
  - Spacing
  - Buttons
  - Cards/sheets
  - Report type styles
- User-facing text should move toward resources or centralized copy definitions rather than scattered hardcoded strings.
- Mock map must be componentized so a real map provider can replace it later.
- Backend API base URL should be configurable for development.
- App must build without map SDK credentials.

## 13. Input and Output

Expected user input:

- Student number.
- Privacy acknowledgement.
- Traffic report type.
- Location label or selected location.
- Optional report description.
- Report feedback: still valid / expired.
- Accident board post information.
- Contact confirmation information.
- Admin demo actions.

Expected output:

- Chinese login and privacy flow.
- Map-first home page.
- Chinese report markers and details.
- Chinese report submission confirmation.
- Chinese error/loading/empty states.
- Chinese accident board.
- Chinese profile/leaderboard/admin pages.
- Backend-connected traffic report workflows for login, list, create, and feedback.

## 14. Acceptance Criteria

This phase is acceptable when:

1. The Android app builds successfully.
2. The app can be installed and opened on a phone or emulator.
3. All main user-facing Android UI text is Simplified Chinese.
4. No obvious garbled characters remain in the Android UI.
5. App name remains `Yuelu Traffic`.
6. Login page is visually redesigned and usable.
7. Map page is the first signed-in screen.
8. Map page has a polished mock map and distinct report markers.
9. Bottom navigation includes 地图, 上报, 事故栏, 我的.
10. Traffic report submission is redesigned and Chinese.
11. Traffic report detail sheet/dialog exists and is Chinese.
12. Android login connects to backend.
13. Android traffic report list connects to backend.
14. Android traffic report creation connects to backend.
15. Android traffic feedback connects to backend.
16. Backend-unavailable state is visible in Chinese and does not mislead users.
17. Accident board is visually improved and Chinese, even if still local/demo in P0.
18. Admin page is visually improved and Chinese, even if still local/demo in P0.
19. Public user code is shown instead of raw student number.
20. Leaderboard does not expose student numbers.
21. Safety/legal wording remains compliant and does not encourage evading law enforcement.
22. `.\gradlew.bat :android:assembleDebug` passes.
23. `.\gradlew.bat :android:testDebugUnitTest` passes.
24. Backend tests pass if backend integration or API contracts are changed.
25. Safety text scan passes.
26. Documentation records what was redesigned, what was connected to backend, and what remains deferred.

## 15. Done When Checklist

The phase is done when:

- [ ] Previous Prompt is archived under `docs/prompts/`.
- [ ] Root `Prompt.md` contains this active requirement set.
- [ ] Android UI is fully Simplified Chinese for main user-facing flows.
- [ ] Garbled UI text is fixed.
- [ ] Formal multi-page Compose structure exists.
- [ ] Login page is redesigned.
- [ ] Map-first home page is redesigned.
- [ ] Report submission page is redesigned.
- [ ] Report detail sheet/dialog is implemented.
- [ ] Accident board is visually improved.
- [ ] My/Profile page exists.
- [ ] Leaderboard is accessible.
- [ ] Admin page is visually improved.
- [ ] Core traffic workflows connect to backend: login, list, create, feedback.
- [ ] Backend-unavailable state is handled clearly.
- [ ] App builds as a debug APK.
- [ ] Android unit tests pass.
- [ ] Relevant backend tests pass if backend-facing code changed.
- [ ] Safety text scan passes.
- [ ] Documentation is updated with validation results and remaining limitations.
- [ ] Production map SDK integration remains documented as deferred, not accidentally required for this phase.
