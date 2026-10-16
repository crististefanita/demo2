# Arhitectura ATF Java

ATF Java este împărțit în module cu responsabilități distincte. Direcția
dependențelor este de la scenarii spre infrastructură; infrastructura nu
cunoaște rutele sau intenția unui test de produs.

~~~mermaid
flowchart LR
    Validation[atf-validation<br/>contracte și verdicturi] --> Core[atf-core<br/>transport și politici]
    Mock[atf-mock<br/>fake configurabil] --> Core
    Execution[atf-execution<br/>rute și scenarii] --> Validation
    Execution --> Core
    Console[atf-console<br/>proiecție diagnostică] --> Core
    Reporting[atf-reporting<br/>artifact Extent] --> Core
    Ui[atf-ui<br/>adaptor Playwright] --> Core
    DebugUi[atf-debug-ui<br/>loopback read-only]
    Tests[atf-tests<br/>unitare și contract] --> Execution
    Tests --> Mock
    Tests --> Validation
    Tests --> Console
    Tests --> Reporting
    Tests --> Ui
    Tests --> DebugUi
    Tests --> Core
~~~

În această diagramă săgeata înseamnă „modulul din stânga depinde de modulul
din dreapta”. `atf-validation` și `atf-reporting` depind de `atf-core`, dar nu
se depind reciproc; `atf-execution` depinde de `atf-core` și `atf-validation`,
iar `atf-tests` este composition root pentru testele care le folosesc pe toate.

`atf-mock` depinde numai de `atf-core`, iar `atf-debug-ui` este un launcher
read-only; faptul că este pornit de teste sau IntelliJ nu îl transformă într-o
dependență de runtime a rendererului.

## Responsabilități

| Modul | Deține | Nu deține |
|---|---|---|
| atf-core | request/response, transport, configurație, autentificare, retry, redaction, status și porturi | HTML, CSS, Extent, JUnit, rute de produs |
| atf-mock | transport fake configurabil și catalog de răspunsuri deterministe | HTTP real, Extent, JUnit, validare de business |
| atf-validation | contracte response, reguli, evaluator, verdict și publicare prin port neutru | HTTP, Extent, HTML, CSS, lifecycle |
| atf-execution | API layer, rute, modele, servicii semantice, lifecycle și cleanup | fixture-uri de test, validare de contract, markup Extent, stiluri |
| atf-console | formatter pentru evenimentele de consolă, `ConsoleSettings`, YAML, nesting, statusuri și fallback ASCII/UTF-8 | HTML, Extent, apeluri HTTP, verdicturi inventate |
| atf-reporting | sesiune, arbore Extent, theme, finalizare HTML, inspector și navigare | apeluri HTTP, consolă concretă, decizii de business, verdicturi inventate |
| atf-ui | configurație browser și adaptor Java Playwright pentru verificări UI automate | generarea HTML-ului și testele executabile |
| atf-debug-ui | publică read-only un HTML existent pe loopback și furnizează target-ul pentru TAB MCP | renderer, modificarea artifactului, verdicturi și teste executabile |
| atf-tests | `src/main/java` păstrează scenarii și compoziție reutilizabilă; `src/test/java` păstrează teste JUnit executabile, host-uri de raport și adaptoare de test | implementarea framework-ului și date externe obligatorii |

Regula practică: dacă un comportament poate fi verificat cu un fake și fără
rețea, testul lui aparține în atf-tests; dacă dovedește comportamentul unui
serviciu real, aparține scenariilor de produs.

## Fluxul unei cereri

~~~mermaid
sequenceDiagram
    participant T as Test
    participant M as API layer
    participant E as Execution
    participant H as Transport
    participant V as atf-validation
    participant R as Raport

    T->>E: scenario + phase
    T->>M: acțiune API semantică
    M->>E: ApiRequest
    E->>H: schimb HTTP
    H-->>E: ApiResponse + durată + retry history
    E->>R: request + trace + JSON sigur
    T->>V: contract + response
    V->>V: evaluate rules + warning policy
    V-->>T: ValidationResult
    V->>R: validation sibling + aggregate assertions
    E->>R: complete phase + scenario verdict
    R->>R: finalizează HTML autonom
~~~

Faptele transportului, satisfacerea contractului și verdictul scenariului sunt
lucruri diferite:

~~~text
schimb HTTP finalizat  !=  contract satisfăcut  !=  scenariu trecut
~~~

## Profile de transport și boundary-ul acceptance

ATF are două profile de transport, cu scopuri diferite:

| Profil | Implementare | Rețea | Proprietar | Scop |
|---|---|---:|---|---|
| `acceptance` | `AtfMockTransport` (`atf-mock`) | nu | `atf-tests` | raport determinist, contracte, lifecycle și randare |
| `live` | `HttpClientTransport` | da | `ApiLiveReportTest` / consumator | verificarea unui serviciu HTTP disponibil |

`AtfMockTransport` nu este un HTTP server local și nu este un proxy către
`atf.local`. El este un test double din `atf-mock`, injectat explicit în
`ApiModuleFactory`.
URL-ul construit de `ApiExecutor` rămâne în evidence pentru ca raportul să
arate ruta semantică, însă transportul fake nu execută DNS, TLS sau request
network. Această separare este intenționată: raportarea și contractele nu
trebuie să depindă de un serviciu extern.

~~~mermaid
flowchart LR
    Scenario[Acceptance scenario] --> Factory[ApiModuleFactory]
    Factory --> Fake[AtfMockTransport]
    Fake --> Offline[Deterministic ApiResponse]
    Offline --> Report[Extent artifact]

    Live[ApiLiveReportTest] --> FactoryLive[ApiModuleFactory]
    FactoryLive --> Http[HttpClientTransport]
    Http --> Server[Real HTTP endpoint]
    Server --> LiveReport[Live Extent artifact]
~~~

Regula verificabilă este:

~~~text
ApiAcceptanceReportTest -> AtfMockTransport -> offline artifact
ApiLiveReportTest       -> HttpClientTransport -> real endpoint
~~~

Un URL precum `https://atf.local/public/v2/users` dintr-un acceptance report
este metadata a request-ului, nu o afirmație că host-ul a fost contactat.
Disponibilitatea DNS, TLS, autentificarea și contractul serviciului sunt
verificabile numai în profilul `live`.

`ApiLiveReportTest` este opt-in prin `-Datf.liveApi.enabled=true`, iar baza
URL este explicită prin `-Datf.liveApi.baseUri`. Testele acceptance nu citesc
această proprietate și nu trebuie să fie convertite implicit în teste de
rețea.

## Vocabularul rutelor

Rutele nu se scriu ca string-uri repetate în teste. Vocabularul stabil al
căilor este separat de catalogul operațional și de configurația mediului:

~~~mermaid
flowchart LR
    Paths[Vocabular stabil al căilor] --> Catalog[Catalog operațional]
    Catalog --> Operation[Metodă + resursă + operație]
    Catalog --> Auth[Politică de autentificare]
    Config[Configurație mediu] --> Catalog
    Operation --> Executor[Executor API]
~~~

Catalogul descrie operația, metoda, resursa și autentificarea. Configurația
poate suprascrie baza URL sau șabloanele fără ca testele să cunoască detaliile
de mediu.

## Configurație și snapshot

`atf.yaml` este configurația generală a proiectului consumator, nu un asset al
unui modul și nu o configurație citită de fiecare request. Fișierul versionat
conține numai secțiunile `api`, `validation` și `reporting`; loaderul din
`atf-reporting` consumă efectiv numai `reporting.extent`, iar loaderul din
`atf-console` consumă efectiv numai `reporting.console`. Catalogul fake nu mai
este în acest fișier: rutele, răspunsurile, body-urile și fallback-ul sunt în
`atf-mock.yaml`, deținut de `atf-mock`.
API-ul este încă asamblat prin `ApiSettings`/`AtfConfiguration`. Integrarea
completă a tuturor secțiunilor este ToDo-ul F-03 și nu trebuie presupusă ca
deja activă.

În configurația actuală, `api.baseUri`, timeout-ul, retry-ul și auth profile-ul
descriu baseline-ul pentru un composition root care construiește explicit un
transport HTTP. `ApiAcceptanceReportTest` își injectează intenționat
`AtfMockTransport` în cod; simpla prezență a unui `baseUri` în YAML nu
schimbă transportul și nu activează rețeaua. Loaderul `atf-reporting` consumă
efectiv doar `reporting.extent` pentru artifactul HTML; consola nu este
fallback-ul rendererului și nu este încărcată de modulul Extent.

Nu se introduce încă o cheie YAML `transport: live` care să poată schimba
implicit natura testelor. Când F-03 va integra configurația API, selecția
transportului trebuie să fie typed, validată la bootstrap și implicit
`acceptance` în `atf-tests`; `live` trebuie declarat explicit și să rămână
opt-in. Astfel YAML-ul poate centraliza ulterior base URI, timeout și retry,
fără să ascundă diferența dintre un fake și o conexiune reală.

~~~mermaid
flowchart LR
    General[atf.yaml · api/validation/reporting] --> Loader[composition root loader · F-03]
    MockYaml[atf-mock.yaml · routes/responses] --> MockLoader[AtfMockConfigurationLoader]
    MockOverride[atf.mock.config / ATF_MOCK_CONFIG] --> MockLoader
    Env[explicit env/property override] --> Loader
    Loader --> Config[AtfConfiguration validată · ținta]
    Config --> Snapshot[immutable runtime snapshot · ținta]
    Snapshot --> Execution[atf-execution]
    Snapshot --> Validation[atf-validation]
    Snapshot --> Reporting[atf-reporting]
    MockLoader --> MockSnapshot[AtfMockConfiguration]
    MockSnapshot --> MockTransport[AtfMockTransport]
    MockTransport --> Execution
~~~

`AtfRuntimeSnapshot` este boundary-ul typed pentru execuție: păstrează
`ApiSettingsSnapshot` și `ReportingSettings` împreună, fără să introducă YAML,
environment sau Extent în `atf-core`. `ApiModuleFactory` îl proiectează în
executorul API, iar reporting consumă separat politica sa. Astfel un
composition root poate crea două snapshots independente pentru rulări
paralele, iar modificarea ulterioară a configurației de input nu schimbă
executorul deja creat.

Reguli obligatorii:

- `atf-core` deține modelele typed și snapshot-urile, dar nu parsează YAML și
  nu citește filesystem/env; în repository-ul curent loaderul complet aparține
  composition root-ului din `atf-tests`/proiectul consumator;
- loaderul citește configurația o singură dată la bootstrap și eșuează clar la
  chei necunoscute, URI invalid, timeout invalid, root de schema absent sau
  output path nesigur;
- `reporting.extent` rămâne responsabilitatea reporting; timeout, retry, auth,
  routes, redaction și schema roots sunt setări runtime neutre;
- căile relative se rezolvă față de fișierul YAML, nu față de `user.dir` al
  IDE-ului; fallback-ul implicit este permis doar ca compatibilitate temporară;
- secretele nu sunt valori YAML publicate, ci referințe către un provider
  explicit;
- după bootstrap, toate modulele primesc snapshot-uri immutable, iar două
  rulări paralele nu își pot modifica reciproc configurația.

Nu se introduce un modul `atf-config`. `atf-reporting` își păstrează loaderul
specializat pentru `reporting.extent`, iar composition root-ul poate compune
setările API și validation în același bootstrap folosind modelele din core.

## Lifecycle și ownership

~~~mermaid
flowchart TD
    Scenario[Scenariu] --> Setup[SETUP]
    Scenario --> Body[TEST BODY]
    Scenario --> Teardown[TEARDOWN]
    Setup --> SR[Request]
    Setup --> SV[Validation sibling]
    Body --> BR[Request]
    Body --> BV[Validation sibling]
    Teardown --> Cleanup[Cleanup în ordine inversă]
~~~

Un request rămâne deschis până când evidence-ul lui tehnic și validarea asociată
au fost publicate. Validarea este următorul frate direct al request-ului, în
aceeași fază; nu este mutată într-o listă globală și nu este ascunsă într-un
teardown. Mai multe assertions pentru același response formează un singur tabel.

Responsabilități obligatorii:

- executorul publică faptele HTTP și durata;
- validatorul calculează rezultatele și publică tabelul agregat;
- lifecycle-ul închide fazele și aplică precedence-ul verdictului;
- reporting-ul redă structura fără să modifice verdictul;
- cleanup-ul deține resursele create și rulează best effort în ordine inversă.

## Reguli OOP

1. Fațadele coordonează; politicile de status, retry, redaction și agregare sunt
   obiecte mici, pure și testabile.
2. Modelele din atf-core și atf-execution nu cunosc HTML, CSS, selectorii sau
   viewport-ul.
3. Starea de execuție este per sesiune și per scenariu; nu se introduce state
   static mutabil între teste.
4. O interfață apare pentru un seam real: transport, reporter, clock, auth sau
   cleanup. Nu se creează interfețe decorative.
5. Orice componentă care decide simultan transportul, verdictul și layout-ul
   are prea multe responsabilități și trebuie separată.

## Testabilitate

~~~mermaid
flowchart LR
    Unit[unitare<br/>politici pure] --> Component[componentă<br/>module + fake transport]
    Component --> Artifact[artifact<br/>HTML/CSS/JS static]
    Artifact --> MCP[TAB MCP<br/>click și computed styles]
    MCP --> Live[scenarii API<br/>comportament real]
~~~

Fiecare schimbare de raportare care afectează structură, collapse, tab-uri,
font, culoare, scroll sau overflow are nevoie de test de cod și verificare a
artifactului. Un string găsit în HTML nu dovedește că elementul se comportă
corect la click.

