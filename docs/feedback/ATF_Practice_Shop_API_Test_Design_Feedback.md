# Practice Shop API — comportament live și designul probelor

Feedback-ul urmărește diferența dintre contractele verificate cu transport controlat
și comportamentul confirmat prin HTTP real. Limitele publice ale modulelor și ale
suitelor rămân în [README-ul Practice Shop](../../atf-test-practice-shop/README.md#boundary-ul-probelor-api).

## SHOP-API-LIVE-SEMANTICS-01 — filtrare, sortare și mutații remote

**Stare: deschis — implementarea există, dovada completă live este blocată de fixture-ul autentificat.**
**Owner:** Practice Shop.

### Lacuna și decizia

- Testele unit/component dovedesc request-ul, validatorul și lifecycle-ul cu
  răspunsuri controlate; ele nu dovedesc semantica serviciului extern.
- `ShopStandaloneApiReportTest` conține acum aserțiuni pentru categoria produsului
  live, sortarea numerică după preț și read-back după update/remove pe un coș izolat.
- Ultima rulare opt-in a primit HTTP 401 la autentificarea fixture-ului local și
  s-a oprit înainte de crearea coșului. Nu există încă dovadă live pentru mutații.
- Nu copiați credențiale în feedback/raport și nu prezentați răspunsurile
  fixture-urilor drept afirmații despre server.

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

### Baza de probe și limita live

- Sursele main și test ale modulului se compilează în configurația curentă.
- Suitele unit/component verifică rutele, filtrarea/sortarea așteptată și
  lifecycle-ul coșului cu transport controlat; ele nu certifică semantica
  serviciului remote.
- Ultima dovadă live disponibilă s-a oprit la autentificare HTTP 401, înainte de
  crearea coșului. Sort/filter și mutațiile remote rămân, așadar, neconfirmate.
- [README-ul Practice Shop](../../atf-test-practice-shop/README.md#boundary-ul-probelor-api)
  păstrează aceeași delimitare și trimite la acest gap; nu se dublează aici
  contractul stabil al modulului și nu se atașează istoricul unui raport.

### Criterii rămase pentru închidere

- [ ] Reparați sau înlocuiți fixture-ul de cont local prin procesul autorizat; nu slăbiți condiția de autentificare și nu ignorați 401.
- [ ] Rulați din nou proba opt-in și confirmați în raport provenance `LIVE / HTTP`, rezultatele filtrării/sortării și read-back-ul pentru cantitatea actualizată și coșul gol după DELETE.
- [ ] Confirmați cleanup atât pe succes, cât și când flow-ul se întrerupe; dacă serviciul nu permite cleanup sigur, păstrați limitarea explicită și nu pretindeți acoperire remote.
- [ ] Reconcileați feedback-ul cu README-ul ownerului la momentul închiderii; până atunci nu eliminați delimitarea documentată.

Suitele unit/component rămân fără trafic către serviciul extern. Rularea live este
opt-in și nu trebuie inclusă implicit în gate-ul determinist.
