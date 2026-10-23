# ATF Reporting — uniformitate UI/API și interacțiuni de aliniat

Reperul este raportul API cu `UsersApiTests` și `PostsApiTests`, produs de
`atf-reporting-tests`. Uniformitatea rămâne deschisă între acest model,
standalone-ul Shop, auditul componentelor și API din UI+API.

Acest fișier păstrează cerințele Reporting încă deschise, lacunele observabile
și dovezile necesare acceptanței. Contractele implementate sunt descrise în
[contractul semantic](../ATF_Reporting_Contract.md) și
[contractul de display și layout](../ATF_Reporting_Display_Contract.md).

- Probe de componentă și browser: [Reporting Tests](../../atf-reporting-tests/README.md).
- Integrarea consumerului: [README Practice Shop](../../atf-test-practice-shop/README.md).
- Modificarea testelor: [guvernanța ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).

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
| P2 — PUB-FS-02 | Pe Windows, ștergerea unui asset temporar vendor poate bloca finalizarea cu `FileSystemException` | Reporting: publication | probă cu asset temporar blocat, cleanup bounded și rezultat explicit; fără oprirea altor procese, artifact parțial promovat sau suprimarea diagnosticului |
| P2 — documentație | Eliminarea contradicțiilor contract/cod | Responsabilii README-urilor | Un contract actual, promovat după implementare |

### Progres verificat în implementarea curentă

| Cerință | Ce există în cod și ce s-a probat | Ce împiedică închiderea completă |
|---|---|---|
| API-01 | `Expand all nodes` / `Collapse all nodes` controlează întregul arbore structural al ownerului selectat: scenariu, fază, checkpoint, validation și exchange imbricat. Componenta verifică click, Enter/Space, repetare și reselectare; disclosure-urile native păstrează starea locală. | Copiile Fails/Categories și matricea mai largă de frați/verdict rămân în gate-ul comun; defectul expandării globale este remediat. |
| API-02 | Rolurile fază/request/validation provin din ierarhia de noduri, nu din titlu; componenta browser folosește un titlu de validare personalizat. Excepția de prezentare pe textul `Contract validation` a fost înlocuită cu prezența dovezilor UI. | Matricea de titluri/metode și aggregate-ul de rețea în browser, inclusiv copie selectată și Fails. |
| API-03 | `ApiExchangeEvidence` publică aceeași proiecție request/response/execution pentru API direct, adaptorul legacy real, API imbricat în UI și fallback; Shop reutilizează proiecția. Șase cazuri browser compară metadatele și JSON-ul, inclusiv absent, JSON `null`, necapturat și mock nereexecutabil. | Paritatea datelor este probată; ownership-ul verdictelor și matricea vizuală extinsă rămân API-06/GOV-API-01, fără PASS contractual inventat. |
| Gate comun | Verificarea statică cere elemente reale în `<body>`; componentele fazei resping și banda ascunsă, redenumită sau goală, inclusiv wrapper-e vendor diferite. | Override CSS final/media query pe stilul efectiv și toate combinațiile de verdict; integrarea matricei comune rămâne GOV-API-01. |
| GOV-API-01 | Există guard-uri de ownership și probe de artifact; unele verificări caută doar tokenuri globale în HTML. | Gate structural per nod, cu probe negative, și verificarea executării aceleiași matrice pentru intrările API. |

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

### Dependență de infrastructură — verificări de output rămase

Adnotările sunt implementate în TestKit conform
[contractului de infrastructură](../ATF_Test_Infrastructure_Annotations.md).
Probele symlink/junction, launcher și matricea output rămân în
[feedback-ul infrastructurii](ATF_Test_Infrastructure_Annotations_Feedback.md).

### Dependență deschisă — selecția canalului testului

Resolverul TestKit și preflight-ul sunt implementate; matricea de integrare
UI/API/UI+API rămâne în [feedback-ul canalelor](ATF_Test_Channel_Annotations_Feedback.md).
API-06, API-04 și GOV-API-01 rămân ownerii separării verdictelor, cauzalității
și structurii comune; selecția canalului nu închide aceste cerințe.

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

Schema auth comună, aliasurile formatterului și geometria existentă sunt în
contractele semantic și de display. Profilele mock/live sau observație/contract
rămân diferențe legitime.

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
