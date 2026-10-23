# ATF Java — registrul cerințelor rămase

Acest registru indexează numai lacune deschise și prioritățile lor. Contractele
stabile și deciziile închise locuiesc în documentația oficială. Aici nu păstrăm
istoricul rapoartelor, capturilor sau rulărilor.

## Pe scurt

Patru priorități active rămân: atomicitatea nodurilor Reporting, boundary-ul
adaptorului Console–Reporting, agregarea evidence-ului Shop peste faze și
confirmarea vizuală a auditului terminal UI. Aliasurile de redactare sunt
implementate și documentate în README-ul `atf-ui`; nu mai au un TODO activ.

Cleanup-ul comun, mutarea harness-ului Shop în `src/test`, activarea fail-closed
Shop, baseline-ul API AutoEx, inventory-ul/coezunea și lifecycle-ul AutoEx —
inclusiv sincronizarea checkout-ului și capturarea failure-evidence înainte de
închiderea browserului — sunt închise și documentate la ownerii lor.
Adaptorul Console–Reporting rămâne deschis până la rezolvarea conflictului de
guvernanță.

Evaluarea AI rămâne în afara priorităților curente. Pentru API Shop:

- numele, nivelurile și separarea unit/component sunt reconciliate cu schimbările live;
- gate-ul unit/component trece cu 95 de teste;
- dovada HTTP live pentru sort/filter și mutațiile coșului rămâne neprioritizată:
  ultima execuție opt-in s-a oprit la login HTTP 401, înainte de orice mutație.

## Prioritatea curentă

Scorul însumează impactul (40), severitatea lacunei (30), posibilitatea de lucru
acum (20) și datoria documentară (10). Domeniile active au pondere `1.00`;
scorurile ordonează backlog-ul și nu reprezintă evaluări de calitate.

| Ordine | Owner / feedback | Impact /40 | Lacună /30 | Lucrabil /20 | Doc. /10 | Pondere | Scor | Rezumatul gap-ului |
|---:|---|---:|---:|---:|---:|---:|---:|---|
| 1 | [Reporting — atomicitatea creării nodurilor](ATF_Reporting_Feedback.md#report-start-atomicity-01--crearea-atomică-a-nodurilor-de-raport) | 34 | 21 | 15 | 7 | 1.00 | **77** | Dacă mutația Extent reușește, dar lifecycle-ul semantic eșuează înainte de returnarea handle-ului, poate rămâne un nod orfan; sink-ul Console este deja best-effort și testat. |
| 2 | [Consumer — boundary-ul adaptorului Console–Reporting](ATF_Consumer_Test_Architecture_Feedback.md#consumer-report-bridge-01--implementat-boundary-neînchis) | 36 | 24 | 10 | 7 | 1.00 | **77** | Adaptorul mapează corect local, dar sursa production din Console referă `atf.api.reporting`, interzis de guvernanța whole-ATF; trebuie decis ownerul permis, fără modificarea regulii. |
| 3 | [Reporting — agregarea fazelor Shop](ATF_Reporting_Feedback.md#shop-capture-01--profil-per-scenariu-și-flow-unificat-peste-faze) | 40 | 7 | 18 | 7 | 1.00 | **72** | Izolarea profilurilor este probată generic; agregarea Shop `SETUP` + `TEST BODY` cu proveniență și verdict păstrate rămâne deschisă. |
| 4 | [Audit terminal pentru Screen validation](ATF_Reporting_Feedback.md#ui-audit-01--audit-terminal-pentru-screen-validation) | 24 | 15 | 16 | 5 | 1.00 | **60** | Implementarea și probele sunt în cod; rămâne confirmarea vizuală a artifactului randat în TAB MCP. |

## Revizuit, dar neprioritizat pentru implementare

| Domeniu | Stare curentă |
|---|---|
| [Evaluarea AI](README_AI_Evaluation.md) | În afara acestei reconcilieri; conținutul și scorurile nu au fost reevaluate. |
| [`SHOP-API-LIVE-SEMANTICS-01` — gap-ul live API Practice Shop](ATF_Practice_Shop_API_Test_Design_Feedback.md) | Scor **59/100** (30 impact + 21 lacună + 2 lucrabil + 6 documentare). Compilarea surselor trece; testele controlate nu dovedesc semantica serverului. Ultima dovadă live disponibilă s-a oprit la login HTTP 401, înainte de mutațiile coșului. Reia după obținerea unui fixture valid prin procesul autorizat. |

## Abateri active de readability și boundary

- `AtfSourceReadabilityGovernanceTest` raportează invocări ATF complet calificate
  în `ExtentReportSessionLifecycleTest` (liniile 34, 110, 139), `AtfConsoleBridge`
  din suportul `atf-tests` (linia 57) și `UiApiVerdictProjectionProbe` (linia 110).
- Aceeași probă găsește un import duplicat `AutoExConfiguration` în
  `AutoExTestAccount`. Acestea sunt curățări de cod necesare pentru gate-ul
  existent; nu sunt motiv pentru modificarea ori relaxarea testului.
- `ArchitectureModuleBoundaryTest` respinge plasarea adaptorului
  Console–Reporting; acesta este același gap urmărit și prioritizat în
  [feedback-ul Consumer](ATF_Consumer_Test_Architecture_Feedback.md).
- Politica de schimbare a acestor teste este deținută de
  [README-ul ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).

`GOV-API-01`, profilele generice per scenariu și integrarea AutoEx final-only
nu mai sunt TODO-uri. Ele se redeschid numai dacă o probă nominală/regresie
arată că invariantul documentat nu mai este respectat.

Scorurile ordonează intervențiile rămase; ele nu înlocuiesc probele și nu
înseamnă că un gap de scor mai mic poate fi ignorat când afectează corectitudinea
evidence-ului publicat.

Regulile de promovare/ștergere a feedback-ului sunt deținute de
[bunele practici documentare](../README_Atf_DocumentationBestPractices.md#păstrarea-și-ștergerea-feedback-ului);
controalele `testChanges` de [baseline-ul lor](../ATF_Test_Changes_Baseline.md)
și [README-ul ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).
Pentru execuții și verificarea artifactelor se aplică
[siguranța compilării și rulării ATF](../ATF_Build_And_Execution_Safety.md).
