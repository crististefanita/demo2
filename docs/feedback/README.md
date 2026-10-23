# ATF Java — registrul cerințelor rămase

Registrul indexează numai gap-uri active. Contractele stabile aparțin README-ului
ownerului; rapoartele și rezultatele unei rulări nu devin documentație permanentă.

## Pe scurt

- Reporting: fidelitatea request-ului observat, ownerul API din Fails și lizibilitatea tabelelor foarte imbricate în panouri înguste.

- AutoEx: register/delete și login/delete cu setup API sunt probate la 70%;
  rămâne vignette-ul natural chiar la Delete account, nu corecția nominală.
  Activarea DOM Close nu repară geometria pointerului.
- Consumeri: echivalența selecției E2E reduse și auditul interactiv API Shop.
- Contractele livrate se găsesc în [indexul documentației](../README.md),
  nu în checklist-uri de implementare deja închise.

## Top feedback-uri active

Scorul este o prioritate de intervenție, nu o notă a codului sau a README-ului.
Formula comună este impact /40 + severitatea lacunei /30 + lucrabil acum /20 +
datorie documentară /10; toate domeniile operaționale au aceeași pondere.

Un feedback cu mai multe gap-uri primește scorul celui mai urgent, nu suma
lor. Gap-urile secundare rămân vizibile în documentul ownerului.

| Ordine | Feedback / owner | Scor /100 | Lacuna care decide ordinea |
|---:|---|---:|---|
| 1 | [Reporting](ATF_Reporting_Feedback.md) | **78** = 31 + 21 + 20 + 6 | fidelitatea headerelor/body-ului observat; separat, ownerul API în Fails (76) și lizibilitatea nesting-ului îngust (64) |
| 2 | [ATF UI — geometrie browser](ATF_UI_Browser_Geometry_Feedback.md) | **72** = 31 + 25 + 10 + 6 | pointer în iframe la zoom CSS non-nativ; activarea JavaScript explicită nu închide această limită |
| 3 | [AutoEx](ATF_Automation_Exercise_Feedback.md) | **65** = 36 + 18 + 10 + 1 | confirmarea și refuzul replay-ului sunt întărite; lipsește proba reclamei naturale chiar la Delete account la 70%, nu succesul din alte tranziții |
| 4 | [Consumer — profile E2E](ATF_Consumer_Business_Test_Focus_Feedback.md) | **57** = 24 + 10 + 17 + 6 | selecția live redusă cere smoke succes/failure și echivalență business, nu doar mapping-ul adnotării |
| 5 | [Documentație — ancore](#doc-link-anchor-01--validarea-automată-a-ancorelor) | **44** = 14 + 11 + 15 + 4 | guard-ul validează fișierul local, dar elimină fragmentul înaintea verificării; ancora lipsă nu este detectată |
| 6 | [Shop API — audit standalone](ATF_Practice_Shop_API_Test_Design_Feedback.md) | **33** = 10 + 5 + 15 + 3 | HTTP este probat; randarea interactivă a raportului consumerului trebuie încă atestată |

Ordinea nu înlocuiește criteriul de închidere. O componentă verde nu certifică
un flow remote, iar succesul live nu certifică toate politicile sau randarea.

## Design AI — evaluare separată

[Feedback-ul AI](README_AI_Evaluation.md) păstrează gap-urile evaluatorului și
ale workflow-ului propus. Scorurile sale măsoară readiness-ul, nu prioritatea
operațională de mai sus; nu sunt recalibrate printr-o revizie editorială.

## DOC-LINK-ANCHOR-01 — validarea automată a ancorelor

**Owner:** guvernanța documentației din `atf-tests`. Linkurile curente sunt
corectate, dar `DocumentationReadmeStructureGovernanceTest.inspectLocalLinks`
verifică numai calea dinaintea lui `#`; nu protejează heading-ul destinație.

- [ ] Adăugați verificarea fragmentelor Markdown, inclusiv link local, cross-file,
      Unicode, heading duplicat și ancoră HTML explicită; excludeți exemplele din fences.
- [ ] Probați destinația validă și ancora dispărută după promovarea feedback-ului,
      cu diagnostic fișier/linie/remediere; păstrați controalele de testChanges și
      rulați validatorul și entry point-ul documentației, fără relaxarea regulilor.

## Păstrare, promovare și verificare

- Un gap dispare numai după verificarea criteriului la owner; informația stabilă
  se mută în documentația proiectului, fără istoric de artifacte.
- Un feedback fără gap-uri se șterge numai după reconcilierea controalelor
  `testChanges`, a linkurilor și a țintelor `@DocumentationTarget`.
- README-urile de modul rămân documentație de utilizare chiar fără ToDo-uri.
  Controalele tehnice nu sunt cerințe funcționale și nu se închid editorial.

Protocolul este în [bunele practici documentare](../README_Atf_DocumentationBestPractices.md#păstrarea-și-ștergerea-feedback-ului).
Controalele verificate aparțin [baseline-ului testelor](../ATF_Test_Changes_Baseline.md);
execuțiile respectă [staging-ul și sync-ul sigur](../ATF_Build_And_Execution_Safety.md).
