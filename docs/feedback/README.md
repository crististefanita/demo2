# ATF Java — registrul ToDo curent

## Overall

Acest registru păstrează numai gap-uri încă deschise, ownerul și următorul pas.
Regulile stabile se află în documentația oficială; închiderile, rapoartele de
run și istoricul screenshot-urilor nu sunt backlog.

## ToDo-uri active, în ordinea priorității

Scorurile sunt priorități de intervenție, nu scoruri de calitate: impact
(40), gap deschis (30), posibilitate de lucru acum (20), datorie editorială
(10). Se recalculează numai după compararea feedback-ului cu implementarea,
testele și artifactele relevante; scorul mare înseamnă că domeniul trebuie
adresat mai devreme.

| Ordine | Owner feedback | Impact /40 | Gap /30 | Lucrabil acum /20 | Datorie doc. /10 | Scor | Ce mai trebuie făcut |
|---:|---|---:|---:|---:|---:|---:|---|
| 1 | [AI evaluation](README_AI_Evaluation.md) | 34 | 26 | 12 | 6 | **78** | evaluatorul typed și snapshot-ul sunt implementate; rămân review/state AI, run-level evidence ledger, publication path, regression delta și cazuri business-negative |
| 2 | [Practice Shop E2E](ATF_Practice_Shop_E2E_Feedback.md) · `SHOP-E2E-INTELLIJ-AUTH-01` | 34 | 25 | 8 | 3 | **70** | ambele clase autentificate cer alegere `REUSE_FIXTURE` / opt-in `REGISTER_PER_RUN`; harness-ul actual închide `SETUP` înainte de callback, deci registration flow-ul lipsește. Înregistrare și login verificate înaintea journey-ului propriu fiecărei clase, fără replay și fără PII |
| 3 | [Practice Shop E2E](ATF_Practice_Shop_E2E_Feedback.md) · `SHOP-E2E-PRESENTATION-01` | 29 | 20 | 12 | 5 | **66** | consumerul nu expune încă prin YAML `UNIFIED_FLOW`, gruparea UI steps și screenshot `FINAL_ONLY`; reporting-ul generic le suportă și este probat |

Starea pe domenii:

- Practice Shop: generic `UNIFIED_FLOW` este implementat și probat în
  `atf-reporting`; configurarea YAML pentru testul canonic și captura finală
  rămân gap-uri ale consumerului și sunt urmărite separat.
- Practice Shop: profilul `ui-only` oprește captura/publicarea API în consumer,
  nu request-urile normale ale aplicației. O clasă E2E separată UI-only nu
  înlocuiește cerința de configurare a testului canonic și poate dubla purchase-ul.
- Practice Shop authenticated: `@AuthenticatedE2e` declară profilul de launch
  independent pentru fiecare clasă; testele modulare confirmă profilul și skip-ul
  sigur fără VM options. Cerința extinde adnotarea pentru ambele clase cu
  lifecycle explicit (`REUSE_FIXTURE` / `REGISTER_PER_RUN`): provisioning-ul
  este opt-in, unic în `SETUP`, fără retry la mutația de înregistrare, apoi login
  verificat și journey-ul clasei. Codul are page object/data parțiale, nu și
  registration flow; run-ul fixture-ului
  live a eșuat la login. Scorul 70 reflectă impactul testelor autentificate,
  gap-ul de implementare și riscul mutației externe fără cleanup demonstrat.
- Practice Shop build: harness-ul păstrează clasele E2E package-private,
  profilele `CONFIGURED`/`UI_ONLY`/`UI_API` sunt aliniate, iar testele țintite
  trec (31/31); problema de compilare anterioară este închisă și nu mai este
  backlog.
- Reporting: tabelele scrollabile, summary-urile API, trace-ul UI/API și
  `UNIFIED_FLOW` sunt închise; lifecycle-ul publică o singură propoziție Gherkin,
  UI evidence agregată și API evidence sibling, cu policy screenshot independentă.
- Practice Shop runtime boundary: excepțiile guest/bootstrap sunt explicite și
  testate; semnalele necunoscute, host/rute diferite și erorile după autentificare
  rămân failure. Contractul stabil este în README-ul consumerului; nu există un
  ToDo de cleanup activ pentru această regulă.
- AI evaluation rămâne deschis; scorurile sunt priorități de lucru, nu scoruri
  de calitate a produsului.

Verificare în afara ordinii de implementare: [ATF Reporting Feedback](ATF_Reporting_Feedback.md)
nu mai are gap de cod. Rămâne un pas IDE al proprietarului proiectului: după
**Reload All Maven Projects**, rerulează configurația IntelliJ cu modelul curent;
eroarea `ReportWorkspaceSweeper` nu a fost reprodusă în Maven și nu justifică o
modificare a librăriei înainte de această verificare.

Feedback-ul ownerului este sursa criteriului de acceptare și a dovezii cerute.
Rândurile de mai sus sunt doar indexul lucrului rămas; nu duplică istoricul sau
contractele deja implementate.

## Contracte stabile, fără gap structural

| Domeniu | Stare | Documentație oficială |
|---|---|---|
| Arhitectura testelor și matricea module × layer | fără ToDo; redeschidere numai la regresie | [ATF Testing Guide](../ATF_Testing_Guide.md), [ATF Code Best Practices](../README_Atf_CodeBestPractices.md) |
| Trace per checkpoint, inspector aggregate și stabilitatea disclosure-urilor | fără ToDo activ; nested network summaries = `34px`, redeschidere numai la regresie | [ATF Reporting Contract](../ATF_Reporting_Contract.md), [ATF Testing Guide](../ATF_Testing_Guide.md) |
| Reporting: tabele scrollabile, summary-uri API, setup-ul până la publisher și grouping pentru `UNIFIED_FLOW` | fără ToDo activ; geometria aggregate nested măsurată la `34px`, redeschidere numai la regresie | [ATF Reporting Contract](../ATF_Reporting_Contract.md), [ATF Reporting README](../../atf-reporting/README.md), [ATF Reporting Tests README](../../atf-reporting-tests/README.md) |
| Inspector UI și politica fără replay | fără ToDo; redeschidere numai la încălcarea contractului | [ATF UI README](../../atf-ui/README.md), [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Practice Shop purchase E2E | purchase-ul complet, profilele IntelliJ, build-ul țintit și runtime boundary sunt documentate/probate; wiring-ul single-flow/UI steps/screenshot policy și dovada cu un cont activ sunt restante | [Practice Shop README](../../atf-test-practice-shop/README.md), [Practice Shop E2E Feedback](ATF_Practice_Shop_E2E_Feedback.md) |

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
