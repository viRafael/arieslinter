# Known limitations

Arieslinter uses static, syntactic heuristics on the Checkstyle AST. The items below were
found by an internal review that ran the checks (Checkstyle 10.23.1) on small examples. They
affect precision/recall and should be reported in any evaluation. This document describes the
**current behavior**; it is not a promise of future fixes.

## Which methods count as tests
- A test is recognised only by the **simple name** of its annotation. `@org.junit.Test` written fully qualified is not recognised, and **no check reports anything** for such a method.
- JUnit 3 (`extends TestCase`, `testXxx` methods) and TestNG are not supported (TestNG `@Test` works only when not qualified; `@Test(enabled = false)` is not treated as ignored).
- `SleepyTest` only looks at `@Test`: a sleep inside `@ParameterizedTest` or `@RepeatedTest` is not reported, nor are `TimeUnit.X.sleep(...)` or a statically imported `sleep`.
- `@TestFactory` methods are treated as tests, so loops there are reported as *Conditional Test Logic*.

## Per check
| Check | Limitation |
| :--- | :--- |
| Ignored Test | Only JUnit 4 `@Ignore`. JUnit 5 `@Disabled` is **not** detected. |
| Verbose Test | Fixed threshold of 30 *physical* lines (braces, comments and blank lines count). The `max` threshold is not configurable (no `setMax`), so `<property name="max" .../>` makes Checkstyle fail. |
| Magic Number | Reports any numeric literal passed as an argument of any call in a test (including `0` and `1`, e.g. `list.get(0)`), not only values in assertions. |
| Assertion Roulette | A message that is not a string literal (lambda, variable, concatenation, text block) counts as "no message"; `assertThrows` and fluent AssertJ chains are counted as assertions without message. |
| Unknown Test | Closed list of assertion names: Mockito `verify`, AssertJ `assertThatThrownBy`, `assertInstanceOf`, custom `assertXxx` helpers and `ExpectedException` are not recognised. |
| Duplicate Assert | AssertJ chains with the same subject but different assertions may be reported as duplicates. |
| Redundant Assertion | May flag legitimate contract tests (`assertEquals(a.hashCode(), a.hashCode())`, `assertSame(emptyList(), emptyList())`). |
| Redundant Print / Sensitive Equality | Match by method name on any receiver (`write`, `print`, any `toString()`), even outside the context the smell describes. |
| Exception Handling | Reports a `throw` inside an `assertThrows` lambda and in `@Test(expected = ...)` tests. |
| Mystery Guest / Resource Optimism | Match by simple type name (`File`, `Connection`, `Session`, `Message`...) without resolving imports; `@TempDir File` is reported; a variable of a domain class named `Message` is reported. Only the test method body is inspected (fixtures in `@BeforeEach` are not). |
| General Fixture | Matches identifiers by name (a local variable with the same name counts as a use of the field) and indexes tests by method name, so overloaded tests collide. |
| Constructor Initialization | Reports test classes with a constructor, including JUnit 5 constructor injection and JUnit 4 `Parameterized` runners. |
| Empty Test | Does not recognise some statements (`synchronized` blocks, labeled statements), which can be reported as empty. |

## Registration and compatibility
- Checks must be referenced by their fully qualified name (`br.ufba.arieslinter.checks.SleepyTestCheck`); the short module name does not work because the JAR does not ship a `checkstyle_packages.xml` at its root.
- Messages are hard-coded in English.
- The same findings were obtained with Checkstyle 8.45.1 to 14.3.0 on a small corpus (`tests/`), but that corpus does not exercise text blocks in all constructs, string templates or every framework.

## Evaluation caveats
- `tests/expected.txt` is a snapshot of the tool's own behavior, reviewed by hand. It protects against regressions; it is **not** an independent ground truth and cannot be used to compute precision or recall.
