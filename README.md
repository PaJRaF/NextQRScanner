# NextQR Scanner

Natywna aplikacja na Androida do **skanowania i generowania kodów QR oraz kodów
kreskowych**, zbudowana z naciskiem na bezpieczeństwo, prywatność i zgodność z
politykami Google Play. Skanowanie działa w pełni **offline i on-device**
(ML Kit), a każdy zeskanowany link jest weryfikowany zanim zostanie otwarty.

> Status: kompletny szkielet produkcyjny (architektura, rdzeń funkcji, konfiguracja
> bezpieczeństwa, monetyzacja, CI/CD i dokumentacja). Logika domenowa jest pokryta
> testami jednostkowymi. Przed publikacją należy uzupełnić klucze API, identyfikatory
> jednostek reklamowych oraz produkty Play Billing (patrz
> [`docs/PRE_LAUNCH_CHECKLIST.md`](docs/PRE_LAUNCH_CHECKLIST.md)).

## Stack technologiczny

| Obszar | Technologia |
| --- | --- |
| Język | Kotlin 100% |
| UI | Jetpack Compose + Material 3 (dynamic color / Material You) |
| Architektura | Clean Architecture (domain / data / presentation) + MVVM |
| Skanowanie | CameraX + ML Kit Barcode Scanning (on-device) |
| Generowanie | ZXing (core) + własny renderer (kolory, logo, kształt modułów) |
| Baza lokalna | Room + SQLCipher (szyfrowanie) |
| Klucze/sekrety | Android Keystore + EncryptedSharedPreferences |
| DI | Hilt |
| Sieć | Retrofit + OkHttp + kotlinx.serialization (Safe Browsing) |
| Monetyzacja | Google Play Billing 7 + AdMob |
| Integralność | Play Integrity API |
| Testy | JUnit, Truth, MockK, Turbine, Compose UI Test |
| Jakość | detekt, GitHub Actions CI/CD, Dependabot |
| Min/Target SDK | 26 / 35 · publikacja jako `.aab` |

## Szybki start

```bash
# 1. Skonfiguruj sekrety (nie są commitowane)
cp keystore.properties.sample keystore.properties
#   uzupełnij SAFE_BROWSING_API_KEY, ADMOB_APP_ID i dane podpisu

# 2. Testy jednostkowe warstwy domenowej / danych
./gradlew testDebugUnitTest

# 3. Analiza statyczna
./gradlew detekt

# 4. Build debug
./gradlew assembleDebug

# 5. Produkcyjny App Bundle (podpisany, zminifikowany R8)
./gradlew bundleRelease
```

> W tym repozytorium `SAFE_BROWSING_API_KEY` i produkcyjne identyfikatory reklam
> są puste/testowe. Build debug używa **oficjalnych testowych** jednostek AdMob,
> więc nigdy nie generuje realnych odsłon reklam podczas developmentu.

## Kluczowe decyzje bezpieczeństwa

- **Brak automatycznego otwierania linków.** Zeskanowany URL trafia najpierw do
  wielowarstwowej weryfikacji (heurystyki offline + Google Safe Browsing) i jest
  pokazywany w całości wraz z werdyktem. Otwarcie wymaga świadomej akcji, a dla
  werdyktu „niebezpieczny” — dodatkowego potwierdzenia.
- **Minimalne uprawnienia.** Wymagany jest tylko aparat. Akcje Wi-Fi / kontakt /
  kalendarz są delegowane do systemu przez `Intent`, więc aplikacja nigdy nie
  przetrzymuje uprawnień do lokalizacji/kontaktów.
- **Szyfrowanie w spoczynku.** Historia skanów w Room jest szyfrowana SQLCipher
  kluczem generowanym losowo i zapieczętowanym w Android Keystore.
- **Tylko HTTPS/TLS.** `network_security_config.xml` blokuje ruch cleartext i
  odrzuca użytkownicze CA (utrudnia MITM).
- **Brak sekretów w kodzie.** Klucze wstrzykiwane w czasie budowania (BuildConfig
  / env / `keystore.properties`), obfuskacja i strip logów w release (R8).
- **RODO/GDPR.** Consent na reklamy, usuwanie danych z poziomu aplikacji, jasna
  polityka prywatności ([`docs/PRIVACY_POLICY.md`](docs/PRIVACY_POLICY.md)).

## Dokumentacja

- [`docs/PROJECT_STRUCTURE.md`](docs/PROJECT_STRUCTURE.md) — struktura projektu i opis modułów
- [`docs/CONFIG_FILES.md`](docs/CONFIG_FILES.md) — wszystkie pliki konfiguracyjne
- [`docs/PRE_LAUNCH_CHECKLIST.md`](docs/PRE_LAUNCH_CHECKLIST.md) — checklista przed publikacją w Google Play
- [`docs/PRIVACY_POLICY.md`](docs/PRIVACY_POLICY.md) — szkic polityki prywatności (RODO)
- [`docs/SECURITY.md`](docs/SECURITY.md) — model bezpieczeństwa i zgłaszanie podatności

## Licencja

Kod aplikacji: do uzupełnienia przez właściciela projektu. Zależności zachowują
własne licencje open-source.
