# ATF Reporting — durate și geometria badge-urilor

Registrul păstrează numai contractele de raportare care încă nu sunt uniforme.

## D-01 — durată numerică și sursă explicită (PARTIAL)

Nodurile care afișează durată trebuie să aibă valoare numerică stabilă și
data-duration-source. Aceeași valoare trebuie reconciliată între Tests,
Overview, Category, Fails și detail.

Rămâne: dovada computed DOM pe toate proiecțiile.

## D-03 — durată absentă (PARTIAL)

0 ms este o durată validă. Lipsa duratei sau o valoare malformed trebuie
afișată ca —, nu ca text de fallback diferit între renderer-e.

Rămâne: fixtures pentru leaf, request fără response duration și nod agregat
fără copii.

## D-04 — un singur token de geometrie (OPEN)

Statusul și timpul trebuie să fie centrate pe aceeași axă și să folosească
tokenurile comune ale temei în toate taburile. Nu se adaugă override-uri locale
care schimbă înălțimea, padding-ul sau raza badge-ului.

Rămâne: test computed-style la viewport normal și îngust pentru Pass,
Warning, Fail și Skip, plus verificarea raportului randat în TAB MCP.

## Gate

Un punct se închide numai după test unit/component, artifact nou și verificare
vizuală în TAB MCP. Nu se închide prin schimbarea textului assertion-ului.
