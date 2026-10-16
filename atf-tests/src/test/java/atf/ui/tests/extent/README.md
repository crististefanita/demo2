# Extent report UI tests

Acest pachet este spațiul exclusiv pentru testele UI care auditează rapoarte
Extent generate de ATF. Nu este un pachet generic pentru testele UI ale
produsului și nu trebuie folosit ca pretext pentru a introduce browser flow-uri
noi aici.

Incrementul plug-and-play este implementat în codul live la 2026-10-16:
`UiReportAuditSpec` → `ApiScenarioManifest` → `UiReportAuditFixture` →
`UiReportAuditDynamicEngine`. Intrarea publică pentru colecții arbitrare este
`configurable/UiReportAuditDynamicTest`. Cele două clase configurabile cerute
explicit rămân intenționat ca profile/manual smoke contracts; nu se mai adaugă
câte o clasă pentru fiecare combinație nouă de familii API.

Reconciliere live: contractul `Screen validation` este închis. Artifactul UI golden final
`artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_22-40-15-301.html`
(SHA-256 `CAE94267A034BEBCF23B8792FF9E4D5220C45FBF71D8F1C694E11794A3102CAC`)
a fost redeschis prin ATF Debug UI/browser MCP; `Screen validation` are cele
trei zone-soră contigue (`35px → 34px → 35px` fără gol), `hr=0`, iar linkul
`📷 base64 img` rămâne inline. Perechea API verificată este
`artifacts/reports/api-atf-extent-report_2026-10-16_22-40-08-404.html`.
Matricea browser data-driven este închisă prin `UiReportAuditBrowserMatrixTest`
(4/4); nu se reintroduce `ui.enabled`.

Strategia țintă pentru suite plug-and-play, manifestul API/mock și testele
JUnit dinamice este detaliată în
[`ATF_Ui_Extent_Reusable_Test_Strategy.md`](../../../../../../../../docs/feedback/ATF_Ui_Extent_Reusable_Test_Strategy.md),
iar statusul executabil și ToDo-urile rămân în
[`ATF_Ui_Exploratory_Feedback.md`](../../../../../../../../docs/feedback/ATF_Ui_Exploratory_Feedback.md).
Inventarul responsabilităților și decizia de modularizare sunt în
[`ATF_Ui_Extent_Inventory.md`](../../../../../../../../docs/feedback/ATF_Ui_Extent_Inventory.md).
Acest README este boundary-ul tehnic al pachetului și trebuie să rămână
sincronizat cu acele două documente.

## Section modules — toate verticalele sunt live

Pentru testarea plug-and-play a unei singure secțiuni de raport există acum
un contract separat de engine-ul legacy:

```java
@ApiScenarioSetup(families = {"lifecycle"})
@UiExtentSectionAudit(
        source = UiExtentReportSource.GENERATED_API,
        sections = {ExtentSection.TESTS, ExtentSection.FAILS},
        checks = {ExtentSectionCheck.STRUCTURE},
        journey = ExtentJourney.FAILURE_NAVIGATION)
class GeneratedTestsSectionReportUiTest { /* @TestFactory */ }
```

Același modul poate consuma un raport existent, fără să-l regenereze:

```java
@UiExtentSectionAudit(
        source = UiExtentReportSource.STATIC_PATH,
        reportProperty = "atf.ui.report",
        sections = {ExtentSection.TESTS, ExtentSection.FAILS},
        journey = ExtentJourney.FAILURE_NAVIGATION)
class StaticTestsSectionReportUiTest { /* @TestFactory */ }
```

Lanțul este `annotation → SectionAuditSpec → ReportSourceResolver →
SectionAuditFixture → SectionAuditRegistry → SectionAuditModule → flow →
locator → snapshot → pure validator → Extent evidence`. Testul declarativ nu
conține `Page`, `Locator` sau selectori. `TestsSectionValidator` și
`FailsSectionValidator` verifică snapshot-uri fără browser, iar modulele sunt
adaptoare browser care doar navighează și publică evidence. Fails păstrează
ramura empty-state și ramura failure rows + primary cause + `Open in Tests`;
cu `ExtentJourney.FAILURE_NAVIGATION` se execută efectiv traseul
`Tests → Fails → Open in Tests → Overview`, cu deep-link, metric de failure și
disclosures colapsate verificate.

Sursa statică este strictă: `STATIC_PATH` cere cheia JVM indicată și verifică
fișierul read-only; `STATIC_RESOURCE` cere exclusiv resource classpath;
`GENERATED_API` folosește `@ApiScenarioSetup` și mock/YAML prin
`ApiReportAuditSetup`. Nu există fallback ambiguu și nu se introduce
`ui.enabled`. Registry-ul conține deja `Overview`, `Tests`, `Categories`,
`Fails`, `Evidence` și `Screen validation`; fiecare are modul și validator pur,
iar `Fails` execută journey-ul `FAILURE_NAVIGATION`. Uniformizarea executorului
pentru toate `checks` și separarea publisherului Extent/JUnit sunt încă active
în feedback-ul de arhitectură.

Statusul și ToDo-urile active sunt în
[`ATF_Ui_Extent_Section_Architecture_Feedback.md`](../../../../../../../../docs/feedback/ATF_Ui_Extent_Section_Architecture_Feedback.md).

## Responsabilități

- `configurable` conține intenția testului și selecția colecției API raportate;
- `flows` conține acțiuni UI reutilizabile și traversarea raportului;
- `locators` este singura zonă care cunoaște selectorii HTML ai raportului;
- `audit` conține validatori și inspectori semantici;
- `support` conține contracte, date, input-uri și lifecycle-ul auditului;
- `exploratory`, `sanity` și `flows.failure` sunt suite specializate pentru
  raportul Extent, nu pentru UI-ul aplicației testate.

Lanțul obligatoriu este:

`test intent -> fixture/setup -> flow -> locator facade -> validator/auditor -> evidence`

Testele nu construiesc selectorii direct, nu folosesc `page.locator(...)` și nu
conțin detalii despre DOM. Un test declară scenariul, familia API sau profilul
de raport și aserțiunea de business; implementarea navigării și a evidenței
stă în flow-uri, locatori și validatori.

## API precondition vs UI audit

`ApiReportAuditSetup` poate rula colecția API configurată ca precondiție pentru
raport. El nu mută teste API în acest pachet și nu validează browser-ul. Testele
din acest namespace verifică exclusiv că raportul Extent rezultat este redat
corect și că păstrează evidența JSON, mock, request/response și pașii UI.

Selecția este plug-and-play: JVM property `atf.ui.api.families` are prioritate,
apoi suita `atf.ui.audit.suite`/lista de familii, apoi
`@ApiScenarioSetup(families = ...)`, apoi familiile YAML din configurația UI.
Profilurile și constantele familiei sunt centralizate în `UiTestData`.

Nu există și nu trebuie introdus `ui.enabled: true`. În live, o suită UI este
selectată prin test/factory, o intrare declarativă `ui.audit.suites`, adnotare,
familiile YAML implicite sau proprietăți JVM; un registry de runtime separat
rămâne țintă, nu o clasă duplicată pentru fiecare combinație.

## Selecție exactă API → audit UI

Pentru un audit punctual, testul poate selecta programatic metode din familii
API diferite. Selecția este tipizată prin `(owner, method)`, nu printr-un
string liber și nu prin reflection peste clase JUnit:

```java
@ApiScenarioSetup(tests = {
        @ApiTestRef(owner = UsersApiScenarios.class,
                method = UsersApiScenarios.GET_COLLECTION),
        @ApiTestRef(owner = PostsApiScenarios.class,
                method = PostsApiScenarios.CREATE_POST)
})
class SelectedApiTestsReportUiTest { /* @TestFactory */ }
```

`ApiScenarioSetupCatalog` rezolvă referințele în publisher-ele de scenarii,
construiește manifestul și proiecția expected înainte de browser, iar YAML/mock
rămâne sursa de adevăr pentru route, status, headere și body JSON. API setup-ul
rulează o singură dată; factory-ul UI publică apoi `SETUP`, `TESTS`,
`REQUEST_CONTRACT`, `OVERVIEW`, `CATEGORIES`, `FAILS`, `DURATIONS`, `EVIDENCE`,
`STYLE`, `RESPONSIVE` și `CROSS_TAB` după regulile declarate.

Acest contract este implementat și protejat de
`SelectedApiTestsReportUiTest`, `ApiScenarioSetupCatalogTest`,
`ExpectedReportProjectionTest`, `ReportProjectionMatrixInspector` și
`ReportStyleValidator`. El este documentație stabilă pentru selecția exactă;
nu se creează câte o clasă UI pentru fiecare combinație nouă de teste API.

## Configurare plug-and-play executabilă

Familiile API rămân configurabile în YAML prin
`ui.apiSetup.scenarioFamilies`, iar selecția manuală are prioritate prin:

```text
-Datf.ui.api.families=users,posts
-Datf.ui.audit.suite=users-posts
-Datf.ui.audit.depth=smoke|deep
-Datf.ui.audit.rules=SETUP,TESTS,REQUEST_CONTRACT,OVERVIEW,CROSS_TAB
```

Forma declarativă pentru suite este:

```yaml
ui:
  audit:
    suites:
      users-posts:
        scenarioFamilies: [users, posts]
        tabs: [tests, overview, categories, fails, evidence]
        validators: [setup, tests, request-contract, overview, categories,
                     fails, duration, responsive, evidence, cross-tab]
        depth: deep
        source: yaml
```

`UiConfigurationYamlReader` încarcă secțiunea în `UiAuditSuiteConfiguration`.
`ApiReportAuditSetup` consumă familiile suitei, iar `UiReportAuditSpec` consumă
validatorii și depth-ul. Proprietățile JVM `atf.ui.audit.rules` și
`atf.ui.audit.depth` au prioritate, pentru override punctual fără modificarea
YAML-ului.

Precedența regulilor este strictă și testată: `atf.ui.audit.rules` →
`atf.ui.audit.depth` → `validators` din suita YAML → `depth` din suita YAML.
Astfel `-Datf.ui.audit.depth=smoke` nu este umbrit de o listă YAML `deep`, iar
un override explicit de reguli rămâne mai specific decât orice depth.

Precedența familiilor este `atf.ui.api.families` → `atf.ui.audit.suite` (suită
YAML sau listă JVM) → `@ApiScenarioSetup` → YAML. `depth=deep` rulează setul complet; `smoke`
rulează setup, Tests, contractul request/response și Overview. `rules` poate
selecta orice subset, fără clasă nouă și fără boolean `ui.enabled`.

Manifestul este construit înainte de browser din familiile selectate și
`AtfMockConfiguration`: owner-i, route identity, status, headere, body mode și
expected request body. DOM-ul este strict actualul observat. Pentru toate
route-urile POST/PUT, mock-ul și validatorul păstrează contractul JSON pentru
request și response; modurile negative sunt explicite (`invalid-json`,
`missing`, `wrong-content-type`).

Factory-ul dinamic reutilizează aceeași sesiune UI pentru toate regulile unei
suite: API setup-ul se execută o singură dată, iar nodurile JUnit rulează
`SETUP`, `TESTS`, `REQUEST_CONTRACT`, `OVERVIEW`, `CATEGORIES`, `FAILS`,
`DURATIONS`, `RESPONSIVE`, `EVIDENCE` și `CROSS_TAB` după spec. Fiecare nod
publică expected/actual și evidence în Extent.

## Implementarea live și arhitectura țintă

Implementarea live are deja un composition root generic și un singur factory
dinamic. Fluxul executabil este:

```text
UiReportAuditSpec
    ↓
ApiReportAuditSetup.Result (report, mock, families, manifest)
    ↓
UiReportAuditFixture
    ↓
UiReportAuditDynamicEngine (src/test)
    ↓
flow-uri/inspectori/validatori existenți + Extent evidence
```

`ApiReportUiFixture` există doar ca facade de compatibilitate și deleagă
integral către `UiReportAuditFixture`; nu este un al doilea composition root.
`UiReportAuditDynamicTest` este intrarea generică, iar engine-ul live selectează
regulile din `UiReportAuditSpec`.

Următoarea diagramă separă explicit ce este live de consolidarea rămasă; nu
este o listă de clase care trebuie recreate pentru fiecare suită:

```text
UiReportAuditSpec
    ↓
ApiReportAuditSetup
    ├── ApiScenarioSetupCatalog
    ├── atf-mock.yaml / AtfMockTransport
    └── API Extent report read-only
    ↓
ApiScenarioManifest (live) + ExpectedReportProjection (live, immutable expected projection)
    ↓
UiReportAuditPlan (țintă; plan separat încă lipsește)
    ├── Tests
    ├── Overview
    ├── Categories
    ├── Fails
    ├── request/response/execution evidence
    └── Tests ↔ Overview ↔ Categories ↔ Fails
    ↓
ReportUiLocators → actions → flows → validators
    ↓
ValidationResult / Finding → JUnit + UI Extent evidence (live pentru
CrossTabReconciler; migrarea celorlalte snapshot-uri continuă incremental)
```

`ApiScenarioManifest` este sursa expected-ului: familii, owner-i, scenarii,
faze, route identity, method/path/query, status, headere, auth profile, body
mode și regulile de evidence. DOM-ul este actualul observat, nu sursa
așteptărilor. Pentru `POST` și `PUT`, manifestul impune body JSON valid și nevid
în request și response; excepțiile negative sunt explicite în mock și în
manifest.

Configurația declarativă YAML de mai jos este parsată live de
`UiConfigurationYamlReader`; `ExpectedReportProjection` este live și reprezintă
proiecția expected comună. `UiReportAuditPlan` și registry-ul typed rămân
obiective separate de consolidare, nu motive pentru un al doilea composition
root:

```yaml
ui:
  audit:
    suites:
      users-posts:
        source: generated-api
        scenarioFamilies: [users, posts]
        depth: deep
        tabs: [tests, overview, categories, fails, evidence]
        validators: [setup, tests-structure, request-json,
                     mock-reconciliation, cross-tab, screenshot-evidence]
```

Fabrica JUnit live este deja `configurable/UiReportAuditDynamicTest`, cu un
`@TestFactory` și un container dinamic pentru regulile selectate. În live,
`atf.ui.audit.rules` și `atf.ui.audit.depth` sunt override-uri JVM; YAML-ul
furnizează atât default-ul `ui.apiSetup.scenarioFamilies`, cât și suitele
`ui.audit.suites` cu `tabs`, `validators`, `depth` și `source`. Adăugarea
familiei `flows`, `negative` sau `lifecycle` schimbă doar catalogul/manifestul
și datele, nu cere o clasă UI nouă.

## Responsabilități și reducerea claselor

```text
locators   = selectorul și scoping-ul
actions    = operația Playwright + target-ul auditabil
flows      = ordinea semantică a navigării
validators = snapshot DOM → ValidationResult/AtfAssertion (live pentru
reconcilierea cross-tab, contractul Categories și matricea tuturor proiecțiilor)
engine     = alegerea validatorilor din spec
test       = declararea spec-ului, fără CSS/owner hard-codat
```

Deciziile pentru clasele curente:

| Zonă | Decizie |
|---|---|
| `ApiReportAuditSetup`, `ReportAuditContext`, `UiAuditFlow`, `UiAuditLifecycle` | păstrate; sunt boundary-uri reale de composition/lifecycle/evidence; vechiul `ApiReportSetupFlow` este doar fațadă |
| `ReportUiLocators` și sub-locatorii | păstrați; selectorii nu se mută în teste, API sau renderer |
| `ApiReportUiFixture` | facade de compatibilitate live; deleagă către `UiReportAuditFixture` și nu trebuie extinsă cu reguli noi |
| `ApiReportUiFlow` și `ApiReportExplorationFlow` | facades de compatibilitate; acțiunile rămân în flow, iar snapshot-urile sunt în `ReportAuditSnapshot` |
| `ApiJsonEvidenceAuditor`, `MockReportContractAuditor` | păstrate ca fațade publice stabile; `MockReportContractAuditor` deleagă deja route resolution, body, request/response metadata, execution evidence și deep JSON către colaboratori tipizați, cu `ValidationResult` comun |
| `ApiMockEvidenceAuditor` | candidat de merge/deprecate după ce mapping-ul request → route este acoperit exact |
| `UsersPostsApiReportUiTest`, `RequestVariantsAndApiHeadersReportUiTest` | păstrate intenționat; sunt cele două profile manuale aprobate și reutilizează același harness |
| `AbstractApiReportUiContractTest` + `ApiReportUiTestProfile` | păstrate ca suport compatibil pentru cele două profile aprobate |
| `AllApiReportUiContractTest`, `AllApiReportUiArchitectureTest` | retrase; erau redundante, iar prima avea owner-i hard-codați și a fost înlocuită de factory-ul dinamic |
| `ApiExtentReportExploratoryTest` | rămâne un golden E2E subțire, nu owner-ul tuturor regulilor |
| testele architecture/locator/lifecycle | se păstrează ca gardieni ai boundary-urilor |

Nu se șterge o clasă înainte ca înlocuitorul dinamic să producă aceeași
acoperire, aceleași findings și un artifact verificat în ATF Debug UI/browser
MCP. Nu se mută browserul în `atf.api.tests.api`, iar `atf-ui` rămâne numai
infrastructura Playwright.

## ToDo rămas după incrementul din 2026-10-16

- [x] Rafinează rendererul `Screen validation`: cele trei zone de evidence
  rămân într-un singur contur, fără linii orizontale decorative între ele;
  linkul `📷 base64 img` rămâne inline, clicabil și centrat vertical.

- [x] Introdu `UiReportAuditSpec`, `UiReportAuditRule` și
  `ApiScenarioManifest` ca modele imutabile.
- [x] Extinde rezultatul `ApiReportAuditSetup` cu manifestul derivat din catalog
  și mock, nu din DOM.
- [x] Creează `UiReportAuditFixture` și un `@TestFactory` reutilizabil; setup-ul
  API rulează o singură dată per spec.
- [x] Elimină agregatorul legacy cu owner-i hard-codați; factory-ul nou derivă
  owner-ii și request-urile exclusiv din manifest.
- [x] Permite selecția `flows`, `negative` și `lifecycle` prin familii dinamice,
  fără teste UI duplicate.
- [x] Rezolvă determinist familiile prin JVM property → suite alias/listă JVM →
  annotation → YAML `ui.apiSetup.scenarioFamilies` și construiește un singur
  `@TestFactory` cu noduri dinamice pe regulă; rules/depth sunt JVM properties.
- [x] Configurația declarativă `ui.audit.suites` este încărcată în
  `UiAuditSuiteConfiguration` și consumată de setup/spec pentru familii,
  tabs, validators, depth și source; testul de loader acoperă normalizarea
  suite ID-ului și valorile YAML. `ExpectedReportProjection` este deja live;
  `UiReportAuditPlan` rămâne doar opțiune de model generic viitor, nu ToDo al
  incrementului curent.
- [x] Mută record-urile de snapshot din `ApiReportExplorationFlow` în
  `ReportAuditSnapshot`; inspectorii JSON/mock/contract/style/durată expun
  acum adapterul comun `ValidationResult`.
- [x] Extrage `CrossTabReconciler` și `ValidationResult` pentru reconcilierea
  dinamică Tests/Overview/Categories/Fails; rezultatul comun publică assertions
  și violations către JUnit și Extent.
- [x] Separă responsabilitățile de durată și viewport: `ApiReportDurationInspector` orchestrează, iar
  `ApiReportDurationAuditor`, `ApiReportDurationProjectionAuditor` și
  `ApiReportStatusBadgeGeometryAuditor` dețin contractul, proiecțiile și
  geometria responsive; guard-ul de compoziție protejează limitele.
- [x] Adaugă `ReportProjectionMatrixInspector`: un singur test browser verifică
  în același raport `Overview`, `Tests`, `Categories` și `Fails` pentru
  vizibilitate, suprafață calculată, conținut dinamic și controale de filtrare
  aplicabile; raportul fresh a fost inspectat prin ATF Debug.
- [x] Acoperă prin contract pur cardinalitățile Categories zero/one/many și
  respinge ID-uri malformed și le verifică browser data-driven pentru
  empty/failure, GET/POST/PUT și viewport normal/narrow.
- [x] `CategoryCardinalityBrowserFixtureTest` publică independent fixture-uri
  generate pentru `zero`, `one` și `many`; publisher-ul injectează un
  `#category-view` stabil și un empty-state accesibil când vendorul nu emite
  markup de categorie. Fixture-ul zero și raportul final au fost deschise prin
  ATF Debug/browser MCP.
- [x] Retrage clasele redundante `AllApiReportUiContractTest` și companion-ul
  lor după replacement coverage verde; cele două profile aprobate rămân.

Gate-ul pentru fiecare increment rămâne: build izolat în `.temp`, fără oprirea
rulărilor curente; sincronizare selectivă în codul live numai după teste și
inspectarea exactului HTML prin ATF Debug UI/browser MCP.

## Extindere

Pentru viitoare teste UI ale aplicației se rezervă un namespace separat, de
exemplu `atf.ui.tests.product`, cu locatori, flows și validatori proprii. Nu se
reutilizează selectorii sau fixture-urile Extent decât printr-o abstracție
explicită, documentată și testată.
