# ATF Java — registrul cerințelor deschise

## Pe scurt

Acest registru păstrează numai cerințele deschise, responsabilul și următorul
pas. Regulile stabile se află în documentația oficială; cerințele închise,
rapoartele de rulare și istoricul capturilor nu sunt lucrări restante.

## Cerințe rămase, în ordinea priorității

- Scor de bază: impact (40) + lacună deschisă (30) + posibilitate de lucru acum
  (20) + datorie editorială (10).
- Scor final: scor de bază × pondere, rotunjit la cel mai apropiat întreg.
- Pondere: `1.00` pentru fiecare domeniu activ; domeniile închise nu intră în
  ordonarea lucrului.
- Ponderea schimbă numai ordinea de lucru, nu gradul de pregătire sau maturitatea
  implementării AI documentate în README-ul dedicat.

| Ordine | Responsabil / feedback | Impact /40 | Lacună /30 | Lucrabil acum /20 | Datorie doc. /10 | Pondere | Scor | Ce mai trebuie făcut |
|---:|---|---:|---:|---:|---:|---:|---:|---|
| 1 | [Uniformitate și interacțiuni UI/API](ATF_Reporting_Feedback.md) | 40 | 29 | 20 | 7 | 1.00 | **96** | TABLE-VIEWPORT-01: plafonul System pentru toate tabelele, inclusiv SETUP/actions/assertions și toate contextele; API-05 fără bandă a fazei, GOV-API-01 structural; rămân schema, subarbore, ownership, metodă/timp, cauzalitate și PUB-01; auth și subsetul viewport request/retry sunt acoperite |
| 2 | [Evaluarea AI](README_AI_Evaluation.md#20-gap-uri-active-pentru-evaluator) | 34 | 26 | 12 | 6 | 1.00 | **78** | clasificarea `AMBIGUOUS`, registrul de dovezi la nivel de rulare, integrarea lifecycle-ului, catalogul business/review/state, publicarea și paritatea de raportare, regression delta și probele pentru fluxul agentic |

- Scorurile din acest registru ordonează lucrul rămas; nu sunt verdicte de calitate.

Contractul comparatorului AI deja executabil și probele sale sunt în
[README-ul ATF Validation](../../atf-validation/README.md#comparatorul-ai--contract-implementat).
Modelele și pattern-urile țintă au fost separate în
[designul AI al proiectului](../ATF_AI_Evaluation_Design.md); feedback-ul AI
păstrează numai gap-urile, scorurile motivate și criteriile de închidere.

Feedback-ul responsabilului de domeniu este sursa criteriului de acceptare și a
dovezii cerute.
Rândurile de mai sus sunt doar indexul lucrului rămas; nu duplică istoricul sau
contractele deja implementate.

## Contracte promovate și stări păstrate

| Domeniu | Stare | Documentație oficială |
|---|---|---|
| Arhitectura testelor și matricea modul × strat | fără cerințe deschise; redeschidere numai la regresie | [Ghidul de testare ATF](../ATF_Testing_Guide.md), [Bune practici de cod ATF](../README_Atf_CodeBestPractices.md) |
| Detalii implicite la consolă și schimbări de teste prin ToDo | contractele și probele negative sunt la owner; registrul YAML păstrează numai scope-ul necesar baseline-ului, nu un backlog funcțional nou | [ATF Console](../../atf-console/README.md), [ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor), [registrul scope-urilor](ATF_Test_Changes_Feedback.md) |
| Raportare: dovezi, trace, agregat, viewport și matricea Network Inspector | probele de viewport existente rămân regresii; acoperirea tuturor tabelelor și contextelor este TABLE-VIEWPORT-01, alături de lacunele UI/API rămase | [Contractul de raportare ATF](../ATF_Reporting_Contract.md), [README ATF Reporting](../../atf-reporting/README.md), [README ATF Reporting Tests](../../atf-reporting-tests/README.md) |
| Inspector UI și politica fără reluare automată | fără cerințe deschise; redeschidere numai la încălcarea contractului | [README ATF UI](../../atf-ui/README.md), [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Selectorul detaliilor acțiunilor Practice Shop | selectabil per clasă; maparea păstrează acțiunile capturate și verdictul | [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Urmărirea achiziției și rezumatul final al fluxului Practice Shop | urmărirea generică, ordinea și captura terminală sunt închise | [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Teste E2E Practice Shop, componente API și suită de audit | politica YAML de captură și matricea facturilor rămân acoperite; intenția apelurilor, metadatele auditului și republicarea înregistrării la login sunt urmărite în Reporting, fără backlog duplicat | [README Practice Shop](../../atf-test-practice-shop/README.md), [Feedback Reporting](ATF_Reporting_Feedback.md) |

Cerințele deschise rămân numai în tabelul prioritizat de mai sus. Rândurile fără
cerință deschisă descriu contracte deja promovate în README-urile modulelor și
verificate de testele aferente.

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
| fluxuri Practice Shop și dovezile consumerului | [README Practice Shop](../../atf-test-practice-shop/README.md) |
| reguli de cod și proiectarea testelor | [Bune practici de cod ATF](../README_Atf_CodeBestPractices.md) |
| reguli editoriale | [Bune practici de documentare ATF](../README_Atf_DocumentationBestPractices.md) |
