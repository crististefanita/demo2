# ATF Java — registrul curent de feedback

## Overall

- feedback-ul păstrează numai riscuri, limite sau decizii care se pot schimba;
- fiecare ToDo are owner, dovadă lipsă și criteriu de închidere;
- regula stabilă se mută în README-ul ownerului;
- analiza datată fără acțiune și artifactele generate nu sunt documentație
  permanentă;
- un run verde nu închide un ToDo fără dovadă relevantă.

## Regula de reconciliere cu codul live

```mermaid
flowchart LR
    Code["cod + POM"] -->|definește| Tests["teste + adnotări"]
    Tests -->|verifică| Readme["README owner"]
    Readme -->|cristalizează| Registry["feedback curent"]
    Registry -->|redeschide doar la regresie| Code
```

<details>
<summary><strong>Legendă — ordinea dovezii</strong></summary>

- codul și POM-ul stabilesc modulele, dependențele și boundary-ul real;
- testele și adnotările stabilesc comportamentul verificat și nivelul lui;
- configurația stabilește inputul plug-and-play, nu capability-uri inexistente;
- README-ul explică regula stabilă și ownerul;
- feedback-ul păstrează limita, criteriul și dovada lipsă;
- artifactul este evidence de run, nu sursă normativă.

</details>

## Verdict whole-ATF

| Zonă | Verdict | Owner / dovadă |
|---|---|---|
| `atf-reporting` production | ✅ standalone | POM parentless, dependency guard |
| reporting tests | ✅ consumer separat | `atf-reporting-tests` și matrix lower-level |
| Practice Shop | ✅ consumer separat | `atf-test-practice-shop` |
| whole-reactor governance | ✅ ownership separat, gate verde | `atf-tests`; boundary-urile production/test și lizibilitatea sunt verificate fără prag relaxat |
| documentație | ✅ guvernată | diagram, prose, structure, style și target guards |
| `atf-execution` | ✅ eliminat | mecanica generică este în `atf-core` |

## Gate-uri curente

| Gate | Rezultat | Interpretare |
|---|---|---|
| documentation governance | ✅ PASS | structura, prose, stil, target și diagrame sunt conforme |
| `atf-console` focused gate | ✅ PASS | bridge-ul are test component și round-trip-ul vocabularului este verificat |
| whole-ATF governance | ✅ PASS | `AtfPackageCohesionGovernanceTest` confirmă maximum 8 clase pe pachet după split-urile `ui.network`, `support.api` și `support.config.environment`; `AtfSourceReadabilityGovernanceTest` confirmă importuri normale |
| reporting consumer gate | ✅ PASS | publisher-ul, renderer-ele Extent/fallback și retry-ul per exchange trec; fresh MCP/Chromium confirmă un singur checkpoint Gherkin cu `UI validation` și `API validation` ca copii frați, iar exchange-urile rămân nested și unice |
| latest reporting rerun | ✅ PASS | 519 teste reporting, 0 failure/error, 3 skip-uri explicite; 13/13 contracte runtime focalizate și 68/68 governance whole-ATF |

## ToDo active

| Prioritate | ID | Owner | Limită | Criteriu de închidere |
|---|---|---|---|---|
| P0 | `SHOP-API-REAL-01` | Practice Shop + mediu extern | fixture placeholder și `authenticatedE2eEnabled=false`; authenticated tests sunt skip | fixture non-placeholder, flag activ, login/current-user `200`, apoi journey live complet |
| P2 | `REP-UX-PERF-01` | `atf-reporting` + `atf-reporting-tests` | scheduler-ul și observerii sunt idempotent/scoped, iar `extent-navigation-performance.js` publică readiness, counters și long-task-uri suportate; pragurile reproductibile pe raport mare nu sunt încă impuse | observer scoped, readiness barrier unic, normalizare incrementală și audit cu buget la load/expand; zero loop de mutații |
| P2 | `REP-UX-PERF-02` | `atf-reporting-tests` | secvența `load → phase → request/validation → details → collapse` este instrumentată, dar nu are încă fixture DOM/component configurabil și buget aprobat | fixture component cu praguri configurabile pentru load, primul expand și open/close repetat; număr de normalizări și long-task-uri observabile; fără E2E business nou |
| P2 | `REP-EVID-08-STRESS` | `atf-reporting-tests` | lipsește un fixture browser dedicat cu minimum 6 semnături pentru proof-ul vizual îngust | artifact fresh, expandare manuală MCP, JSON request/response mare, fără overflow, `<hr>` sau console/page errors |
| P3 | `TEST-ARCH-07` | `atf-tests` | suportul poate avea subzone greu de navigat | mutare numai cu owner, motiv și boundary test |

`REP-UX-TABLE-01` este închis și rămâne în istoricul de închideri din
`ATF_Reporting_Feedback.md`: component test-ul verifică fingerprint/cache,
dirty tables, resize și open/close, iar poarta completă confirmă integrarea în
raportul UI. Nu este confundat cu `REP-UX-PERF-01/02`, care cer încă măsurători
de cost runtime.

### Dovadă exploratorie recentă — trace composition și toggle stability

Pentru artifactul Practice Shop proaspăt au fost parcurse manual în MCP/Chromium
testul selectat, scenario-ul, `TEST BODY`, checkpoint-ul de produs,
`API validation`, `Exchange 1`, apoi `Request details`, `Response details` și
`Execution evidence`. Se văd un singur card Gherkin, copiii `UI validation` și
`API validation`, două exchange-uri distincte, JSON request/response, statusul
`200`, transportul `LIVE / HTTP` și trace-ul tehnic. Auditul DOM a confirmat
`scrollWidth = clientWidth`, zero `<hr>` și zero erori de consolă în tab-ul MCP.

Acest proof închide `REP-TRACE-03` și `REP-UX-TOGGLE-01`. Contractele
component/interaction verifică helper-ul comun, iar MCP confirmă manual pe
artifactul fresh secvența `open → delayed normalize → close → reopen`: scenariul,
phase/request/validation, `aria-expanded`, glyph-ul și panourile rămân coerente;
copiii sunt închiși până la un click explicit.

### Reaudit live — delimitarea evidence-ului UI/API

Reauditul manual al run-ului curent păstrează două dovezi distincte:

| Suprafață | Ce s-a verificat | Interpretare |
|---|---|---|
| UI guest E2E | `TEST BODY` → `GIVEN product page` → `API validation` → `GET /products/search — Exchange 1` → `Request details` / `Response details` / `Execution evidence`; request JSON pretty, response JSON pretty, `HTTP 200`, `LIVE / HTTP` | dovadă reală client–server, legată de checkpoint-ul UI care a cauzat schimbul |
| API component | `POST /carts — create cart`, request/response JSON și technical trace, cu `ACCEPTANCE / MOCK — network was not executed` | contract component determinist; nu este dovadă de business journey live și nu poate înlocui `LIVE_HTTP` |
| Overview / Categories / Fails | System/Environment și Tags sunt populate; categoria raportează warning-ul guest; `Fails` spune explicit `No failed tests` | warning-ul diagnostic nu este reinterpretat ca failure și rămâne vizibil în raport |

Observația semantică găsită în acest audit a fost reparată: `Precondition`
rămâne precondiția API configurată, iar `UI journey precondition` descrie
separat journey-ul efectiv (`guest checkout configuration ready` sau fixture-ul
authenticated). Astfel un run guest nu mai pare authenticated doar pentru că
configurația API are precondiția `authenticated`.

La lățimea MCP implicită verificarea structurală a confirmat `scrollWidth <=
clientWidth`, zero `<hr>` și zero console logs. Acea suprafață îngustă este doar
un smoke diagnostic; nu închide `REP-EVID-08-STRESS`, deoarece viewport-ul
acceptat al consumerului este cel configurat în System/Environment.

## Închideri care nu mai sunt active

| Grup | Dovadă stabilă |
|---|---|
| `TRACE-REQ-01..10` | `ReportTraceMetadata`, `ReportRequestSignature` și testele de trace |
| `REP-EVID-01..07` | policy/profile/session, mock evidence și normalizare idempotentă |
| `REP-AUDIT-08` | snapshot renderer-neutral și validatorii puri |
| `REP-COVERAGE-08` | `ReportingCoverageMatrixTest` |
| `REP-E2E-08` | `E2eSuiteBudgetArchitectureTest`, plafon 8 |
| `REP-FORMAT-01` | source multiline versus publication compactată |
| `REP-SIZE-01` | `ReportAssetSizeBudget` și `ReportAssetSizeInventoryTest`; baseline explicit pentru asset bundle-ul CSS/JavaScript |
| `REP-VOCAB-01` | `AtfReportHtmlVocabulary` și contractele de markup; tokenii production sunt typed, iar `toggleView` este seam vendor explicit |
| `REP-BODY-01` | `ReportingSettings`, `ExtentReportNodeSupport` și testele formatter/config/Extent/HTML; default `100`, `0` dezactivează limita, marker `first N of TOTAL` |
| `REP-LOCATOR-01` | `ReportUiLocatorsTest` și ports din `support/locators`; fațada este compatibilitate publică, iar ownership-ul selectorilor este vertical/coeziv |
| `REP-ASSET-08` | manifest, owner, format, ordine și namespace |
| `REP-BUILD-01` | standalone discovery allowlist-uit |
| `REP-BOUNDARY-01` | architecture ownership și POM guards |
| `CORE-FILES-01` | `atf-core.files.AtfPathLocator.locateModuleRoot`, `AtfFileTree.regularFiles`, `AtfPathLocatorTest`, `AtfFileTreeTest` și `PracticeShopArchitectureTest`; consumerul nu mai reimplementează root discovery sau file traversal |
| `SHOP-API-SCREEN-01..10` | screen flows, journal, assertions și evidence mapper |
| `SHOP-CONFIG-02`, `SHOP-ROUTE-04..05` | host și rute relative; output UI/API separat în `artifacts/reports/ui` și `artifacts/reports/api` |
| `SHOP-UI-VALIDATION-01..04` | page contracts și validator component |
| `SHOP-MIX-01..03` | `UiPhaseLifecycle` + guest E2E proof: selecția, ordinea cauzală și forma single-node sunt verificate; checkpoint-ul are `UI validation` + `API validation` în același nod |
| `SHOP-AUTH-01..07` | registration, fixture loading, login și authenticated purchase implementate; live proof pending până la fixture non-placeholder |
| `SHOP-ARCH-11..13` | runtime/main versus test-only support |
| `SHOP-REPORT-FAILURE-01` | failure projection în `Fails` |
| `SHOP-CONSOLE-01` | sink configurat injectat la composition root |
| `SHOP-CONSOLE-02` | `atf-reporting-tests/.../AtfConsoleBridgeTest`; adapter test-only, fără mapping în Practice Shop sau dependency production inversă |
| `SHOP-API-REAL-02` | guest live product/cart/payment assertions și Payment success verificate prin `LIVE_HTTP`; authenticated standalone rămâne separat în `SHOP-API-REAL-01` |
| `SHOP-API-TRACE-01` | screen flow + network journal: request/response, status, transport, retry, trace și signature rămân legate de checkpoint-ul care le-a cauzat; ordinea de prezentare este verificată prin `UI validation → API validation → Exchange` |
| `REP-RENDER-09`, `REP-MODEL-01`, `REP-TEST-08`, `REP-OVERVIEW-02` | `ReportRendererParityTest`, `AtfReportSemanticSnapshotTest`, `ReportingCoverageMatrixTest`, `ReportEnvironmentSnapshotTest` și guard-urile existente; parity, lifecycle guards, matrix proof și System allowlist sunt verificate |
| `REP-NET-03`, `REP-AUTH-02`, `REP-DIAGNOSTIC-02`, `REP-DIAGNOSTIC-04` | `JourneyAggregate` și `ReportNetworkInspectorTest`; modelul semantic are authentication/correlation/transport/signatures/diagnostics, auth este method/source/status/count, response+console corelate se deduplicatează cu count/sources, iar semnalele necorelate distincte folosesc identity internă exactă și token-uri bounded `UNBOUND-n` |
| `REP-NET-04` | `ReportNetworkAggregateMarkup`, `ReportNetworkInspectorTest`, `HtmlReportViewsTest` și fresh guest E2E/MCP | Extent și fallback publică un singur inspector collapsed cu cinci disclosure-uri nested; `network.aggregate.*` nu mai este expus ca tabel flat în trace. Indexul journey-ului și forma canonicală per exchange sunt acum acoperite împreună cu `REP-TRACE-03` |
| `REP-TRACE-03` | `ExtentReportUiEvidencePublishingTest` (10 teste, inclusiv retry), `HtmlReportViewsTest`, `ExtentReportInteractionContractTest`, Practice Shop guest E2E și fresh MCP | ✅ închis: publisher-ul compus atașează `API validation` la checkpoint-ul Gherkin existent; fresh UI proof confirmă `UI validation` + `API validation` ca copii frați, fără al doilea titlu Gherkin; fiecare exchange este unic, iar retry-ul rămâne în `Execution evidence` |
| `REP-UX-TOGGLE-01` | `setDisclosureState`, `ExtentReportRuntimeNavigationComponentTest`, `ExtentReportInteractionContractTest` și fresh MCP UI artifact | ✅ închis: un singur helper sincronizează panel, flag, `aria-expanded` și glyph; închiderea scenariului resetează descendenții, iar redeschiderea nu reface implicit copii închiși |
| `REP-EVID-08` | `AtfReportMarkup`, `evidence-table.css`, `HtmlReportViewsTest`, `ExtentReportInteractionContractTest` și fresh UI E2E/MCP | ✅ închis: celulele URL primesc clasa semantică de wrapping controlat; valoarea completă rămâne în Extent și fallback, iar browser proof-ul confirmă lipsa overflow-ului la lățimea suportată |
| `REP-TRACE-04` | `ReportRequestSignature`, `ReportTraceMetadataRenderingTest`, `ReportNetworkInspectorTest`, `ExtentReportAssetsTest` și fresh API/UI MCP HTML | ✅ închis: digestul body-ului și fingerprint-ul screenshot-ului rămân interne; HTML-ul public păstrează doar starea bounded a body-ului și referințe locale `screenshot-n`, fără hash brut în trace, assertions sau linkuri |
| `SHOP-REPORT-NET-02`, `SHOP-REPORT-AUTH-02`, `SHOP-DIAGNOSTIC-02` | Practice Shop consumă aggregate-ul generic; binding-ul și dovada de consumer sunt separate de renderer, fără JSON duplicat și fără credentiale în sumar |
| `REP-API-BIND-01`, `SHOP-API-BIND-01` | `PracticeShopApiCheckpoint`, `PracticeShopApiEvidenceBindingTest`, YAML și override system property | selecția per journey/checkpoint este typed și fail-closed; journal-ul publică numai evidence-ul selectat |
| `REP-TRACE-02`, `SHOP-API-TRACE-02` | UI API trace-ul reutilizează vocabularul, redaction policy, filtrarea browser noise, replay-ul redacted și execution evidence ale raportului API; titlul este pasul Gherkin exact, fără prefix URL/`API trace ·`; parity-ul per exchange și atașarea single-node sunt închise |
| `SHOP-REPORT-SYSTEM-01` | `PracticeShopReportSystemMetadataTest` + MCP/Chromium proof: Overview/System păstrează configurația bounded/redacted și separă explicit precondiția API configurată de precondiția journey-ului UI |
| `SHOP-REPORT-SYSTEM-02` | `PracticeShopReportSystemMetadata` + component test: `Overview/System` publică numai configurație operațională allowlist-uită; nu publică `Suite`, `Test package`, `Flow`, `Fixture` sau `Checkout`, iar duratele sunt umane (`20s`, `45s`) |

| `SHOP-E2E-ROOT-01` | `PracticeShopTestSupport.openE2e()` și `E2eHarness` centralizează configurația, contextul și locatorii; testele E2E rămân compoziții business scurte |
| `SHOP-E2E-ISOLATION-01` | cele trei E2E folosesc `BeforeEach/AfterEach`, fără `PER_CLASS`/`BeforeAll`; `PracticeShopArchitectureTest` verifică izolarea contextului per test |
| `SHOP-HTML-CONTRACT-01` | `PracticeShopReportHtmlTestSupport` verifică semantic text, celule exacte, ordine și absență; contractele report nu mai depind de un singur `html.contains(...)` pentru fiecare regulă |
| `SHOP-TEST-LIFECYCLE-01` | `PracticeShopApiClient implements AutoCloseable`, close-ul curăță bearer state și respinge utilizarea ulterioară; integration tests folosesc try-with-resources |
| `SHOP-TEST-DSL-01` | `PracticeShopTestSupport` oferă adnotări/condiții typed pentru authenticated fixture și live API; gating-ul nu mai este copiat prin metode locale `isConfigured()` |
| `SHOP-REPORT-NET-01` | `ReportNetworkInspectorTest` + contract de publisher: proiecție collapsed cu sequence/step/role/initiator/correlation/auth/attempts/retries și aggregate typed; body-ul rămâne în evidence locală |
| `SHOP-TEST-TRUST-01` | `PracticeShopPageValidator`, valori DOM observate, `PracticeShopPaymentPage`, testul negativ pentru login fără `current-user` și guard-ul `PracticeShopArchitectureTest` acoperă E2E plus flow/evidence support; verdicturile literal-`true` din journey și setup au fost eliminate |
| `REP-AUTH-01`, `SHOP-REPORT-AUTH-01` | `ReportAuthMethod`, `ReportAuthObservation`, `ReportNetworkInspectorTest` și publisher integration: taxonomy typed per exchange și aggregate method/source/status/count, cu valorile credential excluse |
| `REP-NET-02`, `REP-RETRY-01` | `ReportNetworkInspector`, `UiRetryPolicy`, `UiRetryOperation`, action/config/render tests; primitivele sunt renderer-neutral, aggregate-ul este namespaced și retry-ul rămâne fail-closed |
| `SHOP-NET-02` | `Scenario.warning` + `publishDiagnostic` + aggregate inspector: facts guest sunt typed (`GET /users/me → 401`), warning-ul este `Runtime diagnostic`, iar response/console sunt agregate cu count/sources |
| `SHOP-DIAGNOSTIC-03` | `Scenario.warningDiagnostics`, `PracticeShopRuntimeDiagnosticsTest` și fresh guest E2E/MCP | warning-ul consumerului proiectează o singură assertion per semnătură semantică, cu count/sources observabile; response și console nu mai apar ca assertions duplicate |
| `REP-DIAGNOSTIC-01`, `REP-DIAGNOSTIC-02`, `REP-DIAGNOSTIC-04`, `REP-TRACE-01` | `UiEvidencePublisher.publishDiagnostic`, `UiPhaseLifecycle.apiTrace`, `ReportNetworkInspectorTest` și consumer proof; boundary-ul semantic, ordinea cauzală, dedupe-ul aggregate și tokenizarea bounded a semnalelor necorelate sunt închise |
| `SHOP-DIAGNOSTIC-SEMANTICS-01`, `SHOP-TRACE-INTERLEAVE-01` | warning semantic și ordinea cauzală UI/API sunt verificate component/integration/E2E; compoziția single-node este închisă prin `UI validation` + `API validation` sub același checkpoint |
| `REP-CONSOLE-01` | `atf-reporting-tests/.../AtfConsoleBridgeTest`; round-trip pentru toate statusurile și porturile production independente |
| `REP-SUITE-01` | `AtfReportingUnitSuite`, `AtfReportingComponentSuite`, `AtfReportingIntegrationSuite`, `AtfReportingApiFixtureSuite`, `AtfReportingUiAuditSuite` și `ReportingCoverageMatrixTest`; fiecare layer are launcher IntelliJ/Maven distinct, package/tag explicit și plafon E2E păstrat |
| `GOV-PACKAGE-08` | `AtfPackageCohesionGovernanceTest`; `ui.network`, `support.api` și `support.config.environment` separă boundary-urile semantice; maximum 8 clase pe pachet | split coeziv verificat, fără excepție artificială și fără relaxarea pragului |
| `TEST-READ-01` | `AtfSourceReadabilityGovernanceTest` + `ExtentReportSessionContractTest`; constructorul folosește importuri normale pentru settings, sink și redactor | invocațiile fully-qualified eliminate și guard-ul whole-ATF este verde |
| `DOC-READ-01`, `DOC-READ-02` | digestie vizuală și documente owner refăcute |
| `DOC-DIAG-01..05` | forme, orientări, legende și guard-uri de diagramă |
| `ROOT-README-01..04` | root baseline stabil și protejat |

## Ownership-ul documentației

| Contract | Document normativ |
|---|---|
| module și reactor | [`docs/ATF_Architecture.md`](../ATF_Architecture.md) |
| reporting standalone | [`atf-reporting/README.md`](../../atf-reporting/README.md) |
| reporting tests | [`atf-reporting-tests/README.md`](../../atf-reporting-tests/README.md) |
| Practice Shop | [`atf-test-practice-shop/README.md`](../../atf-test-practice-shop/README.md) |
| whole-ATF governance | [`atf-tests/README.md`](../../atf-tests/README.md) |
| reporting contract | [`docs/ATF_Reporting_Contract.md`](../ATF_Reporting_Contract.md) |
| code and test best practices | [`docs/README_Atf_CodeBestPractices.md`](../README_Atf_CodeBestPractices.md) |
| documentation best practices | [`docs/README_Atf_DocumentationBestPractices.md`](../README_Atf_DocumentationBestPractices.md) |
| build and sync | [`docs/ATF_Build_And_Execution_Safety.md`](../ATF_Build_And_Execution_Safety.md) |

## Protocol de închidere

<details>
<summary><strong>Checklist pentru orice schimbare</strong></summary>

- [ ] codul live și POM-ul confirmă ownerul real;
- [ ] testul focalizat reproduce comportamentul la nivelul potrivit;
- [ ] guard-ul relevant trece: module, architecture, documentation sau coverage;
- [ ] dacă se schimbă HTML/CSS/JavaScript, artifactul este fresh și verificat
      prin adapter static sau MCP/Chromium;
- [ ] statusul curent este actualizat, fără secțiune datată nouă;
- [ ] regula stabilă este mutată în README-ul ownerului;
- [ ] feedback-ul păstrează numai limita și criteriul de redeschidere;
- [ ] nu se păstrează nume de artifact, path local, timestamp sau secret.

</details>

## Regula de redeschidere

Un item se redeschide numai pentru:

- regresie reproductibilă;
- schimbare de ownership sau dependency boundary;
- consumer nou care nu este acoperit de contract;
- input/configurație nouă;
- schimbare de contract public.

Un screenshot, un timestamp, o eroare de runner izolată sau o schimbare locală
de stil nu redeschide singur un item închis.
