# ATF Java — inventarul UI Extent și ownership-ul responsabilităților

Reconciliere: 2026-10-16 21:35  
Strategie: `UI-EXTENT-REUSE-01.1` / `.01.2` / `.01.3`

Reconciliere live: `.15`, `.16`, `.17`, `.18`, `.19` și `.20` sunt închise;
`CategoryCardinalityBrowserFixtureTest` publică și verifică fixture-uri generate
distinct pentru Categories `zero/one/many`, iar publisher-ul garantează
`#category-view` și empty-state când vendorul omite markup-ul. `UI-22` este
închis după verificarea CSS și a artifactului. Inspectorii expun acum
rezultatul comun `ValidationResult`. D-01 este închis după
auditul dinamic 13/13 al proiecțiilor de durată. Artifactul vizual curent este
`artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_22-40-15-301.html`.

Sursa inventarului: `atf-tests/src/main/java/atf/ui/tests/extent` și
`atf-tests/src/test/java/atf/ui/tests/extent`. La această reconciliere există
**43 clase main** și **28 clase de test** în namespace-ul UI Extent. Clasele
din `src/test` sunt intenționat limitate la composition/factory/profile și la
probele browser; nu sunt runtime de producție.

Acest inventar este caracterizarea de bază pentru testele care auditează
rapoarte Extent. Nu inventează un al doilea runtime și nu mută clase doar pentru
a reduce numărul de fișiere. Fiecare clasă are un owner, un input și un output
explicit.

## Lanțul live

```text
atf-mock.yaml + catalog API
        ↓
ApiReportAuditSetup
        ↓
API Extent HTML read-only + ApiScenarioManifest
        ↓
UiReportAuditFixture / UiAuditFlow
        ↓
ReportUiLocators → actions → semantic flows
        ↓
inspectors / validators → ValidationResult / AtfAssertion
        ↓
UI Extent HTML + inline evidence
```

## Inventory pe pachete

| Source set | Package | Număr | Rol verificat |
|---|---|---:|---|
| main | `audit` | 12 | observații și validatori de raport |
| main | `flows` + `flows.inspection` | 13 | acțiuni, lifecycle de navigare și snapshot extraction |
| main | `locators` | 4 | selector boundary și handles semantice |
| main | `plan` | 3 | spec, reguli și manifest expected |
| main | `support` | 11 | input, lifecycle, contract, date și rezultat |
| test | `configurable` | 3 | factory generic + cele două profile manuale |
| test | `engine` | 1 | wiring dinamic de reguli |
| test | `exploratory` | 3 | golden E2E și source adapter existent |
| test | `flows` și subpachete | 8 | arhitectură, setup, aggregation și failure evidence |
| test | `audit`, `locators`, `plan`, `support`, `sanity` | 13 | unit/architecture/sanity guards |

Numărul este caracterizare, nu obiectiv de reducere. O clasă se unește sau se
retrage numai după replacement coverage și după verificarea artifactului exact
în ATF Debug UI.

## Inventory

| Zonă | Clase reprezentative | Input | Output | Responsabilitate | Nu trebuie să facă |
|---|---|---|---|---|---|
| API setup | `ApiReportAuditSetup` | config, annotation, YAML, mock | API report, mock, familii, manifest | compune precondiția API și artifactul | browser, DOM, selector |
| plan | `UiReportAuditSpec`, `ApiScenarioManifest`, `UiAuditSuiteConfiguration` | suite YAML, JVM overrides, catalog/mock | spec imutabil și expected route/family data | definește ce se validează | Playwright, verdict ascuns |
| fixture/lifecycle | `UiReportAuditFixture`, `ReportAuditContext`, `UiAuditLifecycle` | spec + API artifact | sesiune UI read-only și lifecycle | deschide/închide inputul, publică evidence | route/business assertions |
| locators | `ReportUiLocators` și facades specializate | Page/Locator scope | handles semantice | singurul owner de selector CSS/DOM | setup, verdict |
| actions | `UiAuditFlow.actions()` | semantic handle | operație Playwright + trace | click, wait, text, focus, screenshot | așteptări business hard-coded |
| flows | `ReportNavigationFlow`, `ApiReportUiFlow`, `ApiReportExplorationFlow` | handles + context | navigare și snapshot-uri | ordonează acțiuni UI | selector direct, expected inventat |
| inspectors | `ApiReportStructureInspector`, `ApiReportFilterInspector`, inspectori request | Page + locators | observation/snapshot | extrag datele observate din DOM | setup API, publish verdict final |
| validators | `OverviewScreenValidator`, `CategoryFilterAuditor`, `FailsScreenValidator`, `EvidenceScreenValidator`, `MockReportContractAuditor` | snapshot/DOM + manifest/mock | assertions, violations, `ValidationResult` | verifică contracte semantice | navigare ascunsă |
| engine | `UiReportAuditDynamicEngine` | fixture + spec | dynamic JUnit nodes | alege regula și leagă evidence/verdict | selector, fixture per assertion |
| test intent | `UiReportAuditDynamicTest`, profilele manuale | spec/annotation | test factory sau contract manual | declară colecția și profunzimea | CSS, owner-uri hard-coded |
| publisher | `ApiAcceptancePublisher`, reporting module | execution + evidence | API/UI Extent HTML | randare, JSON, headers, screenshot link | deducerea expected-ului din DOM |

## Roster verificabil al claselor

### `main` — contract executabil

- `audit`: `ApiJsonEvidenceAuditor`, `ApiMockEvidenceAuditor`,
  `ApiReportContractValidation`, `ApiReportDurationAuditor`,
  `CategoryFilterAuditor`, `CategoryScreenValidator`, `CrossTabReconciler`,
  `EvidenceScreenValidator`, `ExistingReportStructureAuditor`,
  `FailsScreenValidator`, `MockReportContractAuditor`,
  `OverviewScreenValidator`.
- `flows`: `ApiReportExplorationFlow`, `ApiReportSetupFlow` (compatibility facade),
  `ApiReportUiFixture`, `ApiReportUiFlow`, `ExistingReportUiFlow`,
  `ReportNavigationFlow`, `ThenDisclosureFlow`, `UiReportAuditFixture`.
- `flows.inspection`: `ApiReportDurationInspector`,
  `ApiReportFilterInspector`, `ApiReportRequestEvidenceInspector`,
  `ApiReportStructureInspector`, `ReportProjectionMatrixInspector`.
- `locators`: `ReportUiLocators`, `ReportCategoryLocators`,
  `ReportEvidenceLocators`, `ReportFailureLocators`.
- `plan`: `ApiScenarioManifest`, `UiReportAuditRule`, `UiReportAuditSpec`.
- `support`: `ReportAuditChecks`, `ReportAuditContext`, `ReportAuditInput`,
  `ReportAuditInputKind`, `ReportAuditInputs`, `ReportAuditInputSource`,
  `ReportUiContract`, `UiAuditFlow`, `UiAuditLifecycle`, `UiTestData`,
  `ValidationResult`.

### `test` — composition și guards

- `configurable`: `UiReportAuditDynamicTest`,
  `UsersPostsApiReportUiTest`, `RequestVariantsAndApiHeadersReportUiTest`.
- `engine`: `UiReportAuditDynamicEngine`.
- `exploratory`: `ApiExtentReportExploratoryTest`,
  `ExistingExtentReportExploratoryTest`,
  `ExistingExtentCategoriesExploratoryTest`.
- `flows`: setup/profile/architecture tests, `AbstractApiReportUiContractTest`,
  `ReportNavigationAggregationFlowTest` și `flows.failure.FailureEvidenceFlowTest`.
- `audit`, `locators`, `plan`, `support`, `sanity`: teste unitare și guard-uri
  pentru contract, selector boundary, manifest, input, lifecycle și disclosure.

## Matrixă de ownership și dependency direction

| Layer | Poate depinde de | Nu poate depinde de | Gate |
|---|---|---|---|
| `plan` | config, catalog, mock, Java collections | `Page`, `Locator`, Playwright, HTML | teste de imuabilitate și rezolvare YAML |
| `locators` | Playwright și `AtfLocator` | setup API, assertions de business, publisher | `UiSelectorBoundaryArchitectureTest` |
| `flows` | locators, actions, context | CSS construit în test, expected hard-coded | flow tests + action trace |
| `audit`/validators | snapshot, locators, manifest/mock | pornire API, hidden navigation, selector direct în test | unit/contract + Extent findings |
| `engine` | fixture, spec, validators/flows | selector, creare fixture per assertion | dynamic factory test |
| `configurable` tests | annotation, factory, profile | `page.locator`, owner/route CSS, renderer internals | UI test-layer architecture guard |
| `reporting` | execution/evidence contract | expected dedus din DOM | API/UI report validation |

Direcția permisă este `test intent → fixture → flow → locator → observation →
validator → evidence`. Inversarea `validator → navigation` sau
`test → selector` este regresie de arhitectură.

## Matrixă de evidence și expected/actual

| Regulă | Expected source | Actual source | Evidence obligatorie | Status |
|---|---|---|---|---|
| setup | YAML/config + API catalog + mock | artifact și manifest publicat | setup node, source path, families | green |
| Tests structure | `ApiScenarioManifest` | DOM Tests tree | owner/scenario/phase/request | green |
| request/mock contract | mock route + JSON contract | request/response disclosures | JSON, headers, auth, status, route identity | green |
| Overview | Tests snapshot + manifest | Overview DOM | expected/actual totals | green |
| Categories | discovered Tests tags | Categories DOM | filter, count, linked test | green pentru contract pur |
| Fails | failure policy + report state | Fails DOM | empty/failure state și link | green pentru state-urile existente |
| duration | node population | rendered badges | numeric/formatted parent-child values | green |
| responsive | shared geometry contract | computed styles la viewport | normal/narrow signature | green |
| screenshot evidence | evidence policy | exact HTML anchor | one Base64 payload, emoji, geometry | green |
| cross-tab | manifest + Tests projection | Overview/Categories/Fails | set reconciliation | green pentru validatorul comun |

`green` înseamnă că există cod și verificare. Matricea browser data-driven
pentru cardinalități, empty/failure și combinații GET/POST/PUT există și este
verde; fixture-urile generate distinct pentru `zero/one/many` sunt acum parte
din gate-ul live UI-15.

## Clase mari și decizia chirurgicală

- `ApiExtentReportExploratoryTest` rămâne golden E2E; setup-ul și regulile noi
  nu se mai adaugă acolo. Extracția ulterioară trebuie să lase doar wiring și
  să păstreze aceeași evidență.
- `UiReportAuditDynamicEngine` este composition layer, nu validator; mutarea
  fiecărei reguli într-o clasă separată se face numai când există un snapshot
  neutru reutilizat, nu prin copiere de metode.
- `ApiReportExplorationFlow` este facade de compatibilitate. Snapshot-urile sunt
  deja în modelul neutru `ReportAuditSnapshot`; migrarea rămasă este extinderea
  rezultatului tipizat la inspectorii non-cross-tab.
- `ExistingExtentReportExploratoryTest` și generated API audit rămân două source
  adapters distincte: unul primește HTML existent, celălalt îl generează prin
  API setup. Nu se combină lifecycle-urile.

### Praguri și acțiuni pentru clase mari

| Clasă | Dimensiune observată | Simptom | Acțiune permisă acum | Gate de închidere |
|---|---:|---|---|---|
| `ReportUiLocators` | ~23 KB | facade mare, dar selector ownership centralizat | păstrează facade-ul; extrage numai grupuri cu API stabil | architecture test + zero selector în test |
| `MockReportContractAuditor` | ~18 KB | fațadă publică pentru JSON/mock mapping și negative policy | păstrează fațada; route resolution, body, metadata, execution evidence și deep JSON sunt deja colaboratori tipizați; nu introduce o a doua fațadă | POST/PUT positive + negative report |
| `ApiExtentReportExploratoryTest` | ~18 KB | golden wiring E2E | păstrează testul subțire și mută orice regulă nouă în engine/inspectors | raport golden identic structural și vizual |
| `UiReportAuditDynamicEngine` | ~17 KB | rule dispatch + Extent publication | păstrează snapshot projection în model neutru, nu în clase per regulă | dynamic suite deep + same findings |
| `ApiReportExplorationFlow` | facade | navigare/disclosure compatibilă | păstrează snapshot-urile în `ReportAuditSnapshot` și convertește gradual inspectorii la rezultate tipizate | inspectorii non-cross-tab nu depind de record-uri legacy |

Nu se aplică o regulă arbitrară de tip „fișier mare = trebuie spart”. Criteriul
este responsabilitate, dependency direction și replacement coverage.

## Snapshot pentru `users-posts`

Snapshot-ul aprobat este derivat din manifest și mock, nu din numere fixe:

- familii: `users`, `posts`;
- owner-i: `UsersApiTests`, `PostsApiTests`;
- request identity: method + path + query canonicalizat;
- tab-uri: Tests, Overview, Categories, Fails, Evidence;
- reguli: setup, Tests, request/mock contract, Overview, Categories, Fails,
  duration, responsive, evidence și cross-tab;
- evidence: request/response JSON, headers/auth/status/duration și inline
  `📷 base64 img`.

## Decizia de modularizare

`atf-report-contracts` nu se extrage încă. Există un consumator real principal
(`atf-tests`); `atf-reporting` publică HTML, dar nu consumă încă un model pur
de manifest/findings ca API stabil. Extracția devine justificată numai când
există doi consumatori independenți ai aceluiași model renderer-neutral și un
dependency graph verificat. Până atunci package split-ul din `atf-tests` este
mai sigur și evită o dependență Maven anticipativă.

## Rămase după inventar — ToDo executabil

- [x] `UI-EXTENT-REUSE-01.15` — contractul pur, matricea browser și fixture-urile
  generate distinct pentru Categories `zero/one/many` acoperă empty/failure,
  GET/POST/PUT și viewport normal/narrow; zero-state este verificat prin MCP.
- [x] `UI-EXTENT-REUSE-01.16` — reducerea `ApiExtentReportExploratoryTest` la
  wiring golden; regulile trebuie să rămână în engine/validators.
- [x] `UI-EXTENT-REUSE-01.18` — unificarea flow-urilor numai după snapshot
  identic pentru Tests/Overview/Categories/Fails.
- [x] inspectorii JSON/mock/contract/style/duration și reconcilierea cross-tab
  expun `ValidationResult`; modulele pe secțiuni folosesc aceeași suprafață.
- [ ] inspectorii DOM rămași (`ApiReportRequestEvidenceInspector`, structura,
  filtrele și validatorii istorici Categories/Evidence) trebuie converși treptat
  la `snapshot → validator pur → ValidationResult`; nu se schimbă selector
  boundary-ul și nu se deduce expected-ul din DOM.

Pentru fiecare item se păstrează aceeași probă: build în `.tmp`, rulările
active nu sunt oprite, artifactul este publicat în directorul configurat și
HTML-ul exact este deschis prin ATF Debug UI/browser MCP înainte de bifare.

## Criteriu de sănătate al inventarului

Inventarul trebuie reverificat când apare o clasă nouă sub namespace-ul UI Extent
sau când un test importă Playwright/HTML direct. Reconcilierea minimă este:

1. numără clasele `main` și `test` pe pachet;
2. actualizează rosterul și layer-ul owner;
3. verifică dependency direction și selector boundary;
4. leagă testul de contract/evidence și artifactul verificat;
5. marchează ToDo-ul doar după replacement coverage, nu după compilare simplă.
