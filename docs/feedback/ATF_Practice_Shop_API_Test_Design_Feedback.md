# Practice Shop API — comportament live și designul probelor

Feedback-ul urmărește diferența dintre contractele verificate cu transport controlat
și comportamentul confirmat prin HTTP real. Limitele publice ale modulelor și ale
suitelor rămân în [README-ul Practice Shop](../../atf-test-practice-shop/README.md#boundary-ul-probelor-api).

## SHOP-API-LIVE-SEMANTICS-01 — filtrare, sortare și mutații remote

**Stare: deschis — implementarea există, dovada completă live este blocată de fixture-ul autentificat.**
**Owner:** Practice Shop.

### Lacuna și decizia

Testele unit/component pot dovedi formarea request-ului, regulile validatorului și
comportamentul lifecycle-ului cu răspunsuri controlate; ele nu dovedesc semantica
serviciului extern. Proba `ShopStandaloneApiReportTest` a fost extinsă pentru a
verifica filtrarea pe categoria produsului găsit live, sortarea numerică ascendentă
după preț și read-back după update/remove pe un coș nou, izolat per execuție.

Rularea live opt-in a ajuns la endpoint-ul real, dar autentificarea fixture-ului
local a primit HTTP 401. Testul a publicat raportul de eșec al precondiției și s-a
oprit înainte de crearea coșului; prin urmare nu există încă dovadă live pentru
mutații și nu se declară ID-ul închis. Nu copiați credențiale în feedback sau în
raport și nu transformați răspunsurile fixture-urilor în afirmații despre server.

### Implementare și protecții adăugate

- Validatorul verifică atât query-ul emis, cât și statusul/body-ul real: lista nu
  este goală, fiecare produs aparține categoriei cerute, iar prețurile numerice sunt
  în ordine ascendentă.
- `ShopApiLiveCartScope` deține un coș creat dinamic, verifică read-back-ul după
  update și removal, șterge produsul rămas la `close()` și confirmă coșul gol.
  Cleanup-ul nu repetă DELETE-ul, iar clientul API este configurat pentru o singură
  încercare la operațiile mutative.
- Component testele acoperă închiderea normală după removal și cleanup când flow-ul
  eșuează după add; verifică un singur DELETE și starea finală goală. Testele unit
  verifică produsele/ordinea și resping categoria sau ordinea greșită.
- Comportamentul de sortare/filtrare a fost observat separat prin GET-uri read-only
  către serviciul public. Acest lucru nu substituie rularea raportului de integrare
  autentificat și nici nu este prezentat drept dovadă pentru mutațiile coșului.

### Dovezi de verificare

- `mvn -pl atf-test-practice-shop -am "-Dtest=ShopApiUnitSuite,ShopApiComponentSuite" "-Dsurefire.failIfNoSpecifiedTests=false" test` — **95 teste trecute**, zero eșecuri/erori/skip-uri.
- `mvn -pl atf-test-practice-shop -am "-Dtest=ShopStandaloneApiReportTest" "-Datf.practice.authenticated-e2e=true" "-Dsurefire.failIfNoSpecifiedTests=false" test` — **eșuat corect la precondiție**: login live HTTP 401; niciun coș nu a fost creat. Raportul de eșec a fost publicat în `artifacts/reports/api`.
- README-ul ownerului descrie încă sort/filter și update/remove live ca nedemonstrate; acea delimitare rămâne corectă până la o execuție autenticată verde. README-ul modulului nu a fost schimbat în acest increment.

### Criterii rămase pentru închidere

- [ ] Reparați sau înlocuiți fixture-ul de cont local prin procesul autorizat; nu slăbiți condiția de autentificare și nu ignorați 401.
- [ ] Rulați din nou proba opt-in și confirmați în raport provenance `LIVE / HTTP`, rezultatele filtrării/sortării și read-back-ul pentru cantitatea actualizată și coșul gol după DELETE.
- [ ] Confirmați cleanup atât pe succes, cât și când flow-ul se întrerupe; dacă serviciul nu permite cleanup sigur, păstrați limitarea explicită și nu pretindeți acoperire remote.
- [ ] Reconcileați feedback-ul cu README-ul ownerului la momentul închiderii; până atunci nu eliminați delimitarea documentată.

Suitele unit/component rămân fără trafic către serviciul extern. Rularea live este
opt-in și nu trebuie inclusă implicit în gate-ul determinist.
