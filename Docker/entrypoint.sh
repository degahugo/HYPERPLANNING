#!/usr/bin/env bash
set -euo pipefail

echo "JAVA:"
java -version
echo "MAVEN:"
mvn -v

# Lance JavaFX dans un X virtuel (robuste)
# + on active les logs Maven détaillés (la vraie erreur sera affichée)
xvfb-run -a -s "-screen 0 1280x720x24" mvn -e -X javafx:run