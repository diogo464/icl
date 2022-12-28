#!/usr/bin/python
import argparse
import dataclasses
import enum
import subprocess
import os
import re
import sys
from typing import Iterable


class RunKind(enum.Enum):
    INTERPRETED = 1
    COMPILED = 2


@dataclasses.dataclass
class TestCase:
    name: str
    input: str
    expected_output: str


def read_test_cases() -> list[TestCase]:
    tests = []
    tests_dir = "tests"
    for file in os.listdir(tests_dir):
        if not file.endswith(".calc"):
            continue
        expected_file = f"{tests_dir}/{file.removesuffix('.calc')}.out"
        sourecode_file = f"{tests_dir}/{file}"
        expected = open(expected_file, "r").read()
        tests.append(TestCase(sourecode_file, sourecode_file, expected))
    tests.sort(key=lambda x: x.name)
    return tests


def run_test_case(kind: RunKind, test_case: TestCase):
    subcmd = "run" if kind == RunKind.INTERPRETED else "crun"
    outputproc = subprocess.run(
        "MAVEN_OPTS=\"-ea\" mvn -q exec:java -Dexec.mainClass=App -Dexec.args=\"{subcmd} {test_case.name}\"",
        shell=True,
        capture_output=True,
    )
    if outputproc.returncode != 0:
        print(f"Test {test_case.name} failed with error code {outputproc.returncode}")
        print(outputproc.stdout.decode("utf-8"))
        print(outputproc.stderr.decode("utf-8"))
        sys.exit(1)

    output = outputproc.stdout.decode("utf-8")
    if test_case.expected_output != output:
        print(f"[FAIL] {test_case.name}")
        print(f"Expected:\n {test_case.expected_output}")
        print(f"Got:\n {output}")
    else:
        print(f"[OK] {test_case.name}")


def run_test_cases(kind: RunKind, test_cases: Iterable[TestCase]) -> bool:
    print(f"Running {kind.name} tests")
    for t in test_cases:
        run_test_case(kind, t)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--no-compile", action="store_true", default=False)
    parser.add_argument("--dry-run", action="store_true", default=False)
    parser.add_argument("--filter", help="regex to match test files")
    args = parser.parse_args()

    if not args.no_compile:
        if args.dry_run:
            print("Compiling")
        else:
            subprocess.run(["mvn", "javacc:javacc", "compile", "package"], check=True)

    test_cases = [
        t
        for t in read_test_cases()
        if args.filter is None or re.match(args.filter, t.name)
    ]

    if args.dry_run:
        for t in test_cases:
            print(f"Running {t.name}")
    else:
        run_test_cases(RunKind.INTERPRETED, test_cases)
        run_test_cases(RunKind.COMPILED, test_cases)


if __name__ == "__main__":
    main()
