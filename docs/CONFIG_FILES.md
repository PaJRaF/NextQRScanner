# Pliki konfiguracyjne — pełna lista

## Build / Gradle

| Plik | Rola |
| --- | --- |
| `settings.gradle.kts` | Repozytoria (google, mavenCentral), moduł `:app`, `FAIL_ON_PROJECT_REPOS`. |
| `build.gradle.kts` (root) | Aliasy wtyczek (AGP, Kotlin, KSP, Hilt, detekt) w trybie `apply false`. |
| `app/build.gradle.kts` | SDK 26/35, Compose, R8 (`isMinifyEnabled`+`isShrinkResources`), podpisywanie z `keystore.properties`, `buildConfigField` dla sekretów, konfiguracja App Bundle, zależności. |
| `gradle/libs.versions.toml` | Katalog wersji — jedno źródło prawdy dla wszystkich bibliotek i wtyczek. |
| `gradle.properties` | `android.useAndroidX`, `nonTransitiveRClass`, R8 full mode, cache/parallel. |
| `gradle/wrapper/*` | Gradle Wrapper 8.11.1. |

## Sekrety (nigdy nie commitowane)

| Plik | Rola |
| --- | --- |
| `keystore.properties.sample` | Wzór; realny `keystore.properties` jest w `.gitignore`. Zawiera dane podpisu (upload key) oraz `SAFE_BROWSING_API_KEY`, `ADMOB_APP_ID`. |
| `.gitignore` | Wyklucza `keystore.properties`, `*.jks/*.keystore`, `google-services.json`, `local.properties`, buildy. |

W CI te same wartości pochodzą z zaszyfrowanych **GitHub Actions Secrets**
(`SAFE_BROWSING_API_KEY`, `ADMOB_APP_ID`) i są wstrzykiwane przez `env`.

## Android — manifest i bezpieczeństwo

| Plik | Rola |
| --- | --- |
| `app/src/main/AndroidManifest.xml` | Uprawnienia (tylko CAMERA wymagane; INTERNET/VIBRATE/READ_MEDIA_IMAGES/AD_ID), `networkSecurityConfig`, `allowBackup="false"`, reguły backupu, FileProvider, widget, meta-data AdMob (placeholder wstrzykiwany w buildzie). |
| `app/src/main/res/xml/network_security_config.xml` | Wymusza HTTPS (blokuje cleartext), ufa tylko systemowym CA (utrudnia MITM). |
| `app/proguard-rules.pro` | Reguły R8/ProGuard: `keep` dla Room/serialization/Hilt/ML Kit/Billing/ZXing, `-repackageclasses`, strip `android.util.Log` w release. |
| `app/src/main/res/xml/data_extraction_rules.xml` | Android 12+: wyklucza szyfrowaną bazę i secure prefs z backupu cloud/transfer. |
| `app/src/main/res/xml/backup_rules.xml` | Pre-12: analogiczne wykluczenia auto-backup. |
| `app/src/main/res/xml/file_paths.xml` | Ścieżki FileProvidera (udostępnianie wygenerowanych kodów / eksportów). |
| `app/src/main/res/xml/quick_scan_widget_info.xml` | Metadane widgetu ekranu głównego. |

## Zasoby

| Plik | Rola |
| --- | --- |
| `res/values/strings.xml` | Teksty (angielski — domyślny). |
| `res/values-pl/strings.xml` | Tłumaczenie polskie (gotowa struktura na kolejne języki). |
| `res/values/themes.xml`, `res/values-night/themes.xml` | Motyw okna + splash (light/dark). |
| `res/values/colors.xml` | Kolory bazowe / tło ikony. |
| `res/mipmap-anydpi-v26/ic_launcher*.xml` | Ikona adaptacyjna (+ monochrome). |
| `res/drawable/*` | Ikony wektorowe, tło widgetu. |
| `res/layout/widget_quick_scan.xml` | Layout widgetu (RemoteViews). |

## Jakość / CI

| Plik | Rola |
| --- | --- |
| `config/detekt/detekt.yml` | Reguły detekt (na bazie domyślnych). |
| `.github/workflows/ci.yml` | detekt → testy jednostkowe → `assembleDebug`; na `main` dodatkowo `bundleRelease` z sekretami. |
| `.github/dependabot.yml` | Cotygodniowe PR-y z aktualizacjami zależności (Gradle + Actions). |

## Do uzupełnienia przed publikacją

- `app/google-services.json` — jeśli włączasz Firebase/dodatkowe usługi Google (git-ignored).
- Produkcyjne identyfikatory jednostek reklamowych AdMob (`AdsManager.PROD_*`).
- Identyfikatory produktów Play Billing (`BillingRepositoryImpl.PRODUCT_*`) zgodne z Play Console.
- Realny `SAFE_BROWSING_API_KEY` (Google Cloud Console → Safe Browsing API).
