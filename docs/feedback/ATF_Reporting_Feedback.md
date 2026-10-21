# ATF Reporting — feedback live pentru librărie și composition tests

## Verdict curent

- `atf-reporting` este librăria standalone pentru model semantic, evidence și
  publicare Extent/HTML;
- `atf-reporting-tests` este consumerul și composition root-ul pentru unit,
  component, integration și browser proof;
- `atf-reporting` nu importă mock, browser, fixture sau alt modul ATF în
  production runtime;
- publisher-ul păstrează datele typed; diagnosticele runtime au seam semantic
  separat de `UI validation`, iar pipeline-ul API trace poate construi
  `API validation → Exchange → request/response/execution`; compoziția live
  atașează acum această validare la checkpoint-ul Gherkin existent, fără un al
  doilea nod semantic pentru aceeași propoziție;
- inspectorul agregat de journey este acum o proiecție typed lower-level, cu
  secțiuni nested pentru auth/correlation/transport/signatures/diagnostics, iar
  trace-ul UI reutilizează structura per exchange a raportului API sub
  `API validation`;
- evidence-ul tehnic are acum un stil vizual comun: `Runtime diagnostic`,
  `API network inspector — ordered UI journey` și copiii lor folosesc aceeași
  familie de accent pentru rail/header, în timp ce badge-ul rămâne semantic
  (`Warning` pentru branch-ul guest, `Pass` pentru inspector). Asta aliniază
  lizibilitatea fără să transforme inspectorul în `UI validation` sau warning-ul
  în failure;
- recalibrarea finală păstrează inspectorul ca nod synthetic unic și mută
  `Network aggregate · N exchanges` într-un copil real al inspectorului; numai
  acest copil deține secțiunile typed. `Unique request signatures` și
  `Technical diagnostics` folosesc acum tokenii comuni pentru summary-uri de
  2×, collapsed implicit, fără request-uri duplicate sau schimbări de verdict;
- `REP-NET-07` și `REP-UX-TABLE-02` sunt închise: component proof-ul CSS/markup
  și MCP fresh confirmă inspectorul nested, summary-uri 2× cu glyph inline,
  collapsed implicit, viewport vertical bounded cu header sticky în Extent și
  fallback, precum și screenshot în afara viewport-ului action. Nu s-au
  schimbat orchestration-ul E2E, deduplicarea, cardinalitatea, verdictul sau
  conținutul canonical al exchange-ului.
- feedback nou cristalizat și închis: toate tabelele cu scroll vertical folosesc
  aceeași geometrie ca `System/Environment`, printr-un singur token
  `AtfReportThemeTokens.TABLE_SCROLL_HEIGHT` / `--atf-table-scroll-height`;
  diferențele de 340/500px între suprafețe nu mai sunt permise. Regula include
  Overview/Category, tag audit, Playwright action, audit, assertions și
  echivalentele API/UI; JSON/code panes rămân separat bounded.
- Politica browser performance rămâne separată de fixture-ul component:
  bugetul acceptă variația bounded a scheduler-ului pentru artifactul generat,
  dar păstrează frontieră explicită pentru long tasks și este verificat prin
  fixture component plus gate browser.
- suportul generic se dovedește mai întâi cu input typed și imagini mock;
- E2E rămâne proof de interacțiune și randare, nu mecanismul implicit de
  acoperire;
- contractul stabil este în [`atf-reporting/README.md`](../../atf-reporting/README.md),
  [`atf-reporting-tests/README.md`](../../atf-reporting-tests/README.md) și
  [`ATF_Reporting_Contract.md`](../ATF_Reporting_Contract.md);
- acest fișier păstrează numai limite, decizii și criterii de redeschidere.

## Boundary-ul verificat

```mermaid
flowchart LR
    Input["typed input + semantic status"] -->|alimentează| Reporting["atf-reporting"]
    Reporting -->|proiectează| Model["model + snapshot"]
    Model -->|compune| Assets["native assets + renderer"]
    Assets -->|publică| Artifact["Extent / HTML artifact"]
    Tests["atf-reporting-tests"] -->|unit / component / integration| Reporting
    Tests -->|auditează| Artifact
    Fixtures["mock + fixtures"] -->|furnizează| Tests
```

<details>
<summary><strong>Legendă — ownership și limite</strong></summary>

- `atf-reporting` deține API-ul public, modelele, renderer-ele și publicarea;
- `atf-reporting-tests` compune inputul și verifică output-ul;
- `atf-mock`, fixture-urile și Playwright aparțin consumerului de test;
- browserul validează interacțiunea cu artifactul, nu înlocuiește validatorii
  puri;
- un defect de model, markup, asset sau contract trebuie coborât la cel mai jos
  nivel observabil;
- Practice Shop consumă suportul generic, dar nu este owner-ul lui.

</details>

## Scor pe axe

Scorurile sunt instrument de prioritizare, nu verdict de release. Se schimbă
numai când codul, testele sau un guard produc o dovadă nouă.

| Axă | Notă | Citire agresiv-constructivă | Dovadă live |
|---|---:|---|---|
| boundary standalone | 99 | fundația este solidă; orice dependență nouă către ATF ar fi regresie | POM parentless, `ReportingStandaloneDependencyTest` |
| model semantic | 96 | snapshot-ul și proiecțiile typed sunt verificate; mutația lifecycle este izolată într-un boundary intern, iar recorderul public a rămas fațadă compatibilă; modelele nested încă pot fi simplificate | `ReportExecutionProjectionTest`, `AtfReportSemanticSnapshotTest`, `HtmlReportSemanticSnapshotMapperTest` |
| vocabular HTML/CSS | 97 | Navigation, Overview, Category și fallback views folosesc builder-e typed; seam-urile vendor și labels sunt allowlist-uit explicit | `AtfReportHtmlVocabulary`, `ExtentReportMarkupContractTest`, `ExtentReportCategoryMarkupTest`, `AtfReportHtmlVocabularyTest` |
| renderer parity | 98 | primitivele comune și parity test-ul acoperă tabel, disclosure, status, failure și category empty-state; seam-urile vendor rămân explicit allowlist-uite | `ReportRendererParityTest`, `ReportAssetContractTest`, `HtmlReportViewsTest` |
| asset publication | 98 | manifestul, compactorul și baseline-ul consumer pentru bundle-ul de asset-uri sunt centralizate; HTML-ul generat rămâne dependent de fixture | `ReportAssetContractTest`, `ReportAssetSizeInventoryTest` |
| audit static | 98 | validatorii pe secțiuni sunt puri și legați de matricea de coverage | `ReportingCoverageMatrixTest` |
| unit/component/integration | 95 | suitele dedicate, matrix-ul și provider-ele typed separă lower-level proof de composition; support-ul mai are câteva facades de simplificat | suitele ATF + `ReportingCoverageMatrixTest` |
| semantică diagnostic și trace UI/API | 98 | aggregate-ul, exchange-urile nested și compoziția consumer sunt coerente: un singur checkpoint Gherkin deține `UI validation` și `API validation`; retry-ul rămâne local în execution evidence | `ReportNetworkInspectorTest`, `PracticeShopRuntimeDiagnosticsTest`, `ExtentReportUiEvidencePublishingTest`, fresh UI HTML/MCP |
| inspector journey și auth evidence | 98 | aggregate-ul este typed, bounded și nested; fiecare exchange are identitate proprie, request/response/execution canonical și retry history local, fără JSON duplicat; inspectorul și diagnosticul au aceeași familie vizuală tehnică, cu statusurile păstrate | `ReportNetworkInspectorTest`, `ExtentReportUiEvidencePublishingTest`, `HtmlReportViewsTest`, `ExtentReportRuntimeScriptTest`, fresh UI HTML |
| API binding configurabil | 96 | binding-ul este typed și configurabil per checkpoint/journey; YAML/override-ul selectează contractul, iar journal-ul expune maparea `evidence(checkpoint)` fără liste `last*` | `PracticeShopApiEvidenceBindingTest`, `PracticeShopConfigurationTest`, `PracticeShopE2eContext` și fresh MCP/System |
| disciplină E2E | 96 | cele 8 clase sunt suficiente pentru browser proof; nu se multiplică pentru variații de contract | `E2eSuiteBudgetArchitectureTest` |
| lizibilitate test-kit | 97 | `ReportUiLocators` păstrează numai compatibilitatea publică; ownership-ul selectorilor este împărțit pe ports coezive și guard-uit | `ReportUiLocatorsTest` + surface tests |
| proiecție Extent/HTML | 98 | forma API directă este reutilizată per exchange sub `API validation`, nodul este atașat aceluiași card Gherkin ca validarea UI, iar evidence-ul tehnic folosește rail/header comun; URL-urile păstrează wrapping controlat, iar digestul intern rămâne bounded | `ExtentReportUiEvidencePublishingTest`, `HtmlReportViewsTest`, `ExtentReportInteractionContractTest`, `AtfReportThemeTokensTest`, fresh UI HTML |
| collapse/expand determinism | 98 | toate tranzițiile trec prin `setDisclosureState`; panelul, flag-ul `data-atf-*-open`, `aria-expanded` și glyph-ul sunt sincronizate, iar închiderea scenariului resetează descendenții | component/interaction tests + fresh guest E2E artifact după rebuild: close → delayed normalize → reopen |
| UI startup și interaction performance | 94 | scheduler-ul de readiness este idempotent, ferestrele duplicate de timer sunt coalesced, observerii filtrează burst-urile fără noduri relevante, iar asset-ul separat `extent-navigation-performance.js` publică snapshot-ul diagnostic; bugetul typed component/browser și fixture-ul standard+narrow sunt impuse pe raportul fresh | `ExtentReportRuntimeNavigationComponentTest`, `ReportRuntimePerformanceComponentTest`, `ReportRuntimePerformanceBrowserProbe`, `extent-navigation-performance.js`, fresh MCP HTML |
| **scor global** | **95** | foundation-ul standalone, trace-ul single-node și determinismul disclosure-urilor sunt solide; există încă simplificări interne minore, iar fixture-ul authenticated este o limită separată a consumerului | combinația de mai sus |

Recalibrare pe codul și build-ul live: scorurile rămân `95` global și `94`
pentru interaction performance. Full-suite-ul curat nu justifică o penalizare
de build, iar dovezile fresh pentru `REP-NET-07` și `REP-UX-TABLE-02` sunt acum
închise; limita authenticated externă a consumerului rămâne deschisă separat.

## Exploratory report audit — contractul curent

- Artifactele generate sunt efemere; feedback-ul nu păstrează nume, timestamp-uri,
  URL-uri sau conținut copiat dintr-un raport concret.
- Verificarea folosește build-ul din staging și browser MCP/Chromium pe artifactul
  proaspăt publicat în directoarele configurate; rularile live existente nu sunt
  oprite și nu sunt folosite ca workspace de build.
- Sunt verificate atât raportul API, cât și raportul UI: `TEST BODY`, `SETUP`,
  `Screen validation`, request/response JSON, status/content-type, `Execution
  evidence`, tab-urile Overview/Categories/Fails și disclosure-urile nested.
- Pentru UI sunt verificate șapte semnături de secțiune la viewport standard și
  responsive; suprafața MCP îngustă nu are overflow, separatori decorativi sau
  erori browser.
- Criteriul este comportamental și repetabil; numărul sau numele unui artifact
  individual nu este parte din contract.

### Revalidare după sincronizarea codului live

- Poarta completă a consumerului reporting a trecut într-un staging exclusiv,
  apoi a trecut din nou incremental după sincronizarea surselor: fiecare run a
  executat `530` teste, zero failure/error și trei skip-uri explicite.
  `SelectedApiTestsReportUiTest` trece `7/7`, iar fixture-urile se încarcă.
- `REP-BUILD-02` este închis ca finding de workflow: `NoClassDefFoundError`
  din checkout-ul partajat a coincis cu build-uri Maven concurente pe același
  `target`; problema nu s-a reprodus în cele două rulări izolate. Regula stabilă
  anti-coliziune este în documentul de build safety, nu într-un ToDo de produs.
- Gate-ul responsive configurabil a trecut separat cu `7` teste, fără skip,
  failure sau error.
- Artifactele API/UI proaspete sunt copiate în directoarele live configurate și
  verificate prin MCP; feedback-ul nu fixează numele sau timestamp-ul lor.
- Normalizarea whitespace-ului din verificarea statică păstrează contractul
  publicat și previne false-negative când asset-ul este compactat.

Explorarea MCP este repetabilă fără a fixa un raport concret: în UI se parcurge
`SETUP → TEST BODY → Screen validation`, iar în API se extinde un exchange și se
verifică request/response JSON, headers, status, content-type și execution
evidence. La ambele suprafețe se verifică `scrollWidth <= clientWidth`, zero
`<hr>`/separatoare decorative și zero erori console/page/request colectate de
validatorul browser.

### Finding reparat — `WARNING` nu mai produce `0 tests` în Overview

- **Cauza:** agregarea nativă Extent păstrează corect scenariul ca `Warning`,
  dar sumarul Overview număra numai `PASS`; de aceea un run cu pași efectiv
  trecuți și un diagnostic warning afișa `0 Tests Passed`, `0 test classes
  passed` și `0 steps passed`.
- **Contractul corect:** `PASS + WARNING` este totalul **pass-like** pentru
  headline-ul `Tests Passed`, cardurile `Tests`/`Steps` și procentele lor;
  warning-ul rămâne separat, vizibil în grafice, categorii, badge-uri și linia
  `warnings`. Un warning nu devine `PASS` și nu este ascuns ca failure.
- **Fix:** `extent-overview-audit.js` folosește `passLikeCount(...)` și un
  singur helper pentru footer-ele `Tests` și `Steps`, cu aceeași regulă și
  fără duplicare de calcul.
- **Dovadă lower-level:**
  `ExtentReportInteractionContractTest.overviewCountsWarningsAsPassLikeWithoutHidingTheWarningBucket`
  publică un raport cu status `WARNING` și protejează headline-ul, cele două
  carduri și bucket-ul warning.
- **Dovadă browser executată pe rerunul E2E proaspăt:** MCP arată `Tests Passed
  = 1`, `Tests Failed = 0`, `1 test classes passed` și `1 steps passed`, în timp
  ce `1 warnings` rămâne vizibil în Overview și Categories. Tab-ul Fails arată
  `No failed tests.`; warning-ul este deci păstrat ca semnal auditabil, dar nu
  mai golește totalurile pass-like și nu este reinterpretat ca failure.
- **Gate de randare:** scenariul este expandabil în `SETUP` și `TEST BODY`, iar
  `API validation → Exchange 1 → Request details / Response details /
  Execution evidence` afișează request/response JSON pretty, HTTP 200,
  transport LIVE/HTTP și replay-ul manual. În MCP, `scrollWidth <= clientWidth`
  și `hr = 0`; raportul este lăsat deschis pentru inspecție.

Limite rămase după acest gate:

- `REP-UX-PERF-01`, `REP-UX-PERF-02` și `REP-EVID-08-STRESS` sunt închise prin
  component/browser gates: fixture-ul configurabil rulează la viewport standard
  și responsive, păstrează minimum șase semnături, verifică expandările,
  JSON/evidence, overflow, separatori, imagini și diagnosticele browserului;
- `SHOP-API-REAL-01`: authenticated fixture-ul extern rămâne placeholder și
  `authenticatedE2eEnabled=false`.

```mermaid
sequenceDiagram
    participant T as Tests
    participant S as Scenario
    participant B as TEST BODY
    participant C as copii / validation
    participant D as details
    T->>S: selectează purchase scenario
    S->>B: expand
    loop 13 copii
        B->>C: open child
        C->>D: Playwright / Audit / Assertions
        D-->>C: wait 1.2–1.8 s și recheck
        C-->>B: phaseOpen=true
    end
```

<details>
<summary>Legendă — arborele verificat în auditul de expandare</summary>

- `T` — tab-ul `Tests` și selecția scenariului.
- `S` — cardul scenariului.
- `B` — faza `TEST BODY`.
- `C` — copilul semantic: checkpoint, request sau validation.
- `D` — disclosure-urile de evidence din copil.
- Săgeata inversă confirmă că faza rămâne deschisă după inspectarea copilului.

</details>

| Contract verificat | Rezultat observat |
|---|---|
| `TEST BODY` rămâne deschis după expand | ✅ `phaseOpen=true`, `aria-expanded=true` |
| închiderea scenariului resetează descendenții | ✅ toate phase/request/validation au `open=false`, `aria-expanded=false`, panel `display:none` |
| redeschiderea scenariului este deterministică | ✅ scenariul revine `open=true`, iar copiii rămân închiși până la click explicit |
| disclosure-uri `Playwright action sequence`, `Audit observations`, `Assertions` | ✅ expandate succesiv în copiii disponibili; părintele a rămas deschis |
| exchange API auditabil | ✅ `GET /products/search — Exchange 1` deschis manual; `Request details` (8 fields), `Response details` (4 fields), `Execution evidence` (1 attempt), request/response JSON pretty, HTTP 200 și trace live vizibile |
| JSON evidence inventory | ✅ 9 request JSON blocks și 15 response JSON blocks în artifactul fresh |
| console/page errors | ✅ 0 |
| overflow la viewport 1280 | ✅ `scrollWidth=clientWidth=bodyScrollWidth=1280` |
| decorative separators | ✅ `hr=0` |

Concluzie pentru `REP-UX-TOGGLE-01`: ✅ închis.

- un singur helper de tranziție resetează explicit întregul subarbore la
  închiderea scenariului;
- contractele component/interaction și auditul MCP confirmă sincronizarea
  panelului, flag-ului, `aria-expanded` și glyph-ului după normalizări întârziate;
- baseline-ul exploratoriu rămâne extern și nu devine input de producție.

### Boundary de interpretare pentru artefactele Practice Shop

Reauditul MCP al run-ului curent confirmă că aceeași formă de evidence nu
înseamnă aceeași proveniență:

- raportul UI guest păstrează `API validation` sub checkpoint-ul Gherkin și
  publică per exchange request/response/execution, JSON pretty, status și
  `LIVE / HTTP — network was executed`;
- raportul API component publică aceleași disclosure-uri canonical și JSON
  pretty, dar poate avea `ACCEPTANCE / MOCK — network was not executed`; acesta
  este contract component, nu assertion live asupra serviciului extern;
- warning-ul guest apare în Overview/Categories, în timp ce Fails rămâne fără
  failed tests; warning-ul diagnostic nu trebuie promovat sau ascuns ca
  failure.
- `Precondition` și `UI journey precondition` sunt acum două câmpuri distincte:
  primul descrie setup-ul API configurat, al doilea descrie journey-ul UI
  efectiv. Nu se mai poate interpreta un flow guest ca authenticated dintr-un
  singur câmp ambiguu.

Prin urmare, `REP-TRACE-03` și parity-ul rendererelor sunt validate separat de
`SHOP-API-REAL-01`: forma HTML poate fi verde pe mock, dar dovada de business
live se închide numai cu transport observat `LIVE_HTTP`.

## Recalibrare live — trace per checkpoint versus inspector agregat

```mermaid
flowchart LR
    UI["un singur pas Gherkin"] -->|păstrează| UiValidation["UI validation"]
    UI -->|păstrează| Validation["API validation"]
    Validation -->|reutilizează per exchange| Evidence["request · response · execution"]
    UI -->|opțional, la final| Inspector["Journey inspector\nsummary collapsed"]
    Evidence -->|indexează fără duplicare| Inspector
    Inspector -->|grupează| Auth["Authentication evidence"]
    Inspector -->|explică| Tech["Technical diagnostics"]
    Inspector -->|indexează| Sig["Unique request signatures"]
    Runtime["Runtime signal"] -->|grupat și deduplicat| Inspector
```

<details>
<summary><strong>Legendă — ce este închis și ce rămâne activ</strong></summary>

- `REP-TRACE-01` rămâne închis pentru cauzalitate, sequence și parity lower-level;
  consumer proof-ul confirmă și atașarea vizuală la același checkpoint Gherkin;
- `REP-TRACE-02` rămâne închis pentru parity de vocabular, redaction, filtrarea
  browser noise, replay și execution evidence;
- `REP-TRACE-03` este închis: lower-level `API validation` și exchange-urile
  typed sunt compuse în același checkpoint Gherkin cu `UI validation`; fresh UI
  proof-ul confirmă un singur titlu semantic, exchange-uri unice și retry local;
- fiecare exchange are disclosure propriu și heading identificabil; contextul
  nu mai este codificat în chei flat `API n · ...`, iar metadata de trace nu mai
  ajunge în response details;
- un retry al aceluiași request nu creează un nou `API n`: logical exchange-ul
  rămâne unic, iar attempts/history rămân în `Execution evidence`;
- `Runtime diagnostic` este semantic separat de `UI validation`, iar guest
  `401` response+console este acum o observație agregată cu count și sources;
- dedupe-ul este sigur numai pentru diagnostice corelate prin method/route/status;
  console/page signals fără aceste facts nu pot folosi tripletul gol drept identitate;
- `API network inspector — ordered UI journey` rămâne un singur nod synthetic
  `TRACE`; aggregate-ul typed este publicat nested și scanabil, iar disclosure-ul
  per exchange este separat sub `API validation`. Această zonă nu justifică un
  al doilea pas Gherkin cu același titlu ca checkpoint-ul UI;
- stilul este comun, nu verdictul: inspectorul și `Runtime diagnostic` folosesc
  rail/header cu `var(--atf-accent)` și `var(--atf-accent08)`, dar badge-urile
  păstrează statusul semantic al fiecărui nod. Propagarea către copiii tehnici
  este verificată astfel încât un detaliu nested să nu rămână neutru vizual;
- `ReportAuthObservation` și aggregate-ul lui descriu ce s-a observat și
  redacționează credential-ul; nu pretind validarea tokenului, issuerului sau
  claims-urilor;
- `Overview → System` rămâne pentru configurație operațională relevantă, nu
  pentru trace, auth map, body-uri sau identitate de test;
- reference report păstrează trace-ul ordonat în JSON și afișează agregarea în
  disclosure-uri colapsabile; aceasta este ținta de UX, nu o invitație la
  copierea markup-ului vendor;
- `ExtentReportUiEvidencePublishingTest` verifică acum publisher-ul semantic,
  inclusiv un exchange cu retry: un singur node logical, attempts/history în
  execution evidence și JSON publicat o singură dată per request/response.

</details>

## Inventar live folosit de registru

| Zonă | Stare observată | Interpretare |
|---|---|---|
| production `atf-reporting` | standalone | modelul, renderer-ele, asset-urile și publicarea rămân independente de mock, browser și fixture |
| `atf-reporting-tests` | composition root separat | unit, component și integration sunt nivelurile implicite; E2E rămâne proof de interacțiune și randare |
| package cohesion | boundary valid, maximum 8 | production `ui.network` și test support `support.api`/`support.config.environment` sunt split-uri semantice; nu există allowlist nouă |
| consumer gate | ✅ PASS | lower-level publisher-ul, renderer parity și consumer proof-ul confirmă `API validation → Exchange → request/response/execution` sub un singur checkpoint Gherkin; MCP/Chromium verifică forma vizibilă |

Numărătorile de clase și de teste se obțin din build, suite și guard-uri. Nu sunt
baseline editorial și nu se păstrează aici ca istoric.

## Închideri promovate în owner docs

Acestea nu mai sunt ToDo-uri. Contractul detaliat este în README-ul indicat,
iar testele de mai jos sunt dovada executabilă.

| ID | Owner / dovadă | Verdict |
|---|---|---|
| `REP-BOUNDARY-01` | `ArchitectureOwnershipBoundaryTest`, POM-uri, composition roots | ownership-ul production versus consumer este explicit |
| `REP-BUILD-01` | allowlist Surefire și standalone dependency tests | clase stale ale consumerului nu contaminează build-ul standalone |
| `REP-ADAPTER-01` | `ReportModelAdaptersTest` | compatibility boundary fail-closed; câmpul invalid rămâne diagnostic |
| `REP-EVID-01..06` | policy, profile, session și integration tests | flow mode, screenshot policy, source și cardinalitatea sunt typed și browser-neutral |
| `REP-JSON-01` | formatter, HTML views și parity tests | JSON-ul este redacted, determinist și identic ca regulă în renderer-ele suportate |
| `REP-FORMAT-01` | `ReportAssetCompactorTest` | sursa CSS/JS rămâne multiline; numai copia publicată este compactată sigur |
| `REP-ASSET-08` | manifest, composer și asset contract | asset-urile au owner, format, ordine și namespace verificabile |
| `REP-FACADE-01` | `UiExtentReportOptions` și composition tests | configurația este snapshot-uită o singură dată pe traseul canonical |
| `REP-OVERVIEW-01` | `ExtentReportOverviewMarkup` și contract test | overview injection este semantic și idempotent |
| `REP-RENDER-08` | renderere action/observation/assertion | facade-ul UI nu mai deține toate responsabilitățile de markup |
| `REP-EVID-04` | `StaticHtmlScreenshotAdapterTest` | PNG-ul real este verificat din HTML local, fără Practice Shop sau endpoint extern |
| `REP-EVID-07` | disclosure component test și browser proof | normalizarea evidence este idempotentă; request/response/execution rămân împreună |
| `REP-AUDIT-08` | `ReportAuditSnapshot` și validatorii puri | Overview, Categories, Fails, Screen, JSON și visual projection au reguli separate |
| `REP-COVERAGE-08` | `ReportingCoverageMatrixTest` | fiecare seam public înregistrat cere o dovadă lower-level identificabilă |
| `REP-E2E-08` | `E2eSuiteBudgetArchitectureTest` | plafonul de 8 clase și entry-point-ul E2E sunt guvernate |
| `REP-SIZE-01` | `ReportAssetSizeBudget`, `ReportAssetSizeInventoryTest` | baseline explicit pentru bundle-ul CSS/JavaScript native și publicat, cu limite pe format |
| `REP-UX-PERF-01`, `REP-UX-PERF-02`, `REP-EVID-08-STRESS` | `ReportRuntimePerformanceComponentTest`, `ReportRuntimePerformanceBrowserProbe`, `SelectedApiTestsReportUiTest`, `UiReportStyleValidator`, `ReportAuditContext` și gate responsive configurabil | lifecycle/counters typed, bugetul performance și fixture-ul standard+narrow sunt închise; aceeași matrice verifică minimum șase secțiuni, expandări, JSON/evidence, overflow, separatori, imagini și diagnostice |
| `REP-VOCAB-01` | `AtfReportHtmlVocabulary`, `ExtentReportMarkupContractTest`, `AtfReportHtmlVocabularyTest` | tokenii HTML/CSS/attribute pentru renderer-ele production sunt centralizați; seam-ul `toggleView` rămâne allowlist vendor explicit |
| `REP-BODY-01` | `ReportingSettings`, `ExtentReportNodeSupport`, formatter/config/Extent/HTML tests | politica body este centrală după redaction/pretty-print; default `100`, `0` dezactivează limita, iar markerul publică `first N of TOTAL` |
| `REP-AUTH-01` | `ReportAuthMethod`, `ReportAuthObservation`, `ReportAuthObservationTest` și Network Inspector/UI evidence component tests | taxonomy-ul per exchange este typed și renderer-neutral: method + source (`none`, `bearer`, `cookie`, `api/function-key`, `signed-url`, `token-acquisition`, `basic`, `other/unknown`); clasificarea fără credential este publicată; harta aggregate este verificată prin `ReportNetworkInspectorTest` |
| `REP-LOCATOR-01` | `ReportUiLocatorsTest` și `atf-reporting-tests/.../support/locators/*` | ownership-ul selectorilor este împărțit pe page/tree/evidence/screen/category/failure ports; fațada rămâne compatibilitate publică, fără selector ownership monolitic |
| `REP-NET-02` | `ReportNetworkInspectorTest`, `ReportAuthObservationTest`, `UiRetryPolicyTest`, `PlaywrightActionSequenceTest` și `ExtentReportUiEvidenceRenderingTest` | primitivele de network sunt typed și compacte, iar retry-ul UI este separat, explicit și fail-closed; aggregate-ul journey este acum construit de aceeași proiecție typed |
| `REP-NET-05` | `UiNetworkInspectionConfigurationTest`, `UiNetworkInspector`, `UiNetworkObservation`, Practice Shop YAML și fresh UI E2E/MCP | vertical slice generic pentru browser network observation: opt-in, host allowlist, auth redacted, service/mechanism, body bounded și signature presentation; settlement-ul este bounded și nu face replay |
| `REP-RETRY-01` | `UiRetryPolicy`, `UiRetryOperation`, `UiActionRecorder` și loader/component/render tests | retry fail-closed: implicit un singur attempt; YAML poate declara max attempts, backoff, motiv și numai operații read-only; retry-ul apare ca rând `Info`, iar failure-ul final rămâne verdictul acțiunii |
| `REP-CONSOLE-01` | `atf-reporting-tests/.../AtfConsoleBridge`, `AtfConsoleBridgeTest` | adapterul este deținut de composition tests; `atf-reporting` rămâne standalone și `atf-console` nu depinde de reporting |
| `REP-DIAGNOSTIC-01` | `UiEvidencePublisher.publishDiagnostic`, `ExtentReportUiEvidencePublishingTest` și guest consumer proof | boundary-ul semantic este închis: diagnostic runtime are status/target/assertions și `Runtime diagnostic`; agregarea cross-source este verificată de `ReportNetworkInspectorTest` |
| `REP-TRACE-01` | `UiPhaseLifecycle.apiTrace`, `ExtentReportUiEvidencePublishingTest` și guest Practice Shop E2E | cauzalitatea, sequence, disclosures canonical și atașarea la checkpoint-ul Gherkin sunt păstrate |
| `REP-TRACE-02` | `UiEvidencePublisher`, `EvidenceTableFormatter`, `ApiManualReplayProjectionTest` și publisher tests | parity-ul de vocabular, redaction, browser-noise filtering, replay și execution evidence este închis; compoziția consumer este verificată separat prin `REP-TRACE-03` |
| `REP-NET-03` | `ReportNetworkInspector.JourneyAggregate`, `ReportNetworkInspectorTest` | modelul typed agregă authentication, correlation, transport, signatures și diagnostics; fields sunt bounded și credential-free |
| `REP-NET-04` | `ReportNetworkAggregateMarkup`, `ReportNetworkInspectorTest`, `HtmlReportViewsTest` și fresh MCP HTML | Extent și fallback publică același inspector collapsed cu `Authentication evidence`, `Scenario correlation`, `Transport counters`, `Unique request signatures` și `Technical diagnostics`; `Response details` păstrează numai indexul bounded (4 fields), iar Gherkin-ul și request-urile nu sunt amestecate sau copiate în fields flat; semnăturile sunt carduri compacte, deduplicate, cu method/route/status/count/auth/source/phase; `network.aggregate.*` nu este expus ca tabel flat |
| `REP-NET-06` | `ExtentRequestScope`, `ReportNetworkAggregateMarkup`, `ExtentReportUiEvidencePublishingTest`, `ReportAssetSizeInventoryTest` și fresh MCP HTML | ✅ închis: inspectorul are copil real `Network aggregate · N exchanges`, cu aceeași geometrie de subpas; secțiunile `Unique request signatures` și `Technical diagnostics` au summary de 2×, iar agregarea rămâne typed, deduplicate și fără duplicarea exchange-urilor |
| `REP-NET-07` | `evidence-table.css`, `evidence-stack.css`, `AtfReportThemeTokensTest`, `AtfReportEvidenceCssTest`, `ReportNetworkInspectorTest` și fresh MCP/Chromium | ✅ închis: tokenul este `68px` față de summary-ul standard `34px`, CSS-ul aplică aceeași familie de disclosure, markup-ul este collapsed implicit, iar proof-ul fresh confirmă expand/collapse fără overflow sau schimbare de semantică |
| `REP-AUTH-02` | `ReportNetworkInspector.AuthenticationObservation`, `ReportNetworkInspectorTest` și UI publishing contract | auth este agregat determinist după method/source/status/count, fără credentiale; semantica rămâne „observed”, nu validarea tokenului |
| `REP-DIAGNOSTIC-02` | `ReportNetworkInspector.DiagnosticObservation`, `ReportNetworkInspectorTest` | response și console corelate pentru aceeași semnătură method/route/status sunt un singur observation cu `count` și `sources`; mesajele volatile nu sunt copiate în summary, iar semnalele necorelate folosesc token-uri bounded `UNBOUND-n` |
| `REP-RENDER-09` | `ReportRendererParityTest`, `ReportAssetContractTest`, `ExtentReportCategoryMarkupTest`, `HtmlReportViewsTest` | fallback și Extent folosesc tokenii comuni pentru failure rows, status și category empty-state; seam-urile vendor rămân explicit allowlist-uite |
| `REP-MODEL-01` | `AtfReportSemanticSnapshotTest`, `ReportExecutionProjectionTest` | state mutation, snapshot și projection au boundary-uri explicite; lifecycle-ul refuză mutații după complete sau închiderea părintelui, iar nested state rămâne co-localizat intenționat |
| `REP-TEST-08` | `ReportingCoverageMatrixTest`, suitele layer-izate și `E2eSuiteBudgetArchitectureTest` | fiecare seam nou are motiv și proof class declarate; matrix-ul verifică unicitatea și layer-ul, iar support/fixture nu devine test executabil |
| `REP-OVERVIEW-02` | `ReportEnvironmentSnapshotTest`, `ExtentReportSessionContractTest`, factory-urile typed | toate composition roots publică snapshot typed; publisher-ul filtrează central identitatea de run (`Suite`, `Test package`, `Flow`, `Fixture`) din System, păstrând doar configurația operațională redacted și bounded |
| `REP-SUITE-01` | `AtfReportingUnitSuite`, `AtfReportingComponentSuite`, `AtfReportingIntegrationSuite`, `AtfReportingApiFixtureSuite`, `AtfReportingUiAuditSuite` și `ReportingCoverageMatrixTest` | fiecare layer reporting are launcher dedicat, cu package și tag explicit; plafonul E2E rămâne neschimbat |
| `REP-API-BIND-01` | `PracticeShopApiCheckpoint`, `PracticeShopApiEvidenceBinding`, `PracticeShopConfigurationLoader`, `PracticeShopApiEvidenceBindingTest`, `PracticeShopApiNetworkJournalTest` și fresh Practice Shop E2E/MCP | binding-ul typed și configurabil per checkpoint/journey este citit din YAML/override, jurnalul păstrează `evidence(checkpoint)`, iar agregarea publică doar `selectedEvidence()`; renderer-ul primește numai contractul generic |
| `REP-DIAGNOSTIC-04` | `ReportNetworkInspectorTest`, `ExtentReportUiEvidencePublishingTest` și `ReportNetworkAggregateMarkup` | semnalele necorelate folosesc identity exactă numai intern, sunt separate când mesajele diferă, repetările reale au count comun, iar HTML-ul afișează doar token-uri bounded `UNBOUND-n`, fără mesaj brut și fără SHA digest |
| `REP-TRACE-03` | `ExtentReportUiEvidencePublishingTest` (10 teste, inclusiv combined node și retry), `HtmlReportViewsTest`, `ExtentReportInteractionContractTest` și fresh UI E2E/MCP | ✅ închis: `PracticeShopE2eContext.checkpoint(...)` atașează `API validation` la checkpoint-ul Gherkin existent; fresh proof-ul arată un singur titlu semantic, copii `UI validation`/`API validation`, fiecare exchange o singură dată și retry-ul în `Execution evidence` |
| `REP-STYLE-01` | `ExtentReportRuntimeScriptTest`, `AtfReportThemeTokensTest`, `ExtentReportUiEvidencePublishingTest` și fresh UI artifact verificat prin MCP | ✅ închis: `Runtime diagnostic`, `API network inspector — ordered UI journey` și copiii tehnici folosesc aceeași familie vizuală de accent; `Warning`/`Pass` rămân statusuri distincte, iar inspectorul nu este reinterpretat ca UI validation |
| `REP-EVID-08` | `AtfReportMarkup`, `evidence-table.css`, `HtmlReportViewsTest`, `ExtentReportInteractionContractTest` și fresh UI E2E/MCP | URL value cells au wrapping controlat semantic; valoarea completă rămâne în Extent/fallback, iar browser proof-ul confirmă lipsa overflow-ului la lățimea suportată |
| `REP-TRACE-04` | `ReportRequestSignature`, `ReportTraceMetadataRenderingTest`, `ReportNetworkInspectorTest`, `ExtentReportAssetsTest`, publisher-ul Practice Shop și fresh API/UI MCP HTML | digestul rămâne identitate internă pentru grouping/deduplicare; HTML-ul public proiectează doar `body=present|absent`, `internal digest hidden` și referințe locale `screenshot-n`; nu apar hash-uri brute în trace, assertions sau ancorele screenshot |
| `REP-UX-TABLE-01` | `ValidationTableLayoutComponentTest`, `SelectedApiTestsReportUiTest` și poarta completă `atf-reporting-tests` | ✅ închis: layout-ul evidence este scoped/dirty, folosește fingerprint + cache/diff, reacționează incremental prin `ResizeObserver` sau `requestAnimationFrame`, iar testele acoperă hidden→visible, resize și open/close fără recalculare globală inutilă |
| `REP-UX-TABLE-02` | `AtfReportThemeTokens`, `evidence-stack.css`, `fallback.css`, `ExtentUiActionRenderer`, `ExtentUiObservationRenderer`, `ExtentUiAssertionRenderer`, `HtmlReportEvidenceView`, `AtfReportEvidenceCssTest` și fresh MCP | ✅ închis: action/audit/assertion și inspectorul network reutilizează summary-ul 2× și viewport-ul vertical bounded cu header sticky în ambele renderere; screenshot-ul rămâne în afara viewport-ului action, fără modificări E2E |
| `REP-UX-TABLE-03` | `AtfReportThemeTokens.TABLE_SCROLL_HEIGHT`, `AtfReportThemeTokensTest`, `AtfReportEvidenceCssTest`, `AtfReportOverviewCssTest`, `overview-category-tables.css`, `overview-tag-audit.css`, `evidence-stack.css` și `fallback.css` | ✅ închis: orice tabel scrollabil folosește viewport-ul unic al `System/Environment` (`500px` curent), cu sticky header; nu există literal per renderer, iar Extent/fallback sunt aliniate |
| `REP-CONSUMER-01` | `ReportEnvironmentSnapshot`, `ReportTraceMetadata`, `AtfReportPorts` și `ReportTraceMetadataRenderingTest` | ✅ închis: un consumer nou injectează configurația typed per run și trace-ul typed per exchange în raportul Extent; allowlist-ul System și redaction-ul sunt verificate pe artifactul produs |
| `REP-CAPTURE-01` | `ReportEvidenceProfileTest`, `ReportEvidenceProfileIntegrationTest`, `PracticeShopScreenshotPolicyTest`, `PracticeShopBrowserCaptureProviderTest`, `PracticeShopReportSystemMetadataTest` și fresh guest E2E/MCP | ✅ închis: providerul browser typed este reutilizat pe sesiune; policy-ul filtrează înainte de captură, provenance și payload-ul PNG sunt verificate; Overview proiectează policy-ul efectiv (`each-checkpoint`), nu o etichetă dedusă din booleanul legacy, iar linkurile 📷 deschid imaginile în viewer |

## Limite și criterii de redeschidere

- Închise prin dovezile de mai sus: `REP-RENDER-09`, `REP-MODEL-01`,
  `REP-TEST-08`, `REP-OVERVIEW-02`, `REP-NET-03`, `REP-NET-04`, `REP-NET-06`,
  `REP-AUTH-02`, `REP-DIAGNOSTIC-02`, `REP-DIAGNOSTIC-04`, `REP-NET-07`,
  `REP-API-BIND-01`, `REP-CAPTURE-01`, `REP-EVID-08`, `REP-UX-TABLE-02`,
  `REP-UX-TABLE-03` și
  `REP-UX-TOGGLE-01`.
- Structura `REP-TRACE-03` este închisă prin testele lower-level și fresh UI proof;
  se redeschide numai dacă o rulare nouă produce două carduri Gherkin pentru aceeași
  intenție sau pierde copilul `API validation`.
- `REP-UX-PERF-01` este închis: `extent-navigation-performance.js` publică
  explicit lifecycle-ul `ready=false → ready=true`, iar readiness-ul Java
  nu mai depinde implicit doar de existența unei durate. Scheduler-ul și
  observerii rămân scoped/idempotent, iar snapshot-ul nu schimbă verdictul
  business.
- `REP-UX-PERF-02` este închis: `ReportRuntimePerformanceSnapshot` și
  `ReportRuntimePerformanceBudget` au proof component, iar
  `ReportRuntimePerformanceBrowserProbe` mapează snapshot-ul browserului fresh,
  expandează request-urile prin flow/locators existenți și folosește un buget
  browser separat de fixture-ul component.
- Calibrarea performance nu este stocată ca tabel cu valori dintr-un raport:
  snapshot-ul typed publică readiness, page-load, primul toggle, durata
  toggle-ului, normalizări, mutații și long tasks, iar `ReportRuntimePerformanceBudget`
  decide acceptarea pe configurația consumerului.

- MCP a confirmat în artifactele fresh `SETUP`, `TEST BODY`, `Screen validation`,
  acțiunile Playwright, audit observations și assertions ca dovezi expandabile;
  DOM-ul standard și cel responsive au trecut `scrollWidth <= clientWidth`, zero
  `<hr>`, zero elemente vizibile non-scrollable cu overflow și zero diagnostice
  console/page/request colectate de gate.

- `REP-UX-TABLE-01` este închis: algoritmul de lățime este scoped pe tabelele
  afectate, dirty/fingerprint-based și idempotent; cache-ul evită rescrierea
  când conținutul, fontul sau lățimea disponibilă nu s-au schimbat. Dovada este
  component test-ul de layout plus E2E-ul UI selectat și poarta completă.

| ID | Owner | Limită actuală | Criteriu de închidere |
|---|---|---|---|
| `REP-UX-PERF-01` | `atf-reporting` + `atf-reporting-tests` | ✅ închis: lifecycle readiness explicit, counters/observeri publicați și gate fresh MCP calibrat | se redeschide numai la readiness indisponibil, depășire de buget, loop de mutații sau flicker verificat |
| `REP-UX-PERF-02` | `atf-reporting-tests` | ✅ închis: component fixture și browser probe typed verifică load, expand, counters și long tasks | se redeschide numai la regresie de mapare browser sau la pierderea pragului de șase semnături |
| `REP-EVID-08-STRESS` | `atf-reporting-tests` | ✅ închis: fixture-ul configurabil rulează standard+narrow cu expandări controlate și minimum șase semnături | se redeschide numai dacă dispar semnăturile/evidence, apare overflow, separator decorativ, imagine ruptă sau diagnostic browser |

Limită externă asociată raportării:

- `SHOP-API-REAL-01` aparține consumerului extern și rămâne blocat până la
  fixture-ul authenticated non-placeholder.
- `REP-CAPTURE-01` este închis prin providerul typed reutilizat în
  `PracticeShopE2eContext`: policy-ul este aplicat înainte de captura
  browserului, sursa declarată coincide cu providerul, iar payload-ul absent sau
  invalid este refuzat de `ReportEvidenceProfile`; composition root-ul
  consumerului rămâne owner-ul browserului.

`REP-UX-TABLE-01` este închis; layout-ul reduce suprafața de lucru a
observer-ului, dar nu substituie măsurarea end-to-end a costului de runtime.
Gate-ul stress nu contrazice contractul de wrapping deja verificat.
Regulile rămân criterii de redeschidere dacă un checkpoint multi-exchange pierde
copilul `API validation`, disclosure-ul per exchange, metadata contextuală,
retry history, valoarea completă a URL-ului sau controlul overflow-ului.

<details>
<summary><strong>Calibrare portabilă din modelul extern</strong></summary>

- auth inspectorul descrie ce s-a observat pe transport; nu afirmă validarea
  tokenului, issuerului sau claims-urilor dacă acestea nu au fost verificate;
- browser traffic și `HttpClient` traffic folosesc același vocabular de evidence,
  cu `service`, `source`, `method`, `status`, `auth method` și `outcome`;
- `System/Environment` descrie configurația efectivă a run-ului, separat de
  request evidence și separat de verdictul Extent;
- body preview-ul este o proiecție bounded, nu ștergerea evidence-ului: sumarul
  arată lungimea totală și limita, iar datele sensibile sunt redactate înainte
  de truncare;
- retry-ul este permis numai la boundary-ul explicit al operației și numai când
  policy-ul îl declară sigur; fiecare attempt rămâne auditabil, iar failure-ul
  inițial nu devine PASS implicit;
- Playwright events (`request`, `response`, `request-failed`, console/page error)
  se corelează cu aceeași acțiune/UI step; un timeout fără exchange observat nu
  poate fi confundat cu un răspuns negativ real.

</details>

<details>
<summary><strong>Contractul obligatoriu pentru Overview → System</strong></summary>

`System` răspunde rapid la întrebarea „cu ce configurație operațională a rulat
artifactul?”, nu la întrebarea „ce test sau ce business flow a rulat?”.

- **runtime** — environment, JVM/OS și clientul efectiv;
- **targets** — UI/API origin normalizate, transport mode și precondition;
- **browser** — engine/channel, headless, viewport și zoom, dacă există UI;
- **time** — limitele efective UI/API/navigation în limbaj uman (`20s`, `45s`);
- **network** — inspector, capture source și body-preview bound, numai când sunt active;
- **retry** — stare efectivă, attempts, eligibility și motivul policy-ului;
- **evidence** — API/screenshot/console policy, numai ca mecanisme active;
- **reporting** — formatul și rolul output-ului.

`Suite`, `Test package`, `Flow`, `Fixture`, `Checkout`, numele scenariului și
payload-ul business nu apar în `System`. Identitatea testului rămâne în `Tests`,
pașii și verdicturile rămân în `TEST BODY`, iar request/response facts rămân în
disclosure-ul lor.

Reguli de afișare:

- se publică numai allowlist-ul de configurări operaționale importante, nu un dump arbitrar;
- fiecare valoare este `configured` sau `effective`, după caz;
- eticheta trebuie să explice ce controlează valoarea, nu să repete numele tehnic al proprietății;
- valoarea trebuie prezentată în limbaj operațional: `Timp maxim de așteptare pentru acțiunile UI: 20 s`, nu `UI timeout: PT20S`;
- unitățile și scopul sunt vizibile în aceeași intrare; forma tehnică (`PT20S`, cheia YAML, sursa) rămâne doar în detaliul colapsat;
- configurările se grupează pe scop — runtime, target, browser, timp, network, retry, evidence și reporting — nu într-o listă plată de chei;
- body-urile și mesajele lungi folosesc limita comună
  `maxBodyPreviewChars`;
- `System` descrie configurația, iar evidence-ul din `TEST BODY` descrie ce s-a
  observat efectiv pe flow.

### Politica disclosure-ului API în flow-ul UI

Cardul `TRACE` dintr-un raport UI este o proiecție de network evidence, nu o
copie a raportului API direct. Titlul cardului identifică deja pagina și faptul
că este trace, iar parent step-ul din response evidence păstrează legătura
cauzală. Pentru fiecare exchange rămân numai câmpurile care permit identificarea
și auditarea acțiunii: metodă, path, URL, profil/tip de auth, query, request
headers relevante, transport, replay safe, status, content type și JSON-urile
request/response. Acestea folosesc aceleași label-uri și aceeași redaction policy
ca raportul API direct. Fiecare exchange este un disclosure propriu sub
`API validation`; corelarea vine din ordinea și titlul typed al exchange-ului,
nu din re-encodarea câmpurilor în chei `API n · ...`.
Headerele browserului și response headers volatile sunt filtrate; response
headers stabile/relevante pot apărea prin aceeași disclosure opțională ca în
raportul API direct.

Pipeline-ul lower-level verificat este structurat astfel; atașarea consumerului
la checkpoint-ul UI este acum parte din contractul închis `REP-TRACE-03`:

- `UiEvidencePublisher` oferă copilul `API validation` și câte un request nested
  pentru fiecare exchange; consumerul îl atașează checkpoint-ului Gherkin existent,
  fără al doilea card;
- `ExtentRequestScope`/`HtmlReportRequestScope` propagă statusul copilului și
  păstrează retry-urile aceluiași exchange în execution evidence;
- `HtmlReportEvidenceView` reutilizează renderer-ele request/response/execution
  pentru fiecare node, iar metadata `TRACE PARENT STEP`/`TRACE POSITION` rămâne
  în request context, nu în response table;
- URL value cells folosesc o clasă semantică comună Extent/fallback pentru
  wrapping controlat, fără trunchierea valorii.

Un trace cu mai multe exchange-uri trebuie să publice un execution disclosure
pentru fiecare exchange. Păstrarea numai a ultimului execution ar face raportul
neauditabil: request/response ar arăta două schimburi, dar durata, retry-ul și
attempt history ar aparține doar celui din urmă.

Suprafața vizibilă minimă trebuie să rămână scurtă:

- **Runtime** — mediu, client și identity tehnică relevantă;
- **Ținte** — aplicația UI și API-ul folosite;
- **Browser** — engine, headless și viewport, dacă există UI;
- **Timp** — limitele efective relevante pentru UI/API/navigation, într-o formulare umană;
- **Precondition** — transport/auth mode, fără identitate de test sau secret;
- **Evidence** — network/auth inspector, retry și screenshot policy, numai când sunt active sau relevante;
- **Reporting** — tipul artefactului și rolul destinației.

Într-un detaliu tehnic separat pot rămâne cheile YAML și valorile ISO necesare
pentru debugging, dar niciodată `Suite`, `Test package`, fixture, checkout,
payload business sau path-uri temporare. Acestea nu trebuie să aglomereze prima
privire și nu se repetă în `TEST BODY`.

</details>

## Contracte verificate în consumerul Practice Shop

- contractul guest E2E a fost verificat prin component/integration tests și
  browser proof MCP/Chromium;
- `Overview/System` arată metadata consumerului;
- Practice Shop nu mai publică `Suite`, `Test package`, `Flow`, `Fixture` sau
  `Checkout` în `Overview/System`; snapshot-ul typed rămâne limitat la
  configurația operațională efectivă;
- `TEST BODY` păstrează inspectorul collapsed-by-default, iar `Request details`
  și `Response details` sunt expandabile;
- warning-ul guest este acum `Runtime diagnostic`, cu status/target/assertions;
  inspectorul final adaugă aggregate-ul typed compact; fresh UI proof-ul arată
  `UI validation` și `API validation` sub același card Gherkin;
- prezența textuală în HTML nu este suficientă: dovada cere component test,
  rerun E2E și verificare vizuală.

`REP-REVIEW-01` este regulă de întreținere, nu gap de production: orice claim
nou trebuie să indice o clasă, un test sau un guard; un run izolat nu închide un
ToDo.

## Contracte live — shell-ul Category și verificarea de markup

Situația curentă a shell-ului Category, a view-ului Fails și a fallback-ului:

- `ExtentReportCategoryMarkup`, `HtmlReportFailsView`, `HtmlReportTestsView` și `ExtentReportPublicationPipeline` folosesc aceeași graniță semantică precum Navigation și Overview;
- `Tag`, `CssClass`, `Attribute`, `ViewId` și `AtfReportLabels` descriu contractul repetat;
- builder-ele compun separat lista, toolbar-ul, back-link-ul și empty-state-ul;
- singurul fragment raw rămas este markerul vendor `atf-fails-view`, păstrat ca boundary de integrare.
- `ExtentReportScreenshotDeduplicator` folosește vocabularul pentru anchor, emoji și referința inline; fingerprint-ul rămâne intern și nu este afișat ca nume sau link de raport.

Dovada lower-level este distribuită astfel:

- `ExtentReportCategoryMarkupTest`, `ExtentReportMarkupContractTest`,
  `HtmlReportViewsTest` și `HtmlReportSessionTest` verifică shell-ul semantic;
- `ExtentReportAssetsTest` verifică deduplicarea screenshot-urilor;
- contracte verificate: idempotency, ordine, empty-state, CSS classes, fallback Fails, redaction și navigare;
  - gate-ul focalizat de model și publisher proof-ul aggregate sunt verzi; lista
    exactă de teste rămâne responsabilitatea suitei și a build-ului, nu a acestui
    registru.
- verificarea fresh prin MCP/Chromium confirmă `SETUP` → `TEST BODY` → `Screen validation`, tabelul `Setup evidence` cu PASS, acțiunile Playwright expandabile și simbolul `+` inline; nu există delimitatori `<hr>` în artefactele API/UI auditate.

REP-VOCAB-01 este închis pentru tokenii production acoperiți de contractele
actuale; seam-urile vendor rămase sunt explicit allowlist-uri, nu ToDo-uri:

- nu se deschide un refactor mecanic al fiecărui token vendor;
- următorul pas adaugă întâi o dovadă component;
- markerii vendor rămân într-un allowlist explicit.

## Progres live — metadata typed în composition roots

`ReportEnvironmentSnapshot` este acum folosit direct în următoarele boundary-uri
de compoziție:

- `AtfExtentReportTestExtension` pentru API contract tests;
- `ApiReportAuditSetup` pentru precondiția API mock a testelor UI;
- `ReportAuditContext` pentru auditul browser al unui artifact existent;
- fixture-urile de failure, category-cardinality și THEN disclosure;
- `PracticeShopReportSystemMetadata` pentru consumerul Practice Shop.

`ApiReportAuditSetup` nu mai construiește direct vocabularul metadata: delegă
factory-ului `ApiReportEnvironmentSnapshotFactory`, iar contractul component
verifică allowlist-ul API precondition separat de orchestration-ul mock/runner.

- aceste roots publică numai câmpuri din `ReportEnvironmentField`;
- valorile sunt bounded și nu includ credentiale sau path-uri tehnice;
- overload-urile cu `Map<String,String>` rămân doar compatibilitate, nu calea
  recomandată pentru cod nou;
- contractul `System` este filtrat central prin `systemDisplayValues()`;
- binding-ul typed per checkpoint/journey este închis în consumer;
- `REP-TRACE-03` este închis pentru compoziția single-node observată în fresh UI;
  se redeschide numai la regresie verificată în Extent/fallback/MCP. `REP-EVID-08`
  rămâne închis pentru wrapping și se redeschide numai la regresie verificată.

### Revalidare recentă a modelului typed

`ReportExecutionProjectionTest` verifică acum și agregatul
`ReportExecutionProjections`, inclusiv coerența dintre fațadele istorice,
snapshot-ul typed și failure-ul nested. Agregatul folosește acum un collector
single-pass, iar testul a trecut în staging extern după sincronizarea live;
această dovadă reduce riscul de proiecții divergente și de traversări duplicate,
dar nu închide încă simplificarea stărilor nested din lifecycle.

`AtfReportSemanticRecorder` nu mai conține storage-ul mutabil: el păstrează
contractul de handle folosit de Extent și fallback, `AtfReportSemanticLifecycle`
este orchestration boundary, iar `AtfReportSemanticState` deține graful mutable
și materializarea snapshot-ului. `AtfReportSemanticSnapshotTest` verifică
snapshot-uri independente înainte și după mutație, fără ca rendererul să fie
implicat. Stările concrete rămân co-localizate în state aggregate deoarece
formează un singur graf de mutație; un split ulterior este justificat numai de
un seam testabil nou, nu de reducerea artificială a numărului de linii.

## Key/value evidence — închis

Regula stabilă pentru `FIELD | VALUE` este în
[contractul de raportare](../ATF_Reporting_Contract.md#layout-pentru-evidence-field--value).

- Test component: `ValidationTableLayoutComponentTest` verifică alocarea și
  `data-atf-key-column-width`.
- Audit browser: `ValidationTableLayoutInspector` verifică lățimea dominantă a
  lui `VALUE` și absența overflow-ului; proba confirmă și
  `data-atf-key-value-layout` la viewport standard și îngust.
- Stare: niciun ToDo activ; redeschidere numai la regresie reprodusă în
  component test și browser.

## Ce nu se redeschide

- o regulă stabilă mutată în README-ul ownerului;
- un test E2E existent doar pentru că se dorește o variantă de date;
- un defect de whitespace când contractul semantic și randarea sunt intacte;
- un artifact local, timestamp, path sau captură care nu este reproductibilă;
- o separare de package propusă fără owner, motiv și test de boundary.

## Protocol de revalidare

<details>
<summary><strong>Pași obligatorii când se schimbă reporting-ul</strong></summary>

1. rulează testul focalizat pe seam-ul schimbat;
2. rulează `ReportingCoverageMatrixTest` și testele component/integration relevante;
3. pentru HTML/CSS/JavaScript, rulează consumer gate-ul în reactor și verifică
   artifactul fresh prin adapterul static sau browser proof-ul existent;
4. pentru schimbări de package/ownership, rulează `WholeAtfGovernanceSuiteTest`;
5. dacă se face o afirmație vizuală, verifică în MCP/Chromium:
   disclosure-uri, statusuri, overflow, screenshot placement și console errors;
6. actualizează tabelul curent, nu adăuga un jurnal datat;
7. mută regula stabilă în README-ul ownerului și păstrează aici numai limita
   care poate redeschide decizia.

</details>

## Regula de evidence pentru feedback

- feedback-ul nu păstrează nume de artifacte generate, timestamp-uri sau URL-uri
  temporare;
- dovada normativă este o clasă/test/guard și, când este necesar, un criteriu
  de rerun configurabil;
- MCP/Chromium este verificare de randare, nu sursă de adevăr pentru contractul
  Java;
- un failure de infrastructură se notează separat de un failure de produs și se
  reclasifică numai după rerun;
- dacă același failure de mediu se reproduce, se deschide un item separat pentru
  stabilitatea runner-ului, nu se contaminează verdictul reporting.

## Referințe normative

- [`atf-reporting/README.md`](../../atf-reporting/README.md) — API, assets,
  publication și formatul sursei versus copia HTML;
- [`atf-reporting-tests/README.md`](../../atf-reporting-tests/README.md) — matrice,
  fixture-uri, coverage și buget E2E;
- [`docs/ATF_Reporting_Contract.md`](../ATF_Reporting_Contract.md) — contractul
  transversal al artifactului Extent/HTML;
- [`docs/ATF_Evidence_Capture_Contract.md`](../ATF_Evidence_Capture_Contract.md)
  — capture policy, source și target generic;
- [`docs/README_Atf_CodeBestPractices.md`](../README_Atf_CodeBestPractices.md)
  — reguli de cod, teste și boundary-uri;
- [`docs/feedback/README.md`](README.md) — registrul central și ownership-ul
  feedback-ului.
