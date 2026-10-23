# ATF Test Kit

`atf-test-kit` este contractul transversal, strict test-only, pentru toate
modulele ATF. Nu conține renderer, mock transport, browser sau cod de producție.

## Pe scurt

```mermaid
flowchart LR
    ModuleTests[teste module] -->|declară| Kit[atf-test-kit]
    Kit -->|clasifică| Levels[test layers]
    Kit -. nu intră .-> Production[production classpath]
```

<details>
<summary><strong>Legendă — contractul transversal al testelor</strong></summary>

- modulele declară stratul prin adnotare, iar kit-ul îl verifică;
- cele patru straturi sunt executable test layers: unit, component, integration și E2E;
- support este cod auxiliar, nu al cincilea strat executabil;
- muchia punctată este o interdicție: test-kit-ul nu intră în classpath-ul de producție.
</details>

| Conține | Nu conține | Gate |
|---|---|---|
| adnotări de nivel/canal/console/output/setup/docs, resolvere neutre și layer guard | renderer, mock, browser | teste TestKit și `AtfTestLayerArchitectureTest` |
| clasificare test | business flow | fiecare clasă are un singur nivel |
| reguli transversale | runtime dependency | fail-closed la drift |

## Clasificare

Fiecare clasă executabilă declară exact un nivel:

```text
@AtfUnitTest        → @Tag("unit")
@AtfComponentTest   → @Tag("component")
@AtfIntegrationTest → @Tag("integration")
@AtfE2eTest         → @Tag("e2e")
```

### Ce verifică guvernanța

- `AtfTestLayerArchitectureTest` inspectează toate directoarele `*/src/test/java`;
- verifică adnotarea, package-ul, importurile interzise și izolarea `support`;
- rezolvă din surse și adnotările compuse, prin import sau același package,
  inclusiv compoziția imbricată; ciclurile nu inventează un nivel;
- regula este fail-closed: o clasă fără nivel sau cu mai multe niveluri oprește
  build-ul, inclusiv când nivelurile provin din adnotări compuse.

### Verificări de compoziție

- `AtfModuleTest`/`AtfModuleTestCondition` rămân specifice suitei
  `atf-reporting-tests`, în `atf.api.reporting.test`;
- un modul lipsă poate produce `SKIPPED` prin gate-ul dedicat;
- kit-ul nu introduce o booleană UI paralelă.

### Adnotări de infrastructură implementate

| Package | API | Aplicare |
|---|---|---|
| `atf.api.testkit.channel` | `AtfTestChannel`, `TestChannelPlan`, resolver | canal UI/API/UI_API, separat de tag-ul JUnit al nivelului |
| `atf.api.testkit` + `.console` | `AtfConsoleMessages`, `AtfConsoleMessagePolicyResolver` | politică typed de consolă; `atf-console` o aplică fără sink/formatter duplicat |
| `atf.api.testkit.output` | cele două adnotări de director și `ReportOutputDirectory` | ancoră UI sau project-relative furnizată de consumator |
| `atf.api.testkit.setup.api` | `ApiScenarioSetup`, `ApiTestRef` | declarații; catalogul și executarea rămân la consumator |
| `atf.api.testkit.documentation` | `DocumentationTarget` repeatable | selecție de documente; validatorii rămân în ATF Tests |

Contractul complet, precedența, exemplele și probele sunt în
[Adnotări de infrastructură pentru teste](../docs/ATF_Test_Infrastructure_Annotations.md).
Selecția canalului și contractul generic de output nu au gap-uri funcționale
deschise. Smoke-testul IntelliJ pentru compoziția project-relative aparține
modulului Reporting Tests, care deține acel root; pașii se rulează la schimbări
relevante și sunt documentați în
[ghidul suitei Extent](../atf-reporting-tests/src/test/java/atf/ui/tests/extent/README.md#rulare-directă-din-intellij).

### Limite de responsabilitate

- contractul generic pentru flow mode, screenshot policy și capture source este în
  [`docs/ATF_Evidence_Capture_Contract.md`](../docs/ATF_Evidence_Capture_Contract.md);
- `atf-test-kit` poate deține mapping-ul transversal numai după un contract typed
  și un gate lower-level;
- politica publisherului și flow-urile Practice Shop rămân în ownerii lor.

## Dependențe

```text
module tests → atf-test-kit (test scope)
atf-test-kit -X-> production
atf-console --optional--> atf-test-kit (JUnit policy bridge only)
reporting composition test scope → atf.api.reporting.test
atf-reporting-tests --test--> atf-reporting production
```

Modulele rămân ownerii semantic ai testelor lor. Kit-ul doar normalizează
vocabularul și enforce-uiește ierarhia; nu mută assertions, fixtures, locators,
CSS, JavaScript sau flow-uri între module. Săgeata `--test-->` este intenționată:
compoziția poate consuma librăria reporting în test scope, în timp ce
`atf-reporting/src/main` rămâne fără dependențe ATF.
