# SBES 2025 paper vs. the current tool

The paper ("AriesLinter: Sniffing Test Smells Before They Happen") describes **10** smells. The current repository implements **17** checks. When citing results from the paper, state which version they refer to.

| Smell | In the paper | In the current tool | Note |
| :--- | :---: | :---: | :--- |
| Conditional Test Logic | yes | yes | |
| Default Test | yes | **no** | Not implemented in the current code. |
| Exception Handling | yes | yes | |
| Verbose Test | yes | yes | Paper: more than 30 lines. |
| Ignored Test | yes | yes (partial) | Paper mentions `@Ignore` or `@Disabled`; code detects only `@Ignore`. |
| Magic Number | yes | yes | Paper: numeric literals in assertions; code: literals passed to any call in a test. |
| Sensitive Equality | yes | yes | Paper: `toString` used in a test; same. |
| Redundant Print | yes | yes | |
| Sleepy Test | yes | yes | |
| Unknown Test | yes | yes | |
| Assertion Roulette | no | yes | Added after the paper. |
| Constructor Initialization | no | yes | Added after the paper. |
| Duplicate Assert | no | yes | Added after the paper. |
| Empty Test | no | yes | Added after the paper. |
| General Fixture | no | yes | Added after the paper. |
| Mystery Guest | no | yes | Added after the paper. |
| Redundant Assertion | no | yes | Added after the paper. |
| Resource Optimism | no | yes | Added after the paper. |

Other statements of the paper that cannot be checked from this repository:
- The paper says the smells kept were those whose detection did "not achieve 100% accuracy" on an internal suite. No measurements are published here; reproduce them before repeating that claim.
- The eight checks added after the paper have no published validation.

See also [LIMITATIONS.md](LIMITATIONS.md).
