package com.sakurasedaia.blenderextensions.project

data class BlenderManifestSettings(
    val id: String,
    val name: String,
    val tagline: String,
    val maintainer: String,
    val website: String? = null,
    val tags: List<String>? = null,
    val blenderVersionMin: String = "4.2.0",
    val blenderVersionMax: String? = null,
    val platforms: List<String>? = null,
    val permissions: Map<String, String>? = null,
    val buildPathsExcludePattern: List<String>? = null
)

class BlenderProjectTemplateGenerator {
    companion object {
        fun generateManifest(settings: BlenderManifestSettings): String {
            val sb = StringBuilder()
            sb.append("schema_version = \"1.0.0\"\n\n")
            sb.append("# Example of manifest file for a Blender extension\n")
            sb.append("# Change the values according to your extension\n")
            sb.append("id = \"${settings.id}\"\n")
            sb.append("version = \"1.0.0\"\n")
            sb.append("name = \"${settings.name}\"\n")
            sb.append("tagline = \"${settings.tagline}\"\n")
            sb.append("maintainer = \"${settings.maintainer}\"\n")
            sb.append("# Supported types: \"add-on\", \"theme\"\n")
            sb.append("type = \"add-on\"\n\n")

            if (!settings.website.isNullOrBlank()) {
                sb.append("# Optional: link to documentation, support, source files, etc\n")
                sb.append("website = \"${settings.website}\"\n\n")
            } else {
                sb.append("# # Optional: link to documentation, support, source files, etc\n")
                sb.append("# website = \"https://extensions.blender.org/add-ons/my-example-package/\"\n\n")
            }

            if (!settings.tags.isNullOrEmpty()) {
                sb.append("# Optional: tag list defined by Blender and server, see:\n")
                sb.append("# https://docs.blender.org/manual/en/dev/advanced/extensions/tags.html\n")
                sb.append("tags = [${settings.tags.joinToString(", ") { "\"$it\"" }}]\n\n")
            } else {
                sb.append("# # Optional: tag list defined by Blender and server, see:\n")
                sb.append("# # https://docs.blender.org/manual/en/dev/advanced/extensions/tags.html\n")
                sb.append("# tags = [\"Animation\", \"Sequencer\"]\n\n")
            }

            sb.append("blender_version_min = \"${settings.blenderVersionMin}\"\n")
            if (!settings.blenderVersionMax.isNullOrBlank()) {
                sb.append("# Optional: Blender version that the extension does not support, earlier versions are supported.\n")
                sb.append("# This can be omitted and defined later on the extensions platform if an issue is found.\n")
                sb.append("blender_version_max = \"${settings.blenderVersionMax}\"\n\n")
            } else {
                sb.append("# # Optional: Blender version that the extension does not support, earlier versions are supported.\n")
                sb.append("# # This can be omitted and defined later on the extensions platform if an issue is found.\n")
                sb.append("# blender_version_max = \"5.1.0\"\n\n")
            }

            sb.append("# License conforming to https://spdx.org/licenses/ (use \"SPDX: prefix)\n")
            sb.append("# https://docs.blender.org/manual/en/dev/advanced/extensions/licenses.html\n")
            sb.append("license = [\n")
            sb.append("  \"SPDX:GPL-3.0-or-later\",\n")
            sb.append("]\n")
            sb.append("# # Optional: required by some licenses.\n")
            sb.append("# copyright = [\n")
            sb.append("#   \"2002-2024 Developer Name\",\n")
            sb.append("#   \"1998 Company Name\",\n")
            sb.append("# ]\n\n")

            if (!settings.platforms.isNullOrEmpty()) {
                sb.append("# Optional: list of supported platforms. If omitted, the extension will be available in all operating systems.\n")
                sb.append("platforms = [${settings.platforms.joinToString(", ") { "\"$it\"" }}]\n\n")
            } else {
                sb.append("# # Optional: list of supported platforms. If omitted, the extension will be available in all operating systems.\n")
                sb.append("# platforms = [\"windows-x64\", \"macos-arm64\", \"linux-x64\"]\n")
                sb.append("# # Other supported platforms: \"windows-arm64\", \"macos-x64\"\n\n")
            }

            if (!settings.permissions.isNullOrEmpty()) {
                sb.append("# Optional: add-ons can list which resources they will require:\n")
                sb.append("# * files (for access of any filesystem operations)\n")
                sb.append("# * network (for internet access)\n")
                sb.append("# * clipboard (to read and/or write the system clipboard)\n")
                sb.append("# * camera (to capture photos and videos)\n")
                sb.append("# * microphone (to capture audio)\n")
                sb.append("#\n")
                sb.append("# If using network, remember to also check `bpy.app.online_access`\n")
                sb.append("# https://docs.blender.org/manual/en/dev/advanced/extensions/addons.html#internet-access\n")
                sb.append("#\n")
                sb.append("# For each permission it is important to also specify the reason why it is required.\n")
                sb.append("# Keep this a single short sentence without a period (.) at the end.\n")
                sb.append("# For longer explanations use the documentation or detail page.\n\n")
                sb.append("[permissions]\n")
                settings.permissions.forEach { (key, reason) ->
                    sb.append("$key = \"$reason\"\n")
                }
                sb.append("\n")
            } else {
                sb.append("# # Optional: add-ons can list which resources they will require:\n")
                sb.append("# # * files (for access of any filesystem operations)\n")
                sb.append("# # * network (for internet access)\n")
                sb.append("# # * clipboard (to read and/or write the system clipboard)\n")
                sb.append("# # * camera (to capture photos and videos)\n")
                sb.append("# # * microphone (to capture audio)\n")
                sb.append("# #\n")
                sb.append("# # If using network, remember to also check `bpy.app.online_access`\n")
                sb.append("# # https://docs.blender.org/manual/en/dev/advanced/extensions/addons.html#internet-access\n")
                sb.append("# #\n")
                sb.append("# # For each permission it is important to also specify the reason why it is required.\n")
                sb.append("# # Keep this a single short sentence without a period (.) at the end.\n")
                sb.append("# # For longer explanations use the documentation or detail page.\n")
                sb.append("#\n")
                sb.append("# [permissions]\n")
                sb.append("# network = \"Need to sync motion-capture data to server\"\n")
                sb.append("# files = \"Import/export FBX from/to disk\"\n")
                sb.append("# clipboard = \"Copy and paste bone transforms\"\n\n")
            }

            if (!settings.buildPathsExcludePattern.isNullOrEmpty()) {
                sb.append("# Optional: advanced build settings.\n")
                sb.append("# https://docs.blender.org/manual/en/dev/advanced/extensions/command_line_arguments.html#command-line-args-extension-build\n")
                sb.append("[build]\n")
                sb.append("# These are the default build excluded patterns.\n")
                sb.append("# You only need to edit them if you want different options.\n")
                sb.append("paths_exclude_pattern = [${settings.buildPathsExcludePattern.joinToString(", ") { "\"$it\"" }}]\n")
            } else {
                sb.append("# # Optional: advanced build settings.\n")
                sb.append("# # https://docs.blender.org/manual/en/dev/advanced/extensions/command_line_arguments.html#command-line-args-extension-build\n")
                sb.append("# [build]\n")
                sb.append("# # These are the default build excluded patterns.\n")
                sb.append("# # You only need to edit them if you want different options.\n")
                sb.append("# paths_exclude_pattern = [\n")
                sb.append("#   \"__pycache__/\",\n")
                sb.append("#   \"/.git/\",\n")
                sb.append("#   \"/*.zip\",\n")
                sb.append("# ]\n")
            }

            return sb.toString()
        }

        fun generateSimpleInit(name: String, author: String): String {
            return """
                # This program is free software; you can redistribute it and/or modify
                # it under the terms of the GNU General Public License as published by
                # the Free Software Foundation; either version 3 of the License, or
                # (at your option) any later version.
                
                bl_info = {
                    "name": "$name",
                    "author": "$author",
                    "description": "",
                    "blender": (4, 2, 0),
                    "version": (0, 0, 1),
                    "location": "",
                    "warning": "",
                    "category": "Generic",
                }

                def register():
                    pass

                def unregister():
                    pass
            """.trimIndent()
        }

        fun generateAutoLoadInit(name: String, author: String): String {
            return """
                # This program is free software; you can redistribute it and/or modify
                # it under the terms of the GNU General Public License as published by
                # the Free Software Foundation; either version 3 of the License, or
                # (at your option) any later version.
                
                bl_info = {
                    "name": "$name",
                    "author": "$author",
                    "description": "",
                    "blender": (2, 80, 0),
                    "version": (0, 0, 1),
                    "location": "",
                    "warning": "",
                    "category": "Generic",
                }

                from . import auto_load

                auto_load.init()

                def register():
                    auto_load.register()

                def unregister():
                    auto_load.unregister()
            """.trimIndent()
        }

        fun generateReadme(): String {
            return """
                # Blender Extension

                This is a Blender extension developed in PyCharm.

                ## Setup Instructions

                To get the best development experience, follow these steps to configure your Python environment using PyCharm's built-in tools:

                1.  **Configure Python Interpreter**:
                    - Go to **File > Settings > Project** (or **PyCharm > Settings** on macOS) and select **Python Interpreter**.
                    - Click **Add Interpreter** and select **Add Local Interpreter...**.
                    - Choose **Virtualenv Environment**.
                    - Ensure **New environment** is selected. The **Location** should default to a `.venv` folder in your project root.
                    - Select a **Base interpreter** (Python 3.11 is recommended for Blender 4.2+ and 5.0).
                    - Click **OK**. PyCharm will create the virtual environment and configure it for your project.

                2.  **Install Linting Stubs**:
                    - In the same **Python Interpreter** settings page, click the **+** icon (Install) to open the Available Packages dialog.
                    - Search for `fake-bpy-module` and click **Install Package**.
                    - Once installed, you will have full code completion and type hinting for the `bpy` module.

                ## Development

                1.  Make sure you have Blender 4.2 or later installed.
                2.  Open **Run > Edit Configurations...**.
                3.  Select the **Blender** configuration.
                4.  Choose a Blender version (which the plugin will download) or select **Custom/Pre-installed** and provide the path to your Blender executable.
                5.  Click **Run** to start Blender with your extension automatically linked and enabled.

                ## Building and Validating

                You can use the provided Run Configurations to validate and build your extension:
                - **Validate Extension**: Checks your extension for errors and compliance with Blender's requirements.
                - **Build Extension**: Packages your extension into a `.zip` file ready for distribution.

                ## Useful Resources

                - [Blender Python API Documentation](https://docs.blender.org/api/current/)
                - [Blender Extension Platform](https://extensions.blender.org/)
            """.trimIndent()
        }

        fun generateLicense(): String {
            return """
                GNU GENERAL PUBLIC LICENSE
                Version 3, 29 June 2007

                Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
                Everyone is permitted to copy and distribute verbatim copies
                of this license document, but changing it is not allowed.

                [Shortened for brevity. Full text of GPLv3 would go here.]
            """.trimIndent()
        }

        fun generateGitignore(): String {
            return """
                # Byte-compiled / optimized / DLL files
                __pycache__/
                *.py[cod]
                *${'$'}py.class

                # C extensions
                *.so

                # Distribution / packaging
                .Python
                build/
                develop-eggs/
                dist/
                downloads/
                eggs/
                .eggs/
                lib/
                lib64/
                parts/
                sdist/
                var/
                wheels/
                share/python-wheels/
                *.egg-info/
                .installed.cfg
                *.egg
                MANIFEST

                # Environments
                .env
                .venv
                env/
                venv/
                ENV/
                env.bak/
                venv.bak/

                # PyCharm
                .idea/

                # Blender
                *.blend*
            """.trimIndent()
        }
        
        fun getAutoLoadContent(): String {
            val resource = BlenderProjectTemplateGenerator::class.java.getResourceAsStream("/templates/auto_load.py")
            return resource?.bufferedReader()?.use { it.readText() } ?: ""
        }

        fun generateAgentGuidelines(): String {
            return """
                ### Universal Blender Agent Guidelines

                These guidelines are based on the Blender 5.0 Documentation and are designed to ensure consistency across all AI agents working on this project.

                #### 1. Extension Structure & Manifest
                - **Root Directory**: The active addon code is located in the `src/` directory.
                - **Manifest Required**: Every extension must include a `blender_manifest.toml` file at its root (within `src/` for active development).
                - **Required Metadata**: The manifest must include `id`, `version`, `name`, `tagline`, `maintainer`, `type`, and `license`.
                - **Permissions**: Explicitly state required permissions (e.g., `network`, `files`) in the manifest with a short explanation.

                #### 2. Versioning (MAJOR.MINOR.PATCH)
                Follow the standard `MAJOR.MINOR.PATCH` format:
                - **MAJOR**: Increment for breaking changes (e.g., UI reworks, API changes that break compatibility, or if users need to relearn a significant part of the add-on).
                - **MINOR**: Increment for new functionality that is backward compatible.
                - **PATCH**: Increment for bug fixes and minor internal changes.

                #### 3. Python Style Conventions
                - **Naming**: 
                  - Class names: `CamelCase` (e.g., `MyOperator`).
                  - Module/Variable names: `snake_case` (e.g., `my_module`).
                - **Indentation**: Use **4 spaces** for indentation. Never use tabs.
                - **Imports**: 
                  - Use explicit imports only. 
                  - **No wildcard imports** (e.g., `from bpy.types import *` is forbidden).
                  - **BPY Importing Alias**: 
                    - When importing Blender's built-in modules, use the following pattern:
                      ```python
                      # Example Pattern, use this for all BPY modules if possible.
                      import bpy.types as T
                      ```
                    - Use the aliased import name when referencing module components (e.g., `T.Operator` instead of `bpy.types.Operator`).
                    - Replace existing full-name references with the aliased import name.
                - **Operators**: Use spaces around operators (e.g., `1 + 1`, not `1+1`).
                - **Quotes**: 
                  - Use **single quotes** (`'`) for enums and internal identifiers.
                  - Use **double quotes** (`"`) for strings intended for the UI or end-users.
                - **Line Length**: Aim for a maximum of 79 characters per line (following PEP8).

                #### 4. Licensing
                - **Add-ons**: Must use `GPL-3.0-or-later`.
                - **Manifest Reference**: The license must be specified in `blender_manifest.toml` as `license = ["SPDX:GPL-3.0-or-later"]`.

                #### 5. Best Practices & Efficiency
                - **Error Handling**: Implement proper error handling for file I/O and network operations.
                - **Local File Storage**: When creating options relating to local file storage for the addon, use the method `U.extension_path_user(__package__, create=True, path="")` to ensure compliance with [Blender Extension guidelines](https://developer.blender.org/docs/handbook/extensions/addon_guidelines/).
                - **UI Layout**: Maintain consistent spacing and alignment as per Blender's UI guidelines.
                - **No Redundancy**: Avoid duplicate classes or functions; use utility modules for shared logic.
                - **Old Versions**: Do not reference the old version (located in `archives/`) for existing code unless explicitly asked by the user.
                - **Documentation**: Keep `CHANGELOG.md` and `README.md` updated with every significant change to the **Addon Source Code**. Updates to Agent Guidelines or other internal documentation should not be reflected in the `CHANGELOG.md`.

                #### 6. Development Workflow
                - **Logging**: Every chat session MUST be logged in `.logs/`, unless the user explicitly starts a request with "No Log". In that case, do not log the session, do not commit the associated changes, and do not mention the omission in the response.
                - **Commits**: Upon successful completion of a task, automatically commit the changes to Git. Keep commit messages brief (at most 2 full sentences) and only divulge necessary information about the changes.
                    - **No Commit**: If a request starts with "No Commit", do not commit at the end of the request.
                - **Python Style**: PEP 8 (autopep8), minimal nesting (<= 4 indents), standard library preference.
                - **Resource Management**: Download fonts and images locally (e.g., `src/fonts`) instead of using CDNs to ensure reliability and offline availability.
                - **Validation**: Before submitting, verify that the code complies with the `blender_manifest.toml` and that all class names follow the project's specific naming standards (e.g., category and function).
                - **Cleanup**: Remove unused variables, redundant parentheses, and debug print statements (use `self.report()` instead).
            """.trimIndent()
        }
    }
}
