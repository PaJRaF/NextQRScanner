# Bezpieczeństwo i kwestie prawne

## ⚖️ Ramy prawne (przeczytaj najpierw)

OSCam jest legalnym oprogramowaniem open-source. **Legalność Twojego
wdrożenia zależy od sposobu użycia:**

- ✅ **Dozwolone (typowo):** dekodowanie **własnej, opłaconej** subskrypcji na
  urządzeniach we własnym gospodarstwie domowym (np. multiroom — jedna karta,
  kilka telewizorów w tej samej sieci), zgodnie z regulaminem operatora.
- ⚠️ **Zależne od jurysdykcji / umowy:** udostępnianie karty poza własne
  gospodarstwo domowe, nawet nieodpłatnie.
- ❌ **Nielegalne:** nieautoryzowany *card sharing* płatnych treści osobom
  trzecim, korzystanie z cudzych/nielegalnych kart, obchodzenie zabezpieczeń
  bez zgody uprawnionego.

Skonsultuj regulamin operatora oraz lokalne przepisy. Autorzy tego szablonu
nie ponoszą odpowiedzialności za sposób jego wykorzystania.

---

## Utwardzenie serwera

### Panel webif
- **Wymuszaj HTTPS** — prefix `+` przy `httpport` (domyślnie `+8888`).
- **Silne, unikalne hasło** (`httppwd`): `openssl rand -base64 24`.
- **Whitelist IP** (`httpallowed`) — zawęź do pojedynczych hostów zamiast
  całego `/16`, jeśli to możliwe.
- Rozważ własny certyfikat TLS (`httpcert`) zamiast self-signed.
- Panel **nie powinien** być wystawiony do internetu. Dostęp zdalny tylko
  przez VPN (WireGuard/OpenVPN).

### Konta i dostęp
- Osobne konto na każdego klienta, zasada **minimalnych uprawnień**
  (parametr `group`, `services`, `au=0` jeśli klient nie ma aktualizować EMM).
- `uniq = 1` — jedno aktywne połączenie na konto (utrudnia współdzielenie loginu).
- Włączony **failban** (`failbancount`, `failbantime`) — blokada po nieudanych
  logowaniach.

### Anti-cascading
Sekcja `[anticasc]` w `oscam.conf` ogranicza nadmierne, równoległe
odpytywanie — ustaw `numusers`, `samples`, `penalty` zgodnie z realnym
zastosowaniem.

### Sieć / firewall
Otwórz tylko realnie używane porty. Przykład `nftables`:

```
# Panel i protokoły tylko z LAN
table inet filter {
  chain input {
    type filter hook input priority 0; policy drop;
    ct state established,related accept
    iif "lo" accept
    ip saddr 192.168.0.0/16 tcp dport { 8888, 12000, 15000 } accept
  }
}
```

### Kontener Docker
Compose w tym repo stosuje: `read_only: true`, `no-new-privileges`,
`cap_drop: ALL`, limity CPU/RAM, rotację logów. Nie uruchamiaj kontenera jako
root — obraz przełącza się na użytkownika `oscam`.

### systemd
Unit stosuje `ProtectSystem=strict`, `NoNewPrivileges`, `PrivateTmp`,
`RestrictAddressFamilies`. Rozszerzaj `ReadWritePaths` tylko o niezbędne ścieżki.

---

## Sekrety i repozytorium

- **Nie commituj** plików z prawdziwymi hasłami/kluczami do repo publicznego.
  Trzymaj placeholdery `<ZMIEN_...>`, a wartości podstawiaj przy wdrożeniu
  (np. przez zmienne środowiskowe, `docker secrets` lub prywatny vault).
- Rozważ dodanie `oscam-server/config/oscam.conf` z realnymi hasłami do
  `.gitignore` na środowisku wdrożeniowym.
- Regularnie rób kopie zapasowe (`scripts/backup.sh`) i przechowuj je
  zaszyfrowane, poza serwerem.

## Aktualizacje
- Śledź wydania OSCam i odbudowuj obraz (`docker compose build --pull`).
- Aktualizuj bazowy obraz Alpine oraz pakiety systemu hosta.
