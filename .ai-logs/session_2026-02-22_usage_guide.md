# Session Log - 2026-02-22 - Expanded Usage Guide

## Task: Expand Usage Guide into Detailed Sections

The `Usage-Guide.md` in the GitHub Wiki was expanded into comprehensive sections based on the different functions of the plugin.

## Changes:
- **`docs/wiki/Usage-Guide.md`**:
    - Rewritten with a clear, numbered structure.
    - Added **Project Lifecycle** section: Detailed information on the New Project Wizard, Manifest configuration (including Addon ID formatting and permissions), and bootstrapping options (Auto-load, Agent guidelines, Git repo).
    - Added **Environment Management** section: Documentation for the Blender Management Tool Window, version support (4.2+ and 5.0), and custom path fallback.
    - Added **Development & Testing** section: Explanation of the "Testing" run configuration, sandboxing, user configuration import, and custom splash screens.
    - Added **Hot-Reloading** section: Detailed steps for automatic and manual reloads, including an explanation of the underlying robust reload mechanism.
    - Added **CLI Tools** section: Instructions for building, validating, and running custom commands using the Blender Extensions CLI.
    - Added **Troubleshooting** section: Information on the `blender_plugin.log` and common issues (Windows symlinks, macOS extraction).

## Verification:
- Verified all described fields (e.g., in the Manifest Wizard and Run Configuration) against the actual implementation in `BlenderProjectGenerators.kt` and `BlenderSettingsEditor.kt`.
- Ensured consistency with the `Features.md` wiki page.
- Checked that all project naming conventions (`.blender-sandbox`, `pycharm` app template) are correctly referenced.

## Commit:
- Expanded the GitHub Wiki usage guide with detailed functional sections.
