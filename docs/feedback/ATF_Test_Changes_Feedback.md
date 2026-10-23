# ATF — controale tehnice active ale schimbărilor de teste

## Pe scurt

Acest registru păstrează numai controalele `ACTIVE` pentru diferențe Git în
lucru. Fiecare control precizează perimetrul tehnic și comanda de probă;
cerința și criteriile funcționale rămân în feedback-ul domeniului.

| Control | Cerință urmărită | Responsabil tehnic |
|---|---|---|
| `REPORT-LARGE-ARTIFACT-01` | [Reporting — PUB-01](ATF_Reporting_Feedback.md#pub-01--bugetul-de-memorie-al-publicării) | Reporting / Reporting Tests: configurația JVM și probele de publicare |
| `ANN-CHANNEL-01` | [Canalele UI/API](ATF_Test_Channel_Annotations_Feedback.md) | TestKit / adaptoarele consumatoare: resolver și preflight |
| `ANN-INFRA-EXTRACTION-01` | [Infrastructura adnotărilor](ATF_Test_Infrastructure_Annotations_Feedback.md) | TestKit / Reporting Tests / guvernanță: output, setup și documentație |

Procedura, schema și configurarea CI sunt în
[README ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).
Contextul business, rolurile și transferul în `VERIFIED` sunt explicate în
[registrul controalelor verificate](../ATF_Test_Changes_Baseline.md).

## Controale active de schimbare

Eligibilitatea se verifică pentru baza Git a rulării și data curentă. După
verificarea incrementului, intrarea se transferă în registrul `VERIFIED` numai
dacă diferența încă o cere; când nu mai există controale active, fișierul poate
fi eliminat împreună cu referințele sale, păstrând [indexul feedback](README.md).

```yaml
testChanges:
  - id: REPORT-LARGE-ARTIFACT-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Verificarea bugetului JVM configurabil si a integritatii dovezilor din rapoarte voluminoase.
    proof: mvn -pl atf-reporting-tests -am -Dtest=ExtentReportFinalizerBoundaryTest,LargeReportPublicationComponentTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - pom.xml
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/session/ExtentReportFinalizerBoundaryTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/publication/LargeReportPublicationComponentTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/support/publication/LargeReportPublicationProbe.java
  - id: ANN-CHANNEL-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Canal typed rezolvat si aplicat la adaptoarele Shop cu test neutru si preflight.
    proof: mvn -pl atf-test-kit,atf-test-practice-shop,atf-tests -am -Dtest=AtfTestChannelResolverTest,ShopE2eTestSupportTest,ShopArchitectureTest,WholeAtfGovernanceSuiteTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-test-kit/src/test/java/atf/api/testkit/channel/AtfTestChannelResolverTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/e2e/sanity/UiThenDisclosureSanityTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/e2e/ExtentReportingE2eSuite.java
  - id: ANN-INFRA-EXTRACTION-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Extrage output setup si document targets in TestKit cu probe de rezolvare si integrare.
    proof: mvn -pl atf-test-kit,atf-reporting-tests,atf-tests -am -Dtest=ReportOutputDirectoryTest,UiReportSubdirectoryTest,WholeAtfGovernanceSuiteTest -Dsurefire.failIfNoSpecifiedTests=false test
    allowDelete: true
    paths:
      - atf-test-kit/src/test/java/atf/api/testkit/output/ReportOutputDirectoryTest.java
      - atf-test-kit/src/test/java/atf/api/testkit/output/ReportOutputSymlinkFixture.java
      - atf-tests/src/test/java/atf/api/tests/governance/documentation/DocumentationTarget.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/section/runtime/SectionAuditPreconditions.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/api/ApiReportSetupDeclarations.java
      - atf-reporting-tests/src/test/java/atf/api/tests/support/setup/ApiScenarioSetupCatalog.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/plan/ApiSelectionMockPublisherComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/plan/ApiScenarioManifestTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/architecture/DeclarativeUiTestArchitectureTest.java
      - atf-reporting-tests/src/test/java/atf/api/tests/support/ApiScenarioSetupCatalogTest.java
```
