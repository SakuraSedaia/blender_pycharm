### Blender Scanner and System Installation Integration

Implemented a new system to scan and discover existing Blender installations on the user's machine, and integrated it into both the Blender Management tool window and the Blender run configurations.

#### Key Changes:
- **BlenderScanner.kt**: New utility to scan standard OS locations (Windows Program Files, macOS Applications, Linux /usr/bin, /opt, etc.) for Blender executables. It also attempts to detect the Blender version using the `--version` CLI command.
- **BlenderVersions.kt**: Updated to provide a unified list of selectable versions, combining managed (downloaded) versions, discovered system versions, and the custom path option.
- **BlenderSettingsEditor.kt**: The Run Configuration UI now populates the Blender version dropdown with all discovered installations.
- **BlenderRunProfileState.kt**: Enhanced to correctly resolve selected versions, whether they are managed IDs or absolute paths to system installations. It also detects the version of system installations to enable accurate user configuration importing.
- **BlenderToolWindowContent.kt**: Added a new section "System Blender Installations" that displays all detected versions on the machine.
- **BlenderScannerTest.kt**: Added a smoke test to ensure the scanner runs without errors.

#### Verification:
- Verified that the project builds and tests pass.
- Discovered system installations are now selectable in the Start Blender run configuration.
- Discovered installations are listed in the Blender Management tool window.