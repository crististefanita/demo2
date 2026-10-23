# ATF Test Infrastructure — integrarea rămasă pentru UI, API și UI+API

Adnotarea `AtfTestChannel`, enum-ul, planul imutabil și resolverul sunt
implementate în TestKit. Shop și Reporting Tests aplică selecția înaintea
resurselor; contractul livrat este în [documentația adnotărilor](../ATF_Test_Infrastructure_Annotations.md).

## Cerințe deschise

| Prioritate / ID | Gap confirmat | Owner | Dovada pentru închidere |
|---|---|---|---|
| P1 — KIND-02 | aliasul YAML `api` încă selectează tăcut `UI_API`; lipsește proba întregii precedențe YAML → canal → binding | Shop / config | diagnostic de migrare explicit; API-only distinct; canalul și selecția absentă versus `[]` păstrează sensul |
| P1 — KIND-03 | există probe pentru exchange fără contract, asociere lipsă și checkpoint necunoscut; lipsește compoziția lor cu selecția canalului și lista YAML explicit goală | consumator / Reporting | API fără contract rămâne observație; contract lipsă/necunoscut și selecție explicit goală sunt probate fără PASS inventat |
| P1 — KIND-04 | settlement-ul bounded și refuzul read-back-ului vechi au probe; rămâne completitudinea terminală per checkpoint UI+API | Shop / UI / Reporting | inspector terminal unic; apel obligatoriu lipsă, late, timeout, overflow și failure au diagnostice și verdict local corect |
| P1 — KIND-05 | paritatea Extent/fallback și verdictele locale pentru toate canalele | Reporting / consumator | UI PASS/API FAIL și invers, contract negativ așteptat, mai multe exchange-uri și copii Tests/Categories/Fails |
| P2 — KIND-06 | guard structural și matrice JUnit/lifecycle completă | TestKit / ATF Tests / consumator | individual/suită, parameterized/dynamic, disabled/aborted, failure, teardown și concurență; fiecare clasă relevantă consumă planul |

KIND-01 și documentarea API-ului implementat sunt promovate în documentul
normativ. Rezolvarea canalului și refuzul capabilităților lipsă nu închid
automat matricea de evidence, verdict și lifecycle de mai sus.

### Următorul increment, fără reluarea probelor existente

| Se reutilizează | Se mai adaugă |
|---|---|
| `ShopApiEvidenceBindingTest`: absent, selecție typed și checkpoint necunoscut | probă YAML reală absent versus `[]`, urmată de preflight-ul canalului; CLI gol nu trebuie confundat cu o listă goală explicită |
| `ShopApiCallAuditReportComponentTest`: două exchange-uri, contract negativ, fără contract și asociere neconsumată | aceeași matrice prin adaptoarele de canal, cu verdict local și scenariu distinct, inclusiv aborted |
| `ShopUiNetworkSettlementComponentTest`: read-back întârziat, timeout, cursor și fără replay | un singur diagnostic terminal pentru checkpoint obligatoriu lipsă, late/overflow/failure, cu owner identificabil |
| `ShopE2eTestSupportTest`: preflight, close o singură dată și failure primar păstrat la teardown | execuții reale JUnit parameterized/dynamic, disabled/aborted și concurență; testul helperului nu dovedește întreg lifecycle-ul JUnit |

Aceste probe existente reduc lucrul restant, dar nu închid automat KIND-02–06.
Nu se adaugă un dispatcher generic sau încă un mecanism de captură pentru a
completa matricea; se verifică adaptoarele și inspectorul existente.

## Cod existent și limitele probelor

| Boundary | Există în live | Ce nu este încă demonstrat complet |
|---|---|---|
| TestKit | UI/API/UI_API; direct, compus, superclass, override, echivalență, conflict, ciclu; interfețele/enclosing sunt excluse | un dispatcher generic nu există și nu este necesar pentru aceste contracte |
| Shop E2E | UI/UI_API; cele cinci clase migrate folosesc `@AtfTestChannel` ca selecție unică, fără `profile` redundant; refuză API-only și profil legacy contradictoriu înaintea browserului | aliasul YAML și toate combinațiile de selecție/configurație și lifecycle JUnit |
| Preflight UI+API | binding nevid, inspector activ și allowlist | completitudinea schimburilor și contractelor în timpul execuției |
| Shop API audit | cere API înaintea capturii; un preflight eșuat nu mai deschide raportul în `afterEach` (test dedicat); publică schimburile prin Reporting | `SuiteReport.record` mapează încă orice excepție JUnit la FAIL, inclusiv abort; disabled/aborted și verdictul metodei cer clasificare/probe distincte |
| Reporting UI | declară UI și refuză owner API-only; E2E local verifică raportul generat | verificarea unui raport API prin browser nu certifică API-only sau întreg UI+API |
| Tag-uri | adnotările de nivel produc tag-uri JUnit; canalul este selecție typed separată | tag-urile și categoriile raportului nu sunt populate automat de resolver |

## Criterii de integrare

| Canal | Dovezi așteptate | Criteriu restant |
|---|---|---|
| UI | acțiuni, observații, assertions și captură conform politicii | nicio request/response fictivă; selecția UI nu blochează traficul aplicației |
| API | request/response/execution și contract asociat exchange-ului | fără browser; metoda, body-ul, headers, timpul și proveniența rămân fidele |
| UI_API | aceeași sesiune UI, contracte API selectate și inspector terminal | ownership local pentru UI/API/corelare; fără reexecutarea schimburilor capturate |

- Layer-ul, canalul, transportul, setup-ul, validările, capturile și output-ul sunt axe distincte.
- Aliasul legacy `api` nu se remapează tăcut la API-only.
- Lipsa selecției și lista explicit goală trebuie diferențiate la migrare.
- Politica screenshot YAML rămâne autoritară; canalul nu activează capturi implicit.
- O schimbare de prezentare păstrează acțiunile și aserțiunile; UI+API poate adăuga propriile contracte, fără normalizarea artificială a profilurilor.
- Precondiția externă, aborted, disabled și teardown failure se clasifică explicit; absența unei excepții nu este singură dovadă de PASS.
- Permisiunea pentru registration, purchase sau alt efect extern rămâne la consumator.
- Refolosirea unui exchange capturat nu este un nou apel; retry-ul și apelurile distincte păstrează identitatea lor.

## Matricea probelor de completat

<details>
<summary><strong>Acceptanță funcțională, integrare și display</strong></summary>

| Nivel | Probe |
|---|---|
| Config / consumer | alias legacy, profil explicit contradictoriu, absent versus `[]`, selecție necunoscută; capabilități lipsă respinse înainte de browser/network/output (configurația locală poate fi citită mai întâi) |
| Lifecycle | prepare/execute/close, resurse închise o singură dată, excepții, individual/suită și concurență fără state global |
| Contract API | POST/PUT JSON bilateral, content-type, body absent/indisponibil, 204 explicit, expected failure și lipsă contract |
| Verdict | UI PASS/API FAIL și invers; ambele PASS/FAIL; INFO/WARNING/SKIP; două exchange-uri cu rezultate diferite |
| Network | subscribe/detach, scenariu/fază, cursor/settlement; zero exchange, mandatory missing, late, timeout, overflow și redactare |
| JUnit | parameterized/dynamic, disabled/aborted, failure primar și failure la teardown |
| Browser | runtime real: click/tastatură, glyph inline, JSON scroll, media inline/lightbox, viewport și default collapsed |
| Proiecții | Tests/Categories/Fails cu owner și verdict; Overview fără pași inventați; Extent/fallback pe aceeași intrare |
| Guvernanță | layer separat de canal, consumer care aplică planul, no-cycle/no-browser API, participarea matricei la suită |
| E2E minim | integrarea reprezentativă care nu poate fi demonstrată inferior; matricea variațiilor rămâne la componentă |

</details>

Verificarea randării folosește raport proaspăt din staging extern, inspectat
prin ATF Debug și MCP. Verdictul testului care verifică rendererul este distinct
de FAIL-ul contractual deliberat al raportului-țintă.

## Ownerii criteriilor comune

- [API-06](ATF_Reporting_Feedback.md#api-06--aserțiuni-și-verdict-local-înainte-de-agregare): assertions și verdicte locale.
- [API-04](ATF_Reporting_Feedback.md#api-04--intenție-identitate-și-cauzalitate): identitate, intenție și publicare cauzală.
- [GOV-API-01](ATF_Reporting_Feedback.md#gov-api-01--guvernanță-mai-puternică-pentru-formatul-api): schema structurală și matricea comună.
- [Feedback infrastructură](ATF_Test_Infrastructure_Annotations_Feedback.md): probe de mediu și output; [documentația adnotărilor](../ATF_Test_Infrastructure_Annotations.md): API-ul livrat.
- [Contractul Reporting](../ATF_Reporting_Contract.md), [display/layout](../ATF_Reporting_Display_Contract.md) și [captură](../ATF_Evidence_Capture_Contract.md): forma stabilă a dovezilor.

Modificarea testelor urmează [guvernanța ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).
Feedback-ul păstrează criteriile restante; controlul Git se urmărește separat.
