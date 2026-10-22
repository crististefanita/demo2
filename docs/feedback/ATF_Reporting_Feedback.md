# ATF Reporting — reconciled feedback

## Stare

Tabelele, summary-urile și trace-ul UI/API sunt închise. `REP-UX-FLOW-01` rămâne
deschis. Worktree-ul are acum o implementare WIP pentru `UNIFIED_FLOW`, dar ea
creează un nod părinte și apoi păstrează `Checkpoint N — <Gherkin title>` pentru
fiecare checkpoint. Acesta grupează nodurile, nu le reduce la o singură
propoziție semantică. Modificarea nu are încă dovadă HTML/render fresh.

Regulile stabile sunt în [ATF Reporting Contract](../ATF_Reporting_Contract.md)
și [ATF Reporting README](../../atf-reporting/README.md). Cerința Practice Shop
este un consumator concret al acestui contract, nu motiv pentru un registry
generic de UI substeps.

## ToDo activ

| ID | Scor / prioritate | Gap verificat | Criteriu de închidere |
|---|---:|---|---|
| `REP-UX-FLOW-01` | **73 / P1** | WIP-ul `UNIFIED_FLOW` învelește checkpoint-urile, dar încă emite câte un `Checkpoint N — GIVEN/WHEN/THEN ...`; nu îndeplinește contractul de single proposition | test HTML arată exact un business/Gherkin root, fără children/details `Checkpoint N` sau Gherkin duplicat; UI actions sunt o singură evidență UI grupată, iar API evidence rămâne sibling când este selectată; `SCREEN_CHECKPOINTS` nu regresează; `FINAL_ONLY` publică numai imaginea finală |

`FINAL_ONLY` este deja policy generic de captură, iar contractul său este probat.
Gap-ul activ este semantica arborelui pentru `UNIFIED_FLOW`, nu enum-ul de
screenshot. Un singur root nu poate ascunde o listă de propoziții Gherkin
numerotate în children sau în metadata. Acțiunile Playwright sunt payload tehnic
al unei singure evidențe UI; nu devin Gherkin, nu controlează execuția și nu cer
un mecanism plug-and-play de substeps. Consumerul Practice Shop și wiring-ul
YAML sunt urmărite separat în [feedback-ul E2E](ATF_Practice_Shop_E2E_Feedback.md).

## Contracte închise

| ID | Contract stabil | Proof de regresie |
|---|---|---|
| `REP-UX-TABLE-03` | wrapper-ele de tabele au `height:auto` și plafon comun `TABLE_SCROLL_HEIGHT=500px`; conținutul scurt rămâne natural, iar cel lung derulează intern sub header sticky | `AtfReportEvidenceCssTest`, `AtfReportOverviewCssTest`, `CategoryCardinalityBrowserFixtureTest` și `ScrollableTableViewportBrowserProbe` |
| `REP-UX-SUMMARY-01` | summary-urile aggregate standard sunt `34px`; `Unique request signatures` și `Technical diagnostics` sunt `68px` și collapsed implicit | `AtfReportThemeTokensTest`, `AtfReportEvidenceCssTest`, `CategoryCardinalityBrowserFixtureTest` |
| `REP-COMP-API-01` | selecția exactă API ajunge până la publisher prin mock/lifecycle, iar selecția invalidă se oprește înainte de publicare | `ApiReportAuditSetupTest`, `ApiScenarioManifestTest`, `ApiSelectionMockPublisherComponentTest` |
| `REP-TRACE-03` | ownerul semantic deține UI validation și API validation ca evidence siblings: checkpoint-ul în `SCREEN_CHECKPOINTS`, unicul flow proposition în `UNIFIED_FLOW`; retry-ul aceluiași request rămâne un singur exchange logic | [`ExtentReportUiEvidencePublishingTest`](../../atf-reporting-tests/src/test/java/atf/api/reporting/ui/ExtentReportUiEvidencePublishingTest.java): `keepsUiAndApiValidationUnderOneGherkinCheckpoint`, `rendersExecutionEvidenceForEveryExchangeInOneUiApiTrace`, `keepsRetryHistoryInsideOneExchangeNode`; pentru unified flow mai trebuie proof HTML fără `Checkpoint N` |

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
