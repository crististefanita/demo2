# ATF Test Infrastructure — verificări rămase după extracția adnotărilor

Output-ul UI, declarațiile de setup API și țintele de documentație există în
TestKit și sunt consumate din codul live. Contractul implementat, exemplele și
ownership-ul sunt în [documentația adnotărilor](../ATF_Test_Infrastructure_Annotations.md).

## Lacune deschise

| Prioritate / ID | Ce lipsește | Owner | Criteriu de închidere |
|---|---|---|---|
| P1 — ANN-FS-06 | proba junction trece pentru destinație nouă, dar symlink-ul nu poate fi creat pe acest host Windows (test SKIP din lipsă de permisiune) | TestKit / runner | rerulare symlink pe un runner Windows cu Developer Mode/elevare sau drepturi compatibile; SKIP nu dovedește boundary-ul |
| P2 — ANN-IDE-07 | discovery-ul din resursa modulului trece cu `user.dir` străin și cu directorul `atf-reporting-tests`; lansarea reală din IntelliJ și output-ul IDE în afara reactorului rămân neprobate | Reporting Tests / IDE | probă reală IntelliJ, cu aceeași rădăcină/destinație ca Maven și output-ul IDE extern; configurare explicită dacă discovery-ul nu găsește markerul |

## Închise prin probe automate

| ID | Ce s-a verificat | Dovadă |
|---|---|---|
| ANN-OUTPUT-08 | căi cu spații/Unicode, cale Windows invalidă și diagnostic cu owner, ancoră și cauza originală | `ReportOutputDirectoryTest` |
| ANN-RUN-10 | 32 de rezervări simultane pentru același owner/timestamp produc fișiere atomice distincte; conținutul fiecărui fișier rămâne intact; roots diferite nu se ciocnesc | `AtfUiConfigurationLoaderTest.reservesCollisionFreeReportPathsUnderParallelWritersAndIndependentRoots` |
| ANN-CONFIG-11 | default/YAML, system property peste environment, override explicit peste YAML, cale relativă lângă YAML și subdirectorul declarat | `AtfUiConfigurationLoaderTest`, `UiReportSubdirectoryTest` |
| ANN-CONSOLE-13 | JUnit engine: PASS, aborted, disabled, parameterized, dynamic pass/skip/fail, teardown failure și execuție concurentă fără scurgere ThreadLocal | `AtfConsoleTestExtensionIntegrationTest`; dynamic children folosesc explicit `AtfConsoleTestContext.dynamicTest(...)` |

## Decizii amânate — nu intră în backlog-ul activ

- Compatibilitatea unor importuri externe vechi nu are un consumator identificat
  în ATF-ul curent. Nu adăugăm aliasuri sau dependențe Core → TestKit fără un
  caz real; redeschidem decizia când există un consumator afectat.
- Metadata per invocare și un buget declarativ de execuție sunt idei de design,
  nu contracte aprobate sau funcționalitate cerută. Nu intră în scorul acestui
  feedback.

## Ce confirmă codul și probele existente

| Boundary | Confirmat | Limita rămasă |
|---|---|---|
| Selecție output | ancore distincte, direct/meta/ciclu, echivalență, override superclass, conflict, normalizare, UNC, traversal, blank/absolute/drive-relative/URI, Unicode și spații; cale nativ invalidă produce diagnostic owner/ancoră/cauză; junction-ul respinge ieșirea | symlink-ul real rămâne neprobat pe acest host din cauza permisiunilor; probele validează căi relative, nu toate filesystem provider-ele |
| Integrare Reporting | compoziția folosește resolverul TestKit; E2E declară output project-relative; rădăcina se rezolvă din resursa testului atât cu cwd străin cât și cu cwd modul | proba automată nu pornește IntelliJ efectiv și nu probează output-ul IDE extern reactorului |
| Alocare output | 32 de rezervări concurente pe același owner/timestamp creează fișiere atomice distincte; conținutul fiecăruia rămâne intact; rădăcinile independente nu se ciocnesc | sincronizarea unui director de rapoarte distribuit/network filesystem rămâne în afara contractului local |
| Precedență config | default/YAML, property peste env, override explicit peste YAML, cale relativă rezolvată lângă YAML și subdirector declarativ acoperite de teste | contractul de bază `<project-root>/artifacts/reports/ui` este intenționat fix, nu un override global |
| Setup API | referințele exacte și familiile sunt rezolvate la consumator; catalogul nu s-a mutat în kit | nu există consumator extern identificat; compatibilitatea rămâne o decizie amânată, nu lacună activă |
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

## Lifecycle JUnit pentru politica consolei

- Contractul `@AtfConsoleMessages` este documentat în [infrastructură](../ATF_Test_Infrastructure_Annotations.md), [TestKit](../../atf-test-kit/README.md) și [Console](../../atf-console/README.md).
- `AtfConsoleTestExtensionIntegrationTest` rulează probele prin JUnit Platform TestKit pentru verdicturi, disabled/aborted, teardown, parametrizare, dynamic și concurență.
- Copiii dinamici cer `AtfConsoleTestContext.dynamicTest(...)`: Jupiter nu invocă extension callbacks pentru fiecare child; helperul aplică aceeași politică și închide fiecare rezultat.
- Proba este trecută; nota despre dynamic este o limită de API și regula de utilizare, nu o lacună rămasă.

Regulile stabile de design și limita de coeziune a pachetelor sunt păstrate în
[bunele practici de cod](../README_Atf_CodeBestPractices.md) și în
[contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md); acest
feedback nu le mai dublează.

## Legături cu celelalte cerințe

| Arie | Owner |
|---|---|
| Canal și integrare UI/API | [Feedback canale](ATF_Test_Channel_Annotations_Feedback.md) |
| Output/setup/docs implementate | [Documentația adnotărilor](../ATF_Test_Infrastructure_Annotations.md) |
| Controale Git, schemă și transfer | [Guvernanța ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor) |
| Criterii de display/evidence | [Feedback Reporting](ATF_Reporting_Feedback.md) |
