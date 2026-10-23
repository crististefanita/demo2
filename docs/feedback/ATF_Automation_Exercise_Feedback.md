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

- `AUTOEX-API-01` — **închis ca decizie de scope, nu ca implementare de API
  tests**. Endpoint-urile publice documentate nu justifică dublarea acestui
  consumer UI cu o a doua suită API. Varianta `UI_API` observă traficul
  first-party real produs de același browser și păstrează request/response în
  raport; ea nu pretinde că verifică separat contractul fiecărui endpoint.
  Endpoint-urile mutabile (crearea/ștergerea contului și comenzile) ar cere un
  contract separat de izolare a datelor și cleanup confirmat, ceea ce ar schimba
  scopul consumerului și nu se adaugă implicit.
- Dacă apare cerința de verificare directă a contractelor API, aceasta se
  redeschide ca suită API separată, cu matrice de metode/parametri/răspunsuri,
  ownership explicit al datelor și dovadă de cleanup. Nu se va deduce acoperirea
  aceasta din simpla observare de network în E2E.

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

`AUTOEX-EVIDENCE-MATRIX-01` — **închis după gate live și inspecție vizuală**,
2026-10-23:

- `AutoExPurchaseSuite` a trecut 3/3: UI, UI+API și UI cu captură numai la
  final. Raportul proaspăt a fost deschis prin ATF Debug în browserul MCP;
  verdictul nu se bazează numai pe HTML static sau pe build.
- În fiecare scenariu, `TEST BODY` conține 12 checkpoint-uri Gherkin distincte
  și 12 tabele de acțiuni. Expandarea nodurilor păstrează pașii și dovezile
  asociate; nu mai există tabelul agregat de 63 de acțiuni din captura veche.
- În UI+API, `GET /api/productsList — Exchange 1` este copilul checkpoint-ului
  API care îl observă, cu Request details, Response details și Execution
  evidence. Nu este duplicat ca pas API în celelalte variante. Răspunsul vizibil
  include JSON formatat; transportul este raportat ca `LIVE / HTTP`.
- Varianta final-only are 12 tabele de acțiuni și exact un singur link de
  screenshot, atașat ultimului `Screen validation`, după acțiunea terminală.
  Linkul a fost deschis manual și imaginea completă (1440×1000) s-a randat în
  lightbox. Rendererul a deduplicat bytes identici cu o captură anterioară din
  același run; linkul de la checkpoint-ul terminal rămâne funcțional și duce la
  imaginea verificată. Aceasta este deduplicare de asset, nu o captură mutată
  vizual în alt pas.
- Verificare vizuală la viewport MCP 868×648: tabelele și documentul nu au
  overflow orizontal; consola browserului a raportat zero erori și zero
  avertismente. Nu s-a pretins o verificare separată de `pageerror`, pentru care
  sesiunea MCP nu expune un instrument dedicat.
- Testele de componentă pentru profil, politica de captură, aserțiunile de
  catalog, cleanup și combinarea excepțiilor au trecut; integration testul
  reporting verifică checkpoint-uri, acțiuni, capturi și ordinea Gherkin.

## Limita dovezii API

- Rularea UI+API observă un schimb real, read-only, `GET /api/productsList`,
  ca precondiție explicită a achiziției și îl raportează cu request/response.
  Nu pretinde că probează separat metodele, parametrii sau efectele endpoint-urilor
  de scriere (`searchProduct`, cont, comandă). Această limită este intenționată,
  nu un test API verde sau o presupunere că UI și API sunt același contract.
