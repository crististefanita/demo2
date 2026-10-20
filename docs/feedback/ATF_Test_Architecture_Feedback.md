# ATF Java — feedback pentru structura testelor și plug-and-play

## Verdict executiv

**Scor de maturitate structurală: 88/100.** Contractul de layer, ownership-ul
modulelor și traseul de onboarding sunt acum coerente. Singura limită rămasă
este rafinarea semantică a zonelor `support`; nu există motiv pentru ștergeri
speculative sau pentru mutarea mecanică a tuturor testelor în directoare noi.

Feedback-ul descrie codul live și păstrează numai gap-uri care pot schimba
structura, testele sau contractele publice. Numărătorile exacte se obțin prin
build și guard-uri, nu se păstrează ca jurnal în acest document.

## Contracte confirmate în live

| Contract | Owner | Verdict |
|---|---|---|
| `unit`, `component`, `integration`, `e2e` | `atf-test-kit` | ✅ adnotări publice și un guard transversal |
| layer-ul testului | clasa executabilă | ✅ adnotarea este sursa de adevăr; path-ul este doar diagnostic |
| `support` | fiecare composition root | ✅ fixture, builder, loader, adapter, locator; nu este layer executabil |
| reporting standalone | `atf-reporting` | ✅ independent; testele lui au excepție formală, finită și verificată |
| reporting composition | `atf-reporting-tests` | ✅ API/UI/E2E pentru librărie, nu pentru o aplicație de business |
| Practice Shop | `atf-test-practice-shop` | ✅ config, locatori, pages, flows și purchase E2E |
| whole-ATF governance | `atf-tests` | ✅ guard-urile sunt active și nu mai există dependency production `atf-console → atf-reporting`; adapterul cross-port este test-only în `atf-reporting-tests` |
| overview arhitectural | root `README.md` | ✅ subțire, top-down, cu module map și linkuri către owneri |

## Matricea de testare

<details>
<summary><strong>Matricea detaliată module × layer</strong></summary>

| Modul | Unit | Component | Integration | E2E | Limită |
|---|---:|---:|---:|---:|---|
| `atf-core` | ✅ | — | — | — | infrastructură generică |
| `atf-validation` | ✅ | — | — | — | contracte de verdict |
| `atf-console` | ✅ | ✅ | — | — | proiecție diagnostică; port renderer-neutral fără dependency către reporting |
| `atf-mock` | — | ✅ | — | — | transport fake |
| `atf-ui` | — | ✅ | — | — | boundary Playwright generic |
| `atf-debug-ui` | — | ✅ | — | — | inspector read-only |
| `atf-reporting` | independent | — | — | — | librărie Maven standalone |
| `atf-reporting-tests` | ✅ | ✅ | ✅ | ✅ | consumer/reference composition root |
| `atf-test-practice-shop` | — | ✅ | — | ✅ | aplicație consumer |
| `atf-tests` | — | ✅ | — | — | guvernanță whole-reactor |
| `atf-test-kit` | contracte | contracte | contracte | contracte | verifică propriul vocabular |

</details>

Matricea de mai sus este o hartă de responsabilități. Guard-ul din
`atf-test-kit` reconstruiește matricea din clasele executabile și adnotările lor;
nu există count-uri hardcodate sau directoare goale create artificial.

## Închideri promovate în acest increment

| ID | Rezultat | Dovadă stabilă |
|---|---|---|
| `TEST-ARCH-01` | ✅ politica pentru testele standalone este formală și finită | `AtfTestLayerArchitectureTest` verifică POM parentless, lipsa dependențelor ATF și cele două clase publice permise |
| `TEST-ARCH-02` | ✅ adnotarea este sursa layer-ului | guard-ul cere exact o adnotare pentru fiecare test executabil non-standalone; nu mai deduce layer-ul din path |
| `TEST-ARCH-03` | ✅ ownership reconciliat | root README, `atf-reporting-tests/README.md`, `atf-tests/README.md` și Practice Shop README folosesc aceeași separare |
| `TEST-ARCH-04` | ✅ matricea module × layer este derivată automat | testul de guvernanță produce matricea din sursele live și tratează reporting-ul standalone explicit |
| `TEST-ARCH-05` | ✅ integration tests sunt grupate după boundary | `atf-reporting-tests/README.md` mapează API scenarios, reporting API și UI composition |
| `TEST-ARCH-06` | ✅ contract plug-and-play este explicit | selecția semantică, setup-ul typed și support-ul reutilizabil sunt documentate ca pași separați |
| `ROOT-README-01` | ✅ `atf-tests` este governance-only | root overview și module table nu mai atribuie Practice Shop acestui modul |
| `ROOT-README-02` | ✅ module map complet, fără duplicate | toate modulele din root POM apar o singură dată în tabel și în gruparea semantică |
| `ROOT-README-03` | ✅ root README este subțire | lifecycle, class contracts, YAML și CSS sunt trimise către owner docs |
| `ROOT-README-04` | ✅ baseline-ul root este acceptat și protejat | conținutul, forma aprobată a overview-ului și ownership table rămân stabile; orice schimbare ulterioară cere cerință explicită și dovadă nouă |
| `DOC-DIAG-02` | ✅ diagrama modulelor este acceptată ca formă și conținut | feedback confirmat: „Harta modulelor” este clară și elegantă; platforma versus consumers/governance rămâne explicată în legenda colapsată și tabel |
| `DOC-READ-01` | ✅ digestie vizuală aplicată în root, owner docs și feedback | fiecare diagramă are legendă imediată, orientarea este aleasă după întrebare, iar explicațiile secundare sunt colapsabile |
| `DOC-DIAG-05` | ✅ guard de diagramă whole-ATF | `DocumentationDiagramGovernanceTest` trece pe documentația live; muchiile au relații scurte și semantice, flowchart-urile non-root păstrează orientarea cerută, iar harta root are limite compacte de nod/muchie |

## Layer policy

O clasă executabilă trebuie să urmeze acest contract:

```text
test class
   │
   ├─ exactly one layer annotation → unit / component / integration / e2e
   ├─ semantic selection           → test reference / scenario annotation
   ├─ typed setup                  → mock / fixture / browser / report input
   └─ reusable support             → builders / loaders / locators / assertions
```

Pachetul descrie domeniul sau feature-ul și ajută navigarea; nu decide layer-ul.
Un test care combină mai multe boundary-uri declară integration, iar helper-ul
reutilizabil rămâne în `support` fără să devină test executabil.

`atf-reporting` rămâne independent de `atf-test-kit` pentru a putea fi construit
cu `mvn -f atf-reporting/pom.xml test`. Cele două teste standalone sunt o
excepție documentată și verificată separat; ele nu pretind că aparțin matricei
reactorului și nu importă niciun alt modul ATF.

## Support semantic

Structura existentă este suficient de coezivă pentru a fi păstrată:

```text
support/
├─ api/          setup și flow-uri API
├─ audit/        contexte și validatori
├─ config/       loader-e și snapshot-uri typed
├─ inspection/   inspectori DOM, duration și projection
├─ lifecycle/    sesiune și captură
├─ locators/     singurul owner pentru selectorii Playwright
├─ navigation/   flow-uri semantice de navigare
├─ section/      extract, module, runtime și publish
└─ uireport/     input și inspectori de artifact
```

Nu se mută clase doar pentru a satisface o matrice vizuală și nu se șterge nimic
fără dovadă de nefolosire. Următorul refactor permis este doar extragerea unei
responsabilități coezive atunci când un subpachet devine greu de navigat.

## Root README: contract editorial

Root-ul răspunde rapid la patru întrebări:

1. ce este ATF și ce boundary produce;
2. cine deține fiecare modul;
3. cum curge intenția până la artifact și audit;
4. unde continuă cititorul pentru contractul detaliat.

Nu conține lifecycle intern, class diagrams pentru fiecare port, schema completă
YAML, CSS sau jurnal de execuție. Diagramele folosesc:

| Întrebare | Formă |
|---|---|
| context, ownership stratificat, onboarding | `flowchart LR` |
| module map compactă | `flowchart LR` |
| dependențe și date cu direcție reală | `flowchart LR` |
| execuție temporală | `sequenceDiagram` |
| lifecycle/verdict | `stateDiagram-v2` în owner docs |
| contract public | `classDiagram` în owner docs |

Legenda este colapsabilă, iar tabelul de lângă diagramă poartă detaliul de
ownership. Nu se publică path-uri locale, timestamp-uri, artefacte generate sau
statusuri temporare.

## Digestie vizuală

Regula comună pentru root README, owner docs și feedback este:

- vizibil: scop, owner, boundary, verdict și pașii de continuare;
- schematic: bullets, scheme scurte sau tabele minimale;
- colapsat: explicații lungi, excepții, rationale și tabele secundare;
- detaliu normativ: în README-ul ownerului, nu duplicat în feedback.

`<summary>` trebuie să descrie explicit conținutul ascuns. Nu se ascund
scopul, owner-ul sau criteriul principal de acceptare.

## ToDo-uri active

| ID | Prioritate | ToDo | Criteriu de închidere |
|---|---:|---|---|
| `TEST-ARCH-07` | P3 | păstrează gruparea semantică a zonelor `support` și identifică eventuale subzone care depășesc limita de coeziune | fiecare mutare are owner, motiv observabil, test de boundary și zero ștergeri speculative |

`AtfConsoleBridgeTest` declară layer-ul component și trece focused gate-ul.
Adapterul este owner-uit de `atf-reporting-tests`, singurul composition root care
consumă ambele porturi. Astfel `atf-console` și `atf-reporting` rămân
production-independent, iar whole-ATF boundary nu trebuie să accepte o
dependență inversă doar pentru conversia test-only.

Nu se redeschid item-urile închise pentru o schimbare de stil locală. Se redeschid
doar la regresie reproductibilă, schimbare de ownership, consumer nou sau
modificare de contract.
