### Session Log - Blender Extension Integration Improvements

#### Date: 2026-02-20

#### Improvements for macOS and Windows:
1.  **macOS DMG Support**:
    -   Implemented automatic mounting of Blender `.dmg` files using `hdiutil`.
    -   Automated copying of `Blender.app` from the mounted DMG to the local cache directory.
    -   Automatic unmounting/detaching of the DMG after extraction.
2.  **Windows Symlink Robustness**:
    -   Added a fallback to **Directory Junctions** (`mklink /J`) on Windows if standard symbolic link creation fails.
    -   This avoids common permission issues on Windows where standard symlinks require Developer Mode or Administrator privileges, while junctions do not.
3.  **Enhanced Logging**:
    -   Implemented a runtime logger that writes to `blender_plugin.log` in the project root.
    -   Logs include:
        -   Download URLs and progress.
        -   Extraction steps (ZIP, TAR.XZ, DMG).
        -   Symbolic link / Junction creation status.
        -   Blender startup parameters and sandbox configuration.
4.  **Extraction Error Handling**:
    -   Refactored `extractFile` to better handle different platforms and file formats, with explicit error logging for each step.
5.  **Automatic Extension Enabling**:
    -   Modified the injected startup script to include an `ensure_extension_enabled` function.
    -   Used a timer to automatically enable the extension after Blender's initial indexing (~1.0s delay), ensuring a smooth developer experience.
6.  **Custom and Default Splash Screen for Sandboxing**:
    -   Integrated a default splash screen into the plugin resources (`src/main/resources/splash.png`).
    -   Updated the sandboxing function to use this default splash screen automatically.
    -   Maintained support for project-specific overrides if a `splash.png` exists in the user's project root.

#### How to test on Windows:
1.  Open the project in PyCharm/IntelliJ.
2.  Go to **Run/Debug Configurations**.
3.  Create a new **Blender** configuration.
4.  Select a Blender version (e.g., 4.2).
5.  Click **Run**.
6.  Check `blender_plugin.log` in the project root to see the download and extraction logs.
7.  Verify that the extension is correctly linked in the Blender extensions directory (check the log for the exact path).
8.  If symlink fails, verify that a junction was created instead.

#### How to test on macOS:
1.  Follow the same steps as Windows.
2.  The plugin should now successfully handle the `.dmg` download, mount it, and copy the `Blender.app`.
