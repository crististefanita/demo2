# Practice Shop E2E — live reconciled feedback

## Stare

Cele trei probe de purchase reutilizează același flow; adnotarea clasei fixează dacă raportul este UI-only, UI+API sau un singur flow UI cu acțiuni agregate și captură finală. YAML consumer pentru selectarea modului de prezentare rămâne separat și încă deschis.

| Suprafață | Fapt verificat live |
|---|---|
| Test canonic | `PracticeShopPurchaseE2eTest` execută `PracticeShopPurchaseFlow` și trimite checkpoint-urile către `journey.checkpoint`; flow-ul real include confirmarea finală a comenzii |
| Config YAML | `validation.profile` și `apiEvidence` controlează evidence-ul API; `screenshotOnFailure` este boolean legacy; nu există încă setare consumer pentru detaliile pașilor UI |
| Reporting `UNIFIED_FLOW` | contract generic implementat și probat în `atf-reporting`; noul test single-flow îl selectează explicit prin presetarea typed a harness-ului, nu prin YAML consumer |
| Profiluri purchase | Cele două clase fixează independent `UI_ONLY` și `UI_API` prin adnotarea typed `@WithE2eHarness`; UI-only nu publică API evidence, iar testul canonic nu poate fi dezactivat accidental de un override global `ui-only` |
| Proof UI-only + UI/API | 2026-10-22 09:26–09:27: ambele clase au trecut E2E sub override global `atf.practice.validation-profile=ui-only`; raportul canonic păstrează API validation/network inspector, iar raportul UI-only nu conține niciuna dintre secțiuni. Ambele confirmă purchase-ul complet (2 comenzi demo create) |
| Artifacte proaspete | [UI + API — 09:26](../../artifacts/reports/ui/ui-atf-practice-shop-e2e_2026-10-22_09-26-51-951.html) · [UI-only — 09:27](../../artifacts/reports/ui/ui-atf-practice-shop-e2e_2026-10-22_09-27-29-626.html); ambele status `Pass`, fără overflow orizontal la 1440 px în MCP; arborele UI/API este vizibil și coerent |
| Single-flow preset | 2026-10-22 09:41: `PracticeShopPurchaseUiOnlySingleFlowE2eTest` a trecut live; raportul conține un proposition Gherkin, un tabel Playwright agregat (62 acțiuni), o captură finală PNG decodabilă (1440×1000), fără API evidence și fără overflow în MCP: [raport single-flow](../../artifacts/reports/ui/ui-atf-practice-shop-e2e_2026-10-22_09-41-07-474.html) |

Request-urile clientului către aplicație continuă normal în ambele profile.
`ui-only` înseamnă fără captură/publicare API de către evidence consumer, nu
blocarea traficului aplicației.

## ToDo-uri active

| ID | Scor / prioritate | Gap verificat | Criteriu de închidere |
|---|---:|---|---|
| `SHOP-E2E-INTELLIJ-AUTH-01` | **70 / P1 — lifecycle-ul contului este incomplet** | launch-ul independent și reutilizarea contului preînregistrat sunt implementate, dar niciuna dintre clasele autentificate nu poate provisiona identitatea; ownerul cere alegerea configurabilă între fixture și setup de înregistrare | `REUSE_FIXTURE` rămâne default, iar `REGISTER_PER_RUN` este opt-in pentru ambele clase: harness-ul înregistrează o identitate sintetică o singură dată, verifică succesul și login-ul, apoi execută account/catalog smoke sau purchase-ul complet, în funcție de clasă; raportul nu expune date personale |
| `SHOP-E2E-PRESENTATION-01` | **66 / P1** | presetarea typed per test `UI_ONLY + UNIFIED_FLOW + FINAL_ONLY` este implementată și verificată; rămâne wiring-ul YAML consumer pentru alegerea configurației de prezentare | consumerul expune gruparea UI ca disclosure tehnic configurabil și captura `FINAL_ONLY`, fără registry/plugin sau subpropoziții Gherkin; profilele de validare rămân independente. Presetarea single-flow are deja proof live și HTML; închiderea TODO-ului general cere proof de config YAML |

### Rulare autentificată din IntelliJ și lifecycle-ul contului

Launch-ul direct și reutilizarea fixture-ului sunt implementate; provisioning-ul
automat nu există încă. Cerința clarificată este ca `PracticeShopAuthenticatedPurchaseE2eTest` și
`PracticeShopAccountAndCatalogE2eTest` să poată selecta explicit lifecycle-ul contului, fără a transforma înregistrarea
într-o acțiune implicită pentru orice rulare autentificată:

```text
@AuthenticatedE2e(accountSetup = REUSE_FIXTURE)   # default sigur; comportamentul curent
@AuthenticatedE2e(accountSetup = REGISTER_PER_RUN) # opt-in pentru oricare clasă auth
```

Contractul de lifecycle urmărit este:

```text
@AuthenticatedE2e(accountSetup = ...)
  └─ SETUP (se încheie înainte să înceapă TEST BODY)
     ├─ REUSE_FIXTURE: încarcă fixture → login verificat
     └─ REGISTER_PER_RUN: înregistrează o dată → succes verificat → login verificat
  └─ TEST BODY: account/catalog smoke sau purchase flow → Confirm → order confirmation
```

Adnotarea selectează strategia cunoscută, nu transportă credențiale sau date
personale. Datele sintetice și politica de email/parolă rămân configurate separat;
adresa nouă trebuie să fie unică și acceptată de site, iar secretul nu ajunge în
adnotare, YAML urmărit de Git, loguri sau raport. Fragmentul de mai jos este o
schiță de API, nu o adnotare existentă:

| Aspect | Stare live | Contract de închidere |
|---|---|---|
| Adnotare | `@AuthenticatedE2e` selectează `accountFile`, browser mode și zoom; nu are strategie de creare cont | adăugarea unei opțiuni typed `accountSetup` selectează numai `REUSE_FIXTURE` ori `REGISTER_PER_RUN`; fără configurare, nu se face mutație externă |
| Lifecycle setup/body | `PracticeShopE2eContext.scenario()` finalizează `SETUP` static, apoi constructorul `Scenario` deschide `TEST BODY` înainte să ruleze callback-ul din `harness.run(...)`; callback-ul curent nu poate atașa retroactiv acțiuni la setup | harness-ul primește setup separat de business (sau API echivalent); rulează înregistrarea/login-ul în faza `SETUP`, o încheie cu verdict/evidence, abia apoi deschide `TEST BODY`. Eșecul setup-ului oprește purchase-ul |
| Înregistrare | `PracticeShopRegistrationPage` și `PracticeShopRegistrationData` există; `submit()` doar dă click; nu există flow, verificare de rezultat, API route contract sau teste de provisioning | reutilizează page object-ul printr-un flow dedicat; identifică exchange-ul real produs de UI (fără endpoint inventat sau client paralel), verifică succesul înainte de login și păstrează aceeași identitate pe tot run-ul |
| Date de cont | modulul cere acum email și parolă de cont deja înregistrat; fixture-ul live folosit anterior nu a autentificat | datele non-secrete vin din config sintetică; email-ul per-run este unic și dintr-un domeniu acceptat/controlat; parola este secretă și nu se publică |
| Fixture reutilizat | `.local.properties` rămâne ignorat de Git și are validare structurală; run-ul live a eșuat la login | `REUSE_FIXTURE` păstrează condiția actuală: cont activ, email controlat și login live reușit |
| Efect extern / idempotency | pagina de înregistrare trimite o mutație externă; în cod nu s-a găsit cleanup de cont și nici mecanism de idempotency pentru înregistrare | `REGISTER_PER_RUN` cere opt-in explicit, identitate unică și zero retry/replay automat la submit-ul de înregistrare; rezultat incert se raportează, nu se retrimite orbește |
| Evidence/API binding | journal-ul capturează traffic-ul UI; checkpoint-urile actuale sunt `product/login/cart/payment/order`, fără `registration`; setup-ul de provisioning nu este conectat | dacă `UI_API` e selectat, înregistrarea și login-ul sunt evidence de setup, separate de purchase; adaugă un seam typed de registration fără a dubla exchange-uri. `UI_ONLY` nu publică API evidence, dar traficul continuă |
| Redactare | `PracticeShopJsonEvidence` redactează email, parolă, token și secret; nu redactează nume, DOB, telefon sau adresă | nu publica payload-ul brut de înregistrare; definește allowlist/mascare field-aware și teste pentru request + response înainte să activezi inspectorul pe acel exchange. Raportul/System afișează numai politica și starea, nu identitatea |
| Raport | setup-ul actual afirmă numai că fixture-ul este disponibil; account/catalog și purchase sunt journey-uri distincte | setup-ul rămâne separat de business flow; după autentificare rulează journey-ul clasei, iar purchase-ul ajunge până la `Confirm` și order confirmation; fără propoziții Gherkin duplicate |
| Safety gate | skip-ul sigur verifică fixture-ul și nu expune credențiale | preflight-ul depinde de modul: fixture valid pentru `REUSE_FIXTURE`; config de înregistrare completă și opt-in pentru `REGISTER_PER_RUN`; nicio fallback silențioasă între moduri. Înregistrarea rămâne mutație externă persistentă: fără cleanup demonstrat, retenția/acceptarea conturilor create trebuie declarată, iar outcome-ul incert nu se reîncearcă |
| README | documentează manual setup one-time și comenzi IntelliJ/Maven pentru fixture existent | păstrează instrucțiunile curente până la implementare; după implementare documentează ambele moduri, efectul extern și ce evidence se așteaptă |

- Testele de profil/preflight nu fac login sau purchase live. Ultima verificare a
  fixture-ului reutilizat a fost respinsă la login; un email rezervat a fost
  oprit înainte de browser. Prin urmare, nu există încă un authenticated `PASS`.

**Launch gata; lifecycle-ul contului este incomplet — `SHOP-E2E-INTELLIJ-AUTH-01` (70 / P1).**

- Gata: launch individual fără VM options, reutilizare fixture, skip acționabil și page object/data parțiale pentru formularul de înregistrare.
- Deschis: selectarea prin adnotare între fixture reutilizat și cont nou per-rulare, provisioning real în `SETUP`, login pe contul nou și purchase end-to-end.
- Blocat pentru proof live: fixture-ul actual nu autentifică; modul nou nu poate fi considerat funcțional până la implementare și run verificat.
- Confidențialitate: fixture-ul și capturile cu identitate rămân locale; nu se
  publică date de cont în feedback sau raport.

## Contractul cerut pentru prezentarea E2E

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
- Setarea nouă controlează numai dacă evidence-ul pașilor Playwright este
  publicat ca un grup UI. Ea nu generează Gherkin, nu definește pași prin YAML
  și nu schimbă acțiunile sau aserțiunile executate.
- Fără model plug-and-play pentru UI substeps: nu există listă extensibilă de
  checkpoint-uri Gherkin și nici câte un report node per click/navigation.
- UI-only versus UI+API rămâne axa existentă `validation.profile`/`apiEvidence`;
  nu se deduce din setarea UI steps. UI-only oprește evidence-ul API, nu
  request-urile aplicației.
- Purchase-ul rămâne complet: flow-ul apasă `Confirm` și validează confirmarea
  comenzii. Ascunderea evidence-ului UI nu scurtează flow-ul.
- Captura finală este policy separată de Gherkin și de acțiuni; când este aleasă
  `FINAL_ONLY`, se publică o singură imagine finală, nu una per acțiune.

Forma de mai jos este doar schiță de intenție; cheia YAML nu este încă un
contract implementat:

```yaml
practiceShop:
  reporting:
    uiSteps: collapsed # un grup UI; nu este o listă de subpași Gherkin
    screenshotPolicy: final-only # policy separată; cheia consumerului rămâne de stabilit
  validation:
    profile: ui-api # ui-only | ui-api; independent de uiSteps
```

`ui-only` și `ui-api` există deja. `ReportEvidenceFlowMode.UNIFIED_FLOW` și
`FINAL_ONLY` sunt contracte generice implementate în reporting; wiring-ul
Practice Shop nu le expune încă drept configurare consumer typed.

Contractele rezolvate — profilele UI-only/UI+API, runtime boundary, overview și
API evidence — sunt documentate în [README-ul Practice Shop](../../atf-test-practice-shop/README.md)
și nu rămân ca ToDo-uri în acest feedback. Incidentele IntelliJ de reporting
rămân numai în feedback-ul ownerului `ATF_Reporting_Feedback.md`.

## Regula de validare

- contractul de single-flow se probează în HTML cu aserții negative explicite:
  zero carduri `Checkpoint N`, zero propoziții Gherkin duplicate și un singur
  business proposition;
- `uiSteps` se verifică drept setare de presentation-only: aceleași acțiuni,
  aserțiuni și rezultat de purchase pentru enabled/disabled;
- UI-only / UI+API se verifică separat; rularea UI-only live creează o comandă demo;
- `FINAL_ONLY` cere o singură captură finală; nu se deduce din ascunderea pașilor;
- niciun diagnostic runtime pentru ramura guest acceptată nu se publică în raport; erorile runtime fără corelare rămân failure;
- artifactul proaspăt se inspectează în MCP pe `Overview`, `Tests` și `Fails`;
- nu se blochează traficul normal al site-ului pentru a ascunde evidence.

Un run E2E live poate crea o comandă externă. Nu se generează o comandă nouă
doar pentru verificarea documentației; se folosește o rulare deja autorizată.
