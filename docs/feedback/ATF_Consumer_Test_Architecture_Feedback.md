# Feedback — arhitectura testelor consumer Shop și AutoEx

## Pe scurt

Acest document păstrează cerințele comune de structură pentru testele E2E ale
Practice Shop și Automation Exercise. Contractele generice rămân la ownerii ATF;
scenariile și vocabularul de business rămân în consumerul lor.

## Dovezi din codul curent

| Consumer | E2E și suită | Stare a suportului |
|---|---|---|
| Practice Shop | 5 E2E-uri în `atf.ui.tests.practiceshop.e2e`, selectate explicit de `ShopE2eSuite`; există și `ShopComponentSuite`, `ShopApiComponentSuite`, `ShopApiCallAuditSuite` | Suitele component/API au selecții suprapuse; intenția și efectul trebuie făcute verificabile. `ShopApiScreenFlowIntegrationTest` nu apare într-o suită și nu are comandă documentată; `ShopStandaloneApiReportTest` are rulare live opt-in documentată. |
| Automation Exercise | 3 variante ale aceluiași purchase E2E în `atf.ui.tests.autoex.e2e`, selectate explicit de `AutoExPurchaseSuite` | Cele 3 teste componentă curente nu au suită componentă comună: două sunt în `atf.ui.autoex.support`, unul în `atf.ui.tests.autoex.component`. Journey-ul, page objects, config și support sunt încă în `src/test/java`; modulul nu are `src/main/java`, iar dependențele sunt test-scope. |

Codul curent are acoperire parțială, nu un dicționar complet:

- `ShopPurchaseTraceStep` oferă keyword și formulări pentru checkpoint-urile
  principale;
- `ShopE2eContext` construiește local formulări GIVEN de setup/failure;
- `AutoExPurchaseJourney` pasează texte inline în `checkpoint(...)`.

## Cerințe deschise

Curățarea nu înseamnă ștergerea testelor care nu sunt E2E. Consumerul trebuie
să păstreze dovezile sale de componentă/API, dar să nu devină un al doilea loc
pentru testarea generică a ATF sau un depozit de clase fără cale clară de rulare.

| ID | Cerință | Criteriu de acceptanță | Scor |
|---|---|---|---:|
| `CONSUMER-TEST-OWNERSHIP-CLEANUP-01` | Elimină zgomotul și clarifică ownership-ul și calea de rulare pentru clase/teste Shop și AutoEx. | Inventar complet, fără teste orphan/duplicate; separare probată între teste generice ATF și adaptere consumer; cerința se aplică simetric ambelor module. | 95 |
| `CONSUMER-TEST-BOUNDARY-01` | Delimitează testele, suportul consumer și capabilitățile generice pentru Shop și AutoEx. | E2E-urile sunt numai în pachetele `...practiceshop.e2e` / `...autoex.e2e`; suitele sunt în pachetele `...suites` și au membri expliciți/verificabili. Testele componente și API formează a doua categorie; testele API de integrare/reporting nu devin un al treilea tip. Mută suportul consumer-specific reutilizat în `src/main/java` unde lipsește (în special AutoEx), păstrează testele, suitele și fixture-urile strict de test în `src/test/java`, iar codul generic la modulul ATF owner. Guardrail-urile verifică ownership-ul și descoperirea fără clase helper numărate ca teste. | 93 |
| `CONSUMER-GHERKIN-CATALOG-01` | Folosește un catalog tipizat al frazelor Gherkin pentru fiecare consumer. | Extinde dicționarul Shop existent pentru toate frazele raportate, inclusiv GIVEN-urile de setup/failure, și adaugă unul echivalent în AutoEx. Toate checkpoint-urile afișate ca `GIVEN/WHEN/THEN/AND` iau keyword-ul și textul din catalog; textele de business nu se duplică inline în flow-uri, lifecycle sau clase E2E. Testele componente probează maparea și guardrail-ul detectează formulări Gherkin hard-coded; numele scenariului și overview-ul rămân câmpuri distincte. | 90 |

### Criterii de acceptanță — cleanup și ownership

- Inventariază fiecare clasă Java din `src/main` și `src/test`: rol, caller,
  owner, categorie logică (`E2E` / `Component/API`), layer ATF, suite sau comandă
  standalone și efecte externe. Include testele, suitele, fixtures, helpers și
  suportul runtime; nu presupune că numele `Test` sau pachetul determină ownerul.
- Fiecare test păstrat are o cale de rulare declarată: membru într-o suită
  numită sau comandă standalone opt-in justificată. Elimină/consolidează numai
  după ce verifici că nu există comportament unic; helper-ele nu sunt entry
  points, iar suitele conțin doar selecție, ordine și compoziție.
- Pentru Shop, justifică suprapunerile dintre `ShopComponentSuite`,
  `ShopApiComponentSuite` și `ShopApiCallAuditSuite`. Decide explicit dacă
  `ShopApiScreenFlowIntegrationTest` este o probă live distinctă față de
  `ShopStandaloneApiReportTest`; documentează launcherul, consolidează duplicatele
  sau elimină testul cu motiv.
- Pentru AutoEx, aplică aceleași reguli, dar păstrează separarea potrivită
  consumerului: cele trei E2E-uri rămân variantele suitei existente; cele trei
  teste componente actuale trebuie organizate sub ownerul `tests/autoex` și să
  aibă launcher explicit. Creează o suită componentă numai dacă agregarea
  simplifică rularea; altfel documentează comenzile selective.
- Mută testele pentru contracte ATF neutre la modulele owner — renderer/
  publisher în `atf-reporting-tests`, primitive UI în `atf-ui`, adnotări în
  `atf-test-kit`, utilitare în `atf-core`. Păstrează în consumer testele de
  adapter/compoziție care validează contracte, rute, configurație sau date
  specifice Shop/AutoEx; un import Reporting/UI nu face singur un test generic.
- Mută în `src/main` numai suportul runtime specific aplicației folosit de
  journey; JUnit, fixture-urile și orchestration-ul exclusiv de test rămân în
  `src/test`. Nu muta clase mecanic pentru a reduce numărul de fișiere din
  test sau pentru a forța structură identică între consumatori.

## Boundary-ul dorit

| Loc | Conținut permis |
|---|---|
| `src/test/java/.../e2e` | Clase de test declarative: adnotări, nume/overview de scenariu, apelul flow-ului și verdictul observabil |
| `src/test/java/.../suites` | Numai entry-point-uri și selecție/ordine a membrilor E2E ori Component/API; fără logică de business sau fixture-uri |
| `src/test/java/.../component` și `.../api` | Teste consumer de componentă/API; testele `integration` rămân opt-in și sunt grupate operațional în Component/API, fără categorie business suplimentară |
| `src/test/java/.../support` | Fixture-uri, harness-uri și adaptere folosite exclusiv de teste; pachetul nu creează o a treia categorie de teste |
| `src/main/java` în consumer | Configurație, modele, flow-uri, pagini și support necesar testelor, dar specific site-ului și fără framework generic de test clonat |
| Module ATF generice | Playwright/reporting/adnotări/utilitare fără contract, locator sau date de business Shop/AutoEx |

Două categorii logice nu înseamnă un singur nivel JUnit: integration rămâne
etichetat corect tehnic, dar nu justifică un al treilea tip de suită consumer.
Selecția și suprapunerea suitei trebuie să fie explicite; testele de support
nu devin implicit teste doar fiindcă stau lângă producție sau sunt scanate larg.

- Fixture-urile și harness-urile strict de test rămân în `src/test/java`.
- Suportul consumer mutat în `main` trebuie să fie folosit de flow, nu doar să
  reprezinte mecanism de execuție a testului.
- Mutările se fac după rol și dependențe, nu mecanic după numele clasei.

## Închidere

- Închide `CONSUMER-TEST-OWNERSHIP-CLEANUP-01` cu inventarul Shop/AutoEx,
  decizia pentru testul API integration Shop fără launcher documentat și
  justificarea fiecărei suite/suprapuneri; AutoEx trebuie să aibă selecție
  componentă explicită sau comenzi selective documentate.
- Documentează clasele mutate și dependențele Maven; verifică înainte de mutare
  dacă testul aparține generic ATF sau doar validează adapterul consumerului.
- Întărește guvernanța pentru pachete, suite-to-test membership, suprapuneri
  intenționate, discoverability fără orphan tests și excluderea helperelor.
- Păstrează catalogul Gherkin și probele sale independente de aceste mutări.
- Rulează suitele componente/API și compilează modulele; rulează E2E-urile numai
  opt-in, cu dovezi de raport pentru varianta selectată.
- Actualizează README-urile ownerilor după implementare; elimină acest feedback
  doar când ambele criterii sunt probate și nu rămâne lucru comun deschis.
