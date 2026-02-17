#  Arieslinter

A static analysis linter for detecting **test smells** in Java test code.

> 📄 Accepted at **CBSOFT 2025** — [Read the paper](https://drive.google.com/file/d/1SQ5c2_XlFVYEGTERW3FSlOD4qIpctqsD/view) · [Artifact on Zenodo](https://zenodo.org/records/16998677)

---

## What It Does

Arieslinter analyzes Java test code and reports occurrences of test smells - patterns that reduce the quality, readability, and maintainability of test suites. It integrates with existing Java tooling via Checkstyle, requiring no changes to your build process.

### Detected Test Smells

| Check | Description |
|-------|-------------|
| `AssertionRouletteTestCheck` | Multiple assertions without explanatory messages |
| `ConditionalTestLogicCheck` | Conditional/loop logic inside test methods |
| `ConstructorInitializationCheck` | Test class defines a constructor instead of using `@Before` |
| `DefaultTestCheck` | Default auto-generated test classes left unrenamed |
| `DuplicateAssertCheck` | Same assertion repeated multiple times in a test |
| `EagerTestCheck` | Test method calls multiple different production methods |
| `EmptyTestCheck` | Test method with no executable statements |
| `ExceptionHandlingCheck` | Use of try/catch/finally inside test methods |
| `GeneralFixtureCheck` | `setUp()` initializes fields not used by all tests |
| `IgnoredTestCheck` | Tests annotated with `@Ignore` |
| `LazyTestCheck` | Multiple tests calling the same production method |
| `MagicNumberCheck` | Unexplained numeric literals inside test assertions |
| `MysteryGuestCheck` | Test uses external resources (files, database, network) |
| `RedundantAssertionCheck` | Assertion that always passes or always fails |
| `RedundantPrintCheck` | Print statements left in test methods |
| `ResourceOptimismCheck` | File used without checking existence first |
| `SensitiveEqualityCheck` | Use of `toString()` in assertions |
| `SleepyTestCheck` | Use of `Thread.sleep()` inside tests |
| `UnknownFixtureCheck` | Test method without any assertion |
| `VerboseTestCheck` | Overly long test methods |

---

## Installation

Install Arieslinter into your local Maven repository:

```bash
git clone https://github.com/viRafael/arieslinter.git
cd arieslinter
mvn clean install
```
---

## Configuration

Add a `checkstyle.xml` file to your project root:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC "-//Puppy Crawl//DTD Check Configuration 1.3//EN"
        "http://checkstyle.sourceforge.net/dtds/configuration_1_3.dtd">

<module name="Checker">
    <module name="TreeWalker">
        <module name="br.ufba.arieslinter.checks.AssertionRouletteTestCheck"/>
        <module name="br.ufba.arieslinter.checks.ConditionalTestLogicCheck"/>
        <module name="br.ufba.arieslinter.checks.ConstructorInitializationCheck"/>
        <module name="br.ufba.arieslinter.checks.DefaultTestCheck"/>
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
        <module name="br.ufba.arieslinter.checks.UnknownFixtureCheck"/>
        <module name="br.ufba.arieslinter.checks.VerboseTestCheck"/>
    </module>
</module>
```

---

## IDE Integration

### Visual Studio Code

1. Install the [Checkstyle for Java](https://marketplace.visualstudio.com/items?itemName=shengchen.vscode-checkstyle) extension
2. Add the following to your `settings.json`:

```json
"java.checkstyle.configuration": "${workspaceFolder}/checkstyle.xml",
"java.checkstyle.autocheck": true,
"java.checkstyle.modules": ["/home/{user}/.m2/repository/br/ufba/arieslinter/1.0/arieslinter-1.0.jar"]
```

> Replace `{user}` with your system username. Example: `/home/rafael/`

---

### IntelliJ IDEA

1. Install the [CheckStyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea) plugin
2. Open **Settings → Tools → Checkstyle**
3. Under **Configuration File**, click `+` and fill in:
   - **Description:** Arieslinter
   - **Use a local Checkstyle file:** path to your `checkstyle.xml`
   - **Scan Scope:** All files in project
4. Under **Third-Party Checks**, click `+` and add the path to the JAR:
   ```
   /home/{user}/.m2/repository/br/ufba/arieslinter/1.0/arieslinter-1.0.jar
   ```

---

## Research

Arieslinter was developed as part of an academic research project at **UFBA (Universidade Federal da Bahia)**. The accompanying paper was accepted at **CBSOFT 2025**, presenting the tool's architecture, the implemented checks, and experimental results from real-world Java projects.

- 📄 [Read the paper (Google Drive)](https://drive.google.com/file/d/1SQ5c2_XlFVYEGTERW3FSlOD4qIpctqsD/view)
- 📦 [Artifact on Zenodo](https://zenodo.org/records/16998677)