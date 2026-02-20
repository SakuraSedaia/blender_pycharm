package com.sakurasedaia.blenderextensions.project

class BlenderProjectTemplateGenerator {
    companion object {
        fun generateManifest(id: String, name: String, maintainer: String): String {
            return """
                schema_version = "1.0.0"
                id = "$id"
                version = "1.0.0"
                name = "$name"
                tagline = "A Blender extension"
                maintainer = "$maintainer"
                type = "add-on"
                tags = ["Animation", "Generic"]
                blender_version_min = "4.2.0"
                license = ["SPDX:GPL-3.0-or-later"]
            """.trimIndent()
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
                    "blender": (2, 80, 0),
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
                *.blend
                *.blend1
                *.blend2
                *.blend3
                *.blend4
                *.blend5
                *.blend6
                *.blend7
                *.blend8
                *.blend9
            """.trimIndent()
        }
        
        fun getAutoLoadContent(): String {
            val resource = BlenderProjectTemplateGenerator::class.java.getResourceAsStream("/templates/auto_load.py")
            return resource?.bufferedReader()?.use { it.readText() } ?: ""
        }
    }
}
