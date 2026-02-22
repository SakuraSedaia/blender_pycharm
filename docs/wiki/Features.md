# Features

The Blender Development for PyCharm plugin offers a rich set of features designed to make extension development as smooth as possible.

## Automated Environment Management
- **One-Click Downloads**: Automatically download and manage multiple Blender versions (4.2, 4.3, 4.4, 4.5, and 5.0) directly from the IDE.
- **Version Switching**: Easily switch between different Blender versions for testing and validation.
- **Global Management**: A dedicated tool window on the right sidebar allows you to see what's installed and delete versions you no longer need.

## Sandboxed Development
- **Isolation**: Launch Blender in a project-local environment (`.blender-sandbox`) to prevent conflicts with your main Blender installation.
- **Project-Specific Config**: Each project can have its own user preferences and startup files.
- **Custom Splash Screen**: Personalize your development sessions with a project-specific `splash.png`.
- **User Config Import**: Optionally import your system Blender's user preferences, startup files, and bookmarks into the sandbox.

## Real-time Hot-Reloading
- **Auto-Reload**: Automatically trigger a reload in Blender whenever you save a file in PyCharm.
- **Manual Reload**: Use `Ctrl+Alt+R` (or the Blender menu) to trigger a reload at any time.
- **Robust Cache Purging**: The plugin uses a deep purging logic to ensure that Python's `sys.modules` cache is cleared, making sure your changes are always reflected.

## Smart Project Templates
- **New Project Wizard**: Quickly bootstrap new extensions with a structure that mirrors PyCharm's Pure Python setup.
- **Manifest Configuration**: Configure your `blender_manifest.toml` (metadata, permissions, compatibility) directly during project creation.
- **Simple Add-on & Module Templates**: Quickly create a single-script Blender add-on (legacy `bl_info` support) or a boilerplate Blender module with standard imports from the **New** menu.
- **Auto-load Support**: Optionally include an `auto_load.py` helper to handle complex module structures automatically.
- **Automatic Run Configs**: Gets you up and running immediately with pre-configured Testing, Build, and Validate profiles.

## Integrated CLI Tools
- **Build**: One-click building of your extension into a ZIP distribution.
- **Validate**: Ensure your extension meets Blender's manifest requirements.
- **Custom Commands**: Run any `blender --command` directly from the IDE.

## Smart UI Integration
- **Folder Icons**: Directories containing a `blender_manifest.toml` are automatically marked with a Blender icon for easy identification.
- **Integrated Logging**: A dedicated `blender_plugin.log` in your project root tracks all operations for easy troubleshooting.
