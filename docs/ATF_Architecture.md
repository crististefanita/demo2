# Arhitectura ATF Java

ATF Java este împărțit în module cu responsabilități distincte. Direcția
dependențelor este de la scenarii spre infrastructură; infrastructura nu
cunoaște rutele sau intenția unui test de produs.

~~~mermaid
flowchart TD
    Tests[atf-tests<br/>unitare și contract] --> Module[atf-execution<br/>rute și scenarii]
    Tests --> Validation[atf-validation<br/>contracte și verdicturi]
    Tests --> Reporting[atf-reporting<br/>artifact Extent]
    Module --> Core[atf-core<br/>transport și politici]
    Module --> Validation
    Validation --> Core
    Reporting --> Core
    Core --> Ports[porturi neutre față de renderer]
~~~

## Responsabilități

| Modul | Deține | Nu deține |
|---|---|---|
| atf-core | request/response, transport, configurație, autentificare, retry, redaction, status și porturi | HTML, CSS, Extent, JUnit, rute de produs |
| atf-validation | contracte response, reguli, evaluator, verdict și publicare prin port neutru | HTTP, Extent, HTML, CSS, lifecycle |
| atf-execution | rute, modele, servicii semantice, lifecycle, fixture-uri și cleanup | validare de contract, markup Extent, stiluri |
| atf-reporting | sesiune, arbore Extent, theme, finalizare HTML, inspector și navigare | apeluri HTTP, decizii de business, verdicturi inventate |
| atf-tests | teste unitare, de componentă, contract și scenarii de verificare | implementarea framework-ului și date externe obligatorii |

Regula practică: dacă un comportament poate fi verificat cu un fake și fără
rețea, testul lui aparține în atf-tests; dacă dovedește comportamentul unui
serviciu real, aparține scenariilor de produs.

## Fluxul unei cereri

~~~mermaid
sequenceDiagram
    participant T as Test
    participant M as Modul
    participant E as Executor
    participant H as Transport
    participant V as atf-validation
    participant R as Raport

    T->>M: intenție semantică
    M->>E: ApiRequest
    E->>H: schimb HTTP
    H-->>E: ApiResponse + durată
    E->>R: trace și JSON sigur
    T->>V: contract + response
    V->>R: assertions prin ValidationSink
    R->>R: închide request-ul după validare
~~~

Faptele transportului, satisfacerea contractului și verdictul scenariului sunt
lucruri diferite:

~~~text
schimb HTTP finalizat  !=  contract satisfăcut  !=  scenariu trecut
~~~

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

## Lifecycle și ownership

~~~mermaid
flowchart TD
    Scenario[Scenariu] --> Setup[SETUP]
    Scenario --> Body[TEST BODY]
    Scenario --> Teardown[TEARDOWN]
    Setup --> SR[Request]
    SR --> SV[Validation sibling]
    Body --> BR[Request]
    BR --> BV[Validation sibling]
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

