# Blender Dev Tools for PyCharm

Blender Dev Tools integration for PyCharm. This plugin allows you to launch Blender from within PyCharm and automatically or manually reload your Blender extensions during development.

## Quick Links

- [Installation Guide](#installation)
- [Operating Instructions](USAGE.md)
- [Architecture Overview](ARCHITECTURE.md)
- [Contributing](CONTRIBUTING.md)
- [Notices and Acknowledgments](NOTICE.md)
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

---

For legal notices and acknowledgments, please see [NOTICE.md](NOTICE.md).
