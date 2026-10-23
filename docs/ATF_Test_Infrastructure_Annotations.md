# ATF — adnotări de infrastructură pentru teste

Acest document descrie API-ul implementat în `atf-test-kit` și aplicarea lui
de către consumatori. Kit-ul furnizează declarații și rezolvare neutră;
consumatorul deține resursele, execuția și publicarea dovezilor.

## Pe scurt

| Axă | API / package | Responsabilitate |
|---|---|---|
| Nivel și tag JUnit | `AtfUnitTest`, `AtfComponentTest`, `AtfIntegrationTest`, `AtfE2eTest` | un singur nivel executabil per clasă |
| Canal | `channel.AtfTestChannel`, `TestChannel`, `AtfTestChannelResolver`, `TestChannelPlan` | selecție `UI`, `API` sau `UI_API` și verificarea suportului |
| Mesaje consolă | `AtfConsoleMessages`, `console.AtfConsoleMessagePolicyResolver` | politică typed `INHERIT`, `DETAILS`, `SUMMARY`, `OFF`; `atf-console` o aplică peste YAML |
| Output UI | `output.UiReportSubdirectory`, `ProjectRelativeUiReportDirectory`, `ReportOutputDirectory` | director relativ la baza UI sau la proiect |
| Setup API | `setup.api.ApiScenarioSetup`, `ApiTestRef` | metode exacte sau familii definite de consumator |
| Documentație | `documentation.DocumentationTarget` și containerul repeatable | documente selectate pentru validatoarele consumatorului |

Prefixul comun este `atf.api.testkit`. Modulele ATF consumă kit-ul în test
scope; rendererul Reporting rămâne utilizabil independent.

## Compoziție și roluri

```mermaid
flowchart LR
    Declaration[Adnotare pe test] -->|rezolvă| Plan[Selecție typed]
    Plan -->|verifică suportul| Consumer[Adaptorul consumatorului]
    Consumer -->|execută| Test[Test și validări]
    Test -->|publică dovezi| Reporting[Reporting]
```

<details>
<summary><strong>Legendă — cine decide și cine execută</strong></summary>

- TestKit deține vocabularul și rezolvarea selecției.
- Consumatorul furnizează rădăcina proiectului, baza UI, catalogul API și
  capabilitățile disponibile; deschide și închide resursele.
- Testul verifică rezultatul observat prin contractele domeniului.
- Reporting proiectează dovezile și verdictele primite prin porturile sale.
- Adnotările nu acordă permisiuni pentru registration, purchase sau apeluri
  live; aceste politici rămân explicite la consumator.

</details>

## Nivel, canal și tag-uri

| Declarație | Efect |
|---|---|
| `@AtfUnitTest` | nivel unit și `@Tag("unit")` |
| `@AtfComponentTest` | nivel component și `@Tag("component")` |
| `@AtfIntegrationTest` | nivel integration și `@Tag("integration")` |
| `@AtfE2eTest` | nivel E2E și `@Tag("e2e")` |
| `@AtfTestChannel(UI)` | canal selectat pentru adaptor; nu adaugă un tag JUnit |
| Tag-uri suplimentare JUnit | selecție locală definită de consumator |
| Etichete Extent / Categories | metadata publicată de consumator; nu rezultă automat din resolverul canalului |

Nivelul selectează stratul piramidei; canalul selectează tipul execuției.
Auditul browser al unui raport API are canalul `UI`, deoarece verifică pagina
raportului; setup-ul API rămâne o precondiție separată.

```java
import atf.api.testkit.AtfE2eTest;
import atf.api.testkit.channel.AtfTestChannel;
import atf.api.testkit.output.ProjectRelativeUiReportDirectory;

import static atf.api.testkit.channel.TestChannel.UI;

@AtfE2eTest
@AtfTestChannel(UI)
@ProjectRelativeUiReportDirectory("artifacts/reports/ui/e2e")
class ReportAuditTest {
    // Composition root-ul consumatorului aplică selecțiile înaintea execuției.
}
```

Exemplul prezintă declarațiile; consumatorul trebuie să le citească și să le
aplice. Kit-ul nu înregistrează automat o extensie JUnit sau un browser.

### Rezolvarea canalului

| Situație | Comportament implementat |
|---|---|
| Clasă concretă și meta-adnotări | toate declarațiile acelui nivel sunt analizate |
| Valori identice pe același nivel | aceeași selecție efectivă |
| Valori diferite pe același nivel | `IllegalStateException` cu tipul și valorile conflictuale |
| Fără declarație locală | căutare în superclass, de la cel mai apropiat |
| Override pe subclasă | selecția subclasei precedă declarația părintelui |
| Cicluri între meta-adnotări | traversare delimitată prin tipurile vizitate |
| Interfețe și clase enclosing | nu sunt surse implicite de selecție |
| Fără declarație în ierarhie | `Optional.empty()`; adaptorul decide compatibilitatea legacy |
| Canal nesuportat de adaptor | `TestChannelPlan.requireSupported(...)` eșuează înaintea resurselor |

`TestChannelPlan` este imutabil și păstrează canalul și clasa care l-a declarat.
Contractul este per clasă; nu există override de canal per metodă.

### Consumatorii actuali

| Consumator | Aplicare |
|---|---|
| Shop E2E | acceptă `UI` și `UI_API`; transformă selecția în profil local; refuză conflictul cu un profil explicit diferit |
| Shop API audit | cere canal `API` înaintea instalării capturii apelurilor |
| Reporting Tests | composition root-ul UI refuză un canal declarat diferit de `UI` |

În Shop, `UI_API` cere binding nevid, inspector activ și allowlist înaintea
browserului. `UI` elimină selecția dovezilor API; YAML-ul păstrează controlul
capturilor și permisiunilor externe.

### Politica mesajelor de consolă

`@AtfConsoleMessages` este o declarație per clasă, neutră față de formatter și
stdout. `AtfConsoleTestExtension` rezolvă politica înaintea testului; adaptorii
UI/API reutilizează sink-ul activ prin `AtfConsoleTestContext.configuredSink()`.
Astfel, evenimentele rămân în același formatter și nu se dublează.

```java
@AtfConsoleMessages(AtfConsoleMessages.Policy.DETAILS)
@ExtendWith(AtfConsoleTestExtension.class)
class ApiContractTest { }
```

| Politică | Efect |
|---|---|
| `INHERIT` | păstrează `reporting.console` din YAML și fallback-ul existent |
| `DETAILS` | păstrează lifecycle-ul și activează detaliile dacă consola globală este enabled |
| `SUMMARY` | păstrează lifecycle-ul/verdictul și suprimă evenimentele detail/code |
| `OFF` | suprimă proiecția pentru clasa curentă |

Dezactivarea YAML (`reporting.console.enabled: false`) rămâne autoritară; nici
`DETAILS` nu o reactivează. Declarațiile meta-adnotate sunt acceptate,
subclasa poate suprascrie superclass-ul, iar politicile diferite la același
nivel eșuează înaintea execuției. Fără adnotare, comportamentul nu se schimbă.

- `@TestFactory` nu primește callbacks de lifecycle pentru fiecare copil; folosește `AtfConsoleTestContext.dynamicTest(owner, name, body)` când ai nevoie de policy și verdict per copil.

## Directoare UI cu ancore distincte

Consumatorul apelează `ReportOutputDirectory.resolve(projectRoot, uiReportBase, owner)`.
Resolverul calculează și verifică destinația; crearea directorului și publicarea
fișierului aparțin consumatorului.

| Declarație | Destinație |
|---|---|
| `@UiReportSubdirectory("a/b")` | `<ui-report-base>/a/b` |
| `@ProjectRelativeUiReportDirectory("a/b")` | `<project-root>/a/b` |
| Fără declarație sau owner `null` | baza UI primită, absolută și normalizată |

- Clasa concretă precedă superclass-ul; se traversează și meta-adnotările.
- Selecțiile identice sunt acceptate; declarațiile diferite la același nivel
  sunt conflict, inclusiv cele cu ancore diferite.
- Valorile goale, URI, UNC, absolute, drive-relative și ieșirea din ancoră
  sunt respinse; rezultatul nu poate fi însăși ancora.
- Strămoșii existenți sunt verificați prin `toRealPath`; proba symlink Windows
  are nevoie de un mediu care permite crearea linkului.
- Kit-ul nu descoperă proiectul și nu reordonează opțiunile YAML.

Reporting Tests caută mai întâi rădăcina reactorului în strămoșii resursei UI
încărcate, prin markerul `atf-reporting/pom.xml`, apoi în strămoșii directorului
de pornire; absența markerului este eroare explicită. Furnizează baza
`<project-root>/artifacts/reports/ui`. În această compoziție, acea bază înlocuiește
directorul UI încărcat din YAML; `atf.ui.report` selectează inputul auditului.

E2E-urile Reporting declară directorul project-relative
`artifacts/reports/ui/e2e`. În staging, proiectul este checkout-ul extern;
adnotarea nu redirecționează output-ul în proiectul live.

## Setup API și documentație

| Declarație | Ce oferă kit-ul | Ce aplică consumatorul |
|---|---|---|
| `ApiScenarioSetup(tests = ...)` | lista de referințe exacte | rezolvă owner/metodă și validează catalogul |
| `ApiTestRef(owner = ..., method = ...)` | referință typed la clasa și metoda selectată | verifică existența și executabilitatea |
| `ApiScenarioSetup(families = ...)` | nume de familii | fallback când selecția exactă lipsește |
| `DocumentationTarget("module/README.md")` | declarație repeatable RUNTIME pe clasă | verifică path-ul și execută validatoarele |

În Reporting Tests, referințele exacte au prioritate față de familii;
catalogul, mock-ul și executarea rămân în modulul consumator.
`ApiScenarioSelection` rămâne model neutru în Core; declarațiile de adnotare
sunt în TestKit, fără dependență Core main către kit.

`ApiScenarioSetup` și `DocumentationTarget` nu folosesc resolverele
compoziționale ale canalului/output-ului. Nu se deduce pentru ele un suport
generic de meta-adnotări, superclass sau override per metodă.

## Probe și limite de acceptanță

| Boundary | Probe existente | Limită |
|---|---|---|
| Nivel / tag | `AtfTestLayerArchitectureTest` | clasifică sursele de teste; nu certifică output-ul Extent |
| Consolă | `AtfConsoleMessagePolicyResolverTest`, `AtfConsoleMessagePolicyTest`, Shop E2E/API și API resource tests | policy, precedență, conflict, override global și consumatori deterministici; concurența/lifecycle dinamic rămâne o probă separată |
| Canal | `AtfTestChannelResolverTest`, `ShopE2eTestSupportTest`, `ShopApiCallAuditPreflightTest`, `ShopArchitectureTest` | rezolvarea UI/API/UI_API, verificarea suportului și preflight înainte de I/O; contractul canalului este închis. Paritatea raportului și ownership-ul verdictelor aparțin Reporting, nu resolverului |
| Output | `ReportOutputDirectoryTest`, `UiReportSubdirectoryTest`, `AtfUiConfigurationLoaderTest` | ancore, inheritance/compoziție, cicluri, conflicte, path-uri invalide și working directory extern; containment-ul este probat prin symlink când e permis și directory junction ca fallback Windows |
| Setup | `ApiScenarioSetupCatalogTest`, `ApiScenarioManifestTest`, `ApiSelectionMockPublisherComponentTest` | selecție și compoziție locală, fără servicii externe |
| Documentație | `DocumentationTargetGovernanceTest` și suita whole-ATF | declarația repeatable selectează aceleași validatoare |
| Browser local | `UiThenDisclosureSanityTest`, `ApiReportRenderedEvidenceComponentTest`, `SelectedApiTestsReportUiTest` | disclosure, reconciliere mock/câmpuri/query și navigarea fazei; nu certifică launcherul IntelliJ sau întreaga integrare API/UI_API |

Singura lacună de infrastructură rămasă — lansarea reală din IntelliJ și
destinația raportului IDE — este urmărită în
[feedback-ul infrastructurii](feedback/ATF_Test_Infrastructure_Annotations_Feedback.md).
Selecția canalelor este un contract închis; redeschiderea cere un consumer nou
sau o regresie. Paritatea UI/API, structura evidence-ului și verdicturile sunt
urmărite la ownerul lor în [feedback-ul Reporting](feedback/ATF_Reporting_Feedback.md).
Pentru dovezi și layout, contractele normative rămân
[Reporting](ATF_Reporting_Contract.md), [display/layout](ATF_Reporting_Display_Contract.md)
și [captura evidence](ATF_Evidence_Capture_Contract.md).
