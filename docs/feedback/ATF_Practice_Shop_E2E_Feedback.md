# Practice Shop — suite și gap-uri de raportare

Suitele `ShopE2eSuite`, `ShopApiComponentSuite`, `ShopApiCallAuditSuite` și
`ShopComponentSuite` sunt implementate și au owneri separați. Rămâne deschisă
precedența politicii screenshot pentru rularea agregată și forma semantică a
raportului API-call audit. Contractul de utilizare și comenzile canonice sunt în
[README-ul Practice Shop](../../atf-test-practice-shop/README.md).

## Contractul suitei E2E

Ordinea explicită păstrează contul sintetic disponibil flow-ului autentificat,
fără persistența credentialelor pe disc:

1. `NewAccountPurchaseE2eTest` — înregistrează contul și finalizează purchase-ul.
2. `AuthenticatedPurchaseE2eTest` — reutilizează contul creat în run și
   finalizează purchase-ul autentificat.
3. `PurchaseE2eTest` — flow UI + API.
4. `UiOnlyPurchaseE2eTest` — flow UI-only.
5. `UiOnlySingleFlowPurchaseE2eTest` — un singur flow UI, fără sub-propoziții.

`ShopE2eSuite` emite un singur raport Extent. În codul actual, suite mode
înlocuiește politica YAML cu `atf.practice.shop.e2e.suite.screenshot-policy`,
care implicit devine `final-and-failure`. Această precedență nu respectă cerința
ca politica YAML să decidă capturile: când YAML declară `each-checkpoint`, suita
nu trebuie să reducă implicit capturile la final.

**Gap activ:** YAML-ul `atf-practice-shop.yaml` declară `each-checkpoint`, dar
raportul din 2026-10-22 17:07 arată `Final screenshot + failures`, checkpoint-uri
cu `screenshot=not captured` și numai capturi de flow terminale. Politica YAML
trebuie să fie autoritară în `ShopE2eSuite`; absența proprietății de sistem nu
este o interdicție YAML și nu trebuie să o suprascrie. Politicile YAML care cer
capturi mai rare sau `none` rămân respectate.

Pentru API-call audit, formatul și semantica sunt urmărite separat în
[ATF Reporting Feedback](ATF_Reporting_Feedback.md): auditul exchange-urilor nu
este echivalent cu `Contract validation`, iar rezultatul JUnit nu trebuie
prezentat ca request API.

## Dovadă de verificare

| Verificare | Rezultat |
|---|---|
| Contractul de detectare automată a suitei și profilele E2E | 20 teste, fără failure/error/skip |
| `ShopE2eSuite`, lansată fără `atf.practice.shop.e2e.suite=true` | toate cele 5 journey-uri live PASS; 25 teste în total cu testele contractuale, fără failure/error/skip |
| Raport cu override explicit `each-checkpoint` | [Raportul suitei E2E](../../artifacts/reports/ui/ui-atf-practice-shop-e2e_2026-10-22_17-02-39-818.html) — dovedește că publisher-ul poate atașa capturi la tabelele Playwright când proprietatea suitei cere explicit acest mod; nu dovedește că valoarea YAML este respectată |
| Precedență screenshot în rularea suitei | [Raportul ShopE2eSuite](../../artifacts/reports/ui/ui-atf-practice-shop-e2e_2026-10-22_17-07-57-420.html) — YAML-ul cere `each-checkpoint`, dar raportul declară `Final screenshot + failures`; checkpoint-urile nu au captură și există numai capturi terminale |
| `ShopApiComponentSuite` | 51 teste API component, fără failure/error/skip; nu execută journey-uri UI live |
| `ShopApiCallAuditSuite` | 13 teste din patru clase request-executing, fără failure/error/skip; câte un scenariu raportat pentru fiecare metodă JUnit și cel puțin un exchange capturat per scenariu |
| Raport API-call Extent | [Raportul API-call audit](../../artifacts/reports/api/api-atf-practice-shop-api-call-audit_2026-10-22T14-11-39.693582700Z.html) — include 13 `Test method outcome`, dar niciun `Contract validation`; analiza semantică este deschisă în [ATF Reporting Feedback](ATF_Reporting_Feedback.md) |
| `ShopComponentSuite` | 149 teste, fără failure/error/skip |
| Prefix de tip `Shop` pentru întreg modulul | `ShopArchitectureTest` adaugă verificarea fișierelor și declarațiilor Java din `main`/`test`; 11 teste arhitecturale trecute, scanare globală fără referințe Java `PracticeShop*` |

Comenzile reproductibile din root-ul repository-ului:

```text
mvn -pl atf-test-practice-shop -am "-Dtest=ShopE2eTestSupportTest,ShopConfigurationTest,ShopAuthenticatedLaunchProfileTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

```text
$env:MAVEN_OPTS='-Xms256m -Xmx2g -XX:MaxMetaspaceSize=512m'
mvn -pl atf-test-practice-shop -am "-Dtest=ShopE2eTestSupportTest,ShopAuthenticatedLaunchProfileTest,ShopE2eSuite" "-Datf.practice.shop.e2e.suite.screenshot-policy=each-checkpoint" "-Datf.practice.product-name=Pliers" "-DforkCount=0" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

```text
mvn -pl atf-test-practice-shop -am "-Dtest=ShopComponentSuite" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

```text
mvn -pl atf-test-practice-shop -am "-Dtest=ShopApiComponentSuite" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

```text
mvn -pl atf-test-practice-shop -am "-Dtest=ShopApiCallAuditSuite" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

La ultima rulare live, `Combination Pliers` nu era disponibil; `Pliers` era
disponibil și a fost selectat prin override pentru validarea suitei. Testul
continuă să eșueze dacă produsul selectat nu este disponibil; nu ocolește starea
observată a aplicației.

Randarea raportului suitei cu override explicit a fost verificată în MCP: fiecare journey are un
`Screen validation` cu screenshot-ul full-screen încorporat ca PNG Base64 și
accesibil sub evidența terminală a scenariului. În renderer-ul actual fotografia
este expusă ca link `base64 img` în disclosure, nu ca thumbnail inline. Pentru
rularea de audit `each-checkpoint`, MCP a confirmat un singur HTML cu cinci
scenarii, 77 de secvențe de acțiuni — fiecare cu screenshot PNG valid încorporat
— și cinci capturi terminale suplimentare; nu există linkuri de imagine invalide.
Acea verificare confirmă numai comportamentul cu override explicit. Raportul
17:07 confirmă gap-ul de precedență descris mai sus; politica implicită curentă
`final-and-failure` nu este criteriul cerut când YAML-ul solicită `each-checkpoint`.
Tabelele Playwright rămân expandabile și ordonate în arborele de dovezi.

## Criterii de menținere

- **Convenție de numire:** toate clasele și fișierele Java din modulul Practice
  Shop folosesc prefixul `Shop`, nu `PracticeShop`. Pachetul lowercase
  `practiceshop` rămâne neschimbat: el identifică domeniul fără a repeta-l în
  fiecare tip. `ShopArchitectureTest` verifică automat numele fișierelor și
  declarațiile de tip atât în `main`, cât și în `test`.
- `ShopApiComponentSuite` și `ShopApiCallAuditSuite` sunt lane-uri distincte:
  prima verifică tot contractul component API, a doua publică exchange-urile
  doar pentru testele explicit marcate `api-call`; nu le combina cu E2E/UI.
- Păstrează ordinea de registration → authenticated reuse înaintea celorlalte
  trei journey-uri.
- Menține izolarea credentialelor în cache run-scoped, în memorie, și curățarea
  cache-ului la teardown.
- Rulează suita E2E înaintea suitei de componentă atunci când se validează
  schimbări ale contractului end-to-end; component suite nu generează raport E2E
  și nu execută acțiuni live asupra magazinului.
- Nu considera rapoarte mai vechi sau rezultate Surefire stale drept dovadă
  pentru codul curent.

## Gap-uri active

1. Respectarea `reporting.screenshotPolicy` din YAML în `ShopE2eSuite`; eliminați
   fallback-ul implicit care o suprascrie. Închiderea cere probe pentru YAML
   `each-checkpoint`, politici de captură finală și `none`, plus verificarea
   raportului rezultat.
2. Alinierea semantică a API-call audit report și a testului consumer — vezi
   [ATF Reporting Feedback](ATF_Reporting_Feedback.md). Raportul nu trebuie să
   inventeze request-uri `ASSERT` sau contract validations care nu au fost
   evaluate.

Restul suitei și separarea API component/API-call audit rămân acoperite de
comenzile și probele existente; nu se redeschid fără regresie concretă.
