# ATF — scope-uri de guvernanță pentru schimbarea testelor

## Pe scurt

Schimbările testelor din modulele ATF, exceptând `atf-test-practice-shop`,
cer un ToDo structurat înainte de editare. Fiecare cerere fixează baseline-ul
Git, fișierele exacte, motivul, expirarea și proba cerută.

`VERIFIED` nu înseamnă lucrare funcțională restantă: păstrează autorizarea
pentru diff-ul încă inclus în baseline. Cerințele API/UI rămase sunt numai la
[ownerul Reporting](ATF_Reporting_Feedback.md); regulile stabile sunt în
[README-ul ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).

<details>
<summary>Scope-uri verificate — păstrate pentru baseline, nu ToDo-uri deschise</summary>

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

- POM-ul rădăcină și POM-urile modulelor sunt protejate: pot configura ori dezactiva testele.
- Excepția Shop rămâne exactă, nu se extinde la module cu nume asemănătoare.
- Transferul de ownership cere închiderea vechiului scope înainte de activarea celui nou.
- Inventarul include POM-urile noi ignorate prin `.gitignore`; proba negativă
  verifică rădăcina și un modul nou, alături de testele ignorate deja acoperite.
- Indexul și working tree-ul sunt comparate separat cu baseline-ul, apoi reunite:
  restaurarea fișierului de pe disc nu ascunde un edit staged sau committed.
- Deduplicarea păstrează perechea tip/cale; o recreare nu elimină ștergerea
  staged și nu ocolește `allowDelete`. Adăugarea staged rămâne vizibilă chiar
  dacă fișierul de pe disc a fost șters.
- Fixture-urile Git pentru aceste cazuri eșuează pe inventarul vechi și trec
  după corecție; proba de închidere rămâne suita whole-ATF, nu numai fixture-ul.

```yaml
testChanges:
  - id: TEST-CHANGE-GOVERNANCE-01
    status: VERIFIED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Guvernanta explicita a schimbarilor testelor si probe negative ale politicii.
    proof: mvn -pl atf-tests -am -Dtest=WholeAtfGovernanceSuiteTest -Datf.governance.testChanges.baseRef=521135291e642194294f9734ee1410ede0f19ef6 -Dsurefire.failIfNoSpecifiedTests=false test
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

Verificarea completă cere și separarea browserului de intenția E2E: probele de
tab rămân în context, nu într-un `Page` deținut de test. Locatorul pentru dovada
UI trebuie să recunoască rolul semantic și acțiunile, inclusiv cu titlu custom;
fallback-ul textual rămâne doar pentru artifacte legacy.

Extractorul deschide și checkpoint-urile Gherkin dintre fază și validare, dinspre
ancestori spre copii; un nod ascuns nu este confundat cu o dovadă absentă.

```yaml
testChanges:
  - id: REPORT-AUDIT-INPUT-01
    status: VERIFIED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Verificarea schimbarilor existente pentru selectia inputului auditului UI.
    proof: mvn -pl atf-reporting-tests -am -Dtest=ReportAuditInputsTest,AtfTestLayerArchitectureTest,FailureEvidenceFlowTest,ReportNavigationAggregationFlowTest,StaticUiExtentReportContractTest,DeclarativeUiTestArchitectureTest,UiTestLayerArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/e2e/failure/FailureEvidenceFlowTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/e2e/navigation/ReportNavigationAggregationFlowTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/e2e/uireport/StaticUiExtentReportContractTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/audit/ReportAuditInputs.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/uireport/browser/UiReportContractFixture.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/audit/ReportAuditInputsTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/uireport/browser/UiReportContractInputFactory.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/locators/ReportScreenValidationLocators.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/section/extract/ScreenValidationSectionExtractor.java
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

## UI-CHECKPOINT-CONTEXT-01 — context intern fără tabel HTTP în pasul UI

Cerut direct: propoziția UI nu afișează `Status / Details / Info` și
`Request details · 2 fields` pentru `request.action` și `request.target`.
Contextul rămâne în model; request/response/execution pentru schimburile API
rămân publice. Probele acoperă ambele proiecții, metadata mixtă și browserul
headless cu un checkpoint UI care deține o validare API.

Controlul de autentificare verifică rândurile și valorile canonice live
`AUTH PROFILE=bearer` și `AUTH TYPE=bearer`, nu prezența accidentală a
unei etichete în scriptul inclus. `auth.mode` păstrează separat eticheta
`AUTHENTICATION`; refactorizarea paralelă a vocabularului nu este anulată.

Absența unui disclosure este verificată
în markup, nu prin interzicerea vocabularului legitim din asset-urile JavaScript.

Testul browser comun rămâne în scope-ul `REPORT-API-UNIFORMITY-01` din
[feedback-ul Reporting](ATF_Reporting_Feedback.md#scope-executabil--uniformitate-și-viewport);
comanda de probă îl execută și aici, fără să-i dubleze ownership-ul.

```yaml
testChanges:
  - id: UI-CHECKPOINT-CONTEXT-01
    status: VERIFIED
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
    status: VERIFIED
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
    status: VERIFIED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Guard-ul static trebuie sa verifice helper-ul live pentru toate panourile directe.
    proof: mvn -pl atf-reporting-tests -am -Dtest=ExtentReportInteractionContractTest,ExtentReportDisclosureBrowserComponentTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/session/ExtentReportInteractionContractTest.java
```

</details>

## Închidere

- Cererea rămâne `ACTIVE` în timpul verificării; `VERIFIED` păstrează scope-ul
  verificat până la integrare. `CLOSED` nu autorizează modificări noi.
- După integrare se poate elimina înregistrarea când baseline-ul verificării
  nu mai include schimbarea. Regulile stabile aparțin README-ului `atf-tests`.
- Transferurile închise nu ascund lucrări restante: criteriile funcționale
  API/UI și publicarea voluminoasă rămân la ownerul din
  [feedback-ul Reporting](ATF_Reporting_Feedback.md).
