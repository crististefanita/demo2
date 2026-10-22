# ATF Reporting — feedback activ

Contractele stabile și probele generale rămân în documentele owner de mai jos.
Rămâne însă un gap de format pentru rapoartele API-call audit: rezultatul metodei
JUnit este afișat ca request sintetic, iar dovada nu distinge suficient rezultatul
testului de contractul unui schimb HTTP.

## ToDo: model semantic comun pentru API audit și contract validation

**Dovadă observată:** raportul
[API call audit din 2026-10-22 14:11](../../artifacts/reports/api/api-atf-practice-shop-api-call-audit_2026-10-22T14-11-39.693582700Z.html)
conține 13 noduri `JUnit result` / `Test method outcome` și niciun `Contract
validation`. Schimburile reale sunt publicate informativ; extensia adaugă separat
un request cu metoda `ASSERT` și ruta numelui clasei, apoi o validare generică
pentru „metoda a trecut” și „s-a capturat cel puțin un exchange”. Asta nu arată
asserțiunea reală a testului și produce o ierarhie diferită de un API contract
report.

Forma dorită păstrează tipurile vizibile și nu amestecă verdictul testului cu un
schimb HTTP:

```text
JUnit scenario (statusul metodei)
└── TEST BODY
    ├── Exchange observat (metodă + rută reale)
    │   ├── Request / Response / Execution evidence
    │   └── Contract validation (numai dacă acel response a fost validat)
    └── Audit completeness (numai dacă trebuie raportat separat)
```

`Audit completeness` nu este contract API; nu trebuie să devină un request
`ASSERT` și nici să dubleze verdictul final al metodei.

Codul are două responsabilități distincte, care trebuie păstrate explicite:

- `PracticeShopApiReportPublisher.publishObserved(...)` surprinde exchange-ul,
  fără să inventeze un contract HTTP generic;
- `ShopApiCallAuditTestSupport` agregă rezultatul JUnit și auditul exchange-urilor,
  dar îl modelează momentan drept `ASSERT` request. Detaliile sunt în
  [feedback-ul Practice Shop](ATF_Practice_Shop_E2E_Feedback.md).

### Criteriul de acceptare

- Un request din arbore reprezintă numai un schimb API real. Pentru un contract
  de răspuns efectiv evaluat, `Contract validation` este copilul request-ului și
  conține aserțiunile `expected / actual / result`.
- Verdictul metodei JUnit aparține scenariului/testului; nu se reprezintă printr-un
  request artificial `ASSERT` și nu se redenumește `Test method outcome` ca și cum
  ar fi un API contract.
- Un API-call audit fără contract typed rămâne audit de exchange-uri. Nu se
  fabrică `Contract validation`; metoda JUnit și completitudinea capturii se
  raportează distinct, fără ca lipsa unui exchange să rescrie verdictul metodei.
- Proveniența transportului fizic și proveniența rezultatului publicat de ATF sunt
  etichetate separat când diferă (de exemplu, transport mock într-un test al
  mapării `ApiTransportMode.LIVE`). Raportul nu trebuie să sugereze trafic live
  când request-ul a fost executat de fixture.
- Ierarhia și stilul sunt comune cu restul raportului Extent; nu se rezolvă prin
  CSS special pentru `Test method outcome`.

### Probe necesare

- În `atf-reporting-tests`, teste de componentă pentru arborele tipizat și HTML:
  request real → evidence disclosures → validare nested numai dacă există
  contract; verdict de metodă fără request sintetic; statusuri PASS/FAIL și
  păstrarea mesajului de eșec; fără schimb HTTP; mai multe exchange-uri și retry;
  sursa transportului separată de proveniența rezultatului ATF; aceeași structură
  semantică trebuie verificată atât pentru Extent, cât și pentru fallback.
- În `atf-test-practice-shop`, test de integrare al extensiei suitei care
  verifică output-ul auditului; testele rendererului generic nu pot proteja de
  o compoziție greșită făcută de consumer. Testul existent
  `HtmlReportSessionTest.rendersApprovedStructureWithValidationAsRequestSibling`
  afirmă nested în aserțiuni, dar numele spune sibling; corectează numele odată
  cu extinderea protecției pentru auditul consumerului, ca terminologia să nu
  perpetueze ierarhia ambiguă.
- Verificarea DOM trebuie să afirme relațiile părinte–copil și numărul exact de
  noduri, nu doar prezența unor texte sau clase CSS.

Nu se schimbă `atf-reporting` ca să depindă de JUnit. Dacă lipsește un seam
renderer-neutral pentru verdict/asserțiuni de nivel test, acesta trebuie definit
în modelul de reporting și consumat de adaptoarele de test.

## Owneri normativi

- [ATF Reporting Contract](../ATF_Reporting_Contract.md) — semantica și
  ierarhia publicată.
- [README-ul atf-reporting](../../atf-reporting/README.md) — proiecția, markup-ul,
  summary-ul și Network Inspector.
- [README-ul atf-reporting-tests](../../atf-reporting-tests/README.md) — matricea
  de coverage și probele componentelor rendererului.
