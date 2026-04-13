#!/bin/bash

# Activer BuildKit
#export DOCKER_BUILDKIT=1
#export COMPOSE_DOCKER_CLI_BUILD=1

# Build avec cache actif
#docker compose build

# Lancer les containers
#docker compose up


#!/bin/bash
set -e

# Java 21 via SDKMAN (si disponible)
if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    sdk use java 21.0.10-tem
fi

echo "--- Vérification de l'environnement ---"
java -version
mvn -version

echo "--- Nettoyage des anciens conteneurs ---"
docker compose down -v

echo "--- Démarrage de PostgreSQL ---"
docker compose up -d postgres

echo "--- Attente que PostgreSQL soit prêt ---"
until docker compose exec postgres pg_isready -U sa -d hyperplanning > /dev/null 2>&1; do
    echo "  PostgreSQL pas encore prêt, attente..."
    sleep 2
done
echo "  PostgreSQL est prêt."

docker compose ps

echo "--- Compilation ---"
mvn clean compile

echo "--- Lancement de l'application ---"
mvn javafx:run
