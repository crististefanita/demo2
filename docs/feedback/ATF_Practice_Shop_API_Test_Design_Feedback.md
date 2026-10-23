# Practice Shop — feedback pentru designul testelor API

Feedback-ul este reconciliat cu testele și fixture-urile live din
`atf-test-practice-shop/src/test/java/atf/ui/tests/practiceshop/api`.
Este o analiză a calității testelor, nu o aprobare de schimbare a testelor;
închiderea unui punct cere modificare autorizată și dovadă la boundary-ul potrivit.

## Verdict

[`ShopApiContractVariantTest`](../../atf-test-practice-shop/src/test/java/atf/ui/tests/practiceshop/api/component/contract/ShopApiContractVariantTest.java)
este un exemplu bun pentru o matrice mică: nume de caz vizibil, date distincte,
un singur traseu de execuție și aceleași aserțiuni pentru toate variantele.
Este un **model de formă**, nu standardul pentru orice test API. Nu comprima
scenarii cu rezultate ori invariants diferite într-o singură metodă doar pentru
a reduce numărul de linii.

Testele Shop nu trebuie să facă toate trafic extern. Testele cu transport mock
rămân valide când verifică clientul, request factory-ul, contractele și
comportamentul Practice Shop. Testele generice de Reporting au owner separat în
`atf-reporting-tests`; fixture-ul mock nu schimbă owner-ul comportamentului.

## Observații constructive

### 1. Numele unor cazuri promit mai mult decât verifică

- `ShopApiCatalogCartVariantTest.categorySortAndOutOfStockRemainObservableWithoutASecondE2e`
  trimite un query cu sortare, categorie și filtru rental, dar asertează numai
  categoria, forma listei și stocul produsului separat. Nu verifică ordinea
  sortată și nici că răspunsul respectă toate filtrele declarate.
- `ShopApiEndpointMatrixTest` are cazuri numite „returns matching results”,
  „returns a created account” sau „accepts a known product”, însă matricea comună
  verifică în principal metoda/ruta, statusul și forma JSON. Pentru fiecare caz,
  fie adăugați aserțiunea semantică minimă care justifică numele, fie restrângeți
  numele la ceea ce este verificat efectiv.
- „request succeeded” sau „response is JSON” nu dovedește identitatea produsului,
  efectul mutation-ului ori asocierea cu factura. Aserțiunile de business trebuie
  să rămână explicite și locale cazului.

### 2. Matricele au suprapuneri care trebuie intenționate

`ShopApiContractVariantTest` și `ShopApiEndpointMatrixTest` suprapun login-ul
invalid, produsul inexistent și payment check, dar au niveluri diferite de
asserțiune:

- `ContractVariant` verifică și corpul exact așteptat;
- `EndpointMatrix` verifică invariantul comun de transport și răspuns.

Păstrați suprapunerea numai dacă diferența rămâne explicită; altfel eliminați
doar cazul duplicat sau combinați-l într-o probă cu scop unic. Nu eliminați
aserțiunea mai puternică doar pentru a micșora suita.

Separarea recomandată este:

| Probă | Întrebarea la care răspunde |
|---|---|
| `ShopApiRequestFactoryTest` | Ce request tipizat construim: metodă, rută, query, headers și body? |
| `ShopApiContractVariantTest` | Ce rezultat distinct produce cazul nominal/negativ selectat? |
| `ShopApiEndpointMatrixTest` | Ce endpoint-uri respectă contractul comun de transport și răspuns? |
| `ShopApiScreenFlowTest` | Se compun precondiția și ecranele API într-un flow fără browser? |
| `ShopStandaloneApiReportTest` | Funcționează contractul business contra serviciului live și se publică dovada? |

### 3. Nivelul declarat nu corespunde mereu boundary-ului exercitat

Definiția ATF este: `unit` pentru logică pură, `component` pentru boundary de
modul cu vecini controlați, `integration` pentru colaborare reală. Merită
reevaluat nivelul pentru testele pur deterministe ale rutelor, operațiilor,
selectorului, recovery-ului și validatorilor: de exemplu
`ShopApiRoutesTest`, `ShopApiOperationTest`, `ShopApiExchangeSelectorTest`,
`ShopLoginBodyRecoveryTest`, `ShopApiBodyEvidenceTest` și
`ShopApiReportValidationTest`. Verificați în aceeași trecere și
`ShopApiRequestFactoryTest`, `ShopApiAssertionsTest` și
`ShopApiLoginAssertionsTest`.

Nu schimbați adnotările mecanic: `ShopApiComponentSuite` selectează explicit tag-ul
`component`. Dacă probele pure devin `unit`, definiți unde sunt rulate și
documentate; altfel dispar din comanda API component fără ca testele să eșueze.
`ShopApiContractVariantTest` rămâne component: exercită clientul cu transport
controlat și publică exchange-ul observat.

### 4. Unele clase sunt aglomerate, dar split-ul trebuie să urmeze semantica

- `ShopApiRequestFactoryTest` are 8 teste și acoperă autentificare, cont/invoice,
  catalog, coș și payment. Grupează navigarea după boundary; nu transforma
  fixture-ul într-un DSL ascuns și nu ascunde metoda/ruta/body în helpers generici.
- `ShopApiCatalogCartVariantTest` pune comportamente independente de catalog în
  aceeași metodă, apoi combină update/remove și un lifecycle de coș. Păstrați
  secvența stateful ca un singur scenariu; separați cazurile independente pentru
  diagnostic mai bun, fără a multiplica clase fără motiv.
- `ShopApiEndpointMatrixTest` (20 de intrări) este utilă ca matrice comună, dar
  trebuie să rămână o matrice de invariants comune. Contractele speciale—de
  exemplu body exact, `204` fără body sau efectul unei mutații—au nevoie de
  aserțiuni dedicate, nu de condiții care fac testul generic greu de urmărit.
- `ShopApiCallAuditReportComponentTest` este lungă (9 teste) deoarece acoperă
  asocierea exchange–contract, retry, deduplicare și diagnostice. Dacă motivele
  de schimbare se separă, împărțiți după aceste boundary-uri; nu mutați clasa în
  Reporting cât timp folosește publisherul și contractele Shop.

Pachetul `api.component.exchange` are deja 7 clase de test; pragul de coeziune
este 8. Preferă gruparea semantică în interiorul claselor existente sau un
subpackage motivat, nu adăugarea automată de clase la fiecare split.

### 5. Numele trebuie să distingă mod live de transport live

`ShopApiClientContractTest.explicitlyLiveTransportIsPublishedAsLiveProvenance`
injectează un transport înregistrat/mock și verifică eticheta modului
`LIVE`; nu face o cerere externă. Numele trebuie să exprime că verifică maparea
provenienței pentru modul configurat, nu să poată fi confundat cu
`ShopStandaloneApiReportTest`, singura integrare directă identificată aici care
folosește `ShopApiClient.live`.

## Ordine de lucru recomandată

1. Repară sau restrânge numele cazurilor pentru care assertion-urile nu susțin
   promisiunea (în special sortarea și rezultatele semantice din matrix).
2. Decide scopul unic al `ContractVariant` față de `EndpointMatrix` și elimină
   numai suprapunerea fără valoare distinctă.
3. Reevaluează nivelurile `unit/component` împreună cu selecția suitelor și
   comenzile README; dovedește că niciun test nu a ieșit din execuția uzuală.
4. Abia apoi refactorizează clasele/helpers; păstrează raportul de audit drept
   probă de integrare Shop și invariants renderer neutre în `atf-reporting-tests`.

## Criterii de închidere

- Numele fiecărui caz descrie exact un rezultat verificat, iar variațiile
  importante au assertion semantic, nu numai status/JSON-valid.
- Matricele au scop distinct și fiecare caz duplicat are justificare verificabilă.
- Fiecare adnotare reflectă boundary-ul testat; unitățile sunt incluse într-o
  comandă documentată, iar component suite rămâne izolată de serviciul extern.
- Nicio extragere spre `atf-reporting-tests` nu introduce dependență inversă a
  consumerului și nu mută acolo mapping-ul sau politica Practice Shop.
- Suitele component/API și testele live păstrează scopuri și comenzi distincte.
