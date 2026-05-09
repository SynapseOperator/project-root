# Plan.md

This file tells Codex what order to work in and how each stage should be validated.

The plan is intentionally technology-neutral. After `Prompt.md` is filled in, Codex should adapt validation commands to the selected project type while preserving the milestone order unless the user explicitly changes it.

## 1. Project Strategy

Build the project incrementally.

Default strategy:

1. Clarify requirements from `Prompt.md`.
2. Select the minimal suitable technology stack.
3. Initialize the project structure.
4. Implement the smallest working version.
5. Add P0 features one by one.
6. Validate after each milestone.
7. Refactor only after functionality is stable.
8. Prepare final documentation and deliverables.

Default priorities:

- MVP first.
- P0 before P1.
- P1 before P2.
- Validation before expansion.
- Documentation after every meaningful change.

## 2. Validation Command Examples

Use commands appropriate for the selected stack. Examples:

```bash
# Python
python --version
python -m pytest
python main.py

# Node.js
npm test
npm run lint
npm run build
npm run dev

# Java
mvn test
gradle test

# Android
./gradlew test
./gradlew assembleDebug

# Database project
# Run the SQL scripts with the selected database client and record results.

# UI project
# Build successfully, launch locally if possible, and perform a visual check.

# Data analysis project
# Run the analysis script or notebook execution path and verify generated outputs.

# ML / NLP project
# Run the baseline or training script on a small sample and record metrics or artifacts.
```

If a command does not apply, replace it with a concrete validation method in `Documentation.md`.

## 3. Milestones

---

## Milestone 0 — Project Understanding and Setup

### Goal

Understand `Prompt.md`, identify project type, choose a minimal technical direction, and prepare the repository for implementation.

### Tasks

- [ ] Read `Prompt.md`.
- [ ] Identify the project type and MVP scope.
- [ ] Select the minimal technology stack.
- [ ] Check existing files and avoid overwriting user work.
- [ ] Create or update the initial project structure.
- [ ] Record decisions and assumptions in `Documentation.md`.

### Validation

Commands or checks:

```bash
# No code validation required yet.
# Confirm that Prompt.md is filled enough to start implementation.
# Confirm that Documentation.md records the selected stack and next milestone.
```

### Done When

- [ ] Project type is clear.
- [ ] MVP scope is clear.
- [ ] Technology stack is recorded.
- [ ] Initial folder structure exists or is intentionally deferred.
- [ ] `Documentation.md` is updated.

---

## Milestone 1 — Minimal Running Skeleton

### Goal

Create the smallest runnable version of the project.

### Tasks

- [ ] Initialize the codebase using the selected stack.
- [ ] Add the minimal entry point.
- [ ] Add dependency configuration only if needed.
- [ ] Add basic run instructions.
- [ ] Add the simplest possible validation path.
- [ ] Update `Documentation.md`.

### Validation

Use project-appropriate commands, for example:

```bash
# Python
python --version
python main.py
python -m pytest

# Node.js
npm install
npm run dev
npm test

# Java
mvn test
gradle test

# Android
./gradlew test
./gradlew assembleDebug

# Database
# Run the initial SQL script and record the output.

# UI
# Build or launch the app and perform a visual check.
```

### Done When

- [ ] Project runs locally or has a documented manual execution path.
- [ ] Basic validation succeeds or limitations are documented.
- [ ] `README.md` contains initial run instructions for the selected stack.
- [ ] `Documentation.md` records validation results.

---

## Milestone 2 — Core P0 Feature 1

### Goal

Implement the first core P0 feature from `Prompt.md`.

### Tasks

- [ ] Identify the exact P0 requirement to implement.
- [ ] Identify required files and interfaces.
- [ ] Implement the smallest complete version of the feature.
- [ ] Add tests or manual verification.
- [ ] Update `Documentation.md`.

### Validation

Use project-specific commands after stack selection, for example:

```bash
python -m pytest
npm test
npm run build
mvn test
gradle test
./gradlew test
./gradlew assembleDebug
```

For database, UI, data analysis, or ML projects, run the smallest meaningful workflow and record the result.

### Done When

- [ ] Feature works for its intended P0 scenario.
- [ ] Validation passes or limitations are documented.
- [ ] No unrelated changes are introduced.
- [ ] `Documentation.md` is updated.

---

## Milestone 3 — Core P0 Feature 2

### Goal

Implement the second core P0 feature from `Prompt.md`.

### Tasks

- [ ] Identify the exact P0 requirement to implement.
- [ ] Integrate with existing code.
- [ ] Add tests or manual verification.
- [ ] Update user-facing instructions if needed.
- [ ] Update `Documentation.md`.

### Validation

Use the same validation family selected for the project, for example:

```bash
python -m pytest
npm test
npm run lint
npm run build
mvn test
gradle test
./gradlew test
./gradlew assembleDebug
```

### Done When

- [ ] Feature works.
- [ ] Feature integrates with previous milestone work.
- [ ] Validation passes or limitation is documented.
- [ ] `Documentation.md` is updated.

---

## Milestone 4 — Core P0 Feature 3

### Goal

Implement the third core P0 feature from `Prompt.md`.

### Tasks

- [ ] Identify the exact P0 requirement to implement.
- [ ] Implement feature behavior.
- [ ] Integrate with the existing workflow.
- [ ] Add tests or manual verification.
- [ ] Update `Documentation.md`.

### Validation

Use project-specific commands, for example:

```bash
python -m pytest
npm test
npm run build
mvn test
gradle test
./gradlew test
./gradlew assembleDebug
```

If fewer than three P0 features exist, mark this milestone as not applicable in `Documentation.md`.

### Done When

- [ ] Feature works, or the milestone is explicitly marked not applicable.
- [ ] Validation passes or limitation is documented.
- [ ] `Documentation.md` is updated.

---

## Milestone 5 — Integration and Error Handling

### Goal

Make the implemented features work together reliably.

### Tasks

- [ ] Test the complete main workflow.
- [ ] Add basic error handling for expected failure cases.
- [ ] Fix integration bugs.
- [ ] Remove obvious dead code.
- [ ] Confirm important edge cases from `Prompt.md`.
- [ ] Update `Documentation.md`.

### Validation

Use project-specific test, build, lint, type-check, script, or manual workflow commands, for example:

```bash
python -m pytest
npm test
npm run lint
npm run build
mvn test
gradle test
./gradlew test
./gradlew assembleDebug
```

For UI projects, also perform a visual check of the main workflow. For database projects, run integration SQL scripts and record outputs.

### Done When

- [ ] Main workflow succeeds.
- [ ] Common failure cases are handled.
- [ ] Validation passes or limitations are documented.
- [ ] `Documentation.md` is updated.

---

## Milestone 6 — Tests and Quality Check

### Goal

Improve confidence in correctness and maintainability.

### Tasks

- [ ] Add or improve tests around P0 behavior.
- [ ] Run the test suite.
- [ ] Run lint, build, type check, or formatter checks if available.
- [ ] Fix failures.
- [ ] Record remaining known issues.
- [ ] Update `Documentation.md`.

### Validation

Use the strongest practical validation set for the selected stack, for example:

```bash
python -m pytest
npm test
npm run lint
npm run build
mvn test
gradle test
./gradlew test
./gradlew assembleDebug
```

Manual checks are acceptable only when automated checks are not practical. Record manual steps and results.

### Done When

- [ ] Tests pass or documented manual checks are complete.
- [ ] Build passes if the project has a build step.
- [ ] Known issues are documented.
- [ ] `Documentation.md` is updated.

---

## Milestone 7 — Final Documentation and Delivery

### Goal

Prepare the project for handoff.

### Tasks

- [ ] Update `README.md`.
- [ ] Update `Documentation.md`.
- [ ] Add usage examples.
- [ ] Record limitations.
- [ ] Record future improvements.
- [ ] Confirm all P0 acceptance criteria.
- [ ] Confirm final deliverable location or run path.

### Validation

Re-run final validation commands for the selected stack, for example:

```bash
python -m pytest
npm test
npm run lint
npm run build
mvn test
gradle test
./gradlew test
./gradlew assembleDebug
```

Also verify that `README.md` instructions are accurate.

### Done When

- [ ] User can run the project from `README.md`.
- [ ] All P0 requirements are complete or explicitly deferred by user decision.
- [ ] Final validation is recorded.
- [ ] Final limitations are documented.
- [ ] `Documentation.md` includes final handoff notes.

