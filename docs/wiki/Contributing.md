# Contributing

Thank you for your interest in contributing to the Blender Dev Tools for PyCharm plugin!

## Getting Started

### Prerequisites
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Community or Ultimate)
- [JDK 21](https://adoptium.net/temurin/releases/?version=21) or later
- [Blender](https://www.blender.org/) 4.2 or later (supporting extensions)

### Setup
1. **Fork the repository** on GitHub.
2. **Clone your fork** locally.
3. **Open the project** in IntelliJ IDEA.
4. **Set up the SDK**: Ensure you have JDK 21+ installed and configured.

## Development Workflow

### 1. Create a Branch
```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/your-bug-fix
```

### 2. Make Changes
Implement your changes in the `src/main/kotlin` directory. Follow the existing project structure and [[Coding Guidelines]].

### 3. Test Your Changes
Run the plugin in a sandbox environment to verify your changes:
```bash
./gradlew runIde
```
If you've added new logic, please consider adding unit tests in `src/test/kotlin` and run them with:
```bash
./gradlew test
```

### 4. Build and Verify
To build the distribution plugin ZIP file:
```bash
./gradlew build
```
The built plugin will be available in `build/distributions/`.

### 5. Open a Pull Request
Submit a Pull Request on GitHub. Provide a clear description of the changes and any related issues.

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
