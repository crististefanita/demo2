# ATF Java — registrul ToDo curent

## Pe scurt

Acest registru păstrează numai gap-uri încă deschise, ownerul și următorul pas.
Regulile stabile se află în documentația oficială; închiderile, rapoartele de
run și istoricul screenshot-urilor nu sunt backlog.

## ToDo-uri active, în ordinea priorității

- Scor de bază: impact (40) + gap deschis (30) + posibilitate de lucru acum
  (20) + datorie editorială (10).
- Scor final: scor de bază × pondere, rotunjit la cel mai apropiat întreg.
- Pondere: `1.00` pentru fiecare domeniu activ; domeniile închise nu intră în
  ordonarea lucrului.
- Ponderea schimbă numai ordinea de lucru, nu readiness-ul sau maturitatea
  implementării AI documentate în README-ul dedicat.

| Ordine | Owner feedback | Impact /40 | Gap /30 | Lucrabil acum /20 | Datorie doc. /10 | Pondere | Scor | Ce mai trebuie făcut |
|---:|---|---:|---:|---:|---:|---:|---:|---|
| 1 | [API report semantic parity](ATF_Reporting_Feedback.md) | 36 | 29 | 18 | 8 | 1.00 | **91** | elimină request-ul sintetic `ASSERT`; separă verdictul JUnit de auditul exchange-urilor și de `Contract validation`; definește modelul de reporting și teste structurale în `atf-reporting-tests`, plus test consumer |
| 2 | [Practice Shop screenshot precedence](ATF_Practice_Shop_E2E_Feedback.md) | 35 | 27 | 19 | 8 | 1.00 | **89** | fă YAML-ul `reporting.screenshotPolicy` autoritar în `ShopE2eSuite`; nu înlocui `each-checkpoint` cu fallback-ul implicit `final-and-failure`; probează capturile per tabel și politicile de dezactivare |
| 3 | [AI evaluation](README_AI_Evaluation.md) | 34 | 26 | 12 | 6 | 1.00 | **78** | clasificarea `AMBIGUOUS` (enum-ul există, evaluatorul nu o produce), run-level evidence ledger, review/state, reporting parity, regression delta și publication path; grounding per field, snapshot-ul immutable și package cohesion sunt deja demonstrate |

- Scorurile din acest registru ordonează lucrul rămas; nu sunt verdicte de calitate.

Feedback-ul ownerului este sursa criteriului de acceptare și a dovezii cerute.
Rândurile de mai sus sunt doar indexul lucrului rămas; nu duplică istoricul sau
contractele deja implementate.

## Contracte promovate și stări păstrate

| Domeniu | Stare | Documentație oficială |
|---|---|---|
| Arhitectura testelor și matricea module × layer | fără ToDo; redeschidere numai la regresie | [ATF Testing Guide](../ATF_Testing_Guide.md), [ATF Code Best Practices](../README_Atf_CodeBestPractices.md) |
| Reporting: evidence, trace, aggregate, viewport și coverage matrix Network Inspector | contractele rendererului rămân acoperite; semantica API-call audit vs. rezultat JUnit/`Contract validation` este deschisă, scor 91 | [ATF Reporting Contract](../ATF_Reporting_Contract.md), [ATF Reporting README](../../atf-reporting/README.md), [ATF Reporting Feedback](ATF_Reporting_Feedback.md) |
| Inspector UI și politica fără replay | fără ToDo; redeschidere numai la încălcarea contractului | [ATF UI README](../../atf-ui/README.md), [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Practice Shop action-details selector | selectabil per clasă; maparea păstrează acțiunile capturate și verdictul | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Practice Shop purchase trace și journey summary terminal | trace-ul generic, ordinea și captura terminală sunt închise | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Practice Shop E2E, API component și API-call audit suites | suitele sunt implementate; precedence screenshot pentru `ShopE2eSuite` este deschisă, scor 89; formatul API-call audit urmărit separat la Reporting | [Practice Shop README](../../atf-test-practice-shop/README.md), [feedback și dovezi](ATF_Practice_Shop_E2E_Feedback.md) |

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

| Contract | Owner normativ |
|---|---|
| module și dependențe | [ATF Architecture](../ATF_Architecture.md) |
| test layers, matrice și alegerea proof-ului | [ATF Testing Guide](../ATF_Testing_Guide.md) |
| API/UI report, trace și aggregate | [ATF Reporting Contract](../ATF_Reporting_Contract.md) |
| Practice Shop flows și consumer evidence | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| reguli de cod și test design | [ATF Code Best Practices](../README_Atf_CodeBestPractices.md) |
| reguli editoriale | [ATF Documentation Best Practices](../README_Atf_DocumentationBestPractices.md) |
