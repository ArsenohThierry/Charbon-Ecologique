#!/bin/bash
set -e

echo "=== Setup Tailwind CSS ==="

# 0. Install system dependencies (Node.js / npm)
if ! command -v npm &>/dev/null; then
    echo "[0/3] Installation de Node.js et npm..."
    if command -v apt &>/dev/null; then
        sudo apt update && sudo apt install -y nodejs npm
    elif command -v dnf &>/dev/null; then
        sudo dnf install -y nodejs npm
    elif command -v pacman &>/dev/null; then
        sudo pacman -S --noconfirm nodejs npm
    else
        echo "ERREUR : Gestionnaire de paquets non reconnu. Installe Node.js manuellement."
        exit 1
    fi
else
    echo "[0/3] Node.js $(node -v) — déjà installé"
fi
    echo "[1/3] Installation des dépendances npm..."
    npm install

# 2. Build CSS
echo "[2/3] Génération du fichier CSS..."
npm run build:css

echo "=== Terminé ==="
echo "Fichier généré : src/main/resources/static/css/output.css"
