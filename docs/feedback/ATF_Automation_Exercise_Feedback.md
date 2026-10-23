# Automation Exercise — cerințe business rămase

Consumerul verifică magazinul public prin journey-uri reale și reutilizează
browserul, adnotările, cleanup-ul și raportarea ATF. Contractul stabil și
comenzile sunt în [README-ul AutoEx](../../atf-test-auto-ex/README.md).

## ToDo — intenții business distincte

| ID | Cerință | Acceptanță | Prioritate |
|---|---|---|---:|
| `AUTOEX-CLEANUP-RECOVERY-01` | Confirmă recuperarea cleanup-ului după o întrerupere sub vignette | Proba live negativă trebuie să păstreze cauza inițială și captura, să navigheze la magazin și să confirme ștergerea identității acelei rulări. Un succes fără reclamă nu certifică acest traseu. | 76 |
| `AUTOEX-CART-EDIT-01` | Modificarea și eliminarea produselor din coș | Un journey distinct verifică recalcularea cantității/totalului și eliminarea rândului; nu repetă checkout-ul numai pentru alt profil de captură. | 67 |
| `AUTOEX-SESSION-01` | Continuitatea coșului după autentificare | Identitatea sintetică și produsele alese sunt păstrate după login; contul creat de test are cleanup explicit. | 62 |

Aceste extensii sunt backlog de business. Testele generice de adnotări,
Playwright, lifecycle, HTML/CSS/JS și layout aparțin modulelor ATF owner.

## Increment în lucru — coș, sesiune și cleanup observat

Implementarea păstrează confirmarea creării înainte de navigarea cleanup-ului,
verifică identitatea înaintea ștergerii și reia autentificarea numai cu datele
contului sintetic al rulării. Recuperarea din vignette se publică numai când
fragmentul a fost observat efectiv; proba negativă nu este simulată și nu se
închide printr-o rulare fără reclamă.

`AutoExBusinessSuite` selectează trei povești distincte, nu trei formate ale
aceleiași comenzi. `AutoExApiComponentSuite` separă componentele offline de
suitele API live existente. Acceptanța rămâne în lucru până la rularea lor și
inspecția rapoartelor finale în MCP.

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
