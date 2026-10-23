# ATF — feedback API pentru Automation Exercise (închis)

Contractele stabile au fost promovate în
[`atf-test-auto-ex/README.md`](../../atf-test-auto-ex/README.md), care este
sursa normativă pentru rute, fixture-uri, rulări live și raportarea API/UI_API.
Acest fișier păstrează numai rezoluția și criteriile de redeschidere; nu este un
jurnal al rapoartelor sau al sesiunilor de test.

## AUTOEX-API-CONTRACT-01 — rezolvat

**Stare:** închis. **Scor rezidual:** 0/100.

Sunt acoperite contractele API offline și live, verificarea semantică a
căutării, separarea statusului HTTP de `responseCode` JSON, păstrarea MIME-ului
observat ca evidence, cleanup-ul failure-safe al contului sintetic și
publicarea raportului API. Pentru `UI_API`, precondiția catalogului este
publicată înaintea baseline-ului; inspectorul include numai schimburile
cauzale ulterioare, iar un agregat valid poate avea zero schimburi.

### Probe de închidere

- `AutoExComponentSuite` probează offline contractele, aserțiunile semantice,
  redacția/publicarea și comportamentul cleanup-ului la eșec.
- `AutoExApiReadOnlyLiveSuite` și `AutoExApiLiveSuite` sunt probe live separate,
  cu opt-in explicit; prima nu mută date, iar a doua folosește un cont sintetic
  cu cleanup.
- `AutoExPurchaseUiApiE2eTest` probează wiring-ul și raportul randat; artifactul
  a fost explorat în TAB MCP, inclusiv cazul valid cu zero schimburi cauzale.

Detaliile contractuale și comenzile reproductibile sunt în README-ul ownerului,
nu se mențin în paralel aici. Redeschideți acest ID numai dacă o regresie
nominală contrazice unul dintre criteriile de mai sus sau contractul documentat.

## Control temporar pentru testele API AutoEx

Acest control acoperă numai schimbările test-only din worktree pentru aserțiuni
semantice și cleanup-ul contului sintetic. Nu redeschide backlog-ul funcțional
închis mai sus și nu autorizează teste E2E sau modificări de guvernanță.

```yaml
testChanges:
  - id: AUTOEX-API-ASSERTIONS-CLEANUP-TESTS-01
    status: ACTIVE
    base: 4e5967b2324c8c1f1f5b66699f86b2d02e92c352
    expires: 2026-10-30
    reason: Record exact temporary approval for the existing AutoEx API contract test edits and the semantic-response/account-cleanup component coverage.
    proof: mvn -pl atf-test-auto-ex -Dtest=AutoExComponentSuite -Dsurefire.failIfNoSpecifiedTests=false test
    paths:
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/api/AutoExApiLiveContractTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/api/AutoExApiReadOnlyLiveContractTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/api/AutoExApiAccountCleanup.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/api/AutoExApiResponseAssertions.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/api/AutoExApiContractReportPublisherComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/api/AutoExApiAccountCleanupComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/api/AutoExApiResponseAssertionsComponentTest.java
```
