# ATF Reporting — cerințe rămase

Feedback-ul păstrează numai lacunele deschise și controalele temporare.
Contractele stabile aparțin [contractului semantic](../ATF_Reporting_Contract.md),
[contractului de display](../ATF_Reporting_Display_Contract.md) și README-urilor
[Reporting](../../atf-reporting/README.md) / [Reporting Tests](../../atf-reporting-tests/README.md).

## Ordinea intervențiilor

Scorul folosește formula registrului: impact /40 + lacună /30 + lucrabil /20 +
datorie documentară /10. Este prioritate de remediere, nu notă de calitate.

| ID | Scor | Motivul ordinii |
|---|---:|---|
| `REPORT-BROWSER-PLACEMENT-02` | 91 = 36 + 27 + 20 + 8 | noua probă Network cu Playwright încalcă ownerul UI component și face Test Kit roșu |
| `UI-FAILURE-CAPTURE-02` | 84 = 34 + 24 + 20 + 6 | motivul lipsei capturii dispare din evidence-ul de eșec |
| `REPORT-CLIENT-EXPECTATION-02` | 61 = 21 + 15 + 20 + 5 | testul HTML cere încă fallback-ul Client vechi, contrar contractului neutru |

## REPORT-BROWSER-PLACEMENT-02 — proba Network trebuie plasată la ownerul UI

**Stare: deschis, reprodus de Test Kit pe codul nou.**
`AtfTestLayerArchitectureTest` respinge importurile Playwright din
`atf.api.reporting.ui.network.NetworkCaptureEvidenceTest`, clasă componentă
aflată în afara pachetului UI permis.

- [ ] Mutați proba neutră la `atf.ui.tests.extent.component`, după modelul
      retry deja mutat; alternativ separați cazurile pure de cele browser.
      Păstrați toate cele cinci cazuri și adnotarea componentei.
- [ ] Păstrați testele generice în Reporting Tests, nu în Shop/AutoEx;
      nu extindeți allowlist-ul guvernanței pentru a ascunde placement-ul.
- [ ] Autorizați exact ștergerea/adăugarea prin `testChanges`, actualizați
      package/importuri/comenzi și comparați invocările înainte/după mutare.
- [ ] Rulați clasa, `AtfReportingComponentSuite` și toate testele Test Kit;
      trecerea componentei funcționale nu închide failure-ul de arhitectură.

## UI-FAILURE-CAPTURE-02 — lipsa capturii trebuie explicată în raport

**Stare: deschis, reprodus în suita unit Reporting.**
`UiAuditLifecycleTest.preservesActionsWhenFailureHasNoScreenshot` păstrează
acțiunile, cauza și verdictul FAIL, dar nu găsește explicația vizibilă
`screenshot disabled or unavailable`.

- `UiEvidencePublisher.publishFailure` pune motivul în `evidence.policy`;
  `EvidenceTableFormatter` filtrează acea cheie pentru `ui-failure`, în ambele
  căi de proiecție. Modelul conține informația, dar artifactul o pierde.
- [ ] Publicați explicit starea capturii în `Failure evidence`, fără a
      reintroduce `Audit details` sau metadate HTTP fictive la pasul UI.
- [ ] Păstrați aserțiunea comportamentală: acțiunile/cauza/FAIL rămân prezente,
      motivul lipsei capturii este vizibil și nu se inventează link Base64.
- [ ] Acoperiți cu fixture neutră Extent și fallback: captură permisă validă,
      captură indisponibilă și politică dezactivată; separați motivele fără
      duplicarea acțiunilor sau a propoziției Gherkin.
- [ ] Revedeți clasificarea probei: sesiune → publisher → HTML este boundary
      de componentă, deși clasa declară acum `@AtfUnitTest`. Cazurile trebuie
      executate și prin entry point-ul component, fără relaxarea guvernanței.

Comanda de reproducere este locală și nu cere aplicație externă:

```text
mvn -pl atf-reporting-tests -am "-Dtest=UiAuditLifecycleTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

## REPORT-CLIENT-EXPECTATION-02 — oracol HTML rămas în urmă

**Stare: deschis în test, nu regresie demonstrată a producției.**
`ExtentReportSessionContractTest.publishesTheSharedVisualContract` cere încă
`ATF ApiTransport (configured per execution)` într-o sesiune fără Client explicit.
Artifactul publică `Configured per execution`, conform contractului neutru și
`ReportEnvironmentSnapshotTest.genericClientFallbackDoesNotClaimAnApiTransportForUiOnlyRuns`.

- [ ] Aliniați proba HTML la contractul neutru exact și păstrați verificarea că
      un raport fără transport explicit nu pretinde `ATF ApiTransport`.
- [ ] Probați separat Client explicit furnizat de consumer → aceeași valoare
      în System, cu redactare; nu schimbați default-ul reutilizabil ca să
      satisfaceți vechea aserțiune.
- [ ] Revedeți layer-ul cazurilor care publică și citesc HTML: clasa este acum
      unit, astfel că suita componentă nu vede această divergență. Migrarea
      cere inventar înainte/după, control exact și păstrarea aserțiunilor de stil.

## REPORT-TIMESTAMP-01 — format coerent în Categories

**Stare: închis — format exclusiv 24h, componentă nativă și audit TAB MCP.**

- [x] Folosiți exclusiv format 24h (`HH:mm:ss`), fără `AM/PM`, în toate
      tab-urile; păstrați aceeași zonă temporală pentru același instant.
- [x] Probați la owner ore după prânz, miezul nopții și deep-link-ul din
      Categories, fără modificarea timestamp-ului semantic al evenimentului.

- `ExtentReportTimestampReporter` este adaptorul de prezentare Spark folosit
  de sesiunea canonică. Configurează timpul și data/ora în 24h; adaptează în
  memorie numai expresia Categories hardcodată de vendor ca `HH:mm:ss a`.
  Nu modifică JAR-ul, instanții, fusul orar, duratele sau payload-urile.
- `ReportingTimestampComponentTest` locuiește în `atf-reporting/src/test`, în
  pachetul standalone și cu tag JUnit `component`; nu importă Test Kit sau
  alt modul ATF. Probează `00`, `12`, `15`, `23`, navbar/Overview/Tests/log/Categories,
  conservarea instanților și duratei, plus integrarea sesiunii canonice.
- Gate-uri în staging extern: 14 teste native Reporting, inclusiv cele cinci
  cazuri noi, și 36 de regresii de compoziție trecute, fără teste sărite.
- TAB MCP prin ATF Debug: Categories afișează `15:05:07` și `00:05:07`;
  linkurile deschid testul original, Overview păstrează începutul/sfârșitul în
  24h, iar durata rămâne `2 s`. Zero console error/warn și zero overflow la 1280 px.

## Contracte închise, promovate în documentația proiectului

| Contract stabil | Owner canonic |
|---|---|
| Direcție/format body, JSON/form/text, absent/not-captured, MIME și escaping | [Dovezi body în Reporting](../../atf-reporting/README.md#dovezi-json-și-componentele-de-randare-ui) |
| Pornire atomică, rollback și cauză primară/suppressed | [Lifecycle Reporting](../../atf-reporting/README.md#pornirea-atomică-a-scenariilor-și-fazelor) |
| Profil per scenariu, izolare și flow unificat peste faze | [Contractul raportului](../../atf-reporting/README.md#contractul-raportului) și [mapping-ul Shop](../../atf-test-practice-shop/README.md#raport-partajat--starea-consumerului) |
| Audit terminal, separarea ACTION/OBSERVE și captură conform profilului | [Audit terminal](../../atf-reporting/README.md#audit-terminal-în-screen-validation) |

Multipart și whitespace sunt implementate la owner și au probe componente;
nu mai sunt TODO-uri. Regresiile de failure fără captură, oracolul Client și
formatul timestamp rămân distincte; o suită componentă verde nu le închide.

## Aprobări temporare pentru modificări de teste

Aceste controale sunt aprobări temporare pentru schimbările exacte de probe
enumerate mai jos; nu sunt TODO-uri funcționale și nu autorizează modificarea
testelor de guvernanță.

```yaml
testChanges:
  - id: REPORT-OPEN-GAPS-COMPONENT-03
    status: ACTIVE
    base: 5eb3fd3cb099fc67b49be3c17de564db63755db7
    expires: 2026-11-23
    reason: Păstrează toate cazurile Network la ownerul UI component, reclasifică publicarea HTML ca probă componentă și verifică motivul capturii și Client neutru sau explicit redactat. Ștergerea vechiului path Network este deja autorizată de UI-NETWORK-CAPTURE-PROOFS-01.
    proof: mvn -pl atf-reporting-tests,atf-test-kit -am "-Dtest=AtfReportingComponentSuite,UiAuditLifecycleTest,UiFailureCaptureComponentTest,ExtentReportSessionContractTest,NetworkCaptureEvidenceTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/inspection/network/NetworkCaptureEvidenceTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/UiAuditLifecycleTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/failure/UiFailureCaptureComponentTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/session/ExtentReportSessionContractTest.java
  - id: REPORT-NATIVE-CLOCK-LAYER-03
    status: ACTIVE
    base: 5eb3fd3cb099fc67b49be3c17de564db63755db7
    expires: 2026-11-23
    reason: Înregistrează exact proba nativă ReportingTimestampComponentTest în inventarul standalone finit, fără a permite alte importuri sau a extinde allowlist-ul browser.
    proof: mvn -pl atf-test-kit -am test
    paths:
      - atf-test-kit/src/test/java/atf/api/testkit/AtfTestLayerArchitectureTest.java
      - atf-reporting/src/test/java/atf/api/reporting/standalone/ReportingTimestampComponentTest.java
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
