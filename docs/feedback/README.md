# ATF Java — registrul cerințelor rămase

Acest registru indexează numai lacune deschise și prioritățile lor. Contractele
stabile și deciziile închise locuiesc în documentația oficială. Aici nu păstrăm
istoricul rapoartelor, capturilor sau rulărilor.

## Pe scurt

- Reporting păstrează explicația capturii lipsă la failure și oracolul Client
  rămas în urmă; formatul 24h este implementat și verificat la owner.
- Consumerii păstrează acceptanța reducerii profilelor live și auditul
  interactiv al raportului API Shop.
- Multipart/whitespace și Network failure/overflow au implementare și probe la
  owneri; rămân în documentația oficială, fără backlog funcțional duplicat.
- Cleanup-ul comun, clasificarea claselor vechi, profilele generice, auditul terminal
  și flow-ul Shop cross-phase sunt închise; contractele lor sunt la owneri.
- Diagrama și regulile selecției traficului sunt în
  [arhitectura proiectului](../ATF_Architecture.md#capturarea-și-selecția-traficului-ui)
  și [README-ul ATF UI](../../atf-ui/README.md#inspectorul-de-rețea--limită-generică-de-responsabilitate),
  nu într-un checklist de implementare deja rezolvat.

## Prioritatea curentă

Scorul însumează impactul (40), severitatea lacunei (30), posibilitatea de lucru
acum (20) și datoria documentară (10). Domeniile active au pondere `1.00`;
scorurile ordonează backlog-ul și nu reprezintă evaluări de calitate.

| Ordine | Owner / feedback | Impact /40 | Lacună /30 | Lucrabil /20 | Doc. /10 | Pondere | Scor | Rezumatul gap-ului |
|---:|---|---:|---:|---:|---:|---:|---:|---|
| 1 | [Reporting — owner UI pentru Network](ATF_Reporting_Feedback.md#report-browser-placement-02--proba-network-trebuie-plasată-la-ownerul-ui) | 36 | 27 | 20 | 8 | 1.00 | **91** | NetworkCaptureEvidenceTest trece funcțional, dar încalcă regula importurilor Playwright; mutare fără pierderea celor cinci cazuri și fără relaxarea guard-ului. |
| 2 | [Reporting — failure fără captură](ATF_Reporting_Feedback.md#ui-failure-capture-02--lipsa-capturii-trebuie-explicată-în-raport) | 34 | 24 | 20 | 6 | 1.00 | **84** | Regresie reprodusă de UiAuditLifecycleTest; motivul din evidence.policy este filtrat și lipsește din HTML, deși acțiunile și FAIL rămân. |
| 3 | [Reporting — oracol Client](ATF_Reporting_Feedback.md#report-client-expectation-02--oracol-html-rămas-în-urmă) | 21 | 15 | 20 | 5 | 1.00 | **61** | Aserțiunea HTML veche contrazice default-ul neutru corect; aliniere exactă și probă pentru Client explicit, fără schimbarea producției. |
| 4 | [Consumer — profile E2E](ATF_Consumer_Business_Test_Focus_Feedback.md#consumer-e2e-profiles-01--variantele-de-evidence-și-acoperirea-business) | 24 | 10 | 17 | 6 | 1.00 | **57** | Reducerea selecției Shop și acceptanța profilelor cer smoke local succes/failure, echivalență și exemple manuale păstrate. |
| 5 | [Shop API — auditul standalone](ATF_Practice_Shop_API_Test_Design_Feedback.md) | 10 | 5 | 15 | 3 | 1.00 | **33** | HTTP live este probat; randarea interactivă a artifactului consumerului nu este încă atestată. |

## Revizuit, dar neprioritizat pentru implementare

| Domeniu | Stare curentă |
|---|---|
| [AutoEx — compatibilitate business](ATF_Automation_Exercise_Feedback.md) | Integrarea Close opt-in la 70% este în verificare UI/UI_API; confirmarea ștergerii după reclamă rămâne distinctă. |
| [Geometrie browser / iframe](ATF_UI_Browser_Geometry_Feedback.md) | Pentru pointer la zoom CSS non-nativ rămâne o limită probată; activarea DOM explicită AutoEx nu o închide. |
| [Evaluarea AI](README_AI_Evaluation.md) | În afara acestei reconcilieri; conținutul și scorurile nu au fost reevaluate. |

`GOV-API-01`, profilele generice per scenariu și integrarea AutoEx final-only
nu mai sunt TODO-uri. Ele se redeschid numai dacă o probă nominală/regresie
arată că invariantul documentat nu mai este respectat.

Scorurile ordonează intervențiile rămase; ele nu înlocuiesc probele și nu
înseamnă că un gap de scor mai mic poate fi ignorat când afectează corectitudinea
evidence-ului publicat.

Regulile de promovare/ștergere a feedback-ului sunt deținute de
[bunele practici documentare](../README_Atf_DocumentationBestPractices.md#păstrarea-și-ștergerea-feedback-ului);
controalele `testChanges` de [baseline-ul lor](../ATF_Test_Changes_Baseline.md)
și [README-ul ATF Tests](../../atf-tests/README.md#guvernanța-schimbării-testelor).
Pentru execuții și verificarea artifactelor se aplică
[siguranța compilării și rulării ATF](../ATF_Build_And_Execution_Safety.md).
