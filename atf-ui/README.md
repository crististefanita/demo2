# ATF UI

`atf-ui` este modulul de infrastructură UI al ATF Java. Prima integrare are un
singur adaptor browser: Playwright. Testele efective nu sunt aici; ele rămân în
pachetul `atf.ui.tests` din modulul existent `atf-tests`.

```text
atf.ui.tests
    → atf-ui: UiConfiguration + PlaywrightUiSession
        → Playwright browser/context/page
            ├──→ API report HTML existent, read-only input
            └──→ UI Extent report separat, generat de atf-reporting
```

Responsabilitățile sunt delimitate astfel:

| Zonă | Owner |
|---|---|
| browser, context, timeout, viewport | `atf-ui` |
| page objects și workflow-uri de produs | `atf-tests` / pachete viitoare de produs |
| verdict, evidence și HTML Extent | `atf-reporting` (`UiExtentReportSession` pentru UI) |
| scenarii API | `atf-tests` / `atf.api.tests` |

Nu se scrie HTML în `atf-ui`, nu se apelează Extent din page objects și nu se
creează un modul separat pentru testele UI. `PlaywrightUiSession` este scoped,
deține un singur browser context și închide resursele idempotent.

`atf-ui` controlează browserul; nu deține raportul. Un test UI poate deschide un
raport API existent ca input read-only și poate publica pașii, validările și
capturile Base64 ca linkuri media într-un raport UI separat prin
`atf-reporting.UiExtentReportSession`. Capturile nu sunt fișiere externe.

## Screen validation și auditarea unui raport existent

`UiScreenValidation.capture(page, configuration, title)` este singurul punct
care transformă starea vizuală Playwright în Base64. Nu scrie PNG-uri externe.
Testul colectează toate assert-urile care descriu aceeași pagină, apoi apelează
`UiExtentReportSession.UiPhase.screenValidation(...)`. Rendererul publică o
singură zonă colapsabilă pentru acea stare: link Base64 către captură, detalii despre
acțiune și tabelul agregat de validări. O navigare sau o schimbare vizuală începe
un nou grup; nu se atașează aceeași imagine la fiecare assert.

### Contractul evidence: payload Base64, nu imagine HTML duplicată

`UiScreenValidation` produce un payload PNG Base64. Publisher-ul din
`atf-reporting` îl expune ca link nativ `data:image/png;base64,...`, cu
`target="_blank"` și `rel="noopener"`, folosind eticheta explicită
`Open screenshot · Base64 image`. Nu se emite `<img>` duplicat, nu se scrie un
PNG extern și nu se folosește `data-featherlight`; click-ul deschide direct
payload-ul Base64 într-un tab nou.

Metoda existentă `Evidence.hasInlineImage()` este denumită istoric și poate
induce o interpretare greșită: „inline” descrie payload-ul Base64, nu un element
HTML `<img>`. Ea trebuie redenumită/documentată ca `hasScreenshotPayload()` într-o
schimbare compatibilă, acoperită de test. Până atunci, README-urile și testele
folosesc termenul „Base64 screenshot payload/link”, nu „inline image”.

### Secvența auditabilă Playwright

`PlaywrightActionSequence` este recorderul neutru al acțiunilor efective. Testul
execută operația prin Playwright și păstrează aceeași operație, în ordinea reală,
în grupul vizual curent. Sunt suportate explicit `Navigate`, `Click`, `Get text`,
`Find element`, `Expect text`, `Focus`, `Wait for visible`, `Expect visible`,
`Wait` și observații de audit. O verificare UI nu se face printr-un assert Java
separat care dispare din artifact: lookup-ul, citirea și așteptarea sunt executate
prin aceste operații Playwright și devin rânduri auditabile.
Fiecare rând păstrează locatorul/target-ul, rezultatul așteptat, rezultatul
observat, durata în milisecunde și statusul canonic `Pass`/`Warning`/`Fail`/`Skip`.

La publicare, acțiunile consecutive sunt randate într-un singur disclosure
`Playwright action sequence (N entries)`, cu tabelul:

```text
# | OPERATION | TARGET | EXPECTED | OBSERVED | TIME | RESULT
```

Coloanele au lățimi contractuale pentru a păstra lizibile operația, locatorul,
expected și observed; tabelul are `min-width: 760px` și `table-layout: fixed`.
La viewport îngust, wrapperul permite scroll orizontal controlat, iar textul
lung se împachetează în celula lui fără să transforme antetul în litere
verticale. Rezultatul
este redat ca marker emoji (`✅`, `⚠️`, `❌`, `⏭️`, `ℹ️`), cu `aria-label` și
`title` pentru accesibilitate; nu se afișează `passed` sau textul verdictului în
celula tabelului.

`atf-reporting` este singurul owner al HTML-ului și al CSS-ului tabelului; codul
Playwright doar execută și înregistrează. `drain()` închide grupul curent înainte
de o nouă stare vizuală, iar captura Base64 și assert-urile acelei stări rămân
în aceeași zonă auditabilă. Când există acțiuni, captura este atașată natural
imediat sub tabel, în același disclosure și în aceeași propoziție Gherkin; nu
devine un nod sau un tabel separat. Nu se acceptă textul `passed` în rezultatul
acestei secțiuni și nu se creează imagini externe.

`AtfAssertion` nu înlocuiește aceste operații. El este rezumatul semantic al
validării (`EXPECTED`/`ACTUAL`/`RESULT`); dovada modului în care UI-ul a fost
inspectat trebuie să rămână în secvența Playwright, cu locatorul real și
rezultatul observat.

### Contract strict pentru `TARGET` / locator

`TARGET` este o coloană obligatorie de audit pentru fiecare rând `ACTION`. Nu
este suficient să existe o descriere care spune ce intenționa testul să facă:
valoarea trebuie să fie locatorul efectiv folosit de obiectul Playwright din
aceeași operație. Regula se aplică tuturor operațiilor element-bound, inclusiv
`Click`, `Get text`, `Find element`, `Expect text`, `Focus`, `Wait for visible`
și `Expect visible`.

Preferința de proiect pentru locatorii CSS, în ordinea stabilității, este:

```text
#stable-id
element[data-testid='stable-contract']
button[aria-label='Open filters']
a[href='#test-view']
.scoped-container .item:nth-of-type(2)   // numai când nu există atribut stabil
```

Un selector trebuie să descrie exact `Locator`-ul executat, să fie suficient de
scoped pentru a nu depinde de poziția accidentală în DOM și să folosească
atribute relevante (`data-testid`, `data-filter-value`, `aria-label`, `href`,
`role` sau alte atribute contractuale). Descrieri precum `"selected owner
detail"`, `"TEST BODY collapse node"`, `"details disclosure summary"` sau
`"tree node"` nu sunt locatori și nu sunt acceptate în `TARGET`. Ele pot apărea
doar în `EXPECTED`/`OBSERVED`, unde explică rezultatul, niciodată în coloana
dedicată locatorului.

Exemplu corect:

```java
Locator filter = page.locator("button[data-filter-value='clear']");
actions.click(filter, "button[data-filter-value='clear']", "filters are cleared");
actions.waitForVisible(page.locator("#category-view .test-list"),
        "#category-view .test-list");
```

`Navigate` este singura acțiune al cărei `TARGET` este în mod natural URI-ul
destinației, nu un selector CSS. Un timeout de stabilizare trebuie să primească
și să publice locatorul-anchor al stării observate; `TARGET=page` nu mai este
acceptat pentru secvențele Playwright auditabile. Chiar și cu un anchor, un
timeout fix trebuie înlocuit preferențial cu `Wait for visible` sau o altă
condiție semantică pe locator.

Contractul de publicare este:

```text
# | OPERATION | TARGET (locator/URI) | EXPECTED | OBSERVED | TIME | RESULT
```

Rendererul nu inventează selectorul și nu transformă o descriere prose într-un
locator. Testele trebuie să eșueze înainte de publicare dacă un target de
acțiune este gol, generic sau nu corespunde locatorului executat. Orice
refactorizare care schimbă selectorul trebuie să actualizeze împreună codul,
testul și dovada din raport; un raport „frumos” cu `TARGET` prose nu este
auditabil.

### Ce trebuie să fie auditabil și ce nu trebuie mimat

`Click`, `Get text`, `Focus`, `Wait for visible` și `Expect visible` sunt acțiuni
executate de Playwright și devin rânduri reale. `Tab visible`, `Screenshot` și
alte constatări post-acțiune sunt observații; ele nu trebuie prezentate ca și
cum ar fi operații de browser. Dacă rămân în aceeași secvență pentru lizibilitate,
modelul trebuie să le marcheze explicit ca `OBSERVE`, păstrând `ACTION` pentru
operațiile Playwright. Un verdict fără locator, expected și observed nu este
dovadă auditabilă.

`PlaywrightUiSession` modelează în prezent un singur browser/context/page.
Auditarea unui link care deschide o fereastră nouă sau a mai multor pagini cere
un contract explicit de popup/page lifecycle; nu se presupune că `target=_blank`
înseamnă că sesiunea a validat automat conținutul noii pagini.

### Regula strictă pentru auditul vizual

`Screen validation` nu înseamnă doar că s-au executat click-uri. Este pasul
Gherkin care leagă acțiunile, starea observată și assertions. Într-un audit cu
`ui.screenValidation.enabled=true`, orice tabel `Playwright action sequence`
publicat în artifactul UI trebuie să aibă exact o captură Base64 pentru aceeași
stare, afișată imediat sub tabel. Un pas tehnic explicit non-vizual poate exista
fără imagine, dar nu trebuie să emită tabel Playwright. Un tabel fără imagine nu
este acceptat în raportul UI auditabil; politica de captură dezactivată produce
doar audit funcțional, marcat ca fără dovadă vizuală completă.

```text
acțiune Playwright → observație/assertion → screenshot al aceleiași stări
                  → un singur pas Gherkin auditabil
```

O excepție aruncată între ultima acțiune și `screenValidation(...)` nu trebuie
să piardă rândurile deja înregistrate. Failure boundary-ul trebuie să le
publice cu status `Fail`, fără să inventeze o captură imposibilă. Garanția de
cardinalitate a capturii este închisă prin F-08 și testul negativ din
`atf-reporting`; păstrarea acțiunilor pe excepție rămâne F-09 deschis. Ambele
contracte sunt urmărite în `docs/feedback/ATF_UI_Exploratory_Feedback.md`.

### Limita responsabilității modulului

`atf-ui` deține browserul, timing-ul și modelul neutru al acțiunilor; nu devine
un al doilea renderer sau un al doilea orchestrator de raport. În același timp,
testele UI din `atf-tests` nu trebuie să conțină clase de 400–500 de linii care
reimplementează navigatorul, snapshots și publication lifecycle. Logica
reutilizabilă se extrage în `atf.ui.tests.support`, iar clasele de test rămân
succesiuni lizibile `GIVEN → WHEN → THEN`.

Pentru auditarea unui raport deja generat, fără rerularea API-ului:

```text
mvn -pl atf-tests -am `
  '-Datf.ui.enabled=true' `
  '-Datf.ui.report=artifacts/reports/ui/ui-atf-extent-report-TIMESTAMP.html' `
  '-Dtest=atf.ui.tests.exploratory.ExistingExtentReportExploratoryTest' `
  '-Dsurefire.failIfNoSpecifiedTests=false' test
```

`ExistingExtentReportExploratoryTest` servește inputul read-only prin
`atf-debug-ui`, explorează Overview, Tests, Category și Fails prin click-uri
Playwright și publică un artifact separat cu prefixul
`existing-ui-atf-extent-report_`. Numărul de clase, scenarii, faze, validări,
tag-uri și eventualele request-uri este descoperit din DOM; testul nu presupune
structura unui raport API atunci când inputul este deja un raport UI.

Inputul poate fi declarat repetabil în YAML:

```yaml
ui:
  audit:
    inputReport: artifacts/reports/api-atf-extent-report.html
```

Rezoluția are o singură precedență explicită:

```text
-Datf.ui.report=<path>  >  ui.audit.inputReport  >  niciun input
```

Calea relativă din YAML este rezolvată față de directorul lui `atf.yaml`.
Override-ul de proces poate fi absolut sau relativ față de working directory și
este util pentru o investigație manuală punctuală. Dacă nu există input, testul
de audit este dezactivat/respins înainte de pornirea browserului; nu se creează
un fișier implicit care ar putea ascunde o configurare greșită.

## Configurație

Configurația generală stă în `atf.yaml`, sub `ui`. Este opt-in pentru a păstra
build-ul API offline și poate fi suprascrisă cu `-Datf.ui.enabled=true`.

```yaml
ui:
  enabled: false
  browser: chromium
  baseUrl: about:blank
  headless: true
  timeout: PT10S
  navigationTimeout: PT15S
  viewport:
    width: 1440
    height: 900
  screenshotOnFailure: true
  screenValidation:
    # One visual state -> one Base64 media link + one grouped assertion table.
    # The disclosure is rendered closed; the link is not an external file.
    enabled: true
    fullPage: false
  apiSetup:
    # Ordered API families used to build the read-only input report.
    scenarioFamilies: [posts, users, requests, headers, lifecycle]
  audit:
    # Existing report consumed read-only by ExistingExtentReportExploratoryTest.
    inputReport: artifacts/reports/api-atf-extent-report.html
  report:
    directory: artifacts/reports/ui
    filePrefix: ui-atf-extent-report_
```

Loaderul caută `atf.yaml`/`atf.yml` din working directory către părinți sau
primește un fișier explicit prin `-Datf.ui.config=...` ori `ATF_UI_CONFIG`.
Directorul raportului este relativ la YAML și nu poate ieși din directorul
configurației.

### Boundary-ul configurației

Configurația UI este încărcată în două responsabilități distincte:

- `AtfUiConfigurationLoader` deține doar inputurile de proces: discovery,
  fișier explicit și override-ul `atf.ui.enabled`;
- `UiConfigurationYamlReader` este intern modulului și deține parsarea secțiunii
  `ui`, conversia tipurilor, valorile implicite și verificarea că directorul
  raportului rămâne sub rădăcina fișierului YAML;
- `UiConfiguration` este snapshot-ul immutable consumat de Playwright și de
  publisher-ul raportului.
- `ui.audit.inputReport` este inputul declarativ pentru auditarea unui artifact
  deja publicat; `atf.ui.report` este override-ul de execuție și are prioritate.

Reader-ul nu citește proprietăți de sistem și nu pornește browserul, iar loaderul
nu construiește manual obiecte din fiecare câmp YAML. Astfel discovery-ul,
precedence-ul și path security pot evolua separat, fără să amestece configurația
cu execuția Playwright sau cu raportarea Extent.

Pentru prima execuție Playwright, browserul trebuie instalat în mediul local
conform versiunii dependinței Maven. Testul UI rămâne separat de testele
unitare: validarea configurației nu pornește browserul, iar browserul este
folosit numai de testele opt-in din `atf.ui.tests`, inclusiv
`ApiExtentReportUiTest` și `atf.ui.tests.exploratory.ApiExtentReportExploratoryTest`.

`ui.apiSetup.scenarioFamilies` este compoziția inputului API, nu o listă de
teste UI. Familiile sunt publicate în ordinea configurată, iar testul
explorator descoperă clasele, scenariile și fazele din HTML; nu presupune un
număr fix sau nume hardcodate. `@ApiScenarioSetup` poate furniza o ordine locală
pentru o clasă de explorare; când este gol, ordinea YAML este sursa unică.

### Context comun pentru audituri

`atf-tests` folosește `ReportAuditContext` pentru ambele variante:

```text
API setup → apiInputReport → ReportAuditContext
existing path             → ReportAuditContext
                                  ├─ ReportArtifactInspector
                                  ├─ atf-debug-ui read-only
                                  ├─ Playwright browser
                                  └─ UiExtentReportSession (audit output)
```

Contextul centralizează verificarea HTML-ului, pornirea/închiderea serverului
și browserului, navigarea pe tab-uri și publicarea artifactului de audit.
Primitivele de assertion/status/normalizare numerică sunt comune, iar
validările dinamice rămân descrise în testele Gherkin care cunosc contractul
tab-ului verificat. Astfel raportul existent și raportul generat live folosesc
aceeași infrastructură fără să amestece setup-ul API cu explorarea UI.
