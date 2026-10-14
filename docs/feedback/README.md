# ATF Java — feedback activ

Acest director este registrul operațional pentru lucrările care încă trebuie
implementate sau întărite prin teste în codul live ATF Java. Păstrează numai
ToDo-uri verificabile. Contractele stabile, explicațiile permanente și
diagramele de utilizare apar în documentele normative din `docs/` și în
README-urile modulelor.

## Regula obligatorie de lucru

Pentru fiecare ToDo:

1. se reanalizează codul live și testele existente înainte de orice editare;
2. se copiază snapshot-ul într-un staging separat, fără oprirea proceselor sau
   a rulărilor existente;
3. se adaugă ori se extinde testul de contract înaintea refactorizării;
4. se păstrează API-ul public și comportamentul observabil, dacă ToDo-ul nu
   cere explicit un contract nou;
5. se repară implementarea, niciodată testul pentru a ascunde regresia;
6. se rulează testele unitare, boundary și componentă relevante;
7. dacă se modifică HTML/CSS/JS, se generează artifactul în staging și se
   verifică în TAB MCP: Overview, Tests, Category, Fails, filtre, collapse,
   deep-links, durate și statusuri;
8. după stabilizare, regula devine normativă în `docs/`, iar ToDo-ul se
   elimină din feedback.

Build-ul de verificare nu se face peste checkout-ul live. Staging-ul trebuie să
fie o locație distinctă, iar comparația finală trebuie să confirme că sursa
staged și sursa live au același conținut, exceptând `target`, `artifacts`,
`.tmp` și logurile generate.

## Gate obligatoriu: sync live → build staging → raport verificat

Acesta este procesul unic pentru orice ToDo de refactorizare. Un build verde
într-un staging vechi nu este dovadă pentru codul live.

1. **Snapshot live:** se recitesc sursele, testele, POM-urile și feedback-ul
   direct din `C:\work\ex\java\atf`; se identifică modificările apărute de la
   ultimul snapshot și se notează clasele afectate.
2. **Sync controlat:** se copiază codul live într-o locație staging nouă și
   unică. Nu se reutilizează un staging vechi și nu se copiază `target`,
   `artifacts`, `.tmp` sau `hs_err_pid*.log` ca sursă de adevăr.
3. **Build complet:** se rulează testele pentru întreg reactorul Maven din
   staging, nu doar testul clasei schimbate. Un exemplu acceptat este
   `mvn.cmd -f <staging>\pom.xml clean test`; pentru gate-ul de acceptance se
   poate folosi `mvn.cmd -f <staging>\pom.xml -pl atf-tests -am test`.
4. **Unit și boundary tests:** se verifică explicit rezultatele din toate
   modulele (`atf-core`, `atf-validation`, `atf-execution`, `atf-reporting` și
   `atf-tests`). Niciun failure nu se ascunde prin relaxarea testului. Orice
   skip trebuie să fie intenționat, documentat și separat de verdictul buildului.
5. **Extent artifact:** rularea trebuie să producă raportul HTML autonom din
   staging. Se verifică existența, dimensiunea, checksum-ul și faptul că asset-
   urile sunt inline/curățate conform contractului.
6. **Randare MCP:** raportul se deschide în TAB MCP, niciodată într-un browser
   Windows pentru validarea taskului. Se verifică efectiv Overview, Tests,
   Category și Fails, apoi status/tag filters, hash deep-links, expand/collapse,
   request → validation, assertions, durate `N ms`, badge-uri, background,
   timestamp-ul absent din tabelul Tests și navigarea `Open in Tests`.
7. **Reconciliere:** după verificare se compară fișierele live/staging pe căi
   relative, excluzând doar artefactele generate. Dacă un alt agent a modificat
   live-ul în timpul buildului, rezultatul se consideră expirat și se reia de la
   pasul 1.
8. **Închiderea ToDo-ului:** numai după ce implementarea, toate testele și
   randarea confirmă același contract se mută regula stabilă în `docs/` și se
   elimină ToDo-ul. Raportul sau testul nu devin surse de adevăr în locul
   codului live.

### Criteriu de sincronizare

Agentul nu are voie să spună că a validat codul live dacă nu poate indica
snapshot-ul staging derivat din live, comanda de build executată pe acel
snapshot, testele complete rezultate și raportul verificat în TAB MCP. Orice
raport copiat anterior, chiar dacă arată corect, este doar referință istorică.

## Ordinea corectă a intervențiilor

```text
contract tests
      ↓
semantic model / ownership
      ↓
facade publică stabilă
      ↓
renderer / adapter / infrastructură
      ↓
component tests + artifact HTML
      ↓
verificare interactivă TAB MCP
      ↓
mutarea regulii stabile în docs/
```

Nu se fac extrageri pur cosmetice. O clasă nouă este justificată numai dacă
primește un singur ownership clar, poate fi testată independent și reduce
cuplarea sau duplicarea reală.

## Ordinea de impact pentru refactorizare

```text
P0  F-01 theme providers → F-02 semantic document → F-03 runtime controllers
P1  F-05 redaction → F-06 immutable runtime snapshot
P2  F-06 architecture rules → F-07 DOM/component verification
```

P0 protejează cel mai mare risc de regresie vizuală și de responsabilități
amestecate. P1 face execuția și validarea deterministe. P2 elimină blind spots
din guard-urile arhitecturale, fixture-uri și verificarea comportamentului
observabil. Ordinea poate fi schimbată numai dacă un test de contract justifică
dependența nouă.

## ToDo-uri active după auditul codului live

### F-01 — Separă `AtfReportTheme` pe suprafețe vizuale

`AtfReportTheme` are acum aproximativ 291 de linii și este o fațadă de
compoziție, dar încă deține prea mult CSS de layout/execution și reguli de
precedență pentru Tests, faze, noduri, badge-uri și responsive behavior. Riscul
real este ordinea selectorilor: o ajustare locală poate schimba alt tab fără să
fie evident din numele clasei.

Implementat deja: `AtfReportThemeTokens`, `AtfReportEvidenceCss`,
`AtfReportOverviewCss`, `AtfReportFailsStatusCss` și `AtfFallbackTheme` dețin
fragmente clare; `AtfReportTheme` le compune într-o singură ordine. Nu se mai
re-desface această parte.

De făcut: extrage doar providerii rămași pentru layout global, execution/faze,
navigation-adjacent și responsive rules. Elimină regulile duplicate de
annotations/badge-uri; fiecare selector comun trebuie să aibă un singur owner.

Teste obligatorii: fiecare provider produce un fragment non-gol; testul de
compoziție protejează ordinea și absența selectorilor duplicați; contractele
pentru culori, badge geometry, background, trace table, tab-uri și responsive
rules rămân verzi; artifactul este verificat în toate tab-urile.

### F-02 — Separă modelul semantic de fallback renderer

`HtmlReportSession` păstra în aceeași clasă sesiunea, modelul mutable
`ScenarioModel`/`PhaseModel`/`RequestModel`/`ValidationModel`, scope-urile de
lifecycle, rendererele pentru patru tab-uri, escaping-ul și scriptul inline.
Primul pas este acum aplicat: `HtmlReportSemanticDocument` deține identitatea,
ordinea și arborele semantic mutabil, iar sesiunea a rămas fațadă de lifecycle
și renderer fallback. Modelul nu cunoaște HTML, CSS, JavaScript sau publicarea
pe disc; proiecțiile sunt acum separate în `HtmlReportTestsView`,
`HtmlReportCategoryView`, `HtmlReportFailsView` și `HtmlReportOverviewView`.

De făcut: păstrează documentul semantic ca sursă unică pentru Extent și
fallback și aplică același model intern și rendererelor Extent. Păstrează
`HtmlReportSession` ca fațadă publică și nu muta reguli de verdict în renderer.
Scopes-urile trebuie să primească în continuare doar node-ul lor, nu lista
globală de scenarii.

Teste adăugate: `HtmlReportSemanticDocumentTest` verifică ID-uri deterministe,
ordine de inserare, deduplicarea tag-urilor, snapshot read-only și sibling-ul
semantic `request → validation`. Rămân obligatorii: aceeași fixture semantică
trebuie să producă aceleași statusuri, durate, tags, assertions și reguli de
redaction în fallback și Extent; testele trebuie să verifice și lifecycle-ul
invalid, close repetat și publish atomic.

### F-03 — Descompune finalizarea Extent și JavaScript-ul runtime

`ExtentReportFinalizer` este acum o fațadă mică, iar
`ExtentReportPublicationPipeline` coordonează asset inlining, theme injection,
view identifiers, metadata, runtime și publicarea atomică. Runtime-ul nu mai
este un singur owner pentru durate și Fails:
`ExtentReportDurationRuntime` deține agregarea duratelor, iar
`ExtentReportFailureRuntime` deține proiecția Fails, diagnosticul și deep-link-ul
„Open in Tests”.

De făcut: păstrează pipeline-ul ca orchestrator și extrage controllere
deterministe pentru navigation/hash, filters, collapse și DOM contract. Aceste
responsabilități încă locuiesc în `injectRuntimeBehaviour`, un script foarte
mare. Durata și Fails sunt deja extrase; nu le muta înapoi și nu adăuga un al
doilea bootstrap care suprascrie primul.
CSS-ul de evidence este de asemenea extras în `AtfReportEvidenceCss`; acesta
deține JSON, trace tables și disclosure details, iar `AtfReportEvidenceCssTest`
verifică atât providerul izolat, cât și compoziția finală a temei.
Fiecare controller primește doar markup-ul/selectorii de care are nevoie.
Ordinea fragmentelor trebuie declarată și testată; nu se adaugă al doilea
script care suprascrie primul.

Teste obligatorii: pipeline order, asset cleanup concurent, deep-link Tests /
Category / Fails, filtrele de status/tag, collapse, `Open in Tests`, scenarii
fără children, durate `N ms`, lipsa timestamp-ului din tabelul Tests și
preservarea unui `atfOpenTest(id)` backward-compatible.

### F-04 — Fă redaction-ul compozabil și fără bypass accidental

Implementat incremental: `SensitiveDataRedactor` este acum fațadă peste
`SecretPatternRedactor`, `StructuredJsonRedactor`, `PiiRedactor` și
`LocalPathRedactor`, toate primind același `RedactionContext` immutable.
`enabled=false` rămâne singura dezactivare explicită și traversează toate
strategiile fără bypass implicit.

Teste adăugate pentru ownership-ul strategiilor, nested JSON, arrays, malformed
JSON, PII, local paths și input immutability. Fallback-ul text verifică explicit
forma `token=***REDACTED***` și păstrează parametrii query neconfidențiali, de
exemplu `mode=full`. Traversarea JSON folosește o listă stabilă de nume de
câmpuri, deci nu mută iteratorul în timpul redacției; `ExtentReportTree`
păstrează aceeași politică pentru expected/actual înainte de publicare.
Rămâne de întărit în iterația următoare propagarea secretelor prin retry
history și failure cause.

### F-05 — Completează snapshot-ul immutable de runtime

`AtfConfiguration` și snapshot-urile existente sunt corecte ca direcție, însă
loader-ul de reporting citește încă `user.dir`, system property și environment
în puncte statice. Timeout, retry, auth, redaction, routes și publication
policy nu sunt încă un singur context immutable transmis tuturor componentelor.

De făcut: construiește un snapshot explicit pentru o execuție și injectează-l în
composition root; păstrează discovery doar ca bootstrap. Nu lăsa modulele să
revină la `user.dir` după bootstrap și nu folosi state static mutabil.

Teste obligatorii: două working directories, două configurații paralele,
snapshot immutable după creare, precedence property/environment/file, schema
root explicit și publish directory separat.


### F-06 — Înlocuiește boundary scan-ul fragil cu reguli verificabile

`ArchitectureBoundaryTest` caută tokenuri text în surse. Este un guard util,
dar poate rata importuri indirecte, false-positive-uri în comentarii/string-uri
și dependențe introduse în POM. Auditul live arată acum 27 de clase în
`atf.api.reporting` și 22 în `atf.api.core.http`; pragul de maximum 8 clase pe
pachet trebuie să rămână semnal de revizuire, nu motiv pentru directoare
artificiale.

De făcut: verifică atât importurile/source packages, cât și dependențele Maven;
raportează package ownership, composition roots și clasele peste prag într-un
diagnostic clar. Interzice Extent în core/validation/execution, HTTP/business
validation în reporting și state static mutabil de execuție.

Teste obligatorii: încălcare intenționată în fixture izolată, dependență POM
interzisă, numărare pe package, composition root și allow-list explicit pentru
compatibility APIs.

**Întărire r13:** `ArchitectureBoundaryTest` verifică acum și POM-urile,
nu doar tokenuri din surse: modulele renderer-neutral nu pot depinde de
`atf-reporting`, iar direcția permisă `atf-core → atf-validation →
atf-execution`, cu `atf-reporting` la marginea de publicare, este verificată
explicit. Testul nu transformă pragul de 8 clase într-o regulă artificială;
package count rămâne diagnostic de coeziune pentru refactorizări ulterioare.

### F-07 — Ridică testele de la `contains(...)` la comportament DOM

Există contracte statice puternice și testele de interacțiune acoperă mult din
artifact, dar assertions pe string nu pot confirma singure computed style,
aria state, filtrarea efectivă sau navigarea după JavaScript.

De făcut: adaugă un component test headless/browserless care pornește din
artifactul autonom și verifică DOM-ul după interacțiune. Testul nu depinde de
browserul Windows, server extern sau raport temporar din altă rulare.

Teste obligatorii: Overview → Tests → Category → Fails, status/tag filters,
hash deep-links, expand/collapse, `Open in Tests`, count-uri/durate `N ms`,
badge geometry, background uniform și absența timestamp-ului din tabelul Tests.

## Criterii de închidere

Un ToDo se închide numai când:

- există owner de cod și modificare minimă, localizabilă;
- există test unitar determinist și, unde este necesar, test component;
- testele existente nu sunt șterse, relaxate sau rescrise pentru a ascunde o
  regresie;
- build-ul rulează într-un staging separat și nu produce loguri crash sau
  modificări în checkout-ul live;
- artifactul HTML este verificat în TAB MCP, nu doar prin compilare sau
  `contains(...)`;
- regula stabilă este mutată în documentul normativ potrivit;
- ToDo-ul închis este eliminat din acest registry.

## Documente de referință

- [ATF_Java_Improvement_Backlog.md](ATF_Java_Improvement_Backlog.md) — ordinea
  P0/P1/P2 și matricea de teste pentru intervențiile mari.
- [ATF_Reporting_Duration_Semantics.md](ATF_Reporting_Duration_Semantics.md) —
  durate, agregare structurală și geometria status badge-urilor.
- [ATF_Architecture.md](../ATF_Architecture.md) — boundary-urile stabile ale
  modulelor.
- [ATF_Reporting_Contract.md](../ATF_Reporting_Contract.md) — contractul
  observabil al raportului.
- [ATF_BUILD_AND_EXECUTION_SAFETY.md](../ATF_BUILD_AND_EXECUTION_SAFETY.md) —
  staging, sync live și build fără oprirea rulărilor.
