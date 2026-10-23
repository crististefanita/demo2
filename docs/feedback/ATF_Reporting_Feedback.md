# ATF Reporting — stare și controale temporare

Revizia capturilor invalide este verificată; gate-ul cleanup adăugat în paralel
rămâne separat, până la probele sale proprii.
Contractele stabile aparțin [Reporting](../../atf-reporting/README.md),
[Reporting Tests](../../atf-reporting-tests/README.md),
[contractului semantic](../ATF_Reporting_Contract.md) și
[contractului de display](../ATF_Reporting_Display_Contract.md).

## REPORT-ASSET-CLEANUP-LOCK-05 — sharing violation la publicare

Windows poate raporta un fișier Spark ocupat prin `FileSystemException`,
nu numai `AccessDeniedException`; retry-ul existent acoperă numai subclasa.

- [ ] Păstrați limita existentă de încercări și intervalul bounded pentru ambele
      forme; eșecul persistent și întreruperea trebuie propagate, nu ascunse.
- [ ] Probați politica injectată și reluați suita componentă completă, inclusiv
      publicarea raportului de contract și randarea prin ATF Debug/MCP.

## Reconcilierea cerințelor

| ID | Stare | Rezultatul verificat |
|---|---|---|
| `UI-FAILURE-CAPTURE-03` | închis | Base64 malformed și non-PNG sunt indisponibile, nu capturate. Patru regresii noi Extent/fallback păstrează cauza originală fără link fals; fixture-ul inspectabil include cazul invalid. |
| `REPORT-README-SCAN-04` | închis | Contractele celor două componente sunt grupate într-un tabel; informația și completările paralele rămân păstrate. Gate-ul documentației trece. |
| `REPORT-LIVE-CONTROL-04` | închis | Cele cinci diferențe au fost revizuite explicit față de HEAD și reverificate prin componente și Test Kit; controlul exact nu rebazează și nu înlocuiește aprobările anterioare. |
| `REPORT-BROWSER-PLACEMENT-02` | închis | Proba Network păstrează toate cele cinci cazuri în ownerul UI component; selectorii aparțin fațadei `ReportNetworkCaptureLocators`. Guard-urile browser/locatori nu au fost relaxate. |
| `UI-FAILURE-CAPTURE-02` | închis | `Failure evidence` explică starea capturii prin `OBSERVE/INFO`, fără HTTP fictiv, link fals sau aserțiune business nouă. Extent și fallback păstrează acțiunea și cauza. |
| `REPORT-CLIENT-EXPECTATION-02` | închis | Oracol neutru exact `Configured per execution`; Clientul explicit este păstrat și redactat în System. Probele de publicare HTML sunt componente. |

## Boundary și probe

- **Dovezi:** calea fără captură închide disclosure-ul acțiunilor înaintea
  observațiilor și aserțiunilor; snapshot-ul fallback păstrează conținutul,
  nu numai numărătorile.
- **Redactare:** captura folosește ținta `current page`, fără a copia un
  locator cu secrete; proba existentă de redactare rămâne intactă.
- **Gate-uri:** suita componentă completă, suita unit, probele standalone,
  Test Kit și controalele selectate de schimbare/coeziune/documentație sunt
  verificate în staging extern.
- **Standalone:** inventarul finit include proba nativă de ceas; Reporting
  nu primește dependență către Test Kit.
- **TAB MCP:** ATF Debug confirmă cauza, starea capturii, imaginea validă,
  Fails → Open in Tests și System Client; fără link fals, overflow sau erori
  de consolă în suprafețele verificate.
- **Scope:** rapoartele controlate de failure sunt fixture-uri, nu rulări
  business Shop/AutoEx.
- **Revizia capturilor invalide:** 251 de probe în rularea componentă extinsă,
  401 unit, 14 standalone, 52 Test Kit și 7 controale selectate trec în staging
  extern. Matricea capturilor are 35 de cazuri; verificarea comună inspectează
  encoding-ul Base64 și semnătura PNG, nu decodarea integrală a imaginii.
- **Limită live:** rezultatele acestei revizii nu certifică schimbările cleanup
  sau AutoEx apărute ulterior în paralel; gate-ul lor rămâne explicit separat.

Controalele acestei revizii au fost transferate în
[baseline-ul tehnic](../ATF_Test_Changes_Baseline.md). Aprobările altor
incrementuri rămân mai jos, fără a fi confundate cu ToDo-uri funcționale.

## Aprobări temporare pentru modificări de teste

Aceste controale sunt aprobări temporare pentru schimbările exacte de probe
enumerate mai jos; nu sunt TODO-uri funcționale și nu autorizează modificarea
testelor de guvernanță.

```yaml
testChanges:
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
  - id: REPORT-ASSET-CLEANUP-LOCK-05
    status: ACTIVE
    base: 5eb3fd3cb099fc67b49be3c17de564db63755db7
    expires: 2026-11-24
    reason: Probează ambele forme de lock Windows, epuizarea bounded și întreruperea, fără a schimba plafonul cleanup-ului sau a ignora erorile de publicare. Revizuirea exactă a fișierului confirmă același conținut de bază în 5eb3fd3 și b9b1325; controlul folosește baza comună a gate-ului, fără extindere de path-uri și fără a declara incrementul verificat.
    proof: mvn -pl atf-reporting-tests -am "-Dtest=ExtentReportAssetCleanupTest,AtfReportingComponentSuite,UiFailureReportContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/markup/ExtentReportAssetCleanupTest.java
  - id: REPORT-INVALID-CAPTURE-03
    status: ACTIVE
    base: b9b1325c28d665be6289bb2cf9286666513e7a05
    expires: 2026-11-24
    reason: Adaugă payload-uri Base64 invalide și non-PNG în matricea Extent/fallback, fără a elimina cazurile integrate în paralel; verifică absența linkurilor false și păstrarea cauzei originale.
    proof: mvn -pl atf-reporting-tests -am "-Dtest=UiAuditLifecycleTest,ReportEvidenceProfileIntegrationTest,ReportScreenshotTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/UiAuditLifecycleTest.java
  - id: REPORT-FAILURE-CLIENT-COMPONENT-04
    status: ACTIVE
    base: 7b365028ac8356960614e35023556e757311e633
    expires: 2026-11-24
    reason: Păstrează cazurile originale și cele integrate în paralel; adaugă matricea politicilor, snapshot-uri Extent/fallback și cazul fără acțiuni, păstrând Client exact o dată și starea capturii într-un vocabular tipizat.
    proof: mvn -pl atf-reporting-tests -am "-Dtest=AtfReportingComponentSuite" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/UiAuditLifecycleTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/session/ExtentReportSessionContractTest.java
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
  - id: REPORT-CLIENT-METADATA-01
    status: ACTIVE
    base: a23c76e662516504f55a4369c58ca98bbf2f26ec
    expires: 2026-10-30
    reason: Verify the generic System Client fallback is neutral when consumer metadata is absent.
    proof: mvn -pl atf-reporting-tests -am "-Dtest=ReportEnvironmentSnapshotTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/configuration/ReportEnvironmentSnapshotTest.java
```
