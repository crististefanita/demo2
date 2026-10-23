# ATF — feedback API pentru Automation Exercise

Documentul păstrează diferența dintre modelarea locală a API-ului AutoEx,
coverage-ul determinist și verificarea serviciului public. Rutele și testele
componente sunt deja în ownerul
[`atf-test-auto-ex`](../../atf-test-auto-ex/README.md); aici rămâne doar
coverage-ul incomplet care afectează corectitudinea și siguranța execuției.

## AUTOEX-API-CONTRACT-01 — contracte API complete și cauzalitate UI/API

**Stare: parțial implementat, deschis. Prioritate recalibrată: 89/100**
(impact 38 + lacună 25 + lucrabil 18 + datorie documentară 8). Catalogul de rute,
request factory-ul și matricea deterministă există; nu sunt încă probe ale
contractelor răspunsului serviciului public sau ale operațiilor mutabile live.
Auditul a găsit și o ambiguitate de contract ce trebuie rezolvată înainte ca
statusurile curente să fie considerate dovadă.

### Situația verificată în cod

- `AutoExApiRoute` deține opt rute publicate; `AutoExApiRequestFactory` compune
  request-uri GET, query și form, iar `AutoExApiAccount` modelează câmpurile
  create/update și redactează parola din `toString()`.
- `AutoExApiContractComponentTest` enumeră toate cele 14 scenarii din API List,
  trece fiecare request prin `ApiExecutor` și un transport determinist și
  verifică metoda, ruta, host-ul fixture, transport HTTP fixture `200`, câmpul
  JSON `responseCode`, fragmentul semantic minim din body și un singur exchange.
  Testele sunt offline și fac parte din `AutoExComponentSuite`; fixture-ul separă
  acum statusul de transport de codul documentat, dar nu dovedește statusurile
  reale ale serviciului.
- Testul suplimentar verifică form-ul de login, query-ul pentru user detail și
  redactarea `toString()` a modelului; nu verifică fiecare body/header/query,
  nici schema semantică a tuturor răspunsurilor.
- Statusurile și body-urile sunt furnizate de test fixture; testul nu face
  request la `automationexercise.com`, deci nu certifică serviciul live.
- API List numește valorile „Response Code”, iar corpul API are propriul câmp
  `responseCode`. Testul live compară acum acea valoare cu JSON-ul și raportează
  separat statusul HTTP observat; nu impune încă un status HTTP exact, doar
  respinge transportul non-2xx/3xx/4xx. Confirmarea statusului HTTP așteptat,
  endpoint cu endpoint, rămâne deschisă. În special, nu susținem că `201` la
  API 11 este neapărat HTTP 201.
- `AutoExApiLiveContractTest` și `AutoExApiLiveSuite` există separat și sunt
  dezactivate implicit. Suita construiește un cont sintetic unic, armează
  cleanup înainte de create și verifică `responseCode` la închidere. Verifică
  `responseCode` JSON pentru toate scenariile și emailul contului sintetic în
  obiectul user de la detail; statusul HTTP este separat în diagnostic și are
  numai un guard generic. Nu a fost executată în această validare; compatibilitatea
  contului, statusurilor HTTP exacte, mesajelor, schemelor și cleanup-ului live
  rămâne nedovedită.
- `AutoExPurchaseUiApiE2eTest` observă traficul din browser, însă
  `GET /api/productsList` este precondiție de setup înaintea căutării. Nu este
  dovadă că o acțiune UI a cauzat acel exchange; baseline-ul de trafic îl
  exclude din numărătoarea cauzală, iar proba componentă verifică izolarea.
  Căutarea UI folosește `/products?search=...`, nu `POST /api/searchProduct`.
- Testul API live nu publică un raport Extent dedicat pentru contractele directe.
  Nici redactarea secretelor din request evidence, replay manual, consolă și
  raport nu este demonstrată end-to-end; verificarea `toString()` acoperă numai
  reprezentarea modelului.

### Suprafața publică modelată

Matricea urmărește cele 14 scenarii publicate de
[Automation Exercise API List](https://automationexercise.com/api_list). Sunt
așteptări documentate ale serviciului, nu rezultate live obținute de ATF.

| Scenariu | Request | Documented JSON `responseCode` | Efect și izolare |
|---|---|---:|---|
| API 1 | `GET /api/productsList` | 200 | read-only |
| API 2 | `POST /api/productsList` | 405 | metodă neacceptată |
| API 3 | `GET /api/brandsList` | 200 | read-only |
| API 4 | `PUT /api/brandsList` | 405 | metodă neacceptată |
| API 5 | `POST /api/searchProduct`, `search_product` prezent | 200 | căutare fără mutație |
| API 6 | `POST /api/searchProduct`, parametru lipsă | 400 | validare input |
| API 7 | `POST /api/verifyLogin`, credențiale valide | 200 | cont fixture deținut de test |
| API 8 | `POST /api/verifyLogin`, email lipsă | 400 | validare input |
| API 9 | `DELETE /api/verifyLogin` | 405 | metodă neacceptată |
| API 10 | `POST /api/verifyLogin`, credențiale invalide | 404 | fără conturi personale/shared |
| API 11 | `POST /api/createAccount` | 201 | cont sintetic unic; cleanup armat înaintea create |
| API 12 | `DELETE /api/deleteAccount` | 200 | șterge numai contul creat de test |
| API 13 | `PUT /api/updateAccount` | 200 | actualizează numai contul sintetic al testului |
| API 14 | `GET /api/getUserDetailByEmail` | 200 | citește numai identitatea sintetică a testului |

### Cerințe rămase

1. Distincția cod/JSON este implementată în testele componente și în aserțiunile
   live. Rămâne să stabilești statusul HTTP așteptat pentru fiecare endpoint din
   capturi reale sau dintr-un contract upstream explicit și să-l validezi
   separat; nu deduce HTTP 201 pentru API 11 numai din „Response Code: 201”.
2. Păstrează request factory-ul și testul offline existente; nu recrea încă o
   matrice de rute. Întărește contractul component pe fiecare caz: form/query,
   header-ele relevante, encoding, body absent/prezent și aserțiuni pe câmpurile
   semantice ale răspunsului, inclusiv cazurile negative. Evită snapshot-uri ale
   întregului catalog.
3. Separă explicit trei niveluri de dovadă: request-model local, componentă prin
   transport fixture și integrare live opt-in. Nu prezenta statusul fixture-ului
   ca status observat la serviciu.
4. Suita live există și nu pornește în buildul implicit. Baza URL și timeout-ul
   sunt configurabile; contul este sintetic și unic. Destinația raportului nu
   este încă o opțiune dedicată a suitei API live.
5. Rulează suita numai cu opt-in explicit și verifică serviciul real. Cleanup-ul
   este armat înainte de create și try-with-resources păstrează excepția din
   corp drept cauză primară, atașând o eroare de cleanup ca suppressed; totuși,
   trebuie validat live că ștergerea reușește după create/update și pe o cale de
   eșec controlată. Testele offline nu substituie această probă.
6. Redactează secretele înainte de log, retry history, manual replay și evidence.
   Testul actual al `toString()` este necesar, dar nu demonstrează redactarea
   body-ului `ApiRequest` sau a proiecțiilor Reporting.
7. În `UI_API`, etichetează request-urile de setup separat și leagă traficul
   cauzal numai de checkpoint-ul/acțiunea observată. Inspectorul agregat nu
   înlocuiește contractul direct și nu dublează exchange-uri.
8. Păstrează testele API departe de purchase E2E: journey-ul rămâne business
   flow, iar matricea endpoint-urilor are suite și raport dedicate.

### Criterii de închidere

- Componenta offline verifică request și response semantic pentru toate cele
  14 scenarii și eșuează la mutarea/eliminarea unui caz fără a apela serviciul.
- Contractul transportului și `responseCode` din JSON au aserțiuni distincte;
  statusurile HTTP exacte sunt adăugate numai după confirmarea sursei lor.
- Suita live opt-in a fost executată controlat și dovedește status/schema
  observate; operațiile pe cont demonstrează identitate unică și cleanup pe
  success și failure fără eclipsarea cauzei primare.
- Secretele lipsesc din toate suprafețele publicate, inclusiv request body,
  replay manual, consolă și raport.
- Dovada UI/API separă precondiția de request-urile cauzate de browser; fiecare
  exchange are un singur owner și un singur loc de publicare.
- README-ul AutoEx descrie nivelul offline efectiv, iar acest feedback păstrează
  numai diferențele încă deschise.
