# Documentation & Wiki Skill

## Objectives
- Keep internal and external documentation synchronized.
- Ensure all technical documentation accurately reflects the current state of the codebase.
- Maintain a high standard for RST and Markdown formatting.

## Internal Documentation
1. **README.md**: Central source of truth for users. Update it whenever features change.
2. **CHANGELOG.md**: Record all non-trivial changes under the appropriate (or new) version header.
3. **CONTRIBUTING.md**: Update developer-facing instructions when tools or workflows change.
4. **.agent/project.md**: Maintain the high-level architecture and task status for future sessions.

## Project Path Guidelines
- **Sphinx Wiki**: Whenever referencing the "sphinx wiki", it always refers to `C:\Users\Sakura\Documents\PycharmProjects\PycharmBlenderWiki`.
- **General Projects**: All projects under `PycharmProjects` (including the wiki) will always exist in `$USER_HOME\Documents\PycharmProjects`.

## External Wiki (Sphinx/RST)
1. **Source Location**: See **Project Path Guidelines** above.
2. **Context**: Use `.agent/wiki_guidelines.md` for Sphinx/RST specific formatting rules.
3. **Authorization**: Editing the wiki's source code is allowed only when authorized.
4. **Syncing**: When updating internal docs that affect users, provide a `WIKI_UPDATES.md` file or directly update the `PycharmBlenderWiki` if authorized.

## Maintenance Procedures
- Check all internal and external links after updates.
- Verify Markdown and RST syntax using appropriate tools or manual inspection.
