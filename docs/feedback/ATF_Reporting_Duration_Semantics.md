# ATF reporting — ToDo-uri active pentru durate și badge-uri

Acest document păstrează numai diferențele încă neînchise din contractul de
durată/status. Regula stabilă este în
[ATF_Reporting_Contract.md](../ATF_Reporting_Contract.md) și în
[atf-reporting/README.md](../../atf-reporting/README.md).

## Durate

### D-01 — Păstrează valoarea numerică și sursa duratei

**Dovadă curentă:** scriptul final normalizează textul la `N ms`, iar agregarea
clasei scrie `data-duration-ms` și `data-duration-source="aggregated"`. Frunzele
și fazele nu au încă un model explicit pentru valoarea numerică și sursa ei.

**De făcut:** păstrează separat `data-duration-ms` pentru fiecare nod relevant
și modelează sursa ca `measured`, `exclusive` sau `aggregated`. Textul vizibil
este doar formatul uman și nu poate fi folosit pentru a deduce count-uri.

**Teste:** parsează `1 ms`, `16 ms`, valori peste o secundă, durate Extent
native și badge-uri `Pass 16 ms`; verifică faptul că statusul și durata sunt
citite din elemente distincte.

### D-02 — Agregare structurală fără dublare

**Dovadă curentă:** navigatorul clasei agregă scenariile directe, dar phase,
request și validation nu au încă o regulă generală pentru durată proprie,
copii direcți și durată Extent inclusivă.

**De făcut:** pentru fiecare părinte calculează
`exclusive + suma copiilor structurali direcți`; nu traversa toți descendenții
și nu adăuga din nou o durată vendor deja inclusivă. Clasa din stânga și header-ul
clasei trebuie să rămână egale cu scenariile directe vizibile.

**Teste:** arbore nested cu durate măsurate, părinte fără durată proprie,
părinte cu durată inclusivă și scenarii ascunse de filtru.

### D-03 — Absența duratei și frunzele

**De făcut:** request, validation, phase și lifecycle trebuie să distingă
durata măsurată de count-ul de assertions/noduri. Când durata nu există,
afișează `—`, nu `0`, count sau timestamp; păstrează unitatea `ms` numai când
există o valoare numerică validă.

**Teste:** nod fără response, teardown fără response, response cu durată zero,
valoare malformed și artifact cu durată lipsă verificat în Tests și Fails.

## Badge-uri de status

### D-04 — Un singur token de geometrie pentru status

**Dovadă curentă:** geometria Fails este deținută de `AtfReportFailsStatusCss`
și este compusă după regulile generale ale temei. Failure detail consumă acum
aceleași tokenuri; nu mai păstrează `height`, `padding`, `font-size` sau
`font-weight` locale pentru badge-ul semantic.

**De făcut:** păstrează geometria extrasă într-un token/selector comun și aplic-o în
Tests, Category, Fails, Overview și detail projections:
`height:18.2px`, `padding:0 5px`, `border:0`, `radius:4px`, `font-size:9.52px`,
`line-height:18.2px`, `font-weight:400`. Badge-urile neutre de durată/metadata
folosesc separat `22.4px`, padding `5px 7px` și line-height `10.6624px`.
Elimină override-urile contradictorii.
Păstrează culorile în `AtfReportColors`, nu în selectoare locale.

**Teste:** assertions pe CSS pentru toate containerele și test DOM pentru
computed style, text centering și status/duration separate în fiecare tab.

## Dovada finală pentru acest document

Un ToDo se închide numai după:

- testele unitare și de componentă verzi;
- artifact HTML generat într-un staging separat;
- verificarea în TAB MCP a Tests, Overview, Category și Fails;
- confirmarea că nu există durate fără `ms`, timestamp în tabelul Tests sau
  badge-uri cu geometrie diferită;
- eliminarea ToDo-ului închis din acest fișier și mutarea regulii stabile în
  documentul normativ.
