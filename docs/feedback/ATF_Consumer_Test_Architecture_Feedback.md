# Feedback — arhitectura testelor consumer Shop și AutoEx

## Pe scurt

Acest document păstrează cerințele comune de structură pentru testele E2E ale
Practice Shop și Automation Exercise. Contractele generice rămân la ownerii ATF;
scenariile și vocabularul de business rămân în consumerul lor.

## Dovezi din codul curent

| Consumer | E2E și suită | Stare a suportului |
|---|---|---|
| Practice Shop | 5 clase în `atf.ui.tests.practiceshop.e2e`; `ShopE2eSuite` selectează explicit toate cele 5 | Flow, config, pagini și o parte din support sunt în `src/main/java`; rămân și teste/harness-uri în pachetele `tests.practiceshop.support` și `practiceshop.support` |
| Automation Exercise | 3 variante în `atf.ui.tests.autoex.e2e`; `AutoExPurchaseSuite` le selectează explicit | Toate clasele, inclusiv `AutoExPurchaseJourney`, page objects, config și extension/run, sunt în `src/test/java`; modulul nu are `src/main/java`, iar dependențele sunt test-scope |

Codul curent are acoperire parțială, nu un dicționar complet:

- `ShopPurchaseTraceStep` oferă keyword și formulări pentru checkpoint-urile
  principale;
- `ShopE2eContext` construiește local formulări GIVEN de setup/failure;
- `AutoExPurchaseJourney` pasează texte inline în `checkpoint(...)`.

## Cerințe deschise

| ID | Cerință | Criteriu de acceptanță | Scor |
|---|---|---|---:|
| `CONSUMER-TEST-BOUNDARY-01` | Delimitează testele, suportul consumer și capabilitățile generice pentru Shop și AutoEx. | E2E-urile sunt numai în pachetele `...practiceshop.e2e` / `...autoex.e2e`; suitele sunt în pachetele `...suites` și au membri expliciți/verificabili. Testele componente și API formează a doua categorie; testele API de integrare/reporting nu devin un al treilea tip. Mută suportul consumer-specific reutilizat în `src/main/java` unde lipsește (în special AutoEx), păstrează testele, suitele și fixture-urile strict de test în `src/test/java`, iar codul generic la modulul ATF owner. Guardrail-urile verifică ownership-ul și descoperirea fără clase helper numărate ca teste. | 93 |
| `CONSUMER-GHERKIN-CATALOG-01` | Folosește un catalog tipizat al frazelor Gherkin pentru fiecare consumer. | Extinde dicționarul Shop existent pentru toate frazele raportate, inclusiv GIVEN-urile de setup/failure, și adaugă unul echivalent în AutoEx. Toate checkpoint-urile afișate ca `GIVEN/WHEN/THEN/AND` iau keyword-ul și textul din catalog; textele de business nu se duplică inline în flow-uri, lifecycle sau clase E2E. Testele componente probează maparea și guardrail-ul detectează formulări Gherkin hard-coded; numele scenariului și overview-ul rămân câmpuri distincte. | 90 |

## Boundary-ul dorit

| Loc | Conținut permis |
|---|---|
| `src/test/java/.../e2e` | Clase de test declarative: adnotări, nume/overview de scenariu, apelul flow-ului și verdictul observabil |
| `src/test/java/.../suites` | Suitele E2E și Component/API cu selecția membrilor explicită; nu logică de business sau fixture-uri |
| `src/test/java/.../component` și `.../api` | Teste de componentă/API, inclusiv verificările de raportare/audit și testele care exercită integrarea API |
| `src/test/java/.../support` | Fixture-uri, harness-uri și adaptere folosite exclusiv de teste; pachetul nu creează o a treia categorie de teste |
| `src/main/java` în consumer | Configurație, modele, flow-uri, pagini și support necesar testelor, dar specific site-ului și fără framework generic de test clonat |
| Module ATF generice | Playwright/reporting/adnotări/utilitare fără contract, locator sau date de business Shop/AutoEx |

Shop are suite separate pentru E2E, component și API audit; suita componentă
selectează și pachete de support. Închiderea trebuie să facă descoperirea
testelor intenționată și să păstreze două categorii logice: E2E și Component/API.

- Fixture-urile și harness-urile strict de test rămân în `src/test/java`.
- Suportul consumer mutat în `main` trebuie să fie folosit de flow, nu doar să
  reprezinte mecanism de execuție a testului.
- Mutările se fac după rol și dependențe, nu mecanic după numele clasei.

## Închidere

- Inventariază toate clasele E2E, componente/API, suite, fixtures și support din
  ambele module; documentează destinația claselor mutate și dependențele Maven.
- Verifică arhitectura cu teste de componentă/guvernanță: pachetul fiecărei
  clase, suite-to-test membership, dictionary-only Gherkin și excluderea helperelor.
- Rulează suitele componente/API și compilează modulele; rulează E2E-urile numai
  opt-in, cu dovezi de raport pentru varianta selectată.
- Actualizează README-urile ownerilor după implementare; elimină acest feedback
  doar când ambele criterii sunt probate și nu rămâne lucru comun deschis.
