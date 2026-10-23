# Practice Shop API — comportament live și designul probelor

Feedback-ul urmărește diferența dintre contractele verificate cu transport controlat
și comportamentul confirmat prin HTTP real. Limitele publice ale modulelor și ale
suitelor rămân în [README-ul Practice Shop](../../atf-test-practice-shop/README.md#boundary-ul-probelor-api).

## SHOP-API-LIVE-SEMANTICS-01 — filtrare, sortare și mutații remote

**Stare: închis — dovada live autentificată și contractele mutative au trecut.**
**Owner:** Practice Shop.

### Lacuna și decizia

- Testele unit/component dovedesc request-ul, validatorul și lifecycle-ul cu
  răspunsuri controlate; ele nu dovedesc semantica serviciului extern.
- `ShopStandaloneApiReportTest` conține acum aserțiuni pentru categoria produsului
  live, sortarea numerică după preț și read-back după update/remove pe un coș izolat.
- Fixture-ul anterior a primit HTTP 401. Un cont sintetic nou pentru aplicația
  demo a fost înregistrat prin API (HTTP 201), autentificat (HTTP 200) și pus
  în fixture-ul modulului; rularea opt-in a trecut ulterior integral.
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
- GET-urile read-only au oferit un diagnostic preliminar; dovada de închidere
  este rularea de integrare autentificată, care include și mutațiile coșului.

### Baza de probe live și limita auditului vizual

- Sursele main și test ale modulului se compilează în configurația curentă.
- Suitele unit/component verifică rutele, filtrarea/sortarea așteptată și
  lifecycle-ul coșului cu transport controlat; ele nu certifică semantica
  serviciului remote.
- `ShopStandaloneApiReportTest` a trecut cu `LIVE / HTTP` pentru login, catalog,
  filtrare/sortare, creare/adăugare/update/remove coș, read-back și checkout.
  Raportul HTML generat în rularea curentă conține contractele live, metodele
  `GET`/`PUT`/`DELETE` și disclosure-urile request/response. Verificarea
  aceasta este de conținut HTML; politica browserului a blocat explicit
  deschiderea `file:`, deci randarea interactivă nu este declarată verificată.
- Testele de componentă acoperă cleanup-ul și pe calea de eșec; rularea live
  de mai sus confirmă cleanup-ul pe calea de succes, fără a pretinde o
  întrerupere controlată a unui flow remote.
- [README-ul Practice Shop](../../atf-test-practice-shop/README.md#boundary-ul-probelor-api)
  deține contractul stabil al modulului; aici rămân doar probele de închidere
  și limita verificării vizuale, fără a duplica acel contract.

### Criterii de închidere și probe

- [x] Fixture-ul local de cont demo a fost înlocuit după înregistrare și login
  confirmate prin HTTP real; condiția de autentificare a rămas obligatorie.
- [x] Rularea opt-in a trecut, cu `LIVE / HTTP`, sortare/filtrare și read-back
  pentru cantitatea actualizată și coșul gol după DELETE.
- [x] Cleanup-ul pe succes este probat live; cleanup-ul la întrerupere rămâne
  probat determinist de testele de componentă, nu de o mutație remote indusă.
- [x] Delimitarea dintre probele controlate și cele live este păstrată; nu se
  revendică o verificare browser a raportului.

Suitele unit/component rămân fără trafic către serviciul extern. Rularea live este
opt-in și nu trebuie inclusă implicit în gate-ul determinist.
