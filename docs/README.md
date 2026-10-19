# Documentația ATF Java

Acest director descrie contractele stabile ale ATF Java: arhitectura, modul de
adăugare a testelor API, raportarea Extent și modul sigur de lucru atunci când
există execuții active în alt checkout. Documentele sunt scrise pentru oameni
care întrețin framework-ul, nu pentru o singură rulare sau pentru un singur
artifact.

## Diagramă de bază

```mermaid
flowchart TD
    Docs[docs index] --> Architecture[architecture + module boundaries]
    Docs --> Guides[API/UI guides + contracts]
    Docs --> Safety[build and execution safety]
    Docs --> Practices[best practices]
    Docs --> Feedback[feedback register + dated audits]
```

## Ordinea recomandată de lectură

1. [ATF_Architecture.md](ATF_Architecture.md) — module, dependențe și responsabilități.
2. [../atf-core/README.md](../atf-core/README.md) — kernelul neutru, porturile
   și modelele comune.
3. [../atf-test-kit/README.md](../atf-test-kit/README.md) — contractul test-only
   pentru nivelurile unit/component/integration/e2e și guard-ul transversal.
4. [../atf-execution/README.md](../atf-execution/README.md) — API layer,
   servicii, composition root și lifecycle.
5. [../atf-validation/README.md](../atf-validation/README.md) — validarea
   independentă și portul de publicare.
6. [../atf-reporting/README.md](../atf-reporting/README.md) — rendererul,
   contractele și suita test-only/component/E2E pentru raportare.
7. [../atf-tests/README.md](../atf-tests/README.md) — guvernanța globală a
   modulelor ATF și testul de boundary al întregului reactor.
8. [../atf-console/README.md](../atf-console/README.md) — proiecția diagnostică
   de consolă și contractul de output.
9. [../atf-ui/README.md](../atf-ui/README.md) — politicile generice și adaptorul
   Playwright.
10. [../atf-reporting/src/test/java/atf/ui/tests/extent/README.md](../atf-reporting/src/test/java/atf/ui/tests/extent/README.md)
   — contractul stabil pentru selecția API, mock-backed expected projection,
   secțiuni Extent și validarea UI reutilizabilă.
11. [../atf-debug-ui/README.md](../atf-debug-ui/README.md) — publicarea
   read-only a unui HTML pentru investigație manuală în Playwright/TAB MCP.
12. [ATF_API_TEST_GUIDE.md](ATF_API_TEST_GUIDE.md) — pașii minimali pentru un test API nou.
13. [ATF_Reporting_Contract.md](ATF_Reporting_Contract.md) — arborele Extent,
   evidence-ul și regulile de randare.
14. [ATF_Reporting_Reference_Guard.md](ATF_Reporting_Reference_Guard.md) —
   reperul vizual/funcțional și procedura de comparație în TAB MCP.
15. [ATF_Build_And_Execution_Safety.md](ATF_Build_And_Execution_Safety.md) —
   build, sincronizare și validare fără oprirea execuțiilor existente.
16. [README_Atf_BestPractices.md](README_Atf_BestPractices.md) — reguli OOP,
   testabilitate și mentenanță.
17. [feedback/ATF_Test_Layering_Feedback.md](feedback/ATF_Test_Layering_Feedback.md)
   — delimitarea activă `unit → component → integration → e2e` și rolul
   infrastructurii `support` pentru API, UI și reporting.

## Hartă rapidă a responsabilităților

```mermaid
flowchart LR
    ReportingTests[atf-reporting test scope<br/>API/UI tests + E2E] --> Execution[atf-execution<br/>API + lifecycle]
    ReportingTests --> Mock[atf-mock<br/>fake determinist]
    ReportingTests --> Validation[atf-validation<br/>contract + verdict]
    ReportingTests --> Reporting[atf-reporting<br/>Extent + HTML]
    ReportingTests --> Ui[atf-ui<br/>Playwright adapter]
    ReportingTests --> DebugUi[atf-debug-ui<br/>read-only inspector]
    Execution --> Core[atf-core<br/>modele + porturi]
    Mock --> Core
    Validation --> Core
    Reporting --> Core
    Ui --> Core
```

Săgeata înseamnă „depinde de”. `atf-reporting` production publică artifactul,
iar test scope-ul aceluiași modul compune testele API, mock, contract/component
și E2E UI. `atf-tests` deține doar testele de guvernanță globală și nu este
composition root pentru API/UI; `atf-ui` controlează browserul, iar
`atf-debug-ui` doar servește read-only un HTML deja generat.
Pentru detalii și limitele exacte, `README.md` din root și
`ATF_Architecture.md` sunt sursele normative.

Pentru auditul UI Extent, sursa normativă de implementare este README-ul
pachetului `atf.ui.tests.extent` din `atf-reporting/src/test/java`:
`SectionAuditRegistry` leagă cele șase module,
`SectionAuditCheckExecutor` aplică policy-ul comun pe rezultate renderer-neutral,
iar `ExtentSectionAuditPublisher` publică separat Extent/JUnit. Feedback-ul
istoric nu mai este sursa contractului închis; el păstrează doar limite,
criterii de redeschidere și dovezi.

## Regula de lucru pentru orice schimbare

Înainte de refactorizare se sincronizează contextul live, apoi se lucrează
într-un staging unic și separat. Fiecare increment trebuie să treacă prin
testele relevante, să genereze un artifact nou și să fie verificat prin click-uri
în TAB MCP înainte de orice sincronizare selectivă înapoi. Procesele și rulările
existente nu se opresc, iar output-urile generate nu se copiază în surse.

Ordinea obligatorie și criteriile detaliate sunt în
[ATF_Build_And_Execution_Safety.md](ATF_Build_And_Execution_Safety.md) și
[README_Atf_BestPractices.md](README_Atf_BestPractices.md).

Lucrările încă neînchise sunt păstrate în [feedback/README.md](feedback/README.md)
și în documentele ToDo pe responsabilități. Analizele punctuale datate sunt
indexate în [feedback/ATF_Live_Analysis_2026-10-18.md](feedback/ATF_Live_Analysis_2026-10-18.md).
Deciziile închise sunt reflectate
direct în README-urile ownerilor și în teste; feedback-ul rămâne concentrat pe
riscurile și pașii care mai trebuie validați.

Regula de promovare este strictă: când un ToDo devine parte din soluție, contractul
stabil, ownership-ul și modul de folosire se mută în README-ul ownerului. Feedback-ul
păstrează doar limita rămasă, criteriul de închidere și dovada necesară; nu este
sursa normativă pentru o capabilitate deja livrată.

## Reguli de documentație

- README-urile descriu contracte, ownership și fluxuri reproductibile.
- Nu se introduc scoruri, timestamp-uri, nume de rapoarte sau stări intermediare.
- Nu se păstrează linkuri către artefacte HTML generate; se documentează doar
  tipul de output, locația configurabilă și criteriul de verificare.
- O diagramă trebuie să reflecte responsabilitățile și fluxurile existente;
  dacă acestea se schimbă, diagrama se actualizează împreună cu testele.
- Codul și raportul sunt sursa comportamentului; documentația nu poate inventa
  o capabilitate care nu este protejată de un test.
- Orice schimbare vizuală sau de navigare se verifică în artifactul HTML și,
  când este relevant, prin interacțiune în TAB MCP.

### Gate obligatoriu pentru raportul UI randat

Orice schimbare de clasă mare, CSS, markup, disclosure sau evidence urmează
aceeași probă, indiferent dacă raportul este produs dinamic sau auditat static:

```text
GENERATED_API: setup → API/mock → API report → ATF Debug/browser MCP
STATIC_*:      HTML existent → ATF Debug/browser MCP
                                 ↓
                         UI audit → UI report → ATF Debug/browser MCP
```

Se verifică DOM-ul, accessibility tree, computed-style, console/network,
expand/collapse, taburile și cel puțin un caz pozitiv și unul negativ. Pentru
`Screen validation`, acțiunile, observațiile și assertions rămân zone-soră fără
separator orizontal decorativ, iar `📷 base64 img` este link inline, cu aceeași
încadrare verticală pe rând. POST/PUT trebuie să arate request și response JSON
valid/nevid și să reconcilieze mock expectation-ul cu `ValidationResult`.

Build-ul se face în staging extern, sub `${ATF_STAGING_ROOT}`, nu în staging-ul
proiectului; raportul fresh se copiază în
`artifacts/reports/` sau `artifacts/reports/ui/` numai după verificarea prin
ATF Debug UI și browser MCP. Browserul Windows real și oprirea rulărilor curente
sunt interzise.

## Vocabular scurt

test = intenția de produs verificată  
request = schimbul HTTP și evidence-ul lui tehnic  
validation = fratele direct al request-ului, cu tabelul agregat de assertions  
phase = SETUP, TEST BODY sau TEARDOWN; fazele SETUP și TEARDOWN sunt opționale
scenario = testul complet, cu fazele și verdictul final  
artifact = raportul HTML autonom rezultat după finalizarea sesiunii

