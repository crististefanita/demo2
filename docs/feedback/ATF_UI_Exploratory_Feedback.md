# ATF Java — feedback UI explorator

Documentul urmărește numai gap-urile rămase în atf.ui.tests, atf-ui și
atf-reporting. Contractele vizuale stabile sunt în
atf-reporting/README.md.

## Contract demonstrat

- Inputul existent este read-only și poate veni din -Datf.ui.report sau
  ui.audit.inputReport.
- ReportAuditContext deține servirea loopback, browserul Playwright și sesiunea
  de output UI.
- Testele descoperă clasele, scenariile și fazele din DOM; nu folosesc numere
  hardcodate.
- Screen validation păstrează Gherkin, acțiunile, assertions și captura Base64
  în aceeași zonă.
- Request details, Response details și Execution evidence sunt disclosures
  distincte.
- Playwright action sequence păstrează locator, expected, observed, timp în
  ms și markerul emoji; captura rămâne imediat sub tabel.
- Linkul de captură este nativ data:image/png;base64,..., cu target="_blank"
  și rel="noopener"; nu există fișier PNG extern sau img duplicat.
- Tests verifică toate owner-ele, scenariile și fazele descoperite.
- Categories verifică starea zero/one/many; Fails verifică empty-state sau
  relația failure → cauză → Open in Tests.

## ToDo-uri active

| ID | Prioritate | Observație verificată în cod | Următorul pas |
|---|---:|---|---|
| F-09 | P0 | failure handling este repetat în clasele exploratorii | extrage un failure boundary comun și adaugă test negativ cu flush garantat |
| F-10 | P1 | navigatorul și validatorii DOM sunt încă prea mult în clasele de test | extrage checks/lifecycle fără a ascunde pașii Gherkin |
| F-12 | P0 | observation() stă în aceeași listă cu acțiunile reale | publică sursa ACTION/OBSERVE în tabel |
| F-13 | P1 | inputul are precedență stabilă, dar output prefix-ul nu este complet configurabil | elimină numele de output hardcodat |
| UI-15 | P1 | ramura zero există, dar nu are fixture dedicat în matricea live | adaugă test zero/one/many și empty-state |
| D-04 | P2 | tokenurile de badge există, dar trebuie eliminat orice override concurent | verifică computed-style în toate taburile și la viewport mic |
| LOC-01 | P1 | contractul respinge blank/null și normalizează selectorul; producerul generic încă poate construi prose nevid | păstrează testul negativ pentru blank și adaugă validare semantică doar dacă producerul primește un locator real, fără a ghici selectorul în renderer |
| LOC-02 | P1 | explorările live publică selectorii CSS efectivi pentru toate rândurile `ACTION`; observațiile `Screenshot/current page` sunt marcate separat ca `OBSERVE` | menține ratchet-ul în orice test nou și nu promova observațiile în acțiuni Playwright |
| LOC-03 | P2 | timeout-urile înregistrate folosesc locator-anchor; unele așteptări de explorare rămân candidate pentru state wait semantic | înlocuiește incremental delay-ul direct cu state wait pe locator și păstrează target-ul anchor în evidence |

Punctele F-11, UI-14 și UI-16 sunt închise și nu se redeschid fără o
regresie nouă demonstrată.

### Ratchet aplicat — 2026-10-16

API-ul `PlaywrightActionSequence` validează la intrare că fiecare operație
element-bound primește un target nevid; testul unit acoperă `null`, blank și
normalizarea unui selector CSS (`button[data-filter-value='clear']`). Artifactul
UI live din 2026-10-16 reconfirmă 252 de rânduri `ACTION`, fără target gol și
fără target prose. `Screenshot/current page` apare numai în tabelul separat de
observații, conform contractului `ACTION/OBSERVE`; nu este un locator lipsă.

## Ordinea recomandată

    F-09 → LOC-01 → LOC-02 → F-12 → UI-15 → F-10 → F-13 → D-04

## Gate UI

Un ToDo UI nu se închide doar după testele Java. Sunt obligatorii:

1. cod live sincronizat înainte de build;
2. test de regresie verde;
3. raport nou, nu artifact vechi;
4. click-uri reale în TAB MCP pe tabul afectat;
5. verificare expand/collapse, navigare și computed styles;
6. confirmare că screenshot-ul este sub tabelul acțiunilor și este clicabil.

## Ultima dovadă

    input:
      artifacts/reports/api-atf-extent-report_2026-10-16_09-08-16-654.html
    audit:
      artifacts/reports/ui/ui-atf-extent-report_2026-10-16_09-08-21-736.html

Rezultat: ExistingExtentReportExploratoryTest 4/4, Categories 1/1, fără
failure sau error. Verificarea vizuală a fost făcută în TAB MCP pe Overview,
Tests, Category, Fails, scenario, faze și disclosures.

### Dovadă live după corecția rendererului — 2026-10-16

- reporting contract: 14/14 teste verzi;
- `ApiExtentReportExploratoryTest`: 5/5 teste UI verzi;
- artifact verificat: `artifacts/reports/ui/ui-atf-extent-report_2026-10-16_09-08-21-736.html`;
- 15 linkuri Base64 native, toate cu `target="_blank"`, `rel="noopener"` și prefix
  `data:image/png;base64,`;
- eticheta legacy `📷 base64 img` și atributul `data-featherlight` nu mai apar;
- action-table: `width=760px`, `min-width=760px`, `table-layout=fixed`, șapte coloane;
- click-urile MCP au acoperit Overview → Tests → scenariu → TEST BODY → THEN →
  Screen validation → Playwright action sequence, apoi Category și Fails;
- console errors: 0.

Concluzie: regresia de randare era în cei trei rendereri și în CSS-ul comun,
nu în inputul raportului. Screenshots rămân linkuri Base64 clicabile, fără
fișiere externe și fără `<img>` duplicat. Orice nouă modificare de renderer
trebuie să repete același gate: testele de reporting, testul UI și click-uri
reale în TAB MCP.
