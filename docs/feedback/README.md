# ATF Java — registrul ToDo curent

## Pe scurt

Acest registru păstrează numai gap-uri încă deschise, ownerul și următorul pas.
Regulile stabile se află în documentația oficială; închiderile, rapoartele de
run și istoricul screenshot-urilor nu sunt backlog.

## ToDo-uri active, în ordinea priorității

- Scor de bază: impact (40) + gap deschis (30) + posibilitate de lucru acum
  (20) + datorie editorială (10).
- Scor final: scor de bază × pondere, rotunjit la cel mai apropiat întreg.
- Pondere temporară: AI Evaluation `0.30`; toate celelalte domenii `1.00`.
- Ponderea schimbă numai ordinea de lucru, nu readiness-ul sau maturitatea
  implementării AI documentate în README-ul dedicat.

| Ordine | Owner feedback | Impact /40 | Gap /30 | Lucrabil acum /20 | Datorie doc. /10 | Pondere | Scor | Ce mai trebuie făcut |
|---:|---|---:|---:|---:|---:|---:|---:|---|
| 1 | [AI evaluation](README_AI_Evaluation.md) | 34 | 26 | 12 | 6 | 0.30 | **23** | clasificarea `AMBIGUOUS` (enum-ul există, evaluatorul nu o produce), run ledger, review-state, reporting parity, regression delta și publication path; grounding per field, snapshot-ul immutable și package cohesion sunt deja demonstrate |

- **Acoperit și testat:** PNG, redaction, API binding, journal, precondiția
  guest, ordinale Playwright, journey summary și Network Inspector.
- **Flow-uri existente:** guest `UI_API`, variantele `UI_ONLY` și E2E autentificat.
- **Auth:** `AuthenticatedPurchaseE2eTest` și `NewAccountPurchaseE2eTest` creează
  sesiunea în `SETUP`, apoi finalizează purchase-ul; rularea individuală nu cere
  VM options.
- **În afara cerinței active:** un flow separat account/catalog; assertions
  read-only nu justifică acțiuni Playwright inventate.
- **Reporting generic:** fără gap activ; coverage matrix urmărește projection,
  renderer și publication pentru Network Inspector.
- Ponderea `0.30` plasează temporar AI Evaluation sub ToDo-ul Practice Shop,
  fără să modifice readiness-ul de pilot (`78/100`) ori maturitatea implementării
  (`50/100`) din documentul AI.
- Scorurile din acest registru ordonează lucrul rămas; nu sunt verdicte de calitate.

Feedback-ul ownerului este sursa criteriului de acceptare și a dovezii cerute.
Rândurile de mai sus sunt doar indexul lucrului rămas; nu duplică istoricul sau
contractele deja implementate.

## Contracte stabile, fără gap structural

| Domeniu | Stare | Documentație oficială |
|---|---|---|
| Arhitectura testelor și matricea module × layer | fără ToDo; redeschidere numai la regresie | [ATF Testing Guide](../ATF_Testing_Guide.md), [ATF Code Best Practices](../README_Atf_CodeBestPractices.md) |
| Reporting: evidence, trace, aggregate, viewport, selecția API până la publisher și coverage matrix Network Inspector | fără ToDo activ; proiecția/markup, renderer component și publication au dovezi canonice protejate în matrice | [ATF Reporting Contract](../ATF_Reporting_Contract.md), [ATF Reporting README](../../atf-reporting/README.md), [ATF Reporting Tests README](../../atf-reporting-tests/README.md) |
| Inspector UI și politica fără replay | fără ToDo; redeschidere numai la încălcarea contractului | [ATF UI README](../../atf-ui/README.md), [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Practice Shop action-details selector | selectabil per clasă; maparea păstrează acțiunile capturate și verdictul | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Practice Shop purchase trace și journey summary terminal | trace-ul generic, ordinea și captura terminală sunt închise | [Practice Shop README](../../atf-test-practice-shop/README.md) |

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

## Responsabilitatea documentației

| Contract | Owner normativ |
|---|---|
| module și dependențe | [ATF Architecture](../ATF_Architecture.md) |
| test layers, matrice și alegerea proof-ului | [ATF Testing Guide](../ATF_Testing_Guide.md) |
| API/UI report, trace și aggregate | [ATF Reporting Contract](../ATF_Reporting_Contract.md) |
| Practice Shop flows și consumer evidence | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| reguli de cod și test design | [ATF Code Best Practices](../README_Atf_CodeBestPractices.md) |
| reguli editoriale | [ATF Documentation Best Practices](../README_Atf_DocumentationBestPractices.md) |
