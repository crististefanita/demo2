# ATF Reporting — gap-uri active

## Overall

| Reper live · 21 octombrie 2026 | Rezultat | Limită explicită |
|---|---|---|
| Maturitate reporting | **90/100** | nu este verdict de release sau prioritate de intervenție |
| Teste componente focalizate | **27 passed** | 14 CSS/network + 13 overview/setup/selecție API; nu măsoară geometria în browser și nu acoperă publisher-ul într-un singur vertical slice |
| Aggregate scroll | clasele și stilurile shared sunt implementate | măsurătorile browser pentru toate viewports încă lipsesc |

`atf-reporting` rămâne librăria standalone; `atf-reporting-tests` deține
compoziția, fixture-urile și browser proof-ul. Limitele Practice Shop au owner
separat. Acest fișier păstrează doar gap-urile care cer încă implementare,
decizie sau dovadă.

## ToDo-uri active

### `REP-UX-TABLE-03` — viewport comun pentru toate tabelele scrollabile

Tokenul `AtfReportThemeTokens.TABLE_SCROLL_HEIGHT` există și este consumat de
familiile UI/API, dar proba vizuală încă nu acoperă complet tabelele scurte și
lungi la viewport standard și îngust.

Regresia de clipping din ultima coloană a tabelului de acțiuni Playwright a fost
corectată: la 591px, raportul guest real trece toate cele 4 verificări de tab fără
overflow orizontal. Această dovadă închide doar lizibilitatea responsive a acelui
tabel, nu măsurarea completă a viewport-ului scrollabil descrisă mai jos.

Implementarea curentă aliniază explicit și agregările de network: atât `Unique
request signatures`, cât și `Technical diagnostics` folosesc clasa comună
`network-aggregate-scroll`, aceeași înălțime `--atf-table-scroll-height`,
overflow vertical bounded, gutter stabil și overflow orizontal ascuns. Contractul
markup/CSS este acoperit component pentru Extent și fallback, iar un E2E live
fresh a fost expandat manual în TAB MCP pentru ambele secțiuni. ToDo-ul rămâne
deschis până când browser proof-ul măsoară automat geometria cerută pentru
cazuri scurte/lungi, inclusiv `clientWidth/scrollWidth`,
`clientHeight/scrollHeight` și `scrollTop`.

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

### `REP-COMP-API-01` — selecția exactă API până la publisher

Proof-urile curente sunt împărțite:

- `ApiScenarioManifestTest` verifică selecția exactă, ownerii, titlurile,
  statusurile și respingerea scenariilor nealese;
- `ApiReportAuditSetupTest` verifică override-ul JVM pentru adnotările cu
  familii, dar nu testează conflictul cu `tests = ...`;
- mock transportul, lifecycle-ul și browser report-ul au teste separate.

Rămân lipsă testul negativ pentru precedence-ul exact-vs-JVM și o probă
componentă unică până la publisher/artifact semantic. `SelectedApiTestsReportUiTest`
este browser proof și nu înlocuiește probele lower-level.

**Închidere:**

- adaugă cazul negativ direct pentru `tests = ...` față de override-ul JVM;
- compune selecția exactă → setup → mock/lifecycle → publisher → artifact;
- verifică ownerii/ordinea/statusurile și request/response/execution fără browser
  sau fixture Practice Shop.

Precedence-ul curent este documentat în
[`atf-reporting-tests/README.md`](../../atf-reporting-tests/README.md#selecția-scenariilor-pentru-un-raport-api).

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
Separarea preview-ului System de JSON-ul Request/Response este implementată la
consumer și documentată în contractele oficiale; nu este un ToDo generic al
rendererului `atf-reporting`.
