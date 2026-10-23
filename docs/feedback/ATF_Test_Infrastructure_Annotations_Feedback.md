# ATF Test Infrastructure — gapuri rămase după extracția adnotărilor

Contractele implementate, exemplele și ownership-ul sunt în
[documentația adnotărilor](../ATF_Test_Infrastructure_Annotations.md) și în
README-urile ownerilor. Acest feedback păstrează doar verificarea manuală încă
nerealizată.

## Gap activ

| Prioritate / ID | Ce lipsește | Owner | Criteriu de închidere |
|---|---|---|---|
| P2 — ANN-IDE-07 | Nu a fost observată lansarea reală a suitei browser-backed din IntelliJ pe acest host. Testele componente simulează `user.dir`, dar nu pornesc IntelliJ. | Reporting Tests / IDE | Rulează `ExtentReportingE2eSuite` direct din IntelliJ de două ori: cu working directory la root și la `atf-reporting`. În ambele cazuri verifică raportul proaspăt în `<reactor>/artifacts/reports/ui/e2e`, nu în working directory; confirmă raportul deschis și rezultatul suitei. Nu înlocui smoke-ul cu o simulare JUnit. |

## Închidere

Discovery-ul cu `user.dir` extern, output-ul per owner, containment-ul pentru
symlink/junction, precedența configurației și lifecycle-ul consolei sunt deja
probate și documentate la owner. Nu sunt ToDo-uri active și nu se repetă aici.

După smoke-ul IntelliJ, mută numai orice regulă reutilizabilă nou descoperită în
documentația normativă; apoi elimină acest fișier și referința sa din
[registrul feedback](README.md) dacă nu mai rămâne niciun gap de infrastructură.
