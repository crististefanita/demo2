# ATF Reporting — reconciliere feedback

Nu mai există cerințe de implementare deschise pentru domeniile de mai jos.
Contractele stabile au fost promovate în documentația oficială, iar probele sunt
ținute lângă componentele care le garantează. Acest fișier rămâne doar ca punct
de închidere și redeschidere la regresie; nu este un backlog paralel.

| Domeniu reconciliat | Rezultat verificat | Sursa normativă |
|---|---|---|
| Audit API per schimb | Fiecare schimb observat păstrează propriul request/response/execution. `Contract validation` apare numai pentru contract tipizat asociat; statusul așteptat este explicit, iar auditul de completitudine este publicat o singură dată la nivel de fază. | [Contractul de raportare](../ATF_Reporting_Contract.md), [README Practice Shop](../../atf-test-practice-shop/README.md) |
| Tabelul `TECHNICAL TRACE` | Markup-ul comun emite wrapperul `atf-trace-table-wrap`; tabelul scurt rămâne natural, cel lung derulează în tokenul comun, headerul rămâne vizibil, iar JSON-ul/disclosure-ul exterior rămân în afara wrapperului. Extent și fallback folosesc aceeași structură și politică CSS. | [README ATF Reporting](../../atf-reporting/README.md) |
| Probe și protecție la regresie | Testele de markup/CSS verifică structura și tokenul ambelor renderere; proba browser verifică API direct și trace API lângă checkpoint UI, la viewport standard/îngust, cu date scurte/lungi, overflow, header sticky și acces la ultimul rând. Testul component API verifică două exchange-uri cu statusuri așteptate diferite și o singură raportare a completitudinii. | [README Reporting Tests](../../atf-reporting-tests/README.md), `AtfReportMarkupTest`, `AtfReportEvidenceCssTest`, `CategoryCardinalityBrowserFixtureTest`, `ShopApiCallAuditReportComponentTest` |

## Redeschidere

Se redeschide numai dacă o schimbare ulterioară încalcă unul dintre contractele
de mai sus sau dacă apare o cerință nouă, distinctă. Un artifact vechi ori o
randare istorică nu este singură dovadă că implementarea live a regresat.
