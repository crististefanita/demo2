# ATF Reporting — reconciled feedback

## Stare

Tabelele, summary-urile și trace-ul UI/API sunt închise. A rămas un gap în flow
mode: `UNIFIED_FLOW` este păstrat de profil, dar nu schimbă arborele Extent.
Regulile stabile sunt în [ATF Reporting Contract](../ATF_Reporting_Contract.md)
și [ATF Reporting README](../../atf-reporting/README.md).

## ToDo activ

| ID | Scor / prioritate | Gap verificat | Criteriu de închidere |
|---|---:|---|---|
| `REP-UX-FLOW-01` | **73 / P1** | `ReportEvidenceFlowMode.UNIFIED_FLOW` este metadata; `UiExtentReportSession` și lifecycle-ul nu colapsează checkpoint-urile sau acțiunile în raport | test HTML de integrare arată un singur nod Gherkin/business pentru flow-ul unificat, cu acțiunile nested; `SCREEN_CHECKPOINTS` rămâne neschimbat; `FINAL_ONLY` publică numai captura finală |

`FINAL_ONLY` este deja policy generic de captură, iar contractul său este probat.
Gap-ul activ este semantica arborelui pentru `UNIFIED_FLOW`, nu enum-ul de
screenshot. Consumerul Practice Shop și wiring-ul YAML sunt urmărite separat în
[feedback-ul E2E](ATF_Practice_Shop_E2E_Feedback.md).

## Contracte închise

| ID | Contract stabil | Proof de regresie |
|---|---|---|
| `REP-UX-TABLE-03` | wrapper-ele de tabele au `height:auto` și plafon comun `TABLE_SCROLL_HEIGHT=500px`; conținutul scurt rămâne natural, iar cel lung derulează intern sub header sticky | `AtfReportEvidenceCssTest`, `AtfReportOverviewCssTest`, `CategoryCardinalityBrowserFixtureTest` și `ScrollableTableViewportBrowserProbe` |
| `REP-UX-SUMMARY-01` | summary-urile aggregate standard sunt `34px`; `Unique request signatures` și `Technical diagnostics` sunt `68px` și collapsed implicit | `AtfReportThemeTokensTest`, `AtfReportEvidenceCssTest`, `CategoryCardinalityBrowserFixtureTest` |
| `REP-COMP-API-01` | selecția exactă API ajunge până la publisher prin mock/lifecycle, iar selecția invalidă se oprește înainte de publicare | `ApiReportAuditSetupTest`, `ApiScenarioManifestTest`, `ApiSelectionMockPublisherComponentTest` |
| `REP-TRACE-03` | un checkpoint Gherkin deține UI validation și API validation ca evidence siblings; retry-ul aceluiași request rămâne un singur exchange logic | [`ExtentReportUiEvidencePublishingTest`](../../atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidencePublishingTest.java): `keepsUiAndApiValidationUnderOneGherkinCheckpoint`, `rendersExecutionEvidenceForEveryExchangeInOneUiApiTrace`, `keepsRetryHistoryInsideOneExchangeNode`; consumer proof separat în `atf-test-practice-shop` |

Proba de viewport folosește artifacte UI și API separate, conținut scurt și
lung, viewport standard și `591px`; verifică overflow orizontal, max-height,
natural short height, header sticky și accesul la ultimul rând. Astfel, plafonul
de `500px` nu este doar o constantă afirmată în CSS: este măsurat în browser.

## Regula de redeschidere

- redeschide un ID numai dacă testul owner eșuează sau un artifact fresh arată
  regresia descrisă de contract;
- o verificare de source-only nu substituie geometria măsurată în browser;
- artifactele istorice și numărătorile de run nu sunt backlog și nu se copiază
  în feedback-ul curent;
- o schimbare viitoare de CSS sau markup cere testele unit/component relevante
  și proba browser a suprafeței afectate.

Ownership-ul fixture-urilor și al testelor rămâne în
[ATF Reporting Tests README](../../atf-reporting-tests/README.md); feedback-ul
nu dublează implementarea sau inventarul testelor.
