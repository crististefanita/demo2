# ATF Java — registrul cerințelor rămase

Acest registru indexează numai lacune deschise și prioritățile lor. Contractele
stabile și deciziile închise locuiesc în documentația oficială. Aici nu păstrăm
istoricul rapoartelor, capturilor sau rulărilor.

## Pe scurt

Cele patru priorități active sunt boundary-ul suportului E2E comun,
coverage-ul API AutoEx, integrarea profilelor Reporting în suita Practice Shop
și confirmarea vizuală a auditului terminal UI. Adaptorul Console–Reporting a
fost centralizat și verificat; coeziunea pachetului Reporting Tests a fost
rezolvată. O constatare nouă de guvernanță AutoEx este inclusă în feedback-ul
E2E, nu ascunsă ca failure preexistent.

Split-ul și inventarul de producție AutoEx, precedența adnotării UI evidence în
rulare standalone și baseline-ul precondiției AutoEx sunt deja în cod și în
probe; nu mai sunt backlog separat. Evaluarea AI rămâne un backlog separat și
neprioritizat.

## Prioritatea curentă

Scorul însumează impactul (40), severitatea lacunei (30), posibilitatea de lucru
acum (20) și datoria documentară (10). Domeniile active au pondere `1.00`;
scorurile ordonează backlog-ul și nu reprezintă evaluări de calitate.

| Ordine | Owner / feedback | Impact /40 | Lacună /30 | Lucrabil /20 | Doc. /10 | Pondere | Scor | Rezumatul gap-ului |
|---:|---|---:|---:|---:|---:|---:|---:|---|
| 1 | [Suport E2E comun și coeziune/gate-urile AutoEx](ATF_E2E_Test_Support_Architecture_Feedback.md) | 36 | 27 | 16 | 8 | 1.00 | **87** | Shop harness-ul browser/report rămâne în `src/main`; AutoEx lifecycle are două căi nesigure și gate-ul detectează inventory production neactualizat plus un pachet component cu 9 clase. Se urmăresc doar primitive neutre în Test Kit, fără modul UI nou. |
| 2 | [Automation Exercise — contracte API complete și cauzalitate UI/API](ATF_Automation_Exercise_Feedback.md) | 38 | 22 | 18 | 6 | 1.00 | **84** | Coverage-ul fixture offline este complet; rămân schimburile live prin transport ATF, statusurile HTTP vs `responseCode`, cleanup-ul real și artifactul UI/API cauzal. |
| 3 | [Reporting — integrare și lifecycle](ATF_Reporting_Feedback.md) | 40 | 7 | 18 | 7 | 1.00 | **72** | Profilul fiecărui scenariu este izolat; rămâne agregarea `SETUP` + `TEST BODY` fără pierderea provenienței/lifecycle-ului. |
| 4 | [Audit terminal pentru Screen validation](ATF_Reporting_Feedback.md#ui-audit-01--audit-terminal-pentru-screen-validation) | 24 | 15 | 16 | 5 | 1.00 | **60** | Implementarea și probele sunt în cod; rămâne confirmarea vizuală a artifactului în TAB MCP. |

## Revizuit, dar neprioritizat pentru implementare

| Domeniu | Stare curentă |
|---|---|
| [Evaluarea AI](README_AI_Evaluation.md) | Reevaluată față de cod și documentație. Comparatorul typed este implementat; rămân gap-urile evaluatorului extins și workflow-ul agentic. Scorul implementării este **50/100**, iar pregătirea unui pilot **78/100**; acestea nu sunt scoruri de backlog. Dezvoltarea rămâne separată de prioritățile active de mai sus. |

## Contracte promovate în documentația oficială

| Contract | Sursa normativă |
|---|---|
| Adaptor Console–Reporting unic, cu Reporting independent de Core/Console | [Feedback Consumer — decizie verificabilă](ATF_Consumer_Test_Architecture_Feedback.md#consumer-report-bridge-01--adaptor-comun-console-reporting), [README ATF Console](../../atf-console/README.md) |
| Boundary-ul `main`/`test`, ownership și selecția testelor consumer | [Arhitectura ATF](../ATF_Architecture.md), [ghidul de testare](../ATF_Testing_Guide.md), README-urile Practice Shop și Automation Exercise |
| Regula de coeziune: maximum opt clase per pachet; inventarele includ modulele cu surse de producție | [Bune practici de cod](../README_Atf_CodeBestPractices.md#coeziunea-pachetelor-pragul-de-opt-clase); pachetul Reporting UI este sub prag, iar noul hotspot și inventory-ul AutoEx sunt deschise în feedback-ul E2E |
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
