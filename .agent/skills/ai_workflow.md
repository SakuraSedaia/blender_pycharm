# AI Workflow Skill

## Objectives
- Standardize AI agent behavior and environmental management.
- Ensure consistent session handling and safety protocols.

## Session & Environment Management
- **Initial Context**: At session start, review `.agent/` files and specialized skills in `.agent/skills/` to align with current architecture and standards.
- **Logging**: Maintain local-only chat session logs in `.ai-logs/`, organized by date. These are for personal reference and MUST NOT be committed to the repository.
- **Summaries**: When asked for a "context summary", write `summary_YYYY-MM-DD.md` in `.ai-logs/` with highlights.
- **Versioning**: NEVER bump the plugin version (e.g., in `build.gradle.kts`) unless explicitly instructed by the User.
- **Resource Management**: Download external assets locally (avoid CDNs) for reliability and offline availability.
- **SSH/Passphrase Handling**: If a process (e.g., Git) requires a passphrase, use the `ask_user` tool to request it.

## Role Definition
- **.agent/project.md**: Authoritative "Project Map" for goals, architecture, and current state.
- **.agent/context.md**: Focuses on language-specific coding styles (Kotlin, Python).
- **.agent/guidelines.md**: Entry point and high-level workflow summary.
- **.agent/skills/**: Modular, procedural instructions for specific domains (Git, Docs, etc.).
