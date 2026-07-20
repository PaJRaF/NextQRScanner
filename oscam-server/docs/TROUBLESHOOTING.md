# Diagnostyka (troubleshooting)

## Podstawowe źródła informacji
- **Logi:** `docker logs -f oscam` lub `journalctl -u oscam -f`.
- **Panel webif:** zakładki *Readers*, *Users*, *Status*, *Log*.
- Tymczasowo podnieś szczegółowość logów (`oscam.conf`): nie zostawiaj wysokiego
  poziomu debug na produkcji.

---

## Karta nie jest wykrywana (`CARD ERROR` / brak `CARD OK`)
1. Sprawdź urządzenie: `ls -l /dev/ttyUSB*`, `dmesg | grep -i tty`.
2. Docker: czy urządzenie jest przekazane (`devices:` w compose)?
3. systemd: czy odkomentowane `DeviceAllow` i `SupplementaryGroups=dialout`?
4. Zmień taktowanie w `oscam.server`: spróbuj `mhz=600 cardmhz=600` lub
   odwrotnie `357/357`.
5. Zmień `protocol` (`mouse` ↔ `smartreader` ↔ `pcsc`) zależnie od czytnika.
6. Uprawnienia: użytkownik `oscam` musi należeć do grupy urządzenia (`dialout`).

## Klient nie może się połączyć
1. Zgodność **grup**: `group` klienta w `oscam.user` musi pokrywać się z `group`
   czytnika w `oscam.server`.
2. Poprawny **port/protokół** po stronie klienta (CCcam 12000 / Newcamd 15000).
3. Newcamd: **`key` (DES)** musi być identyczny na serwerze i kliencie.
4. Firewall/whitelist: czy IP klienta jest dopuszczone?
5. Sprawdź failban w panelu (*Status* → *Failban*) — być może IP zostało
   zablokowane po błędnych logowaniach.

## Panel webif niedostępny
1. Port `8888` otwarty i nie zajęty przez inny proces (`ss -tlnp | grep 8888`).
2. Używasz **HTTPS** (prefix `+` w `httpport` wymusza TLS) — wpisz `https://`.
3. Twoje IP mieści się w `httpallowed`.
4. Docker healthcheck `unhealthy`? Sprawdź `docker logs oscam` — zwykle błąd
   składni w plikach konfiguracyjnych zatrzymuje start.

## `ECM` bez odpowiedzi / brak obrazu (freeze)
1. Czy karta faktycznie ma uprawnienia do danego CAID/providera (panel *Readers*)?
2. Zwiększ `cwtimeout` w `oscam.conf`, jeśli karta odpowiada wolno.
3. Konflikt priorytetów DVBAPI — sprawdź reguły `P:`/`I:` w `oscam.dvbapi`.
4. Anti-cascading z `penalty=1` może zwracać *fake CW* przy przekroczeniu
   limitu — poluzuj `numusers`/`samples`, jeśli to Twój legalny multiroom.

## Kontener restartuje się w pętli
- Najczęściej **błąd składni** w `config/*`. Uruchom jednorazowo w trybie
  foreground, aby zobaczyć komunikat:
  ```bash
  docker compose -f docker/docker-compose.yml run --rm oscam
  ```
- `read_only: true` blokuje zapis poza `/tmp` i wolumenem logów — upewnij się,
  że `logfile` wskazuje na `/var/log/oscam/…`.

## Build obrazu nie przechodzi
- Repozytorium źródeł niedostępne — ustaw inny `OSCAM_REPO`/`OSCAM_REF` (arg
  build) lub sprawdź dostęp sieciowy do `git.streamboard.tv`.
- Brak zależności — obraz `builder` instaluje `libusb-dev`, `pcsc-lite-dev`,
  `openssl-dev`, `cmake`; przy własnych zmianach zachowaj te pakiety.
