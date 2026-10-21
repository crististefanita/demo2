# Practice Shop E2E — live reconciled feedback

## Stare

Purchase trace-ul, verdictul și alinierea raportului cu YAML-ul curent sunt
închise. Raportul MCP proaspăt este `PASS`, fără warning și fără pas Gherkin
sintetic de runtime. Au rămas două gap-uri de proiectare și unul de cleanup al
codului.

| Suprafață | Fapt verificat live |
|---|---|
| Overview/System | `chromium · headed · 1440 × 1000`; profil UI + API; API evidence la `cart, login, order, payment, product`; screenshot `each-checkpoint` |
| Tests/TEST BODY | 11 checkpoint-uri Gherkin; `UI validation` și `API validation` sunt children la checkpoint-urile cauzale; fără propoziții duplicate |
| Network aggregate | 7 exchanges; `Technical diagnostics · 0`; request-ul ambiental nu este publicat |
| Verdict/render | authenticated purchase `PASS`, zero failures/warnings; MCP viewport 857px fără overflow orizontal |
| Config YAML | `ui-only` / `ui-api` și selecția `apiEvidence` sunt implementate; `screenshotOnFailure: true` produce acum `each-checkpoint` |

Request-urile clientului către aplicație continuă normal în ambele profile.
`ui-only` înseamnă fără captură/publicare API de către evidence consumer, nu
blocarea traficului aplicației.

## ToDo-uri active

| ID | Scor / prioritate | Gap verificat | Criteriu de închidere |
|---|---:|---|---|
| `SHOP-E2E-PRESENTATION-01` | **66 / P1** | YAML-ul consumerului nu expune flow mode și policy typed de screenshot; raportul curent publică fiecare checkpoint | `ui-only` produce zero API validation/aggregate; `ui-api` păstrează exchange-urile selectate; `unified-flow` afișează un singur nod business cu acțiunile Playwright colapsate; `final-only` publică o singură captură finală. Adaugă teste de config și HTML fără purchase live |
| `SHOP-E2E-BOUNDARY-01` | **62 / P1** | capture/runtime diagnostics păstrează excepții specifice pentru semnale guest/bootstrap; raportul le ascunde, dar logica și testele încă le cunosc | elimină excepțiile ad-hoc și le înlocuiește cu binding-ul API contractual; nu bloca request-urile aplicației; errors neallowlist-uite rămân failure; raportul nu conține request, warning sau pas sintetic |

Forma YAML cerută pentru presentation este propunere, nu schema implementată:

```yaml
practiceShop:
  validation:
    profile: ui-only # ui-only | ui-api
  reporting:
    evidence:
      flowMode: unified-flow # screen-checkpoints | unified-flow
      screenshotPolicy: final-only
```

`ui-only` există deja. `ReportEvidenceFlowMode.UNIFIED_FLOW` există, dar este
metadata și nu schimbă arborele raportului; `FINAL_ONLY` există generic, însă
consumerul Practice Shop îl mapează încă prin booleanul legacy.

## Dovada auditului curent

| Fapt | Dovadă live / limită |
|---|---|
| Profil și viewport raportate | MCP `Overview → System`: `chromium · headed · viewport 1440 × 1000 · zoom 70%`; YAML-ul curent are `headless: false` |
| Purchase complet | `PracticeShopConfirmationPage.confirmOrder()` apasă `Confirm`; raportul arată confirmarea UI și response evidence pentru order |
| Network aggregate | MCP `Tests → TEST BODY`: 7 exchange-uri și `Technical diagnostics · 0`; fără request ambiental sau pas Gherkin `Runtime diagnostic` în raport |
| Verdict | authenticated purchase `PASS`; `Overview` are 0 failures/warnings, iar `Fails` arată `No failed tests` |
| Geometrie | MCP viewport 857px; `scrollWidth = clientWidth`, fără overflow orizontal |
| Profil `ui-only` | binding-ul gol oprește abonarea journal-ului și publicarea aggregate-ului; testele component acoperă profilul, dar lipsește un proof HTML consumer |

## Contracte închise și proof-ul lor

| Contract | Implementare live | Proof de regresie |
|---|---|---|
| Precondiția din Overview nu pretinde autentificare observată | `PracticeShopReportSystemMetadata` afișează `API precondition (configured)` și separă starea journey-ului UI; inspectorul rămâne sursa pentru auth efectivă | `PracticeShopReportSystemMetadataTest` și network aggregate-ul E2E; exchange-urile guest arată explicit `AUTHENTICATION none` |
| Purchase complet, inclusiv confirmarea finală | `PracticeShopPurchaseFlow` apasă `Confirm`; payment check doar validează metoda și nu creează comanda | `PracticeShopPurchaseE2eTest`, assertions pentru mesajul de confirmare și order response |
| O propoziție Gherkin pentru fiecare checkpoint; validările UI/API sunt copii semantici | `PracticeShopPurchaseTraceStep` și `PracticeShopE2eContext.Scenario.checkpoint(...)` | `PracticeShopPurchaseTraceStepTest` și publisher/report tests |
| Reportul nu creează warning sau pas Gherkin din runtime diagnostics | completion-ul publică `PASS`; artifactul MCP proaspăt arată zero diagnostics și zero warnings | `PracticeShopRuntimeDiagnosticsTest` și raportul MCP current |
| `ui-only` suprimă API evidence în consumer | `PracticeShopApiEvidenceBinding.none()` face attach și publish no-op | `PracticeShopApiEvidenceBindingTest`, `PracticeShopValidationProfileTest`; lipsește proof HTML consumer |
| Semnalele ambientale nu sunt publicate ca API evidence | capture-ul le exclude, dar păstrează încă filtre guest/bootstrap explicite în sursă și teste; acestea nu sunt contract stabil | cleanup-ul este activ în `SHOP-E2E-BOUNDARY-01` |
| Erorile runtime | `assertClean()` eșuează pentru erorile neașteptate; încă există allowlist pentru 401 înainte de login dovedit și un mesaj `cart_items` | `PracticeShopRuntimeDiagnosticsTest`; allowlist-ul rămâne în ToDo până la eliminare |
| Billing verifică label-ul vizibil, nu codul tehnic al opțiunii | `PracticeShopCountryOption` păstrează label și value separat; validatorul compară label-ul | `PracticeShopCountryOptionTest` și `PracticeShopApiAssertionsTest` |
| Network aggregate nu stretch-uiește rândurile la viewport îngust | `extent-evidence-roles.js` marchează cardul agregat; `evidence-stack.css` fixează statusul și calculează lane-ul Details | `ExtentReportRuntimeScriptTest`, `AtfReportEvidenceCssTest`, `CategoryCardinalityBrowserFixtureTest` și `ScrollableTableViewportBrowserProbe`; MCP confirmă că pagina nu are overflow orizontal |

## Regula de validare

- flow mode se probează în `atf-reporting-tests` și în consumerul Practice Shop;
- UI-only / UI+API se verifică pe HTML generat fără purchase nou;
- failure-urile runtime reale nu devin warnings pentru a păstra testul verde;
- artifactul proaspăt se inspectează în MCP pe `Overview`, `Tests` și `Fails`;
- nu se blochează traficul normal al site-ului pentru a ascunde evidence.

Un run E2E live poate crea o comandă externă. Nu se generează o comandă nouă
doar pentru verificarea documentației; se folosește o rulare deja autorizată.
