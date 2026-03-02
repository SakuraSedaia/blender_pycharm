# Wiki Update Recommendations - Session 2026-03-02

The following changes have been implemented in the Blender Development for PyCharm plugin and should be reflected in the [Wiki](https://sakurasedaia.github.io/PycharmBlenderWiki/).

## 1. Installation & Setup
- **New Feature**: Automatic Python Interpreter Detection.
  - When a managed Blender version is downloaded via the plugin, it now automatically locates the bundled Python interpreter.
  - Users should be informed that they no longer need to manually find the `python.exe` (or `python` binary) within the Blender installation folders for linting and stub support.
- **Improved Scanner**: Scanning for Blender installations is now faster due to version metadata caching.

## 2. Usage Guide (Run Configurations)
- **Inline Download Buttons**: The Run Configuration editor now features a "Download" button next to the Blender version dropdown if the selected version is missing.
- **Multiple Source Folders**: The "Addon source directory" field now supports multiple, comma-separated paths. The plugin will create separate symlinks/junctions for each folder within Blender's extension repository.
- **Connection Status**: The Blender Management tool window now displays a "Status" label indicating whether PyCharm is actively connected to a running Blender instance.

## 3. Architecture & Communication
- **Bidirectional Heartbeat**: The communication protocol has been upgraded. Blender now sends a "ready" signal upon startup, and PyCharm utilizes retry logic (up to 5 attempts) to establish a stable TCP connection.
- **Error Handling**: Critical communication errors are now surfaced via IDE balloon notifications in addition to the `blender_plugin.log`.

## 4. Contributing
- **Headless Testing**: A new suite of headless integration tests has been added. Contributors should run `./gradlew test` to verify that their changes don't break the core communication and reload cycles.
- **CI/CD**: A GitHub Actions workflow (`.github/workflows/verify.yml`) now automatically validates all Pull Requests.
- **Localization Assistance**: A new [Localization Guide](docs/wiki/localization.md) has been added to the wiki to help community members contribute to translating the plugin into different languages.

## 5. Troubleshooting
- **Heartbeat Timeout**: If the "Status" remains disconnected, check if a firewall is blocking the dynamic TCP port (defaulting to 5555).
- **Metadata Cache**: If the plugin fails to detect a newly installed (custom) Blender version, clearing the plugin's internal cache may be necessary (stored in the IDE's system directory).
