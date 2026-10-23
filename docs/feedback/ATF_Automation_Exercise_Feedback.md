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

Traseul suplimentar de [client existent](../../atf-test-auto-ex/README.md#pregătirea-clientului-existent)
este livrat și verificat nominal. Succesul login/delete și al pregătirii API
nu înlocuiește proba reclamei naturale din criteriile de mai sus.
