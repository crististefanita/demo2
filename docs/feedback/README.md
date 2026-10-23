# ATF Java — registrul cerințelor deschise

## Pe scurt

Acest registru păstrează numai cerințele deschise, responsabilul și următorul
pas. Regulile stabile se află în documentația oficială; cerințele închise,
rapoartele de rulare și istoricul capturilor nu sunt lucrări restante.

| Zonă | Responsabilitate |
|---|---|
| [Feedback Reporting](ATF_Reporting_Feedback.md) | cerințe funcționale, lacune și criterii de acceptanță Reporting |
| [Feedback Automation Exercise](ATF_Automation_Exercise_Feedback.md) | cerințe pentru consumerul UI/API separat al site-ului Automation Exercise |
| [Controale tehnice active](ATF_Test_Changes_Feedback.md) | numai `ACTIVE`, legate de cerințe și diferențe Git în lucru |
| [Registrul controalelor verificate](../ATF_Test_Changes_Baseline.md) | context business, roluri, reguli și controale `VERIFIED` colapsate |

Indexul se păstrează permanent. Feedback-ul fără lacune sau controale active
se elimină după promovarea conținutului stabil și actualizarea referințelor.

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
| 1 | [Uniformitate și interacțiuni UI/API](ATF_Reporting_Feedback.md) | 40 | 23 | 20 | 4 | 1.00 | **87** | API-03 și filtrul Categories fără rezultate sunt verificate; rămân UI-FAIL-01 (proba browser 3/4 eșuată), cauzalitate API-04, ownership API-06, gate/GOV-API-01, limitele PUB-01 și cleanup Windows PUB-FS-02 |
| 2 | [Adnotări UI, API și UI+API](ATF_Test_Channel_Annotations_Feedback.md) | 38 | 24 | 20 | 6 | 1.00 | **88** | aliasul `api` are diagnostic/API-only explicit, iar selecția absentă vs. goală și preflight-ul UI+API sunt probate; rămân completitudinea terminală și matricea verdict/proiecții/lifecycle pentru adaptoarele Shop/Reporting |
| 3 | [Consumer Automation Exercise](ATF_Automation_Exercise_Feedback.md) | 35 | 22 | 18 | 8 | 1.00 | **83** | rămân corelarea UI_API pentru căutare, o colecție API publică cu formate probate, reziliența cleanup-ului de cont și politica de drift a catalogului |
| 4 | [Infrastructură de adnotări și output project-relative](ATF_Test_Infrastructure_Annotations_Feedback.md) | 35 | 12 | 20 | 6 | 1.00 | **73** | Unicode/căi invalide, rezervare concurentă fără suprascriere, precedență config și lifecycle JUnit au probe; rămân permisiunea symlink pe Windows și lansarea reală IntelliJ; compatibilitatea/metadata/bugetele sunt amânate |

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
| Arhitectura testelor și adnotări transversale | nivelurile, resolverul de canal și extracțiile output/setup/docs sunt implementate; feedback-ul păstrează numai probele încă neconfirmate | [Ghidul de testare ATF](../ATF_Testing_Guide.md), [Contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md), [feedback infrastructură](ATF_Test_Infrastructure_Annotations_Feedback.md) |
| Politica declarativă a consolei | contractul, integrarea UI/API și matricea JUnit/lifecycle au probe verzi; `@TestFactory` folosește helperul explicit pentru verdict per copil | [ATF Console](../../atf-console/README.md), [feedback infrastructură](ATF_Test_Infrastructure_Annotations_Feedback.md) |
| Raportare: dovezi, trace, agregat, display/layout, viewport și matricea Network Inspector | TABLE-VIEWPORT-01 și fidelitatea metodei/timpilor API sunt promovate la owneri; bugetele asset-urilor au guard de regresie, nu ToDo separat; matricea extinsă și lacunele UI/API rămase sunt în feedback | [Contractul semantic de raportare](../ATF_Reporting_Contract.md), [contractul display/layout](../ATF_Reporting_Display_Contract.md), [README ATF Reporting](../../atf-reporting/README.md), [README ATF Reporting Tests](../../atf-reporting-tests/README.md) |
| Inspector UI și politica fără reluare automată | fără cerințe deschise; redeschidere numai la încălcarea contractului | [README ATF UI](../../atf-ui/README.md), [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Selectorul detaliilor acțiunilor Practice Shop | selectabil per clasă; maparea păstrează acțiunile capturate și verdictul | [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Urmărirea achiziției și rezumatul final al fluxului Practice Shop | urmărirea generică, ordinea și captura terminală sunt închise | [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Teste E2E Practice Shop, componente API și suită de audit | politica YAML de captură și matricea facturilor rămân acoperite; intenția apelurilor, metadatele auditului și republicarea înregistrării la login sunt urmărite în Reporting, fără backlog duplicat | [README Practice Shop](../../atf-test-practice-shop/README.md), [Feedback Reporting](ATF_Reporting_Feedback.md) |
| Consumer Automation Exercise | contractul de bază UI și fluxul complet sunt în README-ul modulului; numai gap-urile UI/API, API și reziliența live rămân deschise | [README Automation Exercise](../../atf-test-auto-ex/README.md), [Feedback Automation Exercise](ATF_Automation_Exercise_Feedback.md) |

Cerințele funcționale deschise rămân numai în tabelul prioritizat de mai sus;
controalele `ACTIVE` de guvernanță a schimbărilor sunt urmărite separat în
[registrul dedicat](ATF_Test_Changes_Feedback.md). Rândurile fără cerință
deschisă descriu contracte promovate în README-urile modulelor și verificate
de testele aferente.

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
