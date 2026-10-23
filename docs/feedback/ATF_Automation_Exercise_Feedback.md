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
| `AUTOEX-EVIDENCE-MATRIX-01` | Închide validarea matricei de evidence și a lizibilității pașilor în raportul live. | `AutoExPurchaseSuite` selectează UI, UI_API și UI final-screenshot într-un singur raport; fiecare checkpoint Gherkin este un nod separat cu doar acțiunile, aserțiunile și exchange-urile sale. Capturile checkpoint sunt lângă checkpoint-ul lor, iar varianta final-only publică exact o captură terminală fără să comprime pașii. Raportul proaspăt este inspectat vizual în browser; schimburile sunt doar cele observate în browser și corelările nu sunt inventate. | 88 |
| `AUTOEX-API-01` | Evaluează dacă endpoint-urile API public documentate justifică o colecție de teste API separată de E2E-ul UI. | Metoda, parametrii/body-ul, răspunsul și efectele sunt probate față de contractul site-ului; datele sunt izolate și cleanup-ul confirmat. Nu presupune JSON pentru POST/PUT și nu eticheta API drept precondiție UI fără o dependență reală. | 78 |

Scorurile ordonează numai lucrul rămas; nu sunt o evaluare globală a modulului.
Un ToDo se închide după probarea criteriului său, nu după simpla mutare a
codului între module.

## Rezolvat în codul curent — necesită gate de raport proaspăt

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
- Structura capturată în raportul suitei era agregată intenționat de
  `UNIFIED_FLOW`: un singur nod de validare, un tabel de 63 acțiuni, apoi
  capturi repetate fără proximitate vizuală față de checkpoint-ul care le-a
  produs. AutoEx folosește acum `SCREEN_CHECKPOINTS`, la fel ca modelul de
  audit detaliat din Practice Shop. Varianta `FINAL_SCREENSHOT_ONLY` păstrează
  separat fiecare pas/acțiune, dar politica de captură este `FINAL_ONLY`.
- Pentru raportul comun, sesiunea Extent are profilul-capabilitate
  `SCREEN_CHECKPOINTS` + `EACH_CHECKPOINT`; fiecare test aplică apoi propria
  politică înainte să publice captura. Astfel `FINAL_SCREENSHOT_ONLY` nu trimite
  capturi intermediare, deși raportul comun poate afișa capturi de la celelalte
  două scenarii.

Execuție live în staging, 2026-10-23: `AutoExPurchaseSuite` a trecut 3/3.
Raportul a avut 12 tabele de acțiuni în fiecare scenariu; API validation a
apărut numai în UI+API, iar scenariul final-only a avut un singur link de
captură la validarea terminală. Integration testul de reporting verifică două
checkpoint-uri, două tabele, două capturi și ordinea lor Gherkin. **Matricea
rămâne deschisă până la inspecția vizuală în browser a acestui raport proaspăt**;
blocarea navigării MCP la URL `file:` înseamnă că verificarea HTML și testele
nu sunt prezentate drept confirmare vizuală.

## Dovezi live care țin cerințele deschise

- `AUTOEX-API-01` rămâne evaluare separată: o colecție API independentă se
  adaugă numai cu contract HTTP probat, izolare de date și cleanup verificat;
  nu este precondiție implicită pentru journey-ul UI.
