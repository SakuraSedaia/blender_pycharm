# Git Management Skill

## Objectives
- Maintain a clean and atomic commit history.
- Ensure all commits follow the project's standardized prefix system.
- Correctly attribute contributions to Junie using co-author trailers.

## Procedures
1. **Commit Prefixes**: Always use one of the approved prefixes:
   - `[Fix]`, `[Feature]`, `[Refactor]`, `[Removal]`, `[Chore]`, `[Docs]`, `[Test]`, `[Style]`.
2. **Co-author Trailer**: Append `--trailer "Co-authored-by: Junie <junie@jetbrains.com>"` to every commit command.
3. **Atomic Commits**: Each commit should represent a single logical change. If multiple distinct tasks are performed, commit them separately.
4. **No Premature Versioning**: NEVER bump the plugin version unless explicitly instructed by the user.

## Verification
- Run `git log -1` after a commit to verify the message and trailer format.
