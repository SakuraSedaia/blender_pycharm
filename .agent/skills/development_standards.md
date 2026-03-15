# Development Standards Skill

## Objectives
- Maintain high code quality and consistency across the project.
- Ensure proper use of IntelliJ Platform SDK and language-specific best practices.

## IntelliJ Platform SDK Practices
- **API Versioning**: Always use the most recent, non-deprecated APIs. Check for `@Deprecated` annotations and follow Javadoc suggestions.
- **Logging**: Use `com.intellij.openapi.diagnostic.Logger` for diagnostic logging instead of `println`.
- **Services**: Use the `@Service` annotation for project-level or application-level services. Avoid manual singleton patterns.
- **Process Execution**: Use `com.intellij.execution.configurations.GeneralCommandLine` for launching external processes.
- **File I/O**: Prefer `java.nio.file.Path` and IntelliJ's `VfsUtil`, `FileUtil`, or `NioPathUtil` over `java.io.File`.
- **Network**: Use `com.intellij.util.io.HttpRequests` for downloading files or making network requests.
- **UI Components**: Use IntelliJ-specific components like `JBLabel`, `JBTextField`, `JBCheckBox`, and `FormBuilder` for a consistent UI.
- **Progress**: Wrap long-running operations in `ProgressManager.getInstance().runProcessWithProgressSynchronously` or `runModal`.

## Clean & Readable Code Practices
- **Single Responsibility Principle (SRP)**: Each class/function should have one clear purpose. Decompose large classes (e.g., `BlenderService`) if they handle multiple distinct responsibilities.
- **Descriptive Naming**: Use names that clearly state intent; avoid cryptic abbreviations.
- **Function Size**: Aim for small, focused functions. Extract sub-functions if logic becomes complex.
- **DRY (Don't Repeat Yourself)**: Extract common logic into helper methods or utility classes.
- **Constant Extraction**: Avoid hardcoded strings or numbers. Extract them into meaningful constants.
- **Icon Management**: All icons must be declared in `BlenderIcons.kt` and referenced as `BlenderIcons.Icon`.
  - Custom icons: `@JvmField val IconName: Icon = IconLoader.getIcon("/images/icon_name.svg", BlenderIcons::class.java)`
  - JetBrains icons: `@JvmField val IconName: Icon = AllIcons.Icon.IconName`
- **Internationalization (i18n)**: All user-facing UI strings and log messages MUST have an associated i18n mapping.
  - Use `LangManager.message("key")` for localized strings.
  - English mappings are stored in: `src/main/resources/messages/LangManager.properties`
- **Comments**: Write self-documenting code. Use comments only to explain "why" if it's not obvious from "what" and "how".
- **TODO: Comments**: Do NOT automatically resolve or implement `TODO:` comments unless explicitly requested by the user.
- **Scratch & Temp Files**: **Always** use the IDE scratch directory for scratch, temp, or logging files: `/home/sakura/.config/JetBrains/IntelliJIdea2025.3/scratches/`.
- **Run Configurations**: **Immediately delete** any new run configuration made for testing purposes. The only run configurations that should exist are: `buildPlugin`, `runIde`, `runAllTests`, `runUnitTests`, and `runIntegrationTests`.
