# ATF — arhitectura adaptorului Console–Reporting

Contractele stabile despre adnotări, limite `main`/`test` și ownership sunt în
README-urile ownerilor și în [arhitectura ATF](../ATF_Architecture.md). Gap-ul
de integrare Console–Reporting de mai jos este rezolvat și păstrat aici numai
ca decizie verificabilă; registrul de backlog nu îl mai numără ca prioritate.

## CONSUMER-REPORT-BRIDGE-01 — adaptor comun Console–Reporting

**Stare: rezolvat și verificat.** Ownership-ul unic este
`atf-console/src/main/java/atf/api/console/reporting/AtfReportingConsoleSinkAdapter.java`.

- Adaptorul traduce evenimentele `atf-reporting` către portul `AtfConsoleSink`
  din Core.
- `atf-console` depinde de `atf-reporting`; dependența inversă nu există, deci
  librăria de raportare rămâne standalone.
- `atf-core` și `atf-test-kit` nu cunosc Reporting.
- Shop consumă adaptorul direct; seam-ul package-private AutoEx doar deleagă la
  el și nu mai deține mapping duplicat.

### Rezultatul și probele

- Testul componentă acoperă toate scope-urile pentru `START`, `DETAIL` și
  `CODE`, toate statusurile și durata pentru `COMPLETE`, valoarea nulă validă
  pentru statusurile non-terminal, calea absolută `REPORT_PUBLISHED`, sink nul
  și eveniment nul.
- `ShopConsoleBridge` a fost eliminat; testul de arhitectură Shop a rămas intact.
- Rulare izolată, de la `HEAD` cu schimbarea Consumer: `ShopComponentSuite` —
  224 passed; `AutoExComponentSuite` — 21 passed; adapter component — 3 passed;
  `TestChangeGovernanceTest` — 1 passed; zero failure/error/skip. Reactorul
  compilează `atf-console`, Shop și AutoEx.
- Revalidare pe snapshot-ul cu modificările Shop live, în staging separat:
  `ShopComponentSuite` — 226 passed, zero failure/error/skip.
- Încercarea pe snapshot-ul cu modificări concurente s-a oprit înainte de teste
  la trei apeluri incompatibile în `AutoExApiReadOnlyLiveContractTest`; acestea
  sunt externe acestui increment și nu au fost modificate. AutoEx nu a fost
  validat pe acel snapshot; suita sa a trecut pe baseline-ul curat menționat mai sus.

### Contract promovat

- Orice conversie Reporting → Core aparține adaptorului din `atf-console`; modulele
  consumer nu reimplementează maparea și librăria `atf-reporting` nu depinde de
  Console/Core.
- Adaptorul nu scrie la consolă, nu creează raport și nu modifică lifecycle,
  verdict ori renderer; doar proiectează același eveniment în modelul Core.
- `ShopArchitectureTest` și pragurile de guvernanță rămân nemodificate.

Schimbările de teste sunt autorizate temporar de controlul de mai jos, până când
diff-ul este integrat în baseline-ul Git; controlul nu include alte fișiere sau
editări concurente.

```yaml
testChanges:
  - id: CONSUMER-REPORT-BRIDGE-TESTS-01
    status: ACTIVE
    base: 4c97b18d6a8e7a17b979db40238f13ce20a481c9
    expires: 2026-10-30
    reason: Centralize the Reporting-to-Core event adapter and verify the mapping and AutoEx delegation without changing governance rules.
    proof: mvn -pl atf-test-practice-shop,atf-test-auto-ex,atf-tests -am -Dtest=ShopComponentSuite,AutoExComponentSuite,AtfReportingConsoleSinkAdapterTest,TestChangeGovernanceTest -Dsurefire.failIfNoSpecifiedTests=false -Datf.governance.testChanges.baseRef=HEAD test
    paths:
      - atf-console/pom.xml
      - atf-console/src/test/java/atf/api/console/reporting/AtfReportingConsoleSinkAdapterTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/autoex/support/AutoExConsoleBridge.java
```
