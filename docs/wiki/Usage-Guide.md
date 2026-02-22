# Usage Guide

This guide provides a detailed walkthrough of the features and workflows available in the Blender Development for PyCharm plugin.

## 1. Project Lifecycle

### Creating a New Project
To start a new Blender extension project:
1. Go to **File** > **New Project...**.
2. Select **Blender Extension** from the left-hand list.
3. **Project Name**: As you type, the project location and **Addon ID** are automatically synchronized. The Addon ID is formatted to kebab-case as required by Blender.
4. **Blender Manifest Wizard**:
   - **Addon ID**: Unique identifier (e.g., `my-cool-extension`).
   - **Metadata**: Set the Tagline, Maintainer name, and optional Website/Tags.
   - **Blender Versions**: Define the minimum (and optional maximum) supported Blender versions.
   - **Platforms**: Specify supported platforms (e.g., `windows-x64, macos-arm64`).
   - **Permissions**: If your extension needs special access (Network, Filesystem, etc.), check the relevant box and provide a mandatory reason (max 64 characters, no trailing period).

### Bootstrapping Options
- **Add automatic module/class registration script**: Includes `auto_load.py` and an autoload-ready `__init__.py` to simplify complex project structures.
- **Append pre-made agent guidelines**: Adds a `.agent-guidelines.md` file to help AI coding agents understand your project structure.
- **Create Git repository**: Automatically initializes a Git repository in the project folder.

---

## 2. Environment Management

### Blender Management Tool Window
Located on the right sidebar (labeled **MBVS**), this tool window allows you to manage Blender installations globally:
- **Refresh Status**: Scans the system path for already downloaded versions.
- **Download/Delete**: Automatically fetch Blender 4.2+ or 5.0 versions directly from the Blender Foundation's servers.
- **Clear Sandbox**: Quickly delete the project-local `.blender-sandbox` directory to reset your testing environment.

### Version Support
The plugin officially supports **Blender 4.2+** and **Blender 5.0**. If you wish to use a version not managed by the plugin, select **Custom/Pre-installed** in the run configuration and point to your local executable.

---

## 3. Development & Testing

### The "Testing" Run Configuration
Upon project creation, a **Start Blender** (Testing) configuration is created automatically. It is pre-configured for active development:
- **Enable Sandboxed Environment**: (Recommended) Runs Blender using a project-local directory (`.blender-sandbox`). This prevents your development settings from affecting your main Blender installation.
- **Import User Configuration**: When sandboxing is enabled, check this to copy your main Blender preferences, startup file, and bookmarks into the sandbox.
- **Addon Source Directory**: Specifies where your source code is located (defaults to `src`).
- **Addon Symlink Name**: The name of the folder inside Blender's extension repository that will point to your source.

### Custom Splash Screens
In sandboxed mode, the plugin uses a default splash screen to remind you you're in a development session. To use your own, simply place a `splash.png` file in your project root.

---

## 4. Hot-Reloading

The plugin features a robust hot-reloading mechanism that ensures your changes are immediately visible in Blender without needing to restart it.

### Automatic Reload
1. Go to **Settings** > **Tools** > **Blender Extension Integration**.
2. Check **Auto-reload extension on save**.
3. Now, whenever you save a file (`Ctrl+S`), the plugin sends a reload command to Blender.

### Manual Reload
- **Keyboard Shortcut**: `Ctrl+Alt+R` (Windows/Linux/macOS).
- **Menu Action**: **Blender** > **Reload Extension**.

### How it Works
The reload process is more than just a script refresh; it:
1. Disables your extension.
2. Purges the module and all submodules from Python's cache (`sys.modules`).
3. Refreshes Blender's extension repositories.
4. Re-enables the extension to force a clean import.

---

## 5. CLI Tools (Extensions CLI)

Blender 4.2+ introduced a dedicated CLI for extension management. The plugin provides integrated run configurations for these tools.

### Build Configuration
Used to package your extension for distribution.
- **Action**: Runs `blender --command extensions build`.
- **Output**: Generates a `.zip` file in your project directory containing only the files defined in your manifest's include/exclude patterns.

### Validate Configuration
Used to ensure your `blender_manifest.toml` is correct.
- **Action**: Runs `blender --command extensions validate`.
- **Check**: Verifies IDs, versions, permissions, and required fields.

### Custom Commands
You can create a **Blender** run configuration with the **Command** template to run any arbitrary `blender --command <cmd>` operation.

---

## 6. Troubleshooting

### Runtime Logs
The plugin maintains a log file named `blender_plugin.log` in your project root. Check this file if:
- Blender fails to start.
- Reloading doesn't seem to happen.
- Symbolic links/junctions cannot be created.

### Common Issues
- **Windows Symlinks**: On Windows, creating symbolic links may require Developer Mode enabled or Administrator privileges. If both fail, the plugin will attempt to create a **Directory Junction** as a fallback.
- **macOS Extraction**: Downloading Blender on macOS involves mounting a DMG and copying the app. Ensure the plugin has permission to mount volumes if prompted.
