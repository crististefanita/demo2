# ATF Java — backlog activ de refactorizare

Acesta este backlog-ul tehnic scurt. Pentru criteriul de închidere și dovada
live se citește README.md.

## Ordine

    F-09 → F-12 → UI-15 → F-10 → F-13 → F-01/F-02 → F-06/F-07 → D-01/D-03/D-04

## Matrice

| ID | Status | Owner | Dovadă cerută |
|---|---|---|---|
| F-01 | OPEN | atf-reporting | orchestration split fără schimbarea artifactului |
| F-02 | OPEN | atf-reporting | snapshot semantic comun pentru Extent și fallback |
| F-03 | PARTIAL | composition root | precedence, root-scoping și rulări paralele |
| F-05 | PARTIAL | atf-tests | DOM/computed-style pe toate taburile |
| F-06 | OPEN | architecture tests | importuri, static state, inventory și limita de 8 clase |
| F-07 | PARTIAL | metadata/reporting | package Java real inclusiv atf-tests/src/main/java |
| F-09 | OPEN | atf-ui/atf-tests | failure flush garantat și test negativ |
| F-10 | PARTIAL | atf-tests | validatori DOM și lifecycle extrase în suport |
| F-12 | OPEN | atf-ui/atf-reporting | ACTION și OBSERVE vizibile distinct |
| F-13 | PARTIAL | atf-ui | prefix output derivat din config |
| UI-15 | PARTIAL | atf-tests | matrice zero/one/many |
| D-01 | PARTIAL | atf-reporting | duration marker/source pe toate proiecțiile |
| D-03 | PARTIAL | atf-reporting | durată absentă uniformă (—) |
| D-04 | OPEN | atf-reporting | un singur token geometric pentru badge/status |

F-11, UI-14 și UI-16 sunt rezolvate și eliminate din backlog-ul activ.

## Gate comun

Pentru fiecare increment: sync cu codul live, staging nou, test întâi,
implementare minimă, build/test, artifact nou și verificare în TAB MCP. Un
test relaxat sau un raport vechi nu este dovadă.
