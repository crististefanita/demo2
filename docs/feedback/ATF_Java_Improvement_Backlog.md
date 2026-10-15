# ATF Java — backlog activ de refactorizare

Acest document este matricea de execuție pentru ToDo-urile rămase din
[feedback-ul activ](README.md). Statusurile sunt auditate pe codul live, nu pe
rapoarte vechi. F-04 este păstrat separat mai jos ca închis/verificat și nu mai
ocupă ordinea de implementare.

## Ordine recomandată

```text
F-06 hotspot decomposition
  → F-01 reporting orchestration
  → F-02 semantic snapshot comun
  → F-03 configuration composition root
  → F-05 verificare comportamentală artifact
  → F-07 package metadata + cohesion gate global
  → D-01..D-04 durate și badge-uri
```

F-06 și F-01 trebuie începute primele deoarece definesc limitele în care pot fi
extrase componentele fără a crea package-uri artificiale. Inventarul și regula
de boundary există deja; backlog-ul urmărește doar reducerea hotspot-urilor și
menținerea verificărilor.

## Matrice de execuție auditată

| ID / status | Owner | Test minim obligatoriu | Dovadă finală / gap actual |
|---|---|---|---|
| `OPEN` F-01 | `atf-reporting` theme/runtime | composition order, filters, collapse, CSS tokens, parallel sessions | `AtfReportTheme` și `ExtentReportRuntimeBehaviour` rămân hotspot-uri mari, cu CSS/JS duplicat |
| `OPEN` F-02 | semantic document + Extent adapter | aceeași fixture și aceleași count/status în ambele proiecții | fallback și Extent nu consumă încă același snapshot semantic immutable |
| `PARTIAL` F-03 | composition root + config loaders | roots concurente, precedence, schema strictă, snapshot immutable pentru `atf.yaml` + `atf-mock.yaml` | snapshot-urile există; root-scoping strict și sursele explicite încă lipsesc |
| `CLOSED` F-04 | execution lifecycle + reporting evidence | retry + body failure + teardown failure + redaction | implementat și verificat în `LifecycleApiScenarios`/`ApiAcceptanceReportTest`; `Fails` și `Tests` cu chain ordonat |
| `PARTIAL` F-05 | report artifact runtime | click tabs, filters, hash, disclosures, siblings, computed styles | explorarea dinamică și raportul UI separat sunt închise; rămân matricea filtrelor și computed styles globale |
| `OPEN` F-06 | architecture tests | import/POM forbidden, static state, package inventory, allow-list | hotspot-urile reale rămân; `atf.api.reporting` este încă monolitul dominant |
| `OPEN` F-07 | report metadata + architecture test | package Java real în overview, environment coerent, inventory global inclusiv `atf-tests/src/main/java`, limită 8 | `ReportEnvironment` încă hardcodează `atf-tests`; inventory-ul exclude `atf-tests/src/main/java` |
| `PARTIAL` D-01 | duration runtime | `data-duration-ms` și `data-duration-source` pe noduri | markerii sunt adăugați selectiv prin runtime, nu demonstrați pe toate tab-urile/nodurile |
| `PARTIAL` D-02 | duration aggregation | suma copiilor direcți, fără vendor double-count | implementat pentru class header/scenarii directe, nu generalizat pentru toate filtrele/nodurile |
| `PARTIAL` D-03 | duration formatter + missing values | absent/malformed/zero/leaf | formatterul `ms`/`s` este închis; lipsa rămâne inconsistentă (`No response duration recorded` vs ținta `—`) |
| `OPEN` D-04 | theme tokens | badge geometry, status/duration separation, computed styles | tokenurile există, dar tema conține încă duplicate/override-uri pentru tag-uri și status |

F-04 și partea exploratorie din F-05 sunt închise/verificate în această rundă;
nu le mai tratăm ca implementări lipsă. F-05 rămâne `PARTIAL` numai pentru
filtrele și contractele vizuale generale enumerate mai sus. D-03 are formatterul
închis, dar rămâne parțial până când lipsa duratei are aceeași reprezentare în
toate proiecțiile.

### F-05 — dovadă live curentă

Rularea din 2026-10-15 14:09 a executat `ApiExtentReportExploratoryTest` cu
`4/4` teste verzi, `0` failures și `0` errors. Testul generează un raport API
input și un raport UI separat, apoi verifică prin Playwright: Overview ↔ Tests,
Category/Fails navigation, clase/scenarii/faze descoperite dinamic, expandarea
idempotentă, Request/Response/Execution disclosures, body-uri JSON condiționale,
asserțiuni și capturi Base64 inline. Artifactele sunt în
`artifacts/reports/` și `artifacts/reports/ui/`; verificarea finală a fost făcută
în TAB MCP.

## Gate comun

Pentru fiecare increment: recitește live, creează staging nou, scrie testul
contractului, implementează refactorizarea, rulează reactorul relevant și
compară staging-ul cu live după build. Pentru schimbări observabile,
generează raportul în staging, deschide-l în TAB MCP și verifică toate
suprafețele relevante. Un raport vechi, un build pe checkout-ul live sau un
test relaxat nu este dovadă de închidere.

Un test care verifică doar că un script conține un selector nu închide un
comportament de browser; un formatter care convertește `301 ms` nu închide
semantics pentru durată absentă. Statusul se schimbă numai după ce gap-ul exact
este demonstrat pe cod, test și artifact.
