# Installation

Currently, the plugin is not published on the JetBrains Marketplace, and as such it must be built from source and installed manually.

## Prerequisites

- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Community or Ultimate)
- [JDK 21](https://adoptium.net/temurin/releases/?version=21) or later

## Building from Source

1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/Sakura-Sedaia/BlenderExtensions.git
   ```
2. Open the project in IntelliJ IDEA.
3. Run the following command in the terminal or use the Gradle tool window:
   ```bash
   ./gradlew buildPlugin
   ```
   The plugin ZIP file will be generated in `build/distributions/`.

## Installing the Plugin

1. In PyCharm, go to **Settings** (or **Preferences** on macOS) > **Plugins**.
2. Click the gear icon (⚙️) next to the "Installed" tab and select **Install Plugin from Disk...**.
3. Navigate to the `build/distributions/` directory and select the generated ZIP file.
4. Click **OK** and **Restart** PyCharm.

## Configuration

After installation, go to **Settings** > **Tools** > **Blender Extension Integration** to enable **Auto-reload extension on save**.
