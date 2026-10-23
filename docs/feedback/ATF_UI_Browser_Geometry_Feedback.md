# ATF UI — geometria browserului și iframe-uri

## UI-FRAME-POINTER-01 — coordonate corecte la zoom CSS non-nativ

**Stare: deschis.** Un click normal pe un control din iframe la zoom CSS 70%
poate fi respins cu `body/html intercepts pointer events`. Efectul a apărut
pe vignette-ul real AutoEx și se reproduce independent cu un control HTML
neutru în browserul de componentă `atf-ui`; nu este un mock al reclamei.

Inițializarea nu trebuie să aplice încă o dată zoom-ul în documentele copil.
Această separare previne compunerea scalei, dar nu rezolvă singură coordonatele
pointerului când documentul părinte rămâne la zoom CSS non-nativ. AutoEx
selectează explicit 100%; click-ul standard Close a fost probat pe reclama
reală. Această alegere nu certifică geometria pointerului la zoom non-nativ.

### Criterii rămase

- [ ] Alegeți și probați la owner o soluție pentru pointer și iframe la zoom
      non-nativ: compatibilitate driver/browser sau mecanism de scalare cu
      geometrie corectă; nu corecții de coordonate în page objects de produs.
- [ ] Verificați click real, focus, tastatură și captură pe documentul părinte
      și iframe-uri same-origin/cross-origin la limitele relevante ale scalei.
- [ ] Nu folosiți `force`, dispatch de evenimente sau modificarea DOM-ului
      site-ului pentru a declara un click trecut. 100% rămâne alegerea sigură
      pentru consumerii cu controale embedded până la demonstrarea soluției.

Regulile stabile sunt în [README ATF UI](../../atf-ui/README.md#configurație-generică-fără-flag-redundant).
Recuperarea contului AutoEx sub reclama observată rămâne o acceptanță separată
în [feedback-ul consumerului](ATF_Automation_Exercise_Feedback.md).

## Controlul incrementului de platformă

```yaml
testChanges:
  - id: UI-FRAME-ZOOM-01
    status: ACTIVE
    base: 0f6a95265024a78c20754d5c18937d24434a2d2d
    expires: 2026-11-23
    reason: Separa zoom-ul documentului principal de iframe si probeaza geometria pointerului la scara nativa in ownerul UI, fara reclama sau site artificial AutoEx.
    proof: mvn -pl atf-ui,atf-test-auto-ex,atf-tests -am "-Dtest=PlaywrightUiSessionTest,AutoExComponentSuite,AutoExBusinessSuite,WholeAtfGovernanceSuiteTest" "-Datf.autoex.e2e.enabled=true" "-Datf.governance.testChanges.baseRef=0f6a95265024a78c20754d5c18937d24434a2d2d" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-ui/src/test/java/atf/api/ui/playwright/PlaywrightUiSessionTest.java
```
