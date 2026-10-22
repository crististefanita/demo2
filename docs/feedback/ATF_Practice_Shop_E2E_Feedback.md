# Practice Shop E2E — feedback și reconcilieri

## Overall

Feedback-ul a fost verificat împotriva codului, raportului și aplicației live.
Contractele de componentă sunt implementate, dar replay-ul manual cu fixture-ul
exact din raportul PASS a fost respins de aplicația curentă la submit-ul comenzii.
Prin urmare, dovada live nu este închisă și artifactul nu este reproductibil
cap-coadă cu valorile pe care le raportează.

| Zonă | Stare | Dovadă | Rămâne |
|---|---|---|---|
| confirmarea comenzii și corelarea invoice | implementat în cod; live neconfirmat | fence după `Confirm` + assertions UI/API; exact replay manual a primit `422` înainte de confirmarea UI | rezolvă mismatch-ul adresă/lookup și rulează un singur E2E fresh cu fixture-ul exact |
| lookup/autofill și valori finale | mismatch live confirmat | lookup `RO / 010101 / 10` întoarce `Smithamburgh / Wisconsin`; adresa finală raportată `Bucharest / Bucharest` este respinsă la invoice | clarifică validarea demo și aliniază fixture-ul cu adresa acceptată de serviciu; regenerează artifactul |
| agregatul de rețea | implementat | rol explicit de `journey summary`, fără business step duplicat | verificare vizuală pe artifactul E2E fresh |

## Findings reconciliate

### `SHOP-E2E-REPLAY-01` — confirmarea comenzii nu trebuie legată de un exchange vechi

Replay-ul manual a observat că după `Check payment` aplicația afișează
`Payment was successful` și `Confirm`, dar raportul vechi declara PASS pentru
invoice. Aici riscul real era reutilizarea unui `POST /invoices/...` capturat
într-o încercare anterioară, nu faptul că raportul ar trebui să presupună că
orice răspuns 2xx reprezintă confirmarea curentă.

Codul este acum legat de punctul de business corect:

1. `PracticeShopCheckoutPurchaseStage` salvează `sequenceMarker()` înainte de
   click-ul final `Confirm`;
2. `confirmOrder()` așteaptă textul de confirmare și extrage rezultatul afișat;
3. `PracticeShopApiNetworkJournal.orderCreated(fence, confirmation)` acceptă
   numai exchange-uri de succes capturate după fence;
4. assertions verifică transportul JSON, id-ul, invoice-ul, totalul pozitiv și
   apariția invoice-ului în confirmarea UI;
5. dacă exchange-ul curent lipsește, raportul produce assertions FAIL și nu
   publică evidence falsă pentru checkpoint-ul `ORDER`.

Acest comportament este întărit de
`PracticeShopApiNetworkJournalTest.orderEvidenceCannotReuseAnOrderCapturedBeforeTheConfirmFence`
și de testele de componentă pentru `PracticeShopApiAssertions`.

**Status:** implementare închisă; dovada live rămâne deschisă până la o singură
execuție E2E fresh, cu fixture dedicat și artifact verificat prin browser MCP.
Nu se repetă manual `Confirm` în aceeași sesiune.

### `SHOP-E2E-REPLAY-02` — lookup-ul poștal nu este același lucru cu adresa finală

Replay-ul a arătat că lookup-ul pentru `Romania / 010101 / 10` poate completa
valori diferite de fixture (`Sam Fall / Smithamburgh / Wisconsin`), după care
flow-ul suprascrie explicit adresa business dorită. Raportul trebuie să
distingă aceste două momente, altfel o validare de vizibilitate pare mai puternică
decât este.

Contractul curent face distincția corect:

- `PracticeShopBillingPage.fill(...)` selectează țara și completează explicit
  câmpurile finale configurate;
- `PracticeShopPageValidator.validateValues(...)` publică expected/actual pentru
  țară, cod poștal, număr, stradă, oraș și stat, nu doar `visible`;
- fixture-ul rămâne sursa unică pentru valorile finale folosite de test;
- request-ul de lookup este evidence API separat și nu este prezentat drept
  dovadă a valorilor finale din formular.

**Status:** contractul de raportare a expected/actual există; criteriul live
rămâne deschis.

- Adresa din `Assertions` (`Romania / 010101 / 10 / Test Street / Bucharest /
  Bucharest`) a primit `422` la `POST /invoices/guest`, cu răspunsul că țara și
  orașul nu corespund.
- Lookup-ul pentru aceeași țară/cod/număr a întors `Sam Fall / Smithamburgh /
  Wisconsin`; diferența împiedică finalizarea comenzii pe aplicația live.
- Închide finding-ul numai după ce un E2E fresh folosește o adresă acceptată și
  raportul dovedește confirmarea curentă.

### `SHOP-E2E-REPLAY-03` — agregatul de rețea este sumar, nu pas de business

`API network inspector — journey summary` rămâne o singură proiecție agregată
după checkpoint-urile Gherkin. Nu dublează request-urile JSON deja atașate
pașilor și nu pretinde că este o acțiune suplimentară a utilizatorului.

Pentru auditabilitate, lifecycle-ul atașează detail-ul semantic:
`journey summary; not an additional business step`. Testul de integrare
`NetworkInspectorSummaryRoleTest` verifică artifactul HTML generat, iar testele
de renderer verifică disclosure-ul și păstrarea screenshot-ului fără tabel de
acțiuni duplicat.

**Status:** implementare închisă; verificarea vizuală finală trebuie făcută pe
artifactul E2E fresh, nu pe raportul istoric dinaintea modificării.

### Limită de validare live

- Explorarea manuală poate confirma traseul UI, dar nu demonstrează că valorile
  remote corespund fixture-ului configurat.
- Gate-ul rămâne deschis până când un E2E fresh corelează fixture-ul, confirmarea
  UI și exchange-ul invoice, iar raportul este inspectat vizual.

- O încercare cu date sintetice alternative, păstrând valorile generate de
  postcode lookup, a ajuns la confirmarea UI și a creat o comandă demo.
- Repetarea cu adresa exactă din `Assertions` (`Romania / 010101 / 10 / Test
  Street / Bucharest / Bucharest`) a eșuat la confirmarea comenzii. Lookup-ul
  live a returnat `Sam Fall / Smithamburgh / Wisconsin`; după completarea
  valorilor configurate din raport, `POST /invoices/guest` a răspuns `422` cu
  mesajul de mismatch țară/oraș. UI-ul a rămas pe `Payment was successful`, iar
  `Thanks for your order!` nu a apărut. Nu s-a repetat `Confirm`.

Rezultatul exact din raportul vechi (PASS) nu a putut fi reprodus cu valorile sale
pe aplicația live curentă. Nu s-a rulat un test ATF nou și nu se deduce trecerea
din raportul istoric.

## Auditabilitate UI-only — replay fără acces la codul ATF

### Scop și limită

Acest finding evaluează exclusiv dacă raportul UI permite unui cititor fără acces
la codul ATF să înțeleagă și să repete manual același traseu în browser. Nu cere
ca cititorul să interpreteze implementarea, aserțiunile Java sau traficul API.
Verdictul trebuie să rezulte din stările și mesajele vizibile în aplicație.

### Ce demonstrează raportul curent

- Artifactul analizat are un singur pas Gherkin pentru flow, 11 checkpoint-uri, 62 de
  acțiuni Playwright, 124 de assertions și o captură finală. Structura oferă
  scheletul traseului și nu repetă propoziția Gherkin pentru fiecare acțiune.
- Valorile fixture-ului sunt prezente în disclosure-ul `Assertions`, dar tabelul
  `Playwright action sequence` descrie completările doar ca `value entered`.
  Pentru replay, cititorul trebuie să caute separat valoarea și să deducă la ce
  acțiune `Fill` se aplică. Raportul conține informația, însă nu o leagă suficient
  de momentul în care utilizatorul o introduce.
- Artifactul analizat își reia numerotarea de la `#1` în grupuri diferite. Secvența
  vizuală păstrează ordinea, dar acel HTML este anterior corecției și nu poate
  reprezenta comportamentul codului live curent. `UiActionRecorder` păstrează
  acum identificatorul global; confirmarea trebuie făcută pe un artifact fresh.
- Captura este doar cea finală (`Final screenshot only`). Ea nu dovedește vizual
  stările intermediare; pentru acestea, raportul trebuie să se bazeze pe pași și
  rezultate observabile descrise explicit, nu pe presupunerea că cititorul poate
  deduce starea din cod.
- Replay-ul manual cu valorile exacte ale raportului nu a ajuns la confirmarea
  finală vizibilă: după `Confirm`, ecranul a rămas la plata reușită și nu a
  afișat `Thanks for your order!`. Acesta este verdictul UI observat. Nu se
  declară flow-ul reproductibil cap-coadă pe baza unei capturi ori a unui PASS
  istoric.

### Cerința pentru un raport reproductibil manual

Pentru fiecare checkpoint UI, raportul trebuie să facă vizibile, în ordinea
folosită de cititor:

1. pagina de pornire și precondițiile strict necesare, fără presupuneri despre
   fixture-uri ascunse;
2. acțiunea efectuată și ținta ei (de exemplu, câmpul sau controlul), inclusiv
   valoarea introdusă când acțiunea este `Fill`;
3. rezultatul observabil așteptat în interfață după acțiune și rezultatul
   verificat efectiv, într-un limbaj inteligibil fără surse Java;
4. un identificator de ordine unic și monoton pentru fiecare acțiune, păstrat
   între checkpoint-uri;
5. condiția terminală explicită care înseamnă succesul flow-ului, nu doar
   succesul unui pas intermediar.

Valorile sensibile trebuie mascate; cele nesensibile necesare replay-ului trebuie
să apară lângă acțiunea care le consumă, nu doar într-un disclosure separat de
assertions. Dacă o valoare este generată dinamic de aplicație, raportul trebuie
să spună clar ce vede cititorul și cum continuă cu acea valoare, fără să
prezinte rezultatul drept constant.

### ToDo UI-only

- **Rezolvat în cod — acțiune și valoare împreună:** `Fill` și `Select option`
  publică acum valoarea efectivă lângă ținta și descrierea acțiunii, în coloanele
  `EXPECTED` și `OBSERVED`. Valorile sensibile sunt afișate ca `[REDACTED]`,
  dar sunt trimise intact către aplicație. Testul component
  `PlaywrightActionSequenceTest.fillKeepsReplayValueBesideItsTargetForNonSensitiveFields`
  verifică valoarea nesensibilă, iar testul existent verifică mascarea.
- **Rezolvat în contractul de checkpoint:** `PracticeShopPageValidator` și
  `PracticeShopPurchaseScreenValidator` publică expected/actual pentru starea
  vizibilă și valorile finale ale fiecărui checkpoint. Acțiunea descrie ce a fost
  introdus; assertion-ul descrie ce a rămas efectiv pe ecran. Nu se mai folosește
  `Assertions` ca substitut pentru valoarea acțiunii.
- **Rezolvat în cod — ordine fără ambiguitate:** `UiActionRecorder` păstrează
  ordinalul global între `SETUP`, `TEST BODY`, `THEN` și drains succesive. Testul
  `keepsActionOrdinalStableAcrossSetupAndBodyDrains` verifică numerotarea 1, 2,
  3 între grupuri diferite.
- **P1 — Succes cap-coadă verificat vizual:** un replay fresh cu valorile
  raportate trebuie să ajungă la confirmarea finală din UI. Până atunci, starea
  rămâne `not reproducible`; mesajul observat la pasul anterior nu este suficient.
- **P2 — Capturi intermediare, condițional:** nu impune screenshot pentru fiecare
  click. Se adaugă doar la checkpoint-uri semnificative dacă pașii și
  expected/actual nu fac starea reproductibilă și verificabilă autonom.

Implementarea de mai sus este verificată component/integration; nu închide
replay-ul live. Raportul E2E fresh trebuie încă să demonstreze că valorile
acceptate de aplicația remote conduc la confirmarea finală.

## Criteriu de închidere — perspectiva UI

Un cititor fără cod ATF poate porni de la precondițiile raportate, urma acțiunile
în ordinea lor, introduce valorile indicate, verifica rezultatele vizibile după
fiecare checkpoint și recunoaște succesul final. Criteriul se validează printr-un
replay manual fresh, folosind doar raportul și aplicația, nu prin citirea
implementării. Până când replay-ul exact ajunge la starea finală așteptată,
gap-ul de auditabilitate/reproductibilitate UI rămâne deschis.
