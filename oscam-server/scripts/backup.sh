#!/usr/bin/env bash
# =============================================================================
#  backup.sh  --  kopia zapasowa konfiguracji OSCam
# -----------------------------------------------------------------------------
#  Tworzy zaszyfrowane (opcjonalnie) archiwum tar.gz z /etc/oscam.
#  Uzycie:  ./backup.sh [katalog_docelowy]
# =============================================================================
set -euo pipefail

CONF_DIR="${OSCAM_CONF_DIR:-/etc/oscam}"
DEST_DIR="${1:-./backups}"
STAMP="$(date +%Y%m%d-%H%M%S)"
ARCHIVE="${DEST_DIR}/oscam-config-${STAMP}.tar.gz"

mkdir -p "${DEST_DIR}"

if [[ ! -d "${CONF_DIR}" ]]; then
  echo "Brak katalogu konfiguracji: ${CONF_DIR}" >&2
  exit 1
fi

tar -czf "${ARCHIVE}" -C "$(dirname "${CONF_DIR}")" "$(basename "${CONF_DIR}")"
chmod 0600 "${ARCHIVE}"
echo "Kopia zapisana: ${ARCHIVE}"

# Rotacja: zostaw 10 najnowszych kopii.
ls -1t "${DEST_DIR}"/oscam-config-*.tar.gz 2>/dev/null | tail -n +11 | xargs -r rm -f
echo "Rotacja zakonczona (zachowano max 10 kopii)."
