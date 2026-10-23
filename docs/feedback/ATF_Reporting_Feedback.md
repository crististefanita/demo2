# ATF Reporting — cerințe rămase

Acest feedback păstrează numai gap-urile deschise din Reporting și integrarea
consumatorilor. Contractele stabile sunt în
[contractul semantic](../ATF_Reporting_Contract.md),
[contractul de display și layout](../ATF_Reporting_Display_Contract.md) și
README-urile [ATF Reporting](../../atf-reporting/README.md) și
[ATF Reporting Tests](../../atf-reporting-tests/README.md).

## Cerințe deschise

| Prioritate | ID | Gap | Owner principal |
|---|---|---|---|
| P2 | `REPORT-START-ATOMICITY-01` | Finalizarea/reconcilierea nodurilor dacă lifecycle-ul semantic aruncă după mutația Extent și înainte de returnarea handle-ului; eșecurile sink-ului Console sunt deja izolate | `atf-reporting` + `atf-reporting-tests` |
| P2 | `SHOP-CAPTURE-01` | Agregarea evidence-ului `SETUP` + `TEST BODY` în `UNIFIED_FLOW`, păstrând proveniența și lifecycle-ul | Reporting pentru contractul generic; Practice Shop pentru mapping-ul și lifecycle-ul consumerului |
| P3 | `UI-AUDIT-01` | Confirmare vizuală în TAB MCP a observației terminale în artifactele AutoEx și Shop; implementarea și probele sunt deja în cod | Verificare manuală a artifactelor; fără schimbare de renderer dacă randarea este conformă |

## REPORT-START-ATOMICITY-01 — crearea atomică a nodurilor de raport

**Stare: parțial închisă; gap-ul de atomicitate internă rămâne deschis. Prioritate: 77/100** (impact 34 + lacună 21 + lucrabil
15 + datorie documentară 7).

### Fapt live

- `ExtentReportTree.startScenario` creează nodul Extent înainte de a porni
  înregistrarea semantică și de a returna `ExtentScenarioScope`.
- `ExtentScenarioScope.startPhase` creează nodul copil înainte de a porni faza
  semantică și de a returna `ExtentPhaseScope`.
- Evenimentele sink-ului Console sunt acum best-effort: o excepție runtime la
  `SCENARIO`/`PHASE` start sau completion nu mai întrerupe lifecycle-ul. Regresia
  este probată de `ExtentReportSessionLifecycleTest` cu sink intenționat defect.
- Dacă o operație din acest interval aruncă, apelantul nu primește handle-ul
  necesar pentru a completa nodul parțial. Consumerul poate păstra setup failure
  ca eroare primară, dar nu poate repara singur modelul intern Reporting.
- `AutoExE2eInitialization` închide corect resursele și nodurile pentru care a
  primit handle; proba sa nu poate închide această fereastră internă a API-ului.

### Contractul de închidere

Inițializarea trebuie să fie atomică față de caller. Dacă un efect secundar
eșuează după publicarea parțială, Reporting fie retrage nodul în siguranță, fie
îl lasă într-o stare terminală `FAIL` cu diagnosticul cauzei; nu rămâne un nod
orfan `IN_PROGRESS` și raportul/snapshot-ul semantic rămân reconciliabile.
Eșecul inițial rămâne primar, iar erorile de rollback sunt păstrate ca
`suppressed`.

### Probe cerute în `atf-reporting-tests`

- Fault-injection după fiecare etapă de `startScenario` și `startPhase`, în
  special între mutația Extent și lifecycle-ul semantic; Console este deja
  probat separat ca sink diagnostic best-effort.
- Verificarea absenței nodurilor orfane sau a unei proiecții Extent/semantic
  contradictorii și confirmarea că sesiunea poate fi publicată după eșec.
- Verificarea păstrării cauzei inițiale și a tuturor eșecurilor secundare de
  rollback.
- Probe deterministe fără browser/site real; consumatorii verifică separat doar
  propagarea verdictului și păstrarea failure-ului de setup.

## SHOP-CAPTURE-01 — profil per scenariu și flow unificat peste faze

| Dovada | Stare live |
|---|---|
| Profil explicit și izolare generică între scenarii | `UiExtentReportSession.startScenario(..., scenarioProfile)` este probat în `ReportEvidenceProfileIntegrationTest`, inclusiv ordinea inversă. |
| Mapping Automation Exercise | Folosește profilul per scenariu pentru varianta final-only. |
| Mapping Practice Shop | `ShopE2eContext` transmite profilul rezolvat; `ShopE2eContextEvidenceProfileTest` verifică override-ul față de YAML, nu izolarea a doi frați. |

### Gap-ul confirmat în Practice Shop

- `ShopE2eContext.Scenario` publică `SETUP` și `TEST BODY` ca faze separate.
  `UNIFIED_FLOW` generic agregă în interiorul unei faze, nu peste această
  limită; setup-ul autentificat poate rămâne separat de secvența terminală.

### Contractul țintă

- **Implementat:** consumerul transmite profilul rezolvat fiecărui scenariu în
  raportul comun; configurația furnizează baza, iar `@AtfUiEvidence` suprascrie
  axele declarate. Politica de captură folosește același profil per context;
  `UI_ONLY`/`UI_API` nu schimbă presentation-ul.
- `UNIFIED_FLOW` publică acțiunile relevante din setup și body o singură dată,
  în ordine cronologică, într-o narațiune coerentă. Păstrează identitatea fazei,
  verdicturile, aserțiunile și relația cauzală a schimburilor API; nu dublează
  propoziția Gherkin și nu mută request-uri între checkpoint-uri.
- Politica de captură rămâne independentă de gruparea acțiunilor. O captură
  terminală nu se inventează dacă execuția eșuează înainte de starea finală.
- `Overview/System` rămâne metadata de raport, nu container pentru conținutul
  journey-ului.

### Probe și acceptanță

- `atf-reporting-tests` păstrează probe neutre pentru profile per scenariu,
  ordinea inversă și semantica `UNIFIED_FLOW`; aici se testează numai invariantul
  generic de lifecycle/rendering.
- **Acoperit generic:** `ReportEvidenceProfileIntegrationTest` publică profile
  diferite într-o sesiune comună și verifică izolarea; Shop verifică separat
  mapping-ul override-ului, nu repetă proba generică.
- **Rămas:** definește și testează agregarea între `SETUP` și `TEST BODY` fără
  Gherkin duplicat, fără pierderea fazei de proveniență și fără schimbarea
  verdictului/setup failure. Testele componente nu pornesc site-ul.
- `ShopE2eContextEvidenceProfileTest` verifică izolarea profilelor per scenariu;
  această probă nu închide agregarea cross-phase și nu justifică relaxarea
  vreunui test de arhitectură.
- E2E-ul consumer confirmă integrarea reală; nu se complică journey-urile doar
  pentru a testa rendererul.
- Artifactul verifică o singură apariție a fiecărei acțiuni, ordinea, un singur
  owner Gherkin pentru flow-ul unificat, evidence API la checkpoint-ul corect,
  capturile conforme cu profilul efectiv și lipsa scurgerii de profil între frați.

## UI-AUDIT-01 — audit terminal pentru Screen validation

**Stare:** implementarea și probele sunt închise; rămâne numai confirmarea
vizuală în TAB MCP. **Prioritate:** 60/100 (impact 24 + lacună 15 + lucrabil
16 + datorie documentară 5).

`Screen validation` grupează acțiunile, observațiile, aserțiunile și captura;
wiring-ul terminal și probele componente sunt implementate în Shop și AutoEx.
Rămâne doar verificarea vizuală interactivă a artifactelor prin TAB MCP; până
atunci nu declarăm randarea vizuală închisă.

### Contractul țintă

- Pentru o validare terminală care afirmă că starea UI finală respectă
  contractul, raportul publică o singură observație cu sens, de tip
  `Audit screen contract`, în disclosure-ul `Audit observations` al aceleiași
  validări. Nu se adaugă un nod Gherkin repetat și nu se creează un pas separat.
- Observația este evidence explicativă, nu o a doua acțiune Playwright și nu o
  aserțiune duplicată. Tabelul Playwright rămâne o singură secvență; tabelul de
  assertions rămâne sursa verdictelor; screenshot-ul rămâne captura terminală
  unică în modul final-only.
- Observația identifică obiectul verificat și rezultatul contractului, numărul
  aserțiunilor relevante, starea capturii și URL-ul sigur. Nu copiază body-uri,
  secrete sau payload-uri complete și nu declară `PASS` doar pentru că există o
  captură.
- În modelul generic se păstrează distincția `ACTION`/`OBSERVE`; adaptorul nu
  convertește observația în click sau altă acțiune. Înregistrarea are loc
  înaintea oricărui `drain()`/snapshot al secvenței, ori este transmisă ca
  evidence typed separat dacă aceasta este calea stabilă oferită de Reporting.
- Domeniul este dovada contractului terminal, nu cerința de a inventa observații
  pentru orice pas UI. Dacă un consumer nu capturează screenshot conform
  politicii efective, raportul trebuie să spună explicit asta și să nu pretindă
  că există evidence vizuală.

### Probe și criterii de închidere

- `atf-reporting-tests` construiește fixture neutrală cu acțiuni, exact o
  observație `OBSERVE`, assertions și screenshot și verifică DOM-ul randat:
  `Audit observations` este distinct, observația nu intră în tabelul de acțiuni,
  ordinea și cardinalitățile sunt stabile. Proba verifică acum și contractul
  exact `Audit screen contract`, starea PNG și URL-ul; fixture-ul include și
  absența observației pentru detectarea regresiei, fără un site real.
- Implementat în AutoEx: `finalScreenshot(...)` înregistrează o observație
  terminală înainte de drenare; proba de componentă verifică unicitatea,
  păstrarea ordinii, sursa `OBSERVE`, starea capturii și consumarea secvenței.
  Aserțiunile nu se dublează.
- Implementat în Shop: `pass(...)` folosește
  `drainScreenContractEvidence(...)`, care validează PNG-ul, înregistrează
  observația și abia apoi drenează secvența pentru publicare. Calea fără captură
  păstrează fallback-ul `THEN` fără pretenție de evidence vizuală. Proba de
  componentă validează lista exactă predată adaptorului, inclusiv ordinea
  `record -> drain`, și păstrează separat testele checkpoint-urilor.
- Testele componente ale rendererului și consumerilor sunt probele automate;
  această verificare nu le înlocuiește și nu cere schimbarea testelor E2E.
- În TAB MCP confirmă în artifactul randat că observația rămâne în același
  `Screen validation`, fără Gherkin duplicat, și că politica efectivă de captură
  este respectată. Nu deschide alt browser și nu modifica tabul utilizatorului.

## Aprobări temporare pentru modificări de teste

Aceste controale sunt aprobări temporare pentru schimbările exacte de probe
enumerate mai jos; nu sunt TODO-uri funcționale și nu autorizează modificarea
testelor de guvernanță.

```yaml
testChanges:
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
