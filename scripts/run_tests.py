#!/usr/bin/python
import subprocess
import os
import re
import sys

from colorama import Fore, Style

subprocess.run(["mvn", "javacc:javacc", "compile", "package"], check=True)

TESTS_DIR = "tests"
TESTS = []
for file in os.listdir(TESTS_DIR):
    if not file.endswith(".calc"):
        continue
    expected_file = f"{TESTS_DIR}/{file.removesuffix('.calc')}.out"
    sourecode_file = f"{TESTS_DIR}/{file}"

    if len(sys.argv) > 1 and not re.match(sys.argv[1], file):
        continue

    expected = open(expected_file, "r").read()
    TESTS.append((sourecode_file, expected))
TESTS.sort(key=lambda x: x[0])

for (sourecode_file, expected) in TESTS:
    # outputproc = subprocess.run(
    #     [
    #         "java",
    #         "-ea",
    #         "-cp",
    #         "target/icl-1.0-SNAPSHOT.jar",
    #         "App",
    #         "run",
    #         sourecode_file,
    #     ],
    #     capture_output=True,
    # )
    outputproc = subprocess.run(
        [
            "mvn",
            "-q",
            "exec:java",
            "-Dexec.mainClass=App",
            f"-Dexec.args=run {sourecode_file}",
        ],
        capture_output=True,
        env={"MAVEN_OPTS": "-ea"},
    )

    if outputproc.returncode != 0:
        print(f"Test {sourecode_file} failed with error code {outputproc.returncode}")
        print(outputproc.stdout.decode("utf-8"))
        print(outputproc.stderr.decode("utf-8"))
        continue

    output = outputproc.stdout.decode("utf-8")
    if expected != output:
        print(f"[{Fore.RED}FAIL{Style.RESET_ALL}] {sourecode_file}")
        print(f"Expected:\n {expected}")
        print(f"Got:\n {output}")
    else:
        print(f"[{Fore.GREEN}OK{Style.RESET_ALL}] {sourecode_file}")
