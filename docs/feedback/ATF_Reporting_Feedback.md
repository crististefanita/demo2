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
| Checkpoint `screen-validation` fără request/response HTTP sintetice; context și politică păstrate intern | [README ATF Reporting](../../atf-reporting/README.md), [Contractul de raportare](../ATF_Reporting_Contract.md) |
| Context intern al fazei; diagnostic tipizat la scenariu și cauză izolată în Fails | [README ATF Reporting](../../atf-reporting/README.md#context-de-fază-și-diagnostic-de-execuție), [Contractul de raportare](../ATF_Reporting_Contract.md#context-intern-și-diagnostic-al-scenariului) |
| Schema auth: profil, mecanism și redactare | [Schema auth a dovezilor API](../ATF_Reporting_Contract.md#schema-auth-a-dovezilor-api) |

Fișierul păstrează ținta existentă `@DocumentationTarget`; cerințele de mai jos
sunt corelate cu sursele live și cu interacțiunile din rapoarte randate.

## Cerințe deschise, în ordinea implementării

| Prioritate | Cerință | Responsabil | Dovada pentru închidere |
|---|---|---|---|
| P1 — PUB-01 | Publicare predictibilă pentru HTML voluminos cu PNG-uri inline | Reporting: pipeline și teste de componentă | Proces separat cu heap bounded, fixture determinist și artifact complet |
| P1 — API-01 | Închiderea părintelui ascunde întregul subarbore | Reporting: runtime de navigare | Browser: noduri cu dovezi proprii și copii |
| P1 — API-02 | Rolul și prezentarea provin din tipul nodului | Reporting: model și proiecție | Titluri personalizate și metode variate păstrează formatul |
| P1 — API-03 | Aceeași schemă de dovezi pentru toate intrările API | Reporting și adaptoare | Paritate semantică/vizuală, corp absent și replay |
| P1 — API-06 | Aserțiuni și verdicte atribuite canalului și exchange-ului corect | Reporting și contractul checkpoint-ului | UI PASS/API FAIL și invers, fără contaminarea fraților |
| P1 — API-04 | Intenție lizibilă și publicare cauzală fără dubluri | Shop la intrare; Reporting la randare | Apeluri distincte, retry și republicare diferențiate |
| P1 — gate comun | Teste de componentă care resping abaterile | `atf-reporting-tests` | Matrice pozitivă și probe negative explicite |
| P1 — GOV-API-01 | Guvernanță pentru aceeași structură API, fără excepții per consumer | `atf-tests` și `atf-reporting-tests` | Boundary comun, matrice executată și fixture-uri neconforme respinse |
| P2 — CATEGORIES-EMPTY-01 | Filtrul fără rezultate păstrează detaliul categoriei precedente | Reporting: Categories | Cu filtrul Fail pe fixture fără FAIL: listă și detaliu goale explicit; Clear restaurează selecția |
| P2 — documentație | Eliminarea contradicțiilor contract/cod | Responsabilii README-urilor | Un contract actual, promovat după implementare |

### Progres verificat în implementarea curentă

| Cerință | Ce există în cod și ce s-a probat | Ce împiedică închiderea completă |
|---|---|---|
| API-01 | `Expand all nodes` / `Collapse all nodes` controlează întregul arbore structural al ownerului selectat: scenariu, fază, checkpoint, validation și exchange imbricat. Componenta verifică click, Enter/Space, repetare și reselectare; disclosure-urile native păstrează starea locală. | Copiile Fails/Categories și matricea mai largă de frați/verdict rămân în gate-ul comun; defectul expandării globale este remediat. |
| API-02 | Rolurile fază/request/validation provin din ierarhia de noduri, nu din titlu; componenta browser folosește un titlu de validare personalizat. Excepția de prezentare pe textul `Contract validation` a fost înlocuită cu prezența dovezilor UI. | Matricea de titluri/metode și aggregate-ul de rețea în browser, inclusiv copie selectată și Fails. |
| API-03 | `ApiExchangeEvidence` publică aceeași proiecție request/response/execution pentru API direct, adaptorul legacy real, API imbricat în UI și fallback; Shop reutilizează proiecția. Șase cazuri browser compară metadatele și JSON-ul, inclusiv absent, JSON `null`, necapturat și mock nereexecutabil. | Paritatea datelor este probată; ownership-ul verdictelor și matricea vizuală extinsă rămân API-06/GOV-API-01, fără PASS contractual inventat. |
| API-07 | Metoda rămâne fidelă capturii; `null` devine `Not captured`, zero măsurat rămâne `0 ms`, response/total sunt distincte de badge-ul ciclului de raportare. Testele resping durate negative/malformed și verifică adaptorul Shop. | Incrementul metode/timing este rezolvat; latența browserului rămâne sincer nemăsurată dacă sursa nu o capturează, nu este estimată de publisher. |
| Gate comun | Verificarea statică cere elemente reale în `<body>`; componentele fazei resping și banda ascunsă, redenumită sau goală, inclusiv wrapper-e vendor diferite. | Override CSS final/media query pe stilul efectiv și toate combinațiile de verdict; integrarea matricei comune rămâne GOV-API-01. |
| GOV-API-01 | Există guard-uri de ownership și probe de artifact; unele verificări caută doar tokenuri globale în HTML. | Gate structural per nod, cu probe negative, și verificarea executării aceleiași matrice pentru intrările API. |

- API-04 și API-06 rămân deschise pentru publicarea cauzală și ownership-ul
  verdictelor; nu sunt închise de paritatea câmpurilor sau de corectarea duratelor.
- Testele de șiruri sunt guard-uri auxiliare; probele de browser și
  artefactele generate decid închiderea.
- API-05 este promovat la ownerii de mai sus: matrice Extent/fallback, probe
  negative structurale, diagnostic redactat și izolarea SETUP/PASS de cauza
  TEST BODY/FAIL. Suita completă a incrementului: 597 teste Reporting, fără
  eșecuri, erori sau skip; MCP verifică și colapsarea scenariului.
- API-08 și cardinalitatea etichetelor sunt promovate în
  [contractul de raportare](../ATF_Reporting_Contract.md#dovezile-unei-cereri) și
  [matricea responsive](../../atf-reporting-tests/README.md#verificarea-randării-responsive).
  `TABLE-VIEWPORT-01` extinde verificarea la politica comună, SETUP, aserțiuni
  și contexte/proiecții variate, fără redeschiderea regresiilor demonstrate.

### Dependență deschisă — adnotările directorului de output

Extracția în TestKit și ancora project-relative pentru E2E Reporting sunt
urmărite exclusiv în [feedback-ul infrastructurii de adnotări](ATF_Test_Infrastructure_Annotations_Feedback.md).
Această analiză nu schimbă sensul actual al lui `UiReportSubdirectory` și nu
închide cerințele de prezentare ale raportului.

### Dependență deschisă — selecția canalului testului

Standardizarea UI, API și UI+API prin TestKit este analizată în
[feedback-ul adnotărilor de canal](ATF_Test_Channel_Annotations_Feedback.md).
API-06, API-04 și GOV-API-01 rămân ownerii separării verdictelor, cauzalității
și structurii comune; adnotarea propusă nu închide aceste cerințe.

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
- **Observație separată:** o execuție extinsă a întâlnit un lock tranzitoriu
  pe assetul generat `spark/logo.png` la finalizare; ownerul lock-ului nu este stabilit.
  Rerularea completă a trecut fără oprirea proceselor; riscul concurenței asseturilor rămâne de investigat în PUB-01.

### ASSET-SIZE-01 — verificat pe snapshot-ul integrat

Controllerul fază/nod și wrapperul evidence au fost deduplicate fără praguri relaxate.
După integrarea viewportului comun, alocarea slack-ului coloanelor este proporțională
într-un singur pas, iar Categories reutilizează citirea statusurilor și markup-ul filtrelor.

| Inventar prin manifestul real | Bytes | Plafon existent |
|---|---:|---:|
| JavaScript nativ | 124904 | 125000 |
| Total nativ | 261234 | 265000 |
| JavaScript publicat | 101680 | 105000 |
| Total publicat | 217337 | 220000 |

- Recontrolul integrat Reporting unit/component/integration: 602 teste, fără failure/error/skip; ambele teste de inventar trec.
- Pragurile nu au fost modificate, iar contractele ARIA, layout și scroll local rămân acoperite.
- Marja JavaScript nativ este numai 96 bytes; orice creștere viitoare trebuie reverificată, nu rebazată tacit.

## TABLE-VIEWPORT-01 — verificat, contract promovat la owner

Contractul stabil este la [Reporting](../../atf-reporting/README.md#contractul-raportului),
[contractul ATF](../ATF_Reporting_Contract.md#dovezile-unei-cereri) și
[probele de modul](../../atf-reporting-tests/README.md#verificarea-randării-responsive).

- Orice tabel de date emis de builder primește un viewport; familiile noi folosesc politica implicită.
- Plafonul rămâne tokenul System, cu înălțime naturală pentru scurt și scroll local pentru lung; nu există prag după numărul rândurilor.
- Politica și interacțiunile locale sunt comune Extent/fallback, independente de titlu, contor, fază și părinte `validation`.
- Tabelele de layout, JSON-ul și screenshot-urile rămân în afara viewportului; adaptoarele legacy sunt bounded și idempotente.
- Probele includ toate fazele, contexte request/validation și proiecții, densități variate, celulă unică înaltă, limita de overflow, token alternativ și lățimi standard/înguste.
- Mouse-ul și tastatura ating ultimul rând fără deplasarea panoului exterior; titlurile/contoarele și comportamentul summary-urilor sunt păstrate.
- Cazurile negative resping wrapper lipsă/dublu, plafon greșit/absent, overflow ascuns, antet nefixat și ultim rând tăiat.

Probe țintite verzi și verificare MCP pe artifact nou; gate-ul de dimensiune
`ASSET-SIZE-01` trece pe același snapshot integrat, fără modificarea pragului.

### Scope executabil — TABLE-VIEWPORT-01

Cerut direct: implementare generică în Reporting, fără schimbarea titlurilor
sau contoarelor.

- Probe noi: geometrie, faze, densități, token comun, inventar și abateri deliberate.
- Guard CSS: rămâne în `REPORT-API-UNIFORMITY-01`, cu plafon și antet sticky verificate la ownerul comun.
- Overview: verifică noul owner al plafonului, nu repetarea regulii în fiecare provider.
- Setup: `UI-CHECKPOINT-CONTEXT-01` cere viewportul și închiderea sa înaintea disclosure-ului, fără validare UI artificială sau schimbarea etichetelor.

```yaml
testChanges:
  - id: TABLE-VIEWPORT-01
    status: VERIFIED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Politica comuna de viewport pentru orice tabel de date, inclusiv SETUP si assertions.
    proof: mvn -pl atf-reporting-tests -am -Dtest=ReportTableViewportComponentTest,TableViewportBrowserComponentTest,AtfReportEvidenceCssTest,AtfReportOverviewCssTest,CategoryCardinalityBrowserFixtureTest,ReportApiViewportParityComponentTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/markup/table/ReportTableViewportComponentTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/theme/css/AtfReportOverviewCssTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/inspection/table/TableViewportBrowserComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/table/TableViewportReportFixture.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/table/TableViewportProbe.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/locators/table/TableViewportLocators.java
```

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

Regresia ARIA observată interactiv în ATF Debug este remediată, fără a închide
întreg API-01.

- Listener-ul delegat pentru `toggle` acoperă și disclosure-urile Java și copiile selectate.
- Enter/Space schimbă o singură dată starea summary-ului, fără a intercepta controalele copil.
- Componenta verifică trei canale, două selecții ale ownerului și zero page errors.
- MCP confirmă `open=true`/`aria-expanded=true`; Enter închide, iar Space redeschide sincronizat.

- Defectul `Expand all nodes` este remediat prin controllerul semantic comun,
  owner-scoped; nu se mai bazează pe simpla schimbare a claselor vendor.
  Proba dedicată acoperă și `Collapse all nodes`, imbricare, repetare și reselectare.

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

Producătorii reutilizează acum `ApiExchangeEvidence` din Reporting pentru
metadate, JSON și execuție; adaptorul legacy real nu pierde snapshot-ul tipizat.
Shop păstrează contextul business în request, nu în metadatele response.

Schema auth comună și păstrarea mecanismului după redactare sunt acoperite prin
`ApiAuthenticationEvidence` și probe de componentă. Contractul stabil este în
[schema auth oficială](../ATF_Reporting_Contract.md#schema-auth-a-dovezilor-api);
proiecția comună păstrează și provenance, replay și starea capturii corpului.

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

Increment rezolvat pentru metoda și durata snapshot-ului de transport.
Clasificarea `SEARCH` poate selecta `QUERY`, fără echivalare cu `GET` în dovezi.

- Probe: `QUERY` cu JSON, control `GET`, metodă necunoscută, semnătură și replay.
- Gate: `ShopComponentSuite`, fixture nouă prin publisherul UI/API și inspecție MCP.
- Participare: suita trebuie să includă și testele package-private din
  `atf.ui.practiceshop.support`; selecția doar a pachetului cu `.tests` le omitea.
- Durata absentă rămâne `null` în modelele response/attempt/total și devine
  `Not captured` la prezentare; `Duration.ZERO` rămâne măsurătoarea `0 ms`.

| Dovadă a incrementului | Rezultat și limită |
|---|---|
| Gate extins în staging extern | 602 teste Reporting unit/component/integration pe snapshot-ul integrat; zero failure/error/skip |
| Originea artefactelor | Fixture-uri de componentă din snapshot-uri deterministe, nu E2E live; declarat în Overview/System |
| Replay MCP prin ATF Debug | QUERY și cele trei canale deschise în artifact nou; JSON pretty, response 17 ms / total 43 ms, redacție cu delimitatori păstrați |
| Geometrie inspectată | La 1280 și 960 px, fără overflow orizontal al documentului; zero `<hr>` și zero erori în consolă |
| Originea statusului | Fixture-ul neutru folosește INFO pentru trace și WARNING pentru captura indisponibilă; nu reprezintă un E2E live trecut |
| Limită API-03 | Proiecția comună nu schimbă ownership-ul aserțiunilor/verdictelor; acestea rămân API-06 |

| Gate final al incrementului | Rezultat |
|---|---|
| Reporting unit/component/integration | 602 teste, fără failure/error/skip |
| ShopComponentSuite | 205 teste, fără failure/error/skip |
| WholeAtfGovernanceSuiteTest | 55 teste, fără failure/error/skip |
| Recontrol țintit bulk/timing/parity/replay/assets | 11 teste, fără failure/error/skip |
| MCP UI imbricat | 30/30 noduri deschise; colapsare 0 noduri/0 dovezi vizibile; Enter și Space funcționale |
| MCP API/legacy/UI | JSON pretty, 17/43 ms distincte, zero măsurat și necapturat; replay redactat intact |
| MCP navigare/geometrie | Tests, Categories, Overview/System și Fails inspectate; 1280/960 px fără overflow orizontal, zero `<hr>`, zero erori în consolă |

Fallback-ul folosește acum favicon inline, fără cererea implicită către host;
regresia este verificată în testul autonom și prin MCP/ATF Debug.
Fixture-urile raportate sunt probe de componentă, nu execuții business live.

Selecția largă Shop a descoperit și două teste authenticated live opt-in,
sărite deoarece staging-ul nu a primit fixture-ul extern.
Acestea nu sunt incluse în verdictul celor 205 teste din `ShopComponentSuite`.

Metoda nu mai este rescrisă în mapper, titlu sau rezumat: se păstrează și în
request-ul ambilor publisheri Shop. Probele verifică fiecare nod semantic,
JSON-ul, semnătura și replay-ul; clasificarea `SEARCH` rămâne separată.

Adaptorul browser nu mai construiește durate zero pentru măsurători absente.
Adaptorul direct păstrează response/total distincte; sumarul comun explică
separat timpul transportului și ciclul raportării.

Corpul absent, JSON-ul literal `null` și corpul necapturat sunt stări distincte.
Ultimul nu produce JSON fictiv, primește semnătura `#body=not-captured` și
replay `NOT REPLAYABLE`; mock-ul rămâne `NOT EXECUTABLE`.

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
- Regresiile API-05 deja implementate păstrează redacția, snapshot-ul,
  verdictul, tastatura și viewportul; gate-ul comun trebuie să le integreze,
  nu să le înlocuiască prin verificări `contains` permisive.
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

## Scope executabil — directorul rapoartelor UI

Reconcilierea live a detectat două clase noi în `support.config`, ridicând
pachetul la zece clase. Adnotarea, rezolvarea sigură și testele se separă în
`support.config.output`, fără schimbarea subdirectoarelor selectate de E2E și
fără ieșire din directorul UI; aserțiunile existente se păstrează.

Cele trei E2E-uri deja înregistrate în `REPORT-AUDIT-INPUT-01` rămân la acel
owner unic: failure evidence, navigation aggregation și static UI contract.
Schimbarea importului output este verificată împreună cu fixture-ul lor;
nu se dublează aceleași path-uri în două scope-uri eligibile.

```yaml
testChanges:
  - id: REPORT-UI-OUTPUT-01
    status: VERIFIED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Separarea configurarii output UI pentru limita de opt clase, fara schimbarea comportamentului sau a validarilor E2E.
    proof: mvn -pl atf-reporting-tests,atf-tests -am -Dtest=UiReportSubdirectoryTest,SelectedApiTestsReportUiTest,WholeAtfGovernanceSuiteTest -Datf.governance.testChanges.baseRef=521135291e642194294f9734ee1410ede0f19ef6 -Dsurefire.failIfNoSpecifiedTests=false test
    allowDelete: true
    paths:
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/UiReportSubdirectory.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/UiReportSubdirectoryTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/ReportingTestResources.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/output/UiReportSubdirectory.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/output/UiReportOutputDirectory.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/output/UiReportSubdirectoryTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/e2e/configurable/CategoryCardinalityBrowserFixtureTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/e2e/failure/UiFailureReportContractTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/e2e/section/SelectedApiTestsReportUiTest.java
```

## Scope executabil — expandare globală, timp și proiecție API comună

Cerut direct: butoanele ownerului aplică starea semantică întregului arbore,
durata necapturată nu devine zero, iar publisherii folosesc aceeași schemă
request/response/execution. Se păstrează verdicturile consumerului și cazurile
negative; numai inputuri egale sunt comparate drept proiecții echivalente.

- Guard-urile textuale deja înregistrate își păstrează verificările, cu
  semnăturile controllerului comun și etichete lizibile pentru timpi, URL și retry.
- Celulele și valorile rămân cerute exact; interacțiunea se probează separat în browser.
- Replay-ul trebuie să rămână redactat și sintactic întreg după a doua redactare;
  un corp necapturat nu primește semnătura „absent” sau o comandă curl aparent completă.
- Autonomia fallback include favicon inline; verificarea prin HTTP nu acceptă erorile hostului drept probă de raport fără erori.

```yaml
testChanges:
  - id: REPORT-EXCHANGE-PARITY-01
    status: VERIFIED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Expandare semantica globala si aceeasi proiectie a capturii API cu durate necunoscute explicite.
    proof: mvn -pl atf-reporting-tests,atf-test-practice-shop -am -Dtest=ReportBulkDisclosureComponentTest,ReportTransportTimingTest,ApiExchangeParityComponentTest,ApiReplayRedactionBoundaryTest,ShopComponentSuite -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/inspection/ReportBulkDisclosureComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/disclosure/ReportBulkDisclosureProbe.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/locators/disclosure/ReportBulkDisclosureLocators.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/timing/ReportTransportTimingTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/evidence/ApiReplayRedactionBoundaryTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/inspection/ApiExchangeParityComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/parity/ApiExchangeParityProbe.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/locators/parity/ApiExchangeParityLocators.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/acceptance/ApiAcceptancePublisher.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/support/CoreReportPortsAdapter.java
```

### ARIA — scope separat deja verificat

Probe browser pentru starea efectivă `open` și ARIA, prin click și tastatură,
inclusiv după selectarea repetată a ownerului. Fixture-ul folosește rendererul
real; nu înlocuiește HTML-ul produs cu un DOM simplificat.

```yaml
testChanges:
  - id: REPORT-DISCLOSURE-ARIA-01
    status: VERIFIED
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
