# ATF Java — registrul cerințelor deschise

## Pe scurt

Acest registru păstrează numai cerințele deschise, responsabilul și următorul
pas. Regulile stabile se află în documentația oficială; cerințele închise,
rapoartele de rulare și istoricul capturilor nu sunt lucrări restante.

| Zonă | Responsabilitate |
|---|---|
| [Feedback Reporting](ATF_Reporting_Feedback.md) | cerințe funcționale, lacune și criterii de acceptanță Reporting |
| [Feedback Automation Exercise](ATF_Automation_Exercise_Feedback.md) | matricea UI/UI+API/final-only este verificată; decizia documentată este să nu duplicăm purchase UI într-o suită API standalone |
| [Feedback arhitectura testelor consumer](ATF_Consumer_Test_Architecture_Feedback.md) | **închis**: boundary `main`/`test`, suite componentă, eliminarea testului redundant și cataloagele Gherkin Shop/AutoEx |
| [Registrul controalelor verificate](../ATF_Test_Changes_Baseline.md) | context business, roluri, reguli și controale `VERIFIED` colapsate |

Indexul se păstrează permanent. Feedback-ul de domeniu fără lacune se elimină
după promovarea conținutului stabil și actualizarea referințelor. Un control
tehnic `ACTIVE`, dacă este necesar, se atașează feedback-ului ownerului; nu există
un registru activ separat.

## Cerințe rămase, în ordinea priorității

- Scor de bază: impact (40) + lacună deschisă (30) + posibilitate de lucru acum
  (20) + datorie editorială (10).
- Scor final: scor de bază × pondere, rotunjit la cel mai apropiat întreg.
- Pondere: `1.00` pentru fiecare domeniu activ; domeniile închise nu intră în
  ordonarea lucrului.
- Elementele amânate explicit nu intră în clasamentul activ; scorul lor anterior
  rămâne doar context istoric și nu exprimă prioritatea curentă.

| Ordine | Responsabil / feedback | Impact /40 | Lacună /30 | Lucrabil acum /20 | Datorie doc. /10 | Pondere | Scor | Ce mai trebuie făcut |
|---:|---|---:|---:|---:|---:|---:|---:|---|
| 1 | [Uniformitate și interacțiuni UI/API](ATF_Reporting_Feedback.md) | 40 | 21 | 20 | 4 | 1.00 | **85** | API-03/API-04/API-05/API-06/API-08, Categories-empty, TABLE-VIEWPORT-01, UI-FAIL-01, ASSET-SIZE-01, PUB-01 și PUB-FS-02 sunt promovate; GOV-API-01 are gate verde (135 teste, 0 failure/error/skip) și probe pentru Request/Response absent/mutat (8/8). Rămân izolarea sibling-urilor, interacțiunea vizibilă și verdicturile UI/API mixte; REPORT-PATH-01 și SHOP-CAPTURE-01 sunt deschise |

- Scorurile din acest registru ordonează lucrul rămas; nu sunt verdicte de calitate.

Automation Exercise și arhitectura testelor consumer nu mai au ToDo-uri active
în această revizie; deciziile, limita de scope și probele sunt păstrate în
feedback-urile lor pentru trasabilitate.

## Amânat — prioritate și importanță foarte joase

| Feedback | Prioritate curentă | Scor separat, nu prioritate |
|---|---|---:|
| [Evaluarea AI](README_AI_Evaluation.md) | **Foarte joasă — amânată; nu face parte din revizuirea curentă** | readiness 78/100 în document; nu este scor de prioritate |

Conținutul feedback-ului AI nu a fost reevaluat și scorul nu a fost recalculat.
Nu îl reluăm înaintea cerințelor active decât la o solicitare explicită.

Feedback-ul responsabilului de domeniu este sursa criteriului de acceptare și a
dovezii cerute.
Rândurile de mai sus sunt doar indexul lucrului rămas; nu duplică istoricul sau
contractele deja implementate.

## Contracte promovate și stări păstrate

| Domeniu | Stare | Documentație oficială |
|---|---|---|
| Selecția canalelor de test | contractul `@AtfTestChannel` și resolverul UI/API/UI_API sunt probate; fără ToDo funcțional deschis; redeschidere doar la regresie sau consumer nou | [Contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md), [README TestKit](../../atf-test-kit/README.md) |
| Arhitectura testelor și adnotări transversale | nivelurile, resolverul de canal și extracțiile output/setup/docs sunt implementate; smoke-testul IDE pentru output project-relative este o procedură la schimbări relevante, nu un gap permanent | [Ghidul de testare ATF](../ATF_Testing_Guide.md), [Contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md), [procedura IntelliJ Reporting](../../atf-reporting-tests/src/test/java/atf/ui/tests/extent/README.md#rulare-directă-din-intellij) |
| Politica declarativă a consolei | contractul, integrarea UI/API și matricea JUnit/lifecycle au probe verzi; `@TestFactory` folosește helperul explicit pentru verdict per copil | [ATF Console](../../atf-console/README.md), [contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md) |
| Raportare: dovezi, trace, agregat, display/layout, viewport și matricea Network Inspector | API-03/API-04/API-05/API-06/API-08, Categories-empty, TABLE-VIEWPORT-01 (inclusiv Request details imbricat), UI-FAIL-01, ASSET-SIZE-01, PUB-01 și PUB-FS-02 sunt promovate. Probele generice apar cu precădere în `atf-reporting-tests`, pe fixture-uri neutre/mock; testele de extensie JUnit și wiring consumer rămân la ownerii lor. GOV-API-01 mai cere negative pentru Request structure/ownership și sibling isolation, verificarea interacțiunii vizibile și verdicte UI/API mixte; REPORT-PATH-01 cere notificarea căii absolute după publish; SHOP-CAPTURE-01 cere profil per clasă `UNIFIED_FLOW` + `FINAL_ONLY` peste `SETUP` + `TEST BODY` | [Contractul semantic de raportare](../ATF_Reporting_Contract.md), [contractul display/layout](../ATF_Reporting_Display_Contract.md), [README ATF Reporting](../../atf-reporting/README.md), [README ATF Reporting Tests](../../atf-reporting-tests/README.md) |
| Inspector UI și politica fără reluare automată | fără cerințe deschise; redeschidere numai la încălcarea contractului | [README ATF UI](../../atf-ui/README.md), [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Selectorul detaliilor acțiunilor Practice Shop | selectabil per clasă; maparea păstrează acțiunile capturate și verdictul | [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Urmărirea achiziției și rezumatul final al fluxului Practice Shop | urmărirea generică, ordinea și captura terminală sunt închise | [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Teste E2E Practice Shop, componente API și suită de audit | SHOP-CAPTURE-01 rămâne un singur gap: profilul per clasă pentru un tabel cu acțiunile din `SETUP` + `TEST BODY` și captura terminală; proba agregării neutre aparține `atf-reporting-tests`, iar Shop verifică doar rezolvarea adnotării și wiring-ul | [README Practice Shop](../../atf-test-practice-shop/README.md), [Feedback Reporting](ATF_Reporting_Feedback.md) |
| Consumer Automation Exercise | un singur purchase journey are trei variante evidence (UI, UI_API, final-only), selectate într-un singur raport; run-ul 3/3 și auditul vizual au trecut, iar schimbul read-only este atașat checkpoint-ului explicit de precondiție. Rămâne numai evaluarea unei colecții API standalone și a contractelor sale | [README Automation Exercise](../../atf-test-auto-ex/README.md), [Feedback Automation Exercise](ATF_Automation_Exercise_Feedback.md) |
| Arhitectura testelor consumer | Shop are cinci E2E într-o suită și suite component/API cu suprapuneri; AutoEx are trei E2E într-o suită, trei teste componente fără suită comună, iar suportul consumer este încă în `src/test/java`; cerințele de curățare, ownership și discoverability sunt urmărite împreună cu boundary-ul și Gherkin | [Feedback arhitectura testelor consumer](ATF_Consumer_Test_Architecture_Feedback.md), [README Practice Shop](../../atf-test-practice-shop/README.md), [README Automation Exercise](../../atf-test-auto-ex/README.md) |

Cerințele funcționale deschise rămân numai în tabelul prioritizat de mai sus;
controalele `ACTIVE` de guvernanță a schimbărilor sunt blocuri YAML în feedback-ul
ownerului, cât timp diff-ul le cere. Rândurile fără cerință deschisă descriu
contracte promovate în README-urile modulelor și verificate de testele aferente.

## Regula de reconciliere

La fiecare actualizare se inspectează codul și testele responsabilului înainte de
a schimba starea. O cerință se închide numai când criteriul ei trece cu dovada
potrivită; comportamentul stabil se promovează în documentația oficială înainte
ca detaliul să dispară din feedback.

Un artefact vechi, o captură izolată sau o rulare verde fără legătură cu
criteriul nu închide o cerință. Pentru compilare, staging, E2E și verificare
vizuală se urmează [Siguranța compilării și rulării ATF](../ATF_Build_And_Execution_Safety.md).

## Responsabilitatea documentației

| Contract | Responsabil normativ |
|---|---|
| module și dependențe | [Arhitectura ATF](../ATF_Architecture.md) |
| straturi de testare, matrice și alegerea dovezii | [Ghidul de testare ATF](../ATF_Testing_Guide.md) |
| raportare API/UI, trace și agregare | [Contractul de raportare ATF](../ATF_Reporting_Contract.md) |
| display, layout, disclosure și responsive viewport | [Contractul de display și layout](../ATF_Reporting_Display_Contract.md) |
| fluxuri Practice Shop și dovezile consumerului | [README Practice Shop](../../atf-test-practice-shop/README.md) |
| reguli de cod și proiectarea testelor | [Bune practici de cod ATF](../README_Atf_CodeBestPractices.md) |
| reguli editoriale | [Bune practici de documentare ATF](../README_Atf_DocumentationBestPractices.md) |
