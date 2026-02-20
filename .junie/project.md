# Project Overview

## Goal
- Provide a first-class development experience for Blender Extensions (4.2+) within PyCharm/IntelliJ, including reliable hot-reloading and project management.

## Current State
- Hot-Reloading: Implemented via structured JSON protocol with the sequence "disable -> purge -> refresh -> enable" to ensure re-imports.
- Project Linking: Automatically links local source directories to Blender's extensions repository.
- Blender Support: Compatible with Blender 4.2+ and 5.0. Supports automatic downloading and sandboxing of Blender versions with isolated app templates and user directories.
- UI: Custom Run Configuration for Blender with version selection and sandboxing toggles.
- Project Template: Provides a single template mirroring PyCharm’s “Pure Python” setup. Includes a “Project name” field that auto-formats (preserving capitals, hyphenating spaces) to drive the folder and addon metadata. The field is synchronized with the platform's Location field. A checkbox allows optionally enabling Auto-load (adds `auto_load.py` and an autoload-ready `__init__.py`). Manual setup instructions for the Python interpreter and linting stubs are provided in the generated `README.md`. Automatically creates a pre-configured Blender Run Configuration (targeting Blender 5.0 with sandboxing) on project creation.

## Core Architecture
- IntelliJ Platform plugin (Kotlin) for Blender extension development and hot reloading. 
- Service-oriented architecture:
    - `BlenderService`: Project-level facade for Blender operations.
    - `BlenderDownloader`: Application-level management of Blender downloads and version selection.
    - `BlenderLinker`: Project-level management of symbolic links and junctions for extension sources.
    - `BlenderLauncher`: Project-level management of Blender process startup and sandboxing.
    - `BlenderCommunicationService`: Project-level TCP server for hot-reloading command protocol.
    - `BlenderScriptGenerator`: Application-level generator of Blender-side Python scripts.
    - `BlenderLogger`: Project-level custom logger with file logging support.
- Custom Run Configurations for launching Blender and common extension operations (validate, build).
- Project Templates: `DirectoryProjectGenerator` implementations for quick project setup.

## Key Files
- `src/main/kotlin/com/sakurasedaia/blenderextensions/blender/BlenderService.kt`: Core project facade.
- `src/main/kotlin/com/sakurasedaia/blenderextensions/blender/BlenderCommunicationService.kt`: Hot-reloading protocol server.
- `src/main/kotlin/com/sakurasedaia/blenderextensions/run/BlenderRunConfiguration.kt`: UI and entry point for launching Blender.
- `src/main/kotlin/com/sakurasedaia/blenderextensions/run/BlenderRunProfileState.kt`: Execution logic for Blender run configurations.
- `src/main/kotlin/com/sakurasedaia/blenderextensions/project/BlenderProjectGenerators.kt`: Logic for new project templates.
- `.ai-logs/`: Historical logs and summaries of development sessions.
- `.reference/blender_vscode`: Reference of the VS Code Blender extension.
