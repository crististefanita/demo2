# ATF Java — registrul cerințelor rămase

Acest registru indexează numai lacune deschise și prioritățile lor. Contractele
stabile și deciziile închise locuiesc în documentația oficială. Aici nu păstrăm
istoricul rapoartelor, capturilor sau rulărilor.

## Pe scurt

Prioritățile active includ capturarea traficului UI/AutoEx și profilele E2E.
Auditul terminal UI și flow-ul Shop unificat peste
faze sunt închise după corecții și verificare în TAB MCP prin ATF Debug.
Aliasurile de redactare sunt
implementate și documentate în README-ul `atf-ui`; nu mai au un TODO activ.

Cleanup-ul comun, mutarea harness-ului Shop în `src/test`, activarea fail-closed
Shop, baseline-ul API AutoEx, inventory-ul/coezunea și lifecycle-ul AutoEx —
inclusiv sincronizarea checkout-ului și capturarea failure-evidence înainte de
închiderea browserului — sunt închise și documentate la ownerii lor.
Selecția network și completitudinea jurnalului AutoEx au însă gap-uri
reproductibile separate, urmărite în feedback-ul Network.

Evaluarea AI rămâne în afara priorităților curente. Pentru API Shop:

- numele, nivelurile și separarea unit/component sunt reconciliate cu schimbările live;
- gate-ul unit/component trece cu 95 de teste;
- dovada HTTP live autentificată pentru sort/filter și mutațiile coșului a
  trecut după înlocuirea fixture-ului sintetic; detaliile și limita auditului
  vizual sunt în [feedback-ul Shop](ATF_Practice_Shop_API_Test_Design_Feedback.md).

## Prioritatea curentă

Scorul însumează impactul (40), severitatea lacunei (30), posibilitatea de lucru
acum (20) și datoria documentară (10). Domeniile active au pondere `1.00`;
scorurile ordonează backlog-ul și nu reprezintă evaluări de calitate.

| Ordine | Owner / feedback | Impact /40 | Lacună /30 | Lucrabil /20 | Doc. /10 | Pondere | Scor | Rezumatul gap-ului |
|---:|---|---:|---:|---:|---:|---:|---:|---|
| 1 | [Network UI / AutoEx — selecție și captură completă](ATF_UI_Network_Inspection_Feedback.md) | 37 | 27 | 20 | 8 | 1.00 | **92** | XHR-ul coșului este exclus de filtrul `/api/`; asset-urile umplu jurnalul comun înainte de checkout; probele generice aparțin `atf-ui` și `atf-reporting-tests`. |
| 2 | [Consumer — profile E2E](ATF_Consumer_Business_Test_Focus_Feedback.md#consumer-e2e-profiles-01--variantele-de-evidence-și-acoperirea-business) | 24 | 10 | 17 | 6 | 1.00 | **57** | AutoEx separă gate-ul business de matricea manuală; reducerea Shop și acceptanța profilelor cer încă smoke local succes/failure și păstrarea exemplelor configurabile. |

## Revizuit, dar neprioritizat pentru implementare

| Domeniu | Stare curentă |
|---|---|
| [Geometrie browser / iframe](ATF_UI_Browser_Geometry_Feedback.md) | Zoom-ul documentului copil nu se compune; pentru pointer la zoom CSS non-nativ rămâne o limită probată. Alegerea AutoEx 100% nu închide acest contract generic. |
| [Reporting — body-uri externe non-JSON](ATF_Reporting_Feedback.md#api-body-media-01--etichete-corecte-pentru-body-uri-externe-non-json) | Request-ul real form-urlencoded este etichetat JSON; proiecția trebuie să separe formatul de direcția evidence-ului. |
| [Evaluarea AI](README_AI_Evaluation.md) | În afara acestei reconcilieri; conținutul și scorurile nu au fost reevaluate. |
| [Clasificarea testelor — guard Test Kit](ATF_Consumer_Business_Test_Focus_Feedback.md#test-layer-declarations-01--metadate-preexistente-de-clasificare) | Două verificări de clasificare eșuează pe metadate preexistente; guvernanța principală verde nu le înlocuiește. |
| [Automation Exercise — business](ATF_Automation_Exercise_Feedback.md) | Recalcularea/eliminarea coșului, continuitatea după login și cleanup-ul sub reclamă reală sunt probate; închiderea controlului Google vignette rămâne deschisă, iar suita business nu este încă verde. |

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
