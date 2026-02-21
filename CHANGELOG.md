# Changelog

## [1.0.0] - 2026-02-20
- Initial release of Blender Extension Development for PyCharm.
- Implemented Blender Run Configuration with automatic download and sandboxing.
- Reorganized Blender Run Configurations into four specific templates: Start Blender, Build, Validate, and Command.
- Updated Run Configuration UI to dynamically hide/show fields based on the selected mode.
- Set Blender 5.0 as default version in run configurations.
- Added project templates for simple and autoload-ready addons.
- Added robust hot-reloading mechanism via structured JSON protocol.
- Automatically include Start Blender, Build, and Validate configs in new projects.
- Added "Blender Management" Tool Window to manage global installations and project sandboxes.
