# ATF — controale tehnice active ale schimbărilor de teste

## Pe scurt

Registrul păstrează controalele `ACTIVE` pentru diferențe Git în lucru.
Fiecare control rămâne activ până la confirmarea probelor propriului perimetru;
o refactorizare paralelă nu este promovată pe baza probelor altui increment.

| Control | Cerință și owner | Dovada încă lipsă |
|---|---|---|
| `PLATFORM-REUSABLE-VOCABULARY-01` | vocabular comun / Core și UI | confirmarea matricei declarate de owner înaintea transferului în VERIFIED |
| `ANN-INFRA-EXTRACTION-01` | [Infrastructura adnotărilor](ATF_Test_Infrastructure_Annotations_Feedback.md) / TestKit | `ReportOutputDirectoryTest.symlinkCannotEscapeExistingAnchor` executat pe Windows; SKIP nu dovedește boundary-ul |
| `ANN-CONSOLE-MESSAGES-01` | [Politica adnotării console](ATF_Test_Infrastructure_Annotations_Feedback.md) / TestKit, Console și consumatori | probele policy/lifecycle și adopția Shop + Reporting trebuie să treacă împreună |

Resolverul/preflight-ul canalelor și proba bounded a publicării sunt în
[registrul VERIFIED](../ATF_Test_Changes_Baseline.md#controale-tehnice-verificate).
Cerințele funcționale rămase, inclusiv launcherul IntelliJ și scalabilitatea
publicării, rămân în feedback-urile domeniilor.

## Controale active de schimbare

Procedura, schema, transferul și configurarea CI sunt în
[README ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).
Când nu mai există controale active, fișierul poate fi eliminat cu actualizarea
referințelor; [indexul feedback](README.md) se păstrează permanent.

```yaml
testChanges:
  - id: PLATFORM-REUSABLE-VOCABULARY-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Centralizeaza autentificarea, diagnosticul si vocabularul DOM reutilizabil la ownerii atf-core si atf-ui, cu consumatori si guard de ownership verificabili.
    proof: mvn -pl atf-core,atf-ui,atf-reporting-tests,atf-tests -am -Dtest=ThrowableDiagnosticsTest,AuthProfilesTest,HtmlElementTest,ApiReportRenderedEvidenceComponentTest,ConstantsOwnershipGovernanceTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-core/src/test/java/atf/api/core/diagnostics/ThrowableDiagnosticsTest.java
      - atf-core/src/test/java/atf/api/core/http/auth/AuthProfilesTest.java
      - atf-ui/src/test/java/atf/api/ui/dom/HtmlElementTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/failure/FailureTransportEvidenceInspector.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/section/extract/ApiJsonEvidenceExtractor.java
      - atf-tests/src/test/java/atf/api/tests/governance/ConstantsOwnershipGovernanceTest.java
  - id: ANN-INFRA-EXTRACTION-01
    status: ACTIVE
    base: da4327b5dea8476b719779da055d445d8b2d4e48
    expires: 2026-11-21
    reason: Extrage output setup si document targets in TestKit cu probe de rezolvare si integrare.
    proof: mvn -pl atf-test-kit,atf-reporting-tests,atf-tests -am -Dtest=ReportOutputDirectoryTest,AtfUiConfigurationLoaderTest,UiReportSubdirectoryTest,WholeAtfGovernanceSuiteTest -Datf.governance.testChanges.baseRef=da4327b5dea8476b719779da055d445d8b2d4e48 -Dsurefire.failIfNoSpecifiedTests=false test
    allowDelete: true
    paths:
      - atf-test-kit/src/test/java/atf/api/testkit/output/ReportOutputDirectoryTest.java
      - atf-test-kit/src/test/java/atf/api/testkit/output/ReportOutputSymlinkFixture.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/AtfUiConfigurationLoaderTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/output/UiReportSubdirectoryTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/uireport/browser/UiReportContractInputFactory.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/failure/UiFailureReportFactory.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/audit/ReportAuditContext.java
      - atf-tests/src/test/java/atf/api/tests/governance/documentation/DocumentationTarget.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/section/runtime/SectionAuditPreconditions.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/api/ApiReportSetupDeclarations.java
      - atf-reporting-tests/src/test/java/atf/api/tests/support/setup/ApiScenarioSetupCatalog.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/plan/ApiSelectionMockPublisherComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/plan/ApiScenarioManifestTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/architecture/DeclarativeUiTestArchitectureTest.java
      - atf-reporting-tests/src/test/java/atf/api/tests/support/ApiScenarioSetupCatalogTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/UiConfiguration.java
  - id: ANN-CONSOLE-MESSAGES-01
    status: ACTIVE
    base: da4327b5dea8476b719779da055d445d8b2d4e48
    expires: 2026-11-22
    reason: Implementeaza policy typed AtfConsoleMessages si aplica printr-un singur sink in Shop UI/API si Reporting API tests.
    proof: mvn "-pl=atf-test-kit,atf-console,atf-reporting-tests,atf-test-practice-shop,atf-tests" -am "-Dtest=AtfConsoleMessagePolicyResolverTest,AtfConsoleMessagePolicyTest,AtfConsoleTestExtensionIntegrationTest,UsersApiTests,RequestVariantsApiTests,ApiHeadersAndEdgeCasesTests,ShopApiContractReportComponentTest,ShopPurchaseE2eTest,ShopUiOnlyPurchaseE2eTest,ShopUiOnlySingleFlowPurchaseE2eTest,WholeAtfGovernanceSuiteTest" "-Datf.governance.testChanges.baseRef=da4327b5dea8476b719779da055d445d8b2d4e48" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-console/pom.xml
      - atf-test-kit/src/test/java/atf/api/testkit/console/AtfConsoleMessagePolicyResolverTest.java
      - atf-console/src/test/java/atf/api/console/junit/AtfConsoleMessagePolicyTest.java
      - atf-reporting-tests/src/test/java/atf/api/tests/api/headers/ApiHeadersAndEdgeCasesTests.java
      - atf-reporting-tests/src/test/java/atf/api/tests/api/requests/RequestVariantsApiTests.java
      - atf-reporting-tests/src/test/java/atf/api/tests/support/junit/ApiResourceTestBase.java
      - atf-console/src/test/java/atf/api/console/junit/AtfConsoleTestLifecycleTest.java
      - atf-console/src/test/java/atf/api/console/junit/AtfConsoleTestExtensionIntegrationTest.java
  - id: AUTOEX-MODULE-BASE-01
    status: ACTIVE
    base: da4327b5dea8476b719779da055d445d8b2d4e48
    expires: 2026-11-22
    reason: Introduce a separately owned, opt-in Automation Exercise UI E2E consumer using existing Test Kit annotations, synthetic account data, and independently published Extent reports.
    proof: mvn "-pl=atf-test-auto-ex,atf-tests" -am "-Dtest=AutoExConfigurationTest,AutoExPurchaseE2eTest,DocumentationTargetGovernanceTest,WholeAtfGovernanceSuiteTest" "-Datf.autoex.e2e.enabled=true" "-Datf.governance.testChanges.baseRef=da4327b5dea8476b719779da055d445d8b2d4e48" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - pom.xml
      - atf-test-auto-ex/pom.xml
      - atf-test-auto-ex/src/test/resources/atf-auto-ex.yaml
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/config/AutoExConfiguration.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/config/AutoExConfigurationLoader.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/locator/AutoExRoutes.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/locator/AutoExSelectors.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/page/AccountPage.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/page/AutoExPage.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/page/CartPage.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/page/CheckoutPage.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/page/HomePage.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/page/ProductPage.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/page/ProductsPage.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExConsoleBridge.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eExtension.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eRun.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExProjectRoot.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExTestAccount.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/configuration/AutoExConfigurationTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/e2e/AutoExPurchaseE2eTest.java
      - atf-tests/src/test/java/atf/api/tests/governance/documentation/DocumentationTargetGovernanceTest.java
```
