# Git Management Skill

## Objectives
- Maintain a clean, readable, and atomic version history.
- Ensure all contributors and agents follow the same version control protocol.

## Protocol
- **Atomic Commits**: Each commit should represent a single, logical change.
- **Auto-Commit**: Commit changes automatically upon successful task completion unless "No Commit" is specified.
- **Co-Author Trailer**: Always include `Co-authored-by: Junie <junie@jetbrains.com>` at the end of commit messages.

## Standardized Prefixes
Use these prefixes for all commit messages:
- `feat:`: New feature.
- `fix:`: Bug fix.
- `docs:`: Documentation changes.
- `style:`: Formatting, missing semi colons, etc; no code change.
- `refactor:`: Refactoring production code.
- `test:`: Adding missing tests, refactoring tests; no production code change.
- `chore:`: Updating build tasks, package manager configs, etc; no production code change.

## Message Format
- Keep messages brief (max 2 sentences).
- Only include essential information about the changes.
