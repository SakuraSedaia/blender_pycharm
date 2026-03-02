# Junie Coding Guidelines

## Development Standards

### IntelliJ Platform SDK Practices
- **API Versioning**: Always use the most recent, non-deprecated APIs. Check for `@Deprecated` annotations and follow Javadoc suggestions.
- **Logging**: Use `com.intellij.openapi.diagnostic.Logger` for diagnostic logging instead of `println`.
- **Services**: Use the `@Service` annotation for project-level or application-level services. Avoid manual singleton patterns.
- **Process Execution**: Use `com.intellij.execution.configurations.GeneralCommandLine` for launching external processes.
- **File I/O**: Prefer `java.nio.file.Path` and IntelliJ's `VfsUtil`, `FileUtil`, or `NioPathUtil` over `java.io.File`.
- **Network**: Use `com.intellij.util.io.HttpRequests` for downloading files or making network requests.
- **UI Components**: Use IntelliJ-specific components like `JBLabel`, `JBTextField`, `JBCheckBox`, and `FormBuilder` for a consistent UI.
- **Progress**: Wrap long-running operations in `ProgressManager.getInstance().runProcessWithProgressSynchronously` or `runModal`.

### Clean & Readable Code Practices
- **Single Responsibility Principle (SRP)**: Each class/function should have one clear purpose. Decompose large classes (e.g., `BlenderService`) if they handle multiple distinct responsibilities.
- **Descriptive Naming**: Use names that clearly state intent; avoid cryptic abbreviations.
- **Function Size**: Aim for small, focused functions. Extract sub-functions if logic becomes complex.
- **DRY (Don't Repeat Yourself)**: Extract common logic into helper methods or utility classes.
- **Constant Extraction**: Avoid hardcoded strings or numbers. Extract them into meaningful constants.
- **Icon Management**: All icons must be declared in `BlenderIcons.kt` and referenced as `BlenderIcons.Icon`.
  - Custom icons: `@JvmField val IconName: Icon = IconLoader.getIcon("/images/icon_name.svg", BlenderIcons::class.java)`
  - JetBrains icons: `@JvmField val IconName: Icon = AllIcons.Icon.IconName`
- **Comments**: Write self-documenting code. Use comments only to explain "why" if it's not obvious from "what" and "how".

## AI Agent Workflow & Documentation

### Session & Environment Management
- **Initial Context**: At session start, review `.agent/` files (`project.md`, `context.md`, `guidelines.md`) and specialized skills in `.agent/skills/` to align with current architecture and standards.
- **Logging**: Maintain local-only chat session logs in `.ai-logs/`, organized by date. These are for personal reference and MUST NOT be committed to the repository.
- **Summaries**: When asked for a "context summary", write `summary_YYYY-MM-DD.md` in `.ai-logs/` with highlights.
- **Versioning**: NEVER bump the plugin version (e.g., in `build.gradle.kts`) unless explicitly instructed by the User.
- **Resource Management**: Download external assets locally (avoid CDNs) for reliability and offline availability.
- **SSH/Passphrase Handling**: If a process (e.g., Git) requires a passphrase, use the `ask_user` tool to request it.

### Documentation & Wiki Maintenance
- **Internal Docs**: Keep `.agent/project.md`, `README.md`, and `CONTRIBUTING.md` updated as the project evolves.
- **Rule Placement**: Place generic rules/guidelines in `guidelines.md`; project-specific data in `project.md`; and language/Kotlin/Python context in `context.md`.
- **Guideline Sync**: When updating internal guidelines that affect development standards, also update `CONTRIBUTING.md`.
- **External Wiki**: `wiki_guidelines.md` is strictly for the external Sphinx/RST wiki (`PycharmBlenderWiki`) and has NO effect on this project's code or internal documentation.
  - Refer to `.agent/wiki_guidelines.md` for Sphinx and reStructuredText (RST) standards.
  - Editing the external wiki's source code is allowed only when authorized.

## Agent Skills
Detailed procedural skills are maintained in `.agent/skills/`:
- **Git Management**: `.agent/skills/git_management.md`
- **Documentation**: `.agent/skills/documentation.md`

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
