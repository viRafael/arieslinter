#!/usr/bin/env python3
"""Runs the Checkstyle CLI + Arieslinter on tests/corpus and compares the findings
with tests/expected.txt. Use it to prove the detection is identical across
Checkstyle versions / Java versions / environments (the engine every IDE plugin uses).

    python3 tests/check_corpus.py --java java --checkstyle-jar checkstyle-10.23.1-all.jar \
        --arieslinter-jar target/arieslinter-1.0.jar
    python3 tests/check_corpus.py ... --update      # regenerate expected.txt
"""
import argparse
import difflib
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
LINE = re.compile(r"^(?:\[\w+\]\s*)?.*?([\w]+\.java):(\d+)(?::\d+)?:.*\[(\w+)\]\s*$")


def run(java, cs_jar, aries_jar):
    cp = os.pathsep.join([cs_jar, aries_jar])
    res = subprocess.run(
        [java, "-cp", cp, "com.puppycrawl.tools.checkstyle.Main", "-c", os.path.join(HERE, "checkstyle.xml"),
         os.path.join(HERE, "corpus")],
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, timeout=300)
    findings, problems = [], []
    for line in res.stdout.splitlines():
        m = LINE.match(line.strip())
        if m:
            findings.append(f"{m.group(1)}:{m.group(2)} {m.group(3)}")
        elif line.strip() and line.strip() not in ("Starting audit...", "Audit done."):
            problems.append(line.strip())
    return sorted(findings), problems


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--java", default="java")
    p.add_argument("--checkstyle-jar", required=True)
    p.add_argument("--arieslinter-jar", required=True)
    p.add_argument("--update", action="store_true")
    a = p.parse_args()

    findings, problems = run(a.java, a.checkstyle_jar, a.arieslinter_jar)
    if problems:
        print("ENGINE PROBLEMS:")
        for x in problems[:10]:
            print("  ", x)
    expected_path = os.path.join(HERE, "expected.txt")
    if a.update:
        with open(expected_path, "w") as f:
            f.write("\n".join(findings) + "\n")
        print(f"expected.txt updated ({len(findings)} findings)")
        return 0
    with open(expected_path) as f:
        expected = sorted(l.strip() for l in f if l.strip())
    if findings == expected and not problems:
        print(f"OK: {len(findings)} findings identical to expected.txt")
        return 0
    print("MISMATCH vs expected.txt:")
    for d in difflib.unified_diff(expected, findings, "expected", "actual", lineterm="", n=0):
        print("  ", d)
    return 1


if __name__ == "__main__":
    sys.exit(main())
