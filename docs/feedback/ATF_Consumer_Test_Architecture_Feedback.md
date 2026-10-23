# ATF — adaptor Console–Reporting (închis)

Decizia și contractul stabil au fost promovate în
[`atf-console/README.md`](../../atf-console/README.md#contracte-de-consum).
Acest feedback păstrează numai rezoluția și dovada, nu repetă documentația
normativă.

## CONSUMER-REPORT-BRIDGE-01 — rezolvat

- Unicul owner al mapării Reporting → Core este
  `AtfReportingConsoleSinkAdapter` din `atf-console`.
- `ShopConsoleBridge` a fost eliminat; adaptorul AutoEx rămas doar deleagă.
- Dependența Reporting → Console/Core nu există; Reporting rămâne standalone.
- În staging izolat au trecut `ShopComponentSuite`, `AutoExComponentSuite` și
  `AtfReportingConsoleSinkAdapterTest`.
  `ShopArchitectureTest` și pragurile de guvernanță au rămas nemodificate.

Blocul `testChanges` de mai jos este numai aprobarea temporară a modificărilor
de teste până la expirare/reconciliere; nu este backlog funcțional și nu
autorizează alte fișiere ori modificări ale testelor de guvernanță.

```yaml
testChanges:
  - id: CONSUMER-REPORT-BRIDGE-TESTS-01
    status: ACTIVE
    base: 4c97b18d6a8e7a17b979db40238f13ce20a481c9
    expires: 2026-10-30
    reason: Centralizarea adaptorului Reporting–Core și verificarea mapării/delegării AutoEx fără modificarea regulilor de guvernanță.
    proof: mvn -pl atf-test-practice-shop,atf-test-auto-ex,atf-tests -am -Dtest=ShopComponentSuite,AutoExComponentSuite,AtfReportingConsoleSinkAdapterTest,TestChangeGovernanceTest -Dsurefire.failIfNoSpecifiedTests=false -Datf.governance.testChanges.baseRef=HEAD test
    paths:
      - atf-console/pom.xml
      - atf-console/src/test/java/atf/api/console/reporting/AtfReportingConsoleSinkAdapterTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExConsoleBridge.java
```
