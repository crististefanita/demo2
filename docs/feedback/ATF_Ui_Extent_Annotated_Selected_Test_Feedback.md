# ATF Java — feedback pentru selecția programatică a testelor API în auditul UI Extent

Status: `CLOSED — IMPLEMENTAT ȘI VALIDAT COMPLET (scope exact-selection)`  
Data: `2026-10-16`  
Scor increment: **100/100**

Acest document descrie incrementul în care un test UI Extent selectează
programatic teste punctuale din familii API diferite, fără YAML pentru selecția
concretă și fără `ui.enabled: true`.

Scorul și ToDo-urile de mai jos sunt pentru acest increment de selecție exactă,
nu pentru întregul backlog de reutilizare UI Extent. Statusul global rămâne în
[`docs/feedback/README.md`](README.md), unde sunt urmărite separat UI-15,
D-01, OPS-UI-01 și F-01.

## Reaudit profund — gate fresh 2026-10-16 22:40

În timpul revalidării codului live am găsit și am închis chirurgical o breșă
în contractul exact-selection: `ExpectedReportProjection.matches(...)` folosea
`containsAll` pentru owneri și valida doar numărul de scenarii. Astfel, un
raport cu un owner sau un titlu de scenariu suplimentar putea trece parțial.

Corecția este acum live și renderer-neutral:

- `ReportAuditSnapshot.Tests` păstrează și titlurile scenariilor observate din
  DOM, cu constructorul vechi păstrat pentru auditările de familie;
- `ApiReportStructureInspector` capturează titlul fiecărui card de scenariu
  prin locatorul centralizat, nu prin CSS în test;
- `ExpectedReportProjection.matches(...)` cere cardinalitate exactă și
  egalitate de multiset pentru owneri și titluri; fazele/request-urile rămân
  praguri minime deoarece un scenariu poate produce mai multe request-uri;
- testul unit `ExpectedReportProjectionTest` respinge explicit owner extra și
  scenario title extra.

### Dovezi executate

- build staging izolat: `.tmp/annotated-selected-reconcile-20261016-224001/`;
- unit gate: `ExpectedReportProjectionTest` + `TestsSectionValidatorTest`,
  `4/4` passed;
- UI/API gate: `SelectedApiTestsReportUiTest`, `13/13` passed, `0` failures,
  `0` errors;
- raport UI fresh: `ui-atf-extent-report_selected-api-tests-report-ui-test_2026-10-16_22-41-01-634.html`,
  SHA-256 `C5F98B6D82FF4DA81ECA3C9F1040733966F142225BE6E995944C4434361A7A16`;
- raport API fresh: `api-atf-extent-report_2026-10-16_22-40-54-971.html`,
  SHA-256 `594379AA7312BFF8307096D775C211E5BECB5CB88741B2DFF5C0AA52B6A8FE5F`.

### Repetare manuală UI → API

Raportul UI a fost deschis în Codex In-app Browser/ATF Debug UI pe loopback
(port temporar `64919`), fără browser Windows real. Accessibility tree-ul a
confirmat clasa `SelectedApiTestsReportUiTest` cu verdict `Pass` și toate cele
`13` noduri trecute. În `Screen validation` și evidence au fost verificate:

1. acțiunile Playwright de navigate, wait, get text, click, focus și
   wait-for-state pentru ambii owneri;
2. scenariile exacte `GET /public/v2/users — collection contract` și
   `POST /public/v2/posts — create contract`, urmate până la phase, request și
   contract validation;
3. linkurile inline `📷 base64 img`, `Audit observations` și `Assertions`;
4. assertion-ul de identity: `selected=2`, `rendered=2`, cu ambele metode și
   titlurile raportate în actual.

Raportul API pereche a fost deschis separat în ATF Debug UI pe loopback
(port temporar `49701`) și reconciliat manual. Accessibility tree-ul și HTML-ul
servit confirmă exact owner-ii selectați `UsersApiTests` și `PostsApiTests`:

- `UsersApiTests`: GET, `HTTP 200`, `Content-Type: application/json`, response
  JSON cu `id`, `name`, `email`, iar contract assertions sunt verzi;
- `PostsApiTests`: POST, request JSON cu `title/body/userId`, response `201`,
  `Content-Type: application/json`, response JSON cu `id/user_id/title/body/status/created`;
- mock route, status, header și body sunt astfel aceeași sursă pentru ce
  validează UI-ul în raportul Extent; HTML-ul conține exact `2` blocuri
  `REQUEST JSON` și `2` blocuri `RESPONSE JSON`.

Artefactele de mai sus au fost promovate în `artifacts/reports/` și
`artifacts/reports/ui/` după gate-ul verde. Nicio rulare curentă nu a fost
oprită; raportul a fost servit local din staging pentru verificarea finală.

## Contractul cerut

Clasa executabilă este [`SelectedApiTestsReportUiTest.java`](../../atf-tests/src/test/java/atf/ui/tests/extent/configurable/SelectedApiTestsReportUiTest.java).

Selecția este declarată direct în Java:

```java
@ApiScenarioSetup(tests = {
        @ApiTestRef(owner = UsersApiScenarios.class,
                method = UsersApiScenarios.GET_COLLECTION),
        @ApiTestRef(owner = PostsApiScenarios.class,
                method = PostsApiScenarios.CREATE_POST)
})
@UiExtentAudit(rules = {
        UiReportAuditRule.SETUP,
        UiReportAuditRule.TESTS,
        UiReportAuditRule.TESTS_DEEP,
        UiReportAuditRule.REQUEST_CONTRACT,
        UiReportAuditRule.OVERVIEW,
        UiReportAuditRule.OVERVIEW_DEEP,
        UiReportAuditRule.CATEGORIES,
        UiReportAuditRule.FAILS,
        UiReportAuditRule.DURATIONS,
        UiReportAuditRule.EVIDENCE,
        UiReportAuditRule.RESPONSIVE,
        UiReportAuditRule.STYLE,
        UiReportAuditRule.CROSS_TAB
})
class SelectedApiTestsReportUiTest { /* @TestFactory reutilizabil */ }
```

`ApiTestRef` este o referință tipizată `(owner class, scenario method)`. Nu
este un string liber și nu este un nume de test JUnit căutat prin reflection.
Metoda indicată este un publisher de scenariu din catalogul API; ea produce
owner-ul și pașii raportabili în raportul Extent. În exemplul curent:

| Referință | Familie | Ce intră în raport |
|---|---|---|
| `UsersApiScenarios.getCollection` | `users` | owner `UsersApiTests`, GET collection |
| `PostsApiScenarios.createPost` | `posts` | owner `PostsApiTests`, POST create cu body JSON |

Selecția exactă are precedence peste `families`, suite alias/JVM și familiile
YAML. YAML-ul/mock-ul rămâne sursa de adevăr pentru contractele transportului,
nu mecanismul de alegere a celor două metode din acest test.

## Lanțul executabil

```text
@ApiScenarioSetup(tests = { ... })
        ↓
ApiScenarioSetupCatalog.resolve(ApiTestRef)
        ↓
ApiScenarioSelection + ApiScenarioManifest
        ↓
ApiReportSetupFlow (API mock, fără browser)
        ↓
API Extent report read-only pentru UI
        ↓
UiReportAuditFixture
        ↓
@TestFactory → setup / Tests / Overview / evidence / CSS / cross-tab
        ↓
UI Extent report auditabil
```

API-ul rămâne exclusiv în `atf.api.tests.api`. Orice `Page`, `Locator`, CSS,
disclosure, screenshot sau aserțiune despre randarea HTML rămâne în
`atf.ui.tests.extent`.

## Ce este implementat și verificat

- [x] selecție exactă cu `@ApiTestRef(owner, method)`, nu listă YAML;
- [x] rezoluție deterministă a familiilor și owner-ilor pentru toate
  publisher-ele live: `users`, `posts`, `requests`, `headers`, `flows`,
  `negative`, `lifecycle`;
- [x] publicare selectivă în catalog, fără rerularea scenariilor necerute;
- [x] manifest cu scenariile selectate, expected derivat din catalog/mock și
  route identity;
- [x] o singură precondiție API și un singur fixture UI pentru factory;
- [x] `TESTS_DEEP`: număr, owner, titlu scenariu, disclosures și evidence;
- [x] `OVERVIEW_DEEP`: reconcilierea numărului de scenarii cu Tests;
- [x] `REQUEST_CONTRACT`: request/response JSON și reconciliere mock;
- [x] `STYLE`: display, visibility, bounding box și suprafața structurală în
  Tests, Overview, Categories, Fails și request/evidence;
- [x] `CROSS_TAB`, Categories, Fails, durations, responsive și evidence;
- [x] locatori și acțiuni centralizate; testul nu construiește CSS direct;
- [x] test de contract pentru cardinalitate `1/2/N`, familii multiple,
  duplicate și owner invalid; toate publisher-ele au `scenarioTitle` și
  `publishSelected` tipizate;
- [x] build izolat în `.tmp`, fără oprirea rulărilor existente;
- [x] regresia contractuală focalizată a trecut în staging: catalog, setup
  flow, projection, snapshot, CSS/theme/interaction contracte; build-ul este
  compilabil după reconcilierea snapshot-urilor semantice;
- [x] pentru baseline-ul anterior, artifactele API și UI au fost copiate în
  directoarele live configurate;
- [x] artifactul UI baseline a fost redeschis prin ATF Debug UI/browser MCP și
  verificat prin click-uri și disclosures; nu s-a folosit browser Windows real.
- [x] rerun-ul complet final în staging a trecut `13/13` pași, `0` failures,
  `0` errors, iar raportul final a fost redeschis în ATF Debug UI/browser MCP;
- [x] randarea finală a fost verificată în Overview și Tests, inclusiv
  disclosure-urile `Request details`, `Response details`, `Screen validation`,
  `Playwright action sequence`, `Audit observations` și `Assertions`;
- [x] artefactele finale validate au fost promovate numai după gate-ul verde în
  `artifacts/reports/` și `artifacts/reports/ui/`.

Referințele vechi de la 18:27–19:03 nu mai sunt folosite ca dovadă curentă:
nu există în directorul live și nu sunt promovate din nou. Dovada canonicală
pentru acest feedback este perechea fresh de mai sus, generată din același
staging și verificată prin ATF Debug UI/browser MCP.

## Ce înseamnă validarea profundă în acest increment

Validarea nu se oprește la existența tab-urilor. Pentru selecția exactă,
engine-ul verifică relația:

```text
selecție Java → owner/scenariu raportat → request/response/mock
             → Tests → Overview → evidence/style → cross-tab
```

Regula `STYLE` este intenționat renderer-aware doar la nivelul contractului
vizual stabil: elementul important trebuie să fie vizibil, să aibă suprafață
calculată și să nu fie ascuns prin `display:none`, `visibility:hidden` sau
geometrie nulă. Ea nu hard-codează culori arbitrare, pentru a nu lega testul de
o temă accidentală.

## ToDo-uri reconciliate cu codul live — toate închise

| Status | Prioritate | Scor | ToDo / constatare live | Criteriu pentru închidere |
|---|---:|---:|---|---|
| DONE-CODE | P1 | 100 | `ApiTestRef` era limitat la `users` și `posts`; acum catalogul rezolvă toate cele șapte publisher-e live, cu owner/familie/titlu și publicare selectivă. | Închis prin `ApiScenarioSetupCatalogTest` pentru toate familiile; orice owner/metodă necunoscută eșuează înainte de browser. |
| DONE-CODE | P1 | 100 | `ApiScenarioManifest` avea nevoie de o proiecție immutable separată pentru selecție și shape-ul raportului. | Închis prin `ExpectedReportProjection` + `ApiScenarioManifest.expectedProjection()` și `ExpectedReportProjectionTest`; expected-ul se construiește înainte de DOM. |
| DONE-CODE | P1 | 100 | Style contractul trebuia să acopere componente semantice și toate tab-urile. | `ReportStyleValidator` verifică owner/scenario/request/evidence per scenariu, iar `ReportProjectionMatrixInspector` este rulat pentru Tests/Overview/Categories/Fails. |
| DONE | P2 | 100 | Coverage-ul de evidence este verificat pentru fiecare scenariu/request în style contract, cu canale Request/Response/Execution distincte, capturi inline și linkuri vizibile. | Închis prin rerun complet verde `SelectedApiTestsReportUiTest` (`13/13`), verificare în ATF Debug UI a Overview/Tests/evidence și promovarea artefactului final. |
| DONE-CODE | P2 | 100 | Matricea exact-selection nu acoperea toate cardinalitățile și publisher-ele. | Închis prin `ApiScenarioSetupCatalogTest`: `1/2/N`, duplicate, owner invalid și toate familiile publisher. |
| DONE | P3 | 93 | `ApiExtentReportExploratoryTest` este deja golden E2E subțire; orchestration-ul comun este în factory/engine/inspectors. | Menține testul golden subțire și nu reintroduce aserțiuni comune în exploratory. |
| DONE | P3 | 100 | `ReportProjectionMatrixInspector` și `ReportStyleValidator` acoperă Tests, Overview, Categories, Fails și request/evidence per scenariu. | Închis prin raportul final cu `1` clasă trecută, `13` pași trecuți și `0` failures; durata dinamică și geometria responsive sunt verzi. |

Scorul de 100/100 reflectă atât implementarea codului, cât și gate-ul operațional
final. Rerun-ul final a fost executat izolat sub `.tmp`, fără oprirea
proceselor existente, apoi raportul exact rezultat a fost deschis prin ATF Debug
UI/browser MCP. Overview a confirmat `1` clasă trecută și `13` pași trecuți;
Tests a confirmat toate regulile verzi, iar evidence-ul a fost deschis pentru a
verifica vizual separarea Request/Response și Screen validation. Niciun raport
cu failures istorice nu a fost promovat.

## Gate obligatoriu la fiecare extindere

1. Citește codul live și README-urile ownerilor.
2. Adaugă referința programatică și testul de contract înainte de wiring nou.
3. Construiește într-un staging nou sub `.tmp`; nu opri rulările active.
4. Rulează testele focalizate și verifică request/response JSON din mock.
5. Copiază numai artifactele validate în `artifacts/reports/` și
   `artifacts/reports/ui/`.
6. Deschide exact raportul UI prin ATF Debug UI/browser MCP; verifică tab-ul
   Tests, tab-ul Overview, disclosures, linkul screenshot și computed styles.
7. Actualizează acest document numai cu statusul demonstrat de cod și artifact.

### Gate final executat și verde — 2026-10-16 22:40

Build-ul izolat a fost executat în:
`.tmp/annotated-selected-reconcile-20261016-224001/`.

Comanda de gate a rulat `SelectedApiTestsReportUiTest` cu `13/13` pași trecuți,
`0` failures și `0` errors. Raportul UI final a fost deschis în ATF Debug UI pe
portul temporar `64919`, fără browser Windows real. Verificarea a acoperit
Overview, Tests, status/durations, tab-urile de navigare, disclosure-urile
Request/Response/Screen validation și verdicturile `Pass` din accessibility
tree. Raportul API pereche a fost redeschis pe portul `49701` și conține GET
users + POST posts cu request/response JSON.

Artefactele promovate după gate:

- UI: `artifacts/reports/ui/ui-atf-extent-report_selected-api-tests-report-ui-test_2026-10-16_22-41-01-634.html`
  (SHA-256 `C5F98B6D82FF4DA81ECA3C9F1040733966F142225BE6E995944C4434361A7A16`)
- API: `artifacts/reports/api-atf-extent-report_2026-10-16_22-40-54-971.html`
  (SHA-256 `594379AA7312BFF8307096D775C211E5BECB5CB88741B2DFF5C0AA52B6A8FE5F`)

Proceselor/rulărilor existente nu li s-a trimis stop/kill; staging-ul și cele
două servere Debug UI au fost folosite numai pentru validarea izolată și
auditabilă. Aceste două artifacte sunt dovada canonicală pentru incrementul
curent.
