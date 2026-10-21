# ATF UI Network Inspector — feedback compilat

## Verdict curent

**IMPLEMENTAT — observație bounded, request-failed și settlement consumer proof.
Retry-ul generic al mutațiilor este exclus prin design.**

**Maturitate inspector: 88/100.** Cursorul pre-acțiune exclude read-back-ul
vechi; retry-ul de acțiune este limitat la operații read-only.

ATF Java are acum un boundary generic pentru observații de rețea Playwright:
opt-in, host allowlist, observații typed și redacted, auth method/source,
signature counts, body preview bounded și settlement bounded. Practice Shop îl
adaptează în journal-ul existent și îl proiectează în checkpoint-ul UI existent.

Inspectorul nu este agent, nu execută prompturi, nu calculează verdicturi și nu
repetă acțiuni de browser. `README_AI_Evaluation.md` rămâne ownerul cerințelor
pentru AI evaluation și `atf-reporting` rămâne renderer-neutral.

## Contract de siguranță pentru mutații

`UI-NET-11` este închis ca decizie de arhitectură:

- ATF nu reia automat mutații Playwright și nu oferă mutation retry generic;
- `UiRetryOperation` conține numai acțiuni read-only; politica fail-closed nu
  poate configura `Click` sau `Submit`;
- settlement-ul așteaptă response/read-back și nu execută din nou acțiunea;
- `requestSignature` grupează observații. Digestul folosește body redacted și
  bounded, iar query-ul este ascuns; nu reprezintă payload identity și nu
  dovedește idempotency.

Un retry specific unui consumer ar necesita toate condițiile de mai jos înainte
de implementare:

- identity stabilă a operației și a resursei afectate;
- payload complet asociat încercării, cu comparație sigură care nu publică date
  secrete și nu se bazează pe preview-ul bounded;
- contract server-side de idempotency, inclusiv comportamentul la duplicat și
  timeout ambiguu;
- opt-in explicit al consumerului și test negativ care dovedește că lipsa
  contractului nu produce replay.

Aceste condiții sunt criterii pentru o posibilă facilitate separată, nu un ToDo
pentru inspectorul generic.

Contractul stabil este în [README-ul `atf-ui`](../../atf-ui/README.md),
[README-ul Practice Shop](../../atf-test-practice-shop/README.md) și
[contractul de raportare](../ATF_Reporting_Contract.md). Configurația concretă
este în `atf-test-practice-shop/src/test/resources/atf-practice-shop.yaml`.

<details>
<summary><strong>Închideri verificate — UI-NET-01..17</strong></summary>

| ID | Cerință | Stare | Dovadă / limită |
|---|---|---|---|
| `UI-NET-01` | inspectorul este explicit opt-in și fail-closed | ✅ | `UiNetworkInspectionConfiguration`; default disabled |
| `UI-NET-02` | traficul este limitat la host-uri API configurate | ✅ | `allowedHosts`; host necunoscut este ignorat |
| `UI-NET-03` | observația este typed înainte de reporting | ✅ | `UiNetworkObservation` păstrează method, path, status, headers redacted și body preview bounded |
| `UI-NET-04` | auth-ul publică numai method/source, fără credential | ✅ | bearer/basic/api-key/cookie/signed-url/none clasificate fără valori secrete |
| `UI-NET-05` | fiecare observație are service, mechanism și signature | ✅ | aggregate-ul publică host/service și `browser network / Playwright`; digestul brut rămâne intern |
| `UI-NET-06` | body-ul este bounded și prezentarea nu multiplică request-ul | ✅ | `maxBodyPreviewCharacters`, `presentationObservations()` și signature counts |
| `UI-NET-07` | API evidence se leagă de pasul UI care a cauzat-o | ✅ | Practice Shop păstrează un checkpoint Gherkin, cu `UI validation` și `API validation` ca siblings |
| `UI-NET-08` | request/response/execution folosesc forma API canonicală | ✅ | fiecare exchange are disclosures locale; retry history aparține `Execution evidence` |
| `UI-NET-09` | settlement-ul este bounded, configurabil și exclude read-back-uri anterioare acțiunii | ✅ | `UiNetworkSettlementPolicy`; `observationCursor()` + `awaitAfter(...)` resping GET-ul stale, iar wait-ul refuză politica disabled |
| `UI-NET-10` | inspectorul nu face replay automat | ✅ | replay-ul mutației este în afara boundary-ului și cere contract de idempotency |
| `UI-NET-11` | mutation retry generic este exclus | ✅ | `UiRetryOperation` enumeră numai operații read-only; `UiRetryPolicyTest` respinge `Click`, iar `PlaywrightActionSequenceTest.retryPolicyDoesNotReplayMutatingActions` verifică un singur attempt |
| `UI-NET-12` | un retry al aceluiași request rămâne un singur exchange | ✅ | count/attempt history locală; nu se creează `API n` duplicat |
| `UI-NET-13` | observațiile pot fi consumate plug-and-play de alt consumer | ✅ | `UiNetworkInspector` este independent de Extent, business routes și assertions |
| `UI-NET-14` | network inspectorul nu se confundă cu UI/API validation | ✅ | inspectorul aggregate este technical evidence; verdictul rămâne la checkpoint/validator |
| `UI-NET-15` | request failure înainte de response este observabil fără false-green | ✅ | `Page.onRequestFailed` → observație redacted → journal/mapper → `Unavailable` + motiv în execution/aggregate; exchange și inspector `WARNING`, fără `RESPONSE JSON` fictiv. Verificat prin teste deterministe de captură, adapter și HTML; nu printr-un eșec live provocat intenționat. |
| `UI-NET-16` | service-ul poate fi numit consumer-specific fără să polueze inspectorul generic | ✅ | `PracticeShopApiNetworkJournal` injectează `Function<String,String>` la adapter boundary, iar aliasul este păstrat în `ReportTraceMetadata.service`; `atf-ui` publică în continuare host-ul honest |
| `UI-NET-17` | intrarea publică pentru observații construite de caller nu poate publica secrete sau body nebounded | ✅ | `UiNetworkObservation.sanitizedFor(...)` reconstruiește URL/path, headers, body, auth/source/failure, signature și service înainte de storage/subscriber; testul negativ verifică query/header/body secret, bounding și signature caller-invalid |

Contractul pentru `request-failed` și pentru resanitizarea custom capture este
protejat prin testele de mai jos; statusul `Unavailable` rămâne diagnostic, nu
un verdict business `FAIL`.

</details>

## Dovada păstrată în source tree

Contractele sunt urmărite prin `UiNetworkInspectionConfigurationTest`,
`ReportNetworkInspectorTest`, `PracticeShopApiNetworkJournalTest`,
`ExtentReportUiEvidencePublishingTest` și
`PracticeShopUiNetworkSettlementComponentTest`. Acestea acoperă resanitizarea,
redaction/bounding, request-failed fără response inventat, settlement după
cursor și absența replay-ului la timeout.

Artifactul browser poate dovedi numai randarea și interacțiunea observate în acel
run; nu dovedește singur ramura `request-failed` sau siguranța retry-ului.
Numărătorile și rezultatele de execuție se citesc din raportul fresh al gate-ului,
nu se păstrează aici ca stare permanentă.

## Status monitorizat și criterii de redeschidere

Nu există ToDo activ. Decizia curentă rămâne no-replay pentru mutation retry
generic; orice extensie consumer-owned trebuie să satisfacă separat contractul
de siguranță de mai sus înainte de replay.
`UI-NET-17` se redeschide doar dacă storage-ul, subscriberul sau raportul poate
primi din nou observația brută a caller-ului.
