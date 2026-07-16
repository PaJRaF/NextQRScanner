# Build, test na telefonie i publikacja w Google Play

Praktyczny przewodnik „od zera do produkcji”. Dwie ścieżki: **A) Android Studio**
(najprościej, zalecane) oraz **B) wiersz poleceń** (dla CI / zaawansowanych).

---

## 0. Wymagania wstępne

Zainstaluj **jedno** z poniższych:

- **Android Studio** (Ladybug/Koala lub nowszy) — zawiera Android SDK, JDK i
  emulator. To najprostsza droga.
- **albo** ręcznie: **JDK 17** + **Android SDK Command-line Tools** (`sdkmanager`).

Pierwsza kompilacja pobiera ~1–2 GB (dystrybucja Gradle + zależności) — potrzebny
internet.

---

## A) Ścieżka Android Studio (zalecana do testów na telefonie)

1. **Otwórz projekt**: File → Open → wskaż katalog `NextQRScanner`. Studio samo
   utworzy `local.properties` ze ścieżką do SDK i pobierze zależności (Gradle sync).
   Jeśli poprosi o instalację brakujących pakietów SDK (compileSdk 35, build-tools)
   — zgódź się.
2. **Podłącz telefon** kablem USB i włącz na nim **Opcje programisty → Debugowanie USB**
   (Ustawienia → Informacje → 7× tap w „Numer kompilacji”, potem Ustawienia →
   System → Opcje programisty → USB debugging). Na telefonie potwierdź „Zezwól na
   debugowanie USB”.
   - Nie masz telefonu pod ręką? Utwórz emulator: Device Manager → Create Device.
3. **Uruchom**: wybierz urządzenie na górnym pasku i kliknij ▶ **Run 'app'**.
   Studio zbuduje wariant **debug**, zainstaluje i uruchomi aplikację.
   - Wariant debug działa od ręki: używa **testowych** jednostek reklam AdMob,
     a `applicationId` ma sufiks `.debug` (może współistnieć z wersją release).

To wystarczy, żeby przetestować skanowanie/generowanie na własnym telefonie.

---

## B) Ścieżka wiersza poleceń

### B1. Wskaż SDK

Utwórz `local.properties` w katalogu głównym (jest w `.gitignore`):

```properties
sdk.dir=/ścieżka/do/Android/Sdk
```

albo ustaw zmienną środowiskową `ANDROID_HOME` / `ANDROID_SDK_ROOT`.

### B2. Zbuduj debug APK i zainstaluj przez adb

```bash
./gradlew assembleDebug
# Wynik: app/build/outputs/apk/debug/app-debug.apk

adb devices                 # sprawdź, że telefon jest widoczny
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Nie masz `adb`? Jest w `Android/Sdk/platform-tools/`. Możesz też po prostu
przesłać plik `app-debug.apk` na telefon (np. przez chmurę) i zainstalować,
pozwalając na „instalację z nieznanych źródeł”.

### B3. Testy i analiza statyczna

```bash
./gradlew testDebugUnitTest        # testy jednostkowe (domena/dane)
./gradlew detekt                   # analiza statyczna
./gradlew connectedDebugAndroidTest  # testy UI/instrumentowane (potrzebny telefon/emulator)
```

---

## C) Przygotowanie do publikacji (wersja release)

### C1. Wygeneruj klucz podpisu (upload key) — raz

```bash
keytool -genkeypair -v \
  -keystore release-upload.jks \
  -alias upload \
  -keyalg RSA -keysize 2048 -validity 9125 \
  -storetype JKS
```

Zapamiętaj hasła i **przechowuj `release-upload.jks` bezpiecznie poza repo**
(nigdy go nie commituj — jest już w `.gitignore`).

### C2. Uzupełnij sekrety

```bash
cp keystore.properties.sample keystore.properties
```

Edytuj `keystore.properties`:

```properties
storeFile=../release-upload.jks
storePassword=TWOJE_HASŁO
keyAlias=upload
keyPassword=TWOJE_HASŁO
SAFE_BROWSING_API_KEY=twój_klucz_z_Google_Cloud   # opcjonalnie na start
ADMOB_APP_ID=ca-app-pub-XXXX~YYYY                  # z konsoli AdMob
```

### C3. Zbuduj podpisany App Bundle (.aab do Google Play)

```bash
./gradlew bundleRelease
# Wynik: app/build/outputs/bundle/release/app-release.aab
```

> Google Play przyjmuje **`.aab`**, nie `.apk`. Jeśli chcesz przetestować dokładnie
> wersję release na telefonie, użyj narzędzia **bundletool** do wygenerowania
> APK-ów z bundla:
> ```bash
> java -jar bundletool.jar build-apks --bundle=app-release.aab \
>   --output=app.apks --mode=universal \
>   --ks=release-upload.jks --ks-key-alias=upload
> java -jar bundletool.jar install-apks --apks=app.apks
> ```

---

## D) Google Play Console — pierwsza publikacja

1. **Załóż konto dewelopera** na <https://play.google.com/console> (jednorazowa
   opłata 25 USD, weryfikacja tożsamości).
2. **Create app**: nazwa, język, typ (App), darmowa/płatna, akceptacja polityk.
3. **Play App Signing**: przy pierwszym uploadzie Google poprosi o zgodę na
   zarządzanie kluczem aplikacji — zaakceptuj. Ty wgrywasz `.aab` podpisany
   **upload key** (z kroku C1); Google podpisuje finalną wersję dla użytkowników.
4. **Utwórz ścieżkę testową** (zalecane w tej kolejności):
   - **Internal testing** → dodaj `app-release.aab`, dodaj testerów (e-maile),
     udostępnij link. Najszybsze (dostępne w minuty).
   - potem **Closed testing** (szerszy krąg) → **Production**.
5. **Wypełnij wymagane sekcje** (bez nich nie opublikujesz):
   - **App content**: polityka prywatności (publiczny URL — patrz
     `docs/PRIVACY_POLICY.md`), **Data safety**, **Content rating** (kwestionariusz),
     grupa docelowa/wiek, reklamy (Tak), dostęp dla recenzenta.
   - **Store listing**: krótki i pełny opis (PL+EN), ikona 512×512, feature
     graphic 1024×500, zrzuty ekranu (telefon + tablet).
6. **Wyślij do recenzji**. Internal testing zwykle bez pełnej recenzji; produkcja
   przechodzi review Google (od kilku godzin do kilku dni).

Pełna lista kontrolna: **`docs/PRE_LAUNCH_CHECKLIST.md`**.

---

## E) Zanim wyślesz na produkcję — do uzupełnienia w kodzie

Te miejsca mają wartości testowe/placeholdery i trzeba je podmienić:

| Co | Gdzie |
| --- | --- |
| Produkcyjne ID jednostek reklamowych AdMob | `data/ads/AdsManager.kt` → `PROD_INTERSTITIAL/BANNER/REWARDED` |
| `ADMOB_APP_ID` | `keystore.properties` / sekret CI (nie w kodzie) |
| Produkty premium (SKU) | `data/billing/BillingRepositoryImpl.kt` → `PRODUCT_*` + utwórz je w Play Console → Products |
| Klucz Safe Browsing | Google Cloud Console → włącz Safe Browsing API → `SAFE_BROWSING_API_KEY` |
| Play Integrity | Skonfiguruj w Play Console → App integrity |

---

## F) Najczęstsze problemy

- **`SDK location not found`** → brak `local.properties`/`ANDROID_HOME` (krok B1).
- **`Failed to install ... INSTALL_FAILED`** → włącz debugowanie USB, potwierdź
  fingerprint na telefonie, użyj `adb install -r`.
- **Build zawiesza się na pobieraniu** → pierwsza kompilacja pobiera Gradle i
  zależności; potrzebny stabilny internet i ~2 GB miejsca.
- **`Minimum supported Gradle version`** → używaj dołączonego `./gradlew`
  (wrapper 8.11.1), nie systemowego `gradle`.
- **CI: build release** → ustaw sekrety `SAFE_BROWSING_API_KEY`, `ADMOB_APP_ID`
  w GitHub → Settings → Secrets (workflow już je czyta).
