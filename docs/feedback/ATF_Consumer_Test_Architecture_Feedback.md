# ATF — boundary-ul adaptorului Console–Reporting

Implementarea adaptorului există, dar plasarea sa trebuie să treacă
guvernanța whole-ATF înainte de a fi tratată drept contract normativ.

## CONSUMER-REPORT-BRIDGE-01 — implementat, boundary neînchis

**Stare:** parțial implementat; guvernanța whole-ATF îl respinge.
`AtfReportingConsoleSinkAdapter` este în `atf-console` și traduce evenimentele
Reporting în evenimente Core, însă
`ArchitectureModuleBoundaryTest.reportingProductionCodeDoesNotReachIntoExecutionOrBusinessValidation`
interzice explicit referințele `atf.api.reporting` în sursele production ale
modulului Console.

- `ShopConsoleBridge` a fost eliminat, adaptorul AutoEx deleagă, iar
  `AtfReportingConsoleSinkAdapterTest` acoperă maparea.
- Aceste probe locale nu anulează încălcarea whole-ATF și nu aprobă plasarea
  actuală a adaptorului.
- Reporting trebuie să rămână independent de Core/Console.
- Regula privind modificarea testelor și allowlist-urilor este deținută de
  [guvernanța ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).
- Acest feedback urmărește doar decizia arhitecturală și remedierea boundary-ului.

### Decizia rămasă

- Alegeți un owner de compoziție permis de regulile existente, fără dependență
  production `atf-console → atf-reporting` și fără dependență inversă
  `atf-reporting → atf-core`.
- Păstrați maparea semantică și izolarea erorilor de sink; proba de componentă
  trebuie să rămână la ownerul adaptorului ales.
- Închideți ID-ul numai când testul adaptorului și
  `WholeAtfGovernanceSuiteTest` trec împreună, fără relaxarea regulilor.
