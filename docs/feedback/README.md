# ATF Java — registrul cerințelor rămase

Acest registru indexează numai lacune deschise și prioritățile lor. Contractele
stabile și deciziile închise locuiesc în documentația oficială. Aici nu păstrăm
istoricul rapoartelor, capturilor sau rulărilor.

## Pe scurt

Șase priorități active rămân: protejarea valorilor sensibile din evidence-ul UI,
atomicitatea nodurilor Reporting, boundary-ul adaptorului Console–Reporting,
agregarea evidence-ului Shop peste faze, corectitudinea clientului publicat
pentru fiecare canal și confirmarea vizuală a auditului terminal UI. Cleanup-ul
comun, mutarea harness-ului Shop în `src/test`, activarea fail-closed Shop,
baseline-ul API AutoEx, inventory-ul/coezunea și teardown-ul AutoEx sunt
închise și documentate la ownerii lor. Adaptorul Console–Reporting rămâne deschis
până la rezolvarea conflictului de guvernanță. Evaluarea AI și designul testelor
API Shop rămân backlog-uri separate, fără prioritate de implementare.

## Prioritatea curentă

Scorul însumează impactul (40), severitatea lacunei (30), posibilitatea de lucru
acum (20) și datoria documentară (10). Domeniile active au pondere `1.00`;
scorurile ordonează backlog-ul și nu reprezintă evaluări de calitate.

| Ordine | Owner / feedback | Impact /40 | Lacună /30 | Lucrabil /20 | Doc. /10 | Pondere | Scor | Rezumatul gap-ului |
|---:|---|---:|---:|---:|---:|---:|---:|---|
| 1 | [Reporting — redactarea valorilor din acțiunile UI](ATF_Reporting_Feedback.md#report-ui-action-redaction-01--redactarea-valorilor-sensibile-din-acțiunile-ui) | 35 | 24 | 16 | 7 | 1.00 | **82** | PAN/CVC pot ajunge în `expected` și `observed`; redacția trebuie aplicată înainte de crearea evidence-ului. |
| 2 | [Reporting — atomicitatea creării nodurilor](ATF_Reporting_Feedback.md#report-start-atomicity-01--crearea-atomică-a-nodurilor-de-raport) | 34 | 21 | 15 | 7 | 1.00 | **77** | Dacă mutația Extent reușește, dar lifecycle-ul semantic eșuează înainte de returnarea handle-ului, poate rămâne un nod orfan; sink-ul Console este deja best-effort și testat. |
| 3 | [Consumer — boundary-ul adaptorului Console–Reporting](ATF_Consumer_Test_Architecture_Feedback.md#consumer-report-bridge-01--implementat-boundary-neînchis) | 36 | 24 | 10 | 7 | 1.00 | **77** | Adaptorul mapează corect local, dar sursa production din Console referă `atf.api.reporting`, interzis de guvernanța whole-ATF; trebuie decis ownerul permis, fără modificarea regulii. |
| 4 | [Reporting — agregarea fazelor Shop](ATF_Reporting_Feedback.md#shop-capture-01--profil-per-scenariu-și-flow-unificat-peste-faze) | 40 | 7 | 18 | 7 | 1.00 | **72** | Profilul efectiv este izolat per scenariu; rămâne agregarea `SETUP` + `TEST BODY` fără pierderea provenienței ori a lifecycle-ului. |
| 5 | [Reporting — metadata Client dependentă de canal](ATF_Reporting_Feedback.md#report-client-metadata-01--clientul-afișat-trebuie-să-corespundă-canalului) | 26 | 17 | 15 | 5 | 1.00 | **63** | Fallback-ul generic declară `ATF ApiTransport` inclusiv pentru un raport UI-only; metadata trebuie să reflecte mecanismul efectiv. |
| 6 | [Audit terminal pentru Screen validation](ATF_Reporting_Feedback.md#ui-audit-01--audit-terminal-pentru-screen-validation) | 24 | 15 | 16 | 5 | 1.00 | **60** | Implementarea și probele sunt în cod; rămâne confirmarea vizuală a artifactului în TAB MCP. |

## Revizuit, dar neprioritizat pentru implementare

| Domeniu | Stare curentă |
|---|---|
| [Evaluarea AI](README_AI_Evaluation.md) | Reevaluată față de cod și documentație. Comparatorul typed este implementat; rămân gap-urile evaluatorului extins și workflow-ul agentic. Scorul implementării este **50/100**, iar pregătirea unui pilot **78/100**; acestea nu sunt scoruri de backlog. Dezvoltarea rămâne separată de prioritățile active de mai sus. |
| [Designul testelor API Practice Shop](ATF_Practice_Shop_API_Test_Design_Feedback.md) | Analiză live neprioritizată: alinierea numelui cu aserțiunile, scopul distinct al variant/matrix și coerența nivelurilor `unit/component`. Schimbările de teste cer un scope de implementare explicit; feedback-ul nu este autorizare. |

## Feedback închis păstrat pentru controale temporare

Următoarele decizii sunt închise și nu intră în backlog. Fișierele rămân
temporar deoarece conțin controale `testChanges` încă active până la
**2026-10-30**; acestea sunt cerințe de guvernanță, nu TODO-uri funcționale.
Nu ștergeți blocurile sau documentele înainte de expirare/reconciliere și de
verificarea guvernanței.

| Feedback închis | Decizie promovată | Control păstrat |
|---|---|---|
| [Automation Exercise API](ATF_Automation_Exercise_Feedback.md) | Contractele API sunt în README-ul ownerului; nu se redeschid fără regresie nominală. | `AUTOEX-API-ASSERTIONS-CLEANUP-TESTS-01` |
| [Reporting — coeziunea pachetului](ATF_Reporting_Feedback.md) | Helperul UI și pragul neschimbat sunt documentate în README-ul `atf-reporting-tests`. | `REPORT-TEST-PACKAGE-01` |

## Contracte promovate în documentația oficială

| Contract | Sursa normativă |
|---|---|
| Boundary-ul `main`/`test`, ownership și selecția testelor consumer | [Arhitectura ATF](../ATF_Architecture.md), [ghidul de testare](../ATF_Testing_Guide.md), README-urile Practice Shop și Automation Exercise |
| Regula de coeziune: maximum opt clase per pachet; inventarele includ modulele cu surse de producție | [Bune practici de cod](../README_Atf_CodeBestPractices.md#coeziunea-pachetelor-pragul-de-opt-clase); package split/inventory AutoEx, boundary-ul Shop harness și activarea Shop sunt reparate; boundary-ul `AutoExPurchaseSession` rămâne deschis |
| Profile Reporting per scenariu și izolare față de scenariile-surori | [README ATF Reporting Tests](../../atf-reporting-tests/README.md), [contractul de captură](../ATF_Evidence_Capture_Contract.md) |
| Nivelul/canalul testelor și declarația neutră UI evidence | [Contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md), [README Test Kit](../../atf-test-kit/README.md); forma comună și izolarea profilului per scenariu sunt implementate; agregarea cross-phase rămâne în `SHOP-CAPTURE-01` |
| Cleanup neutru failure-aware pentru testele E2E | [README Test Kit](../../atf-test-kit/README.md); `FailureAwareCleanup` este implementat și probat de kit și consumatori. Separarea clasificării, activării și instalării harness-ului rămâne în feedback-ul E2E; nu există un harness UI generic în Test Kit |
| UI/API validation, ownership-ul dovezilor și layout-ul raportului | [Contractul semantic de raportare](../ATF_Reporting_Contract.md), [contractul display/layout](../ATF_Reporting_Display_Contract.md) |
| Notificarea opt-in a căii Extent după publicare, inclusiv sesiunea comună Shop | [README Test Kit](../../atf-test-kit/README.md#politica-pentru-calea-raportului-publicat), [ATF Console](../../atf-console/README.md) și [ATF Reporting](../../atf-reporting/README.md); contract implementat, fără backlog separat |
| Screenshot policy și scenariul final-only Automation Exercise | [README Automation Exercise](../../atf-test-auto-ex/README.md), [README ATF Reporting](../../atf-reporting/README.md) |

## Readiness whole-ATF găsit la reconciliere

Suita whole-ATF trebuie să fie verde și pe regulile de readability, nu doar pe
testele locale. Constatarea live acoperă invocări ATF complet calificate în
`ExtentReportSessionLifecycleTest`, `AtfConsoleBridge` și
`UiApiVerdictProjectionProbe`, plus un import duplicat în
`AutoExTestAccount`; acestea cer curățarea sursei, nu relaxarea testelor.
`ArchitectureModuleBoundaryTest` respinge separat plasarea adaptorului Console,
urmărită și scorată mai sus. Guvernanța testelor nu a fost modificată în această
reconciliere.

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
