# Feedback — Automation Exercise

Acest fișier păstrează limitele de business și lacunele de integrare specifice
consumerului [`atf-test-auto-ex`](../../atf-test-auto-ex/README.md). Contractul
stabil și flow-ul implementat sunt documentate în README-ul modulului;
arhitectura comună Shop/AutoEx este urmărită în
[feedback-ul dedicat](ATF_Consumer_Test_Architecture_Feedback.md). Lacuna
generică de profile Reporting este prioritizată o singură dată în
[feedback-ul Reporting](ATF_Reporting_Feedback.md#shop-capture-01); secțiunea
AutoEx de mai jos definește integrarea și dovada cerute, nu un backlog duplicat.

## Context și boundary

- Site-ul public este un magazin de practică separat de Practice Shop.
- Consumerul verifică un singur journey real în trei configurații de evidence:
  `UI`, `UI_API` și `UI` cu captură numai la starea terminală. Toate repetă
  aceeași căutare și cumpărare, înregistrare
  în checkout, adresă, plată demo, confirmare, factură și ștergerea contului.
  Flow-ul este bazat pe [Test Case 14](https://automationexercise.com/test_cases#collapse14)
  și [Test Case 24](https://automationexercise.com/test_cases#collapse24).
- Varianta de bază este `UI`: căutarea browserului navighează la
  `/products?search=...`. API-ul documentat `POST /api/searchProduct` este o
  operație separată și nu a fost observat ca rezultat al aceleiași acțiuni UI.
  Varianta experimentală `UI_API` nu lansează acel endpoint separat; ea
  inspectează traficul first-party din sesiunea browserului și pică dacă nu
  observă exchange-uri `/api/`. Rularea live a observat un `GET
  /api/productsList` explicit în checkpoint-ul de precondiție; nu îl atribuim
  click-ului de căutare. Endpoint-urile sunt descrise în
  [API List](https://automationexercise.com/api_list).
- Cele trei clase de business sunt orchestrate de `AutoExPurchaseSuite`, care
  deține un singur raport Extent. Nu păstrăm teste cu DOM/iframe inventat sau
  variante care testează numai etape deja incluse în purchase journey.
- Datele contului și ale plății sunt sintetice, folosite numai pentru formularul
  demo; contul este șters la final. Suportul necesar execuției reale (config,
  page objects, lifecycle și cleanup) nu este o suită separată de teste.

## Proprietate și reutilizare

- `atf-test-kit`, `atf-ui` și `atf-reporting` dețin adnotările, Playwright,
  acțiunile, capturile și Extent. AutoEx le compune direct.
- Rutele, locatorii, datele și paginile Automation Exercise rămân în consumer;
  vocabularul HTML generic aparține `atf-ui`. Practice Shop nu se copiază ca
  pachet: se extrage în ATF numai o capabilitate fără date/configurație de
  business, folosită și probată de consumatori independenți.
- Precondițiile de mediu (consimțământul CMP și vignette-ul vizibil) sunt
  gestionate prin UI-ul real și rămân în trace; ele nu justifică teste consumer
  sintetice și nu înlocuiesc aserțiunile asupra cumpărării.

## Decizii închise

- `AUTOEX-API-01` — decizia inițială de a nu duplica purchase journey-ul UI cu
  API-uri în E2E rămâne valabilă pentru boundary-ul E2E; **nu închide** însă
  verificarea directă a contractelor API. Cerința distinctă de component/API
  coverage este deschisă în `AUTOEX-API-CONTRACT-01` mai jos. Observarea
  Network Inspector nu este substitut pentru testarea directă a endpoint-urilor.

## Rezolvat în codul curent și verificat

- `AUTOEX-E2E-RESILIENCE-01`: cleanup-ul este armat înainte de submit-ul
  `Create Account`. Dacă pagina nu confirmă crearea și nu oferă acțiunea de
  ștergere pentru sesiunea autentificată, cleanup-ul nu mai raportează fals un
  cont existent; dacă heading-ul confirmă crearea dar controlul de ștergere
  lipsește, cleanup-ul eșuează explicit. `AutoExE2eRun.finish` păstrează
  excepția testului și atașează eșecul cleanup-ului ca suppressed failure.
  Componentele verifică decizia NO_OP/DELETE și păstrarea excepției primare;
  suita live a verificat ștergerea reală a contului în toate cele trei rulări.
- `AUTOEX-DATA-01`: prețul din pagina produsului este comparat cu o linie
  completă, normalizată prin trim, nu cu `contains`; `Rs. 500` nu mai acceptă
  accidental `Rs. 5000`. Prețul din cart și valoarea API rămân comparații exacte.
- `AUTOEX-EVIDENCE-MATRIX-01` — **canalele, cardinalitatea capturii și
  compoziția final-only sunt verificate automat/live; inspecția vizuală rămâne
  deschisă**. Rularea anterioară a
  `AutoExPurchaseSuite` a trecut 3/3: UI, UI_API și UI cu captură numai la
  final. Artifactul acelei matrice a fost inspectat vizual la momentul rulării;
  acea dovadă nu certifică layout-ul artifactului țintit ulterior.
- Artifactul matricei precedente arată checkpoint-uri Gherkin separate pentru
  variantele `SCREEN_CHECKPOINTS`; acesta nu este criteriul variantei
  final-only. În UI_API, `GET /api/productsList — Exchange
  1` a rămas atașat checkpoint-ului real care îl observă, cu request/response și
  transport `LIVE / HTTP`.
- Rularea țintită finală din 2026-10-23 a
  `AutoExPurchaseUiFinalScreenshotE2eTest` a trecut **1/1**, inclusiv ștergerea
  contului sintetic. Artifactul nou are exact un `Playwright action sequence`,
  un tabel `atf-ui-action-table`, un `unified-ui-flow`, o referință de screenshot
  și un element `<img>` pentru captura terminală.
- Artifactul țintit a fost verificat structural, nu vizual în browserul MCP; nu
  marcăm această probă drept audit vizual. Verificările vizuale la viewport MCP
  868×648 și consola fără erori/avertismente aparțin artifactului matricei
  inspectat anterior. Nu s-a pretins o verificare separată de `pageerror`.
- Testele componente AutoEx pentru profil, politica de captură, aserțiunile de
  catalog, cleanup și combinarea excepțiilor au trecut; cele patru teste din
  `ReportEvidenceProfileIntegrationTest` verifică și profile diferite per
  scenariu într-o sesiune partajată.

## AUTOEX-FINAL-PROFILE-01 — un singur tabel Playwright și captura terminală

**Stare: implementat și verificat automat/live; inspecția vizuală în browser a
artifactului proaspăt rămâne de confirmat.** Comportamentul raportului nu mai
este un gap de cod.

| Semnal din cod/raport | Comportament curent |
|---|---|
| `AutoExEvidenceProfile.forRun` | `FINAL_SCREENSHOT_ONLY` compune `UNIFIED_FLOW + FINAL_ONLY`; celelalte moduri păstrează checkpoint-uri separate |
| `AutoExEvidenceProfileComponentTest` | verifică mapping-ul complet flow + screenshot policy pentru toate variantele |
| `UiExtentReportSession.startScenario(..., scenarioProfile)` | permite profile explicite per scenariu în raportul partajat, fără dependență de primul copil |
| `AutoExE2eRun` | rezolvă o singură dată `evidenceMode`, folosește același snapshot de configurare și transmite profilul scenariului |
| `AutoExPurchaseUiFinalScreenshotE2eTest` | artifactul live proaspăt are un singur tabel Playwright și o singură captură terminală |

**Ținta:** `FINAL_SCREENSHOT_ONLY` rezolvă un singur profil tipizat
`UNIFIED_FLOW + FINAL_ONLY`, fără adnotare concurentă pentru flow. Profilul se
aplică scenariului, nu se moștenește accidental de la primul test care deschide
raportul comun.

- Un scenariu final-only publică o singură propoziție Gherkin și un singur
  tabel Playwright; fiecare acțiune rămâne prezentă exact o dată, în ordinea
  execuției, cu target, rezultat și durată. Nu se repetă aceeași propoziție
  pentru a găzdui câte un tabel pe checkpoint.
- La succes, o singură captură este atașată validării terminale, după acțiunea
  finală. La eșec înainte de terminal, raportul păstrează cauza și secvența
  disponibilă fără să inventeze o captură de succes.
- În `AutoExPurchaseSuite`, variantele UI, UI_API și final-only pot păstra
  profile diferite în același raport. Flow mode-ul, politica de captură și
  detaliile unui scenariu nu se scurg în frații săi; rezultatul rămâne același
  dacă ordinea claselor se schimbă. Conflictele de configurare pentru același
  scenariu se resping înainte de browser, nu diferențele declarate între
  scenariile suitei.
- În UI_API, exchange-urile rămân evidence reale, legate de checkpoint-ul care
  le-a observat; agregarea UI nu le dublează și nu le mută arbitrar.
- `ShopUiOnlySingleFlowPurchaseE2eTest` demonstrează gruparea `UNIFIED_FLOW`
  într-un singur tabel. Politica sa YAML de screenshot este separată și nu
  dovedește că produce o singură imagine.

**Ownership-ul testelor:**

- `atf-reporting-tests` verifică profile diferite într-un raport comun, izolarea
  scenariilor-surori, independența față de ordinea inițializării și HTML-ul
  rezultat, folosind fixture-uri neutre/mock.
- Testele componente AutoEx verifică mapping-ul adnotării la
  `UNIFIED_FLOW + FINAL_ONLY` și compoziția suitei; nu clonează rendererul.
- E2E-ul real confirmă integrarea consumerului; închiderea vizuală cere
  inspecția artifactului proaspăt randat.
- Gap-ul generic se închide o singură dată sub `SHOP-CAPTURE-01` în feedback-ul
  Reporting; acest ID AutoEx este criteriu consumer, nu scor duplicat.

**Dovadă live — 2026-10-23:** `AutoExPurchaseUiFinalScreenshotE2eTest` a trecut;
`AutoExEvidenceProfileComponentTest` a trecut, iar cele patru teste din
`ReportEvidenceProfileIntegrationTest` au trecut. În HTML-ul generat, numărul
de `Playwright action sequence`, tabele `atf-ui-action-table`, validări
`unified-ui-flow`, referințe screenshot și elemente `<img>` este câte unul.
Browserul MCP a refuzat deschiderea artifactului local `file://`, deci nu
marcăm confirmarea vizuală drept efectuată; pentru aceasta este necesară o
captură a raportului afișat în tabul MCP.

<a id="autoex-api-contract-01"></a>

## AUTOEX-API-CONTRACT-01 — coverage API directă și cauzalitatea UI/API

**Stare: deschis. Prioritate: 93/100.** Extinde consumerul cu verificări API
separate, după modelul de testare componentă/API din Practice Shop, fără să
transforme purchase E2E-ul într-o matrice de endpoint-uri.

### Dovezi din codul curent

- Catalogul de rute AutoEx conține doar `PRODUCTS_API` (`GET /api/productsList`);
  nu există rute, request factory sau teste API directe pentru celelalte
  endpoint-uri publicate.
- Singurul `fetch(...)` din codul consumerului este în
  `AutoExE2eRun.inspectCatalogApi`. `AutoExPurchaseJourney` îl invocă drept
  precondiție înainte de deschiderea paginii Products și înainte ca utilizatorul
  să trimită căutarea UI.
- Raportul recent al journey-ului UI_API conține un singur exchange `/api/`,
  `GET /api/productsList`, corespunzător acelui `fetch` explicit. Asta confirmă
  capturarea request-ului de precondiție, nu o cerere API provocată de căutarea
  UI sau de checkout.
- Aserțiunea `> 0 API requests caused by this browser journey` numără orice
  observație `/api/` din sesiune; request-ul de setup poate satisface condiția
  fără să existe dovadă pentru o acțiune UI. Numele/semantica acestui verdict
  trebuie să distingă precondiția explicită de traficul cauzal UI → API.
- `AutoExComponentSuite` selectează numai
  `atf.ui.tests.autoex.component`; nu există în prezent suită API component sau
  pachet de contracte API. Căutarea UI navighează la `/products?search=...` și
  nu exercită prin ea însăși `POST /api/searchProduct`.

Aceste constatări provin din sursele consumerului și raportul generat de
implementarea curentă; ele nu trebuie confundate cu o explorare manuală nouă a
site-ului.

### Suprafața API publică de acoperit

Automation Exercise documentează 14 scenarii API. Statusurile de mai jos sunt
contractele publicate de site, nu rezultate validate live de o suită AutoEx.

| Scenarii | Endpoint și cazuri documentate | Risc / izolare necesară |
|---|---|---|
| API 1–4 | `GET /api/productsList` → 200; `POST /api/productsList` → 405; `GET /api/brandsList` → 200; `PUT /api/brandsList` → 405 | read-only sau metodă neacceptată; validează statusul și forma minimă a JSON-ului |
| API 5–6 | `POST /api/searchProduct` cu `search_product` → 200; fără parametru → 400 | input valid/invalid, fără efecte persistente; separă acest API de ruta UI de căutare |
| API 7–10 | `POST /api/verifyLogin`: date valide → 200, parametri lipsă → 400, date invalide → 404; `DELETE` → 405 | cazul pozitiv necesită cont fixture deținut de test; nu folosi credențiale personale/shared |
| API 11–14 | `POST /api/createAccount` → 201; `PUT /api/updateAccount` → 200; `GET /api/getUserDetailByEmail` → 200; `DELETE /api/deleteAccount` → 200 | stare mutabilă; identitate sintetică unică, ordine deterministă și cleanup verificat chiar după eșec |

Referința normativă pentru lista de endpoint-uri este
[API List](https://automationexercise.com/api_list); scenariile browser care
contextualizează consumerul sunt în [Test Cases](https://automationexercise.com/test_cases).
Journey-ul AutoEx actual este aliniat în principal cu TC14 și TC24, nu cu
scenariile distincte de register/login/logout, coș multi-produs, persistența
coșului după autentificare, adrese, categorii și branduri.

### Cerințe și criterii de acceptanță

1. **Suite API componentă separată.** Adaugă suite și pachet dedicat, selectat
   de un runner component/API distinct de `AutoExPurchaseSuite`. Testele rapide
   folosesc transport fixture și nu deschid browserul sau nu contactează site-ul.
2. **Contracte tipizate și matrice completă.** Modelează endpoint-urile,
   metodele, parametrii, request body-urile și citirea răspunsurilor în suportul
   consumerului; acoperă toate cele 14 cazuri documentate în matricea de
   contract. Testele componentă verifică deterministic request/response
   mapping-ul cu transport fixture; integrarea opt-in confirmă contractul
   public real conform politicii de efecte secundare. Include statusurile
   negative 400/404/405 și câmpurile semantice relevante, fără snapshot-uri
   fragile ale întregului catalog.
3. **Nivel live opt-in.** Dacă se cere validarea serviciului real, separ-o de
   componente, activeaz-o explicit și fă destinația, timeout-ul și raportul
   configurabile. Buildul implicit nu trebuie să trimită request-uri către
   serviciul public.
4. **Ciclul contului sigur.** Crearea, citirea, actualizarea, autentificarea și
   ștergerea folosesc o identitate sintetică unică, creată de test. Cleanup-ul
   este armat înainte de mutație, rulează în finalizare garantată, confirmă
   ștergerea și nu maschează eroarea primară; datele sensibile sunt redactate
   din request/response și din console.
5. **Network Inspector cauzal.** În UI_API, precondițiile API explicite se
   marchează separat și nu satisfac aserțiunea de trafic produs de UI.
   Exchange-urile UI se corelează cu checkpoint-ul/gestul, fără dublare și fără
   a transforma raportul agregat într-un contract API.
6. **Testare componentă și guvernanță.** Componentele verifică rutele, metodele,
   parametrii lipsă, status/body mapping, protecția datelor și ramurile de
   cleanup. Un test de guvernanță verifică apartenența testelor la suitele
   potrivite, că API component suite rămâne offline/deterministă și că orice
   test live este opt-in; E2E-ul de purchase nu primește o matrice duplicată.
7. **Explorare manuală read-only.** Închiderea gap-ului cere și o verificare
   interactivă prin browser MCP a paginilor catalog/search și a fluxului de
   autentificare fără submit de date mutabile, cu Network Inspector pornit
   înainte de acțiuni. Se notează schimburile care apar natural după fiecare
   acțiune, separat de setup/precondiții. ATF Debug poate inspecta raportul
   rezultat, dar nu înlocuiește browserul de explorare a site-ului.

**Dovada de închidere:** suitele component/API trec separat și împreună,
testele mutabile demonstrează cleanup inclusiv la eșec, iar raportul proaspăt
distinge request-ul de precondiție de traficul UI cauzal. Până atunci,
`UI_API` este evidence de network în journey, nu dovadă de contract API complet.
