# ATF Reporting — durate și geometria badge-urilor

Reconciliere live: **2026-10-16**. `AtfDurationFormatter`, runtime-ul de
agregare și proiecția `ExpectedReportProjection` sunt active și testate. D-01
este închis prin rerun-ul dinamic complet și prin matricea browser. Artifactul
UI fresh verificat este
`artifacts/reports/ui/ui-atf-extent-report_api-extent-report-exploratory-test_2026-10-16_22-40-15-301.html`.

Registrul păstrează contractele de raportare urmărite și precizează explicit
ceea ce este deja închis, pentru ca statusul central și closure evidence să nu
devină divergente.

## D-01 — durată numerică și sursă explicită (DONE)

Nodurile care afișează durată trebuie să aibă valoare numerică stabilă și
data-duration-source. Aceeași valoare trebuie reconciliată între Tests,
Overview, Category, Fails și detail.

Auditul dinamic a trecut 13/13 cu memorie nativă bounded și verifică markerul
numeric/source în Tests, Overview, Categories, Fails și detail. Validatorul
distinge corect cardurile container `measured`/`aggregated` de badge-urile leaf
formatate, deci nu mai confundă textul complet al cardului cu durata.

## D-03 — durată absentă (DONE)

0 ms este o durată validă. Lipsa duratei sau o valoare malformed trebuie
afișată ca —, nu ca text de fallback diferit între renderer-e.

Închis prin `AtfDurationParserTest` și contractul runtime al
`AtfDurationFormatter`: `0` rămâne o durată validă, iar valoarea absentă,
malformată sau negativă este reprezentată prin `—`. Nu mai există un ToDo
activ pentru parser sau pentru markerul de fallback; eventualele fixture-uri
noi intră la D-01 numai dacă cer o proiecție nouă a duratei.

## D-04 — un singur token de geometrie (DONE)

Statusul și timpul trebuie să fie centrate pe aceeași axă și să folosească
tokenurile comune ale temei în toate taburile. Nu se adaugă override-uri locale
care schimbă înălțimea, padding-ul sau raza badge-ului.

Închis prin testul computed-style
`atf.ui.tests.extent.exploratory.ApiExtentReportExploratoryTest.statusBadgesKeepSharedGeometryAtNormalAndNarrowViewport`.
Testul descoperă statusurile din artifact, compară semnătura computed-style la
viewport normal și la 520px și păstrează cerința de verificare vizuală în TAB MCP.

## Gate

Un punct se închide numai după test unit/component, artifact nou și verificare
vizuală în TAB MCP. Nu se închide prin schimbarea textului assertion-ului.


