# ATF Java — backlog activ de calitate

Acest fișier păstrează numai ToDo-uri care mai cer implementare sau teste în
codul live ATF Java. Nu este jurnal, nu conține scoruri și nu repetă contractele
stabile din README-uri. Un punct se închide numai când implementarea, testul și
dovada observabilă sunt prezente.

## Regula de lucru

Pentru fiecare punct:

1. se verifică din nou codul live înainte de modificare;
2. se creează un snapshot staging nou din codul live, fără oprirea altor
   rulări și fără reutilizarea artefactelor generate;
3. se adaugă întâi testul de contract relevant sau se extinde unul existent;
4. se repară implementarea, nu se relaxează testul;
5. se rulează întreg reactorul Maven: unit, boundary, component și acceptance;
6. se generează raportul Extent din același staging și se verifică efectiv în
   TAB MCP Overview, Tests, Category și Fails, inclusiv interacțiunile;
7. se compară snapshot-ul cu live pe căi relative; dacă live-ul s-a schimbat
   în timpul buildului, rezultatul se invalidează și procesul se reia;
8. regula stabilă se mută în documentul normativ, iar ToDo-ul se elimină.

## Ordine de execuție

```text
J-02 lifecycle unic și teardown sigur
  ↓
J-03 thread confinement și publicare concurentă
  ↓
J-04 model semantic comun pentru renderer-e
  ↓
J-05 finalizer/theme/client împărțite pe responsabilități
  ↓
J-06 configurație și schema resolver deterministe
  ↓
J-07 redaction compozabil
  ↓
J-08 fixture-urile de test separate de orchestrare
  ↓
J-09 teste DOM pentru comportament observabil
  ↓
J-10 boundary checks și pragul de complexitate al pachetelor
```

## P0 — corectitudine și ownership

### J-02 — Unifică lifecycle runner-ul și păstrează cauza primară

**Dovadă curentă:**
`ApiScenarioPhaseExecutor` deține acum invocarea fazei, maparea semnalelor,
publicarea cauzei, teardown-ul compatibil și închiderea fazei.
`ApiScenarioLifecycleRunner` folosește acum un singur `PhaseAccumulator` pentru
overload-ul cu `Consumer<Boolean>` și overload-ul cu `AtfReportPorts.Session`.
Precedența, ordinea fazelor și alegerea cauzei primare sunt decise într-un
singur loc.

**Rămas:** păstrează executorul de fază ca responsabilitate izolată și
extinde matricea numai pentru noi semnale sau politici. Teardown se încearcă
după failure/skip; cauza primară se păstrează, iar failure-ul de teardown se
păstrează ca evidence secundar.

**Teste obligatorii:** `ApiScenarioPhaseExecutorTest` acoperă maparea
semnalelor, primary-cause și cleanup-ul fazei, iar matricea din
`ApiScenarioLifecycleRunnerTest` acoperă setup fail/skip, warning → body fail,
warning → teardown fail, teardown fail după body fail, phase order și statusul
final.

### J-03 — Coordinator explicit pentru concurență în aceeași sesiune

**Dovadă curentă:**
`ExtentReportSession` este acum thread-confined și refuză mutațiile dintr-un
thread diferit. Workspace-ul și publicarea sunt izolate; rămâne nedefinit doar
scenariul în care aceeași sesiune ar trebui să accepte concurență intenționată.

**De făcut:** păstrează contractul thread-confined ca default și, numai dacă
apare un caz real de utilizare, introdu un coordinator serializat separat.
Testează explicit că două sesiuni paralele rămân independente și că nicio
soluție de sincronizare nu mută state între rulări.

**Teste obligatorii:** două sesiuni paralele cu același root, două rapoarte cu
nume timestamped, publicare atomică, cleanup fără ștergerea fonturilor altui
run, close repetat și failure de publish fără pierderea staged artifactului.

## P1 — design intern și comportament stabil

### J-04 — Introdu un document semantic comun pentru renderer-e

**Dovadă curentă:**
`ExtentReportSession` și `HtmlReportSession` construiesc încă modele de
integrare separate, dar fallback-ul nu mai ține arborele semantic în fațada
sa: `HtmlReportSemanticDocument` deține identity, ordering și nodurile
scenario → phase → request/validation. Documentul este package-private și
returnează snapshot-uri read-only, fără dependență de HTML/CSS/JS.

**De făcut:** extinde modelul intern minimal pentru suite → class → scenario
→ phase → request/validation → evidence către Extent. Fallback-ul are deja
renderer-e independente pentru Tests/Category/Fails/Overview; Extent și
fallback trebuie să consume același model și doar să îl redea;
nu muta reguli de business sau redaction în renderer. Nu se adaugă o abstracție
generică fără un test care demonstrează eliminarea unei duplicări reale.

**Teste obligatorii:** aceeași fixture cu pass, fail, skip, warning, tags,
JSON, trace și assertions în ambele renderer-e; aceleași statusuri/count-uri;
aceeași ierarhie și aceleași limite de redaction.

### J-05 — Descompune finalizarea HTML, tema și client behaviour

**Dovadă curentă:**
`ExtentReportFinalizer` este acum o fațadă mică. `ExtentReportPublicationPipeline`
are aproximativ 158 de linii de orchestration, dar conține încă un
`injectRuntimeBehaviour` foarte mare; `AtfReportTheme` are aproximativ 291 de
linii și păstrează layout/execution/responsive CSS; `HtmlReportSession` rămâne
fațada fallback, iar modelul semantic este mutat în
`HtmlReportSemanticDocument`.

Markup-ul static pentru navigare/Fails și auditul Overview este deja extras în
`ExtentReportNavigationView` și `ExtentReportOverviewMarkup`. Runtime-ul este
acum împărțit explicit: `ExtentReportDurationRuntime` deține agregarea
duratelor, iar `ExtentReportFailureRuntime` deține proiecția Fails și deep-link-ul
spre Tests. `ExtentReportRuntimeScriptTest` fixează contractul acestor două
responsabilități. ToDo-ul rămas este separarea controllerelor de navigation/hash,
filters, collapse, DOM și a providerilor CSS, cu aceeași ordine de fragmente și
aceeași dovadă în artifact.
`ExtentReportFinalizer` este acum o fațadă publică mică; ordinea de publicare
este deținută de `ExtentReportPublicationPipeline`:
assets → theme → navigation → views → overview metadata → runtime → atomic
write → cleanup. `ExtentReportFinalizerBoundaryTest` verifică artifactul lipsă,
injectarea theme/runtime și cleanup-ul fără director `spark`.

Tema fallback este de asemenea separată în `AtfFallbackTheme`; `AtfFallbackThemeTest`
protejează contractul renderer-neutral fără să amestece CSS-ul Extent cu HTML-ul
standalone. CSS-ul Overview/Category este acum separat în `AtfReportOverviewCss`,
iar `AtfReportOverviewCssTest` protejează compoziția și precedența finală a
selectoarelor de tabele, disclosure și statusuri.
Geometria statusului din lista Fails este separată în `AtfReportFailsStatusCss`,
iar `AtfReportFailsStatusCssTest` verifică reutilizarea tokenurilor comune și
absența dimensiunilor locale în statusul failure detail.

Evidence styling este acum deținut de `AtfReportEvidenceCss`, separat de
fațada temei; `AtfReportEvidenceCssTest` protejează regulile pentru JSON,
trace tables și disclosure details. Verificările artifactului sunt deja
împărțite pe grupuri semantice și nu mai constituie un ToDo separat.

**De făcut:** extrage controllere mici și ordonate pentru navigation/hash,
filters, collapse și DOM contract din `injectRuntimeBehaviour`; pentru CSS mai
rămâne separarea layout/execution/responsive, deoarece tokens, evidence,
overview, Fails și fallback sunt deja provideri dedicați. `AtfReportTheme`
trebuie să rămână fațadă de compoziție, nu loc pentru noi reguli locale.
Fațadele păstrează ordinea finală a fragmentelor, deoarece ordinea selectorilor
este parte din randare.

**Teste obligatorii:** pipeline order, asset cleanup în rulări paralele,
computed styles pentru badge-uri/suprafețe, click pe toate tab-urile, filtre,
collapse, deep-link Tests/Category/Fails și centrul titlului scenariului fără
deplasarea toolbar-ului.

### J-06 — Completează snapshot-ul de runtime

**Dovadă curentă:**
`SchemaResourceResolver` primește acum rădăcini declarate și respinge
traversal-ul/symlink escape; rezoluția relativă nu mai este responsabilitatea
implicită a `user.dir`. `AtfConfiguration` este immutable, dar nu reprezintă
încă întregul runtime: timeout, retry, auth, redaction, routes și publication
policy rămân dispersate.

**De făcut:** compune snapshot-ul immutable complet pentru execuție și
raportare, păstrând `SchemaResourceResolver` injectabil. Nu redeschide rezoluția
prin `user.dir`; rădăcinile trebuie să rămână parte a configurației explicite.

**Teste obligatorii:** două working directories, root-uri diferite în paralel,
schema absentă/invalidă/validă, traversal respins și imutabilitate după
crearea executorului.

### J-07 — Separă redactorii fără bypass implicit

**Dovadă curentă:**
`SensitiveDataRedactor` este fațadă peste
`SecretPatternRedactor`, `StructuredJsonRedactor`, `LocalPathRedactor` și
`PiiRedactor`, compuse prin `RedactionContext` immutable. Configurația absentă
rămâne conservatoare; `enabled=false` este singura dezactivare explicită.

**Teste adăugate:** nested JSON, arrays, malformed JSON, headers, query, paths,
PII, ownership separat și verificarea că input-ul original nu se modifică.
Fallback-ul text acoperă explicit `token=...`, iar regex-ul păstrează
parametrii query neconfidențiali de după secret (`mode=full`).
Traversarea JSON folosește o listă stabilă de nume de câmpuri și nu mută
iteratorul în timpul redacției; `ExtentReportTree` redacționează deja
expected/actual înainte de publicare. Rămâne de adăugat explicit testul de
retry/failure propagation.

## P2 — testabilitate și disciplină de mentenanță

### J-08 — Separă fixture-urile API de testele de acceptance

**Dovadă curentă:**
`ApiAcceptanceReportTest.java` este acum orchestratorul public, iar suportul
este separat în `AcceptanceTransport`, `ApiAcceptancePlanBuilder` și
`ApiAcceptancePublisher`. Transportul fake, DSL-ul de lifecycle și publicarea
evidence nu mai sunt responsabilități private ale testului.

**Implementat:** `AcceptanceTransport` gestionează rutele deterministe Users,
Posts și negative; `ApiAcceptancePlanBuilder` construiește planul fluent cu
setup/body/teardown; `ApiAcceptancePublisher` publică request details, JSON,
contract validation și rezultatul semantic în sesiunea Extent. Orchestrarea
raportului rămâne comună și nu se duplică în testele de produs.

**Teste obligatorii:** `ApiAcceptanceFixtureSupportTest` verifică ruta POST
fără autentificare cu status 401, content type și body concret, precum și
conservarea metadatelor și fazelor din plan. `ApiAcceptanceReportTest`
verifică în continuare ownership-ul Users/Posts/lifecycle, phase order,
validation sibling, tags și artifactul HTML conform contractului. Extinderea
în clase acceptance distincte pentru Users, Posts și lifecycle rămâne un pas
opțional, justificat doar de creșterea suitei.

**Validare r12c:** snapshotul live sincronizat în
`C:\work\ex\java\.tmp\Atf-oop-20261014-r12c` a trecut reactorul complet cu
`0` failures și `0` errors; `atf-tests` are un singur skip intenționat.
Artifactul timestamped este conform cu `ReportArtifactInspector` (`53`
checks, `0` violations) și a fost verificat manual în TAB MCP pe Overview,
Tests, Category și Fails, inclusiv expand/collapse și deep-link-urile către
testul exact.

### J-09 — Adaugă teste DOM pentru comportamentul raportului

**Dovadă curentă:**
contractele statice verifică multe fragmente prin `html.contains(...)`; ele nu
pot confirma singure click-ul, computed style-ul, aria state-ul sau filtrarea
după execuția JavaScript.

**De făcut:** adaugă un test component headless pentru artifactul real. Păstrează
testele statice pentru invariants, dar verifică în DOM Overview → Tests →
Category → Fails, filters, deep-links, expand/collapse, Open in Tests, count-uri,
durate `N ms`, fără timestamp în tabel și geometria comună a status badge-urilor.

**Închidere:** testul pornește dintr-un artifact autonom și nu depinde de
browserul Windows sau de un server extern.

### J-10 — Întărește boundary checks și pragul de complexitate

**Dovadă curentă:**
modulele au boundary tests, dar `atf.api.reporting` concentrează numeroase
clase cu responsabilități diferite, iar `AtfReportTheme`/`ExtentReportSession`
au deja hotspots mari. Separarea `atf-validation` este implementată, dar
trebuie menținută prin boundary tests.

**De făcut:** extinde testul de arhitectură pentru a interzice Extent/JUnit în
`atf-core`, `atf-validation` și `atf-execution`, HTTP/business validation în
reporting, state static mutabil și importuri vendor în afara composition root.
Folosește pragul
de maximum 8 clase de producție per pachet ca semnal de revizuire; nu crea
directoare artificiale doar pentru a satisface numărul, ci mută clase numai când
ownership-ul și coeziunea o cer.

**Teste obligatorii:** scanare de dependențe, numărare pe pachete, verificarea
composition root și test care eșuează la încălcarea unei reguli.

## Definition of Done

- ToDo-ul are un owner de cod clar și o modificare minimă, localizabilă;
- există test unitar determinist și, când este cazul, test de componentă;
- testele existente nu sunt șterse, relaxate sau rescrise pentru a ascunde o
  regresie;
- orice schimbare de raport produce artifact în staging separat;
- artifactul este verificat în TAB MCP pentru structură, navigare și randare;
- README-ul normativ este actualizat numai după ce regula este stabilă;
- ToDo-ul este eliminat imediat după închidere, nu păstrat ca istoric.
