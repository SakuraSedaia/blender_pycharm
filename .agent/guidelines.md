# Junie Coding Guidelines

This document serves as the primary entry point for AI agents working on the Blender Extensions plugin. It outlines the core standards and references specialized skills for detailed procedures.

## Core Guidelines
These skills are synchronized across the project to ensure consistent agent behavior.

- **Junie Agent Guidelines**: [`.agent/junie_instructions.md`](junie_instructions.md) (PRIMARY)
- **Development Standards**: [`.agent/skills/development_standards.md`](skills/development_standards.md)
- **AI Workflow**: [`.agent/skills/ai_workflow.md`](skills/ai_workflow.md)
- **Git Management**: [`.agent/skills/git_management.md`](skills/git_management.md)
- **Documentation**: [`.agent/skills/documentation.md`](skills/documentation.md)

## Quick Reference
1. **Always** check `.agent/project.md` for current task status.
2. **Never** bump versions without explicit permission.
3. **Always** use standardized capitalized commit prefixes (`Type(scope):`).
4. **Always** include the co-author trailer in commits.
5. **Always** use the Windows scratch directory: `C:\Users\Sakura\AppData\Roaming\JetBrains\IntelliJIdea2025.3\scratches\`.
6. **Never** automatically resolve or implement `TODO:` comments unless specifically asked.
7. **Immediately delete** any new run configuration made for testing purposes. Only the 5 standard configurations should remain: `buildPlugin`, `runIde`, `runAllTests`, `runUnitTests`, and `runIntegrationTests`.

## Commit Guidelines
All commits MUST use the following standardized capitalized prefixes in `Type(scope): Description` format:
- `Feat`: New feature implementation.
- `Fix`: Bug fixes.
- `Docs`: Documentation updates.
- `Style`: Stylistic changes (whitespace, formatting) with no logic changes.
- `Refactor`: Code clarity refactors (no functional changes).
- `Test`: Adding or updating tests.
- `Chore`: General file cleanup, dependency updates, or authorized version bumps.
- `I18n`: Internationalization updates.
- `Removal`: Removing features or components.
