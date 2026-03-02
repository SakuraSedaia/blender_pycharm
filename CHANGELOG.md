# Changelog

## [0.2.0] - 2026-03-01
### Added
- **Blender Status Indicator**: Added a real-time status bar widget to monitor Blender connection states (Connected, Disconnected, Not Running).
- **Internationalization**: Comprehensive i18n support for all user-facing UI, logs, and console outputs across 11 languages (Spanish, German, French, Italian, Japanese, Korean, Dutch, Polish, Portuguese, Russian, and Chinese).
- **Blender Development Project**: New specialized project type for Blender extension development.
- **Improved Scanner**: Enhanced macOS and Linux Blender detection using the `which` command, and improved custom path labeling.
- **Custom Versions**: Support for manual specification of Blender executable paths and versioning.
- **Source Management**: Option to mark project folders as Blender source directories for better organization.
- **Cross-Platform Compatibility**: Refined path handling for Windows, macOS, and Linux.
- **Documentation**: Simplified internal documentation to English-only to ensure maintainability. Moved comprehensive and localized guides to the [external documentation site](https://wiki.sakura-sedaia.com/docs/blender-development-pycharm/index.html). Localized wiki links are available in [WIKI_LOCALIZED.md](docs/WIKI_LOCALIZED.md).
- **Unit Testing**: Initial suite of unit tests for core plugin functionality.
- **Sandbox Control**: New setting to toggle sandboxing for Blender instances within the New Project Wizard.

### Changed
- **Branding**: Renamed the plugin to **Blender Development** and updated all icons to comply with JetBrains Icon guidelines. Added standardized scaling and positioning for Blender logo icons.
- **Improved**: Added folder icons for directories marked as Blender source folders in the project view.
- **Environment Setup**: Automated the detection and replication of system Blender configuration subdirectories to ensure a consistent sandboxed environment.
- **Diagnostics**: Improved logging with per-day rotation, more detailed configuration, and specific error reporting for extraction/mounting failures.
- **Run Configurations**: Updated templates for testing, building, and validation with a dynamic UI.
  - Removed redundant `--app-template pycharm` arguments when executing `build` and `validate` commands.
  - Enhanced logic for detecting extension-specific commands.
  - Standardized internal `src` path handling using Kotlin NIO.2 utilities for better OS reliability.
- **Licensing**: Transitioned to GNU GPL v3 and moved license text to a standalone template.

### Fixed
- **Management UI**: Reworked the Blender version and sandbox management tool window for better stability.
- **Manifest Formatting**: Switched Manifest IDs from kebab-case to snake_case to comply with Blender's validation requirements.
- **CLI Arguments**: Corrected the extension command syntax in run configurations, fixing a pluralization error.
- **Stability**: Fixed crashes in the version management tool window and resolved validation issues in the New Project Wizard.
- **Path Resolution**: Fixed the `FATAL_ERROR: Missing local "src"` by utilizing absolute paths for the `--source-dir` argument.
- **Process Management**: Configured the `GeneralCommandLine` working directory to ensure correct resolution of relative paths.
- **Extension Logic**: Fixed a bug where `--app-template` was incorrectly applied to CLI-based extension operations.

## [0.1.0] - 2026-02-28
- Initial release
