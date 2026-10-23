# ATF — focalizarea testelor consumer pe business

Feedback-ul păstrează numai lacunele rămase de ownership și acoperire din
Shop/AutoEx. Regulile stabile sunt în [ghidul de testare](../ATF_Testing_Guide.md#reducerea-suitei-fără-pierdere-de-acoperire)
și README-urile ownerilor; mutările propuse nu sunt declarate implementate.

## Ordinea intervențiilor

Scorul folosește formula registrului: impact /40 + lacună /30 + lucrabil /20 +
datorie documentară /10. El ordonează lucrul, nu evaluează calitatea modulului.

| ID | Scor | Intervenție |
|---|---:|---|
| `CONSUMER-PROOF-OWNERSHIP-01` | 75 = 33 + 15 + 20 + 7 | restrângerea probelor mixte la contractele business și adaptor |
| `CONSUMER-E2E-PROFILES-01` | 57 = 24 + 10 + 17 + 6 | reducerea repetării live pentru profile de evidence |

## CONSUMER-PROOF-OWNERSHIP-01 — degajarea probelor generice

**Stare: deschis.** Rămân probe mixte de restrâns; nu se mută integral pachetele `api.reporting` sau `component.api`.

### Probe mixte și ownerul rezultatului

| Sursă locală | Ce trebuie păstrat local | Ce se consolidează la ownerul generic |
|---|---|---|
| `ShopApiAuthenticationSchemaTest` | profil/mecanism și redacție prin mapper direct și browser journal | etichetele HTML și paritatea rendererului: `ApiAuthenticationSchemaContractTest` |
| `ShopApiCallAuditReportComponentTest.retryAttemptsStayInsideOnePublishedRequestAndExecutionDisclosure` | forwarding-ul attempts/history/provenance prin mapperul și publisherul Shop | request unic și disclosure unic pe standalone și UI+API |
| celelalte probe `ShopApiCallAuditReportComponentTest` | intenții distincte, așteptare HTTP explicită, mismatch, lipsa asocierii și eșecul JUnit al adaptorului | prezentarea diagnosticelor: `ReportPhaseDiagnosticTest` și probele browser neutre |
| `AutoExApiContractReportPublisherComponentTest` | `responseCode`/mesaj față de HTTP 200, form-encoded, MIME upstream și alegerea directorului | formatul comun Request/Response/Execution și escaping/redactare generică |
| `ShopApiBodyRedactionReportTest` | politica Shop pentru identitate/adresă, body malformed și ieșirea sanitizată a adaptorului | mecanismul generic de redactare; politica implicită nu substituie câmpurile Shop |

### Criterii de închidere

- [ ] Pentru fiecare reducere, identificați proba echivalentă și cazul negativ:
      pierderea mecanismului auth, retry history sau redacției trebuie respinsă
      în continuare de proba potrivită.
- [ ] Folosiți DTO-uri/snapshot semantic pentru mapping; păstrați o probă de
      ieșire a adaptorului pentru siguranța artifactului. Un mapper corect nu
      dovedește singur lipsa scurgerilor în HTML.
- [ ] Completați în Reporting Tests numai lipsa neutră; proba UI+API de retry
      existentă nu certifică automat calea standalone sau mapperul Shop.
- [ ] Rulați probele neutre, `ShopComponentSuite`/`AutoExComponentSuite` și
      guvernanța schimbării testelor pentru path-urile exacte; fără dependency
      Shop/AutoEx în Reporting Tests/Test Kit.

## CONSUMER-E2E-PROFILES-01 — variantele de evidence și acoperirea business

**Stare: analizat, selecția suitelor este încă neschimbată.** Se reduce costul
rulărilor externe, păstrând configurabilitatea cerută și probele de business.

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
