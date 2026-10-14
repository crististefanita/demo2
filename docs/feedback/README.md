# ATF Java — feedback activ

Acest director păstrează exclusiv lucrările care mai trebuie implementate sau
întărite prin teste în codul live ATF Java. Contractele deja stabilizate,
explicațiile arhitecturale și diagramele normative apar în `docs/` și în
README-urile modulelor; nu sunt duplicate aici.

## Proces obligatoriu pentru orice ToDo

1. Recitește codul, testele, POM-urile și README-urile din checkout-ul live.
2. Creează un staging nou, separat de checkout și de orice rulare existentă;
   nu opri procesele și nu reutiliza `target`, `artifacts`, `.tmp` sau loguri.
3. Adaugă ori extinde testul care exprimă contractul înaintea refactorizării.
4. Repară implementarea; nu relaxa și nu rescrie testul pentru a ascunde o
   regresie.
5. Rulează reactorul relevant și verifică faptul că snapshot-ul staged nu a
   devenit învechit față de live în timpul buildului.
6. Pentru orice schimbare de raportare, generează artifactul în staging și
   verifică-l în TAB MCP: Overview, Tests, Category, Fails, filtre, collapse,
   deep-links, durate, statusuri și computed styles relevante.
7. Mută regula stabilă în README-ul normativ potrivit și elimină ToDo-ul când
   implementarea, testele și dovada observabilă există.

## ToDo-uri active

### F-01 — Finalizează separarea temei și a controllerelor de UI

Descompune orchestration-ul rămas din `injectRuntimeBehaviour` în componente
mici pentru navigation/hash, filtre, collapse și contractul DOM. Separă în
continuare layout, execution și responsive CSS; `AtfReportTheme` trebuie să
rămână fațadă de compoziție, iar tokenurile comune să fie reutilizate în toate
proiecțiile.

Teste necesare: ordine de compoziție a pipeline-ului, filtre, collapse,
deep-links, click pe tab-uri, computed styles pentru badge-uri și suprafețe,
plus independența rulărilor paralele.

Increment transversal verificat: `ExtentEvidenceRenderer` deține acum evidence-ul Extent
legat de request (technical trace/details, JSON, execution/retry history și
assertions), iar `ExtentReportTree` a rămas coordinator pentru arbore,
lifecycle și status aggregation. Markup-ul observabil a rămas neschimbat.

Increment nou verificat: `ExtentReportRuntimeBehaviour` a preluat bootstrap-ul
JavaScript din `ExtentReportPublicationPipeline` și compune explicit runtime-ul
de durate, diagnostic failures și cause-chain. Pipeline-ul păstrează numai
ordinea de publicare și scrierea atomică. `ExtentReportRuntimeScriptTest` fixează
markerii și ordinea compoziției; comportamentul DOM rămâne protejat de testele
de interacțiune și artifact care urmează să fie extinse în F-05. F-01 nu este
închis: navigation/hash, filters, collapse și DOM contract sunt încă în
bootstrap și vor fi extrase incremental.

### F-02 — Folosește un model semantic comun pentru renderer-e

Extinde modelul intern comun pentru suite → class → scenario → phase →
request/validation → evidence și fă Extent-ul și fallback-ul consumatori ai
aceluiași snapshot semantic. Nu muta reguli de business, verdict sau redaction
în renderer.

Test necesar: aceeași fixture cu pass, warning, fail, skip, tags, trace, JSON
și assertions trebuie să producă aceeași ierarhie, aceleași statusuri/count-uri
și aceleași limite de redaction în ambele renderer-e.

### F-03 — Completează snapshot-ul immutable de runtime

Folosește schema actuală din `atf.yaml` ca sursă unică de proiect pentru API,
validation și reporting, fără modul nou `atf-config` și fără să muți parserul în `atf-core`.
În repository-ul curent, loaderul complet aparține composition root-ului din
`atf-tests`/proiectul consumator; `atf-reporting` păstrează doar loaderul
specializat pentru `reporting.extent`. Bootstrap-ul trebuie să încarce o singură dată YAML-ul, să valideze strict schema și să producă un
snapshot typed pentru timeout, retry, auth, routes, redaction, schema roots și
publication policy. `atf-core` păstrează numai modele/snapshot-uri; reporting
adaptează exclusiv `reporting.extent`.

Elimină fallback-ul operațional la `user.dir`; paths relative se rezolvă față
de fișierul YAML, iar secretele vin din provider/env explicit, nu din YAML.

Teste necesare: două working directories, două root-uri concurente, precedence
property/env/YAML/default, chei necunoscute, URI/timeout/path invalid, schema
validă/absentă/invalidă, traversal respins și imutabilitate după crearea
executorului.

Increment implementat: `AtfReportConfigurationContext` capturează o singură
dată rădăcina de căutare și sursele explicite property/environment, iar
`AtfReportConfigurationLoader.load(context)` parsează numai contextul primit.
Precedence-ul property → environment și izolarea dintre două rădăcini sunt
protejate prin teste unitare. Fațada `load()` rămâne compatibilă și este
singurul loc care capturează starea procesului; snapshot-ul complet de runtime
(timeout/retry/auth/redaction/publication) rămâne următorul increment F-03.

Increment nou verificat: calea explicită relativă se rezolvă față de
`startDirectory` capturat, iar `outputDirectory` este normalizat față de YAML și
respinge traversal-ul în afara directorului de configurație. Testele
`resolvesRelativeExplicitConfigAgainstTheCapturedStartDirectory` și
`rejectsOutputDirectoryTraversalOutsideTheYamlDirectory` fixează aceste
invariante fără să schimbe starea procesului.

### F-04 — Închide propagarea completă a failure cause chain

Păstrează cauza primară și cauzele secundare în toate căile de retry, teardown,
Fails și artifact evidence, fără duplicarea arborelui Tests. Redaction-ul
trebuie aplicat înainte de publicare și să nu modifice input-ul original.

Teste necesare: failure după retry, failure de teardown după failure în body,
cause chain în Fails, URL/headers/body redacted și input immutable.

Increment nou verificat: `ExtentReportFailureRuntime` proiectează cauzele
`PRIMARY` și `SECONDARY` din fazele failure în Fails, iar cauza este redată
după redaction la limita phase evidence. Testele runtime/session verifică
markerii, ordinea și redaction-ul; rămâne de demonstrat în artifact combinația
completă body failure + teardown failure provenită dintr-o execuție reală.

### F-05 — Ridică verificarea raportului de la string la comportament DOM

Adaugă un test component headless/browserless pentru artifactul autonom.
Păstrează testele statice pentru invariants, dar dovedește comportamentul după
interacțiune: Overview → Tests → Category → Fails, filtre de status/tag,
hash deep-links, expand/collapse, `Open in Tests`, count-uri și durate cu `ms`.

Testul trebuie să verifice și: badge geometry comună, background uniform,
centrare status/durată, timestamp absent din tabelul Tests, plus request și
validation ca frați direcți.

### F-06 — Întărește boundary checks și ownership-ul pachetelor

Extinde verificarea arhitecturală peste token scan: importuri și dependențe
Maven, composition roots, state static mutabil, ownership și allow-list-uri.
Interzice Extent/JUnit în modulele renderer-neutral și HTTP/business validation
în reporting. Pragul de maximum 8 clase de producție per pachet rămâne semnal
de revizuire a coeziunii, nu motiv pentru directoare artificiale.

Teste necesare: POM interzis, import interzis, fixture intenționat invalid,
numărare pe package și verificarea composition root-ului.

Increment nou verificat: `ArchitectureBoundaryTest` interzice reporting-ului
dependențe directe spre execution/validation și detectează câmpuri statice
mutabile în modulele de producție. Testele rulează în reactorul staged; regula
de numărare pe package și allow-list-ul complet rămân de extins incremental.

## Criteriul de închidere

Un ToDo dispare numai când există owner clar, schimbare minimă, test unitar
determinist, test component când este necesar, build staged și dovadă în TAB
MCP pentru schimbările observabile. Niciun test existent nu este eliminat sau
relaxat pentru a face build-ul verde.

## Documente specializate

- [ATF_Java_Improvement_Backlog.md](ATF_Java_Improvement_Backlog.md) — ordinea
  tehnică și matricea de teste pentru ToDo-urile mari.
- [ATF_Reporting_Duration_Semantics.md](ATF_Reporting_Duration_Semantics.md) —
  durate, agregare structurală și badge-uri.
- [ATF_Architecture.md](../ATF_Architecture.md) — boundary-urile normative.
- [ATF_Reporting_Contract.md](../ATF_Reporting_Contract.md) — contractul
  observabil al artifactului.
- [ATF_BUILD_AND_EXECUTION_SAFETY.md](../ATF_BUILD_AND_EXECUTION_SAFETY.md) —
  sync live, staging și execuții paralele.
