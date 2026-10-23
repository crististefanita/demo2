# ATF Java — registrul cerințelor rămase

Acest registru indexează numai lacune deschise și prioritățile lor. Contractele
stabile și deciziile închise locuiesc în documentația oficială; aici nu păstrăm
istoricul rapoartelor, capturilor sau rulărilor.

## Pe scurt

Cele cinci priorități active sunt coverage-ul API AutoEx, coeziunea pachetelor
Reporting Tests, dovada auditului terminal UI, precedența adnotării UI evidence
între consumeri și integrarea lifecycle-ului Reporting în Shop. Split-ul și
inventarul de producție AutoEx sunt deja în cod și în proba nominală; nu mai sunt
backlog.
Evaluarea AI a fost revizuită, dar rămâne un backlog separat și neprioritizat.

## Prioritatea curentă

Scorul însumează impactul (40), severitatea lacunei (30), posibilitatea de lucru
acum (20) și datoria documentară (10). Domeniile active au pondere `1.00`;
scorurile ordonează backlog-ul și nu reprezintă evaluări de calitate.

| Ordine | Owner / feedback | Impact /40 | Lacună /30 | Lucrabil /20 | Doc. /10 | Pondere | Scor | Rezumatul gap-ului |
|---:|---|---:|---:|---:|---:|---:|---:|---|
| 1 | [Automation Exercise — contracte API complete și cauzalitate UI/API](ATF_Automation_Exercise_Feedback.md) | 38 | 25 | 18 | 8 | 1.00 | **89** | Există request factory și 14 scenarii fixture offline; lipsesc verificarea semantică completă, dovada live opt-in, cleanup sigur și cauzalitatea UI. |
| 2 | [Coeziunea pachetelor din Reporting Tests](ATF_Reporting_Feedback.md#report-test-package-01--coeziunea-pachetelor-din-reporting-tests) | 34 | 26 | 18 | 7 | 1.00 | **85** | Gate-ul whole-ATF găsește nouă clase în `atf.api.reporting.ui`; pachetul depășește pragul opt. |
| 3 | [Audit terminal pentru Screen validation](ATF_Reporting_Feedback.md#ui-audit-01--audit-terminal-pentru-screen-validation) | 34 | 24 | 18 | 7 | 1.00 | **83** | E2E-ul AutoEx nu publică observația de contract terminal; în Shop, observația este adăugată după `drain()` și nu ajunge în evidence-ul deja extras. |
| 4 | [Precedență comună a adnotărilor UI evidence](ATF_Consumer_Test_Architecture_Feedback.md) | 35 | 23 | 18 | 6 | 1.00 | **82** | `AtfUiEvidence` există în ambii consumatori, dar Shop reimpune YAML-ul și ignoră override-ul explicit pentru capturi. |
| 5 | [Reporting — integrare și lifecycle](ATF_Reporting_Feedback.md) | 40 | 11 | 18 | 7 | 1.00 | **76** | Rămâne aplicarea profilelor și agregarea cross-phase în raportul partajat Practice Shop. |

## Revizuit, dar neprioritizat pentru implementare

| Domeniu | Stare curentă |
|---|---|
| [Evaluarea AI](README_AI_Evaluation.md) | Reevaluată față de cod și documentație. Comparatorul typed este implementat; rămân gap-urile evaluatorului extins și workflow-ul agentic. Scorul implementării este **50/100**, iar pregătirea unui pilot **78/100**; acestea nu sunt scoruri de backlog. Dezvoltarea rămâne separată de prioritățile active de mai sus. |

## Contracte promovate în documentația oficială

| Contract | Sursa normativă |
|---|---|
| Boundary-ul `main`/`test`, ownership și selecția testelor consumer | [Arhitectura ATF](../ATF_Architecture.md), [ghidul de testare](../ATF_Testing_Guide.md), README-urile Practice Shop și Automation Exercise |
| Regula de coeziune: maximum opt clase per pachet; inventarele includ modulele cu surse de producție | [Bune practici de cod](../README_Atf_CodeBestPractices.md#coeziunea-pachetelor-pragul-de-opt-clase); includerea AutoEx și split-ul semantic sunt închise, iar numai Reporting Tests rămâne deschis mai sus |
| Profile Reporting per scenariu și izolare față de scenariile-surori | [README ATF Reporting Tests](../../atf-reporting-tests/README.md), [contractul de captură](../ATF_Evidence_Capture_Contract.md) |
| Nivelul/canalul testelor și declarația neutră UI evidence | [Contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md), [README Test Kit](../../atf-test-kit/README.md); forma comună există, dar precedența capturilor în Shop rămâne deschisă mai sus |
| UI/API validation, ownership-ul dovezilor și layout-ul raportului | [Contractul semantic de raportare](../ATF_Reporting_Contract.md), [contractul display/layout](../ATF_Reporting_Display_Contract.md) |
| Notificarea opt-in a căii Extent după publicare, inclusiv sesiunea comună Shop | [Contractul `REPORT-PATH-01`](ATF_Reporting_Feedback.md#report-path-01--închis-cale-absolută-după-publicarea-raportului), implementat în TestKit/Console și compus de consumer |
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
