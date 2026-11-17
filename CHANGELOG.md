# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/) and the project aims at [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Single cross-platform installer `install.py` (Linux, macOS, Windows) that detects VS Code, VS Code Insiders, Code - OSS, VSCodium, Cursor, Windsurf and IntelliJ.
- Marketplace-aware extension selection (Open VSX editors use `a-st.checkstyle-vscode`).
- Final verification that runs Checkstyle + Arieslinter on a sample file, and an honest summary with a non-zero exit code on failure.
- `--uninstall`, `--purge`, `--project`, `--ide`, `--yes`, `--no-extensions`, `--force-xml` options.
- Test corpus (`tests/`) and unit tests for the installer.
- `SECURITY.md`, `CITATION.cff`, `NOTICE`, `docs/LIMITATIONS.md`, `docs/PAPER-VS-TOOL.md`, `docs/RELEASING.md`, issue and PR templates.

### Changed
- Java 11 is the minimum version (`pom.xml` compiles with `release` 11); Checkstyle is pinned to 10.23.1 in IDE settings.
- JARs are pinned by URL and SHA-256 and stored in `~/.arieslinter` instead of overwriting `~/.m2`.
- `install-linux.py` / `install-win.py` only print instructions; they no longer download or run code.
- `.idea/` is no longer tracked; `.gitignore` expanded.
- Checkstyle dependency is now `provided` scope.

### Security
- Fail-closed checksum verification, GitHub-only downloads (redirects included), no shell execution, no writes through symlinks, timestamped backups.
