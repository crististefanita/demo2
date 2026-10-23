# ATF Reporting — cerințe rămase

Acest feedback păstrează numai gap-urile active din boundary-ul Reporting și
din integrarea consumatorilor. Contractele stabile sunt în
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

## Cerințe deschise

| Prioritate | ID | Gap | Owner principal |
|---|---|---|---|
| P2 | `SHOP-CAPTURE-01` | Agregarea evidence-ului `SETUP` + `TEST BODY` în `UNIFIED_FLOW`, păstrând proveniența și lifecycle-ul | Reporting pentru contractul generic; Practice Shop pentru mapping-ul și lifecycle-ul consumerului |
| P2 | `REPORT-TEST-PACKAGE-01` | Helperul comun a fost separat în `ui.support`; testul de pachet trece, dar whole-ATF rămâne blocat de probleme preexistente în snapshot | `atf-reporting-tests`; fără excepții sau schimbări de prag |
| P3 | `UI-AUDIT-01` | Confirmare vizuală în TAB MCP a observației terminale în artifactele AutoEx și Shop; implementarea și probele sunt deja în cod | Verificare manuală a artifactelor; fără schimbare de renderer dacă randarea este conformă |

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
- Validarea țintită a wiring-ului Shop trece 15/15 teste; contractele generice
  Reporting UI selectate trec 39/39. `ShopComponentSuite` rămâne roșie pe o
  constatare separată, `ShopArchitectureTest` despre `ShopConsoleBridge`; aceasta
  nu este relaxată și nu este inclusă în schimbarea `SHOP-CAPTURE-01`.
- E2E-ul consumer confirmă integrarea reală; nu se complică journey-urile doar
  pentru a testa rendererul.
- Artifactul verifică o singură apariție a fiecărei acțiuni, ordinea, un singur
  owner Gherkin pentru flow-ul unificat, evidence API la checkpoint-ul corect,
  capturile conforme cu profilul efectiv și lipsa scurgerii de profil între frați.

## REPORT-TEST-PACKAGE-01 — coeziunea pachetelor din Reporting Tests

Pachetul testelor de renderer respectă acum pragul fără a muta owner-ul din
`atf-reporting-tests`; blocajul rămas este gate-ul whole-ATF, care raportează
și modificări concurente din alte zone.

- `ExtentReportUiEvidenceTestSupport` este în `atf.api.reporting.ui.support`;
  pachetul principal are opt declarații, iar suportul comun are una.
- Importurile/selectoarele rămân în aceeași suită; helperul nu este test și nu
  adaugă o rulare duplicată.
- `AtfPackageCohesionGovernanceTest` trece 5/5, iar cele patru clase Reporting
  afectate trec 36/36 teste.
- `WholeAtfGovernanceSuiteTest` a rulat, dar nu trece încă: lipsesc controale
  `testChanges` pentru alte modificări live, există încălcări de lizibilitate în
  surse din alte module și feedback-ul curent avea o încălcare de densitate.

Controlul temporar acoperă exact mutarea și importurile pentru această cerință;
nu autorizează și nu justifică editări concurente din alte module.

```yaml
testChanges:
  - id: SHOP-CAPTURE-01
    status: ACTIVE
    base: 4c97b18d6a8e7a17b979db40238f13ce20a481c9
    expires: 2026-10-30
    reason: Transmiterea profilului efectiv fiecărui scenariu Practice Shop din raportul partajat, cu probă de izolare între scenarii-surori; agregarea între faze rămâne separată și deschisă.
    proof: mvn -pl atf-test-practice-shop -am -Dtest=ShopE2eContextEvidenceProfileTest,ShopE2eTestSupportTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-test-practice-shop/src/main/java/atf/ui/practiceshop/support/ShopE2eContext.java
      - atf-test-practice-shop/src/test/java/atf/ui/practiceshop/support/ShopE2eContextEvidenceProfileTest.java
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

### Cerință și acceptanță

- Regrupează clasele după responsabilitatea reală și motivele de schimbare
  (de exemplu rendering, publication/failure, lifecycle/composition), nu după
  nume mecanic sau numai pentru a face numărul să treacă.
- Păstrează fiecare package coerent și în limita de opt clase. Nu ridica pragul,
  nu adăuga excepție și nu muta teste în consumeri doar ca să maschezi
  hotspot-ul; o excepție ar necesita justificare explicită, dar nu este soluția
  implicită aici.
- Actualizează importurile, suitele/selectoarele și orice arhitectură care
  folosește package-urile, astfel încât toate testele relevante să ruleze exact
  o dată și owner-ul să rămână `atf-reporting-tests`.
- Rulează `AtfPackageCohesionGovernanceTest` și gate-ul whole-ATF. Închiderea
  cere ca ambele probe să treacă fără relaxarea regulii și ca testele Reporting
  rămase în aceeași responsabilitate să fie încă selectate.

## UI-AUDIT-01 — audit terminal pentru Screen validation

**Stare:** implementarea și probele sunt închise; rămâne numai confirmarea
vizuală în TAB MCP. **Prioritate:** 60/100 (impact 24 + lacună 15 + lucrabil
16 + datorie documentară 5).

`Screen validation` grupează acțiunile, observațiile, aserțiunile și captura;
wiring-ul terminal este implementat în Shop și AutoEx, iar verificarea vizuală
în TAB MCP rămâne deschisă.

- AutoEx înregistrează observația înainte de `drain()`; Shop folosește
  `drainScreenContractEvidence(...)`, care validează PNG-ul, înregistrează
  observația și apoi preia secvența.
- Testele componente Reporting, AutoEx și Shop verifică forma `OBSERVE`,
  cardinalitatea, ordinea și lista transmisă rendererului.
- Două E2E-uri proaspete au produs `Audit screen contract` în `Screen
  validation`; pentru politica fără captură, Shop folosește fallback `THEN`.
- MCP Playwright a refuzat inventarul și snapshot-ul deoarece profilul browserului
  era ocupat; nu s-a deschis alt browser și nu s-a închis tabul utilizatorului.

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
- Testele componentă de renderer, AutoEx și Shop au trecut. Au trecut și două
  E2E-uri proaspete: AutoEx `FINAL_ONLY` (1/1) și Shop UI-only single-flow
  (1/1); ambele au produs raport cu `Screen validation` și observația terminală
  `Audit screen contract` în HTML.
- Verificarea vizuală interactivă în TAB MCP rămâne deschisă: sesiunea MCP a
  refuzat inventarul și snapshot-ul deoarece profilul browserului era deja
  ocupat. Nu s-a deschis alt browser și nu s-a închis tabul utilizatorului.
  Confirmă în artifactele existente că observația este în același `Screen
  validation`, fără Gherkin duplicat și cu captura cerută de profil.
