# Changelog

## [0.4.0]
### Added
- **Python Interpreter Setup**: Added functionality to automatically configure the project's Python interpreter to use the one bundled with a selected Blender installation.

### Changed
- **Blender Downloader**: Simplified the Blender extraction process and flattened the directory structure to reduce nesting. Improved version management by ensuring version directories are created only during extraction and added macOS-specific app renaming (e.g., `Blender 4.2.app`) for better identification.
- **Table Layout**: Separated Managed and System Blender installation tables into individual classes and adjusted column widths for better readability.
- **Run Configuration**: Removed the "Custom" Blender path selection from Run Configurations to focus on using managed and detected system installations.

### Fixed
- **UI Improvements**: Moved the Blender installation path to a dedicated read-only field in the System table for easier access and added an "Interpreter" column to the tables.
- **Code Quality**: Removed unused attributes and refined internal API for version management.
- **Fixed Softlock in NPW**: Addressed a softlock issue in the New Project Wizard. 


## [0.3.0]
### Added
- **Blender Status Bar Widget**: New indicator in the IDE status bar showing connection status to Blender.
- **Support for Multiple Source Folders**: Projects can now designate and manage multiple folders as Blender source directories.
- **Automatic Python Interpreter Setup**: Streamlined environment configuration for new projects.
- **Offline Telemetry**: Added local-only telemetry for debugging and error reporting.
- **Internationalization**: Full i18n support for 11 languages (Spanish, German, French, Italian, Japanese, Korean, Dutch, Polish, Portuguese, Russian, and Chinese).
- **Unit & Integration Testing**: Added a comprehensive test suite, including headless integration tests for TCP heartbeat and reload logic.
- **Sandbox Management**: New tool window for clearing and managing Blender sandboxed environments.
- **Bidirectional Heartbeat**: Implemented a more robust TCP client with bidirectional heartbeat and automatic retry logic for connection stability.

### Changed
- **Localization Refactor**: Standardized all resource bundle keys and migrated from `BlenderBundle` to `LangManager` (extending `DynamicBundle`).
- **Improved Blender Downloader**: Refined extraction logic and updated the selectable version list to focus on LTS releases.
- **Path Resolution**: Centralized and improved cross-platform path handling using Kotlin NIO.2 (`java.nio.file.Path`) utilities.
- **Documentation Migration**: Moved comprehensive guides to a new Sphinx-based documentation site.
- **License Change**: Updated project license to officially use GNU GPL v3.
- **Configuration Discovery**: Switched to dynamic detection and copying of Blender configuration subdirectories (system vs. user) to handle different OS layouts.

### Fixed
- **macOS Compatibility**: Prevented installation of Blender 5.0+ on Intel-based Macs and integrated `tryWhich` for better executable detection.
- **Manifest Validation**: Switched extension Manifest IDs to `snake_case` to comply with Blender requirements.
- **Run Configuration Stability**: Fixed absolute path handling for sandboxed installations and corrected CLI argument syntax for preset configurations.
- **UI Stability**: Resolved crashes in the version management tool window and improved New Project Wizard validation.
- **Logging**: Added log rotation for better disk usage management and expanded debug output for connection handshakes.