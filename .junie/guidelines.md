# Junie Coding Guidelines

## IntelliJ Platform SDK Practices
- API Versioning: Always use the most recent, non-deprecated APIs. Check for `@Deprecated` annotations and follow suggestions in their Javadoc.
- Logging: Use `com.intellij.openapi.diagnostic.Logger` for plugin logging instead of `println` or custom file logging for general diagnostic purposes.
- Services: Use the `@Service` annotation for project-level or application-level services. Avoid manual singleton patterns.
- Process Execution: Use `com.intellij.execution.configurations.GeneralCommandLine` for launching external processes.
- File I/O: Prefer `java.nio.file.Path` and IntelliJ's `VfsUtil`, `FileUtil`, or `NioPathUtil` over `java.io.File` when interacting with the IDE's virtual file system or for better platform abstraction.
- Network: Use `com.intellij.util.io.HttpRequests` for downloading files or making network requests.
- UI Components: Use IntelliJ-specific components like `JBLabel`, `JBTextField`, `JBCheckBox`, and `FormBuilder` for consistent UI.
- Progress: Long-running operations should be wrapped in `ProgressManager.getInstance().runProcessWithProgressSynchronously` or `runModal` to provide feedback and allow cancellation.

## Clean & Readable Code Practices
- Single Responsibility Principle (SRP): Each class and function should have one clear purpose. Decompose large classes (like `BlenderService`) if they handle multiple distinct responsibilities (e.g., downloading, process management, script generation).
- Descriptive Naming: Use names that clearly state the intent of the variable, function, or class. Avoid cryptic abbreviations.
- Function Size: Aim for small, focused functions. If a function is too long, extract sub-functions.
- DRY (Don't Repeat Yourself): Extract common logic into helper methods or utility classes.
- Constant Extraction: Avoid hardcoded strings or numbers. Extract them into meaningful constants.
- Comments: Write code that is self-documenting. Use comments only to explain "why" something is done if it's not obvious from the "what" and "how".

## AI Agent Interaction & Workflow
- AI Agent Interaction: At session start, review `.junie/` files (`project.md`, `context.md`, `guidelines.md`) to align with current architecture, language standards, and rules.
- Logging & Summaries:
  - Maintain logs of all chat sessions in `.ai-logs/`, organized by date.
  - When asked for a "context summary" or "summary of the day/session", write `summary_YYYY-MM-DD.md` in `.ai-logs/` with highlights.
  - When adding rules to `.junie/`, place them in the correct file (generic rules and guidelines here; project-specific data in `project.md`; generic kotlin and python context in `context.md`).
  - Guideline Sync: When updating internal guidelines here that affect development style/structure/standards, also update the public `CONTRIBUTING.md` accordingly.
- Documentation Maintenance: Keep `.junie/project.md`, `README.md`, and `CONTRIBUTING.md` updated throughout the changes this project undergoes.
- Development Workflow:
  - Session Logging: Every chat session MUST be logged in `.ai-logs/` unless the user explicitly says "No Log".
  - Commits: Upon successful completion of a task, commit the changes to Git with brief messages (<= 2 sentences).
  - Resource Management: Download external assets locally (avoid CDNs) for reliability and offline availability.
