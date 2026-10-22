# ATF Reporting — feedback activ

Acest document conține numai golurile deschise față de contractul comun al
raportului. Structura dorită este definită în
[contractul Extent](../ATF_Reporting_Contract.md), iar regulile de implementare
și verificare sunt în [README-ul `atf-reporting`](../../atf-reporting/README.md)
și [README-ul `atf-reporting-tests`](../../atf-reporting-tests/README.md).
Feedback-ul descrie comportament și criterii care rămân valabile între rulări;
nu păstrează nume de rapoarte, ore, contoare sau rezultate volatile.

## 1. Model semantic pentru audit API și validarea contractului

### Situația din cod

Publisherul consumerului afișează schimbul HTTP real cu disclosure-ul
`Contract validation` imbricat; raportul nu mai creează pseudo-request `ASSERT`.
Tabelul include verificări de structură pentru JSON/Content-Type, dar amestecă
acestea cu verdictul JUnit și numărul agregat de schimburi. Rândul `HTTP status`
arată `asserted by the component test` ca valoare așteptată, însă rezultatul lui
este determinat de verdictul general JUnit, nu de comparația cu statusul HTTP
cerut pentru ruta respectivă. Testul component confirmă layout-ul copil, dar
enumeră aceste date JUnit/audit în aceeași validare.

Rămâne deschis contractul tipizat per exchange: consumerul trebuie să transmită
așteptările HTTP și aserțiunile specifice răspunsului curent. Pentru metodele cu
mai multe schimburi, verdictul JUnit și numărul total de schimburi nu trebuie
repetate în fiecare request; completitudinea se publică o singură dată la nivelul
auditului.

### Forma semantică cerută

```text
Scenariu JUnit (verdictul metodei)
└── CORPUL TESTULUI
    ├── Schimb HTTP observat (metodă și rută reale)
    │   ├── Detalii cerere
    │   ├── Detalii răspuns
    │   ├── Dovezi de execuție
    │   └── Validarea contractului (numai când schimbul are așteptări tipizate)
    └── Completitudinea capturii
```

Verdictul final aparține metodei/scenariului JUnit și nu este o aserțiune a
fiecărui nod HTTP. Așteptările HTTP apar în validarea copil numai când testul le
furnizează explicit. Completitudinea capturii este un audit, nu o cerere
artificială și nu se multiplică pe fiecare schimb.

### Condiții de acceptare

1. Nicio cerere sau aserțiune per exchange nu este fabricată pentru verdictul
   JUnit; schimburile rămân exclusiv cele capturate din transport.
2. Verdictul final PASS/FAIL și mesajul eșecului rămân legate de metoda/scenariul
   JUnit; eșecul auditului nu rescrie retroactiv rezultatul metodei.
3. `Contract validation` apare numai dacă răspunsul a fost evaluat contra unui
   contract tipizat. Fiecare tabel `expected / actual / result` folosește
   așteptarea explicită pentru acel exchange; verdictul JUnit sau statusul
   observat nu pot substitui valoarea așteptată.
4. Mai multe schimburi și reîncercările păstrează identitatea și ordinea lor.
   Reîncercarea aceleiași cereri este istoric de execuție, nu un schimb nou dacă
   modelul de transport nu a executat o cerere distinctă.
5. Proveniența execuției fizice este separată de eticheta/proiecția publicată de
   ATF. Un fixture sau transport simulat nu poate fi prezentat ca trafic real.
6. Extent și motorul alternativ publică aceeași ierarhie semantică, aceleași statusuri și
   aceleași detalii relevante, fără CSS special pentru rezultatul metodei.

### Probe necesare

- În `atf-reporting-tests`, teste de componentă și structură HTML verifică
  schimb HTTP → detalii cerere/răspuns/execuție → validare imbricată numai
  dacă există contract; separat verifică verdictul metodei, eșecul, absența
  schimburilor, mai multe schimburi și retry.
- Aceleași relații părinte–copil și număr de noduri se verifică pentru Extent și
  motorul alternativ; verificările nu se limitează la text ori clase CSS.
- În Practice Shop, testul de componentă al consumerului trebuie să confirme că
  fiecare Request corespunde unui schimb capturat, că nu conține un verdict JUnit
  reutilizat ca validare și că snapshot-ul are zero noduri Validation în lipsa
  unui contract tipizat. Înainte de a considera testul drept dovadă, aliniază
  aritatea apelului `publishObserved(...)` între caller și publisher.
- Când consumerul furnizează contracte HTTP, verifică separat răspunsuri pozitive
  și negative prin `expected / actual / result`, imbricate numai în schimbul
  evaluat. Absența contractului nu trebuie să genereze un disclosure de validare.
- Metoda
  `HtmlReportSessionTest.rendersApprovedStructureWithValidationAsRequestSibling`
  afirmă prin aserțiuni că validarea este imbricată în cerere. Numele ei spune
  contrariul și trebuie corectat odată cu consolidarea testului semantic.
- `atf-reporting` rămâne independent de JUnit. Dacă lipsește un model neutru
  pentru verdictul testului, acesta se definește la limita contractului de
  raportare și se adaptează în stratul de test.

Scor de prioritate: **95/100** în [registrul feedback-ului](README.md).

## 2. Derularea comună pentru tabelele `TECHNICAL TRACE`

### Situația din cod

Detaliile de răspuns sunt randate ca tabelul `helm-trace-table` în disclosure-ul
`Response details`. CSS-ul îi stabilește tipografia și coloanele, dar tabelul nu
este învelit într-un container cu derulare verticală. În consecință, un trace cu
multe câmpuri poate extinde excesiv zona de detalii, deși ATF are deja tokenul
comun `AtfReportThemeTokens.TABLE_SCROLL_HEIGHT` și folosește acel plafon pentru
alte tabele.

Acesta este gol de implementare și acoperire. Testul browser actual verifică
suprafețele scrollabile pentru Overview, audit, acțiuni Playwright, aserțiuni și
agregatele Network Inspector, dar nu măsoară tabelul `TECHNICAL TRACE` din
`Response details`.

### Contractul de randare cerut

```text
Response details
├── TECHNICAL TRACE — tabel cu derulare verticală comună
│   ├── înălțime naturală cât timp încape
│   ├── plafon: --atf-table-scroll-height (token unic, în prezent 500px)
│   ├── antet fix în timpul derulării
│   └── ultimul rând accesibil
└── JSON de răspuns — disclosure/panou de cod cu limita proprie
```

Se derulează numai zona tabelului. Disclosure-ul exterior rămâne compact, iar
JSON-ul nu este absorbit în wrapper-ul tabelului și nu își pierde limita proprie.
Regula este aceeași în rapoartele Extent și motorul alternativ și pentru trace-ul API
direct, respectiv schimburile API atașate unui pas UI.

### Condiții de acceptare

1. Tabelul `TECHNICAL TRACE` folosește același token și plafon ca
   `Overview → System/Environment`; nu introduce valoare CSS separată sau
   înălțime hardcodată per renderer.
2. Pentru conținut scurt, containerul rămâne la înălțime naturală și nu rezervă
   spațiu gol. Pentru conținut lung, containerul nu depășește plafonul comun și
   derulează până la ultimul rând.
3. Antetul rămâne vizibil în timpul derulării; celulele se împachetează fără
   overflow orizontal al paginii.
4. Derularea tabelului nu extinde pagina prin toate rândurile și nu modifică
   starea implicit închisă a disclosure-ului `Response details`.
5. JSON-ul de răspuns rămâne separat și își păstrează politica proprie de
   afișare/derulare.
6. Testele verifică DOM-ul randat și stilurile calculate pentru Extent și
   motorul alternativ, atât cu date scurte, cât și cu suficiente câmpuri pentru depășirea
   plafonului. Scenariile includ trace API direct și API atașat la un pas UI.

Scor de prioritate: **85/100** în [registrul feedback-ului](README.md).

## Proprietari normativi

- [Contractul de raportare Extent](../ATF_Reporting_Contract.md) — semantica,
  ierarhia și regulile vizuale comune.
- [README-ul `atf-reporting`](../../atf-reporting/README.md) — modelul,
  proiecțiile, markup-ul și stilurile.
- [README-ul `atf-reporting-tests`](../../atf-reporting-tests/README.md) —
  matricea de verificare și probele componentelor și randării.
- [README-ul Practice Shop](../../atf-test-practice-shop/README.md) —
  compoziția și responsabilitățile specifice consumerului.
