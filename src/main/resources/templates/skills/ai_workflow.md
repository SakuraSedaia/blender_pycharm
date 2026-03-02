# AI Workflow Skill

## Objectives
- Maximize efficiency during AI-assisted development sessions.
- Maintain a structured project state across multiple sessions.

## Project Structure (Core Files)
- **`.agent/project.md`**: Authoritative "Project Map" for goals, architecture, and current task status.
- **`.agent/context.md`**: Project-specific context, including language-specific styling (Kotlin, Python).
- **`.agent/guidelines.md`**: Entry point and high-level workflow summary.
- **`.agent/skills/`**: Modular, procedural instructions for specific domains.

## Workflow Procedures
1. **Context Initialization**: Review `.agent/` files and specialized skills at the beginning of every session.
2. **Update Status**: Keep `.agent/project.md` updated with progress, discoveries, and revised plans.
3. **Atomic Changes**: Implement minimal, focused changes. Avoid broad sweeps unless authorized.
4. **Validation**: Run all relevant tests or validation scripts (e.g., `blender --command extension validate`) before submission.
5. **No Redundancy**: If a skill exists, follow it; do not reinvent or duplicate logic.
