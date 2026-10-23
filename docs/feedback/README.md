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
Shop, baseline-ul API AutoEx, inventory-ul/coezunea și teardown-ul AutoEx sunt
închise și documentate la ownerii lor. Adaptorul Console–Reporting rămâne deschis
până la rezolvarea conflictului de guvernanță.

Evaluarea AI rămâne separată și în afara priorităților curente. În feedback-ul
API Shop, numele, nivelurile și separarea unit/component au fost reconciliate
cu schimbările live, iar gate-ul unit/component trece cu 95 de teste. Rămâne
neprioritizată dovada HTTP live pentru sort/filter și mutațiile coșului: ultima
execuție opt-in s-a oprit la login HTTP 401 înainte de orice mutație.

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
| [`SHOP-API-LIVE-SEMANTICS-01` — gap-ul live API Practice Shop](ATF_Practice_Shop_API_Test_Design_Feedback.md) | Codul probei verifică filtrarea/sortarea răspunsului și update/remove cu read-back pe coș izolat; unit/component sunt verzi. Proba live s-a oprit la autentificare HTTP 401, deci semantica remote rămâne neconfirmată și ID-ul deschis. |

## Contracte promovate în documentația oficială

| Contract | Sursa normativă |
|---|---|
| Boundary-ul `main`/`test` și regula de owner pentru teste business, adaptoare consumer, Reporting generic și Test Kit | [Arhitectura ATF](../ATF_Architecture.md), [ghidul de testare](../ATF_Testing_Guide.md#owner-ul-testului-business-adaptor-sau-infrastructură), README-urile Practice Shop, Automation Exercise, Reporting Tests și Test Kit |
| Regula de coeziune: maximum opt clase per pachet; inventarele includ modulele cu surse de producție | [Bune practici de cod](../README_Atf_CodeBestPractices.md#coeziunea-pachetelor-pragul-de-opt-clase); package split/inventory AutoEx, boundary-ul Shop harness și activarea Shop sunt documentate la owner; `AutoExPurchaseSession` rămâne runtime specific consumerului |
| Profile Reporting per scenariu și izolare față de scenariile-surori | [README ATF Reporting Tests](../../atf-reporting-tests/README.md), [contractul de captură](../ATF_Evidence_Capture_Contract.md) |
| Nivelul/canalul testelor și declarația neutră UI evidence | [Contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md), [README Test Kit](../../atf-test-kit/README.md); forma comună și izolarea profilului per scenariu sunt implementate; agregarea cross-phase rămâne în `SHOP-CAPTURE-01` |
| Cleanup neutru failure-aware pentru testele E2E | [README Test Kit](../../atf-test-kit/README.md); `FailureAwareCleanup` este implementat și probat de kit și consumatori. Activarea fail-closed Shop și boundary-ul intern AutoEx sunt documentate la owner; nu există un harness UI generic în Test Kit |
| UI/API validation, ownership-ul dovezilor și layout-ul raportului | [Contractul semantic de raportare](../ATF_Reporting_Contract.md), [contractul display/layout](../ATF_Reporting_Display_Contract.md) |
| Metadata `System/Environment`, inclusiv fallback-ul neutru pentru Client și descriptorii declarați de consumatori | [README ATF Reporting](../../atf-reporting/README.md); `REPORT-CLIENT-METADATA-01` este închis și nu mai ocupă backlog |
| Notificarea opt-in a căii Extent după publicare, inclusiv sesiunea comună Shop | [README Test Kit](../../atf-test-kit/README.md#politica-pentru-calea-raportului-publicat), [ATF Console](../../atf-console/README.md) și [ATF Reporting](../../atf-reporting/README.md); contract implementat, fără backlog separat |
| Screenshot policy și scenariul final-only Automation Exercise | [README Automation Exercise](../../atf-test-auto-ex/README.md), [README ATF Reporting](../../atf-reporting/README.md) |

## Readiness whole-ATF găsit la reconciliere

- Poarta whole-ATF trebuie să treacă regulile de readability, nu numai suitele
  locale.
- Constatarea live identifică invocări ATF complet calificate în
  `ExtentReportSessionLifecycleTest`, `AtfConsoleBridge` și
  `UiApiVerdictProjectionProbe`, plus un import duplicat în `AutoExTestAccount`.
  Acestea cer curățarea sursei, nu relaxarea testelor.
- `ArchitectureModuleBoundaryTest` respinge separat plasarea adaptorului
  Console; problema este urmărită și scorată mai sus.
- Testele de guvernanță nu au fost modificate în această reconciliere.

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

Un feedback fără TODO funcțional se elimină după promovarea contractului și
reconcilierea exactă a scope-ului de teste. Un control `testChanges` se păstrează
numai cât timp diff-ul față de baza folosită de gate conține schimbările pe care
le acoperă; expirarea sau existența unui commit nou nu decid singure închiderea.
Regula tehnică detaliată este în [baseline-ul schimbărilor de teste](../ATF_Test_Changes_Baseline.md).

Pentru builduri, teste, staging și verificarea artifactelor urmează
[Siguranța compilării și rulării ATF](../ATF_Build_And_Execution_Safety.md).
