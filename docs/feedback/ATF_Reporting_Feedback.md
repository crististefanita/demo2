# ATF Reporting — cerințe rămase

Acest feedback păstrează doar lacunele Reporting care nu sunt încă demonstrate
de cod și teste. Contractele stabile sunt în [contractul semantic](../ATF_Reporting_Contract.md),
[contractul de display și layout](../ATF_Reporting_Display_Contract.md) și
README-urile [ATF Reporting](../../atf-reporting/README.md) și
[ATF Reporting Tests](../../atf-reporting-tests/README.md). Nu păstrăm aici
istoricul remedierilor, capturi sau rezultate de run deja promovate.

## Ownership-ul testelor — regulă pentru toate cerințele de mai jos

| Tipul comportamentului testat | Modulul care deține testul | Forma preferată a probei |
|---|---|---|
| Model, markup, renderer, publisher, fixture API neutră, raport HTML/CSS/JS și comportament browser generic | `atf-reporting-tests` | Fixture deterministă/mocked, artifact generat local și browser probe numai când DOM-ul/CSS-ul ori interacțiunea sunt criteriul; fără endpoint live sau reguli de business Shop |
| Hook JUnit sau lifecycle reutilizabil al test-kit-ului | Modulul owner al hook-ului, în special `atf-test-kit` | Extensie JUnit cu sink și sesiune capturate; în `atf-reporting-tests` rămâne proba integrării hook-ului cu publicarea Reporting |
| Maparea unui consumer către contractul Reporting | Modulul consumerului | Test component/integration restrâns la wiring, configurare și ownership; nu se copiază aici testele generice ale rendererului |
| Flux end-to-end al unei aplicații | Modulul consumerului | Verifică journey-ul, nu substituie testele de componentă ale bibliotecii |

Auditul codului Shop nu justifică mutarea mecanică a testelor specifice.

- `ShopE2eFailureReportComponentTest` verifică adaptorul concret
  `ShopE2eContext → Reporting`; contractele generice pentru failure, redactare
  și randare sunt deja probate în `atf-reporting-tests`.
- Binding-ul checkpoint-urilor, redactarea identității Shop și metadata YAML
  rămân în Shop: depind de configurația și semantica acelui consumer.
- Dacă un test Shop verifică un invariant generic, mută întâi proba neutră în
  `atf-reporting-tests`; păstrează în Shop doar verificarea integrării proprii.

## Reconciliere live — 2026-10-23

- `UiApiVerdictProjectionComponentTest` adaugă proba browser pentru ambele
  direcții independente: UI PASS/API FAIL și UI FAIL/API PASS. Verifică statusul
  scenariului, cauza corectă în `Fails` și `Open in Tests` către scenariul owner.
  Rulează țintit împreună cu `ApiReportRenderedEvidenceComponentTest`: **10/10**.
- `ApiReportRenderedEvidenceComponentTest` verifică în browser ownership-ul
  Request/Response/Execution, izolarea ownerilor și collapse/reopen vizibil: **9/9**.
- `AtfReportingComponentSuite` a trecut într-un staging nou, sincronizat din
  sursele live: **137 teste, 0 failures, 0 errors, 0 skipped**. Rularea a
  compilat reactorul în staging și a inclus probele browser pentru ownership,
  collapse/reopen, verdicte UI/API și viewport. Eșecul agregat anterior nu s-a
  reprodus în această rulare izolată; cauza acelui rezultat nu este declarată
  dovedită, dar gate-ul curent este verde.

Sunt promovate și nu mai sunt ToDo-uri: contractele de API deja implementate,
cardinalitatea Categories, `TABLE-VIEWPORT-01`, failure evidence UI,
inventarul asset-urilor, publicarea bounded și cleanup-ul `spark`. Detaliile
reutilizabile sunt în cele două contracte Reporting și în
[README-ul testelor Reporting](../../atf-reporting-tests/README.md); nu se
reintroduc ca secțiuni istorice aici.

## Cerințe încă deschise

| Prioritate | ID | Lacună rămasă | Owner principal |
|---|---|---|---|
| P2 | `REPORT-PATH-01` | Opt-in declarativ prin adnotare pusă explicit pe E2E-urile și ownerii suitei API component vizate; hook-ul afișează calea absolută după publish, ca ultim mesaj de lifecycle ATF | Reporting deține contractul publish/path; core + console dețin semnalul și proiecția; infrastructura JUnit/TestKit deține adnotarea și hook-ul neutru; consumerii declară opt-in |
| P2 | `SHOP-CAPTURE-01` | Rămâne agregarea unificată Shop peste `SETUP` + `TEST BODY`; profilul generic per scenariu și AutoEx final-only sunt implementate, cu inspecția vizuală AutoEx încă în așteptare | `atf-reporting-tests` pentru contract; Practice Shop pentru integrarea cross-phase |

## Contract promovat — GOV-API-01

`AtfReportingComponentSuite` este gate-ul comun, iar rularea izolată curentă
este verde: **137/137**, fără skip-uri. Contractul acoperă negativele Request /
Response absente sau atribuite ownerului greșit, izolarea a doi owneri, starea
vizibilă la collapse/reopen, verdictele UI/API independente în ambele direcții,
`Fails` și deep-link-ul către scenariul corect. Fixture-urile sunt neutre și nu
apelează Practice Shop. Cerința este închisă; se redeschide numai la regresie
sau la introducerea unei proiecții noi.

### REPORT-PATH-01 — cale absolută după publicarea raportului

- `ExtentReportSession.reportPath()` oferă o cale absolută normalizată, iar
  `close()` finalizează fișierul staged și îl publică atomic la calea finală.
- Nu există un hook comun care să emită calea la consolă **după publicarea
  reușită**; `reportPath()` este disponibil și înainte de close, deci nu dovedește
  că artifactul este gata.
- `openConfigured(...)` construiește sesiuni cu `AtfConsoleSink.noOp()`, iar
  consumatorii pot injecta tot un sink no-op. Hook-ul nu poate presupune că
  simpla existență a unei sesiuni produce output.
- Cerința este **opt-in**, nu output implicit pentru fiecare test sau fixture:
  adnotarea se adaugă explicit, manual, atât pe testele E2E vizate, cât și pe
  clasele/ownerii suitei API de componentă care publică raport Extent și pentru
  care se dorește calea în output. Absența adnotării înseamnă fără mesaj de cale.
- Politica trebuie să fie configurabilă declarativ prin adnotare (opt-in,
  dezactivare explicită și comportament moștenit/compus definit fără ambiguitate).
  `@AtfConsoleMessages` controlează nivelul de detaliu al consolei, nu semantica
  publicării artifactului; nu se reutilizează ca proxy și nu se suprascrie
  implicit. Dacă se extinde contractul existent în locul unei adnotări dedicate,
  rolurile celor două politici rămân independente și documentate.

#### Contractul de lifecycle și ownership

- Hook-ul trebuie atașat ownerului care închide sesiunea, nu fiecărui test
  individual: sesiune per clasă → finalizare după ultimul test al clasei;
  sesiune partajată → finalizare la închiderea resursei root/suite, după ultimul
  copil. Un `AfterAllCallback` de clasă nu este suficient pentru artifactul
  deținut de root store.
- Integrarea trebuie să fie reutilizabilă și automată **după opt-in** pentru
  testele E2E și suitele API de componentă selectate. Adnotarea se pune manual
  pe ownerul raportului/clasa de test vizată; după declarare, hook-ul gestionează
  lifecycle-ul fără blocuri `@AfterAll` scrise în test, fără printarea căii din
  test și fără a doua închidere a sesiunii. Clasele de componentă/fixture și
  clasele E2E fără adnotare nu emit notificare; adăugarea unui test nou nu
  activează outputul prin convenție globală.
- Pentru un raport comun la root, opt-in-ul declarat de clasele E2E sau API
  component participante se agregă la ownerul artifactului: zero declarații
  înseamnă zero notificări; una sau mai multe clase opt-in înseamnă o singură
  notificare după ultimul copil. Politicile ON/OFF incompatibile pentru același
  artifact sunt respinse înaintea execuției, nu rezolvate prin ordinea testelor.
- Notificarea este emisă numai după ce `close()` a terminat fără eroare,
  publicarea atomică s-a încheiat și fișierul final există. Valoarea afișată este
  calea finală absolută, normalizată — nici calea staging, nici calea relativă,
  nici un simplu `reportPath()` citit înainte de publish.
- După notificare nu mai apare niciun mesaj de lifecycle ATF, cleanup sau
  diagnostic de raport. Pentru un singur artifact, linia cu calea este ultimul
  mesaj produs de ATF înainte ca execuția să revină runnerului. Nu se promite că
  Maven/IntelliJ nu va afișa ulterior propriul sumar de teste. Pentru mai multe
  artifacte, notificările formează un bloc final contiguu, în ordine
  deterministă, câte una per artifact publicat.
- Fiecare artifact se anunță exact o dată, indiferent de câți copii au folosit
  sesiunea sau de câte ori este invocat cleanup-ul. Un raport dezactivat,
  necreat ori doar inspectat nu produce anunț; un fixture temporar/intern
  trebuie să poată dezactiva explicit notificarea pentru a nu polua consola.
- Rezoluția adnotării respectă regulile generale ATF pentru clase, meta-adnotări
  și moștenire: selecțiile identice sunt stabile, iar politici contradictorii
  la același nivel eșuează înainte de deschiderea browserului/raportului. YAML-ul
  global `reporting.console.enabled: false` rămâne autoritar; absența
  adnotării sau politica ei OFF nu poate fi anulată de sink-ul global.

#### Contractul de consolă și erori

- Folosește canalul și formatterul tipizat ATF; se introduce un eveniment de
  lifecycle/artifact adecvat, nu se reutilizează semantic un eveniment
  `REQUEST`/`VALIDATION` și nu se scrie direct cu `System.out`, `System.err` sau
  `println` în Reporting, TestKit ori consumer. Proiecția respectă
  `reporting.console.enabled`; dezactivarea detaliilor obișnuite nu trebuie să
  ascundă accidental calea artifactului final.
- Dacă testul a eșuat, dar teardown-ul a publicat raportul, calea se afișează
  totuși. Dacă finalizarea/publicarea eșuează, nu se emite mesaj de succes sau o
  cale prezentată ca artifact valid; cauza de publicare nu trebuie să șteargă
  eroarea testului, iar eroarea primară și cele suprimate trebuie păstrate.
- Hook-ul nu mută `ExtentReportSession` în `atf-test-kit` și nu introduce o
  dependență de Reporting în TestKit. Contractul căii/publicării aparține
  Reporting, evenimentul neutru și formatarea aparțin `atf-core`/`atf-console`,
  iar extensia JUnit generică aparține infrastructurii de test care poate
  rămâne neutră față de renderer. Consumerul furnizează configurația/sink-ul și
  ownership-ul, nu reimplementează mecanismul.

#### Probe obligatorii și delimitarea ownerilor

- `atf-reporting-tests`: dovedește calea absolută normalizată și ordinea
  publish-before-notify pe artifactul final; fișierul este complet și
  deschizabil la momentul notificării; nu se emite pentru publicare eșuată ori
  artifact inexistent; close repetat nu dublează semnalul.
- Modulul care deține hook-ul JUnit: probează prin JUnit Platform atât
  ownership per clasă, cât și root/suite partajat, raport publicat după test
  eșuat, cardinalitate unu, ordinea ultimei linii ATF și păstrarea excepțiilor
  din teardown. Verifică distinct clasa anotată și neanotată, OFF,
  moștenire/meta-adnotare și conflict; pentru root, probează copii E2E/API
  component opt-in și neanotați împreună, notificarea unică la închidere și
  conflictul de policy. O simplă verificare a formatterului nu acoperă lifecycle-ul.
- `atf-console`/core: verifică formatul stabil al evenimentului, includerea
  căii absolute, regulile `enabled`/`detailsEnabled` și lipsa outputului pentru
  sink-ul no-op, fără să cunoască Extent sau suitele consumer.
- Testele consumer verifică adopția efectivă: adnotarea este aplicată explicit
  claselor E2E și ownerilor suitei API component care publică artifactele vizate,
  sesiunea este legată de hook, se folosește canalul configurat, iar suitele
  partajate anunță o singură dată. Nu copiază testele de renderer, formatter sau
  JUnit lifecycle.

**Acceptanță:** adnotarea este prezentă efectiv pe E2E-urile și ownerii suitei
API de componentă selectați; testele de integrare rulează ambele forme direct și
prin suite cu raport partajat. Calea finală, absolută și existentă este ultimul
output de lifecycle ATF, publicarea precedă mesajul, nu există duplicate, iar
clasele fără opt-in rămân tăcute. Testele negative pentru OFF/global
disabled/no-op, publicare eșuată, fixture fără artifact și test eșuat cu publish
reușit demonstrează că outputul reflectă configurația și starea reală, fără
slăbirea contractelor de publicare sau a verdictului JUnit.

### SHOP-CAPTURE-01 — profil UI per scenariu și flow unificat cross-phase

**Stare: parțial rezolvat.** Contractul generic pentru profile per scenariu și
wire-up-ul AutoEx final-only sunt implementate și verificate automat. Gap-ul de
cod rămas este compoziția Shop peste fazele `SETUP` + `TEST BODY`; confirmarea
vizuală a artifactului AutoEx rămâne pending în feedback-ul consumerului.

Setările generice `UNIFIED_FLOW` și `FINAL_ONLY` există, dar nu compun încă
un profil complet selectabil și izolat la granița fiecărui scenariu:

| Suprafață | Codul curent | Gap confirmat |
|---|---|---|
| Lifecycle Reporting | `UiExtentReportSession` păstrează profilul sesiunii ca default și oferă `startScenario(..., scenarioProfile)` pentru override explicit pe scenariu | Izolarea per scenariu este implementată și testată; rămâne suportul de agregare care traversează faze diferite ale aceluiași scenariu |
| Compoziție AutoEx | `AutoExPurchaseSuite` partajează raportul; `AutoExEvidenceMode(FINAL_SCREENSHOT_ONLY)` compune `UNIFIED_FLOW + FINAL_ONLY`, iar `AutoExE2eRun` transmite profilul per scenariu | Comportamentul de cod este verificat; inspecția vizuală a artifactului live este încă în așteptare, fără alt gap de implementare cunoscut |
| Compoziție Shop | `WithE2eHarness` expune `reportFlow`, iar `ShopE2eContext` compune raportul și politica YAML | Profilul complet per clasă nu este încă rezolvat și aplicat pentru flow și capturi împreună; cazul `SETUP` + `TEST BODY` cere agregare cross-phase |

**Contract deja verificat:** profilul tipizat care guvernează evidence-ul unui
scenariu se rezolvă și se izolează per scenariu; integrarea generică din
`atf-reporting-tests` verifică scenarii-surori cu profile diferite în aceeași
sesiune, iar AutoEx live demonstrează `UNIFIED_FLOW + FINAL_ONLY`. Cerințele de
mai jos rămân criterii pentru completarea Shop cross-phase, nu motive de a
redeschide wiring-ul generic deja trecut.

- Profilul per scenariu guvernează cel puțin `flow mode`, politica de captură
  și sursa capturii.
- Setările artifact-wide, precum prezentarea comună, rămân la sesiune; nu este
  necesar ca fiecare opțiune a raportului să varieze per test.
- Consumerul selectează profilul declarativ, fără adnotări concurente care se
  pot contrazice; `CONFIGURED` păstrează YAML, iar override-ul se rezolvă înainte
  de browser.
- Overview/System rămâne metadata de raport, nu recipientul conținutului per scenariu.

- Pentru `UNIFIED_FLOW`, acțiunile UI ale scenariului apar o singură dată, în
  ordinea cronologică, într-un singur tabel Playwright; fiecare rând păstrează
  target, rezultat și durată. În Shop, agregarea traversează `SETUP` și
  `TEST BODY`, fără să piardă identitatea fazei sau verdicturile/aserțiunile.
- Pentru `FINAL_ONLY`, succesul publică o singură captură, atașată validării
  terminale după acțiunea finală. La eșec înainte de terminal nu se inventează
  captura finală; cauza și acțiunile observate rămân în raport, iar politica
  separată de screenshot-on-failure rămâne explicită.
- Profile diferite sunt valide pentru scenarii-surori din același raport comun;
  fiecare profil rămâne izolat și comportamentul este independent de ordinea
  inițializării. Se resping numai conflictele de configurare pentru același
  scenariu, înainte de side-effect-uri.
- `UI_API` păstrează validările și exchange-urile API la checkpoint-ul lor;
  acțiunile agregate nu duplică, mută sau elimină dovezi API.
- Captura finală și gruparea acțiunilor sunt axe distincte: folosirea
  `UNIFIED_FLOW` nu implică o imagine unică, iar `FINAL_ONLY` singură nu
  consolidează tabelele.

**Acceptanță și ownership:** testele generice și consumer verifică straturi diferite.

- `atf-reporting-tests` folosește fixture neutră cu mai multe scenarii/faze și
  profile diferite; inversează ordinea inițializării și verifică izolarea,
  acțiunile, capturile și evidence-ul API.
- Browser proof-ul verifică artifactul proaspăt. Shop și AutoEx păstrează
  teste componente pentru mapping și wiring/preflight; E2E-urile dovedesc
  integrarea reală. Pentru raportul AutoEx, testul live și contractul HTML
  trec; confirmarea vizuală în tabul MCP rămâne deschisă deoarece `file://` a
  fost refuzat de politica browserului.
- Acoperirea include YAML, override, UI-only, UI_API, final-only, succes și
  failure înainte de terminal, fără business flow consumer în fixture-ul generic.

## Închiderea feedback-ului

O cerință se elimină numai după ce criteriul ei trece prin testul din modulul
owner și, când randarea/interacțiunea este criteriu, prin browser pe artifact
proaspăt. Contractul stabil se promovează mai întâi în README-ul/contractul
oficial relevant; rămânerea unei integrări înguste în consumer nu ține deschis
un ToDo generic deja acoperit.
