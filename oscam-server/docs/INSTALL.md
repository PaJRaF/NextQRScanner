# Instalacja OSCam — krok po kroku

Dwie ścieżki wdrożenia: **Docker** (zalecane, izolacja) oraz **natywnie**
(systemd, jeśli potrzebujesz bezpośredniego dostępu do czytnika USB).

---

## Wariant A — Docker

### Wymagania
- Docker 24+ oraz plugin `docker compose`.
- (Opcjonalnie) czytnik kart USB — wymaga przekazania urządzenia do kontenera.

### Kroki

1. **Ustaw hasła** w plikach `config/`:
   - `config/oscam.conf` → `httppwd` (panel webif). Wygeneruj silne hasło:
     ```bash
     openssl rand -base64 24
     ```
   - `config/oscam.user` → `pwd` dla każdego konta.

2. **Whitelist IP** panelu w `oscam.conf` (`httpallowed`) — zawęź do swojej
   sieci LAN. Domyślnie dopuszczony jest zakres `192.168.0.0-192.168.255.255`.

3. **Czytnik USB** (jeśli używasz lokalnej karty) — w `docker/docker-compose.yml`
   odkomentuj sekcję `devices:` i wskaż właściwe urządzenie (`/dev/ttyUSB0`).
   Sprawdź je poleceniem `ls -l /dev/ttyUSB*` lub `dmesg | grep tty`.

4. **Build i start**:
   ```bash
   cd oscam-server
   docker compose -f docker/docker-compose.yml up -d --build
   ```

5. **Weryfikacja**:
   ```bash
   docker ps            # STATUS powinien być "healthy" po ~30 s
   docker logs -f oscam # szukaj "reader ... card detected" i "Webif started"
   ```

6. Otwórz panel: `https://<host>:8888` (self-signed cert — zaakceptuj wyjątek
   lub podaj własny certyfikat przez `httpcert`).

---

## Wariant B — Natywnie (systemd)

### Wymagania
- Debian 12 / Ubuntu 22.04+ (skrypt używa `apt`).
- Uprawnienia root (`sudo`).

### Kroki

1. **Uruchom instalator** (kompiluje ze źródeł, tworzy użytkownika `oscam`,
   kopiuje konfigurację, instaluje unit systemd):
   ```bash
   sudo oscam-server/scripts/install.sh
   ```
   Możesz przypiąć wersję: `OSCAM_REF=11692 sudo -E ./scripts/install.sh`.

2. **Ustaw hasła** w `/etc/oscam/oscam.conf` i `/etc/oscam/oscam.user`.

3. **Start i autostart**:
   ```bash
   sudo systemctl enable --now oscam
   systemctl status oscam
   ```

4. **Czytnik USB** — jeśli używasz karty lokalnej, odkomentuj w
   `/etc/systemd/system/oscam.service` linie `DeviceAllow` oraz
   `SupplementaryGroups=dialout`, potem:
   ```bash
   sudo systemctl daemon-reload && sudo systemctl restart oscam
   ```

---

## Konfiguracja czytnika karty

W `config/oscam.server` (blok `[reader]` `local_card`):

- `device` — ścieżka do czytnika (`/dev/ttyUSB0`, dla PCSC: `pcsc`).
- `protocol` — `mouse` (Phoenix/Smargo), `smartreader`, `pcsc`, `internal`.
- `mhz` / `cardmhz` — taktowanie. Start od `357/357`; wiele kart działa na
  `600/600` (szybszy init). Dobierz eksperymentalnie, obserwując logi initu.
- `caid` / `ident` — zostaw puste, aby OSCam wykrył automatycznie.

Po starcie sprawdź w panelu **Readers** czy karta pokazuje `CARD OK` i
poprawne CAID/serial.

## Kopia zapasowa

```bash
sudo OSCAM_CONF_DIR=/etc/oscam oscam-server/scripts/backup.sh /var/backups/oscam
```

Skrypt tworzy archiwum `tar.gz` z uprawnieniami `0600` i rotuje do 10 kopii.
