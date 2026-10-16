# ATF Java — strategie pentru testele UI Extent reutilizabile

Status: `DONE / GATE-CLOSED` — spec/manifest/dynamic factory, suite YAML
declarativă, exact family selection, expected projection and inventory/ownership
are live; the fresh artifact gate is closed. Typed-result consolidation is
complete for the converged auditors and remains a separate follow-up for the
older DOM inspectors. The six section modules are live, while uniform check
execution and independent Extent/JUnit publication remain tracked separately in
`ATF_Ui_Extent_Section_Architecture_Feedback.md`.
2026-10-16  
ID: `UI-EXTENT-REUSE-01`  
Evaluare curentă a reutilizabilității UI Extent: **100/100** pentru scope-ul
selecției/configurării suitei și al auditului dinamic, nu pentru eliminarea
tuturor seam-urilor istorice din inspectorii DOM.

Reconciliere live cu sursa și artifactul UI validat: `UI-22`, `.15`, `.16`,
`.17`, `.18`, `.19` și `.20` sunt închise în cod. `.15` are acum fixture-uri
browser generate distinct pentru Categories `zero`, `one` și `many`, inclusiv
shell-ul stabil pentru zero categories. Inspectorii JSON/mock/contract/style și
durată expun acum adapterul comun `ValidationResult`; nu mai există un ToDo de
convergență pentru acest increment.
Pointerul vizual curent este
`artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_22-40-15-301.html`
(SHA-256 `CAE94267A034BEBCF23B8792FF9E4D5220C45FBF71D8F1C694E11794A3102CAC`),
iar perechea API este
`artifacts/reports/api-atf-extent-report_2026-10-16_22-40-08-404.html`
(SHA-256 `931EC5C5BEE51F4D805E283CCFB93A8924BAC9AA1ABDAC0723FB593925B4C7A4`).
Artifactele mai vechi din ledger sunt istorice.

Acesta este documentul de feedback pentru direcția plug-and-play a auditului UI
al rapoartelor Extent. Nu înlocuiește contractul vizual deja stabilit în
`ATF_Ui_Exploratory_Feedback.md`; definește cum trebuie compuse setup-ul API,
mock-ul, raportul și testele UI dinamice.

Inventarul verificabil al responsabilităților, input/output-urilor și deciziei
de modularizare este în [`ATF_Ui_Extent_Inventory.md`](ATF_Ui_Extent_Inventory.md).

## Reconcilierea obligatorie cu README-urile și bunele practici

Strategia nu poate închide un ToDo dacă intră în conflict cu un contract deja
stabilit în README-ul ownerului. La fiecare increment se citesc și se verifică
următoarele surse:

| Sursă | Regula pe care o impune strategiei UI | Aplicare în acest document |
|---|---|---|
| [`README.md`](../../README.md) | boundary API/UI, ownership pe module și lanțul `API → mock → report → UI` | API setup este precondiție read-only; UI nu intră în `atf.api.tests.api` |
| [`docs/README_Atf_BestPractices.md`](../README_Atf_BestPractices.md) | clasă cu motiv dominant, package cohesion, locatori dedicați, un tabel de assertions | refactorizarea se face pe responsabilități, nu după numărul de linii |
| [`atf-tests/README.md`](../../atf-tests/README.md) | `ReportUiLocators` este boundary-ul selectorilor și testul păstrează Gherkin-ul | niciun test nou nu construiește CSS sau `page.locator` direct |
| [`atf-ui/README.md`](../../atf-ui/README.md) | browser/configuration în `atf-ui`, raportul UI separat, input read-only | `UiConfiguration` este snapshot; `atf.ui.report` are precedence explicită |
| [`atf-mock/README.md`](../../atf-mock/README.md) | mock YAML separat, transport deterministic, fail-fast, fără HTTP real | expected-ul API/UI vine din route catalog și body-urile mock |
| [`atf-validation/README.md`](../../atf-validation/README.md) | validare renderer-neutral, un tabel agregat, reporterul nu decide verdictul | `ValidationResult`/findings nu depind de Extent sau HTML |
| [`atf-reporting/README.md`](../../atf-reporting/README.md) | arbore semantic, JSON bounded, evidence fără duplicare, geometry stabilă | `Screen validation` are cele trei zone directe și screenshot inline unic |
| [`atf-debug-ui/README.md`](../../atf-debug-ui/README.md) | server loopback read-only și verificare prin Playwright/TAB MCP | nu se folosește browser Windows real pentru gate-ul final |
| [`docs/ATF_BUILD_AND_EXECUTION_SAFETY.md`](../ATF_BUILD_AND_EXECUTION_SAFETY.md) | staging nou → build → artifact → MCP → sync selectiv | rulările curente nu se opresc; output-ul nu se copiază în surse |
| [`docs/feedback/README.md`](README.md) | closure gate: implementare + test focalizat + artifact fresh + MCP | `[x]` se pune numai după toate cele patru dovezi |

Două nuanțe sunt deliberate și nu se confundă:

1. Nu există și nu se introduce `ui.enabled: true` ca gate de suită. Câmpul
   `ui.screenValidation.enabled` din configurația browserului controlează numai
   capturarea evidence-ului vizual; nu activează sau dezactivează execuția unei
   suite și nu înlocuiește selecția prin spec/familii.
2. Pragul de 8 clase/package din bunele practici este un semnal de review, nu
   motiv pentru split artificial. `audit`, `flows` și `support` sunt hotspot-uri
   cunoscute în `atf-tests`; orice split trebuie să reducă responsabilități
   amestecate și să păstreze replacement coverage.

### Pașii reproductibili pentru orice increment UI

```text
1. citește codul live și README-ul ownerului;
2. enumeră ToDo-ul exact și scrie testul focalizat/contractul lipsă;
3. creează staging nou în `.tmp` și sincronizează numai sursele;
4. implementează schimbarea în ownerul semantic corect;
5. rulează unit/component/integration și guard-urile de boundary;
6. dacă se schimbă output-ul, publică raport fresh în `artifacts/reports[/ui]`;
7. deschide exact HTML-ul prin ATF Debug UI/browser MCP și verifică click-uri,
   disclosures, styles, screenshot link și geometrie;
8. sincronizează selectiv codul validat în live și actualizează README/feedback.
```

Un build verde fără artifact fresh nu închide o schimbare vizuală. Un substring
în HTML fără click real prin MCP nu dovedește screenshotul sau navigarea.

Notă de reconciliere cu codul live: `UiReportAuditSpec`,
`ApiScenarioManifest`, `UiReportAuditFixture`,
`configurable/UiReportAuditDynamicTest` și
`test/.../engine/UiReportAuditDynamicEngine` există și sunt verificate.
`ValidationResult` și `CrossTabReconciler` există acum și sunt folosite de
regula dinamică `CROSS_TAB`; auditorii JSON/mock/contract/style/durată și
modulele de secțiune expun aceeași validare renderer-neutral prin metode
`validation()`/rezultate de secțiune. `UiAuditSuiteConfiguration` și loaderul
YAML pentru `ui.audit.suites` există și sunt verificate; `ExpectedReportProjection`
este live ca expected projection comun. `UiReportAuditPlan` și un model generic
`Finding`
nu sunt introduse deoarece nu există încă un al doilea consumator care să le
justifice; `SectionAuditRegistry` este registry-ul live pentru modulele pe
secțiuni.

## Verdict executiv

Fundația actuală este bună și trebuie păstrată:

- `atf.api.tests.api` rămâne exclusiv API și nu primește browser, `Page`,
  `Locator`, Playwright sau verificări DOM;
- `ApiReportAuditSetup` poate selecta o colecție API din proprietate JVM,
  adnotare sau YAML; `ApiReportSetupFlow` este doar fațada de compatibilitate;
- `ReportAuditContext`, `UiAuditFlow`, `UiAuditLifecycle`, locators, flow-uri și
  validatori există deja ca infrastructură comună;
- `ApiReportContractValidation` verifică deja request/response JSON și contractul
  YAML/mock pentru request-urile randate;
- raportul UI păstrează acțiunile reale, observațiile, aserțiunile și captura
  Base64 în aceeași stare vizuală.

Incrementul executabil este acum plug-and-play la nivelul compoziției suitei:

- `UiReportAuditSpec` selectează familiile și regulile ca date;
- `ApiScenarioManifest` fixează expected-ul înainte de browser din catalog + mock;
- `UiReportAuditFixture` creează un singur setup API și un singur context UI per
  spec;
- `UiReportAuditDynamicEngine` publică noduri dinamice pentru setup, Tests,
  contract JSON/mock, Overview, Categories, Fails, durate, responsive, evidence
  și cross-tab;
- `UiReportAuditDynamicTest` este intrarea generică pentru o colecție, cu
  selecție manuală prin proprietățile documentate în README-ul pachetului.

Implementarea este plug-and-play pentru selecția familiilor și auditul dinamic;
limitele de mai jos sunt follow-up-uri de consolidare, nu motive pentru a crea
clase UI noi pentru fiecare combinație:

1. `UsersPostsApiReportUiTest` și
   `RequestVariantsAndApiHeadersReportUiTest` sunt cele două profile manuale
   aprobate; reutilizează același harness și nu se extind prin clase noi.
2. `ApiReportUiTestProfile` păstrează doar cele două profile manuale aprobate;
   toate cele șapte familii (`users`, `posts`, `requests`, `headers`, `flows`,
   `negative`, `lifecycle`) sunt totuși selectabile prin resolverul generic și
   `ApiTestRef`, fără clase UI noi.
3. `ApiExtentReportExploratoryTest` este acum wiring golden subțire; setup-ul,
   navigarea, duratele, filtrele, evidence-ul și regulile comune sunt în
   engine/inspectors.
4. `ApiReportExplorationFlow` este facade semantică de tranziție, iar
   snapshot-urile neutre sunt în `ReportAuditSnapshot`; nu mai este owner-ul
   rezultatelor inspectorilor.
5. `ApiJsonEvidenceAuditor`, `ApiMockEvidenceAuditor` și
   `MockReportContractAuditor` au responsabilități apropiate, dar acum expun
   aceeași suprafață `ValidationResult`; primul auditorizează JSON în browser,
   al doilea caută documente JSON în HTML, iar ultimul validează request-ul
   față de route. Findings sunt comune, iar contoarele rămân locale domeniului.
6. `UiReportAuditFixture` reutilizează un singur setup API și un singur context
   UI per spec; duplicarea de raport nu mai este o limită a factory-ului.

Prin urmare, `ARCH-UI-01`, `ARCH-04`, `ARCH-05` și
`UI-EXTENT-REUSE-01` sunt închise pentru scope-ul actual: selecția, manifestul,
factory-ul, convergența rezultatelor și auditul dinamic sunt end-to-end.

Scorul de 100/100 este pentru scope-ul reutilizabil verificat. El reflectă
compoziția reutilizabilă, manifestul, loaderul YAML, boundary-ul de locatori,
proiecția expected, matricea browser, evidence-ul și convergența auditorilor.
Eventuala extracție a unui model generic parametrizat sau a unui submodul
renderer-neutral rămâne o decizie viitoare, declanșată de al doilea consumator,
nu un defect al incrementului curent.

## Regula de configurare

Nu se introduce și nu se reintroduce `ui.enabled: true`. În live, execuția este
controlată prin `UiReportAuditDynamicTest`, adnotare, suitele declarative YAML
sau proprietăți JVM; registry-ul `SectionAuditRegistry` este deja live pentru
modulele pe secțiuni, iar engine-ul global rămâne compatibility path până la
închiderea replacement coverage.
Controlul execuției se face prin selecție, nu printr-o variabilă booleană fără
semnificație.

Forma live pentru YAML este declarativă și configurabilă:

```yaml
ui:
  audit:
    suites:
      users-posts:
        source: generated-api
        scenarioFamilies: [users, posts]
        depth: deep
        tabs: [tests, overview, categories, fails, evidence]
        validators:
          - setup
          - tests-structure
          - request-json
          - mock-reconciliation
          - cross-tab
          - screenshot-evidence
          - responsive
```

Proprietățile JVM existente rămân compatibile:

```text
-Datf.ui.api.families=users,posts
-Datf.ui.audit.suite=users-posts
```

Precedența implementată în live este:

```text
atf.ui.api.families → atf.ui.audit.suite/listă JVM → @ApiScenarioSetup →
ui.apiSetup.scenarioFamilies
```

Live: `depth` și `rules` sunt JVM properties cu prioritate peste YAML, iar
`UiReportAuditRule` este enum. `ui.audit.suites` este citită din YAML prin
`UiAuditSuiteConfiguration` și `UiConfigurationYamlReader`; familiile, tabs,
validators, depth și source intră în resolverul suitei. Un profil `deep` selectează regulile complete;
`smoke` selectează setup, Tests, request contract și Overview. Nu se copiază
metode pentru fiecare profil.

## Lanțul de adevăr și de validare

Raportul UI nu ghicește ce ar trebui să fie corect numai din HTML. Așteptările
sunt construite înainte de browser și apoi comparate cu observatul:

```text
UiReportAuditSpec
       ↓
ApiScenarioSelection
       ↓
ApiReportAuditSetup
       ├── API scenario catalog
       ├── AtfMockTransport / atf-mock.yaml
       └── API Extent input report (read-only pentru UI)
       ↓
ApiScenarioManifest (live) + ExpectedReportProjection (live)
       ↓
UiReportAuditSpec / SectionAuditSpec + SectionAuditRegistry
       ├── Tests tab
       ├── Overview tab
       ├── Categories tab
       ├── Fails tab
       ├── request/response/execution evidence
       └── cross-tab reconciliation
       ↓
Playwright locators → actions → flows → snapshot extractors → validators
       ↓
ValidationResult / AtfAssertion → JUnit + Extent UI report;
`ValidationResult` este live pentru cross-tab, modulele pe secțiuni și auditorii
de JSON/mock/contract/style/durată; inspectorii DOM istorici păstrează încă
observații locale până la migrarea lor incrementală.
```

Manifestul trebuie să conțină cel puțin:

- `suiteId` și sursa (`GENERATED_API` sau `EXISTING`);
- familiile selectate, în ordinea declarată;
- owner-ul, scenariile și fazele așteptate;
- identitatea fiecărui request: method, path, query canonicalizat și owner;
- route key-ul mock-ului, statusul, headerele, auth profile și body mode;
- regulile JSON pentru request și response;
- tab-urile și validatorii selectați;
- politica de evidence: acțiuni reale, screenshot per stare și markerul curent.

Acest manifest este diferența dintre „am găsit niște carduri în DOM” și
„raportul UI dovedește că setup-ul API selectat a fost raportat complet”.

## Modelul Java țintă pentru consolidarea rezultatului

Modelul rezultat trebuie să fie compozițional, imuabil și fără dependență de
Playwright. `support.ValidationResult` live este contractul minimal actual
(`valid`, `assertions`, `violations`) și este folosit de CrossTab, modulele de
secțiune și auditorii convergenți. Un model mai bogat se introduce numai dacă
apare un al doilea consumator renderer-neutral.

Schița de mai jos este un model conceptual pentru un eventual al doilea
consumator; nu este semnătura codului live și nu trebuie implementată doar
pentru a schimba numele claselor:

```java
public record UiReportAuditSpec(
        String id,
        ReportSource source,
        List<ApiFamily> apiFamilies,
        Set<ReportTab> tabs,
        Set<ValidationRule> validators,
        ValidationDepth depth,
        EvidencePolicy evidencePolicy) {}

public record ApiScenarioManifest(
        String suiteId,
        List<ApiFamily> families,
        List<ApiOwnerManifest> owners,
        List<ApiRouteExpectation> routes,
        Set<ReportTab> tabs,
        Set<ValidationRule> validators) {}

public record ValidationResult(
        String ruleId,
        ReportTab tab,
        ValidationStatus status,
        List<AtfAssertion> assertions,
        List<String> violations,
        ReportObservation observation) {
    public boolean passed() { return violations.isEmpty(); }
}
```

În implementarea curentă, `ApiReportAuditSetup.Result` conține deja
`(report, mock, families, manifest)`. Constructorul de compatibilitate cu trei
componente și `ApiReportUiFixture` rămân delegate pentru migrare; nu reprezintă
un al doilea flux de setup.

Reguli importante:

- validatorul pur returnează `ValidationResult` cu `AtfAssertion`/violation;
  adaptoarele JUnit și Extent traduc rezultatul, fără să mute selectorii sau
  expected-ul în test;
- test factory-ul transformă rezultatul în verdict JUnit și publică rezumatul
  în Extent;
- `AtfAssertion` rămâne rezumatul semantic, nu înlocuiește acțiunile reale
  Playwright din audit trail;
- numele owner-ilor și route key-urile provin din manifest, nu din `assertTrue`
  hard-codat în teste;
- `ReportUiContract.Tab`, `NodeRole`, `Phase`, `ValidationRule` și
  `EvidenceChannel` țin vocabularul în constante/enums.

## Engine-ul dinamic și testele JUnit

Testul public live este deja foarte mic. Pentru suitele generice, engine-ul
live este `UiReportAuditDynamicEngine`; pentru secțiuni, registry-ul și engine-ul
live sunt `SectionAuditRegistry` și `SectionAuditDynamicEngine`. Nu se introduce
un al treilea registry doar pentru redenumire. Wiring-ul live este:

```text
UiReportAuditDynamicTest
  → UiReportAuditFixture
  → UiReportAuditDynamicEngine.container(fixture)

Section test cu @UiExtentSectionAudit
  → SectionAuditFixture
  → SectionAuditDynamicEngine.container(fixture)
  → SectionAuditRegistry.module(section)
```

Fiecare container de suită produce dinamic nodurile stabile:

```text
users-posts
├── SETUP / manifest and input artifact
├── TESTS / owners, scenarios, phases, requests, validations
├── TESTS / request JSON and mock reconciliation
├── OVERVIEW / counts and metadata
├── CATEGORIES / discovered tags and links
├── FAILS / empty-state or failure navigation
├── EVIDENCE / request, response, execution and screenshot policy
├── RESPONSIVE / discovered badge geometry
└── CROSS-TAB / Tests ↔ Overview ↔ Categories ↔ Fails
```

Numele nodurilor sunt generate din `spec.id`, `ReportTab` și `ValidationRule`.
Nu se creează clase noi pentru fiecare combinație de familii.

Setup-ul API se execută o singură dată pentru un `UiReportAuditSpec`, iar toate
nodurile consumă același artifact API read-only. Raportul UI are un singur
`ReportAuditContext` pe suită și un lifecycle clar; nu se pornește câte un
server/browser pentru fiecare assertion.

Pentru `EXISTING` se folosește aceeași matrice de validatori, dar fără API setup:
`ReportAuditInput` este sursa, iar regulile dependente de manifest sunt marcate
`NOT_APPLICABLE` sau primesc un manifest furnizat explicit. Nu se amestecă
semantics de „generated API” cu „existing HTML”.

## Validatori plug-and-play pe tab-uri

### Tests

`TestsTabValidator` trebuie să descopere și să verifice dinamic:

- exact owner-ii din manifest, fără owner hard-codat;
- scenarii, faze, request-uri și carduri de validation;
- fiecare request mapping către un singur route identity;
- `REQUEST JSON` și `RESPONSE JSON`, cu excepții numai pentru modelele negative
  declarate de mock;
- `REQUEST HEADERS`, `RESPONSE HEADERS`, status, URL, auth și query;
- legătura request → response → execution evidence;
- `POST` și `PUT`: body JSON valid, nevid și conform cu mock-ul, atât în request,
  cât și în response;
- status, duration și verdict pentru fiecare nod.

### Overview

`OverviewTabValidator` primește snapshot-ul Overview, snapshot-ul Tests și o
expectation construită înainte de DOM: pentru generated, familiile/mock-ul și
`ExpectedReportProjection`; pentru static, proiecția explicită a fixture-ului.
Nu mai derivează expected totals din Tests. Compară actualul observat cu
owners, scenarii, request-uri și audit totals așteptate, fără self-oracle.

### Categories

`CategoriesTabValidator` verifică item-urile descoperite, filtrele de status/tag,
starea zero/one/many și linkul către testul sursă. Fixture-urile pentru cele trei
cardinalități trebuie să fie date-driven, nu trei metode copiate.

### Fails

`FailsTabValidator` trebuie să suporte atât empty state, cât și raport cu
failure real. Pentru fiecare failure verifică sursa, cauza primară și linkul
înapoi în Tests; nu presupune că orice suită trebuie să conțină failure.

### Evidence

`EvidenceValidator` verifică disclosures distincte pentru request, response și
execution, câmpurile și body-urile JSON, plus action sequence și screenshotul
relevant. Contractul vizual rămâne cel deja stabilit:

- un singur link Base64 pentru starea vizuală;
- markerul emoji curent pe aceeași linie, centrat vertical;
- fără `<img>` duplicat sau payload repetat;
- `Screen validation`, `Playwright action sequence`, `Audit observations` și
  `Assertions` sunt zone surori, fără linii orizontale decorative între ele.

### Cross-tab

`CrossTabReconciler` este validator separat, nu un `assertTrue` ascuns în
Overview. El compară seturi și relații:

```text
Tests owners/scenarios/requests
    ↔ Overview totals/metadata
    ↔ Categories linked tests
    ↔ Fails linked tests
    ↔ manifest route identities
```

Un raport nu este conform dacă fiecare tab pare corect izolat, dar seturile nu
se reconciliază între ele.

## Regruparea responsabilităților

### Structura țintă în `atf-tests`

```text
atf.ui.tests.extent
├── plan
│   ├── UiReportAuditSpec (live)
│   ├── ApiScenarioManifest (live)
│   └── ExpectedReportProjection (live)
├── section/plan + section/module
│   ├── SectionAuditSpec (live)
│   ├── SectionAuditRegistry (live)
│   └── module per Overview/Tests/Categories/Fails/Evidence/Screen validation
├── engine
│   ├── UiReportAuditDynamicEngine (live în src/test)
│   ├── SectionAuditDynamicEngine (live în src/test)
│   └── CrossTabReconciler (live; folosește ValidationResult comun)
├── setup
│   └── ApiReportAuditSetup
├── flows
│   ├── TestsTabFlow
│   ├── OverviewTabFlow
│   ├── CategoriesTabFlow
│   ├── FailsTabFlow
│   └── EvidenceFlow
├── locators
│   └── ReportUiLocators + ownerii specializați
├── validators
│   ├── TestsTabValidator
│   ├── OverviewTabValidator
│   ├── CategoriesTabValidator
│   ├── FailsTabValidator
│   ├── EvidenceValidator
│   ├── ResponsiveValidator
│   └── ApiMockContractValidator
└── support
    ├── ReportAuditContext
    ├── UiAuditFlow
    ├── UiAuditLifecycle
    └── ReportUiContract
```

Configurația `UiAuditSuiteConfiguration` este în `atf-ui`, nu în planul
`atf-tests`: `atf-ui` deține parsing/configuration, iar `atf-tests` consumă o
configurație normalizată. Această separare împiedică browser-testele să devină
proprietarul YAML-ului.

Diferența critică este aceasta:

```text
locators = unde este elementul
actions  = cum este executată operația
flows    = în ce ordine se parcurge UI-ul
validators = ce s-a observat și dacă respectă contractul
engine   = ce validatori se rulează pentru spec
test     = ce spec se cere
```

Un validator nu navighează ascuns. Un flow nu decide verdictul de business. Un
test nu construiește CSS și nu știe detalii DOM. Un engine nu are selector.

### Ce se păstrează, ce se unește, ce se retrage

| Componentă live | Decizie | Motiv |
|---|---|---|
| `ApiReportAuditSetup` | păstrat ca composition root | API precondition; returnează report, mock, familii și manifest; este folosit direct de fixture-uri |
| `ApiReportSetupFlow` | păstrat ca facade de compatibilitate | nu este un al doilea composition root |
| `ApiReportUiFixture` | păstrat temporar ca facade | deleagă deja către `UiReportAuditFixture`; ulterior poate deveni `UiReportAuditSession` |
| `ReportAuditContext`, `UiAuditFlow`, `UiAuditLifecycle` | păstrate | lifecycle/evidence comune pentru generated și existing |
| `ReportUiLocators` și sub-locatorii | păstrate | boundary obligatoriu pentru selectorii UI |
| `ApiReportUiFlow` | păstrat temporar, apoi împărțit semantic | are navigare și disclosure actions; nu trebuie să dețină validarea |
| `ApiReportExplorationFlow` | facade de compatibilitate | snapshot-urile sunt deja în `ReportAuditSnapshot`; inspectorii delegă către auditori specializați |
| `flows/inspection/*` | redenumit gradual în `inspectors` sau `validators` | numele trebuie să distingă citirea DOM de verdictul de contract |
| `ApiReportContractValidation` | facade de compoziție | compune regulile actuale; rezultatele converg prin `ValidationResult` acolo unde auditorul are contract typed |
| `CrossTabReconciler`, `ValidationResult` | păstrate ca increment live | reconcilierea dinamică și auditorii de JSON/mock/contract/style/durată expun rezultate renderer-neutral |
| `ApiJsonEvidenceAuditor` | păstrat ca regulă JSON browser-side | returnează `ValidationResult` prin adapterul domeniului propriu |
| `ApiMockEvidenceAuditor` | păstrat ca evidence scanner | scanarea globală rămâne distinctă de mapping-ul request → route, validat separat |
| `MockReportContractAuditor` | păstrat ca validator per request | consumă manifestul și expune același contract `ValidationResult` |
| `UsersPostsApiReportUiTest` | păstrat intenționat | profil manual aprobat; folosește harness-ul comun |
| `RequestVariantsAndApiHeadersReportUiTest` | păstrat intenționat | profil manual aprobat; folosește harness-ul comun |
| `AbstractApiReportUiContractTest` | păstrat ca suport compatibil | este dependency directă a celor două profile aprobate |
| `AllApiReportUiContractTest` + companion | retras | redundant față de factory-ul dinamic și hard-coda owner-i |
| `ApiExtentReportExploratoryTest` | păstrat ca golden E2E în formă subțire | rămâne o probă end-to-end, nu containerul tuturor regulilor |
| `ExistingExtentReportExploratoryTest` | păstrat ca source adapter | auditul unui HTML existent este un caz distinct, nu setup API |
| `ExistingExtentCategoriesExploratoryTest` + `ReportNavigationAggregationFlowTest` | merge posibil după snapshot contract | ambele acoperă navigare/agregare; se elimină doar după acoperire echivalentă |
| architecture/lifecycle/locator tests | păstrate | sunt gardienii boundary-urilor și ai migrației |

### Mapping țintă → implementare live

| Numele din strategia conceptuală | Implementarea live | Observație |
|---|---|---|
| `TestsTabValidator` | `ApiReportStructureInspector` + `ApiReportContractValidation` | separarea verdictului de observație este încă în migrare |
| `OverviewTabValidator` | `OverviewSectionValidator` | contract pur; expected-ul este independent de snapshot-ul Tests |
| `CategoriesTabValidator` | `CategoryScreenValidator` + `CategoryFilterAuditor` + `CategoryCardinalityBrowserFixtureTest` | zero/one/many este închis pur și prin fixture browser generat; publisher-ul injectează shell-ul zero |
| `FailsTabValidator` | `FailsScreenValidator` | suportă empty și failure state observat |
| `EvidenceValidator` | `EvidenceScreenValidator` + `ApiJsonEvidenceAuditor` | JSON și screenshot sunt încă două canale de rezultat |
| `ApiMockContractValidator` | `MockReportContractAuditor` | mapping intenționat; fațada existentă este validatorul public per request, iar colaboratorii tipizați îi păstrează responsabilitățile coezive |
| `UiReportAuditEngine` | `UiReportAuditDynamicEngine` | engine-ul este în `src/test` pentru a păstra runtime-ul browserului în teste |
| `UiReportAuditRegistry` | nu se introduce | configurația YAML + `UiReportAuditSpec.resolve` acoperă suitele generice; `SectionAuditRegistry` acoperă modulele pe secțiuni |

Retragerea a fost făcută numai pentru `AllApiReportUiContractTest` și
companion-ul său, după replacement coverage verde. Cele două profile cerute
explicit nu se ating; orice retragere viitoare cere aceeași evidență și
verificare prin ATF Debug UI.

## Extracția în submodule

Nu mutăm Playwright în `atf-validation` și nu mutăm testele UI în `atf-ui`.
Direcția sigură este în două etape:

1. **Mai întâi package split în `atf-tests`**: `plan`, `engine`, `validators`,
   `flows`, `locators`, `support`. Aici se stabilizează modelul și se elimină
   duplicarea fără a introduce imediat o dependență Maven nouă.
2. **Apoi extracție opțională într-un `atf-report-contracts`** dacă manifestul,
   `ExpectedReportProjection` și `ValidationResult` au cel puțin doi consumatori
   reali (UI Extent și alt publisher/adapter); `ValidationResult` singur nu
   justifică un submodule.

`atf-report-contracts` ar fi renderer-neutral: fără Playwright, JUnit, HTML sau
Debug UI. `atf-tests` ar rămâne adapterul browserului; `atf-reporting` ar rămâne
owner-ul HTML/CSS; `atf-ui` ar rămâne owner-ul browser/session; `atf.api.tests.api`
ar rămâne owner-ul testelor API.

Nu se introduce un submodule doar pentru a muta clase mari. Criteriul este
reutilizarea unui model pur și verificabil, nu numărul de fișiere.

## ToDo executabil

### Faza 0 — caracterizare, fără ștergeri

- [x] `UI-EXTENT-REUSE-01.1` Inventariază fiecare test UI, flow, locator,
  inspector și validator cu responsabilitate, input și output.
- [x] `UI-EXTENT-REUSE-01.2` Listează pentru fiecare clasă ce este API setup,
  browser action, DOM observation, semantic validation sau report publication.
- [x] `UI-EXTENT-REUSE-01.3` Adaugă un snapshot al familiilor, owner-ilor,
  route identity-urilor și tab-urilor pentru o suită `users-posts`.

### Faza 1 — model și manifest

- [x] `UI-EXTENT-REUSE-01.4` Introdu `UiReportAuditSpec` și `UiReportAuditRule`
  ca modele imutabile de selecție.
- [x] `UI-EXTENT-REUSE-01.5` Extinde setup result cu `ApiScenarioManifest`;
  manifestul este derivat din catalog și mock, nu din DOM.
- [x] `UI-EXTENT-REUSE-01.6` Factory-ul nou derivează owner-ii și route
  identity din manifest; agregatorul hard-codat a fost retras.
- [x] `UI-EXTENT-REUSE-01.7` Familiile `flows`, `negative` și `lifecycle`
  pot fi selectate fără a copia clase de test.

### Faza 2 — engine și test factory

- [x] `UI-EXTENT-REUSE-01.8` Resolver determinist pentru familii: JVM property
  → suite alias/listă JVM → annotation → YAML
  `ui.apiSetup.scenarioFamilies`; reguli/depth sunt configurabile prin JVM
  properties.
- [x] `UI-EXTENT-REUSE-01.8a` Adaugă loader YAML pentru suite complete
  (`ui.audit.suites` cu familii, tabs, validators, depth și source). Resolverul
  live consumă `UiAuditSuiteConfiguration`; JVM rules/depth au prioritate, iar
  YAML furnizează configurația declarativă reutilizabilă. Testele dedicate
  acoperă loaderul și selecția suitei `users-posts`.
- [x] `UI-EXTENT-REUSE-01.9` `@TestFactory` cu container per spec și teste
  dinamice per regulă.
- [x] `UI-EXTENT-REUSE-01.10` Setup API o singură dată per spec, artifact
  reutilizat read-only de toate nodurile.
- [x] `UI-EXTENT-REUSE-01.11` Extent publică `GIVEN setup → THEN expected/actual`
  cu screenshot și assertions pentru fiecare regulă.

### Faza 3 — validare profundă

- [x] `UI-EXTENT-REUSE-01.12` Regula cross-tab folosește acum
  `CrossTabReconciler`, cu seturi dinamice și relații între proiecții, nu
  count-uri fixe ascunse în motorul de audit.
- [x] `UI-EXTENT-REUSE-01.13` Leagă fiecare request randat de un singur route
  din mock, inclusiv query canonicalizat și body mode.
- [x] `UI-EXTENT-REUSE-01.14` Rezultatele de reconciliere publică acum un
  `ValidationResult` immutable comun, cu assertions și violations, fără să
  rupă facades existente.
- [x] `UI-EXTENT-REUSE-01.15` Contractul pur zero/one/many și matricea browser
  data-driven pentru failure/empty, GET/POST/PUT și viewport normal/narrow sunt
  verzi; `CategoryCardinalityBrowserFixtureTest` publică și auditează fixture-uri
  generate distinct pentru Categories `zero`, `one` și `many`.

### Faza 4 — consolidare și retragere controlată

- [x] `UI-EXTENT-REUSE-01.16` Reduce `ApiExtentReportExploratoryTest` la golden
  E2E + wiring; mută regulile în engine/validators.
- [x] `UI-EXTENT-REUSE-01.17` Retrage agregatorul `AllApiReportUiContractTest`
  și companion-ul redundant; cele două profile configurabile rămân ca intrări
  manuale aprobate.
- [x] `UI-EXTENT-REUSE-01.18` Unește flow-urile de navigare doar dacă snapshot-ul
  Tests/Overview/Categories/Fails rămâne identic.
- [x] `UI-EXTENT-REUSE-01.19` Decide extracția `atf-report-contracts` pe baza
  consumatorilor reali: extracția este amânată justificat; package split-ul
  rămâne în `atf-tests` până există doi consumatori renderer-neutral.
- [x] `UI-EXTENT-REUSE-01.20` Actualizează README-urile și feedback-ul pe baza
  codului live, a build-ului `.temp` și a artifactului inspectat.

### Dovadă incrementală — suite YAML declarative, 2026-10-16

- [x] Build izolat în `.temp/ui-strategy-20261016-173500`; rulările curente nu
  au fost oprite și sursele validate au fost sincronizate în proiectul live.
- [x] `UiAuditSuiteConfiguration`, loaderul `ui.audit.suites` și resolverul
  dinamic sunt verificate prin testele de configurație, plan și factory.
- [x] Suita `users-posts` cu `depth: deep` a trecut: setup, Tests,
  request/mock JSON, Overview, Categories, Fails, duration, responsive,
  evidence și cross-tab.
- [x] Artifact API: `artifacts/reports/api-atf-extent-report_2026-10-16_17-36-18-409.html`;
  9 perechi request/response JSON detectate static.
- [x] Artifact UI: `artifacts/reports/ui/ui-atf-extent-report_users-posts_2026-10-16_17-36-22-338.html`;
  11 Screen validation și 9 linkuri inline `📷 base64 img` detectate static.
- [x] Exact HTML-ul UI a fost deschis prin ATF Debug UI/browser MCP și verificat
  vizual: cele trei rânduri directe sunt consecutive, fără linii sau spații
  goale decorative. Raportul API a fost deschis în aceeași suprafață și
  verificat pe structura Tests/owner/scenario și intrarea POST.
- [x] Inventory increment: 43 clase `main`, 28 clase `test`, roster și
  dependency direction reconciliate cu filesystem-ul; guard-urile
  `UiSelectorBoundaryArchitectureTest`, `UiTestLayerArchitectureTest`,
  `ApiReportUiArchitectureTest`, `ReportUiLocatorsTest` și `UiReportAuditSpecTest`
  au trecut în același staging.

## Ledger de închidere

| Increment | Implementare | Test focalizat | Artifact/inspecție | Status |
|---|---|---|---|---|
| `.01.4–.01.7` | spec, manifest, familii dinamice | `ApiScenarioManifestTest` | dynamic API/UI report | închis |
| `.01.8/.8a–.01.11` | resolver și suite YAML | `AtfUiConfigurationLoaderTest`, `UiReportAuditSpecTest` | raport `users-posts` deep | închis |
| `.01.12–.01.14` | cross-tab + `ValidationResult` | `CrossTabReconcilerTest` | Tests/Overview/Categories/Fails | închis |
| `.01.1–.01.3/.19` | inventory + modularizare decision | architecture guards | document + source reconciliation | închis |
| `.01.15` | browser cardinality matrix | contract pur + fixture-uri generate zero/one/many; zero shell publicat când vendorul omite view-ul | raport cu fixture `zero/one/many`, raport final și verificare MCP | închis |
| `.01.16/.18` | exploratory reduction + flow merge | `ApiExtentReportExploratoryTest`, `ReportAuditSnapshot`, `CrossTabReconciler` | golden artifact + source guards | închis |

Un item deschis nu se poate marca închis doar pentru că modelul conceptual
există. Are nevoie de implementare, test focalizat, build izolat, artifact în
directorul configurat și verificare vizuală prin ATF Debug UI/browser MCP.

## Reguli de implementare care nu se negociază

- selectorii rămân numai în clase dedicate de locatori;
- testele UI nu folosesc `page.locator`, `getByRole` sau `getByText` direct;
- flow-ul execută acțiunea; validatorul observă și returnează verdictul;
- testul declară spec-ul, nu repetă setup, CSS, owner sau route string;
- API setup-ul poate fi precondiție UI, dar nu transformă `atf.api.tests.api` în
  pachet de browser;
- mock-ul este sursă de expected contract, nu doar server de răspuns;
- POST/PUT au body JSON în request și response; excepțiile negative sunt
  explicite în manifest și raportate ca atare;
- fiecare request are identity stabilă: method + path + query canonicalizat;
- numerele observate se reconciliază cu seturi și manifest, nu cu valori fixe;
- fiecare schimbare de renderer/evidence păstrează captura Base64, emoji-ul,
  geometria și lipsa delimitărilor decorative dintre cele trei zone;
- un artifact nu este declarat valid până când exact HTML-ul rezultat nu este
  deschis prin ATF Debug UI/browser MCP și inspectat vizual.

## Criteriul de închidere

`UI-EXTENT-REUSE-01` devine `DONE` numai când toate sunt adevărate:

1. o singură clasă/factory poate rula orice colecție configurată de familii API;
2. adăugarea unei familii noi cere manifest/catalog și date, nu o clasă UI nouă;
3. validatorii dinamici acoperă Tests, Overview, Categories, Fails, evidence și
   cross-tab;
4. expected-ul este derivat din setup + catalog + mock, iar actualul din DOM;
5. fiecare finding este auditabil în raportul UI și are expected/actual;
6. nu există selector direct în testele UI sau în flow-urile semantic publice;
7. clasele retrase au replacement coverage demonstrat;
8. build-ul și rulările noi se fac în `.tmp`, fără oprirea rulărilor curente;
9. codul validat este sincronizat selectiv în proiectul live;
10. raportul final este copiat în `artifacts/reports/ui`, deschis prin ATF Debug
    UI/browser MCP și verificat ca randare înainte de a declara task-ul închis.

