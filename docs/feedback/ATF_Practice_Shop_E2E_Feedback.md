# Practice Shop E2E — live reconciled feedback

## Stare

Cele trei probe de purchase reutilizează același flow business; `UI_ONLY` și
`UI_API` sunt deja opțiuni typed ale `@WithE2eHarness(profile = ...)`, iar a
treia clasă selectează presetul `UNIFIED_FLOW + FINAL_ONLY`. Clasele curente
fixează aceste valori, deci lansarea mai multora repetă purchase-ul. Pentru
authenticated E2E, `@AuthenticatedE2e` selectează fixture-ul și browserul, dar
nu poate crea un cont.

| Suprafață | Fapt verificat live |
|---|---|
| Test canonic | `PracticeShopPurchaseE2eTest` execută `PracticeShopPurchaseFlow` și trimite checkpoint-urile către `journey.checkpoint`; flow-ul real include confirmarea finală a comenzii |
| Config consumer | `@WithE2eHarness` expune deja `UI_ONLY` / `UI_API`; YAML-ul configurează `apiEvidence` și booleanul legacy `screenshotOnFailure`. Adnotarea are un preset de presentation, nu flow mode, screenshot policy și detaliile acțiunilor ca axe independente |
| Reporting `UNIFIED_FLOW` | contract generic implementat și probat în `atf-reporting`; noul test single-flow îl selectează explicit prin presetarea typed a harness-ului, nu prin YAML consumer |
| Profiluri purchase | `UI_ONLY` și `UI_API` sunt selectabile în `@WithE2eHarness`; exemplele guest le fixează în clase distincte, iar lansarea ambelor repetă purchase-ul. UI-only nu publică API evidence, iar testul canonic nu poate fi dezactivat accidental de un override global `ui-only` |
| Proof UI-only + UI/API | 2026-10-22 09:26–09:27: ambele clase au trecut E2E sub override global `atf.practice.validation-profile=ui-only`; raportul canonic păstrează API validation/network inspector, iar raportul UI-only nu conține niciuna dintre secțiuni. Ambele confirmă purchase-ul complet (2 comenzi demo create) |
| Artifacte proaspete | [UI + API — 09:26](../../artifacts/reports/ui/ui-atf-practice-shop-e2e_2026-10-22_09-26-51-951.html) · [UI-only — 09:27](../../artifacts/reports/ui/ui-atf-practice-shop-e2e_2026-10-22_09-27-29-626.html); ambele status `Pass`, fără overflow orizontal la 1440 px în MCP; arborele UI/API este vizibil și coerent |
| Single-flow preset | 2026-10-22 09:41: `PracticeShopPurchaseUiOnlySingleFlowE2eTest` a trecut live; raportul conține un proposition Gherkin, un tabel Playwright agregat (62 acțiuni), o captură finală PNG decodabilă (1440×1000), fără API evidence și fără overflow în MCP: [raport single-flow](../../artifacts/reports/ui/ui-atf-practice-shop-e2e_2026-10-22_09-41-07-474.html) |

Request-urile clientului către aplicație continuă normal în ambele profile.
`ui-only` înseamnă fără captură/publicare API de către evidence consumer, nu
blocarea traficului aplicației.

## ToDo-uri active

| ID | Scor / prioritate | Gap verificat | Criteriu de închidere |
|---|---:|---|---|
| `SHOP-E2E-INTELLIJ-AUTH-01` | **70 / P1 — lifecycle-ul contului este incomplet** | clasele autentificate rulează individual cu fixture; nu există strategie de înregistrare. `PracticeShopAccountAndCatalogE2eTest` are două metode, fiecare cu propriul harness și login | `REUSE_FIXTURE` rămâne default; `REGISTER_PER_RUN` este opt-in prin adnotarea clasei. O lansare a clasei provision-ează o singură identitate sintetică, reutilizată de metodele ei; o lansare individuală a metodei creează un singur cont. Confirmarea registration precedă TEST BODY, dar login-ul rămâne în body acolo unde este comportamentul testat. Fără replay, PII sau presupuneri de cleanup |
| `SHOP-E2E-PRESENTATION-01` | **66 / P1 — configurare incompletă a evidence-ului** | profilele UI-only/UI+API există deja în adnotare; presetul `SINGLE_FLOW_FINAL_SCREENSHOT` combină `UNIFIED_FLOW` și `FINAL_ONLY`; `screenshotOnFailure` nu înseamnă doar captură la eșec. Nu există opțiune separată pentru detaliile acțiunilor UI | păstrează selecția typed existentă a profilului; expune independent prin adnotarea E2E `SCREEN_CHECKPOINTS`/`UNIFIED_FLOW`, politica de screenshot și includerea detaliilor Playwright. Flow-ul/aserțiunile rămân identice; API evidence apare doar în `UI_API`; pașii intermediari nu devin noduri Gherkin per click |

### Rulare autentificată din IntelliJ și lifecycle-ul contului

Launch-ul direct și reutilizarea fixture-ului sunt implementate; provisioning-ul
automat nu există încă. `@AuthenticatedE2e` actual configurează `accountFile`,
`browserMode` și `zoomPercent`, nu strategia de account setup. Cerința este ca
`PracticeShopAuthenticatedPurchaseE2eTest` și
`PracticeShopAccountAndCatalogE2eTest` să poată selecta explicit lifecycle-ul
contului, fără ca înregistrarea să devină implicită:

```text
@AuthenticatedE2e(accountSetup = REUSE_FIXTURE)   # default sigur; comportamentul curent
@AuthenticatedE2e(accountSetup = REGISTER_PER_RUN) # opt-in pentru oricare clasă auth
```

Contractul de lifecycle urmărit este per lansare independentă a clasei selectate.
În `AccountAndCatalog`, cele două metode trebuie să reutilizeze același cont
provisionat pentru acea lansare, nu să înregistreze câte unul fiecare. Lansarea
doar a unei metode rămâne autonomă și creează cel mult un cont. O clasă lansată
separat nu moștenește contul nou al altei lansări.

Contractul de lifecycle urmărit este:

```text
@AuthenticatedE2e(accountSetup = ...)
  └─ SETUP (se încheie înainte să înceapă TEST BODY)
     ├─ REUSE_FIXTURE: verifică fixture-ul disponibil
     └─ REGISTER_PER_RUN: înregistrează o dată → succes verificat → identitate runtime disponibilă
  └─ TEST BODY: login verificat în flow-ul clasei → account/catalog smoke sau purchase → confirmation
```

Adnotarea selectează strategia cunoscută, nu transportă credențiale sau date
personale. Datele sintetice și politica de email/parolă rămân configurate separat;
adresa nouă trebuie să fie unică și acceptată de site, iar secretul nu ajunge în
adnotare, YAML urmărit de Git, loguri sau raport. Fragmentul de mai jos este o
schiță de API, nu o adnotare existentă:

| Aspect | Stare live | Contract de închidere |
|---|---|---|
| Adnotare | `@AuthenticatedE2e` selectează `accountFile`, browser mode și zoom; nu are strategie de creare cont | adăugarea unei opțiuni typed `accountSetup` selectează numai `REUSE_FIXTURE` ori `REGISTER_PER_RUN`; fără configurare, nu se face mutație externă |
| Lifecycle setup/body | `PracticeShopE2eContext.scenario()` publică și închide `SETUP` static înainte de callback; `TEST BODY` începe înainte să ruleze callback-ul din `harness.run(...)`. Login-ul actual este deci în body, nu în setup | harness-ul primește setup executabil separat. Înregistrarea se face și se verifică în `SETUP`; apoi TEST BODY rulează login-ul și comportamentul real al clasei. Un eșec de setup oprește body-ul și este raportat ca setup failure |
| Înregistrare | `PracticeShopRegistrationPage` și `PracticeShopRegistrationData` există; `submit()` doar dă click; nu există flow, verificare de rezultat, API route contract sau teste de provisioning | reutilizează page object-ul printr-un flow dedicat; identifică exchange-ul real observat din browser (fără endpoint inventat sau client paralel), verifică succesul înainte de login și face provisioning o dată per lansare de clasă |
| Scope între metode | `E2eExtension` deschide/închide harness per metodă; account/catalog are două metode care repetă login-ul | setup-ul de cont este class-run scoped și partajează numai identitatea runtime între metodele aceleiași lansări; browserul/report scope rămâne per metodă. Lansarea unei metode singure rămâne funcțională |
| Date de cont | modulul cere acum email și parolă de cont deja înregistrat; fixture-ul live folosit anterior nu a autentificat | datele non-secrete vin din config sintetică; email-ul per-run este unic și dintr-un domeniu acceptat/controlat; parola este secretă și nu se publică |
| Fixture reutilizat | `.local.properties` rămâne ignorat de Git și are validare structurală; run-ul live a eșuat la login | `REUSE_FIXTURE` păstrează condiția actuală: cont activ, email controlat și login live reușit |
| Efect extern / idempotency | pagina de înregistrare trimite o mutație externă; în cod nu s-a găsit cleanup de cont și nici mecanism de idempotency pentru înregistrare | `REGISTER_PER_RUN` cere opt-in explicit, identitate unică și zero retry/replay automat la submit-ul de înregistrare; rezultat incert se raportează, nu se retrimite orbește |
| Evidence/API binding | journal-ul capturează traffic-ul UI; checkpoint-urile actuale sunt `product/login/cart/payment/order`, fără `registration`; setup-ul de provisioning nu este conectat | înregistrarea poate adăuga evidence de setup numai pentru `UI_API`; login-ul rămâne checkpoint al body-ului, mai ales în account/auth smoke. Adaugă seam typed fără exchange-uri duplicate. `UI_ONLY` nu publică API evidence, dar traficul continuă |
| Redactare | `PracticeShopJsonEvidence` redactează email, parolă, token și secret; nu redactează nume, DOB, telefon sau adresă | nu publica payload-ul brut de înregistrare; definește allowlist/mascare field-aware și teste pentru request + response înainte să activezi inspectorul pe acel exchange. Raportul/System afișează numai politica și starea, nu identitatea |
| Raport | setup-ul actual afirmă că fixture-ul/configurația este disponibilă; login-ul se execută în TEST BODY | raportul arată registration doar când s-a ales opt-in; login-ul și business proof-ul rămân pașii clasei. Purchase-ul ajunge la `Confirm` și order confirmation; fără propoziții duplicate |
| Safety gate | skip-ul sigur verifică fixture-ul și nu expune credențiale | preflight-ul depinde de modul: fixture valid pentru `REUSE_FIXTURE`; config de înregistrare completă și opt-in pentru `REGISTER_PER_RUN`; nicio fallback silențioasă între moduri. Înregistrarea rămâne mutație externă persistentă: fără cleanup demonstrat, retenția/acceptarea conturilor create trebuie declarată, iar outcome-ul incert nu se reîncearcă |
| README | documentează manual setup one-time și comenzi IntelliJ/Maven pentru fixture existent | păstrează instrucțiunile curente până la implementare; după implementare documentează ambele moduri, efectul extern și ce evidence se așteaptă |

- Testele de profil/preflight nu fac login sau purchase live. Ultima verificare a
  fixture-ului reutilizat a fost respinsă la login; un email rezervat a fost
  oprit înainte de browser. Prin urmare, nu există încă un authenticated `PASS`.

**Launch gata; lifecycle-ul contului este incomplet — `SHOP-E2E-INTELLIJ-AUTH-01` (70 / P1).**

- Gata: launch individual fără VM options, reutilizare fixture, skip acționabil și page object/data parțiale pentru formularul de înregistrare.
- Deschis: selectarea prin adnotare între fixture reutilizat și cont nou per lansarea clasei, provisioning real în `SETUP`, apoi login în flow-ul de business și purchase end-to-end.
- Blocat pentru proof live: fixture-ul actual nu autentifică; modul nou nu poate fi considerat funcțional până la implementare și run verificat.
- Confidențialitate: fixture-ul și capturile cu identitate rămân locale; nu se
  publică date de cont în feedback sau raport.

## Contractul cerut pentru configurarea E2E

Modurile sunt independente; adnotarea clasei care se rulează direct din IntelliJ
este punctul de selecție, iar configurația YAML nu trebuie să suprascrie tacit
alegerea acelui test:

| Axă typed | Opțiuni generice existente | Situația consumerului |
|---|---|---|
| Validare | `UI_ONLY` / `UI_API` în `HarnessProfile` | implementat prin `@WithE2eHarness(profile = ...)`; clasele actuale sunt exemple presetate |
| Granularitate flow | `SCREEN_CHECKPOINTS` / `UNIFIED_FLOW` | `STANDARD` sau presetul atomic `SINGLE_FLOW_FINAL_SCREENSHOT` |
| Screenshot | `EACH_CHECKPOINT`, `FINAL_ONLY`, `FAILURE_ONLY`, `FINAL_AND_FAILURE`, `NONE` | YAML expune numai booleanul legacy `screenshotOnFailure` |
| Detalii acțiuni | action evidence este produsă de flow-ul UI | fără switch consumer pentru a include/omite detaliile intermediare |

`UI_ONLY` înseamnă fără evidence API, nu fără trafic. Granularitatea Gherkin
decide dacă raportul arată checkpoint-uri distincte sau un singur flow; detaliile
Playwright sunt evidence tehnică grupată, nu pași Gherkin generați pentru fiecare
click. Schimbarea presentation/screenshot nu trebuie să schimbe traseul sau
rezultatul testului.

## Forma raportului urmărită

```text
TEST BODY
  └── WHEN guest completes the configured purchase and sees the order confirmation
    ├── UI validation
    │   ├── UI steps: one grouped, collapsed technical disclosure (configurable)
    │   └── final screenshot: one capture when FINAL_ONLY is selected
    └── API validation: present only when the existing API evidence profile selects it
```

- Un singur flow business este invariantul; fiecare checkpoint de business are
  o singură propoziție Gherkin, nu una duplicată separat pentru UI și API.
- Opțiunea pentru detalii UI controlează numai publicarea evidence-ului
  intermediar Playwright ca un grup UI. Ea nu generează Gherkin și nu schimbă
  acțiunile sau aserțiunile executate.
- Fără model plug-and-play pentru UI substeps: nu există listă extensibilă de
  checkpoint-uri Gherkin și nici câte un report node per click/navigation.
- UI-only versus UI+API se selectează prin `@WithE2eHarness(profile = ...)`;
  lista `apiEvidence` selectează checkpoint-urile API publicate numai când
  profilul este `UI_API`. Niciuna nu se deduce din detaliile acțiunilor UI.
  UI-only oprește evidence-ul API, nu request-urile aplicației.
- Purchase-ul rămâne complet: flow-ul apasă `Confirm` și validează confirmarea
  comenzii. Ascunderea evidence-ului UI nu scurtează flow-ul.
- Captura finală este policy separată de Gherkin și de acțiuni; când este aleasă
  `FINAL_ONLY`, se publică o singură imagine finală, nu una per acțiune.

`@WithE2eHarness` și schema YAML curente nu oferă încă toate axele de mai sus.
Forma de mai jos ilustrează doar limitele semantice, nu chei de configurare
implementate:

```yaml
practiceShop:
  reporting:
    flowMode: unified-flow # separat de screenshotPolicy
    screenshotPolicy: final-only # separat de flowMode
    uiActionDetails: included # grupate; nu o listă de subpași Gherkin
  validation:
    profile: ui-api # ui-only | ui-api; independent de uiActionDetails
```

`ui-only` și `ui-api` există deja. `ReportEvidenceFlowMode.UNIFIED_FLOW` și
`FINAL_ONLY` sunt contracte generice implementate în reporting; consumerul nu
le expune independent și nici nu are toggle pentru acțiunile UI. Preferința
ownerului este selecția din adnotarea testului lansat în IntelliJ; YAML-ul poate
păstra default-uri, fără secrete și fără a schimba profilele fixe ale altor teste.

Contractele rezolvate — profilele UI-only/UI+API, runtime boundary, overview și
API evidence — sunt documentate în [README-ul Practice Shop](../../atf-test-practice-shop/README.md)
și nu rămân ca ToDo-uri în acest feedback. Incidentele IntelliJ de reporting
rămân numai în feedback-ul ownerului `ATF_Reporting_Feedback.md`.

## Regula de validare

- contractul de single-flow se probează în HTML cu aserții negative explicite:
  zero carduri `Checkpoint N`, zero propoziții Gherkin duplicate și un singur
  business proposition;
- includerea/omiterea action details și flow mode se verifică drept
  presentation-only: aceleași acțiuni, aserțiuni și rezultat pentru fiecare mod;
- UI-only / UI+API se verifică pe același contract business; API evidence
  diferă, traficul și rezultatul nu;
- toate cele cinci screenshot policies se testează modular; `FINAL_ONLY` cere
  o singură captură finală și nu se deduce din ascunderea pașilor;
- niciun diagnostic runtime pentru ramura guest acceptată nu se publică în raport; erorile runtime fără corelare rămân failure;
- artifactul proaspăt se inspectează în MCP pe `Overview`, `Tests` și `Fails`;
- nu se blochează traficul normal al site-ului pentru a ascunde evidence.

Un run E2E live poate crea o comandă externă. Nu se generează o comandă nouă
doar pentru verificarea documentației; se folosește o rulare deja autorizată.
