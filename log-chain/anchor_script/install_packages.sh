#!/bin/bash
set -e

FABRIC_VERSION=2.5.5
CA_VERSION=1.5.9

echo ">>> Updating packages..."
sudo apt-get update -y

#!/usr/bin/env bash
set -euo pipefail

echo "=== Installing OpenJDK 17 ==="

# Update package index
sudo apt-get update -y

# Install Java 17
sudo apt-get install -y gradle openjdk-17-jdk

# Verify installation
echo
echo "=== Java version ==="
java -version

echo
echo "=== Java home ==="
JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
echo "JAVA_HOME=$JAVA_HOME"

# Optionally add JAVA_HOME to ~/.bashrc if not already set
if ! grep -q "JAVA_HOME" ~/.bashrc; then
  echo "export JAVA_HOME=$JAVA_HOME" >> ~/.bashrc
  echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
  echo "JAVA_HOME added to ~/.bashrc"
fi

echo
echo "=== Installation Java complete ==="

echo ">>> Installing dependencies (Docker, Git, Curl, etc.)..."
sudo apt-get install -y docker.io docker-compose git curl jq

# Enable docker without sudo
sudo usermod -aG docker $USER
