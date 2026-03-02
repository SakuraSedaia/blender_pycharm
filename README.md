# Blender Development for PyCharm

Blender Development integration for PyCharm. This plugin allows you to launch Blender from within PyCharm and automatically or manually reload your Blender extensions during development.

## Documentation

- [Installation Guide](#installation)
- [Operating Instructions](docs/USAGE.md) ([Localized Wiki](docs/WIKI_LOCALIZED.md))
- [Architecture Overview](docs/ARCHITECTURE.md) ([Localized Wiki](docs/WIKI_LOCALIZED.md))
- [Localized Wiki Links](docs/WIKI_LOCALIZED.md)
- [Contributing](CONTRIBUTING.md)
- [Notices and Acknowledgments](docs/NOTICE.md)
- [External Wiki](https://sakurasedaia.github.io/PycharmBlenderWiki/)

## Features

- **Testing Environment**: Launch Blender with auto-reload, sandboxing, and multi-folder symlinking support.
- **Auto-Reload**: Real-time extension updates on file save with robust TCP communication.
- **Project Template**: Integrated New Project Wizard for Blender extensions with full manifest configuration.
- **Blender Management**: Dedicated tool window for multi-version downloads (4.2+ & 5.0) and sandbox management.
- **Customization**: Support for custom splash screens and user configuration inheritance in sandboxes.

## Installation

### Option 1: Install Prebuilt Binary (Recommended)

1. Download the latest plugin ZIP file from the [GitHub Releases](https://github.com/Sakura-Sedaia/BlenderExtensions/releases) page.
2. In PyCharm, go to **Settings** > **Plugins**.
3. Click ⚙️ > **Install Plugin from Disk...**.
4. Select the downloaded ZIP and restart PyCharm.

### Option 2: Build from Source

1. Clone the repository: `git clone --depth 1 https://github.com/Sakura-Sedaia/BlenderExtensions.git`
2. Run build:
   - **Windows**: `.\gradlew.bat buildPlugin`
   - **macOS/Linux**: `./gradlew buildPlugin`
3. Install the ZIP from `build/distributions/` using the steps in Option 1.

## Future Plans

- **Enhanced UI**: More granular control over symlink management and multi-extension projects.
- **Deeper Integration**: Better support for Blender's internal asset browser and library management.

## License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. See the [LICENSE](LICENSE) file for the full license text.

---

For legal notices and acknowledgments, please see [NOTICE.md](docs/NOTICE.md).
