# ATF Core — kernel neutru și porturi

`atf-core` este stratul de bază al ATF Java. Conține modele, contracte și
abstracții reutilizabile, fără să decidă cum este orchestrat un scenariu sau
cum este randat un raport.

## Locul în arhitectură

```mermaid
flowchart LR
    Validation[atf-validation] --> Core[atf-core]
    Execution[atf-execution] --> Core
    Reporting[atf-reporting] --> Core
    Tests[atf-tests] --> Core
```

Săgeata înseamnă „modulul din stânga depinde de modulul din dreapta”.
`atf-core` nu depinde de modulele de deasupra lui. El furnizează vocabularul
comun prin care modulele comunică fără să se cunoască implementările concrete.

## Responsabilități

| Zonă | Owner în `atf-core` | Nu face |
|---|---|---|
| HTTP | `ApiRequest`, `ApiResponse`, `ApiTransport`, `ApiExecutor` | nu definește scenarii sau assertions |
| routing neutru | `ApiRoute`, `ApiUrlBuilder` | nu deține serviciile Users/Posts |
| timp și retry | `ApiExecutionClock`, `ApiExecutionDeadline`, `ApiRetryWaiter`, `RetryPolicy` | nu decide verdictul testului |
| configurație | `AtfConfiguration`, `AtfRuntimeSnapshot`, `ApiSettings`, `ReportingSettings` și snapshot-uri | nu parsează YAML, nu citește env/filesystem și nu recitește configurația în fiecare request |
| statusuri | `AtfReportStatus`, `ReportStatusAggregator` | nu randă badge-uri |
| reporting ports | `AtfReportPorts`, `ValidationSink`, `ReportStepScope`, `AtfConsoleEvent`, `AtfConsoleSink` | nu importă Extent, HTML, `System.out` sau implementarea `atf-console` |
| redaction | `Redactor`, `RedactionContext`, strategiile de redaction | nu modifică input-ul original |

## Regula de decuplare

```text
ApiRequest / ApiResponse / ValidationResult / Report ports
                         ↓
              modulele de execuție și reporting
```

Core poate defini porturi, dar nu implementează Extent, HTML, CSS, JavaScript,
formatarea consolei, scrierea în `System.out`, JUnit sau servicii de business.
Un port trebuie să fie mic, semantic și
testabil cu fake-uri în memorie.

Configurația este typed și immutable în core. Fișierul YAML, variabilele de
mediu și proprietățile de proces sunt surse externe asamblate de composition
root; core primește rezultatul validat și îl transformă într-un
`AtfRuntimeSnapshot` cu proiecții API și reporting. Astfel
un test paralel nu poate schimba timeout-ul, retry-ul, auth-ul sau politica de
redaction a unei alte sesiuni.

Exemple de direcții corecte:

```text
ApiExecutor → ApiExecutionResult
Validator    → ValidationResult
Reporting   → AtfReportPorts
```

Direcții interzise:

```text
core → ExtentTest
core → JUnit
core → HTML/CSS
core → UsersService / PostsService
```

## Determinism și concurență

Fiecare execuție primește setări snapshot-uite. Timpul monotonic, deadline-ul
comun al request-ului și delay-ul de retry sunt injectabile; testele nu trebuie
să aștepte timp real pentru a verifica timeout-ul sau retry-ul. Modelele care
ies din core nu expun colecții mutabile și nu păstrează state static de rulare.

Pentru write contracts, `ApiRoute.request(...)` respinge `POST` și `PUT` fără
body, iar `requestBody(...)` cere JSON ne-gol, valid și normalizează
`Content-Type: application/json`. Această invariantă aparține construcției
transportului; publisher-ul și auditorii o verifică din nou în evidence, pentru
ca un fixture sau un mock configurat greșit să nu poată produce un raport aparent
valid.

Redaction-ul este compus prin `RedactionContext` immutable. Orice evidence
trimis către reporting trebuie să treacă prin politica de redaction fără să
modifice request-ul, response-ul sau assertion-ul original.

## Teste

Testele din `atf-core` verifică separat:

- request/response și autentificare;
- timeout, cancellation, retry și `Retry-After` cu clock injectabil;
- snapshot-uri și precedence-ul configurației;
- status aggregation și precedența `FAIL > SKIP > WARNING > PASS`;
- redaction pe JSON, headers, query, path și PII;
- contractele porturilor fără Extent concret.

Un test core nu generează raport HTML. Dovada vizuală aparține lui
`atf-reporting`, iar scenariile end-to-end aparțin lui `atf-tests`.
