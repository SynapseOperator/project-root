# Prompt.md

This file is the source of truth for project requirements.

Use it to describe what to build and what level of completion counts as done. Codex should not implement work outside this file unless the user updates this file or explicitly requests a scoped change.

## 1. Project Name

Yuelu Traffic

## 2. One-Sentence Goal

Build a deployable Android-based crowdsourced traffic safety and incident reporting app for the Central South University and Lushan South Road area, helping users view and submit public traffic conditions, accidents, construction, congestion, road controls, and traffic management presence in a lawful, privacy-aware way.

## 3. Background

Students and nearby commuters around Central South University and Lushan South Road often encounter traffic congestion, road construction, temporary road controls, small collisions, and other abnormal road conditions. Existing information is scattered across chats, campus channels, and informal communication groups, making it hard to quickly understand current road conditions or follow up on minor accidents.

Yuelu Traffic aims to provide a community-based reporting platform where users can submit time- and location-based traffic information, view reports on a map, confirm or challenge report accuracy, and use an accident board to reconnect after minor incidents.

The app must be positioned as a traffic safety, road condition, and public traffic management information tool. It must not provide advice, routing, alerts, or product behavior intended to help users evade law enforcement, avoid penalties, bypass checkpoints, or violate traffic rules.

## 4. Target Users

The project is for:

- Primary users: students and daily commuters around Central South University and Lushan South Road.
- Secondary users: campus traffic volunteers, nearby residents, administrators, and users involved in minor traffic incidents.
- User environment: Android phone users traveling through the defined pilot area.
- User skill level: ordinary mobile users with no technical knowledge required.

## 5. Core Problem

The project solves:

- Users lack a centralized, real-time, map-based view of local traffic conditions.
- Minor accidents in the university area may be hard to handle immediately when students are rushing to class.
- Informal traffic reports from chats or campus communities are fragmented and difficult to verify.
- False or low-quality reports need to be filtered through reputation, voting, reporting, and admin moderation.

Why current alternatives are insufficient:

- General map apps may not capture campus-level incidents quickly enough.
- QQ groups or campus channels are fragmented, hard to search, and not structured around location and expiration.
- There is no unified accident follow-up board with privacy-aware contact exchange.
- Existing channels do not provide a built-in reputation mechanism for judging report quality.

## 6. Project Type

Select one or more:

- [x] Android app
- [ ] Web frontend
- [ ] Full-stack web app
- [x] Backend API service
- [ ] Python tool
- [ ] Desktop app
- [x] Database project
- [ ] Data analysis project
- [ ] Machine learning / NLP experiment
- [ ] Course assignment
- [ ] Research baseline
- [x] Small complete software engineering project
- [x] Other: Admin interface may be implemented as a simple web dashboard or backend-managed console if needed.

## 7. Functional Requirements

### P0 - Must Have

These requirements define the minimum deployable version.

1. Android users can enter a student number as a lightweight identifier.
   - The app must clearly state that the student number is used only to distinguish users in the app.
   - The app must not publicly display student numbers.
   - The app must avoid claiming formal school identity verification unless actual verification exists.

2. The app must provide a map-centered traffic report view for the pilot area.
   - Pilot area: Central South University and Lushan South Road.
   - Reports must be displayed as map markers.
   - Marker style, color, or severity must reflect report type and confidence level.

3. Users can submit traffic reports with:
   - Location
   - Time
   - Type
   - Optional description
   - Initial credibility value
   - Default expiration behavior based on report type

4. Supported report types in MVP:
   - Traffic management / public enforcement presence
   - Construction
   - Congestion
   - Road closure / traffic control
   - Accident or abnormal road condition

5. Report expiration rules:
   - Congestion reports default to expiring after 30 minutes.
   - Traffic management / public enforcement presence reports default to expiring after 6 hours.
   - Time-based expiration is secondary.
   - Primary expiration should be community feedback: when enough users indicate that a report is no longer valid, the report should be downgraded or removed from active display.

6. Users can vote on or evaluate reports.
   - Users can confirm a report is still valid.
   - Users can report that a report is inaccurate, expired, or malicious.
   - Feedback affects report confidence.

7. The app must include a user reputation system.
   - Higher-reputation users have greater report weight.
   - Lower-reputation users require more confirmations before their reports are treated as high-confidence.
   - Confirmed false or malicious reports reduce reputation.
   - Repeated malicious submissions can trigger temporary posting bans.

8. The MVP must include a points leaderboard and title system.
   - Users can gain points through useful reports and valid confirmations.
   - Leaderboard ranks users by points or reputation.
   - High-ranking users may receive level boosts or visible titles.
   - Public leaderboard display must avoid exposing sensitive personal identifiers.

9. The app must include an accident board.
   - Users can post minor accident entries with time, location, and description.
   - Users can browse accident entries.
   - Users involved in the same incident can request contact with each other.
   - Contact information must only become visible after both sides confirm.

10. The app must include privacy protection for accident handling.
    - Phone number, QQ, WeChat, or other contact details must not be public by default.
    - Contact exchange must require mutual confirmation.
    - The app should display clear privacy expectations before contact exchange.

11. The app must include administrator functionality.
    - Admins can review reported false information.
    - Admins can remove or hide invalid reports.
    - Admins can manage malicious users or temporary posting bans.
    - Admins can inspect accident board abuse reports.
    - Admins can adjust report status when necessary.

12. The project must be designed for real deployment.
    - It must include persistent backend storage.
    - It must include documented run and deployment steps.
    - It must include validation for core workflows.
    - It must not rely only on in-memory demo data for final completion.

13. Legal and safety boundary:
    - The app may show public traffic safety and traffic management information as community reports.
    - The app must not recommend evasion routes, avoidance tactics, illegal driving behavior, or ways to bypass law enforcement.
    - The app must not use wording such as "evade police" or "avoid enforcement" in user-facing UI.

### P1 - Should Have

These requirements are important but can be implemented after P0.

1. Report confidence visualization.
   - More confirmations and higher reputation should produce a stronger marker.
   - Conflicting feedback should reduce marker confidence.

2. Report detail page.
   - Shows type, approximate location, submit time, description, confidence, expiration status, and feedback actions.

3. User profile page.
   - Shows points, reputation level, title, recent contribution count, and active restrictions if any.

4. Posting restriction flow.
   - Users under temporary ban can view reports but cannot submit new reports.
   - The app should explain the ban reason and expiration time.

5. Admin moderation dashboard.
   - Can be Android-only or a simple web/admin interface.
   - Must support reviewing reports, user flags, and accident board posts.

6. Basic anti-abuse rules.
   - Limit repeated submissions from the same user in a short period.
   - Prevent one user from repeatedly voting on the same report.
   - Track suspicious behavior for admin review.

7. Location scope handling.
   - Warn or block submissions outside the pilot area.
   - Clearly show the supported service area.

### P2 - Nice to Have

These requirements are optional enhancements.

1. External data ingestion from QQ groups and campus channels.
   - This is allowed only if permission has been obtained from all required parties.
   - The system should import only permitted traffic-related data.
   - The system must avoid collecting private conversation content unrelated to traffic.
   - The MVP may simulate this source with manually imported sample data.

2. Image evidence and watermarking.
   - Users can attach images to reports.
   - Images may include a watermark containing time and approximate location.
   - Image upload must include privacy warnings.

3. Shake-to-match accident handling.
   - Users near the same location can trigger fast accident matching.
   - Both sides must confirm before contact information is exchanged.

4. Push notifications.
   - Notify users of high-confidence nearby road closures, accidents, or major congestion.
   - Notifications must not be designed to encourage evasion of enforcement.

5. More detailed analytics for admins.
   - Report volume by area and type.
   - False report rate.
   - Active user count.
   - Moderation workload.

## 8. Non-Goals

The first version must not include:

1. Any feature that instructs users how to evade traffic police, bypass law enforcement, avoid penalties, or violate traffic rules.
2. Automatic route planning around enforcement points.
3. Public exposure of student numbers, phone numbers, QQ, WeChat, or other private contact details.
4. Formal university identity verification unless a real authorized verification mechanism is implemented.
5. Unapproved scraping of QQ groups, campus channels, or private conversations.
6. Payment, monetization, advertising, or commercial ranking boosts.
7. Complex machine learning prediction of traffic events.
8. Large-scale citywide coverage outside Central South University and Lushan South Road during MVP.

## 9. Technical Constraints

Technical constraints:

- Runtime environment: Android client plus deployable backend service.
- Language or framework preference: TBD during implementation planning.
- Database: persistent database required; exact choice TBD.
- Network requirement: internet access required for backend sync and map data.
- UI requirement: Android map-based interface with report markers, report submission, accident board, leaderboard, and profile/admin flows.
- Deployment requirement: backend must be deployable to a real server or cloud environment.
- Hardware or device requirement: Android phone with location permission for map and nearby reporting.
- Operating system requirement: Android target version TBD.
- Package manager or build system requirement: TBD after stack selection.
- Performance requirement: map and nearby report list should load quickly for the pilot area; exact SLA TBD.
- Security or privacy requirement:
  - Do not publicly expose student numbers or contact information.
  - Store user identifiers securely.
  - Contact exchange requires mutual confirmation.
  - Admin access must be protected.
  - Avoid storing unnecessary private data.
  - External data ingestion must respect consent and platform rules.

## 10. Input and Output

Expected input:

- Student number entered by the user as a lightweight identifier.
- User-submitted traffic report: location, time, type, optional description.
- User feedback on reports: confirm valid, mark expired, report false/malicious.
- Accident board posts: time, location, optional description, contact exchange request.
- Admin moderation actions.
- Optional future external data imports from permitted QQ/campus sources.

Expected output:

- Map markers showing current traffic reports in the pilot area.
- Report details with confidence, time, type, description, and feedback controls.
- Accident board list and detail pages.
- Mutual-confirmation contact exchange result.
- User reputation, points, leaderboard, and title display.
- Admin moderation views and status updates.

Example input:

- User submits a congestion report near Lushan South Road at 08:10 with description "northbound slow traffic."
- Three users confirm it, one user marks it expired after traffic clears.
- User posts a minor accident entry near a campus gate at 12:20 and later matches with another involved user.

Example output:

- The map shows a congestion marker near Lushan South Road with medium confidence.
- After enough expired feedback, the marker is removed from active display.
- The accident board shows the incident entry, and contact information becomes visible only after both involved users confirm.

## 11. Data Requirements

Does this project need data storage?

- [ ] No
- [ ] Local file
- [ ] SQLite
- [ ] MySQL / PostgreSQL / SQL Server
- [x] Cloud database or deployable server database
- [ ] In-memory only
- [ ] Dataset files
- [ ] Other: TBD

Data schema or structure:

Core entities should include:

1. User
   - Internal user ID
   - Student number or hashed student-number-based identifier
   - Reputation score
   - Points
   - Level/title
   - Posting ban status and expiration
   - Created time

2. TrafficReport
   - Report ID
   - Type
   - Location coordinates
   - Human-readable location name if available
   - Submit time
   - Optional description
   - Submitter ID
   - Confidence score
   - Status: active, expired, hidden, removed, under review
   - Default expiration time
   - Created/updated time

3. ReportFeedback
   - Feedback ID
   - Report ID
   - User ID
   - Feedback type: confirm, expired, false, malicious
   - Weight based on user reputation
   - Created time

4. AccidentPost
   - Accident ID
   - Location
   - Time
   - Description
   - Created by user ID
   - Status: open, matched, closed, hidden
   - Created/updated time

5. ContactExchangeRequest
   - Request ID
   - Accident ID
   - Requesting user ID
   - Target/other user ID if known
   - Confirmation status for each side
   - Contact visibility status

6. AdminAction
   - Action ID
   - Admin user ID
   - Target type and target ID
   - Action type
   - Reason
   - Created time

Sample data availability:

- MVP may include seed data for reports, users, leaderboard, accident posts, and admin review examples.
- External QQ/campus data ingestion is P2 and may be simulated in MVP.

Data privacy or retention rules:

- Student numbers must not be publicly visible.
- Contact information must not be visible until mutual confirmation.
- Store only the minimum information required for app functionality.
- Report and accident data should support deletion or hiding by admins.
- External imported data must be permission-based and limited to traffic-related content.

## 12. External Dependencies

APIs, datasets, SDKs, services, models, devices, or accounts required:

1. Android build tooling.
2. Map SDK or map rendering provider suitable for China-area Android usage.
3. Backend hosting environment.
4. Persistent database.
5. Optional future QQ/campus data source with documented permission.
6. Optional push notification service.

Dependency constraints:

- Must be free or local-only: preferred for development and testing where possible.
- Requires credentials: map SDK, hosting, database, and push notification services may require credentials.
- Requires internet: yes, for map, backend sync, deployment, and external sources.
- Can be mocked for MVP: external QQ/campus data ingestion, push notifications, image upload, and shake-to-match can be mocked or deferred.

## 13. Acceptance Criteria

The project is acceptable when:

1. An Android user can enter a student number, see the privacy notice, and enter the app.
2. The app shows a map focused on Central South University and Lushan South Road.
3. A user can submit each MVP report type with location, time, type, and optional description.
4. Submitted reports appear as map markers with type-specific visual distinction.
5. Report confidence changes based on confirmations, expired feedback, and user reputation.
6. Congestion reports expire by default after 30 minutes unless community feedback keeps or removes them earlier.
7. Traffic management / public enforcement presence reports expire by default after 6 hours, while community feedback remains the primary validity control.
8. Users can report false or malicious information, and repeated abuse can reduce reputation or trigger posting bans.
9. Leaderboard and user title/level display work without exposing private identifiers.
10. Users can create and browse accident board posts.
11. Accident contact information is hidden until both sides confirm exchange.
12. Admins can review, hide/remove reports, handle abuse reports, and manage posting restrictions.
13. The backend persists users, reports, feedback, accident posts, and admin actions.
14. README explains how to run the Android app, backend, database, and validation commands.
15. Core validation commands pass, or any environment limitation is documented in `Documentation.md`.
16. The UI and documentation avoid language or behavior that encourages evading law enforcement.

## 14. Done When Checklist

The project is done when:

- [ ] All P0 requirements are implemented.
- [ ] Android app can run on a local emulator or physical Android device.
- [ ] Backend service can run locally and has deployment instructions.
- [ ] Persistent database is configured and documented.
- [ ] Map report workflow works end to end.
- [ ] Accident board workflow works end to end.
- [ ] Reputation, voting, leaderboard, and title system work for MVP scenarios.
- [ ] Admin moderation workflow works for MVP scenarios.
- [ ] Privacy constraints are implemented and documented.
- [ ] All required validation commands pass or documented manual checks are completed.
- [ ] The project can be run from `README.md` instructions.
- [ ] `Documentation.md` accurately reflects current status.
- [ ] Known limitations are documented.
- [ ] No critical TODO blocks remain in core paths.
- [ ] The final deliverable is generated or clearly described.
- [ ] The user can verify the main workflow without relying on hidden assumptions.
