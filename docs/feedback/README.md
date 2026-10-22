# ATF Java — registrul ToDo curent

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
| 1 | [Semantica auditului API](ATF_Reporting_Feedback.md) | 38 | 30 | 18 | 9 | 1.00 | **95** | separă verdictul JUnit de dovezile fiecărui schimb și de validarea contractului; nu plasa verdictul JUnit într-o validare per schimb; aliniază API-ul publisherului cu extensia și testele consumerului |
| 2 | [Derularea tabelului TECHNICAL TRACE](ATF_Reporting_Feedback.md) | 34 | 26 | 17 | 8 | 1.00 | **85** | aplică plafonul comun de derulare, păstrează JSON-ul separat și adaugă probe browser pentru tabelul din `Response details` în renderer-ele suportate |
| 3 | [Evaluarea AI](README_AI_Evaluation.md) | 34 | 26 | 12 | 6 | 1.00 | **78** | clasificarea `AMBIGUOUS` (enum-ul există, evaluatorul nu o produce), registrul de dovezi la nivel de rulare, revizuire/stare, paritate de raportare, diferența față de regresia anterioară și calea de publicare; fundamentarea la nivel de câmp, instantaneul imuabil și coeziunea pachetelor sunt deja demonstrate |

- Scorurile din acest registru ordonează lucrul rămas; nu sunt verdicte de calitate.

Feedback-ul ownerului este sursa criteriului de acceptare și a dovezii cerute.
Rândurile de mai sus sunt doar indexul lucrului rămas; nu duplică istoricul sau
contractele deja implementate.

## Contracte promovate și stări păstrate

| Domeniu | Stare | Documentație oficială |
|---|---|---|
| Arhitectura testelor și matricea module × layer | fără ToDo; redeschidere numai la regresie | [ATF Testing Guide](../ATF_Testing_Guide.md), [ATF Code Best Practices](../README_Atf_CodeBestPractices.md) |
| Reporting: dovezi, trace, agregat, viewport și matricea Network Inspector | semantica auditului API este deschisă, scor 95; derularea tabelului `TECHNICAL TRACE` este deschisă, scor 85 | [ATF Reporting Contract](../ATF_Reporting_Contract.md), [ATF Reporting README](../../atf-reporting/README.md), [ATF Reporting Feedback](ATF_Reporting_Feedback.md) |
| Inspector UI și politica fără replay | fără ToDo; redeschidere numai la încălcarea contractului | [ATF UI README](../../atf-ui/README.md), [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Practice Shop action-details selector | selectabil per clasă; maparea păstrează acțiunile capturate și verdictul | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Practice Shop purchase trace și journey summary terminal | trace-ul generic, ordinea și captura terminală sunt închise | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Practice Shop E2E, API component și API-call audit suites | politica YAML de captură se aplică și în `ShopE2eSuite`; matricea deterministă verifică statusurile de succes ale facturilor conform OpenAPI; nu rămâne ToDo Practice Shop separat | [Practice Shop README](../../atf-test-practice-shop/README.md); semantica publisherului API rămâne la ownerul Reporting |

Gap-urile active rămân numai în tabelul prioritizat de mai sus. Rândurile fără
ToDo activ descriu contracte deja promovate în README-urile ownerilor și
verificate de testele aferente.

## Regula de reconciliere

La fiecare actualizare se inspectează codul și testele ownerului înainte de a
schimba statusul. Un ToDo se elimină numai când criteriul lui trece cu dovada
potrivită; comportamentul stabil se promovează în README-ul oficial înainte ca
detaliul să dispară din feedback.

Un artifact vechi, un screenshot izolat sau un run verde fără legătură cu
criteriul nu închide un ToDo. Pentru build, staging, E2E și verificare vizuală se
urmează [ATF Build and Execution Safety](../ATF_Build_And_Execution_Safety.md).

## Responsabilitatea documentației

| Contract | Responsabil normativ |
|---|---|
| module și dependențe | [ATF Architecture](../ATF_Architecture.md) |
| test layers, matrice și alegerea proof-ului | [ATF Testing Guide](../ATF_Testing_Guide.md) |
| API/UI report, trace și aggregate | [ATF Reporting Contract](../ATF_Reporting_Contract.md) |
| Practice Shop flows și consumer evidence | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| reguli de cod și test design | [ATF Code Best Practices](../README_Atf_CodeBestPractices.md) |
| reguli editoriale | [ATF Documentation Best Practices](../README_Atf_DocumentationBestPractices.md) |
