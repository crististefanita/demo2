# ATF — feedback API pentru Automation Exercise

Documentul păstrează diferența dintre modelarea locală a API-ului AutoEx,
coverage-ul determinist și verificarea serviciului public. Rutele și testele
componente sunt deja în ownerul
[`atf-test-auto-ex`](../../atf-test-auto-ex/README.md); aici rămâne doar
coverage-ul incomplet care afectează corectitudinea și siguranța execuției.

## AUTOEX-API-CONTRACT-01 — contracte API complete și cauzalitate UI/API

**Stare:** parțial implementat, deschis. **Prioritate:** 84/100 (impact 38 +
lacună 22 + lucrabil 18 + datorie documentară 6).

Matricea offline acoperă separat request-ul și răspunsul pentru toate cele 14
scenarii. Publisher-ul Extent pentru API direct compilează acum și are o probă
componentă offline pentru schimb, aserțiuni JSON, configurarea directorului și
redactarea parolei din form. Rămân nedovedite contractele reale ale serviciului,
statusurile HTTP exacte și cleanup-ul live.

### Situația verificată în cod

- `AutoExApiRoute` deține opt rute publicate; `AutoExApiRequestFactory` compune
  request-uri GET, query și form, iar `AutoExApiAccount` modelează câmpurile
  create/update și redactează parola din `toString()`.
- `AutoExApiContractComponentTest` enumeră toate cele 14 scenarii din API List,
  trece fiecare request prin `ApiExecutor` și un transport determinist și
  verifică metoda, ruta, host-ul fixture, transport HTTP fixture `200`, câmpul
  JSON `responseCode`, mesajul și câmpul JSON semantic așteptat din body, cu un
  singur exchange. Corpurile fixture sunt acum declarate literal per scenariu,
  separat de așteptările parsate; nu mai sunt fabricate de o funcție care deduce
  mesajul din codul de răspuns sau rută. Testele sunt offline și fac parte din
  `AutoExComponentSuite`; fixture-ul separă statusul de transport de codul
  documentat, dar nu dovedește statusurile reale ale serviciului.
- Testele suplimentare verifică vocabulary-ul complet al form-urilor de
  create/update, form-ul de login/delete, `Content-Type` la DELETE, query-ul
  user-detail, encoding-ul căutării și redactarea parolei din `toString()`.
  Matricea offline declară acum independent form fields și query pentru fiecare
  din cele 14 request-uri și verifică în transport metoda, ruta, body-ul decodat,
  query-ul și `Content-Type` absent/prezent. Schema JSON verificată este
  intenționat minimă (cod, mesaj sau câmp-cheie), nu un contract complet tipizat;
  nu este un oracle upstream și nu certifică serviciul public.
- Statusurile și body-urile sunt furnizate de test fixture; testul nu face
  request la `automationexercise.com`, deci nu certifică serviciul live.
- API List numește valorile „Response Code” și descrie separat „Response JSON”; nu
  precizează dacă acel cod este statusul HTTP sau un câmp JSON. Testul live
  verifică proprietatea JSON `responseCode` și raportează separat statusul HTTP
  observat; guard-ul generic acceptă doar transport 2xx–4xx. Până la o captură
  live, mapping-ul dintre documentație, JSON și transport rămâne nedovedit. În
  special, nu susținem că `201` la API 11 este neapărat HTTP 201.
- `AutoExApiLiveContractTest` și `AutoExApiLiveSuite` există separat și sunt
  dezactivate implicit. Suita construiește un cont sintetic unic, armează
  cleanup înainte de create și verifică `responseCode` JSON pentru toate
  scenariile; pentru detail verifică și identitatea contului. Statusul HTTP este
  separat și are doar un guard generic. Nu a fost executată în această validare;
  statusurile HTTP exacte, mesajele, schemele și cleanup-ul live rămân nedovedite.
- În `UI_API`, `GET /api/productsList` este publicat în checkpoint-ul explicit
  de precondiție; journey-ul marchează baseline-ul după acel checkpoint, iar
  agregatul ulterior exclude traficul observat până acolo. Testul componentei
  verifică acum și ordinea API request → checkpoint de precondiție → baseline;
  o precondiție eșuată nu poate avansa baseline-ul. Artifactul UI/API generat
  live trebuie totuși inspectat pentru a confirma vizual exchange-ul o singură
  dată, separat de agregatul cauzal. Căutarea UI folosește `/products?search=...`,
  nu `POST /api/searchProduct`.
- `AutoExApiContractReportExtension` publică fiecare exchange direct în raportul
  Extent al clasei; directorul este configurabil prin
  `atf.autoex.api.report-directory` (implicit `artifacts/reports/api/auto-ex`).
  Aserțiunile pentru JSON `responseCode` și `message` sunt atașate schimbului,
  iar `@AtfReportPath` poate emite calea artifactului.
- `AutoExApiContractReportPublisherComponentTest` generează offline HTML real și
  verifică schimbul, aserțiunile JSON și faptul că parola form-urlencoded este
  redactată. Verifică și formatterul manual replay pe același secret sintetic,
  precum respingerea căilor absolute sau cu traversare. Nu execută rețeaua și nu
  probează output-ul de consolă dintr-o rulare live.
- La eșec înaintea publicării unui exchange, extensia scrie acum un diagnostic
  `JUnit failure` compact în `TEST BODY`, cu maximum patru excepții înrudite
  (cause și suppressed) și redactorul HTML existent. Proba componentă injectează
  o cauză cu bearer token și o eroare de cleanup suppressed; confirmă că ambele
  explicații rămân, dar secretul nu apare în HTML.
- `AutoExApiReadOnlyLiveSuite` selectează numai `GET /api/productsList`,
  `GET /api/brandsList` și `POST /api/searchProduct`; nu mută conturi. ATF nu a
  emis încă aceste schimburi live: inițializarea `HttpClient` eșuează înaintea
  primului request cu `Unable to establish loopback connection`. Probe read-only
  independente au confirmat răspunsurile API 1, 3 și 5; câmpul de produse pentru
  API 5 este acum aliniat la `products` în sursa și testul local. Aceste probe nu
  înlocuiesc transportul ATF și raportul său.

Matricea stabilă a celor 14 cazuri publicate și limitele fixture-ului sunt în
[`atf-test-auto-ex/README.md`](../../atf-test-auto-ex/README.md#acoperirea-celor-14-cazuri-publicate-în-api-list).
Valorile acelei matrice sunt așteptări `responseCode` din JSON, nu statusuri HTTP
confirmate pentru fiecare rută live.

Matricea offline, publisher-ul direct API, redacția și separarea suitei API de
purchase E2E sunt contracte deja implementate și documentate în README-ul
ownerului; nu sunt backlog separat.

### Cerințe rămase

1. API List publică „Response Code” separat de „Response JSON”. Rămâne deschisă
   o verificare live controlată care compară statusul HTTP cu proprietatea JSON
   `responseCode` pentru rutele acoperite. Nu echivala valorile și nu afirma că
   API 11 are HTTP 201 doar pentru că JSON-ul publică `responseCode: 201`.
2. Destinația raportului direct API este implementată și configurabilă prin
   `atf.autoex.api.report-directory`; path-ul este limitat la directorul
   proiectului, iar generarea/redactarea HTML sunt probate de componenta offline.
   Rămâne inspecția unui raport cu request/response live efectiv.
3. Suita read-only este izolată de mutații, dar ATF nu a emis încă schimburi
   live: inițializarea `HttpClient` eșuează înaintea primului request cu
   `Unable to establish loopback connection`. Rămâne de rulat într-un runtime
   unde loopback-ul JVM funcționează. Suita mutabilă trebuie rulată separat și
   controlat, verificând status/schema și cleanup-ul real după create/update,
   inclusiv pe o cale de eșec; fixture-urile offline și cererile independente nu
   substituie aceste probe.
4. Redactarea HTML și a replay-ului este probată componentar. Rămâne verificarea
   output-ului consolei și a formei raportului într-o execuție cu exchange-uri
   live, fără duplicarea testelor generice ale rendererului.
5. În `UI_API`, păstrează `GET /api/productsList` în checkpoint-ul explicit de
   precondiție și baseline-ul imediat după acesta. Proba componentă de wiring
   protejează request → checkpoint → baseline și eșuează dacă checkpoint-ul nu
   trece. Rămâne de verificat în artifactul UI/API live că agregatul conține
   numai traficul ulterior, fără dublarea exchange-ului. Inspectorul agregat nu
   înlocuiește contractul API direct.

### Criterii de închidere

- Componenta offline verifică request și response cu oracle-uri separate pentru
  toate cele 14 scenarii, inclusiv corpuri JSON fixture literale. Testele nu
  apelează serviciul public și nu sunt prezentate ca dovadă live — îndeplinit.
- Contractul transportului și `responseCode` din JSON au aserțiuni distincte;
  statusurile HTTP exacte rămân condiționate de confirmarea sursei — deschis.
- Suita read-only live este izolată de mutații; exchange-urile nu sunt încă
  dovedite prin transportul și raportarea ATF, deoarece inițializarea clientului
  JVM eșuează înaintea primului request — deschisă.
- Contractul live mutabil, inclusiv cleanup real pe success și failure, nu a
  fost executat — deschis.
- Publisher-ul API direct, directorul configurabil, redactarea și diagnosticul
  de eșec sunt verificate componentar în `AutoExComponentSuite` (27 teste, fără
  trafic public) — îndeplinit la nivel componentă. MCP a verificat artifactul
  de eșec live; vizualizarea unui exchange live rămâne deschisă.
- Redactarea replay-ului pentru form-ul AutoEx este verificată componentar;
  diagnosticul de eșec, excepția suppressed de cleanup și redactarea bearer sunt
  verificate într-un HTML componentar, iar artifactul de eșec a fost verificat în
  MCP. Output-ul consolei dintr-o rulare cu request-uri live rămâne de inspectat
  — parțial deschisă.
- Wiring-ul precondiției UI/API (request → checkpoint → baseline), inclusiv
  imposibilitatea de a avansa baseline-ul după un checkpoint eșuat, este
  verificat componentar. Verificarea raportului browser-generated pentru
  precondiție unică și agregat strict cauzal rămâne deschisă.
- Separarea suitei API față de purchase E2E este deja implementată — îndeplinită.
