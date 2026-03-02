# Changelog

## [0.3.0] - 2026-03-02
### Added
- **Bidirectional Heartbeat**: Implemented a "ready" signal and retry logic for Blender-to-PyCharm TCP connections, ensuring reliable communication.
- **Connection Status UI**: Added a real-time status indicator in the Blender Management tool window.
- **Enhanced Run Configs**: Introduced inline download buttons for missing Blender versions and support for multiple source folders.
- **Automatic Interpreter Setup**: Plugin now automatically detects and configures the bundled Python interpreter from managed Blender installations.
- **Performance**: Added a version metadata cache to significantly speed up Blender scanning.
- **CI/CD**: Added a GitHub Actions workflow for automated plugin verification and headless integration tests.

### Fixed
- **Stability**: Improved error handling with balloon notifications for critical TCP and process errors.
- **Environment**: Fixed issues with symlink creation when multiple source folders are involved.

## [0.2.0] - 2026-03-01
### Added
- **Blender Dev Tools Project**: New specialized project type for Blender extension development.
- **Improved Scanner**: Enhanced macOS and Linux Blender detection using the `which` command.
- **Custom Versions**: Support for manual specification of Blender executable paths and versioning.
- **Source Management**: Option to mark project folders as Blender source directories for better organization.
- **Cross-Platform Compatibility**: Refined path handling for Windows, macOS, and Linux.
- **Documentation**: Moved comprehensive guides to the [external documentation site](https://wiki.sakura-sedaia.com/docs/blender-development-pycharm/index.html).
- **Unit Testing**: Initial suite of unit tests for core plugin functionality.
- **Sandbox Control**: New setting to toggle sandboxing for Blender instances within the New Project Wizard.

### Changed
- **Branding**: Renamed the plugin to **Blender Dev Tools** and updated all icons to comply with JetBrains Icon guidelines. Added standardized scaling and positioning for Blender logo icons.
- **Enhanced Wiki Configuration**: Added a dedicated "Wiki Project Path" section to `.junie/wiki_guidelines.md` for seamless multi-OS development (Windows/Linux).
- **Improved**: Added folder icons for directories marked as Blender source folders in the project view.
- **Environment Setup**: Automated the detection and replication of system Blender configuration subdirectories to ensure a consistent sandboxed environment.
- **Diagnostics**: Improved logging with per-day rotation and more detailed configuration.
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
- **Readme Generation**: Resolved a missing argument error in the README generator template call.

## [0.1.0] - 2026-02-28
- Initial release
