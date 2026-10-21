# ATF Reporting — gap-uri active

## Overall

Scorul curent de maturitate este **88/100**; nu este verdict de release și nu
este scorul de prioritate din registrul central. Contractele închise sunt
promovate în [README-ul `atf-reporting`](../../atf-reporting/README.md),
[README-ul `atf-reporting-tests`](../../atf-reporting-tests/README.md) și
[contractul de raportare](../ATF_Reporting_Contract.md). Acest fișier păstrează
numai gap-urile care cer încă implementare, decizie sau dovadă.

`atf-reporting` rămâne librăria standalone; `atf-reporting-tests` deține
compoziția, fixture-urile și browser proof-ul. Limitele Practice Shop au owner
separat și nu se numără ca defecte ale librăriei.

## ToDo-uri active

### `REP-UX-TABLE-03` — viewport comun pentru toate tabelele scrollabile

Tokenul `AtfReportThemeTokens.TABLE_SCROLL_HEIGHT` există și este consumat de
familiile UI/API, dar proba vizuală încă nu acoperă complet tabelele scurte și
lungi la viewport standard și îngust.

**Închidere:**

- toate tabelele scrollabile folosesc același plafon vertical ca
  `System/Environment` (`--atf-table-scroll-height`); un tabel scurt nu rezervă
  spațiu gol inutil;
- tabelul lung permite scroll până la ultimul rând, păstrând headerul sticky;
- nici Category, nici alt tabel nu este tăiat lateral la viewport standard sau
  îngust, în Extent și fallback;
- browser proof fresh măsoară `clientWidth/scrollWidth`, `clientHeight/scrollHeight`
  și `scrollTop` pentru cazurile scurt/lung.

### `REP-UX-SUMMARY-01` — agregările tehnice cu summary de două ori mai înalt

Geometria de bază este implementată la `34px`, însă tokenul și testele live nu
definesc excepția cerută pentru `Unique request signatures` și
`Technical diagnostics`: summary-ul fiecăruia trebuie să aibă de două ori
înălțimea de bază și să rămână collapsed implicit. Această prezentare nu schimbă
datele, deduplicarea sau statusul.

**Închidere:** token/selector explicit pentru cele două summary-uri, teste CSS
pentru Extent și fallback, apoi artifact fresh verificat vizual; celelalte
summary-uri își păstrează geometria compactă standard.

### `REP-UX-SYSTEM-01` — preview-ul System separat de limita JSON

`reporting.bodyMaxDisplayCharacters` este limita JSON-ului din Request/Response,
nu limita unui preview compact din `Overview → System/Environment`. Refolosirea
ei pentru preview ar trunchia evidence-ul. `networkInspector.maxBodyPreviewCharacters`
este o setare diferită și nu trebuie reutilizată.

**Închidere:** setare separată, configurabilă, pentru preview-ul System (default
100 caractere); System publică numai valoarea explicativă/bounded, iar JSON-ul
request/response își păstrează limita și disclosure-ul propriu. Testează
redaction, truncare și paritatea Extent/fallback.

### `REP-COMP-API-01` — selecția exactă API până la publisher

Există probe separate pentru catalog, manifest, setup, mock și artifact; lipsește
o probă componentă unică pentru traseul selecție exactă → setup → mock/lifecycle
→ publisher → artifact semantic. `SelectedApiTestsReportUiTest` rămâne browser
proof și nu înlocuiește această probă.

**Închidere:** testează pozitiv și negativ selecția exactă, ownerii/ordinea/
statusurile, request/response/execution și artefactul publicat fără browser sau
fixture Practice Shop. Clarifică și fixează precedența dintre
`@ApiScenarioSetup(tests=...)` și override-ul JVM de familii; documentația și
comportamentul trebuie să exprime aceeași regulă.

## Regula de verificare

- modelul și deduplicarea se verifică în unit/component;
- integrarea setup → transport mock → publisher se verifică în integration;
- browserul se folosește numai pentru randare, navigare și tranziții de
  disclosure, pe artifact fresh;
- build-ul și artifactele noi se produc într-un staging izolat, conform
  [Build and Execution Safety](../ATF_Build_And_Execution_Safety.md);
- un finding se închide numai după criteriul propriu, nu printr-un test verde
  fără legătură.

Trace per checkpoint, inspectorul aggregate și stabilitatea disclosure-urilor
sunt contractate în [ATF Reporting Contract](../ATF_Reporting_Contract.md) și
[ATF Testing Guide](../ATF_Testing_Guide.md); nu sunt ToDo-uri duplicate aici.
