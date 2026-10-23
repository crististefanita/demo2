# Automation Exercise — cerințe business rămase

Consumerul verifică magazinul public prin journey-uri reale și reutilizează
browserul, adnotările, cleanup-ul și raportarea ATF. Contractul stabil și
comenzile sunt în [README-ul AutoEx](../../atf-test-auto-ex/README.md).

## ToDo — acceptanță încă deschisă

| ID | Cerință | Acceptanță | Prioritate |
|---|---|---|---:|
| `AUTOEX-VIGNETTE-DISMISS-01` | Închiderea auditabilă a reclamei reale | În `AutoExBusinessSuite`, controlul vizibil `#dismiss-button` este uneori interceptat de corpul iframe-ului. Rezolvarea trebuie să folosească o interacțiune UI reală cu Close, fără force-click/DOM injectat, cu probă component și o rulare live în care toate cele trei povești trec. Eșecul și cleanup-ul actual nu trebuie mascate. | 76 |
`AUTOEX-CLEANUP-RECOVERY-01` este probat pe o reclamă **reală**, nu doar pe
un hash controlat: rularea `AutoExBusinessSuite` a păstrat `TimeoutError` de la
Close ca eroare primară și `Failure evidence` cu captură, a navigat din
`#google_vignette` la Home și a publicat confirmarea ștergerii contului acelei
rulări. Corecția care a permis cleanup-ul evită tratarea ca unic a selectorului
`h2.title.text-center` pe pagina Home. Proba controlată de staging a verificat
suplimentar aceeași ordine fără să fie confundată cu dovada live.

## Verificat — povești business

| ID | Dovadă din rularea izolată, pe magazinul public |
|---|---|
| `AUTOEX-CART-EDIT-01` | `AutoExCartUiE2eTest#recalculatesAndRemovesCartProduct` a trecut: același produs a fost adăugat din nou, cantitatea și totalul au fost recalculate, apoi rândul a fost eliminat și mesajul coșului gol verificat. A doua căutare a necesitat așteptarea rezultatului; pagina are și un `#empty_cart` ascuns, deci confirmarea folosește numai elementul vizibil. |
| `AUTOEX-SESSION-01` | `AutoExCartUiE2eTest#preservesGuestCartAfterLoginAndRemovesAccount` a trecut: identitatea sintetică, produsul, cantitatea și totalul au fost verificate după login, apoi ștergerea contului a fost confirmată. |
| `AUTOEX-CLEANUP-RECOVERY-01` | Proba live negativă din `AutoExBusinessSuite` a păstrat eșecul de închidere a vignette-ului, captura, revenirea la magazin și ștergerea contului. Testul a rămas corect marcat FAIL; cleanup-ul nu transformă eșecul în PASS. |

Pentru `AUTOEX-VIGNETTE-DISMISS-01`, o probă izolată cu focus + Enter pe
controlul vizibil a trecut pe un iframe de test, **dar a eșuat pe reclama
reală**: Enter nu a închis-o, iar click-ul a fost interceptat în continuare
de `body` din iframe. Varianta experimentului meu nu a fost promovată; între
timp, în codul live a apărut separat o variantă Enter-only, care necesită
probă live și nu poate fi declarată remediu pe baza fixture-ului. Nu folosiți
un fixture sintetic drept dovadă suficientă și nu rezolvați prin force-click
sau injectare de DOM; este necesară o interacțiune UI reală, verificată pe
reclama efectivă.

Cele două fluxuri au trecut separat și în gate-ul combinat din staging.
`AutoExBusinessSuite` a rulat 3 teste: 2 PASS, 1 ERROR din interacțiunea
cu reclama reală; nu declarăm gate-ul verde. Testele generice de adnotări,
Playwright, lifecycle, HTML/CSS/JS și layout aparțin modulelor ATF owner.

## Increment în lucru — coș, sesiune și cleanup observat

Implementarea păstrează confirmarea creării înainte de navigarea cleanup-ului,
verifică identitatea înaintea ștergerii și reia autentificarea numai cu datele
contului sintetic al rulării. Recuperarea din vignette se publică numai când
fragmentul a fost observat efectiv; proba negativă nu este simulată și nu se
închide printr-o rulare fără reclamă.

- `AutoExBusinessSuite` selectează trei povești distincte, nu trei formate ale
  aceleiași comenzi; `AutoExApiComponentSuite` separă componentele offline de
  suitele API live existente.
- `AutoExComponentSuite` a trecut în staging cu 49 de teste și zero failure/error.
- Inspectarea vizuală prin MCP a rapoartelor locale rămâne necesară înaintea
  acceptanței finale a suitei; protocolul `file:` nu este disponibil agentului
  în tabul MCP.

```yaml
testChanges:
  - id: AUTOEX-CART-SESSION-01
    status: ACTIVE
    base: 0f6a95265024a78c20754d5c18937d24434a2d2d
    expires: 2026-11-23
    reason: Adauga povesti business reale pentru recalcularea cosului si continuitatea dupa login, pastrand cleanup-ul identitatii si suite API separate.
    proof: mvn -pl atf-test-auto-ex,atf-tests -am "-Dtest=AutoExComponentSuite,AutoExBusinessSuite,AutoExApiReadOnlyLiveSuite,AutoExApiLiveSuite,WholeAtfGovernanceSuiteTest" "-Datf.autoex.e2e.enabled=true" "-Datf.autoex.api.readonly.live.enabled=true" "-Datf.autoex.api.live.enabled=true" "-Datf.governance.testChanges.baseRef=0f6a95265024a78c20754d5c18937d24434a2d2d" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExE2eRun.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/api/AutoExApiLiveContractTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/e2e/AutoExCartUiE2eTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/suites/AutoExApiComponentSuite.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/suites/AutoExBusinessSuite.java
```
