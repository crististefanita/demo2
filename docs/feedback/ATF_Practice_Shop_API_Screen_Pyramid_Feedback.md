# Practice Shop — gap-uri active pentru API, UI și evidence

## Overall

Scorul curent de maturitate al consumerului este **94/100**. Scorul măsoară
contractele și dovezile existente; prioritatea de intervenție este calculată
separat în [registrul central](README.md). Contractele stabile sunt în
[README-ul Practice Shop](../../atf-test-practice-shop/README.md); aici rămân
doar acțiunile încă deschise.

Practice Shop deține configurația, fixture-ul, pages/flows și purchase journey.
`atf-reporting` deține proiecția generică; `atf-reporting-tests` deține
compoziția librăriei. Nu mutați reguli de business în reporting.

## ToDo-uri active

| ID | Gap rămas | Dovadă de închidere |
|---|---|---|
| `SHOP-API-REAL-01` | Business flow-ul guest/authenticated a fost demonstrat; raportul fresh mai cere inspecție vizuală completă a screenshot-urilor și a exchange-urilor API extinse, inclusiv viewport îngust. | Run opt-in cu fixture valid; verifică screenshot-urile, exchange-urile canonical și lipsa clipping-ului la viewport normal/îngust prin ATF Debug/TAB MCP. Păstrează separat transportul live de probele mock. |
| `SHOP-REPORT-SYSTEM-03` | Filtrarea `UI journey precondition` este implementată și testată component; raportul authenticated deschis precedă fixul. | Raport fresh în Extent și fallback: precondiția apare numai în `Tests`, iar `System/Environment` păstrează doar configurația operațională relevantă. Se coordonează cu `REP-UX-SYSTEM-01`; aceeași randare nu se numără ca două gap-uri. |
| `SHOP-REPORT-BODY-01` | Preview-ul System folosește încă limita pentru JSON evidence; cele două politici trebuie separate. | Introdu o limită dedicată și configurabilă pentru preview (default `100` caractere); System arată preview-ul bounded, Request/Response păstrează JSON-ul sub limita sa proprie. Verifică redaction, truncare și Extent/fallback. |
| `SHOP-AUTH-09` | Statutul credentialului din istoricul Git nu poate fi dedus din eliminarea fișierului local; necesită confirmarea ownerului și, dacă a fost activ, rotație prin procesul aprobat. | Ownerul confirmă utilizarea și rotația fără a publica secretul; verifică separat istoricul Git. Păstrează loader-ul fail-closed și configurația externă. Nu declara credentialul rotit fără dovadă. |

`SHOP-REPORT-SYSTEM-03` și `SHOP-REPORT-BODY-01` sunt distincte: primul
verifică rolul și locul metadata-ului; al doilea cere o politică nouă de preview.
Schimbarea preview-ului aparține împreună consumerului și ownerului reporting,
nu unei setări reutilizate accidental.

## Protocol de închidere

- contractele de loader, metadata, fixture și mapping se dovedesc la component;
- integrarea externă se rulează numai cu opt-in explicit și fixture valid;
- browser proof-ul este necesar pentru randare, screenshot, ordine sau
  disclosure, folosind un artifact fresh;
- un rezultat mock nu este prezentat ca dovadă de business live;
- feedback-ul se scurtează după ce regula stabilă a fost promovată în
  [README-ul ownerului](../../atf-test-practice-shop/README.md).

Pentru forma generică a trace-ului UI/API și a inspectorului agregat, vezi
[ATF Reporting Contract](../ATF_Reporting_Contract.md) și
[ATF Testing Guide](../ATF_Testing_Guide.md). Pentru build și artifacte
izolate, vezi [Build and Execution Safety](../ATF_Build_And_Execution_Safety.md).
