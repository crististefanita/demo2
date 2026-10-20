# ATF Tests — guvernanță whole-reactor

## Overall

- `atf-tests` este modulul de control al proiectului ATF;
- verifică reactorul, boundary-urile Maven/source și gate-urile de arhitectură;
- nu este owner pentru teste de produs;
- nu conține locatori, page objects sau flow-uri Practice Shop;
- adaptorul UI de compatibilitate din
  `src/test/java/atf/ui/tests/support/reporting` nu este test executabil;
- reziduul este urmărit prin [`REP-BOUNDARY-01`](../docs/feedback/README.md) și
  `TEST-ARCH-07`.

```mermaid
flowchart LR
    Root["root POM"] -->|alimentează| Governance["governance"]
    Governance -->|verifică| Checks["architecture + runtime guards"]
    Governance -.->|protejează| Consumers["consumer modules"]
    Consumers -->|folosesc| Libraries["ATF libraries"]
```

<details>
<summary><strong>Legendă — ce guvernează atf-tests</strong></summary>

- root POM și graph-ul de module intră în gate-ul whole-reactor;
- atf-tests verifică limitele, nu implementează flow-uri de aplicație;
- Practice Shop și reporting composition rămân consumatori separați;
- `Checks` grupează verificările de arhitectură, scope și siguranță runtime;
- muchia punctată arată guard-ul aplicat consumatorilor, nu ownership transferat către governance;
- consumatorii folosesc bibliotecile prin dependințe explicite, iar governance-ul
  verifică această direcție fără să devină runtime.
</details>

| Responsabilitate | Owner | Excludere explicită |
|---|---|---|
| reactor, POM și dependency direction | `atf-tests` | nu modifică production code |
| source/package ownership și module placement | `ArchitectureModuleBoundaryTest`, `ArchitectureOwnershipBoundaryTest` | nu deține selectorii unei aplicații |
| documentație relocabilă și surse coerente | `governance.documentation.*`, `AtfSourceReadabilityGovernanceTest` | nu validează conținutul unui singur consumer |
| ownership de constante și coeziune de package | `ConstantsOwnershipGovernanceTest`, `AtfPackageCohesionGovernanceTest` | excepțiile trebuie să fie explicite |
| Practice Shop purchase journey | `atf-test-practice-shop` | nu se copiază aici |
| reporting contract/API/UI composition | `atf-reporting-tests` | nu se mută în governance |

## Suita whole-ATF și reziduul de compatibilitate

```text
src/test/java/atf/api/tests/governance/
├── ArchitectureModuleBoundaryTest.java
├── ArchitectureOwnershipBoundaryTest.java
├── AtfPackageCohesionGovernanceTest.java
├── AtfGovernanceRules.java
├── AtfGovernanceTestSupport.java
├── AtfSourceReadabilityGovernanceTest.java
├── ConstantsOwnershipGovernanceTest.java
├── documentation/
│   ├── DocumentationDiagramGovernanceTest.java
│   ├── DocumentationProseGovernanceTest.java
│   ├── DocumentationReadmeStructureGovernanceTest.java
│   ├── DocumentationStyleGovernanceTest.java
│   ├── DocumentationTarget.java
│   ├── DocumentationTargetGovernanceTest.java
│   ├── DocumentationGovernanceSupport.java
│   └── DynamicPathDocumentationArchitectureTest.java
└── WholeAtfGovernanceSuiteTest.java
```

`WholeAtfGovernanceSuiteTest` este entry point-ul explicit pentru verificarea
întregului reactor. El grupează verificările fără să mute responsabilitatea
testelor de reporting în acest modul:

- `ArchitectureModuleBoundaryTest` — reactor, POM, direcția dependențelor,
  importuri interzise, output de proces, stare statică și eliminarea
  `atf-execution`;
- `ArchitectureOwnershipBoundaryTest` — placement-ul modulelor, package roots,
  ownership-ul reporting/UI și absența duplicărilor;
- `AtfPackageCohesionGovernanceTest` — coeziunea package-urilor de producție și
  test, cu excepții revizuite explicit;
- `AtfSourceReadabilityGovernanceTest` — importuri duplicate și invocații
  fully-qualified care ascund intenția testului;
- `ConstantsOwnershipGovernanceTest` — vocabularul de protocol, rute și
  boundary-ul reporting standalone;
- `governance.documentation.DynamicPathDocumentationArchitectureTest` — documentație
  relocabilă, fără căi de mașină sau nume de artifact generate concret;
- `governance.documentation.DocumentationReadmeStructureGovernanceTest` — un titlu,
  orientare `Overall`, `<details>` echilibrate și linkuri locale rezolvabile în
  toate README-urile și în `docs/ATF_Architecture.md`;
- `governance.documentation.DocumentationStyleGovernanceTest` — stil semantic ATF:
  fence-uri tipizate, headings fără salturi, bullets `-` și fără font/CSS/HTML
  prezentational în README-uri și în documentul arhitectural canonic;
- `governance.documentation.DocumentationProseGovernanceTest` — limitează proza
  rapid scan la cel mult trei propoziții pe paragraf și două paragrafe dense
  consecutive; responsabilitățile, limitele și relațiile dense trebuie mutate în
  bullets, tabele, diagrame sau `<details>`;
- `governance.documentation.DocumentationTargetGovernanceTest` — exemplu
  plug-and-play cu `@DocumentationTarget("atf-test-practice-shop/README.md")`; aceeași clasă
  poate valida orice README prin toate modulele de documentație;
- `governance.documentation.DocumentationDiagramGovernanceTest` — orientare `LR`,
  legendă imediată, fence-uri echilibrate și relații etichetate în README-urile
  non-root;
- `governance.documentation.DocumentationGovernanceSupport` — vocabular comun pentru
  diagnostice: cod, fișier, linie, regulă și remediere. Nu ascunde excepții și nu
  transformă un feedback în „pass”.

Fiecare abatere de documentație trebuie să indice dinamic: ce fișier și linie au
fost găsite, ce regulă este încălcată și ce modificare minimă o poate remedia.
Un README poate rămâne intenționat roșu în timpul reconcilierii; suita trebuie să
facă gap-ul reproductibil, nu să-l îngroape într-o excepție.

Testele de guvernanță sunt contract protejat: nu se modifică, dezactivează sau
relaxează pentru a transforma un README roșu în pass. Se schimbă numai pe baza
unui feedback direct care modifică regula, ownerul, criteriul sau dovada cerută;
în caz contrar se repară documentul țintă.

Pentru o verificare punctuală, se schimbă doar valoarea adnotării:

```java
@DocumentationTarget("atf-reporting/README.md")
class ReportingReadmeGovernanceTest {
}
```

Testul țintit apelează aceleași validatoare ca suita whole-ATF: structură,
stil, proză, diagrame, relocatabilitate și nume de artifacte.

Astfel, `atf-reporting-tests` nu mai deține verificări care inspectează întregul
repository; păstrează doar boundary-ul API/UI propriu compoziției reporting.

<details>
<summary><strong>Ținta de curățare urmărită în feedback</strong></summary>

- `atf-tests` trebuie să rămână cu guard-ul whole-reactor și suportul strict
  necesar acestuia;
- adaptoarele de reporting/UI se mută sau se elimină numai după dovadă de
  boundary și după verificarea consumerului canonical;
- o dependență test-scope existentă nu este declarată „eliminată” doar pentru
  că ownership-ul dorit este descris în tabel;
- orice schimbare se închide prin POM, import scan și testul de arhitectură.

</details>

## Module consumer separate

Testarea site-ului `https://practicesoftwaretesting.com` este izolată în
`atf-test-practice-shop`. Acolo sunt YAML-ul, configurația checkout-ului,
locators, page objects, flow-urile și testele component/E2E. Flow-ul principal
este guest purchase complet și nu cere un secret; login-ul opțional folosește
doar un fișier extern injectat explicit.

```text
atf-test-practice-shop/src/
├── main/java/atf/ui/practiceshop/
│   ├── api/         client, request factory și screen API flows
│   ├── config/      records typed încărcate din YAML
│   ├── locators/    singurul owner al selectorilor aplicației
│   ├── pages/       page objects
│   ├── flow/        pași de business compuși
│   └── support/     context și evidence adapters
├── test/java/atf/ui/tests/practiceshop/
│   ├── component/       teste rapide de module
│   ├── api/component/   variații fake pozitive/negative
│   ├── api/integration/ precondiție login live, opt-in
│   └── e2e/             purchase journey și lifecycle report/browser
└── test/resources/atf-practice-shop.yaml
```

Nu se adaugă clase Practice Shop în `atf-tests` sau `atf-reporting-tests`. Dacă
un test verifică rendererul/raportul, aparține reporting composition; dacă
verifică aplicația externă, aparține modulului Practice Shop.

## Rulare și staging

```text
mvn -pl atf-tests -am "-Dtest=WholeAtfGovernanceSuiteTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
mvn -pl atf-tests -am test
mvn -pl atf-test-practice-shop -am -Dtest=PracticeShopArchitectureTest,PracticeShopConfigurationTest,PracticeShopCheckoutConfigurationTest test
mvn -pl atf-test-practice-shop -am -Dtest=PracticeShopPurchaseE2eTest test
```

Build-urile se fac într-un staging extern (`${ATF_STAGING_ROOT}/...`), fără a
opri execuții active. Se sincronizează numai fișierele aprobate, după recitirea
live și reconcilierea modificărilor paralele; nu se copiază `target`, `artifacts`
sau cache-uri peste live. Procedura normativă este
[`docs/ATF_Build_And_Execution_Safety.md`](../docs/ATF_Build_And_Execution_Safety.md).

## Regula de ownership

```text
whole reactor / module graph / architecture gates → atf-tests
Practice Shop config / locators / pages / flows / E2E → atf-test-practice-shop
reporting contracts / Extent composition → atf-reporting-tests
renderer / HTML / CSS / public reporting API → atf-reporting
```

Regulile generale OOP, locator boundary și layering sunt în
[`docs/README_Atf_BestPractices.md`](../docs/README_Atf_BestPractices.md), iar
statusul mutării este în [`docs/feedback/README.md`](../docs/feedback/README.md).
