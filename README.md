# ATF Java — framework pentru automatizare API

ATF Java este un framework standalone pentru automatizarea și validarea
scenariilor API. README-ul rădăcină explică numai arhitectura, componentele,
fluxurile și responsabilitățile. Contractele detaliate de raportare, staging,
build și verificare a artifactului sunt documentate în modulele și documentele
specializate către care trimite această pagină.

## Diagramă de bază

```mermaid
flowchart LR
    Test[API/UI test intent] --> Execution[atf-execution<br/>lifecycle + services]
    Execution --> Core[atf-core<br/>models + ports]
    Execution --> Validation[atf-validation<br/>contracts + verdicts]
    Execution --> Reporting[atf-reporting<br/>Extent/HTML artifact]
    Test --> Mock[atf-mock<br/>deterministic transport]
    Test --> Ui[atf-ui<br/>Playwright runtime]
    Reporting --> Debug[atf-debug-ui<br/>read-only inspection]
    Reporting --> Console[atf-console<br/>diagnostic projection]
```

Săgeata înseamnă „folosește/deține colaborarea”, nu neapărat o dependență
Maven directă. Graful exact al modulelor este în
[`docs/ATF_Architecture.md`](docs/ATF_Architecture.md).

## Secvența completă a unui test API

Testul exprimă intenția, API layer-ul produce acțiunea, execuția coordonează
lifecycle-ul, validarea produce verdictul semantic, iar raportarea înregistrează
evidence-ul printr-un port neutru.

```mermaid
sequenceDiagram
    participant T as Test class
    participant L as atf-execution / lifecycle
    participant A as API service
    participant X as ApiExecutor
    participant H as HTTP transport
    participant V as atf-validation
    participant R as atf-reporting

    T->>L: define scenario + tags + only the phases it needs
    L->>R: open session / class / scenario
    L->>R: open requested phases (TEST BODY, optional SETUP/TEARDOWN)
    T->>A: semantic API action
    A->>X: ApiRequest from canonical route
    X->>H: authenticated HTTP call
    H-->>X: ApiResponse + duration + retry history
    X-->>A: ApiExecutionResult
    A-->>T: response/result
    L->>R: register request + technical evidence
    T->>V: response + ResponseContract
    V->>V: evaluate rules and warning policy
    V-->>T: ValidationResult
    V->>R: publish validation through neutral port
    L->>R: close phases and calculate scenario verdict
    R->>R: publish the reporting artifact
```

Pentru verificarea interactivă a artifactului există infrastructura
`atf-ui`: testele de audit al rapoartelor Extent rămân în pachetul `atf.ui.tests.extent` din test scope-ul `atf-reporting`, iar
Playwright consumă raportul Extent deja publicat.
Pentru investigație manuală există `atf-debug-ui`, care servește read-only un
HTML existent pe loopback și tipărește URL-ul pentru Playwright/TAB MCP.

### Boundary-ul normativ API/UI

Package-ul este parte din responsabilitate, nu doar o convenție de naming:

| Zonă | Conține | Nu poate conține |
|---|---|---|
| `atf.api.tests.api` | teste API pe resurse, request-uri, headere și lifecycle | browser, Playwright, DOM, locatori UI sau Debug UI |
| `atf.ui.tests.extent` | teste de browser pentru audit Extent, DOM, screenshot, linkuri inline și validări vizuale | teste API ascunse în namespace-ul API sau teste UI de produs |
| `atf-ui` | configurație și adaptor Playwright/browser | teste JUnit și scenarii API |
| `atf-debug-ui` | server loopback read-only pentru inspecția artifactului | execuție API și verdict de test |

Regula de diagnostic este strictă: dacă un fișier din `atf.api.tests.*` importă
Playwright, `Page`, `Locator`, `DebugUiServer` sau `atf.api.ui`, este clasificat
greșit și trebuie mutat în `atf.ui.tests.extent` (sau în namespace-ul UI al
produsului), niciodată în `atf.api.tests.api`. Setup-ul API pentru un test UI este
deținut de `atf.ui.tests.extent.support.api.ApiReportAuditSetup`; flow-urile
auxiliare rămân în `atf.ui.tests.extent.support.api`, iar testele nu compun
transportul direct.
Testele API care verifică catalogul mock folosesc `atf.api.tests.support.ApiTestResources`;
nu importă `ReportingTestResources`, deoarece acesta aparține setup-ului UI/report.
UI-ul consumă raportul API ca input read-only și publică dovada UI într-un artifact separat. Direcția
este `API/setup → raport API → browser UI → raport UI`, niciodată invers.

### Auditul UI Extent pe secțiuni

Auditul raportului Extent este o compoziție declarativă în test scope-ul
`atf-reporting`, nu o
regulă globală ascunsă în test:

```text
@UiExtentSectionAudit
  → SectionAuditSpec + SectionAuditFixture
  → SectionAuditRegistry
  → section module (snapshot + validator pur)
  → SectionAuditCheckExecutor + SectionAuditCheckPolicy
  → SectionAuditResult
  → ExtentSectionAuditPublisher
  → artifact UI + verdict JUnit
```

Modulele active sunt `Overview`, `Tests`, `Categories`, `Fails`, `Evidence` și
`Screen validation`. Modulele nu publică Extent și nu aruncă aserțiuni JUnit;
executorul comun decide check-urile aplicabile, iar publisherul separat este
singurul adapter care scrie evidence-ul Extent, screenshot-ul și verdictul.
Selecția API poate porni din `@ApiScenarioSetup` și mock/YAML, dar namespace-ul
`atf.api.tests.api` rămâne exclusiv pentru teste API și nu primește browser.
Contractul detaliat și exemplele plug-and-play sunt în
[`atf-reporting/src/test/java/atf/ui/tests/extent/README.md`](atf-reporting/src/test/java/atf/ui/tests/extent/README.md).

### Ownership pe fiecare pas

| Pas | Owner | Responsabilitate | Nu face |
|---:|---|---|---|
| 1 | `atf-reporting` test scope | definește scenariul, tag-urile și fazele pentru suitele de raportare | nu construiește HTTP sau HTML |
| 2 | `atf-execution` | coordonează sesiunea, lifecycle-ul și precedence-ul | nu evaluează contracte |
| 3 | API services | exprimă acțiuni semantice (`UsersService`, `PostsService`) | nu cunosc JUnit/Extent |
| 4 | `atf-core` | modelează request, transport, auth, retry și rezultat | nu deține servicii de business |
| 5 | `atf-validation` | evaluează response-ul și produce verdict/evidence | nu depinde de Extent/HTML |
| 6 | `atf-reporting` | consumă porturile și publică evidence-ul | nu inventează verdicturi |

## Arborele semantic al testului

Acesta este contractul de ownership al execuției, nu un detaliu de CSS:

```text
TestCase
├── SETUP (optional; only preconditions/fixtures)
│   └── Request(s)
├── TEST BODY (required when the test performs an action)
│   ├── Request
│   │   ├── Request details
│   │   │   ├── URL / method / path / headers / params / auth
│   │   │   └── REQUEST JSON (payload sau null dacă body-ul lipsește)
│   │   ├── Response details
│   │   │   ├── Technical trace
│   │   │   └── RESPONSE JSON (payload sau null dacă body-ul lipsește)
│   │   └── Execution evidence
│   └── Validation — sibling direct al request-ului owner
│       └── aggregate assertion table
└── TEARDOWN (optional; only cleanup/read-back/lifecycle outcome)
    └── Request(s)
```

Request-ul și validation-ul sunt noduri surori în aceeași fază. API service-ul
nu știe de raportare, validatorul nu decide layout-ul, iar reporting-ul nu
calculează verdictul. O implementare care sare peste un owner sau inversează
această ordine încalcă arhitectura.

Regula body-ului este dependentă de metoda HTTP: pentru `POST` și `PUT`, atât
request-ul cât și response-ul trebuie să fie documente JSON ne-goale și valide;
`null` nu este o valoare acceptată pentru un write contract. Pentru exchange-uri
fără body, inclusiv `GET` fără payload și răspunsurile `204`/`304`, raportul
păstrează ambele disclosure-uri și redă explicit JSON `null`. Aceeași regulă
este aplicată de `ApiRoute`, publisher-ul de evidence, mock și auditorul UI.

### Fazele nu sunt obligatorii în bloc

Un test nu trebuie să declare automat `SETUP` și `TEARDOWN`. Planul de lifecycle
alege fazele după intenția testului:

| Tip de test | Faze recomandate | Când se folosește |
|---|---|---|
| unit/smoke minimal | `TEST BODY` | acțiunea este autonomă și nu are fixture sau cleanup |
| test cu precondiții | `SETUP → TEST BODY` | pregătirea trebuie să fie vizibilă separat |
| test cu resurse | `SETUP → TEST BODY → TEARDOWN` | există cleanup, read-back sau outcome de lifecycle real |

`atf-reporting` publică exact fazele create de test și nu inventează faze lipsă.
Închiderea sesiunii publică artifactul și pentru planurile minimale; `TEARDOWN`
nu este o confirmare artificială a închiderii raportului.

## Arhitectura modulelor

```mermaid
flowchart LR
    Tests[atf-reporting test scope] --> Execution[atf-execution]
    Tests --> Mock[atf-mock]
    Tests --> Validation[atf-validation]
    Tests --> Console[atf-console]
    Tests --> Reporting[atf-reporting]
    Tests --> Ui[atf-ui]
    Tests --> DebugUi[atf-debug-ui]
    Execution --> Core[atf-core]
    Execution -.-> Validation
    Validation --> Core
    Console --> Core
    Reporting --> Core
    Ui --> Core
```

`atf-reporting` produce artifactul HTML prin porturile neutre definite de
`atf-core`; artifactul este un output al modulului, nu o dependență Maven.

| Modul | Deține | Nu deține |
|---|---|---|
| `atf-core` | request/response, transport contracts, statusuri, config, retry și porturi | HTML, JUnit, business services |
| `atf-mock` | transport fake configurabil, catalog de rute și răspunsuri deterministe | HTTP real, Extent, JUnit, business validation |
| `atf-validation` | `ResponseContract`, reguli, evaluator, verdict și evidence prin port neutru | HTTP, Extent, HTML, CSS |
| `atf-execution` | rute, modele, API services, executor, lifecycle și cleanup | validare de contract, CSS, JavaScript |
| `atf-console` | adaptorul, formatterul diagnostic, politica de stil, nesting-ul, `ConsoleSettings` și loaderul YAML al consolei | HTML, Extent, verdicturi, transport |
| `atf-reporting` | integrarea Extent, documentul semantic, theme, fallback și publicarea artifactului | transport, business validation, verdicturi inventate |
| `atf-ui` | configurație UI validată și adaptorul scoped Playwright | HTML Extent, verdicturi, page objects de produs, teste executabile |
| `atf-debug-ui` | publicare read-only loopback pentru un HTML ales și target URL pentru TAB MCP | generare/modificare HTML, verdicturi, teste executabile |
| `atf-reporting` test scope | teste unitare, contract, componentă, scenarii API și E2E UI; fixture-uri, flows, locatori și adaptoare Playwright | implementarea framework-ului runtime și decizii de producție |
| `atf-tests` | guvernanță globală ATF: module boundary, Maven direction, import graph și process-state ownership | API/UI tests, fixture-uri, publisher-e sau suport de reporting |

### Direcția dependențelor

Săgeata înseamnă „modulul din stânga depinde de modulul din dreapta”.

```mermaid
flowchart LR
    Tests[atf-reporting test scope] --> Execution[atf-execution]
    Tests --> Mock[atf-mock]
    Validation[atf-validation] --> Core[atf-core]
    Mock[atf-mock] --> Core
    Execution[atf-execution] -.-> Validation
    Execution --> Core
    Console[atf-console] --> Core
    Reporting[atf-reporting] --> Core
    Ui[atf-ui] --> Core
    Tests --> DebugUi[atf-debug-ui]
    Tests --> Validation
    Tests --> Reporting
    Tests --> Console
    Tests --> Core
    Tests --> Ui
```

`atf-core` nu depinde de modulele superioare. `atf-validation` nu depinde de
`atf-reporting`; publicarea se face prin porturi neutre. `atf-execution` și
`atf-reporting` folosesc `atf-validation` numai ca adapter de test, marcat
`scope=test`; runtime-ul ambelor module rămâne decupabil peste `atf-core`.
`atf-reporting` nu depinde de `atf-execution` la runtime. Test scope-ul reporting
le compune în testele de integrare și acceptance, dar nu devine runtime
dependency a framework-ului. `atf-tests` rulează separat suita de guvernanță
globală și nu devine owner pentru scenarii API/UI sau infrastructură de reporting.

`atf-ui` este un modul de infrastructură, nu un modul de teste. Testele UI
executabile sunt în `atf-reporting/src/test/java/atf/ui/tests/extent`, iar `atf-ui`
furnizează numai configurația browserului și sesiunea Playwright. Composition
root-ul generated este
`atf.ui.tests.extent.e2e.section.SelectedApiTestsReportUiTest`: selectează
exact metode API prin `@ApiTestRef` (GET, POST JSON și lifecycle cu rezultate
positive/skip/fail), publică raportul API read-only și verifică secțiunile prin
Playwright. Nu există un al doilea smoke generated care să activeze implicit
toate familiile API. Pentru un raport UI deja existent există gate-ul opt-in
`StaticUiExtentReportContractTest`; inputul este read-only și nu este modificat.

`atf-debug-ui` are o responsabilitate diferită: publică temporar un artifact
HTML deja generat, numai pe loopback, pentru ca un agent să îl poată deschide
în TAB MCP și să îl investigheze manual. Nu este renderer, nu modifică
artifactul și nu devine owner al testelor.

Porturile comune sunt renderer-neutral. Extent este o implementare a boundary-
ului de reporting, iar consola este o proiecție diagnostică separată. Nici
`atf-reporting` nu depinde de implementarea JUnit sau de alte module superioare,
`atf-core` de implementări concrete. Composition root-ul injectează explicit
sink-ul dorit. Configurația YAML a consolei este responsabilitatea
`atf-console`; lifecycle-ul JUnit este inclus în același modul, iar configurația
Extent rămâne în `atf-reporting`.

## Configurația runtime

Fișierul `atf.yaml` aparține proiectului consumator și stă la rădăcina lui; nu
aparține unui singur modul. Loaderele citesc numai secțiunile de care sunt
responsabile: `atf-reporting` pentru `reporting.extent`, iar configurația
browser/API de audit este deținută local de `atf-reporting/src/test/resources`.
`atf-ui` furnizează infrastructura Playwright, iar `atf-debug-ui` folosește
launcher-ul explicit și nu modifică artifactul. `atf.yaml` nu este un container
global de opțiuni UI și nu conține `ui.enabled`.

Separarea țintă este:

| Responsabilitate | Owner | Regula |
|---|---|---|
| modele typed și snapshot-uri immutable | `atf-core` | fără YAML, filesystem, env sau `user.dir` în request |
| încărcarea și validarea sursei YAML | owner-ul secțiunii / proiectul consumator | o singură citire la bootstrap, cu erori fail-fast |
| API timeout/retry/auth/routes | `atf-core` + `atf-execution` | executorul primește doar snapshot-ul |
| output, timestamp și renderer | `atf-reporting` | consumă setările de reporting, nu API settings |
| override-uri de test | `atf-reporting` test scope / consumator | explicite, locale și fără mutarea configurației globale |
| catalog de rute și răspunsuri fake pentru reporting | `atf-reporting` test scope + `atf-mock` loader | `atf-reporting/src/test/resources/atf-mock.yaml`; nu aparține configurației generale |

În acest repository, composition root-ul de test este `atf-reporting/src/test`;
pentru un consumator extern este proiectul de teste al acelui consumator.
`atf-core` definește contractele, composition root-ul asamblează
configurația, iar reporting adaptează numai partea sa. `atf-reporting` păstrează
loaderul său pentru `reporting.extent`; acesta nu devine loaderul configurației
API complete. Parserul YAML nu se mută în `atf-core`, deoarece ar transforma
kernelul într-un owner de filesystem și surse externe.

Configurația este împărțită după ownership. `atf.yaml` este documentul general
ATF și conține politici API, validation și reporting; configurația UI pentru
testarea autonomă a raportării este `atf-reporting-ui.yaml` în test resources,
iar debug UI este pornit explicit de launcher-ul său:

```yaml
api:
  baseUri: https://gorest.co.in
  defaultAuthProfile: default
  timeout: PT30S
  retryStatuses: [429, 503]
  maxAttempts: 2
validation:
  warningMode: false
reporting:
  assertionMaxDisplayCharacters: 240
  bodyMaxDisplayCharacters: 12000
  console:
    enabled: true
    detailsEnabled: false
  extent:
    enabled: true
    outputDirectory: artifacts/reports
    fileName: ${REPORT_FILE_NAME}
    timestampEnabled: true
```

Catalogul operațional al transportului fake pentru acest proiect este separat
și este deținut de test scope-ul `atf-reporting`,
[`atf-reporting/src/test/resources/atf-mock.yaml`](atf-reporting/src/test/resources/atf-mock.yaml):

```yaml
mock:
  routes:
    - method: GET
      path: /public/v2/users
      status: 200
      body: '[{"id":1,"name":"ATF User"}]'
  fallback:
    status: 500
    body: '{"message":"Unexpected mock route"}'
```

Legătura este configurabilă fără a polua `atf.yaml`: `ReportingTestResources`
încarcă explicit resursa classpath pentru testele reporting, iar
`AtfMockConfigurationLoader` păstrează override-ul generic
`-Datf.mock.config=...` / `ATF_MOCK_CONFIG` pentru consumatori externi. O cale
explicită invalidă produce eroare; numai lipsa completă a unui fișier permite
fixture-ul built-in. Astfel rularea directă din IntelliJ, CI și rulările paralele
pot selecta cataloage diferite, fără ca un mock să poată activa HTTP live.

### Contractul de transport pentru testele acceptance

`ApiReportAuditSetup` folosește în mod intenționat `AtfMockTransport` din
modulul `atf-mock`, un fake determinist injectat în composition root. `ApiReportSetupFlow`
este doar o fațadă de compatibilitate. Setup-ul nu execută DNS, TLS sau
request-uri de rețea. De aceea `api.baseUri` din YAML descrie baseline-ul unei
compoziții API și URL-ul afișat în evidence, dar nu pornește HTTP și nu
schimbă transportul setup-ului UI.

Testele care trebuie să contacteze un serviciu real sunt opt-in prin:

```text
-Datf.liveApi.enabled=true
-Datf.liveApi.baseUri=https://host-ul-real
```

Această diferență este o invariantă de arhitectură, nu o convenție de naming:

```text
acceptance -> AtfMockTransport -> artifact reproductibil offline
live       -> HttpClientTransport -> endpoint HTTP disponibil
```

Nu adăugăm încă `transport: live` în YAML, deoarece loaderul YAML consumă
astăzi efectiv doar `reporting.extent`. Când configurația API va fi integrată,
transportul implicit trebuie să rămână `acceptance`, iar `live` să fie o
alegere explicită, validată o singură dată la composition root.

Secretele nu se păstrează în YAML; se injectează printr-un provider/env
explicit. Căile relative se rezolvă față de fișierul YAML, configurația se
validează strict, iar după bootstrap snapshot-ul nu se mai modifică și nu se
recitește pentru fiecare request.

`ExtentReportSession.openConfigured()` rămâne entry point-ul rendererului, iar
testele JUnit orientate pe resursă îl folosesc prin adaptorul comun
`ApiResourceTestBase`. Astfel `UsersApiTests` sau `PostsApiTests` pot fi rulate
direct din IntelliJ și pot produce artifactul Extent fără un runner separat.
`reporting.extent.enabled` activează/dezactivează numai publicarea HTML; nu
schimbă transportul, validarea sau verdictul testului. Loaderul de raportare
descoperă `atf.yaml` din working directory în sus, iar `outputDirectory` și
timestamp-ul decid destinația. Loaderul mock are propriul lanț de override și
descoperire; în acest repository catalogul nu mai există la root, ci este
fixture-ul test-owned `atf-reporting/src/test/resources/atf-mock.yaml`.

## Lifecycle și calculul rezultatului

```mermaid
flowchart LR
    Setup[Optional SETUP] --> Acc[Phase accumulator]
    Body[TEST BODY] --> Acc
    Teardown[Optional TEARDOWN] --> Acc
    Acc --> Outcome[scenario outcome]
    Outcome --> Evidence[phase outcomes + primary failure]
```

`ApiScenarioLifecycleRunner` păstrează ordinea fazelor, precedence-ul și cauza
primară; executorul de fază deține invocarea și maparea semnalelor. `TEARDOWN`
se încearcă atunci când este definit, chiar după failure/skip. Verdictul final
nu este recalculat de renderer.

Regula semantică este:

```text
FAIL > SKIP > WARNING > PASS
```

Warning-ul rămâne evidence și poate fi soft assertion; nu devine failure doar
pentru că este publicat în raport. Validarea și lifecycle-ul trebuie să păstreze
aceeași decizie în execuție și în artifact.

## Principii de design

- fiecare clasă are un owner unic și o responsabilitate observabilă;
- fațadele publice coordonează, nu absorb implementarea tuturor subsistemelor;
- modelele de domeniu sunt immutable unde reprezintă configurație, identity sau
  evidence;
- timpul, retry-ul și auth-ul sunt politici injectabile și testabile determinist;
- modulele renderer-neutral nu importă Extent, JUnit sau HTML;
- testele de contract protejează API-ul și semantica înaintea refactorizării;
- o abstracție nouă este justificată numai când reduce coupling sau duplicare;
- limita de clase per package este semnal de coeziune, nu motiv pentru directoare
  artificiale;
- documentația permanentă descrie contracte stabile, nu rapoarte intermediare.

## Unde găsești detaliile

- [docs/README.md](docs/README.md) — harta documentației și regulile comune;
- [docs/ATF_Architecture.md](docs/ATF_Architecture.md) — boundary-uri și
  dependențe detaliate;
- [atf-core/README.md](atf-core/README.md) — kernel, porturi și politici;
- [atf-execution/README.md](atf-execution/README.md) — API layer, services și
  lifecycle;
- [atf-validation/README.md](atf-validation/README.md) — contracte și
  evaluare renderer-neutrală;
- [atf-reporting/README.md](atf-reporting/README.md) — contractul vizual,
  conținutul și navigarea artifactului Extent;
- [atf-console/README.md](atf-console/README.md) — proiecție diagnostică,
  loaderul YAML și politica ASCII/UTF-8;
- [atf-tests/README.md](atf-tests/README.md) — guvernanța globală a modulelor ATF;
- [atf-reporting/src/test/java/atf/ui/tests/extent/README.md](atf-reporting/src/test/java/atf/ui/tests/extent/README.md)
  — contractul plug-and-play pentru setup API, secțiuni Extent și audit UI;
- [atf-ui/README.md](atf-ui/README.md) — configurația și adaptorul Playwright;
- [atf-debug-ui/README.md](atf-debug-ui/README.md) — publicarea read-only pentru investigație manuală în TAB MCP;
- [docs/ATF_API_TEST_GUIDE.md](docs/ATF_API_TEST_GUIDE.md) — pașii practici
  pentru adăugarea unui test API;
- [docs/ATF_Reporting_Contract.md](docs/ATF_Reporting_Contract.md) — arborele
  și contractul observabil al raportului;
- [docs/ATF_Reporting_Reference_Guard.md](docs/ATF_Reporting_Reference_Guard.md)
  — reperul vizual/funcțional și procedura de comparație;
- [docs/ATF_Build_And_Execution_Safety.md](docs/ATF_Build_And_Execution_Safety.md)
  — sync live, staging, build și verificare MCP;
- [docs/README_Atf_BestPractices.md](docs/README_Atf_BestPractices.md) —
  reguli OOP, testabilitate și mentenanță;
- [docs/feedback/README.md](docs/feedback/README.md) — ToDo-uri active și
  criterii de închidere validate prin cod și teste.
- [docs/feedback/ATF_Live_Analysis_2026-10-18.md](docs/feedback/ATF_Live_Analysis_2026-10-18.md)
  — snapshot de audit, scorecard și plan secvențial.

Documentele descriptive folosesc convenția `ATF_<PascalCase>`, cu acronimul
`ATF` doar în primul segment; forma canonică și exemplele sunt în
[docs/README_Atf_BestPractices.md](docs/README_Atf_BestPractices.md).

README-ul rădăcină nu repetă contracte de culori, layout, tab-uri, navigare,
staging sau validare vizuală. Acestea au un singur owner documentar în
`atf-reporting` sau în `docs/` și sunt referite de aici.
