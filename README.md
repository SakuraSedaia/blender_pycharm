# Blender Dev Tools for PyCharm

Blender Dev Tools integration for PyCharm. This plugin allows you to launch Blender from within PyCharm and automatically or manually reload your Blender extensions during development.

## AI Content Notice

This project was developed almost exclusively using the AI integration tools by JetBrains s.r.o. in Intellij IDEA. The AI tools were used to generate the initial code, documentation, and some of the content in this README. The final review and editing were done by a human to ensure accuracy and clarity.

## Documentation

Visit our [Wiki](https://sakurasedaia.github.io/PycharmBlenderWiki/) for in-depth documentation:
- [Installation Guide](https://sakurasedaia.github.io/PycharmBlenderWiki/docs/Installation.html)
- [Usage Guide](https://sakurasedaia.github.io/PycharmBlenderWiki/docs/usage-guide/index.html)
- [Architecture Overview](https://sakurasedaia.github.io/PycharmBlenderWiki/docs/Architecture.html)
- [Contributing](https://sakurasedaia.github.io/PycharmBlenderWiki/docs/Contributing.html)
- [Coding Guidelines](https://sakurasedaia.github.io/PycharmBlenderWiki/docs/Coding-Guidelines.html)

## Features

For a detailed look at all features, check out the [Wiki](https://sakurasedaia.github.io/PycharmBlenderWiki/docs/Features.html).

- **Testing**: Launch a Blender instance in a development environment with auto-reload, sandboxing, and symlinking enabled. Supports multiple source folders via automatic junction/symlink creation.
- **Auto-Reload**: Automatically reload your extension in Blender whenever you save a file in PyCharm. Uses a bidirectional heartbeat and retry logic for robust communication.
- **Manual Reload**: Trigger a reload manually using a keyboard shortcut or menu action.
- **Project Template**: Quickly start a new Blender Extension project from a single template that mirrors PyCharm’s Pure Python setup. It includes a comprehensive project wizard to configure your `blender_manifest.toml`:
    - **Metadata**: Set Addon ID, Tagline, Maintainer, Website, and Tags.
    - **Compatibility**: Specify minimum and maximum Blender versions, and supported platforms.
    - **Permissions**: Easily request permissions for Network, Filesystem, Clipboard, Camera, and Microphone (with mandatory reasons).
    - **Build Settings**: Configure build exclude patterns and license information.
    - **Automation**: Optionally enable “Auto-load” to include an `auto_load.py` helper and autoload-ready `__init__.py`.
    - **AI Integration**: Check “Append pre-made agent guidelines” to include standardized instructions for AI agents in a `.junie/` directory.
    - **Run Configurations**: The template automatically creates pre-configured Blender Run Configurations for Testing, Build, and Validate.
    - **Post-Creation**: The generated `README.md` provides clear instructions on configuring your Python interpreter and linting stubs using PyCharm's built-in tools. Automatic Python interpreter detection is available for managed Blender installations.
- **Blender File Icons**: `.blend` and `.blend1` files are automatically identified with a custom Blender color icon.
- **Blender Management Tool Window**: A new tool window (right side) to manage global Blender installations (Download/Delete), view real-time connection status, and clear the project-local sandbox.
- **Configurable**: Easily set the path to your Blender executable, toggle auto-reload, and manage version metadata caching for faster scanning.
- **Automated Verification**: Integrated GitHub Actions workflow for headless testing and validation of the communication cycle.

---

## Installation

The plugin can be installed either by downloading a prebuilt binary or by building it from source.

### Option 1: Install Prebuilt Binary (Recommended)

Installing a prebuilt binary is the **safe and stable** option for most users.

1. Download the latest plugin ZIP file from the [GitHub Releases](https://github.com/Sakura-Sedaia/BlenderExtensions/releases) page.
2. In PyCharm, open **Settings** (or **Preferences** on macOS) > **Plugins**.
3. Click the gear icon (⚙️) next to the "Installed" tab and select **Install Plugin from Disk...**.
4. Navigate to the downloaded ZIP file and click **OK**.
5. Restart PyCharm to complete the installation.

### Option 2: Build from Source

Building from source provides the **most up-to-date** features but may be **unstable** as it reflects the current development state.

1. Ensure you have **JDK 21** or later installed.
2. Clone this repository:
   ```bash
   git clone https://github.com/Sakura-Sedaia/BlenderExtensions.git
   cd BlenderExtensions
   ```
3. Build the plugin using the included Gradle wrapper:
   - **Windows**: `.\gradlew.bat buildPlugin`
   - **macOS/Linux**: `./gradlew buildPlugin`
4. The built plugin ZIP file will be located in the `build/distributions` directory.
5. Follow steps 2-5 from **Option 1** to install the generated ZIP file.

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
6. (Testing only) Toggle **Enable Sandboxing** to isolate your development environment. When enabled, the plugin runs Blender with a project-local app template and user dirs to avoid conflicts. The plugin includes a default splash screen for sandboxed sessions. You can also provide a custom one by placing a `splash.png` file in your project root. You can disable sandboxing any time. Additionally, check **Import User Configuration** to copy your standard Blender settings (user preferences, startup file, etc.) into the sandbox. Note that fields not relevant to the selected mode (like sandboxing for Build/Validate) are automatically hidden. Inline **Download** buttons are available if a specific Blender version is missing.
7. (Testing only) Set the **Addon source directory** (defaults to the project's root) and optionally a **symlink name**. Multiple source directories can be specified (comma-separated or through the UI) to be linked into Blender.
8. (Optional) In **Settings** > **Tools** > **Blender Dev Tools**, check **Auto-reload extension on save** to enable automatic reloads.

### Logging and Troubleshooting
The plugin maintains a runtime log in the project root: `blender_plugin.log`. This log contains detailed information about:
- Blender downloads and extraction status (including macOS DMG mounting).
- Symbolic link or directory junction creation.
- Startup arguments and sandboxing details.

If you encounter issues, check this log first.

### Troubleshooting

- **macOS/Linux Binary Path**: If you are using a custom Blender installation, ensure the path points to the actual executable, not the `.app` bundle or a shell script. 
    - On macOS, this is typically `Blender.app/Contents/MacOS/Blender`.
    - On Linux, if you downloaded a tarball, it's the `blender` binary inside the extracted folder.
- **Connection Issues**: If PyCharm fails to connect to Blender:
    - Check the `Status` in the **Blender Management** tool window.
    - Ensure no firewall is blocking port `5555` (or the dynamically allocated port shown in the logs).
    - If Blender takes a long time to load, the plugin will retry up to 5 times. Check the Blender console for connection errors.
- **Permission Errors**: On Windows, creating symbolic links may require Developer Mode enabled or running PyCharm as Administrator. The plugin will attempt to create a directory junction as a fallback.

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

1. This extension is **heavily** based on and inspired by [Jacques Lucke's blender_vscode](https://github.com/JacquesLucke/blender_vscode) extension for Visual Studio Code. Some of the core functions are pulled directly from his extension, including:
- Extension repository management.
- Communication between Blender and PyCharm.
- Robust reload cycle.
- New Project Wizard (integrated into PyCharm's New Project Wizard).

2. The initial versions of this project were developed using JetBrains' AI tools, specifically Junie, which wrote a significant portion of the code and documentation.

## Trademark Notice

Blender is a registered trademark of the Blender Foundation. The Blender logo is used within this project under **nominative fair use** to denote compatibility and integration features. 

This project is an independent community development and is **not** affiliated with, endorsed by, or sponsored by the Blender Foundation. 

The icons used in this project are either custom-made, based on JetBrains' `AllIcons` library, or utilize the [Blender Community Logo](https://www.blender.org/about/logo/#community) ("the eye") in a way that respects its intended use for third-party extensions and tools.

---

For more information on contributing, see [CONTRIBUTING.md](./CONTRIBUTING.md).

---

## Disclaimer

This project is not affiliated with, endorsed by, or sponsored by the Blender Foundation or JetBrains s.r.o. It is an independent community effort.

---

## Future Plans

The following features and improvements are planned for future releases:

- **Enhanced UI**: More granular control over symlink management and multi-extension projects.
- **Deeper Integration**: Better support for Blender's internal asset browser and library management from within PyCharm.
