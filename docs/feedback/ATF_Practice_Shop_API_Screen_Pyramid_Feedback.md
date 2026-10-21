# Practice Shop — feedback API/UI și evidence

## Overall

| Reper | Verdict | Dovadă |
|---|---|---|
| Maturitate feature scope | **100/100 · închis** | scor de completitudine a cerințelor livrate, nu verdict pentru ultima rulare sau release |
| Component/support proof · 21 octombrie 2026, 22:23 | **122 passed · 0 fail · 0 skip** | 38 clase selectate explicit; fără E2E și fără integrare externă |
| Guest purchase E2E · 21 octombrie 2026, 21:58 | **1/1 passed** | Surefire local, flow `LIVE / HTTP` |
| API authenticated proof | **1/1 înregistrat anterior** | dovadă istorică; în checkout, ultimul Surefire API este `SKIP` la 18:14 deoarece opt-in-ul implicit este dezactivat |
| UI authenticated proof | **3/3 înregistrat anterior** | dovadă istorică în MCP; authenticated E2E este `SKIP` în checkout-ul implicit, nu este prezentat ca rulat în auditul curent |
| Artifacte | **există local; nu au fost reauditate vizual acum** | ultima verificare vizuală consemnată anterior; request/response și redaction se verifică prin protocolul de mai jos |

Practice Shop deține configurația, fixture-ul, pages/flows și purchase journey.
`atf-reporting` deține proiecția generică, iar `atf-reporting-tests` deține
compoziția librăriei. Nu mutați reguli de business în reporting.

## ToDo-uri active

### `SHOP-GHERKIN-DISPLAY-01` — fir Gherkin uniform în trace-ul purchase

**Prioritate: 68/100 · Owner: `atf-test-practice-shop` · Status: deschis**

Trace-ul business actual are două `GIVEN` și două `WHEN`. Deși Gherkin le
permite, acest purchase journey trebuie să se citească drept un singur fir:

| Parte | Regula de afișare |
|---|---|
| Context inițial | un `GIVEN` |
| Prima acțiune | un `WHEN` |
| Checkpoint-uri intermediare | `AND` |
| Rezultat final | un `THEN` |

Implementarea trebuie să respecte aceste limite:

- normalizează keyword-urile la checkpoint-urile Practice Shop; renderer-ul
  generic nu rescrie pașii altor consumatori;
- păstrează descrieri corecte, ordinea checkpoint-urilor, validările UI/API și
  corelarea exchange-urilor; exclude `SETUP` din trace-ul business;
- închide cu un contract `1 GIVEN → 1 WHEN → 7 AND → 1 THEN` și un raport E2E
  fresh verificat vizual în MCP; nu elimina checkpoint-uri sau exchange-uri;
- tratează freshness-ul authenticated/API separat: `authenticatedE2eEnabled:
  false`, iar mutațiile remote cer opt-in explicit.

## ToDo-uri închise prin dovadă

| ID | Rezolvare chirurgicală | Dovadă verificată |
|---|---|---|
| `SHOP-API-REAL-01` | Dovada authenticated live este închisă pe baza run-ului anterior; contractul API local și guest E2E au regresie curentă. | API `1/1 LIVE / HTTP` este dovadă istorică; Surefire local curent nu îl rerulează cât timp opt-in-ul este `false`. |
| `SHOP-AUTH-09` | Politica este explicită și typed: `PracticeShopAccountFixturePolicy.SOURCE_TEST_RESOURCE`. YAML-ul, loader-ul și constructorul direct acceptă fixture-ul owned în `src/test/resources`; override-urile externe sunt respinse. | 122 teste component/support trec; login-ul MCP și UI authenticated 3/3 sunt dovada consemnată anterior. SETUP publică numai stare redacted și clasificarea fixture-ului. |
| `SHOP-AUTH-10` | Fixture-ul demo este recreabil și deținut în `src/test/resources`; policy-ul nu tratează disponibilitatea drept dovadă de login remote. | Login manual MCP și UI authenticated 3/3 sunt înregistrate anterior; credentialele nu se repetă în feedback sau artifact. |
| `SHOP-TRACE-ORDER-02` | Selectorul de business pentru order acceptă atât `POST /invoices` (authenticated live), cât și aliasul `POST /invoices/guest`; nu mai presupune ruta guest într-un flow autenticat. | Component tests 11/11; UI live a capturat `POST /invoices` cu `201` JSON, iar assertion-ul invoice/UI a trecut. |
| `SHOP-REPORT-BODY-01` | API și UI păstrează body-urile JSON reale ca evidence, distinct de preview-ul System. | Artifactele fresh conțin `REQUEST JSON`, `RESPONSE JSON` și `LIVE / HTTP`; emailul/parola nu apar în HTML. |
| `SHOP-REPORT-SYSTEM-03` | System/Environment rămâne configurație operațională, nu verdict business; setup-ul declară fixture policy fără valori sensibile. | Component tests + raport UI authenticated verificat prin TAB MCP. |

## Contractul fixture-ului authenticated

Fixture-ul este intenționat sintetic și este deținut de modul:

```text
atf-test-practice-shop/src/test/resources/
└── practice-software-testing-account.properties
```

Loaderul și `PracticeShopConfiguration.accountConfigured()` aplică aceeași
regulă. Un path extern este rejectat; un path din `src/test/resources` este
acceptat pentru acest contract de demo. Această decizie este valabilă pentru
aplicația Practice Shop de test și nu este un model pentru credențiale de
producție.

Raportul nu publică emailul, parola sau path-ul credentialului. `SETUP` publică
doar:

- `Account fixture policy` — fixture sintetic owned de modul;
- `Account setup` — disponibil/missing și starea autentificării;
- target generic redacted, fără valori secrete.

## Protocol de închidere

- component tests dovedesc loaderul, policy boundary, metadata și redaction;
- integration dovedește business assertions pe transport real;
- E2E dovedește login, catalog și checkout în UI real;
- dovada vizuală se face pe un raport fresh prin TAB MCP și se verifică prin expand/click:
  `SETUP`, `GIVEN`, `THEN`, `Screen validation`, API validation, request/response
  JSON și screenshot;
- un rezultat mock nu este prezentat ca dovadă de business live;
- dacă se schimbă codul, raportul și scorul, acest feedback se actualizează
  înainte de orice declarație de închidere.

Warning-ul guest `GET /users/me → 401` rămâne un comportament așteptat pentru
traseul guest și nu reprezintă failure.

## Diagnostic known issue, fără ToDo de business

Raportul UI authenticated poate rămâne `Warning` la nivel de scenariu deși
testul Maven și validările business sunt `PASS`.

Semnalele observate prin MCP au fost:

- request-uri `GET /users/me` eșuate/aborted în browser network;
- diagnostice console fără exchange HTTP asociat.

Acestea nu indică o eroare de fixture, login sau checkout. Aggregate-ul a
păstrat 10 exchange-uri canonice, 8 semnături unice și corelarea cu 11 pași
Gherkin. Dacă bootstrap-ul extern se schimbă, subiectul se redeschide în
feedback-ul network-inspector, fără a modifica verdictul business.

Pentru forma generică a trace-ului UI/API vezi [ATF Reporting Contract](../ATF_Reporting_Contract.md),
iar pentru build și artifacte izolate vezi [Build and Execution Safety](../ATF_Build_And_Execution_Safety.md).
