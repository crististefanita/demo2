# ATF reporting — ToDo-uri active pentru durate și badge-uri

Regula stabilă este în [ATF_Reporting_Contract.md](../ATF_Reporting_Contract.md)
și [atf-reporting/README.md](../../atf-reporting/README.md). Aici rămân numai
diferențele care mai cer implementare sau teste.

## D-01 — Păstrează valoarea numerică și sursa duratei

Modelează separat `data-duration-ms` și sursa `measured`, `exclusive` sau
`aggregated` pentru fiecare nod relevant. Textul `N ms` este doar prezentare și
nu poate fi interpretat ca număr de assertions sau pași.

Teste: `1 ms`, `16 ms`, durate peste o secundă, durată Extent inclusivă și
badge status/duration ca elemente distincte.

## D-02 — Agregare structurală fără dublare

Calculează `exclusive + suma copiilor structurali direcți`; nu traversa toți
descendenții și nu adăuga încă o dată o durată vendor deja inclusivă. Clasa din
stânga și header-ul clasei trebuie să fie egale cu scenariile directe vizibile.

Teste: arbore nested, părinte fără durată proprie, părinte cu durată inclusivă
și scenarii ascunse prin filtru.

## D-03 — Absența duratei și frunzele

Request, validation, phase și lifecycle trebuie să distingă durata de count-ul
de assertions/noduri. Fără valoare numerică se afișează `—`, nu `0`, count sau
timestamp; unitatea `ms` apare numai pentru valori valide.

Teste: nod fără response, teardown fără response, zero, malformed și durată
lipsă în Tests și Fails.

## D-04 — Un singur token de geometrie pentru status

Aplică în Tests, Category, Fails, Overview și detail aceeași geometrie:
`height:18.2px`, `padding:0 5px`, `border:0`, `radius:4px`, `font-size:9.52px`,
`line-height:18.2px`, `font-weight:400`. Durata/metadata folosește token separat.
Culorile rămân în `AtfReportColors`, nu în selectori locali.

Teste: CSS pentru toate containerele și test DOM pentru computed style,
centrare și separarea statusului de durată în fiecare tab.

## Închidere

ToDo-ul dispare după teste unitare/componentă verzi, artifact staged verificat
în TAB MCP și mutarea regulii stabile în documentul normativ. Nu se păstrează
istoric sau scoruri în acest fișier.
