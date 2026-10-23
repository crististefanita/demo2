# ATF Reporting — backlog de uniformitate UI/API și publicare

Reperul este raportul API cu `UsersApiTests` și `PostsApiTests`, produs de
`atf-reporting-tests`. Uniformitatea rămâne deschisă între acest model,
standalone-ul Shop, auditul componentelor și API din UI+API.

Acest fișier păstrează numai gap-urile Reporting încă deschise și dovezile
necesare acceptanței. Contractele implementate sunt descrise în
[contractul semantic](../ATF_Reporting_Contract.md) și
[contractul de display și layout](../ATF_Reporting_Display_Contract.md).

- Probe de componentă și browser: [Reporting Tests](../../atf-reporting-tests/README.md).
- Integrarea consumerului: [README Practice Shop](../../atf-test-practice-shop/README.md).
- Modificarea testelor: [guvernanța ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).

## Cerințe deschise, în ordinea implementării

| Prioritate | Cerință încă deschisă | Responsabil | Dovada pentru închidere |
|---|---|---|---|
| P1 — PUB-01 | Limite măsurate pentru publicarea HTML voluminos cu PNG-uri inline | Reporting: pipeline și teste de componentă | Profiluri de volum/PNG distincte, buget de durată și failure-path fără promovarea unui artifact parțial |
| P1 — API-04 | Intenție lizibilă și publicare cauzală fără dubluri | Shop la intrare; Reporting la randare | Apeluri distincte, retry într-un exchange și republicare diferențiate prin fixtures executable |
| P1 — API-06 | Aserțiuni și verdicte atribuite canalului și exchange-ului corect | Reporting și contractul checkpoint-ului | UI PASS/API FAIL și invers, exchange-uri cu verdicturi diferite și agregare fără contaminarea fraților |
| P1 — UI-FAIL-01 | Failure UI generic, acționabil, în Extent și fallback | Reporting + `atf-reporting-tests`; consumer wiring în Practice Shop | Cauza, acțiunea/locatorul și timpul rămân sub ownerul FAIL; redacție, Fails/deep-link și probe negative. Extent browser pentru cele patru canale trece; fallback și matricea negativă rămân |
| P1 — GOV-API-01 | Un singur gate comun pentru structura, stilul și comportamentul API | `atf-tests` + `atf-reporting-tests` | Aceeași fixture prin publishers; probe negative structurale, de stil și ownership; gate-ul este obligatoriu în suită |
| P2 — PUB-FS-02 | Finalizare robustă când asset-ul temporar vendor este blocat pe Windows | Reporting: publication | Cleanup bounded, rezultat explicit și fără oprirea altor procese, artifact parțial promovat sau diagnostic suprimat |
| P2 — REPORT-PATH-01 | Afișarea automată a căii absolute pentru fiecare raport Extent publicat de teste | Reporting: semnal de finalizare; test infrastructure: hook JUnit; consumers: integrarea lifecycle-ului | Probe cu sink capturat confirmă un singur mesaj după publicare, inclusiv la teste eșuate și rapoarte partajate; nu se afișează cale de succes când publicarea eșuează |
| P2 — ASSET-SIZE-01 | Pachetul JavaScript publicat încape în baseline-ul existent, fără reducerea funcționalității | Reporting assets + component tests | `ReportAssetSizeInventoryTest` trece sub plafonul nemodificat de 125.000 B; baseline-ul curent este depășit cu 706 B și trebuie redus prin optimizare verificată |

### Implementare verificată și limite rămase

| Domeniu | Dovadă live | Stare |
|---|---|---|
| API-01 — disclosure | Expand/collapse-ul atomic și interacțiunile de bază sunt acoperite în componente UI/API. | Numai matricea reziduală cross-projection (owner selectat, frați și Fails) rămâne în gate-ul comun; nu este un ToDo independent. |
| API-02 — roluri | Rolurile typed sunt independente de titlu, iar o validare cu titlu personalizat este probată. | Combinațiile reziduale de metode, aggregate Network Inspector, copie selectată și Fails sunt consolidate în gate-ul comun; nu este un ToDo independent. |
| API-04 — intenție explicită | `ShopApiCallAuditReportComponentTest` (7/7) publică login-ul valid și cel invalid către aceeași rută cu intenții distincte; `ShopApiOperationTest` (5/5) probează override-ul și fallback-ul. | Parțial rezolvat: titlul folosește intenția declarată de contract, nu statusul HTTP sau deducții din rută. Rămân retry-un-singur-exchange, register → login fără republicare și identitatea izolată pe scenariu. |
| UI-FAIL-01 | `UiFailureReportContractTest` trece 4/4 suprafețe în Extent browser; `ExtentReportRuntimeEvidenceComponentTest` trece 3/3. Un defect de wiring omitea `appendUiFailureEvidence` pentru nodurile `validation` (Screen validation), deși le apela pentru `request`; acum failure leaf-ul nativ este adăugat în aceeași evidență, iar Request/Response fictive rămân interzise. | Parțial închis. Browserul Extent verifică Fails/deep-link, verdicturile, starea collapsed și failure leaf-ul pentru SETUP, acțiune, observație și assertion. Rămân fallback-ul, fixture-ul generic cu `TimeoutError`/locator/durată și probele negative pentru redacție, cauză absentă și contaminarea scenariilor-surori. |
| ASSET-SIZE-01 | Rulare izolată: `ReportAssetSizeInventoryTest` raportează JavaScript nativ 125.706 B față de 125.000 B; celelalte 3 probe din invocarea de 5 teste (viewport inclus) trec. | Deschis și reproductibil; pragul este păstrat. Optimizarea trebuie să elimine minimum 706 B fără pierdere de comportament, apoi se rerulează inventarul și browser contracts. |
| PUB-01 | `LargeReportPublicationComponentTest` a trecut în rularea țintită cu JVM copil `-Xmx384m`, 1.500 referințe PNG inline, două imagini distincte/repetate și artifact >9 MB. | Rămân volume mai mari și PNG-uri distincte mari, buget de durată și verificarea failure-path fără promovare parțială. |
| GOV-API-01 | Există verificări de artifact, ownership, API parity și browser; rulările țintite confirmă trasee pozitive, dar nu un gate comun complet. | Lipsesc fixture-urile negative și dovada că matricea completă rulează obligatoriu în suită; testele `contains` singure nu închid cerința. |
| PUB-FS-02 | Nu am găsit în probele de publication existente o simulare deterministă pentru fișier vendor blocat pe Windows. | Deschis; criteriul din backlog rămâne necesar înainte de închidere. |

### UI-FAIL-01 — cauza unei acțiuni Playwright eșuate trebuie să supraviețuiască în raport

Într-o rulare Practice Shop, `ShopAuthenticatedPurchaseE2eTest` apărea FAIL,
însă textul HTML nu conținea cauza din consola IntelliJ: `TimeoutError`,
`Timeout 20000ms exceeded` sau locatorul `button[data-test='login-submit']`.
Pierderea era pe calea de eșec a
fazei `SETUP`: wrapper-ul UI publica `Failure evidence` doar când exista
`TEST BODY`; înainte de acesta, setup-ul se închidea cu o aserțiune generică,
fără Throwable-ul original. Un setup deja închis putea apoi continua spre
ramura de `TEST BODY` inexistentă și masca excepția.

Starea verificată pentru failure evidence:

- Reporting are `failureEvidence(...)`, teste de publisher și disclosure pentru
  cauza tehnică.
- Consumer wiring-ul Shop pentru `SETUP` este acoperit separat, fără dependență
  inversă de clasele Shop.
- Proba neutră `UiFailureReportContractTest` există și trece 4/4 suprafețe în
  Extent browser. `ExtentReportRuntimeEvidenceComponentTest` trece 3/3.
- Cauza celor trei eșecuri anterioare a fost concretă: `Screen validation` este
  rolul `validation`, iar lifecycle-ul adăuga failure leaf numai la `request`.
  Hook-ul rulează acum pe ambele roluri; verificarea caută evidence sub ownerul
  efectiv, fără a cere un disclosure duplicat și fără HTTP inventat.

| Probă în `atf-reporting-tests` | Acceptanță |
|---|---|
| Fixture neutră cu excepție mock `TimeoutError` și acțiune `Wait for visible` FAIL | încă neacoperită de contractul generic curent: cele patru fixtures dovedesc rutarea pe canal, nu propagarea end-to-end a excepției cu mesaj, locator și durată. Adaugă și negativele de redacție/izolare înainte de închidere; nu inventa request/response API. |
| Extent și fallback, inclusiv Fails/deep-link | aceeași cauză este găsibilă la testul corect; rezumatul este compact implicit, iar detaliul tehnic se deschide la cerere |
| Redactare și negativ | parole/tokenuri din message sau target sunt redactate; un scenariu FAIL fără cauză lizibilă ori cu evidence atașată unui frate face proba să pice |
| Acțiune capturată și screenshot | tabela de acțiuni păstrează statusul FAIL și urmează politica existentă; screenshot-ul nu înlocuiește diagnosticul |

**Progres confirmat:**

- Traseul Shop `SETUP` publică Throwable-ul original prin `failureEvidence`.
- Componenta Shop verifică timeout-ul, locatorul, verdictul FAIL și redacția.
- Contractul browser generic Extent verifică failure leaf-ul sub ownerul
  `validation`, Fails și deep-link-ul pentru toate cele patru canale probate.
**Închidere:** aceeași probă trebuie să confirme Extent și fallback cu
`TimeoutError`, locator și durată, plus redacție negativă și izolare față de
scenarii-surori; fără HTTP inventat. Până atunci UI-FAIL-01 rămâne parțial.

- API-04 și API-06 rămân deschise pentru publicarea cauzală și ownership-ul
  verdictelor; nu sunt închise de paritatea câmpurilor sau de corectarea duratelor.
- Testele de șiruri sunt guard-uri auxiliare; probele de browser și
  artefactele generate decid închiderea.
- API-05 este promovat în documentația Reporting și Reporting Tests: matrice Extent/fallback, probe
  negative structurale, diagnostic redactat și izolarea SETUP/PASS de cauza
  TEST BODY/FAIL. Contractul este în README-urile Reporting și Reporting Tests;
  nu mai este un ToDo separat.
- API-08 și cardinalitatea etichetelor sunt promovate în
  [contractul de raportare](../ATF_Reporting_Contract.md#dovezile-unei-cereri) și
  [contractul de display și layout](../ATF_Reporting_Display_Contract.md) și
  [matricea responsive](../../atf-reporting-tests/README.md#verificarea-randării-responsive).

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
  păstrează ambele ancore, markerul final, tema și runtime-ul. Proba raportează
  vârful heap și cere exit code 0, fără E2E live; măsurătoarea unui run nu devine buget universal.
- **Rămâne de făcut:** praguri reprezentative pentru volume mai mari și PNG-uri
  mari/distincte, plus buget de durată calibrat; această fixture demonstrează
  doar profilul explicit de 384 MiB, nu scalarea generală a pipeline-ului.
- **Gate:** publicare completă și autonomă, capturi păstrate, exit code valid;
  failure-path fără artifact parțial promovat și fără pierderea diagnosticului.
  Optimizează copiile HTML numai după măsurare; nu trata creșterea heap-ului
  ca dovadă de închidere a acestei cerințe.

Schema API și paritatea de bază sunt verificate prin `ApiExchangeParityComponentTest`
și descrise în contractele semantic și display. Profilele mock/live sau
observație/contract rămân diferențe legitime, nu abateri de uniformitate.

## API-04 — intenție, identitate și cauzalitate

În audit, login-ul valid și cel invalid ajung la același `POST /users/login`.
Publisherul folosea descrierea generică `establish API session`, deși apelul
avea intenție declarată diferită. Contractul de audit transportă acum
`operationIntent` separat de aserțiunea HTTP, iar `publishObserved` îl folosește
numai pentru titlu; metoda, ruta și verdictul rămân date observate/contractate.

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

**Progres verificat:** `ShopApiCallAuditReportComponentTest` publică ambele
login-uri deterministe și verifică titlurile `sign in with valid credentials`
și `verify invalid credentials are rejected` sub aceeași metodă/rută. Acest
lucru închide numai diferențierea intenției; matricea de retry, republicare și
identitate rămâne deschisă.


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

## GOV-API-01 — gate comun rămas

Același contract trebuie verificat prin API direct, audit și UI+API în Extent
și fallback. Paritatea se compară pe aceeași fixture: se normalizează numai
identificatori, timestamp-uri și valori volatile; ordinea, rolul, ownership-ul,
disclosures și conținutul semantic trebuie să rămână egale.

| Dovadă încă lipsă | Criteriu de închidere |
|---|---|
| Structurală, prin porturile publice | Fixture-urile negative pentru disclosure lipsă/mutat, bandă de fază reintrodusă și evidence atașată ownerului greșit pică din motivul corect. |
| Browser și stil efectiv | Runtime-ul real verifică starea selectată, vizibilitatea copiilor după collapse, tastatura/ARIA, computed style și viewport; template-uri ascunse nu se numără ca probe. |
| Izolare verdict/diagnostic | UI PASS/API FAIL și invers, exchange-uri cu rezultate diferite și Fails/deep-link păstrează ownerul și cauza redactată; nu se fabrică HTTP pentru UI-only. |
| Participare obligatorie | Gate-ul este descoperit și executat în suita desemnată, fără skip, excludere sau zero teste. |

Contractele și probele rămân la ownerii lor:

- [Contractul Reporting](../ATF_Reporting_Contract.md) și
  [Contractul Display](../ATF_Reporting_Display_Contract.md) definesc regulile
  canonice.
- README-urile [Reporting](../../atf-reporting/README.md) și
  [Reporting Tests](../../atf-reporting-tests/README.md) descriu compoziția și
  probele.
- Testele generice folosesc fixture-uri neutre și porturile publice; integrarea
  specifică rămâne testată în Shop.
- Testele de guvernanță se modifică numai conform
  [procedurii ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).
- Feedback-ul nu repetă regulile deja livrate și păstrează numai gap-urile
  confirmate.

API-03, API-05, API-08, cardinalitatea Categories și TABLE-VIEWPORT-01 sunt
promovate ca documentație/contracte și rămân regresii, nu ToDo-uri. Închiderea
oricărui gap rămas cere test executat pe codul curent, verificare interactivă
unde este relevantă și probe negative care eșuează pentru încălcarea vizată.

## REPORT-PATH-01 — calea raportului după publicarea Extent

`ExtentReportSession.reportPath()` expune deja o cale absolută normalizată,
însă aceasta nu este afișată consecvent la consolă după generarea raportului.
Lifecycle-ul diferă între consumatori: unele teste închid sesiunea după fiecare
metodă, iar Practice Shop poate partaja aceeași sesiune între scenarii și o
închide la terminarea suitei. Un `@AfterAll` copiat în fiecare clasă ar putea
rula prea devreme, duplica mesajele sau rata raportul partajat.

**Cerință:** adaugă un hook JUnit reutilizabil, expus printr-o adnotare compusă
sau integrat în adnotările de test existente, astfel încât fiecare test care
generează un raport Extent să anunțe automat artifactul final:

Numele adnotării poate fi stabilit la implementare (de exemplu,
`@AtfReportOutput`); contractul este aplicarea automată și acoperirea completă,
nu un nume public ales înainte de verificarea extensiilor existente.

- afișează calea absolută și normalizată numai după ce sesiunea a fost închisă
  și publicarea artifactului a reușit;
- scrie o singură linie per raport finalizat, nu per checkpoint sau per test
  copil atunci când mai multe teste împart raportul suitei;
- rulează și când testul a eșuat, dacă teardown-ul a publicat raportul;
- nu anunță o cale ca succes dacă publicarea a eșuat și nu maschează excepția
  originală a testului;
- folosește `AtfConsoleSink`/facilitatea de consolă configurată; nu introduce
  `System.out`, `System.err` sau `println` în reporting, test-kit ori consumers;
- se aplică atât testelor E2E, cât și testelor de modul/integrări care creează
  rapoarte Extent; testele care doar citesc un raport existent nu emit anunț.

**Proiectare și ownership:** sesiunea Reporting rămâne sursa adevărului pentru
cale și rezultatul publicării; hook-ul JUnit aparține infrastructurii de test,
nu rendererului `atf-reporting`. Hook-ul trebuie notificat după închiderea reală
a resursei. Pentru rapoarte partajate, anunțul aparține proprietarului sesiunii
(de exemplu teardown-ul suitei), nu fiecărui test copil.

| Probă | Criteriu de acceptanță |
|---|---|
| Lifecycle un raport/metodă | Linia de consolă conține calea absolută normalizată și apare după ce fișierul final este publicat. |
| Test FAIL, publicare reușită | Calea raportului este afișată exact o dată, iar verdictul/excepția testului rămâne neschimbată. |
| Raport partajat de suită | Mai multe teste copil produc un singur anunț la închiderea sesiunii suitei. |
| Publicare eșuată sau fără raport | Nu apare un mesaj care pretinde că există un artifact finalizat; diagnosticul original este păstrat. |
| Acoperire automată | Guardrail-ul găsește testele JUnit care creează sesiuni Extent și confirmă că sunt acoperite de hook-ul reutilizabil, fără adnotări manuale inconsistente. |
| Ownership-ul consolei | Sink-ul capturat primește mesajul; verificarea arhitecturală continuă să interzică scrierea directă în stream-uri în afara `atf-console`. |

**Închidere:** probele de lifecycle trec pentru output UI și API, integrarea
acoperă cel puțin un raport per-test și unul partajat, iar raportul real poate fi
deschis din calea afișată. Până atunci, REPORT-PATH-01 rămâne deschis.
