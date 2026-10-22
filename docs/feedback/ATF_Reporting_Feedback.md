# ATF Reporting — reconciled feedback

## Stare

Implementarea generică și randarea sunt închise; singurul follow-up raportat aici ține de reîncărcarea modelului Maven din IntelliJ, nu de un defect reprodus în librărie.

| Subiect | Situație verificată |
|---|---|
| Contracte reporting | Tabelele, summary-urile, trace-ul UI/API și `REP-UX-FLOW-01` au implementare generică, contract documentat și teste component/integration pe HTML. Nu există ToDo generic activ în `atf-reporting` / `atf-reporting-tests`. |
| Network Inspector | Summary nested folosește acum regula comună de `34px`; excepția `68px` rămâne doar pentru aggregate standalone. Proba browser măsoară ambele viewport-uri; fără CSS special în Practice Shop. |
| IntelliJ | `.idea/modules.xml` enumeră doar `atf-tests`, deși run configurations vizează `atf-test-practice-shop`, iar root Maven include reporting modules. Este un indiciu de model IDE stale, nu cauza runtime demonstrată. |
| Pasul următor | Reload All Maven Projects la root `pom.xml`, confirmă `atf-reporting` / `atf-reporting-tests` și rerulează aceeași configurație. Dacă persistă, capturează classpath-ul efectiv; nu muta și nu duplica `ReportWorkspaceSweeper`. |

- Captura IntelliJ E2E a raportat `NoClassDefFoundError: ReportWorkspaceSweeper`
  la cleanup; aceeași clasă există în `atf-reporting` și este invocată prin
  `ReportStagingWorkspace`. Reactorul, executat cu fork JVM și staging extern,
  trece `ReportStagingWorkspaceTest` **6/6**, deci nu există reproducere Maven.
- În configurația IDE locală inspectată, `.idea/modules.xml` înregistrează doar
  modulul legacy `atf-tests`, în timp ce configurațiile E2E din `workspace.xml`
  selectează `atf-test-practice-shop`; root `pom.xml` declară ambele module,
  plus `atf-reporting` și `atf-reporting-tests`. Acest mismatch indică un model
  Maven IntelliJ stale, dar nu dovedește singur cauza runtime.
- Pasul rămas este **Reload All Maven Projects** pe root `pom.xml`, apoi
  confirmarea modulelor `atf-reporting` / `atf-reporting-tests` și rerularea
  aceleiași configurații IntelliJ. Dacă eroarea persistă după reload, capturează
  classpath-ul efectiv al run configuration-ului înainte de orice schimbare de
  cod. Nu muta și nu duplica `ReportWorkspaceSweeper` pentru a compensa IDE-ul.

Contractul stabil este descris în [ATF Reporting Contract](../ATF_Reporting_Contract.md)
și [ATF Reporting README](../../atf-reporting/README.md). Practice Shop îl
consumă; cerința sa nu justifică un registry generic de UI substeps.

## Contracte închise

| ID | Contract stabil | Proof de regresie |
|---|---|---|
| `REP-UX-TABLE-03` | wrapper-ele de tabele au `height:auto` și plafon comun `TABLE_SCROLL_HEIGHT=500px`; conținutul scurt rămâne natural, iar cel lung derulează intern sub header sticky | `AtfReportEvidenceCssTest`, `AtfReportOverviewCssTest`, `CategoryCardinalityBrowserFixtureTest` și `ScrollableTableViewportBrowserProbe` |
| `REP-UX-SUMMARY-01` | disclosure-urile nested `Unique request signatures` și `Technical diagnostics` au geometria comună de `34px`, collapsed implicit, fără spațiul gol produs de vechea regulă `68px` | `AtfReportThemeTokensTest`, `AtfReportEvidenceCssTest`, `CategoryCardinalityBrowserFixtureTest` la viewport standard și `591px` |
| `REP-COMP-API-01` | selecția exactă API ajunge până la publisher prin mock/lifecycle, iar selecția invalidă se oprește înainte de publicare | `ApiReportAuditSetupTest`, `ApiScenarioManifestTest`, `ApiSelectionMockPublisherComponentTest` |
| `REP-TRACE-03` | ownerul semantic deține UI validation și API validation ca evidence siblings; retry-ul aceluiași request rămâne un singur exchange logic | [`ExtentReportUiEvidencePublishingTest`](../../atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidencePublishingTest.java): `keepsUiAndApiValidationUnderOneGherkinCheckpoint`, `rendersExecutionEvidenceForEveryExchangeInOneUiApiTrace`, `keepsRetryHistoryInsideOneExchangeNode` |
| `REP-UX-FLOW-01` | `SCREEN_CHECKPOINTS` păstrează request-urile Gherkin; `UNIFIED_FLOW` publică un singur request cu ultimul `THEN` (fallback: ultimul checkpoint), o validare UI agregată și cel mult o validare API sibling. Nu apar `Checkpoint N` sau Gherkin duplicat; screenshot policy rămâne independentă | [`ReportEvidenceProfileIntegrationTest`](../../atf-reporting-tests/src/test/java/atf/api/reporting/ui/profile/ReportEvidenceProfileIntegrationTest.java): `unifiedFlowPublishesOneBusinessNodeWithNestedEvidenceAndOnlyTheFinalScreenshot`, `screenCheckpointsKeepTheirExistingIndependentGherkinNodes`; [`ExtentReportUiEvidenceRenderingTest`](../../atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidenceRenderingTest.java): `rendersPlaywrightActionsWhenScreenshotPolicyOmitsTheCheckpointImage`; HTML fresh verificat în ATF Debug de `StaticUiExtentReportContractTest` (Playwright headless) |

Proba de viewport folosește artifacte UI și API separate, conținut scurt și
lung, viewport standard și `591px`; verifică overflow orizontal, max-height,
natural short height, header sticky și accesul la ultimul rând. Astfel, plafonul
de `500px` nu este doar o constantă afirmată în CSS: este măsurat în browser.

## Regula de redeschidere

- redeschide un ID numai dacă testul owner eșuează sau un artifact fresh arată
  regresia descrisă de contract;
- o verificare de source-only nu substituie geometria măsurată în browser;
- artifactele istorice și numărătorile de run nu sunt backlog și nu se copiază
  în feedback-ul curent;
- o schimbare viitoare de CSS sau markup cere testele unit/component relevante
  și proba browser a suprafeței afectate.

Ownership-ul fixture-urilor și al testelor rămâne în
[ATF Reporting Tests README](../../atf-reporting-tests/README.md); feedback-ul
nu dublează implementarea sau inventarul testelor.
