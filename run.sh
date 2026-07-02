#!/bin/bash
set -e

echo "=== Lancement de charbonecolo ==="

# Setup Tailwind CSS (install npm deps + build output.css)
./tailwind.sh

echo ""
echo "Compilation et démarrage avec Maven..."
./mvnw spring-boot:run
