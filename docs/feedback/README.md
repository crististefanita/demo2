# ATF Java — registrul ToDo curent

## Overall

Acest registru păstrează numai gap-uri încă deschise, ownerul și următorul pas.
Regulile stabile se află în documentația oficială; închiderile, rapoartele de
run și istoricul screenshot-urilor nu sunt backlog.

## ToDo-uri active, în ordinea priorității

Scorul este prioritate de intervenție, nu scor de calitate: impact (40), gap
deschis (30), posibilitate de lucru acum (20), datorie editorială (10).

| Ordine | Owner feedback | Scor | Ce mai trebuie făcut |
|---:|---|---:|---|
| 1 | [Practice Shop](ATF_Practice_Shop_API_Screen_Pyramid_Feedback.md) | 96 | închide proof-ul vizual pe raport fresh pentru screenshot/API evidence și decizia ownerului pentru fixture de cont versionat; criteriile exacte sunt în secțiunea ToDo a ownerului |
| 2 | [Reporting](ATF_Reporting_Feedback.md) | 88 | închide viewport-ul comun al tabelelor, summary-urile agregate de 2×, preview-ul System separat de JSON și proof-ul component API selection → mock → publisher |
| 3 | [AI evaluation](README_AI_Evaluation.md) | 60 | package cohesion este închisă și păzită de `AtfPackageCohesionGovernanceTest`; următoarele ToDo-uri sunt grounding ambiguity, run-level evidence ledger și publication path conform readiness snapshot-ului ownerului |

Feedback-ul ownerului este sursa criteriului de acceptare și a dovezii cerute.
Rândurile de mai sus sunt doar indexul lucrului rămas; nu duplică istoricul sau
contractele deja implementate.

## Fără ToDo activ

| Domeniu | Stare | Documentație oficială |
|---|---|---|
| Arhitectura testelor și matricea module × layer | fără ToDo; redeschidere numai la regresie | [ATF Testing Guide](../ATF_Testing_Guide.md), [ATF Code Best Practices](../README_Atf_CodeBestPractices.md) |
| Trace per checkpoint, inspector aggregate și stabilitatea disclosure-urilor | fără ToDo local; gap-urile de tabel/renderer sunt deținute de Reporting | [ATF Reporting Contract](../ATF_Reporting_Contract.md), [ATF Testing Guide](../ATF_Testing_Guide.md) |
| Inspector UI și politica fără replay | fără ToDo; redeschidere numai la încălcarea contractului | [ATF UI README](../../atf-ui/README.md), [Practice Shop README](../../atf-test-practice-shop/README.md) |

Scorurile acestor domenii nu mai apar în ordinea de lucru cât timp nu există un
gap activ. Contractele închise rămân în README-urile ownerilor și în testele
care le verifică.

## Regula de reconciliere

La fiecare actualizare se inspectează codul și testele ownerului înainte de a
schimba statusul. Un ToDo se elimină numai când criteriul lui trece cu dovada
potrivită; comportamentul stabil se promovează în README-ul oficial înainte ca
detaliul să dispară din feedback.

Un artifact vechi, un screenshot izolat sau un run verde fără legătură cu
criteriul nu închide un ToDo. Pentru build, staging, E2E și verificare vizuală se
urmează [ATF Build and Execution Safety](../ATF_Build_And_Execution_Safety.md).

## Ownership documentație

| Contract | Owner normativ |
|---|---|
| module și dependențe | [ATF Architecture](../ATF_Architecture.md) |
| test layers, matrice și alegerea proof-ului | [ATF Testing Guide](../ATF_Testing_Guide.md) |
| API/UI report, trace și aggregate | [ATF Reporting Contract](../ATF_Reporting_Contract.md) |
| Practice Shop flows și consumer evidence | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| reguli de cod și test design | [ATF Code Best Practices](../README_Atf_CodeBestPractices.md) |
| reguli editoriale | [ATF Documentation Best Practices](../README_Atf_DocumentationBestPractices.md) |
