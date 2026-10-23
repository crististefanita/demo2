# ATF Java — registrul cerințelor deschise

## Pe scurt

Acest registru păstrează numai cerințele deschise, responsabilul și următorul
pas. Regulile stabile se află în documentația oficială; cerințele închise,
rapoartele de rulare și istoricul capturilor nu sunt lucrări restante.

| Zonă | Responsabilitate |
|---|---|
| [Feedback Reporting](ATF_Reporting_Feedback.md) | cerințe funcționale, lacune și criterii de acceptanță Reporting |
| [Feedback infrastructură adnotări](ATF_Test_Infrastructure_Annotations_Feedback.md) | numai limitele de mediu/IDE încă nedemonstrate |
| [Feedback Automation Exercise](ATF_Automation_Exercise_Feedback.md) | gap-uri pentru consumerul UI E2E Automation Exercise; evaluarea unei suite API rămâne separată și deschisă |
| [Feedback arhitectura testelor consumer](ATF_Consumer_Test_Architecture_Feedback.md) | boundary-ul E2E/component/API, suportul `main`/`test` și dicționarele Gherkin comune Shop/AutoEx |
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
| 1 | [Arhitectura testelor consumer Shop/AutoEx](ATF_Consumer_Test_Architecture_Feedback.md) | 38 | 27 | 20 | 8 | 1.00 | **93** | Delimitarea `src/test`/`src/main`, cele două categorii logice E2E și Component/API, selecția suitei și dicționarele Gherkin sunt cerințe deschise pentru ambii consumatori |
| 2 | [Uniformitate și interacțiuni UI/API](ATF_Reporting_Feedback.md) | 40 | 21 | 20 | 4 | 1.00 | **85** | API-03/API-04/API-05/API-06/API-08, Categories-empty, TABLE-VIEWPORT-01, UI-FAIL-01 și ASSET-SIZE-01 sunt promovate; rămân deschise GOV-API-01, PUB-01, PUB-FS-02, REPORT-PATH-01 și SHOP-CAPTURE-01 |
| 3 | [Consumer Automation Exercise](ATF_Automation_Exercise_Feedback.md) | 35 | 22 | 20 | 6 | 1.00 | **83** | `AUTOEX-EVIDENCE-MATRIX-01` cere rulare live și verificarea raportului pentru UI/UI_API/final-only; rămân evaluarea API separată, armarea cleanup-ului înainte de submit și prețul strict. Codul UI_API nu inventează un apel; live proof încă lipsește |
| 4 | [Infrastructură de adnotări și output project-relative](ATF_Test_Infrastructure_Annotations_Feedback.md) | 35 | 10 | 20 | 6 | 1.00 | **71** | containment-ul reparse point/junction, discovery-ul cu `user.dir` străin și output per owner trec; rămâne doar smoke-ul real al suitei din IntelliJ la două working directories; compatibilitatea/metadata/bugetele sunt amânate |

- Scorurile din acest registru ordonează lucrul rămas; nu sunt verdicte de calitate.

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
| Arhitectura testelor și adnotări transversale | nivelurile, resolverul de canal și extracțiile output/setup/docs sunt implementate; feedback-ul păstrează numai probele încă neconfirmate | [Ghidul de testare ATF](../ATF_Testing_Guide.md), [Contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md), [feedback infrastructură](ATF_Test_Infrastructure_Annotations_Feedback.md) |
| Politica declarativă a consolei | contractul, integrarea UI/API și matricea JUnit/lifecycle au probe verzi; `@TestFactory` folosește helperul explicit pentru verdict per copil | [ATF Console](../../atf-console/README.md), [feedback infrastructură](ATF_Test_Infrastructure_Annotations_Feedback.md) |
| Raportare: dovezi, trace, agregat, display/layout, viewport și matricea Network Inspector | API-03/API-05/API-06/API-08, Categories-empty, TABLE-VIEWPORT-01 (inclusiv Request details imbricat), UI-FAIL-01 și ASSET-SIZE-01 sunt promovate; REPORT-PATH-01 cere anunțarea căii absolute după publicarea reușită; SHOP-CAPTURE-01 cere selecția per clasă a `UNIFIED_FLOW` + `FINAL_ONLY`; lacunele rămase sunt listate în feedback | [Contractul semantic de raportare](../ATF_Reporting_Contract.md), [contractul display/layout](../ATF_Reporting_Display_Contract.md), [README ATF Reporting](../../atf-reporting/README.md), [README ATF Reporting Tests](../../atf-reporting-tests/README.md) |
| Inspector UI și politica fără reluare automată | fără cerințe deschise; redeschidere numai la încălcarea contractului | [README ATF UI](../../atf-ui/README.md), [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Selectorul detaliilor acțiunilor Practice Shop | selectabil per clasă; maparea păstrează acțiunile capturate și verdictul | [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Urmărirea achiziției și rezumatul final al fluxului Practice Shop | urmărirea generică, ordinea și captura terminală sunt închise | [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Teste E2E Practice Shop, componente API și suită de audit | politica YAML și capturile existente rămân acoperite; SHOP-CAPTURE-01 urmărește selecția per clasă a unui tabel unic cu acțiunile UI ale scenariului (inclusiv SETUP/TEST BODY) și a unei capturi terminale, fără ToDo duplicat | [README Practice Shop](../../atf-test-practice-shop/README.md), [Feedback Reporting](ATF_Reporting_Feedback.md) |
| Consumer Automation Exercise | un singur journey de business are trei variante evidence (UI, UI_API, final-only); UI_API inspectează doar traficul observat, dar lipsește run-ul live și auditul vizual; rămân evaluarea colecției API, fereastra de cleanup și comparația strictă a prețului | [README Automation Exercise](../../atf-test-auto-ex/README.md), [Feedback Automation Exercise](ATF_Automation_Exercise_Feedback.md) |
| Arhitectura testelor consumer | Shop are deja `ShopPurchaseTraceStep` și suită E2E explicită; AutoEx are trei E2E într-o suită, suporturile sunt în `src/test/java`, iar frazele Gherkin sunt literale în journey | [Feedback arhitectura testelor consumer](ATF_Consumer_Test_Architecture_Feedback.md), [README Practice Shop](../../atf-test-practice-shop/README.md), [README Automation Exercise](../../atf-test-auto-ex/README.md) |

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
