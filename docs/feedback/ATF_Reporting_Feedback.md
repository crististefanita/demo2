# ATF Reporting — cerințe rămase

Acest feedback urmărește gap-ul de format pentru body-uri API și păstrează
dovezile de închidere și controalele temporare de teste. Contractele stabile sunt în
[contractul semantic](../ATF_Reporting_Contract.md),
[contractul de display și layout](../ATF_Reporting_Display_Contract.md) și
README-urile [ATF Reporting](../../atf-reporting/README.md) și
[ATF Reporting Tests](../../atf-reporting-tests/README.md).

## API-BODY-MEDIA-01 — etichete corecte pentru body-uri externe non-JSON

**Stare: deschis, observat în raportul API live AutoEx prin MCP.**
`ApiExchangeEvidence.body(...)` etichetează payload-ul real
`application/x-www-form-urlencoded` ca `REQUEST JSON`. Body-ul și redacția
sunt păstrate, însă eticheta sugerează un format pe care request-ul nu îl are.

### Criterii de acceptare

- [ ] Separați direcția request/response de formatul body-ului în proiecția
      evidence; gruparea nu trebuie să depindă de prefixul textului etichetei.
- [ ] Păstrați JSON pentru documentele JSON și folosiți o etichetă neutră ori
      specifică pentru form/text. MIME-ul upstream rămâne neschimbat, inclusiv
      când serviciul trimite JSON declarat `text/html`.
- [ ] Probați component standalone și UI+API pentru JSON, form, text, absent și
      not-captured, păstrând escaping-ul, redacția și disclosure-urile inline.
- [ ] Verificați în MCP consumerul real. Nu convertiți artificial request-urile
      site-ului în JSON și nu duplicați publisherul în AutoEx pentru a masca
      limita infrastructurii comune.

## DOC-REPORT-SYNC-01 — referințe README rămase în urmă

**Stare: deschis, drift documentar confirmat; modificările rămân în feedback
conform regulii curente de lucru.**

- [Practice Shop — profil și faze](../../atf-test-practice-shop/README.md#configurația-modulului):
  paragraful imediat anterior încă numește auditul vizual `SHOP-CAPTURE-01`
  drept restant, deși verificarea prin ATF Debug/TAB MCP este consemnată mai jos.
- [Practice Shop — boundary API](../../atf-test-practice-shop/README.md#boundary-ul-probelor-api):
  matricele pentru `ShopStandaloneApiReportTest` încă declară ultima rulare
  blocată la 401. Feedback-ul Shop are deja dovada HTTP autentificată verde;
  aceasta nu trebuie confundată cu validitatea unui login UI ulterior.
- [Reporting Tests](../../atf-reporting-tests/README.md) folosește ancora
  `#shop-capture-01`, care nu corespunde heading-ului complet din acest document.
  Referința corectă este
  [profil per scenariu și flow peste faze](#shop-capture-01--profil-per-scenariu-și-flow-unificat-peste-faze).
- La promovarea contractului `API-BODY-MEDIA-01`, ownerii README Reporting și
  Reporting Tests trebuie să descrie canalul body tipizat și compatibilitatea
  portului JSON istoric; textul despre JSON nu trebuie extins implicit la form/text.

## Stare curentă

| Stare | ID | Dovadă | Owner principal |
|---|---|---|---|
| Închis | `SHOP-CAPTURE-01` | Flow autentificat cu setup de înregistrare, login și achiziție trecut; raport fresh verificat prin ATF Debug în TAB MCP, inclusiv ownership API corectat | Reporting + Practice Shop |
| Închis | `UI-AUDIT-01` | Rapoarte Shop și AutoEx verificate interactiv: observație terminală, assertions, captură Base64 deschisă; fallback fără captură probat separat | Reporting + consumatori |

## REPORT-START-ATOMICITY-01 — închis: crearea atomică a nodurilor de raport

**Stare: închis pentru punctele injectabile din lifecycle-ul curent.**

### Fapt live

- `ExtentReportTree.startScenario` normalizează acum tag-urile înainte de
  crearea oricărui nod Extent sau semantic. O colecție caller-owned care aruncă
  la iterare nu mai lasă rădăcină/clasă/scenariu parțial.
- `UiExtentReportSession.startScenario(..., scenarioProfile)` validează acum
  profilul nenul înainte să delege pornirea scenariului; argumentul invalid nu
  mai produce un nod Extent fără handle UI.
- Regresii în `ExtentReportSessionLifecycleTest` și
  `ReportEvidenceProfileIntegrationTest` verifică snapshot-ul/HTML-ul după
  aceste două intrări invalide și faptul că următorul scenariu valid se publică.
- Dacă pornirea semantică eșuează după crearea nodului Extent, scenariul/faza
  este retrasă din arborele Extent. O rădăcină de clasă nouă rămasă goală este
  retrasă și din raport; cauza inițială rămâne primară, iar eroarea de
  compensare devine `suppressed`.
- Un probe intern, inaccesibil consumatorului, injectează eșecul între
  mutația Extent și pornirea semantică pentru scenariu și fază.
- Evenimentele sink-ului Console sunt acum best-effort: o excepție runtime la
  `SCENARIO`/`PHASE` start sau completion nu mai întrerupe lifecycle-ul. Regresia
  este probată de `ExtentReportSessionLifecycleTest` cu sink intenționat defect.
- Dacă o operație din acest interval aruncă, apelantul nu primește handle-ul
  necesar pentru a completa nodul parțial. Consumerul poate păstra setup failure
  ca eroare primară, dar nu poate repara singur modelul intern Reporting.
- `TestRunInitialization` din Test Kit închide resursele și nodurile pentru care a
  primit handle; proba sa nu poate închide această fereastră internă a API-ului.

### Contractul verificat

Inițializarea trebuie să fie atomică față de caller. Dacă un efect secundar
eșuează după publicarea parțială, Reporting fie retrage nodul în siguranță, fie
îl lasă într-o stare terminală `FAIL` cu diagnosticul cauzei; nu rămâne un nod
orfan `IN_PROGRESS` și raportul/snapshot-ul semantic rămân reconciliabile.
Eșecul inițial rămâne primar, iar erorile de rollback sunt păstrate ca
`suppressed`.

### Probe în `atf-reporting-tests`

- Tag-uri invalide înainte de mutație, profil UI nul înainte de delegare,
  eșec semantic injectat după crearea scenariului și fazei, plus compensare
  secundară defectă. Probele verifică snapshot-ul, absența nodului orfan în
  HTML, cauza primară/suppressed și reutilizarea sesiunii.
- Console rămâne probat separat ca sink diagnostic best-effort. Testele sunt
  deterministe și nu pornesc browser ori site real.

## SHOP-CAPTURE-01 — profil per scenariu și flow unificat peste faze

**Stare: închis după rularea și verificarea interactivă a artifactelor fresh.**

| Dovada | Stare live |
|---|---|
| Profil explicit și izolare generică între scenarii | `UiExtentReportSession.startScenario(..., scenarioProfile)` este probat în `ReportEvidenceProfileIntegrationTest`, inclusiv ordinea inversă. |
| Mapping Automation Exercise | Folosește profilul per scenariu pentru varianta final-only. |
| Mapping Practice Shop | `ShopE2eContext` transmite profilul rezolvat; `ShopE2eContextEvidenceProfileTest` verifică override-ul față de YAML, nu izolarea a doi frați. |

### Stare implementată

- `UiScenario.unifyAcrossPhases()` activează bufferul de scenariu numai pentru
  `UNIFIED_FLOW`; `UiPhase.completeFinal(...)` publică o singură propoziție
  Gherkin cu checkpoints în ordine. Fiecare checkpoint păstrează faza,
  verdictul fazei și al pasului, aserțiunile și API validation imediat după
  evidence-ul UI proprietar. Modul obișnuit per fază rămâne nemodificat.
- Shop activează opțiunea din profilul efectiv. `finishSetup(FAIL)` finalizează
  flow-ul în `SETUP`; `TEST BODY` nu este creat și captura finală nu este
  inventată. `pass`, `complete` și `fail` închid faza terminală explicit.
- Probele în staging: `ShopComponentSuite` — 200 teste verzi;
  `ReportEvidenceProfileIntegrationTest` +
  `ExtentUnifiedFlowMultipleScreenshotTest` — 9 teste verzi;
  `ShopUiOnlySingleFlowPurchaseE2eTest` — 1 test live trecut, raport nou în
  staging. Testele noi acoperă setup + body, ownership API, verdictul unui
  setup eșuat și absența `TEST BODY`/capturii terminale după eșec.
- Auditul browser a descoperit repetarea înregistrării la checkpoint-ul de
  login. `ShopAuthenticatedAccountTestSupport` publică acum liste locale
  fiecărui checkpoint, fără acumularea request-ului sau aserțiunilor anterioare.
  Rerularea autentificată a trecut; TAB MCP confirmă exact un nod de schimb
  pentru `POST /users/register` și unul pentru `POST /users/login`.

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
- **Confirmat în TAB MCP:** setup explicit autentificat, ordinea setup/body,
  un singur owner Gherkin, API validation după checkpoint-ul proprietar și o
  singură captură finală. Fixture-ul diagnostic din staging reutilizează
  journey-ul existent cu adnotări explicite; nu a fost promovat în testele live.
- Calea negativă cu fixture reutilizat a expirat la `h1`: raportul a păstrat
  `SETUP` eșuat fără `TEST BODY`; cauza și deep-link-ul Fails au fost verificate.
  Această probă nu declară fixture-ul reutilizat drept cont UI valid.
- E2E-ul consumer confirmă integrarea reală; nu se complică journey-urile doar
  pentru a testa rendererul.
- Artifactul verifică o singură apariție a fiecărei acțiuni, ordinea, un singur
  owner Gherkin pentru flow-ul unificat, evidence API la checkpoint-ul corect,
  capturile conforme cu profilul efectiv și lipsa scurgerii de profil între frați.

## UI-AUDIT-01 — audit terminal pentru Screen validation

**Stare: închis — cod, probe și verificare vizuală prin ATF Debug/TAB MCP.**

`Screen validation` grupează acțiunile, observațiile, aserțiunile și captura;
wiring-ul terminal și probele componente sunt implementate în Shop și AutoEx.
Verificarea interactivă a fost făcută pe artifactele fresh generate în staging.

| Aspect | Stare și dovadă cerută |
|---|---|
| Probe automate | Verifică structura evidence-ului; succesul lor nu certifică randarea vizuală. |
| Artifact inspectat | Shop UI-only, Shop autentificat unificat și AutoEx final-only, deschise prin ATF Debug pe loopback în TAB MCP. |
| Închidere | Click pe observații/assertions și deschidere efectivă a capturilor PNG; la 1280 px pagina nu are overflow orizontal, zero `<hr>` decorative și zero mesaje console error/warn. |

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
- Reauditul a găsit și reparat ramura terminală Shop fără captură: publica
  acțiunile fără observația de audit. `terminalContractEvidence(...)` păstrează
  acum observația și mesajul explicit `screenshot=not captured` înainte de
  drenare. `ShopTerminalEvidenceComponentTest` verifică ordinea, unicitatea,
  drenarea și faptul că un payload invalid nu este declarat PNG valid.
- Verificare în staging extern: 34 de teste Reporting (lifecycle, profile,
  flow unificat și randare HTML), 3 probe AutoEx pentru profil/audit și suita
  Shop cu 200 de teste au trecut. Aceste probe nu închid gate-ul vizual.
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
