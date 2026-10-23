# ATF — focalizarea testelor consumer pe business

Feedback-ul păstrează reducerea încă nevalidată a repetării live pentru
profilele Shop/AutoEx.
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
Acceptanța și comenzile gate-ului business sunt normate în
[README-ul AutoEx](../../atf-test-auto-ex/README.md#rulare); selecția Shop nu este redusă.

| Modul | Variante de păstrat ca dovadă reală | Repetare determinată de evidence |
|---|---|---|
| Shop | cont nou + purchase autentificat, cont existent + purchase, guest UI+API și guest UI-only | `ShopUiOnlySingleFlowPurchaseE2eTest` reia guest UI-only pentru `UNIFIED_FLOW` |
| AutoEx | purchase UI și UI+API; al doilea adaugă precondiția API catalog și observarea traficului | `AutoExPurchaseUiFinalScreenshotE2eTest` rulează același journey cu alt profil |

### Condiții înainte de reducerea selecției live

- [ ] Păstrați un smoke local per adaptor: adnotare/YAML → profilul scenariului
      → captură/publicare, pe succes și failure; reflecția adnotării sau
      verificarea enum-ului nu sunt suficiente.
- [ ] Reutilizați `ReportEvidenceProfileIntegrationTest` și
      `UiScreenshotPolicyIntegrationTest` pentru contractul generic, fără
      duplicarea probelor de renderer în consumer. Izolarea profilurilor și
      flow-ul Shop cross-phase sunt contracte închise; aici lipsește dovada
      echivalenței selecției reduse, nu o nouă implementare a lor.
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
