# ATF Test Infrastructure — verificări rămase după extracția adnotărilor

Output-ul UI, declarațiile de setup API și țintele de documentație există în
TestKit și sunt consumate din codul live. Contractul implementat, exemplele și
ownership-ul sunt în [documentația adnotărilor](../ATF_Test_Infrastructure_Annotations.md).

## Lacune deschise

| Prioritate / ID | Ce lipsește | Owner | Criteriu de închidere |
|---|---|---|---|
| P1 — ANN-FS-06 | proba symlink Windows este omisă când sistemul refuză crearea linkului; junction-ul nu are probă dedicată | TestKit / runner | probe executate pentru strămoș existent și destinație nouă, cu refuzul ieșirii din ancoră; SKIP rămâne lipsă de dovadă |
| P2 — ANN-IDE-07 | root-ul se descoperă acum din resursa modulului înainte de `user.dir`, iar testul cu working directory străin trece; lansările reale Maven din modul și IntelliJ rămân neprobate | Reporting Tests / IDE | aceeași rădăcină și destinație ca în Maven din reactor, inclusiv când output-ul IDE e în afara reactorului; configurare explicită dacă discovery-ul nu poate găsi markerul |
| P2 — ANN-OUTPUT-08 | testele acoperă meta-adnotări, cicluri, declarații echivalente/conflictuale, normalizare, UNC și traversal; lipsesc spații/Unicode, path invalid nativ și diagnostic uniform (`Path.of` poate arunca direct `InvalidPathException`) | TestKit / Reporting Tests | matrice nominală și negativă completă, cu diagnostic de owner/ancoră/cauză, nu doar tipul excepției |
| P2 — ANN-COMPAT-09 | decizia de compatibilitate pentru vechile importuri publice din Core | ownerii API-ului TestKit/Core | migrare documentată pentru consumatori; fără dependență Core main către TestKit și fără alias care creează ciclu |
| P2 — ANN-RUN-10 | lipsește proba output-ului concurent; `timestampedReportFileName` are precizie de milisecundă, deci același prefix/suffix și aceeași milisecundă pot produce același nume | consumator / Reporting Tests | probă deterministă cu același ceas/owner, invocări concurente și rădăcini diferite; fișiere distincte, nicio scriere în live din staging și nicio suprascriere |
| P2 — ANN-CONFIG-11 | `ReportingTestResources` fixează baza `<project-root>/artifacts/reports/ui` după citirea YAML, deci `ui.report.directory` configurat nu decide output-ul acestui consumer | Reporting Tests / config | precedență explicită și testată pentru default, YAML, adnotare și override; fără promisiune falsă de configurabilitate |

## Ce confirmă codul și probele existente

| Boundary | Confirmat | Limita rămasă |
|---|---|---|
| Selecție output | ancore distincte, direct/meta/ciclu, echivalență, override superclass, conflict, normalizare, UNC, traversal, cale goală, absolută, drive-relative și URI | spații/Unicode, diagnostic complet și proba symlink/junction rămân deschise |
| Integrare Reporting | compoziția folosește resolverul TestKit; E2E declară output project-relative; root-ul vine întâi din resursa testului, cu test pentru working directory străin | proba Maven/browser nu certifică lansarea reală IntelliJ sau output-ul IDE în afara reactorului |
| Setup API | referințele exacte și familiile sunt rezolvate la consumator; catalogul nu s-a mutat în kit | compatibilitatea importurilor externe |
| Documentație | declarație repeatable în TestKit; validatorii și lista țintelor rămân în ATF Tests | nu se deduce suport generic de meta-adnotări pentru această declarație |
| Boundary Maven | kit-ul este test-only; Reporting rămâne standalone; contractele Shop și Reporting rămân locale | orice extensie viitoare trebuie să păstreze graful și limita de opt clase |

Implementarea verifică strămoșii existenți prin real-path, însă testul care
necesită crearea unui symlink poate fi SKIP. Această diferență între cod,
probă executată și probă lipsă se păstrează la reconcilierea controlului tehnic.

## Reguli pentru probele restante

- Proiectul din staging este checkout-ul extern; output-ul relativ se rezolvă acolo.
- Kit-ul primește rădăcina și baza UI; discovery-ul și precedența YAML aparțin consumatorului.
- Verificarea de filesystem trebuie făcută înaintea publicării; containment-ul lexical singur nu dovedește siguranța linkurilor.
- Verificarea output-ului păstrează input-ul auditului, politica screenshot și identitatea fișierelor.
- Catalogul API concret, mock-urile, browserul și permisiunile pentru efecte externe rămân în consumator.
- Un gate verde cu probe de mediu omise nu închide cerința de mediu.
- Promovarea unui control tehnic verificat nu elimină aceste criterii restante.

## Adnotări utile de proiectat — consola este prima prioritate

Aceste contracte sunt propuneri noi, încă neimplementate. Punctul de plecare
este `ConsoleSettings` și extensia `AtfConsoleTestExtension` din `atf-console`;
TestKit nu primește o dependență către acel modul sau către Core.

Ordinea propusă este: plan/resolver neutru → adaptor Console → doi consumatori
reali → probe lifecycle/izolare. Output-ul și canalele deja implementate nu se
rescriu ca parte a acestei extensii. `ConsoleSettings.enabled/detailsEnabled`
rămâne detaliul adaptorului, nu o matrice nouă de booleeni în test.

| Prioritate / ID | Cerință | Owner | Criteriu de acceptare |
|---|---|---|---|
| P1 — ANN-CONSOLE-11 | declarație reutilizabilă pentru mesajele de consolă, nume propus `AtfConsoleMessages` | TestKit / Console | politică typed `INHERIT`, `DETAILS`, `SUMMARY`, `OFF`; fără combinații de booleeni pe test; absența păstrează configurația curentă |
| P1 — ANN-CONSOLE-12 | aplicarea politicii în adaptoarele API, UI și Reporting | Console / consumatori | același plan înaintea lifecycle-ului; fără `System.setProperty`, modificarea YAML sau un reporter suplimentar care dublează mesajele |
| P1 — ANN-CONSOLE-13 | lifecycle și izolarea mesajelor | Console / consumatori | nominal, fail, aborted, disabled, teardown, parameterized/dynamic și concurență; un singur start/end, nicio scurgere `ThreadLocal`, captură de stdout deterministă |
| P2 — ANN-METADATA-14 | folosirea coerentă a `Tag` și `DisplayName` existente în consolă și raport | TestKit / consumatori / Reporting | metadata per invocare, deduplicată și fidelă sursei; canalul nu devine un tag decorativ; o adnotare nouă cere un gap neacoperit de JUnit |
| P2 — ANN-BUDGET-15 | evaluarea unui buget de execuție declarativ reutilizabil | TestKit / consumatori | numai cu minimum doi consumatori și DTO-uri de durată existente; nu dublează `@Timeout`, nu confundă action timeout cu scenariul și nu acordă permisiune pentru retry/mutații |

### Reguli de proiectare pentru consolă

- Declarația și planul sunt neutre; adaptorul Console le mapează la
  `ConsoleSettings`, păstrând un singur formatter și un singur sink.
- Prima versiune este per clasă și meta-adnotare, cu precedență explicită
  față de superclass și refuzul conflictelor; selecția per metodă se adaugă
  numai cu un caz real și probe distincte.
- `INHERIT` păstrează YAML/default-ul. Dezactivarea globală explicită rămâne
  autoritară; adnotarea nu reactivează o consolă oprită de politica proiectului.
- `SUMMARY` păstrează lifecycle-ul și verdictul; `DETAILS` adaugă detaliile
  structurale existente; `OFF` oprește doar proiecția consolei.
- Mesajele folosesc evenimentele typed și titlurile semantice existente;
  adnotarea nu introduce JSON, selectors sau template-uri mari în test.
- Redactarea se aplică înaintea sink-ului în orice mod; PII, tokenurile și
  datele de autentificare nu sunt expuse prin selectarea `DETAILS`.
- Politica consolei nu schimbă execuția, capturile, HTTP-ul, assertions,
  verdictul sau conținutul normativ al raportului.
- Testele verifică și consumul prin doi adaptoare reale, fără browser/live
  pentru matricea pură de policy și lifecycle.

### Eleganță și guvernanță

- Se preferă extinderea declarațiilor și resolverelor existente înaintea unei noi familii de adnotări.
- Fiecare opțiune are un owner, un default, un efect și o probă negativă.
- Enum-urile poartă vocabular stabil; valorile dinamice rămân în config/fixture.
- Fără scanner global, lifecycle paralel sau interfață introdusă fără doi consumatori reali.
- Pachetele rămân coezive, cu maximum opt clase; formatterul și reporterul rămân în Console.
- După implementare se promovează numai API-ul demonstrat în documentația de profil; aceste propuneri nu sunt prezentate drept funcționalitate livrată.

## Legături cu celelalte cerințe

| Arie | Owner |
|---|---|
| Canal și integrare UI/API | [Feedback canale](ATF_Test_Channel_Annotations_Feedback.md) |
| Output/setup/docs implementate | [Documentația adnotărilor](../ATF_Test_Infrastructure_Annotations.md) |
| Controale Git, schemă și transfer | [Guvernanța ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor) |
| Criterii de display/evidence | [Feedback Reporting](ATF_Reporting_Feedback.md) |
