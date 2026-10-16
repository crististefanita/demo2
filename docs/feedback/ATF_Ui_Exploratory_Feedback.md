# ATF Java — active UI exploratory feedback

Ultima reconciliere cu codul live și artifactul verificat: **2026-10-16**.
Acest document rămâne feedback-ul UI/Extent activ; statusul exclusiv pentru
API este centralizat în [`README.md`](README.md) și în README-urile modulelor API.

Pointerul curent de artifact este
`artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_23-10-51-265.html`
(SHA-256 `88776EC0CA17F58945255EFAB60318CEE1C868C172B4F8A81FB3642C49207342`).
Perechea API verificată este
`artifacts/reports/api-atf-extent-report_2026-10-16_23-10-44-916.html`
(SHA-256 `6FB9C307C25B5249CA91E7042D3F76250FF78237F2281F75225EC05750DC4EF3`).
Rulările 17-04/17-16/17-26/19-00 rămân doar trasabilitate istorică.

This document contains the active UI/reporting gaps plus the latest closure
ledger. The stable visual
contract belongs to [`atf-reporting/README.md`](../../atf-reporting/README.md).
The implemented plug-and-play architecture, manifest and dynamic test-factory
contract belongs to the package boundary
[`atf-tests/.../extent/README.md`](../../atf-tests/src/test/java/atf/ui/tests/extent/README.md);
this document retains only active gaps.

## Namespace ownership — DONE 2026-10-16

- [x] Toate testele actuale care deschid browserul pentru auditarea raportului
  Extent sunt sub `atf.ui.tests.extent`.
- [x] `audit`, `flows`, `locators`, `support`, `configurable`, `exploratory`,
  `sanity` și `flows.failure` sunt delimitate semantic în acest namespace.
- [x] Contractul local este în
  `atf-tests/src/test/java/atf/ui/tests/extent/README.md`.
- [x] Viitoarele teste UI ale aplicației nu se adaugă aici; namespace-ul
  rezervat este separat (de exemplu `atf.ui.tests.product`).
- [x] Guardrail-urile de arhitectură rulează după migrare pentru căile acoperite;
  toate straturile reusable sunt acoperite de guard-ul locator boundary; nu există
  selector direct în support/flow/audit/inspection.

## Stable contract already implemented

- The input report is read-only and may be selected through `-Datf.ui.report`
  or `ui.audit.inputReport`.
- `atf-tests` owns executable Extent-report UI tests in `atf.ui.tests.extent`; `atf-ui` owns the
  Playwright configuration and adapter, not the HTML renderer.
- Gherkin steps keep the action sequence, assertions and screenshot of the same
  visual state together.
- Real Playwright operations (`Click`, `Get text`, `Focus`, `Find`, `Wait` and
  `Expect`) record their effective locator in `TARGET`; observations are marked
  separately as `OBSERVE`.
- UI tests use `ReportUiLocators` as the single semantic owner for flow, action
  and validation locators. Direct `page.locator(...)` calls are forbidden in
  UI test/flow/auditor layers and are protected by
  `UiSelectorBoundaryArchitectureTest` plus the existing UI test-layer guard.
- Consecutive actions are one collapsed table with
  `# | OPERATION | TARGET | EXPECTED | OBSERVED | TIME | RESULT`.
- Each action table has one real Base64 screenshot immediately below it, inside
  one clickable native anchor with `href="data:image/png;base64,..."`,
  `data-featherlight="image"`, `target="_blank"`, `rel="noopener"` and marker
  `📷 base64 img`. The anchor carries the only payload and opens that same image
  in a Featherlight lightbox, with a new-page fallback. No external PNG, inline
  `<img>`, inline click handler or duplicate payload reference is allowed.
  A repeated payload is represented by `.atf-ui-screenshot-duplicate` and is
  not embedded again.
  The marker is a compact 18px inline-flex pill with vertically
  centered icon/text, and the complete anchor is clickable.
- `Screen validation` keeps the three sibling evidence zones
  (`Playwright action sequence`, `Audit observations`, `Assertions`) inside
  one shared contour: only the outer top/bottom/side frame remains; no
  decorative horizontal separator is rendered between the zones.

The inspected `RegressionExtent.html` reference confirms the intended model:
the screenshot is a real visual artifact associated with the action sequence,
not merely a text marker. ATF keeps the stronger safe-tab attributes while
ensuring the payload is emitted once as a text-only native link and opened from
that same source without an inline thumbnail.
- Tests discover owners, scenarios, phases, tags and categories from the DOM;
  counts are reconciled dynamically instead of hard-coded.
- Categories have a pure zero/one/many contract plus malformed-ID checks; the
  data-driven browser matrix now collects every category cardinality and covers
  empty/failure, one/many, GET/POST/PUT and normal/narrow viewport permutations.
- Status badge geometry is verified by the generated-report UI test at the
  normal viewport and at 520px; discovered status labels and the computed
  display/box/height/padding/line-height/radius signature must remain stable.
- `ReportAuditInput` makes the source explicit: `API_GENERATED` is produced by
  API setup and `EXISTING` is resolved from YAML or `-Datf.ui.report`; both
  use the same read-only `ReportAuditContext` and Debug UI/Playwright lifecycle.
- `UiTestData` centralizes API family keys, API owner names and report tags.
  `ApiReportAuditSetup` accepts a manually selected collection through
  `-Datf.ui.api.families` or `-Datf.ui.audit.suite`, with precedence over
  `@ApiScenarioSetup` and YAML;
  duplicate/unknown/empty selections fail fast. The live catalog includes
  `posts`, `users`, `requests`, `headers`, `flows`, `negative` and `lifecycle`;
  the reusable UI contract was verified with both `users,posts` and
  `requests,headers`.

### UI-CONFIG-01 — closed

The UI layer can now choose the API collection used to generate its report
precondition without duplicating test classes. Annotation profiles remain the
readable default for the two manual contracts, YAML supplies the default
`ui.apiSetup.scenarioFamilies`, iar `-Datf.ui.audit.suite` și
`-Datf.ui.api.families` sunt override-uri manuale. Rules/depth sunt JVM
properties; aceeași infrastructură de locatori, actions, flows, validatori și
evidence este reutilizată pentru orice familie selectată.

### UI-EXTENT-REUSE-01 — DONE / GATE-CLOSED

`UI-CONFIG-01` și incrementul `UI-EXTENT-REUSE-01` rezolvă generarea dinamică
a suitei UI prin `UiReportAuditSpec`, `ApiScenarioManifest`,
`UiReportAuditFixture` și `UiReportAuditDynamicEngine`. Cele două profile
configurabile aprobate rămân intenționat pentru rulare manuală, iar codul nou nu
mai cere o clasă UI pentru fiecare combinație de familii API:

- `UsersPostsApiReportUiTest` și `RequestVariantsAndApiHeadersReportUiTest`,
  profilele manuale păstrate la cerere, care reutilizează același harness;
- `AbstractApiReportUiContractTest`, păstrat strict ca suport de compatibilitate
  pentru cele două profile aprobate;
- `ApiExtentReportExploratoryTest`, care este acum wiring golden E2E subțire;
  regulile comune sunt în factory/engine/inspectors;
- `ApiReportExplorationFlow`, care este o facade semantică de compatibilitate;
  snapshot-urile sunt în modelul neutru `ReportAuditSnapshot`.

Scorul curent pentru reutilizarea plug-and-play este **100/100** pentru scope-ul
exact verificat: locatorii, lifecycle-ul, evidence-ul, manifestul expected,
engine-ul dinamic, resolverul tuturor familiilor, reconcilierea request/mock și
rezultatul comun `ValidationResult` sunt verificate. `AllApiReportUiContractTest`
și companion-ul său au fost retrase chirurgical: primul hard-coda owner-i și
dublează factory-ul dinamic, iar al doilea îl păzea doar pe primul. Soluția
stabilă este în
[`atf-tests/.../extent/README.md`](../../atf-tests/src/test/java/atf/ui/tests/extent/README.md),
iar boundary-ul tehnic al pachetului este în
[`atf-tests/.../extent/README.md`](../../atf-tests/src/test/java/atf/ui/tests/extent/README.md).

Nu se adaugă `ui.enabled: true`. În live, execuția este controlată prin
`UiReportAuditDynamicTest`, annotation, familiile YAML implicite sau proprietăți
JVM; suitele declarative YAML (`ui.audit.suites`) sunt live prin
`UiAuditSuiteConfiguration` și `UiConfigurationYamlReader`; registry-ul de
runtime separat și planul neutru sunt încă ținte. Fără familii rezolvate,
setup-ul eșuează explicit.

Incrementul curent a extras `CrossTabReconciler` și `ValidationResult` în live;
reconcilierea cross-tab publică acum assertions și violations identic către
JUnit și Extent, iar Categories are contract pur pentru zero/one/many. Matricea
browser este live. `Finding` nu este introdus doar ca alias: `ValidationResult`
este contractul comun actual, iar un model mai bogat se justifică numai dacă
apare un al doilea consumator renderer-neutral.

Lanțul live este:

```text
UiReportAuditSpec
  → API setup + atf-mock.yaml
  → ApiScenarioManifest / ExpectedReportProjection (live)
  → UiReportAuditSpec / SectionAuditSpec + SectionAuditRegistry (live)
  → dynamic tests: Tests, Overview, Categories, Fails, Evidence, Cross-tab
  → extractor → ReportAuditSnapshot → pure validator → ValidationResult
  → AtfAssertion + Extent evidence (fără validare DOM în validatorii de secțiune)
```

În live, `UiReportAuditSpec` + `ApiScenarioManifest` +
`UiReportAuditDynamicEngine` acoperă deja selecția, expected-ul din catalog/mock
și nodurile dinamice. Expected-ul trebuie derivat din familiile selectate,
catalog și mock; actualul
din DOM. Pentru `POST`/`PUT`, regula JSON request + response rămâne obligatorie,
iar excepțiile negative trebuie declarate în manifest. Fiecare request trebuie
legat de un singur route identity (`method + path + query canonicalizat`), nu
doar de un text găsit în HTML.

ToDo rămas:

- [x] Introdu `UiReportAuditSpec`, `UiReportAuditRule` și
  `ApiScenarioManifest`; owner-ii și route identity-urile factory-ului provin
  din manifest.
- [x] Creează un singur `@TestFactory` pentru suite și noduri dinamice pe
  regulă; setup-ul API se execută o singură dată per spec.
- [x] Verifică deep full cu toate familiile configurate în YAML și cu
  request/response JSON, mock, Categories, Fails, durate, responsive, evidence
  și cross-tab.
- [x] Extrage `CrossTabReconciler` și `ValidationResult` pentru reconcilierea
  dinamică Tests/Overview/Categories/Fails; rezultatul publică assertions și
  violations către JUnit și Extent.
- [x] Încarcă suite declarative din YAML (`ui.audit.suites`) cu familii, tabs,
  validators, depth și source; resolverul live și testul `users-posts` sunt
  verificate în build izolat.
- [x] Inventariază ownership-ul testelor, flow-urilor, locatorilor,
  inspectorilor și validatorilor în
  [`ATF_Ui_Extent_Inventory.md`](ATF_Ui_Extent_Inventory.md); decizia de a nu
  extrage încă `atf-report-contracts` este documentată pe baza consumatorilor
  reali.
- [x] Separă snapshot modelul de facade și converg JSON/mock/contract/style/
  duration în `ValidationResult`; reconcilierea cross-tab și modulele de
  secțiune folosesc același rezultat.
- [x] Migrează complet inspectorii DOM rămași la forma
  `extractor → ReportAuditSnapshot → pure validator → ValidationResult`.
  `EvidenceSectionExtractor` + `ApiJsonEvidenceExtractor` separă request/response
  raw evidence de `JsonEvidenceValidator`; `ApiReportStructureExtractor` și
  `FilterSectionExtractor` separă Tests/Overview/Annotations/Failures de
  contractul `Filters`; `CategoriesSectionExtractor` +
  `CategoriesSectionValidator` și `VisualEvidenceSectionExtractor` +
  `VisualEvidenceValidator` închid vechile Categories/Evidence paths. Selectorii
  rămân în adaptoarele extractor/locator, iar validatorii nu primesc `Page`,
  `Locator` sau DOM; `DomInspectorMigrationBoundaryTest` și convergența pură
  acoperă replacement-ul fără schimbarea contractului de raport.
- [x] Retrage agregatorul generic redundant după replacement coverage
  echivalentă; cele două clase configurabile și suportul lor abstract rămân
  intenționat.
- [x] Păstrează `ApiExtentReportExploratoryTest` doar ca golden E2E subțire și
  `ExistingExtentReportExploratoryTest` ca adapter pentru input HTML existent.

## API/UI ownership boundary

The API package never owns a browser. `atf.api.tests.api` contains only API
resource/request/lifecycle tests; API artifact setup is owned by
`atf.ui.tests.extent.composition.ApiReportAuditSetup` (the historical
`ApiReportSetupFlow` is only a compatibility facade). Any `Page`, `Locator`, Playwright,
Debug UI, DOM, screenshot or rendered-report assertion belongs in
`atf.ui.tests.extent`. The generated UI audit may call `ApiReportAuditSetup` for its
configurable API precondition, but the API setup is not a separately ordered
test suite and the API input remains read-only.
- `OverviewScreenValidator` is the single reusable Overview contract. It reads
  the rendered Overview once and validates it against the dynamic Screen Tests
  snapshot; generated API tests can additionally bind the declared YAML/setup
  families and the mock route catalog without duplicating assertions.

## Remaining ToDos

### UI-21 — locator boundary închis / ARCH-02 DONE

- [x] Move selectors from exploratory and navigation-flow code behind the
  canonical `ReportUiLocators` facade.
- [x] Add an architecture test that rejects direct Playwright selector calls in
  flow/auditor code and retain the UI test-layer guard.
- [x] Keep Gherkin intent and assertions in the test; locator ownership belongs
  to the locator class and action/flow code only consumes semantic handles.
- [x] Inject one `ReportUiLocators` facade through the main API exploration
  inspectors; request, category, failure and evidence paths use semantic
  locator methods.
- [x] Move the remaining tab/runtime-root selectors behind semantic locator
  methods and extend the architecture guard across support, flow, audit and
  inspection layers. Direct selector construction remains exclusively in the
  locator boundary.
- [x] Remove the superseded DOM inspector classes after their responsibilities
  were consolidated into section extractors, immutable `ReportAuditSnapshot`
  records and pure `ValidationResult` validators; `FailsScreenValidator` remains
  only where the existing-report compatibility contract still requires it.

| ID | Priority | Gap | Next verifiable step |
|---|---:|---|---|
| ARCH-UI-01 | DONE | Tests/Overview/Categories/Fails and API request/evidence now use reusable typed contracts; dynamic mock reconciliation remains behind the same composition boundary. | Keep the dynamic UI audit, locator boundary and mock-backed audit green. |
| F-10 | DONE | Source selection, lifecycle, structural, screen and API request/evidence validators are shared without hiding Gherkin intent. | Keep the dedicated request/evidence contract covered by the generated UI audit. |
| UI-EXTENT-REUSE-01 | DONE / GATE-CLOSED | Spec/manifest/engine/factory, resolverul tuturor celor șapte familii, `ExpectedReportProjection`, `ValidationResult`/`CrossTabReconciler`, fixture-urile generate Categories `zero/one/many`, convergența auditorilor și reconcilierea mock-backed sunt live; cele două profile aprobate și seam-urile legacy rămân intenționat. | Fresh artifact gate, matrix 4/4 și verificarea ATF Debug UI/browser MCP sunt închise. |
| F-05 | DONE | `ReportProjectionMatrixInspector` verifică într-un singur test browser Overview, Tests, Categories și Fails pentru vizibilitate, computed surface, conținut dinamic și controale de filtrare aplicabile. | Artifactul UI curent `artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_23-10-51-265.html` a fost deschis și verificat prin ATF Debug UI/browser MCP. |
| UI-15 | DONE | `CategoryCardinalityBrowserFixtureTest` generează și auditează fixture-uri Categories `zero`, `one` și `many`; zero-state-ul publisherului este stabil și a fost deschis prin ATF Debug/browser MCP. | Păstrează fixture gate-ul și contractul `#category-view` pentru zero categories. |
| LOC-04 | DONE | `ReportUiLocators` is the canonical boundary; the reusable support/flow/audit/inspection guard covers the selector seam and the focused tests pass. | Keep `UiSelectorBoundaryArchitectureTest` and `ReportUiLocatorsTest` green. |
| D-04 | DONE | Shared badge geometry is gated at normal and narrow viewport using the generated API report. | `ApiExtentReportExploratoryTest.statusBadgesKeepSharedGeometryAtNormalAndNarrowViewport` compares computed signatures. |
| LOC-03 | DONE | Nu există `waitForTimeout` în sursele live UI/test; tranzițiile de structural-card, disclosure și runtime-ready folosesc stări semantice. | Păstrează guard-ul de arhitectură și matricea browser ca regression gates. |
| UI-22 | DONE | `Screen validation` are trei zone-soră contigue, cu aceeași lățime și fără separatoare orizontale decorative între ele. | `StaticScreenValidationReportUiTest` + artifactul static de gate au trecut; linkul `📷 base64 img` rămâne inline și centrat vertical. |
| UI-18 | DONE | Reporting screenshot fixtures use shared minimal valid PNG payloads; arbitrary bytes remain only in the generic Spark asset-inlining test. | `ReportTestScreenshots`, `ExtentReportAssetsTest` and `ExtentReportUiEvidenceTest` pass in the isolated reporting regression. |
| OPS-UI-01 | DONE | `AtfDebugUiLauncherParallelTest` a servit două rapoarte independente cu heap bounded; nu a apărut niciun nou `hs_err_pid`. | Păstrează testul ca regression gate. |

### Evidence for the implemented increment — 2026-10-16

- [x] Exact final golden artifact copied to
  `artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_23-10-51-265.html`.
- [x] Artifact opened through ATF Debug UI/browser MCP: Overview shows 1
  report class, 6 passed steps and 0 failures; Tests exposes the exploratory
  UI contract screens.
- [x] Categories renders the generated entries with linked test entries;
  Fails renders the explicit empty state `No failed tests.`.
- [x] A request contract screen was opened, its Playwright action sequence
  expanded, the three sibling validation zones inspected without internal
  separators, and the current `📷 base64 img` inline link verified as a
  lightbox-capable data URI with centered vertical alignment.
- [x] No Windows real-browser validation was used; the report was inspected
  only through the ATF Debug UI/browser MCP surface.
- [x] The paired API artifact contains the POST/PUT JSON and transport evidence;
  the full API/UI chain remains configurable through the dynamic factory and
  the two approved manual profiles. Historical backlog-depth reports are not
  used as the current status pointer.
- [x] The focused migration gate passed in staging: `7/7` pure convergence and
  boundary tests, followed by the golden `ApiExtentReportExploratoryTest` with
  `1/1` test passed. The generated API report visibly exposes request URL,
  auth profile/type, headers, request JSON, HTTP status, content type, response
  headers, response JSON and execution attempts for the POST contract.
- [x] The five superseded DOM inspector classes are absent; the replacement
  coverage is explicit in `section/extract`, `section/validate` and
  `ReportAuditSnapshot`. The final staged UI/API pair was opened through ATF
  Debug UI/browser MCP and visually inspected; no real Windows browser was
  used.
- [x] Static section gate copied to
  `artifacts/reports/ui/ui-atf-extent-report_static-screen-validation-report-ui-test_2026-10-16_22-41-46-797.html`;
  it reopens the golden UI input read-only and validates the three-zone contract.

## Target structure

The report source is variable; the audited contract is shared:

```text
ReportSource (generated API | existing HTML)
        ↓
ReportAuditContext + PlaywrightFlow
        ↓
Overview / Tests / Filters / Categories / Fails auditors
        ↓
ContractResult → JUnit assertions + Extent evidence
```

The implementation must not duplicate the same validator for generated and
existing reports. Public tests describe Gherkin intent, parameters and setup;
reusable support owns browser lifecycle, discovery, snapshotting and evidence
publication.

## UI closure gate

1. Synchronize the live source before the build.
2. Add a regression test before changing the renderer or flow.
3. Run focused unit/component tests and the affected UI test.
4. Generate a fresh UI artifact; never use an old report as proof.
5. Open that exact artifact in TAB MCP and verify clicks, disclosures,
   navigation, computed styles and local screenshot association.
6. Update this file only with the remaining gap.

The report is audit-ready only when each visual state can be reconstructed as:

```text
Gherkin step → real Playwright actions/locators → assertions → one Base64 link
```

