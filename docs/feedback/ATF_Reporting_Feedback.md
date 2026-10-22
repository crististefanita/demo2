# ATF Reporting — gap-uri deschise

Nu există gap-uri deschise pentru `atf-reporting` sau `atf-reporting-tests`.
Contractele și responsabilitățile reporting sunt descrise în
[ATF Reporting Contract](../ATF_Reporting_Contract.md),
[README-ul atf-reporting](../../atf-reporting/README.md) și
[README-ul atf-reporting-tests](../../atf-reporting-tests/README.md).

Constatările despre folosirea adapterelor, redactarea body-urilor și organizarea
journal-ului Practice Shop sunt urmărite în
[feedback-ul ownerului Practice Shop](ATF_Practice_Shop_E2E_Feedback.md).
Ele nu redeschid automat `atf-reporting`: acel modul rămâne fără gap cât timp
contractele sale generice sunt respectate; dacă soluția aleasă cere schimbarea
API-ului comun, se adaugă aici numai criteriul generic afectat și testul care îl
dovedește.

Contractul generic al journey summary este închis și acoperit la nivel component:

- summary solicitat: exact unul, terminal, după checkpoint-uri și failure evidence;
- zero exchange-uri: summary-ul explicit este păstrat; UI-only îl omite prin necerere.

Detaliile și testul sunt în [README-ul atf-reporting](../../atf-reporting/README.md).

Redeschide feedback-ul numai când o schimbare în cod sau în testele owner
încalcă un contract stabil; adaugă doar criteriul generic rămas neacoperit.
