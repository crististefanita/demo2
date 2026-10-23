# ATF Reporting — cerințe rămase

Acest feedback păstrează doar lacunele Reporting care nu sunt încă demonstrate
de cod și teste. Contractele stabile sunt în [contractul semantic](../ATF_Reporting_Contract.md),
[contractul de display și layout](../ATF_Reporting_Display_Contract.md) și
README-urile [ATF Reporting](../../atf-reporting/README.md) și
[ATF Reporting Tests](../../atf-reporting-tests/README.md). Nu păstrăm aici
istoricul remedierilor, capturi sau rezultate de run deja promovate.

## Ownership-ul testelor — regulă pentru toate cerințele de mai jos

| Tipul comportamentului testat | Modulul care deține testul | Forma preferată a probei |
|---|---|---|
| Model, markup, renderer, publisher, fixture API neutră, raport HTML/CSS/JS și comportament browser generic | `atf-reporting-tests` | Fixture deterministă/mocked, artifact generat local și browser probe numai când DOM-ul/CSS-ul ori interacțiunea sunt criteriul; fără endpoint live sau reguli de business Shop |
| Hook JUnit sau lifecycle reutilizabil al test-kit-ului | Modulul owner al hook-ului, în special `atf-test-kit` | Extensie JUnit cu sink și sesiune capturate; în `atf-reporting-tests` rămâne proba integrării hook-ului cu publicarea Reporting |
| Maparea unui consumer către contractul Reporting | Modulul consumerului | Test component/integration restrâns la wiring, configurare și ownership; nu se copiază aici testele generice ale rendererului |
| Flux end-to-end al unei aplicații | Modulul consumerului | Verifică journey-ul, nu substituie testele de componentă ale bibliotecii |

Auditul codului Shop nu justifică mutarea mecanică a testelor specifice.

- `ShopE2eFailureReportComponentTest` verifică adaptorul concret
  `ShopE2eContext → Reporting`; contractele generice pentru failure, redactare
  și randare sunt deja probate în `atf-reporting-tests`.
- Binding-ul checkpoint-urilor, redactarea identității Shop și metadata YAML
  rămân în Shop: depind de configurația și semantica acelui consumer.
- Dacă un test Shop verifică un invariant generic, mută întâi proba neutră în
  `atf-reporting-tests`; păstrează în Shop doar verificarea integrării proprii.

## Reconciliere live — 2026-10-23

- Comanda documentată pentru `AtfReportingComponentSuite` a fost rulată din reactor.
- Rezultat final după toate probele negative Request/Response: **135 teste,
  0 failures, 0 errors, 0 skipped**.
- `ApiReportRenderedEvidenceComponentTest`: 8/8; `UiSelectorBoundaryArchitectureTest`:
  2/2. Absența disclosure-ului și mutarea la ownerul scenariului sunt respinse
  pentru ambele canale, prin facade-ul `ReportUiLocators`.
- Un rezultat verde nu închide criterii neexercitate; gap-urile rămase sunt
  enumerate separat mai jos.

Sunt promovate și nu mai sunt ToDo-uri: contractele de API deja implementate,
cardinalitatea Categories, `TABLE-VIEWPORT-01`, failure evidence UI,
inventarul asset-urilor, publicarea bounded și cleanup-ul `spark`. Detaliile
reutilizabile sunt în cele două contracte Reporting și în
[README-ul testelor Reporting](../../atf-reporting-tests/README.md); nu se
reintroduc ca secțiuni istorice aici.

## Cerințe încă deschise

| Prioritate | ID | Lacună rămasă | Owner principal |
|---|---|---|---|
| P1 | `GOV-API-01` | Gate comun pentru contractul API semantic și randat: cazuri negative de structură/ownership, interacțiuni cross-projection și verdicte UI/API independente | `atf-reporting-tests` |
| P2 | `REPORT-PATH-01` | Anunțarea automată, o singură dată, a căii absolute după publicarea raportului Extent | Hook-ul aparține test infrastructure; semnalul/publicarea aparține Reporting |
| P2 | `SHOP-CAPTURE-01` | Selecție per clasă pentru un singur tabel complet de acțiuni UI și politica de captură finală într-un scenariu, inclusiv `SETUP` + `TEST BODY` | Integrare Shop; contractul generic în `atf-reporting-tests` |

### GOV-API-01 — gate comun, acoperire încă incompletă

Acoperirea existentă include `ApiReportContractAuditorTest`,
`ApiExchangeParityComponentTest`,
`ApiReportRenderedEvidenceComponentTest`,
`ReportApiViewportParityComponentTest` și
`EvidenceDisclosureAriaComponentTest`. `ReportingCoverageMatrixTest` verifică
prezența și layer-ul lor, iar `AtfReportingComponentSuite` le selectează.
Fixture-urile sunt controlate și nu cer serviciul API Practice Shop.

Probele negative randate confirmă că auditul respinge atât **Request**, cât și
**Response disclosure absent** sau mutat din cardul request-ului la owner-ul
scenariului. Testele țintite trec 8/8, iar mutările folosesc facade-ul
`ReportUiLocators`; gate-ul complet trece 135/135 după extinderea pentru Request.
Astfel, simpla prezență a JSON-ului în altă zonă nu satisface contractul
exchange-ului. Rămân de demonstrat explicit:

- izolarea între owneri/sibling și selecția unui owner care nu trebuie să
  preia dovezile altui scenariu;
- starea selectată și ciclul collapse/reopen în browser: copiii dispar din
  conținutul vizibil la collapse și reapar la reopen; markup ascuns/template nu
  este dovadă suficientă;
- verdicte independente și stabile pentru UI PASS/API FAIL, UI FAIL/API PASS și
  exchange-uri cu rezultate diferite; `Fails`/deep-link trebuie să deschidă
  ownerul corect cu cauza aferentă, iar un pas UI-only nu trebuie să inventeze
  exchange HTTP.
- probele browser din `support` folosesc exclusiv facade-ul de locatori existent;
  nu compun direct selectori Playwright și nu slăbesc testul arhitectural pentru
  a accepta ocolirea boundary-ului.

**Acceptanță:**

- Extinde fixture-ul neutru și probele din `atf-reporting-tests`; negativele
  trebuie să eșueze când structura sau ownerul deviază.
- Păstrează testul de model/renderer separat de browser proof și declară fiecare
  seam în matricea selectată de suita componentă.
- Rularea locală dovedește componenta, nu CI. Testul consumer Shop poate confirma
  wiring-ul, dar nu înlocuiește aceste probe.

### REPORT-PATH-01 — cale absolută după publicarea raportului

`ExtentReportSession.reportPath()` oferă deja o cale absolută normalizată,
însă nu există un hook reutilizabil care să anunțe automat la consolă raportul
finalizat. Cerința este pentru orice test care **deține publicarea** unui raport
Extent, inclusiv E2E, teste de modul și o suită care partajează sesiunea; un test
care doar citește un raport existent nu emite anunț.

- Notificarea apare numai după închiderea resursei și publicarea reușită; eșecul
  testului nu o suprimă dacă teardown-ul a publicat raportul.
- Se emite o singură linie pentru artifactul final. În sesiunea partajată,
  proprietarul sesiunii/suitei anunță, nu fiecare test copil.
- Publicarea eșuată nu este raportată ca succes și eroarea originală nu este
  mascată.
- Folosește `AtfConsoleSink`/canalul configurat; fără `System.out`, `System.err`
  sau `println` în Reporting, test-kit ori consumer.

**Acceptanță:** contractul căii și al evenimentului se testează în
`atf-reporting-tests`; lifecycle-ul adnotării/extensiei JUnit se testează în
modulul care o deține; integrarea consumerului verifică doar wiring-ul. Probe
capturate acoperă succes, test eșuat cu publicare reușită, raport partajat,
publicare eșuată și lipsa raportului. În output, calea trebuie să existe și să
fie deschizabilă după anunț.

### SHOP-CAPTURE-01 — profil de captură selectabil per clasă E2E

Setările generice `UNIFIED_FLOW` și `FINAL_ONLY` există, dar nu compun încă
contractul cerut peste întreg scenariul:

| Suprafață | Codul curent | Gap confirmat |
|---|---|---|
| Lifecycle | `UNIFIED_FLOW` este bufferizat/publicat în `UiPhaseLifecycle`; `SETUP` și `TEST BODY` au lifecycle separat | Un singur tabel nu traversează încă fazele scenariului |
| Adnotare | `WithE2eHarness` expune `reportFlow`, nu `screenshotPolicy` | Lipsește override-ul `FINAL_ONLY` per clasă |
| Compoziția Shop | `ShopE2eContext` reimpune politica YAML la deschiderea raportului | Selecția per clasă nu ajunge în profilul publicat |

**Cerință:** permite selectarea adnotată, fără adnotare concurentă inutilă, a
`UNIFIED_FLOW` + `FINAL_ONLY`; `CONFIGURED` păstrează YAML. Rezoluția este
deterministă înainte de deschiderea raportului și se reflectă în Overview/System.

- Toate acțiunile UI din `SETUP` și `TEST BODY` apar o singură dată, în ordine,
  într-un singur tabel per scenariu; fiecare rând păstrează faza. Ierarhia,
  verdicturile și aserțiunile fazelor rămân distincte.
- Se publică numai captura terminală când scenariul ajunge la ea; nu se inventează
  captură finală dacă setup-ul sau flow-ul eșuează înainte. Politica de captură
  la failure rămâne explicită.
- `UI_API` păstrează validările și exchange-urile API la checkpoint-ul lor;
  acțiunile agregate nu duplică, mută sau elimină dovezi API.
- În sesiunea partajată a suitei, selecțiile incompatibile sunt respinse în
  preflight înaintea browserului și a raportului.

**Acceptanță și ownership:** `atf-reporting-tests` folosește fixture neutră cu
mai multe faze pentru ordinea acțiunilor, deduplicare, capturi și păstrarea
dovezii API; browser proof-ul verifică artifactul generat. Shop păstrează numai
testul de rezolvare a adnotării/configurației și preflight-ul sesiunii partajate.
Acoperă YAML implicit, override per clasă, UI-only, UI_API, succes și failure
înainte de terminal; nu adăuga business flow Shop la fixture-ul generic.

## Închiderea feedback-ului

O cerință se elimină numai după ce criteriul ei trece prin testul din modulul
owner și, când randarea/interacțiunea este criteriu, prin browser pe artifact
proaspăt. Contractul stabil se promovează mai întâi în README-ul/contractul
oficial relevant; rămânerea unei integrări înguste în consumer nu ține deschis
un ToDo generic deja acoperit.
