### Session Test Summary (2026-02-22)

- Added unit tests covering core plugin functions.
  - BlenderProjectTemplateGeneratorTest: manifest and template generation assertions.
  - BlenderScriptGeneratorTest: startup script content and variable injection.
  - BlenderVersionsTest: constants and helper array.
  - BlenderCommunicationServiceTest: TCP server lifecycle and reload protocol.
  - BlenderDownloaderTest: download dir resolution and executable discovery (with Linux exec bit).
  - BlenderLinkerTest: sandboxed repo directory resolution.
- All tests executed successfully via Gradle.
- No production code changes were required.

Run:
- ./gradlew test

