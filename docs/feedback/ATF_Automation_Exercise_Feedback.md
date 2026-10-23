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
- Consumerul are un singur flow UI E2E complet; căutarea este validată în acel
  flow și nu este duplicată într-un E2E separat.

## Reutilizare și proprietate

- Consumerul are numai E2E-uri de business pe site-ul real; nu păstrează probe cu
  HTML/iframe inventat ori teste interne ale configului și launch policy.
- Clasificarea testelor, Playwright, secvența de acțiuni, sesiunea Extent,
  capturile și output-ul raportului sunt deja primitive ATF reutilizate direct.
- Suportul Practice Shop pentru conturi locale, setup-uri, API journal și suite
  partajate rămâne în ownerul Shop; nu este generic doar fiindcă este util acolo.
- Compoziția JUnit/report per test din AutoEx și contextul mai bogat al Shop sunt
  similare, dar nu au încă un contract comun. O eventuală extracție se face ca
  infrastructură ATF cross-consumer, fără pagini/date de business, și numai cu
  dovadă de paritate în ambii consumatori; nu se copiază suportul Shop în AutoEx.

## Cerințe rămase

| ID | Cerință | Criteriu de acceptare | Prioritate |
|---|---|---|---:|
| `AUTOEX-HYBRID-01` | Adaugă un scenariu plug-and-play `UI_API` numai pentru o acțiune cu schimb API observabil și allowlist sigur. Candidatul inițial este căutarea produselor. | Același checkpoint dovedește request/response corelat, rezultate vizibile și paritate a produselor; Network Inspector nu inventează verdicturi, iar raportul păstrează ownership-ul UI/API. Dacă site-ul nu expune un schimb stabil, scenariul rămâne `UI`. | 88 |
| `AUTOEX-API-01` | Evaluează o colecție API separată pentru endpoint-urile public documentate, cu date și cleanup izolate per rulare. | Cererile, formatele body și răspunsurile sunt probate față de contractul public înainte de alegerea canalului; operațiile de cont folosesc identități sintetice și cleanup confirmat. Nu se presupune că POST/PUT acceptă JSON doar din cauza convențiilor altui API. | 78 |
| `AUTOEX-E2E-RESILIENCE-01` | Confirmă comportamentul cleanup-ului pentru întreruperi în toate punctele de după crearea contului și pentru indisponibilitatea temporară a site-ului. | Rulări repetate și probe de eșec arată fie ștergerea contului creat, fie un diagnostic explicit care identifică cleanup-ul rămas; niciun secret sintetic nu apare în raport/capturi. | 72 |
| `AUTOEX-DATA-01` | Definește cum se menține configurația produsului și a prețului când catalogul public se schimbă. | Datele așteptate sunt actualizate configurabil sau validate printr-un contract stabil; niciun test nu ascunde drift-ul prin aserțiuni permissive. | 65 |

Google vignette-ul este tratat ca precondiție de mediu, nu drept comportament
business și nu are teste consumer cu DOM sintetic. E2E-ul real îl închide numai
prin controlul vizibil exact „Close”, condiționat de fragmentul
`#google_vignette`; clickul apare în trace, iar lipsa controlului oprește fluxul
fără bypass. Orice test al unei politici generice de browser aparține ownerului
ATF, nu suitei de business AutoEx.

Scorurile ordonează cerințele deschise și nu exprimă calitatea globală a
modulului. O cerință se închide numai după dovada criteriului ei; progresul unui
test UI nu închide implicit gap-ul `UI_API` sau API.
