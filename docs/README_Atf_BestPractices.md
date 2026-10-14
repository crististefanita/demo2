# Bune practici pentru ATF Java

Aceste reguli urmăresc trei lucruri: cod ușor de întreținut, comportament
testabil și raport care explică execuția fără zgomot.

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

## Reguli pentru metadata

- lista Tests din stânga rămâne scanabilă: nume, status și durată cu unitate
  (`N ms`), niciodată un număr fără semnificație;
- tag-urile apar în detaliile scenariului și în Overview, nu ca pills în lista
  claselor;
- Category este navigare, nu o a doua sursă de verdict;
- Fails listează toate testele individuale, nu doar grupuri pe clasă;
- Overview este ultimul tab și se deschide implicit;
- durata se afișează în milisecunde (`1 ms`, `16 ms`, `25 ms`); timestamp-ul
  este metadata de eveniment, nu durată, count sau identitate logică;
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

