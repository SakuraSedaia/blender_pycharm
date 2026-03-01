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
                - **Local File Storage**: When creating options relating to local file storage for the addon, use the method `bpy.utils.extension_path_user(__package__, create=True, path="")` to ensure compliance with [Blender Extension guidelines](https://developer.blender.org/docs/handbook/extensions/addon_guidelines/).
                - **UI Layout**: Maintain consistent spacing and alignment as per Blender's UI guidelines.
                - **No Redundancy**: Avoid duplicate classes or functions; use utility modules for shared logic.
                - **Documentation**: Keep `CHANGELOG.md` and `README.md` updated with every significant change to the **Addon Source Code**. Updates to Agent Guidelines or other internal documentation should not be reflected in the `CHANGELOG.md`.

                #### 6. Development Workflow
                - **Local Logging**: Chat sessions may be logged locally for personal and agent reference (e.g., in `.ai-logs/`). These logs should NOT be committed to the repository.
                - **Commits**: Upon successful completion of a task, automatically commit the changes to Git. 
                    - **Message**: Keep commit messages brief (at most 2 full sentences) and only divulge necessary information about the changes.
                    - **No Commit**: If a request starts with "No Commit", do not commit at the end of the request.
                    - **Prefix**: Prefix commit messages with the task type (e.g., "feat:", "fix:", "docs:") and a brief description.
                - **Python Style**: PEP 8 (autopep8), minimal nesting (<= 4 indents), standard library preference.
                - **Resource Management**: Download fonts and images locally (e.g., `src/fonts`) instead of using CDNs to ensure reliability and offline availability.
                - **Validation**: Before submitting, verify that the code complies with the `blender_manifest.toml` and that all class names follow the project's specific naming standards (e.g., category and function), run `blender --command extension validate` to ensure code runs without errors in Blender.
                - **Cleanup**: Remove unused variables, redundant parentheses, and debug print statements (use `self.report()` instead).