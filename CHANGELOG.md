# Changelog

## [0.2.1] - 2026-03-02
### Added
- **Blender Scanner**: Improved macOS Blender detection by utilizing the `which` command.

### Changed
- **Sandboxing**: Dynamically detect and copy all subdirectories from the system's Blender configuration directory, ensuring a more complete and automated environment setup.
- **Run Configurations**: Refactored the internal handling of the `src` path to use standard Kotlin Path utilities for improved reliability across operating systems.

### Fixed
- **Run Configuration Fatal Error**: Fixed the `FATAL_ERROR: Missing local "src"` error by using absolute paths for the `--source-dir` argument in "Build" and "Validate" configurations.
- **Blender Launcher**: Added a working directory to `GeneralCommandLine` for proper resolution of relative paths.
- **Sandboxing Toggle**: Corrected a bug where the `--app-template` argument was being incorrectly added to CLI-based extension commands.
- **Project Generation**: Fixed a missing argument in the README generator's template call.

## [0.2.0] - 2026-03-01
### Added
- New **Blender Dev Tools** project type and icon updates.
- **Custom Blender Versions**: Support for manually specifying Blender versions and paths.
- **Source Directory Management**: Support for marking specific folders as Blender source directories.
- **Universal OS Compatibility**: Improved path handling for Windows, macOS, and Linux.
- **Documentation**: Comprehensive documentation has been moved to an [external site](https://wiki.sakura-sedaia.com/docs/blender-development-pycharm/index.html).
- **Unit Tests**: Added basic unit tests for core plugin functions.
- **NPW Sandboxing Toggle**: Added a new setting to control if the Blender instance is sandboxed inside the New Project Wizard.

### Changed
- **Project Name**: Renamed plugin to "Blender Dev Tools".
- **Logging**: Improved logging with per-day log rotation and enhanced configuration.
- **Run Configurations**: Updated templates for Testing, Build, and Validation with a dynamic UI.
  - No longer appends `--app-template pycharm` when running the `build` and `validate` run configs.
  - Improved extension command detection for run configurations.
- **License**: Updated to full GNU/GPL V3 and extracted into a separate template file.

### Fixed
- **Blender Management Tool Window**: Fixed and reworked the UI for managing Blender versions and sandboxes.
- **Blender Manifest**: Switched from kebab-case to snake_case formatting for Manifest IDs to resolve validation errors within Blender.
- **Run Config CLI**: Corrected CLI arguments for Preset Extension run configurations, removing the extra `s` at the end of extensions.
- **Tool Window Stability**: Fixed crashes in the Blender version management tool window.
- **NPW Stability**: Fixed and improved the stability of the New Project Wizard, squashing many bugs and validation errors.

## [0.1.0] - 2026-02-28
- Initial release
