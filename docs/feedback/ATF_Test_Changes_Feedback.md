# ATF — ToDo-uri pentru schimbarea testelor

## Pe scurt

Schimbările testelor din modulele ATF, exceptând `atf-test-practice-shop`,
cer un ToDo structurat înainte de editare. Fiecare cerere fixează baseline-ul
Git, fișierele exacte, motivul, expirarea și proba cerută.

## CONSOLE-DEFAULTS-01 — detalii implicite la consolă

Cerut direct: UI/API din Reporting și Shop afișează detaliile implicit,
inclusiv la lansare din IntelliJ; YAML poate dezactiva explicit proiecția.
Probe: fallback fără YAML, override `false`, fluxurile core/reporting și
verificarea root-urilor canonice. Starea `VERIFIED` păstrează înregistrarea
până când schimbarea este integrată și baseline-ul nu o mai include.

```yaml
testChanges:
  - id: CONSOLE-DEFAULTS-01
    status: VERIFIED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Detalii implicite la consola UI/API cu override YAML si wiring verificabil.
    proof: mvn -pl atf-console,atf-reporting-tests,atf-tests,atf-test-practice-shop -am -Dtest=AtfConsoleConfigurationLoaderTest,AtfConsoleReporterTest,ReportingTestConsoleTest,ApiModuleFactoryTest,ConsoleDefaultsGovernanceTest,ShopConsoleTelemetryTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-console/src/test/java/atf/api/console/AtfConsoleConfigurationLoaderTest.java
      - atf-console/src/test/java/atf/api/console/AtfConsoleReporterTest.java
      - atf-reporting-tests/src/test/java/atf/api/tests/support/console/ReportingTestConsole.java
      - atf-reporting-tests/src/test/java/atf/api/tests/support/console/ReportingTestConsoleTest.java
      - atf-reporting-tests/src/test/java/atf/api/tests/support/junit/ApiTestFixture.java
      - atf-reporting-tests/src/test/java/atf/api/execution/ApiModuleFactory.java
      - atf-reporting-tests/src/test/java/atf/api/execution/ApiModuleFactoryTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/audit/ReportAuditContext.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/api/ApiReportAuditSetup.java
      - atf-tests/src/test/java/atf/api/tests/governance/console/ConsoleDefaultsGovernanceTest.java
```

## TEST-CHANGE-GOVERNANCE-01 — teste protejate prin feedback

Cerut direct: diferența Git se validează contra cererilor de mai sus și din
celelalte documente feedback. Testele noi protejează inclusiv propria suită;
nu se acceptă wildcard-uri, cereri expirate/închise sau un baseline străin.
Scope-ul protejează și POM-ul rădăcină, respectiv POM-urile modulelor:
ele pot configura ori dezactiva testele. Excepția Shop se păstrează.
Proba de transfer cere închiderea vechiului owner înainte de activarea celui nou.

```yaml
testChanges:
  - id: TEST-CHANGE-GOVERNANCE-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Guvernanta explicita a schimbarilor testelor si probe negative ale politicii.
    proof: mvn -pl atf-tests -am -Dtest=TestChangeGovernanceTest,TestChangePolicyTest,GitTestChangesTest,ConsoleDefaultsGovernanceTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-tests/pom.xml
      - atf-tests/src/test/java/atf/api/tests/governance/WholeAtfGovernanceSuiteTest.java
      - atf-tests/src/test/java/atf/api/tests/governance/changes/TestChangeRequest.java
      - atf-tests/src/test/java/atf/api/tests/governance/changes/FeedbackTestChangeRequests.java
      - atf-tests/src/test/java/atf/api/tests/governance/changes/GitTestChanges.java
      - atf-tests/src/test/java/atf/api/tests/governance/changes/TestChangePolicy.java
      - atf-tests/src/test/java/atf/api/tests/governance/changes/TestChangeGovernanceTest.java
      - atf-tests/src/test/java/atf/api/tests/governance/changes/TestChangePolicyTest.java
      - atf-tests/src/test/java/atf/api/tests/governance/changes/GitTestChangesTest.java
```

## REPORT-AUDIT-INPUT-01 — inputul auditului UI

Scope-ul de mai jos înregistrează schimbările Reporting deja prezente înaintea
cerinței consolei: input de audit explicit sau generat și compoziția fixture-ului
UI. Se verifică separat; nu sunt asimilate schimbării de default a consolei.
`ReportAuditInputsTest` declară explicit `@AtfComponentTest`; verificarea
`AtfTestLayerArchitectureTest` respinge testele executabile fără strat declarat.

```yaml
testChanges:
  - id: REPORT-AUDIT-INPUT-01
    status: VERIFIED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Verificarea schimbarilor existente pentru selectia inputului auditului UI.
    proof: mvn -pl atf-reporting-tests -am -Dtest=ReportAuditInputsTest,AtfTestLayerArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/e2e/failure/FailureEvidenceFlowTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/e2e/navigation/ReportNavigationAggregationFlowTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/e2e/uireport/StaticUiExtentReportContractTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/audit/ReportAuditInputs.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/uireport/browser/UiReportContractFixture.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/audit/ReportAuditInputsTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/uireport/browser/UiReportContractInputFactory.java
```

## REPORT-EVIDENCE-STATIC-GATE-01 — contract structural UI/API

La activarea politicii de mai sus, această modificare de test exista deja în
checkout și era susținută de cerința `ATF_Reporting_Feedback.md` privind
respingerea unui fals verde când zonele de evidence lipsesc din HTML. Este
înregistrată separat ca tranziție a baseline-ului; modificările viitoare cer
în continuare un ToDo înainte de editare.

```yaml
testChanges:
  - id: REPORT-EVIDENCE-STATIC-GATE-01
    status: VERIFIED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Contractul structural trebuie sa respinga zone UI/API absente chiar daca selectorii exista in CSS.
    proof: mvn -pl atf-reporting-tests -am -Dtest=ReportEvidenceStyleContractTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/evidence/ReportEvidenceStyleContractTest.java
```

## TEST-READABILITY-01 — import explicit, contract neschimbat

Gate-ul whole-ATF a identificat o invocație calificată complet într-un test
existent. Remedierea introduce importul tipului, fără schimbarea aserțiunilor.
Proba importului a trecut; înregistrarea este închisă prin transferul ownership-ului
fișierului către `UI-CHECKPOINT-CONTEXT-01`, care acoperă modificările ulterioare
din același baseline. Acest transfer nu reprezintă o integrare Git.

```yaml
testChanges:
  - id: TEST-READABILITY-01
    status: CLOSED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Remedierea importului explicit cerut de gate fara relaxarea contractului testat.
    proof: mvn -pl atf-tests,atf-reporting-tests -am -Dtest=AtfSourceReadabilityGovernanceTest,ExtentReportUiEvidencePublishingTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidencePublishingTest.java
```

## UI-CHECKPOINT-CONTEXT-01 — context intern fără tabel HTTP în pasul UI

Cerut direct: propoziția UI nu afișează `Status / Details / Info` și
`Request details · 2 fields` pentru `request.action` și `request.target`.
Contextul rămâne în model; request/response/execution pentru schimburile API
rămân publice. Probele acoperă ambele proiecții, metadata mixtă și browserul
headless cu un checkpoint UI care deține o validare API.
Controlul de autentificare verifică rândurile și valorile canonice
`AUTH PROFILE=bearer` și `AUTHENTICATION=bearer`, nu prezența accidentală a
aliasului `AUTH TYPE` în scriptul inclus. Absența unui disclosure este verificată
în markup, nu prin interzicerea vocabularului legitim din asset-urile JavaScript.
Testul browser comun rămâne în scope-ul `REPORT-API-UNIFORMITY-01` din
[feedback-ul Reporting](ATF_Reporting_Feedback.md#scope-executabil--uniformitate-și-viewport);
comanda de probă îl execută și aici, fără să-i dubleze ownership-ul.

```yaml
testChanges:
  - id: UI-CHECKPOINT-CONTEXT-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Contextul intern al checkpoint-ului UI nu trebuie publicat ca request HTTP sintetic.
    proof: mvn -pl atf-reporting-tests -am -Dtest=UiCheckpointEvidenceProjectionTest,ExtentReportUiEvidencePublishingTest,ExtentReportDisclosureBrowserComponentTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/contract/UiCheckpointEvidenceProjectionTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidencePublishingTest.java
```

## TEST-LAYER-COMPOSITION-01 — același strat declarat prin meta-adnotare

Gate-ul complet a identificat că `@ShopApiCallComponentTest` include deja
`@AtfComponentTest`, dar inventarul textual recunoaște numai adnotările directe.
Se păstrează regula exact unui strat: rezolvarea generică folosește sursele
adnotărilor importate, respinge compoziția ambiguă și termină ciclurile.
Nu se adaugă un alias specific Practice Shop sau o excepție de modul.

```yaml
testChanges:
  - id: TEST-LAYER-COMPOSITION-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Inventarul recunoaste stratul canonic prin adnotari compuse fara sa relaxeze unicitatea.
    proof: mvn -pl atf-test-kit -am -Dtest=AtfTestLayerArchitectureTest test
    paths:
      - atf-test-kit/src/test/java/atf/api/testkit/AtfTestLayerArchitectureTest.java
```

## REPORT-DISCLOSURE-CONTRACT-01 — helper comun pentru întregul subarbore

Suita completă a găsit o aserțiune textuală rămasă la vechiul `panel.style`.
Runtime-ul live controlează deja toate panourile directe prin `nodePanels` și
`panels.forEach`, pentru a nu lăsa validările vizibile după închiderea părintelui.
Guard-ul static trebuie să ceară această iterație și actualizarea vizibilității,
nu să impună vechiul nume de variabilă. Proba browser mouse/Enter/Space rămâne
obligatorie; nu se schimbă runtime-ul pentru a satisface textul depășit.

```yaml
testChanges:
  - id: REPORT-DISCLOSURE-CONTRACT-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Guard-ul static trebuie sa verifice helper-ul live pentru toate panourile directe.
    proof: mvn -pl atf-reporting-tests -am -Dtest=ExtentReportInteractionContractTest,ExtentReportDisclosureBrowserComponentTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/session/ExtentReportInteractionContractTest.java
```

## Închidere

- Cererea rămâne `ACTIVE` în timpul verificării; `VERIFIED` păstrează scope-ul
  verificat până la integrare. `CLOSED` nu autorizează modificări noi.
- După integrare se poate elimina înregistrarea când baseline-ul verificării
  nu mai include schimbarea. Regulile stabile aparțin README-ului `atf-tests`.
- Probe rapide și negative pentru console, parserul ToDo, baseline-ul Git și
  zonele structurale de evidence sunt verificate separat, cu scope exact.
- Verificări staging, 2026-10-22: consola **29 teste**, inputurile de audit
  **4**, contractul structural **1**, readability + publicarea dovezii UI **14**
  și guvernanța focalizată **16**; toate au trecut fără skip-uri.
- `WholeAtfGovernanceSuiteTest` rămâne un diagnostic mai larg, nu proba de
  închidere a acestui feedback: la 2026-10-22 raportează probleme de densitate
  a prozei din README-urile `atf-reporting-tests` și `atf-test-practice-shop`.
  Aceste documente de modul nu sunt modificate în acest task.
