# AutoEx — compatibilitatea business rămasă

Contractele stabile pentru vocabularul UI/API, validarea căutării și ownership-ul
fazelor sunt în [README-ul AutoEx](../../atf-test-auto-ex/README.md#vocabular-și-adrese).
Feedback-ul păstrează numai lacunele business încă neînchise.

## AUTOEX-VIGNETTE-70-01 — închiderea reclamei la 70%

**Stare: în verificare.** Integrarea folosește explicit
`clickWithJavaScript(..., postcondition)` numai pentru Close; profilul AutoEx
revine la 70%. Click-urile business obișnuite și gap-ul generic al pointerului
rămân separate de această activare DOM.

### De la experiment la integrarea opt-in

- `HTMLElement.click()` pe controlul Close vizibil a închis vignette-ul real
  din flow-ul coșului; fragmentul din URL a dispărut și adăugarea produsului
  a continuat normal. Cele trei povești business au trecut în aceeași probă.
- Raportul inspectat în MCP arată scala 70%, acțiunea `JavaScript click`,
  observația separată a închiderii și captura stării ulterioare `Added!`.
- Este activare DOM programatică, nu click de pointer trusted. Nu repară
  geometria și nu certifică închiderea reclamei în toate contextele checkout.
- API-ul integrat cere postcondiție și publică un singur rezultat după aceasta;
  nu există fallback JavaScript silențios și nici retry automat al activării.

### Gate-uri de închidere

- [ ] Probați API-ul opt-in `atf-ui`, cu înregistrare distinctă și fără
      schimbarea click-urilor business obișnuite.
- [ ] Probați la owner ținta unică/vizibilă/activă, propagarea erorii și
      postcondiția nereușită; AutoEx nu primește teste artificiale de reclamă.
- [ ] Verificați separat UI_API și apariția naturală a reclamei în checkout la
      70%, păstrând cleanup-ul și failure evidence dacă închiderea eșuează.

| Responsabilitate | Owner |
|---|---|
| Geometria pointerului la zoom non-nativ | [ATF UI — gap generic](ATF_UI_Browser_Geometry_Feedback.md) |
| Proba reclamei reale și păstrarea flow-ului business | `atf-test-auto-ex` |
| Cleanup și contractele business deja probate | [README AutoEx](../../atf-test-auto-ex/README.md#contul-creat-și-cleanup-ul-sub-vignette) |

### Acceptanță consumer

- [ ] Verificați cele trei povești din `AutoExBusinessSuite` și UI_API la 70%,
      fără reclamă simulată; nu declarați pointerul reparat pe baza unui click DOM.
- [ ] Inspectați raportul nou în MCP: scala efectivă, acțiunea JavaScript,
      starea ulterioară și failure evidence anterior recuperării, dacă e cazul.

Poveștile de coș, continuitatea după login, factura și recuperarea cleanup-ului
nu sunt ToDo-uri noi. Selecția și completitudinea traficului UI_API sunt
documentate la [boundary-ul cauzal AutoEx](../../atf-test-auto-ex/README.md#boundary-ul-cauzal-pentru-ui_api).

## AUTOEX-DELETE-AFTER-AD-01 — confirmarea ștergerii după reclamă

O rulare UI_API la 100% a trecut checkout-ul și factura, dar după Close la
ștergerea contului a ajuns pe Home. `accountDeletionResult()` a găsit două
headings `h2.title.text-center`, iar testul a rămas FAIL. Cleanup-ul de
recuperare a revenit la `/delete_account` și a confirmat ștergerea.

- [ ] Corelați rezultatul ștergerii cu ruta și confirmarea business după Close,
      fără `first()` care ar accepta un heading Home și fără mascarea erorii.
- [ ] Probați tranziția în componenta business AutoEx și pe site-ul real;
      păstrați failure evidence înainte de recuperare.

## Control tehnic temporar pentru integrarea opt-in

```yaml
testChanges:
  - id: UI-JAVASCRIPT-CLICK-01
    status: ACTIVE
    base: 5eb3fd3cb099fc67b49be3c17de564db63755db7
    expires: 2026-11-23
    reason: Integreaza activarea DOM explicita cu postconditie atomica si probe neutre la ownerul UI; inlocuieste fallback-ul AutoEx 100 cu profilul 70 fara schimbarea click-urilor business.
    proof: mvn "-pl=atf-test-auto-ex,atf-tests" -am "-Dtest=PlaywrightJavaScriptClickComponentTest,PlaywrightActionSequenceTest,PlaywrightUiSessionTest,AutoExComponentSuite,AutoExBusinessSuite,AutoExPurchaseUiApiE2eTest,WholeAtfGovernanceSuiteTest" "-Datf.autoex.e2e.enabled=true" "-Datf.governance.testChanges.baseRef=5eb3fd3cb099fc67b49be3c17de564db63755db7" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-ui/src/test/java/atf/api/ui/playwright/PlaywrightJavaScriptClickComponentTest.java
      - atf-ui/src/test/resources/atf/ui/playwright/javascript-click.html
      - atf-test-auto-ex/src/test/resources/atf-auto-ex.yaml
```
