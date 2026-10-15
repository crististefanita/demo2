# ATF Java — feedback activ

Acest director păstrează exclusiv îmbunătățirile care mai trebuie
implementate sau întărite prin teste în codul live. Contractele stabile,
deciziile închise și exemplele istorice apar în `docs/` și în README-urile
modulelor, nu aici.

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

`AtfRuntimeSnapshot` și compunerea `ApiModuleFactory` există și sunt
verificate. ToDo-ul rămas este încărcarea root-scoped, strictă și unică:
`AtfReportConfigurationContext` recurge încă la `user.dir`, iar console,
mock și reporting au loader-e specializate care pot descoperi configurații
independent.

Păstrează configurația în `atf.yaml`, fără modul Maven nou și fără parser YAML
în `atf-core`. Composition root-ul trebuie să rezolve explicit fișierul,
calea relativă față de acel fișier și să producă un snapshot immutable pentru
API, mock, console și reporting. După bootstrap, modulele nu mai recitesc
fișierul și nu mai depind de working directory global.

Teste de închidere: două root-uri concurente, două working directories,
precedence property/env/YAML/default, schema invalidă/chei necunoscute,
URI/timeout/path invalid, path traversal și imutabilitate după bootstrap.

### F-04 — Închide failure-cause chain pentru retry + lifecycle

Propagarea unui failure de contract în lifecycle este acoperită: publisher-ul
publică evidence înainte de verdict, faza devine `FAIL`, teardown-ul continuă,
iar precedence-ul rămâne `FAIL > SKIP > WARNING > PASS`. Proiecția
`PRIMARY/SECONDARY` și redaction-ul de bază sunt de asemenea acoperite.

Rămâne neacoperită combinația reală retry + failure de body + failure de
teardown, verificată până la artifactul final și `Fails`.

Păstrează cauza primară în ordinea execuției și adaugă cauzele secundare fără
duplicarea arborelui `Tests`. Aceeași cauză trebuie să fie observabilă în
evidence-ul requestului și în `Fails`, iar tokenurile sensibile rămân
redacted.

Test de închidere: execuție mock cu retry, body failure și teardown failure,
cu ordinea cauzelor verificată în artifact și în `Fails`.

### F-05 — Verifică artifactul prin comportament, nu numai prin string-uri

Contractele statice și testele runtime-script există, dar majoritatea protejează
prezența textului JavaScript, nu comportamentul DOM după interacțiune.

Adaugă o verificare componentă pentru artifactul autonom, fără să schimbi
formatul vizual: navigare `Overview → Tests → Category → Fails`, filtre de
status/tag, hash deep-links, expand/collapse, `Open in Tests`, count-uri,
durate cu `ms`, status/durată centrate, timestamp absent din tabelul Tests și
Request/Validation ca frați direcți.

Dovada finală trebuie să combine testul determinist cu verificarea manuală în
TAB MCP pe artifactul generat din staging. Testele actuale protejează markup-ul
și scripturile, dar nu execută încă DOM-ul într-un browser headless; această
verificare comportamentală rămâne ToDo-ul.

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

## Reporting — ToDo-uri active

### D-01 — Durată numerică și sursă explicită

Separarea dintre `Request details`, `Response details` și `Execution evidence`
este implementată. Rămâne de standardizat pentru fiecare nod
`data-duration-ms` și `data-duration-source` (`measured`, `exclusive` sau
`aggregated`), inclusiv în `Fails`, `Category` și header-ul clasei.

Teste: `1 ms`, `16 ms`, peste o secundă, durată lipsă/malformed/zero și sursă
corectă după filtrare.

### D-02 — Agregare structurală fără dublare

Agregarea trebuie să folosească numai copiii structurali direcți și să evite
adăugarea unei durate vendor deja inclusive. Durata clasei din stânga trebuie
să fie egală cu suma scenariilor directe vizibile în dreapta.

Teste: părinte cu/fără durată proprie, nested phase, scenarii ascunse de filtru
și recalculare după schimbarea tab-ului.

### D-03 — Durată absentă și frunze

Request, validation, phase și lifecycle trebuie să distingă durata de count-ul
de assertions/noduri. Fără valoare numerică se afișează `—`, nu `0`, count sau
timestamp; unitatea `ms` apare numai pentru valori valide.

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

- [ATF_Architecture.md](../ATF_Architecture.md)
- [ATF_Reporting_Contract.md](../ATF_Reporting_Contract.md)
- [ATF_BUILD_AND_EXECUTION_SAFETY.md](../ATF_BUILD_AND_EXECUTION_SAFETY.md)
- [atf-reporting/README.md](../../atf-reporting/README.md)
