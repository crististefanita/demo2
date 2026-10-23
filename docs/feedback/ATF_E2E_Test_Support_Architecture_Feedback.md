# ATF Java — suport E2E comun între consumatori

Acest feedback fixează cerința de reducere a suportului duplicat din testele E2E
Shop și AutoEx. `atf-test-kit` este singurul loc candidat pentru primitivele
generice de test; nu se propune un modul UI separat și nu se afirmă că aceste
primitive există deja.

**Stare:** deschis. **Prioritate:** 81/100 (impact 36 + lacună 21 + lucrabil
16 + datorie documentară 8); recalibrat după închiderea gap-urilor AutoEx.

## Gap-ul observat

Două compoziții Java reale repetă infrastructură apropiată:

| Consumator | Owner-ul curent | Responsabilități vizibile |
|---|---|---|
| Practice Shop | [`ShopE2eTestSupport`](../../atf-test-practice-shop/src/test/java/atf/ui/tests/practiceshop/support/ShopE2eTestSupport.java) și [`ShopE2eContext`](../../atf-test-practice-shop/src/main/java/atf/ui/practiceshop/support/ShopE2eContext.java) | extensie JUnit și injectarea harness-ului, configurare/canal, sesiune Playwright, evidence/report, fazele `SETUP` și `TEST BODY`, network journal și reguli de cont |
| Automation Exercise | [`AutoExE2eExtension`](../../atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eExtension.java) și [`AutoExE2eRun`](../../atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eRun.java) | opt-in JUnit, injectarea run-ului, configurare/canal, sesiune Playwright, sesiune Extent partajată, profil evidence, teardown și sumarul de network |

Există o intersecție plauzibilă, dar încă nedemonstrată: ownership-ul resurselor
și agregarea erorilor par comune; browserul, raportul și callback-urile JUnit
trebuie comparate prin teste înainte să fie abstractizate. Orice extracție în
`atf-test-kit` trebuie să rămână fără API de business, UI, Reporting și semantica
rețelei.

Diferențele sunt la fel de importante: Shop are conturi și precondiții de business,
checkpoint-uri specifice și faze explicite `SETUP`/`TEST BODY`; AutoEx are opt-in
pentru site public, trafic cauzal și handler-e specifice site-ului. Acestea nu
devin parametri generici ai harness-ului doar pentru a elimina câteva fișiere.

### Source-set-ul Shop este un gap separat

| Fapt live | Consecință și cerință |
|---|---|
| `ShopE2eContext` este în `src/main`, dar referințele sale sunt în infrastructura și testele E2E. | Harness-ul browser/report ține în scope-ul livrat dependențe UI/Reporting fără un consumator de producție demonstrat; separați-l de codul Shop reutilizabil. |
| `ShopArchitectureTest` cere explicit ca `ShopE2eContext` să rămână în `src/main`, deși contextul este folosit numai de harness/teste. | Gate-ul codifică acum boundary-ul greșit. Nu îl relaxați fără criterii: înlocuiți excepția cu reguli care disting suportul de aplicație de harness-ul și fixture-urile test-only; mutarea se face odată cu probele și dependency scope-ul corect. |
| `AutoExE2eRun` este în `src/test`, dar `AutoExPurchaseSession` din `src/main` expune `Page`, `AtfAssertion`, configurația AutoEx, acțiuni/selectori, checkpoint-uri, observații API și callback de cleanup. | Portul separă runnerul JUnit de journey, dar nu este neutru și nu trebuie copiat ca model generic. Decideți dacă acel contract aparține exclusiv test-support-ului sau poate fi îngustat la o frontieră de aplicație fără browser/reporting. |
| `ShopE2eContext` include journal, config, capture provider și metadata specifice Shop. | Nu mutați clasa întreagă; separați fazele și contractele generice de aceste responsabilități locale. |

### Constatare curentă de coeziune și guvernanță

Starea verificată a coeziunii AutoEx:

- inventory-ul production include `AutoExCatalogApiPrecondition`, iar proba sa
  component este în `component.api`;
- testele de lifecycle și activare sunt în subpachetul dedicat;
- suitele AutoEx și gate-ul de coeziune verifică comportamentul și inventarul
  fără să introducă excepții sau să relaxeze pragul de opt clase.

Activarea AutoEx și continuarea teardown-ului sunt contracte închise, nu TODO-uri:
politica de activare și motivul deciziei sunt documentate în
[`atf-test-auto-ex/README.md`](../../atf-test-auto-ex/README.md), iar
`AutoExTeardownComponentTest` probează că eșecurile unei etape nu opresc
etapele următoare și nu înlocuiesc cauza inițială.

### Gap-uri de lifecycle rămase

Tabelul păstrează numai comportamente neînchise, confirmate în codul live:

| Situație | Efect posibil | Cerință de închidere |
|---|---|---|
| `ShopE2eContext.openBrowser` deschide sesiunea Playwright, apoi creează providerul/journal-ul și atașează listener-ele într-un `try` al cărui `catch` doar re-aruncă excepția; `open` închide raportul propriu fără să suprime un eventual eșec de close. | O eroare după deschiderea browserului poate lăsa sesiunea deschisă, iar eșecul închiderii raportului poate masca eroarea inițială. | La eșecul oricărei etape de achiziție, închide resursele deja create în ordine inversă; păstrează eroarea inițială și atașează eșecurile de cleanup ca suppressed. **Rămas deschis pentru Shop.** |

Rollback-ul inițializării și teardown-ul AutoEx sunt închise pentru fiecare
resursă al cărei handle a fost returnat:

- `AutoExE2eInitialization` marchează FAIL faza/scenariul disponibile, închide
  browserul și păstrează setup failure ca eroare primară;
- `AutoExTeardown` încearcă independent inspectorul, browserul și completarea
  fazei/scenariului la `finish`;
- `AutoExE2eInitializationComponentTest` injectează eșec după browser,
  scenariu, fază și run, inclusiv eșecuri secundare la rollback și rezultate
  `null` de la factory.

Rămâne un boundary Reporting, nu AutoEx: dacă `startScenario`/`startPhase`
creează un nod intern și apoi aruncă înainte să returneze handle-ul, consumerul
nu îl poate marca FAILED. Cerința este urmărită în
[`ATF_Reporting_Feedback.md`](ATF_Reporting_Feedback.md#report-start-atomicity-01--crearea-atomică-a-nodurilor-de-raport).

### Declarație, activare și instalarea harness-ului sunt contracte distincte

| Mecanism live | Ce face | Ce nu face |
|---|---|---|
| `@AtfE2eTest` / `@AtfTestChannel` | clasifică nivelul și canalul testului | nu autorizează accesul la site și nu deschide browserul |
| `@AtfUiEvidence` | declară forma flow-ului și intenția capturilor | nu instalează o extensie JUnit și nu pornește lifecycle-ul |
| `@WithE2eHarness` în Shop / `@ExtendWith` în AutoEx | instalează infrastructura locală a consumerului | nu reprezintă încă un contract comun de opt-in |
| YAML / proprietăți JVM / marker IDE AutoEx | selectează politica de activare, cu precedențe diferite | nu trebuie confundate cu adnotările de clasificare/evidence |

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
- Nu se propune și nu se creează un modul UI separat. Prima opțiune este să
  extragem în `atf-test-kit` numai primitive generice de testare: ownership de
  resurse, close-once și combinarea erorii primare cu eșecurile de cleanup.
- `atf-test-kit` nu primește profile de capture, network inspector sau raportare;
  adaptoarele care le folosesc rămân în consumer.
- Extensiile care deschid browserul, adaptează evidence-ul și dețin sesiunea
  raportului rămân în `src/test` al fiecărui consumer. O bază JUnit comună se
  extrage numai dacă testele confirmă aceeași semantică și API-ul nu transportă
  concepte UI/report.
- `atf-reporting` rămâne independent de modulele ATF în `src/main`.

Primitivele propuse trebuie să rămână infrastructură de test, nu un framework de
business. Ele intră în `atf-test-kit` numai după ce ambele consumatoare le
demonstrează prin teste și dependențele reactorului rămân aciclice.

## Ce este comun și ce rămâne local

| Se extrage numai dacă API-ul comun rămâne neutru | Rămâne în consumer |
|---|---|
| primitivi de ownership pentru resurse `AutoCloseable`, close-once și combinarea excepțiilor, fără browser/report API | `ShopConfiguration`, `AutoExConfiguration`, YAML și selecția profilului de business |
| helper de test JUnit doar dacă elimină lifecycle duplicat real fără a decide opt-in-ul consumerului | `ShopLocators`, `AutoExSelectors`, page objects, rute, request factory și purchase flow |
| un contract neutru de cleanup, dacă behavior tests demonstrează aceleași reguli | conturi, înregistrare/login, cleanup-ul contului, fixture-uri și reguli de siguranță ale fiecărui site |
| abstracții suplimentare numai după probe; nu se mută rendererul sau browserul în Kit | `ShopPurchaseCheckpoint`, `AutoExPurchaseTraceStep`, network journal, corelare cauzală și semantica API specifică |
| faze opționale numai dacă sunt abstracție reală, nu redenumirea fazelor Shop | titluri Gherkin, verdicturi de business, confirmarea comenzii și comportamentul handler-elor vizuale |

Primitivele neutre de resurse și agregare a erorilor sunt singurii candidați
inițiali pentru `atf-test-kit`; nici acestea nu se copiază înainte de teste
comportamentale în ambele module, inclusiv eșec parțial la achiziție și cleanup
multiplu. Profilele de evidence, lifecycle-ul browserului și sesiunea de raport
partajată rămân locale. Faptul că ambele consumatoare deschid Extent nu demonstrează
echivalența și nu justifică un API de UI în Test Kit.

Integrarea Console–Reporting este acoperită separat în
[`ATF_Consumer_Test_Architecture_Feedback.md`](ATF_Consumer_Test_Architecture_Feedback.md):
`ShopConsoleBridge` a fost eliminat, iar adaptorul local AutoEx doar deleagă la
adaptorul comun din `atf-console`.
Nu amestecați ownership-ul Console–Reporting cu extracția lifecycle-ului UI;
cele două schimbări pot avea dependențe, dar au contracte și teste distincte.

## Cerințe de acceptare pentru implementare

1. **Achiziție atomică în practică:** pentru AutoEx, testele injectează eșec
   după obținerea browserului, scenariului și fazei; resursele cu handle returnat
   se închid, verdicturile disponibile devin FAIL, iar eroarea de setup rămâne
   primară. Pentru Shop, rollback-ul achiziției parțiale rămâne deschis. Niciun
   consumer nu poate recupera un handle pe care API-ul Reporting nu l-a returnat.
2. **Teardown best-effort, fără scurtcircuit:** în AutoEx, eșecul la network
   inspector, completarea fazei/scenariului sau `browser.close` nu împiedică
   încercarea etapelor rămase; eroarea testului rămâne primară, iar erorile
   ulterioare sunt păstrate ca suppressed. Eșecul de cleanup la achiziția Shop
   rămâne deschis.
3. **Scope explicit:** resursa per metodă și sesiunea de raport per root/suită au
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
8. **Testare la ownerul potrivit:** teste componente cu factory/port injectabil
   forțează fiecare eșec de achiziție și teardown, verifică ordinea/numărul de
   close-uri și păstrarea excepțiilor; fiecare consumer probează mapping-ul,
   opt-in-ul și raportul său. E2E-ul rămâne proba integrării reale.
9. **Guvernanță:** `atf-test-kit` nu importă `atf.ui.practiceshop`,
   `atf.ui.autoex`, `atf-ui`, Playwright sau Reporting; consumatorii îl declară
   în test scope dacă nu au utilizare legitimă în `src/main`; niciun support nu
   devine clasă executabilă ori layer E2E ascuns; gate-ul distinge suportul
   reutilizabil de aplicație de harness-ul exclusiv de test; pragul de opt clase
   per pachet rămâne activ.
10. **Migrare fără drift:** rapoartele și comportamentul observable existente
    rămân stabile; copiile locale ale primitivelor se șterg numai după ce Shop și
    AutoEx folosesc contractul Test Kit și probele ambelor trec.

## Ordinea recomandată

1. Adăugați teste de caracterizare cu failure injection pentru achiziție,
   publicare și teardown în fiecare consumer; fixați ownership-ul și excepția
   primară/suppressed înainte de orice extracție.
2. **Închis pentru AutoEx:** politica de activare, precedența și diagnosticul au
   teste componente; nu se introduce o adnotare compusă comună.
3. Corectați boundary-ul Shop: harness-ul browser/report exclusiv de test iese
   din `src/main`; modificați guard-ul ca să nu mai confunde `support` cu
   reutilizabilitatea pentru aplicație. Dacă `atf-test-kit` nu este folosit de
   `src/main`, declarați dependența Shop în `test` scope.
4. Îngustați sau reclasificați `AutoExPurchaseSession`; nu-l promovați drept port
   generic cât timp expune Playwright, Reporting și semantica AutoEx.
5. Măsurați intersecția rămasă după aceste clarificări. Dacă primitivele generice
   sunt identice, adăugați-le în `atf-test-kit`; lifecycle-ul browserului,
   profilele evidence, rețeaua și business-ul rămân locale. Nu creați un modul UI.
6. Migrați pe rând consumatorii, apoi adăugați guard-uri de dependență și
   verificați inventarul de pachete și suitele whole-ATF.

**Criteriul de succes nu este numărul minim de fișiere.** Primitivele neutre de
resurse/erori au un singur owner în Test Kit; două adaptoare locale păstrează
browserul, raportarea și semantica site-ului; testele localizează precis
regresia. Lifecycle-ul UI nu este abstractizat doar pentru a reduce duplicarea.

## Control temporar pentru schimbarea testelor

Următoarea autorizare este limitată la repararea inventory-ului, coeziunea
testelor, teardown-ul failure-safe și politica de activare AutoEx.
Nu autorizează schimbări ale altor teste sau ale regulilor de guvernanță.

```yaml
testChanges:
  - id: AUTOEX-E2E-LIFECYCLE-AND-INVENTORY-01
    status: ACTIVE
    base: 4e5967b2324c8c1f1f5b66699f86b2d02e92c352
    expires: 2026-10-30
    reason: Repair the AutoEx production inventory, cohere component tests, and verify null-safe initialization rollback, teardown continuation, and activation-source diagnostics.
    proof: mvn -pl atf-test-auto-ex,atf-tests -am -Dtest=AutoExComponentSuite,AtfPackageCohesionGovernanceTest -Dsurefire.failIfNoSpecifiedTests=false test
    allowDelete: true
    paths:
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eRun.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eExtension.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/lifecycle/AutoExTeardown.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/lifecycle/AutoExE2eActivationPolicy.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/lifecycle/AutoExE2eInitialization.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/AutoExCatalogApiPreconditionComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/api/AutoExCatalogApiPreconditionComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/lifecycle/AutoExTeardownComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/lifecycle/AutoExE2eActivationPolicyComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/lifecycle/AutoExE2eInitializationComponentTest.java
      - atf-tests/src/test/java/atf/api/tests/governance/AtfPackageCohesionGovernanceTest.java
```

