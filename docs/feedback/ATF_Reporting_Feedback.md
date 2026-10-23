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
| P1 — GOV-API-01 | Un singur gate comun pentru structura, stilul și comportamentul API | `atf-reporting-tests` | Aceeași fixture prin publishers; probe negative structurale, de stil și ownership; gate-ul este obligatoriu în suita componentă Reporting |
| P2 — REPORT-PATH-01 | Afișarea automată a căii absolute pentru fiecare raport Extent publicat de teste | Reporting: semnal de finalizare; test infrastructure: hook JUnit; consumers: integrarea lifecycle-ului | Probe cu sink capturat confirmă un singur mesaj după publicare, inclusiv la teste eșuate și rapoarte partajate; nu se afișează cale de succes când publicarea eșuează |
| P2 — SHOP-CAPTURE-01 | Selectarea per clasă a unui singur screenshot final și a unui tabel cu toate acțiunile UI ale scenariului E2E | Practice Shop: adnotare/harness; Reporting: agregare și profile; `atf-reporting-tests`: contract generic | Proba confirmă acțiuni complete, ordonate și ne-dublate într-un singur tabel per scenariu, inclusiv peste fazele SETUP/TEST BODY, plus exact o captură terminală; verdicturile și dovezile API rămân intacte |

### Implementare verificată și limite rămase

| Domeniu | Dovadă live | Stare |
|---|---|---|
| API-01 — disclosure | Expand/collapse-ul atomic și interacțiunile de bază sunt acoperite în componente UI/API. | Numai matricea reziduală cross-projection (owner selectat, frați și Fails) rămâne în gate-ul comun; nu este un ToDo independent. |
| API-02 — roluri | Rolurile typed sunt independente de titlu, iar o validare cu titlu personalizat este probată. | Combinațiile reziduale de metode, aggregate Network Inspector, copie selectată și Fails sunt consolidate în gate-ul comun; nu este un ToDo independent. |
| UI-FAIL-01 | Contract browser Extent: 4/4 suprafețe și izolare sibling; `ExtentReportUiFailureEvidenceTest` 9/9; `UiFallbackFailureEvidenceTest` 1/1; proiecția UI 3/3. Invocarea combinată actuală, împreună cu probele PUB-01: 26/26. | Închis. Timeout/cauză, locator, durata, redacția cauzei și targetului, Fails/deep-link, izolarea sibling și absența disclosure-urilor HTTP false au probe executabile. |
| ASSET-SIZE-01 | Inventory nativ/publicare în limita baseline-urilor: JavaScript nativ 124.950 B față de 125.000 B; testele inventory/budget, viewport, disclosure și browser trec 23/23. | Închis. Explicațiile asset-urilor au fost mutate în README; plafonul nu a fost schimbat și logica executabilă a rămas neschimbată. |
| PUB-01 | `LargeReportPublicationComponentTest` are două profile bounded-heap, iar `ExtentReportSessionLifecycleTest` verifică failure-path-ul fără promovare parțială. Patru rulări locale: text/HTML 10.539–12.704 ms și PNG distinct 710–933 ms; heap total eșantionat maxim 236.800.048 B și 81.315.680 B. | Închis pentru profilele măsurate. Timeout configurabil 60 s (peste 4× maximul observat) și JVM copil `-Xmx384m`; limita nu este SLA pentru alte host-uri și nu pretinde fotografii de rezoluție foarte mare. |
| GOV-API-01 | `ReportingCoverageMatrixTest` mapează acum explicit contractul API direct, aceeași fixture prin API/fallback/UI, reconcilierea browser a query-ului/fazei/ownership-ului, viewport-ul API și ARIA/keyboard. Gate-ul verifică package-urile și tag-ul `component` selectate de `AtfReportingComponentSuite`. Rularea explicită a suitei a executat agregat 131 teste: 0 failures, 0 errors, 0 skipped. | **Parțial.** Mai lipsesc negative dedicate pentru disclosure API absent/mutat și evidence la owner greșit, plus matricea mixed UI/API verdict–Fails/deep-link. Suita a rulat cu probele disponibile, dar acestea nu acoperă încă fiecare criteriu de acceptanță; cerința rămâne deschisă. |
| PUB-FS-02 | `ExtentReportAssetCleanupTest` (2/2) simulează `AccessDeniedException`: retry tranzitoriu reușește la a treia încercare; blocarea persistentă se oprește la 4 încercări/3 pauze și păstrează fișierul. `ExtentReportSessionLifecycleTest` (9/9) injectează cleanup failure după promovare și verifică HTML complet, fără `.tmp`, cu diagnosticul păstrat. | Închis. Retry limitat la `AccessDeniedException` (4 încercări, pauze de 50 ms); alte erori se propagă imediat. Guard-ul acceptă doar directorul generat `spark`; nu oprește procese externe și nu maschează cleanup failure. |

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
- Proba neutră `UiFailureReportContractTest` este annotation-driven și trece
  4/4 suprafețe în Extent browser; acțiunea Playwright este acum un timeout de
  20 s, cu locator și mesaj redactat. `ExtentReportRuntimeEvidenceComponentTest`
  trece 3/3.
- Cauza celor trei eșecuri anterioare a fost concretă: `Screen validation` este
  rolul `validation`, iar lifecycle-ul adăuga failure leaf numai la `request`.
  Hook-ul rulează acum pe ambele roluri; verificarea caută evidence sub ownerul
  efectiv, fără a cere un disclosure duplicat și fără HTTP inventat.

| Probă în `atf-reporting-tests` | Acceptanță |
|---|---|
| Fixture neutră cu excepție mock `TimeoutError` și acțiune `Wait for visible` FAIL | `ExtentReportUiFailureEvidenceTest` (6/6), `UiFallbackFailureEvidenceTest` (1/1) și fixture-ul annotation-driven `UiFailureReportContractTest` păstrează clasa/cauza timeout-ului, locatorul și durata; elimină parola/Bearer brut. Contractul browser confirmă și că UI failure nu produce disclosure Request/Response. |
| Extent și fallback, inclusiv Fails/deep-link | Extent browser păstrează Fails/deep-link pentru cele patru canale; timeout-ul apare în exact un scenariu și nu contaminează scenariile sibling. Fallback component verifică aceeași izolare. |
| Redactare și negativ | Parola/Bearer din message sunt redactate în Extent și fallback; secretul de query din target este redactat în detaliul requestului și în tabelul acțiunilor. Dacă Throwable lipsește, cauza se derivă din prima acțiune FAIL; dacă lipsește și acțiunea FAIL, publicarea respinge evidence-ul. Toate cazurile au probe. |
| Acțiune capturată și screenshot | tabela de acțiuni păstrează statusul FAIL și urmează politica existentă; screenshot-ul nu înlocuiește diagnosticul |

**Progres confirmat:**

- Traseul Shop `SETUP` publică Throwable-ul original prin `failureEvidence`.
- Componenta Shop verifică timeout-ul, locatorul, verdictul FAIL și redacția.
- Contractul browser generic Extent verifică failure leaf-ul sub ownerul
  `validation`, Fails și deep-link-ul pentru toate cele patru canale probate.
- Componentele UI Extent și fallback păstrează acum un `TimeoutError` sintetic,
  locatorul `button[data-test='login-submit']` și durata `20000ms`; mesajul
  redactează `password` și Bearer token. Fallback-ul verifică link-ul Fails
  către ID-ul scenariului și absența cauzei din articolul scenariului-frate.
- Fixture-ul annotation-driven Extent folosește acum același tip de timeout și
  verifică în browser action/locator/durată, redacția vizibilă în Fails și zero
  disclosure-uri HTTP fabricate.
- Renderer-ul acțiunilor folosește acum redactorul Reporting pentru target,
  expected și observed; o probă cu `access_token` confirmă că valoarea brută nu
  apare în HTML, iar selectorul sigur rămâne util.
- Fără Throwable, publisher-ul derivează diagnosticul din prima acțiune FAIL;
  fără Throwable și fără acțiune FAIL respinge apelul, în loc să publice o
  cauză generică neacționabilă.
- Inspectorul browser verifică acum că fixture-ul conține sibling-uri, că
  timeout-ul și locatorul apar împreună într-un singur scenariu și că niciun
  sibling nu conține cauza sau credentialele brute. Proba rulează în suprafața
  `PLAYWRIGHT_ACTION` a contractului annotation-driven și trece 4/4 cazuri.
**Închidere:** rularea combinată curentă a publisherului Extent, fallback-ului,
proiecției UI, contractului browser și probelor de publicare PUB-01 a trecut
26/26 teste; browser-contract-ul a trecut 4/4, inclusiv verificarea sibling.
UI-FAIL-01 este închis.

- Testele de șiruri sunt guard-uri auxiliare; probele de browser și
  artefactele generate decid închiderea.

### PUB-01 — profilele bounded-heap și bugetul temporal

- **Cauză confirmată:** heap-ul de 256 MiB al fork-ului Surefire poate fi epuizat
  în `ExtentReportPublicationPipeline.ensureViewIdentifiersAndOverviewDefault`,
  la `String.replaceFirst`, după terminarea journey-urilor.
- **Mecanism:** pipeline-ul citește și transformă întregul HTML în mai multe
  etape; deduplicarea PNG-urilor are loc abia după aceste transformări.
  Dimensiunea fișierului final nu este bugetul de heap necesar.
- **Remediere de configurație:** bugetul implicit este 2 GiB per fork,
  configurabil prin `atf.test.heap.max`; nu schimbăm politica de captură și
  nu dezactivăm fork-ul. Configurația nu dovedește scalabilitatea pipeline-ului.
- **Profil text/HTML:** JVM separat cu `-Xmx384m`, 1.500 referințe PNG inline
  (două payload-uri distincte repetate) și text Unicode; artifactul final
  depășește 9 MB și păstrează markerul final, tema, runtime-ul și ancorele.
- **Profil media:** patru PNG-uri generate distincte 256×256, fiecare referit de
  două ori. După deduplicare, toate cele patru payload-uri rămân acționabile,
  iar payload-urile inline depășesc împreună 900.000 caractere. Profilul separă
  costul media de volumul Unicode al probei text.
- **Măsurare:** heap-ul total este eșantionat la intervale de 2 ms în timpul
  publicării (nu se însumează vârfurile pool-urilor JVM). În patru rulări locale,
  profilul text a durat 10.539–12.704 ms, cu maximum observat de 236.800.048 B;
  profilul media a durat 710–933 ms, cu maximum observat de 81.315.680 B.
- **Timeout calibrat:** fiecare JVM copil are implicit 60 s, configurabil prin
  `atf.test.publication.timeout.seconds`; limita este de peste 4× cea mai lentă
  măsurătoare locală. La depășire, doar procesul copil este terminat. Nu este
  un SLA portabil între host-uri.
- **Failure-path verificat:** `ExtentReportSessionLifecycleTest` încearcă
  publicarea către o destinație care este director și conține un sentinel.
  Închiderea eșuează explicit, destinația și sentinelul rămân intacte, iar
  workspace-ul privat `.atf-extent-session-*` este eliminat. Profilele de
  publicare și lifecycle au trecut împreună 10/10 teste.
- **Închidere și limită:** PUB-01 este închis pentru profilele declarate și
  măsurate. Volume arbitrare și screenshot-uri de rezoluție mare rămân în afara
  promisiunii; nu creștem heap-ul și nu reducem capturile pentru a face proba să
  treacă. Orice profil nou cere măsurători repetate înainte de a modifica timeout-ul.

### PUB-FS-02 — cleanup bounded pentru asset-uri vendor blocate

- Cleanup-ul vizează numai directorul generat cu numele `spark`; căile din afara
  lui sunt no-op. Pentru `AccessDeniedException`, operația încearcă cel mult de
  patru ori, cu trei pauze de 50 ms. Nu termină procese și nu folosește ștergere
  recursivă externă.
- `ExtentReportAssetCleanupTest` probează determinist două cazuri: acces refuzat
  tranzitoriu, urmat de ștergere reușită, și blocare persistentă care produce un
  `IOException` cu calea și numărul încercărilor; în al doilea caz fișierul
  blocat rămâne prezent.
- Proba de lifecycle injectează cleanup failure după scrierea atomică și confirmă
  că artifactul HTML rămâne complet, fișierul temporar de publicare este eliminat,
  iar excepția nu este suprimată. Rularea țintită a asset-urilor, profilelor de
  volum, cleanup-ului și lifecycle-ului a trecut 19/19 teste.
- **Închidere:** PUB-FS-02 este închis pentru contractul `AccessDeniedException`
  simulat determinist; nu afirmă că fiecare antivirus sau driver Windows expune
  toate tipurile de lock prin aceeași excepție.

Schema API și paritatea de bază sunt verificate prin `ApiExchangeParityComponentTest`
și descrise în contractele semantic și display. Profilele mock/live sau
observație/contract rămân diferențe legitime, nu abateri de uniformitate.

## GOV-API-01 — gate comun, acoperire încă incompletă

Același contract trebuie verificat prin API direct, audit și UI+API în Extent
și fallback. Paritatea se compară pe aceeași fixture: se normalizează numai
identificatori, timestamp-uri și valori volatile; ordinea, rolul, ownership-ul,
disclosures și conținutul semantic trebuie să rămână egale.

| Dovadă încă lipsă | Criteriu de închidere |
|---|---|
| Structurală, prin porturile publice | Rămân negative dedicate pentru disclosure API lipsă/mutat și evidence atașată ownerului greșit. Testul existent de reconciliere API acoperă query-ul lipsă și faza absentă, nu aceste defecte. |
| Browser și stil efectiv | Paritatea API este inclusă în suita componentă; probele existente acoperă navigarea/rendering-ul, ARIA + keyboard și computed viewport overflow pentru tabele API. Rămâne de probat explicit starea selectată și vizibilitatea copiilor după collapse în matricea cross-projection; template-urile ascunse nu sunt acceptate ca dovadă. |
| Izolare verdict/diagnostic | Rămân UI PASS/API FAIL și invers, exchange-uri cu rezultate diferite și Fails/deep-link care păstrează ownerul și cauza redactată; nu se fabrică HTTP pentru UI-only. |
| Participare obligatorie | **Acoperit pentru entry point-ul declarat:** testul verifică package-urile, tag-ul `component` și includerea celor cinci probe API în `AtfReportingComponentSuite`; rularea explicită a suitei a raportat agregat 131 teste, fără failures sau skip. Integrarea într-un pipeline CI rămâne în afara dovezii locale. |

Probe legate în gate: `ApiReportContractAuditorTest`,
`ApiExchangeParityComponentTest`, `ApiReportRenderedEvidenceComponentTest`,
`ReportApiViewportParityComponentTest` și `EvidenceDisclosureAriaComponentTest`.
Coverage-ul suitei este verificat de `ReportingCoverageMatrixTest`; rularea
țintită locală a folosit comanda documentată în README-ul Reporting Tests.

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

## SHOP-CAPTURE-01 — un singur tabel de acțiuni UI și captura finală selectate per clasă

Mecanismele generice există deja: `ReportEvidenceFlowMode.UNIFIED_FLOW`
agregă acțiunile UI în interiorul unui `UiPhase`, iar
`ReportScreenshotPolicy.FINAL_ONLY` păstrează numai captura terminală.
Totuși, `UNIFIED_FLOW` nu garantează un singur tabel pentru un scenariu cu faze
distincte `SETUP` și `TEST BODY`. În plus, selecția de captură pe clasa E2E
Practice Shop lipsește: `@WithE2eHarness.reportFlow` poate alege forma
unificată, dar `reporting.screenshotPolicy` din YAML rămâne autoritar și
adnotarea nu poate alege `FINAL_ONLY`.

**Cerință:** extinde adnotarea/harness-ul existent (preferabil, fără o a doua
adnotare concurentă) astfel încât o clasă de test să poată cere explicit un
screenshot la starea finală și un singur tabel `Playwright action sequence`
per scenariu E2E, cu toate acțiunile UI ale scenariului.

- Valoarea implicită `CONFIGURED` păstrează comportamentul YAML curent; alegerea
  explicită `FINAL_ONLY` se aplică testului/clasei și se reflectă în profilul
  rezolvat și în Overview → System.
- Profilul combină `UNIFIED_FLOW` cu `FINAL_ONLY`: toate acțiunile UI ale
  scenariului, inclusiv acțiunile din `SETUP` și `TEST BODY`, apar o singură
  dată și în ordine într-un tabel per scenariu. Rândurile păstrează contextul
  fazei; acțiunile nu se pierd, repetă sau convertesc în pași Gherkin.
- Un raport cu mai multe scenarii păstrează câte un tabel per scenariu; nu
  amestecă acțiuni între teste. Ierarhia și verdicturile `SETUP`/`TEST BODY`
  rămân distincte, chiar dacă tabelul acțiunilor este agregat la nivel de scenariu.
- O singură imagine se atașează la checkpoint-ul terminal `FINAL`; checkpoint-urile
  intermediare nu primesc imagini. Dacă testul nu ajunge la starea finală,
  raportul nu inventează o captură finală; politica de captură la failure rămâne
  distinctă și explicită.
- Canalul `UI_API` păstrează `API validation` și exchange-urile sub checkpoint-ul
  lor; politica de imagine și unificarea acțiunilor nu mută, elimină sau
  dublează evidence-ul API. Verdicturile, aserțiunile, mascarea câmpurilor și
  journey-ul de business rămân neschimbate.
- Rularea individuală poate folosi selecția clasei. Într-o sesiune partajată de
  suită, politica efectivă se fixează înainte de deschiderea raportului; clasele
  participante trebuie să aibă aceeași selecție explicită sau conflictul este
  respins clar, nu rezolvat prin ordinea accidentală a testelor.

| Probă | Criteriu de acceptanță |
|---|---|
| Rezolvarea profilului | Test component verifică `CONFIGURED` → YAML și override-ul adnotat `FINAL_ONLY`, inclusiv combinația cu `UNIFIED_FLOW`. |
| Contract Reporting | Fixture neutră cu `SETUP` și `TEST BODY` confirmă un singur tabel per scenariu, ordinea și atribuirea fazei pentru fiecare acțiune, fără duplicate, plus numai captura `FINAL`; `EACH_CHECKPOINT` rămâne neschimbat. |
| Browser / artifact | Raport proaspăt confirmă tabelul unic pentru toate acțiunile scenariului, fără imagini intermediare și cu exact o captură finală când terminalul este atins. |
| UI/API și failure | UI-only și UI_API păstrează verdicturile și ownership-ul; API evidence rămâne soră, iar eșecul înainte de terminal nu este prezentat ca succes cu imagine finală. |
| Sesiune de suită | O suită cu profil coerent publică o singură configurație; profiluri explicite contradictorii eșuează în preflight înainte de browser și raport. |

**Închidere:** testele de componentă pentru profil și renderer, plus verificarea
browserului pe artifact generat, trec pentru YAML implicit, override per clasă,
UI-only, UI_API și scenariu cu faze multiple. Până atunci, SHOP-CAPTURE-01
rămâne deschis; `UNIFIED_FLOW` în interiorul fazei și `FINAL_ONLY` existente
nu sunt redeschise ca gap-uri separate.
