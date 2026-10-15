# Bune practici pentru ATF Java

Aceste reguli urmăresc trei lucruri: cod ușor de întreținut, comportament
testabil și raport care explică execuția fără zgomot.

## Politica limbii proiectului

README-urile sunt singurele documente care pot fi scrise în română, pentru a
fi ușor de urmărit de echipa locală. Restul proiectului folosește exclusiv
engleza:

- codul de producție și codul de test, inclusiv clase, metode, variabile,
  teste, constante și identificatori;
- comentariile inline, Javadoc-ul, TODO/FIXME și mesajele de assertion sau
  de raportare;
- fișierele de configurare și comentariile din YAML, XML și properties;
- documentația care nu este README, inclusiv feedback, arhitectură, ADR-uri,
  requirements și diagramele asociate.

Textele românești existente în aceste zone se traduc în engleză atunci când
sunt întâlnite. Un fixture poate păstra o valoare românească numai dacă testul
verifică explicit un serviciu sau un payload care trebuie să conțină acea
valoare; comentariul și explicația testului rămân în engleză.

## Principii de design

1. O clasă are un motiv dominant de schimbare.
2. Fațadele coordonează; nu ascund politici de retry, verdict sau randare.
3. Modelele și contractele sunt neutre față de Extent și HTML.
4. Starea este per sesiune/per scenariu, nu globală și statică.
5. Endpoint-urile și constantele de rutare nu se repetă în teste.
6. Validările pentru un response sunt agregate într-un singur tabel.
7. Cleanup-ul are ownership clar și rulează în ordine inversă.
8. O interfață se introduce numai pentru o dependență care trebuie înlocuită,
   observată sau izolată.

## Coeziunea pachetelor

Un package de producție nu ar trebui să depășească, ca regulă de bună practică,
8 clase concrete, record-uri sau interfețe. Pragul este un semnal obligatoriu
de review pentru coeziune și ownership:

- peste 8 clase se caută mai întâi responsabilități distincte, fațade prea mari
  și dependențe amestecate;
- împărțirea se face după responsabilitate semantică, nu prin directoare create
  artificial doar pentru a coborî numărul;
- regula se aplică separat pe fiecare package de producție din `src/main/java`;
- testele, codul generat și asset-urile vendor nu intră în numărătoarea de
  producție;
- o excepție trebuie documentată și protejată printr-un test arhitectural sau
  printr-o justificare explicită în README-ul modulului.

Pragul nu înlocuiește analiza de design: un package cu 8 clase poate fi încă
slab coeziv, iar unul cu mai mult de 8 poate necesita o separare justificată.
`ArchitectureBoundaryTest` inventariază acum package-urile de producție și
respinge orice hotspot nou care nu apare într-o allow-list explicită. Excepțiile
curente sunt `atf.api.core.http`, `atf.api.core.reporting` și
`atf.api.reporting`; fiecare are un motiv semantic documentat și rămâne datorie
de refactorizare, nu o anulare a pragului.

## Separarea modulelor

~~~mermaid
flowchart LR
    Scenario[scenariu] --> Service[serviciu semantic]
    Service --> Executor[executor]
    Executor --> Transport[transport]
    Executor --> Evidence[evidence neutru]
    Evidence --> Reporter[reporting Extent]
    Service --> Validator[validator]
    Validator --> Assertions[tabel agregat]
~~~

Aceasta este o diagramă de colaborare runtime, nu o diagramă de dependențe
Maven. Modulele, direcția importurilor și composition root-ul sunt definite în
[ATF_Architecture.md](ATF_Architecture.md).

Testele nu construiesc transportul, nu manipulează HTML-ul și nu decid local
precedence-ul statusurilor. Reporting-ul nu face apeluri HTTP și nu modifică
assertions pentru a obține o randare mai convenabilă.

## Teste care merită păstrate

- unitare pentru route building, configurație, redaction, retry, timeout,
  cancellation, precedence și agregare;
- de componentă pentru executor + fake transport + reporter;
- de artifact pentru HTML autonom, tab-uri, collapse, JSON și theme;
- de integrare pentru modul, lifecycle și cleanup;
- scenarii API pentru contractul real al serviciului.

Un bug de raportare are nevoie de testul cel mai ieftin care dovedește regula,
apoi de un test de componentă sau artifact dacă schimbarea traversează module.

## Raport fără duplicare

~~~text
phase
├── request
│   ├── technical trace
│   ├── request/response JSON
│   └── durată
└── validation sibling
    └── assertion table agregat
~~~

TEST BODY nu repetă failure-urile detaliate mai jos în tabel. Fazele rămân
compacte și colapsate; detaliile se deschid doar când sunt necesare.

## Mesaje de consolă fără responsabilități amestecate

Consola este o proiecție diagnostică a aceluiași lifecycle, nu o sursă de
verdict și nu un înlocuitor pentru artifactul Extent. `atf-core` definește
`AtfConsoleEvent`/`AtfConsoleSink`, iar modulul separat `atf-console` formatează
evenimentele în `AtfConsoleReporter` și citește numai `reporting.console` din
YAML. Adapterul JUnit este opțional și este inclus în `atf-console`, în
subpachetul `atf.api.console.junit`.
`atf-reporting` consumă numai sink-ul injectat și rămâne fără formatter sau
loader de consolă concret. Ordinea observabilă este:

```text
scenario → phase → request → validation → complete(status, duration-ms)
```

Reguli obligatorii:

- mesajele de bază includ nivelul (`[PASS]`, `[FAIL]`, `[WARNING]`, `[SKIP]`),
  numele nodului și durata formatată de politica comună: `ms` până la `300 ms`,
  apoi secunde (`0.301 s`, `1 s`);
- detaliile sunt opt-in prin `reporting.console.detailsEnabled` și sunt
  redacted înainte de a ajunge la sink;
- JSON-ul și stacktrace-ul nu se tipăresc implicit;
- un sink care nu poate scrie nu modifică verdictul și nu întrerupe reportarea;
- rulările paralele folosesc sink-uri per sesiune și output-uri timestamped;
- testele pot folosi `ExtentReportSession.openConfigured()` pentru rularea
  directă din IntelliJ, iar `atf.yaml` rămâne singura sursă de output policy.

## Reguli pentru metadata

- lista Tests din stânga rămâne scanabilă: nume, status și durată cu unitate
  (`N ms` până la `300 ms`, apoi `N s`), niciodată un număr fără semnificație;
- tag-urile apar în detaliile scenariului și în Overview, nu ca pills în lista
  claselor;
- Category este navigare, nu o a doua sursă de verdict;
- Fails listează toate testele individuale, nu doar grupuri pe clasă;
- Overview este ultimul tab și se deschide implicit;
- durata se afișează în `ms` pentru valori de până la `300 ms`, iar peste prag
  în secunde cu precizie de milisecunde (`0.301 s`, `1.5 s`, `12.345 s`);
  timestamp-ul este metadata de eveniment, nu durată, count sau identitate logică;
- o frunză păstrează durata măsurată; un părinte însumează numai copiii
  structurali direcți și adaugă durata proprie exclusivă când există;
- totalul clasei din navigator este suma scenariilor directe din panoul din
  dreapta și trebuie să fie identic cu totalul din header-ul clasei;
- o durată sursă deja inclusivă nu se adună din nou cu descendenții;
- statusul și durata sunt informații distincte: `Pass` + `16 ms`, nu un număr
  interpretabil ca assertion count sau step count;
- badge-urile `Pass`, `Warning` și `Fail` păstrează aceeași geometrie în toate
  tab-urile: `18.2px` înălțime, `0 5px` padding, `4px` radius, `9.52px`
  font/line-height, `400` weight și border `0`.

## Schimbări vizuale

Tema este centralizată. Nu se adaugă fonturi, culori, grosimi de linii sau
dimensiuni direct în rendererul unui singur nod. Pentru orice schimbare de UI:

1. actualizează token-ul comun;
2. adaugă test pentru token și componentă;
3. generează artifactul în staging;
4. verifică randarea și computed styles în TAB MCP;
5. confirmă că Extent și fallback-ul arată la fel.

Pentru header-ele din `Tests`, regula este separată pe axe: titlul și tag-urile
sunt aliniate la stânga, tag-urile rămân sub titlu, iar grupul lor este centrat
vertical în header. Toolbar-ul din dreapta păstrează statusul și durata cu
`ms`, pe aceeași centrare verticală; nu este permis ca aceste valori să coboare
pe rândul tag-urilor sau să se centreze orizontal împreună cu titlul.
Badge-urile semantice (`Pass`, `Warning`, `Fail`, `Info`) reutilizează în toate
tab-urile aceeași geometrie compactă (`18.2px`, padding `0 5px`, rază `4px`,
font `9.52px`, line-height `18.2px`). Lățimea rămâne naturală după text; nu se
introduc variante locale pentru Category, Fails sau Overview. Duratele și
ancorele neutre folosesc token-ul meta separat, fără să mărească statusurile.

## Lucrul sigur cu execuții active

Build-ul și testele se fac într-un staging nou, separat de checkout-ul sau
artifactul folosit de o rulare existentă. Nu se folosesc operații destructive și
nu se copiază output-uri generate înapoi în surse. Procedura completă este în
[ATF_BUILD_AND_EXECUTION_SAFETY.md](ATF_BUILD_AND_EXECUTION_SAFETY.md).

## Protocol pentru fiecare refactorizare

Orice îmbunătățire ATF Java urmează aceeași buclă verificabilă:

```text
sync live
  → baseline în staging
  → un increment cu responsabilitate clară
  → test nominal + failure-path
  → artifact timestamped
  → click-uri și verificare în TAB MCP
  → re-sync cu live
  → README contract stabil + feedback doar cu ToDo-uri rămase
```

Reguli obligatorii:

- live-ul se recitește înainte de fiecare increment și înainte de sincronizarea
  înapoi; o copie locală veche nu este suficientă;
- staging-ul este unic, separat și fără `target`, `artifacts`, cache-uri sau
  log-uri copiate din alte rulări;
- un failure de test este diagnostic de implementare, nu motiv pentru relaxarea
  testului;
- o modificare de reporting se acceptă numai după verificarea artifactului în
  TAB MCP, inclusiv interacțiunea relevantă, nu doar sursa HTML;
- documentația permanentă descrie comportamentul stabil, iar feedback-ul nu
  repetă explicații sau rezultate deja închise;
- sincronizarea în live este selectivă și se oprește la conflict, fără overwrite
  sau reset destructiv.

## Review final

Înainte de a considera o schimbare terminată:

- responsabilitatea este mai clară decât înainte;
- testele protejează contractul, nu detalii accidentale;
- request-ul și validation-ul au ownership corect;
- assertions sunt agregate și nu sunt duplicate;
- artifactul este autonom și navigabil;
- raportul a fost verificat în TAB MCP dacă s-a schimbat randarea;
- README-ul și diagrama descriu codul actual, fără statusuri temporare.

