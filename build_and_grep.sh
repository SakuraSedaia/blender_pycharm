#!/bin/bash
./gradlew compileKotlin --console=plain > build_output.txt 2>&1
grep -C 5 "e: " build_output.txt
grep -C 5 "error:" build_output.txt
