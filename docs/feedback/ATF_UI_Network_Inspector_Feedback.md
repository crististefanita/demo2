# ATF UI Network Inspector — feedback compilat

## Verdict curent

**PARTIAL — vertical slice, request-failed, settlement consumer proof și custom
capture guard implementate; mutation retry rămâne design-only.**

ATF Java are acum un boundary generic pentru observații de rețea Playwright:
opt-in, host allowlist, observații typed și redacted, auth method/source,
signature counts, body preview bounded și settlement bounded. Practice Shop îl
adaptează în journal-ul existent și îl proiectează în checkpoint-ul UI existent.

Inspectorul nu este agent, nu execută prompturi, nu calculează verdicturi și nu
repetă acțiuni de browser. `README_AI_Evaluation.md` rămâne ownerul cerințelor
pentru AI evaluation și `atf-reporting` rămâne renderer-neutral.

## ToDo activ

| ID | Owner | Limită | Criteriu de închidere |
|---|---|---|---|
| `UI-NET-11` | `atf-ui` + consumer | Mutation retry este design-only; read-back-ul bounded nu dovedește că replay-ul este sigur. | Identity și payload stabile, idempotency verificată și test negativ pentru absența replay-ului implicit. |

Contractul stabil este în [README-ul `atf-ui`](../../atf-ui/README.md),
[README-ul Practice Shop](../../atf-test-practice-shop/README.md) și
[contractul de raportare](../ATF_Reporting_Contract.md). Configurația concretă
este în `atf-test-practice-shop/src/test/resources/atf-practice-shop.yaml`.

<details>
<summary><strong>Închideri verificate — UI-NET-01..10 și 12..17</strong></summary>

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
| `UI-NET-09` | settlement-ul este bounded și configurabil | ✅ | `UiNetworkSettlementPolicy`; wait-ul refuză politica disabled |
| `UI-NET-10` | inspectorul nu face replay automat | ✅ | replay-ul mutației este în afara boundary-ului și cere contract de idempotency |
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

## Dovadă curentă

- `UiNetworkInspectionConfigurationTest`: 10 teste verzi; inclusiv listenerul
  `onRequestFailed`, redactarea URL/headers/body/motiv, custom capture
  resanitization/bounding și detach la închidere;
- `ReportNetworkInspectorTest`: 6 teste verzi;
- `PracticeShopApiNetworkJournalTest`: 5 teste verzi; failure-ul trece prin
  adapter, cu motiv și source păstrate, inclusiv fără checkpoint reușit;
- `ExtentReportUiEvidencePublishingTest`: 11 teste verzi; HTML-ul failure-ului
  arată scenariul și exchange-ul `Warning` chiar după `complete(PASS)`,
  `Unavailable`, motivul și niciun `RESPONSE JSON` fictiv;
- `PracticeShopUiNetworkSettlementComponentTest`: compactarea consecutivă,
  read-back bounded și timeout fără replay;
- Pentru acest fix s-au rerulat probele focalizate (10 UI + 5 Practice Shop +
  17 reporting, toate verzi), apoi suita completă în staging extern (530 teste,
  0 failure, 0 error, 3 skip-uri explicite);
- raport fresh verificat prin Playwright: readiness/performance true,
  `scrollWidth == clientWidth`, zero `<hr>`, exchange-uri nested, disclosures
  `Request details → Response details → Execution evidence`, auth și transport
  redacted/bounded. Raportul live nu este dovada ramurii `request-failed`;
  aceasta este acoperită de probele deterministe de mai sus.

## Limite active

Mutation retry se poate activa numai după proof-ul `UI-NET-11`; inspectorul nu
face replay implicit. `UI-NET-17` se redeschide doar dacă storage-ul,
subscriberul sau raportul poate primi din nou observația brută a caller-ului.
