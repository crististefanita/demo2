# ATF Java — suport E2E comun între consumatori

Acest feedback fixează cerința de reducere a suportului duplicat din testele E2E
Shop și AutoEx. `atf-test-kit` deține acum o primitivă comună failure-aware,
demonstrată prin ambele consumatoare. Nu se propune un modul UI separat și nu se
mută în Kit browserul, raportarea sau business-ul.

**Stare:** închis cu decizie explicită. Cele două gap-uri consumer au owner,
politică și probe; scorul rămas este 0/100. Activarea fail-closed Shop este
implementată și acoperită componentar. Boundary-ul `AutoExPurchaseSession` este
menținut intenționat ca API intern al runtime-ului consumer AutoEx, nu ca
abstracție reutilizabilă ATF. Atomicitatea
nodurilor Reporting este un gap separat, cu owner și scor propriu în
[`ATF_Reporting_Feedback.md`](ATF_Reporting_Feedback.md#report-start-atomicity-01--crearea-atomică-a-nodurilor-de-raport); nu se numără de două ori aici.

## Gap-ul observat

Două compoziții Java reale repetă infrastructură apropiată:

| Consumator | Owner-ul curent | Responsabilități vizibile |
|---|---|---|
| Practice Shop | [`ShopE2eTestSupport`](../../atf-test-practice-shop/src/test/java/atf/ui/tests/practiceshop/support/ShopE2eTestSupport.java) și [`ShopE2eContext`](../../atf-test-practice-shop/src/test/java/atf/ui/tests/practiceshop/support/lifecycle/ShopE2eContext.java) | extensie JUnit și injectarea harness-ului, configurare/canal, sesiune Playwright, evidence/report, fazele `SETUP` și `TEST BODY`, network journal și reguli de cont |
| Automation Exercise | [`AutoExE2eExtension`](../../atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eExtension.java) și [`AutoExE2eRun`](../../atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eRun.java) | opt-in JUnit, injectarea run-ului, configurare/canal, sesiune Playwright, sesiune Extent partajată, profil evidence, teardown și sumarul de network |

Intersecția demonstrată este limitată la cleanup-ul failure-aware: încercarea
ordonată a tuturor etapelor, închiderea inversă a resurselor parțiale și păstrarea
erorii inițiale. `FailureAwareCleanup` este probat direct și prin adaptoarele
Shop și AutoEx. Browserul, raportul, callback-urile JUnit și semantica rețelei
rămân locale; nu există încă dovezi pentru un harness generic.

Diferențele sunt la fel de importante: Shop are conturi și precondiții de business,
checkpoint-uri specifice și faze explicite `SETUP`/`TEST BODY`; AutoEx are opt-in
pentru site public, trafic cauzal și handler-e specifice site-ului. Acestea nu
devin parametri generici ai harness-ului doar pentru a elimina câteva fișiere.

### Source-set-ul Shop — închis

Harness-ul browser/report și adaptoarele exclusiv E2E sunt în `src/test`, iar
`ShopArchitectureTest` păzește această limită fără a muta paginile, flow-urile
sau locatoarele din `src/main`. Harta source roots și contractul normativ sunt
în [README-ul Practice Shop](../../atf-test-practice-shop/README.md); acest
punct nu mai este un TODO.

### Boundary-ul AutoEx rămas deschis

`AutoExE2eRun` este în `src/test`, iar `AutoExPurchaseSession` din `src/main`
expune `Page`, `AtfAssertion`, configurația AutoEx, acțiuni/selectori,
checkpoint-uri, observații API și callback de cleanup. Decizia este să rămână
în runtime-ul consumer: journey-ul și page objects din `src/main` îl folosesc
ca boundary intern de execuție, iar implementarea concretă JUnit/evidence rămâne
în `src/test`. Nu este un port neutru, API public pentru alți consumatori sau
model de mutat în `atf-test-kit`; dependențele Playwright/Reporting sunt
capabilități explicite ale acestui consumer. Testul de arhitectură păzește
ownership-ul runtime și interzice scurgerea JUnit/Test Kit în contract.

### Cleanup, rollback și coeziune — închise

- `FailureAwareCleanup` este descris în [README-ul Test Kit](../../atf-test-kit/README.md)
  și probat direct, apoi prin adaptoarele Shop/AutoEx.
- Rollback-ul inițializării, teardown-ul AutoEx, inventory-ul și pragul de opt
  clase au probe la ownerii consumerilor; nu s-au adăugat excepții de guvernanță.
- Contractele stabile sunt în [README-ul Practice Shop](../../atf-test-practice-shop/README.md),
  [README-ul AutoEx](../../atf-test-auto-ex/README.md) și
  [bunele practici de cod](../README_Atf_CodeBestPractices.md).

Atomicitatea nodurilor Extent nu este un gap E2E și se urmărește numai în
[`REPORT-START-ATOMICITY-01`](ATF_Reporting_Feedback.md#report-start-atomicity-01--crearea-atomică-a-nodurilor-de-raport).
Nu păstrăm aici rezultate numerice sau artifacte de rulare; acestea se verifică
la execuție conform [ghidului de testare](../ATF_Testing_Guide.md).

### Declarație, activare și instalarea harness-ului sunt contracte distincte

| Mecanism live | Ce face | Ce nu face |
|---|---|---|
| `@AtfE2eTest` / `@AtfTestChannel` | clasifică nivelul și canalul testului | nu autorizează accesul la site și nu deschide browserul |
| `@AtfUiEvidence` | declară forma flow-ului și intenția capturilor | nu instalează o extensie JUnit și nu pornește lifecycle-ul |
| `@WithE2eHarness` în Shop / `@ExtendWith` în AutoEx | instalează infrastructura locală a consumerului | nu substituie politica explicită de activare |
| YAML / proprietăți JVM / marker IDE AutoEx și proprietatea JVM/marker IDE Shop | selectează politica de activare | nu trebuie confundate cu adnotările de clasificare/evidence |

Politica AutoEx este implementată și probată pentru Maven fără opt-in, YAML,
heuristica IntelliJ și override-ul JVM explicit `true`/`false`; sursa deciziei
este prezentată în motivul JUnit. Markerul IDE rămâne deliberat heuristic, iar
adnotările de clasificare nu sunt confundate cu opt-in-ul. O adnotare compusă
comună rămâne o opțiune de design, nu o condiție pentru extragerea lifecycle-ului.

## Decizie de boundary

**Nu mutați clasele `ShopE2eTestSupport`, `ShopE2eContext` sau `AutoExE2eRun` ca
atare în `atf-test-kit`.** Sunt compoziții mari, cu dependențe și vocabular de
consumer. Mutarea lor ar ascunde cuplarea în modulul comun, nu ar elimina-o.

Boundary-ul țintă este în două niveluri:

```text
atf-test-kit
  clasificare + metadata · lifecycle generic de resurse/erori
  fără Playwright, browser, Extent sau semantics UI
       ↓ test scope în consumer
Practice Shop / AutoEx
  harness adapter local · browser/report · config și journey de business
```

- `atf-test-kit` păstrează dependențele actuale Jackson/YAML și JUnit; nu importă
  `atf-ui`, Playwright sau `atf-reporting`. `atf-ui` consumă deja Test Kit în test
  scope, deci dependența inversă ar crea un ciclu în reactor.
- Nu se propune și nu se creează un modul UI separat. `FailureAwareCleanup` din
  `atf-test-kit` furnizează numai cleanup neutru: rularea tuturor etapelor,
  închiderea inversă și agregarea erorilor primare/suppressed.
- `atf-test-kit` nu primește profile de capture, network inspector sau raportare;
  adaptoarele care le folosesc rămân în consumer.
- Extensiile care deschid browserul, adaptează evidence-ul și dețin sesiunea
  raportului rămân în `src/test` al fiecărui consumer. O bază JUnit comună se
  extrage numai dacă testele confirmă aceeași semantică și API-ul nu transportă
  concepte UI/report.
- `atf-reporting` rămâne independent de modulele ATF în `src/main`.

Primitivele comune rămân infrastructură de test, nu un framework de business.
`FailureAwareCleanup` este folosit de ambele consumatoare numai din test scope;
orice extracție ulterioară cere aceeași demonstrație comportamentală și un
reactor aciclic.

## Ce este comun și ce rămâne local

| Se extrage numai dacă API-ul comun rămâne neutru | Rămâne în consumer |
|---|---|
| `FailureAwareCleanup`: ownership/închidere inversă pentru `AutoCloseable`, încercarea etapelor și agregarea excepțiilor, fără browser/report API | `ShopConfiguration`, `AutoExConfiguration`, YAML și selecția profilului de business |
| helper de test JUnit doar dacă elimină lifecycle duplicat real fără a decide opt-in-ul consumerului | `ShopLocators`, `AutoExSelectors`, page objects, rute, request factory și purchase flow |
| cleanup neutru failure-aware, demonstrat în Shop și AutoEx | conturi, înregistrare/login, cleanup-ul contului, fixture-uri și reguli de siguranță ale fiecărui site |
| abstracții suplimentare numai după probe; nu se mută rendererul sau browserul în Kit | `ShopPurchaseCheckpoint`, `AutoExPurchaseTraceStep`, network journal, corelare cauzală și semantica API specifică |
| faze opționale numai dacă sunt abstracție reală, nu redenumirea fazelor Shop | titluri Gherkin, verdicturi de business, confirmarea comenzii și comportamentul handler-elor vizuale |

`FailureAwareCleanup` este singura primitivă comună extrasă în această etapă.
Profilele de evidence, lifecycle-ul browserului și sesiunea de raport partajată
rămân locale. Faptul că ambele consumatoare deschid Extent nu justifică un API
de UI în Test Kit.

Integrarea Console–Reporting este acoperită separat în
[`ATF_Consumer_Test_Architecture_Feedback.md`](ATF_Consumer_Test_Architecture_Feedback.md):
`ShopConsoleBridge` a fost eliminat, iar adaptorul local AutoEx doar deleagă la
adaptorul comun din `atf-console`.
Nu amestecați ownership-ul Console–Reporting cu extracția lifecycle-ului UI;
cele două schimbări pot avea dependențe, dar au contracte și teste distincte.

## Cerințe de acceptare pentru implementare

1. **Achiziție atomică la handle-urile returnate — rezolvată:** injection după
   browser/scenariu/fază verifică rollback invers, eroarea inițială primară și
   erorile de rollback ca `suppressed`. Atomicitatea internă a publisher-ului
   Reporting rămâne în feedback-ul ownerului Reporting.
2. **Teardown best-effort, fără scurtcircuit — rezolvat:** eșecul la failure evidence,
   network inspector, completarea fazei/scenariului sau `browser.close` nu
   împiedică încercarea etapelor rămase; eroarea testului rămâne primară, iar
   erorile ulterioare sunt păstrate ca suppressed.
3. **Scope explicit — verificat:** resursa per metodă și sesiunea de raport per root/suită au
   owneri diferiți și se închid la boundary-ul corect, inclusiv la setup eșuat;
   primitivele Test Kit sunt folosite numai din test scope-ul consumerului.
4. **Evidence izolată:** profilul fiecărui scenariu se aplică acelui scenariu,
   nu se scurge între scenarii-surori într-un raport partajat.
5. **Faze opționale:** infrastructura permite setup înainte de body fără să
   impună vocabulary-ul Shop sau să inventeze setup pentru AutoEx.
6. **Rețea fără semantică falsă:** infrastructura comună poate transporta
   observations/evidence, dar selecția request-urilor, baseline-ul cauzal,
   endpoint-urile și verdictul rămân la consumer.
7. **Activare neambiguă:** clasificarea, metadata evidence, instalarea extensiei
   și decizia de opt-in au contracte separate; override-ul explicit `false`
   prevalează peste YAML și detectarea IDE; mesajul numește cauza. **Implementat
   și acoperit componentar în AutoEx; markerul IDE rămâne dependent de runner.**
8. **Testare la ownerul potrivit:** cleanup-ul comun și rollback-ul Shop/AutoEx
   sunt deja probate componentar; testele rămase vizează activarea directă Shop
   și decizia de boundary pentru `AutoExPurchaseSession`. Atomicitatea internă
   a nodurilor se testează exclusiv în `atf-reporting-tests`. E2E-ul rămâne
   proba integrării reale.
9. **Guvernanță:** `atf-test-kit` nu importă `atf.ui.practiceshop`,
   `atf.ui.autoex`, `atf-ui`, Playwright sau Reporting; consumatorii îl declară
   în test scope dacă nu au utilizare legitimă în `src/main`; niciun support nu
   devine clasă executabilă ori layer E2E ascuns; gate-ul distinge suportul
   reutilizabil de aplicație de harness-ul exclusiv de test; pragul de opt clase
   per pachet rămâne activ.
10. **Migrare fără drift — cleanup rezolvat:** adaptoarele locale păstrează
    comportamentul observable, iar ambele consumă contractul Test Kit; profilele
    de evidence, sesiunea raportului și cleanup-ul de business nu au fost mutate.

## Ordinea rămasă pentru boundary-urile consumer

1. **Shop E2E activation — închis:** `@WithE2eHarness` instalează acum și
   `ShopE2eActivationExtension`. Rularea directă din IntelliJ rămâne activă;
   alte runnere sunt dezactivate implicit și pot fi activate explicit cu
   `-Datf.practice.shop.e2e.enabled=true`. Valoarea explicită `false` prevalează
   peste markerul IntelliJ, iar valori invalide eșuează închis cu motiv clar.
   Politica este component-testată; skip-ul JUnit afișează motivul deciziei.
2. **AutoEx session boundary — decizie închisă:** `AutoExPurchaseSession` rămâne
   contract privat al runtime-ului consumer, cu Playwright/Reporting declarate
   ca dependențe intenționate; numai implementarea JUnit și lifecycle-ul rămân
   în test scope. Nu se extrage o interfață comună și nu se mută contractul în
   Test Kit. Componenta de arhitectură verifică această delimitare.

Atomicitatea internă a nodurilor Extent se urmărește exclusiv în
[`ATF_Reporting_Feedback.md`](ATF_Reporting_Feedback.md#report-start-atomicity-01--crearea-atomică-a-nodurilor-de-raport),
nu este un al treilea TODO E2E. După închiderea celor două gap-uri consumer,
nu este un al treilea TODO E2E. După închiderea boundary-ului AutoEx rămas,
rerulați guvernanța whole-ATF; nu relaxați allowlist-uri sau praguri pentru a
ascunde drift-ul. Poarta trebuie să treacă și regulile de readability, nu numai
inventarul de pachete și aserțiunile componentelor.

**Criteriul de succes nu este numărul minim de fișiere.** Primitivele neutre de
resurse/erori au un singur owner în Test Kit; două adaptoare locale păstrează
browserul, raportarea și semantica site-ului; testele localizează precis
regresia. Lifecycle-ul UI nu este abstractizat doar pentru a reduce duplicarea.

## Control temporar pentru schimbarea testelor

Următoarea autorizare este limitată la repararea inventory-ului, coeziunea
testelor, teardown-ul failure-safe și politicile de activare AutoEx și Shop.
Nu autorizează schimbări ale altor teste sau ale regulilor de guvernanță.

```yaml
testChanges:
  - id: AUTOEX-E2E-LIFECYCLE-AND-INVENTORY-01
    status: ACTIVE
    base: 84a8e9d92334d86f835d6dae3347170ef0e0e02e
    expires: 2026-10-30
    reason: Move the Shop E2E harness to test scope; centralize only neutral failure-aware cleanup; prove AutoEx initialization rollback, teardown continuation, activation diagnostics, and package inventory without lowering governance thresholds.
    proof: mvn -pl atf-test-kit,atf-test-practice-shop,atf-test-auto-ex,atf-tests -am "-Dtest=FailureAwareCleanupTest,ShopComponentSuite,AutoExComponentSuite,AtfPackageCohesionGovernanceTest,ConsoleDefaultsGovernanceTest,DocumentationTargetGovernanceTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
    allowDelete: true
    paths:
      - atf-test-kit/src/test/java/atf/api/testkit/lifecycle/FailureAwareCleanupTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eRun.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eExtension.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExFailureCombiner.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/lifecycle/AutoExTeardown.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/lifecycle/AutoExE2eActivationPolicy.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/lifecycle/AutoExE2eInitialization.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/AutoExCatalogApiPreconditionComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/api/AutoExCatalogApiPreconditionComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/AutoExFailureCombinerComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/lifecycle/AutoExTeardownComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/lifecycle/AutoExE2eActivationPolicyComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/lifecycle/AutoExE2eInitializationComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/AutoExConsumerArchitectureComponentTest.java
      - atf-tests/src/test/java/atf/api/tests/governance/console/ConsoleDefaultsGovernanceTest.java
      - atf-tests/src/test/java/atf/api/tests/governance/AtfPackageCohesionGovernanceTest.java
```

