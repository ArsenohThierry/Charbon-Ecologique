#!/bin/bash
set -e  # Stoppe le script si une commande échoue

DB_NAME="charbon"  # Nom de ta base
DB_USER="postgres"
SQL_DIR="./sql"    # Dossier contenant tes fichiers .sql

# 1. Déconnecter les utilisateurs et supprimer la base
psql -U "$DB_USER" -d postgres -c \
"SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$DB_NAME';"
psql -U "$DB_USER" -d postgres -c "DROP DATABASE IF EXISTS \"$DB_NAME\";"

# 2. Recréer la base
psql -U "$DB_USER" -d postgres -c "CREATE DATABASE \"$DB_NAME\";"

# 3. Importer tous les fichiers .sql triés par nom
for file in $(ls "$SQL_DIR"/*.sql | sort); do
    echo "📥 Import de $file..."
    psql -U "$DB_USER" -d "$DB_NAME" -f "$file"
done

echo "✅ Base $DB_NAME restaurée avec succès."
