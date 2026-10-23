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

**Stare: închisă ca analiză de ownership.** Inventarul și ownerii sunt
clarificați; nu există o clasă Shop întreagă care să poată fi mutată în
siguranță doar pe baza numelui sau a pachetului.
Owner-ul se decide după comportamentul verificat și dependențele testului, nu
după faptul că un fișier se află sub `api/reporting` sau conține aserțiuni HTML.
`atf.ui.tests.practiceshop` nu trebuie redus la teste care fac trafic extern:
consumerul își păstrează testele de componentă și E2E pentru contractele sale,
inclusiv atunci când transportul este simulat.

| Test / comportament | Owner corect | Limită de mutare |
|---|---|---|
| Request-uri live și contracte de business Practice Shop | `atf-test-practice-shop` | `ShopStandaloneApiReportTest` folosește `ShopApiClient.live`; journey-urile UI E2E rămân tot la consumer. Raportul rezultat este parte din dovada consumerului. |
| Teste Shop API cu `ShopApiComponentFixture` | `atf-test-practice-shop` | Transportul este determinist, nu un apel extern; testele verifică rute, payload-uri, sesiuni, statusuri și flow-uri Shop. Nu le mutați în `atf-mock` doar fiindcă folosesc un transport fake. |
| Redactare, schema de autentificare, checkpoint binding, metadata și proiecția Shop → raport | `atf-test-practice-shop` | Sunt contracte de securitate și mapping ale consumerului. Păstrați probe înguste cu date simulate; nu duplicați aici testarea generică a rendererului. |
| HTML/DOM/CSS/JavaScript, modelul și publisherul Reporting, policy generică de screenshot/evidence | `atf-reporting-tests` | Fixture-urile trebuie să fie neutre și să nu importe `atf.ui.practiceshop`, `ShopApiClient`, `ShopApiRoutes` sau configurația Shop. |
| Matching-ul și răspunsurile transportului mock | `atf-mock` | Modulul testează implementarea proprie; nu devine owner pentru request-urile sau contractele de business ale Shop. |
| Adnotări și primitive test-only fără UI/Reporting | `atf-test-kit` | Nu introduceți dependență de Reporting în Kit. `atf-reporting-tests` este owner de teste, nu o bibliotecă de fixtures pentru consumeri. |

### Inventarul live care schimbă interpretarea

- `ShopStandaloneApiReportTest` este testul de integrare care construiește un
  client live, execută flow-ul API de business și verifică proveniența `LIVE_HTTP`.
- Testele din `api/component/**` nu sunt, în general, apeluri către serviciul
  public: fixture-ul injectează un `ApiTransport` determinist și etichetează
  execuțiile `ACCEPTANCE_MOCK`. Rămân totuși teste Shop valide, deoarece probează
  contractele clientului și ale aplicației, nu rendererul ATF.
- `ShopApiCallAuditReportComponentTest` exercită raportarea unor exchange-uri
  sintetice prin publisherul și modelul de audit Shop. Se analizează test cu
  test: invarianta neutră de request/response/retry/HTML se mută sau se acoperă
  la Reporting; asocierea așteptărilor Shop cu exchange-uri și mapping-ul către
  statusuri/intenții de business rămân la consumer. Nu se mută clasa întreagă
  doar fiindcă produce un raport.
- `ShopApiContractReportComponentTest` rulează un screen-flow Shop prin
  transport determinist și verifică forma raportului rezultat; este probă de
  integrare a clientului și publisherului Shop, nu test autonom al rendererului.
- Testele de redactare, auth și body evidence verifică politica Shop de
  clasificare/mapping înainte de publicare. `atf-reporting-tests` verifică
  separat redactorul și rendererul generice; nu se mută testul Shop fără a
  separa mai întâi regula de date de adaptorul consumerului.
- `api/component/reporting/**` conține atât proiecții Reporting, cât și reguli
  Shop (redactare PII, mecanism de auth, statusuri și corpuri observate). Numele
  pachetului nu justifică relocarea automată.
- În `atf-test-practice-shop/src/test/resources` nu există imagini raster;
  captura browserului este dinamică. PNG-urile de un pixel sunt fixture-uri
  sintetice inline, iar `ReportTestScreenshots` din `atf-reporting-tests`
  deservește deja contractele rendererului. Nu mutați capturi sau fixture-uri
  raport-only în consumer; nici nu transformați modulul de teste Reporting într-o
  dependență de test reutilizată de Shop.

### Criteriu pentru mutarea fiecărei probe

1. Testul live sau journey-ul de business rămâne la consumer. Un test mock-backed
   rămâne tot la Shop dacă validează clientul, rutele, payload-ul sau politica
   aplicației; mock-ul descrie izolarea transportului, nu owner-ul comportamentului.
2. O probă poate fi mutată la Reporting numai dacă nu validează un contract Shop
   și intrările pot fi exprimate ca modele/fixture-uri neutre, fără
   `Shop*`, rute de aplicație, credențiale sau YAML consumer.
3. Invarianta este exprimată o singură dată la ownerul Reporting; în Shop rămâne
   numai proba că adaptorul transmite corect datele consumerului.
4. Testele Shop API cu transport mock nu se șterg pentru că nu sunt live:
   mock-ul izolează transportul, dar contractul testat rămâne al aplicației.
5. Mutarea nu face `atf-test-practice-shop` dependent de `atf-reporting-tests`;
   acesta din urmă este un modul de probe, nu un test-support artifact.

Regula stabilă a fost promovată în [README-ul ATF Reporting
Tests](../../atf-reporting-tests/README.md) și [README-ul Practice
Shop](../../atf-test-practice-shop/README.md): testele de renderer rămân neutre
în Reporting, iar contractele Shop — inclusiv cele mock-backed — rămân la
consumer. Redeschideți ID-ul numai dacă apare un invariant generic nominal care
lipsește ori este duplicat în Reporting; o clasă consumer nu se mută integral
doar fiindcă publică HTML.

## Cerințe deschise

| Prioritate | ID | Gap | Owner principal |
|---|---|---|---|
| P2 | `REPORT-START-ATOMICITY-01` | Crearea atomică sau finalizarea explicită a nodurilor dacă `startScenario`/`startPhase` eșuează după efecte parțiale, înainte de returnarea handle-ului | `atf-reporting` + `atf-reporting-tests` |
| P2 | `SHOP-CAPTURE-01` | Agregarea evidence-ului `SETUP` + `TEST BODY` în `UNIFIED_FLOW`, păstrând proveniența și lifecycle-ul | Reporting pentru contractul generic; Practice Shop pentru mapping-ul și lifecycle-ul consumerului |
| P3 | `UI-AUDIT-01` | Confirmare vizuală în TAB MCP a observației terminale în artifactele AutoEx și Shop; implementarea și probele sunt deja în cod | Verificare manuală a artifactelor; fără schimbare de renderer dacă randarea este conformă |

## REPORT-START-ATOMICITY-01 — crearea atomică a nodurilor de raport

**Stare: deschisă. Prioritate: 77/100** (impact 34 + lacună 21 + lucrabil
15 + datorie documentară 7).

### Fapt live

- `ExtentReportTree.startScenario` creează și pornește nodul Extent, apoi
  publică evenimentul Console și pornește starea semantică înainte să returneze
  `ExtentScenarioScope`.
- `ExtentScenarioScope.startPhase` publică evenimentul de start, creează nodul
  copil și pornește faza semantică înainte să returneze `ExtentPhaseScope`.
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

- Fault-injection după fiecare etapă de `startScenario` și `startPhase`, inclusiv
  Console, modelul Extent și lifecycle-ul semantic.
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
  suita Shop trece pe snapshot-ul curent. Aceste probe nu închid agregarea
  cross-phase și nu justifică relaxarea vreunui test de arhitectură.
- E2E-ul consumer confirmă integrarea reală; nu se complică journey-urile doar
  pentru a testa rendererul.
- Artifactul verifică o singură apariție a fiecărei acțiuni, ordinea, un singur
  owner Gherkin pentru flow-ul unificat, evidence API la checkpoint-ul corect,
  capturile conforme cu profilul efectiv și lipsa scurgerii de profil între frați.

## REPORT-TEST-PACKAGE-01 — închis ca backlog

Suportul UI comun a fost mutat în `atf.api.reporting.ui.support`; pachetul
principal rămâne în pragul de opt clase, iar suitele/selectoarele păstrează
execuția testelor la ownerul `atf-reporting-tests`. Contractul de coeziune este
promovat în README-ul modulului și pragul nu a fost relaxat.

În staging curat, `AtfReportingComponentSuite` a trecut. Acest rezultat închide
gap-ul local Reporting; nu afirmă că gate-ul transversal
whole-ATF este verde. Constatările transversale AutoEx rămân în feedback-ul E2E
și nu redeschid acest ID fără o regresie la pachetul Reporting.

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
