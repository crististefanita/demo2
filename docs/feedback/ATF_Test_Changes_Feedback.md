# ATF — controale tehnice active ale schimbărilor de teste

Acest registru conține exclusiv controalele `ACTIVE` pentru diferențele Git
aflate în lucru. În prezent este gol. Fișierul rămâne intenționat în repository:
guvernanța ATF Tests și ținta de documentație îl cer ca intrare stabilă. Cele
`VERIFIED` și perimetrele lor exacte sunt păstrate colapsat în
[registrul baseline](../ATF_Test_Changes_Baseline.md#controale-tehnice-verificate).

## Regula de întreținere

Înaintea unei modificări protejate, adaugă aici un control cu ID, baseline Git
imuabil, expirare, motiv, path-uri exacte și comanda Maven de probă. După
verificare, mută controlul în registrul `VERIFIED`; elimină-l când diferența nu
mai există față de baza folosită de guvernanță. Nu păstra aici rezultate
închise, istoric sau cerințe business.

Schema executabilă, lifecycle-ul și limitele controlului sunt definite în
[README ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).
Cerințele funcționale rămân în feedback-ul domeniului, iar indexul feedback-urilor
se păstrează în [README-ul feedback](README.md).
