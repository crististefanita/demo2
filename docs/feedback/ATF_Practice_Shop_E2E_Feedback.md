# Practice Shop E2E — cerință reconciliată

Cerința acoperă flow-uri E2E autentificate, configurabile prin adnotările
testului și rulabile individual din IntelliJ. Implementarea curentă o acoperă;
nu există un ToDo activ pentru această zonă.

## Dovadă în codul live

| Suprafață | Dovadă în cod și teste |
|---|---|
| `AuthenticatedPurchaseE2eTest` | Înregistrează un cont sintetic, confirmă autentificarea în `SETUP` și finalizează purchase-ul E2E. |
| `NewAccountPurchaseE2eTest` | Înregistrează un cont sintetic, confirmă autentificarea în `SETUP` și finalizează purchase-ul E2E. |
| Configurare și lansare IntelliJ | Ambele selectează `REGISTER_PER_RUN + SYNTHETIC` prin adnotările clasei; `PracticeShopAuthenticatedLaunchProfileTest` verifică profilul fără VM options. |
| Captură după fiecare tabel Playwright | `practiceShop.reporting.screenshotPolicy` este singura autoritate; implicit `each-checkpoint` capturează după fiecare tabel nevid din `SETUP` și `TEST BODY`, inclusiv în flow-urile autentificate și UI-only. `none` dezactivează capturile numai când YAML-ul o cere explicit. |
| Siguranța capturilor | `PracticeShopBrowserCaptureProvider` folosește masca Playwright pentru `input`, `textarea`, `select` și `[contenteditable]`; capture provider-ul rămâne generic, iar screenshot-urile nu expun valorile introduse în formulare. |
| Ordinea raportului | Testele din `atf-reporting-tests` verifică randarea screenshot-ului după tabelul de acțiuni; `PracticeShopBrowserCaptureProviderTest` verifică selectorul de mască și opțiunile Playwright, iar `PracticeShopScreenshotPolicyTest` verifică override-ul YAML și compatibilitatea cheii legacy. |
| Relația dintre flow-uri | Ambele au în prezent aceeași intenție și un traseu aproape identic. Suprapunerea nu este un gap de coverage; o eventuală diferențiere sau deduplicare este o decizie separată și trebuie să păstreze ambele flow-uri autentificate și rularea lor individuală. |

## Statut

Feedback-ul este închis. Captura implicită pe fiecare tabel Playwright este
acoperită de codul live și de teste de componentă; adnotările E2E nu mai pot
restrânge politica, iar YAML-ul rămâne punctul unic de configurare. Redeschide-l
numai dacă o captură permisă lipsește după un tabel de acțiuni, expune un câmp
mascat sau un flow autentificat nu mai poate fi rulat individual din IntelliJ.

## Contracte stabile și owneri normativi

| Contract | Documentație oficială |
|---|---|
| Profilurile locale/sintetice, preflight, redaction și interdicția de replay | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| `SETUP` înaintea corpului business și evidence-ul înregistrării/autentificării | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Captura `FINAL`, `Screen validation` și separarea auditului de acțiunile Playwright | [Practice Shop README](../../atf-test-practice-shop/README.md), [ATF Reporting Contract](../ATF_Reporting_Contract.md) |
| Structura `Setup evidence`, sibling UI/API evidence și capturarea terminală generică | [ATF Reporting Contract](../ATF_Reporting_Contract.md), [atf-reporting README](../../atf-reporting/README.md) |
| Dovezi API selectate pe checkpoint și journey summary terminal | [Practice Shop README](../../atf-test-practice-shop/README.md), [ATF Reporting Contract](../ATF_Reporting_Contract.md) |
