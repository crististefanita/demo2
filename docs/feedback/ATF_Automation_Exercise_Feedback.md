# Feedback — Automation Exercise

Acest fișier păstrează numai cerințele de business încă deschise pentru
consumerul [`atf-test-auto-ex`](../../atf-test-auto-ex/README.md). Contractul
stabil, flow-ul implementat și regulile de reutilizare sunt documentate în
README-ul modulului; arhitectura comună de test Shop/AutoEx este urmărită în
[feedback-ul dedicat](ATF_Consumer_Test_Architecture_Feedback.md), iar
guvernanța diff-urilor Git rămâne în baseline-ul tehnic.

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
  observă exchange-uri `/api/`. Rularea live și interpretarea corelării rămân
  de confirmat. Endpoint-urile sunt descrise în
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

## ToDo de business

| ID | Cerință rămasă | Criteriu de acceptare | Prioritate |
|---|---|---|---:|
| `AUTOEX-EVIDENCE-MATRIX-01` | Validează live și vizual matricea de evidence implementată pentru aceeași purchase journey. | `AutoExPurchaseSuite` selectează UI, UI_API și UI final-screenshot; raportul comun conține doar schimburile browser observate, assertion-ul UI_API eșuează dacă nu apare niciun request `/api/`, iar varianta final-only publică o captură terminală într-un singur flow unificat. Rulează suita opt-in pe site și verifică raportul randat; nu inventa request-uri sau corelări. | 88 |
| `AUTOEX-API-01` | Evaluează dacă endpoint-urile API public documentate justifică o colecție de teste API separată de E2E-ul UI. | Metoda, parametrii/body-ul, răspunsul și efectele sunt probate față de contractul site-ului; datele sunt izolate și cleanup-ul confirmat. Nu presupune JSON pentru POST/PUT și nu eticheta API drept precondiție UI fără o dependență reală. | 78 |
| `AUTOEX-E2E-RESILIENCE-01` | Închide fereastra fără cleanup dintre submit-ul de creare a contului și armarea cleanup-ului; verifică și eșecul cleanup-ului. | Înregistrează cleanup-ul înainte de submit, îl face no-op dacă site-ul nu a creat contul și șterge contul dacă acesta există; un eșec de cleanup este vizibil în raport fără să înlocuiască excepția primară. Dovada include traseul real și un test de componentă al lifecycle-ului fără DOM/site simulat în consumer. | 72 |
| `AUTOEX-DATA-01` | Protejează aserțiunile configurabile împotriva drift-ului catalogului. | Valorile așteptate rămân configurabile și stricte, iar raportul oferă un diagnostic clar. În codul curent, numele produsului este comparat exact, dar prețul din detalii folosește `contains(configuredUnitPrice)`; închide doar după ce un preț cu prefix comun, dar valoare diferită, este respins fără a ascunde drift-ul. | 65 |

Scorurile ordonează numai lucrul rămas; nu sunt o evaluare globală a modulului.
Un ToDo se închide după probarea criteriului său, nu după simpla mutare a
codului între module.

## Dovezi live care țin cerințele deschise

- `AccountPage.registerFromCheckout` armează `onFailureCleanup` după click-ul
  de creare **și** după citirea heading-ului rezultatului. Dacă citirea eșuează
  în acel interval, contul poate exista fără cleanup înregistrat.
- Verificarea prețului în pagina de produs folosește `contains`, deci nu
  respinge garantat valori cu același prefix. Cart price/total și numele
  produsului au verificări distincte; ele nu închid singure acest caz.
