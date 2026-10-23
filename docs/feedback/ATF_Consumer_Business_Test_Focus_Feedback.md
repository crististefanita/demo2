# ATF — focalizarea testelor consumer pe business

Feedback-ul păstrează reducerea încă nevalidată a repetării live pentru
profilele Shop/AutoEx și lacunele preexistente de clasificare.
Ownership-ul suportului și al probelor
neutre este documentat în [ghidul de testare](../ATF_Testing_Guide.md#reducerea-suitei-fără-pierdere-de-acoperire)
și README-urile ownerilor.

## Ordinea intervențiilor

Scorul folosește formula registrului: impact /40 + lacună /30 + lucrabil /20 +
datorie documentară /10. El ordonează lucrul, nu evaluează calitatea modulului.

| ID | Scor | Intervenție |
|---|---:|---|
| `CONSUMER-E2E-PROFILES-01` | 57 = 24 + 10 + 17 + 6 | reducerea repetării live pentru profile de evidence |

## CONSUMER-E2E-PROFILES-01 — variantele de evidence și acoperirea business

**Stare: parțial implementat.** AutoEx are `AutoExBusinessSuite` pentru povești
distincte și păstrează separat matricea manuală `AutoExPurchaseSuite`.
Acceptanța live a gate-ului business este urmărită în
[feedback-ul AutoEx](ATF_Automation_Exercise_Feedback.md); selecția Shop nu este redusă.

| Modul | Variante de păstrat ca dovadă reală | Repetare determinată de evidence |
|---|---|---|
| Shop | cont nou + purchase autentificat, cont existent + purchase, guest UI+API și guest UI-only | `ShopUiOnlySingleFlowPurchaseE2eTest` reia guest UI-only pentru `UNIFIED_FLOW` |
| AutoEx | purchase UI și UI+API; al doilea adaugă precondiția API catalog și observarea traficului | `AutoExPurchaseUiFinalScreenshotE2eTest` rulează același journey cu alt profil |

### Condiții înainte de reducerea selecției live

- [ ] Păstrați un smoke local per adaptor: adnotare/YAML → profilul scenariului
      → captură/publicare, pe succes și failure; reflecția adnotării sau
      verificarea enum-ului nu sunt suficiente.
- [ ] Reutilizați `ReportEvidenceProfileIntegrationTest` și
      `UiScreenshotPolicyIntegrationTest` pentru contractul generic; gap-ul
      verificarea vizuală Shop cross-phase `SHOP-CAPTURE-01` rămâne independentă.
- [ ] Păstrați exemplele de adnotări executabile manual; decideți dacă ies numai
      din suita live sau sunt înlocuite de fixture-uri locale. Clasa AutoEx
      este folosită direct de `AutoExEvidenceProfileComponentTest`.
- [ ] Schimbarea selecției protejate în `ConsumerModuleArchitectureGovernanceTest`
      cere feedback direct asupra regulii și control exact; guvernanța nu se
      relaxează implicit în acest audit.
- [ ] Confirmați rularea independentă a clasei cu cont existent; ordinea suitei
      nu devine precondiție ascunsă.
- [ ] Aliniați README-urile și suitele după probele de echivalență; mock-ul nu
      înlocuiește confirmarea fluxului live UI+API.

## TEST-LAYER-DECLARATIONS-01 — metadate preexistente de clasificare

**Stare: deschis, separat de consolidarea probelor.** Owner: modulele claselor
de test; regula executabilă rămâne în Test Kit.

| Probă / zonă | Constatare |
|---|---|
| `mvn -pl atf-test-kit test` | 52 cazuri: 50 trecute, două eșecuri preexistente de clasificare |
| `AtfTestLayerArchitectureTest` | `everyConcreteExecutableTestDeclaresExactlyOneLayer` și `moduleLayerMatrixIsDerivedFromDeclaredAnnotations` se opresc la `AtfConsoleTestExtensionIntegrationTest`, fără nivel ATF |
| Inventarul claselor | `AtfReportPathOptInTest` nu are nici el declarația; oprirea la primul fișier nu reprezintă inventar complet |

- [ ] Stabiliți lista completă și declarați nivelul potrivit boundary-ului
      fiecărei clase, păstrând adnotările compuse și separarea
      unit/component/integration/E2E.
- [ ] Rulați guard-ul Test Kit și componentele ownerilor afectați; înregistrați
      path-urile protejate fără relaxarea regulii sau omiterea claselor.

`WholeAtfGovernanceSuiteTest` și acest guard sunt entry-point-uri distincte.
Succesul primei suite nu certifică populația verificată de al doilea.
