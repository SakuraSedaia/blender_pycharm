# Contributing to Blender Extensions for PyCharm

Thank you for your interest in contributing to this project! We welcome all contributions, from bug reports and feature requests to code changes and documentation improvements.

## Getting Started

1. **Fork the repository** on GitHub.
2. **Clone your fork** locally.
3. **Open the project** in IntelliJ IDEA.
4. **Set up the SDK**: Ensure you have a JDK 21+ installed and configured. IntelliJ should automatically pick up the Gradle configuration.

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
If you've added new logic, please consider adding unit tests in `src/test/kotlin` (if applicable) and run them with:
```bash
./gradlew test
```

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

## Documentation References

When referencing Blender documentation, please refer to the `.blender-docs` directory in the project root instead of online documentation whenever possible. This contains a local copy of the Blender Python and Manual docs for offline and consistent reference.

## Repository Reference

The [Jacques Lucke VS-Code extension](https://github.com/JacquesLucke/blender_vscode.git) is the main basis for this project. A clone of this repository is maintained in the `.reference` directory for architectural and feature parity reference.

## AI-Assisted Development

This project uses AI agents (like Junie) to maintain high-quality code and documentation. When contributing using an AI agent:

- **Logging**: Ensure all chat sessions are logged in the `.ai-logs/` folder following the naming convention `chat-session-YYYY-MM-DD.log`.
- **Summaries**: Create a `summary_YYYY-MM-DD.md` in the same folder after significant milestones.
- **Context**: Keep `.junie/context.md` and `.junie/project.md` updated with the latest architectural decisions and project state.

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
