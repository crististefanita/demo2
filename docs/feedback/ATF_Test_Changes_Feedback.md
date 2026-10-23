# ATF — controale tehnice active ale schimbărilor de teste

## Pe scurt

Registrul păstrează controalele `ACTIVE` pentru diferențe Git în lucru.
Fiecare control rămâne activ până la confirmarea probelor propriului perimetru;
o refactorizare paralelă nu este promovată pe baza probelor altui increment.

| Control | Cerință și owner | Dovada încă lipsă |
|---|---|---|
| `PLATFORM-REUSABLE-VOCABULARY-01` | vocabular comun / Core și UI | confirmarea matricei declarate de owner înaintea transferului în VERIFIED |
| `ANN-INFRA-EXTRACTION-01` | [Infrastructura adnotărilor](ATF_Test_Infrastructure_Annotations_Feedback.md) / TestKit | `ReportOutputDirectoryTest.symlinkCannotEscapeExistingAnchor` executat pe Windows; SKIP nu dovedește boundary-ul |

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
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Extrage output setup si document targets in TestKit cu probe de rezolvare si integrare.
    proof: mvn -pl atf-test-kit,atf-reporting-tests,atf-tests -am -Dtest=ReportOutputDirectoryTest,AtfUiConfigurationLoaderTest,UiReportSubdirectoryTest,WholeAtfGovernanceSuiteTest -Datf.governance.testChanges.baseRef=521135291e642194294f9734ee1410ede0f19ef6 -Dsurefire.failIfNoSpecifiedTests=false test
    allowDelete: true
    paths:
      - atf-test-kit/src/test/java/atf/api/testkit/output/ReportOutputDirectoryTest.java
      - atf-test-kit/src/test/java/atf/api/testkit/output/ReportOutputSymlinkFixture.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/AtfUiConfigurationLoaderTest.java
      - atf-tests/src/test/java/atf/api/tests/governance/documentation/DocumentationTarget.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/section/runtime/SectionAuditPreconditions.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/api/ApiReportSetupDeclarations.java
      - atf-reporting-tests/src/test/java/atf/api/tests/support/setup/ApiScenarioSetupCatalog.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/plan/ApiSelectionMockPublisherComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/plan/ApiScenarioManifestTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/architecture/DeclarativeUiTestArchitectureTest.java
      - atf-reporting-tests/src/test/java/atf/api/tests/support/ApiScenarioSetupCatalogTest.java
```
