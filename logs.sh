#!/bin/bash

# ============================================================
# logs.sh — Capture les logs du container hyperplanning
#            déjà lancé par run.sh
# Usage : ./logs.sh  (dans un 2ème terminal pendant que run.sh tourne)
# ============================================================

LOG_FILE="errors_$(date +%Y%m%d_%H%M%S).log"

echo "========================================"
echo " HyperPlanning — Capture des logs"
echo " Fichier de sortie : $LOG_FILE"
echo " Ctrl+C pour arrêter"
echo "========================================"
echo ""

# Vérifie que le container tourne
if ! docker compose ps --services --filter "status=running" 2>/dev/null | grep -q "hyperplanning"; then
    echo "[!] Le container 'hyperplanning' ne tourne pas."
    echo "    Lance d'abord ./run.sh dans un autre terminal."
    exit 1
fi

echo "===== LOGS HYPERPLANNING =====" > "$LOG_FILE"

# Suit les logs EN DIRECT du container déjà lancé (depuis le début)
docker compose logs --no-color --follow hyperplanning 2>&1 | tee -a "$LOG_FILE"

# ── Résumé après Ctrl+C ──────────────────────────────────────
echo "" >> "$LOG_FILE"
echo "===== RÉSUMÉ ERREURS (ERROR / WARN / Exception / Caused by) =====" >> "$LOG_FILE"
grep -iE "error|warn|exception|caused by|at fr\.univ" "$LOG_FILE" \
    | grep -v "^=====" \
    >> "$LOG_FILE" 2>&1 || true

echo ""
echo "========================================"
echo " Logs complets     : $LOG_FILE"
echo " Erreurs détectées : $(grep -ciE 'error|exception|caused by' "$LOG_FILE" || echo 0)"
echo "========================================"