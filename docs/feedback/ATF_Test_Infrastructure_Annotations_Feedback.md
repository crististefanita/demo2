# ATF Test Infrastructure — adnotări reutilizabile și directoare de raport

Contractele neutre de output, setup API și documentație sunt implementate în
TestKit și consumate de Reporting Tests / guvernanță. Build-ul și gate-urile au
rulat în staging extern; promovarea în live și probele de mediu rămân urmărite
separat. TestKit nu depinde de renderer, browser sau aplicația Shop.

## Stare și probe

| ID | Stare verificată | Dovadă / limită |
|---|---|---|
| ANN-OUTPUT-01 | Implementat în staging | `ReportOutputDirectoryTest`, `UiReportSubdirectoryTest`: ancoră UI, absență, compoziție, moștenire, conflicte și căi invalide; consumer neutru fără Reporting Tests. |
| ANN-PROJECT-02 | Implementat în staging | Cele șase E2E Reporting folosesc `ProjectRelativeUiReportDirectory`; E2E local a produs fișierul sub rădăcina proiectului de staging. IntelliJ cu alt working directory nu a fost probat direct. |
| ANN-SETUP-03 | Implementat în staging | Declarațiile JDK-only sunt în TestKit; catalogul și selecția exactă rămân în Reporting Tests. `ApiScenarioSetupCatalogTest` și `ApiScenarioManifestTest` verzi. |
| ANN-DOC-04 | Implementat în staging | Declarația repeatable și containerul sunt în TestKit; validatorul rămâne în ATF Tests. `WholeAtfGovernanceSuiteTest` verde. |
| ANN-BOUNDARY-05 | Decizie aplicată parțial | `AtfModuleTest`, adnotările Reporting, auth și harness-ul Shop rămân la ownerii lor; graful Maven a compilat. Nu am introdus un SPI nou fără doi consumatori reali. |

Acest fișier este ownerul unic al extracției transversale; feedback-ul
Reporting doar trimite aici. Probele de symlink/junction pe Windows au fost
omise de runner când privilegiul de creare lipsește; implementarea verifică
strămoșii existenți prin real-path, dar acea probă cere și un runner privilegiat.
Perimetrele tehnice sunt centralizate în [registrul activ](ATF_Test_Changes_Feedback.md)
și [registrul verificat](../ATF_Test_Changes_Baseline.md); un path schimbat are
un singur owner eligibil pentru baza și data gate-ului.

Canalele executabile `UI`, `API` și `UI_API`, aplicarea lor prin adaptoare și
matricea de probe sunt analizate separat în
[feedback-ul adnotărilor de canal](ATF_Test_Channel_Annotations_Feedback.md).
Ele nu schimbă ancora output-ului sau ownership-ul setup-ului descris aici.

## Situația codului după extracție

- [Adnotările output](../../atf-test-kit/src/main/java/atf/api/testkit/output/UiReportSubdirectory.java) și [resolverul neutru](../../atf-test-kit/src/main/java/atf/api/testkit/output/ReportOutputDirectory.java) sunt în TestKit. `UiReportSubdirectory` păstrează ancora UI; `ProjectRelativeUiReportDirectory` folosește rădăcina proiectului.
- [Composition root-ul](../../atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/ReportingTestResources.java) descoperă rădăcina prin markerul specific `atf-reporting/pom.xml` și impune apoi directorul standard UI, după încărcarea YAML.
- [Testele consumerului](../../atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/config/output/UiReportSubdirectoryTest.java) și [ale kit-ului](../../atf-test-kit/src/test/java/atf/api/testkit/output/ReportOutputDirectoryTest.java) probează ambele ancore, compoziția, moștenirea și respingerea căilor invalide. Cazul symlink a fost SKIP pe runnerul Windows fără privilegiu.
- Kit-ul nu depinde acum de alte module ATF; [Core](../../atf-core/pom.xml) îl consumă în test scope, iar [Reporting](../../atf-reporting/pom.xml) este o librărie standalone independentă.

### Inventarul adnotărilor și decizia de ownership

<details>
<summary>Toate familiile de adnotări ATF găsite în declarațiile Java live</summary>

| Adnotare / familie | Owner actual | Decizie propusă și limită |
|---|---|---|
| `AtfUnitTest`, `AtfComponentTest`, `AtfIntegrationTest`, `AtfE2eTest` | TestKit | Sunt deja generice; se păstrează, fără adnotări noi care dublează layer-ele |
| `UiReportSubdirectory` | TestKit, `output` | Mutată cu rezolvarea neutră; aceeași ancoră UI |
| `ProjectRelativeUiReportDirectory` | TestKit, `output` | Contract distinct, ancoră în proiect, folosit explicit de E2E Reporting |
| `ApiScenarioSetup`, `ApiTestRef` | [TestKit / setup.api](../../atf-test-kit/src/main/java/atf/api/testkit/setup/api/ApiScenarioSetup.java) | Declarații neutre mutate din Core; catalogul concret rămâne în Reporting Tests |
| `DocumentationTarget`, `DocumentationTarget.Container` | [TestKit / documentation](../../atf-test-kit/src/main/java/atf/api/testkit/documentation/DocumentationTarget.java) | Declarație repeatable mutată; validatorii și documentele selectate rămân în guvernanță |
| `AtfModuleTest` + `AtfModuleTestCondition` | [Reporting Tests](../../atf-reporting-tests/src/test/java/atf/api/reporting/test/AtfModuleTest.java) | Candidat condiționat: importă `AtfModule` și loaderul Core; nu se mută mecanic în kit |
| `UiExtentSectionAudit`, `UiExtentReportContract`, `UiFailureReportAudit` | Reporting, contract public | Rămân în Reporting: secțiuni, tab-uri, checks și surfaces sunt vocabularul librăriei, nu al platformei test-only |
| `ShopPageContract` | Practice Shop, main | Rămâne local: depinde de `ShopScreen` și `ShopControl`, fiind contract al paginilor aplicației |
| `AuthenticatedE2e`, `RequiresAuthenticatedFixture`, `RequiresLiveAuthenticatedApi` | [ShopTestSupport](../../atf-test-practice-shop/src/test/java/atf/ui/tests/practiceshop/support/ShopTestSupport.java) | Rămân adaptoare Shop; folosesc configurația, fixture-urile și condițiile aplicației |
| `AllowsRegistrationPerRun` | ShopTestSupport | Opt-in-ul pentru efect extern nu se ascunde într-o adnotare generică sau compusă |
| `WithE2eHarness` | [ShopE2eTestSupport](../../atf-test-practice-shop/src/test/java/atf/ui/tests/practiceshop/support/ShopE2eTestSupport.java) | Se pot analiza politici neutre separat; lifecycle-ul harness-ului, auth și profilurile Shop rămân locale |
| `ShopApiCallComponentTest` | Shop API tests | Compoziție locală peste layer-ul generic; extensia și auditul apelurilor Shop nu aparțin kit-ului |

</details>

Reutilizarea cere un contract neutru și consumatori reali, nu doar o adnotare
cu parametri. JUnit păstrează ownership-ul adnotărilor sale; nu le clonăm în ATF.

## Două ancore diferite, nu o redenumire

Numele recomandat este `ProjectRelativeUiReportDirectory`: explică proiectul,
relativitatea și faptul că valoarea este un director de output UI, nu un fișier.

| Declarație | Bază | Rezultat propus |
|---|---|---|
| `@UiReportSubdirectory("a/b/c")` | directorul UI ales de compoziție | `<ui-report-base>/a/b/c` |
| `@ProjectRelativeUiReportDirectory("a/b/c")` | rădăcina proiectului | `<project-root>/a/b/c` |
| Fără adnotare | politica implicită/configurată a consumerului | Comportamentul existent, fără relocare incidentală |
| Ambele pe același nivel de declarație | Ambiguu | Eroare explicită înainte de publicare |

Exemplu folosit de E2E Reporting:

```java
@AtfE2eTest
@ProjectRelativeUiReportDirectory("artifacts/reports/ui/e2e")
class SelectedApiTestsReportUiTest {
    // Setup-ul și testele existente rămân neschimbate.
}
```

- Nu se adaugă implicit încă un prefix `artifacts/reports/ui` valorii project-relative.
- Nu se schimbă sensul lui `UiReportSubdirectory`; o mutare de package nu autorizează schimbarea ancorei.
- Noua adnotare nu implică automat layer-ul E2E; rămâne compozabilă cu layer-ul potrivit.
- Nu se introduc în paralel alte nume pentru același contract sau o booleană UI nouă.

## Compoziția propusă

```mermaid
flowchart LR
    Declaration["Adnotare"] -->|declară| Spec["Spec tipizat"]
    Spec -->|ghidează| Resolve["Resolver"]
    Context["Context capturat"] -->|ancorează| Resolve
    Resolve -->|validează| Output["Path de output"]
    Output -->|configurează| Publisher["Publisher consumer"]
```

<details>
<summary><strong>Legendă — contract generic și adaptor de consumer</strong></summary>

- TestKit deține declarația, ancora tipizată, contextul și rezolvarea neutră; publisherul rămâne la consumer.
- O singură politică rezolvă ambele ancore, fără două implementări paralele ale validării căilor.
- Contextul primește rădăcina proiectului și baza UI explicit, capturate o dată; resolverul nu recitește `user.dir` la fiecare test.
- Un record de tip `ReportDirectorySpec` și un enum cu ancorele `PROJECT_ROOT` / `UI_REPORT_BASE` sunt potrivite; numele helperilor nu sunt obligații de a introduce clase fără responsabilitate.
- Pachete candidate: `atf.api.testkit.output`, `atf.api.testkit.setup.api`, `atf.api.testkit.documentation`; cel mult opt clase per pachet, fără umflarea rădăcinii kit-ului.
- Resolverul întoarce un `Path`; nu creează directoare, nu generează HTML și nu pornește browserul.

</details>

### Contractul rădăcinii și al configurației

- Pentru ATF, `<project-root>` este rădăcina reactorului ATF; nu directorul modulului curent, directorul YAML sau rădăcina Git a unui eventual monorepo părinte.
- Alt proiect consumator furnizează propria rădăcină; kit-ul nu caută obligatoriu `atf-reporting/pom.xml` și nu importă un helper din testele de guvernanță.
- Maven și IntelliJ trebuie să obțină aceeași ancoră; un root explicit este autoritar, iar discovery-ul de fallback trebuie delimitat și probat, nu ghicit după numele proiectului.
- În staging extern, proiectul este copia de staging; nicio adnotare relativă nu trebuie să redirecționeze scrierea în checkout-ul live.
- Compoziția stabilește separat baza UI și relația ei cu YAML; suprascrierea actuală din `ReportingTestResources` trebuie decisă explicit, nu transportată ca efect ascuns în resolverul generic.
- Adnotarea project-relative selectează output-ul direct; adnotarea subdirectory selectează numai un copil al bazei UI. Absența lor nu schimbă automat precedence-ul existent.
- `atf.ui.report` rămâne selector de raport de intrare pentru audit; nu se reutilizează ca override al directorului de ieșire.

### Declarații compuse și căi invalide

| Arie | Contract de implementat |
|---|---|
| Retention / target | RUNTIME; TYPE și ANNOTATION_TYPE pentru compoziție; documentare explicită a moștenirii |
| Precedență | Declarația clasei concrete înaintea celei moștenite; conflictele la același nivel sunt respinse, nu rezolvate prin ordinea reflection |
| Meta-adnotări | Rezolvare delimitată, cu detectarea ciclurilor; nu se presupune că `getAnnotation` vede automat compoziția |
| Path | Director relativ nevid, normalizat, conținut în ancora selectată; fără URI, cale absolută, UNC sau formă Windows drive-relative |
| Filesystem | Containment-ul lexical nu dovedește siguranța unui symlink/junction; se verifică și strămoșul existent înaintea scrierii, prin boundary-ul IO potrivit |
| Scope / concurență | Context imutabil; directoarele pot fi comune, dar publisherul păstrează identitatea fișierelor și nu suprascrie alt run |
| Eroare | Diagnostic cu owner, adnotare și ancoră; nicio publicare parțială înainte de validare |

Rădăcina și baza UI sunt concepte distincte chiar când, într-un fixture,
se întâmplă să indice același director.

## Limite pentru celelalte extracții

- `ApiScenarioSetup` și `ApiTestRef` sunt declarații JDK-only; se păstrează selecția exactă, prioritatea `tests()` față de families și validarea referințelor.
- Catalogul concret, mock-urile și publicarea scenariilor API rămân la consumer; mutarea adnotărilor nu mută automat `ApiScenarioSetupCatalog` în kit.
- Mutarea din Core cere inventarierea importurilor și o decizie de compatibilitate pentru consumatori externi; nu se introduce un alias Core care depinde de TestKit în producție.
- `AtfModuleTest` nu poate aduce Core în kit: Core consumă deja kit-ul în test scope, deci dependența inversă creează un ciclu în reactor.
- Pentru acel gate se păstrează adaptorul Core local sau se proiectează separat un SPI neutru; nu se dublează enum-ul `AtfModule` și nu se înlocuiește siguranța typed cu string-uri doar pentru a muta un fișier.
- `DocumentationTarget` poate fi extras împreună cu containerul său repeatable; lista documentelor, validatoarele și diagnosticele rămân în `atf-tests`.
- Contractele publice Reporting rămân utilizabile standalone, fără o dependență de kit; opțiunile business și permisiunile pentru efecte externe rămân explicite la consumer.

## Probe și pași de implementare

<details>
<summary>Matricea de verificare pentru migrare, fără reducerea acoperirii</summary>

| Nivel | Probe cerute |
|---|---|
| Unit — output | Ambele ancore cu baze diferite, lipsa adnotării, spații/Unicode și normalizare; modelul/contextul nu se modifică |
| Unit — selecție | Direct, moștenit și compus; conflicte și cicluri; fără prioritate accidentală dată de reflection |
| Unit / component — negativ | Gol, absolut, UNC, drive-relative, URI, traversal; symlink/junction cu path nou și strămoș existent |
| Component — consumer | Reporting aplică path-ul rezultat, păstrează input-ul auditului și politica screenshot; un consumer neutru dovedește reutilizarea fără dependency pe Reporting Tests |
| Integration — launcher | Maven din root/modul și IntelliJ dintr-un working directory diferit produc aceeași ancoră, inclusiv în staging extern |
| API setup / docs | Referințe exacte, familii fallback, erori de selecție; `DocumentationTarget` repeatable păstrează aceleași documente și aceiași validatori |
| Guvernanță | Graful Maven fără ciclu, test scope păstrat, Reporting standalone și limita de opt clase; nicio relaxare a guard-urilor existente |
| E2E / MCP | Adnotarea nouă pe E2E Reporting; fișier în directorul declarat, raport deschis în MCP, Gherkin, evidence, JSON, screenshot inline și navigare păstrate |

</details>

- [x] ANN-OUTPUT-01: declarația și resolverul neutru sunt în TestKit; consumerul păstrează adaptorul către `UiConfiguration`.
- [x] ANN-PROJECT-02: E2E Reporting folosesc ancora explicită în proiect; E2E local a produs raportul în directorul declarat.
- [x] ANN-SETUP-03: declarațiile sunt mutate fără catalog sau mock în kit.
- [x] ANN-DOC-04: declarația repeatable este mutată, iar guvernanța a trecut.
- [x] ANN-BOUNDARY-05: ownership-ul contractelor Reporting/Shop rămâne local, fără SPI nou și fără ciclu Maven.
- [x] Scope-urile `testChanges` au baseline, expirare, proof și `allowDelete` pentru mutări; governance gate a trecut în staging.
- [x] Build și teste în staging extern, fără oprirea rulărilor; raportul a fost deschis și inspectat prin MCP.
- [ ] Probă symlink/junction pe runner Windows privilegiat și launcher IntelliJ cu working directory diferit.
- [ ] Promovarea normativă în README-urile modulelor este în afara acestui task: utilizatorul a limitat editările documentației la feedback. Nu rescriem README-urile fără instrucțiune nouă.
