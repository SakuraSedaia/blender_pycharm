# Blender Extension Development for PyCharm

Blender Extension Development integration for PyCharm. This plugin allows you to launch Blender from within PyCharm and automatically or manually reload your Blender extensions during development.

## AI Content Notice

This project was developed almost exclusively using the AI integration tools by JetBrains s.r.o. in Intellij IDEA. The AI tools were used to generate the initial code, documentation, and some of the content in this README. The final review and editing were done by a human to ensure accuracy and clarity.

## Documentation

Visit our [GitHub Wiki](https://github.com/Sakura-Sedaia/BlenderExtensions/wiki) for in-depth documentation:
- [Installation Guide](docs/wiki/Installation.md)
- [Usage Guide](docs/wiki/Usage-Guide.md)
- [Architecture Overview](docs/wiki/Architecture.md)
- [Coding Guidelines](docs/wiki/Coding-Guidelines.md)

## Features

For a detailed look at all features, check out our [[Wiki|Features]].

- **Testing**: Launch a Blender instance in a development environment with auto-reload, sandboxing, and symlinking enabled.
- **Auto-Reload**: Automatically reload your extension in Blender whenever you save a file in PyCharm.
- **Manual Reload**: Trigger a reload manually using a keyboard shortcut or menu action.
- **Project Template**: Quickly start a new Blender Extension project from a single template that mirrors PyCharm’s Pure Python setup. It includes a comprehensive project wizard to configure your `blender_manifest.toml`:
    - **Metadata**: Set Addon ID, Tagline, Maintainer, Website, and Tags.
    - **Compatibility**: Specify minimum and maximum Blender versions, and supported platforms.
    - **Permissions**: Easily request permissions for Network, Filesystem, Clipboard, Camera, and Microphone (with mandatory reasons).
    - **Build Settings**: Configure build exclude patterns and license information.
    - **Automation**: Optionally enable “Auto-load” to include an `auto_load.py` helper and autoload-ready `__init__.py`.
    - **AI Integration**: Check “Append pre-made agent guidelines” to include standardized instructions for AI agents in a `.junie/` directory.
    - **Run Configurations**: The template automatically creates pre-configured Blender Run Configurations for Testing, Build, and Validate.
    - **Post-Creation**: The generated `README.md` provides clear instructions on configuring your Python interpreter and linting stubs using PyCharm's built-in tools.
- **Automated Folder Icon Detection**: Directories containing a `blender_manifest.toml` file are automatically identified with a custom Blender extension folder icon for better project navigation.
- **Blender Management Tool Window**: A new tool window (right side) to manage global Blender installations (Download/Delete) and clear the project-local sandbox.
- **Configurable**: Easily set the path to your Blender executable and toggle auto-reload.

---

## Build Instructions

Currently, the plugin is not published on the JetBrains Marketplace, and as such it must be built from source and installed manually.

1. Ensure you have the latest version of Intellij IDEA (Community or Pro) installed.
2. Clone this repository and open it in Intellij IDEA.
3. Run `./gradlew buildPlugin` or the "Build Plugin" run configuration.
    - The plugin will be built in the `build/distributions` directory.
4. In PyCharm, open **Settings** (or **Preferences** on macOS) > **Plugins**.
5. Click the gear icon (⚙️) next to the "Installed" tab and select **Install Plugin from Disk...**.
6. Navigate to the downloaded ZIP file and click **OK**.
7. Restart PyCharm to complete the installation.

---

## Operating Instructions

### Setup

1. Open **Run/Debug Configurations** (Run > Edit Configurations...).
2. Click **+** and select **Blender**.
3. (Optional) Open the **Blender Management** tool window on the right sidebar to view, download, or delete managed Blender versions, or to clear the project's `.blender-sandbox` folder.
4. Choose a configuration template:
    - **Testing**: Launch Blender in a development environment with auto-reload, sandboxing, and symlinking. This is the primary mode for active development.
    - **Build**: Build your extension using `blender --command extensions build`.
    - **Validate**: Validate your extension using `blender --command extensions validate`.
    - **Command**: Run a custom Blender command using `blender --command <command>`.
5. Choose a Blender version from the **Blender version** dropdown (4.2+). Blender 5.0 is the default version. The plugin will download and manage it for you. Alternatively, pick **Custom/Pre-installed** and set the path manually.
6. (Testing only) Toggle **Enable Sandboxing** to isolate your development environment. When enabled, the plugin runs Blender with a project-local app template and user dirs to avoid conflicts. The plugin includes a default splash screen for sandboxed sessions. You can also provide a custom one by placing a `splash.png` file in your project root. You can disable sandboxing any time. Additionally, check **Import User Configuration** to copy your standard Blender settings (user preferences, startup file, etc.) into the sandbox. Note that fields not relevant to the selected mode (like sandboxing for Build/Validate) are automatically hidden.
7. (Testing only) Set the **Addon source directory** (defaults to the project's root) and optionally a **symlink name**.
8. (Optional) In **Settings** > **Tools** > **Blender Extension Integration**, check **Auto-reload extension on save** to enable automatic reloads.

### Logging and Troubleshooting
The plugin maintains a runtime log in the project root: `blender_plugin.log`. This log contains detailed information about:
- Blender downloads and extraction status (including macOS DMG mounting).
- Symbolic link or directory junction creation.
- Startup arguments and sandboxing details.

If you encounter issues, check this log first.

### Usage

- **Testing**: Create a new **Blender** Run Configuration (Run > Edit Configurations... > + > Blender), select the **Testing** template, and run it.
- **Reload Extension**:
    - **Manual**: Go to the **Blender** menu and select **Reload Extension**, or use the shortcut `Ctrl+Alt+R`.
    - **Automatic**: If enabled in settings, simply save any file in your project (`Ctrl+S`).

## Project Structure

The project follows the standard IntelliJ Platform plugin structure:

```
.
├── build.gradle.kts        # Gradle build configuration
├── src
│   ├── main
│   │   ├── kotlin          # Plugin source code
│   │   │   └── com.sakurasedaia.blenderextensions
│   │   │       ├── actions     # Keyboard shortcuts and menu actions
│   │   │       ├── blender     # Core Blender service, TCP server, and communication
│   │   │       ├── icons       # Custom icons and icon providers
│   │   │       ├── listeners   # File system listeners for auto-reload
│   │   │       ├── project     # Project template generators and manifest wizards
│   │   │       ├── run         # Specialized Run Configurations (Testing, Build, etc.)
│   │   │       ├── settings    # Persistent settings and configuration UI
│   │   │       └── ui          # Blender Management Tool Window and UI components
│   │   └── resources
│   │       └── META-INF
│   │           └── plugin.xml  # Plugin manifest
```

## How it Works

The plugin starts a local TCP server when Blender is launched. It injects a startup Python script into Blender that:
1. **Repository Management**: Automatically configures a local extension repository named `blender_pycharm` pointing to the project's symlink. Handles API differences between Blender 4.2+ and 5.0.
2. **Communication**: Connects back to PyCharm and listens for structured JSON reload commands (e.g., `{"type": "reload", "name": "my_extension"}`).
3. **Robust Reload Cycle**: Executes a robust reload sequence on Blender's main thread (via `bpy.app.timers`):
    - Disables the specific extension module.
    - Purges the module and all its submodules from Python's `sys.modules` to clear the module cache.
    - Forces a refresh of all extension repositories (`bpy.ops.extensions.repo_refresh_all()`).
    - Re-enables the extension, forcing a fresh import of your code changes.
4. **Sandboxing and Configuration**: Creates a project-local Blender user environment (`.blender-sandbox`) that isolates development settings. This includes custom splash screens and optionally imported user configurations (preferences, startup file).
5. **Global Version Management**: Automatically handles multi-version downloads (4.2+ and 5.0) and global installation management through the dedicated tool window.

This ensures that your code changes are always picked up, avoiding common caching issues in Blender's Python environment.

---

## Acknowledgments

1. This extension is **heavily** based on and inspired by [Jacques Lucke's blender_vscode](https://github.com/JacquesLucke/blender_vscode) extension for Visual Studio Code. Some of the core functions are pulled directly from his extension, those include in this project are:
- Extension repository management.
- Communication between Blender and PyCharm.
- Robust reload cycle.
- New Project Wizard. (Integrated into PyCharm's New Project Wizard)

2. The Blender logo is a trademark of the Blender Foundation, and is only used within this project to denote Blender specific actions and features.

3. The initial versions of this project were developed using JetBrains' AI tools, specifically Junie, which wrote a significant portion of the code and documentation.

---

For more information on contributing, see [CONTRIBUTING.md](./CONTRIBUTING.md).

---

## Disclaimer

This project is not affiliated with either Blender, the Blender Foundation, or JetBrains s.r.o, as I am not a developer or maintainer for any of these organizations.