#!/bin/bash

# Activer BuildKit
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Build avec cache actif
docker compose build

# Lancer les containers
docker compose up