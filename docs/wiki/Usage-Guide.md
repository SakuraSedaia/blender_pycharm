# Usage Guide

This guide will help you get started with the Blender Development for PyCharm plugin.

## Creating a New Project

1. Go to **File** > **New Project...**.
2. Select **Blender Extension** from the list of project types.
3. Configure your project name and location.
4. Fill in the **Blender Manifest** details:
   - **Addon ID**: The unique identifier for your extension.
   - **Metadata**: Tagline, Maintainer, Website, Tags.
   - **Permissions**: Network, Filesystem, etc. (Required by Blender for extensions).
5. (Optional) Check **Enable Auto-load** to include the `auto_load.py` helper script.
6. Click **Create**.

## Setting Up Run Configurations

On project creation, the plugin automatically creates three run configurations for you:

### 1. Testing (Run Blender)
This is your primary development mode.
- **Blender version**: Select the version you want to use (e.g., 5.0).
- **Enable Sandboxing**: Isolates your development environment to `.blender-sandbox`.
- **Import User Config**: Copies your main Blender settings into the sandbox.
- **Addon source directory**: The directory containing your extension code.

### 2. Build
Builds your extension using `blender --command extensions build`. The resulting `.zip` file will be in your project's root (or as configured in your manifest).

### 3. Validate
Checks your `blender_manifest.toml` for errors using `blender --command extensions validate`.

## Reloading your Extension

### Automatic Reload
If enabled in **Settings** > **Tools** > **Blender Extension Integration**, saving any file in your project (`Ctrl+S`) will automatically trigger a reload in the running Blender instance.

### Manual Reload
- **Keyboard Shortcut**: Press `Ctrl+Alt+R`.
- **Menu**: Go to the **Blender** menu in the main menu bar and select **Reload Extension**.

## Blender Management Tool Window
Located on the right sidebar, this tool window allows you to:
- **Refresh Status**: Manually scan for downloaded Blender versions.
- **Download/Delete**: Manage global Blender installations.
- **Clear Sandbox**: Delete the `.blender-sandbox` directory to reset your local testing environment.

## Troubleshooting
Check the `blender_plugin.log` in your project root for detailed logs on:
- Blender startup and arguments.
- Symbolic link creation.
- Communication between PyCharm and Blender.
- Any errors during downloads or extraction.
