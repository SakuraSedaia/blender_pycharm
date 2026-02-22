### Blender File Icons and UI Cleanup

A new icon association for '.blend' and temp blend files ('.blend1', '.blend2', etc.) has been added using the 'blender-color.svg' icon. Additionally, the 'AddonSrcFolder' code has been removed while keeping the vector graphics for future implementation.

#### Changes Made:
- **BlenderIcons.kt**: Removed 'AddonSrcFolder' property and corrected 'BlenderColor' icon path to '/icons/blender-color.svg'.
- **BlenderIconProvider.kt**: Removed the logic for identifying directories with 'blender_manifest.toml' and added new logic to associate '.blend' and backup files with the 'BlenderColor' icon using a robust name-based check.
- **Documentation**: Updated 'README.md' and 'docs/wiki/Features.md' to reflect the removal of automated folder icon detection and the addition of '.blend' file icon support.
- **Build**: Verified that the project builds successfully.
