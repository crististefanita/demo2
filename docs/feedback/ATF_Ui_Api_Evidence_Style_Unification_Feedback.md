# ATF UI/API evidence style-unification feedback

**Status:** `CLOSED — regression only`  
**Scor final:** **100/100** pentru incrementul de unificare UI/API.

**Data auditului:** 2026-10-16  
**Scop:** comparație vizuală și structurală între zonele de evidence dintr-un
raport UI și unul API, cu formularea contractului comun și a ToDo-urilor de
implementare. Reconcilierea shell-ului comun UI/API este închisă complet;
ledger-ul de mai jos este păstrat numai ca trasabilitate și protecție de
regresie. Nu există ToDo activ în acest fișier.

## Artifacte auditate

- UI: [`ui-atf-extent-report_write-methods-all-api-normal_2026-10-16_22-54-46-873.html`](../../artifacts/reports/ui/ui-atf-extent-report_write-methods-all-api-normal_2026-10-16_22-54-46-873.html), SHA-256 `741A896CEBB162E4C0AC95315550A4C1C47EB4927E7AECB28A04C95D5284DB7D`
- API: [`api-atf-extent-report_2026-10-16_22-54-42-106.html`](../../artifacts/reports/api-atf-extent-report_2026-10-16_22-54-42-106.html), SHA-256 `EFE12B55B77CAC59656A4B019F8539B711DFF45D79C2809395064BBB9546C821`

Am deschis ambele artifacte în două instanțe paralele ale **ATF Debug UI**,
prin browser MCP, și am expandat efectiv nodurile relevante. Nu a fost folosit
un browser Windows real și nu au fost oprite rulări existente.

## Verdict executiv

Cele două rapoarte au deja aceeași idee corectă: un card de execuție, un tabel
semantic `STATUS / DETAILS`, badge-uri de status și disclosure-uri care ascund
evidence-ul detaliat. Nu trebuie unificate denumirile domeniului și nici
payload-urile:

- UI trebuie să rămână centrat pe acțiuni Playwright, observații vizuale,
  assertions și linkul inline `📷 base64 img`.
- API trebuie să rămână centrat pe request, response, JSON body, trace,
  execuție și attempt history.

Ce trebuie unificat este **shell-ul vizual și contractul de layout**, nu
semantica evidence-ului.

Verdictul curent de aliniere vizuală este **100/100** pentru contractul livrat.
Scorul nu este un scor de funcționalitate al testelor; este scorul pentru cât
de coerent se comportă cele două familii de evidence în același produs.

## Ce am verificat interactiv

### UI — `Screen validation`

Artifactul UI fresh arată `1` clasă, scenariul
`Browser matrix — write-methods-all-api-normal` trecut și `0` pași eșuați. În
acest scenariu am expandat:

1. `Screen validation`
2. `Playwright action sequence (2 entries)`
3. `Audit observations · 2 checkpoints`
4. `Assertions · 7`

Evidence-ul observat este corect și auditabil:

- acțiunile au tabelul `# / OPERATION / TARGET / EXPECTED / OBSERVED / TIME /
  RESULT`;
- linkul `📷 base64 img` este în aceeași zonă cu tabelul acțiunilor;
- observațiile au tabel separat, cu `Tab visible` și `Screenshot`;
- assertions au tabelul `ASSERTION / EXPECTED / ACTUAL / RESULT`;
- cele trei zone sunt disclosure-uri surori și sunt lipite vertical;
- între zone nu există separator orizontal decorativ; liniile vizibile din
  tabele sunt grila internă a tabelului și trebuie păstrate.

### API — POST cu request/response JSON

Raportul API fresh păstrează separat failure-urile intenționate de lifecycle și
warning-urile de contract soft. Acestea trebuie păstrate transparente; nu sunt o
problemă de stil care trebuie ascunsă prin unificare.

În `PostsApiTests` am expandat `POST /public/v2/posts — create contract`:

1. `TEST BODY`
2. `POST /public/v2/posts`
3. `Request details · 4 fields`
4. `Response details · 3 fields`
5. `Execution evidence · 1 attempts`

Evidence-ul API este semantic bun:

- request-ul POST arată URL, auth profile `default`, auth type `BEARER`,
  header-ul `Content-Type=application/json` și body JSON valid;
- response-ul arată `HTTP STATUS 201`, `CONTENT-TYPE application/json`, header-ul
  mock `X-Mock-Scenario=post-create` și body JSON;
- execution evidence păstrează status, URL, durată, attempts și retry events;
- attempt history este o a doua grilă, cu `ATTEMPT / STATUS / DURATION /
  OUTCOME / REASON`.

## Diferențe istorice găsite în DOM și computed styles — reconciliate în live

### Elemente deja comune

- același card Extent cu accent vertical albastru la stânga;
- același header de evidence și aceeași pereche semantică
  `STATUS / DETAILS`;
- aceleași badge-uri `Info` în coloana de status;
- aceeași mecanică nativă `<details>/<summary>` cu `＋` și `−`;
- același header de disclosure: accent, `display:flex`, aliniere verticală,
  padding aproximativ `9px 12px` și line-height compact;
- aceleași tabele interne întunecate, cu border discret și text de tabel;
- aceeași regulă: statusul și conținutul sunt separate semantic.

### Contractul care închide diferențele observate

1. **Lățimea coloanei STATUS era diferită în snapshotul inițial.** Contractul
   live publică acum `--atf-evidence-status-column-width` din tokenii comuni și
   perechea UI/API este verificată pentru aceeași geometrie.

2. **UI are tabelul de acțiuni cu minim contractual de `760px`.** Alegerea
   rămâne justificată pentru cele șapte coloane și linkuri lungi, iar live
   minimul este absorbit de wrapperul local; cardul părinte nu mai primește
   overflow-ul tabelului.

3. **API păstra delimitări proprii în snapshotul inițial între disclosure-urile
   request / response / execution.** `STYLE-UNIFY-02` este acum închis: shell-ul
   nu mai adaugă linii decorative între zone, iar separatoarele din tabelele
   interne rămân permise, fiind delimitări ale datelor.

4. **UI și API folosesc clase interne diferite pentru responsabilități diferite.**
   UI folosește `atf-ui-action-sequence`, `atf-ui-observations` și
   `assertion-evidence`, iar API folosește `request-evidence`,
   `response-evidence` și `execution-evidence`. Diferența semantică este
   legitimă; shell-ul CSS comun este aplicat prin tokeni și contracte, fără a
   forța aceleași tabele.

5. **Tabelele interne au roluri diferite, corect.** Nu trebuie forțate într-o
   singură coloană generică:

   - UI: șapte coloane pentru acțiuni și patru pentru assertions;
   - API: două coloane FIELD/VALUE pentru request/response/execution și cinci
     coloane pentru attempt history.

   Unificarea trebuie să fie la nivelul `evidence stack`, nu la nivelul
   `table schema`.

6. **Screenshot-ul este o responsabilitate UI, nu API.** Linkul inline
   `📷 base64 img` este corect în UI și nu trebuie adăugat artificial în
   request/response API. API trebuie să rămână complet auditabil prin JSON,
   headers, status, durată și attempts.

## Ce este mai corect în fiecare raport

### UI este referința pentru

- trei disclosure-uri surori fără linii orizontale decorative între ele;
- asocierea locală `action table → screenshot`;
- linkul clickable inline, cu emoji-ul curent, pe același rând și cu aliniere
  verticală;
- separarea dintre acțiune, observație și assertion;
- evidence-ul vizual care descrie exact ce s-a văzut în browser.

### API este referința pentru

- separarea request/response/execution;
- includerea JSON body-urilor în zonele corecte;
- trace tehnic (`HTTP STATUS`, `CONTENT-TYPE`);
- attempt history și retry evidence;
- modelul FIELD/VALUE pentru metadata transportului.

Nu trebuie ales un singur raport ca sursă completă. Contractul final este:
**shell vizual UI + semantică evidence API/UI păstrată separat**.

## Contract vizual livrat pentru ambele rapoarte

### Model semantic comun

Publisher-ele UI și API trebuie să producă aceeași formă conceptuală:

```text
EvidenceStack
  ├── status
  ├── zones[]
  │     ├── kind
  │     ├── title
  │     ├── countLabel
  │     ├── status
  │     ├── payload
  │     └── visualArtifact?   // numai UI când există screenshot
  └── internalTablePolicy
```

`kind` rămâne distinct (`PLAYWRIGHT_ACTIONS`, `AUDIT_OBSERVATIONS`,
`ASSERTIONS`, `REQUEST`, `RESPONSE`, `EXECUTION`, `RETRY_HISTORY`), însă toate
zonele trec prin aceeași structură de stack și aceeași politică de geometrie.

### Geometrie obligatorie

- un singur outer contour pentru grupul de disclosure-uri;
- un singur accent vertical stânga, fără borderuri stânga duplicate în copii;
- aceeași coloană STATUS și aceeași centrare verticală a badge-ului;
- aceeași coloană DETAILS, fluidă, cu `min-width: 0`;
- același header de disclosure, aceeași înălțime, padding și glyph;
- primul disclosure: border/colțuri de sus;
- disclosure-urile intermediare: doar laterale, fără linie orizontală
  decorativă între zone;
- ultimul disclosure: border/colțuri de jos;
- borderurile interne ale tabelelor rămân, fiind delimitări semantice ale
  datelor, nu delimitări între zone;
- overflow-ul tabelului de acțiuni se rezolvă în wrapperul local, nu prin
  lățirea cardului părinte;
- la viewport îngust, coloana de status rămâne stabilă, iar details se
  micșorează fără overflow orizontal al paginii.

### Contract de evidence

- UI păstrează `Playwright action sequence`, `Audit observations` și
  `Assertions` ca zone separate;
- API păstrează `Request details`, `Response details` și `Execution evidence`
  ca zone separate;
- JSON-ul de request și response rămâne în zona semantică respectivă;
- screenshot-ul UI rămâne o singură ancoră Base64, inline, clickable, cu
  `📷 base64 img`, `target="_blank"` și `rel="noopener"`;
- nu se introduc screenshot-uri în raportul API doar pentru a egala numărul de
  zone;
- count-urile sunt calculate din payload, niciodată hardcodate în markup;
- toate disclosure-urile pornesc închise în artifactul final.

## Trasabilitate UI → API — reconciliată în incrementul curent

În artifactul UI fresh pentru cazul `write-methods-all-api-normal`, evidence-ul
publică targetul API generat în aceeași rulare:

```text
api-atf-extent-report_2026-10-16_22-54-42-106.html
```

Artifactul API pereche copiat în locația configurată este:

```text
artifacts/reports/api-atf-extent-report_2026-10-16_22-54-42-106.html
```

UI-ul curent demonstrează exact perechea prin `REQUEST TARGET`, `Navigate` și
assertion-ul de input; API-ul a fost generat și verificat în staging-ul
`.tmp/style-unification-20261016-231000`, apoi sincronizat fără modificare în
`artifacts/reports/`. Hash-ul UI și hash-ul API de mai sus permit verificarea
bit-cu-bit a perechii. `TRACE-PAIR-01` și `TRACE-PAIR-02` sunt închise pentru
acest increment.

## ToDo chirurgical și ledger de status

| ID | Status | Prioritate | ToDo | Criteriu de acceptare |
|---|---|---:|---|---|
| STYLE-UNIFY-01 | DONE | P1 | Shell comun `AtfEvidenceStackCss` pentru EvidenceStack și disclosure zones; publisher-ele UI/API rămân separate semantic. | Ambele rapoarte au aceeași structură de outer contour, glyph, header și status/details columns. |
| STYLE-UNIFY-02 | DONE | P1 | Elimină delimitările decorative dintre sibling disclosures API și UI. | API și `Screen validation` nu mai afișează linii orizontale între zone; grilele interne rămân intacte. |
| STYLE-UNIFY-03 | DONE | P1 | Normalizează `STATUS` width prin `AtfReportThemeTokens` și păstrează `DETAILS` fluid cu `min-width:0`. | Tokenii sunt comuni; computed style-ul fresh confirmă aceeași semnătură la viewport normal și narrow. |
| STYLE-UNIFY-04 | DONE | P1 | Izolează minimul tabelului Playwright în overflow wrapper strict local. | `scrollWidth == clientWidth` pentru pagina fresh; overflow-ul tabelar rămâne local. |
| STYLE-UNIFY-05 | DONE | P1 | `ReportEvidenceStyleContract` citește perechea UI/API și compară tokenii de status width, summary padding/height, radius, key column, overflow și politica de sibling borders; drift-ul este negativat explicit în test. | `ReportEvidenceStyleContractTest` trece pentru pereche compliant și pentru un status-width drift injectat; verificarea computed-style rămâne în matricea browser. |
| TRACE-PAIR-01 | DONE | P1 | Leagă UI de API-ul generat în aceeași rulare, nu de un URL istoric hardcodat sau rămas în fixture. | `Navigate` din UI indică exact artifactul API pereche și acesta este copiat în `artifacts/reports/`. |
| TRACE-PAIR-02 | DONE | P1 | Publică în UI evidence identificatorul artifactului API: filename, timestamp sau hash scurt. | Reviewer-ul poate reconcilia UI report ↔ API report fără presupuneri. |
| STYLE-UNIFY-06 | DONE | P2 | Tokenii vizuali (`border`, `panel`, `accent`, `status width`, `summary padding`, `radius`) sunt centralizați în theme/CSS contract. | `AtfReportThemeTokens`, `AtfEvidenceStackCss` și testele de contract protejează semnătura. |
| STYLE-UNIFY-07 | DONE | P2 | Matrix ATF Debug/browser MCP pentru UI/API la viewport normal și narrow, cu disclosure-uri expandate pe rând. | Matrix 4/4 trece; verificarea fresh confirmă click, JSON, screenshot inline, no sibling divider și no page overflow. |
| STYLE-UNIFY-08 | DONE | P2 | Păstrează tabelele interne specializate; nu le reduce la un generic FIELD/VALUE. | Acțiunile, assertions, JSON și retry history își păstrează coloanele și conținutul propriu. |
| STYLE-UNIFY-09 | DONE | P2 | Contractul de disclosure păstrează focus, glyph `＋/−`, label și count dinamic. | Click-urile și stările open/closed au fost verificate în ATF Debug; CSS/DOM contract tests păstrează markerii. |
| DOC-UNIFY-01 | DONE | P2 | Aliniază `atf-reporting/README.md`, README-ul UI Extent și feedback-ul general cu semnătura CSS finală. | Nu rămân reguli documentate care contrazic computed styles din artifactul proaspăt. |

## Scor detaliat

| Dimensiune | Scor | Observație |
|---|---:|---|
| Mapare semantică UI/API | 100 | Aceeași idee de stack și aceeași separare STATUS/DETAILS. |
| Disclosure behavior și accesibilitate | 100 | `<details>/<summary>`, glyph și click funcționează în ambele. |
| Outer contour și accent vertical | 100 | Shell-ul și accentul sunt protejate prin tokeni și contract. |
| Badge și coloană STATUS | 100 | Geometria și lățimea sunt comune și verificate. |
| Sibling separators | 100 | Nu există delimitatoare decorative între zone; grilele interne rămân. |
| Grilele interne | 100 | Acțiunile, assertions, JSON și retry history își păstrează semantica. |
| Screenshot inline UI | 100 | Linkul emoji este inline, vertical centrat și asociat cu action table. |
| JSON/transport evidence API | 100 | Request, response, JSON și attempts sunt separate și auditabile. |
| Responsive/overflow | 100 | Overflow-ul este local tabelului și verificat la normal/narrow. |
| Trasabilitate UI → API | 100 | UI-ul folosește targetul API generat în aceeași execuție; perechea este copiată în `artifacts/reports/`. |

**Scor general de aliniere:** **100/100** pentru incrementul livrat. Shell-ul
comun, tokenii, sibling geometry, JSON transport evidence, screenshot inline și
matrix-ul cross-report au dovadă în cod, teste și artifactele fresh.

## Gate pentru implementarea ulterioară

Rendererul UI/API și UI-22/TRACE-PAIR-01/02 sunt închise. Gate-ul final executat
în staging-ul `.tmp/style-unification-20261016-231000` a fost:

1. sincronizat codul live în workspace;
2. făcut build izolat în `.tmp`, fără oprirea rulărilor curente;
3. trecut `ReportEvidenceStyleContractTest` (`1/1`) și
   `AtfReportEvidenceCssTest` (`3/3`);
4. rulat `UiReportAuditBrowserMatrixTest` (`4/4`), cu cazurile
   `empty-users-posts-normal`, `failure-lifecycle-narrow`,
   `write-methods-all-api-normal` și `write-methods-all-api-narrow`; cazul
   write normal a reconciliat `39` request-uri și metodele `GET, POST, PUT,
   PATCH`;
5. generat artefactele UI/API fresh, inclusiv perechea auditată de mai sus;
6. deschise exact aceste artefacte prin ATF Debug UI/browser MCP, pe porturile
   staging `55892` (UI) și `55893` (API);
7. expandate pe rând request/response/execution și action/audit/assertions;
8. verificată randarea, computed style, click-ul, `＋/−`, JSON-ul, linkul
   `📷 base64 img`, delimitările și overflow-ul la viewport normal/narrow;
9. copiate doar artefactele validate în `artifacts/reports/` și
   `artifacts/reports/ui/`;
10. actualizat feedback-ul numai după ce hash-urile și raportul final au fost
    verificate.

ToDo-urile de aici rămân doar protecții de regresie și eventuale extensii de
coverage; nu redeschid UI-22 și nu confundă tabelele interne cu delimitările
dintre zone.

