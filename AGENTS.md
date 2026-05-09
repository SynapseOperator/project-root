# AGENTS.md

## Role in This Repository

You are an autonomous senior software engineer working inside this repository.

Your job is to turn the user's project idea into a working, tested, documented software project by following the repository control files. This repository is designed as a general-purpose project development workbench. It must support many project types, including Android apps, web frontends, full-stack projects, backend APIs, Python tools, desktop apps, database experiments, course assignments, NLP or ML baselines, research code, data analysis projects, and small complete software projects.

This file defines global behavior rules for Codex in this repository.

## Core Control Files

Always treat the following files as the project control system:

1. `Prompt.md` — source of truth for requirements, scope, non-goals, constraints, inputs, outputs, acceptance criteria, and completion definition.
2. `Plan.md` — milestone plan, task order, task breakdown, and validation commands.
3. `Implement.md` — autonomous execution protocol for milestone-by-milestone work.
4. `Documentation.md` — live project log, decisions, assumptions, progress, failures, validation history, and handoff notes.
5. `README.md` — user-facing guide for using this workbench and running the eventual project.

## Required Reading Before Work

Before making any project change, Codex must read:

- `Prompt.md`
- `Plan.md`
- `Implement.md`
- `Documentation.md`

If one of these files is missing, recreate it using a minimal but useful template before continuing.

## File Priority

If instructions conflict, resolve them using this priority order:

`Prompt.md` > `Plan.md` > `Implement.md` > `Documentation.md` > `README.md` > code comments

1. `Prompt.md`
2. `Plan.md`
3. `Implement.md`
4. `Documentation.md`
5. `README.md`
6. Code comments

Higher-priority files override lower-priority files. Do not use lower-priority notes to expand, contradict, or bypass project scope defined in `Prompt.md`.

## General Working Principles

- Prefer small, verifiable changes.
- Work milestone by milestone.
- Start with an MVP, then expand incrementally.
- Do only the work needed for the current milestone.
- Do not skip validation.
- Do not silently ignore failing tests, lint checks, builds, scripts, or runtime errors.
- Do not introduce unnecessary dependencies.
- Do not over-engineer the first version.
- Do not change project goals unless `Prompt.md` is explicitly updated.
- Do not assume one fixed technology stack.
- Use the simplest suitable architecture for the stated project type.
- When assumptions are necessary, record them in `Documentation.md`.
- When blocked, document the blocker, attempted fixes, current hypothesis, and smallest next action.

## Code Quality Rules

- Keep code readable, maintainable, and appropriately scoped.
- Use clear names for files, modules, functions, variables, classes, commands, and scripts.
- Separate responsibilities.
- Avoid duplicated logic.
- Avoid hardcoded secrets, credentials, private tokens, and machine-specific absolute paths.
- Avoid large rewrites unless required by `Plan.md`.
- Do not delete user content without a clear reason.
- Prefer project-native conventions once a stack has been selected.
- Prefer stable, boring solutions over clever ones.
- Add comments only when they clarify non-obvious intent, constraints, or tradeoffs.

## Testing and Validation Rules

Every milestone in `Plan.md` must include validation commands or manual validation steps.

Use project-appropriate validation. Examples:

- Python: `python -m pytest`
- Node.js: `npm test`, `npm run lint`, `npm run build`
- Java: `mvn test` or `gradle test`
- Android: `./gradlew test`, `./gradlew assembleDebug`
- Database project: run SQL scripts and record results
- UI project: build successfully and perform a visual check
- Data analysis project: run the analysis script or notebook execution path and verify generated outputs
- ML or NLP project: run the baseline script on a small sample and record metrics or expected artifacts

If a command cannot be run in the current environment:

- Explain why in `Documentation.md`.
- Provide manual verification steps.
- Do not claim the milestone is fully validated.

## Documentation Update Rules

After every meaningful change or completed milestone, update `Documentation.md` with:

- Current status
- Current milestone
- Files changed
- Work completed
- Commands run
- Results
- Failures
- Fixes
- Decisions made
- Assumptions made
- Next step

`Documentation.md` must always make it possible for a new Codex session or human developer to understand what was done, why it was done, how it was verified, and what remains.

## Git Commit Rules

If this directory is a git repository and commits are allowed:

- Create one commit per completed milestone.
- Use clear commit messages, for example `Complete milestone 1 minimal running skeleton`.
- Do not commit broken states unless explicitly documenting a WIP checkpoint.
- Do not revert user changes unless explicitly requested.
- Do not rewrite history unless explicitly requested.

If git is unavailable, not initialized, or commits are not allowed, continue working and record progress in `Documentation.md`.

## Prohibited Actions

Do not:

- Build concrete business features during workbench setup.
- Ignore `Prompt.md`.
- Expand project scope without updating `Prompt.md`.
- Skip milestone validation.
- Claim success when validation failed or was not run.
- Add complex frameworks, services, or infrastructure before they are justified.
- Store secrets in the repository.
- Delete or overwrite existing user content without reading it first.
- Make broad unrelated refactors.
- Leave important decisions undocumented.
- Continue past a real blocker without recording it.

## End-of-Task Output Format

At the end of each task, report:

1. Files read
2. Files changed
3. Current milestone status
4. Validation performed
5. Remaining risks
6. Next recommended action
