# Contributing

Thanks for helping to improve Arieslinter.

## Setup
- Java 11+ and Maven 3.9+ to build the checks (`mvn clean package`).
- Python 3.8+ for the installer and tests.

## Before opening a pull request
1. Run the installer unit tests: `python3 -m unittest discover -s tests -p "test_*.py"`.
2. Build the JAR and run the corpus check against it:
   `python3 tests/check_corpus.py --java java --checkstyle-jar checkstyle-10.23.1-all.jar --arieslinter-jar target/arieslinter-1.0.jar`
3. If you changed a check **on purpose**, regenerate `tests/expected.txt` with `--update` and explain every changed line in the PR. Add a positive *and* a negative (borderline) case to `tests/corpus/` for the check you touched.
4. Update `CHANGELOG.md`, and the *Known limitations* section of the README if behavior changed.

## Commit messages
Use [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `docs:`, `test:`, `chore:`) with a message that says what changed.

## Reporting problems
Use the issue templates. For security issues see `SECURITY.md`.
