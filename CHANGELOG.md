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
- `SECURITY.md`, `CITATION.cff`, `NOTICE`, issue and PR templates, and a *Known limitations* section in the README.

### Fixed
- Choosing `a` when no IDE was detected was rejected as an invalid selection; the menu now explains it and offers option `c`.
- Editors that are not in the built-in table (e.g. Antigravity) can be used through `--ide-cmd` or menu option `c`; Antigravity was also added to the table (CLI name and folders are best effort).

- Open VSX editors older than VS Code 1.144 (e.g. an outdated Code - OSS package) now get the older `ahmedsmaha.vscode-checkstyle` extension automatically, chosen by editor version or after an "not compatible" error.

### Changed
- Java 11 is the minimum version (`pom.xml` compiles with `release` 11); Checkstyle is pinned to 10.23.1 in IDE settings.
- JARs are pinned by URL and SHA-256 and stored in `~/.arieslinter` instead of overwriting `~/.m2`.
- `install-linux.py` / `install-win.py` only print instructions; they no longer download or run code.
- `.idea/` is no longer tracked; `.gitignore` expanded.
- Checkstyle dependency is now `provided` scope.

### Security
- Fail-closed checksum verification, GitHub-only downloads (redirects included), no shell execution, no writes through symlinks, timestamped backups.
