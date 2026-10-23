# ATF Java — registrul cerințelor rămase

Acest registru indexează numai lacune deschise și prioritățile lor. Contractele
stabile și deciziile închise locuiesc în documentația oficială. Aici nu păstrăm
istoricul rapoartelor, capturilor sau rulărilor.

## Pe scurt

Registrul separă backlog-ul activ de contractele implementate și de deciziile
promovate în documentația oficială.

- **Deschise:** boundary-ul suportului E2E, atomicitatea inițializării Reporting,
  agregarea fazelor Shop și confirmarea vizuală MCP pentru auditul terminal UI.
- **Închise:** corelarea cauzală `UI_API` AutoEx, precondiția API, inventory-ul și
  coeziunea pachetelor AutoEx, activarea și rollback-ul pentru handle-urile
  returnate; adaptorul Console–Reporting și package cohesion Reporting.
- **Neprioritizat:** evaluarea AI rămâne un backlog separat.

## Prioritatea curentă

Scorul însumează impactul (40), severitatea lacunei (30), posibilitatea de lucru
acum (20) și datoria documentară (10). Domeniile active au pondere `1.00`;
scorurile ordonează backlog-ul și nu reprezintă evaluări de calitate.

| Ordine | Owner / feedback | Impact /40 | Lacună /30 | Lucrabil /20 | Doc. /10 | Pondere | Scor | Rezumatul gap-ului |
|---:|---|---:|---:|---:|---:|---:|---:|---|
| 1 | [Suport E2E comun și boundary-uri consumer](ATF_E2E_Test_Support_Architecture_Feedback.md) | 36 | 21 | 16 | 8 | 1.00 | **81** | `ShopE2eContext` păstrează harness browser/report în `src/main` și poate pierde resurse la achiziție parțială; `AutoExPurchaseSession` expune concepte browser/report din `src/main`. Inventory-ul, coeziunea, activarea și rollback-ul AutoEx pentru handle-urile returnate sunt probate. |
| 2 | [Reporting — atomicitatea creării nodurilor](ATF_Reporting_Feedback.md#report-start-atomicity-01--crearea-atomică-a-nodurilor-de-raport) | 34 | 21 | 15 | 7 | 1.00 | **77** | `startScenario`/`startPhase` pot lăsa un nod parțial dacă o etapă ulterioară aruncă înaintea returnării handle-ului. |
| 3 | [Reporting — agregare între fazele Shop](ATF_Reporting_Feedback.md#shop-capture-01--profil-per-scenariu-și-flow-unificat-peste-faze) | 40 | 7 | 18 | 7 | 1.00 | **72** | Rămâne agregarea `SETUP` + `TEST BODY` fără pierderea provenienței ori a lifecycle-ului. |
| 4 | [Audit terminal pentru Screen validation](ATF_Reporting_Feedback.md#ui-audit-01--audit-terminal-pentru-screen-validation) | 24 | 15 | 16 | 5 | 1.00 | **60** | Implementarea și probele sunt în cod; rămâne confirmarea vizuală a artifactului în TAB MCP. |

## Revizuit, dar neprioritizat pentru implementare

| Domeniu | Stare curentă |
|---|---|
| [Evaluarea AI](README_AI_Evaluation.md) | Reevaluată față de cod și documentație. Comparatorul typed este implementat; rămân gap-urile evaluatorului extins și workflow-ul agentic. Scorul implementării este **50/100**, iar pregătirea unui pilot **78/100**; acestea nu sunt scoruri de backlog. Dezvoltarea rămâne separată de prioritățile active de mai sus. |

## Contracte promovate în documentația oficială

| Contract | Sursa normativă |
|---|---|
| Adaptor Console–Reporting unic, cu Reporting independent de Core/Console | [Feedback Consumer — decizie verificabilă](ATF_Consumer_Test_Architecture_Feedback.md#consumer-report-bridge-01--rezolvat), [README ATF Console](../../atf-console/README.md) |
| Boundary-ul `main`/`test`, ownership și selecția testelor consumer | [Arhitectura ATF](../ATF_Architecture.md), [ghidul de testare](../ATF_Testing_Guide.md), README-urile Practice Shop și Automation Exercise |
| Regula de coeziune: maximum opt clase per pachet; inventarele includ modulele cu surse de producție | [Bune practici de cod](../README_Atf_CodeBestPractices.md#coeziunea-pachetelor-pragul-de-opt-clase); package split și inventory-ul AutoEx sunt reparate; Shop harness rămâne deschis în feedback-ul E2E |
| Profile Reporting per scenariu și izolare față de scenariile-surori | [README ATF Reporting Tests](../../atf-reporting-tests/README.md), [contractul de captură](../ATF_Evidence_Capture_Contract.md) |
| Nivelul/canalul testelor și declarația neutră UI evidence | [Contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md), [README Test Kit](../../atf-test-kit/README.md); forma comună și izolarea profilului per scenariu sunt implementate; agregarea cross-phase rămâne în `SHOP-CAPTURE-01` |
| Primitive neutre de lifecycle pentru testele E2E, separarea clasificării de activare și cleanup failure-safe | [Feedback suport E2E comun](ATF_E2E_Test_Support_Architecture_Feedback.md); cerințe deschise, fără API generic declarat implementat |
| UI/API validation, ownership-ul dovezilor și layout-ul raportului | [Contractul semantic de raportare](../ATF_Reporting_Contract.md), [contractul display/layout](../ATF_Reporting_Display_Contract.md) |
| Notificarea opt-in a căii Extent după publicare, inclusiv sesiunea comună Shop | [README Test Kit](../../atf-test-kit/README.md#politica-pentru-calea-raportului-publicat), [ATF Console](../../atf-console/README.md) și [ATF Reporting](../../atf-reporting/README.md); contract implementat, fără backlog separat |
| Screenshot policy și scenariul final-only Automation Exercise | [README Automation Exercise](../../atf-test-auto-ex/README.md), [README ATF Reporting](../../atf-reporting/README.md) |

`GOV-API-01`, profilele generice per scenariu și integrarea AutoEx final-only
nu mai sunt TODO-uri. Ele se redeschid numai dacă o probă nominală/regresie
arată că invariantul documentat nu mai este respectat.

Scorurile ordonează intervențiile rămase; ele nu înlocuiesc probele și nu
înseamnă că un gap de scor mai mic poate fi ignorat când afectează corectitudinea
evidence-ului publicat.

## Regula de reconciliere

Înainte de a schimba starea unui ID, verifică sursa, testul ownerului și
documentația normativă. Închide un gap numai după ce criteriul său trece la
boundary-ul potrivit și informația stabilă este promovată în README-ul ownerului
sau în contractul oficial. O captură veche ori o rulare fără legătură directă cu
criteriul nu este dovadă de închidere.

Pentru builduri, teste, staging și verificarea artifactelor urmează
[Siguranța compilării și rulării ATF](../ATF_Build_And_Execution_Safety.md).
