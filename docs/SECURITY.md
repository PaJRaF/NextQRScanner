# Model bezpieczeństwa

## Powierzchnia zagrożeń i mechanizmy obronne

| Zagrożenie | Obrona |
| --- | --- |
| Złośliwy URL w kodzie QR (phishing/malware) | Wielowarstwowa weryfikacja: `UrlHeuristics` (offline) + Google Safe Browsing. **Brak auto-open** — pełny adres i werdykt pokazywane przed otwarciem; werdykt „niebezpieczny” wymaga dodatkowego potwierdzenia. |
| QRLjacking (przejęcie sesji przez podstawiony QR logowania) | Heurystyka wykrywa przekierowania `redirect=…login/auth`; ostrzeżenie w banerze bezpieczeństwa. |
| Homograph / IDN spoofing | Wykrywanie Punycode (`xn--`) i mieszanych skryptów (łacina + cyrylica/greka). |
| Skrócone linki maskujące cel | Wykrywanie znanych skracaczy; ostrzeżenie. |
| Wyciek historii skanów | Szyfrowanie SQLCipher, klucz 256-bit w Android Keystore (StrongBox gdy dostępny), wykluczenie z backupu. |
| MITM / podsłuch sieci | `network_security_config.xml`: wymuszony HTTPS, tylko systemowe CA (odrzucenie CA użytkownika). |
| Sekrety w binarce | Brak hardcoded kluczy; wstrzykiwanie w buildzie (BuildConfig/env/keystore.properties), R8 + `-repackageclasses`. |
| Reverse engineering / zmodyfikowana instalacja | Play Integrity API (do wpięcia w Play Console + backend). Obfuskacja R8 full mode. |
| Wyciek danych przez logi | Strip `android.util.Log` w release; logi sieciowe tylko na poziomie BASIC w debug (bez ciała żądań). |
| Nadmiarowe uprawnienia | Tylko aparat wymagany; akcje delegowane do systemu przez `Intent`. |

## Zasady, których przestrzega kod

1. **Nigdy nie otwieraj linku automatycznie.** Każde `Intent.ACTION_VIEW` dla URL
   jest efektem świadomej akcji użytkownika (`ContentActions.perform`).
2. **Nie loguj zeskanowanej treści ani danych osobowych** w release.
3. **Nie przechowuj sekretów w repozytorium** (patrz `.gitignore`, `keystore.properties`).
4. **Szyfruj dane wrażliwe w spoczynku** (SQLCipher + Keystore).
5. **Cała komunikacja sieciowa przez HTTPS/TLS 1.2+.**

## Zgłaszanie podatności

Znalazłeś problem bezpieczeństwa? Napisz na **[SECURITY-EMAIL]**. Prosimy o
odpowiedzialne ujawnianie (coordinated disclosure) i nieujawnianie szczegółów
publicznie do czasu wydania poprawki.
