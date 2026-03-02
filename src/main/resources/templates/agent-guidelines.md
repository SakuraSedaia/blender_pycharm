# Agent Coding Guidelines

This document serves as the primary entry point for AI agents working on this Blender Extension project. It outlines the core standards and references specialized skills for detailed procedures.

## Core Skills
These skills are synchronized across the project to ensure consistent agent behavior.

- **Blender Extension Dev**: [`.agent/skills/blender_extension_dev.md`](skills/blender_extension_dev.md)
- **Python Practices**: [`.agent/skills/python_practices.md`](skills/python_practices.md)
- **Git Management**: [`.agent/skills/git_management.md`](skills/git_management.md)
- **AI Workflow**: [`.agent/skills/ai_workflow.md`](skills/ai_workflow.md)

## Quick Reference
1. **Always** check `.agent/project.md` for current task status.
2. **Never** use wildcard imports (e.g., `from bpy.types import *`).
3. **Always** use standardized commit prefixes (e.g., `feat:`, `fix:`).
4. **Always** include the co-author trailer in commits.
5. **Always** validate code using `blender --command extension validate`.