# Security policy

## Reporting a vulnerability

Please do **not** open a public issue. Use GitHub's private reporting: *Security → Report a vulnerability* in this repository, and describe the problem and how to reproduce it. We will answer as soon as we can.

## What the installer does and does not trust

`install.py` is meant to be downloaded, read and then run. Its security model:

- **Pinned artifacts.** The Arieslinter JAR (release tag, URL and SHA-256) and the Checkstyle JAR (URL and SHA-256) are constants in `install.py`. A file whose hash differs is discarded, and a download without a pinned hash is refused. Nothing is executed before it has been verified.
- **GitHub only.** Downloads are restricted to `https` on `github.com` and `githubusercontent.com`, including redirects.
- **No shell.** IDE command lines are started by absolute path without a shell, and profile names read from IDE files are validated first.
- **No writes through symlinks.** `checkstyle.xml`, `.vscode/settings.json` and `.idea/checkstyle-idea.xml` are never written through a symlink, are backed up (timestamped) before being changed, and a file that cannot be parsed is left untouched.
- **No remote code at runtime.** The old `install-linux.py` / `install-win.py` no longer download or execute anything.

Limits you should know about:

- The pins live in the same repository as the script, so they protect against a tampered or corrupted *release asset*, not against a compromised repository. To pin the installer itself, download it from a release tag or a commit SHA instead of `main`.
- `java.checkstyle.modules` makes your IDE load a JAR on every start. Anyone who can write the JAR (`~/.arieslinter`) or the project's `.vscode/settings.json` can run code as you. Do not commit that settings file, and open only projects you trust.
- The installer cannot verify what happens inside your editor; it only proves, from the command line, that Java + Checkstyle + Arieslinter work together.
