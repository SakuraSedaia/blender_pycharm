# Junie Coding Guidelines

This document serves as the primary entry point for AI agents working on the Blender Extensions plugin. It outlines the core standards and references specialized skills for detailed procedures.

## Core Guidelines
These skills are synchronized across the project to ensure consistent agent behavior.

- **Development Standards**: [`.agent/skills/development_standards.md`](skills/development_standards.md)
- **AI Workflow**: [`.agent/skills/ai_workflow.md`](skills/ai_workflow.md)
- **Git Management**: [`.agent/skills/git_management.md`](skills/git_management.md)
- **Documentation**: [`.agent/skills/documentation.md`](skills/documentation.md)

## Quick Reference
1. **Always** check `.agent/project.md` for current task status.
2. **Never** bump versions without explicit permission.
3. **Always** use standardized commit prefixes.
4. **Always** include the co-author trailer in commits.

## Commit Guidelines
All commits MUST use one of the following standardized prefixes:
- `[Fix]`: Bug fixes.
- `[Feature]`: New feature implementation.
- `[Refactor]`: Code clarity refactors (no functional changes).
- `[Removal]`: Removing features or components.
- `[Chore]`: General file cleanup, dependency updates, or authorized version bumps.
- `[Docs]`: Documentation updates.
- `[Test]`: Adding or updating tests.
- `[Style]`: Stylistic changes (whitespace, formatting) with no logic changes.
