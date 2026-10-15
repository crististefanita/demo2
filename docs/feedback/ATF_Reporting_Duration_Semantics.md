# ATF reporting — ToDo-uri active pentru durate și badge-uri

Contractul stabil este în [ATF_Reporting_Contract.md](../ATF_Reporting_Contract.md)
și [atf-reporting/README.md](../../atf-reporting/README.md). Aici rămân numai
diferențele care mai cer implementare sau teste.

## D-01 — Valoare numerică și sursă explicită

Separarea dintre `Request details`, `Response details` și `Execution evidence`
este implementată și testată. Rămâne de standardizat pentru fiecare nod
`data-duration-ms` și `data-duration-source` cu valorile `measured`, `exclusive`
sau `aggregated`, inclusiv în `Tests`, `Category`, `Fails` și în header-ul
clasei. Implementarea actuală marchează explicit agregarea clasei, dar nu
expune încă același contract pentru toate nodurile și proiecțiile.

Teste: `1 ms`, `16 ms`, peste o secundă, durată lipsă/malformed/zero și sursă
corectă după filtrare.

## D-02 — Agregare structurală fără dublare

Runtime-ul actual însumează scenariile directe pentru header-ul clasei, dar
contractul nu este încă generalizat pentru toate nodurile și stările filtrate.
Agregarea trebuie să folosească numai copiii structurali direcți și să evite
adăugarea unei durate vendor deja inclusive. Durata clasei din stânga trebuie
să fie egală cu suma scenariilor directe vizibile în dreapta.

Teste: părinte cu/fără durată proprie, nested phase, scenarii ascunse prin
filtru și recalculare după schimbarea tab-ului.

## D-03 — Durată absentă și frunze

Formatterul numeric acoperă deja zero și durate pozitive, însă contractul de
afișare pentru request, validation, phase și lifecycle trebuie să distingă
durata de count-ul de assertions/noduri. Fără valoare numerică se afișează `—`,
nu `0`, count sau timestamp; unitatea `ms` apare numai pentru valori valide.

Teste: request fără response, teardown fără response, zero, malformed și durată
lipsă în `Tests` și `Fails`.

## D-04 — Un singur token de geometrie pentru status

Tokenurile de badge există, dar `AtfReportTheme` conține încă reguli duplicate
și override-uri pentru annotations/tag-uri. Consolidează status badge, duration
metadata și tag annotations în tokenuri distincte, reutilizate în `Tests`,
`Category`, `Fails`, `Overview` și detail.

Teste: CSS contract pentru toate containerele și verificare DOM/computed style
pentru height, padding, radius, centrare și separarea statusului de durată.

## Gate de închidere

ToDo-ul dispare numai după teste unitare/componentă verzi, artifact staged
verificat în TAB MCP și mutarea regulii stabile în documentul normativ. Nu se
păstrează scoruri sau istoric în acest document.
