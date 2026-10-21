# Practice Shop — feedback live pentru API, UI și evidence

## Verdict curent

- `atf-test-practice-shop` este consumerul aplicației, nu owner-ul suportului
  generic de reporting, capture sau console;
- runtime-ul reutilizabil stă în `src/main/java`; testele rămân în
  `component`, `api/component`, `api/integration` și `e2e`;
- contractele stabile sunt în [`atf-test-practice-shop/README.md`](../../atf-test-practice-shop/README.md);
- feedback-ul păstrează numai limite externe, decizii cross-module și dovezi
  care trebuie reobținute;
- mock/component nu poate declara PASS pentru un business journey live.

## Boundary-ul consumerului

```mermaid
flowchart LR
    Config["YAML + account fixture"] -->|configurează| Shop["Practice Shop flow"]
    Shop -->|interacționează| Ui["atf-ui / Playwright"]
    Shop -->|diagnostichează| Console["atf-console / diagnostics"]
    Shop -->|publică evidence| Reporting["atf-reporting / evidence"]
    Ui -->|produce| Artifact["Extent / HTML artifact"]
    Console -->|scrie| Stdout["stdout / stderr"]
```

<details>
<summary><strong>Legendă — ce consumă Practice Shop și unde se oprește</strong></summary>

- YAML-ul configurează consumerul; nu mută rute sau locatori în librăria generică;
- `atf-ui` execută acțiuni și observații Playwright;
- `atf-console` este canalul diagnostic pentru pașii UI;
- `atf-reporting` publică artifactul semantic, fără să cunoască Practice Shop;
- testele verifică suportul generic în `atf-reporting-tests`, nu în acest modul;
- `src/main` deține suportul reutilizabil al aplicației, iar `src/test` deține
  orchestration-ul și transporturile de test.

</details>

## Scor pe axe

Scorurile au fost recalibrate pe implementarea și testele live curente; ele nu
înlocuiesc verdictul gate-urilor și nu transformă un test `SKIP` într-o dovadă
de business.

| Axă | Notă | Verdict actual |
|---|---:|---|
| API screen contracts | 98 | login, catalog, product, cart și checkout sunt typed, cu variații component și flow live verificabile |
| UI page validation | 97 | contractele declarative și validatorul semantic acoperă ecranele traversate fără E2E multiplicat |
| API evidence în UI | 98 | binding-ul per checkpoint, exchange-ul typed și atașarea single-node sunt confirmate în artifactul UI regenerat: un singur card Gherkin deține `UI validation` și `API validation`, iar request/response/execution rămân nested fără digesturi brute |
| E2E client–server trace | 98 | ordinea cauzală este confirmată vizual: `un singur Gherkin → UI validation + API validation → Exchange → Request/Response/Execution`; POST/GET rămân exchange-uri distincte, iar retry-ul nu creează pași API suplimentari |
| Overview configuration observability | 96 | snapshot-ul typed publică doar configurația relevantă, bounded și redacted, cu etichete operaționale și destinații UI/API separate |
| console wiring | 97 | Practice Shop consumă `atf-console` prin composition boundary; nu există mapping sau scriere directă în consumer |
| E2E discipline | 97 | guest și authenticated sunt journey-uri distincte; variațiile rămân component/API integration, iar contextul este per test |
| Test assertion trust | 96 | verdicturile provin din DOM, config și network observate; guard-ul respinge literal-`true`, iar contractele HTML folosesc helper semantic |
| E2E composition quality | 98 | `E2eHarness` centralizează configurația, contextul și locatorii; `checkpoint(...)` compune UI și API în același nod semantic, iar `apiTrace(...)` rămâne doar calea explicită standalone |
| Test readability/OOP | 97 | flow-urile sunt business-oriented, lifecycle-ul este explicit și support-ul are boundary-uri clare; evidence multi-exchange păstrează aceeași formă canonicală ca API direct |
| Plug-and-play test selection | 96 | adnotările și condițiile typed selectează fixture-ul authenticated/live fără metode `isConfigured()` duplicate |
| live business proof | 82 | guest product → cart → payment este verificat prin `LIVE_HTTP`; login/catalog/cart/payment authenticated rămâne blocat de fixture extern |
| **scor consumer** | **95** | contractul guest, binding-ul typed, proiecția per exchange și topologia single-node sunt confirmate pe codul live, suitele verzi și artifactul regenerat; limita principală rămasă este fixture-ul authenticated extern care rămâne explicit skip |

## Închideri promovate în README-ul ownerului

| Grup | Stare | Dovadă stabilă |
|---|---|---|
| `SHOP-API-SCREEN-01..10` | ✅ | screen flows, transport mock, selecție, jurnal, assertions și evidence mapper |
| `SHOP-UI-VALIDATION-01..04` | ✅ | `@PracticeShopPageContract`, `PracticeShopPageValidator`, component evidence |
| `SHOP-MIX-01..03` | ✅ | checkpoint-ul UI și exchange-urile API sunt în ordinea cauzală corectă, fără JSON duplicat; `UI validation` și `API validation` sunt copii ai aceluiași card Gherkin |
| `SHOP-AUTH-01..07` | ✅ pe implementare; live proof pending | registration, fixture loading, login setup, redaction și purchase authenticated sunt implementate; authenticated E2E/API rămân skip până la fixture extern acceptat |
| `SHOP-JSON-03..04` | ✅ | JSON tree/path și vocabular typed; substring-ul nu este contract |
| `SHOP-CONFIG-02` și `SHOP-ROUTE-04..05` | ✅ | owner unic pentru host, rute relative, segmente dinamice validate și output separat `artifacts/reports/ui` / `artifacts/reports/api` |
| `SHOP-ARCH-11..13` | ✅ | runtime în `src/main`, test-only support în `src/test`, capture/select separate |
| `SHOP-REPORT-FAILURE-01` | ✅ | cauza failure-ului este proiectată în `Fails`, nu ascunsă de primul assertion verde |
| `SHOP-API-REAL-02` | ✅ guest live | `PracticeShopPurchaseE2eTest` verifică în aceeași sesiune LIVE_HTTP `product_id`, `name`, `quantity`, `cart_items`, `payment_method` și `Payment was successful`; authenticated API rămâne separat blocat de fixture |
| `SHOP-NET-02` | ✅ guest live | boundary-ul warning-ului guest este corect: target `GET /users/me → 401`, assertions observabile și evidence type `runtime-diagnostic`, fără copilul generic `UI validation`; deduplicarea aggregate este închisă |
| `SHOP-DIAGNOSTIC-03` | ✅ guest live | `Scenario.warningDiagnostics` și `PracticeShopRuntimeDiagnosticsTest` proiectează o singură assertion per semnătură semantică, cu count/sources observabile; response și console nu mai produc assertions duplicate |
| `SHOP-API-TRACE-01` | ✅ guest live | checkpoint-ul compus păstrează request/response/execution canonical și inspectorul final compact sub `API validation`; nu mai materializează al doilea card Gherkin |
| `SHOP-API-TRACE-02` | ✅ guest live — parity lower-level | disclosure-ul UI reutilizează labels, redaction policy, filtrarea browser noise și replay-ul API direct; `API validation` păstrează request/response/execution per exchange și este atașat cardului UI |
| `SHOP-DIAGNOSTIC-SEMANTICS-01` | ✅ | `UiEvidencePublisher.publishDiagnostic(...)`, test component și guest E2E produc `Runtime diagnostic` + status/target/assertions observabile |
| `SHOP-TRACE-INTERLEAVE-01` | ✅ | `UiPhaseLifecycle.apiTrace(...)` și `PracticeShopE2eContext.Scenario.checkpoint(...)` verifică ordinea UI → API → UI, un singur nod Gherkin și o singură proiecție JSON per exchange |
| `SHOP-REPORT-BODY-01` | ✅ pe owner generic | `ReportingSettings.bodyMaxDisplayCharacters=100` este încărcat din `atf.yaml`, aplicat după redaction/pretty-print și păstrat în disclosure; `0` dezactivează limita, iar System îl publică bounded, fără copiere în sumar |
| `SHOP-CONSOLE-01` | ✅ pe consumer | sink-ul configurat este injectat la composition root; flow-urile nu folosesc `println` |
| `SHOP-CONSOLE-02` | ✅ pe composition boundary | adapterul este test-only în `atf-reporting-tests`; modulele production rămân independente, iar Practice Shop nu face mapping |
| `SHOP-REPORT-SYSTEM-01`, `SHOP-REPORT-NET-01`, `SHOP-REPORT-AUTH-01` | ✅ pe consumer/generic owner | System/Environment și primitivele de network/auth sunt typed, bounded, redacted și verificate component/integration; fresh MCP/Chromium confirmă configurația operațională fără `Suite`/`Test package`/`Flow`/`Fixture`/`Checkout`, durate umane (`20s`, `45s`), disclosure-urile API canonical și separarea `API precondition` / `UI journey precondition`; aggregate-ul journey este acum typed și namespaced în aceeași cerere collapsed |
| `SHOP-PLAYWRIGHT-RETRY-01` | ✅ | `PracticeShopUiRetryConfiguration` încarcă `maxAttempts`, `backoff`, operațiile read-only și motivul fără boolean redundant; YAML-ul live este fail-closed (`maxAttempts=1`, `eligibleOperations=[]`), iar `UiActionRecorder` publică attempt-ul eșuat ca `Info` înaintea rândului de retry; `UiRetryPolicyTest`, `PracticeShopUiRetryConfigurationTest` și `PlaywrightActionSequenceTest` verifică policy-ul și istoricul ordonat. Dacă un consumer schimbă ulterior `eligibleOperations`, acesta redeschide explicit dovada E2E vizuală. |
| `SHOP-REPORT-SYSTEM-02` | ✅ | `PracticeShopReportSystemMetadata` publică numai configurație operațională allowlist-uită: runtime, ținte, browser, timp, network, retry, evidence și reporting; component test-ul respinge durate ISO și orice identitate de test în `System` |
| `SHOP-E2E-ROOT-01` | ✅ | `PracticeShopTestSupport.openE2e()` și `E2eHarness` sunt composition root-ul unic pentru configurație, context și locatori; testele păstrează numai orchestration-ul business |
| `SHOP-E2E-ISOLATION-01` | ✅ | toate cele trei E2E folosesc `BeforeEach/AfterEach`, fără `PER_CLASS`/`BeforeAll`; `PracticeShopArchitectureTest` guvernează explicit context nou per test |
| `SHOP-HTML-CONTRACT-01` | ✅ | `PracticeShopReportHtmlTestSupport` separă text, celulă exactă, ordine și absență; component/integration checks nu mai depind de un singur șir HTML mare pentru contractul principal |
| `SHOP-TEST-LIFECYCLE-01` | ✅ | `PracticeShopApiClient` este `AutoCloseable`, curăță bearer state și refuză utilizarea după close; integration tests folosesc try-with-resources |
| `SHOP-TEST-DSL-01` | ✅ | `PracticeShopTestSupport` oferă condiții compuse typed pentru authenticated fixture și live API; gating-ul nu mai este duplicat prin `isConfigured()` în fiecare clasă |
| `SHOP-TEST-TRUST-01` | ✅ pe toate journey-urile și suportul reutilizabil | setup-ul compară expected/actual, checkout-ul observă markerul DOM, controlul authenticated și payment option-ul selectat; `PracticeShopApiAssertionsTest` dovedește și că login-ul fără `current-user` nu trece cu transport JSON invalid; `PracticeShopArchitectureTest` guvernează E2E + flow/evidence support; orice verdict literal sau self-comparison reaprinde item-ul |
| `REP-TRACE-03` | ✅ owner generic | publisher-ul generic și rendererele Extent/fallback publică trace-ul per exchange sub `API validation`; fresh consumer proof-ul arată un singur card Gherkin cu copii `UI validation` și `API validation` |
| `REP-EVID-08` | ✅ owner generic | URL value cells păstrează textul complet și wrapping controlat, verificat prin component/integration/MCP |
| `REP-TRACE-04` | ✅ owner generic | Practice Shop folosește signature-ul și fingerprint-ul numai intern pentru corelare/deduplicare; publisher-ul proiectează body-state marker și linkuri locale `screenshot-n`, fără digest brut în HTML |

## Audit live — calitatea testelor

```mermaid
flowchart LR
    Metadata["layer + journey metadata"] -->|selectează| Fixture["typed config + fixture"]
    Fixture -->|alimentează| Root["composition root scurt"]
    Root -->|apelează| Flow["semantic flow"]
    Flow -->|produce| Observed["observed state"]
    Observed -->|compară| Verdict["assertions reale + evidence"]
```

<details>
<summary><strong>Legendă — constatări prioritare și boundary-ul testelor</strong></summary>

- `PracticeShopAccountAndCatalogE2eTest` folosește acum
  `PracticeShopPageValidator` pentru identity și redaction, iar ruta este
  verificată prin `URI` + contractul de path;
- assertions-urile hardcodate din account/catalog E2E, purchase flow și setup au
  fost eliminate; component/report contracts rămân lower-level, iar
  `PracticeShopReportHtmlTestSupport` le separă de dovada business live;
- `PracticeShopE2eContext` publică acum setup assertions cu expected/actual
  observabil: modul guest/authenticated, fixture availability și profilul de
  evidence; `PracticeShopReportSystemMetadata` explică separat precondiția API
  configurată și precondiția efectivă a journey-ului UI;
- payment selection folosește textul opțiunii selectate din DOM, nu repetă
  valoarea YAML în coloana `ACTUAL`; authenticated branch verifică markerul
  `already logged in` și controlul de continuare observat;
- `PracticeShopApiAssertionsTest.loginWithoutCurrentUserDoesNotPassWhenTheObservedJsonTransportIsInvalid`
  fixează negativ ramura în care `current-user` nu este emis: verdictul trebuie
  să derive din status, content-type și transportul observat, nu din absența
  exchange-ului și nu dintr-un `true` implicit;
- guard-ul component extins scanează `src/main/.../flow` și `support`, nu doar
  clasele E2E, astfel încât o aserțiune sintetică să nu poată fi ascunsă în
  suportul reutilizabil;
- `PracticeShopPurchaseE2eTest`, `PracticeShopAuthenticatedPurchaseE2eTest` și
  `PracticeShopAccountAndCatalogE2eTest` folosesc acum același
  `PracticeShopTestSupport.openE2e()` și același `E2eHarness`; repetarea rămasă
  este doar adaptarea JUnit a câmpului per-test și nu mai construiește manual
  configurația, contextul sau locatorii;
- `Scenario.warning(...)` folosește acum seam-ul generic `diagnostic(...)`;
  raportul publică `Runtime diagnostic` și `runtime-diagnostic`, păstrând
  warning status, target-ul și assertions fără să-l clasifice ca `UI validation`.
- `Scenario.checkpoint(...)` publică un singur checkpoint Gherkin, cu
  `UI validation` și `API validation` ca copii frați; request/response/execution
  sunt proiectate o singură dată per exchange. Ordinea și forma single-node sunt
  verificate prin component/integration contract și fresh MCP/Chromium proof.
- contractele HTML principale folosesc acum `PracticeShopReportHtmlTestSupport`
  pentru text semantic, celule exacte, ordine și absență; `contains` rămâne doar
  pentru smoke text sau tokeni explicit interziși, nu pentru structura raportului;
- `PracticeShopApiScreenFlowIntegrationTest` și `PracticeShopStandaloneApiReportTest`
  închid acum clientul prin try-with-resources; orice revenire la `client = null`
  este regresie de lifecycle, nu stil acceptat;
- `PracticeShopE2eContext` rămâne boundary-ul runtime, iar construction root-ul
  de test este separat în `PracticeShopTestSupport`; următorul refactor opțional
  este o extensie JUnit dacă mai apare un al treilea consumer E2E.

</details>

## Ținta de refactorizare a testelor

```text
@AtfE2eTest + metadata typed
  → context/extension configurat o singură dată
  → fixture semantic
  → flow semantic
  → assertions observate
  → evidence și teardown în support
```

Reguli de calitate:

- testul descrie business intent; nu construiește manual infrastructura de
  raportare, browser sau transport;
- un test E2E are puține linii și un singur rezultat de business verificat;
- `expected` vine din config/fixture, `actual` vine din page/API/DOM observat;
- orice assertion care se termină cu `true` hardcodat este respinsă;
- suportul care publică assertions este guvernat identic cu testul: niciun flow,
  page helper sau evidence publisher nu poate transforma o presupunere într-un
  verdict verde;
- ruta se validează prin parser/path contract, nu prin `contains`;
- fiecare composition root poate fi rulat singur și nu depinde de ordinea altui
  test;
- variations matrix rămâne în component tests; integration dovedește boundary-ul;
- adnotarea exprimă layer, journey, transport și precondiție; nu se repetă
  `isConfigured()` în fiecare clasă;
- static imports sunt permise pentru assertions și constante cu owner evident;
- lifecycle, retry, screenshot și report close aparțin extension/harness-ului,
  nu corpului testului.

## Criterii pentru închiderea limitelor active

- code owner-ul și source-set-ul afectat sunt identificați explicit;
- fixul păstrează separarea component → integration → E2E și nu adaugă browser
  pentru o variație care poate fi verificată lower-level;
- testul focalizat dovedește comportamentul la nivelul potrivit;
- o excepție este documentată cu owner, motiv și boundary verificabil, nu doar
  adăugată într-o allow-list;
- regula stabilă rezultată este promovată în README-ul ownerului;
- feedback-ul rămâne cu gap-ul activ și criteriul de redeschidere.

## Închideri promovate în owner docs

| ID | Dovadă | Verdict |
|---|---|---|
| `SHOP-REPORT-NET-02` | `ReportNetworkInspector.JourneyAggregate`, `ReportNetworkInspectorTest` și consumer E2E | aggregate-ul journey calculează authentication, correlation, transport, signatures și diagnostics fără JSON duplicat; collision handling-ul necorelat este închis generic prin `REP-DIAGNOSTIC-04` |
| `SHOP-REPORT-NET-03` | `ReportNetworkAggregateMarkup`, `ReportNetworkInspectorTest` și fresh MCP/Chromium | Extent și fallback publică inspectorul nested, cu cele cinci secțiuni typed și fără fields flat `network.aggregate.*`; `Response details` are numai indexul bounded, `Scenario correlation` este separat de request-uri, iar `Unique request signatures` afișează fiecare semnătură o singură dată ca un card compact; request/response/execution per exchange rămân în checkpoint-ul canonical prin `REP-TRACE-03` |
| `SHOP-REPORT-AUTH-02` | `AuthenticationObservation` + component/HTML contract | method/source/status/count sunt grupate determinist și rămân credential-free; semantică „observed” |
| `SHOP-DIAGNOSTIC-02` | `DiagnosticObservation` + component/HTML contract | response și console pentru aceeași semnătură sunt un singur observation cu count/sources; alte rute/statusuri rămân distincte |
| `SHOP-DIAGNOSTIC-03` | `Scenario.warningDiagnostics`, `PracticeShopRuntimeDiagnosticsTest` și fresh guest E2E/MCP | warning-ul consumerului grupează diagnosticele după semnătură semantică și publică count/sources într-o singură assertion; erorile neașteptate rămân failure |
| `SHOP-API-BIND-01` | `PracticeShopApiCheckpoint`, `PracticeShopApiEvidenceBinding`, `PracticeShopApiEvidenceBindingTest` și fresh E2E/MCP | binding-ul per journey/checkpoint este citit din YAML/override, journal-ul publică `evidence(checkpoint)`, iar System confirmă checkpoint-urile efective |

## Limite active și criterii de redeschidere

| ID | Owner | Limită actuală | Criteriu de închidere |
|---|---|---|---|
| `SHOP-API-REAL-01` | Practice Shop + mediu extern | fixture-ul local este placeholder, `authenticatedE2eEnabled=false`, iar login-ul live nu este dovadă de sesiune validă | fixture extern non-placeholder, `authenticatedE2eEnabled=true`, testele authenticated și standalone API nu mai sunt skip, login/current-user `200`, apoi rerun `LIVE_HTTP` cu login → catalog → cart → payment |

<details>
<summary><strong>Interpretare și limite de redeschidere</strong></summary>

- collision handling pentru diagnosticele necorelate este închis generic prin
  `REP-DIAGNOSTIC-04`; Practice Shop consumă contractul, fără ownership pentru
  algoritmul generic;
- `REP-TRACE-03` este închis: fresh UI proof-ul arată un singur card cu aceeași
  propoziție Gherkin și `API validation` ca frate al `UI validation`; se redeschide
  numai la regresie vizibilă;
- `REP-EVID-08` rămâne închis pentru wrapping și se redeschide numai la regresie
  verificată;
- nu există ToDo local separat pentru `SHOP-API-SCREEN-01..10`, UI validation,
  evidence policy, JSON semantic sau split-ul capture/select; orice extensie nouă
  începe cu contract component, nu cu E2E pentru o variație de endpoint sau selector.

</details>

## Dovadă necesară pentru închiderea unui ToDo

- component: contract typed, variație pozitivă și negativă, fără browser real;
- integration: boundary-ul real și lifecycle-ul resursei sunt verificabile;
- E2E: journey-ul selectat produce assertions observate, evidence și verdict
  semantic, fără a transforma raportul în sursă de contract;
- MCP/Chromium: se folosește numai când criteriul este vizual — randare,
  disclosure, ordine sau System/Environment;
- feedback-ul păstrează criteriul și dovada lipsă, nu nume de artifact,
  timestamp, număr istoric de teste sau transcript de rulare.

## Piramida de testare intenționată

```text
component
  route/config/page contract → input invalid → JSON/status/header matrix

integration (opt-in)
  account fixture → LIVE_HTTP session → catalog → cart → checkout

E2E
  guest purchase       → UI + API evidence pe checkpoint-uri
  authenticated purchase → login setup + UI + API evidence
```

<details>
<summary><strong>Reguli de interpretare</strong></summary>

- component test dovedește contractul și este rapid, determinist și browser-free;
- integration test dovedește boundary-ul de transport și este opt-in;
- E2E dovedește orchestration-ul, nu fiecare endpoint în parte;
- screenshot policy și flow mode sunt configurate typed, nu prin flag-uri ad-hoc;
- network inspectorul este selectat prin `validation.profile` typed, fără boolean
  separat; retry policy-ul Playwright rămâne de configurat explicit la ownerul UI;
- screenshot-urile pot fi per checkpoint, final-only sau none, dar cardinalitatea
  trebuie verificată fără browser înainte de journey;
- `atf-reporting-tests` este ownerul dovezii generice; Practice Shop este doar
  consumerul configurat.
- guest evidence-ul curent este suficient pentru auditul typed al întregului
  journey: `UI step → auth → attempt → exchange → retry → assertion` este
  păstrat în ledger și în disclosure-urile locale; forma vizuală conformă este
  `un Gherkin checkpoint → UI validation + API validation → UI continuation`.
  Raportul fresh confirmă această topologie, iar diagnosticul guest rămâne
  `Runtime diagnostic`, nu `UI validation`.
- inspectorul final este un index compact al journey-ului, nu o a doua copie a
  cardurilor API și nici o zonă pentru System; aggregate-ul typed de auth,
  diagnostics, correlation, counters și signatures este publicat în aceeași
  cerere collapsed și rămâne ușor de scanat.

</details>

## Reguli promovate în README-ul ownerului

Regulile stabile pentru flow-uri, assertions, evidence, lifecycle și selecția
testelor sunt în [`atf-test-practice-shop/README.md`](../../atf-test-practice-shop/README.md).
Acest feedback păstrează numai limitele care încă pot schimba codul sau
contractul și criteriile lor de redeschidere.

## Protocol de revalidare

<details>
<summary><strong>Când se schimbă flow-ul sau contractul consumerului</strong></summary>

1. modifică ownerul semantic: config, route, locator, page, flow sau evidence mapper;
2. rulează component gate-ul focalizat;
3. rulează integration numai cu fixture-ul explicit necesar;
4. rulează unul dintre cele două journey-uri E2E numai când schimbarea atinge
   orchestration-ul browser;
5. verifică în MCP/Chromium artifactul fresh dacă afirmația este vizuală;
6. actualizează acest registru doar dacă statusul sau criteriul de închidere s-a
   schimbat;
7. mută regula stabilă în README, nu o copia în feedback.

</details>

## Referințe normative

- [`atf-test-practice-shop/README.md`](../../atf-test-practice-shop/README.md) —
  config, routes, pages, flows, evidence și gates;
- [`atf-reporting/README.md`](../../atf-reporting/README.md) — suportul generic
  de reporting și publication;
- [`atf-reporting-tests/README.md`](../../atf-reporting-tests/README.md) —
  component/integration/browser proof pentru librărie;
- [`docs/ATF_Evidence_Capture_Contract.md`](../ATF_Evidence_Capture_Contract.md)
  — contract transversal pentru capture policy;
- [`docs/feedback/README.md`](README.md) — registrul central.
