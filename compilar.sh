#!/usr/bin/env bash
# Compila sin Maven. Requiere lib/sqlite-jdbc-3.46.1.3.jar
set -e
mkdir -p out
javac -Xlint:all,-serial -encoding UTF-8 -d out -cp lib/sqlite-jdbc-3.46.1.3.jar $(find src/main/java -name "*.java")
