# ATF Reporting — uniformitate UI/API și interacțiuni de aliniat

Reperul este raportul API cu `UsersApiTests` și `PostsApiTests`, produs de
`atf-reporting-tests`. Uniformitatea rămâne deschisă între acest model,
standalone-ul Shop, auditul componentelor și API din UI+API.

Contractele deja promovate au următorii owneri:

| Contract | Documentație oficială |
|---|---|
| Audit API per schimb și publicarea contractului asociat | [Contractul de raportare](../ATF_Reporting_Contract.md), [README Practice Shop](../../atf-test-practice-shop/README.md) |
| `TECHNICAL TRACE`, viewport comun și paritate Extent/fallback | [README ATF Reporting](../../atf-reporting/README.md) |
| Matricea probelor de componentă și browser | [README Reporting Tests](../../atf-reporting-tests/README.md) |
| Checkpoint UI fără request HTTP sintetic; context păstrat intern | [README ATF Reporting](../../atf-reporting/README.md), [Contractul de raportare](../ATF_Reporting_Contract.md) |
| Schema auth: profil, mecanism și redactare | [Schema auth a dovezilor API](../ATF_Reporting_Contract.md#schema-auth-a-dovezilor-api) |

Fișierul păstrează ținta existentă `@DocumentationTarget`; cerințele de mai jos
sunt corelate cu sursele live și cu interacțiunile din rapoarte randate.

## Cerințe deschise, în ordinea implementării

| Prioritate | Cerință | Responsabil | Dovada pentru închidere |
|---|---|---|---|
| P1 — PUB-01 | Publicare predictibilă pentru HTML voluminos cu PNG-uri inline | Reporting: pipeline și teste de componentă | Proces separat cu heap bounded, fixture determinist și artifact complet |
| P1 — TABLE-VIEWPORT-01 | Toate tabelele de date folosesc plafonul Overview/System, inclusiv SETUP și Assertions | Reporting: prezentare comună; Reporting Tests: componente | Date scurte/lungi, fiecare context și proiecție, scroll local și probe negative |
| P1 — API-01 | Închiderea părintelui ascunde întregul subarbore | Reporting: runtime de navigare | Browser: noduri cu dovezi proprii și copii |
| P1 — API-02 | Rolul și prezentarea provin din tipul nodului | Reporting: model și proiecție | Titluri personalizate și metode variate păstrează formatul |
| P1 — API-03 | Aceeași schemă de dovezi pentru toate intrările API | Reporting și adaptoare | Paritate semantică/vizuală, corp absent și replay |
| P1 — API-06 | Aserțiuni și verdicte atribuite canalului și exchange-ului corect | Reporting și contractul checkpoint-ului | UI PASS/API FAIL și invers, fără contaminarea fraților |
| P1 — API-07 | Metoda și timpul reflectă captura | Adaptor captură și Reporting | `QUERY` păstrat; măsurat, necapturat și timp de publicare distincte |
| P1 — API-04 | Intenție lizibilă și publicare cauzală fără dubluri | Shop la intrare; Reporting la randare | Apeluri distincte, retry și republicare diferențiate |
| P1 — API-05 | Fără bandă de audit proprie fazei API | Reporting și adaptor audit API | Fără `Audit details` sau wrapper gol; exchange-uri și diagnostic real păstrate |
| P1 — gate comun | Teste de componentă care resping abaterile | `atf-reporting-tests` | Matrice pozitivă și probe negative explicite |
| P1 — GOV-API-01 | Guvernanță pentru aceeași structură API, fără excepții per consumer | `atf-tests` și `atf-reporting-tests` | Boundary comun, matrice executată și fixture-uri neconforme respinse |
| P2 — documentație | Eliminarea contradicțiilor contract/cod | Responsabilii README-urilor | Un contract actual, promovat după implementare |

### Progres verificat în implementarea curentă

| Cerință | Ce există în cod și ce s-a probat | Ce împiedică închiderea completă |
|---|---|---|
| API-01 | Runtime-ul schimbă starea tuturor panourilor directe ale nodului; componenta browser verifică `.collapse` + `.accordion`, mouse, Enter, Space, ARIA și vizibilitatea calculată. | Aceeași probă pentru Gherkin cu API imbricat, normalizare repetată și frați. |
| API-02 | Rolurile fază/request/validation provin din ierarhia de noduri, nu din titlu; componenta browser folosește un titlu de validare personalizat. Excepția de prezentare pe textul `Contract validation` a fost înlocuită cu prezența dovezilor UI. | Matricea de titluri/metode și aggregate-ul de rețea în browser, inclusiv copie selectată și Fails. |
| API-03 | Aliasurile `manual.replay`, `transport.provenance` și `auth.profile` rămân în request și primesc etichete distincte; testul de formatter le apără. | Paritatea completă direct/Shop/UI+API, corp absent versus JSON `null`, provenance și replay. |
| API-05 | Componentele Extent/fallback și inspecția MCP confirmă lipsa benzii fazei, diagnosticul redactat la scenariu, Fails și închiderea subarborelui; șase cazuri acoperă inclusiv asociere lipsă fără schimbarea JUnit PASS. | Rerularea completă finală și promovarea feedback-ului după verificarea inventarului strict al funcțiilor JS. |
| Gate comun | Verificarea statică cere elemente reale în `<body>`; componentele fazei resping și banda ascunsă, redenumită sau goală, inclusiv wrapper-e vendor diferite. | Override CSS final/media query pe stilul efectiv și toate combinațiile de verdict; integrarea matricei comune rămâne GOV-API-01. |
| GOV-API-01 | Există guard-uri de ownership și probe de artifact; unele verificări caută doar tokenuri globale în HTML. | Gate structural per nod, cu probe negative, și verificarea executării aceleiași matrice pentru intrările API. |

API-04, API-06 și API-07 rămân deschise; nu s-a schimbat încă semantica
publicării cauzale, ownership-ul verdictelor ori fidelitatea snapshot-ului Shop.
Testele de șiruri rămân doar guard-uri auxiliare; probele de browser și
artefactele generate decid închiderea.

Viewportul API-08 și cardinalitatea etichetelor sunt promovate în
[contractul de raportare](../ATF_Reporting_Contract.md#dovezile-unei-cereri) și
[matricea responsive](../../atf-reporting-tests/README.md#verificarea-randării-responsive).
API-08 rămâne închis pentru metadate request/retry; nu certifică automat toate
tabelele, SETUP-ul sau copiile din Fails. Acoperirea transversală rămasă este
`TABLE-VIEWPORT-01`, fără redeschiderea regresiilor deja demonstrate.

### PUB-01 — bugetul de memorie al publicării

- **Cauză confirmată:** heap-ul de 256 MiB al fork-ului Surefire poate fi epuizat
  în `ExtentReportPublicationPipeline.ensureViewIdentifiersAndOverviewDefault`,
  la `String.replaceFirst`, după terminarea journey-urilor.
- **Mecanism:** pipeline-ul citește și transformă întregul HTML în mai multe
  etape; deduplicarea PNG-urilor are loc abia după aceste transformări.
  Dimensiunea fișierului final nu este bugetul de heap necesar.
- **Remediere de configurație:** bugetul implicit este 2 GiB per fork,
  configurabil prin `atf.test.heap.max`; nu schimbăm politica de captură și
  nu dezactivăm fork-ul. Configurația nu dovedește scalabilitatea pipeline-ului.
- **Probă măsurată:** `LargeReportPublicationComponentTest` pornește un JVM
  separat cu `-Xmx384m`, generează 1.500 linkuri PNG inline cu două imagini
  distincte/repetate și text Unicode; artifactul final depășește 9 MB,
  păstrează ambele ancore, markerul final, tema și runtime-ul. Proba raportează
  vârful heap și cere exit code 0, fără E2E live; măsurătoarea unui run nu devine buget universal.
- **Rămâne de făcut:** praguri reprezentative pentru volume mai mari și PNG-uri
  mari/distincte, plus buget de durată calibrat; această fixture demonstrează
  doar profilul explicit de 384 MiB, nu scalarea generală a pipeline-ului.
- **Gate:** publicare completă și autonomă, capturi păstrate, exit code valid;
  failure-path fără artifact parțial promovat și fără pierderea diagnosticului.
  Optimizează copiile HTML numai după măsurare; nu trata creșterea heap-ului
  ca dovadă de închidere a acestei cerințe.

## TABLE-VIEWPORT-01 — plafon comun pentru toate tabelele de date

Orice tabel de date care depășește plafonul Overview → System trebuie să deruleze
local, inclusiv tabelele de acțiuni Playwright, setup, audit și aserțiuni.
Se reutilizează `AtfReportThemeTokens.TABLE_SCROLL_HEIGHT`, astăzi `500px`,
publicat ca `--atf-table-scroll-height`; nu se introduce încă o constantă.

- Înălțimea este naturală până la plafon, apoi scrollabilă; nu se rezervă `500px` pentru tabele scurte.
- Reperul este plafonul System, nu înălțimea sa accidentală când are puține rânduri.
- Contează înălțimea randată, nu titlul, contorul sau numărul de rânduri; etichetele și contoarele afișate rămân neschimbate.
- Aceeași regulă acoperă SETUP, TEST BODY și TEARDOWN, noduri request/validation, Tests și copiile selectate din Fails/Categories, în Extent și fallback.

<details>
<summary>Constatări din HTML-ul raportului, codul live și limitele probelor actuale</summary>

| Punct verificat | Constatare | Lacună |
|---|---|---|
| [Tokenul comun](../../atf-reporting/src/main/java/atf/api/reporting/theme/tokens/AtfReportThemeTokens.java) și [System](../../atf-reporting/src/main/resources/atf/reporting/css/overview-category-tables.css) | Plafonul există deja; System folosește `height:auto`, `max-height` comun și `overflow-y:auto` | Existența constantei nu dovedește aplicarea ei fiecărui tabel |
| [CSS evidence](../../atf-reporting/src/main/resources/atf/reporting/css/evidence-stack.css) și [acțiuni](../../atf-reporting/src/main/resources/atf/reporting/css/evidence-interaction.css) | Regula de plafon pentru acțiuni/assertions cere `.test-view .test-content-detail .card[data-atf-node-role='validation']`; wrapperul acțiunilor are implicit `overflow:hidden` | Politica de viewport depinde de părintele raportului, nu doar de componenta tabelului |
| [Publicarea SETUP](../../atf-reporting/src/main/java/atf/api/reporting/ui/UiEvidencePublisher.java) | Acțiunile setup-ului sunt publicate direct la request, nu într-o validare UI artificială | Extinderea CSS numai pentru noduri validation nu acoperă acest traseu; nu se schimbă semantica setup-ului pentru a potrivi selectorul |
| [Setup renderer](../../atf-reporting/src/main/java/atf/api/reporting/evidence/ExtentTransportEvidenceRenderer.java) și [builderul de tabel](../../atf-reporting/src/main/java/atf/api/reporting/markup/AtfReportMarkup.java) | `atf-setup-evidence-table` este emis în corpul disclosure-ului, fără viewport propriu; builderul selectează wrapper pentru trace/request/retry | Setup și alte familii de tabele nu intră automat în același contract |
| [Assertions UI](../../atf-reporting/src/main/java/atf/api/reporting/evidence/ui/ExtentUiAssertionRenderer.java) | Există `atf-validation-table-wrap`; în Extent plafonul depinde însă de selectorul de context | Se probează tabelele de aserțiuni în toate contextele și la densități diferite, fără dependență de titlu sau contor |
| [Fixture-ul de densitate](../../atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/layout/ScrollableTableReportFixture.java) și [proba browser](../../atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/layout/ScrollableTableViewportBrowserProbe.java) | Acțiunile/assertions sunt generate prin `screenValidation` în TEST BODY; inventarul măsoară suprafețe nominalizate, fără Setup evidence | O suită verde pe acest subset nu dovedește toate tabelele sau SETUP-ul |
| [Guard-ul CSS existent](../../atf-reporting-tests/src/test/java/atf/api/reporting/theme/evidence/AtfReportEvidenceCssTest.java) | Verifică inclusiv prezența `max-height:var(--atf-table-scroll-height)` în bundle | Șirul poate exista într-o regulă care nu se aplică tabelului sau părintelui curent; sunt necesare probele de geometrie și cazurile negative |

HTML-ul artefactului UI+API analizat confirmă wrapperul acțiunilor/assertions,
tabelul setup fără viewport și tokenul comun. Acesta este diagnostic static,
nu măsurare de computed style; instanțele din template și copia selectată nu
sunt numărate drept execuții distincte.

</details>

### Soluția urmărită — compoziție în Reporting, nu patch per consumer

- `ReportEvidenceTable` rămâne modelul de date; o politică/prezentare de viewport renderer-owned compune tabelul, wrapperul și antetul sticky prin seam-ul comun `AtfReportMarkup` sau o componentă delegată mică.
- Nu se impune un nume nou de clasă sau un arbore de moștenire; se preferă compoziția unei politici tipizate, cu responsabilitate unică și default comun pentru tabelele de date.
- Stilul viewportului se aplică prin rolul componentei, independent de titlu, Shop, numele fazei ori existența unui părinte `validation`; adaptoarele Extent/fallback păstrează aceeași semantică.
- Builderii UI, setup, API, Overview și diagnostic reutilizează această politică; nu se mai adaugă selectori/constante pentru fiecare etichetă sau un whitelist deschis de nume CSS.
- System/Overview poate păstra containerul vendor existent ca viewport semantic; adaptorul aplică politica comună fără încă un wrapper scrollabil și fără rescrierea rendererului Extent.
- Un tabel are un singur viewport vertical; antetul, ultimul rând și celulele lungi rămân accesibile, cu wrap lizibil și fără scroll orizontal al documentului.
- Plafonul aparține tabelului, nu întregului disclosure/card/fazei. `Status / Timestamp / Details` folosit ca layout al evenimentelor nu se învelește ca încă un tabel de date, deoarece ar tăia copiii, JSON-ul și screenshot-urile.
- Screenshot-ul și linkul său rămân după acțiuni, în afara viewportului; JSON-ul își păstrează componenta separată. Nu se modifică statusuri, counts, ordinea dovezilor sau expandarea părintelui.
- Markup-ul nou conține viewportul de la publicare; compatibilitatea legacy poate normaliza idempotent un tabel lipsit de wrapper, fără rescanări globale, wrapper-e duplicate sau recalculări de dimensiuni la fiecare toggle.

### Ce se reutilizează și ce rămâne în Shop

Componentele de tabel, tokenurile și renderer-ele există deja în Reporting.
Nu s-a identificat în Shop o implementare generică de viewport care trebuie
mutată pentru acest defect; mutarea unui publisher business nu este soluția.

- Viewportul și politica de prezentare aparțin `atf-reporting`; fixture-urile neutre, măsurarea și cazurile negative aparțin modulului existent `atf-reporting-tests`.
- Shop păstrează page objects, setup-ul contului, checkpoint-urile și mapping-ul aplicației; nu primește CSS, praguri sau cod special pentru acest raport.
- O extracție viitoare cere un contract generic verificabil și dependențe corecte; nu se promovează în Reporting date de produs, checkout, credentiale ori un helper de test doar fiindcă este reutilizat de două E2E-uri Shop.

<details>
<summary>Probe de componentă cerute și criteriul complet de închidere</summary>

| Axă | Probe obligatorii |
|---|---|
| Familii de date | System/Overview, categorii și tag audit; Setup evidence; Playwright actions și audit observations; Assertions; request/response metadata, execution summary, attempts/retry și trace; tabelele de diagnostic și aggregate |
| Context | SETUP/TEST BODY/TEARDOWN, request cu dovezi directe și validation copil, selecție/relansare a nodului și proiecțiile Fails/Categories; Extent și fallback |
| Densitate | 0/1/scurt/lung, inclusiv aserțiuni multi-line și rând unic foarte înalt; cazuri sub/la/peste plafon, fără prag după numărul de rânduri |
| Geometrie | computed `max-height` egal cu reperul System în CSS px; scurt natural, lung cu scroll local, antet sticky fără suprapunere, ultim rând accesibil și fără overflow orizontal la viewport standard/îngust |
| Token unic | Fixture controlată cu altă valoare a tokenului comun schimbă toate suprafețele; un `500px` local nu poate trece numai fiindcă default-ul curent este tot 500 |
| Robusteză | Colapsare/redeschidere, scroll cu mouse și tastatură, screenshot în afara viewportului și niciun handler/wrapper duplicat după normalizarea repetată |
| Probe negative | Wrapper eliminat cu CSS păstrat; cap diferit de System; `overflow:hidden`; stil aplicabil numai la validation; ultim rând tăiat; wrapper dublu; tabel de date nou neacoperit |

Se reutilizează probele și fixture-urile Reporting Tests unde sunt potrivite,
dar se adaugă componente parametrizate pe porturile publice, nu complexitate
în E2E-urile Shop. Fixture-ul include SETUP real de raport și familii neutre de
date, nu rânduri clonate în DOM pentru a fabrica dovada.

- Inventarul probei trebuie să respingă un tabel semantic lipsit de politica comună; nu măsoară numai wrapper-ele deja corecte și nu ignoră o familie absentă.
- Fiecare abatere negativă este respinsă separat, cu familie/context/proiecție, plafonul așteptat și geometria observată; o căutare de token în CSS nu este dovadă comportamentală.
- Testele și suportul se modifică numai după un scope `testChanges` exact în feedback, cu baseline și proof; această analiză nu implementează sau relaxează teste.

**Închidere:** matricea conformă trece, fiecare abatere deliberată eșuează,
iar artifactul nou este inspectat randat. Abia atunci regula demonstrată se
promovează la owner; API-08 rămâne proba subsetului request/retry, nu dovada
universală a acestei cerințe.

</details>

## Paritatea API — diferențele încă de demonstrat

Reperul rămâne `ApiAcceptancePublisher`, cu `ApiJsonEvidence`,
`ApiManualReplay` și contractul evaluat prin `ResponseContractReporter`.
Paritatea compară aceeași fixture și același profil, nu un număr fix de câmpuri.

| Domeniu | Lacună rămasă | Criteriu |
|---|---|---|
| Request / execution | Publisherii compun separat provenance, replay și contextul capturii | Același domeniu semantic în direct, audit și UI+API |
| Response | Statusul și denumirea content-type diferă între intrări | Aceeași schemă, fără a confunda response-ul cu validarea lui |
| Body absent / indisponibil | Auditul omite unele canale JSON; publicarea UI este condiționată | Absență confirmată → `null`; necapturat → explicație distinctă |
| Verdict și aserțiuni | Checkpoint-ul mixt nu păstrează ownerul fiecărui contract | API-06: rezultate locale, apoi agregare |
| Identitate și timp | Apeluri republicate și durate browser construite cu zero | API-04/07: captură fidelă și cauzalitate |

Schema auth comună, aliasurile formatterului și geometria existentă nu mai
sunt ToDo-uri. Contractele și probele lor sunt la ownerii oficiali indicați
mai sus; profilele mock/live sau observație/contract rămân diferențe legitime.

## API-01 — închiderea atomică a subarborelui

Regresie observată interactiv în ATF Debug: disclosure-urile Java pentru
request, response și execution sunt deschise, dar `summary[aria-expanded]`
rămâne `false`. Binding-ul existent deservește doar wrapper-ele create în JS.
Incrementul dedicat trebuie să acopere și copiile selectate, fără listener dublat.

- Replay-ul MCP a mai găsit un criteriu separat rămas deschis: `Expand all nodes`
  din ownerul selectat al fixture-ului API nu deschide scenariile și request-urile.
  Click-ul individual funcționează; proba următoare trebuie să urmărească starea
  semantică, nu numai efectul vendor asupra claselor CSS.

Reproducere: deschide scenariul, faza, request-ul și validarea; închide apoi
request-ul, fără să închizi manual validarea.

- `nodePanels` și `setDisclosureState` controlează deja ambele corpuri directe;
  proba browser request → validation acoperă mouse, Enter și Space.
- Rămâne matricea checkpoint UI+API, frați și normalizare repetată, inclusiv
  copia selectată și navigarea din Fails.
- Toate suprafețele nodului trebuie controlate împreună, printr-un container
  comun sau printr-o politică explicită pentru toate corpurile sale.
- Închis înseamnă că niciun descendent nu este vizibil sau accesibil prin Tab;
  antetul, glyph-ul, ARIA și corpurile trebuie să rămână sincronizate.
- Închiderea nu schimbă un frate; redeschiderea păstrează starea locală a
  copiilor conform aceleiași politici în API direct și UI+API.
- Normalizarea, schimbarea ownerului și sarcinile amânate ale runtime-ului nu
  pot redeschide copii, dubla evenimente sau schimba spontan starea selectată.
- Același contract acoperă tastatura pentru scenariu și fază, nu doar pentru
  request/validation; `role=button` și `tabindex` nu asigură singure activarea.
- `<details open>`, glyph-ul și `summary[aria-expanded]` rămân sincronizate și
  pentru disclosure-urile emise direct de Java, pe care normalizarea le sare.

**Închidere:** proba de browser eșuează pe comportamentul descris și trece pentru
request cu dovezi + copii, nod numai cu copii și validare cu exchange-uri.

## API-02 — rol semantic explicit, independent de titlu

Rolurile provin deja din structură, iar excepția literală pentru
`Contract validation` a fost eliminată. Rămâne verificarea pe toate proiecțiile,
cu metode și titluri variate, inclusiv aggregate-ul de rețea și Fails.

- Nodurile create prin porturile request/validation își păstrează rolul tipizat
  până în DOM; titlul rămâne conținut pentru cititor.
- O validare cu nume personalizat și o metodă precum `OPTIONS` păstrează stilul
  și interacțiunile rolului lor, fără regex-uri noi pentru fiecare denumire.
- Layout-ul unei validări API se decide prin tipul dovezii, nu prin egalitate
  cu textul `Contract validation`; redenumirea nu schimbă DOM-ul.
- HTML-ul extern/istoric poate avea un fallback delimitat; artefactele noi nu
  depind de acest fallback pentru identificarea rolurilor.
- O singură politică de componente și tokenuri deține antetul, badge-urile,
  spațierea, disclosure-urile și tabelele; fără CSS particular per test.

**Închidere:** aceleași date și același tip produc aceeași prezentare, chiar dacă
titlul nu conține HTTP, Gherkin, `NETWORK` sau `VALIDATION`.

## API-03 — aceeași schemă, prin aceeași compoziție

Rendererul comun există, însă producătorii încă asamblează separat câmpurile și
JSON-ul; astfel diferă nivelul de detaliu din API direct, audit și UI+API.

Schema auth comună și păstrarea mecanismului după redactare sunt acoperite prin
`ApiAuthenticationEvidence` și probe de componentă. Contractul stabil este în
[schema auth oficială](../ATF_Reporting_Contract.md#schema-auth-a-dovezilor-api);
restul diferențelor de request/response rămân deschise în această cerință.

```text
METHOD /route — intenția apelului
├── Request details    → metadate request + REQUEST JSON
├── Response details   → metadate response + RESPONSE JSON
├── Execution evidence → attempts, retry, durată și corelare
└── Contract validation → numai cu un contract evaluat
```

- Sub `API validation` dintr-un checkpoint mixt se reutilizează același
  subarbore; `UI validation` rămâne fratele grupului API.
- Compoziția comună primește snapshot-uri neutre request/response/execution;
  consumerul furnizează intenția și contractul aplicației.
- Se reutilizează `ApiJsonEvidence`, `ApiManualReplay` și formatarea existentă;
  nu se copiază un publisher de teste în librărie cu dependențe de Shop.
- Un corp confirmat absent are documentul JSON `null`; un corp necapturat sau
  indisponibil are o explicație distinctă, fără inventarea dovezii.
- Politica pentru auth, headers, provenance și replay este comună; numărul de
  câmpuri poate varia justificat cu datele disponibile.
- Semnătura nu înlocuiește arbitrar metoda/calea, iar câmpurile nu migrează
  între request, response și execution în funcție de publisher.
- O singură aserțiune și un contract cu mai multe aserțiuni pot avea același
  stil; uniformitatea nu cere inventarea acelorași verificări.
- Un trace observat nu primește un verdict contractual artificial; un răspuns
  HTTP negativ așteptat poate satisface contractul său.

**Închidere:** aceeași fixture neutră și același profil de dovezi, prin cele trei
intrări, au aceeași schemă și stil, inclusiv GET fără corp și 204.

## API-04 — intenție, identitate și cauzalitate

În audit, `publishObserved` preia titlul generic al operației: login-ul valid
și cel invalid apar ca `establish API session`, deși al doilea demonstrează
respingerea și invalidarea sesiunii.

- Titlul combină metoda/calea cu intenția explicită a apelului sau contractului;
  cititorul distinge pregătirea de verificarea negativă.
- Două invocări reale ale aceluiași endpoint rămân două exchange-uri;
  metoda/calea identică nu demonstrează retry sau duplicare.
- Attempt-urile aceluiași exchange rămân într-un singur `Execution evidence`;
  nu generează un alt request/response ori o validare paralelă.
- Setup-ul contului publică dovezile locale fiecărui checkpoint; lista
  acumulată pentru înregistrare nu se republică integral la login.
- Identitatea include domeniul capturii/scenariului; un `eventId` local nu se
  deduplicatează global între scenarii independente.
- Inspectorul agregat poate referi evenimente deja publicate, fără să le
  prezinte ca invocări noi sau alte propoziții Gherkin.

**Închidere:** componente pentru două intenții pe același endpoint, retry într-un
exchange și register → login fără republicarea înregistrării.

## API-05 — fără bandă de audit în faza API

Increment în lucru: `Phase.detail` rămâne context intern, fără proiecție de
log în fază. Diagnosticele explicite de execuție/asociere au un model comun,
redactat și publicat la scenariu, apoi în Fails; nu devin exchange-uri sau
aserțiuni business. Probele păstrează metadatele semantice și verifică separat
zero/mai multe exchange-uri, eșecul JUnit și lipsa unei asocieri de contract.
Compatibilitatea pentru cheile de diagnostic existente este delimitată în
model, nu dedusă din titlul scenariului ori din consumer.
Regresia a fost demonstrată în ambele renderere înaintea schimbării. Scope-ul
de teste folosește baseline-ul de review înregistrat mai jos, explicit prin
`atf.governance.testChanges.baseRef`; HEAD-ul live a avansat între timp.
Proba de redacție verifică acum celula reală `Failure cause`, nu un token
global care poate exista numai în JavaScript.
Inspecția MCP a descoperit separat că Extent emite logul scenariului în afara
panoului colapsabil. Normalizarea dedicată mută numai diagnosticul explicit
în panoul scenariului, elimină wrapper-ul vendor și este idempotentă; proba
browser cere și absența scurgerii sub un scenariu închis.
Inventarul strict al funcțiilor Tests runtime crește de la 27 la 28 prin
`normalizeScenarioDiagnostics`; se păstrează unicitatea și se verifică explicit
ownerul Scenarios, fără a permite mutarea funcției în Filters sau Categories.
Control suplimentar: diagnosticul păstrează statusul typed al fazei sursă.
O notă din SETUP/PASS rămâne vizibilă în Tests, dar nu poate deveni cauza unui
TEST BODY/FAIL și nici nu poate fi reclasificată ca validare UI. Proba traversează
ambele renderere și verifică mesajul efectiv din Fails.

Cerința directă înlocuiește soluția anterioară cu disclosure secundar:
`Status / Details → Info → Audit details` nu trebuie publicat în faza API,
nici colapsat, nici ascuns cu CSS. Faza organizează pașii reali și păstrează
formatul standalone; nu introduce un jurnal propriu înaintea exchange-urilor.

<details>
<summary>Diagnosticul din cod și testele care protejează încă forma nedorită</summary>

| Sursă live | Constatare | Consecință |
|---|---|---|
| [Fixture-ul disclosure](../../atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/disclosure/ExtentReportDisclosureProbe.java) | Adaugă `phase.detail` și cere un disclosure `PHASE_DETAILS` colapsat | Banda provine și din Reporting Tests, nu doar din Shop |
| [Scope-ul Extent](../../atf-reporting/src/main/java/atf/api/reporting/extent/ExtentPhaseScope.java) | `complete` trimite detaliile fazei către renderer | Publicarea este explicită, nu un artefact de culoare sau spațiere |
| [Rendererul transport](../../atf-reporting/src/main/java/atf/api/reporting/evidence/ExtentTransportEvidenceRenderer.java) | `renderDetails` emite `node.info` cu `Audit details` | Extent construiește tabelul de log și badge-ul `Info` |
| [Proiecția fallback](../../atf-reporting/src/main/java/atf/api/reporting/html/HtmlReportTestsView.java) | `renderPhaseDetails` precedă copiii fazei | Același contract trebuie decis explicit și pentru fallback |
| [Adaptorul de audit Shop](../../atf-test-practice-shop/src/test/java/atf/ui/tests/practiceshop/api/reporting/ShopApiCallAuditTestSupport.java) | Folosește același canal pentru completitudine, asociere și eșec JUnit | Eliminarea necondiționată poate pierde cauza reală a unui eșec |
| [Paritatea semantică](../../atf-reporting-tests/src/test/java/atf/api/reporting/semantic/AtfReportSemanticSnapshotTest.java), [testul fallback](../../atf-reporting-tests/src/test/java/atf/api/reporting/html/HtmlReportViewsTest.java) | Cer prezența blocului; snapshot-ul comun reține numele detaliilor, nu valorile lor | Egalitatea snapshot-urilor nu dovedește păstrarea mesajului de eșec |

- Sursa HTML confirmă banda la nivel de fază în fixture-ul disclosure;
  standalone-ul de referință nu o conține.
- Fixture-ul disclosure testează interacțiuni și nu publică toate datele
  response; nu poate înlocui fixture-ul complet pentru paritatea API.
- Numărarea globală a textelor poate include template-uri și copii de
  navigare; absența și cardinalitatea se verifică pe ownerul și nodul corect.
- Inspecția HTML și captura explică structura; verificarea interactivă a
  variantei implementate rămâne obligatorie pentru închidere.

</details>

- Nu se emite banda și nu rămân tabelul gol `Status / Details`, rândul `Info`,
  containerul gol sau spațiul rezervat ei după normalizarea runtime-ului.
- Nu se rezolvă prin redenumire, `display:none`, mutarea după exchange-uri
  ori o excepție pentru titlul testului sau numele consumerului.
- Se păstrează subarborele API-03: Request → Response → Execution și validarea
  contractului real. Tabelele `Status / Details` ale dovezilor legitime ale
  request-ului sau validării nu sunt ținta eliminării.
- Completitudinea capturii rămâne informație de audit internă; nu este răspuns
  HTTP, contract business, configurație `System/Environment` sau pas nou.
- Un eșec JUnit ori o asociere de contract lipsă păstrează verdictul real și
  diagnosticul redactat în zona dedicată erorii/scenariului, accesibilă din
  Fails; nu se șterge cauza odată cu rezumatul de completitudine.
- Zero exchange-uri nu inventează HTTP, validare sau PASS contractual;
  pierderea capturii rămâne explicabilă fără banda introductivă.
- Aceleași reguli se aplică Extent și fallback; `SETUP` cu precondiții reale,
  `Setup evidence` și inspectorul agregat nu se elimină incidental.

**Închidere:** componente cu/fără metadate, zero/mai multe exchange-uri și eșec
JUnit păstrează dovezile și diagnosticul, fără banda fazei în HTML și în
randare. Testele care cereau banda sunt revizuite explicit, nu doar șterse.

## API-06 — aserțiuni și verdict local, înainte de agregare

`ShopPurchaseCheckpoint.withAdditionalAssertions` pierde distincția UI/API;
`UiEvidencePublisher` trimite lista combinată în validarea vizuală și atribuie
exchange-urilor statusul grupului, cu excepția răspunsului indisponibil.

- Checkpoint-ul păstrează separat aserțiunile UI, contractele API și corelările
  dintre canale, cu referință la exchange atunci când aceasta există.
- Separarea este tipizată; prefixul `API` din numele aserțiunii nu este un
  protocol pentru a deduce ownerul sau verdictul.
- Query, headers și identitatea din JSON se verifică în canalul API; ruta,
  controalele și imaginea rămân în UI, sub aceeași propoziție Gherkin.
- O corelare UI ↔ API își explică cele două surse fără să dubleze propoziția,
  screenshot-ul ori întreaga listă de aserțiuni în ambii frați.
- UI PASS + API FAIL produce checkpoint FAIL și păstrează UI PASS; UI FAIL +
  API PASS păstrează rezultatul API, fără a recolora toate exchange-urile.
- Două exchange-uri cu rezultate diferite nu primesc automat aceeași stare;
  lipsa contractului rămâne observație explicită, fără PASS contractual inventat.
- Un 401 așteptat poate fi PASS contractual, iar 200 cu JSON greșit poate fi
  FAIL; codul HTTP singur nu înlocuiește decizia ownerului.
- Aceleași reguli se aplică modului checkpoint și `UNIFIED_FLOW`; agregarea
  fluxului nu trebuie să mute toate verificările în `Screen validation`.

**Închidere:** componente cu UI PASS/API FAIL, UI FAIL/API PASS, API indisponibil,
negativ așteptat și două exchange-uri diferite demonstrează ownership-ul local.

## API-07 — fidelitatea snapshot-ului de transport

Increment delimitat: păstrarea metodei capturate în `ShopApiOperation`,
`ShopApiEvidenceMapper`, rezumatul jurnalului și ambii publisheri Shop.
Clasificarea `SEARCH` poate selecta `QUERY`, fără echivalare cu `GET` în dovezi.

- Probe: `QUERY` cu JSON, control `GET`, metodă necunoscută, semnătură și replay.
- Gate: `ShopComponentSuite`, fixture nouă prin publisherul UI/API și inspecție MCP.
- Participare: suita trebuie să includă și testele package-private din
  `atf.ui.practiceshop.support`; selecția doar a pachetului cu `.tests` le omitea.
- Acest increment nu închide duratele necapturate ori latența versus timpul publicării.

Metoda nu mai este rescrisă în mapper, titlu sau rezumat: se păstrează și în
request-ul ambilor publisheri Shop. Probele verifică fiecare nod semantic,
JSON-ul, semnătura și replay-ul; clasificarea `SEARCH` rămâne separată.

Rămâne deschisă construirea duratelor browserului cu zero. Adaptorul direct
păstrează duratele response/total distincte, dar această probă nu dovedește
tratarea unei măsurători absente. Schema auth nu mai reprezintă un ToDo.

- Metoda HTTP capturată se păstrează în titlu, request, semnătură și replay;
  aliasul operației business este o informație separată.
- `QUERY` cu un corp nu devine `GET` doar pentru un titlu cunoscut; orice
  adaptare de protocol cere o echivalență demonstrată și ambele valori explicite.
- O durată necapturată se explică drept nemăsurată; nu este zero măsurat.
- Response time, totalul cu retry/backoff și timpul scope-ului de raportare
  se disting; nu se prezintă timpul publicării Extent drept latență HTTP.
- Modelul API pornește request-ul de raportare după obținerea execuției;
  badge-ul lui măsoară ciclul de raportare și nu probează singur durata rețelei.
- Datele exacte, când există, se păstrează în toate intrările API și în adaptorul
  legacy/core; redacția protejează secretele fără a schimba metoda sau corelarea.

**Închidere:** fixture cu metodă `QUERY`, durate response/total distincte și
durată necunoscută verifică inclusiv replay-ul. Schema auth este verificată separat.

## Gate de componentă — comportament, nu prezența unor șiruri

Testele existente apără contracte utile, dar nu demonstrează uniformitatea
completă: verificarea textului unui script sau expandarea tuturor nodurilor
poate trece când un copil rămâne vizibil sub un părinte închis.

Contractul static respinge deja disclosure-uri absente din `<body>`, chiar
dacă stylesheet-ul păstrează selectorii. Dovada statică nu închide matricea
interactivă sau testele negative pentru cascade CSS și verdicte locale.

<details>
<summary>Matricea de închidere — extinderi și regresii de păstrat</summary>

Cazurile deja acoperite rămân regresii obligatorii la orice refactorizare.
Lucrările restante sunt cele delimitate în tabelul de progres, nu toate
rândurile acestei matrice.

| Probă | Contract observabil | Abatere care produce FAIL |
|---|---|---|
| Request cu `.collapse` și `.accordion` | Închiderea ascunde dovezile și copiii | Validarea/tabelul rămâne sub `＋` |
| Nod numai cu copii; validare cu exchange-uri | Stare comună pe întregul corp | Grup vizibil, antet închis |
| Mouse, Enter și Space | Stare identică pentru aceeași acțiune | ARIA/glyph diferă de vizibilitate |
| Disclosure emis direct de Java | `open`, glyph și ARIA identice după toggle | Vizibil deschis, `aria-expanded=false` |
| Cicluri repetate și normalizare ulterioară | Stare stabilă, frați nemodificați | Toggle dublu sau copil reapărut |
| API direct, audit, API din UI+API | Același subarbore și tokenuri | Tabele brute ori altă ordine |
| Fază API cu și fără metadate de completitudine | Exchange-uri directe, fără bandă proprie fazei | `Audit details`, echivalent redenumit sau wrapper gol |
| Bandă de audit ascunsă prin CSS ori mutată | Nodul nepermis lipsește structural | Gate verde doar fiindcă banda nu este vizibilă |
| Titlu personalizat, metodă variată, validare redenumită | Rolul semantic decide prezentarea | Rol pierdut ori schimbat după text |
| GET fără corp, 204, dovadă indisponibilă | Absența și lipsa capturii sunt distincte | Canal omis ori `null` inventat |
| Succes, negativ așteptat, contract eșuat | Expected/actual și verdict corecte | Status sau aserțiuni falsificate |
| UI PASS/API FAIL și invers; două API cu stări diferite | Aserțiuni și verdict local, apoi agregare | Verdictul părintelui copiat în toți copiii |
| Aliasuri pentru replay/provenance/content type/auth | Același domeniu și label canonic | Replay în response sau profil confundat cu mecanism |
| `QUERY` cu corp; timp necunoscut și timp măsurat | Snapshot și replay fidele sursei | Metodă rescrisă, durată inventată ca zero |
| Două apeluri reale; un exchange cu retry | Identități distincte versus attempts | Comasare sau duplicare arbitrară |
| Înregistrare urmată de login | Eveniment la checkpoint-ul cauzal | Același eveniment republicat ca apel nou |
| Date scurte/lungi, viewport standard/îngust | Tokenuri, wrap, scroll și ultim rând accesibil | Înălțime particulară sau clipping |
| Request metadata și retry history lungi | Același plafon System/Environment și sticky | Tabel nelimitat ori coadă inaccesibilă |
| Audit fără exchange-uri; eșec JUnit | Metadate sincere, fără HTTP fictiv | Request/contract inventat |
| Disclosure eliminat, stylesheet păstrat | Validatorul respinge absența nodului real | CSS/JS este acceptat drept dovadă de conținut |
| Override CSS final/media query cu tokenuri inițiale intacte | Se compară stilul efectiv în viewport | Egalitatea primelor declarații ascunde drift-ul |

</details>

- `atf-reporting-tests` generează HTML real cu bundle-ul runtime al librăriei
  și îl verifică în browser; un DOM simplificat nu acoperă acest criteriu.
- Validatorul de format primește și fixture-uri negative deliberate; mesajul
  indică owner/scenariu/pas, regula, starea observată și remedierea.
- Eliminarea unei zone, mutarea unui câmp și ruperea închiderii se verifică
  independent; o singură probă de drift al unui token nu le acoperă pe toate.
- Se compară computed style, vizibilitate, ordine și accesibilitate;
  imaginile sunt probe suplimentare, nu singurul verdict.
- Se inspectează ownerul selectat în `.test-content-detail:visible`; template-ul
  ascuns și copia selectată nu se numără drept două execuții sau două probe.
- Se extind componentele de modul și integrarea adaptorului Shop; E2E-urile
  aplicației nu se complică pentru CSS sau stări locale ale rendererului.
- Guard-ul de arhitectură verifică dependențele și ownership-ul generic;
  nu impune nume de clase ori rezultate prin căutări fragile în text.
- Reporting Tests folosește fixture-uri neutre și porturile publice; testele
  adaptorului Shop rămân în Shop, fără dependență inversă între modulele de teste.
- Review-ul documentează aceste cerințe; implementarea și modificarea testelor
  de guvernanță aparțin incrementului ulterior.

## GOV-API-01 — guvernanță mai puternică pentru formatul API

Un singur contract de prezentare trebuie să guverneze modelul Reporting Tests,
standalone-ul Shop, auditul și API din UI+API. Aceeași intrare semantică produce
același subarbore API; diferă numai părintele legitim și datele disponibile.

<details>
<summary>Gate-uri cerute, responsabilitate și probe negative obligatorii</summary>

- [Contractele de artifact](../../atf-reporting/src/main/java/atf/api/reporting/artifact/contract/ReportArtifactEvidenceContracts.java)
  verifică în principal tokenuri prin `html.contains`; prezența lor nu exclude
  banda fazei și nu stabilește părintele unui disclosure.
- [Guvernanța ownership-ului](../../atf-tests/src/test/java/atf/api/tests/governance/ArchitectureOwnershipBoundaryTest.java)
  protejează separarea modulelor; nu dovedește singură paritatea arborelui API.
- Probele noi trebuie să completeze aceste limite, nu să redenumească drept
  „guvernanță de format” verificările de existență deja prezente.

| Gate | Responsabil | Abatere care trebuie respinsă |
|---|---|---|
| Compoziție generică și dependențe | `atf-tests`: guvernanță de arhitectură | Consumer cu renderer/CSS API paralel sau dependență a Reporting de Shop/teste |
| Schema per exchange și fază | `atf-reporting-tests`: componente | Bandă de audit a fazei, ordine diferită sau validare la alt owner |
| Câmpuri și corpuri, pe același profil | `atf-reporting-tests`: fixture neutră parametrizată | Auth/replay în alt domeniu, corp omis sau date inventate |
| Adaptorul de aplicație | Shop: componente proprii | Date pierdute ori bandă reintrodusă la integrarea publisherului |
| Stare și stil efectiv | Reporting Tests: browser cu runtime real | Copii vizibili sub părinte închis, ARIA fals ori geometrie diferită |
| Verdict și diagnostic | Componente Reporting și Shop | Eliminarea benzii pierde eșecul, cauza redactată sau legătura Fails |
| Participare la suită | Guvernanța suitei și configurației de test | Matrice neexecutată, exclusă, dezactivată ori zero cazuri descoperite |

</details>

- Contractul executabil generic aparține `atf-reporting`; fixture-urile și
  componentele îl consumă în `atf-reporting-tests`. Shop verifică propriul
  adaptor fără importarea publisherilor săi în testele neutre Reporting.
- Guard-ul de arhitectură inspectează dependențele și limitele de compoziție,
  nu impune numele unei clase noi sau un singur apel textual la o metodă.
- Validatorul structural inspectează nodurile reale și ownership-ul lor;
  simpla prezență a tokenurilor în CSS/JS nu demonstrează formatul.
- Aceeași fixture completă traversează porturile publice pentru API direct,
  audit și UI+API, în Extent și fallback; comparația normalizează doar ID-uri,
  timestamp-uri și valori volatile, nu ordinea, ownerul sau prezența nodurilor.
- Se probează separat reintroducerea benzii, ascunderea ei cu CSS, redenumirea,
  pierderea unui disclosure legitim, mutarea validării și modificarea stilului;
  fiecare variantă neconformă trebuie respinsă de regula corespunzătoare.
- Fixture-urile minimale de navigare rămân utile, dar nu certifică schema
  completă; un profil UI fără API nu primește exchange-uri fictive pentru gate.
- Nu se fixează numere globale de rânduri sau numele endpointurilor business;
  se fixează rolurile, ordinea, domeniile, tokenurile și starea de expandare.
- Revizuirea probelor care cer acum `Audit details` păstrează aserțiunile
  pentru redacție, snapshot, verdict, tastatură și viewport și adaugă absența
  benzii; nu înlocuiește verificările stricte prin `contains` permisiv.
- Nu se relaxează testele de guvernanță existente: schimbarea lor cere
  feedback direct, scope `testChanges` exact, baseline și probe negative.
  Această analiză nu schimbă scope-urile executabile și nu implementează teste.

**Închidere:** fiecare variantă neconformă produce un diagnostic cu regula,
scenariul și nodul afectat; matricea conformă trece și este descoperită în
suita obligatorie, fără skip. Contractul se promovează în README-urile ownerilor
numai după implementare și verificarea randării.

## Documentație și criteriu de închidere comun

- [Contractul de raportare](../ATF_Reporting_Contract.md) delimitează formatul
  nou per exchange de cheile legacy; fiecare implementare viitoare promovează
  numai contractul demonstrat, fără a prezenta criteriul restant ca funcționalitate livrată.
- [README Reporting](../../atf-reporting/README.md) deține compoziția și
  tokenurile; [README Reporting Tests](../../atf-reporting-tests/README.md)
  deține probele; [README Shop](../../atf-test-practice-shop/README.md)
  descrie intențiile, contractele și selecția consumerului.
- Etichetele au pluralizare corectă, inclusiv `1 attempt`; formatarea numărului
  nu se repară separat în fiecare publisher.
- Cheile interne precum `response.duration.ms` și `retry.events` nu țin locul
  etichetelor pentru cititor; modelul însuși are această lacună de prezentare.
- Închiderea cere fixture nouă din codul implementat, verificare interactivă a
  criteriului și probe negative care chiar eșuează.
- O suită verde de markup, date sau viewport nu închide defecte de interacțiune;
  contractele deja demonstrate rămân păstrate.
- După implementare se promovează regulile stabile în documentele ownerilor și
  se elimină din feedback numai lacunele efectiv închise.

## Scope executabil — uniformitate și viewport

Acest ToDo structurează schimbările testelor aferente cerințelor existente de
mai sus. Nu închide interacțiunile sau uniformitatea înaintea probelor cerute.
Testul browser declară numai intențiile; fixture-ul dedicat din `support`
deține generarea controlată și verificările interacțiunilor.
Selectorii probei sunt centralizați separat în `support/locators/disclosure`;
fixture-ul consumă această interfață semantică, fără selectori Playwright direcți.
Același owner deservește și proba de viewport API în Extent/fallback, fără
duplicarea selectorilor pentru request, execution, wrapper și rânduri.

Scope-ul include concordanța singular/plural deja introdusă în probele Extent,
fallback și UI; rămân aceleași aserțiuni stricte, cu vocabularul comun actual.

```yaml
testChanges:
  - id: REPORT-API-UNIFORMITY-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Probe pentru uniformitatea dovezilor API, inchiderea subarborelui si viewport-ul comun.
    proof: mvn -pl atf-reporting-tests -am -Dtest=ReportPhaseDiagnosticTest,ReportPhaseAuditBrowserComponentTest,ExtentReportRuntimeTestsComponentTest,HtmlReportViewsTest,AtfReportMarkupTest,ExtentReportRuntimeScriptTest,ExtentReportRuntimeNavigationComponentTest,ExtentReportDisclosureBrowserComponentTest,ReportApiViewportParityComponentTest,AtfReportEvidenceCssTest,ReportEvidenceMarkupTest,ExtentReportSessionContractTest,HtmlReportSessionTest,AtfReportSemanticSnapshotTest,ExtentReportUiEvidenceRenderingTest,ExtentReportUiFailureEvidenceTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/html/HtmlReportViewsTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/markup/AtfReportMarkupTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/runtime/core/ExtentReportRuntimeScriptTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/runtime/tests/ExtentReportRuntimeTestsComponentTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/runtime/section/ExtentReportRuntimeNavigationComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/inspection/ExtentReportDisclosureBrowserComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/inspection/ReportApiViewportParityComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/disclosure/ExtentReportDisclosureProbe.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/viewport/ReportApiViewportProbe.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/locators/disclosure/ReportDisclosureLocators.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/theme/evidence/AtfReportEvidenceCssTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/evidence/markup/ReportEvidenceMarkupTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/session/ExtentReportSessionContractTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/html/HtmlReportSessionTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/semantic/AtfReportSemanticSnapshotTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidenceRenderingTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiFailureEvidenceTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/diagnostic/ReportPhaseDiagnosticTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/inspection/ReportPhaseAuditBrowserComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/diagnostic/ReportPhaseAuditProbe.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/locators/diagnostic/ReportPhaseAuditLocators.java
```

## Scope executabil — ARIA pentru disclosure-uri native

Probe browser pentru starea efectivă `open` și ARIA, prin click și tastatură,
inclusiv după selectarea repetată a ownerului. Fixture-ul folosește rendererul
real; nu înlocuiește HTML-ul produs cu un DOM simplificat.

```yaml
testChanges:
  - id: REPORT-DISCLOSURE-ARIA-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Disclosure-urile native Java trebuie sa sincronizeze open si aria-expanded inclusiv in copia selectata.
    proof: mvn -pl atf-reporting-tests -am -Dtest=EvidenceDisclosureAriaComponentTest,ExtentReportDisclosureBrowserComponentTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/inspection/EvidenceDisclosureAriaComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/disclosure/EvidenceDisclosureAriaProbe.java
```

## Scope executabil — publicarea rapoartelor voluminoase

Înregistrarea delimitează PUB-01: buget JVM configurabil, păstrarea dovezilor
și măsurarea într-un proces separat. Fixture-ul bounded este implementat;
failure-path-ul și profiluri mai mari rămân criterii de închidere.

```yaml
testChanges:
  - id: REPORT-LARGE-ARTIFACT-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Verificarea bugetului JVM configurabil si a integritatii dovezilor din rapoarte voluminoase.
    proof: mvn -pl atf-reporting-tests -am -Dtest=ExtentReportFinalizerBoundaryTest,LargeReportPublicationComponentTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - pom.xml
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/session/ExtentReportFinalizerBoundaryTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/publication/LargeReportPublicationComponentTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/support/publication/LargeReportPublicationProbe.java
```

## Scope executabil — schema auth comună

Cerut direct: păstrează testul Shop existent și aliniază profilul, mecanismul,
aliasurile și redactarea între standalone și UI+API. Probele compară și
fallback-ul, mecanismul necunoscut și accesul anonim. Aserțiunile UI existente
rămân în scope-ul `UI-CHECKPOINT-CONTEXT-01`; se schimbă numai eticheta
contractuală în `AUTH TYPE` și se adaugă controlul negativ pentru vechea celulă.

Suita Shop verifică strict default-ul `Pliers`, deja prezent în YAML și în
fallback-ul loaderului din codul de bază; o probă separată păstrează alegerea
explicită `Pliers` sau `Combination Pliers`. Nu se schimbă fixture-ul live
și nu se înlocuiește egalitatea strictă printr-o aserțiune permisivă.

```yaml
testChanges:
  - id: API-AUTH-SCHEMA-01
    status: VERIFIED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Schema auth comuna trebuie sa pastreze distinct profilul si mecanismul dupa redactare.
    proof: mvn -pl atf-reporting-tests,atf-test-practice-shop -am -Dtest=ApiAuthenticationSchemaContractTest,ReportAuthObservationTest,ExtentReportUiEvidencePublishingTest,ShopComponentSuite -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/auth/ApiAuthenticationSchemaContractTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/ReportAuthObservationTest.java
```
