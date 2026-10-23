# ATF Reporting — lacune active

Aici rămân lacunele deschise și controalele tehnice încă necesare.
Contractele stabile aparțin [Reporting](../../atf-reporting/README.md),
[Reporting Tests](../../atf-reporting-tests/README.md),
[contractului semantic](../ATF_Reporting_Contract.md) și
[contractului de display](../ATF_Reporting_Display_Contract.md).

## REPORT-OBSERVED-HEADERS-01 — fidelitatea request-ului observat

**Stare: deschis, confirmat în cod și în artifactul UI/API fresh.**

`ApiManualReplay.effectiveHeaders(...)` adaugă implicit `Accept` și
`Content-Type: application/json` înaintea headerelor capturate. La XHR-ul GET
fără body, Request details și replay-ul arată un Content-Type neobservat;
aceeași zonă afișează și `REQUEST JSON: null` deși capturarea declară body absent.

- [ ] Distingeți explicit în model observația browser autoritativă de
      default-urile efective ale clientului API; nu creați alt overload legacy.
      Un header absent din observație nu devine header executat.
- [ ] Aliniați Request details și replay-ul sanitizat la aceeași proiecție;
      nu deduceți Content-Type-ul request-ului din răspuns sau din URL.
- [ ] Pentru body absent ori necapturat nu inventați un payload JSON `null`;
      păstrați distinct cazul în care textul JSON literal `null` a fost trimis.
- [ ] Probați Extent, fallback și UI/API în `atf-reporting-tests`: GET fără body,
      headere observate explicite, body necapturat, JSON literal null și
      default-uri reale ale clientului API. Păstrați redacția și istoricul retry.
- [ ] Reconciliați regula istorică „body absent → JSON null” din
      [contractul semantic](../ATF_Reporting_Contract.md#dovezile-unei-cereri)
      cu distincția body absent / necapturat / JSON literal `null`, odată cu fixul.

## REPORT-API-FAILURE-OWNER-01 — owner API corect în Fails

**Stare: deschis, reprodus în MCP pe fixture neutră cu model tipizat.**

UI rămâne PASS, API validation și exchange-ul invalid sunt FAIL, iar cauza
concretă este păstrată. Totuși Fails afișează `STEP UI assertion` pentru
`API assertions`; detecția generică din `extent-failure-detection.js`
setează `ui: true` și poate înlocui contextul request-ului API.

- [ ] Derivați ownerul din contextul semantic tipizat al validării și al
      request-ului, nu din titlul liber sau dintr-un tabel de assertions comun.
- [ ] Păstrați metoda/ruta și checkpoint-ul Gherkin proprietar pentru eșecul API;
      un API FAIL nu este redenumit UI assertion și nu recolorează UI PASS.
- [ ] Extindeți proba browser neutră din `atf-reporting-tests` pentru ambele
      combinații UI/API și pentru exchange-uri surori, inclusiv Fails și
      Open in Tests. Nu adăugați teste generice în AutoEx ori Shop.

## REPORT-NARROW-READABILITY-01 — tabele imbricate la lățime mică

**Prioritate:** 64/100 = impact 28 + lacună 18 + lucrabil 14 + documentare 4.
În viewport-uri înguste, inclusiv la 591 px și aproximativ 392 px,
`Failure evidence` cu observații și aserțiuni deschise comprimă coloanele până
devin ilizibile. Absența overflow-ului orizontal nu dovedește lizibilitatea;
gate-urile existente nu acoperă suficient această adâncime de imbricare.

- [ ] Adăugați o fixture neutră cu aceeași adâncime și texte scurte/lungi,
      verificată la lățime standard, 591 px și lățime de panou îngust.
- [ ] Verificați lățimea utilă a coloanelor, textul vizibil și accesul la ultimul
      rând prin constante explicite, nu numai `scrollWidth <= clientWidth`.
- [ ] Reduceți spațiul consumat de nesting sau aplicați o prezentare mobilă;
      păstrați ierarhia, fontul lizibil și expand/collapse, fără a relaxa plafoanele.

## Controale tehnice încă necesare

Aceste aprobări protejează diferențe Git, nu sunt ToDo-uri funcționale.
Transferul și eliminarea lor urmează
[baseline-ul tehnic](../ATF_Test_Changes_Baseline.md#reguli-de-folosire),
fără rebazare, extindere de scope sau relaxarea guard-urilor.

<details>
<summary><strong>Controale tehnice active</strong></summary>

```yaml
testChanges:
  - id: REPORT-OBSERVED-OWNER-LIVE-08
    status: ACTIVE
    base: 9b0860bfc734df570c7512b76aa5dbb559527b7d
    expires: 2026-11-24
    reason: Revizuire explicită a incrementului observat/owner față de noul commit live; păstrează migrarea paralelă UiScreenAudit din AutoEx și toate cazurile anterioare, adaugă owner UI/API și disponibilitate body fără relaxarea guard-urilor. Preia unic AutoExE2eRun de la controlul consumer pentru această bază; controlul original 5eb3fd3 nu este rebazat.
    proof: mvn -pl atf-reporting-tests,atf-test-auto-ex,atf-test-practice-shop,atf-tests -am "-Dtest=AtfReportingUnitSuite,AtfReportingComponentSuite,AtfReportingIntegrationSuite,ShopComponentSuite,AutoExComponentSuite,WholeAtfGovernanceSuiteTest" "-Datf.governance.testChanges.baseRef=9b0860bfc734df570c7512b76aa5dbb559527b7d" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/request/ReportRequestHeadersTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/request/ObservedRequestEvidenceComponentTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/support/request/ObservedRequestFixture.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/validation/UiApiValidationContractComponentTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/evidence/ApiBodyMediaComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/reconciliation/UiApiVerdictProjectionProbe.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eRun.java
  - id: REPORT-OBSERVED-OWNER-07
    status: ACTIVE
    base: 5eb3fd3cb099fc67b49be3c17de564db63755db7
    expires: 2026-11-24
    reason: Probează input-ul observat versus default-uri configurate, body absent/necapturat/null literal și ownerul API în Fails cu exchange-uri surori; păstrează cazurile existente și migrează numai boundary-ul observat AutoEx. Preia explicit path-urile ApiBodyMediaComponentTest și AutoExE2eRun din controalele VERIFIED anterioare pentru acest increment încă ACTIVE, fără dublarea ownerului la aceeași bază sau certificarea anticipată a fixului.
    proof: mvn -pl atf-reporting-tests -am "-Dtest=ReportRequestHeadersTest,ObservedRequestEvidenceComponentTest,ApiBodyMediaComponentTest,UiApiVerdictProjectionComponentTest,AtfReportingComponentSuite" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/request/ReportRequestHeadersTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/request/ObservedRequestEvidenceComponentTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/support/request/ObservedRequestFixture.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/validation/UiApiValidationContractComponentTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/evidence/ApiBodyMediaComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/reconciliation/UiApiVerdictProjectionProbe.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eRun.java
  - id: REPORT-ASSET-CLEANUP-COMPONENT-06
    status: ACTIVE
    base: 5eb3fd3cb099fc67b49be3c17de564db63755db7
    expires: 2026-11-24
    reason: Adaugă numai fixture-ul Spark offline pentru două cazuri componente ale pipeline-ului de publicare; probele existente ale cleanup-ului și sesiunii păstrează controalele lor exacte, fără dublare de owner sau rebazare automată.
    proof: mvn -pl atf-reporting-tests -am "-Dtest=ExtentReportAssetCleanupTest,ExtentReportSessionContractTest,AtfReportingComponentSuite,UiFailureReportContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/support/publication/CleanupPublicationFixture.java
  - id: REPORT-LIVE-CONTROL-04
    status: ACTIVE
    base: b9b1325c28d665be6289bb2cf9286666513e7a05
    expires: 2026-11-24
    allowDelete: true
    reason: Revizuiește explicit diff-ul curent al relocării Network cu toate cele cinci cazuri și fațada de locatori, al probei Client și al inventarului finit standalone. Nu modifică guard-urile; reexecută probele pentru baza curentă, fără rebazarea automată a controalelor anterioare.
    proof: mvn -pl atf-reporting-tests,atf-test-kit -am "-Dtest=AtfReportingComponentSuite,AtfTestLayerArchitectureTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/session/ExtentReportSessionContractTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/network/NetworkCaptureEvidenceTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/inspection/network/NetworkCaptureEvidenceTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/locators/network/ReportNetworkCaptureLocators.java
      - atf-test-kit/src/test/java/atf/api/testkit/AtfTestLayerArchitectureTest.java
  - id: SHOP-CAPTURE-01
    status: ACTIVE
    base: 0f6a95265024a78c20754d5c18937d24434a2d2d
    expires: 2026-10-30
    reason: Probează agregarea fazelor Shop, ownership-ul API și cazul setup eșuat fără body ori screenshot terminal.
    proof: mvn -pl atf-reporting-tests,atf-test-practice-shop -am "-Dtest=ShopComponentSuite,ReportEvidenceProfileIntegrationTest,ExtentUnifiedFlowMultipleScreenshotTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/profile/ReportEvidenceProfileIntegrationTest.java
  - id: REPORT-UI-ACTION-REDACTION-01
    status: ACTIVE
    base: a23c76e662516504f55a4369c58ca98bbf2f26ec
    expires: 2026-10-30
    reason: Prove card/CVC redaction before UI evidence recording, including exception and diagnostic trace paths, plus the rendered HTML artifact.
    proof: mvn -pl atf-ui -am "-Dtest=PlaywrightActionSequenceTest" "-Dsurefire.failIfNoSpecifiedTests=false" test; mvn -pl atf-reporting-tests -am "-Dtest=ExtentReportUiEvidenceRenderingTest,ReportEnvironmentSnapshotTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-ui/src/test/java/atf/api/ui/playwright/PlaywrightActionSequenceTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidenceRenderingTest.java
```

</details>
