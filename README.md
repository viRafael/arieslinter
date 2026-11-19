# Arieslinter 🚀

A real-time static analysis linter built on the **Checkstyle** framework to proactively detect **test smells** in Java test code as they are written.

---

## 🌟 Overview

**Arieslinter** was developed as part of an academic research project at **UFBA (Universidade Federal da Bahia)**. It aims to reduce technical debt and refactoring effort by sniffing test smells proactively in developers' IDEs or CI/CD pipelines.

By integrating seamlessly with the Java ecosystem through standard Checkstyle configurations, it requires **zero changes** to your application's production source code.

---

## 🔍 Detected Test Smells

Arieslinter currently supports **17 rules** to identify test smell patterns that compromise the readability, maintainability, and quality of your test suite:

| Check Style Module | Smell Name | Description |
| :--- | :--- | :--- |
| [`AssertionRouletteTestCheck`](src/main/java/br/ufba/arieslinter/checks/AssertionRouletteTestCheck.java) | Assertion Roulette | Multiple assertions inside a test method without explanatory messages, making it hard to identify which one failed. |
| [`ConditionalTestLogicCheck`](src/main/java/br/ufba/arieslinter/checks/ConditionalTestLogicCheck.java) | Conditional Test Logic | Conditional or loop statements (`if`, `switch`, `for`, `while`) inside test methods. Tests should be linear and simple. |
| [`ConstructorInitializationCheck`](src/main/java/br/ufba/arieslinter/checks/ConstructorInitializationCheck.java) | Constructor Initialization | Test classes that define a constructor instead of using `@Before` or `@BeforeEach` for setup initialization. |
| [`DuplicateAssertCheck`](src/main/java/br/ufba/arieslinter/checks/DuplicateAssertCheck.java) | Duplicate Assert | Identical assertion expressions repeated multiple times within the same test method. |
| [`EmptyTestCheck`](src/main/java/br/ufba/arieslinter/checks/EmptyTestCheck.java) | Empty Test | Test methods with empty bodies or no executable statements. |
| [`ExceptionHandlingCheck`](src/main/java/br/ufba/arieslinter/checks/ExceptionHandlingCheck.java) | Exception Handling | Explicit `try-catch` blocks or explicit exceptions thrown within the test instead of letting the framework handle it. |
| [`GeneralFixtureCheck`](src/main/java/br/ufba/arieslinter/checks/GeneralFixtureCheck.java) | General Fixture | SetUp methods initializing fields/fixtures that are not actually used by all test methods in the class. |
| [`IgnoredTestCheck`](src/main/java/br/ufba/arieslinter/checks/IgnoredTestCheck.java) | Ignored Test | Test methods or classes marked with the `@Ignore` annotation. |
| [`MagicNumberCheck`](src/main/java/br/ufba/arieslinter/checks/MagicNumberCheck.java) | Magic Number | Unnamed numerical literals used directly in test assertions. Use named constants or descriptive variables instead. |
| [`MysteryGuestCheck`](src/main/java/br/ufba/arieslinter/checks/MysteryGuestCheck.java) | Mystery Guest | Tests that depend on external resources (e.g., local files, databases, network connections) rather than isolated fixtures. |
| [`RedundantAssertionCheck`](src/main/java/br/ufba/arieslinter/checks/RedundantAssertionCheck.java) | Redundant Assertion | Assertions that will always pass or always fail (e.g., `assertTrue(true)` or comparing identical constants). |
| [`RedundantPrintCheck`](src/main/java/br/ufba/arieslinter/checks/RedundantPrintCheck.java) | Redundant Print | Leftover print statements (`System.out.println`, etc.) inside test methods that clutter the console output. |
| [`ResourceOptimismCheck`](src/main/java/br/ufba/arieslinter/checks/ResourceOptimismCheck.java) | Resource Optimism | Using files or external resources in tests without checking if they exist first. |
| [`SensitiveEqualityCheck`](src/main/java/br/ufba/arieslinter/checks/SensitiveEqualityCheck.java) | Sensitive Equality | Using `toString()` output for object comparison in assertions instead of proper equality methods. |
| [`SleepyTestCheck`](src/main/java/br/ufba/arieslinter/checks/SleepyTestCheck.java) | Sleepy Test | Explicit usage of `Thread.sleep(...)` inside test methods, causing tests to run slowly and introducing flakiness. |
| [`UnknownTestCheck`](src/main/java/br/ufba/arieslinter/checks/UnknownTestCheck.java) | Unknown Test | A test method that completes without executing any assertions. |
| [`VerboseTestCheck`](src/main/java/br/ufba/arieslinter/checks/VerboseTestCheck.java) | Verbose Test | Test methods that are excessively long, making them difficult to read and maintain. |

---

## ⚙️ Installation & Setup

**Requirements:** Python 3.8+ (installer only), Java **11 or newer** and one of the supported IDEs below. Arieslinter is a set of checks that runs *inside Checkstyle*, so any tool that runs Checkstyle with third-party modules can use it.

| IDE | Extension / plugin used by the installer | Marketplace |
| :--- | :--- | :--- |
| VS Code, VS Code Insiders | [Checkstyle for Java](https://marketplace.visualstudio.com/items?itemName=shengchen.vscode-checkstyle) (`shengchen.vscode-checkstyle`) | Microsoft |
| Code - OSS, VSCodium, Cursor, Windsurf, Antigravity | [Checkstyle for VS Code](https://open-vsx.org/extension/a-st/checkstyle-vscode) (`a-st.checkstyle-vscode`) | Open VSX |
| IntelliJ IDEA | [CheckStyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea) | JetBrains |
| Any other VS Code based editor | Give its CLI command (menu option `c` or `--ide-cmd`); the installer uses Open VSX unless it detects the Microsoft marketplace | Open VSX |
| Any other / CI | Checkstyle command line (see [Command line](#-command-line-any-ide-or-ci)) | - |

> [!IMPORTANT]
> The Microsoft extension `shengchen.vscode-checkstyle` is **not published on Open VSX**, so Code - OSS, VSCodium and similar editors cannot install it. The installer picks the right extension for each editor. Both extensions use the same `java.checkstyle.*` settings. Note: the Open VSX extension's own README currently asks for a recent JDK (25) to run inside the editor, while the command line and the Microsoft extension work with Java 11+.

### 🚀 Option 1: Quick Automated Install (Recommended)

The installer detects your IDEs, lets you choose which to configure, downloads the Arieslinter and Checkstyle JARs (SHA-256 verified), writes `checkstyle.xml`, configures the IDE, and **finishes by running Checkstyle on a sample file to prove the checks actually work**. Its final summary lists what succeeded, what only produced a warning and what failed; it exits with a non-zero code if anything failed.

Download the script, **read it**, then run it. It only talks to GitHub, and every file it downloads is pinned by SHA-256.

#### 🐧 Linux & macOS
```bash
curl -sSL -o install.py https://raw.githubusercontent.com/viRafael/arieslinter/main/install.py
python3 install.py
```

#### 🪟 Windows (PowerShell or CMD)
```powershell
curl.exe -sSL -o install.py https://raw.githubusercontent.com/viRafael/arieslinter/main/install.py
py install.py
```

> [!TIP]
> Use `curl.exe` (not `curl`): in Windows PowerShell 5.1 `curl` is an alias of `Invoke-WebRequest`. If `py` is not found, use `python` (and, if it opens the Microsoft Store, install Python from python.org).

> [!NOTE]
> To pin the installer itself to a reviewed version, replace `main` in the URL with a release tag or a commit SHA. The older `install-linux.py` / `install-win.py` links now only print these instructions.

#### Non-interactive options
```bash
python3 install.py --project /path/to/project --ide code,cursor --yes
```
| Flag | Meaning |
| :--- | :--- |
| `--project DIR` | Project directory (skips the prompt). |
| `--ide LIST` | `all`, `none` or ids: `code`, `code-insiders`, `code-oss`, `vscodium`, `cursor`, `windsurf`, `antigravity`, `intellij`. |
| `--ide-cmd CMD` | Any other VS Code based editor: its CLI command or full path (e.g. `--ide-cmd antigravity`). In the interactive menu use option `c`. |
| `--yes` | Install missing IDE extensions without asking. |
| `--no-extensions` | Never install extensions, only check them. |
| `--force-xml` | Overwrite an existing `checkstyle.xml` (a timestamped backup is kept). |
| `--uninstall` | Remove what the installer added to `--project` (settings entries and an unmodified `checkstyle.xml`). |
| `--purge` | With `--uninstall`, also delete the downloaded JARs in `~/.arieslinter`. |

> [!NOTE]
> The JARs are saved in `~/.arieslinter` (override with the `ARIESLINTER_HOME` environment variable), so they never overwrite a build you installed with Maven. All Checkstyle engines used by the IDE plugins must be the same version the JAR was built with. The installer pins **Checkstyle 10.23.1** in the IDE settings, because the extensions' own defaults differ (e.g. 9.3 for the Microsoft extension, 14.3.0 for the Open VSX one).

---

### 🛠️ Option 2: Manual Installation & Setup

If you prefer to compile the JAR and configure your environment manually, follow these steps:

#### 1. Build and install the Arieslinter JAR
Build the Arieslinter JAR and install it into your local Maven cache (`~/.m2`):
```bash
# Clone the repository
git clone https://github.com/viRafael/arieslinter.git
cd arieslinter

# Build and install to local Maven cache
mvn clean install
```

This compiles the Checkstyle rules and produces a reusable artifact located at:
`~/.m2/repository/br/ufba/arieslinter/1.0/arieslinter-1.0.jar`

#### 2. Create the `checkstyle.xml` configuration file
Create a `checkstyle.xml` configuration file at the root of the project you want to analyze with the following content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC "-//Puppy Crawl//DTD Check Configuration 1.3//EN"
        "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <module name="TreeWalker">
        <!-- Register Arieslinter custom checks -->
        <module name="br.ufba.arieslinter.checks.AssertionRouletteTestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.ConditionalTestLogicCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.ConstructorInitializationCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.DuplicateAssertCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.EmptyTestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.ExceptionHandlingCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.GeneralFixtureCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.IgnoredTestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.MagicNumberCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.MysteryGuestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.RedundantAssertionCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.RedundantPrintCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.ResourceOptimismCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.SensitiveEqualityCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.SleepyTestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.UnknownTestCheck"><property name="severity" value="warning"/></module>
        <module name="br.ufba.arieslinter.checks.VerboseTestCheck"><property name="severity" value="warning"/></module>
    </module>
</module>
```

#### 3. IDE Integration

##### Visual Studio Code
1. Install the [Checkstyle for Java](https://marketplace.visualstudio.com/items?itemName=shengchen.vscode-checkstyle) extension.
2. Open your VS Code `settings.json` and add:
   ```json
   "java.checkstyle.configuration": "${workspaceFolder}/checkstyle.xml",
   "java.checkstyle.autocheck": true,
   "java.checkstyle.modules": [
       "/home/{user}/.m2/repository/br/ufba/arieslinter/1.0/arieslinter-1.0.jar"
   ]
   ```
   > 💡 **Tip:** Replace `{user}` with your user directory path.
   > - **Linux/macOS:** `/home/username/.m2/repository/...`
   > - **Windows:** `C:/Users/username/.m2/repository/...`

##### IntelliJ IDEA
1. Install the [CheckStyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea) plugin.
2. Navigate to **Settings** (or **Preferences** on macOS) → **Tools** → **Checkstyle**.
3. Under **Configuration File**, click the `+` button and configure:
   - **Description:** `Arieslinter`
   - **Use a local Checkstyle file:** Browse to your `checkstyle.xml`.
   - **Scan Scope:** `All files in project`.
4. Under **Third-Party Checks**, click the `+` button and add the path to the Arieslinter JAR:
   - **Linux/macOS:** `/home/{user}/.m2/repository/br/ufba/arieslinter/1.0/arieslinter-1.0.jar`
   - **Windows:** `C:\Users\{user}\.m2\repository\br\ufba\arieslinter\1.0\arieslinter-1.0.jar`

---


## ⚠️ Known limitations

The detectors are static heuristics. Keep these points in mind before using the results in a study:

- Tests are recognised by the simple name of their annotation (`@Test`, ...). `@org.junit.Test` written fully qualified, JUnit 3 (`extends TestCase`) and TestNG are not recognised.
- *Ignored Test* only detects JUnit 4 `@Ignore`; JUnit 5 `@Disabled` is **not** detected.
- *Verbose Test* uses a fixed threshold of 30 lines and its `max` property cannot be configured.
- Several checks have known false positives (e.g. Magic Number flags `0`/`1`, Mystery Guest flags `@TempDir`, Assertion Roulette counts non-literal messages as missing).
- The tool has 17 checks; the SBES 2025 paper describes an earlier version with 10 (it includes *Default Test*, which is not implemented here).
- `tests/expected.txt` is a snapshot of the tool's own behavior, not an independent ground truth.

## 🩺 Troubleshooting

| Symptom | Cause / fix |
| :--- | :--- |
| `Java ... does not run` (macOS) | `/usr/bin/java` is only a stub. Install a JDK 11+ (e.g. Temurin) and run again. |
| `CERTIFICATE_VERIFY_FAILED` | macOS python.org builds: run `Install Certificates.command`. Behind a corporate proxy set `SSL_CERT_FILE`. The installer already retries with `curl`. |
| `checksum mismatch` | The download was corrupted or tampered with; it was discarded. Try again, and open an issue if it persists. |
| `Failed to install ... not compatible` | The default Open VSX extension needs an editor based on VS Code 1.144+. The installer then uses an older extension (`ahmedsmaha.vscode-checkstyle`, editors 1.64+) automatically; updating the editor is still recommended. |
| Extension cannot download Checkstyle 10.23.1 | Set `java.checkstyle.version` to `9.3`, the version bundled with the older extension (it gives the same results on the test corpus). |
| `Unable to instantiate` / `NoClassDefFoundError` | The IDE uses a different Checkstyle version or the JAR path is wrong. Set `java.checkstyle.version` to `10.23.1` and check `java.checkstyle.modules`. |
| `No interactive terminal available` | Run the downloaded script directly or pass `--project` and `--ide`. |
| No warnings in the IDE | Make sure the file is a Java *test* (annotated with `@Test`), the Java extension is enabled, and `checkstyle.xml` lists the Arieslinter modules. |

To remove everything: `python3 install.py --uninstall --project /path/to/project --purge`.

## 💻 Command line (any IDE or CI)

Use the same engine the IDE plugins use, without any IDE:

```bash
java -cp checkstyle-10.23.1-all.jar:arieslinter-1.0.jar \
  com.puppycrawl.tools.checkstyle.Main -c checkstyle.xml src/test
```
(On Windows use `;` instead of `:`.) The Checkstyle "all" JAR is available at <https://github.com/checkstyle/checkstyle/releases>.

## ✅ Tests: same detection in every environment

The detection is performed by the Checkstyle engine, not by the IDE plugin, so the result depends only on the Checkstyle and Java versions. `tests/` contains a corpus of test classes with known smells (JUnit 4, JUnit 5 and Java 17 syntax) and the expected findings:

```bash
python3 tests/check_corpus.py --java java \
  --checkstyle-jar checkstyle-10.23.1-all.jar --arieslinter-jar arieslinter-1.0.jar
python3 -m unittest discover -s tests -p "test_*.py"   # installer unit tests
```

The same 33 findings were obtained with Checkstyle 8.45.1, 9.3, 10.0, 10.12.5, 10.23.1, 11.1.0, 12.3.0, 13.0.0 and 14.3.0 on Java 11, 17 and 25.

---

## 🎓 Research & Citation

Arieslinter is an academic tool developed at **UFBA (Universidade Federal da Bahia)**. If you use this tool or its artifacts in your academic work, please cite the paper accepted at **SBES 2025 (Brazilian Symposium on Software Engineering / CBSOFT)**:

- **Paper Title:** *AriesLinter: Sniffing Test Smells Before They Happen*
- 📄 [Read the Paper (Google Drive)](https://drive.google.com/file/d/1SQ5c2_XlFVYEGTERW3FSlOD4qIpctqsD/view)
- 💾 [Research Artifact on Zenodo](https://zenodo.org/records/16998677)

### BibTeX
```bibtex
@inproceedings{arieslinter2025sbes,
  title     = {AriesLinter: Sniffing Test Smells Before They Happen},
  author    = {Rocha, Rafael and Junior, Eronildo and Virg{\'i}nio, T{\'a}ssio and Rocha, Larissa and Bezerra, Carla and Machado, Ivan},
  booktitle = {Proceedings of the XXXIX Brazilian Symposium on Software Engineering (SBES 2025)},
  year      = {2025},
  publisher = {SBC}
}
```