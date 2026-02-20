# Project Overview - Blender PyCharm Plugin

## Goal
To provide a first-class development experience for Blender Extensions (4.2+) within PyCharm/IntelliJ, including reliable hot-reloading and project management.

## Current State
- **Hot-Reloading**: Fully implemented using a structured JSON-based communication protocol. Supports "disable -> purge -> refresh -> enable" for guaranteed module re-imports.
- **Project Linking**: Automatically links local source directories to Blender's extensions repository.
- **Blender Support**: Compatible with Blender 4.2+ and 5.0.

## Key Files
- `src/main/kotlin/com/sakurasedaia/blenderextensions/blender/BlenderService.kt`: Core logic for communication and process management.
- `.ai-logs/`: Historical logs and summaries of development sessions.
- `.reference/blender_vscode`: Reference implementation of the VS Code Blender extension.
