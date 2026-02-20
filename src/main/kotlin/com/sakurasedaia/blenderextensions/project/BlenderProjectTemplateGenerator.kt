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

                1.  **Create a Virtual Environment**:
                    - Open the terminal in the project root.
                    - Run `python -m venv .venv` (or `python3 -m venv .venv`).
                2.  **Install Linting Stubs**:
                    - Run `.venv/bin/pip install fake-bpy-module --target .linting` (Linux/macOS) or `.venv\Scripts\pip install fake-bpy-module --target .linting` (Windows).
                3.  **Configure PyCharm Interpreter**:
                    - Go to **File > Settings > Project > Python Interpreter**.
                    - Click **Add Interpreter > Add Local Interpreter...**.
                    - Select **Existing** and point it to `.venv/bin/python` (Linux/macOS) or `.venv\Scripts\python.exe` (Windows).
                4.  **Add Linting to Interpreter Paths**:
                    - In the **Python Interpreter** settings, click the gear icon (or the three dots) and select **Show All...**.
                    - Select your interpreter and click the **Show paths for the selected interpreter** icon.
                    - Click **+** and add the `.linting` folder from your project root. This ensures you get code completion for `bpy`.

                ## Development

                1.  Make sure you have Blender 4.2 or later installed.
                2.  Configure the Blender Run Configuration in PyCharm to point to your Blender executable.
                3.  Run the configuration to start Blender with your extension enabled.

                ## Building and Validating

                You can use the provided Run Configurations to validate and build your extension:
                - **Validate Extension**: Runs `blender --extension validate` on your source.
                - **Build Extension**: Runs `blender --extension build` to create a `.zip` package.

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
