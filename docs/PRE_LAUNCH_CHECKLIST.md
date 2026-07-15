# Checklista przed publikacją w Google Play

Lista kontrolna do przejścia **przed** wysłaniem `.aab` na produkcję. Pogrupowana
tematycznie; `[ ]` = do zrobienia, `[x]` = zapewnione w tym repozytorium.

## 1. Konfiguracja techniczna buildu

- [x] Format publikacji: **Android App Bundle** (`./gradlew bundleRelease`).
- [x] `minSdk 26`, `targetSdk 35` (spełnia aktualny wymóg Google Play dla nowych aplikacji/aktualizacji).
- [x] R8/ProGuard: `isMinifyEnabled = true`, `isShrinkResources = true`, `enableR8.fullMode`.
- [x] Strip logów (`android.util.Log`) w release.
- [ ] `versionCode` / `versionName` podbite względem poprzedniego wydania.
- [ ] Splity App Bundle (język/gęstość/ABI) zweryfikowane na urządzeniach testowych.

## 2. Podpisywanie

- [ ] Skonfigurowane **Play App Signing** (klucz aplikacji zarządzany przez Google).
- [ ] Upload key wygenerowany i bezpiecznie przechowywany (poza repo).
- [ ] `keystore.properties` uzupełniony lokalnie / sekrety w CI (`SAFE_BROWSING_API_KEY`, `ADMOB_APP_ID`).
- [ ] Weryfikacja, że release jest podpisany (`./gradlew bundleRelease` bez ostrzeżenia o braku signing config).

## 3. Bezpieczeństwo

- [x] Tylko HTTPS/TLS (`network_security_config.xml`, cleartext zablokowany).
- [x] Historia szyfrowana (SQLCipher + klucz w Android Keystore).
- [x] Brak sekretów w kodzie (BuildConfig / env / keystore.properties).
- [x] Minimalne uprawnienia (tylko aparat wymagany).
- [x] Weryfikacja URL przed otwarciem (heurystyki + Safe Browsing), brak auto-open.
- [ ] **Play Integrity API**: skonfigurowany w Play Console + backend weryfikujący werdykty (biblioteka jest już w zależnościach).
- [ ] Realny klucz Safe Browsing z limitem i alertami quota w Google Cloud.
- [ ] Test penetracyjny / przegląd bezpieczeństwa (`./gradlew` + `docs/SECURITY.md`).
- [ ] Skan podatności zależności (OWASP Dependency-Check / `dependabot`).

## 4. Prywatność i zgodność prawna

- [ ] **Polityka prywatności** opublikowana pod publicznym URL (szkic: `docs/PRIVACY_POLICY.md`).
- [ ] Link do polityki w Play Console **oraz** w aplikacji (ekran Ustawienia/O aplikacji).
- [ ] Sekcja **Data safety** w Play Console wypełniona zgodnie z rzeczywistością:
  - zbierane dane: historia skanów (lokalnie, szyfrowana), identyfikator reklamowy (jeśli reklamy),
  - brak udostępniania danych osobowych stronom trzecim poza dostawcami (AdMob/Play),
  - dane szyfrowane w spoczynku i w tranzycie, możliwość usunięcia z poziomu aplikacji.
- [ ] **Consent Management (IAB TCF / Google UMP)** dla reklam w regionach EOG.
- [ ] Deklaracja `AD_ID` (uprawnienie już w manifeście) w kwestionariuszu Play.
- [ ] RODO: mechanizm „usuń moje dane” dostępny w aplikacji (zaimplementowany w Ustawieniach).

## 5. Zgodność z politykami Google Play

- [ ] **Ads Policy**: częstotliwość interstitiali rozsądna (domyślnie co 5 skanów), brak reklam nakładających się na treść/przyciski, brak przypadkowych kliknięć.
- [ ] **Play Billing Policy**: cyfrowe funkcje premium sprzedawane wyłącznie przez Play Billing (zaimplementowane).
- [ ] **Permissions & APIs that Access Sensitive Information**: uzasadnienie użycia aparatu; brak zbędnych uprawnień.
- [ ] **Families Policy**: kategoria wiekowa ustawiona świadomie. Aplikacja nie jest kierowana do dzieci → nie uczestniczy w „Designed for Families”. Reklamy skonfigurowane jako **nie** child-directed.
- [ ] **Data deletion**: link do usuwania konta/danych (jeśli wymagany) w Play Console.

## 6. Metadane sklepowe

- [ ] Ikona 512×512 (adaptacyjna już w projekcie; przygotować PNG do sklepu).
- [ ] Grafika polecana (feature graphic) 1024×500.
- [ ] Zrzuty ekranu: telefon (min. 2–8) **oraz** tablet 7"/10".
- [ ] Krótki opis (≤ 80 znaków) i pełny opis (PL + EN).
- [ ] Kategoria: Narzędzia; tagi/słowa kluczowe.
- [ ] Dane kontaktowe dewelopera + adres e-mail wsparcia.

## 7. Ocena treści i testy

- [ ] Kwestionariusz **content rating** wypełniony (IARC).
- [ ] Ścieżki testowe: **internal testing → closed testing → production**.
- [ ] Pre-launch report (Play Console) bez krytycznych błędów/awarii.
- [ ] Testy na realnych urządzeniach (różne producenty aparatów, Android 8–15).
- [ ] Dostępność: TalkBack, kontrast, skalowanie tekstu (100–200%).
- [ ] i18n: weryfikacja PL i EN, brak przyciętych/utwardzonych stringów.

## 8. Jakość kodu (CI zielone)

- [x] `./gradlew detekt` — analiza statyczna.
- [x] `./gradlew testDebugUnitTest` — testy jednostkowe domeny/danych.
- [ ] `./gradlew connectedDebugAndroidTest` — testy instrumentowane/Compose UI (na emulatorze/urządzeniu).
- [x] GitHub Actions CI zielone na gałęzi wydania.

## 9. Po publikacji

- [ ] Monitoring awarii (Play Console → Android vitals / Crashlytics).
- [ ] Alerty na quota Safe Browsing / AdMob / Billing.
- [ ] Proces aktualizacji zależności (Dependabot) i reagowania na CVE.
