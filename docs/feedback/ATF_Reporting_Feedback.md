# ATF Reporting — cerințe rămase

Acest feedback păstrează gap-urile active din boundary-ul Reporting și integrarea
consumatorilor, plus decizia de coeziune închisă recent pentru trasabilitatea
reconcilierii. Contractele stabile sunt în
[contractul semantic](../ATF_Reporting_Contract.md),
[contractul de display și layout](../ATF_Reporting_Display_Contract.md) și
README-urile [ATF Reporting](../../atf-reporting/README.md) și
[ATF Reporting Tests](../../atf-reporting-tests/README.md).

## Ownership-ul testelor

| Comportament | Owner | Probă potrivită |
|---|---|---|
| Model, markup, renderer, publisher, CSS/JS și interacțiuni generice | `atf-reporting-tests` | fixture deterministă; browser numai când DOM-ul, stilul sau interacțiunea este criteriu |
| Hook JUnit/lifecycle al test-kit-ului | modulul care deține hook-ul | test JUnit Platform cu sink, sesiune și ownership capturate; Reporting verifică integrarea cu publicarea |
| Configurarea/mapping-ul unui consumer | modulul consumer | test component/integration restrâns la wiring și policy proprie |
| Journey de aplicație | modulul consumer | E2E-ul verifică integrarea, nu înlocuiește testele componentă ale bibliotecii |

Nu se mută mecanic testele Shop în `atf-reporting-tests`: redactarea
identității, binding-ul checkpoint-urilor și metadata YAML rămân în consumer.
Orice invariant generic identificat în Shop primește întâi o probă neutră la
ownerul Reporting; testul Shop păstrează numai integrarea sa.

## SHOP-REPORT-TEST-OWNERSHIP-01 — decizie de owner închisă

**Stare: închisă ca analiză de ownership.** Regula stabilă este promovată în
[README-ul ATF Reporting Tests](../../atf-reporting-tests/README.md#limitele-de-responsabilitate-ale-aplicațiilor-consumatoare)
și [README-ul Practice Shop](../../atf-test-practice-shop/README.md#ce-deține-modulul).
`atf-reporting-tests` deține fixture-urile neutre pentru model, publisher,
renderer și DOM; Practice Shop păstrează testele mock-backed sau live care
verifică propriile rute, payload-uri, redactare, checkpoint-uri și semantica
business. Mock-ul descrie izolarea transportului, nu owner-ul comportamentului,
iar un raport HTML nu schimbă owner-ul testului consumer.

Regula stabilă este descrisă în [README-ul ATF Reporting
Tests](../../atf-reporting-tests/README.md) și [README-ul Practice
Shop](../../atf-test-practice-shop/README.md): testele de renderer rămân neutre
în Reporting, iar contractele Shop — inclusiv cele mock-backed — rămân la
consumer.

- Redeschideți ID-ul numai dacă apare un invariant Reporting concret, duplicat
  ori lipsă în `atf-reporting-tests`.
- O clasă consumer nu se mută integral doar fiindcă publică HTML.

## Cerințe deschise

| Prioritate | ID | Gap | Owner principal |
|---|---|---|---|
| P1 | `REPORT-UI-ACTION-REDACTION-01` | Acțiunile UI pot publica valori de plată introduse în câmpuri sensibile | `atf-ui` pentru captură; `atf-reporting-tests` pentru contractul artifactului |
| P2 | `REPORT-START-ATOMICITY-01` | Finalizarea/reconcilierea nodurilor dacă lifecycle-ul semantic aruncă după mutația Extent și înainte de returnarea handle-ului; eșecurile sink-ului Console sunt deja izolate | `atf-reporting` + `atf-reporting-tests` |
| P2 | `SHOP-CAPTURE-01` | Agregarea evidence-ului `SETUP` + `TEST BODY` în `UNIFIED_FLOW`, păstrând proveniența și lifecycle-ul | Reporting pentru contractul generic; Practice Shop pentru mapping-ul și lifecycle-ul consumerului |
| P2 | `REPORT-CLIENT-METADATA-01` | `System/Environment → Client` poate declara `ATF ApiTransport` pentru un raport UI-only, deși execuția folosește Playwright | Reporting pentru fallback-ul neutru; consumerul pentru metadata de canal |
| P3 | `UI-AUDIT-01` | Confirmare vizuală în TAB MCP a observației terminale în artifactele AutoEx și Shop; implementarea și probele sunt deja în cod | Verificare manuală a artifactelor; fără schimbare de renderer dacă randarea este conformă |

## REPORT-UI-ACTION-REDACTION-01 — redactarea valorilor sensibile din acțiunile UI

**Stare:** deschis. **Prioritate:** 82/100 (impact 35 + lacună 24 + lucrabil
16 + datorie documentară 7).

Raportul UI publică valorile introduse în câmpurile de număr de card și cod de
securitate. Cauza este în `PlaywrightLocatorActions`: lista de selectori
sensibili maschează parola, token-ul și datele de identitate/contact, dar nu
recunoaște câmpurile de plată. `fill` inserează valoarea în ambele coloane ale
acțiunii (`expected` și `observed`), iar `SensitiveDataRedactor` din Reporting
nu poate reconstitui sigur sensibilitatea pierdută. Mascarea screenshot-ului
este o protecție separată și nu acoperă tabelul Playwright.

### Contractul de închidere

- PAN, CVC/CVV, parole, token-uri și alte valori declarate sensibile nu sunt
  păstrate în `AtfUiActionRecord` și nu apar în HTML, loguri sau replay evidence;
  mascarea se aplică înainte ca valoarea să intre în modelul de evidence.
- Sensibilitatea nu depinde numai de o listă fragilă de fragmente CSS. Câmpurile
  standard sunt recunoscute, iar consumatorul poate declara explicit un locator
  sensibil fără să schimbe verdictul ori acțiunea executată.
- Ambele proiecții — valoarea așteptată și cea observată — folosesc aceeași
  decizie. Valoarea originală nu se păstrează prin hash, prefix/sufix sau
  metadata auxiliară.
- Testele `atf-ui` demonstrează că valori sintetice unice nu supraviețuiesc în
  record și în șirurile de replay pentru selectorii card/CVC și pentru
  sensibilitatea explicită; valorile non-sensibile rămân utile pentru debug.
- Proba neutră de randare din `atf-reporting-tests` verifică absența valorilor
  sensibile în artifactul complet, nu doar în captură. Consumatorii păstrează
  numai testele de integrare ale mapping-ului lor.

Datele demo observate nu sunt publicate în acest feedback; constatarea privește
contractul general al evidence-ului UI și nu depinde de un card sau de o rulare
anume.

## REPORT-CLIENT-METADATA-01 — clientul afișat trebuie să corespundă canalului

**Stare:** deschis. **Prioritate:** 63/100 (impact 26 + lacună 17 + lucrabil
15 + datorie documentară 5).

`ReportEnvironment.defaults()` publică literal `Client = ATF ApiTransport
(configured per execution)`. Valoarea devine incorectă pentru un raport UI-only
când consumerul nu o suprascrie: `AutoExE2eRun` deschide o sesiune Playwright,
dar folosește `UiExtentReportSession.openConfigured(...)` fără metadata de
client. Practice Shop transmite explicit `Playwright browser`, deci același
raport nu are o regulă coerentă între consumeri.

### Contractul țintă

- Fallback-ul Reporting nu afirmă un transport sau un client specific, dacă
  composition root-ul nu l-a declarat.
- Consumerul publică un descriptor scurt și adevărat pentru canalul efectiv:
  browser/Playwright pentru UI, transportul configurat pentru API, respectiv
  o descriere combinată numai când ambele sunt folosite de execuție.
- `System/Environment` rămâne metadata despre mediul și mecanismul real de
  execuție; nu se adaugă acolo identitatea testului, pașii sau detaliile flow-ului.
- Probe componente verifică valoarea pentru UI-only, API-only și UI+API,
  inclusiv cazul fără metadata consumer, care nu trebuie să moștenească o
  afirmație falsă din fallback-ul generic.

Observația este despre corectitudinea semantică a metadatelor, nu despre
randarea tabelului. Remedierea aparține contractului de environment snapshot și
mapping-ului consumer; nu necesită un raport special sau logică bazată pe numele
clasei de test.

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

Profilul generic per scenariu este disponibil și testat în Reporting:
`UiExtentReportSession.startScenario(..., scenarioProfile)` izolează profilele
scenariilor-surori, inclusiv când ordinea inițializării se inversează.

AutoEx folosește deja această cale pentru varianta final-only. Practice Shop transmite
acum profilul rezolvat fiecărui scenariu din `ShopE2eContext` și îl folosește —
nu profilul implicit al sesiunii comune — la deciziile de captură.
`ShopE2eContextEvidenceProfileTest` verifică într-un raport partajat că override-
ul unui scenariu nu schimbă politica screenshot a scenariului-soră.

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
- **Trecut:** proba componentă Practice Shop publică doi scenarii-surori în
  aceeași sesiune cu politici diferite și verifică izolarea screenshot-ului.
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

## REPORT-TEST-PACKAGE-01 — închis ca backlog

- Helperul de UI este în `atf.api.reporting.ui.support`, iar pachetul principal
  respectă pragul de opt clase.
- Suitele/selectoarele păstrează testele la ownerul `atf-reporting-tests`; regula
  și structura sunt documentate în [README-ul modulului](../../atf-reporting-tests/README.md).
- `AtfPackageCohesionGovernanceTest` protejează coeziunea. ID-ul este închis;
  blocul `testChanges` rămas este numai control temporar de guvernanță.

## Aprobări temporare pentru modificări de teste

Blocul de mai jos este un control de aprobare `testChanges`, nu o listă de
TODO-uri funcționale. Păstrează-l până la expirarea/reconcilierea guvernanței;
nu modifică și nu autorizează modificarea testelor de guvernanță.

```yaml
testChanges:
  - id: REPORT-TEST-PACKAGE-01
    status: ACTIVE
    base: 4c97b18d6a8e7a17b979db40238f13ce20a481c9
    expires: 2026-10-30
    reason: Mutarea helperului UI comun în subpachetul support pentru a respecta limita de coeziune fără a schimba pragul sau testele executate.
    proof: mvn -pl atf-reporting-tests,atf-tests -am -Dtest=ExtentReportUiEvidencePublishingTest,ExtentReportUiEvidenceRenderingTest,ExtentReportUiFailureEvidenceTest,ExtentReportUiScreenshotContractTest,AtfPackageCohesionGovernanceTest -Dsurefire.failIfNoSpecifiedTests=false test
    allowDelete: true
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidencePublishingTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidenceRenderingTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidenceTestSupport.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiFailureEvidenceTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiScreenshotContractTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/support/ExtentReportUiEvidenceTestSupport.java
```

Control temporar pentru izolarea excepțiilor sink-ului Console față de
lifecycle-ul raportului:

```yaml
testChanges:
  - id: REPORT-CONSOLE-LIFECYCLE-SAFETY-01
    status: ACTIVE
    base: 84a8e9d92334d86f835d6dae3347170ef0e0e02e
    expires: 2026-10-30
    reason: Keep diagnostic Console sink failures from interrupting Extent scenario/phase lifecycle or artifact publication; prove the behavior with a deliberately throwing sink.
    proof: mvn -pl atf-reporting-tests -am -Dtest=ExtentReportSessionLifecycleTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/session/ExtentReportSessionLifecycleTest.java
```

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
