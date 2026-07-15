# Polityka prywatności — NextQR Scanner

**Wersja:** 1.0 · **Data wejścia w życie:** _[UZUPEŁNIĆ DATĘ]_

> Szkic gotowy do publikacji. Przed opublikowaniem uzupełnij pola oznaczone
> `[…]` (administrator danych, dane kontaktowe, jurysdykcja) i skonsultuj tekst
> z osobą odpowiedzialną za zgodność prawną. Politykę należy udostępnić pod
> publicznym adresem URL i podlinkować w Google Play Console oraz w aplikacji.

## 1. Administrator danych

Administratorem danych osobowych w rozumieniu RODO (rozporządzenie (UE) 2016/679)
jest **[NAZWA / IMIĘ I NAZWISKO ADMINISTRATORA]**, _[ADRES]_.
Kontakt w sprawach prywatności: **[ADRES E-MAIL]**.

## 2. Zasada nadrzędna: prywatność w projekcie

NextQR Scanner został zaprojektowany tak, aby **przetwarzać jak najmniej danych**.
Skanowanie i rozpoznawanie kodów odbywa się **w całości na Twoim urządzeniu**
(on-device, offline) — obrazy z aparatu **nigdy nie są wysyłane** na nasze ani
zewnętrzne serwery. Historia skanów jest przechowywana **wyłącznie lokalnie** i
**szyfrowana** na urządzeniu.

## 3. Jakie dane przetwarzamy

### 3.1. Dane przetwarzane wyłącznie lokalnie (nie opuszczają urządzenia)

- **Zawartość zeskanowanych/wygenerowanych kodów** oraz metadane (typ, znacznik
  czasu, oznaczenie „ulubione”) — zapisywane w lokalnej, **szyfrowanej** bazie
  (SQLCipher, klucz w sprzętowym Android Keystore). Możesz wyłączyć zapisywanie
  historii oraz usunąć wszystkie dane w Ustawieniach.
- **Ustawienia aplikacji** (motyw, wibracja, zgody) — w szyfrowanych preferencjach.

### 3.2. Dane wysyłane do usług zewnętrznych (tylko w określonych sytuacjach)

- **Weryfikacja bezpieczeństwa linków (Google Safe Browsing).** Gdy zeskanujesz
  kod zawierający adres URL i funkcja „Sprawdzaj bezpieczeństwo linków” jest
  włączona, **adres URL** jest wysyłany do Google Safe Browsing w celu sprawdzenia
  reputacji. Nie wysyłamy przy tym Twojego identyfikatora ani innych danych
  osobowych. Funkcję można wyłączyć w Ustawieniach (wówczas stosowane są tylko
  heurystyki offline).
- **Reklamy (Google AdMob) — tylko w wersji darmowej.** W celu wyświetlania
  reklam Google może przetwarzać **identyfikator reklamowy** oraz ograniczone
  dane techniczne/urządzenia. Reklamy spersonalizowane są wyświetlane **wyłącznie
  po wyrażeniu zgody** (zarządzanie zgodą zgodne z IAB TCF / Google UMP); bez
  zgody wyświetlane są reklamy niespersonalizowane.
- **Płatności (Google Play Billing) — zakup wersji Premium.** Transakcje obsługuje
  Google Play; nie przetwarzamy ani nie przechowujemy danych karty płatniczej.
- **Weryfikacja integralności (Google Play Integrity).** W celu ochrony przed
  nadużyciami aplikacja może poprosić Google o token integralności instalacji.

## 4. Uprawnienia aplikacji

- **Aparat** — wymagany do skanowania kodów. Obraz jest przetwarzany lokalnie.
- **Dostęp do zdjęć (READ_MEDIA_IMAGES)** — opcjonalnie, do skanowania kodu z
  obrazu z galerii; na nowszych systemach używany jest Photo Picker (bez trwałego
  uprawnienia).
- **Internet / stan sieci** — do weryfikacji linków, reklam, płatności i Play Integrity.
- **Wibracje** — sygnalizacja udanego skanu.
- **Identyfikator reklamowy (AD_ID)** — dla reklam w wersji darmowej.

Aplikacja **nie** żąda dostępu do lokalizacji, kontaktów ani mikrofonu. Akcje typu
połączenie z Wi-Fi, dodanie kontaktu czy wydarzenia w kalendarzu są wykonywane
przez systemowe aplikacje (przez `Intent`), a nie przez samą aplikację.

## 5. Podstawy prawne przetwarzania (RODO)

- **Art. 6 ust. 1 lit. b** — wykonanie usługi (skanowanie/generowanie, zakup Premium).
- **Art. 6 ust. 1 lit. f** — uzasadniony interes (bezpieczeństwo: weryfikacja
  linków, ochrona przed nadużyciami, podstawowe działanie reklam).
- **Art. 6 ust. 1 lit. a** — zgoda (reklamy spersonalizowane; można ją w każdej
  chwili wycofać w Ustawieniach).

## 6. Odbiorcy danych

Dostawcy usług działający jako niezależni administratorzy lub podmioty
przetwarzające: **Google** (Safe Browsing, AdMob, Play Billing, Play Integrity).
Nie sprzedajemy danych osobowych.

## 7. Przekazywanie poza EOG

Usługi Google mogą przetwarzać dane poza Europejskim Obszarem Gospodarczym.
Przekazywanie odbywa się w oparciu o mechanizmy zgodne z RODO (m.in. standardowe
klauzule umowne). Szczegóły: polityka prywatności Google.

## 8. Okres przechowywania

- Historia skanów i ustawienia — do czasu ich usunięcia przez Ciebie lub
  odinstalowania aplikacji (dane lokalne).
- Dane po stronie Google — zgodnie z politykami Google.

## 9. Twoje prawa

Masz prawo do: dostępu, sprostowania, usunięcia, ograniczenia i przenoszenia
danych, sprzeciwu wobec przetwarzania oraz wycofania zgody. Ponieważ dane
aplikacji są lokalne, większość praw realizujesz bezpośrednio: funkcja
**„Usuń wszystkie moje dane”** w Ustawieniach trwale kasuje szyfrowaną historię.
W sprawach dotyczących danych po stronie dostawców skontaktuj się z nami:
**[ADRES E-MAIL]**. Masz też prawo wniesienia skargi do organu nadzorczego
(w Polsce: Prezes UODO).

## 10. Dzieci

Aplikacja nie jest kierowana do dzieci i nie zbiera świadomie danych osób poniżej
16 roku życia. Reklamy są konfigurowane jako **nie** kierowane do dzieci.

## 11. Bezpieczeństwo

Stosujemy m.in.: szyfrowanie danych w spoczynku (SQLCipher + Android Keystore),
wymuszony HTTPS/TLS, minimalizację uprawnień, obfuskację kodu (R8) oraz brak
logowania danych osobowych w wersji produkcyjnej.

## 12. Zmiany polityki

O istotnych zmianach poinformujemy przez aktualizację tej strony i, w razie
potrzeby, komunikat w aplikacji. Data ostatniej aktualizacji widnieje na górze.

---

_Wersja angielska: opublikuj równoległe tłumaczenie EN pod tym samym adresem
(sekcja/podstrona), aby spełnić wymóg wielojęzyczności Google Play._
