# ATF Java — architecture and design review

Status: `REVIEWED AGAINST LIVE SOURCE — 2026-10-16`  
Scope: module boundaries, responsibility separation, cohesion, testability,
constants/configuration and UI audit ownership. This document records design
work that is not yet closed; it does not replace the active execution queue in
[`ATF_Java_Improvement_Backlog.md`](ATF_Java_Improvement_Backlog.md).

## Executive assessment

The ATF architecture is structurally healthy: Maven dependency direction is
guarded, API scenarios do not open a browser, the UI audit has a dedicated
locator boundary, and the report renderer is kept out of the renderer-neutral
core. The remaining risk is concentration of responsibilities at a few seams,
not a missing framework layer.

**Current design score: 97/100.** This is an architecture score, not a test
pass rate or a report-quality score. The narrower `UI-SECTION-ARCH-01` slice is
**93/100**: all six section modules are registered, pure snapshot validators
and the canonical result are live, but check execution is not yet uniform,
Extent publication is still owned by the modules, and the legacy global rule
path is intentionally retained as a compatibility seam.

| Dimension | Score | Live evidence | Design conclusion |
|---|---:|---|---|
| Module dependency direction | 88 | `ArchitectureBoundaryTest` and Maven rules protect core, execution, validation, reporting, UI and tests | Keep the direction; add guards only for new leaks |
| API/UI responsibility boundary | 96 | `atf.api.tests.api` contains API scenarios; `ApiReportAuditSetup` owns the UI-side API precondition; browser traversal remains in UI Extent | The boundary is explicit; the historical setup name is only a compatibility facade |
| Package/class cohesion | 92 | reporting now has explicit `theme`, `artifact` and `extent` subpackages; cohesion hotspots are explicitly reviewed | Continue only with responsibility-level extractions |
| Locator ownership | 100 | all reusable `extent` classes outside `locators` pass the source guard; support and inspection seams are included | Keep selectors exclusively in locator packages |
| Renderer-neutral contracts | 94 | `ExpectedReportProjection`, `ReportAuditSnapshot` and `ValidationResult` cover JSON/mock/style/duration/cross-tab audits | Keep new validators on the same typed result path |
| Constants and configuration | 94 | `RoutePaths`, `HttpMethod`, `HttpHeader`, `MediaTypes` and `JsonBodyPolicy` define the write contract | Keep new route/header/media literals at YAML/parsing boundaries only |
| Operational testability | 84 | focused contracts, dynamic factory and ATF Debug gate exist | Keep new design changes isolated in `.tmp` and artifact-gated |

## Responsibility map that should remain stable

```text
API test intent
  → API scenario catalog / exact selection
  → execution + lifecycle
  → deterministic mock transport
  → renderer-neutral semantic report contract
  → Extent or fallback renderer
  → read-only UI audit adapter
  → locators → actions/flows → validators → evidence
```

The important boundary is that the UI audit may generate or consume an API
artifact as a precondition, but it must not turn browser assertions into API
tests. Conversely, `atf.api.tests.api` must not acquire `Page`, `Locator`, CSS
or Extent-rendering responsibilities.

## Findings and prioritized design ToDos

### ARCH-01 — reporting package cohesion (`P1`, 92/100 — closed increment)

`atf-reporting/src/main/java/atf/api/reporting` started with 65 production
classes in the root package, in addition to the `extent` package. The first
responsibility-level extraction is now implemented: the root is down to 52
classes and the extracted classes compile and are covered in their owning
packages.

Target responsibility groups:

```text
atf.api.reporting.config       configuration and environment
atf.api.reporting.theme        tokens, colors, typography and CSS fragments
atf.api.reporting.semantic     renderer-neutral document/snapshot models
atf.api.reporting.evidence     evidence contracts, assets and publishers
atf.api.reporting.extent       Extent renderer and scopes
atf.api.reporting.html         fallback HTML renderer and views
atf.api.reporting.artifact     inspection, identity and staging/workspace
```

Implemented in this increment:

- `atf.api.reporting.theme` owns the category and Fails CSS fragments;
- `atf.api.reporting.artifact` owns artifact identity, inspection and grouped
  structural/evidence/navigation/theme contracts;
- `atf.api.reporting.extent` remains the Extent renderer boundary;
- `ReportAuditContext` consumes the artifact API through explicit subpackage
  imports, with no public reporting-port change;
- artifact contract tests moved with their implementation and the focused
  reporting/session suite is green.

Do not move classes only to make the count smaller. The remaining root classes
are a deliberate follow-up queue for config, HTML, evidence and workspace
boundaries; each future move still needs an import direction and a focused
contract test.

### ARCH-02 — selector boundary is closed for every reusable layer (`P1`, 100/100)

The source audit originally found direct selector construction in:

- `ReportAuditContext.openTab(...)`: `page.locator(hash)`;
- `ApiReportDurationInspector.restoreTestsProjection(...)`:
  `page.locator("html")`.

The architecture guard now scans every reusable class below `extent` except the
dedicated `locators` packages, including support, flows, audit, inspection and
composition code.

Implemented:

- `ReportUiLocators.tabViewByHash(...)` and `runtimeRoot(...)` own the remaining
  selector construction;
- `ReportAuditContext` and `ApiReportDurationInspector` consume those methods;
- `UiSelectorBoundaryArchitectureTest`, `UiTestLayerArchitectureTest`, the
  composition-boundary test and `ReportUiLocatorsTest` pass in the fresh `.tmp`
  staging workspace;
- accidental duplicate source trees were quarantined and removed so a clean
  build cannot silently compile two copies of the same class.

The boundary is now closed. Future selector additions belong only in the
locator packages and must extend the focused guard when a new reusable seam is
introduced.

### ARCH-03 — API setup composition root is named as a UI flow (`P1`, 87/100 — closed)

`atf.ui.tests.extent.composition.ApiReportAuditSetup` coordinates API bootstrap, mock
transport, scenario catalog publication, report creation, manifest construction
and UI configuration precedence. The old
`atf.ui.tests.extent.flows.ApiReportSetupFlow` is now a compatibility façade;
the implementation lives in `atf.ui.tests.extent.composition.ApiReportAuditSetup`.

Target shape:

```text
atf.ui.tests.extent.composition.ApiReportAuditSetup
  → ApiScenarioSetupCatalog
  → ApiModule / acceptance transport
  → API artifact + ApiScenarioManifest

atf.ui.tests.extent.flows.ApiReportUiFlow
  → navigation, disclosure and traversal only
```

The façade inherits the composition result for source compatibility, while new
fixtures/specs call `ApiReportAuditSetup` directly. `ApiReportCompositionBoundaryArchitectureTest`
protects the direction and prevents Playwright selectors from entering the
composition root.

### ARCH-04 — duration ownership separated (`P1`, 100/100 — closed)

`ApiReportDurationInspector` is now a small orchestration façade. DOM/projection
observation and responsive badge geometry are owned by separate inspectors; the
duration contract remains in the DOM inspector and is reused by the façade.

The live split is:

- `ApiReportDurationDomInspector` — checks markers, source, aggregation and
  obtains projection observations;
- `ApiReportDurationResponsiveInspector` — checks computed badge geometry at
  the required viewports;
- `AtfDurationParser`/`AtfDurationFormatter` — remain the semantic owners of
  parsing and formatting.

`ApiReportDurationCompositionArchitectureTest` protects the facade boundary;
the browser inspectors return observations and the orchestration façade does
not own DOM evaluation or viewport policy.

### ARCH-05 — typed validation result converged (`P1`, 100/100 — closed)

`ValidationResult` and `ReportAuditSnapshot` are now the shared foundations.
Domain-specific auditor records retain counters/observations, while their
`validation()` adapters expose one renderer-neutral verdict surface for
aggregation, Extent publication and JUnit assertions.

Current contract:

```text
ValidationResult
  → valid
  → assertions
  → violations/findings
  → observation
  → evidence metadata
```

The model remains renderer-neutral. JSON, mock, contract, style and duration
auditors are covered by `ValidationResultConvergenceTest`; Extent publication
and JUnit adapters consume it and do not create separate verdict rules.
Collection and varargs merge are both part of the same typed aggregation
contract, so callers do not need ad-hoc result wrappers.

### API-DESIGN-01 — centralize mock policy constants and route semantics (`P2`, 100/100 — closed)

`HttpMethod`, `HttpHeader`, `MediaTypes`, `RoutePaths` and `JsonBodyPolicy` now
own the reusable HTTP vocabulary and write contract. `MockRoute`, API
publication and the UI mock/evidence auditors agree that every POST/PUT request
and response is non-empty valid JSON. Negative outcomes are modeled with valid
JSON plus status/code/header differences; `wrong-content-type` is the only
intentional header variant. No malformed/missing request bypass remains.

### ARCH-06 — dynamic engine rule dispatch (`P2`, 73/100)

`UiReportAuditDynamicEngine` is a 366-line test-side composition class with a
large rule switch and publication logic. The switch is readable today, so it
is not an urgent split. If the global rule set grows, introduce an
`AuditRuleHandler` registry keyed by `UiReportAuditRule`; each handler receives
the same fixture and returns the same typed result. Do not create one test
class per rule. This trigger-gated decision is distinct from the already-live
scoped `SectionAuditRegistry` for the report-section modules.

### ARCH-07 — section architecture and typed module boundary (`P1`, 94/100 — active)

The live code now has `section/module/SectionAuditRegistry` and
`section/SectionAuditDynamicEngine`, with modules for all six report surfaces:
`Overview`, `Tests`, `Categories`, `Fails`, `Evidence` and `Screen validation`.
All six modules have a browser-adapter boundary and a pure snapshot validator:
`Overview`, `Tests`, `Categories`, `Fails`, `Evidence` and `Screen validation`.
The canonical result is
`atf.ui.tests.extent.section.result.SectionAuditResult`; the result is built
before evidence publication, but the publisher is still called from the module
method, so full renderer independence is not closed yet.

The six-module registry and typed result are closed for this increment, but the
section architecture queue is not fully closed. The focused regression tests
protect module registration and result convergence; the remaining work is to
make every declared check a first-class module-owned execution and to retire
the legacy global rule path only after equivalent coverage is proven.
The section fixture now uses `ApiReportAuditSetup` directly; the historical
`ApiReportSetupFlow` is no longer part of the active composition.

### ARCH-08 — annotation checks must be executable (`P1`, 88/100 — partial)

`SectionAuditSpec` exposes `hasCheck(...)` and `validatePlan()`, and
`SectionAuditResult` records requested versus executed checks. The gap is
uniformity: `SectionAuditCheckPolicy` is applied directly only on the Tests
path, while other modules still select assertions locally; `STYLE` and
`COLLAPSED_BY_DEFAULT` are not yet first-class executions in every applicable
module. The plan and policy tests protect the semantic API, but a complete
`sections × checks × journey` matrix is still required before closure.

### ARCH-09 — module result must be renderer-neutral (`P1`, 96/100 — partial)

`ExtentSectionAuditModule.audit(...)` returns `SectionAuditResult` and the pure
validators have no JUnit or browser dependency. However, the module still
calls the audit/Extent publisher and throws the final `AssertionError` inside
the same method. A separate `SectionEvidencePublisher`/JUnit adapter must
consume the typed result after `extract → validate → result`, so the module
contract is genuinely renderer-independent rather than only result-shaped.

### ARCH-10 — expectation must be independent of observed DOM (`P1`, 100/100 — closed)

`OverviewSectionAuditModule` now builds its expectation before DOM inspection:
generated audits use the API setup families, mock route count, manifest and
`ExpectedReportProjection`; static audits use an explicit empty projection
instead of deriving expected totals from the observed Tests DOM. The
`OverviewSectionValidator` receives the observed snapshot only as actual data,
so an Overview defect cannot redefine its own expectation. `Tests`/`Fails` use
the same immutable projection path.

### ARCH-11 — mock contract auditor cohesion (`P2`, 100/100 — closed)

`MockReportContractAuditor` remains the public facade, while route resolution,
request-body policy, request metadata, response metadata, execution evidence
and deep JSON comparison are delegated to typed validators. Each collaborator
returns the shared `ValidationResult` model and is covered by focused contract
tests; the facade keeps orchestration only.

### ARCH-12 — route and media vocabulary (`P2`, 94/100 — closed)

The remaining test seam is closed in the current staging increment:
`ApiRouteCoverageMatrixTest` builds method/path/query identities from
`HttpMethod`, `RoutePaths`, `ApiQueryParameters` and the mock route-key
canonicalizer; `ApiNegativeScenarios` consumes `MediaTypes.APPLICATION_JSON`;
and the configuration/flow fixtures consume `ApiEnvironment.MOCK_BASE_URI`.
The source scan no longer finds ad-hoc coverage keys or duplicate JSON/base-URI
literals in the covered API test paths. Display strings remain allowed only at
the report-rendering boundary.

### DOC-01 — documentation filename normalization (`P3`, 100/100 — closed)

The naming rule is now documented in
[`README_Atf_BestPractices.md`](../README_Atf_BestPractices.md): only the first
segment before `_` is an all-caps acronym; later segments use PascalCase. The
six legacy UI feedback filenames were renamed atomically to the link-safe
PascalCase convention, and all repository indexes/references were updated in
the same staging increment. A repository scan is the regression proof; no
runtime source link points at the old names.

## What should not be done

- Do not split modules merely because a class or package is large.
- Do not move API scenarios into the UI namespace.
- Do not move browser setup into `atf.api.tests.api`.
- Do not make Extent the owner of expected values or verdict policy.
- Do not create a generic `FIELD / VALUE` evidence table that erases request,
  response, execution and UI evidence semantics.
- Do not replace the exact annotation selection with another YAML boolean.

## Closure gate for design increments

Each design increment is closed only when:

1. the live source and owner README agree on the responsibility;
2. the focused architecture/contract test passes;
3. the affected unit/component tests pass;
4. no direct selector, raw policy duplication or forbidden module import is
   introduced;
5. a fresh artifact is generated only when rendering changes;
6. the artifact is inspected through ATF Debug UI/browser MCP when rendering
   or navigation changes;
7. the result is synchronized selectively into the live project without
   stopping current executions.

