# Contributing to Blender Dev Tools for PyCharm

Thank you for your interest in contributing to this project! We welcome all contributions, from bug reports and feature requests to code changes and documentation improvements.

## Getting Started

### Prerequisites

- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Community or Ultimate)
- [JDK 21](https://adoptium.net/temurin/releases/?version=21) or later
- [Blender](https://www.blender.org/) 4.2 or later (supporting extensions)

### Setup

1. **Fork the repository** on GitHub.
2. **Clone your fork** locally.
3. **Open the project** in IntelliJ IDEA.
4. **Set up the SDK**: Ensure you have JDK 21+ installed and configured. IntelliJ should automatically pick up the Gradle configuration.

## Development Workflow

### 1. Create a Branch
Create a new branch for your work:
```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/your-bug-fix
```

### 2. Make Changes
Implement your changes in the `src/main/kotlin` directory. Follow the existing project structure and coding style.

### 3. Test Your Changes
Run the plugin in a sandbox environment to verify your changes:
```bash
./gradlew runIde
```
This project includes both unit tests and headless integration tests that validate the communication cycle with Blender. Run all tests with:
```bash
./gradlew test
```
The integration tests use a headless Blender environment to verify the TCP heartbeat and reload logic.
To build the distribution plugin ZIP file, run:
```bash
./gradlew build
```
The built plugin will be available in `build/distributions/`.

### 4. Commit and Push
Commit your changes with descriptive messages:
```bash
git add .
git commit -m "Add feature: detailed description"
git push origin feature/your-feature-name
```

### 5. Open a Pull Request
Submit a Pull Request on GitHub. Provide a clear description of the changes and any related issues.

## Code Style

- Use **Kotlin** for all plugin logic.
- Follow the official [Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html).
- Keep the UI consistent with IntelliJ Platform guidelines.
- Use Swing and `FormBuilder` for configuration UIs.
- Icon Management: Declare all icons in `BlenderIcons.kt` and reference them as `BlenderIcons.Icon` across the codebase.
  - Custom icon example: `@JvmField val BlenderColor: Icon = IconLoader.getIcon("/images/blender_color.svg", BlenderIcons::class.java)`
  - JetBrains icon example: `@JvmField val Install: Icon = AllIcons.Actions.Install`

## Documentation References

When referencing Blender documentation, please refer to the `.blender-docs` directory in the project root instead of online documentation whenever possible. This contains a local copy of the Blender Python and Manual docs for offline and consistent reference.

## Repository Reference

The [Jacques Lucke VS-Code extension](https://github.com/JacquesLucke/blender_vscode.git) is the main basis for this project. A clone of this repository is maintained in the `.reference` directory for architectural and feature parity reference.

## AI-Assisted Development

This project uses AI agents (like Junie) to maintain high-quality code and documentation. When contributing using an AI agent:

- **Local Logging**: Chat sessions may be logged locally in the `.ai-logs/` folder for personal reference. These logs should NOT be committed to the repository.
- **Versioning**: NEVER bump the plugin version (e.g., in `build.gradle.kts`) unless explicitly told by the maintainers.
- **Context**: Keep `.junie/project.md`, `README.md`, and `CONTRIBUTING.md` updated throughout all project changes. Also, keep `.junie/context.md` (language context) and `.junie/guidelines.md` (general rules) updated as standards evolve.
- **Wiki**: The external wiki is what the `wiki_guidelines.md` is for, which has information and context for editing said wiki. When updating the `PycharmBlenderWiki` project, follow the Sphinx/RST practices defined in `.junie/wiki_guidelines.md`. **Editing the wiki's source code is allowed when authorized.**
- **Authentication**: If a Git operation requires an SSH passphrase, provide it to the agent via chat if it asks for it.

## Reporting Issues

If you find a bug or have a feature request, please [open an issue](https://github.com/Sakura-Sedaia/BlenderExtensions/issues) with:
- A clear title and description.
- Steps to reproduce the bug.
- Your OS, PyCharm version, and Blender version.
- Any relevant logs or screenshots.

## Communication

For questions or discussions, feel free to use GitHub Discussions or open an issue.

---
By contributing, you agree that your contributions will be licensed under the project's license.
