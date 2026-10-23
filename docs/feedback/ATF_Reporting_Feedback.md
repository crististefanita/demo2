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
| P2 | `SHOP-CAPTURE-01` | Aplicarea profilului fiecărui scenariu în raportul partajat Shop și agregarea evidence-ului `SETUP` + `TEST BODY` în `UNIFIED_FLOW` | Reporting pentru contractul generic; Practice Shop pentru mapping-ul și lifecycle-ul consumerului |
| P2 | `REPORT-TEST-PACKAGE-01` | Pachetul `atf.api.reporting.ui` din `atf-reporting-tests` depășește pragul de opt clase și face gate-ul de coeziune roșu | `atf-reporting-tests`; guvernanța whole-ATF rămâne neschimbată |
| P2 | `UI-AUDIT-01` | Dovada de audit a contractului terminal UI lipsește în AutoEx și se pierde în calea terminală Shop înainte de publicare | `atf-reporting-tests` pentru separarea/randarea generică `OBSERVE`; consumerii pentru înregistrare și lifecycle |

## REPORT-PATH-01 — închis: cale absolută după publicarea raportului

Reporting expune `reportPath()` și finalizează/publică artifactul la `close()`.
Notificarea se leagă de evenimentul terminal `REPORT_PUBLISHED`, nu de path-ul
disponibil înainte de `close()`, care singur nu dovedește că fișierul este gata.

### Contract implementat

`ExtentReportSession.close()` emite `REPORT_PUBLISHED` numai după flush,
publicarea artifactului și confirmarea fișierului final. Evenimentul poartă
calea absolută normalizată; închiderea repetată nu dublează emiterea, publicarea
eșuată nu anunță succes, iar excepția unui sink diagnostic nu schimbă verdictul.
- `@AtfReportPath` din TestKit oferă politici `ENABLED`/`DISABLED`; resolverul
  respectă declarația directă, meta-adnotările și override-ul celei mai apropiate
  clase, iar conflictele la același nivel eșuează înainte de setup.
- `AtfConsoleTestExtension` aplică separat politica pentru cale; mesajul depinde
  de `reporting.console.enabled`, nu de politica detaliilor `@AtfConsoleMessages`.
  Absența adnotării păstrează output-ul de cale dezactivat.
- Raportul partajat Shop agregă opt-in-ul tuturor claselor din root cu
  `AtfReportPathOptIn`; clasa care deschide prima sesiunea nu decide singură
  dacă artifactul va fi anunțat. Testele probează ordinea opt-in/opt-out inversată.
- API resource tests, AutoEx E2E și harness-ul Shop declară opt-in prin
  adnotare; nu adaugă `println`, `@AfterAll` manual sau a doua închidere.
- `ExtentReportSession` emite `REPORT_PUBLISHED` după publicare și confirmarea
  existenței fișierului final. Close repetat nu dublează evenimentul, eșecul de
  publicare nu anunță succes, iar erorile sink-ului nu schimbă verdictul.
- Rularea reală `UsersApiTests` a produs o singură linie `[ATF] Extent report:`
  după output-ul lifecycle și după publicarea fișierului; 4 teste au trecut.

**Contractul este închis pentru raport per clasă și pentru raportul partajat
Shop.** Alte sesiuni partajate compun explicit `AtfReportPathOptIn`; agregatorul
este reutilizabil în `atf-console`.

## SHOP-CAPTURE-01 — profil per scenariu și flow unificat peste faze

Profilul generic per scenariu este disponibil și testat în Reporting:
`UiExtentReportSession.startScenario(..., scenarioProfile)` izolează profilele
scenariilor-surori, inclusiv când ordinea inițializării se inversează. AutoEx
folosește deja această cale pentru varianta final-only; acest comportament nu
mai este TODO.

### Gap-ul confirmat în Practice Shop

- În `ShopE2eTestSupport.E2eExtension`, fiecare clasă rezolvă un profil, dar
  `SuiteReportResource.open(...)` creează raportul partajat numai la primul
  apel. Profilele claselor următoare nu sunt aplicate automat scenariilor lor.
- `ShopE2eContext.startReportScenario(...)` apelează `report.startScenario(...)`
  fără `scenarioProfile`; `ShopE2eContext` folosește și profilul sesiunii la
  decizia de captură. În `ShopE2eSuite`, configurația de presentation a unui
  scenariu poate deveni dependentă de clasa care a deschis prima raportul.
- `ShopE2eContext.Scenario` publică `SETUP` și `TEST BODY` ca faze separate.
  `UNIFIED_FLOW` generic agregă în interiorul unei faze, nu peste această
  limită; setup-ul autentificat poate rămâne separat de secvența terminală.

### Contractul țintă

- Consumerul transmite profilul rezolvat fiecărui scenariu în raportul comun;
  profilele sunt izolate, iar rezultatul nu depinde de ordinea claselor din
  suită. YAML-ul rămâne autoritatea pentru politica de screenshot, iar
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
- Practice Shop adaugă probe de mapping/wiring pentru scenarii cu profile
  diferite în același raport, `SETUP` + `TEST BODY`, UI-only/UI_API, screenshot
  policy și failure înainte de final. Testele componente nu pornesc site-ul.
- E2E-ul consumer confirmă integrarea reală; nu se complică journey-urile doar
  pentru a testa rendererul.
- Artifactul verifică o singură apariție a fiecărei acțiuni, ordinea, un singur
  owner Gherkin pentru flow-ul unificat, evidence API la checkpoint-ul corect,
  capturile conforme cu YAML și lipsa scurgerii de profil între frați.

## REPORT-TEST-PACKAGE-01 — coeziunea pachetelor din Reporting Tests

În source set-ul curent, pachetul de teste `atf.api.reporting.ui` conține nouă
clase și depășește pragul normativ de opt. `AtfPackageCohesionGovernanceTest`
verifică separat limita Reporting și o include în controlul pachetelor de test
din reactor; discrepanța este structurală și reproductibilă din surse, nu o
constatare dependentă de un raport istoric. Schimbările recente de captură sau
publicare nu reduc acest inventar.

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

Contractul de display cere ca `Screen validation` să grupeze acțiunile,
observațiile de audit, aserțiunile și captura relevantă. Artifactul AutoEx
`FINAL_SCREENSHOT_ONLY` are validarea terminală, secvența Playwright și captura,
dar nu publică un `Audit observations`; codul din `AutoExE2eRun.finalScreenshot`
apelează `screenValidation(...)` cu acțiunile drenate, fără să înregistreze o
observație terminală de contract.

Stabilizarea capturii Playwright la navigare (așteptarea stării `LOAD` și
reîncercarea limitată a screenshot-ului) nu creează această observație și nu
închide cerința de audit; sunt contracte diferite.

Există și o cale Shop care încearcă să emită aceeași dovadă, dar o adaugă prea
târziu: `ShopE2eContext.Scenario.pass(...)` extrage `actions.drain()` în
`actionEvidence`, apoi apelează `recordScreenContract(...)`. Observația
`OBSERVE` adăugată după drenare nu se află în lista deja transmisă către
`body.screenValidation(...)`; raportul nu o poate reda. Calea de checkpoint
înregistrează observația înainte de drenare și nu trebuie regresată.

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
  ordinea și cardinalitățile sunt stabile. Fixture-ul include și lipsa
  observației, ca testul să detecteze regresia fără a folosi un site real.
- Testele componente AutoEx verifică că `FINAL_SCREENSHOT_ONLY` trimite exact o
  observație terminală înainte de drenare, o singură secvență Playwright și o
  captură terminală; aserțiunile existente nu se dublează. Testele componentă
  Shop verifică ordinea `record -> drain -> publish` pe calea `pass(...)` și
  păstrează separat probele checkpoint-urilor.
- Contract test-ul raportului fresh confirmă toate zonele în același
  `Screen validation`, fără Gherkin duplicat, fără observație pierdută și fără
  a transforma auditul într-o acțiune browser.
- E2E-ul verifică wiring-ul real după testele de componentă; nu este folosit ca
  unic test al cardinalității sau al mapping-ului. Criteriul este o observație
  terminală vizibilă, exact o secvență Playwright și exact captura cerută de
  profilul efectiv.
