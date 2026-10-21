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
| 1 | [AI evaluation](README_AI_Evaluation.md) | 34 | 26 | 12 | 6 | **78** | grounding ambiguity/run ledger, review-state, reporting parity, regression delta și publication path; grounding per field, snapshot-ul immutable și package cohesion sunt deja demonstrate |
| 2 | [ATF Reporting](ATF_Reporting_Feedback.md) · `REP-UX-FLOW-01` | 31 | 23 | 14 | 5 | **73** | `UNIFIED_FLOW` este metadata; rendererul nu transformă checkpoint-urile într-un singur flow Gherkin |
| 3 | [Practice Shop E2E](ATF_Practice_Shop_E2E_Feedback.md) · `SHOP-E2E-PRESENTATION-01` | 29 | 20 | 12 | 5 | **66** | leagă YAML-ul de `ui-only` / `ui-api`, flow-ul unificat și screenshot-ul final-only; validează comportamentul raportat |
| 4 | [Practice Shop E2E](ATF_Practice_Shop_E2E_Feedback.md) · `SHOP-E2E-BOUNDARY-01` | 26 | 18 | 13 | 5 | **62** | elimină excepțiile guest/bootstrap specifice rutei și mesajului; capturează doar exchange-urile contractuale selectate |

Starea pe domenii:

- Practice Shop: raportul MCP proaspăt al purchase-ului pe codul curent este
  `PASS`, cu zero warnings și zero technical diagnostics; System arată `headed`,
  conform YAML-ului. Trace-ul păstrează checkpoint-urile Gherkin și cele 7
  exchange-uri selectate.
- Practice Shop: profilul `ui-only` există și oprește captura/publicarea API
  în consumer. Nu blochează request-urile normale ale aplicației testate. Nu
  există încă o probă de raport E2E pentru această ramură sau setarea consumerului
  pentru flow Gherkin unificat + screenshot `final-only`.
- Reporting: tabelele scrollabile, summary-urile API și trace-ul cu evidence
  UI/API ca frați sunt închise. `UNIFIED_FLOW` rămâne metadata și nu schimbă
  arborele Extent; contractul de colapsare a pașilor este activ.
- Practice Shop: collectorul și testele păstrează excepții guest/bootstrap
  specifice. Ele nu apar în raportul curent, dar cleanup-ul cerut rămâne deschis.
- AI evaluation rămâne deschis; scorurile sunt priorități de lucru, nu scoruri
  de calitate a produsului.

Feedback-ul ownerului este sursa criteriului de acceptare și a dovezii cerute.
Rândurile de mai sus sunt doar indexul lucrului rămas; nu duplică istoricul sau
contractele deja implementate.

## Contracte stabile, fără gap structural

| Domeniu | Stare | Documentație oficială |
|---|---|---|
| Arhitectura testelor și matricea module × layer | fără ToDo; redeschidere numai la regresie | [ATF Testing Guide](../ATF_Testing_Guide.md), [ATF Code Best Practices](../README_Atf_CodeBestPractices.md) |
| Trace per checkpoint, inspector aggregate și stabilitatea disclosure-urilor | fără ToDo activ; redeschidere numai la regresie | [ATF Reporting Contract](../ATF_Reporting_Contract.md), [ATF Testing Guide](../ATF_Testing_Guide.md) |
| Reporting: tabele scrollabile, summary-uri API și setup-ul exact până la publisher | fără ToDo activ; redeschidere numai la regresie | [ATF Reporting Contract](../ATF_Reporting_Contract.md), [ATF Reporting README](../../atf-reporting/README.md), [ATF Reporting Tests README](../../atf-reporting-tests/README.md) |
| Inspector UI și politica fără replay | fără ToDo; redeschidere numai la încălcarea contractului | [ATF UI README](../../atf-ui/README.md), [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Practice Shop purchase trace | contractul Gherkin/UI/API și alinierea raportului proaspăt cu YAML-ul sunt închise; prezentarea configurabilă și cleanup-ul bootstrap rămân active | [Practice Shop README](../../atf-test-practice-shop/README.md), [Practice Shop E2E Feedback](ATF_Practice_Shop_E2E_Feedback.md) |

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
