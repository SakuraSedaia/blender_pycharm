# Operating Instructions

This guide provides detailed instructions on how to set up and use the Blender Development for PyCharm. For more in-depth documentation, please visit our [Wiki](https://sakurasedaia.github.io/PycharmBlenderWiki/).

## Setup

1. Open **Run/Debug Configurations** (Run > Edit Configurations...).
2. Click **+** and select **Blender**.
3. (Optional) Open the **Blender Management** tool window on the right sidebar to view, download, or delete managed Blender versions, or to clear the project's `.blender-sandbox` folder.
4. Choose a configuration template:
    - **Testing**: Launch Blender in a development environment with auto-reload, sandboxing, and symlinking. This is the primary mode for active development.
    - **Build**: Build your extension using `blender --command extensions build`.
    - **Validate**: Validate your extension using `blender --command extensions validate`.
    - **Command**: Run a custom Blender command using `blender --command <command>`.
5. Choose a Blender version from the **Blender version** dropdown (4.2+). Blender 5.0 is the default version. The plugin will download and manage it for you. Alternatively, pick **Custom/Pre-installed** and set the path manually.
6. (Testing only) Toggle **Enable Sandboxing** to isolate your development environment. When enabled, the plugin runs Blender with a project-local app template and user dirs to avoid conflicts. The plugin includes a default splash screen for sandboxed sessions. You can also provide a custom one by placing a `splash.png` file in your project root. You can disable sandboxing any time. Additionally, check **Import User Configuration** to copy your standard Blender settings (user preferences, startup file, etc.) into the sandbox. Note that fields not relevant to the selected mode (like sandboxing for Build/Validate) are automatically hidden. Inline **Download** buttons are available if a specific Blender version is missing.
7. (Testing only) Set the **Addon source directory** (defaults to the project's root) and optionally a **symlink name**. Multiple source directories can be specified (comma-separated or through the UI) to be linked into Blender.
8. (Optional) In **Settings** > **Tools** > **Blender Development**, check **Auto-reload extension on save** to enable automatic reloads.

## Usage

- **Testing**: Create a new **Blender** Run Configuration (Run > Edit Configurations... > + > Blender), select the **Testing** template, and run it.
- **Reload Extension**:
    - **Manual**: Go to the **Blender** menu and select **Reload Extension**, or use the shortcut `Ctrl+Alt+R`.
    - **Automatic**: If enabled in settings, simply save any file in your project (`Ctrl+S`).

## Logging and Troubleshooting

The plugin maintains a runtime log in the project root: `blender_plugin.log`. This log contains detailed information about:
- Blender downloads and extraction status (including macOS DMG mounting).
- Symbolic link or directory junction creation.
- Startup arguments and sandboxing details.

If you encounter issues, check this log first.

### Common Issues

- **macOS/Linux Binary Path**: If you are using a custom Blender installation, ensure the path points to the actual executable, not the `.app` bundle or a shell script. 
    - On macOS, this is typically `Blender.app/Contents/MacOS/Blender`.
    - On Linux, if you downloaded a tarball, it's the `blender` binary inside the extracted folder.
- **Connection Issues**: If PyCharm fails to connect to Blender:
    - Check the `Status` in the **Blender Management** tool window.
    - Ensure no firewall is blocking port `5555` (or the dynamically allocated port shown in the logs).
    - If Blender takes a long time to load, the plugin will retry up to 5 times. Check the Blender console for connection errors.
- **Permission Errors**: On Windows, creating symbolic links may require Developer Mode enabled or running PyCharm as Administrator. The plugin will attempt to create a directory junction as a fallback.
