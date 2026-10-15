# ATF Java — feedback activ

Acest director păstrează îmbunătățirile care mai trebuie implementate sau
întărite prin teste în codul live. Contractele stabile și deciziile închise
apar în `docs/` și în README-urile modulelor; aici rămâne și o secțiune scurtă
de trasabilitate pentru ToDo-urile închise, ca să nu reapară feedback-ul deja
rezolvat.

## Audit live — 2026-10-15

Auditul UI poate primi direct un artifact Extent existent prin
`-Datf.ui.report=...`; nu mai este obligatorie rerularea API pentru
investigație. Capturile sunt configurate sub `ui.screenValidation` și sunt
păstrate ca linkuri `data:` Base64, grupate împreună cu assert-urile aceleiași stări
vizuale.

Inputul poate fi păstrat și în snapshot-ul YAML:

```yaml
ui:
  audit:
    inputReport: artifacts/reports/api-atf-extent-report.html
```

Override-ul manual `-Datf.ui.report` are prioritate peste YAML. Ambele variante
ajung în `ReportAuditContext`, care verifică artifactul, îl servește read-only,
pornește Playwright și publică un raport UI separat. Această infrastructură este
comună atât pentru auditarea unui raport existent, cât și pentru auditarea
raportului generat în aceeași rulare; nu se acceptă două lifecycle-uri sau două
implementări divergente ale navigării.

### Audit Categories pe input API existent — verificat 2026-10-15 17:21

`ExistingExtentCategoriesExploratoryTest` a fost adăugat pentru explorarea
profundă a `#category-view`, fără rerularea API-ului. Pe inputul
`artifacts/reports/api-atf-extent-report_2026-10-15_16-57-07-603.html` testul a
selectat toate cele 29 de categorii descoperite live și a verificat pentru
fiecare: filtrul activ, titlul selectat, count-ul declarat, numărul de rânduri
`tag-test-status`, linkurile `a.linked` și `test-id`-urile numerice. Un link real
a fost apoi urmărit până la `#test-view`.

Rezultatul este separat de input:

```text
input:  artifacts/reports/api-atf-extent-report_2026-10-15_16-57-07-603.html
output: artifacts/reports/ui/existing-ui-categories-atf-extent-report_2026-10-15_17-21-14-152.html
29/29 category filters reconciled; navigation PASS
30 inline Base64 media anchors (`base64 img`); 0 duplicated UI screenshot images
```

Pentru aceste explorări, `screenValidationAsMedia(...)` păstrează tabelul
Playwright colapsabil și publică screenshot-ul Base64 inline, ca ancoră
`data:image/png;base64,...` cu badge-ul `base64 img`, în artifactul Extent. Nu se
folosește un `<img>` duplicat, nu se scrie PNG extern și nu se schimbă
raportul API primit.

Validarea UI este întotdeauna subpasul unei propoziții Gherkin din faza care
conține acțiunea (`SETUP`, `TEST BODY` sau `TEARDOWN`); nu se publică niciodată
ca nod frate. `SETUP` și `TEARDOWN` sunt opționale: un audit autonom poate avea
doar `TEST BODY`, iar fazele suplimentare se declară numai când există
precondiții, fixture-uri, cleanup, read-back sau outcome de lifecycle real.
Când există acțiuni
Playwright, captura Base64 este plasată imediat sub tabelul acțiunilor, în aceeași
zonă colapsabilă, fără un wrapper semantic separat.

### Clarificare critică pentru auditul vizual

Un tabel Playwright fără captură nu este automat „evidence complet”.
`Screen validation` reprezintă starea și verdictul, tabelul descrie acțiunile
care au produs starea, iar screenshot-ul este dovada vizuală locală. Pentru
`ui.screenValidation.enabled=true`, relația obligatorie este:

```text
Gherkin step → Screen validation → action table → inline Base64 anchor (`base64 img`)
```

Captura trebuie să fie imediat sub tabel, în aceeași zonă colapsabilă. Fără ea,
un audit poate confirma că s-a făcut un click, dar nu poate confirma ce a fost
vizibil după click. Un pas tehnic non-vizual poate exista fără imagine numai
dacă nu emite tabel Playwright; politica de captură dezactivată trebuie
etichetată ca audit funcțional, nu confundată cu audit vizual complet. La failure, acțiunile deja executate nu
se pierd doar pentru că `screenValidation(...)` nu a mai fost atins.

Detaliile și gap-urile de cod sunt în
[`ATF_UI_Exploratory_Feedback.md`](ATF_UI_Exploratory_Feedback.md), F-09–F-13.
F-08 este închis: rendererul respinge acum orice disclosure de acțiuni care nu
are exact o captură Base64 asociată prin link. Rămân urmărite flush-ul la failure,
extragerea claselor UI mari, expandarea tuturor nodurilor, separarea
observațiilor de acțiuni și snapshot-ul unic de configurare.

Auditul a fost făcut pe checkout-ul curent, nu pe artifacte vechi din `target`
sau `artifacts`. Build-ul reactorului principal a fost verde: `mvn -q test`, cu
247 teste în XML-urile Surefire, 8 skip-uri și 0 suite eșuate. După sincronizarea
completă a surselor, modulelor și configurației root, checkout-ul live a trecut
curat cu `mvn -q -o clean test -DskipTests=false` pentru scope-ul său (`39`
teste, `10` skip-uri, `0` eșecuri). Diferența de număr vine din scope-ul de
rulare și testele UI opt-in, nu dintr-o relaxare a testelor.

Pentru analiza UI curentă, `mvn -pl atf-ui -am test` a trecut cu `38` teste în
`atf-core` și `6` în `atf-ui`, fără failures/errors. În plus, testul negativ
`ExtentReportUiEvidenceTest.rejectsUiActionTableWithoutExactlyOneInlineScreenshot`
confirmă că rendererul nu mai publică tabel de acțiuni fără exact o captură
link. Contractul browser-level pentru F-08 este astfel închis; loaderul,
politica browserului și recorderul rămân verificate separat.
Flush-ul la failure și explorarea tuturor nodurilor rămân explicit în F-09–F-13.

### Revalidare UI live — 2026-10-15 14:09

Explorarea E2E a fost re-rulată pe codul live cu:

```text
mvn -pl atf-tests -am -Datf.ui.enabled=true \
  -Dtest=atf.ui.tests.exploratory.ApiExtentReportExploratoryTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Rezultat: `4/4` teste UI verzi, `0` failures, `0` errors. Setup-ul a generat
un raport API input nou, read-only, iar testul a generat separat raportul UI
în `artifacts/reports/ui/`. În TAB MCP au fost verificate raportul UI și inputul
API: Overview, Tests, Category, Fails, filtrele, deep-link-ul către Tests,
expandarea scenariului/fazelor/pașilor, assertion disclosures și capturi
linkuri Base64; raportul UI nu are imagini externe și sesiunea finală nu a avut
erori de consolă.

Această revalidare închide partea din F-05 care cere explorare comportamentală
dinamică și raport UI auditabil. Nu închide încă standardizarea globală a
duratelor și computed styles din D-01–D-04; acestea rămân ToDo-uri separate.

| Status | ToDo-uri | Concluzie verificată |
|---|---|---|
| `CLOSED` | F-04, F-08 | failure-chain retry + lifecycle + redaction și cardinalitatea strictă a capturii UI sunt acoperite |
| `PARTIAL` | F-03, F-05, F-10, F-13, D-01, D-02, D-03 | fundația există, dar contractul nu este încă uniform sau complet verificat |
| `OPEN` | F-01, F-02, F-06, F-07, F-09, F-11, F-12, D-04 | gap real în codul live, cu impact direct asupra mentenanței, auditabilității sau raportului |

### Revalidare input existent și context comun — 2026-10-15

`ReportAuditContext` este acum infrastructura comună pentru auditul unui raport
generat în aceeași rulare și pentru auditul unui HTML primit ca input. A fost
verificată rularea read-only pe un artifact API existent:

```text
ExistingExtentReportExploratoryTest: 4/4 PASS
input: artifacts/reports/api-atf-extent-report_2026-10-15_16-56-14-768.html
output: artifacts/reports/ui/existing-ui-atf-extent-report_2026-10-15_16-59-56-324.html
```

Rezoluția relativă a fost verificată separat: `-Datf.ui.report=...` se rezolvă
față de directorul lui `atf.yaml`, nu față de `atf-tests`, unde Surefire rulează
fork-ul. Un raport API fără evidence UI existent nu este respins artificial;
este suficient ca auditul curent să producă propria captură Base64, iar
evidence-ul existent devine obligatoriu numai când inputul conține deja acțiuni
UI.

### F-08 — închis prin contract de renderer și regresie negativă

`ExtentEvidenceRenderer.renderUiActions(...)` respinge acum orice secvență de
acțiuni UI care nu are exact o imagine Base64 validă. Regula este
verificată înainte de publicare, nu dedusă din existența unei imagini în altă
parte a documentului. `ExtentReportUiEvidenceTest` acoperă fixture-ul invalid,
iar raportul UI nou a fost verificat prin click-uri reale în TAB MCP: pasul
Gherkin conține `Screen validation`, secvența cu 17 acțiuni Playwright și
linkul către imaginea Base64 asociată în același nod de validare. Verificările `Find element`
și `Expect text` apar ca rânduri în tabel, nu ca assert-uri ascunse. Nu se închide prin aceasta
F-09: o excepție înainte de `screenValidation(...)` trebuie încă să păstreze
și să publice acțiunile deja executate.

Dovadă:

```text
atf-reporting: 111 teste verzi, 0 failures, 0 errors
ApiExtentReportExploratoryTest: 5/5 verzi
ExistingExtentReportExploratoryTest: 4/4 verzi
TAB MCP: action sequence expandat, 17 rânduri, rezultat Pass, link Base64 vizibil
TAB MCP: navigare Overview → Tests și expandare Gherkin/TEST BODY verificate
```

### Închise / verificate

#### F-04 — failure-cause chain pentru retry + lifecycle

Închis pe baza `LifecycleApiScenarios`, `ApiAcceptanceReportTest` și a
artifactului timestamped: retry-ul este păstrat, failure-ul de contract ajunge
în verdict, teardown-ul continuă, cauzele sunt ordonate `PRIMARY/SECONDARY`,
iar redaction-ul este verificat atât în `Tests`, cât și în `Fails`. Nu mai este
un ToDo de implementare; rămâne doar un contract de regresie în testele deja
existente.

## Proces obligatoriu pentru orice ToDo

1. Recitește codul, testele, POM-urile și README-urile din checkout-ul live.
2. Creează un staging nou, separat de checkout și de orice rulare existentă;
   nu opri procesele și nu reutiliza `target`, `artifacts`, `.tmp` sau loguri.
3. Scrie sau extinde testul care exprimă contractul înaintea refactorizării.
4. Repară implementarea; nu relaxa și nu rescrie testul pentru a ascunde
   regresia.
5. Rulează reactorul relevant în staging și verifică faptul că staging-ul nu
   a devenit învechit față de codul live în timpul buildului.
6. Pentru raportare, generează artifactul în staging și verifică în TAB MCP
   `Overview`, `Tests`, `Category`, `Fails`, filtrele, collapse, deep-links,
   duratele, statusurile și computed styles relevante.
7. Compară rezultatul cu codul live și cu contractul normativ. Mută regula
   stabilă în README-ul potrivit și elimină ToDo-ul numai când există dovadă
   în cod, teste și artifact.

## ToDo-uri active

### F-01 — Decompune orchestration-ul din reporting fără a schimba contractul

`AtfReportTheme` are încă aproximativ 300 de linii de CSS compus manual, iar
`ExtentReportRuntimeBehaviour` concentrează navigation, filtre, collapse,
semantic evidence, durate și audit DOM în JavaScript inline. Există deja
tokenuri și contributori CSS, dar fațada încă deține prea multă compoziție
imperativă și are reguli duplicate pentru annotations/tag-uri.

Refactorizarea trebuie să păstreze un singur facade public și să introducă
contributori/coordonatori mici pentru layout, evidence, navigation, filters,
collapse și DOM audit. Elimină duplicatele CSS printr-o singură sursă de
tokenuri; nu crea module Maven sau directoare artificiale.

Teste de închidere:

- ordine deterministă a contribuțiilor CSS/JS;
- filtre, tab-uri, hash deep-links și collapse;
- Request details, Response details, JSON body și Execution evidence ca
  disclosures distincte;
- status/durată centrate, badge geometry și background uniform în toate tab-
  urile;
- două sesiuni paralele fără scurgere de stare.

### F-02 — Folosește un snapshot semantic comun pentru Extent și fallback

`HtmlReportSemanticDocument` modelează fallback-ul, în timp ce adapterul
Extent construiește direct noduri și evidence. Cele două proiecții au deja
contracte apropiate, dar nu consumă aceeași structură semantică immutable.
Aceasta păstrează risc de drift între `Tests`, `Fails`, `Category` și fallback.

Definește un snapshot intern comun pentru:

```text
suite → class → scenario → phase → request/validation → evidence
```

Snapshot-ul trebuie să conțină ownership, status, durate, tag-uri,
Request details, Response details, body-uri, Execution evidence și assertions.
Extent și fallback îl consumă separat; niciun renderer nu calculează verdictul
și nu mută ownership-ul request/validation.

Test de închidere: aceeași fixture cu pass, warning, fail, skip, tags,
request/response body, retry și assertions produce aceeași ierarhie, aceleași
statusuri și aceleași count-uri în ambele proiecții.

### F-03 — Închide snapshot-ul de configurare la composition root

**Status: `PARTIAL`.**

`AtfRuntimeSnapshot` și compunerea `ApiModuleFactory` există și sunt
verificate. Separarea de ownership este acum închisă: `atf.yaml` păstrează
configurația generală (`api`, `validation`, `reporting`), iar catalogul fake
este în `atf-mock.yaml`, încărcat de `atf-mock`. `atf.mock.config` și
`ATF_MOCK_CONFIG` permit selectarea explicită a catalogului pentru IntelliJ,
CI și rulări paralele; fallback-ul built-in este permis numai când nu există
nicio sursă configurată.

ToDo-ul rămas este snapshot-ul root-scoped, strict și unic: composition root-ul
trebuie să rezolve explicit sursele, căile relative față de fișierul lor și să
producă snapshot-uri immutable pentru API, mock, console și reporting. După
bootstrap, modulele nu mai recitesc fișierele și nu mai depind de working
directory global. Nu se introduce modul Maven `atf-config` și nu se mută parserul
YAML în `atf-core`.

Teste de închidere: două root-uri concurente, două working directories,
precedence property/env/YAML/default, schema invalidă/chei necunoscute,
URI/timeout/path invalid, path traversal și imutabilitate după bootstrap.

### F-05 — Verifică artifactul prin comportament, nu numai prin string-uri

**Status: `PARTIAL`.** Există
`atf-ui/src/main/java/atf/api/ui/playwright/PlaywrightUiSession.java`,
`atf-tests/src/test/java/atf/ui/tests/ApiExtentReportUiTest.java` și
`atf-tests/src/test/java/atf/ui/tests/exploratory/ApiExtentReportExploratoryTest.java`.
Testul explorator este opt-in (`-Datf.ui.enabled=true`) și rulează pe artifactul
API generat în setup, nu pe un fixture HTML.

Contractul din `ApiExtentReportUiTest` rămâne minimal pentru build-ul rapid;
contractul comportamental este în testul explorator și interacționează efectiv
cu DOM-ul prin Playwright.

Închis prin testul explorator:

- descoperirea dinamică a claselor, scenariilor și fazelor, fără număr fix sau
  nume hardcodate;
- reconcilierea count-urilor dintre `Tests` și `Overview`;
- Category/Tags → scenarii linkuite → Tests și Fails → cauza/linkul către Tests;
- expandarea idempotentă a scenariului, `TEST BODY`, request-ului și
  disclosure-urilor Request/Response/Execution;
- Request JSON și Response JSON condiționale: body-ul este verificat dacă
  există, nu este inventat pentru request-uri fără body;
- validări agregate în raportul UI, pași `GIVEN/WHEN/THEN`, capturi Base64
  inline și raport UI separat.

Adaugă o verificare componentă pentru artifactul autonom, fără să schimbi
formatul vizual: navigare `Overview → Tests → Category → Fails`, filtre de
status/tag, hash deep-links, expand/collapse, `Open in Tests`, count-uri,
durate cu unitatea comună (`ms` până la `300 ms`, apoi `s`), status/durată centrate, timestamp absent din tabelul Tests și
Request/Validation ca frați direcți.

Dovada finală trebuie să combine testul determinist cu verificarea manuală în
TAB MCP pe artifactul generat din staging. Pentru închiderea completă a F-05 mai
rămâne doar extinderea contractului cu filtrele de status/tag ca matrice
data-driven, computed styles pentru badge-uri și absența timestamp-ului din
tabelul Tests; acestea sunt cerințe de prezentare urmărite separat în D-01–D-04.

### F-06 — Redu hotspot-urile reale de coeziune

Regula de maximum 8 clase de producție per package și verificarea allow-list-ului
sunt deja implementate în `ArchitectureBoundaryTest`. Rămâne refactorizarea
hotspot-urilor live, fără package-uri artificiale:

| Package | Clase | Problemă dominantă |
|---|---:|---|
| `atf.api.reporting` | 50 | theme, runtime, Extent, fallback, artifact contracts și staging în aceeași zonă |
| `atf.api.core.http` | 26 | model request, transport, executor, retry, deadline și telemetry împreună |
| `atf.api.core.reporting` | 9 | porturi, statusuri, assertions și console contract la aceeași limită |

Nu crea package-uri doar pentru a satisface numărul. Extrage numai când există
un motiv distinct de schimbare și un boundary verificabil:

- reporting: `theme/runtime`, `semantic model`, `Extent adapter`, `fallback`,
  `artifact inspection` și `workspace publication`;
- core HTTP: `request/response model`, `transport`, `retry/deadline` și
  `execution telemetry`;
- core reporting: păstrează porturile minime și mută contractele care nu sunt
  necesare tuturor consumatorilor.

Test de închidere pentru partea rămasă: fiecare extragere trebuie să păstreze
import/POM forbidden, static mutable state, package inventory, allow-list stale/
new hotspot și o fixture invalidă intenționat.

### F-07 — Package Java real în overview și gate global de coeziune

Overview-ul `Test packages` afișează în prezent artifactId-ul Maven
`atf-tests`, deoarece `ExtentReportRuntimeBehaviour` construiește tabelul cu
`packages['atf-tests']`. Aceasta nu este informație despre package-ul care
deține testul și nu trebuie păstrată ca valoare implicită.

Raportarea trebuie să primească explicit package-ul Java real al testului sau
scenariului și să afișeze acel package în `Test packages`, cu count și share
calculate pe scenarii. Pentru un raport cu Users și Posts pot apărea mai multe
package-uri reale; rândul generic `atf-tests` nu este acceptat. Metadata-ul de
environment trebuie să evite aceeași valoare hardcodată și să rămână coerent cu
overview-ul.

Regula de maximum 8 clase se aplică tuturor package-urilor de producție,
inclusiv `atf-tests/src/main/java`. În prezent `ArchitectureBoundaryTest`
inventariază modulele framework-ului, dar nu include explicit acest source
root. Extinde inventory-ul și allow-list-ul astfel încât orice depășire nouă,
inclusiv în `atf-tests`, să ceară justificare semantică.

Teste de închidere:

- raport direct `UsersApiTests` cu package-ul Java real, fără `atf-tests`;
- raport agregat Users + Posts cu package-uri, count-uri și share-uri corecte;
- test pentru metadata de environment și overview fără valori divergente;
- inventory de package-uri care include `atf-tests/src/main/java`;
- fixture invalidă care dovedește că o depășire de 8 clase eșuează fără
  excepție explicită.

## Reporting — ToDo-uri active

### D-01 — Durată numerică și sursă explicită

**Status: `PARTIAL`.** Runtime-ul adaugă markerii pentru mai multe badge-uri și
marchează agregarea clasei, dar nu există încă dovadă că fiecare nod din
`Tests`, `Category` și `Fails` primește aceeași valoare numerică și sursă.

Separarea dintre `Request details`, `Response details` și `Execution evidence`
este implementată. Rămâne de standardizat pentru fiecare nod
`data-duration-ms` și `data-duration-source` (`measured`, `exclusive` sau
`aggregated`), inclusiv în `Fails`, `Category` și header-ul clasei.

Teste: `0`, `300 ms`, `301 ms`, `999 ms`, `1 s`, peste o secundă, durată
lipsă/malformed/zero și sursă corectă după filtrare. Regula stabilă este:
`<=300 ms` se afișează în `ms`, iar `>300 ms` în secunde cu precizie de
milisecunde, identic în consolă și în toate tab-urile raportului.

### D-02 — Agregare structurală fără dublare

**Status: `PARTIAL`.** Agregarea directă a scenariilor pentru header-ul clasei
există; cazul general cu filtre, frunze și toate proiecțiile nu este încă
demonstrat.

Agregarea trebuie să folosească numai copiii structurali direcți și să evite
adăugarea unei durate vendor deja inclusive. Durata clasei din stânga trebuie
să fie egală cu suma scenariilor directe vizibile în dreapta.

Teste: părinte cu/fără durată proprie, nested phase, scenarii ascunse de filtru
și recalculare după schimbarea tab-ului.

### D-03 — Durată absentă și frunze

**Status: `PARTIAL`.** Formatterul numeric comun este închis; semantics pentru
durata absentă nu este încă uniformă pe toate suprafețele.

Request, validation, phase și lifecycle trebuie să distingă durata de count-ul
de assertions/noduri. Fără valoare numerică se afișează `—`, nu `0`, count sau
timestamp; unitatea `ms` apare numai pentru valori valide.

Auditul codului live arată că runtime-ul Fails folosește încă `No response
duration recorded`; prin urmare pragul `300/301 ms` este închis, dar contractul
pentru lipsă/malformed nu este încă închis.

Teste: request fără response, teardown fără response, zero, malformed și lipsă
în `Tests` și `Fails`.

### D-04 — Un singur token de geometrie pentru status

Tokenurile comune de badge există și sunt testate, dar `AtfReportTheme` conține
încă reguli duplicate și override-uri pentru annotations/tag-uri. Consolidează
status badge, duration metadata și tag annotations în tokenuri distincte,
reutilizate în `Tests`, `Category`, `Fails`, `Overview` și detail.

Teste: CSS contract pentru toate containerele și verificare DOM/computed style
pentru height, padding, radius, centrare și separarea statusului de durată.

## Criteriu de închidere

Un ToDo dispare numai când există owner clar, schimbare minimă, test
determinist, test component când este necesar, build în staging separat și
dovadă observabilă în artifact verificat în TAB MCP. Niciun test existent nu
se elimină sau relaxează pentru a face build-ul verde.

## Documente normative

- [ATF_UI_Exploratory_Feedback.md](ATF_UI_Exploratory_Feedback.md)
- [ATF_Architecture.md](../ATF_Architecture.md)
- [ATF_Reporting_Contract.md](../ATF_Reporting_Contract.md)
- [ATF_BUILD_AND_EXECUTION_SAFETY.md](../ATF_BUILD_AND_EXECUTION_SAFETY.md)
- [atf-reporting/README.md](../../atf-reporting/README.md)

## Revalidare live UI Categories — 2026-10-15 18:03

Această intrare supersedează orice observație mai veche despre tabelul de acțiuni
care descria `min-width:0`/`table-layout:auto`. Pe codul live, auditul unui raport
API existent a trecut `1/1` test și a reconciliat dinamic `29/29` filtre de
categorii. Artifactul rezultat este:

`artifacts/reports/ui/existing-ui-categories-atf-extent-report_2026-10-15_18-03-47-397.html`

Contractele observate și verificate în TAB MCP:

- 30 tabele `Playwright action sequence` cu operație, target, expected,
  observed, timp și rezultat;
- 30 capturi ca ancore inline `data:image/png;base64,...`, clicabile cu badge-ul
  `base64 img`, `target=_blank`, `rel=noopener` și `data-featherlight="image"`,
  zero `<img>` duplicat și zero PNG extern;
- tabel cu `min-width:760px`, `table-layout:fixed`, coloane
  `42/125/160/145/150/68/70px` și wrapper `overflow-x:auto`; antetele nu se
  mai fragmentează vertical;
- pași Gherkin parametrizați cu numele filtrului și count-ul declarat, de
  exemplu `Category "contract" reconciles declared tests=6 with rendered links`;
- CSS și renderer apărate de testele `AtfReportEvidenceCssTest` și
  `ExtentReportUiEvidenceTest`.

Un click pe linkul Base64 din TAB MCP a confirmat că ancora conține datele
native ale imaginii și atributele corecte. Dacă un backend MCP nu expune popup-ul
`data:` ca tab separat, acesta este un comportament al harness-ului de browser,
nu o lipsă a artifactului; contractul HTML rămâne verificabil prin `href`,
`target` și `rel`.
