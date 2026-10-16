# ATF Java — refactor pentru constante și enum-uri

Status: `IN PROGRESS` — incrementul pentru `MockReportContractAuditorTest` este
implementat și validat izolat; auditul complet al tuturor testelor API rămâne
deschis.

## Scop

Testele API și testele UI care auditează raportul trebuie să folosească aceeași
ierarhie de surse pentru valori. Nu extragem mecanic orice string într-o clasă
globală; extragem numai vocabularul sau datele care au ownership și reutilizare
reală.

```text
enum-uri core / mock
        ↓
RoutePaths + ApiFixtureData + route builders
        ↓
Api*Scenarios / MockReportContractFixtures
        ↓
teste API și validatori UI Extent
```

## Regula de ownership

| Valoare | Owner corect | Exemplu |
|---|---|---|
| verb HTTP | enum transport | `HttpMethod.POST` |
| nume header | enum transport | `HttpHeader.CONTENT_TYPE` |
| media type | catalog media type | `MediaTypes.APPLICATION_JSON` |
| rută publică | catalog de rute | `RoutePaths.POSTS` |
| identitate/payload între familii | fixture semantic | `ApiFixtureData.CREATED_POST_ID` |
| comportare în mock cu vocabular închis | enum mock | `MockRequestBodyMode.JSON` |
| cheie de evidence a raportului mock | enum auditor | `MockReportEvidenceField.RESPONSE_STATUS` |
| valoare strict locală unui caz | fixture-ul testului | `BODY_MISMATCH` |

Un endpoint, header, media type sau sentinel (`*`, `/*`) nu se repetă în
metodele de test. Un payload folosit de un singur caz nu devine constant public
doar pentru a elimina un literal; rămâne într-un route builder sau într-un
fixture local cu nume semantic.

## Increment implementat — `MockReportContractAuditorTest`

- [x] Verbele, ruta demo, statusurile, URL-ul mock și media type-ul sunt
  deținute de `MockReportContractFixtures`, `HttpMethod`, `MediaTypes` și
  `ApiEnvironment`.
- [x] Header-ele și cheile de evidence sunt tipizate prin `HttpHeader` și
  `MockReportEvidenceField`; nu mai există chei duplicate în test și validatori.
- [x] `MockRoute.ANY_METHOD` și `MockRoute.ANY_PATH` sunt sentinelele canonice
  pentru fallback, iar `MockRoute.ANY_AUTH_PROFILE` este sentinelul separat
  pentru wildcard-ul de autentificare; nu se refolosește un nume de metodă HTTP
  pentru altă responsabilitate.
- [x] `MockRequestBodyMode` închide vocabularul `json` /
  `wrong-content-type`, păstrând exact valorile YAML existente.
- [x] Semnalele de failure din test sunt grupate într-un enum semantic, nu în
  fragmente anonime repetate.
- [x] Testul păstrează diferența dintre datele comune și fixture-urile negative
  strict locale; nu ascunde expectation-ul în implementarea auditorului.

Validare izolată 2026-10-16: reactorul Maven complet până la `atf-tests` a
compilat în `.temp/constants-refactor-20261016-final2`; build-ul de staging nu
a atins `target/` live, rulările live sau sursele de producție.

## Reconciliere ATF — ce este deja uniform și ce nu trebuie forțat

Auditul nu tratează orice string ca pe o constantă. Boundary-ul actual este:

| Zonă | Verdict live | Regula aplicată |
|---|---|---|
| `atf.api.tests.api` production | uniform | rutele sunt în `RoutePaths`, datele comune în `ApiFixtureData`, valorile protocolului în enum/catalog, iar variantele sunt în route builders; testele API nu dețin browser sau Extent UI |
| `MockReportContractAuditorTest` | uniform | testul exprimă doar intenția; valorile și contractele de mock/evidence sunt în `MockReportContractFixtures`, `MockRequestBodyMode` și `MockReportEvidenceField` |
| `ApiNegativeRoutes` | corectat în acest increment | auth-ul standard folosește `ApiEnvironment.NO_AUTH_PROFILE`; payload-urile negative rămân locale și numite semantic |
| `atf-mock` test fixtures | încă mixt | `AtfMockTransportTest` și `ApiMockEvidenceAuditorTest` conțin încă rute/header-e/base URL inline; sunt următorul lot, nu se ascund printr-un `AtfConstants` global |
| assertion labels (`GET`, `POST`, `PUT`) | parțial mixt | în contractele de transport trebuie folosit `HttpMethod`; în mesajele explicative ale unei aserțiuni textul poate rămâne dacă nu este identitate de rută |

### Ordinea următoare, după impact

1. **P1 — 85/100:** refactorizează `AtfMockTransportTest` într-un fixture
   semantic al modulului mock; reutilizează `HttpMethod`, `HttpHeader`,
   `MediaTypes`, `ApiEnvironment` și `MockRoute` fără a muta payload-uri de
   edge-case într-un catalog global.
2. **P2 — 70/100:** aliniază `ApiMockEvidenceAuditorTest` cu același model;
   ruta `/users`, fallback-ul și body-urile trebuie să aibă ownership local
   explicit, nu literale anonime în fiecare metodă.
3. **P3 — 55/100:** introdu un architecture guard îngust pentru duplicatele
   canonice (`/public/v2`, header-e, media type, auth default) în teste. Guard-ul
   trebuie să ignore JSON edge-case și texte de explicație intenționate.
4. **P4 — 35/100:** înlocuiește string-urile `GET`/`POST`/`PUT` din helper-ele
   de flow care reprezintă efectiv un verb, păstrând textul liber doar în
   descrieri de business și mesaje de diagnostic.

Acestea sunt ToDo-uri reale, nu defecte ale incrementului curent. Ele rămân în
acest document până când fiecare lot are un owner semantic, un test de regresie
și o validare în staging.

## Evidence de staging și randare

Buildul final izolat a trecut `9/9` teste selectate: `MockRequestBodyModeTest`
`2/2`, `MockReportContractAuditorTest` `6/6`, `ApiNegativeMatrixTests` `1/1`,
`ApiRouteCoverageMatrixTest` `1/1` și `ApiFixtureDataTest` `1/1`. Artifactul API
generat este:

[`api-atf-extent-report_2026-10-16_23-18-16-340.html`](../../artifacts/reports/api-atf-extent-report_2026-10-16_23-18-16-340.html)

Acesta a fost servit prin ATF Debug UI, deschis în in-app browser și verificat în
`Tests`, `Overview` și `Fails`. `Tests` afișează test body-ul și pașii individuali
colapsați by default; `Overview` raportează 1 test/1 step passed; `Fails` afișează
explicit `No failed tests.`. Nu a fost folosit browser Windows real și nu au
fost oprite rulări live.

## Audit ATF rămas

- [ ] Auditează testele din `atf.api.tests.api` pentru literaluri repetate care
  nu sunt încă deținute de `RoutePaths`, `ApiFixtureData`, route builders sau
  enum-uri core.
- [ ] Centralizează numai valorile de header/auth/query repetate între două sau
  mai multe scenarii; valorile unice de edge-case rămân în builderul scenariului.
- [ ] Adaugă un guard arhitectural care detectează duplicatele canonice în
  metodele de test fără să interzică payload-urile locale intenționale.
- [ ] Verifică naming-ul constantelor și elimină clasele `Constants` generice;
  ownership-ul trebuie să fie semantic (`RoutePaths`, `ApiFixtureData`,
  `MockReportEvidenceField`), nu doar tehnic.

## Criteriu de închidere

Refactorul este închis numai după:

1. testele API și auditorul UI compilează și trec în build izolat `.temp`;
2. YAML-ul mock rămâne backward-compatible pentru `request-body-mode`;
3. nu se schimbă contractul JSON POST/PUT sau formatul raportului Extent;
4. un artifact API/UI fresh este inspectat prin ATF Debug UI/browser MCP;
5. ToDo-urile rămase sunt fie închise și mutate în documentația stabilă, fie
   păstrate aici cu owner și criteriu verificabil.

## Anti-pattern-uri interzise

```java
// greșit: vocabular tehnic și route identity ascunse în metoda de test
new MockRoute("POST", "/public/v2/posts", 201, "...", ...);

// corect: vocabular tipizat, datele de scenariu într-un fixture semantic
new MockRoute(HttpMethod.POST.value(), RoutePaths.POSTS, CREATED_STATUS,
        CREATED_RESPONSE_BODY, JSON_HEADERS, NO_DELAY);
```

Nu se introduce `AtfConstants` ca sertar global. O constantă este justificată
prin ownership, reutilizare sau contract extern; altfel, un nume local semantic
este mai clar decât o abstracție artificială.
