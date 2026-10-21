# ATF Test Practice Shop — consumer plug-and-play

## Overall

- consumer configurabil pentru UI și API-ul Practice Software Testing;
- runtime reutilizabil în `src/main`;
- composition roots și teste în `src/test`;
- `atf-reporting` consumat ca bibliotecă standalone;
- `atf-console` folosit ca proiecție diagnostică, nu ca verdict;
- journey-uri E2E puține; variațiile rămân în component și API integration.

```mermaid
flowchart LR
    Config["config"] -->|încarcă| Runtime["consumer runtime"]
    Runtime -->|organizează| Pyramid["test layers"]
    Pyramid -->|publică| Evidence["report + console"]
```

<details>
<summary><strong>Legendă — rolul consumerului</strong></summary>

- **Config**: valori externe, profile de validare, rute relative și precondiții;
- **Runtime**: API client, page objects, flows, validators, capture și publisher;
- **Pyramid**: aceeași intenție este verificată la cel mai jos nivel posibil;
- **Evidence**: raportul Extent păstrează verdictul și evidence-ul, consola păstrează observațiile de execuție;
- modulul verifică Practice Shop, nu implementează rendererul Extent.

</details>

Surse de aprofundare:

- [`atf-core`](../atf-core/README.md) — modele și contracte neutre;
- [`atf-ui`](../atf-ui/README.md) — boundary-ul Playwright;
- [`atf-reporting`](../atf-reporting/README.md) — biblioteca de raportare;
- [`atf-console`](../atf-console/README.md) — diagnostic runtime;
- [`atf-test-kit`](../atf-test-kit/README.md) — meta-anotații și convenții de test;
- [bune practici de documentație](../docs/README_Atf_DocumentationBestPractices.md) — stil și guvernanță README.

### Contract live și limite reale

| Suprafață | Comportament curent | Interpretare |
|---|---|---|
| component/API | contracte typed pentru config, routes, JSON, status, mapping și evidence | matricea de variații se verifică fără browser |
| guest E2E | purchase complet `product → cart → checkout → billing → payment → confirmation` | proof business prin starea UI și exchange-urile live observate |
| authenticated E2E/API | selectat prin adnotare și condiție typed; configurația curentă îl dezactivează explicit | `SKIP` este precondiție/opt-in lipsă, nu PASS degradat; fișierul existent nu dovedește autentificarea live |
| retry Playwright | suport generic fail-closed există în `atf-ui`; YAML-ul Practice Shop declară explicit `maxAttempts`, `backoff`, `eligibleOperations` și `reason` | policy-ul implicit rămâne un singur attempt; numai operații read-only typed pot fi activate ulterior |
| API evidence binding | `validation.apiEvidence` selectează checkpoint-uri typed (`product`, `login`, `cart`, `payment`, `order`); override-ul system property este acceptat | binding-ul fail-closed aparține consumerului; publisher-ul primește numai evidence selectat |
| Overview → System | snapshot allowlist-uit, redacted și bounded; API precondition și starea de configurare UI sunt rezumate separat | nu se publică dump YAML, secrete, path-uri locale, identitatea suitei/testului, pași sau date de checkout |
| output | `artifacts/reports/ui` și `artifacts/reports/api` | rolurile UI/API nu se suprapun |

Contracte și limite curente:

- binding-ul per checkpoint, aggregate-ul nested, diagnosticele bounded și
  exchange-urile canonicale multiple sunt verificate;
- `authenticatedE2eEnabled` este `false` în configurația curentă. Fixture-ul
  sintetic este deținut de modul în
  `src/test/resources/practice-software-testing-account.properties`; loader-ul
  și configurația typed acceptă numai această zonă de ownership. Existența și
  validitatea fixture-ului permit setup-ul, dar nu dovedesc autentificarea live;
- `-Datf.practice.account-file` și `ATF_PRACTICE_ACCOUNT_FILE` pot selecta
  fixture-ul numai în interiorul `src/test/resources`; căile externe sunt
  respinse. Preflight-ul respinge domeniile rezervate `example`, `example.com`,
  `example.net`, `example.org`, `.test`, `.invalid` și `.localhost` (inclusiv
  subdomeniile), dar nu validează contul remote;
- authenticated E2E/API rămân `SKIP` până la opt-in explicit și dovada
  login reușită;
- `Account fixture policy` verifică ownership-ul și redaction-ul independent de
  journey. `Account setup` descrie folosirea per-rulare: guest-ul poate avea
  fixture-ul disponibil fără să-l consume, iar lipsa lui este permisă; flow-ul
  authenticated îl cere și îl încarcă;
- retry-ul generic al mutațiilor este exclus de `atf-ui`; Practice Shop nu reia
  click-ul, submit-ul sau purchase-ul după timeout.

### Network inspector UI — configurabil, fără replay

Practice Shop folosește inspectorul generic din `atf-ui`, nu un listener local
în test. Configurația urmărită îl activează pentru profilul `ui-api`; `ui-only`
nu publică API evidence.

```text
Playwright response / request-failed
  → UiNetworkInspector
  → PracticeShopApiNetworkJournal
  → UI checkpoint / API validation
  → request · response · execution evidence
```

Configurația este în `src/test/resources/atf-practice-shop.yaml` și limitează
colectarea la host-ul API configurat:

```yaml
networkInspector:
  enabled: true
  allowedHosts: [api.practicesoftwaretesting.com]
  maxObservations: 256
  maxBodyPreviewCharacters: 12000
  collapseConsecutiveSignatures: true
```

Inspectorul publică method, safe path, status, service, mechanism, auth
method/source și signature, fără credentiale și fără body nelimitat. Journal-ul
consumerului păstrează trace-ul ordonat, iar aggregate-ul de journey rămâne un
singur nod tehnic collapsed; nu se repetă propoziția Gherkin pentru UI și API.
`observations()` este lossless pentru audit, iar stream-ul de prezentare
colapsează numai semnăturile consecutive.

- Un request eșuat înainte de response rămâne evidence explicită: status
  `Unavailable`, motiv bounded, fără `RESPONSE JSON` inventat.
- Adapterul Practice Shop poate atașa un alias de serviciu; host allowlist-ul
  și semnătura generică rămân neschimbate.
- Inspectorul publică numai exchange-urile relevante contractelor configurate;
  probele ambientale de sesiune ale aplicației externe nu sunt evidence ATF.
- `assertClean()` face testul să eșueze pentru erori runtime neașteptate;
  două excepții bootstrap rămân însă în cod și sunt tracked ca cleanup, nu ca
  politică de business.

Runtime-ul și API-ul au roluri separate:

- probele ambientale de sesiune ale aplicației externe nu sunt contract API;
  aplicația le poate iniția, iar consumerul nu le publică drept evidence;
- inspectorul de journey primește numai exchange-uri API și checkpoint-urile
  lor; nu conține diagnostice browser ca pași sau copii ai unui pas Gherkin;
- erorile runtime neașteptate rămân failure; codul încă suprimă 401 înainte de
  sesiunea autentificată dovedită și mesajul `cart_items`. Eliminarea excepțiilor
  este urmărită în [feedback-ul E2E](../docs/feedback/ATF_Practice_Shop_E2E_Feedback.md);
- diagnosticele nu devin pași Gherkin, `API validation` sau warning-uri de
  business; raportul MCP curent arată `Technical diagnostics · 0`;
- guest și authenticated purchase publică `PASS` după validarea journey-ului și
  `assertClean()`; diagnosticele runtime nu adaugă pași sintetici și nu
  retrogradează scenariul la `WARNING`.

Excluderea de evidence și agregarea runtime sunt verificate fără browser de
`PracticeShopApiNetworkJournalTest` și `PracticeShopRuntimeDiagnosticsTest`.

Settlement-ul pentru acțiuni care modifică serverul este separat și dezactivat
implicit:

- cursorul este capturat înainte de acțiune; read-back-ul bounded caută numai
  observații ulterioare prin `awaitAfter(...)` și nu repetă acțiunea;
- inspectorul nu reia mutații, indiferent de timeout sau de observația capturată;
- `PracticeShopUiNetworkSettlementFixture` acoperă read-back-ul idempotent,
  excluderea unui GET anterior acțiunii, compactarea response-urilor
  consecutive și timeout-ul fără replay sau false-green;
- o viitoare facilitate consumer-owned ar cere separat identity stabilă,
  payload complet asociat și idempotency garantată de server; această facilitate
  nu este activată în Practice Shop.

Contractul generic și criteriile pentru o posibilă extindere sunt în
[README-ul `atf-ui`](../atf-ui/README.md); configurația și comportamentul
consumerului rămân documentate aici.

## Harta internă a consumerului

```mermaid
flowchart LR
    Config["config"] -->|furnizează| Routes["routes + locators"]
    Routes -->|alimentează| Pages["page objects"]
    Pages -->|compun| Flows["semantic flows"]
    Flows -->|dețin| Roots["test composition roots"]
    Roots -->|publică| Evidence["report + console"]
```

<details>
<summary><strong>Legendă — direcția responsabilităților</strong></summary>

- `config/` deține valori typed și loaderul YAML;
- `api/` deține request factory, client, screen APIs și vocabularul wire;
- `routes/` și `locators/` sunt ownerii selectorilor și rutelor relative;
- `pages/` execută interacțiuni semantice pe un ecran;
- `flow/` compune journey-uri și stage-uri business;
- `support/` deține lifecycle, audit, capture, evidence și publisher;
- testele doar compun și verifică; nu reimplementează aceste responsabilități.

</details>

### Boundary-uri și ownership

| Zonă | Owner live | Regula de mentenanță |
|---|---|---|
| host UI și output | `PracticeShopConfiguration` | un singur `baseUrl`; directoare configurabile |
| rute UI | `PracticeShopUiRoutes` | numai segmente relative; ID-urile sunt validate |
| rute API | `PracticeShopApiRoutes` | path-uri și template-uri centralizate |
| selectorii CSS | `PracticeShopLocators` | testele nu folosesc `page.locator(...)` direct |
| contracte de ecran | `@PracticeShopPageContract` + `PracticeShopPageValidator` | topologia este declarativă, assertions sunt typed |
| flow-uri | `PracticeShop*Flow` și stage-urile purchase | business order, fără detalii de renderer |
| API evidence | journal + capture + selector + mapper | orchestration, selecție și publicare separate |
| lifecycle | `PracticeShopE2eContext` / publisher | testele nu gestionează manual raportul |
| scanări de arhitectură | `AtfPathLocator` + `AtfFileTree` din `atf-core` | root discovery și file traversal sunt reutilizate; filtrele include/exclude specifice consumerului rămân lângă guard-ul Practice Shop |

Packaging-ul este intenționat:

- clasele reutilizabile rămân în `src/main/java/atf/ui/practiceshop`;
- `src/test/java` conține teste, fixture-uri și support strict test-only;
- `atf-core`, `atf-console`, `atf-test-kit`, `atf-reporting` și `atf-ui` sunt
  compile dependencies ale runtime-ului consumerului;
- `atf-mock` nu este dependency al modulului; transportul determinist pentru
  component tests rămâne local și strict test-scope;
- `atf-reporting` nu primește dependență inversă către Practice Shop.

## Piramida de testare

```mermaid
flowchart LR
    Component["component"] -->|acoperă| ApiIntegration["API integration"]
    ApiIntegration -->|selectează| E2e["selected E2E"]
    E2e -->|produce| Artifact["auditable evidence"]
```

<details>
<summary><strong>Legendă — niveluri, izolare și proof</strong></summary>

- **component** — verifică rapid și determinist config, routes, locators, JSON,
  mapping, status, redaction și invariants;
- **API integration** — verifică contracte reale și precondiții autentificate,
  numai cu API live configurat explicit;
- **E2E** — verifică wiring-ul, journey-ul și randarea observabilă prin
  Playwright; nu reproduce matricea de variații a componentelor;
- **support** — fixture-uri și transport determinist strict test-only, fără
  business logic ascuns;
- **evidence** — rezultatul observabil este publicat în Extent, iar diagnosticul
  de execuție rămâne separat în consolă.

| Nivel | Suprafață curentă | Browser/network | Scop |
|---|---|---|---|
| component | `src/test/.../component` și `api/component` | nu | contracte și variații locale |
| API integration | `src/test/.../api/integration` | API live, explicit | precondiții și contracte reale |
| E2E | `src/test/.../e2e` | Playwright + aplicația reală | wiring, journey, randare |
| support | `src/main/.../support` + `src/test/.../api/support` | nu implicit | lifecycle, evidence, fixture-uri |

</details>

Principiul de selecție:

- variațiile de endpoint, status, body și mapping se testează component-first;
- integration este opt-in prin configurație explicită;
- E2E demonstrează orchestration-ul real, nu fiecare combinație de API;
- nu se adaugă un test E2E pentru o variație care poate fi verificată fără browser.

### E2E configurabile

| Journey | Clasă | Activare |
|---|---|---|
| guest purchase | `PracticeShopPurchaseE2eTest` | implicit pentru configurația UI validă |
| authenticated purchase | `PracticeShopAuthenticatedPurchaseE2eTest` | `authenticatedE2eEnabled` + precondiție `authenticated` + account fixture |
| account/catalog smoke | `PracticeShopAccountAndCatalogE2eTest` | aceeași activare explicită; smoke slice separat |

Testele autentificate rămân intenționat ignorate fără opt-in și un fixture valid.
Instrucțiunile de rulare manuală sunt colapsate:

<details>
<summary>Rulare manuală autentificată — IntelliJ / Maven</summary>

Ambele clase reutilizează același cont de test deja înregistrat; suite-ul nu
creează conturi noi la fiecare rulare. Înainte de prima rulare, înregistrează
un cont dedicat pe Practice Shop și păstrează datele numai în fișierul local
ignorat de Git:

```text
atf-test-practice-shop/src/test/resources/practice-software-testing-account.local.properties
```

Formatul fișierului:

```properties
email=<registered-test-account-email>
password=<registered-test-account-password>
firstName=<first-name>
lastName=<last-name>
```

Pentru IntelliJ, selectează ambele clase
`PracticeShopAccountAndCatalogE2eTest` și
`PracticeShopAuthenticatedPurchaseE2eTest`; adaugă în **VM options**:

```text
-Datf.practice.authenticated-e2e=true
-Datf.practice.account-file=atf-test-practice-shop/src/test/resources/practice-software-testing-account.local.properties
```

Comanda Maven de mai jos rulează ambele clase:

```text
mvn -pl atf-test-practice-shop -am "-Dtest=PracticeShopAccountAndCatalogE2eTest,PracticeShopAuthenticatedPurchaseE2eTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Datf.practice.authenticated-e2e=true" "-Datf.practice.account-file=atf-test-practice-shop/src/test/resources/practice-software-testing-account.local.properties" test
```

Fișierul local trebuie să conțină un cont înregistrat, eligibil pentru login
live; existența lui singură nu garantează autentificarea. Este acceptat numai în
`src/test/resources`, iar sufixul `.local.properties` este exclus din Git. Nu
introduce credențiale reale în `practice-software-testing-account.properties`:
acel fixture este urmărit de Git. În IntelliJ, **Working directory** trebuie să
fie root-ul repository-ului. Testele efectuează login live; purchase E2E poate
crea o comandă de test în aplicația demo.

</details>

- `@WithE2eHarness` injectează harness-ul ca parametru metodei de test.
- Extensia JUnit creează configurația, contextul, browserul și locatorii per metodă, apoi închide contextul și raportul la teardown.
- `harness.run(title, overview, journey -> ...)` gestionează lifecycle-ul scenariului și publicarea failure-ului.
- Testul păstrează flow-ul business și assertions observate; dacă teardown-ul eșuează, eroarea se atașează ca `suppressed`, iar failure-ul business rămâne principal.

Scurtarea composition root-urilor nu scurtează journey-ul de purchase:

- guest E2E păstrează în aceeași rulare `product → cart → checkout → billing
  → payment check → order confirmation`; `POST /payment/check` validează metoda
  selectată, dar nu creează comanda. Utilizatorul confirmă explicit comanda;
  checkpoint-ul final verifică mesajul și factura din UI corelate cu răspunsul
  `201 POST /invoices/guest`;
- authenticated purchase cere opt-in, fixture acceptat de policy și login
  confirmat; păstrează același flow complet până la confirmarea comenzii;
- checkout-ul authenticated identifică butonul semantic de continuare
  (`AUTHENTICATED_PROCEED`) și verifică starea lui observată; textul generic al
  body-ului nu este contract de UI, fiindcă extractorul poate omite conținutul
  din afara tabelului;
- numai lifecycle-ul, wiring-ul și publicarea diagnosticelor se pot muta în
  support; pașii business și assertions finale nu se elimină și nu se
  înlocuiesc cu un smoke de cart/catalog.

### Contractul Gherkin al purchase journey

Keyword-urile și titlurile statice sunt deținute de enum-ul consumerului
`PracticeShopPurchaseTraceStep`; datele dinamice (produs, cantitate, rută și
exchange-uri) rămân în evidence. Renderer-ul generic nu rescrie pașii
Practice Shop.

| Ordine | Checkpoint guest | Keyword |
|---:|---|---|
| 1 | catalog home — navigation, branding, banner | `GIVEN` |
| 2 | filtre catalog | `AND` |
| 3 | product grid | `AND` |
| 4 | product page | `AND` |
| 5 | cart și cantitatea cerută | `WHEN` |
| 6 | guest checkout | `AND` |
| 7 | billing | `AND` |
| 8 | payment method | `AND` |
| 9 | payment check | `AND` |
| 10 | order confirmation | `THEN` |

- Secvența guest este `1 GIVEN → 1 WHEN → 7 AND → 1 THEN`.
- `SETUP` este separat de `TEST BODY`; nu intră în cei zece checkpoint-uri.
- `UI validation`, `API validation` și network aggregate nu creează pași Gherkin suplimentari; UI/API sunt copii ai aceluiași checkpoint.
- `AUTHENTICATED_CHECKOUT` aparține ramurii authenticated, în afara acestei cardinalități guest.

Regresia locală a vocabularului rulează fără browser sau trafic extern:

```text
mvn -pl atf-test-practice-shop -am "-Dtest=PracticeShopPurchaseTraceStepTest,PracticeShopRuntimeDiagnosticsTest,PracticeShopApiNetworkJournalTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

E2E-ul guest verifică wiring-ul și ordinea efectiv publicată într-un raport
fresh; nu înlocuiește testul component și nu extinde această regulă la alți
consumatori.

<details>
<summary><strong>Legendă — rezultate E2E și structură test-kit</strong></summary>

- guest și authenticated purchase publică `PASS` după business complet și fără
  erori runtime neașteptate; probele ambientale sunt în afara inspectorului;
- erorile neașteptate rămân `FAIL`; bootstrap-ul extern cunoscut nu generează
  warning de scenariu sau pas Gherkin suplimentar;
- integration și journey-urile autentificate inactive sunt `SKIP`, nu `PASS`;
- `component` păstrează matricea de status/body/query; E2E păstrează doar
  journey-uri cu valoare de wiring și randare;
- `src/main` conține suportul reutilizabil al consumerului;
- `src/test` conține composition roots, fixtures și transport local test-only.

```text
src/test/java/atf/ui/tests/practiceshop
├── component/          config, routes, locators, contracts, diagnostics
├── api/
│   ├── component/      request, status, JSON, mapping, publisher
│   ├── integration/    API live opt-in flows
│   └── support/        fixture-uri și transport local
└── e2e/                guest și authenticated composition roots
```

</details>

### Contractul unui test Practice Shop

```text
metadata + config → fixture → flow semantic → stare observată → evidence/verdict
```

- composition root-ul leagă configurația, contextul și flow-ul; nu reconstruiește
  browser, raport, locatori sau transport;
- `expected` vine din configurație/fixture, iar `actual` vine din UI, API sau DOM;
- assertions-urile verifică efecte observate; nu se acceptă `expected == actual`,
  `true` hardcodat sau URL validat prin fragment arbitrar;
- fiecare journey E2E poate rula independent; un context partajat cere reset
  explicit și ownership vizibil;
- variațiile de status, body, headers, query și selector rămân în component/API
  integration, nu sunt multiplicate în browser;
- contractele HTML folosesc un helper semantic pentru structură, ordine și
  redaction; `contains` rămâne doar pentru smoke text sau secrete interzise;
- lifecycle-ul browserului, API clientului, raportului, screenshot-ului și
  retry-ului aparține support/harness-ului, nu corpului testului;
- selecția se face prin adnotare și configurație typed; nu se copiază metode
  `isConfigured()` în fiecare composition root.

<details>
<summary><strong>Model minim pentru composition root</strong></summary>

```text
@AtfE2eTest
test()
  → load once
  → open context once
  → execute configured flow
  → assert observed state
  → support closes resources
```

Un test care conține detalii de renderer, directoare de raport, selectori,
retry sau cleanup manual a depășit boundary-ul composition root-ului.
</details>

Contul nu este o condiție pentru journey-ul guest. Pentru authenticated, setup-ul
API este precondiție și parola rămâne în afara actions, assertions, consolă și HTML.

## Configurare plug-and-play

Sursa principală este
`src/test/resources/atf-practice-shop.yaml`. Override-urile de proces sunt
rezolvate de `PracticeShopConfigurationLoader`; nu se modifică flow-urile pentru
a schimba mediul.

```mermaid
flowchart LR
    Yaml["atf-practice-shop.yaml"] -->|încarcă| Loader["configuration loader"]
    Loader -->|creează| Snapshot["immutable configuration"]
    Snapshot -->|configurează| Policy["validation + API precondition"]
    Snapshot -->|rezolvă| Paths["relative routes + report paths"]
    Policy -->|ghidează| Roots["test composition roots"]
```

<details>
<summary><strong>Legendă — configurație, policy și composition root</strong></summary>

- **YAML** — input extern; nu conține logică de flow;
- **loader** — parsează, validează și raportează cheia invalidă;
- **snapshot** — record immutable folosit de main și test scope;
- **policy** — decide profile-ul UI/API, precondiția și capture policy;
- **roots** — testele compun obiectele; nu rezolvă manual `baseUrl` sau
  lifecycle-ul raportului.

</details>

```yaml
practiceShop:
  baseUrl: "<ui-base-url>"
  browser: chromium
  headless: false
  timeout: PT20S
  navigationTimeout: PT45S
  viewport:
    width: 1440
    height: 1000
  zoomPercent: 70
  validation:
    profile: ui-api
    apiEvidence: [product, login, cart, payment, order]
  networkInspector:
    enabled: true
    allowedHosts: [api.practicesoftwaretesting.com]
    maxObservations: 256
    maxBodyPreviewCharacters: 12000
    collapseConsecutiveSignatures: true
    settlement:
      enabled: false
      timeout: PT10S
      pollInterval: PT0.1S
  retry:
    maxAttempts: 1
    backoff: PT0S
    eligibleOperations: []
    reason: bounded read-only UI retry
  api:
    endpoint: "<api-endpoint>"
    mode: live
    precondition: authenticated
    timeout: PT20S
  reports:
    ui: artifacts/reports/ui
    api: artifacts/reports/api
  reporting:
    systemBodyPreviewCharacters: 100
  authenticatedE2eEnabled: false
  screenshotOnFailure: true
```

Fragmentul reflectă policy-ul urmărit în YAML; endpoint-urile și datele de
checkout sunt configurate separat și nu sunt necesare pentru a înțelege aceste
limite.

| Configurație | Semnificație |
|---|---|
| `validation.profile` / `apiEvidence` | `ui-api` publică exchange-urile selectate la checkpoint; `ui-only` oprește capture subscription și API evidence în raportul E2E |
| `api.mode` | `live` folosește HTTP real; testele component injectează transport determinist direct, nu simulează live prin YAML |
| `api.precondition` | condiția API configurată este independentă de browser; repo-ul o setează la `authenticated`, dar `authenticatedE2eEnabled: false` lasă purchase-ul UI implicit guest |
| `networkInspector` | capturează numai host-urile permise; preview-ul de 12.000 caractere este pentru checkpoint evidence, nu pentru Overview/System |
| `retry` | un singur attempt și nicio operație eligibilă; mutațiile UI nu sunt reluate |
| `reporting.systemBodyPreviewCharacters` | limitează la 100 caractere preview-ul de body din System; disclosure-urile de evidence au limita lor separată |
| `reports.ui` / `reports.api` | directoare relative distincte pentru artifactele UI și API |
| `productPath` | `auto` acceptă numai ruta `/product/*`; o rută exactă e fail-fast dacă nu există |
| `screenshotOnFailure` | switch legacy mapat de `PracticeShopScreenshotPolicy`: `true` → `EACH_CHECKPOINT`, `false` → `FAILURE_ONLY`; `FINAL_ONLY` nu este încă configurabil în acest consumer |
| `accountFile` | fixture sintetic owned în `src/test/resources`; căile externe sunt respinse; disponibilitatea nu dovedește autentificarea live și credentialele nu ajung în evidence |

Pentru un raport E2E UI-only, profilul YAML este:

```yaml
practiceShop:
  validation:
    profile: ui-only
```

- `ui-only` publică UI evidence fără `API validation` sau network aggregate;
- traficul browserului către aplicație continuă normal — profilul nu blochează
  request-uri și nu schimbă testele API standalone;
- `ui-api` publică exchange-urile din `validation.apiEvidence`.

Flow presentation pentru Practice Shop:

- `ReportEvidenceFlowMode.UNIFIED_FLOW` există ca metadata, dar nu colapsează
  nodurile Extent;
- `FINAL_ONLY` există în reporting generic, însă consumerul îl mapează încă prin
  booleanul legacy `screenshotOnFailure`;
- configurarea unui singur business step și a unei capturi finale este urmărită
  în [Practice Shop E2E Feedback](../docs/feedback/ATF_Practice_Shop_E2E_Feedback.md).

`api.precondition` descrie setup-ul API, nu autentificarea sesiunii din browser.
Precondiția API configurată nu transformă guest purchase într-un journey UI
autentificat.

Reguli de configurare:

- testele și page objects folosesc rute relative din `PracticeShopUiRoutes`;
- `PracticeShopConfiguration.uri(...)` este singurul resolver `baseUrl + route`;
- output-ul este separat pe tip de artifact și rămâne relativ la workspace:
  ```text
  atf/artifacts/reports/
  ├── ui/   # rapoarte Extent produse de flow-urile Playwright
  └── api/  # rapoarte Extent produse de flow-urile API
  ```
- `reports.ui` și `reports.api` nu se suprapun și nu primesc căi absolute din
  YAML; loaderul le rezolvă la boundary-ul configurației;
- ID-urile de produs și cart sunt dinamice și nu intră în enum-uri;
- profile-ul este typed (`UI_API`, `UI_ONLY`), fără matrice de booleeni paraleli;
- loaderul eșuează clar la URI, timeout, path sau combinație de precondiții invalidă.
- switch-ul existent este doar mapping-ul de configurație al consumerului;
  profile-ul typed și `PracticeShopBrowserCaptureProvider` sunt contractul de
  execuție. Inclusiv calea opțională authenticated cere captura finală prin
  profil, nu direct din test. Alte politici generice există în reporting, dar
  acest YAML nu le expune încă drept opțiuni Practice Shop.
- `Overview → System/Environment → Screenshot policy` afișează
  `configValue()` din profilul efectiv; nu deduce o etichetă din booleanul
  legacy, astfel metadata și imaginile publicate nu se contrazic.

## Contracte UI și selectors

```mermaid
flowchart LR
    Contract["page contract"] -->|ghidează| Validator["screen validator"]
    Validator -->|folosește| Locators["semantic locators"]
    Locators -->|produce| Assertions["typed assertions"]
    Assertions -->|publică| Checkpoint["checkpoint evidence"]
```

<details>
<summary><strong>Legendă — contract UI și boundary-ul locatorilor</strong></summary>

- **page contract** — descrie topologia și intenția unui screen;
- **screen validator** — transformă starea observată în assertions typed;
- **semantic locators** — sunt centralizate în `PracticeShopLocators`;
- **typed assertions** — decid verdictul, nu doar existența unui nod DOM;
- **checkpoint evidence** — păstrează acțiuni, observations, API evidence și
  screenshot conform policy-ului.

</details>

Ecranele declarate în `PracticeShopScreen` și în contractele page object sunt:

| Screen | Rută relativă | Contract |
|---|---|---|
| `HOME` | `/` | catalog, filtre, grid și footer |
| `PRODUCT` | `/product/*` | produs, preț, descriere și add-to-cart |
| `CART` | `/checkout` | produs, cantitate și proceed |
| `GUEST_CHECKOUT` | `/checkout` | identitate guest și submit |
| `BILLING` | `/checkout` | address fields și proceed |
| `PAYMENT` | `/checkout` | payment method și finish |
| `CONFIRMATION` | `/checkout` | confirmarea plății |
| `LOGIN` | `/auth/login` | credential controls |
| `ACCOUNT` | `/account` | account identity |
| `REGISTRATION` | `/auth/register` | registration controls |

Reguli pentru selectors:

- `PracticeShopLocators` este singurul owner al selectorilor;
- se preferă locatorii semantici și forma stabilă `element[attribute=value]`;
- pentru un produs configurat se folosește `PracticeShopProductSelector`, cu
  `a[data-test^='product-']:has([data-test='product-name']:text-is(...))`;
  potrivirea exactă este obligatorie pentru a nu selecta accidental un produs
  cu nume parțial, indisponibil sau cu alt contract de cumpărare;
- clasele CSS generate, textul parțial și XPath-ul ambiguu nu sunt fallback-uri;
- valorile dinamice inserate în CSS trec prin `PracticeShopCssEscaper`;
- țara de billing acceptă numele sau codul opțiunii configurate, apoi selectează
  după label-ul vizibil; `PracticeShopCountryOptionTest` protejează potrivirea și
  diagnosticul care păstrează numele împreună cu codul;
- `PracticeShopArchitectureTest` apără boundary-ul și scanează și package-ul `component`.

### Validare pe checkpoint

Purchase-ul guest păstrează un număr mic de teste și mai multe dovezi locale:

- HOME poate fi împărțit semantic în structură, filtre și product grid;
- PRODUCT, CART, GUEST, BILLING, PAYMENT și CONFIRMATION rămân checkpoint-uri
  complete, fără fragmentare artificială;
- fiecare checkpoint publică assertions, acțiuni, audit observations și screenshot
  conform configurației;
- `fill → validate → submit` este regula pentru formulare;
- un control apărut după submit se validează în checkpoint-ul următor, nu retroactiv.

## API screen flow și evidence

```mermaid
flowchart LR
    Routes["route vocabulary"] -->|alimentează| Factory["request factory"]
    Factory -->|construiește| ScreenFlow["screen API flow"]
    ScreenFlow -->|înregistrează| Journal["network journal"]
    Journal -->|publică| Evidence["request · response · execution"]
```

<details>
<summary><strong>Legendă — ownerii API și proiecția evidence</strong></summary>

- `PracticeShopApiRoutes` — path-uri, prefixe și template-uri;
- `PracticeShopApiRequestFactory` — request și serializare JSON;
- `PracticeShopApiScreenFlow` — ordinea semantică a operațiilor;
- `PracticeShopApiTrafficCapture` — host filtering și sanitizare;
- `PracticeShopApiBodyEvidence` — marker bounded pentru body indisponibil și cauza transportului;
- `PracticeShopLoginBodyRecovery` — boundary typed `recovered/unavailable` pentru recovery-ul opțional;
- `PracticeShopApiExchangeSelector` — selecție pură a exchange-urilor;
- `PracticeShopApiNetworkJournal` — orchestration și sequence/phase;
- `PracticeShopApiAssertions` — status, JSON și headers;
- `PracticeShopApiEvidenceMapper` — proiecția în evidence Extent;
- `PracticeShopApiReportPublisher` — artifact API standalone.

Rutele live sunt grupate pe intenție, nu repetate în fiecare test:

| Domeniu | Contracte |
|---|---|
| auth | `POST /users/login` |
| catalog | `GET /products/search`, `GET /products/{id}`, `GET /categories` |
| cart | `POST /carts`, `GET /carts/{id}`, update quantity, remove product |
| checkout | `GET /postcode-lookup`, `POST /payment/check`, `POST /invoices/guest` |

</details>

Variațiile de status, body și query rămân în component tests. Nu se introduce
catalog mock sau dependency `atf-mock` în acest consumer. Integration folosește
API-ul live numai prin opt-in explicit.

Regula de evidență este strictă: fixture-ul deterministic este permis pentru
contracte component, variante și erori izolate; un raport prezentat ca dovadă
a flow-ului business trebuie să publice provenance `LIVE_HTTP` și să compare
datele reale ale produsului, cart-ului și payment-ului. Un artifact cu
`ACCEPTANCE_MOCK` nu este verdict business, chiar dacă assertions-urile
structurale sunt verzi.

Separarea raporturilor este obligatorie:

| Scop | Owner | Transport | Verdict |
|---|---|---|---|
| contract/variante | `api/component/PracticeShopApiContractReportComponentTest` | fixture deterministic local | contract component; niciodată dovadă business |
| flow business | `api/integration/PracticeShopStandaloneApiReportTest` | `PracticeShopApiClient.live(...)` | numai `LIVE_HTTP`, cu assertions exacte pe login/catalog/cart/payment |

Raportul live este fail-closed: provenance `ACCEPTANCE_MOCK` sau `UNKNOWN`,
un status non-2xx, o validare business eșuată ori un exchange lipsă propagă
failure-ul în `TEST BODY` și în tab-ul `Fails`. Dacă login-ul live nu trece,
se publică numai evidence-ul exchange-ului real și testul se oprește; nu se
continuă cu cart/payment simulate. Rularea explicită este:

```text
mvn -pl atf-test-practice-shop -am "-Dtest=PracticeShopStandaloneApiReportTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Datf.practice.authenticated-e2e=true" "-Datf.practice.api-mode=live" test
```

Raportul live se verifică în MCP pe `Tests`, `Fails` și `Overview`, folosind un
artifact proaspăt și configurația rulării; un `401` este evidence de precondiție externă eșuată,
nu un PASS degradat și nu un motiv pentru a activa mock-ul.
În cazul unui failure UI real, `atf-reporting` proiectează în `Fails` cardul
`Failure evidence` și cauza concretă înaintea unei validări semantice verzi;
Practice Shop nu adaugă assertions artificiale pentru a influența această
proiecție.

### API evidence în UI

```mermaid
flowchart LR
    Checkpoint["un singur pas Gherkin"] -->|conține| Payload["UI + API evidence"]
    Payload -->|selectează| Capture["screenshot policy"]
    Capture -->|creează| Extent["Extent step"]
```

<details>
<summary><strong>Legendă — payload-ul checkpoint-ului și publicarea</strong></summary>

- **checkpoint** este singurul nod semantic al propoziției Gherkin;
- **payload** grupează separat copii `UI validation` și `API validation`, plus
  assertions, audit observations și evidence-ul selectat;
- **screenshot policy** decide dacă payload-ul primește captură;
- **Extent step** publică rezultatul observabil;
- **assertions** decid verdictul, iar observations explică evidence-ul;
- fiecare exchange păstrează route, method, status, auth, transport, attempt și
  phase;
- request/response JSON rămân în disclosure-urile API, nu sunt duplicate în
  audit;
- body-urile și headers sensibile sunt redacted înainte de publicare;
- `body unavailable` este explicit și include sursa/cauza bounded, niciodată token inventat; un status HTTP non-2xx din recovery rămâne în evidence cu ruta și statusul, nu este degradat la body gol;
- recovery-ul opțional de login nu întoarce body gol și nu înghite failure-ul: publică
  evidence `unavailable`, iar browser exchange-ul rămâne verdictul autoritativ;
- retry-urile rămân distincte prin sequence și signature;
- signature-ul și digestul body-ului sunt folosite intern pentru corelare, dar
  HTML-ul public afișează numai starea bounded `body=present|absent` și markerul
  `internal digest hidden`, niciodată SHA-256-ul brut;
- exchange-ul malformed este omis din audit și nu produce evidence falsă.
- aceeași propoziție Gherkin nu se publică a doua oară doar pentru că are și
  evidence API; un pas poate avea copii diferiți UI și API, dar nu frați cu
  același titlu și aceeași intenție.

</details>

### Traceability contract pentru API în UI

Fiecare exchange publicat în checkpoint păstrează metadata causală comună cu
publisher-ul API.

- `EVENT ID`, `SEQUENCE`, `PHASE`, `ROLE`, `STEP ID`, `INITIATOR`, `CORRELATION`;
- `REQUEST SIGNATURE`, body-state marker, source, `EXPECTED`, `OBSERVED`;
- signature-ul intern = method + safe target + digestul body-ului JSON
  canonicalizat; HTML-ul public proiectează numai `body=present|absent`, iar
  digestul brut rămâne ascuns;
- obiectele JSON sunt sortate, array-urile își păstrează ordinea, iar body absent
  este diferit de `{}`.

<details>
<summary><strong>Aggregate-ul bounded al journey-ului</strong></summary>

- `authentication`, `correlation`, `transport`, `signatures`, `diagnostics`;
- signatures păstrează count și statusuri;
- trace-ul ordonat și metadata fiecărui exchange rămân în `Execution evidence`;
- request repetat ≠ retry, iar body diferit ≠ aceeași signature doar pentru că
  ruta este aceeași.

</details>

<details>
<summary><strong>Binding și proiecție HTML</strong></summary>

- `PracticeShopApiEvidenceBinding` decide înaintea publicării ce checkpoint-uri
  intră în journal;
- profilul `UI_API` fără selecție activează checkpoint-urile known; valoarea
  necunoscută fail-fast;
- renderer-ul nu filtrează business evidence și nu reconstituie liste locale;
- bridge-ul publică aggregate-ul într-un singur nod `TRACE` collapsed, cu
  secțiuni nested, nu în `Overview → System`;
- trace-ul trebuie să păstreze ierarhia vizibilă
  `un pas Gherkin → UI validation și API validation → detalii collapsed`;
  `UI validation` și `API validation`
  sunt copii semantici frați ai aceluiași pas, nu două teste sau două carduri
  Gherkin cu titlu repetat;
- trace-ul trebuie să reutilizeze și forma API canonicală per exchange:
  `Request details` → `Response details` → `Execution evidence`; un tabel
  comun `API 1/API 2` este doar un index insuficient pentru audit;
- fiecare `Execution evidence` trebuie să fie legat vizibil de exchange-ul său;
  `Exchange n` este identificatorul local când checkpoint-ul produce mai multe
  request-uri, nu un motiv pentru duplicarea propoziției Gherkin;
- retry-ul aceluiași request rămâne un singur exchange în raport; numărul de
  attempts și retry history apar în `Execution evidence`, nu ca pași `API n`
  suplimentari;
- `TRACE PARENT STEP` și `TRACE POSITION` sunt context al trace-ului, nu rânduri
  repetate în response details; URL-ul complet rămâne vizibil fără wrapping
  arbitrar în identificator.

</details>

Regula de citire este:

```text
SETUP/precondition → UI action → 0..n API exchanges → business assertion → read-back/cleanup
```

Body-ul sau headers-ul afișate nu sunt assertions. `PracticeShopApiAssertions`
publică separat status, JSON path, identity, quantity, payment message și trace;
un `PASS` business nu poate fi produs doar prin faptul că body-ul este vizibil.

## Consolă diagnostică

```mermaid
flowchart LR
    Flow["UI / API flow"] -->|emite| Observe["observations"]
    Observe -->|proiectează în| Projection["console + reporting"]
    Projection -->|produce| Outputs["diagnostic + HTML"]
```

<details>
<summary><strong>Legendă — două proiecții, un singur flow</strong></summary>

- `atf-console` proiectează diagnosticul în `stdout`/`stderr`;
- `atf-reporting` proiectează artifactul în Extent HTML;
- `PracticeShopE2eContext` injectează reporterul configurat la boundary-ul UI;
- publisher-ul API folosește același concept de sink pentru pași request/validation;
- flow-urile nu dețin `PrintStream` și nu folosesc `println`;
- consola nu decide verdictul și nu înlocuiește assertions sau HTML-ul;
- cross-module boundary: porturile `atf-core` și `atf-reporting` nu se unifică
  printr-un adapter duplicat local; compatibilitatea rămâne test-only în
  composition root-ul dedicat.

</details>

## Vocabular typed și citire semantică

Valorile stabile sunt owner-uite local:

```text
PracticeShopApiHttpStatus      → statusuri HTTP
PracticeShopApiJsonField       → chei JSON
PracticeShopApiQueryParameter  → chei query
PracticeShopApiAuthProfile     → profil auth
PracticeShopApiAuthType        → auth observat
PracticeShopApiOperation       → intenție + method + route template
PracticeShopPaymentMethod      → metode de plată suportate
```

Reguli:

- enum-urile păstrează vocabular stabil, nu date dinamice;
- ID-urile, credentials și payload-urile rămân în config/fixture typed;
- testele capturează response/request o singură dată în aliasuri semantice;
- se preferă `PracticeShopApiHttpStatus.OK.matches(status)` în loc de numere brute;
- JSON-ul se parsează ca tree/path, nu prin `contains`, cu excepția smoke text;
- importurile statice sunt nominale și numai când owner-ul rămâne evident;
- testele nu folosesc `try/catch/finally` pentru lifecycle; support-ul păstrează
  primary failure și închide raportul.

## Feedback și evoluție controlată

Registrul [feedback ATF](../docs/feedback/README.md) indexează gap-urile active.
[Practice Shop E2E Feedback](../docs/feedback/ATF_Practice_Shop_E2E_Feedback.md)
deține numai parity-ul încă deschis dintre configurația live și artifactul verificat.

Reguli de păstrare și redeschidere:

- contractele implementate rămân în acest README; feedback-ul păstrează numai
  gap-ul activ și criteriul lui de închidere;
- checkpoint-ul are o singură propoziție Gherkin cu validări UI/API copil; o
  duplicare în raport redeschide contractul, nu justifică logică locală de renderer;
- artifactul este probă numai pentru build-ul și configurația din care a fost
  generat; un gap de randare/integrare cere testul relevant și artifact proaspăt.

Pentru regula completă privind artifacte și conținut normativ, vezi
[bunele practici de documentație ATF](../docs/README_Atf_DocumentationBestPractices.md).

## Gates

Component/API gate:

```text
mvn -pl atf-test-practice-shop -am "-Dtest=PracticeShopConfigurationTest,PracticeShopApiRoutesTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

E2E selectat:

Rularea folosește aplicația externă și finalizează o comandă demo; nu o folosi
ca înlocuitor pentru gate-urile locale component sau de guvernanță.

```text
mvn -pl atf-test-practice-shop -am "-Dtest=PracticeShopPurchaseE2eTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Guvernanța documentației pentru țintele `@DocumentationTarget`:

```text
mvn -pl atf-tests -am "-Dtest=DocumentationTargetGovernanceTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Reguli de rulare:

- integrarea live și authenticated E2E sunt opt-in;
- output-ul se redirecționează în directoarele configurate, niciodată în surse;
- un build verde nu închide un ToDo fără evidence relevantă;
- sync-ul live promovează numai sursele/documentele aprobate;
- raportul Extent și consola sunt verificate separat: artifactul pentru randare,
  consola pentru pași observabili.

## Întreținere

- schimbă mai întâi ownerul semantic: route, locator, enum, config sau support;
- adaugă test component înainte de integrare și integrare înainte de E2E;
- păstrează composition roots E2E scurte și configurabile, dar flow-ul de
  purchase complet până la confirmarea observată;
- actualizează feedback-ul pentru deciziile dinamice, nu transforma README-ul în jurnal;
- dacă o decizie devine stabilă, mută esența în secțiunea relevantă și marchează
  ToDo-ul în feedback numai după dovadă.
