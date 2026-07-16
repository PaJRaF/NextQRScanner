# Przewodnik „za rękę”: od działającej aplikacji do zarabiania

Prowadzę Cię przez wszystko po kolei. Kolejność ma znaczenie — nie przeskakuj
kroków. Przy każdym punkcie: **co zrobić**, **link** i **gdzie w kodzie** coś
podmienić (ścieżka pliku + nazwa stałej).

Dwa źródła przychodu w tej aplikacji:
- **Reklamy (AdMob)** — płacisz za nie użytkownik „za darmo”, Ty dostajesz kasę
  za wyświetlenia/kliknięcia.
- **Premium (Play Billing)** — jednorazowy zakup lub subskrypcja usuwająca reklamy
  i odblokowująca funkcje.

---

## KROK 0 — Ustal FINALNY identyfikator aplikacji (nieodwracalne!)

`applicationId` to unikalny identyfikator w Google Play. **Po pierwszej publikacji
NIE DA SIĘ go zmienić.** Teraz jest testowy: `com.nextqr.scanner`.

- **Gdzie:** [`app/build.gradle.kts`](../app/build.gradle.kts) linia 44:
  `applicationId = "com.nextqr.scanner"`
- Zmień na swój, w formacie odwróconej domeny, np. `com.twojanazwa.qrscanner`.
  Nie musisz mieć tej domeny — ważne, żeby był unikalny i „Twój”.
- To samo dotyczy `namespace` (linia wyżej) — może zostać `com.nextqr.scanner`
  (to nazwa pakietu w kodzie), ale dla porządku zwykle ustawia się taki sam.
  Jeśli zmieniasz `namespace`, musisz przenieść pakiety źródłowe — **na start
  zmień TYLKO `applicationId`, `namespace` zostaw.**

> Jeśli nie masz pewności co do nazwy — to jedyny krok, przy którym warto się
> zatrzymać i przemyśleć. Reszta jest odwracalna.

---

## KROK 1 — Konto Google Play Developer + profil płatności

Bez tego nic nie opublikujesz ani nie dostaniesz pieniędzy.

1. Załóż konto dewelopera (jednorazowo **25 USD**, weryfikacja tożsamości, bywa
   1–2 dni): <https://play.google.com/console/signup>
2. Załóż **profil płatności** (Payments profile) — to tu wpływają pieniądze z
   Play Billing: Play Console → **Setup → Payments profile**.
3. Uzupełnij dane podatkowe i bankowe. Bez tego Google wstrzyma wypłaty.

---

## KROK 2 — Utwórz aplikację w Play Console i zrób pierwszy upload

AdMob i Billing wymagają, aby aplikacja **istniała w Play Console** i miała
wgrany podpisany plik `.aab` na jakiejś ścieżce testowej.

1. Play Console → **Create app**: nazwa, język, typ „App”, darmowa.
2. Zbuduj **podpisany** App Bundle (jeśli jeszcze nie masz klucza — patrz
   [`docs/BUILD_AND_RELEASE.md`](BUILD_AND_RELEASE.md), sekcja C):
   ```bash
   ./gradlew bundleRelease
   # wynik: app/build/outputs/bundle/release/app-release.aab
   ```
3. Play Console → **Testing → Internal testing → Create new release** → wgraj
   `app-release.aab`. Zaakceptuj **Play App Signing** (Google zarządza kluczem
   finalnym; Ty wgrywasz swoim „upload key”).
4. Dodaj siebie jako testera (swój e-mail), zapisz link do testów.

Od tej chwili aplikacja „istnieje” dla AdMob i Billing.

---

## KROK 3 — AdMob (reklamy) 💰

### 3.1. Konto i podpięcie aplikacji
1. Załóż konto AdMob: <https://admob.google.com>
2. AdMob → **Apps → Add app** → wybierz „Yes, it's on Google Play” i wyszukaj
   swoją aplikację po `applicationId` z Kroku 0 (dlatego najpierw Play Console).

### 3.2. Skopiuj App ID
Po dodaniu aplikacji AdMob pokaże **App ID** w formacie
`ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY` (ze znakiem `~`).

- **Gdzie wkleić:** plik `keystore.properties` (skopiowany z
  [`keystore.properties.sample`](../keystore.properties.sample)):
  ```properties
  ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
  ```
- Ten plik jest git-ignored. Wartość trafia automatycznie do manifestu (przez
  placeholder) — nic więcej nie musisz robić. Domyślka testowa jest w
  [`app/build.gradle.kts`](../app/build.gradle.kts) linie 62 i 66.

### 3.3. Utwórz jednostki reklamowe (Ad units)
AdMob → Twoja aplikacja → **Ad units → Add ad unit**. Utwórz **trzy**:
- **Banner** (Banner)
- **Interstitial** (Interstitial)
- **Rewarded** (Rewarded) — pod odblokowanie premium na czas za obejrzenie reklamy.

Każda da ID w formacie `ca-app-pub-XXXX/ZZZZZZZZZZ` (ze znakiem `/`).

- **Gdzie wkleić:** [`app/src/main/java/com/nextqr/scanner/data/ads/AdsManager.kt`](../app/src/main/java/com/nextqr/scanner/data/ads/AdsManager.kt),
  linie 128–130 — podmień placeholdery `0000...`:
  ```kotlin
  const val PROD_INTERSTITIAL = "ca-app-pub-XXXX/twoje_id_interstitial"
  const val PROD_BANNER       = "ca-app-pub-XXXX/twoje_id_banner"
  const val PROD_REWARDED     = "ca-app-pub-XXXX/twoje_id_rewarded"
  ```
- **Nie ruszaj** stałych `TEST_*` — build debug ich używa (bezpieczne testy).
  Przełącznik jest automatyczny: `USE_TEST_ADS` = `true` w debug, `false` w
  release (patrz `app/build.gradle.kts`, buildTypes).

### 3.4. app-ads.txt (żeby nie tracić przychodu)
Aby reklamodawcy ufali Twojemu ruchowi, opublikuj plik `app-ads.txt` na domenie,
którą podasz w Play Console jako „Website” dewelopera.
- Instrukcja: <https://support.google.com/admob/answer/9363762>
- AdMob wygeneruje dokładną treść (jedna linijka z Twoim wydawcą).

### 3.5. Kadencja i polityka (WAŻNE — nie daj sobie zablokować konta)
- Interstitial pokazuje się **co 5 skanów** — zmienisz w
  [`AdsManager.kt`](../app/src/main/java/com/nextqr/scanner/data/ads/AdsManager.kt)
  linia 120: `SCANS_PER_INTERSTITIAL = 5`.
- **NIGDY nie klikaj własnych reklam** i nie proś o to znajomych — Google banuje
  konta AdMob za „invalid traffic” bezpowrotnie. Do testów służą `TEST_*` (debug).
- Reklamy nie mogą zasłaniać przycisków ani zaskakiwać — obecna implementacja
  jest zgodna (interstitial tylko między skanami, baner osobno).

---

## KROK 4 — Play Billing (Premium) 💳

### 4.1. Utwórz produkty w Play Console
Produkty muszą mieć **dokładnie te same ID** co w kodzie, inaczej paywall będzie
pusty.

- **ID oczekiwane przez kod** —
  [`app/src/main/java/com/nextqr/scanner/data/billing/BillingRepositoryImpl.kt`](../app/src/main/java/com/nextqr/scanner/data/billing/BillingRepositoryImpl.kt)
  linie 216–218:
  | Stała w kodzie | ID produktu | Typ w Play Console |
  | --- | --- | --- |
  | `PRODUCT_LIFETIME` | `premium_lifetime` | In-app product (jednorazowy) |
  | `PRODUCT_SUB_MONTHLY` | `premium_monthly` | Subscription (miesięczna) |
  | `PRODUCT_SUB_YEARLY` | `premium_yearly` | Subscription (roczna) |

Gdzie w Play Console:
- Jednorazowy: **Monetize → Products → In-app products → Create product**, ID =
  `premium_lifetime`, ustaw cenę, **Activate**.
- Subskrypcje: **Monetize → Products → Subscriptions → Create subscription**,
  ID = `premium_monthly` oraz `premium_yearly`, dodaj „base plan” + cenę,
  **Activate**.

> Możesz oczywiście użyć własnych ID — wtedy zmień je w kodzie (linie 216–218)
> tak, by były identyczne z Play Console. Najprościej: użyj tych domyślnych.

### 4.2. Warunek konieczny: aplikacja na ścieżce testowej
Billing działa dopiero, gdy podpisany build (ten sam `applicationId` i podpis) jest
wgrany na **Internal testing** (Krok 2). Testujesz na koncie dodanym jako tester.

### 4.3. Testerzy licencyjni (kupujesz „na niby”, bez prawdziwej płatności)
- Play Console → **Setup → License testing** → dodaj swój e-mail Google.
- Wtedy zakupy przechodzą w trybie testowym (karta nie jest obciążana).
- Dokumentacja: <https://developer.android.com/google/play/billing/test>

---

## KROK 5 — Obowiązkowe sekcje w Play Console (bez nich brak publikacji)

Play Console → **Policy → App content** — wypełnij wszystko:

1. **Polityka prywatności (URL)** — musisz ją opublikować publicznie. Masz gotowy
   szkic: [`docs/PRIVACY_POLICY.md`](PRIVACY_POLICY.md). Najprościej hostować za
   darmo na **GitHub Pages** (Settings repo → Pages) albo na własnej stronie.
   Uzupełnij pola `[…]` w szkicu (administrator, e-mail).
   - Link wklejasz w Play Console **oraz** warto dodać go w aplikacji (patrz
     „Dodatki do rozważenia” niżej).
2. **Data safety** — zadeklaruj: zbierane dane (historia skanów lokalnie,
   identyfikator reklamowy), szyfrowanie w spoczynku/tranzycie, możliwość
   usunięcia danych. Ściąga: [`docs/PRE_LAUNCH_CHECKLIST.md`](PRE_LAUNCH_CHECKLIST.md) sekcja 4.
   - Info: <https://support.google.com/googleplay/android-developer/answer/10787469>
3. **Ads** — zaznacz „Tak, aplikacja zawiera reklamy”.
4. **Content rating** — wypełnij kwestionariusz (IARC).
5. **Target audience** — ustaw wiek (aplikacja **nie** dla dzieci → poza „Designed
   for Families”).
6. **App access** — jeśli funkcje są za loginem, podaj dostęp recenzentowi
   (tu nie ma loginu, więc „All functionality available without restrictions”).

---

## KROK 6 — Zgoda na reklamy w UE (RODO/UMP) — zalecane przed produkcją

W Europejskim Obszarze Gospodarczym reklamy spersonalizowane wymagają zgody przez
**Google User Messaging Platform (UMP)**. W aplikacji jest już przełącznik zgody
w Ustawieniach, ale **do pełnej zgodności z EOG zaleca się integrację UMP SDK**
(wyświetla oficjalny formularz zgody i przekazuje sygnał do AdMob).

- Dokumentacja: <https://developers.google.com/admob/android/privacy>
- W AdMob: **Privacy & messaging → European regulations** → utwórz komunikat zgody.
- Integracja w kodzie to ~40 linii (SDK `com.google.android.ump:user-messaging-platform`).
  **Mogę to dodać za Ciebie** — daj znać.

---

## KROK 7 — Wypuść wersję produkcyjną

1. Podbij wersję w [`app/build.gradle.kts`](../app/build.gradle.kts) linie 47–48
   przy każdym kolejnym wydaniu:
   ```kotlin
   versionCode = 2          // ZAWSZE +1 względem poprzedniego uploadu
   versionName = "1.0.1"    // widoczne dla użytkownika
   ```
2. Zbuduj: `./gradlew bundleRelease`.
3. Play Console → **Production → Create new release** → wgraj `.aab` → opis zmian.
4. Uzupełnij **Store listing**: ikona 512×512, feature graphic 1024×500, zrzuty
   ekranu (telefon + tablet), opis PL i EN.
5. Wyślij do recenzji. Pierwsza recenzja: od kilku godzin do kilku dni.
   Nowe konta bywają dodatkowo weryfikowane (czasem wymóg 14 dni testów zamkniętych
   z min. 12 testerami — Google pokaże, jeśli Cię dotyczy).

---

## KROK 8 — Kiedy i jak dostajesz pieniądze

- **AdMob:** wypłata po przekroczeniu **progu 100 USD** (miesięcznie, ~21. dnia
  następnego miesiąca), na konto podane w AdMob → Payments. Musisz mieć uzupełnione
  dane podatkowe i zweryfikowany adres (Google wysyła PIN).
- **Play Billing (Premium):** wypłata miesięczna z profilu płatności (Krok 1),
  po potrąceniu prowizji Google (15% dla większości małych deweloperów w programie
  „15% do 1 mln USD”, subskrypcje 15% od początku).
- Podatki: uzupełnij formularze podatkowe w obu panelach (AdMob i Play), inaczej
  wypłaty są wstrzymane.

---

## Ściąga: wszystkie miejsca w kodzie do podmiany

| Co | Plik | Linia / stała |
| --- | --- | --- |
| Finalny `applicationId` | `app/build.gradle.kts` | 44 |
| `versionCode` / `versionName` | `app/build.gradle.kts` | 47–48 |
| AdMob **App ID** | `keystore.properties` | `ADMOB_APP_ID=` |
| AdMob **unit IDs** (prod) | `data/ads/AdsManager.kt` | 128–130 (`PROD_*`) |
| Kadencja interstitiali | `data/ads/AdsManager.kt` | 120 (`SCANS_PER_INTERSTITIAL`) |
| ID produktów premium | `data/billing/BillingRepositoryImpl.kt` | 216–218 (`PRODUCT_*`) |
| Klucz Safe Browsing (bezpieczeństwo) | `keystore.properties` | `SAFE_BROWSING_API_KEY=` |
| URL polityki prywatności | Play Console + (opcjonalnie) ekran Ustawień | — |

---

## Dodatki do rozważenia (mogę dodać na życzenie)

1. **Link do polityki prywatności + wersja aplikacji w Ustawieniach** — Google to
   lubi, a użytkownicy tego oczekują. ~15 linii w `SettingsScreen.kt`.
2. **Integracja UMP (zgoda EOG)** — patrz Krok 6.
3. **Reklama rewarded „odblokuj premium na 24h”** — logika `grantRewardedUnlock`
   już jest w `BillingRepositoryImpl`; brakuje loadera rewarded w `AdsManager`.
4. **GitHub Actions auto-upload do Play** (Fastlane / `r0adkll/upload-google-play`)
   — automatyczna publikacja z CI po podpięciu klucza serwisowego Play.

Napisz, które z tych chcesz — dorobię.
