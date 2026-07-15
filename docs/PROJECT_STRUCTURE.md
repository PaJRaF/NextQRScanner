# Struktura projektu i opis modułów

Projekt jest pojedynczym modułem Gradle (`:app`) z **wewnętrznym podziałem na
warstwy Clean Architecture** realizowanym przez pakiety. Taki układ jest w pełni
buildowalny „od zera”, a jednocześnie utrzymuje ścisłe granice zależności:
`presentation → domain ← data` (warstwa `domain` nie zna Androida ani żadnej
biblioteki zewnętrznej poza `javax.inject`).

> Migracja do prawdziwych modułów Gradle (`:core:domain`, `:core:data`,
> `:feature:scanner`, …) jest prosta, bo pakiety już odzwierciedlają docelowe
> moduły — patrz sekcja „Ścieżka do multi-module” na końcu.

```
NextQRScanner/
├── build.gradle.kts               # konfiguracja root (aliasy wtyczek)
├── settings.gradle.kts            # repozytoria + moduł :app
├── gradle/libs.versions.toml      # katalog wersji (single source of truth)
├── keystore.properties.sample     # wzór sekretów (realny plik jest git-ignored)
├── config/detekt/detekt.yml       # reguły analizy statycznej
├── .github/
│   ├── workflows/ci.yml           # CI/CD: detekt, testy, build, bundle
│   └── dependabot.yml             # cotygodniowe aktualizacje zależności
├── docs/                          # dokumentacja wyjściowa (ten katalog)
└── app/
    ├── build.gradle.kts           # moduł aplikacji (SDK, R8, podpisywanie, deps)
    ├── proguard-rules.pro         # reguły R8/ProGuard (release)
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── res/               # zasoby (strings PL/EN, motywy, ikony, xml)
        │   └── java/com/nextqr/scanner/
        │       ├── NextQrApplication.kt   # @HiltAndroidApp + WorkManager
        │       ├── MainActivity.kt        # host Compose + splash + motyw
        │       ├── MainViewModel.kt
        │       ├── di/            # moduły Hilt
        │       ├── domain/        # WARSTWA DOMENOWA (czysty Kotlin)
        │       ├── data/          # WARSTWA DANYCH (implementacje)
        │       ├── presentation/  # WARSTWA UI (Compose + ViewModel)
        │       ├── util/          # akcje treści, share, MediaStore
        │       └── widget/        # widget ekranu głównego
        ├── test/                  # testy jednostkowe (JVM)
        └── androidTest/           # testy instrumentowane / Compose UI
```

## Warstwy (logiczne moduły)

### `domain` — reguły biznesowe (bez zależności od Androida)
Serce aplikacji; w docelowym multi-module byłby to `:core:domain`.

- `model/` — modele: `ScanResult`, `ParsedContent` (sealed), `BarcodeType`,
  `UrlSafety`/`UrlWarning`, `QrGenerationOptions`, `PremiumStatus`, `AppSettings`.
- `repository/` — **interfejsy** repozytoriów (`ScanHistoryRepository`,
  `UrlSecurityRepository`, `SettingsRepository`, `BillingRepository`).
- `usecase/` — logika aplikacyjna:
  - `BarcodeContentParser` — parsowanie surowej treści na strukturę (Wi-Fi,
    vCard/MeCard, VEVENT, mailto/MATMSG, SMS, tel, geo, crypto, EPC/SEPA, URL…).
  - `UrlHeuristics` — offline'owe wykrywanie podejrzanych wzorców URL.
  - `ClassifyContentUseCase`, `VerifyUrlUseCase`, `SaveScanUseCase`.
  - `BarcodeContentParser` i `UrlHeuristics` to **czysty Kotlin** → pokryte
    szybkimi testami JVM (17 testów).

### `data` — implementacje (Android + biblioteki)
W multi-module: `:core:data`.

- `local/` — Room: `ScanEntity`, `ScanDao`, `ScanDatabase`.
- `security/` — `DatabaseKeyProvider` (Keystore + EncryptedSharedPreferences,
  passphrase SQLCipher).
- `remote/` — `SafeBrowsingApi` (Retrofit) + DTO.
- `repository/` — implementacje interfejsów domenowych
  (`ScanHistoryRepositoryImpl`, `UrlSecurityRepositoryImpl`,
  `SettingsRepositoryImpl`).
- `qr/` — `QrEncoder` (ZXing → `Bitmap`, kolory/logo/kształt modułów).
- `scanner/` — `BarcodeAnalyzer` (CameraX `ImageAnalysis.Analyzer` + ML Kit),
  `BarcodeMapper`.
- `billing/` — `BillingRepositoryImpl` (Play Billing), `CurrentActivityHolder`.
- `ads/` — `AdsManager` (kadencja interstitiali, respekt dla premium + consent).

### `presentation` — UI (Compose + MVVM)
W multi-module: moduły `:feature:*`.

- `theme/` — `NextQrTheme`, kolory, typografia (dynamic color, dark/light).
- `navigation/` — `Routes`, `TopLevelDestination`.
- `NextQrApp.kt` — `NavHost` + dolny pasek nawigacji.
- Ekrany (każdy: `*Screen.kt` + `*ViewModel.kt`):
  `scanner/`, `generator/`, `history/`, `settings/`, `premium/`,
  `onboarding/`, `detail/`.
- `components/` — `SecurityBanner`, `BannerAd` i inne współdzielone elementy.

### `util`, `widget`, `di`
- `util/` — `ContentActions` (bezpieczne akcje przez `Intent`), `ShareUtils`,
  `MediaStoreUtils`.
- `widget/` — `QuickScanWidgetProvider` (widget szybkiego skanu).
- `di/` — `DatabaseModule`, `NetworkModule`, `RepositoryModule`.

## Przepływ danych (przykład: skan URL)

```
CameraX frame ─▶ BarcodeAnalyzer (ML Kit, on-device)
             ─▶ ScannerViewModel.onBarcodeDetected
                  ├─ ClassifyContentUseCase → ParsedContent.Url
                  ├─ VerifyUrlUseCase → UrlHeuristics + SafeBrowsingApi → UrlSafety
                  └─ SaveScanUseCase → (Room + SQLCipher)
             ─▶ ScanResultSheet: pełny URL + SecurityBanner (BRAK auto-open)
             ─▶ użytkownik potwierdza → ContentActions.perform (Intent.ACTION_VIEW)
```

## Ścieżka do multi-module (opcjonalnie)

Pakiety 1:1 odpowiadają docelowym modułom:

| Pakiet | Docelowy moduł Gradle |
| --- | --- |
| `domain/**` | `:core:domain` (kotlin-jvm) |
| `data/**` + `di/**` | `:core:data` (android-library) |
| `presentation/scanner` | `:feature:scanner` |
| `presentation/generator` | `:feature:generator` |
| `presentation/history` | `:feature:history` |
| `presentation/{settings,premium,onboarding,detail}` | odpowiednie `:feature:*` |

Wydzielenie polega na przeniesieniu pakietów do modułów i dodaniu wpisów w
`settings.gradle.kts` — bez zmian w logice.
