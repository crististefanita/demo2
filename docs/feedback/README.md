# ATF Java — feedback activ

Acest director conține numai lucruri care mai cer implementare sau dovadă
suplimentară. Contractele deja stabilizate apar în docs/ și în README-urile
modulelor; nu sunt duplicate aici.

## Situație live — 2026-10-16

| Status | Identificatori | Rezultat |
|---|---|---|
| DONE | F-11, UI-14, UI-16 | acoperire dinamică, naming-ul payload-ului Base64 și parserii semantici există și sunt testați |
| PARTIAL | F-03, F-05, F-07, F-10, F-13, UI-15, D-01, D-03 | fundație implementată; mai există gap-uri delimitate |
| OPEN | F-01, F-02, F-06, F-09, F-12, D-04 | refactorizare sau contract de regresie necesar |

Nu se păstrează aici ToDo-uri închise sau istoricul rulărilor. Dovezile de
execuție rămân în artifactele generate și în testele versionate.

Ratchet nou în codul live: `PlaywrightActionSequence` respinge acum target-urile
goale pentru operațiile element-bound, iar `PlaywrightActionSequenceTest` este
verde (`2` teste, `0` failures/errors). Acest ratchet nu închide încă LOC-01 și
LOC-02: target-urile prose ne-goale sunt încă acceptate, iar migrarea lor la
selectorii CSS efectivi trebuie făcută explicit și reconfirmată printr-un
artifact UI nou.

## Ce a fost rezolvat în această revizie

- ExistingExtentReportExploratoryTest expandează fiecare owner, scenariu și
  fază descoperită din DOM; nu mai verifică doar primul scenariu.
- Fails verifică două contracte reale: pentru failures există cauză și
  Open in Tests pentru fiecare rând; fără failures există empty-state și zero
  linkuri.
- Categories acceptă explicit zero, unul sau mai multe rezultate; navigarea
  este obligatorie numai când există un rezultat.
- Evidence.hasScreenshotPayload() este API-ul semantic; aliasul istoric
  hasInlineImage() rămâne doar pentru compatibilitate.
- ReportAuditChecks separă count-urile de durate și testează 0, ms, s,
  separator zecimal, valoare absentă și text invalid.

## Gap-uri active

### P0 — dovezi și lifecycle

| ID | Owner | Gap | Criteriu de închidere |
|---|---|---|---|
| F-09 | atf-ui / atf-tests | failure boundary-ul este repetat în clasele exploratorii | test negativ care aruncă înainte de screenValidation, păstrează acțiunile și publică artifactul cu Fail |
| F-12 | atf-ui / atf-reporting | observațiile Tab visible și Screenshot sunt în aceeași listă cu acțiunile Playwright | contract explicit ACTION/OBSERVE în model și tabel |

### P1 — decuplare și configurare

| ID | Owner | Gap | Criteriu de închidere |
|---|---|---|---|
| F-01 | atf-reporting | orchestration-ul Extent, runtime-ul și theme-ul au hotspot-uri mari | fațade mici și componente cu responsabilitate unică |
| F-02 | atf-reporting | Extent și fallback nu consumă încă un snapshot semantic immutable unic | aceeași fixture produce aceeași populație semantică în ambele proiecții |
| F-03 | composition root | precedence și root-scoping trebuie demonstrate și pentru rulări paralele | test pentru surse concurente, căi relative și snapshot immutable |
| F-10 | atf-tests | clasele exploratorii dețin prea mult navigator, parsing și publication lifecycle | validatorii DOM și lifecycle-ul sunt suport comun |
| F-13 | atf-ui | output naming-ul UI nu este complet derivat din configurație | report.filePrefix controlează efectiv output-ul |
| UI-15 | atf-tests | ramura zero există, dar nu are fixture dedicat în matricea live | test data-driven pentru zero/one/many și empty-state |
| LOC-01 | atf-ui | blank/null sunt respinse și selectorii sunt normalizați; validarea semantică a prose-ului rămâne responsabilitatea producerului | păstrează contractul la intrare și nu lăsa rendererul să ghicească locatori |
| LOC-02 | atf-tests | suite-ul explorator live publică selectorii CSS pentru `ACTION`; observațiile sunt separate `OBSERVE` | ratchet pentru orice test nou; nu promova `Screenshot/current page` în `ACTION` |
| LOC-03 | atf-ui / atf-tests | wait-urile fixe trebuie ancorate în locator și apoi înlocuite cu state wait | zero `TARGET=page`, apoi eliminarea delay-urilor directe |

### P2 — contract vizual și durate

| ID | Owner | Gap | Criteriu de închidere |
|---|---|---|---|
| F-05 | atf-tests | computed-style și filtrele trebuie verificate pe toate suprafețele | test UI pe Overview, Tests, Category și Fails |
| F-06 | architecture tests | hotspot-urile și limita de coeziune trebuie urmărite global | gate pe importuri, static state, inventory și allow-list |
| F-07 | reporting metadata | inventarul trebuie să includă package-urile de producție din atf-tests | overview cu package real și test de reconciliere |
| D-01 | atf-reporting | duration marker și sursa nu sunt demonstrate pe toate proiecțiile | contract DOM pentru toate nodurile și taburile |
| D-03 | atf-reporting | durata absentă nu are aceeași reprezentare peste tot | — stabil pe frunze, request-uri și sumar |
| D-04 | atf-reporting | există tokenuri, dar încă există override-uri concurente | un singur token geometric și computed-style verificat |

## Proces obligatoriu

1. Citește codul live și README-ul ownerului.
2. Creează staging separat; nu edita checkout-ul folosit de altă rulare.
3. Scrie întâi testul de regresie care trebuie să eșueze pe comportamentul
   invalid.
4. Implementează schimbarea minimă în modulul owner.
5. Rulează testele unitare și integrarea relevantă.
6. Dacă schimbarea este observabilă, generează artifact nou.
7. Deschide exact artifactul nou în TAB MCP și verifică prin click-uri reale,
   expand/collapse, navigare și computed styles.
8. Numai după verificarea randării se actualizează statusul ToDo-ului.

Build-ul într-o altă locație nu schimbă regula: înainte de build se face sync
cu codul live, iar după build se verifică faptul că artifactul provine din
sursele sincronizate. Nu se închid ToDo-uri prin relaxarea testelor sau prin
schimbarea mesajului de assertion.

## Dovezi curente

Input API generat dinamic:

    artifacts/reports/ui/ui-atf-extent-report_2026-10-16_08-33-44-049.html

Audit read-only verificat prin Playwright/TAB MCP:

    artifacts/reports/ui/existing-ui-atf-extent-report_2026-10-16_08-39-07-688.html
    artifacts/reports/ui/existing-ui-categories-atf-extent-report_2026-10-16_08-38-48-130.html

Rularea a trecut auditul Existing 4/4 și Categories 1/1, fără failure sau
error. Raportul nou a fost deschis în TAB MCP și verificat prin expandarea
Tests, scenario, faze, evidence și filtrarea Categories.

## Surse normative

- docs/ATF_Architecture.md
- docs/ATF_Reporting_Contract.md
- docs/ATF_BUILD_AND_EXECUTION_SAFETY.md
- docs/README_Atf_BestPractices.md
- docs/feedback/ATF_UI_Exploratory_Feedback.md
- docs/feedback/ATF_Java_Improvement_Backlog.md
