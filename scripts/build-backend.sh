#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../backend"
mvn clean package -DskipTests
