# AutoEx — compatibilitatea business rămasă

Contractele stabile pentru vocabularul UI/API, validarea căutării și ownership-ul
fazelor sunt în [README-ul AutoEx](../../atf-test-auto-ex/README.md#vocabular-și-adrese).
Feedback-ul păstrează numai lacunele business încă neînchise.

Activarea Close opt-in la 70% și contractul său de evidence sunt documentate în
[README AutoEx](../../atf-test-auto-ex/README.md#vignette-și-activare-javascript).
Limita pointerului rămâne în [feedback-ul generic UI](ATF_UI_Browser_Geometry_Feedback.md).

## AUTOEX-DELETE-AFTER-AD-01 — confirmarea ștergerii după reclamă

**Prioritate:** 65/100 = impact 36 + lacună 18 + lucrabil 10 + documentare 1,
conform [registrului comun](README.md#top-feedback-uri-active).

**Stare: corecție implementată; acceptanță remote incompletă.** Confirmarea
cere acum identitatea exactă, ruta `/delete_account`, heading-ul dedicat și
un singur request. Regula de continuare și refuzul replay-ului sunt probate
în componentă; flow-urile reale UI/UI_API și self-service-ul contului trec la 70%.

Contractul livrat este în [README AutoEx — navigare întreruptă](../../atf-test-auto-ex/README.md#delete-account--navigare-întreruptă).
În rapoartele inspectate în MCP se văd ruta, numărul real de request-uri,
confirmarea și captura ștergerii. Reclama a apărut natural în altă tranziție,
nu încă în Delete account; succesul acelei navigări nu închide criteriul de mai jos.

- [ ] Observați vignette-ul natural chiar la Delete account într-un flow real
      la 70%; fără hash, overlay sau răspuns fabricat și fără pauză artificială
      introdusă ca să provoace reclama.
- [ ] În MCP, verificați pasul Gherkin al continuării: zero request-uri înainte,
      identitate revalidată, un singur request final și confirmarea pe ruta corectă;
      dacă un request a pornit deja, lipsa confirmării rămâne FAIL fără replay.

Traseu în verificare: client existent pregătit prin API-ul public, login în UI,
apoi Delete account în UI. Pregătirea contului este evidence de setup, nu trafic
cauzat de UI. Nu schimbă probabilitatea sau conținutul reclamelor prin scripting.

## Control tehnic activ

```yaml
testChanges:
  - id: AUTOEX-EXISTING-CUSTOMER-DELETE-01
    status: ACTIVE
    base: 5eb3fd3cb099fc67b49be3c17de564db63755db7
    expires: 2026-11-24
    reason: Adaugă self-service-ul real login/delete pentru un client pregătit prin API, fără reclame fabricate sau replay; modificările adaptorului și factory-ului live rămân la controalele existente AUTOEX-VOCABULARY-01 și AUTOEX-CONTRACT-VOCABULARY-02, revalidate de aceeași probă.
    proof: mvn -pl atf-test-auto-ex,atf-tests -am "-Dtest=AutoExComponentSuite,AutoExExistingCustomerUiApiE2eTest,AutoExAccountUiE2eTest,AutoExApiLiveSuite,WholeAtfGovernanceSuiteTest" "-Datf.autoex.e2e.enabled=true" "-Datf.autoex.api.live.enabled=true" "-Datf.governance.testChanges.baseRef=5eb3fd3cb099fc67b49be3c17de564db63755db7" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExCustomerApiSetup.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/e2e/AutoExExistingCustomerUiApiE2eTest.java
```
