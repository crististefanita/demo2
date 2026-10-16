# ATF UI Extent — section architecture feedback

Ultima analiză a codului live: **2026-10-16**.
Acest document propune decuplarea testelor Extent pe secțiuni autonome de
raport. Ținta este ca aceeași secțiune (`Tests`, `Overview`, `Categories`,
`Fails`, `Screen validation` sau un grup de evidence) să poată fi verificată:

1. ca test pur de contract, folosind un snapshot static;
2. ca audit UI read-only, folosind un raport HTML static declarat prin
   adnotare;
3. ca audit integrat, pornind din `@ApiScenarioSetup`, mock/YAML și raportul
   API generat înainte de browser.

Selecția secțiunii trebuie să fie programatică și tipizată. YAML-ul rămâne
sursa pentru mock și răspunsuri API, nu mecanismul principal de alegere a
testului UI.

## Implementare sincronizată

Fundația pe secțiuni și registry-ul celor șase module sunt implementate și
verificate în staging fără oprirea rulărilor curente:

- `UiExtentSectionAudit` declară programatic sursa (`STATIC_PATH`,
  `STATIC_RESOURCE`, `GENERATED_API`), secțiunea și check-urile; nu există
  boolean `ui.enabled` și nu se introduce selecție YAML pentru secțiune.
- `SectionAuditSpec` validează strict contractul sursei: path-ul folosește
  exclusiv cheia JVM declarată, resource-ul exclusiv classpath, iar generated
  nu acceptă fallback static ambiguu.
- `ReportSourceResolver` rezolvă raportul static read-only; `SectionAuditFixture`
  compune același `ReportAuditContext`, `UiAuditFlow`, flow și inspectors pentru
  static și generated.
- `ExtentSectionAuditModule`, `SectionAuditRegistry` și
  `SectionAuditDynamicEngine` introduc registry-ul celor șase module fără să
  rupă `UiReportAuditDynamicEngine` legacy.
- `TestsSectionAuditModule` + `FailsSectionAuditModule` citesc
  DOM-ul numai prin flow și locatori, apoi aplică validatorii puri peste
  `ReportAuditSnapshot.Tests` și `ReportAuditSnapshot.Failures`; clasa de test
  nu conține `Page`, `Locator` sau selector.
- `OverviewSectionAuditModule`, `CategoriesSectionAuditModule`,
  `EvidenceSectionAuditModule` și `ScreenValidationSectionAuditModule` sunt
  de asemenea în registry; toate șase folosesc snapshot-uri și validatori puri,
  dar modulele păstrează încă orchestrarea browserului și publicarea Extent în
  aceeași metodă `audit(...)`.
- `GeneratedTestsSectionReportUiTest` și `StaticTestsSectionReportUiTest`
  rulează aceeași definiție `Tests + Fails` pe surse generated/static; testele
  pure protejează snapshot-urile independente.

Artifactele staging au fost deschise prin ATF Debug/browser MCP. Pentru ambele
rapoarte, DOM-ul a confirmat `Tests visible`, `openStructuralCards=0`,
`openDetails=0`, iar consola browserului a avut 0 erori. Randarea a fost
verificată vizual la viewport-ul MCP 1336×651; cardul secțiunii rămâne compact,
aliniat și colapsat implicit.

Artifactele finale au fost sincronizate în proiectul live și reverificate în
ATF Debug/browser MCP:

- API: `artifacts/reports/api-atf-extent-report_section-tests-fails-generated_2026-10-16_21-17-24-629.html` — 2 failure rows, detail pane, primary/secondary cause și `Open in Tests`.
- UI generated: `artifacts/reports/ui/ui-atf-extent-report_section-tests-fails-generated_2026-10-16_21-17-28-180.html` — modulele `Tests` și `Fails` trec, ambele carduri colapsate.
- UI static: `artifacts/reports/ui/ui-atf-extent-report_section-tests-fails-static_2026-10-16_21-18-05-060.html` — aceeași compoziție și aceeași stare colapsată, pe input static.

În verificarea DOM, cele două carduri UI au `card-body` cu dimensiune 0×0
implicit, iar API `Fails` are exact două rânduri și un panou de detaliu vizibil;
consola browserului a raportat 0 erori.

Recalibrarea a identificat și a reparat inconsistențe latente din codul live:
`FailureFlowInspector` folosea query-uri lipsă din `ReportUiLocators`, iar
facade-ul de setup trebuia aliniat cu composition root-ul verificat. Locatorii
sunt acum în boundary-ul canonic, setup-ul public este
`ApiReportAuditSetup`, iar flow-ul vechi rămâne doar compatibility facade;
`FAILURE_FLOW` este în enum și engine, astfel build-ul curat nu mai depinde de
clase stale din `target`.

## Verificare profundă sincronizată — 2026-10-16 21:20

Am refăcut gate-ul pe un staging separat, fără oprirea niciunei rulări active:

- am păstrat un singur boundary arhitectural, `atf.ui.tests.extent.section`; nu
  există un al doilea package parallel pentru modulele de secțiune;
- `ResolvedReportSource` păstrează `ReportAuditInput` + `ReportArtifactIdentity`
  (SHA-256) și declară explicit input-ul read-only;
- `ReportSourceResolver` validează înainte de Debug UI existența fișierului,
  `doctype`, non-empty, source policy și fallback-ul controlat pentru
  `atf.ui.report` din `UiConfiguration`; un HTML malformed este respins;
- `SectionAuditSpec.resolve` respinge `GENERATED_API` fără
  `@ApiScenarioSetup`; nu există fallback către un raport static;
- testele pure au trecut `TestsSectionValidatorTest` 2/2, iar contractul de
  source/hash/strict annotation a trecut `SectionSourceResolverTest` 3/3;
- auditul browser generat `GeneratedTestsSectionReportUiTest` a trecut 2/2:
  `Tests` și `Fails`, cu evidence Extent și screenshot per stare vizuală.

Artifactele rezultate au fost produse în
`C:\work\ex\java\.tmp\section-architecture-top4-20261016-2115`, apoi copiate
selectiv live:

- API: `artifacts/reports/api-atf-extent-report_2026-10-16_21-20-14-680.html`,
  SHA-256 `66209FFC3367BE83F1F32FF2E20A7C0F356D6A1DEDAB05DFA4E79BE17901EA07`;
- UI: `artifacts/reports/ui/ui-atf-extent-report_generated-tests-section-report-ui-test_2026-10-16_21-20-18-722.html`,
  SHA-256 `F6AAFA55983E70A0FB94CCD01E8A3023E8BF4DE7639AEB8E5DECA297C1436289`.

Gate-ul final curent este închis pe staging-ul nou și pe exact artefactele
copiate live:

- golden API: `artifacts/reports/api-atf-extent-report_2026-10-16_21-34-29-685.html`,
  SHA-256 `1D0232A7CA60B87FB5546B0CF001FC715F33C8880A397AED11DA16CCD58A7EBD`;
- golden UI: `artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_21-34-33-931.html`,
  SHA-256 `678989FAB3D9B18CBE351239C9C061DC11533303ED3E27E368CD341136415AA3`;
- static Screen validation gate: `artifacts/reports/ui/ui-atf-extent-report_static-screen-validation-report-ui-test_2026-10-16_21-35-23-658.html`,
  SHA-256 `C7C8AEDCC8BC2CF38AC41FED5FA85F4EBBC6264DD9194289CA22914F643CA788`.

`StaticScreenValidationReportUiTest` a trecut pe golden UI și a verificat prin
ATF Debug/browser MCP contractul celor trei zone-soră, `hr=0`, linkul
`📷 base64 img`, vertical-align/middle și deschiderea lightbox-ului. Nu a fost
folosit browser Windows real și nu au fost oprite rulări active.

## Reaudit profund — Screen validation + ExpectedReportProjection — 2026-10-16 21:50

În staging-ul izolat `C:\work\ex\java\.tmp\section-projection-screen-20261016-214100`
au fost închise chirurgical cele două puncte active:

- `ReportAuditSnapshot.Tests` păstrează și titlurile scenariilor, cu constructor
  backward-compatible; `ScreenValidation` păstrează topologia zonelor,
  geometria, cardinalitatea payload-ului și imaginile nested. Snapshot-urile
  rămân immutable și renderer-neutral.
- `ExpectedReportProjectionValidator` este pur și reutilizat de
  `TestsSectionValidator` și `FailsSectionValidator`. Pentru rapoarte generate,
  proprietarii, titlurile selectate, fazele, request-urile și validation cards
  sunt comparate cu selecția setup/mock; DOM-ul nu devine expectation.
- `ScreenValidationSectionAuditModule` selectează cardul structural intitulat
  exact `Screen validation`, apoi verifică 1/1/1 zone, 3 rânduri, aceeași
  tabelă, contiguitate, `hr=0`, payload unic, zero imagini nested, link Base64
  inline, aliniere verticală și lightbox.
- Inspectorul screenshot-ului traversează și carduri dinamice generice și alege
  secvența care deține efectiv linkul Base64; secvențele de audit fără media nu
  mai pot masca screenshot-ul canonical.

Validări: 12 teste pure projection/Tests/Fails/ScreenValidation, 18 teste
reporting/CSS/evidence, `SelectedGeneratedSectionsReportUiTest` 2/2 și
`StaticScreenValidationReportUiTest` 1/1. Verificarea finală a fost făcută în
browser MCP/ATF Debug, fără browser Windows real și fără oprirea rulărilor.

Artifactele secțiunii produse în incrementul intermediar au fost evidence de
staging și nu mai sunt pointeri live; nu le păstrăm ca nume normative după ce
directorul de artifacte a fost rotit. Perechea finală promovată și reverificată
este:

- API: `artifacts/reports/api-atf-extent-report_2026-10-16_22-40-08-404.html`,
  SHA-256 `931EC5C5BEE51F4D805E283CCFB93A8924BAC9AA1ABDAC0723FB593925B4C7A4`;
- UI: `artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_22-40-15-301.html`,
  SHA-256 `CAE94267A034BEBCF23B8792FF9E4D5220C45FBF71D8F1C694E11794A3102CAC`;
- Screen validation gate: `artifacts/reports/ui/ui-atf-extent-report_static-screen-validation-report-ui-test_2026-10-16_22-41-46-797.html`,
  SHA-256 `5DD627F54F04B17637F2659112054DAEAF00FAB323B3205A85BA2E1BA8633D06`.

ToDo status: source resolver, registry, verticalele `Tests + Fails`,
`Overview + Categories + Evidence`, `Screen validation`,
`ExpectedReportProjection` și rezultatul renderer-neutral al modulelor sunt
**DONE** la nivelul de compoziție actual. Toate cele șase module returnează
`atf.ui.tests.extent.section.result.SectionAuditResult` și toate șase au
validator pur. Rămân active două consolidări demonstrabile: un executor comun
pentru fiecare `ExtentSectionCheck` (inclusiv `STYLE` și
`COLLAPSED_BY_DEFAULT`) și un publisher Extent/JUnit separat de module.
Retragerea `UiReportAuditRule` global rămâne după aceste două gate-uri, nu este
o refactorizare obligatorie acum.

## Scorecard actual

| Dimensiune | Notă | Observație verificată în codul live |
|---|---:|---|
| Input static vs generated | 96/100 | `SectionAuditFixture` și `ReportSourceResolver` folosesc același lifecycle `ReportAuditContext`; generated/static au expectation explicit, iar Screen validation este deliberat static/read-only pentru a nu valida circular output-ul pe inputul API. |
| Decuplare pe secțiuni | 93/100 | Toate cele șase secțiuni au un singur modul în registry + engine, iar `ExtentSectionAuditModule.audit(...)` returnează rezultatul canonic; publicarea Extent și o parte din selecția check-urilor sunt încă în module, iar engine-ul global rămâne facade de compatibilitate. |
| Testare pură fără browser | 96/100 | Toate cele șase module au snapshot + validator pur; browser-ul este încă necesar pentru extracție/evidence, iar separarea publisherului de module nu este completă. |
| Configurare prin adnotări | 88/100 | `@UiExtentSectionAudit` declară source/section/checks/journey și planul este validat; executorul comun nu acoperă încă uniform toate check-urile, în special `STYLE` și `COLLAPSED_BY_DEFAULT`. |
| Boundary locators → flows → validators | 100/100 | Screen validation consumă numai metode semantice din `ReportUiLocators`; guard-urile acoperă support/flow/audit/inspection. |
| Reutilizare plug-and-play | 92/100 | `Tests + Fails`, `Overview`, `Categories`, `Evidence` și `Screen validation` rulează aceeași definiție generated/static; selecția este tipizată, dar ownership-ul efectiv al fiecărui check și publisherul separat mai trebuie uniformizate. |
| **Scor general pentru section architecture** | **93/100** | Fundația tipizată și validatorii puri sunt live; rămân executorul complet al check-urilor, publisherul independent și retragerea controlată a alias-ului global. |

Nota nu indică o regresie a raportării actuale. Indică distanța până la o
arhitectură în care adăugarea unei secțiuni noi nu cere modificarea unui engine
central, a unui enum global și a mai multor teste de integrare.

## Ce este deja corect

- `ReportAuditContext` tratează raportul ca input read-only și pornește Debug UI
  + Playwright printr-un singur lifecycle.
- `ReportAuditInput` diferențiază `EXISTING` de `API_GENERATED`; această
  distincție trebuie ridicată în composition root, nu duplicată în teste.
- `@ApiScenarioSetup(families = ...)` și `@ApiScenarioSetup(tests = ...)` oferă
  selecție API tipizată, inclusiv metode punctuale din familii diferite.
- `@UiExtentAudit(rules = ...)` separă intenția suitei de implementarea
  browserului.
- `ReportAuditSnapshot`, `ExpectedReportProjection` și `ValidationResult` sunt
  direcția bună pentru contracte renderer-neutre.
- Locatorii sunt concentrați în `ReportUiLocators` și sub-locatorii lui, iar
  testele nu trebuie să construiască selectori HTML direct.
- `LifecycleFailuresReportUiTest` demonstrează că o suită dinamică poate audita
  `Tests → Fails → Overview` și că mock-ul API poate alimenta așteptările UI.

## Ce trebuie decuplat

### 1. Sursa raportului nu trebuie să fie ascunsă în fixture — CLOSED

`SectionAuditSpec` și `ReportSourceResolver` descriu explicit sursa, iar
`SectionAuditFixture` folosește același `ReportAuditContext` pentru generated și
static. `GENERATED_API` trece prin `ApiReportAuditSetup`, în timp ce
`STATIC_PATH`/`STATIC_RESOURCE` verifică inputul read-only și identitatea lui.

Ținta este un input explicit:

```text
ReportAuditSource
  STATIC_RESOURCE  → classpath resource, hash și raport read-only
  STATIC_PATH      → path configurat, hash și raport read-only
  GENERATED_API    → ApiScenarioSetupCatalog → mock → API report → hash
```

Acest contract este deja implementat; API setup-ul este o strategie a sursei,
nu o responsabilitate duplicată în fiecare test de secțiune.

### 2. Regula globală trebuie înlocuită incremental cu un modul de secțiune

`UiReportAuditRule` este util ca vocabular de compatibilitate, dar nu trebuie să
rămână composition root-ul. Engine-ul actual are câte o metodă pentru fiecare
regulă și un `switch (rule)`. Soluția este un registry tipizat:

```text
ExtentSection
  OVERVIEW | TESTS | CATEGORIES | FAILS | EVIDENCE | SCREEN_VALIDATION

SectionAuditSpec
  section + checks + source policy + navigation policy + expected projection

ExtentSectionAuditModule
  section()
  audit(SectionAuditContext context, SectionAuditSpec spec)

SectionAuditRegistry
  section → module
```

Registry-ul și engine-ul de secțiuni sunt live pentru toate cele șase suprafețe.
Limitarea rămasă este că `ExtentSectionAuditModule.audit(...)` publică încă
direct evidence și assertion-ul JUnit, iar `UiReportAuditDynamicEngine` legacy
există în paralel. Următorul increment trebuie să păstreze modulele actuale și
să decupleze rezultatul pur de adaptoarele JUnit/Extent, nu să recreeze testele.

### 3. Browserul trebuie separat de contractul secțiunii

Pentru fiecare secțiune trebuie să existe două niveluri:

```text
SectionSnapshotExtractor (Page + locators → snapshot)
        ↓
SectionValidator (snapshot + expectation → ValidationResult)
        ↓
JUnit assertion + Extent evidence
```

`SectionValidator` trebuie să fie pur: fără `Page`, fără `Locator`, fără
navigare și fără `assertTrue`. Browserul doar extrage fapte și execută flow-ul.
Astfel `Overview`, `Tests`, `Fails` și `Screen validation` pot avea teste unitare
rapide pe snapshot-uri sintetice, iar o singură verificare browser confirmă că
extractorul citește DOM-ul real.

Acest lucru este important pentru `Fails`: testul pur poate verifica două
failure rows, cauze primare/secundare și deep-link-ul așteptat; testul browser
verifică numai că markup-ul real produce snapshot-ul respectiv și că un click
pe link ajunge în `Tests`.

## Contractul de adnotări propus

### A. Audit static al unei singure secțiuni

Path-ul absolut nu trebuie încorporat în source. Preferința este resource de
classpath, apoi o proprietate JVM/configurare pentru artifactul local:

```java
@UiExtentSectionAudit(
        source = UiExtentReportSource.STATIC_RESOURCE,
        reportResource = "reports/RegressionExtent.html",
        sections = {ExtentSection.SCREEN_VALIDATION},
        checks = {
                ExtentSectionCheck.STRUCTURE,
                ExtentSectionCheck.COLLAPSED_BY_DEFAULT,
                ExtentSectionCheck.SCREENSHOT_LINK,
                ExtentSectionCheck.INLINE_ALIGNMENT,
                ExtentSectionCheck.STYLE
        })
class ScreenValidationStaticSectionTest {
}
```

Pentru artifacte generate în `artifacts/reports/` se folosește o cheie
configurabilă, nu un path lipit în cod:

```java
@UiExtentSectionAudit(
        source = UiExtentReportSource.STATIC_PATH,
        reportProperty = "atf.ui.report",
        sections = {ExtentSection.TESTS})
class TestsSectionExistingReportTest {
}
```

Acesta este „unit test-ul de secțiune” în sensul suitei UI: contractul lui este
izolat la o secțiune și la un input static. Validatorul pur asociat poate fi
executat fără browser; testul static UI este component/read-only, nu pretinde
că este unit test Java pur.

### B. Audit dinamic API → mock → report → secțiune UI

Selecția API rămâne programatică și reutilizabilă:

```java
@ApiScenarioSetup(families = {"users", "posts"})
@UiExtentSectionAudit(
        source = UiExtentReportSource.GENERATED_API,
        sections = {ExtentSection.TESTS, ExtentSection.FAILS, ExtentSection.OVERVIEW},
        checks = {
                ExtentSectionCheck.STRUCTURE,
                ExtentSectionCheck.MOCK_RECONCILIATION,
                ExtentSectionCheck.CROSS_SECTION,
                ExtentSectionCheck.STYLE
        })
class UsersPostsSectionsDynamicTest {
}
```

Pentru selecție punctuală, `@ApiScenarioSetup(tests = { @ApiTestRef(...) })`
rămâne forma exactă. Pentru `LifecycleApiScenarios`, aceeași adnotare poate
selecta fixture-ul intenționat cu failures reale, fără clasă UI duplicată.

### C. Adnotare compusă pentru un flow cross-section

Un flow precum `Tests → Fails → Open in Tests → Overview` nu trebuie să fie
împrăștiat în patru teste. Se declară ca journey tipizat peste modulele deja
existente:

```java
@ApiScenarioSetup(families = {"lifecycle"})
@UiExtentSectionAudit(
        source = UiExtentReportSource.GENERATED_API,
        sections = {ExtentSection.TESTS, ExtentSection.FAILS, ExtentSection.OVERVIEW},
        journey = ExtentJourney.FAILURE_NAVIGATION,
        checks = {ExtentSectionCheck.CROSS_SECTION,
                  ExtentSectionCheck.COLLAPSED_BY_DEFAULT})
class FailureNavigationSectionsTest {
}
```

Adnotarea descrie intenția; engine-ul nu citește selectorii și nu face
`page.locator`. Selectorii rămân în locator boundary, iar pașii în flow.

## Structura OOP recomandată

```text
atf.ui.tests.extent.sections
├── annotation
│   ├── UiExtentSectionAudit
│   ├── UiExtentReportInput
│   └── ExtentSectionCheck
├── model
│   ├── ExtentSection
│   ├── SectionAuditSpec
│   ├── SectionAuditContext
│   └── SectionSnapshot
├── source
│   ├── StaticReportSource
│   ├── GeneratedApiReportSource
│   └── ReportSourceResolver
├── module
│   ├── ExtentSectionAuditModule
│   ├── OverviewSectionModule
│   ├── TestsSectionModule
│   ├── FailsSectionModule
│   ├── EvidenceSectionModule
│   └── ScreenValidationSectionModule
├── extract
│   └── Page-specific snapshot extractors
├── validate
│   └── Pure snapshot validators
└── engine
    └── SectionAuditDynamicEngine
```

Structura nu trebuie introdusă prin mutarea mecanică a tuturor claselor curente.
Se migrează un vertical slice complet, de exemplu `Screen validation` sau
`Fails`, apoi se elimină responsabilitatea echivalentă din engine-ul global.

## Separarea responsabilităților

| Componentă | Face | Nu face |
|---|---|---|
| `ReportSourceResolver` | rezolvă static/generated, verifică file/hash și read-only | nu navighează și nu validează CSS |
| `SectionAuditModule` | compune pașii și check-urile unei secțiuni | nu construiește selectori și nu decide sursa API |
| `SectionSnapshotExtractor` | citește DOM-ul prin locator facade | nu face aserțiuni JUnit și nu publică direct verdictul |
| `SectionValidator` | verifică snapshot-ul pur față de expectation | nu știe Playwright, URL sau Extent |
| `SectionFlow` | navighează, click-uiește și așteaptă stări semantice | nu conține reguli de business duplicate |
| `SectionLocators` | deține selectorii și scoping-ul | nu știe mock/YAML |
| `SectionEvidencePublisher` | publică expected/actual, screenshot și finding | nu decide dacă un API este valid |
| testul cu adnotări | declară sursa, secțiunea, checks și API precondition | nu conține DOM, CSS sau ordine procedurală |

## Ordinea de implementare și ToDo

### P0 — fundația comună

- [x] Introdu `UiExtentReportSource` și un `ReportSourceResolver` peste
  `ReportAuditInput`/`ReportAuditContext`; nu duplica lifecycle-ul Debug UI.
- [x] Adaugă `UiExtentSectionAudit` + `SectionAuditSpec` cu `source`,
  `reportResource`/`reportProperty`, `sections`, `checks` și `journey`, fără să
  rupă compatibilitatea cu `@UiExtentAudit(rules=...)`.
- [x] Adaugă validare strictă: resource/path/property exact unul pentru static;
  generated nu face fallback silențios; selecția API rămâne în
  `@ApiScenarioSetup`/composition root.

### P1 — primul vertical slice — DONE

- [x] Migrează `Screen validation` într-un `ScreenValidationSectionAuditModule`.
  Modulul verifică separat cele trei zone-soră, screenshot link-ul inline,
  alinierea verticală și lipsa separatorilor decorativi.
- [x] Creează `ReportAuditSnapshot.ScreenValidation` + validator pur și teste
  unitare fără browser; gate-ul browser este static/read-only și rulează pe
  raportul UI produs de golden E2E, nu circular pe inputul API.
- [x] Creează `SectionAuditDynamicEngine` cu registry; engine-ul vechi rămâne
  facade de compatibilitate până când toate secțiunile sunt migrate.
- [x] Migrează primul vertical slice `Tests + Fails`: static path + generated
  API, snapshot pur, cauze/deep-link și raport Extent verde cu screenshot.

### P1 — al doilea vertical slice — CLOSED

- [x] Migrează `Fails` + `Tests` în module reutilizabile: failure rows, cauze,
  deep-link și disclosures colapsate sunt verificate prin
  `FailsSectionAuditModule`/`TestsSectionAuditModule`.
- [x] Rulează aceeași definiție cu `STATIC_PATH` și `GENERATED_API`;
  `GeneratedTestsSectionReportUiTest` și `StaticTestsSectionReportUiTest`
  folosesc același `SectionAuditDynamicEngine`, iar inputul API rămâne sursă
  read-only pentru auditul UI.
- [x] Refolosește `ExpectedReportProjection` ca expectation, nu text copiat
  din DOM și nu status hard-codat în test.

### P2 — convergență și retragere controlată

- [x] Migrează `Overview`, `Categories` și `Evidence` pe aceeași interfață
  `ExtentSectionAuditModule`; `SectionAuditRegistryTest` verifică faptul că
  toate cele șase secțiuni au un singur modul.
- [x] Păstrează `UiReportAuditRule` global ca facade de compatibilitate; alias-urile
  și ștergerea enum-ului sunt trigger-gated și nu se justifică la dimensiunea
  actuală a suitei.
- [x] Separă snapshot-urile browser de validatorii puri în toate cele șase
  module; `Overview`, `Categories` și `Evidence` folosesc acum validatori
  `ValidationResult` pe snapshot, iar `Page` rămâne numai în adaptorul browser.
- [x] Adaugă architecture guards: `DeclarativeUiTestArchitectureTest`
  respinge `ReportUiLocators`, `Page`, `Locator` și selectorii în testele cu
  adnotări; guard-ul locatorilor acoperă support/flow/audit/inspection.
- [x] Documentează contractul stabil în README-ul pachetului Extent; feedback-ul
  păstrează numai ToDo-urile active și criteriile de închidere.

### P1 — consolidarea rămasă după migrarea celor șase module

- [ ] Aplică `SectionAuditCheckPolicy` (sau un executor echivalent) în toate cele
  șase module și verifică `requestedChecks ⊇ executedChecks` pentru fiecare
  combinație validă `sections × checks × journey`; `STYLE` și
  `COLLAPSED_BY_DEFAULT` trebuie să aibă execuție și finding propriu, nu doar
  enum/filtrare declarativă.
- [ ] Extrage `SectionEvidencePublisher` și adaptorul JUnit din
  `ExtentSectionAuditModule.audit(...)`; modulul trebuie să returneze rezultatul
  typed înainte de publicarea Extent și fără `AssertionError` în composition
  layer.
- [ ] Retrage gradual alias-ul global `UiReportAuditRule` doar după parity
  coverage și un raport verificat în ATF Debug/browser MCP; până atunci rămâne
  compatibility facade documentată.

## Criterii de acceptare

Soluția este considerată implementată numai când toate aceste cazuri sunt
verzi:

1. un validator pur verifică snapshot-ul secțiunii fără browser;
2. aceeași secțiune rulează pe un raport static declarat prin adnotare;
3. aceeași secțiune rulează pe raport API generat din adnotare + mock/YAML;
4. output-ul Extent conține expected/actual, evidence și screenshotul corect;
5. locatorii nu apar în testul de secțiune;
6. raportul static este read-only și identificat prin path/resource + hash;
7. build-ul și artifactele sunt produse în `.temp`, apoi sincronizate selectiv
   în live și redeschise prin ATF Debug/browser MCP;
8. nu se introduce `ui.enabled`, un boolean duplicat sau o a doua compoziție
   API/UI ascunsă.

## Decizia recomandată

Direcția corectă este **section modules + source resolver + pure snapshots**,
nu câte o clasă mare pentru fiecare tab și nici un mega-test parametrizat cu
toate responsabilitățile. Se începe cu `Screen validation` ca secțiune statică
și cu `Fails/Tests` ca journey dinamic `LifecycleApiScenarios`; acestea verifică
atât decuplarea, cât și legătura `setup → API → mock → report → UI → validare`.

Până la închiderea P0/P1, clasele curente rămân funcționale și nu se șterg.
Orice mutare este acceptată doar după aceeași acoperire, același raport
verificat și aceeași trasabilitate a artifactului.
