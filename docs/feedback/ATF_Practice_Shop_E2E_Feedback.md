# Practice Shop E2E — live reconciled feedback

## Stare

Direcția cerută este acum explicită: `PracticeShopPurchaseE2eTest` rămâne un
singur test pentru un purchase complet, cu **o singură propoziție Gherkin** în
raport. Detaliile pașilor Playwright sunt evidence UI configurabilă, nu o listă
configurabilă de subpropoziții Gherkin.

Codul din worktree conține deja încercări necomise pentru `UNIFIED_FLOW` și un
`PracticeShopPurchaseUiOnlyE2eTest` separat. Nu le considerăm contract închis:
publisher-ul nou adaugă un părinte `UNIFIED FLOW`, dar păstrează copii
`Checkpoint N — <Gherkin title>` și câte o propoziție per checkpoint; testul
UI-only separat repetă purchase-ul în loc să configureze testul canonic.
Aceste forme nu satisfac cerința clarificată și nu au fost validate printr-un
raport fresh în această analiză.

| Suprafață | Fapt verificat live |
|---|---|
| Test canonic | `PracticeShopPurchaseE2eTest` execută `PracticeShopPurchaseFlow` și trimite checkpoint-urile către `journey.checkpoint`; flow-ul real include confirmarea finală a comenzii |
| Config YAML | `validation.profile` și `apiEvidence` controlează evidence-ul API; `screenshotOnFailure` este boolean legacy; nu există încă setare consumer pentru detaliile pașilor UI |
| WIP `UNIFIED_FLOW` | bufferul agregă checkpoint-uri, dar publisher-ul le republică drept `Checkpoint N — GIVEN/WHEN/THEN ...`; un wrapper comun nu elimină fragmentarea semantică |
| WIP UI-only | `PracticeShopPurchaseUiOnlyE2eTest` selectează UI-only prin adnotare și rulează încă un purchase separat; nu demonstrează configurarea YAML a testului canonic |
| Limită de raport curent | ultimul raport menționat în feedback este dinaintea schimbărilor locale; nu este dovadă pentru noul arbore sau pentru YAML-ul propus |

Request-urile clientului către aplicație continuă normal în ambele profile.
`ui-only` înseamnă fără captură/publicare API de către evidence consumer, nu
blocarea traficului aplicației.

## ToDo-uri active

| ID | Scor / prioritate | Gap verificat | Criteriu de închidere |
|---|---:|---|---|
| `SHOP-E2E-PRESENTATION-01` | **66 / P1** | lipsește configurarea YAML pentru evidence-ul UI; `UNIFIED_FLOW` din WIP păstrează subpropozițiile checkpoint-urilor, iar UI-only este un al doilea test | un singur `PracticeShopPurchaseE2eTest`; o singură propoziție Gherkin; UI steps sunt un singur grup tehnic configurabil, fără registry/plugin sau subpropoziții; API profile-ul existent rămâne independent; captura finală urmează `FINAL_ONLY`. Proof de config și HTML fără purchase live |
| `SHOP-E2E-BOUNDARY-01` | **62 / P1** | capture/runtime diagnostics păstrează excepții specifice pentru semnale guest/bootstrap; raportul le ascunde, dar logica și testele încă le cunosc | elimină excepțiile ad-hoc și le înlocuiește cu binding-ul API contractual; nu bloca request-urile aplicației; errors neallowlist-uite rămân failure; raportul nu conține request, warning sau pas sintetic |

## Contractul cerut pentru prezentarea E2E

```text
TEST BODY
└── WHEN guest completes the configured purchase and sees the order confirmation
    ├── UI validation
    │   ├── UI steps: one grouped, collapsed technical disclosure (configurable)
    │   └── final screenshot: one capture when FINAL_ONLY is selected
    └── API validation: present only when the existing API evidence profile selects it
```

- Un singur flow business și o singură propoziție Gherkin sunt invariantul; nu
  există `GIVEN/AND/WHEN/THEN` separat pentru catalog, produs, cart, billing,
  payment și confirmare.
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

`ui-only` și `ui-api` există deja. `ReportEvidenceFlowMode.UNIFIED_FLOW` este în
lucru în worktree, însă forma care păstrează `Checkpoint N` nu satisface
contractul de mai sus. `FINAL_ONLY` există generic, dar wiring-ul Practice Shop
nu îl expune încă drept policy consumer typed.

## Dovada auditului curent

| Fapt | Dovadă live / limită |
|---|---|
| Confirmarea purchase-ului | `PracticeShopConfirmationPage.confirmOrder()` apasă `Confirm`; această acțiune și validarea rezultatului trebuie să rămână în fiecare mod de prezentare |
| Configurația curentă | YAML are `profile`, `apiEvidence` și `screenshotOnFailure`; nu exprimă dacă detaliile UI apar în raport sau cum sunt grupate |
| Raportare unificată WIP | `UiEvidencePublisher.publishUnifiedFlow(...)` creează un părinte, dar păstrează titluri și fields `Checkpoint N` din fiecare Gherkin; nu este încă un singur business proposition |
| Multiplicarea run-urilor | clasa separată UI-only execută același purchase; targetul cerut este un singur test canonic configurat, evitând run-uri live duplicate pentru o variație de evidence |
| Verificare de randare | nicio probă fresh MCP pentru starea locală necomisă; verdictul și geometria din artifactul anterior nu închid acest ToDo |

## Contracte închise și proof-ul lor

| Contract | Implementare live | Proof de regresie |
|---|---|---|
| Precondiția din Overview nu pretinde autentificare observată | `PracticeShopReportSystemMetadata` afișează `API precondition (configured)` și separă starea journey-ului UI; inspectorul rămâne sursa pentru auth efectivă | `PracticeShopReportSystemMetadataTest` și network aggregate-ul E2E; exchange-urile guest arată explicit `AUTHENTICATION none` |
| Purchase complet, inclusiv confirmarea finală | `PracticeShopPurchaseFlow` apasă `Confirm`; payment check doar validează metoda și nu creează comanda | `PracticeShopPurchaseE2eTest`, assertions pentru mesajul de confirmare și order response |
| Un owner semantic pentru evidence | checkpoint-ul Gherkin în modul checkpoint sau propoziția unică a flow-ului în modul unificat deține validările UI/API; retry-ul rămâne în exchange-ul logic | `PracticeShopPurchaseTraceStepTest` și publisher/report tests; pentru single-flow lipsește încă testul HTML fără `Checkpoint N` |
| Reportul nu creează warning sau pas Gherkin din runtime diagnostics | completion-ul publică `PASS`; artifactul MCP proaspăt arată zero diagnostics și zero warnings | `PracticeShopRuntimeDiagnosticsTest` și raportul MCP current |
| `ui-only` suprimă API evidence în consumer | `PracticeShopApiEvidenceBinding.none()` face attach și publish no-op | `PracticeShopApiEvidenceBindingTest`, `PracticeShopValidationProfileTest`; lipsește proof HTML consumer |
| Semnalele ambientale nu sunt publicate ca API evidence | capture-ul le exclude, dar păstrează încă filtre guest/bootstrap explicite în sursă și teste; acestea nu sunt contract stabil | cleanup-ul este activ în `SHOP-E2E-BOUNDARY-01` |
| Erorile runtime | `assertClean()` eșuează pentru erorile neașteptate; încă există allowlist pentru 401 înainte de login dovedit și un mesaj `cart_items` | `PracticeShopRuntimeDiagnosticsTest`; allowlist-ul rămâne în ToDo până la eliminare |
| Billing verifică label-ul vizibil, nu codul tehnic al opțiunii | `PracticeShopCountryOption` păstrează label și value separat; validatorul compară label-ul | `PracticeShopCountryOptionTest` și `PracticeShopApiAssertionsTest` |
| Network aggregate nu stretch-uiește rândurile la viewport îngust | `extent-evidence-roles.js` marchează cardul agregat; `evidence-stack.css` fixează statusul și calculează lane-ul Details | `ExtentReportRuntimeScriptTest`, `AtfReportEvidenceCssTest`, `CategoryCardinalityBrowserFixtureTest` și `ScrollableTableViewportBrowserProbe`; MCP confirmă că pagina nu are overflow orizontal |

## Regula de validare

- contractul de single-flow se probează în HTML cu aserții negative explicite:
  zero carduri `Checkpoint N`, zero propoziții Gherkin duplicate și un singur
  business proposition;
- `uiSteps` se verifică drept setare de presentation-only: aceleași acțiuni,
  aserțiuni și rezultat de purchase pentru enabled/disabled;
- UI-only / UI+API se verifică separat pe HTML generat fără purchase nou;
- `FINAL_ONLY` cere o singură captură finală; nu se deduce din ascunderea pașilor;
- failure-urile runtime reale nu devin warnings pentru a păstra testul verde;
- artifactul proaspăt se inspectează în MCP pe `Overview`, `Tests` și `Fails`;
- nu se blochează traficul normal al site-ului pentru a ascunde evidence.

Un run E2E live poate crea o comandă externă. Nu se generează o comandă nouă
doar pentru verificarea documentației; se folosește o rulare deja autorizată.
