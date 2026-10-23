# Practice Shop API — acceptanța vizuală rămasă

## SHOP-API-RENDER-01 — audit interactiv al raportului standalone

**Stare: deschis numai pentru randarea interactivă.** Login-ul HTTP,
filtrarea/sortarea și mutațiile coșului cu read-back au fost probate live.
Contractul stabil și limitele probelor sunt în
[README-ul Shop](../../atf-test-practice-shop/README.md#boundary-ul-probelor-api).

**Prioritate:** 33/100 = impact 10 + lacună 5 + lucrabil 15 + documentare 3.

- [ ] Deschideți un artifact standalone proaspăt în TAB MCP/ATF Debug, servit pe
      loopback dacă accesul `file:` este blocat; nu folosiți browserul Windows.
- [ ] Comparați request/response/execution și contract validation cu modelul
      comun Reporting, inclusiv `AUTH PROFILE`/`AUTH TYPE`, body, retry și
      disclosure-urile implicit colapsate.
- [ ] Verificați interactiv scroll-ul local, glyph/ARIA, redeschiderea părinților
      și navigarea Fails → Tests, fără overflow orizontal sau detalii duplicate.

Inspecția conținutului HTML și succesul componentelor nu închid acest audit.
Nu este necesară repetarea mutațiilor remote pentru verificarea rendererului:
invarianta comună are fixture neutră la Reporting, iar consumerul confirmă
wiring-ul pe artifactul propriu.

Credentialele rămân în fixture-ul local, nu în documentație sau artifact.
Rularea HTTP rămâne opt-in; gate-ul component nu contactează aplicația externă.
