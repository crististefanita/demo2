# ATF UI — browser, context și sesiune Playwright

`atf-ui` este infrastructura runtime pentru browserul Playwright. Configurează
browserul, contextul, pagina, timeout-urile, viewport-ul, screenshot-ul și
recorderul de acțiuni. Nu conține testele UI ale raportului, rendererul HTML,
publisherul Extent sau page objects de produs.

## Overall

| Input | Output | Boundary |
|---|---|---|
| YAML browser policy | browser/context/page | runtime generic |
| Playwright actions | neutral action records | fără Extent |
| screenshot/viewport | UI evidence | consumat de reporting la compoziție |

## Diagramă de bază

```mermaid
flowchart LR
    Tests[consumer tests] -->|selectează| Config[browser policy]
    Config -->|configurează| Session[Playwright session]
    Session -->|deschide| Browser[browser / page]
    Browser -->|produce| Evidence[action + screenshot]
    Evidence -->|alimentează| Reporting[reporting]
```

<details>
<summary><strong>Legendă — runtime browser fără reporting</strong></summary>

- configurația descrie browserul și evidence-ul generic;
- sesiunea compune browser, context și page, apoi înregistrează acțiuni;
- atf-ui livrează payload-ul către consumer, nu decide verdictul și nu construiește HTML/CSS/Extent;
- publisherul și auditul aparțin atf-reporting, iar scenariile concrete aparțin test scope-ului consumator.
</details>

| Responsabilitate | Owner |
|---|---|
| browser, context, page, viewport, timeout | `atf-ui` |
| acțiuni Playwright primitive și recorder neutru | `atf-ui` |
| locatori, page objects, flows și testele declarative | `atf-reporting-tests` test scope |
| snapshot contract și validatori de browser | `atf-reporting` production + `atf-reporting-tests` test scope |
| HTML, CSS, Extent lifecycle și publisher | `atf-reporting` |
| mock, API scenarios și contract validation | `atf-reporting-tests` test scope + modulele API |

`SectionAuditCheckExecutor` și `ExtentSectionAuditPublisher` NU aparțin
`atf-ui`. Validarea statică și proiecția de raport aparțin `atf-reporting`;
execuția browserului și scenariile E2E apar explicit în
[`atf-reporting-tests/src/test/java/atf/ui/tests/extent/README.md`](../atf-reporting-tests/src/test/java/atf/ui/tests/extent/README.md).
`atf-ui` nu trebuie să importe acele clase, să publice Extent sau să emită
assert-uri de secțiune.

## Componentele runtime

- `UiBrowserConfiguration` — browser, headless, timeout, navigation timeout și
  viewport;
- `UiScreenValidationConfiguration` — politica generică de captură, fără
  cunoașterea raportului, a Extent sau a outputului;
- `PlaywrightUiSession` — lifecycle scoped al unui browser context și page;
- `PlaywrightBrowserActions` / `PlaywrightLocatorActions` — operații primitive;
- `PlaywrightActionSequence` / `UiActionRecorder` — model neutru al operațiilor;
- `UiScreenValidation` — capturează payload-ul PNG Base64 din page.

Sesiunea este idempotentă la close și nu deține serverul Debug UI. `atf-ui` nu
descoperă YAML de proiect, nu rezolvă outputuri și nu primește decizii de audit;
consumerul compune politicile generice și publică artifactul prin
`atf-reporting`.

## Configurație generică, fără flag redundant

`atf-ui` expune doar valori care descriu execuția browserului și capturarea
generică. Nu este owner pentru `report.directory`, `apiSetup`, `audit` sau
`filePrefix`; aceste valori sunt test-only și aparțin configurației interne a
modulului `atf-reporting`:

[`atf-reporting-tests/src/test/resources/atf-reporting-ui.yaml`](../atf-reporting-tests/src/test/resources/atf-reporting-ui.yaml)

```yaml
ui:
  browser: chromium
  baseUrl: about:blank
  headless: false
  timeout: PT10S
  navigationTimeout: PT45S
  viewport: { width: 1440, height: 900 }
  # Page zoom used by Playwright screenshots; 70% shows more report evidence.
  zoomPercent: 70
  screenshotOnFailure: true
  screenValidation:
    enabled: true
    fullPage: false
```

- nu există și nu se adaugă `ui.enabled`;
- runtime-ul se construiește când consumerul selectează un test;
- opțiunile concrete descriu browserul și evidence-ul;
- `ui.screenValidation.enabled` controlează numai capturarea screenshot-ului;
- cheia nu activează testele și nu publică raportul;
- `ui.zoomPercent` controlează zoom-ul CSS pentru fiecare pagină navigată;
- valoarea recomandată pentru rapoarte Extent este `70`;
- `viewport.width` și `viewport.height` rămân neschimbate;
- valoarea este validată în intervalul `25..200`, iar lipsa cheii păstrează `100`;
- YAML loading, override-ul `-Datf.ui.config`/`-Datf.ui.report` și rezoluția
  inputului audit aparțin exclusiv test scope-ului `atf-reporting`.

## Acțiuni și locatori

`atf-ui` poate executa o operație pe un `AtfLocator`, dar nu decide selectorul
și nu știe intenția business. Locator facade-urile sunt în `atf-reporting-tests`
test scope:

```text
Report*Locators
    → AtfLocator(selector + Locator)
    → PlaywrightLocatorActions
    → PlaywrightActionSequence
    → flow / extractor / validator din atf-reporting
```

- testele nu scriu direct `page.locator(...)`, `getByRole(...)` sau
  `getByText(...)`;
- în `TARGET` se publică selectorul efectiv folosit, nu descrieri precum
  `"selected owner"` sau `"collapse node"`;
- operațiile suportate sunt `Navigate`, `Click`, `Get text`, `Find element`,
  `Expect text`, `Focus`, `Wait for visible`, `Expect visible` și `Wait`;
- stările asincrone folosesc așteptări Playwright cu timeout:
  `waitFor`, `waitForCondition`, `waitForURL`;
- `waitForTimeout` nu este API de stabilizare acceptat.

În CI/ATF Debug, când revizia Playwright cerută nu este disponibilă în cache,
runnerul poate primi un browser headless deja provisionat prin
`-Datf.ui.playwright.executablePath=<chromium-executable>` sau prin
`ATF_UI_PLAYWRIGHT_EXECUTABLE_PATH`. Este un override de bootstrap, nu o cheie
de business YAML; în lipsa lui, launcherul păstrează comportamentul Playwright
standard și fail-closed dacă nu poate provisiona browserul.

`PlaywrightActionSequence` este recorderul, nu auditorul:

- înregistrează ordinea, target-ul, expected, observed, durata și statusul;
- validatorul pur din `atf-reporting` decide dacă snapshot-ul DOM corespunde
  proiecției așteptate.

## Screen validation și screenshot

`UiScreenValidation.capture(page, configuration, title)` returnează un payload
PNG Base64 și nu scrie fișier PNG extern. Publisherul din `atf-reporting` îl
expune o singură dată ca link inline `📷 base64 img` cu `data:image/png;base64,...`.
Alinierea pill-ului, separarea tabelului și disclosure-urile sunt contract de
reporting, nu CSS în `atf-ui`.

Separarea generică dintre flow mode, screenshot policy și capture source este
documentată în
[`docs/ATF_Evidence_Capture_Contract.md`](../docs/ATF_Evidence_Capture_Contract.md).
`atf-ui` este providerul browser, nu owner-ul policy-ului, al YAML-ului consumerului
sau al publisherului HTML.

Un pas `Screen validation` trebuie să păstreze în aceeași stare vizuală:

```text
acțiuni reale Playwright
  → screenshotul acelei stări
  → observații
  → assertions
```

Nu se atașează aceeași imagine la fiecare assertion și nu se folosește un
assert Java care ascunde acțiunea din raport. Contractul complet de cardinalitate,
markup și randare este în
[`atf-reporting/README.md`](../atf-reporting/README.md).

## Network inspector — boundary generic

`UiNetworkInspector` este opt-in și acceptă numai host-urile configurate.
Listenerii Playwright observă atât `response`, cât și `request-failed`; nu
execută replay și nu decid verdictul testului. Observațiile browser sunt
transformate în date typed, cu URL/query, headers și body redacted/bounded;
failure-ul înainte de response are status `Unavailable` și motiv explicit,
fără un response inventat.

| Contract | Owner |
|---|---|
| stream-ul raw și colapsarea doar a semnăturilor consecutive pentru prezentare | `atf-ui` |
| așteptarea bounded printr-o settlement policy explicită, fără retry/replay de acțiune | `atf-ui` |
| aliasul de serviciu, corelarea cu checkpoint-ul și verdictul business | consumerul |
| cardurile HTML și sumarul agregat | `atf-reporting` |

`capture(UiNetworkObservation)` este și o intrare publică pentru fixture-uri și
adaptoare. Inspectorul resanitizează observația înainte de storage și de
notificarea subscriberilor: URL/path, headers, body, auth/source, failure,
service și signature sunt reconstruite sau limitate. Testul negativ pentru
input creat de caller protejează acest boundary; criteriul de redeschidere este
în [`ATF_UI_Network_Inspector_Feedback.md`](../docs/feedback/ATF_UI_Network_Inspector_Feedback.md).

Read-back după acțiune:

- înainte de acțiune: capturează `observationCursor()`;
- după acțiune: folosește `awaitAfter(cursor, predicate, timeout)`; un GET vechi
  nu poate satisface settlement-ul curent;
- limită: cursorul garantează ordinea, nu payload identity sau idempotency;
  `await(...)` caută în tot jurnalul, iar nicio metodă nu repetă mutația.

Contractul de retry Playwright:

- `UiRetryOperation` permite numai operații read-only; click, submit și alte
  mutații nu sunt configurabile și nu primesc al doilea attempt;
- retry-ul business-specific aparține consumerului și cere identity stabilă,
  payload complet asociat și garanție server-side de idempotency;
- `requestSignature` grupează observații și folosește body redacted/bounded; nu
  reprezintă payload identity pentru replay;
- request observation și cursor temporal nu dovedesc idempotency.

Detaliul și criteriile de extindere sunt în
[`ATF_UI_Network_Inspector_Feedback.md`](../docs/feedback/ATF_UI_Network_Inspector_Feedback.md).

## Ce nu se pune în `atf-ui`

- teste JUnit sau colecții API;
- `UiConfiguration`, `UiAuditSuiteConfiguration`, loaderul YAML și contextul
  pentru `atf.ui.config`/`atf.ui.report` — sunt configurație de test a
  raportării și stau în `atf-reporting-tests/src/test/java/.../support/config`;
- `SectionAuditCheckExecutor`, `SectionAuditCheckPolicy` sau
  `ExtentSectionAuditPublisher`;
- `ReportProjection`, reguli pentru Tests/Fails/Categories sau contractul static HTML;
- HTML/CSS/Extent nodes sau finalizarea artifactului;
- page objects, selectorii produsului și flows de business;
- fallback la un artifact arbitrar sau modificarea inputului API.

Aceste limite sunt intenționate: `atf-ui` rămâne reutilizabil și testabil ca
runtime Playwright, `atf-reporting` rămâne reutilizabil ca librărie de raportare,
iar `atf-reporting-tests` compune scenariile E2E și adaptorul browser.

## Verificare

Teste runtime/config:

```text
mvn -pl atf-ui -am test
```

Testele de raport UI se rulează din composition module-ul
`atf-reporting-tests`, de exemplu:

```text
mvn -pl atf-reporting-tests -am -Dtest=SelectedApiTestsReportUiTest test
```

Build și audit:

- build-ul se face în staging extern (`${ATF_STAGING_ROOT}/atf-ui-<id>`);
- rulările curente și serverele rămân intacte;
- raportul UI aprobat se publică numai în directorul configurat al consumerului;
- artifactul HTML rezultat se verifică prin ATF Debug UI și Playwright/browser
  MCP;
- verificarea acoperă click-uri, expand/collapse, accessibility tree,
  console/network și randare;
- browserul Windows real nu se folosește.

Pentru procesul de sync și siguranță vezi
[`docs/ATF_Build_And_Execution_Safety.md`](../docs/ATF_Build_And_Execution_Safety.md);
pentru contractul vizual vezi
[`atf-reporting/README.md`](../atf-reporting/README.md).
