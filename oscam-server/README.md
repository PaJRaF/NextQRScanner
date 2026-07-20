# Profesjonalny serwer OSCam

Kompletny, produkcyjny szkielet konfiguracji **serwera OSCam** — oprogramowania
open-source do obsługi kart dostępu warunkowego (Conditional Access Module).
Zestaw zawiera bezpieczne domyślne ustawienia, wdrożenie w Dockerze i natywnie
(systemd), skrypty pomocnicze oraz dokumentację.

> **Katalog niezależny od aplikacji QR Scanner** w tym repozytorium — OSCam to
> osobne oprogramowanie napisane w C, nie ma powiązania z kodem Androida.

## ⚖️ Uwaga prawna

OSCam samo w sobie jest legalnym oprogramowaniem open-source. **Odpowiadasz za
zgodność z prawem swojego wykorzystania.** Ten szablon zakłada użycie
**własnej, legalnej karty/subskrypcji** oraz udostępnianie wyłącznie we
własnej sieci (np. multiroom) lub z peerami, na których masz zgodę operatora.
Nieautoryzowane współdzielenie płatnych treści (card sharing) jest nielegalne
w większości jurysdykcji. Szczegóły: [`docs/SECURITY.md`](docs/SECURITY.md).

## Struktura

```
oscam-server/
├── config/                 # Pliki konfiguracyjne OSCam (szablony)
│   ├── oscam.conf          #   ustawienia globalne, webif, DVBAPI, anti-cascading
│   ├── oscam.server        #   czytniki: lokalna karta + peery (CCcam/Newcamd)
│   ├── oscam.user          #   konta klientów
│   ├── oscam.services      #   grupy usług (CAID/provider)
│   └── oscam.dvbapi        #   priorytety/ignorowanie dla DVBAPI
├── docker/                 # Wdrożenie w kontenerze
│   ├── Dockerfile          #   build ze źródeł (multi-stage, Alpine)
│   ├── docker-compose.yml  #   uruchomienie z utwardzeniem kontenera
│   └── .dockerignore
├── systemd/
│   └── oscam.service       # Unit systemd (instalacja natywna)
├── scripts/
│   ├── install.sh          # Instalacja natywna ze źródeł (Debian/Ubuntu)
│   └── backup.sh           # Kopia zapasowa konfiguracji
└── docs/
    ├── INSTALL.md          # Instrukcja instalacji krok po kroku
    ├── SECURITY.md         # Utwardzenie i kwestie prawne
    └── TROUBLESHOOTING.md  # Diagnostyka najczęstszych problemów
```

## Szybki start (Docker)

```bash
cd oscam-server

# 1. Ustaw hasła w plikach konfiguracyjnych (placeholdery <ZMIEN_...>)
#    - config/oscam.conf  -> httppwd (panel webif)
#    - config/oscam.user  -> pwd (konta klientów)

# 2. Zbuduj i uruchom
docker compose -f docker/docker-compose.yml up -d --build

# 3. Otwórz panel zarządzania (login/hasło z oscam.conf)
#    https://<host>:8888

# 4. Podgląd logów
docker logs -f oscam
```

## Szybki start (natywnie, systemd)

```bash
sudo oscam-server/scripts/install.sh
# następnie ustaw hasła w /etc/oscam/*, potem:
sudo systemctl enable --now oscam
```

## Porty domyślne

| Port  | Protokół | Rola |
| ----- | -------- | ---- |
| 8888  | HTTPS    | Panel webif (zarządzanie) |
| 12000 | CCcam    | Klienci CCcam (opcjonalnie) |
| 15000 | Newcamd  | Klienci Newcamd (opcjonalnie) |
| 988   | Monitor  | Monitor lokalny (tylko localhost) |

## Bezpieczne domyślne ustawienia

- Panel webif **wymusza TLS** (prefix `+` przy porcie) i whitelistę IP (LAN).
- **Failban** blokuje IP po nieudanych logowaniach.
- **Anti-cascading** ogranicza nadmierne współdzielenie.
- Kontener Docker: `read_only`, `no-new-privileges`, `cap_drop: ALL`, limity CPU/RAM.
- Unit systemd: `ProtectSystem=strict`, `NoNewPrivileges`, `PrivateTmp`.

Więcej w [`docs/INSTALL.md`](docs/INSTALL.md) i [`docs/SECURITY.md`](docs/SECURITY.md).
