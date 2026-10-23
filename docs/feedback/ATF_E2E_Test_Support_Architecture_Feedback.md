# ATF Java — suport E2E comun între consumatori

Acest feedback fixează cerința de reducere a suportului duplicat din testele E2E
Shop și AutoEx. `atf-test-kit` este singurul loc candidat pentru primitivele
generice de test; nu se propune un modul UI separat și nu se afirmă că aceste
primitive există deja.

**Stare:** deschis. **Prioritate:** 87/100 (impact 36 + lacună 27 + lucrabil
16 + datorie documentară 8).

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
| `AutoExCatalogApiPrecondition` a fost adăugat în `src/main` și este folosit de `AutoExPurchaseJourney`; testul său component este în pachetul component root. | Clasa este specifică journey-ului AutoEx, iar package inventory-ul protejat nu a fost sincronizat cu ea; testul component trebuie plasat într-un subpachet coerent. Nu mutați helperul în Kit și nu actualizați testul de guvernanță fără feedback explicit pentru schimbarea testului protejat. |
| `ShopE2eContext` include journal, config, capture provider și metadata specifice Shop. | Nu mutați clasa întreagă; separați fazele și contractele generice de aceste responsabilități locale. |

### Constatare curentă de coeziune și guvernanță

`AutoExComponentSuite` trece 27 de teste, dar gate-ul transversal de pachete
eșuează separat în două locuri: inventory-ul exact din
`AtfPackageCohesionGovernanceTest` nu include noua clasă production
`AutoExCatalogApiPrecondition`, iar pachetul
`atf.ui.tests.autoex.component` conține acum nouă clase, peste pragul de opt.
Aceasta este o constatare activă, nu un test „preexistent” și nu se rezolvă prin
ridicarea pragului sau relaxarea listei. Un split semantic al testului API în
`atf.ui.tests.autoex.component.api` ar elimina hotspot-ul de nouă clase; inventory-
ul production necesită o decizie separată și feedback explicit înaintea oricărei
modificări a testului de guvernanță.

### Probleme de lifecycle confirmate în cod

Acestea sunt defecte concrete, nu doar riscuri teoretice; rămân deschise până
când sunt reparate și acoperite prin teste cu failure injection:

| Situație | Efect posibil | Cerință de închidere |
|---|---|---|
| `ShopE2eContext.openBrowser` deschide sesiunea Playwright, apoi creează providerul/journal-ul și atașează listener-ele într-un `try` al cărui `catch` doar re-aruncă excepția. | O eroare după deschiderea browserului poate lăsa sesiunea deschisă. | La eșecul oricărei etape de achiziție, închide resursele deja create în ordine inversă; păstrează eroarea inițială și atașează eșecurile de cleanup ca suppressed. |
| `AutoExE2eRun.finish` publică network inspector-ul înaintea blocurilor protejate pentru completarea fazei/scenariului și închiderea browserului. | O eroare la publicarea inspectorului poate sări completarea raportului și cleanup-ul browserului. | Fiecare etapă de finalizare trebuie încercată independent într-un teardown failure-safe; o eroare nu trebuie să împiedice etapele ulterioare. |
| `AutoExE2eExtension` activează E2E-ul dacă detectează markerul IntelliJ, chiar dacă YAML-ul are `e2eEnabled: false`; proprietatea JVM explicită `false` are precedență și oprește testul. | „Dezactivat implicit” nu descrie toate modurile de lansare, iar motivul `enabled` poate pretinde incorect că activarea a fost explicită. | Documentați separat comportamentul Maven și IDE; definiți markerul IDE drept heuristică de activare, păstrați `false` explicit ca override, iar diagnosticul să numească sursa reală a deciziei. |

### Declarație, activare și instalarea harness-ului sunt contracte distincte

| Mecanism live | Ce face | Ce nu face |
|---|---|---|
| `@AtfE2eTest` / `@AtfTestChannel` | clasifică nivelul și canalul testului | nu autorizează accesul la site și nu deschide browserul |
| `@AtfUiEvidence` | declară forma flow-ului și intenția capturilor | nu instalează o extensie JUnit și nu pornește lifecycle-ul |
| `@WithE2eHarness` în Shop / `@ExtendWith` în AutoEx | instalează infrastructura locală a consumerului | nu reprezintă încă un contract comun de opt-in |
| YAML / proprietăți JVM / marker IDE AutoEx | selectează politica de activare, cu precedențe diferite | nu trebuie confundate cu adnotările de clasificare/evidence |

Cerința deschisă este să fie documentată și probată precedența activării pentru
fiecare consumer: implicit fail-closed în rulările obișnuite, opt-in clar pentru
resurse externe și un override explicit `false` care oprește testul indiferent
de YAML sau detectarea IDE. O adnotare compusă comună este o opțiune de design,
nu o funcționalitate existentă și nici o condiție pentru extragerea lifecycle-ului.

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

Adaptorii `ShopConsoleBridge` și `AutoExConsoleBridge` au feedback separat în
[`ATF_Consumer_Test_Architecture_Feedback.md`](ATF_Consumer_Test_Architecture_Feedback.md).
Nu amestecați ownership-ul Console–Reporting cu extracția lifecycle-ului UI;
cele două schimbări pot avea dependențe, dar au contracte și teste distincte.

## Cerințe de acceptare pentru implementare

1. **Achiziție atomică în practică:** injectează eșec după fiecare resursă
   dobândită; resursele deja deschise se închid invers, o singură dată, iar
   eroarea de achiziție rămâne primară.
2. **Teardown best-effort, fără scurtcircuit:** eșecul la failure evidence,
   network inspector, completarea fazei/scenariului sau `browser.close` nu
   împiedică încercarea etapelor rămase; eroarea testului rămâne primară, iar
   erorile ulterioare sunt păstrate ca suppressed.
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
   prevalează peste YAML și detectarea IDE; mesajul de skip/enable numește cauza.
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
2. Scrieți separat contractul de activare: default pentru Maven, detectarea IDE,
   precedența YAML/proprietăți și override-ul explicit `false`; clarificați
   rolurile adnotărilor și ale extensiei JUnit.
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

