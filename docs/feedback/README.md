# ATF Java — active engineering feedback

This directory contains the active gaps plus a compact status ledger used to
keep feedback synchronized with the live source. Stable contracts belong in
the module READMEs and `docs/`; completed work is not kept as an active ToDo
or as a running-history diary.

Specialized ownership: API-only quality is tracked in this central ledger and
the API module READMEs; [`ATF_Ui_Exploratory_Feedback.md`](ATF_Ui_Exploratory_Feedback.md)
for UI/Extent gaps, [`ATF_Ui_Extent_Inventory.md`](ATF_Ui_Extent_Inventory.md)
and [`ATF_Ui_Extent_Reusable_Test_Strategy.md`](ATF_Ui_Extent_Reusable_Test_Strategy.md)
for the plug-and-play architecture, and
the module READMEs for the closed cross-report shell and exact-selection
contracts.

The live design scorecard and responsibility review are in
[`ATF_Architecture_Design_Review.md`](ATF_Architecture_Design_Review.md).

## Live status — reconciled with source and final UI artifact

| Status | IDs | Meaning |
|---|---|---|
| DONE | F-01, F-02, F-03, F-05, F-06, F-07, F-09, F-10, F-11, F-12, F-13, API-DESIGN-01, API-JSON-01, API-JSON-02, API-QUERY-01, API-PACKAGE-01, API-CONSTANTS-01, REP-UI-01, UI-14, UI-15, UI-16, UI-17, UI-18, UI-19, UI-20, UI-21, UI-22, UI-23, UI-CONFIG-01, UI-EXTENT-REUSE-01, LOC-03, LOC-04, D-03, D-04, ARCH-01, ARCH-02, ARCH-03, ARCH-04, ARCH-05, ARCH-UI-01, ARCH-UI-02, ARCH-UI-03, DOC-01, OPS-UI-01 | The contract is implemented and protected by source-level, artifact or ATF Debug UI evidence. |
| DECISION CLOSED | ARCH-06 | The readable dynamic switch remains intentional; a registry is trigger-gated by rule/parameter growth and is not justified by the current source. |
| ACTIVE | UI-SECTION-ARCH-01, UI-EXTENT-INSPECTOR-01, `AtfReportTheme`, `UiExtentReportSession` | The section modules need uniform check execution plus independent publication; the remaining DOM inspectors and two reporting composition hotspots need controlled, contract-preserving refactors. |

### Closed contracts moved to module documentation

The former closed feedback increments are now stable module documentation:

- [`atf-reporting/README.md`](../../atf-reporting/README.md) — UI/API evidence
  shell and style contract.
- [`atf-tests/.../extent/README.md`](../../atf-tests/src/test/java/atf/ui/tests/extent/README.md)
  — exact API-test selection and deep UI audit contract.

The exact-selection score is intentionally scoped to that increment; the
current reusable UI closure is recorded below and does not imply a new class
for every API-family combination.

### Closed gate already verified

**`UI-EXTENT-REUSE-01` fresh artifact gate — DONE:** the selected golden path
   was rerun in isolated `.tmp`, the exact HTML was copied to the configured
   API/UI report folders, and both exact files were opened through ATF
   Debug/browser MCP. Current closure pair:
   `api-atf-extent-report_2026-10-16_22-40-08-404.html` and
   `ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_22-40-15-301.html`.
   The API POST/PUT request/response JSON and the UI three-zone/screenshot layout
   were expanded and visually checked. Current runs were not stopped.

Current exact artifact hashes:

- API: `931EC5C5BEE51F4D805E283CCFB93A8924BAC9AA1ABDAC0723FB593925B4C7A4`
- UI: `CAE94267A034BEBCF23B8792FF9E4D5220C45FBF71D8F1C694E11794A3102CAC`
- Screen validation gate: `5DD627F54F04B17637F2659112054DAEAF00FAB323B3205A85BA2E1BA8633D06`
- staging: `C:/work/ex/java/.tmp/deep-docs-live-20261016-2258`

### Priority by impact — current scope closed

1. **Closed — `UI-15`:** dedicated browser fixtures now prove Categories
   `zero`, `one` and `many`, including the generated empty category shell.
2. **Closed — projection/CSS parity:** Extent/fallback and
   `ExpectedReportProjection` remain aligned under semantic CSS/evidence guards.
3. **Closed — `OPS-UI-01`:** the official bounded launcher passed the parallel
   smoke and produced no new `hs_err_pid` file.
4. **Closed — `F-01`:** Categories empty-state CSS/markup now have semantic
   owners and focused contract tests; further splitting is optional and
   trigger-gated by a new semantic owner.

`D-01`, `F-02`, `D-03`, `LOC-03`, API contract tracks, locator ownership and the
renderer/style contracts are closed implementation items and remain regression
guards, not active ToDos.

### Latest reconciliation

`UI-22` is closed. The live CSS, its focused regression test and the exact
final UI artifact agree: `Screen validation` keeps the three evidence zones as
direct siblings with one shared contour, no decorative horizontal separators
between them, and the inline `📷 base64 img` link remains aligned on the same
line. This is not an active gap anymore.

The authoritative current visual evidence is the exact UI artifact listed in
the closure pair above (and its separate static Screen validation gate). It
was opened through ATF Debug UI/browser MCP after an isolated `.tmp` staging
run; the live copies are under `artifacts/reports/ui/`. Older narrow/write
artifacts remain historical evidence only and are not current status pointers.

`LOC-03` is closed for the current UI production path: a source scan finds no
`waitForTimeout` calls in the live UI/test sources, and the browser matrix uses
semantic structural-card, disclosure and runtime-ready waits. Keep the
architecture guard and matrix as regression protection.

The screenshot contract is no longer an active gap: every reporting path uses
`ExtentReportAssets.base64ScreenshotLink` and emits one visible native
`a[href="data:image/png;base64,..."]` per visual state. The anchor uses
`data-featherlight="image"`, `target="_blank"` and `rel="noopener"`; the
Base64 payload occurs exactly once, in the anchor `href`. No external PNG,
inline `<img>`, inline click handler or duplicate payload is allowed. The
marker is `📷 base64 img` and remains a compact, vertically centered label in
the same anchor.
The marker is a compact 18px inline-flex pill: icon and text are centered on
the same line. The complete anchor opens the Base64 payload in the Featherlight
overlay, with a new-tab fallback.
If the same payload is captured again in the same publication, only the first
anchor carries the Base64 bytes; later occurrences become an explicit
`.atf-ui-screenshot-duplicate` marker with a stable SHA-256 reference.

### Color notation rule — HEX only

The live reporting theme and all new UI/CSS changes must express colors as
hexadecimal tokens: `#RRGGBB` or `#RRGGBBAA` when alpha is required. Do not add
`rgb(...)`, `rgba(...)` or spaced variants such as `rgb( a, b, c)` to CSS,
markup, Java constants or configuration. Keep colors semantic and centralized
in the shared theme/token layer. Browser `computedStyle` output may normalize
HEX to `rgb(...)`; that runtime representation is acceptable for visual
inspection, while the source contract remains HEX-only.

`Screen validation` keeps `Playwright action sequence`, `Audit observations`
and `Assertions` as sibling disclosures. There are no decorative horizontal
dividers or empty vertical gaps between these three zones: they are stacked
directly, with the shared group contour and internal table grids retained.

The D-04 badge geometry contract is executable in
`atf.ui.tests.extent.exploratory.ApiExtentReportExploratoryTest.statusBadgesKeepSharedGeometryAtNormalAndNarrowViewport`.
The test discovers the status population from the generated report, captures
the computed display/box/height/padding/line-height/radius signature at the
normal viewport, repeats it at 520px, and requires the same labels and
signature without assuming that every run contains every status.

The visual reference `RegressionExtent.html` was inspected through the read-only
TAB MCP loopback. Its visual intent is a real clickable screenshot associated
with the action sequence. ATF keeps that intent with one text-only native link,
the compact `base64 img` marker and the explicit safe `target="_blank"` /
`rel="noopener"` contract; tests compare the single data URI source and the
opened document, not a duplicated inline image.

The two UI input modes now converge before browser work: `API_GENERATED` is the
fresh artifact created by API setup and `EXISTING` is the read-only artifact
resolved from YAML or `-Datf.ui.report`. `ReportAuditInputsTest` protects this
resolution and `ReportAuditContext` serves both through the same loopback
lifecycle. `UiAuditFlow` now also centralizes lifecycle, action-sequence
ownership, screenshot capture and failure publication for generated-report,
  existing-report and navigation-flow tests. Structural DOM auditors own Tests
  and Overview discovery/expansion, while the API report exploration facade
  delegates structure, filters, duration/responsive checks and request
  evidence to focused inspectors under `flows/inspection`.

The API collection used as a UI precondition is now configurable and reusable:
`UiTestData` owns family keys, API owners and report tags, while
`ApiReportAuditSetup` resolves the collection with deterministic precedence
(`ApiReportSetupFlow` remains a compatibility façade):
`-Datf.ui.api.families` → `-Datf.ui.audit.suite`/suite list →
`@ApiScenarioSetup(families = ...)` → `ui.apiSetup.scenarioFamilies`. The live catalog supports `posts`, `users`,
`requests`, `headers`, `flows`, `negative` and `lifecycle`; the focused UI
contract has passed for both `users,posts` and `requests,headers`. This is
tracked as `UI-CONFIG-01`.

Direcția pentru reutilizare profundă, manifest și teste dinamice este documentată
separat în [`ATF_Ui_Extent_Reusable_Test_Strategy.md`](ATF_Ui_Extent_Reusable_Test_Strategy.md).
Inventarul tehnic al claselor, ownership-ului, dependency direction și
deciziei de modularizare este în
[`ATF_Ui_Extent_Inventory.md`](ATF_Ui_Extent_Inventory.md).
Documentul este `DONE / GATE-CLOSED`: factory-ul dinamic, manifestul,
convergența `ValidationResult` și artifact gate-ul sunt live și verificate.

## API/UI ownership boundary

The API package never owns a browser. `atf.api.tests.api` contains only API
resource/request/lifecycle tests; API artifact setup is owned by
`atf.ui.tests.extent.composition.ApiReportAuditSetup`. Any `Page`, `Locator`, Playwright,
Debug UI, DOM, screenshot or rendered-report assertion belongs in
`atf.ui.tests.extent`. The generated UI audit may call `ApiReportAuditSetup` for its
configurable API precondition, but the API setup is not a separately ordered
test suite and the API input remains read-only.

## Surgical class-refactor checkpoint — 2026-10-16

The two largest orchestration hotspots were split without changing the public
flow/report contracts:

- `ApiReportExplorationFlow` is now a small semantic facade. Structure,
  duration/responsive geometry, filters and request evidence are owned by
  focused inspectors under `atf-tests/src/main/java/atf/ui/tests/extent/flows/inspection/`.
- `UiExtentReportSession` keeps lifecycle ownership, while
  `UiEvidencePublisher` owns the shared action/screenshot/assertion/failure
  publication rules. This prevents the normal, media and failure paths from
  drifting apart and preserves the single inline screenshot contract.

The refactor gate is: preserve the facade method signatures, keep locator
construction in `ReportUiLocators`, run reporting contract tests plus the UI
architecture/lifecycle tests, and re-open the final HTML through ATF Debug UI
before publishing it. Do not split `AtfReportTheme` or the locator facade only
to reduce line count: those classes are cohesive boundaries until a new
semantic owner and contract test exist.

## API evidence status summary

Auditul și backlog-ul exclusiv pentru testele API sunt reflectate în acest
ledger și în README-urile modulelor API. Acest document general ține statusul
transversal și regulile comune; nu duplica aici ToDo-urile API.

| ID | Owner | Closure |
|---|---|---|
| API-JSON-01 | `atf-core` + `atf-tests` + `atf-reporting` | Every standard API publisher emits `REQUEST JSON` and `RESPONSE JSON`. `ApiExecutionResult` retains the effective request. Body-less `GET`/`204`/`304` exchanges use explicit JSON `null`, while every `POST`/`PUT` requires a non-empty valid JSON request and response. `ApiRoute`, `ApiJsonEvidence.writeContractViolations`, `ApiReportAuditSetup` and `MockReportContractAuditor` protect the rule; the dynamic UI contract expands every generated request and deeply reconciles configured YAML response trees. |
| API-COV-01 | `atf-mock` + `atf-tests` | DONE — `MockRoute.key()` includes sorted query identity, so the route matrix owns every YAML route including both search variants. |
| API-NEG-01 | `atf-mock` + `atf-tests` | DONE — `ApiNegativeMatrixTests`, `RequestVariantsApiTests` and their scenario publishers cover malformed writes plus the invalid-query response and report evidence. |
| API-FLOW-01 | `atf-tests` | DONE — POST→LIST→GET and PUT→GET have separate mutation/read-model contracts plus stale-version/broken-self-link guards; POST read-back consumes the mutation response ID. |
| API-ASSERT-01 / API-AUTH-01 / API-HEADER-01 | `atf-validation` + `atf-core` + `atf-tests` | Structural JSON rules, all four auth profiles, header matrix and redacted wire evidence are covered by focused contracts. |
| API-REPORT-01 / API-PARALLEL-01 / API-DELETE-01 / API-DATA-01 | `atf-tests` + `atf-reporting` | Request/evidence and mock reconciliation are reusable; timestamped parallel sessions are tested; cross-family fixture data is centralized; no DELETE route exists in the current product mock contract and coverage fails if one is added without an owner. |

This item is closed only for the standard API report path. A new direct
`startRequest(...).json(...)` integration must use `ApiJsonEvidence.publish`
or add an equivalent contract test; publishing only the response body is not
an acceptable shortcut.

The remaining API engineering queue is intentionally kept in
[`ATF_Java_Improvement_Backlog.md`](ATF_Java_Improvement_Backlog.md), not
duplicated in this cross-cutting ledger. There is no open `MockReportContractAuditor`
refactor: its public facade already delegates route resolution, request-body,
request/response metadata, execution-evidence and deep-JSON checks to focused
typed collaborators while preserving `ValidationResult`. The typed
route/header/media/environment vocabulary, JSON body contract, API/UI boundary
and report evidence are implemented and remain regression guards.

## Repeatable API audit checklist

This is the required sequence for every API report refresh. It is intentionally
explicit so a report cannot be declared current from a partial test selection:

1. Read every test under `atf-tests/src/test/java/atf/api/tests/api/` and every
   scenario under `atf-tests/src/main/java/atf/api/tests/api/`.
2. Reconcile each route with `atf-mock.yaml`: method, path/template, status,
   response body, response headers, auth profile and request headers.
3. Run the configured API setup/report flow (`ApiReportAuditSetup`, normally
   through `ApiExtentReportExploratoryTest`) with an isolated
    `atf.report.config` whose output is inside `.tmp`; do not overwrite or
   close an existing live report.
4. Assert that every request node publishes exactly one `REQUEST JSON` and one
   `RESPONSE JSON`. Body-less exchanges remain explicit JSON `null`.
5. Assert that every `POST` and `PUT` has non-empty valid JSON on both sides;
   reject missing, blank or JSON-`null` bodies.
6. Inspect headers, auth profile/type, query parameters, URL, status, duration,
   retry evidence and contract assertions in the generated report.
7. Open that exact artifact through ATF Debug UI/browser MCP, expand at least
   one GET, one POST and one PUT, and visually verify the JSON and assertion
   disclosures.
8. Copy only the validated artifact to `artifacts/reports/`, update this file
   with any new gap, and keep the full report path/hash in the handoff.

Current closure: the mock route `POST /public/v2/posts` is now covered by both
`PostsApiTests` and `PostsApiScenarios`; all mock `POST`/`PUT` routes are present
in the acceptance report and are protected by the non-null JSON contract.

The first stateful baseline is now executable in
`atf.api.tests.api.flows.ApiRequestFlowsTests` and projected by
`ApiRequestFlowScenarios`: `POST → GET → validate` and `PUT → GET → validate`.
The mutation and read-back contracts are intentionally different: POST/PUT
assert the operation result (`created`, `updated`, `changes`, version), while
GET asserts the persisted read model (links, timestamps, audit/read-model
metadata). `ApiFlowAssertions` additionally proves the cross-request relation
(created id → GET self link; PUT id → GET id and mutation lineage). This is a
hard rule: a flow is not deep merely because both requests return `200`/`201`
and repeat the same JSON paths.

### Stateful API flow — closed baseline

- [x] Keep mutation and read-model contracts separate in fixtures and report
  nodes.
- [x] Add deterministic mock fields that make the transition observable and
  validate them from the YAML-backed response, not from invented test data.
- [x] Add cross-request assertions and render them as `FLOW ASSERTIONS` in the
  read-back request, without creating a fake third HTTP request.
- [x] Add a negative mutation/read-back fixture for stale version and broken
  `links.self`, isolated from the positive flow.
- [x] Add a correlated `POST → LIST/FILTER → GET` collection flow; the read
  request must use the identifier returned by the mutation response.

The first stateful baseline is now executable in
`atf.api.tests.api.flows.ApiRequestFlowsTests` and projected by
`ApiRequestFlowScenarios`: `POST → GET → validate` and `PUT → GET → validate`.
The mutation and read-back contracts are intentionally different: POST/PUT
assert the operation result (`created`, `updated`, `changes`, version), while
GET asserts the persisted read model (links, timestamps, audit/read-model
metadata). `ApiFlowAssertions` additionally proves the cross-request relation
(created id → GET self link; PUT id → GET id and mutation lineage). This is a
hard rule: a flow is not deep merely because both requests return `200`/`201`
and repeat the same JSON paths.

## Surgical refactor status — 2026-10-16

The large classes were reviewed by responsibility and public-contract risk,
not by blind line deletion. The following seams are implemented and covered
by the isolated UI exploration run:

- [x] `ApiReportExplorationFlow` is a stable facade; structure,
  category/failure navigation, duration geometry and request evidence are
  separate inspectors returning the original typed snapshots.
- [x] `ExistingReportUiFlow` is a facade over navigation, visual evidence and
  actionable screenshot inspectors.
- [x] `PlaywrightActionSequence` is a recorder facade over locator and browser
  action adapters; the effective locator contract is unchanged.
- [x] Architecture and exploratory UI tests pass after extraction; the fresh
  artifact was opened through ATF Debug UI and its Screen validation zones are
  contiguous, aligned and free of decorative separators/gaps.
- [x] `ReportUiLocators` remains the canonical selector boundary and delegates
  semantic category, evidence and failure selectors to dedicated locator
  classes. Locator-contract and UI-layer architecture tests guard the seam.
- [ ] `AtfReportTheme` remains a CSS composition hotspot; extract theme blocks
  only with a golden HTML/render comparison.
- [ ] `UiExtentReportSession` remains a lifecycle API with nested scenario and
  phase builders; extract those builders only after a snapshot test proves
  identical node order, duration lineage and evidence publication.

Current evidence is intentionally not pinned to a historical filename or
hash. A report is closure evidence only when the exact fresh artifact from the
current synchronized build is linked in the handoff after ATF Debug UI/browser
MCP inspection.

## Verified backlog increment — 2026-10-16

The synchronized staging build closed the API route-key collision and the
mutation correlation gap. `MockRoute.key()` now carries sorted query identity;
`ApiFlowAssertions.resourceId(...)` drives the POST flow read-back; and the
invalid search query is published as a dedicated YAML-backed JSON contract.
`ApiReportContractValidation` remains the shared API/UI composition boundary
for JSON, mock response trees, request metadata, headers and execution evidence.

The current paired artifacts are:

- API: `artifacts/reports/api-atf-extent-report_2026-10-16_19-46-21-599.html`
  (SHA-256 `2DF990AECAB7C8029C9DCF0364B9F9C9854D3AFD9E329101024523C6A298FCAD`)
- UI: `artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_19-46-29-161.html`
  (SHA-256 `C63F56400B161BEB8CE3923BCCA86510334034F08EB1874A67DA873BE0DC68C2`)

The UI artifact is the authoritative visual closure evidence: it was served by
the official ATF Debug UI and inspected through browser MCP. The API artifact
is the paired report from the same synchronized staging area
`.tmp/top4-live-merge-20261016-194500`; its GET/POST/PUT JSON and transport
evidence were opened and inspected through ATF Debug. Older
17-04/17-16/17-26/17-45 paths remain historical run evidence, not the current
status pointer.

The synchronized run publishes all configured API families for the audit, while
the current UI artifact contains contiguous `Screen validation` sibling zones
and the native inline `📷 base64 img` screenshot link. The paired API artifact
contains the deep POST/PUT request/response JSON and transport metadata used by
the mock-YAML reconciliation. The surgical and backlog-depth filenames from
earlier iterations are intentionally retained only as historical traceability;
the 19-35 pair above is the current closure pointer.

The precedence smoke proof is separately published at
`artifacts/reports/api-atf-extent-report_2026-10-16_19-52-depth-smoke.html` and
`artifacts/reports/ui/ui-atf-extent-report_users-posts_2026-10-16_19-52-depth-smoke.html`.
It was generated with `-Datf.ui.audit.suite=users-posts` and
`-Datf.ui.audit.depth=smoke`; the explicit JVM depth selected only SETUP,
TESTS, REQUEST_CONTRACT and OVERVIEW despite the deep validators in YAML. The
exact smoke HTML files were opened and expanded in official ATF Debug
launchers after publication.

The API increment is closed: query semantics, the support namespace and
cross-family fixture data are implemented and architecture-tested. The
reporting increment now publishes one immutable `AtfReportSemanticSnapshot`
from both Extent and fallback, with a pre-render population guard. The typed
API bootstrap is acceptance-by-default and live-opt-in. The UI browser matrix
proves empty/failure, one/many categories, GET/POST/PUT, normal/narrow
viewports and mock-backed request contracts.

### Latest surgical closure — Fails layout and selector boundary

The fresh failure artifact
`artifacts/reports/api-atf-extent-report_backlog-fails-layout-final_2026-10-16_20-03-35-065.html`
was opened through ATF Debug UI/browser MCP. It contains two real failure rows;
the rendered Fails projection keeps the list on the left and the diagnostic
detail on the right, on the same row, with the primary/secondary cause chain
and `Open in Tests` link visible. The promoted API artifact SHA-256 is
`704DEF32265D55029AC707DAC188855942D915256B43D238459A991F2E2F9C8C`.
Paired UI audit artifact:
`artifacts/reports/ui/ui-atf-extent-report_backlog-fails-layout-final_2026-10-16_20-03-39-042.html`
(SHA-256 `688FC00CEE463EE65E5BF4D492140EC7720BC2D8E032467AD7A90E9B365E2131`).

The selector boundary is closed for the reusable audit layers: hash-based tab
navigation and the runtime root are owned by `ReportUiLocators`; the focused
architecture/locator guard passes 6/6 tests. Duplicate source trees discovered
during the clean-build audit were quarantined under the active `.tmp` staging
workspace and removed from live source. Remaining open work is limited to the
explicit architecture/design queue in the table below.

`API-DESIGN-01` is also closed: `HttpMethod`, `HttpHeader` and `MediaTypes`
now own the mock defaults, YAML fallback, write-method detection and rendered
mock-evidence comparisons. `MediaTypes.isJson(...)` accepts vendor JSON
variants without reintroducing a raw media literal. The focused mock and
auditor tests are green.

## Active gaps

### P1 — boundaries and composition

| ID | Owner | Current gap | Closure evidence |
|---|---|---|---|
| UI-SECTION-ARCH-01 | `atf-tests` | The six section modules return `SectionAuditResult`, but check ownership/execution is not uniform, Extent publication remains inside modules, and the global rule path is still a compatibility seam. | `ATF_Ui_Extent_Section_Architecture_Feedback.md`; close only with a full `sections × checks × journey` matrix, independent publisher and fresh ATF Debug/browser MCP artifact. |
| UI-EXTENT-INSPECTOR-01 | `atf-tests` | The remaining DOM inspectors for request evidence, structure/filter and historical Categories/Evidence paths must converge to `extractor → snapshot → pure validator → ValidationResult`. | `ATF_Ui_Exploratory_Feedback.md` and `ATF_Ui_Extent_Inventory.md`; no selector or expected-value ownership may move into tests. |

### P2 — UI contract and semantic metadata

| ID | Owner | Current gap | Closure evidence |
|---|---|---|---|
| `AtfReportTheme` | `atf-reporting` | CSS composition hotspot; extracția este permisă doar pe blocuri semantice, fără schimbarea ordinii CSS sau a HTML-ului observabil. | Golden HTML/render comparison plus focused theme contract tests. |
| `UiExtentReportSession` | `atf-reporting` | Lifecycle API cu scenario/phase builders nested; extracția este permisă numai după snapshot contract pentru node order, duration lineage și evidence publication. | Snapshot comparison, lifecycle contract tests and fresh ATF Debug/browser MCP artifact. |
| — | — | UI-15, UI-23, D-01 și D-03 sunt contracte închise și rămân doar gardieni de regresie. | Contractele sunt documentate în README-urile ownerilor și în feedback-ul specializat. |

### P3 — hardening

| ID | Owner | Current gap | Closure evidence |
|---|---|---|---|
| — | — | Nu există ToDo P3 activ; hardening-ul LOC-03, UI-18 și OPS-UI-01 este închis și rămâne guard de regresie. | Reopen only on a new failing contract or a new observable artifact defect. |

## Required process gate

Every implementation increment follows the same sequence:

```text
read live source and owner README
  → create one isolated staging workspace
  → write the failing regression test
  → implement the smallest owner-scoped change
  → run unit/component/integration tests
  → generate a fresh artifact when observable behaviour changes
  → inspect the exact artifact in TAB MCP (clicks, disclosures, navigation, styles)
  → synchronize selectively back to live
  → update only the remaining feedback items
```

Building from another directory does not remove the live-sync gate. A build
must start from a fresh sync with the live source, and its artifact must be
traceable to that synchronized revision. Existing processes are not stopped,
generated output is not copied into source trees, and a failing test is fixed
in code rather than weakened.

## Evidence rule

An item is closed only when the implementation, the focused test and the
fresh artifact agree. A historical report, a source-only assertion, or a
green test obtained after changing the requirement is not closure evidence.

The detailed reporting contract is in
[`atf-reporting/README.md`](../../atf-reporting/README.md); UI execution and
Playwright ownership are in [`atf-ui/README.md`](../../atf-ui/README.md) and
[`atf-tests/README.md`](../../atf-tests/README.md).

## UI locator architecture rule

New UI tests must follow the explicit boundary documented by `atf-ui` and
`atf-tests`: selector strings live in locator/page-object classes, Playwright
interactions live in action adapters, scenario order lives in flows, and DOM
observations live in auditors/assertions. Direct `page.locator`, `getByRole`
or `getByText` calls are not allowed in new Gherkin methods. Direct selector
construction is forbidden in the UI test/flow/auditor layers and is
protected by `UiSelectorBoundaryArchitectureTest` and
`UiTestLayerArchitectureTest`. The concrete boundary is
`atf.ui.tests.extent.locators.ReportUiLocators`; it owns report selectors and exposes
semantic methods for action, flow and validation layers.

## Evidence location

Fresh test output and generated artifacts are evidence for the current change,
not permanent backlog content. Keep the artifact under `artifacts/reports/`
and link it from the owning test or module README only when the evidence is
needed to explain a stable contract or close a specific ToDo.

## Surgical UI/API evidence checkpoint — 2026-10-16

The shared validation display is now enforced at the rendered-report boundary:

1. API request evidence remains the semantic reference: one details column,
   contiguous request/response/execution disclosures and rounded corners only
   on the outer contour; table grid lines remain internal data delimiters.
2. UI `Screen validation` now applies the same contract to Playwright action
   sequence, audit observations and assertion evidence. Validation table cells
   have no residual horizontal padding, all three disclosures are `width:100%`,
   and their y positions are consecutive.
3. The fresh ATF Debug measurement for the final UI artifact reported equal
   summary geometry for all three zones: `height=33.9946px`,
   `width=561.2365px`, `margin=0`, `padding=9px 12px`, and zero top/bottom
   summary borders. `scrollWidth=clientWidth=1280`; no decorative horizontal
   separator remains between the three zones.
4. The 10-rule dynamic audit artifact is historical evidence for the factory;
   the current visual closure pointer is the exact 19-11 UI artifact listed
   above.

Latest increment evidence: `CrossTabReconciler` now reconciles owner sets,
Overview class projection and Category links through one `ValidationResult`,
and `CategoryFilterAuditor` has focused zero/one/many contract tests. This is
not a closure of the full browser fixture matrix.

Remaining ToDo: keep the geometry contract covered when a new UI evidence type
is introduced; do not add a one-off CSS exception outside the shared validation
selector boundary.
