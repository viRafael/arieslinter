# Arieslinter 🚀

A real-time static analysis linter built on the **Checkstyle** framework to proactively detect **test smells** in Java test code as they are written.

---

## 🌟 Overview

**Arieslinter** was developed as part of an academic research project at **UFBA (Universidade Federal da Bahia)**. It aims to reduce technical debt and refactoring effort by sniffing test smells proactively in developers' IDEs or CI/CD pipelines.

By integrating seamlessly with the Java ecosystem through standard Checkstyle configurations, it requires **zero changes** to your application's production source code.

---

## 🔍 Detected Test Smells

Arieslinter currently supports **19 rules** to identify test smell patterns that compromise the readability, maintainability, and quality of your test suite:

| Check Style Module | Smell Name | Description |
| :--- | :--- | :--- |
| [`AssertionRouletteTestCheck`](src/main/java/br/ufba/arieslinter/checks/AssertionRouletteTestCheck.java) | Assertion Roulette | Multiple assertions inside a test method without explanatory messages, making it hard to identify which one failed. |
| [`ConditionalTestLogicCheck`](src/main/java/br/ufba/arieslinter/checks/ConditionalTestLogicCheck.java) | Conditional Test Logic | Conditional or loop statements (`if`, `switch`, `for`, `while`) inside test methods. Tests should be linear and simple. |
| [`ConstructorInitializationCheck`](src/main/java/br/ufba/arieslinter/checks/ConstructorInitializationCheck.java) | Constructor Initialization | Test classes that define a constructor instead of using `@Before` or `@BeforeEach` for setup initialization. |
| [`DuplicateAssertCheck`](src/main/java/br/ufba/arieslinter/checks/DuplicateAssertCheck.java) | Duplicate Assert | Identical assertion expressions repeated multiple times within the same test method. |
| [`EagerTestCheck`](src/main/java/br/ufba/arieslinter/checks/EagerTestCheck.java) | Eager Test | A single test method calling multiple different production methods, violating the single responsibility principle of tests. |
| [`EmptyTestCheck`](src/main/java/br/ufba/arieslinter/checks/EmptyTestCheck.java) | Empty Test | Test methods with empty bodies or no executable statements. |
| [`ExceptionHandlingCheck`](src/main/java/br/ufba/arieslinter/checks/ExceptionHandlingCheck.java) | Exception Handling | Explicit `try-catch` blocks or explicit exceptions thrown within the test instead of letting the framework handle it. |
| [`GeneralFixtureCheck`](src/main/java/br/ufba/arieslinter/checks/GeneralFixtureCheck.java) | General Fixture | SetUp methods initializing fields/fixtures that are not actually used by all test methods in the class. |
| [`IgnoredTestCheck`](src/main/java/br/ufba/arieslinter/checks/IgnoredTestCheck.java) | Ignored Test | Test methods or classes marked with the `@Ignore` annotation. |
| [`LazyTestCheck`](src/main/java/br/ufba/arieslinter/checks/LazyTestCheck.java) | Lazy Test | Multiple test methods exercising the exact same production method with similar parameters (should be consolidated). |
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

## ⚙️ Installation

To build the Arieslinter JAR and install it into your local Maven cache (`~/.m2`):

```bash
# Clone the repository
git clone https://github.com/viRafael/arieslinter.git
cd arieslinter

# Build and install to local Maven cache
mvn clean install
```

This compiles the Checkstyle rules and produces a reusable artifact located at:
`~/.m2/repository/br/ufba/arieslinter/1.0/arieslinter-1.0.jar`

---

## 🛠️ Configuration

Create a `checkstyle.xml` configuration file at the root of the project you want to analyze:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC "-//Puppy Crawl//DTD Check Configuration 1.3//EN"
        "http://checkstyle.sourceforge.net/dtds/configuration_1_3.dtd">

<module name="Checker">
    <module name="TreeWalker">
        <!-- Register Arieslinter custom checks -->
        <module name="br.ufba.arieslinter.checks.AssertionRouletteTestCheck"/>
        <module name="br.ufba.arieslinter.checks.ConditionalTestLogicCheck"/>
        <module name="br.ufba.arieslinter.checks.ConstructorInitializationCheck"/>
        <module name="br.ufba.arieslinter.checks.DuplicateAssertCheck"/>
        <module name="br.ufba.arieslinter.checks.EagerTestCheck"/>
        <module name="br.ufba.arieslinter.checks.EmptyTestCheck"/>
        <module name="br.ufba.arieslinter.checks.ExceptionHandlingCheck"/>
        <module name="br.ufba.arieslinter.checks.GeneralFixtureCheck"/>
        <module name="br.ufba.arieslinter.checks.IgnoredTestCheck"/>
        <module name="br.ufba.arieslinter.checks.LazyTestCheck"/>
        <module name="br.ufba.arieslinter.checks.MagicNumberCheck"/>
        <module name="br.ufba.arieslinter.checks.MysteryGuestCheck"/>
        <module name="br.ufba.arieslinter.checks.RedundantAssertionCheck"/>
        <module name="br.ufba.arieslinter.checks.RedundantPrintCheck"/>
        <module name="br.ufba.arieslinter.checks.ResourceOptimismCheck"/>
        <module name="br.ufba.arieslinter.checks.SensitiveEqualityCheck"/>
        <module name="br.ufba.arieslinter.checks.SleepyTestCheck"/>
        <module name="br.ufba.arieslinter.checks.UnknownTestCheck"/>
        <module name="br.ufba.arieslinter.checks.VerboseTestCheck"/>
    </module>
</module>
```

---

## 💻 IDE Integration

### Visual Studio Code

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

---

### IntelliJ IDEA

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