# ATF Reporting — uniformitate API și interacțiuni de aliniat

Reperul este raportul API cu `UsersApiTests` și `PostsApiTests`, produs de
`atf-reporting-tests`. Uniformitatea rămâne deschisă între acest model,
standalone-ul Shop, auditul componentelor și API din UI+API.

Contractele deja promovate au următorii owneri:

| Contract | Documentație oficială |
|---|---|
| Audit API per schimb și publicarea contractului asociat | [Contractul de raportare](../ATF_Reporting_Contract.md), [README Practice Shop](../../atf-test-practice-shop/README.md) |
| `TECHNICAL TRACE`, viewport comun și paritate Extent/fallback | [README ATF Reporting](../../atf-reporting/README.md) |
| Matricea probelor de componentă și browser | [README Reporting Tests](../../atf-reporting-tests/README.md) |
| Checkpoint UI fără request HTTP sintetic; context păstrat intern | [README ATF Reporting](../../atf-reporting/README.md), [Contractul de raportare](../ATF_Reporting_Contract.md) |

Fișierul păstrează ținta existentă `@DocumentationTarget`; cerințele de mai jos
sunt corelate cu sursele live și cu interacțiunile din rapoarte randate.

## Cerințe deschise, în ordinea implementării

| Prioritate | Cerință | Responsabil | Dovada pentru închidere |
|---|---|---|---|
| P1 — PUB-01 | Publicare predictibilă pentru HTML voluminos cu PNG-uri inline | Reporting: pipeline și teste de componentă | Proces separat cu heap bounded, fixture determinist și artifact complet |
| P1 — API-01 | Închiderea părintelui ascunde întregul subarbore | Reporting: runtime de navigare | Browser: noduri cu dovezi proprii și copii |
| P1 — API-02 | Rolul și prezentarea provin din tipul nodului | Reporting: model și proiecție | Titluri personalizate și metode variate păstrează formatul |
| P1 — API-03 | Aceeași schemă de dovezi pentru toate intrările API | Reporting și adaptoare | Paritate semantică/vizuală, corp absent și replay |
| P1 — API-06 | Aserțiuni și verdicte atribuite canalului și exchange-ului corect | Reporting și contractul checkpoint-ului | UI PASS/API FAIL și invers, fără contaminarea fraților |
| P1 — API-07 | Metoda și timpul reflectă captura | Adaptor captură și Reporting | `QUERY` păstrat; măsurat, necapturat și timp de publicare distincte |
| P1 — API-04 | Intenție lizibilă și publicare cauzală fără dubluri | Shop la intrare; Reporting la randare | Apeluri distincte, retry și republicare diferențiate |
| P2 — API-05 | Completitudinea auditului are o zonă secundară coerentă | Reporting și adaptor audit API | Metadate colapsate, fără tabel brut introductiv |
| P1 — gate comun | Teste de componentă care resping abaterile | `atf-reporting-tests` | Matrice pozitivă și probe negative explicite |
| P2 — documentație | Eliminarea contradicțiilor contract/cod | Responsabilii README-urilor | Un contract actual, promovat după implementare |

### Progres verificat în implementarea curentă

| Cerință | Ce există în cod și ce s-a probat | Ce împiedică închiderea completă |
|---|---|---|
| API-01 | Runtime-ul schimbă starea tuturor panourilor directe ale nodului; componenta browser verifică `.collapse` + `.accordion`, mouse, Enter, Space, ARIA și vizibilitatea calculată. | Aceeași probă pentru Gherkin cu API imbricat, normalizare repetată și frați. |
| API-02 | Rolurile fază/request/validation provin din ierarhia de noduri, nu din titlu; componenta browser folosește un titlu de validare personalizat. Excepția de prezentare pe textul `Contract validation` a fost înlocuită cu prezența dovezilor UI. | Matricea de titluri/metode și aggregate-ul de rețea în browser, inclusiv copie selectată și Fails. |
| API-03 | Aliasurile `manual.replay`, `transport.provenance` și `auth.profile` rămân în request și primesc etichete distincte; testul de formatter le apără. | Paritatea completă direct/Shop/UI+API, corp absent versus JSON `null`, provenance și replay. |
| API-05 | Detaliile fazei sunt un disclosure secundar în Extent și fallback; fallback-ul are test de conținut și redacție, iar browserul verifică disclosure-ul de fază colapsat. | Auditul complet cu/fără exchange-uri și ordinea vizuală în toate proiecțiile. |
| API-08 — închis | Request metadata și retry history folosesc `atf-api-table-wrap` în Extent și fallback. `ExtentReportDisclosureBrowserComponentTest` verifică tabele lungi și ultimul rând la viewport standard/`760px`; `ReportApiViewportParityComponentTest` verifică tabele scurte în Extent și scurte/lungi în fallback, scroll local, antet sticky și lipsa overflow-ului documentului la ambele lățimi. Ambele artefacte au fost deschise în browser; fallback-ul a fost inspectat și vizual în MCP. | Niciun criteriu API-08 rămas; schimbările viitoare de CSS/markup trebuie să treacă aceleași probe. |
| Gate comun | Verificarea statică cere elemente reale în `<body>`; eliminarea disclosure-urilor cu CSS păstrat este caz negativ. | Fixture reală negativă pentru cascade CSS și toate combinațiile de verdict. |

API-04, API-06 și API-07 rămân deschise; nu s-a schimbat încă semantica
publicării cauzale, ownership-ul verdictelor ori fidelitatea snapshot-ului Shop.
Testele de șiruri rămân doar guard-uri auxiliare; probele de browser și
artefactele generate decid închiderea.

**Corecție de vocabular verificată:** `AtfReportLabels` produce singularul
pentru `1 field/check/attempt/entry`, inclusiv `Audit details · 1 field`.
Testele de etichete, renderer Extent/fallback și componenta browser au trecut;
raportul nou publicat a fost inspectat și în MCP. Nici această corecție, nici
viewport-ul îngust nu închid celelalte cerințe din matrice.

### PUB-01 — bugetul de memorie al publicării

- **Cauză confirmată:** heap-ul de 256 MiB al fork-ului Surefire poate fi epuizat
  în `ExtentReportPublicationPipeline.ensureViewIdentifiersAndOverviewDefault`,
  la `String.replaceFirst`, după terminarea journey-urilor.
- **Mecanism:** pipeline-ul citește și transformă întregul HTML în mai multe
  etape; deduplicarea PNG-urilor are loc abia după aceste transformări.
  Dimensiunea fișierului final nu este bugetul de heap necesar.
- **Remediere de configurație:** bugetul implicit este 2 GiB per fork,
  configurabil prin `atf.test.heap.max`; nu schimbăm politica de captură și
  nu dezactivăm fork-ul. Configurația nu dovedește scalabilitatea pipeline-ului.
- **Probă măsurată:** `LargeReportPublicationComponentTest` pornește un JVM
  separat cu `-Xmx384m`, generează 1.500 linkuri PNG inline cu două imagini
  distincte/repetate și text Unicode; artifactul final depășește 9 MB,
  păstrează ambele ancore, markerul final, tema și runtime-ul. Vârful heap
  observat în proba locală a fost de circa 264 MB, cu exit code 0 și fără E2E live.
- **Rămâne de făcut:** praguri reprezentative pentru volume mai mari și PNG-uri
  mari/distincte, plus buget de durată calibrat; această fixture demonstrează
  doar profilul explicit de 384 MiB, nu scalarea generală a pipeline-ului.
- **Gate:** publicare completă și autonomă, capturi păstrate, exit code valid;
  failure-path fără artifact parțial promovat și fără pierderea diagnosticului.
  Optimizează copiile HTML numai după măsurare; nu trata creșterea heap-ului
  ca dovadă de închidere a acestei cerințe.

## Constatări și comportamente de păstrat

Comparația a inclus raportul-model API din Reporting Tests, standalone-ul Shop,
auditul și ierarhia UI+API în browser. Constatările sunt corelate cu sursele
live; reproducerea nu depinde de fișierul generat sau de ora unei rulări.
Lista de mai jos consemnează diagnosticul care a deschis cerințele, nu afirmă
că fiecare defect se reproduce încă în codul curent; starea implementării este
în tabelul „Progres verificat” și în probele indicate acolo.

- **Defect comun confirmat:** request-ul poate avea `aria-expanded=false` și
  glyph `＋`, cu propriul `.collapse` ascuns, dar `.accordion` vizibil;
  `Contract validation` și tabelul său rămân pe ecran după închiderea părintelui.
- **Același defect în UI+API:** închiderea propoziției Gherkin lasă vizibile
  `API validation` și exchange-ul deschis din interior.
- **Audit:** `Audit details` și un tabel `TECHNICAL TRACE` apar direct în
  `TEST BODY`, înaintea exchange-urilor; standalone-ul nu are acest bloc.
- **Corp absent:** auditul omite `REQUEST JSON` pentru GET fără corp și ambele
  canale JSON pentru DELETE cu răspuns fără conținut; biblioteca oferă deja
  reprezentarea explicită `null`.
- **Cauzalitate:** același eveniment de înregistrare, cu aceeași identitate și
  secvență în același scenariu, apare și sub înregistrare, și sub login.
- **Responsabilitate:** checkpoint-ul de produs pune verificările de query,
  răspuns JSON, headers și trace API în `UI validation`; grupul `API validation`
  are exchange-uri, dar nu primește aceste aserțiuni.
- **Transport:** captura expune `observed method=QUERY`, iar proiecția UI
  publică `GET` și generează replay cu `GET`; echivalența nu este demonstrată.
- **Timp:** mapperul UI introduce `Duration.ZERO` pentru response, total și
  attempt; tabelul afișează astfel `0 ms` fără o măsurătoare păstrată.
- **Accesibilitate:** în model, Enter nu deschide scenariul/faza cu
  `role=button`; deschiderea unui disclosure canonic păstrează ARIA la `false`.
- **De păstrat:** cele trei disclosure-uri au deja geometria compactă comună
  și ordinea request → response → execution; antetele API comparate păstrează
  fontul, culoarea și geometria la viewport standard și îngust.
- **Închis în continuare:** viewportul comun al tabelelor, wrapperul
  `atf-trace-table-wrap`, headerul sticky și separarea JSON/tabel au
  implementare și probe dedicate; nu sunt redeschise prin acest review.

Raportul-model este reper vizual, nu excepție de la contract: dacă același
defect există și în model, el trebuie corectat în componenta comună.

## Modelul API — ce preluăm și unde diferă proiecțiile

Scenariile modelului sunt publicate prin `ApiAcceptancePublisher`, folosesc
`ApiJsonEvidence`/`ApiManualReplay` și un contract evaluat prin
`ResponseContractReporter`; aceste convenții sunt reperul pentru exchange.

<details>
<summary>Comparația domeniilor de date, nu a unor numere fixe de câmpuri</summary>

| Aspect | Model Reporting Tests | Shop standalone / audit | API din UI+API | Criteriu comun |
|---|---|---|---|---|
| Provenance | În request | `transport.provenance` ajunge în response | În request | Același domeniu semantic, indiferent de aliasul cheii |
| Replay | În request; mock explicat ca neexecutabil | Lipsește | Există, dar `manual.replay` ajunge în response | Același câmp în request, fidel transportului |
| Status HTTP | Explicit în response | Lipsește din response în standalone; apare în audit | Explicit în response | Status vizibil în același loc; contractul separat |
| Content type | `CONTENT-TYPE` | `CONTENT TYPE` | `CONTENT TYPE` | Un singur label canonic |
| Auth | `AUTH PROFILE` și `AUTH TYPE` | Aceeași schemă, inclusiv audit | Aceeași schemă; fără profil inventat | Profilul și mecanismul rămân distincte după redactare |
| Corp request absent | `REQUEST JSON` cu `null` | Canalul este omis | Publicare condiționată de corp nevid | Absența confirmată rămâne dovadă explicită |
| Context operație/captură | Exchange cu metadatele relevante | Operation/route/capture amestecate în response | `TRACE EXCHANGE` în request; corelare în execution | Contextul și răspunsul HTTP au domenii distincte |
| Contract | Decizie și expected/actual explicite | Standalone are 2xx/JSON; auditul folosește contractul asociat | Aserțiuni API în UI; starea exchange-ului este preluată din grup | Același format, reguli furnizate de owner |
| Încercări | Istoric disponibil în execution | Istoricul direct este păstrat | Modelul le suportă; adaptorul browser publică o încercare | Nu se inventează retry din două apeluri asemănătoare |
| Geometrie de bază | Antet și disclosures compacte | Aceleași tokenuri | Aceeași familie de componente, sub grupul API | Se păstrează layout-ul comun deja funcțional |

</details>

- `manual.replay` nu este recunoscut de `isRequestDetail`, deși varianta
  `Manual replay` este recunoscută; `transport.provenance` are aceeași problemă
  față de `Transport`. Clasificarea prin restul câmpurilor drept response
  ascunde această nepotrivire dintre publisher și formatter.
- Paritatea se compară pe identități semantice, ordine, valori și stare; un
  response cu mai multe headers poate avea legitim mai multe rânduri.
- Se compară profiluri echivalente: mock/live, contract furnizat/observație și
  corp absent/corp indisponibil sunt diferențe legitime, explicate în raport.
- Fontul comun nu compensează un câmp pus în domeniul greșit, un verdict
  împrumutat sau informații omise.

## API-01 — închiderea atomică a subarborelui

Reproducere: deschide scenariul, faza, request-ul și validarea; închide apoi
request-ul, fără să închizi manual validarea.

- `nodePanel(card)` alege primul `.collapse`, înaintea `.accordion`; un request
  cu ambele suprafețe controlează astfel numai dovezile proprii.
- `setDisclosureState` actualizează antetul pentru un singur panel; starea
  declarată și vizibilitatea copiilor se pot contrazice.
- Toate suprafețele nodului trebuie controlate împreună, printr-un container
  comun sau printr-o politică explicită pentru toate corpurile sale.
- Închis înseamnă că niciun descendent nu este vizibil sau accesibil prin Tab;
  antetul, glyph-ul, ARIA și corpurile trebuie să rămână sincronizate.
- Închiderea nu schimbă un frate; redeschiderea păstrează starea locală a
  copiilor conform aceleiași politici în API direct și UI+API.
- Normalizarea, schimbarea ownerului și sarcinile amânate ale runtime-ului nu
  pot redeschide copii, dubla evenimente sau schimba spontan starea selectată.
- Același contract acoperă tastatura pentru scenariu și fază, nu doar pentru
  request/validation; `role=button` și `tabindex` nu asigură singure activarea.
- `<details open>`, glyph-ul și `summary[aria-expanded]` rămân sincronizate și
  pentru disclosure-urile emise direct de Java, pe care normalizarea le sare.

**Închidere:** proba de browser eșuează pe comportamentul descris și trece pentru
request cu dovezi + copii, nod numai cu copii și validare cu exchange-uri.

## API-02 — rol semantic explicit, independent de titlu

`assignNodeRoles` clasifică nodurile prin regex-uri pentru titlu; normalizarea
aserțiunilor are și o excepție literală pentru `Contract validation`.

- Nodurile create prin porturile request/validation își păstrează rolul tipizat
  până în DOM; titlul rămâne conținut pentru cititor.
- O validare cu nume personalizat și o metodă precum `OPTIONS` păstrează stilul
  și interacțiunile rolului lor, fără regex-uri noi pentru fiecare denumire.
- Layout-ul unei validări API se decide prin tipul dovezii, nu prin egalitate
  cu textul `Contract validation`; redenumirea nu schimbă DOM-ul.
- HTML-ul extern/istoric poate avea un fallback delimitat; artefactele noi nu
  depind de acest fallback pentru identificarea rolurilor.
- O singură politică de componente și tokenuri deține antetul, badge-urile,
  spațierea, disclosure-urile și tabelele; fără CSS particular per test.

**Închidere:** aceleași date și același tip produc aceeași prezentare, chiar dacă
titlul nu conține HTTP, Gherkin, `NETWORK` sau `VALIDATION`.

## API-03 — aceeași schemă, prin aceeași compoziție

Rendererul comun există, însă producătorii încă asamblează separat câmpurile și
JSON-ul; astfel diferă nivelul de detaliu din API direct, audit și UI+API.

Schema auth comună și păstrarea mecanismului după redactare sunt acoperite prin
`ApiAuthenticationEvidence` și probe de componentă. Contractul stabil este în
[schema auth oficială](../ATF_Reporting_Contract.md#schema-auth-a-dovezilor-api);
restul diferențelor de request/response rămân deschise în această cerință.

```text
METHOD /route — intenția apelului
├── Request details    → metadate request + REQUEST JSON
├── Response details   → metadate response + RESPONSE JSON
├── Execution evidence → attempts, retry, durată și corelare
└── Contract validation → numai cu un contract evaluat
```

- Sub `API validation` dintr-un checkpoint mixt se reutilizează același
  subarbore; `UI validation` rămâne fratele grupului API.
- Compoziția comună primește snapshot-uri neutre request/response/execution;
  consumerul furnizează intenția și contractul aplicației.
- Se reutilizează `ApiJsonEvidence`, `ApiManualReplay` și formatarea existentă;
  nu se copiază un publisher de teste în librărie cu dependențe de Shop.
- Un corp confirmat absent are documentul JSON `null`; un corp necapturat sau
  indisponibil are o explicație distinctă, fără inventarea dovezii.
- Politica pentru auth, headers, provenance și replay este comună; numărul de
  câmpuri poate varia justificat cu datele disponibile.
- Semnătura nu înlocuiește arbitrar metoda/calea, iar câmpurile nu migrează
  între request, response și execution în funcție de publisher.
- O singură aserțiune și un contract cu mai multe aserțiuni pot avea același
  stil; uniformitatea nu cere inventarea acelorași verificări.
- Un trace observat nu primește un verdict contractual artificial; un răspuns
  HTTP negativ așteptat poate satisface contractul său.

**Închidere:** aceeași fixture neutră și același profil de dovezi, prin cele trei
intrări, au aceeași schemă și stil, inclusiv GET fără corp și 204.

## API-04 — intenție, identitate și cauzalitate

În audit, `publishObserved` preia titlul generic al operației: login-ul valid
și cel invalid apar ca `establish API session`, deși al doilea demonstrează
respingerea și invalidarea sesiunii.

- Titlul combină metoda/calea cu intenția explicită a apelului sau contractului;
  cititorul distinge pregătirea de verificarea negativă.
- Două invocări reale ale aceluiași endpoint rămân două exchange-uri;
  metoda/calea identică nu demonstrează retry sau duplicare.
- Attempt-urile aceluiași exchange rămân într-un singur `Execution evidence`;
  nu generează un alt request/response ori o validare paralelă.
- Setup-ul contului publică dovezile locale fiecărui checkpoint; lista
  acumulată pentru înregistrare nu se republică integral la login.
- Identitatea include domeniul capturii/scenariului; un `eventId` local nu se
  deduplicatează global între scenarii independente.
- Inspectorul agregat poate referi evenimente deja publicate, fără să le
  prezinte ca invocări noi sau alte propoziții Gherkin.

**Închidere:** componente pentru două intenții pe același endpoint, retry într-un
exchange și register → login fără republicarea înregistrării.

## API-05 — completitudinea auditului este secundară

Completitudinea este deja publicată o singură dată la nivel de fază; problema
rămasă este randarea ca rând de text urmat de tabel tehnic brut.

- Rezumatul rămâne într-o zonă secundară, colapsată, cu aceeași componentă de
  disclosure; detaliile se deschid numai la cerere.
- Numărul de exchange-uri, asocierea contractelor și eșecul JUnit sunt metadate
  de audit/scenariu, fără pas HTTP sau contract business inventat.
- Zero exchange-uri se explică fără request fictiv.
- Nu se reintroduc `Test method outcome`, `Runtime diagnostic` sau alte noduri
  care dublează verdictul ori schimbă arborele numai pentru acest consumer.
- Faza cu și fără metadate proprii păstrează aceeași expandare a exchange-urilor.

**Închidere:** primul nivel pune în față pașii relevanți; metadatele folosesc
zona lor secundară și păstrează verdictul real.

## API-06 — aserțiuni și verdict local, înainte de agregare

`ShopPurchaseCheckpoint.withAdditionalAssertions` pierde distincția UI/API;
`UiEvidencePublisher` trimite lista combinată în validarea vizuală și atribuie
exchange-urilor statusul grupului, cu excepția răspunsului indisponibil.

- Checkpoint-ul păstrează separat aserțiunile UI, contractele API și corelările
  dintre canale, cu referință la exchange atunci când aceasta există.
- Separarea este tipizată; prefixul `API` din numele aserțiunii nu este un
  protocol pentru a deduce ownerul sau verdictul.
- Query, headers și identitatea din JSON se verifică în canalul API; ruta,
  controalele și imaginea rămân în UI, sub aceeași propoziție Gherkin.
- O corelare UI ↔ API își explică cele două surse fără să dubleze propoziția,
  screenshot-ul ori întreaga listă de aserțiuni în ambii frați.
- UI PASS + API FAIL produce checkpoint FAIL și păstrează UI PASS; UI FAIL +
  API PASS păstrează rezultatul API, fără a recolora toate exchange-urile.
- Două exchange-uri cu rezultate diferite nu primesc automat aceeași stare;
  lipsa contractului rămâne observație explicită, fără PASS contractual inventat.
- Un 401 așteptat poate fi PASS contractual, iar 200 cu JSON greșit poate fi
  FAIL; codul HTTP singur nu înlocuiește decizia ownerului.
- Aceleași reguli se aplică modului checkpoint și `UNIFIED_FLOW`; agregarea
  fluxului nu trebuie să mute toate verificările în `Screen validation`.

**Închidere:** componente cu UI PASS/API FAIL, UI FAIL/API PASS, API indisponibil,
negativ așteptat și două exchange-uri diferite demonstrează ownership-ul local.

## API-07 — fidelitatea snapshot-ului de transport

`ShopApiEvidenceMapper` rescrie metoda prin vocabularul operației și construiește
duratele browserului cu zero. Separarea profilului de mecanism este deja acoperită
de schema auth comună și nu mai reprezintă un ToDo.

- Metoda HTTP capturată se păstrează în titlu, request, semnătură și replay;
  aliasul operației business este o informație separată.
- `QUERY` cu un corp nu devine `GET` doar pentru un titlu cunoscut; orice
  adaptare de protocol cere o echivalență demonstrată și ambele valori explicite.
- O durată necapturată se explică drept nemăsurată; nu este zero măsurat.
- Response time, totalul cu retry/backoff și timpul scope-ului de raportare
  se disting; nu se prezintă timpul publicării Extent drept latență HTTP.
- Modelul API pornește request-ul de raportare după obținerea execuției;
  badge-ul lui măsoară ciclul de raportare și nu probează singur durata rețelei.
- Datele exacte, când există, se păstrează în toate intrările API și în adaptorul
  legacy/core; redacția protejează secretele fără a schimba metoda sau corelarea.

**Închidere:** fixture cu metodă `QUERY`, profil diferit de mecanism, durate
response/total distincte și durată necunoscută verifică inclusiv replay-ul.

## API-08 — închis: viewport pentru metadate request / istoricul încercărilor

Proba inițială de densitate pe DOM-ul randat arăta tabele request metadata și
retry history mai înalte decât plafonul comun, fără scroll local sau header
sticky. Acest defect este rezolvat prin `atf-api-table-wrap` comun.

- Și aceste tabele folosesc `--atf-table-scroll-height`, raportat la
  System/Environment; conținutul scurt rămâne la înălțime naturală.
- Headerul și ultimul rând rămân accesibile în scroll local; nu se limitează
  întregul disclosure și nu se ascunde coada tabelului prin `overflow:hidden`.
- JSON-ul rămâne în afara viewport-ului metadatelor; execution summary,
  attempt history și trace își păstrează responsabilitățile și ordinea.
- Probele browser generează artefacte complete cu date scurte/lungi; clonarea
  de rânduri în DOM nu este folosită drept dovadă.
- Suprafețele lipsă sunt verificate de `ExtentReportDisclosureBrowserComponentTest`
  și `ReportApiViewportParityComponentTest`; nu se suprascrie rolul probei
  existente pentru celelalte opt suprafețe.

**Dovada închiderii:** date scurte/lungi, viewport standard/`760px`, plafon
comun, header sticky, ultimul rând accesibil și lipsa overflow-ului orizontal
pentru ambele suprafețe API, în Extent și fallback. Testele browser au trecut,
iar artefactele randate au fost inspectate prin MCP.

## Gate de componentă — comportament, nu prezența unor șiruri

Testele existente apără contracte utile, dar nu demonstrează uniformitatea
completă: verificarea textului unui script sau expandarea tuturor nodurilor
poate trece când un copil rămâne vizibil sub un părinte închis.

**Probă negativă inițială, remediată în incrementul curent:** eliminarea celor
trei disclosure-uri API dintr-o copie în memorie a raportului-model lăsa
`ReportEvidenceStyleContract` cu `compliant=true`, deoarece clasele erau găsite
în CSS/JavaScript. Contractul cere acum noduri reale în `<body>`, iar testele
negative elimină disclosure-urile păstrând stylesheet-ul.

<details>
<summary>Matricea obligatorie și exemplele care trebuie respinse</summary>

| Probă | Contract observabil | Abatere care produce FAIL |
|---|---|---|
| Request cu `.collapse` și `.accordion` | Închiderea ascunde dovezile și copiii | Validarea/tabelul rămâne sub `＋` |
| Nod numai cu copii; validare cu exchange-uri | Stare comună pe întregul corp | Grup vizibil, antet închis |
| Mouse, Enter și Space | Stare identică pentru aceeași acțiune | ARIA/glyph diferă de vizibilitate |
| Disclosure emis direct de Java | `open`, glyph și ARIA identice după toggle | Vizibil deschis, `aria-expanded=false` |
| Cicluri repetate și normalizare ulterioară | Stare stabilă, frați nemodificați | Toggle dublu sau copil reapărut |
| API direct, audit, API din UI+API | Același subarbore și tokenuri | Tabele brute ori altă ordine |
| Titlu personalizat, metodă variată, validare redenumită | Rolul semantic decide prezentarea | Rol pierdut ori schimbat după text |
| GET fără corp, 204, dovadă indisponibilă | Absența și lipsa capturii sunt distincte | Canal omis ori `null` inventat |
| Succes, negativ așteptat, contract eșuat | Expected/actual și verdict corecte | Status sau aserțiuni falsificate |
| UI PASS/API FAIL și invers; două API cu stări diferite | Aserțiuni și verdict local, apoi agregare | Verdictul părintelui copiat în toți copiii |
| Aliasuri pentru replay/provenance/content type/auth | Același domeniu și label canonic | Replay în response sau profil confundat cu mecanism |
| `QUERY` cu corp; timp necunoscut și timp măsurat | Snapshot și replay fidele sursei | Metodă rescrisă, durată inventată ca zero |
| Două apeluri reale; un exchange cu retry | Identități distincte versus attempts | Comasare sau duplicare arbitrară |
| Înregistrare urmată de login | Eveniment la checkpoint-ul cauzal | Același eveniment republicat ca apel nou |
| Date scurte/lungi, viewport standard/îngust | Tokenuri, wrap, scroll și ultim rând accesibil | Înălțime particulară sau clipping |
| Request metadata și retry history lungi | Același plafon System/Environment și sticky | Tabel nelimitat ori coadă inaccesibilă |
| Audit fără exchange-uri; eșec JUnit | Metadate sincere, fără HTTP fictiv | Request/contract inventat |
| Disclosure eliminat, stylesheet păstrat | Validatorul respinge absența nodului real | CSS/JS este acceptat drept dovadă de conținut |
| Override CSS final/media query cu tokenuri inițiale intacte | Se compară stilul efectiv în viewport | Egalitatea primelor declarații ascunde drift-ul |

</details>

- `atf-reporting-tests` generează HTML real cu bundle-ul runtime al librăriei
  și îl verifică în browser; un DOM simplificat nu acoperă acest criteriu.
- Validatorul de format primește și fixture-uri negative deliberate; mesajul
  indică owner/scenariu/pas, regula, starea observată și remedierea.
- Eliminarea unei zone, mutarea unui câmp și ruperea închiderii se verifică
  independent; o singură probă de drift al unui token nu le acoperă pe toate.
- Se compară computed style, vizibilitate, ordine și accesibilitate;
  imaginile sunt probe suplimentare, nu singurul verdict.
- Se inspectează ownerul selectat în `.test-content-detail:visible`; template-ul
  ascuns și copia selectată nu se numără drept două execuții sau două probe.
- Se extind componentele de modul și integrarea adaptorului Shop; E2E-urile
  aplicației nu se complică pentru CSS sau stări locale ale rendererului.
- Guard-ul de arhitectură verifică dependențele și ownership-ul generic;
  nu impune nume de clase ori rezultate prin căutări fragile în text.
- Reporting Tests folosește fixture-uri neutre și porturile publice; testele
  adaptorului Shop rămân în Shop, fără dependență inversă între modulele de teste.
- Review-ul documentează aceste cerințe; implementarea și modificarea testelor
  de guvernanță aparțin incrementului ulterior.

<details>
<summary>Ce apără probele existente și ce componentă trebuie să completeze dovada</summary>

| Probă existentă | Limită verificată în cod | Completare necesară |
|---|---|---|
| `ReportEvidenceStyleContractTest` | HTML minim construit din șiruri și drift al unui token | Artefacte reale, zone eliminate și cascade CSS diferite |
| `ReportEvidenceStyleContract` | Caută clase în întregul fișier și prima valoare a tokenului | Noduri efective, domenii semantice și computed style |
| `ReportRendererParityTest` | Verifică tokenuri în Fails și Categories | Contract de date și subarbore API, inclusiv UI+API |
| `AtfReportMarkupTest` | Vocabular și partiționare cu un subset de chei | Toate aliasurile emise efectiv de publisheri |
| `ExtentReportUiEvidencePublishingTest` | Nume, număr de exchange-uri și istoric în HTML | Ownership aserțiuni, verdict per exchange și interacțiuni |
| `ScrollableTableViewportBrowserProbe` | Suprafețele enumerate explicit | Request metadata și retry history scurte/lungi |
| `ReportingCoverageMatrixTest` | Existența clasei, metodei și adnotării | Dovadă comportamentală și exemple negative pentru seam-ul API |

</details>

<details>
<summary>Punctele diagnosticului inițial și testele de regresie</summary>

Coloana din dreapta explică motivul deschiderii cerinței; nu înlocuiește
statusul verificat de mai sus. În special `nodePanel`, clasificarea după titlu
și false-green-ul contractului static au fost corectate local, dar matricea
completă de proiecții și interacțiuni rămâne deschisă.

| Punct verificat | Legătură cu cerința |
|---|---|
| [Navigare/disclosure](../../atf-reporting/src/main/resources/atf/reporting/javascript/extent-navigation-disclosure.js) — `nodePanel`, `bindNodeCollapse`, `setDisclosureState` | API-01: un singur corp pentru nod cu dovezi și copii |
| [Roluri](../../atf-reporting/src/main/resources/atf/reporting/javascript/extent-evidence-roles.js) și [normalizare dovezi](../../atf-reporting/src/main/resources/atf/reporting/javascript/extent-evidence-disclosure.js) | API-02: clasificare după titlu și excepție literală |
| [Publisher Shop API](../../atf-test-practice-shop/src/main/java/atf/ui/practiceshop/support/ShopApiReportPublisher.java) și [publisher UI](../../atf-reporting/src/main/java/atf/api/reporting/ui/UiEvidencePublisher.java) | API-03/04: compoziții separate și titlu generic |
| [Politica JSON](../../atf-reporting/src/main/java/atf/api/reporting/api/ApiJsonEvidence.java) | API-03: convenția există deja; se aplică uniform |
| [Setup cont](../../atf-test-practice-shop/src/test/java/atf/ui/tests/practiceshop/support/ShopAuthenticatedAccountTestSupport.java) | API-04: listă acumulată trimisă ambelor checkpoint-uri |
| [Adaptor audit API](../../atf-test-practice-shop/src/test/java/atf/ui/tests/practiceshop/api/reporting/ShopApiCallAuditTestSupport.java) | API-05: metadate trimise prin `phase.detail` |
| [Contract runtime](../../atf-reporting-tests/src/test/java/atf/api/reporting/runtime/core/ExtentReportRuntimeScriptTest.java) | API-01: fixează selecția `.collapse`, fără probarea ascunderii copiilor |
| [Flux audit API](../../atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/api/ApiReportUiFlow.java) și [validator](../../atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/audit/api/ApiReportContractValidation.java) | Gate: expandarea și datele nu probează închiderea atomică |
| [Publisherul modelului API](../../atf-reporting-tests/src/test/java/atf/api/reporting/api/acceptance/ApiAcceptancePublisher.java) și [generarea fixture-ului](../../atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/api/ApiReportAuditSetup.java) | Reper: provenance/replay în request, JSON explicit și contract evaluat |
| [Formatter comun](../../atf-reporting/src/main/java/atf/api/reporting/evidence/EvidenceTableFormatter.java) | API-03: aliasuri recunoscute diferit și context care ajunge implicit în response |
| [Contract checkpoint Shop](../../atf-test-practice-shop/src/main/java/atf/ui/practiceshop/support/ShopPurchaseCheckpoint.java) | API-06: lista de aserțiuni nu păstrează ownerul UI/API |
| [Mapper Shop](../../atf-test-practice-shop/src/main/java/atf/ui/practiceshop/support/ShopApiEvidenceMapper.java) și [vocabular operații](../../atf-test-practice-shop/src/main/java/atf/ui/practiceshop/api/ShopApiOperation.java) | API-07: metodă normalizată și durate zero; schema auth este acoperită separat |
| [Contract static de stil](../../atf-reporting/src/main/java/atf/api/reporting/evidence/ReportEvidenceStyleContract.java) și [testul său](../../atf-reporting-tests/src/test/java/atf/api/reporting/evidence/ReportEvidenceStyleContractTest.java) | Gate: false-green după eliminarea zonelor API |
| [Probe viewport](../../atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/layout/ScrollableTableViewportBrowserProbe.java) | API-08: request metadata/retry history nu sunt suprafețe măsurate |

</details>

## Documentație și criteriu de închidere comun

- [Contractul de raportare](../ATF_Reporting_Contract.md) încă descrie prefixul
  `API n ·` în trace-ul UI; proiecția nouă folosește noduri per exchange, iar
  formatterul păstrează separat cheile legacy. Documentația trebuie să delimiteze
  compatibilitatea, fără să o prezinte ca formatul implicit al artefactelor noi.
- [README Reporting](../../atf-reporting/README.md) deține compoziția și
  tokenurile; [README Reporting Tests](../../atf-reporting-tests/README.md)
  deține probele; [README Shop](../../atf-test-practice-shop/README.md)
  descrie intențiile, contractele și selecția consumerului.
- Etichetele au pluralizare corectă, inclusiv `1 attempt`; formatarea numărului
  nu se repară separat în fiecare publisher.
- Cheile interne precum `response.duration.ms` și `retry.events` nu țin locul
  etichetelor pentru cititor; modelul însuși are această lacună de prezentare.
- Închiderea cere fixture nouă din codul implementat, verificare interactivă a
  criteriului și probe negative care chiar eșuează.
- O suită verde de markup, date sau viewport nu închide defecte de interacțiune;
  contractele deja demonstrate rămân păstrate.
- După implementare se promovează regulile stabile în documentele ownerilor și
  se elimină din feedback numai lacunele efectiv închise.

## Scope executabil — uniformitate și viewport

Acest ToDo structurează schimbările testelor aferente cerințelor existente de
mai sus. Nu închide interacțiunile sau uniformitatea înaintea probelor cerute.
Testul browser declară numai intențiile; fixture-ul dedicat din `support`
deține generarea controlată și verificările interacțiunilor.
Selectorii probei sunt centralizați separat în `support/locators/disclosure`;
fixture-ul consumă această interfață semantică, fără selectori Playwright direcți.
Același owner deservește și proba de viewport API în Extent/fallback, fără
duplicarea selectorilor pentru request, execution, wrapper și rânduri.

Scope-ul include concordanța singular/plural deja introdusă în probele Extent,
fallback și UI; rămân aceleași aserțiuni stricte, cu vocabularul comun actual.

```yaml
testChanges:
  - id: REPORT-API-UNIFORMITY-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Probe pentru uniformitatea dovezilor API, inchiderea subarborelui si viewport-ul comun.
    proof: mvn -pl atf-reporting-tests -am -Dtest=HtmlReportViewsTest,AtfReportMarkupTest,ExtentReportRuntimeScriptTest,ExtentReportRuntimeNavigationComponentTest,ExtentReportDisclosureBrowserComponentTest,ReportApiViewportParityComponentTest,AtfReportEvidenceCssTest,ReportEvidenceMarkupTest,ExtentReportSessionContractTest,HtmlReportSessionTest,AtfReportSemanticSnapshotTest,ExtentReportUiEvidenceRenderingTest,ExtentReportUiFailureEvidenceTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/html/HtmlReportViewsTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/markup/AtfReportMarkupTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/runtime/core/ExtentReportRuntimeScriptTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/runtime/section/ExtentReportRuntimeNavigationComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/inspection/ExtentReportDisclosureBrowserComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/component/inspection/ReportApiViewportParityComponentTest.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/disclosure/ExtentReportDisclosureProbe.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/inspection/viewport/ReportApiViewportProbe.java
      - atf-reporting-tests/src/test/java/atf/ui/tests/extent/support/locators/disclosure/ReportDisclosureLocators.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/theme/evidence/AtfReportEvidenceCssTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/evidence/markup/ReportEvidenceMarkupTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/session/ExtentReportSessionContractTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/html/HtmlReportSessionTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/semantic/AtfReportSemanticSnapshotTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidenceRenderingTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiFailureEvidenceTest.java
```

## Scope executabil — publicarea rapoartelor voluminoase

Înregistrarea delimitează PUB-01: buget JVM configurabil, păstrarea dovezilor
și măsurarea într-un proces separat. Fixture-ul bounded este implementat;
failure-path-ul și profiluri mai mari rămân criterii de închidere.

```yaml
testChanges:
  - id: REPORT-LARGE-ARTIFACT-01
    status: ACTIVE
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Verificarea bugetului JVM configurabil si a integritatii dovezilor din rapoarte voluminoase.
    proof: mvn -pl atf-reporting-tests -am -Dtest=ExtentReportFinalizerBoundaryTest,LargeReportPublicationComponentTest -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - pom.xml
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/session/ExtentReportFinalizerBoundaryTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/extent/publication/LargeReportPublicationComponentTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/support/publication/LargeReportPublicationProbe.java
```

## Scope executabil — schema auth comună

Cerut direct: păstrează testul Shop existent și aliniază profilul, mecanismul,
aliasurile și redactarea între standalone și UI+API. Probele compară și
fallback-ul, mecanismul necunoscut și accesul anonim. Aserțiunile UI existente
rămân în scope-ul `UI-CHECKPOINT-CONTEXT-01`; se schimbă numai eticheta
contractuală în `AUTH TYPE` și se adaugă controlul negativ pentru vechea celulă.

Suita Shop verifică strict default-ul `Pliers`, deja prezent în YAML și în
fallback-ul loaderului din codul de bază; o probă separată păstrează alegerea
explicită `Pliers` sau `Combination Pliers`. Nu se schimbă fixture-ul live
și nu se înlocuiește egalitatea strictă printr-o aserțiune permisivă.

```yaml
testChanges:
  - id: API-AUTH-SCHEMA-01
    status: VERIFIED
    base: 521135291e642194294f9734ee1410ede0f19ef6
    expires: 2026-11-21
    reason: Schema auth comuna trebuie sa pastreze distinct profilul si mecanismul dupa redactare.
    proof: mvn -pl atf-reporting-tests,atf-test-practice-shop -am -Dtest=ApiAuthenticationSchemaContractTest,ReportAuthObservationTest,ExtentReportUiEvidencePublishingTest,ShopComponentSuite -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/auth/ApiAuthenticationSchemaContractTest.java
      - atf-reporting-tests/src/test/java/atf/api/reporting/api/ReportAuthObservationTest.java
```
