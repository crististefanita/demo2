# Feedback — Automation Exercise consumer

Acest fișier păstrează numai cerințele funcționale și gap-urile deschise pentru
consumerul [`atf-test-auto-ex`](../../atf-test-auto-ex/README.md). Contractele
deja implementate și comenzile curente sunt documentate în README-ul modulului;
controalele tehnice Git rămân în registrul separat de schimbări.

## Context confirmat

- Site-ul este o aplicație gratuită de practică UI/API, independentă de Practice
  Shop.
- [Test Case 14](https://automationexercise.com/test_cases#collapse14) confirmă
  traseul „Register while Checkout”, plata demo, confirmarea și ștergerea
  contului; [Test Case 24](https://automationexercise.com/test_cases#collapse24)
  adaugă verificarea descărcării facturii.
- API-ul public documentează catalog, căutare și conturi; nu documentează API
  pentru coș, comandă sau plată.
- Primul set implementat este declarat `UI`; nu presupune o corelare API pe care
  site-ul nu o garantează.

## Cerințe rămase

| ID | Cerință | Criteriu de acceptare | Prioritate |
|---|---|---|---:|
| `AUTOEX-HYBRID-01` | Adaugă un scenariu plug-and-play `UI_API` numai pentru o acțiune cu schimb API observabil și allowlist sigur. Candidatul inițial este căutarea produselor. | Același checkpoint dovedește request/response corelat, rezultate vizibile și paritate a produselor; Network Inspector nu inventează verdicturi, iar raportul păstrează ownership-ul UI/API. Dacă site-ul nu expune un schimb stabil, scenariul rămâne `UI`. | 88 |
| `AUTOEX-API-01` | Evaluează o colecție API separată pentru endpoint-urile public documentate, cu date și cleanup izolate per rulare. | Cererile, formatele body și răspunsurile sunt probate față de contractul public înainte de alegerea canalului; operațiile de cont folosesc identități sintetice și cleanup confirmat. Nu se presupune că POST/PUT acceptă JSON doar din cauza convențiilor altui API. | 78 |
| `AUTOEX-E2E-RESILIENCE-01` | Confirmă comportamentul cleanup-ului pentru întreruperi în toate punctele de după crearea contului și pentru indisponibilitatea temporară a site-ului. | Rulări repetate și probe de eșec arată fie ștergerea contului creat, fie un diagnostic explicit care identifică cleanup-ul rămas; niciun secret sintetic nu apare în raport/capturi. | 72 |
| `AUTOEX-DATA-01` | Definește cum se menține configurația produsului și a prețului când catalogul public se schimbă. | Datele așteptate sunt actualizate configurabil sau validate printr-un contract stabil; niciun test nu ascunde drift-ul prin aserțiuni permissive. | 65 |

Gestionarea Google vignette-ului nu este ToDo separat: boundary-ul comun al
acțiunilor UI îl închide numai prin textul exact vizibil „Close”, condiționat de
fragmentul `#google_vignette`; clickul este auditat, iar lipsa controlului vizibil
oprește scenariul fără bypass. Componenta are probe locale pentru close în iframe,
control absent și pagini fără vignette.

Scorurile ordonează cerințele deschise și nu exprimă calitatea globală a
modulului. O cerință se închide numai după dovada criteriului ei; progresul unui
test UI nu închide implicit gap-ul `UI_API` sau API.
