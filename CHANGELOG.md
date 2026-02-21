# Changelog

## [1.1.0] - 2026-02-20

### Added
- **Blender 5.0 Support**: Full support for Blender 5.0, including set as the default version.
- **Improved Run Configurations**: Reorganized run configurations into four specialized templates: **Testing**, **Build**, **Validate**, and **Command**.
- **Dynamic UI**: Run configuration editor now dynamically hides fields that are not relevant to the selected mode.
- **AI Agent Guidelines**: Added an option to the project template to include standardized instructions for AI agents in a `.junie/` directory.
- **Folder Icon Detection**: Automatically assigns a Blender extension icon to directories containing a `blender_manifest.toml` file.
- **Gray Blender Logo**: Replaced the primary plugin icon with the gray Blender logo for a more integrated look.

### Changed
- **Robust Reload Mechanism**: Modernized the reload cycle using a structured JSON-based protocol and thorough `sys.modules` purging to prevent Python caching issues.
- **Improved Repository Management**: Enhanced startup scripts to better handle extension repository registration across different Blender versions.
- **Modernized Codebase**: Refactored `BlenderLauncher` and other core components using Kotlin-idiomatic patterns and `kotlin.io.path`.
- **Gradle Downgrade**: Downgraded to Gradle 8.13.0 for better stability and compatibility with the IntelliJ Platform Gradle Plugin.
- **Documentation Overhaul**: Updated `README.md` and `CONTRIBUTING.md` to reflect current project architecture, installation procedures, and development workflows. Moved technical development instructions to `CONTRIBUTING.md`.

### Fixed
- Resolved various issues with symbolic link creation and Blender version detection.
- Fixed Gradle metadata corruption issues in certain environments.
