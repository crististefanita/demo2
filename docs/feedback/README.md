# ATF Java — registrul ToDo curent

## Overall

Acest registru păstrează numai gap-uri încă deschise, ownerul și următorul pas.
Regulile stabile se află în documentația oficială; închiderile, rapoartele de
run și istoricul screenshot-urilor nu sunt backlog.

## ToDo-uri active, în ordinea priorității

Scorurile sunt priorități de intervenție, nu scoruri de calitate: impact
(40), gap deschis (30), posibilitate de lucru acum (20), datorie editorială
(10). Recalibrare după inspecția codului, testelor și artifactelor disponibile
la 21 octombrie 2026; scorul mare înseamnă că domeniul trebuie adresat mai
devreme.

| Ordine | Owner feedback | Impact /40 | Gap /30 | Lucrabil acum /20 | Datorie doc. /10 | Scor | Ce mai trebuie făcut |
|---:|---|---:|---:|---:|---:|---:|---|
| 1 | [Reporting](ATF_Reporting_Feedback.md) | 37 | 23 | 18 | 6 | **84** | măsurători browser pentru viewport-ul comun, summary-urile aggregate 2× și o probă integrată API selection → mock/lifecycle → publisher |
| 2 | [AI evaluation](README_AI_Evaluation.md) | 34 | 26 | 12 | 6 | **78** | grounding ambiguity/run ledger, review-state, reporting parity, regression delta și publication path; grounding per field, snapshot-ul immutable și package cohesion sunt deja demonstrate |
| 3 | [Practice Shop](ATF_Practice_Shop_API_Screen_Pyramid_Feedback.md) | 24 | 18 | 19 | 7 | **68** | uniformizează keyword-urile trace-ului business la un `GIVEN`, un `WHEN`, checkpoint-uri `AND` și un `THEN` final; păstrează ordinea/evidence UI+API și verifică raport fresh |

Față de scorurile anterioare, Reporting scade cu 2 puncte pentru că precedence-ul
API este acum documentat și limitat la gap-ul de test direct/compoziție; AI scade
cu 1 punct de datorie editorială după alinierea README-ului owner la grounding-ul
și snapshot-ul deja implementate. Scorul de implementare AI rămâne 50/100; cele
două scoruri de mai sus sunt numai priorități de lucru.

Feedback-ul ownerului este sursa criteriului de acceptare și a dovezii cerute.
Rândurile de mai sus sunt doar indexul lucrului rămas; nu duplică istoricul sau
contractele deja implementate.

## Fără ToDo activ

| Domeniu | Stare | Documentație oficială |
|---|---|---|
| Arhitectura testelor și matricea module × layer | fără ToDo; redeschidere numai la regresie | [ATF Testing Guide](../ATF_Testing_Guide.md), [ATF Code Best Practices](../README_Atf_CodeBestPractices.md) |
| Trace per checkpoint, inspector aggregate și stabilitatea disclosure-urilor | fără ToDo local; gap-urile de tabel/renderer sunt deținute de Reporting | [ATF Reporting Contract](../ATF_Reporting_Contract.md), [ATF Testing Guide](../ATF_Testing_Guide.md) |
| Inspector UI și politica fără replay | fără ToDo; redeschidere numai la încălcarea contractului | [ATF UI README](../../atf-ui/README.md), [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Practice Shop purchase trace | ToDo activ `SHOP-GHERKIN-DISPLAY-01`, prioritate 68; policy-ul fixture-ului și setup-ul guest sunt închise | [Practice Shop feedback](ATF_Practice_Shop_API_Screen_Pyramid_Feedback.md) |

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
