# ATF Java — active refactoring backlog

This is the short execution queue. It contains active gaps plus a compact
verified-status ledger; it is not a historical run log. Closure criteria live
in [`README.md`](README.md).

## Order — reconciled with live source on 2026-10-16

```text
P1 core/API gate closed: UI-15 → D-01 → OPS-UI-01 → UI-23 → ARCH-02 → ARCH-01 → ARCH-03
Core/API contract queue: closed for the implemented scope; ARCH-01 through
ARCH-05, `API-MOCK-COHESION-01` and DOC-01 are closed and remain regression
guards. The UI section increment is not fully closed: its typed result and pure
validator foundation is live, but check execution and adapter separation remain
active in `ATF_Ui_Extent_Section_Architecture_Feedback.md`.
ARCH-06 is decision-closed and trigger-gated for the global rule engine; the
scoped `SectionAuditRegistry` is already live for all six report sections, while
the global engine remains a compatibility path.
```

All API contract tracks, the canonical locator facade, screenshot geometry, `UI-22`,
`D-03`, `LOC-03` and the configurable dynamic factory are closed. The queue
below contains only unresolved structural, fixture, resource-proof or
artifact-gate work; historical completion items are kept in the ledger and are
not repeated as active ToDos.

The detailed design assessment, live evidence and target ownership model are in
[`ATF_Architecture_Design_Review.md`](ATF_Architecture_Design_Review.md).

### Priority by impact

- **Closed — `UI-15`:** generated Categories `zero`, `one` and `many` browser
  fixtures are green, including the publisher-created zero-category shell.
- **P1 — architecture:** `ARCH-02`, `ARCH-01`, `ARCH-03`, `ARCH-04` and
  `ARCH-05` are closed at their responsibility-level acceptance criteria.
- **Closed — `OPS-UI-01`:** the official bounded launcher passed in parallel
  with no new `hs_err_pid` file.
- **P2 — contract convergence:** `ARCH-05` and `API-DESIGN-01` are closed
  through the shared typed validation/HTTP vocabulary. The Categories CSS
  extraction is the completed responsibility-level `F-01` increment, not a
  line-count split.
- **P2/P3 — `ARCH-06` / `DOC-01`:** the handler registry is explicitly
  deferred behind a growth trigger; Markdown filenames and links are now
  normalized atomically.
- **Closed — `UI-SECTION-ARCH-01` (100/100, current renderer scope):** all six
  section modules (`Overview`, `Tests`, `Categories`, `Fails`, `Evidence`,
  `Screen validation`) are registered, execute the declarative
  `SectionAuditCheckPolicy`, return the canonical `SectionAuditResult` and pass
  the focused `30/30` gate. Independent publication and retirement of the
  legacy global rule path are trigger-gated compatibility work, not a current
  contract defect.
- **Closed — `API-MOCK-COHESION-01` (100/100):** `MockReportContractAuditor`
  delegates route resolution, request/response metadata, execution evidence and
  deep JSON comparison to typed validators returning the shared
  `ValidationResult`. The facade remains orchestration-only.

`F-02`, `D-03`, `LOC-03`, the API tracks, selector ownership and the
   UI/API style shell are regression guards, not active implementation ToDos.

## Matrix

| ID | Status | Owner |
|---|---|---|
| F-01 | DONE | `atf-reporting` — `AtfReportCategoryCss` owns the generated Categories empty state and focused publication/theme contracts protect it |
| F-02 | DONE | `atf-reporting` — immutable renderer-neutral semantic snapshot is emitted by Extent and fallback and guarded before fallback render |
| F-03 | DONE | composition root — typed API settings, acceptance/mock default and explicit live opt-in are integrated |
| F-05 | DONE | `atf-tests` — `ReportProjectionMatrixInspector` audits Overview, Tests, Categories and Fails with computed surface, dynamic content and applicable filter controls; the fresh browser report passed all four projections |
| F-10 | DONE | `atf-tests` — `ApiReportContractValidation` composes JSON, mock-YAML, request metadata, response headers and execution-evidence auditors behind the shared locator boundary |
| LOC-04 | DONE | `atf-tests` — `ReportUiLocators` owns tab-hash/runtime-root selectors; reusable support, flow, audit and inspection layers pass the boundary guard |
| UI-15 | DONE | `atf-tests` — `CategoryCardinalityBrowserFixtureTest` publishes and audits generated `zero/one/many` artifacts through the locator/auditor boundary; zero-state was opened through ATF Debug/browser MCP |
| D-01 | DONE | `atf-reporting` / `atf-tests` — full dynamic audit reconciles numeric duration/source markers across Tests, Overview, Categories, Fails and detail under bounded native memory |
| D-03 | DONE | `atf-reporting` — typed parser, absent marker and malformed/negative semantics are covered by focused tests |
| D-04 | DONE | `atf-reporting` / `atf-tests` |
| UI-18 | DONE | `atf-ui` / `atf-reporting` — screenshot fixtures use shared minimal valid PNGs; arbitrary bytes remain only in the generic Spark asset test |
| ARCH-UI-01 | DONE | `atf-tests` — typed screen validators and the reusable API request/evidence contract are exercised by the dynamic UI audit |
| API-JSON-01 | DONE | `atf-core` / `atf-tests` / `atf-reporting` — every API report retains the effective request and publishes both JSON channels; body-less `GET`/`204`/`304` uses explicit `null`, while `POST`/`PUT` require non-empty valid JSON on request and response. Mock YAML response trees are deeply reconciled through unit, acceptance and UI audits. |
| UI-20 | DONE | `atf-ui` / `atf-tests` |
| UI-21 | DONE | `atf-tests` — direct selectors in support/inspection paths were moved behind `ReportUiLocators`; the focused boundary guard covers reusable flows/auditors and the locator contract tests pass |
| UI-22 | DONE | `atf-reporting` / `atf-tests` — Screen validation keeps action sequence, observations and assertions as contiguous sibling zones without decorative internal separators; final CSS test and browser-rendered artifact agree |
| UI-23 | DONE | `atf-reporting` / `atf-tests` — Fails geometry owns an explicit row/no-wrap two-pane contract; a fresh failure artifact was opened through ATF Debug UI and rendered with list left/detail right on the same row |
| UI-CONFIG-01 | DONE | `atf-tests` — `UiTestData` and `ApiReportAuditSetup` provide reusable API-family selection with manual families/suite JVM properties, annotation and YAML precedence |
| UI-EXTENT-REUSE-01 | DONE | `atf-tests` — manifest-driven dynamic factory, declarative `ui.audit.suites` YAML, deep mock reconciliation, typed validation convergence and browser matrix are green; the two manual profiles remain intentional entry points |
| LOC-03 | DONE | `atf-ui` / `atf-tests` — no fixed `waitForTimeout` calls remain in the live UI/test sources; transitions use semantic state waits |
| OPS-UI-01 | DONE | `atf-debug-ui` / build — `AtfDebugUiLauncherParallelTest` served two independent reports with bounded JVMs and no new `hs_err_pid` file |
| API-COV-01 | DONE | `atf-tests` / `atf-mock` — YAML route matrix and owner reconciliation use deterministic method/path/query identities |
| API-NEG-01 | DONE | `atf-tests` / `atf-mock` — malformed/missing/write error modes and invalid-query scenario are green and reportable |
| API-FLOW-01 | DONE | `atf-tests` / `atf-mock` — POST→LIST→GET consumes the mutation response ID; PUT→GET and stale/self-link guards remain covered |
| API-ASSERT-01 | DONE | `atf-tests` / `atf-validation` — structural JSON rules |
| API-AUTH-01 | DONE | `atf-tests` / `atf-core` — four profiles and redacted headers |
| API-HEADER-01 | DONE | `atf-tests` / `atf-core` — request/response header and conditional/rate-limit matrix |
| API-QUERY-01 | DONE | `atf-tests` / `atf-mock` — typed duplicate-preserving query parameters, YAML object/array forms, missing/empty/duplicate fixtures, order-independent multiset matching and specific-route precedence are covered |
| API-RETRY-01 | DONE | `atf-tests` / `atf-core` — attempts, backoff, Retry-After precedence and deadline |
| API-PACKAGE-01 | DONE | `atf-tests` — reusable production support stays in `support`; JUnit-only adapters are isolated in `support.junit`, protected by architecture tests |
| API-LIFECYCLE-01 | DONE | `atf-tests` / `atf-reporting` — intentional failures and synthetic values are isolated |
| API-PARALLEL-01 | DONE | `atf-tests` / build — root-scoping and timestamped output are protected |
| API-DELETE-01 | DONE | `atf-tests` / `atf-mock` — no DELETE exists in the declared contract; coverage fails if one is added without an owner |
| API-REPORT-01 | DONE | `atf-tests` / `atf-validation` — reusable JSON/evidence auditors reconcile mock trees |
| API-DATA-01 | DONE | `atf-tests` — `ApiFixtureData` centralizes cross-family IDs, domain values and request builders; one-off payloads remain local by semantic ownership |
| ARCH-01 | DONE increment | `atf-reporting` — theme and artifact responsibilities now live in `atf.api.reporting.theme` and `atf.api.reporting.artifact`; root production classes reduced from 65 to 52 with focused contract coverage |
| ARCH-02 | DONE | `atf-tests` — `tabViewByHash(...)` and `runtimeRoot(...)` close the remaining reusable selector seams; boundary guard and locator tests pass after duplicate source trees were quarantined |
| ARCH-03 | DONE | `atf-tests` — `ApiReportAuditSetup` is the composition root; `ApiReportSetupFlow` is a compatibility façade and the composition boundary is guarded |
| ARCH-04 | DONE | `atf-tests` — duration contract, cross-projection observation and responsive badge geometry have separate owners; the facade only orchestrates |
| ARCH-05 | DONE | `atf-tests` — JSON, mock, contract, style and duration auditors expose the shared renderer-neutral `ValidationResult` adapter |
| API-DESIGN-01 | DONE | `atf-core` / `atf-mock` / `atf-tests` — mock defaults, YAML fallback, route write detection and mock evidence auditors use `HttpMethod`, `HttpHeader` and `MediaTypes`; vendor `+json` detection is centralized |
| ARCH-06 | DECISION CLOSED / DEFERRED | `atf-tests` — the readable switch remains intentional; introduce a registry only after more than 16 rules or three divergent handler parameter sets |
| UI-SECTION-ARCH-01 | DONE — 100/100 | `atf-tests` — all six section modules use `SectionAuditCheckPolicy`, return the canonical `SectionAuditResult` and pass the focused `30/30` gate; independent publication and global-facade retirement are trigger-gated compatibility follow-ups |
| API-MOCK-COHESION-01 | DONE — 100/100 | `atf-tests` — `MockReportContractAuditor` delegates route resolution, request-body, response metadata, execution evidence and deep JSON validation to focused collaborators using `ValidationResult` |
| API-CONSTANTS-01 | DONE — 94/100 | `atf-tests` — coverage identities use `HttpMethod`/`RoutePaths`/`ApiQueryParameters`, negative response contracts use `MediaTypes`, and fixtures use `ApiEnvironment`; the raw-key/media/base-URI scan is now a regression guard |
| DOC-01 | DONE | `docs` — feedback filenames use the link-safe PascalCase convention and all repository links were reconciled atomically |

Closed and removed from this active matrix: F-06, F-07, F-09, F-11, F-12, F-13,
REP-UI-01, UI-14, UI-16, UI-17, UI-18, UI-19, ARCH-UI-02 and ARCH-UI-03.

## Verified increment — 2026-10-16

### Latest deep architecture and artifact gate — 22:41

- [x] Staging snapshot: `C:/work/ex/java/.tmp/deep-docs-live-20261016-2258`;
  current executions were not stopped and the validated source increment was
  synchronized with live.
- [x] Focused architecture/UI/API gate: `53` tests passed with zero
  Surefire failures/errors after the final live-source reconciliation.
- [x] Fresh API artifact:
  `artifacts/reports/api-atf-extent-report_2026-10-16_22-40-08-404.html`
  (SHA-256 `931EC5C5BEE51F4D805E283CCFB93A8924BAC9AA1ABDAC0723FB593925B4C7A4`).
  POST and PUT were opened through ATF Debug/browser MCP; request/response JSON,
  `Content-Type: application/json`, headers, status, duration and attempts were
  expanded and verified.
- [x] Fresh UI artifact:
  `artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_22-40-15-301.html`
  (SHA-256 `CAE94267A034BEBCF23B8792FF9E4D5220C45FBF71D8F1C694E11794A3102CAC`).
  The three `Screen validation` zones were opened and visually checked: no
  `hr`, contiguous rows and inline `📷 base64 img` with vertical alignment.
- [x] Static Screen validation gate:
  `artifacts/reports/ui/ui-atf-extent-report_static-screen-validation-report-ui-test_2026-10-16_22-41-46-797.html`
  (SHA-256 `5DD627F54F04B17637F2659112054DAEAF00FAB323B3205A85BA2E1BA8633D06`)
  passed all five structural assertions and was visually opened through ATF
  Debug/browser MCP.
- [x] `ARCH-06` remains decision-closed/deferred; no registry is introduced
  until the documented growth trigger is met.

The older evidence blocks below remain historical traceability only.

- [x] Backlog gate closure: the zero/one/many Categories fixture, dynamic
  duration audit and official bounded-launcher parallel proof are green in
  `C:/work/ex/java/.tmp/backlog-resolve-20261016-2130`.
- [x] Publisher hardening: when Spark omits category markup, ATF injects a
  stable `#category-view` shell, filter surface, zero count and accessible
  `No categories available` state; existing vendor markup is preserved.
- [x] Fresh browser evidence: final UI report was opened at
  `http://127.0.0.1:57153/ui-atf-extent-report_backlog-final_2026-10-16_19-28-22-072.html`
  and the zero-category fixture at
  `http://127.0.0.1:57153/ui-atf-extent-report_category-cardinality-zero-final.html`.
  The API report was opened at
  `http://127.0.0.1:53719/api-atf-extent-report_backlog-final_2026-10-16_19-28-14-402.html`;
  POST request JSON, auth/header fields, response JSON, status and technical
  trace were expanded and visually confirmed.
- [x] Live artifacts promoted after the browser gate:
  `artifacts/reports/ui/ui-atf-extent-report_backlog-final_2026-10-16_19-28-22-072.html`
  (`SHA-256 5776A02D066BBA87E7D86E56415D2A3B23F81A465102633BF6E90245842CC06A`)
  and
  `artifacts/reports/api-atf-extent-report_backlog-final_2026-10-16_19-28-14-402.html`
  (`SHA-256 779A048203D4CB7F298C098D44A58B719038E8DFD1740AA3F650BEBEBBCA1082`).

- [x] Dynamic mutation correlation: `ApiFlowAssertions.resourceId(...)` drives
  the read-back URL and generated POST→GET report contract; no read-back step
  is coupled to `CREATED_POST_ID`.
- [x] Query semantics: `GET /public/v2/search` now has distinct YAML-backed
  contracts for invalid, missing, explicitly empty and duplicate query values;
  the typed `ApiQueryParameters` model preserves empty values and duplicate
  occurrences through URL serialization, mock matching and report evidence.
- [x] Route identity: `MockRoute.key()` includes a sorted query identity, so
  same method/path routes cannot silently collide in coverage or audit output.
- [x] Reusable API/UI audit: `ApiReportContractValidation` is the composition
  boundary for request JSON, response JSON, mock response tree, request
  metadata, response headers and execution evidence.
- [x] Exact staging artifacts were opened through ATF Debug/browser MCP:
  POST and PUT disclosures show JSON, headers, auth, status and attempts; UI
  `Screen validation` keeps the three sibling disclosures in one aligned
  contiguous group.

Fresh authoritative evidence from the synchronized `.tmp` snapshot and the
live copies:

- API final: `artifacts/reports/api-atf-extent-report_2026-10-16_19-46-21-599.html`
  (SHA-256 `2DF990AECAB7C8029C9DCF0364B9F9C9854D3AFD9E329101024523C6A298FCAD`)
- UI final: `artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_19-46-29-161.html`
  (SHA-256 `C63F56400B161BEB8CE3923BCCA86510334034F08EB1874A67DA873BE0DC68C2`)
- Staging source: `C:/work/ex/java/.tmp/top4-live-merge-20261016-194500`

The UI final is the current visual closure pointer and was reopened through
ATF Debug UI/browser MCP. Earlier 17-04/17-16/17-26/17-45/19-00 reports below remain
historical traceability only.

Historical surgical increment evidence:

- API: `artifacts/reports/api-atf-extent-report_2026-10-16_17-26-59-surgical.html`
  (SHA-256 `9AAEFB3668702BF6591E56FFC058E6D9FC49D396EC003A1808831674B03F8823`)
- UI: `artifacts/reports/ui/ui-atf-extent-report_api_2026-10-16_17-26-35-surgical.html`
  (SHA-256 `A51735F0E66BB5CAFA626592A3259159AF91734A89072F192F198D8322DB8542`)
- Staging: `.temp/backlog-surgical-20261016-1800`; both final copies were
  opened with the official `AtfDebugUiLauncher` and inspected after clicks.

Historical backlog-depth increment evidence:

- API: `artifacts/reports/api-atf-extent-report_2026-10-16_19-00-backlog.html`
  (SHA-256 `447120100E8D0428AC2B9B8E9B0FF9D1FA879E57A2F86FFB606C6DBEC97C6FE9`)
- UI: `artifacts/reports/ui/ui-atf-extent-report_api_2026-10-16_19-00-backlog.html`
  (SHA-256 `90925A4969D27FAA42F58E217E25DB8E119E50F1C7151CFBB29D6DB765F4293A`)
- Staging: `.temp/backlog-deep-20261016-1900`; both artifacts were served by
  the official `AtfDebugUiLauncher` and inspected after navigation and clicks.

Latest configuration-precedence proof:

- `UiReportAuditSpec` now applies the documented order
  `rules JVM → depth JVM → validators YAML → depth YAML`; a JVM `depth=smoke`
  can no longer be shadowed by a deep YAML validator list.
- Focused staging tests: `UiReportAuditSpecTest` 4/4 and
  `ApiReportSetupFlowTest` 3/3; the compatibility façade delegates to `ApiReportAuditSetup`.
- Smoke API artifact:
  `artifacts/reports/api-atf-extent-report_2026-10-16_19-52-depth-smoke.html`
  (SHA-256 `E46E337B8A48CC632E20DEB423AF8E9ADEC5B949108999DF61A99AD65F225D35`).
- Smoke UI artifact:
  `artifacts/reports/ui/ui-atf-extent-report_users-posts_2026-10-16_19-52-depth-smoke.html`
  (SHA-256 `DBA43DBFD05E571FDBEB359FC175D479A27288EE3DB79EA06D8CE8255684BEE2`).
  The dynamic UI factory passed 4/4 and the exact artifacts were opened in
  official ATF Debug launchers on ports 51310/51311; POST evidence and the
  contiguous Screen validation rows were expanded and visually checked.

The final focused UI artifact was re-opened through ATF Debug/browser MCP after the
live copy. The exploratory owner passed 6/6 checks; the aggregated all-api
artifact also passed its configured audit. After clicking and
collapsing the disclosures, the three `Screen validation` zones have the same
x/width, zero margins, and contiguous y positions (the next row starts exactly
at the previous row's bottom); the action sequence keeps the inline
`📷 base64 img` link. The API artifact was opened and clicked as well: POST and
PUT show auth, query (where applicable), headers, request JSON, response JSON,
status, duration, attempts and retry evidence. The API report intentionally
retains the lifecycle failure/warning demonstration; the complete staging
regression is green (100 tests, 0 failures, 0 errors, 8 skipped).

## Remaining ToDo — next surgical increments

- [x] `API-QUERY-01`: typed query collection plus explicit missing/empty and
  duplicate-key fixtures; legacy maps remain compatibility-only and no longer
  define the semantic model.
- [x] `API-DATA-01`: cross-family IDs, post/profile/user values and builders are
  centralized in `ApiFixtureData`; one-off bulk-import and negative values remain
  local by design because they have one semantic owner.
- [x] `API-PACKAGE-01`: production support and JUnit-only support are split into
  `support` and `support.junit`; `ArchitectureBoundaryTest` protects the rule.
- [x] `UI-EXTENT-REUSE-01.12/.14` (increment): cross-tab reconciliation was
  extracted to `CrossTabReconciler`, and its immutable `ValidationResult`
  carries the same assertions/violations to JUnit and Extent. Categories now
  has a pure zero/one/many link-cardinality contract with malformed-ID checks.
- [x] `F-02` semantic publication: `AtfReportSemanticSnapshot` is immutable,
  nested collections are guarded, and Extent/fallback produce an equal
  population from one lifecycle fixture.
- [x] `F-03` typed API bootstrap: `ApiBootstrapConfigurationLoader` resolves
  YAML once, `ApiModuleFactory` selects the validated acceptance/live transport,
  and live requires explicit JVM/environment opt-in.
- [x] `UI-18`: reporting screenshot fixtures now use shared minimal valid PNG
  payloads in `ReportTestScreenshots`; the asset-inlining test intentionally
  keeps arbitrary bytes only to prove generic Spark asset conversion.
- [x] `F-05`: `ReportProjectionMatrixInspector` now audits all four report
  projections in one dynamic browser test. The fresh artifact was opened in
  ATF Debug and its matrix assertions passed for Tests, Overview, Categories
  and Fails with one shared computed surface signature.
- [x] `UI-CONFIG-01` precedence hardening: explicit JVM `depth` now overrides
  suite YAML validators, while explicit JVM `rules` remains highest priority;
  the smoke run proves the resulting four-node audit selection.
- [x] `F-01`: the responsibility-level Categories CSS extraction is complete
  behind focused reporting contract tests; further splitting remains optional
  and is explicitly not a line-count target.
- [x] `D-01`: full dynamic audit passed with bounded native memory and verified
  numeric duration/source projections across Tests, Overview, Categories, Fails
  and detail; the projection validator accepts measured container markers and
  formatted leaf badges without conflating their text content.
- [x] `UI-15`: generated browser fixtures cover Categories `zero`, `one` and
  `many`; zero-state and final UI reports were inspected through ATF Debug.
- [x] `OPS-UI-01`: official bounded launcher parallel smoke passed with no new
  `hs_err_pid` file.
- [x] `UI-23`: Fails now has an explicit row/no-wrap layout owner; the fresh
  failure artifact was rendered through ATF Debug UI and visually verified with
  two failure rows, list left, detail right and top-aligned. Promoted artifact:
  `artifacts/reports/api-atf-extent-report_backlog-fails-layout-final_2026-10-16_20-03-35-065.html`
  (SHA-256 `704DEF32265D55029AC707DAC188855942D915256B43D238459A991F2E2F9C8C`);
  paired UI audit:
  `artifacts/reports/ui/ui-atf-extent-report_backlog-fails-layout-final_2026-10-16_20-03-39-042.html`
  (SHA-256 `688FC00CEE463EE65E5BF4D492140EC7720BC2D8E032467AD7A90E9B365E2131`).
- [x] `ARCH-02`: `ReportAuditContext` uses `tabViewByHash(...)`,
  `ApiReportDurationInspector` uses `runtimeRoot(...)`, and the focused
  architecture/locator guard passes 6/6 tests. Accidental duplicate source
  trees were quarantined in the fresh `.tmp` staging workspace and removed
  from live source.
- [x] `API-DESIGN-01`: mock defaults/YAML fallback and mock-backed report
  auditors now consume typed HTTP method/header/media constants; focused mock
  and evidence tests pass, and the production scan finds no duplicated raw
  method/header/media literals in the covered paths.
- [x] `ARCH-01`: `atf.api.reporting.theme` and
  `atf.api.reporting.artifact` now own the extracted theme/CSS and artifact
  inspection/contract responsibilities; root reporting sources are 52, with
  artifact/session contract tests green.
- [x] `ARCH-02`: the selector guard scans every reusable extent class outside
  `locators`; `UiSelectorBoundaryArchitectureTest`, locator tests and the
  composition boundary test are green.
- [x] `ARCH-03`: `ApiReportAuditSetup` is the composition root, fixtures/specs
  call it directly and `ApiReportSetupFlow` is only a compatibility façade.
- [x] `ARCH-04`: duration ownership is separated into the contract auditor,
  projection auditor and responsive badge-geometry auditor; the legacy
  `ApiReportDurationInspector` is now orchestration-only and its architecture
  guard prevents browser evaluation from returning to the facade.
- [x] `ARCH-05`: `ValidationResult` is the common renderer-neutral adapter for
  JSON evidence, mock evidence, mock contract, style and duration auditors;
  each local record retains its domain counters and delegates findings to it.
- [x] Final browser matrix: `UiReportAuditBrowserMatrixTest` passed 4/4 with
  empty, failure, write-normal and write-narrow profiles. The fresh API
  artifact is `artifacts/reports/api-atf-extent-report_2026-10-16_20-18-34-947.html`
  (SHA-256 `A73531BC4A8242128CB239BCB424ED857AAB134A5C4DCED45AEA3C29D175D0F9`),
  and the UI artifact is
  `artifacts/reports/ui/ui-atf-extent-report_write-methods-all-api-narrow_2026-10-16_20-18-40-159.html`
  (SHA-256 `221AE2027259955D118A680EFD360593B25A2439C4990178681153C3AEBB27E3`).
  Both were opened through ATF Debug UI/browser MCP; POST evidence showed
  request JSON, response JSON, auth profile/type, headers, status and mock
  response headers.
- [x] `ARCH-01`: extract reporting theme and artifact responsibilities into
  semantic subpackages; preserve the public reporting port and run focused
  artifact/session contracts.
- [x] `ARCH-03`: extract the API-report composition boundary into
  `extent.composition` without changing API/UI lifecycle semantics; keep the
  old flow name only as a compatibility façade.
- [x] `ARCH-06`: decision closed as trigger-gated; the current switch remains
  readable and a registry is required only after the documented growth
  threshold is reached.
- [x] `DOC-01`: feedback Markdown names were normalized atomically and all
  repository links/indexes were reconciled in the same staging increment.

- [x] `UI-SECTION-ARCH-01`: `SectionAuditCheckPolicy` is the common owner for
  all six section modules; the focused `sections × checks × journey` gate is
  green (`30/30`) and every module returns the typed result.
- [ ] `ARCH-09` follow-up (trigger-gated): extract the Extent evidence publisher
  and JUnit assertion adapter from `ExtentSectionAuditModule.audit(...)` only
  when a second renderer/consumer appears; the current adapter path is stable
  and verified.
- [ ] `UI-SECTION-ARCH-02` (trigger-gated): retire the global
  `UiReportAuditRule` only after a second consumer, parity coverage and a fresh
  ATF Debug/browser MCP artifact; until then it remains a documented
  compatibility facade, not a second source of business rules.

## Gate

Sync live source before each build, use a fresh staging workspace, test the
contract before implementation, generate a fresh artifact for observable
changes, and verify it in TAB MCP before changing status. Core/API/UI increments
remain closed in the implemented scope. The only deliberate future trigger
outside the regression guards is the ARCH-06 global registry threshold; it is
not represented as a false unchecked completion item.
