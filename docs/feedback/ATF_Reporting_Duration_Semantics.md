# ATF reporting — ToDo-uri active pentru durate și badge-uri

Contractul stabil este în [ATF_Reporting_Contract.md](../ATF_Reporting_Contract.md)
și [atf-reporting/README.md](../../atf-reporting/README.md). Aici rămân numai
diferențele care mai cer implementare sau teste.

## D-01 — Valoare numerică și sursă explicită

**Status: `PARTIAL`.**

Separarea dintre `Request details`, `Response details` și `Execution evidence`
este implementată și testată. Rămâne de standardizat pentru fiecare nod
`data-duration-ms` și `data-duration-source` cu valorile `measured`, `exclusive`
sau `aggregated`, inclusiv în `Tests`, `Category`, `Fails` și în header-ul
clasei. Implementarea actuală marchează explicit agregarea clasei, dar nu
expune încă același contract pentru toate nodurile și proiecțiile.

Runtime-ul marchează deja agregarea clasei și normalizează mai multe badge-uri,
dar nu există încă o demonstrație că toate nodurile din `Tests`, `Category` și
`Fails` primesc markerul numeric și aceeași sursă. Testele curente verifică
prezența scriptului/contractului, nu exhaustivitatea markerilor pe fiecare
proiecție.

Acoperirea dinamică pentru tab-ul `Tests` este închisă separat prin
`ApiExtentReportExploratoryTest.everyTestsNodeExposesReconciledDynamicDuration`:
testul generează raportul API, selectează fiecare owner descoperit din DOM și
verifică fiecare scenario/phase/request/validation structural, inclusiv
markerul numeric, badge-ul formatat, sursa `aggregated` și reconcilierea
bottom-up. `Category` și `Fails` rămân în continuare partea neînchisă a acestui
ToDo global.

Teste rămase: `0`, `300 ms`, `301 ms`, `999 ms`, peste o secundă, durată
lipsă/malformed/zero și sursă corectă după filtrare.

## D-02 — Agregare structurală fără dublare

**Status: `RESOLVED`.**

Runtime-ul agregă bottom-up numai copiii structurali direcți ai fiecărui nod.
Pentru un nod cu acțiuni proprii și copii, folosește maximul dintre suma
copiilor și suma acțiunilor proprii; astfel o durată Extent deja inclusivă nu
este adăugată încă o dată. Pentru un nod fără acțiuni, suma copiilor devine
durata afișată. Header-ul clasei este apoi recalculat ca suma scenariilor
directe vizibile, după filtrare.

Contractul este aplicat după activarea tab-ului, după recalcularea filtrelor și
la schimbarea navigării. Fiecare nod recalculat primește `data-duration-ms` și
`data-duration-source=aggregated`; formatterul comun păstrează pragul `ms` /
`s`.

Teste verzi: `ExtentReportRuntimeScriptTest` verifică ownership-ul controllerului,
copiii direcți, protecția `Math.max` și recalcularea după filtru; `ApiExtentReportUiTest`
rulează setup-ul real `ApiLifecycleAndCombinationsTests`, generează un artifact
cu timestamp și verifică în browser fazele SETUP, TEST BODY și TEARDOWN împreună
cu markerii de durată.

## D-03 — Durată absentă și frunze

**Status: `PARTIAL`.** Formatterul numeric comun este închis; contractul pentru
lipsă/malformed și frunze nu este încă uniform pe toate tab-urile.

Formatterul comun `atf.api.core.reporting.AtfDurationFormatter` acoperă pragul
de afișare pentru request, validation, phase și lifecycle: `<=300 ms` rămâne
în `ms`, iar `>300 ms` devine secunde cu precizie de milisecunde. Ținta pentru
valoarea absentă este `—`, nu `0`, count sau timestamp; în codul actual Fails
folosește încă `No response duration recorded`, deci regula nu este închisă
până când toate proiecțiile aleg aceeași reprezentare și există teste pentru ea.

Teste: `0`, `300`, `301`, `999`, `1000`, request fără response, teardown fără
response, malformed și durată lipsă în `Tests` și `Fails`.

Body-urile JSON au o politică separată de durată și assertions: `reporting.bodyMaxDisplayCharacters`
limitează conținutul după redaction, iar blocul rămâne scrollabil. Valoarea
implicită este `12000`, iar markerul `… [truncated]` face truncarea vizibilă în
artifact, fără să transforme payload-ul într-un tabel sau într-un text nelimitat.

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
