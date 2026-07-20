#!/usr/bin/env bash
# =============================================================================
#  install.sh  --  natywna instalacja OSCam ze zrodel (Debian/Ubuntu)
# -----------------------------------------------------------------------------
#  Kompiluje OSCam, tworzy uzytkownika systemowego, kopiuje konfiguracje
#  i instaluje unit systemd. Uruchamiaj jako root (sudo).
# =============================================================================
set -euo pipefail

OSCAM_REPO="${OSCAM_REPO:-https://git.streamboard.tv/common/oscam.git}"
OSCAM_REF="${OSCAM_REF:-master}"
PREFIX="/usr/local/bin"
CONF_DIR="/etc/oscam"
LOG_DIR="/var/log/oscam"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(dirname "$SCRIPT_DIR")"

if [[ "${EUID}" -ne 0 ]]; then
  echo "Uruchom jako root: sudo $0" >&2
  exit 1
fi

echo "==> Instalacja zaleznosci build..."
apt-get update
apt-get install -y --no-install-recommends \
  build-essential cmake git pkg-config \
  libssl-dev libusb-1.0-0-dev libpcsclite-dev zlib1g-dev

echo "==> Pobieranie zrodel OSCam (${OSCAM_REF})..."
BUILD_TMP="$(mktemp -d)"
trap 'rm -rf "${BUILD_TMP}"' EXIT
git clone --depth 1 --branch "${OSCAM_REF}" "${OSCAM_REPO}" "${BUILD_TMP}/oscam" \
  || git clone --depth 1 "${OSCAM_REPO}" "${BUILD_TMP}/oscam"

echo "==> Kompilacja..."
cmake -S "${BUILD_TMP}/oscam" -B "${BUILD_TMP}/oscam/build" \
  -DWEBIF=1 -DHAVE_LIBUSB=1 -DWITH_SSL=1 -DCS_ANTICASC=1
make -C "${BUILD_TMP}/oscam/build" -j"$(nproc)"

echo "==> Instalacja binarki -> ${PREFIX}/oscam"
install -m 0755 "${BUILD_TMP}/oscam/build/oscam" "${PREFIX}/oscam"

echo "==> Uzytkownik systemowy i katalogi..."
id -u oscam &>/dev/null || useradd --system --no-create-home --shell /usr/sbin/nologin oscam
mkdir -p "${CONF_DIR}" "${LOG_DIR}"

echo "==> Kopiowanie konfiguracji (istniejace pliki NIE sa nadpisywane)..."
for f in "${REPO_DIR}"/config/*; do
  base="$(basename "$f")"
  if [[ -e "${CONF_DIR}/${base}" ]]; then
    echo "    pomijam istniejacy ${CONF_DIR}/${base}"
  else
    install -m 0640 "$f" "${CONF_DIR}/${base}"
  fi
done
chown -R oscam:oscam "${CONF_DIR}" "${LOG_DIR}"

echo "==> Instalacja unitu systemd..."
install -m 0644 "${REPO_DIR}/systemd/oscam.service" /etc/systemd/system/oscam.service
systemctl daemon-reload

cat <<'EOF'

==> Gotowe.
    1. Edytuj /etc/oscam/oscam.conf i USTAW haslo webif (httppwd).
    2. Ustaw haslo konta w /etc/oscam/oscam.user.
    3. Start:   sudo systemctl enable --now oscam
    4. Status:  systemctl status oscam
    5. Panel:   https://<host>:8888

    UWAGA: uzywaj wylacznie wlasnej, legalnej karty/subskrypcji.
EOF
