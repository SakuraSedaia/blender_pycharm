# Coding Guidelines

We follow standard practices for IntelliJ Platform and Kotlin development.

## IntelliJ Platform SDK Practices

- **API Versioning**: Always use the most recent, non-deprecated APIs. Check for `@Deprecated` annotations and follow suggestions in their Javadoc.
- **Logging**: Use `com.intellij.openapi.diagnostic.Logger` for platform-level logging.
- **Services**: Use the `@Service` annotation for project-level or application-level services. Avoid manual singleton patterns.
- **Process Execution**: Use `com.intellij.execution.configurations.GeneralCommandLine` for launching external processes like Blender.
- **File I/O**: Prefer `java.nio.file.Path` and IntelliJ's `VfsUtil`, `FileUtil`, or `NioPathUtil` over `java.io.File`.
- **UI Components**: Use IntelliJ-specific components like `JBLabel`, `JBTable`, `JBScrollPane`, and `FormBuilder` for consistent UI.
- **Progress**: Long-running operations should be wrapped in `ProgressManager.getInstance().runProcessWithProgressSynchronously` or similar tools to provide feedback and allow cancellation.

## Kotlin Styling Practices

- **Standard Conventions**: Follow the official [Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html).
- **Immutability**: Use `val` by default. Only use `var` when mutability is strictly required.
- **Single-Expression Functions**: Use expression bodies (`fun foo() = ...`) for simple functions.
- **Null Safety**: Leverage Kotlin's null safety effectively; avoid `!!` where possible, prefer `?:` (elvis operator) or safe calls.
- **Naming**:
  - `PascalCase` for classes, interfaces, and objects.
  - `camelCase` for functions, variables, and properties.
  - `UPPER_SNAKE_CASE` for constants.
- **String Templates**: Prefer string templates (`"$foo"`) over concatenation.

## Python Styling Practices

- **PEP 8**: Follow PEP 8 standards for any Python scripts (like those injected into Blender).
- **Nesting**: Keep nesting minimal (<= 4 levels of indentation).
- **Standard Library**: Prefer using the Python standard library whenever possible.

## Clean Code Practices

- **Single Responsibility Principle (SRP)**: Each class and function should have one clear purpose.
- **Descriptive Naming**: Use names that clearly state the intent of the variable, function, or class.
- **Function Size**: Aim for small, focused functions.
- **DRY (Don't Repeat Yourself)**: Extract common logic into helper methods or utility classes.
- **Self-Documenting Code**: Write code that is easy to understand. Use comments only to explain "why" something is done if it's not obvious.
