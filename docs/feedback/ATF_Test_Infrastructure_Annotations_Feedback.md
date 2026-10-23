# ATF Test Infrastructure — verificări rămase după extracția adnotărilor

Output-ul UI, declarațiile de setup API și țintele de documentație există în
TestKit și sunt consumate din codul live. Contractul implementat, exemplele și
ownership-ul sunt în [documentația adnotărilor](../ATF_Test_Infrastructure_Annotations.md).

## Lacune deschise

| Prioritate / ID | Ce lipsește | Owner | Criteriu de închidere |
|---|---|---|---|
| P2 — ANN-IDE-07 | lansarea reală a suitei browser-backed din IntelliJ nu a fost observată pe acest host; contractul automat de discovery și alegerea destinației cu `user.dir` extern sunt probate separat | Reporting Tests / IDE | rulează `ExtentReportingE2eSuite` direct din IntelliJ cu working directory la root și la `atf-reporting`, apoi confirmă că raportul apare în `<reactor>/artifacts/reports/ui/e2e`, nu în directorul de lucru; consemnează ambele rezultate |

## Închise prin probe automate

| ID | Ce s-a verificat | Dovadă |
|---|---|---|
| ANN-IDE-07a | resursa UI identifică root-ul reactorului chiar când `user.dir` simulează un director IntelliJ extern; adnotarea ownerului conduce output-ul în `<reactor>/artifacts/reports/ui/ide-smoke`, niciodată în acel director extern | `ReportingTestResourcesIdeLaunchTest`; `AtfUiConfigurationLoaderTest.reportingOutputRootComesFromItsResourceWhenTheWorkingDirectoryIsUnrelated`; `AtfUiConfigurationLoaderTest.reportingOutputRootIsStableWhenLaunchedFromTheReportingModuleDirectory` |

`ANN-FS-06` este închis: `ReportOutputDirectoryTest` probează acum atât symlink-ul
când este permis, cât și un directory junction pe Windows fără privilegiu de
symlink. Ambele variante verifică aceeași interdicție de ieșire din anchor.

Probe sincronizate din codul live și trecute în staging:
`ReportOutputDirectoryTest` **5/5**, `AtfUiConfigurationLoaderTest` **11/11** și
`ReportingTestResourcesIdeLaunchTest` **1/1**. Pe acest host, testul filesystem a
folosit directory junction; criteriul închis este containment-ul față de
reparse point, nu disponibilitatea privilegiului Windows de a crea symlink
simplu. Testul nou simulează un `user.dir` extern în același proces și verifică
rezoluția efectivă `ReportingTestResources.uiConfiguration(owner)`; nu pretinde
că a pornit IntelliJ.

## Decizii amânate — nu intră în backlog-ul activ

- Compatibilitatea unor importuri externe vechi nu are un consumator identificat
  în ATF-ul curent. Nu adăugăm aliasuri sau dependențe Core → TestKit fără un
  caz real; redeschidem decizia când există un consumator afectat.
- Metadata per invocare și un buget declarativ de execuție sunt idei de design,
  nu contracte aprobate sau funcționalitate cerută. Nu intră în scorul acestui
  feedback.

Contractele deja probate sunt documentate în
[contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md) și în
README-urile ownerilor:

- selecția output-ului;
- rezervarea concurentă;
- precedența configurației;
- lifecycle-ul consolei.

Registrul păstrează doar smoke-ul real IntelliJ din tabelul de mai sus; discovery-ul
și alegerea destinației nu mai sunt lacune de cod.

Testul componentă dovedește politica de cale cu `user.dir` extern, dar nu pornește
IntelliJ și nu dovedește că suita browser-backed se termină în acel IDE. De aceea
ANN-IDE-07 rămâne deschis până la smoke-ul descris, fără a prezenta o simulare ca
execuție reală.

Politica consolei și lifecycle-ul JUnit nu mai sunt cerințe funcționale deschise.
Verificările de adopție din diferența curentă aparțin controlului temporar din
[registrul schimbărilor de teste](ATF_Test_Changes_Feedback.md).

Regulile stabile de design și limita de coeziune a pachetelor sunt păstrate în
[bunele practici de cod](../README_Atf_CodeBestPractices.md) și în
[contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md); acest
feedback nu le mai dublează.

## Legături cu celelalte cerințe

| Arie | Owner |
|---|---|
| Output/setup/docs implementate | [Documentația adnotărilor](../ATF_Test_Infrastructure_Annotations.md) |
| Controale Git, schemă și transfer | [Guvernanța ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor) |
| Paritate UI/API, evidence și verdicturi | [Feedback Reporting](ATF_Reporting_Feedback.md) |
