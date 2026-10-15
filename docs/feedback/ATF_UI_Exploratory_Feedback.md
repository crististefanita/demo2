# ATF Java — feedback UI explorator și raport auditabil

Acest document este registrul punctual pentru integrarea Playwright din
`atf.ui.tests`. Se verifică pe codul live și pe artifactele produse de ultima
rulare; raportul API este input read-only, iar raportul UI este dovada separată
a explorării.

## Contract implementat

- [x] `atf.yaml` configurează ordinea familiilor API prin
      `ui.apiSetup.scenarioFamilies`.
- [x] `@ApiScenarioSetup` permite o ordine locală; valoarea goală deleagă
      ordinea către YAML.
- [x] setup-ul construiește raportul API prin `ApiScenarioSetupCatalog`, îl
      publică read-only prin `atf-debug-ui` și nu amestecă raportul de input cu
      raportul UI.
- [x] `UiExtentReportSession` scrie separat în `artifacts/reports/ui/`.
- [x] testul descoperă din DOM clasele, scenariile și fazele; nu presupune un
      număr fix și nu depinde de numele claselor API.
- [x] Overview este verificat pentru secțiunile caracteristice și reconciliat
      cu totalurile descoperite în Tests.
- [x] Category/Tags verifică stilul suprafeței, categoriile și linkurile către
      scenarii; Fails verifică cauze și navigarea către Tests.
- [x] Tests verifică expandarea idempotentă a scenariului, `TEST BODY` și a
      request-ului.
- [x] `ApiExtentReportExploratoryTest.everyTestsNodeExposesReconciledDynamicDuration`
      selectează fiecare owner al raportului API generat dinamic și verifică
      toate cardurile structurale din `Tests`: `data-duration-ms` numeric,
      badge sincronizat și formatat (`ms` / `s`), sursa de agregare și suma
      bottom-up a copiilor/acțiunilor directe fără dublare.
- [x] Request details, Response details și Execution evidence sunt disclosures
      distincte. Request/Response JSON este verificat numai când body-ul există.
- [x] fiecare audit vizual păstrează pași `GIVEN/WHEN/THEN`, aserțiuni agregate,
       status și link către captură Playwright Base64 în raportul UI; pașii tehnici
      non-vizuali pot fi publicați fără imagine și nu sunt declarați audit
      vizual complet.
- [x] `ui.screenValidation` face capturile configurabile (`enabled`, `fullPage`)
      și grupează toate assert-urile aceleiași stări vizuale sub o singură zonă
      colapsabilă; nu se scriu capturi PNG externe.
- [x] `PlaywrightActionSequence` execută și înregistrează acțiunile reale,
      consecutive, cu locator/target, expected, observed, timp în ms și marker
      emoji pentru status; rendererul le publică într-un singur tabel colapsabil per
      stare vizuală (`Navigate`, `Click`, `Get text`, `Focus`, wait/assert).
- [x] tabelul de acțiuni este randat de `atf-reporting`, nu de testul UI; celula
      `RESULT` afișează doar emoji-ul, iar `Pass`/`passed` rămâne exclusiv
      metadată accesibilă, nu text vizibil în tabel; nu există imagini externe.
- [x] validarea UI și captura sunt subpasul propoziției Gherkin (`GIVEN`/`WHEN`/
      `THEN`), niciodată nod frate al acesteia; captura acțiunilor este afișată
      natural imediat sub tabelul Playwright, în același disclosure.
- [x] `ExistingExtentReportExploratoryTest` primește un raport Extent existent
      prin `-Datf.ui.report=...`, fără să reruleze testele API, și verifică
      dinamic Overview → Tests → Category → Fails.
- [x] raportul final nu folosește imagini externe; ultima verificare TAB MCP a
      avut zero erori de consolă.

## Ultima dovadă live

Raportul de referință inspectat în debug UI/TAB MCP este ultima intrare UI:

```text
artifacts/reports/ui/ui-atf-extent-report_2026-10-15_15-57-51-979.html
```

Observația structurală este importantă: inputul este un raport UI, deci are
scenarii → faze → validări, iar nodurile API `request` pot lipsi. Auditul nou
tratează request/response ca extensie condițională, nu ca premisă hardcodată.

Comanda pentru auditul read-only al acestui artifact este:

```text
mvn -pl atf-tests -am -Datf.ui.enabled=true \
  -Datf.ui.report="artifacts/reports/ui/ui-atf-extent-report_2026-10-15_15-57-51-979.html" \
  -Dtest=atf.ui.tests.exploratory.ExistingExtentReportExploratoryTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Testul generează separat `artifacts/reports/ui/existing-ui-atf-extent-report_...html`.

Comanda verificată:

```text
mvn -pl atf-tests -am -Datf.ui.enabled=true \
  -Dtest=atf.ui.tests.exploratory.ApiExtentReportExploratoryTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Rezultatul de bază anterior: `4/4` teste UI, `0` failure, `0` error. Auditul
de durate este acum o a cincea verificare din aceeași clasă și rulează împotriva
artifactului API fresh, nu împotriva unui fixture.

### Dovadă live după auditul duratelor — 2026-10-15 16:57

Rularea finală a trecut `5/5` teste în `ApiExtentReportExploratoryTest`, cu
`0` failure și `0` error. Raportul API a fost generat dinamic din familiile
configurate `[posts, users, requests, headers, lifecycle]`, apoi servit de
`atf-debug-ui` pe loopback și inspectat manual în TAB MCP. Validarea de durate
a acoperit `5` owneri, `80` noduri structurale, `80/80` markeri numerici,
`80/80` badge-uri formatate, `59` noduri agregate și `0` violări de sumă
parent–copii.

Artifactele de referință sunt:

```text
artifacts/reports/api-atf-extent-report_2026-10-15_16-57-07-603.html
artifacts/reports/ui/ui-atf-extent-report_2026-10-15_16-57-11-045.html
```

Debug UI-ul folosit pentru verificarea manuală a fost pornit cu
`atf.api.debugui.AtfDebugUiLauncher --html ... --port 0`; în TAB MCP au fost
verificate consecutiv `Tests`, `Category`, `Fails` și `Overview`, plus
expandarea unui scenario în `Tests`. În mirror-ul live au fost copiate aceleași
surse, documentații și artifacte, cu hash-uri SHA-256 identice.

Artifactele ultimei rulări sunt relative la root-ul proiectului:

```text
artifacts/reports/api-atf-extent-report_2026-10-15_15-57-48-614.html
artifacts/reports/ui/ui-atf-extent-report_2026-10-15_15-57-51-979.html
```

Raportul UI a fost deschis prin serverul loopback al modulului `atf-debug-ui` și
verificat în TAB MCP prin click-uri reale pe tab-uri, categorii, scenarii,
faze, pași, disclosures de screenshot și disclosures de assertions.

### Dovada finală după corectarea nesting-ului — 2026-10-15 15:58

Rularea live a trecut `4/4` pentru `ApiExtentReportExploratoryTest`, apoi auditul
read-only al raportului UI a trecut `5/5`. Artifactul final verificat în TAB MCP
este:

```text
artifacts/reports/ui/existing-ui-atf-extent-report_2026-10-15_15-58-30-472.html
```

Pe owner-ul explorat, DOM-ul a confirmat `15` request-uri Gherkin și `10`
validări, cu `0` validări orfane. Au fost verificate `5` disclosures de acțiuni,
`5` tabele de acțiuni și `5` linkuri către capturi Base64; în fiecare disclosure captura este
imediat după tabel (`atf-ui-action-table`, apoi `atf-ui-action-screenshot`), nu
un nod semantic separat. Au rezultat `39` rânduri de acțiuni, toate cu status
`Pass`, `0` imagini externe și `0` erori de consolă.

Aceasta închide regresia urmărită aici: validarea UI rămâne sub propoziția
Gherkin, iar screenshot-ul rămâne natural sub tabelul acțiunilor Playwright.

### Revalidare read-only pe raport UI existent — istoric 2026-10-15 15:13

Raportul primit ca input a fost încărcat în debug UI prin Playwright/TAB MCP,
fără rerularea testelor API:

```text
artifacts/reports/ui/ui-atf-extent-report_2026-10-15_15-57-51-979.html
```

Comanda executată:

```text
mvn -pl atf-tests -am -Datf.ui.enabled=true \
  -Datf.ui.report="artifacts/reports/ui/ui-atf-extent-report_2026-10-15_15-57-51-979.html" \
  -Dtest=atf.ui.tests.exploratory.ExistingExtentReportExploratoryTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Rezultat: `5/5` teste verzi, `0` failure, `0` error. Artifactul de audit separat
este:

```text
artifacts/reports/ui/existing-ui-atf-extent-report_2026-10-15_15-58-30-472.html
```

În TAB MCP au fost executate click-uri reale pe `Overview`, `Tests`, `Category`
și `Fails`, apoi pe owner, scenariu, fază și disclosure-uri. Raportul primit
conținea un owner, cinci scenarii de audit, faze și validări descoperite din
DOM, 20 grupuri de acțiuni cu 156 de rânduri consecutive, 20 linkuri către capturi
Base64 și zero imagini externe. Auditul UI a păstrat
captura per stare vizuală și assert-urile grupate în aceeași zonă colapsabilă.
Fiecare rând de acțiune a fost verificat cu `operation`, `target`, `expected`,
`observed`, `TIME` în `ms` și markerul emoji al rezultatului; consola TAB MCP a raportat zero
erori.

Separarea verificărilor este intenționată: selectorii și secțiunile obligatorii
sunt contract static al rendererului, iar count-urile, navigarea, expandarea,
reconcilierea Overview → Tests și validarea evidence-ului sunt fluxuri dinamice
calculate din artifactul primit. Un raport UI fără noduri API nu este respins
pentru lipsa request/response evidence; acea verificare rămâne condițională.

## Audit aprofundat al zonei UI — gap-uri reale

Analiza codului live din `atf-ui`, `atf-tests` și `atf-reporting` a identificat
inițial un contract vizual prea permisiv. Gap-ul F-08 este acum închis:
`ExtentEvidenceRenderer` respinge la publicare orice secvență de acțiuni cu
zero sau mai mult de o captură Base64 validă. `Screen validation`
rămâne dovada semantică, iar asocierea locală a tabelului cu captura este
regresată separat. În aceeași secvență intră și verificările UI efective:
`Find element`, `Get text`, `Expect text` și `Expect visible`; `AtfAssertion`
rămâne doar sumarul agregat al verdictului, nu înlocuiește operația Playwright.

### De ce există cele două elemente

`Screen validation` este pasul semantic Gherkin (`THEN`) și deține verdictul
agregat al unei stări de pagină. Tabelul Playwright este evidence-ul operațional
care explică *cum* s-a ajuns în acea stare. Captura este ancora vizuală care
permite auditorului să compare imediat acțiunile și assertions cu pagina
observată. Pentru o stare vizuală completă, ordinea obligatorie este:

```text
THEN <stare verificată>
  └── Screen validation
       └── Playwright action sequence
            └── tabel acțiuni consecutive
                 └── link către screenshot Base64, imediat sub tabel
       └── tabel assertions agregate
```

În artifactul final al `atf.ui.tests`, un `action sequence` fără screenshot nu
este acceptat. Un pas explicit non-vizual (`GIVEN`/`WHEN` de setup, navigare
tehnică sau `outcome` de publicare) poate exista fără imagine, dar nu trebuie să
emită tabel Playwright. Pentru orice `Screen validation`, când
`ui.screenValidation.enabled=true`, lipsa capturii, captura goală sau un tabel
fără captura imediat următoare trebuie să fie regresie. Dacă politica de captură
este dezactivată, raportul poate rula numai ca audit funcțional și trebuie
marcat ca neavând dovadă vizuală completă.

### Găsiri și acțiuni obligatorii

| ID | Găsire live | Severitate | Acțiune de închidere |
|---|---|---:|---|
| F-08 | API-ul public permitea `Screen validation` cu acțiuni, dar fără `base64Png`; testele verificau doar că există o imagine undeva în artifact. | critică | **CLOSED.** Rendererul respinge orice secvență cu cardinalitate de capturi diferită de exact una. Acțiunile și captura sunt copii ai aceluiași nod `Screen validation`; verificările UI (`Find element`, `Get text`, `Expect text`, `Expect visible`) sunt rânduri Playwright, iar `AtfAssertion` rămâne doar sumarul agregat. Testul negativ și artifactul verificat în TAB MCP confirmă ordinea: tabel → link Base64 către captură. |
| F-09 | Pe excepție, `PlaywrightActionSequence` înregistrează acțiunea `FAIL`, dar auditul o publică doar când `screen(...)` ajunge la `drain()`. O eroare mai devreme poate pierde pașii diagnostici. | critică | Flush în failure boundary/`finally`; publică acțiunile deja executate și statusul `Fail`, cu captură numai dacă ea a putut fi obținută. |
| F-10 | `ApiExtentReportExploratoryTest` are aproximativ 505 linii, iar `ExistingExtentReportExploratoryTest` aproximativ 390; ambele repetau navigarea, capturile și publication lifecycle. | mare | **PARTIAL.** `ReportAuditContext` centralizează input inspection, serverul read-only, browserul, navigarea și output session; `ReportAuditChecks` centralizează assertion/status/number. Snapshot-urile și validatorii DOM specifici tab-ului rămân de extras ulterior, fără a ascunde diferențele reale dintre input API și input UI. |
| F-11 | Testul numit „every discovered class and phase” expandează efectiv doar primul scenariu și fazele lui. În Fails, `failureRows >= 0` și `failureLinks >= 0` sunt assertions tautologice. | mare | Iterează fiecare owner/scenario/phase descoperit; pentru Fails verifică relația failure → cause → `Open in Tests` când există failures și marchează explicit cazul empty. |
| F-12 | `PlaywrightActionSequence.observation(...)` pune observații sintetice în același tabel cu acțiunile reale. `Screenshot` și `Tab visible` nu sunt operații Playwright. | medie | Marchează observațiile explicit (`OBSERVE`/`source`) sau separă-le de acțiuni; auditul nu trebuie să confunde operația cu observația. |
| F-13 | `UiConfiguration` încărca `baseUrl`, `report.filePrefix` și `screenValidation`, dar inputul unui artifact existent era transmis manual. | mare | **PARTIAL.** `ui.audit.inputReport` și override-ul `-Datf.ui.report` sunt acum în snapshot-ul immutable, cu precedență și rezoluție relativă stabile; `ReportAuditContext` consumă snapshot-ul comun. Rămâne de eliminat numele explicit al fișierului de output din test și de conecta complet `report.filePrefix` la output-ul UI. |
| F-14 | Tabul Categories putea părea valid doar pentru primul element; nu exista dovadă că fiecare filtru reconciliază count-ul declarat, rândurile și linkurile reale către Tests. | mare | **CLOSED pentru contractul curent.** `ExistingExtentCategoriesExploratoryTest` selectează toate categoriile descoperite live, verifică titlul activ, count-ul, rândurile `tag-test-status`, `a.linked` și `test-id`, apoi urmărește un link în `Tests`. Fiecare stare publică tabel Playwright și screenshot Base64 ca link, fără imagine inline. |

Acestea sunt gap-uri observabile în cod, nu preferințe estetice. Pentru F-09–F-13,
testul trebuie să fie întâi roșu pe o fixture invalidă, apoi verde pe
implementarea corectă. Raportul rezultat se verifică în TAB MCP prin DOM și
click-uri reale; nu se închide un ToDo prin schimbarea mesajului testului.

### Dovadă de închidere F-08 — 2026-10-15 16:59

Implementarea este în `atf-reporting/src/main/java/atf/api/reporting/ExtentEvidenceRenderer.java`.
Regula este apărată de
`atf-reporting/src/test/java/atf/api/reporting/ExtentReportUiEvidenceTest.java`,
care eșuează intenționat când secvența are zero capturi și trece numai pentru
exact o imagine Base64 validă. Verificarea pe artifact nu s-a limitat la
fișier: în TAB MCP au fost deschise `Overview` și `Tests`, apoi expandate
scenariul, `TEST BODY`, propoziția Gherkin și `Playwright action sequence`.
Au fost vizibile tabelul cu 17 acțiuni consecutive, coloanele de audit și
linkul către imagine imediat sub secvență; `Find element` și `Expect text` sunt
operații reale în tabel, iar consola nu a raportat erori. Artifactul verificat
este `artifacts/reports/ui/existing-ui-atf-extent-report_2026-10-15_16-59-56-324.html`.

F-08 și F-14 sunt închise pentru contractele verificate. F-09, F-11 și F-12 rămân deschise; F-10 și F-13 sunt
parțial închise prin infrastructura comună și configurația inputului, dar nu
sunt finalizate până când validatorii DOM și output naming nu sunt complet
decuplați.

## Ce rămâne intenționat separat

Acest feedback nu redeschide lucruri demonstrate mai sus. Rămân în backlog doar
cerințele generale care nu aparțin strict fluxului explorator:

1. F-09–F-13 din auditul UI de mai sus;
2. matrice data-driven pentru filtrele de status/tag cu zero, unul și multe
   rezultate;
3. computed styles pentru badge-uri, durate, centrare și background uniform în
   toate tab-urile;
4. contractul pentru timestamp absent din tabelul Tests;
5. standardizarea duratei absente la `—` și a metadatei
   `data-duration-source` pe toate proiecțiile;
6. verificarea explicită a două sesiuni UI/API paralele, cu directoare de
   raport distincte și fără stare statică partajată.

Acestea se implementează numai prin teste care eșuează pe regresie; nu se
relaxează `ApiExtentReportExploratoryTest` și nu se transformă raportul API în
raport UI. Orice build se face în staging separat, după sincronizarea cu codul
live, iar artifactul nou se deschide în TAB MCP înainte de a marca un ToDo ca
închis.

### Verificare a codului live pentru această analiză — 2026-10-15 16:15

Suita de bază UI a trecut fără erori:

```text
mvn -pl atf-ui -am test
ATF Core: 38 teste verzi
ATF UI:   6 teste verzi
Failures: 0  |  Errors: 0
```

Această rulare confirmă fundația existentă. F-08 este închis separat prin
fixture negativă în `atf-reporting` și verificare browser-level a artifactului.
Pentru F-09, F-11 și F-12 lipsesc încă flush-ul la failure, expandarea tuturor
nodurilor și separarea observațiilor. F-10 are acum contextul comun extras, iar
F-13 are snapshot-ul de input și precedența YAML/property; rămân active doar
validatorii DOM comuni și output naming-ul complet configurabil.

## Rulare din IntelliJ

Rulează `ApiExtentReportExploratoryTest` cu VM option:

```text
-Datf.ui.enabled=true
```

Ordinea familiilor se schimbă în YAML, fără modificarea testului. Pentru o
ordine locală controlată, se folosește `@ApiScenarioSetup(families = {...})`;
testele de explorare rămân dinamice în raport indiferent de numărul de clase,
scenarii sau request-uri produse de input.

### Revalidare finală: tabel Playwright, Base64 și Gherkin parametrizat — 2026-10-15 18:03

Auditul live a corectat regresia vizuală din raportul anterior. Rendererul
folosește acum `min-width:760px` și `table-layout:fixed`, cu lățimi explicite
pentru cele șapte coloane. La viewport mai îngust tabelul rămâne lizibil și
poate fi derulat orizontal; `TARGET`, `EXPECTED` și `OBSERVED` nu mai sunt
îngustate la câte o literă pe rând.

Dovada executată pe codul live:

```text
atf-reporting: 11 teste țintite verzi, 0 failures, 0 errors
existing-report Categories audit: 1/1 test verde
category filters reconciled: 29/29
action tables: 30
Base64 screenshot links: 30
inline action-zone images: 0
legacy visible `base64 img` label: present for every screenshot anchor
parameterized category Gherkin titles: present
```

Artifactul verificat în TAB MCP este:
`artifacts/reports/ui/existing-ui-categories-atf-extent-report_2026-10-15_18-03-47-397.html`.
Prin click-uri reale au fost expandate testul, scenariul, `TEST BODY`, primul
`THEN`, `Screen validation` și `Playwright action sequence`. Computed DOM a
confirmat tabelul la `760px`, cele șapte lățimi contractuale (`42/125/160/145/
150/68/70px`), wrapperul cu `overflow-x:auto` și antetele într-un singur rând.
Ancora vizibilă conține `data:image/png;base64,...`, badge-ul `base64 img`,
`target=_blank`, `rel=noopener` și `data-featherlight="image"`; nu se emit
`<img>` duplicat sau fișiere PNG externe.

Fiecare pas Gherkin identifică filtrul și cardinalitatea verificată, de exemplu
`THEN Category "contract" reconciles declared tests=6 with rendered links`,
iar tabelul păstrează operația, locatorul, expected, observed, timpul și markerul
emoji. Contractul este apărat de `AtfReportEvidenceCssTest` și
`ExtentReportUiEvidenceTest`; verificarea finală s-a făcut în TAB MCP, fără
browser Windows.

### Revalidare: marker RESULT Playwright aliniat cu Assertions — 2026-10-15 18:22

Regresia vizuală cerută a fost închisă chirurgical: celula `RESULT` din tabelul
Playwright folosește acum același marker nativ `✅` ca tabelul Assertions.
Wrapperul semantic păstrează `aria-label="Pass"` și `title="Pass"`, dar nu mai
are cerc, fundal sau bordură proprie și nu afișează textul `Pass/passed`.

Dovada pe codul live și artefactul nou:

```text
atf-reporting focused tests: PASS
API exploratory: 5/5 PASS
existing-report audit: 4/4 PASS
artifact: artifacts/reports/ui/existing-ui-atf-extent-report_2026-10-15_18-22-20-633.html
action tables: 4
Base64 screenshot links: 4
native ✅ markers: 34
visible action-cell Pass/passed text: 0
fixed min-width 760px: absent
```

Verificarea vizuală a fost făcută prin expandarea nodurilor în TAB MCP; tabelul
Assertions și celulele RESULT Playwright folosesc aceeași reprezentare vizuală.
Regula rămâne ratchet: dacă markerul Playwright revine la badge/cerc sau la text
vizibil, testele de reporting trebuie să eșueze și se repară rendererul/CSS-ul,
nu se relaxează testele.
