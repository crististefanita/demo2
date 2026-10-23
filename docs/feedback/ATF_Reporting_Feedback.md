# ATF Reporting — backlog de uniformitate UI/API și publicare

Reperul este raportul API cu `UsersApiTests` și `PostsApiTests`, produs de
`atf-reporting-tests`. Uniformitatea rămâne deschisă între acest model,
standalone-ul Shop, auditul componentelor și API din UI+API.

Acest fișier păstrează numai gap-urile Reporting încă deschise și dovezile
necesare acceptanței. Contractele implementate sunt descrise în
[contractul semantic](../ATF_Reporting_Contract.md) și
[contractul de display și layout](../ATF_Reporting_Display_Contract.md).

- Probe de componentă și browser: [Reporting Tests](../../atf-reporting-tests/README.md).
- Integrarea consumerului: [README Practice Shop](../../atf-test-practice-shop/README.md).
- Modificarea testelor: [guvernanța ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).

## Cerințe deschise, în ordinea implementării

| Prioritate | Cerință încă deschisă | Responsabil | Dovada pentru închidere |
|---|---|---|---|
| P1 — PUB-01 | Limite măsurate pentru publicarea HTML voluminos cu PNG-uri inline | Reporting: pipeline și teste de componentă | Profiluri de volum/PNG distincte, buget de durată și failure-path fără promovarea unui artifact parțial |
| P1 — API-04 | Intenție lizibilă și publicare cauzală fără dubluri | Shop la intrare; Reporting la randare | Apeluri distincte, retry într-un exchange și republicare diferențiate prin fixtures executable |
| P1 — API-06 | Aserțiuni și verdicte atribuite canalului și exchange-ului corect | Reporting și contractul checkpoint-ului | UI PASS/API FAIL și invers, exchange-uri cu verdicturi diferite și agregare fără contaminarea fraților |
| P1 — UI-FAIL-01 | Failure UI generic, acționabil, în Extent și fallback | Reporting + `atf-reporting-tests`; consumer wiring în Practice Shop | Cauza, acțiunea/locatorul și timpul rămân sub ownerul FAIL; redacție, Fails/deep-link și probe negative. Contractul generic curent nu trece încă |
| P1 — gate comun | Gate executabil pentru structură, stil și comportament | `atf-reporting-tests` | Matrice pozitivă și fixture-uri negative care resping lipsa, mutarea, ascunderea sau stilul efectiv incorect |
| P1 — GOV-API-01 | Guvernanță pentru aceeași structură API, fără excepții per consumer | `atf-tests` și `atf-reporting-tests` | Boundary comun, aceeași matrice executată prin publishers și fixture-uri neconforme respinse |
| P2 — PUB-FS-02 | Finalizare robustă când asset-ul temporar vendor este blocat pe Windows | Reporting: publication | Cleanup bounded, rezultat explicit și fără oprirea altor procese, artifact parțial promovat sau diagnostic suprimat |

### Implementare verificată și limite rămase

| Domeniu | Dovadă live | Stare |
|---|---|---|
| API-01 — disclosure | Bulk expand/collapse și disclosure browser sunt implementate; părintele și descendenții sunt auditați în fixture-uri UI/API. | Comportamentul de bază este acoperit; extinderea aceleiași matrice pe copii/proiecții Fails rămâne parte din gate-ul comun, nu un defect separat de bază. |
| API-02 — roluri | Rolurile typed nu se deduc din titlu; probele folosesc titlu custom și rol de validation. | Rămâne acoperirea mai largă pentru combinații de metode, aggregate Network Inspector, copie selectată și Fails. |
| API-03 — schemă exchange | `ApiExchangeParityComponentTest` a trecut în rularea din 23 octombrie: aceeași proiecție pentru API direct, adaptor legacy, API imbricat UI și fallback; include stări de body și replay. | Paritatea de bază este verificată. Ownership-ul verdicturilor și cauzalitatea sunt gap-uri separate, urmărite de API-04/API-06/GOV-API-01. |
| Categories fără rezultate | `CategoryCardinalityBrowserFixtureTest`: 6 teste browser, 0 failure/error/skip. Cazul Fail fără potriviri verifică lista și detaliul goale, fără selecție veche; Clear restaurează categoria. | Închis. Regula normativă este în [contractul Display](../ATF_Reporting_Display_Contract.md#invariante-de-interacțiune). |
| UI-FAIL-01 | Shop publică failure evidence în SETUP, iar componenta Shop verifică timeout-ul simulat și redacția. `UiFailureReportContractTest` a rulat 4 suprafețe: 3 au eșuat, 1 a trecut; inspectorul nu găsește disclosure-ul UI cerut la acțiune/audit/assertion. | Deschis. Nu declara contractul generic Extent/fallback trecut până când raportul conține evidence-ul UI corect sub ownerul FAIL și toate suprafețele, Fails/deep-link și negative trec. Acestea sunt verificări Playwright-only; HTTP Request/Response nu se inventează. |
| PUB-01 | `LargeReportPublicationComponentTest` a trecut în rularea țintită cu JVM copil `-Xmx384m`, 1.500 referințe PNG inline, două imagini distincte/repetate și artifact >9 MB. | Rămân volume mai mari și PNG-uri distincte mari, buget de durată și verificarea failure-path fără promovare parțială. |
| Gate comun / GOV-API-01 | Există verificări de artifact, ownership, API parity și componente browser; rularile țintite confirmă câteva trasee pozitive. | Nu există încă o matrice comună demonstrată pentru toate publishers și probele negative structurale/stil/executare în suite; testele `contains` singure nu închid cerința. |
| PUB-FS-02 | Nu am găsit în probele de publication existente o simulare deterministă pentru fișier vendor blocat pe Windows. | Deschis; criteriul din backlog rămâne necesar înainte de închidere. |

### UI-FAIL-01 — cauza unei acțiuni Playwright eșuate trebuie să supraviețuiască în raport

Raportul [Practice Shop din 23 octombrie 2026](../../artifacts/reports/ui/ui-atf-practice-shop-e2e_2026-10-23_12-33-39-558.html)
arăta `ShopAuthenticatedPurchaseE2eTest` ca FAIL, însă textul HTML nu conținea
cauza din consola IntelliJ: `TimeoutError`, `Timeout 20000ms exceeded` sau
locatorul `button[data-test='login-submit']`. Pierderea era pe calea de eșec a
fazei `SETUP`: wrapper-ul UI publica `Failure evidence` doar când exista
`TEST BODY`; înainte de acesta, setup-ul se închidea cu o aserțiune generică,
fără Throwable-ul original. Un setup deja închis putea apoi continua spre
ramura de `TEST BODY` inexistentă și masca excepția.

Starea verificată pentru failure evidence:

- Reporting are `failureEvidence(...)`, teste de publisher și disclosure pentru
  cauza tehnică.
- Consumer wiring-ul Shop pentru `SETUP` este acoperit separat, fără dependență
  inversă de clasele Shop.
- Proba neutră `UiFailureReportContractTest` există; rularea din 23 octombrie a
  avut 3 eșecuri din 4 suprafețe: Playwright action, audit observation și
  assertion. `SETUP` a trecut.
- Inspectorul nu găsește încă disclosure-ul UI cerut, deci fixture-ul există,
  dar contractul generic nu este verde.

| Probă în `atf-reporting-tests` | Acceptanță |
|---|---|
| Fixture neutră cu excepție mock `TimeoutError` și acțiune `Wait for visible` FAIL | raportul păstrează cauza, mesajul timeout-ului, locatorul și durata sub același test/fază FAIL; nu inventează request/response API (proba există, dar nu trece încă) |
| Extent și fallback, inclusiv Fails/deep-link | aceeași cauză este găsibilă la testul corect; rezumatul este compact implicit, iar detaliul tehnic se deschide la cerere |
| Redactare și negativ | parole/tokenuri din message sau target sunt redactate; un scenariu FAIL fără cauză lizibilă ori cu evidence atașată unui frate face proba să pice |
| Acțiune capturată și screenshot | tabela de acțiuni păstrează statusul FAIL și urmează politica existentă; screenshot-ul nu înlocuiește diagnosticul |

**Progres:** traseul Shop `SETUP` publică Throwable-ul original prin
`failureEvidence`; proba componentă verifică timeout-ul, locatorul, verdictul
FAIL și redacția. Proba browser generică a identificat un gap real: action,
observation și assertion nu prezintă încă disclosure-ul UI așteptat.
**Închidere:** toate cele patru suprafețe trec în Extent și fallback; Fails și
deep-link-ul păstrează ownerul corect; cauza și acțiunea sunt acționabile,
redactate, fără HTTP inventat și fără scurgere la un scenariu frate.

- API-04 și API-06 rămân deschise pentru publicarea cauzală și ownership-ul
  verdictelor; nu sunt închise de paritatea câmpurilor sau de corectarea duratelor.
- Testele de șiruri sunt guard-uri auxiliare; probele de browser și
  artefactele generate decid închiderea.
- API-05 este promovat în documentația Reporting și Reporting Tests: matrice Extent/fallback, probe
  negative structurale, diagnostic redactat și izolarea SETUP/PASS de cauza
  TEST BODY/FAIL. Contractul este în README-urile Reporting și Reporting Tests;
  nu mai este un ToDo separat.
- API-08 și cardinalitatea etichetelor sunt promovate în
  [contractul de raportare](../ATF_Reporting_Contract.md#dovezile-unei-cereri) și
  [contractul de display și layout](../ATF_Reporting_Display_Contract.md) și
  [matricea responsive](../../atf-reporting-tests/README.md#verificarea-randării-responsive).

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

## API-04/API-06 — diferențele încă de demonstrat

Reperul rămâne `ApiAcceptancePublisher`, cu `ApiJsonEvidence`,
`ApiManualReplay` și contractul evaluat prin `ResponseContractReporter`.
Paritatea compară aceeași fixture și același profil, nu un număr fix de câmpuri.

| Domeniu | Lacună rămasă | Criteriu |
|---|---|---|
| Verdict și aserțiuni | Checkpoint-ul mixt nu demonstrează încă ownerul fiecărui rezultat pentru toate cazurile | API-06: verdicturi locale UI/API și pe exchange, apoi agregare |
| Identitate și timp | Apeluri republicate pot pierde identitatea/cauzalitatea; intenția auditului poate fi generică | API-04: două apeluri distincte, retry într-un exchange și register → login fără republicare |

Schema API și paritatea de bază sunt verificate prin `ApiExchangeParityComponentTest`
și descrise în contractele semantic și display. Profilele mock/live sau
observație/contract rămân diferențe legitime, nu abateri de uniformitate.

## API-01 — închiderea atomică a subarborelui

Regresia ARIA observată interactiv în ATF Debug este remediată, fără a închide
întreg API-01.

- Listener-ul delegat pentru `toggle` acoperă și disclosure-urile Java și copiile selectate.
- Enter/Space schimbă o singură dată starea summary-ului, fără a intercepta controalele copil.
- Componenta verifică trei canale, două selecții ale ownerului și zero page errors.
- Browser componenta verifică starea `open` și `aria-expanded` pentru Enter/Space.

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

## API-03 — schemă comună verificată; păstrată ca regresie

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

**Stare:** paritatea de bază este verificată de `ApiExchangeParityComponentTest`
(1 test, 6 variații, verde în rularea din 23 octombrie). Cerința nu mai este un
ToDo activ; varianta completă a criteriului rămâne regresie pentru orice
schimbare de publisher și este normată în [contractul Reporting](../ATF_Reporting_Contract.md#dovezile-unei-cereri).

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
| `Request details` și retry history lungi, imbricate în structura API | Același plafon System/Environment, scroll local, header sticky și ultimul rând accesibil în Extent și fallback; JSON-ul și disclosure-ul exterior rămân în afara viewportului tabelului | Tabel nelimitat, scroll preluat de disclosure/pagină sau coadă inaccesibilă |
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
- Modificarea testelor de guvernanță urmează
  [procedura ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor),
  cu aprobare explicită a regulii și probe negative relevante.

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
- [Contractul de display și layout](../ATF_Reporting_Display_Contract.md) deține
  regulile vizibile promovate: ierarhie, disclosure, viewport și responsive.
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
