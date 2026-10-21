# ATF UI Network Inspector — feedback compilat

## Verdict curent

**PARTIAL — vertical slice implementat, agentic AI încă design-only.**

ATF Java are acum un boundary generic pentru observații de rețea Playwright:
opt-in, host allowlist, observații typed și redacted, auth method/source,
signature counts, body preview bounded și settlement bounded. Practice Shop îl
adaptează în journal-ul existent și îl proiectează în checkpoint-ul UI existent.

Inspectorul nu este agent, nu execută prompturi, nu calculează verdicturi și nu
repetă acțiuni de browser. `README_AI_Evaluation.md` rămâne ownerul cerințelor
pentru AI evaluation și `atf-reporting` rămâne renderer-neutral.

## Cerințe cristalizate

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
| `UI-NET-11` | mutațiile Playwright pot avea retry numai cu identity și payload proof | ⏳ | pattern-ul .NET este identificat; lipsește fixture Java consumer-owned |
| `UI-NET-12` | un retry al aceluiași request rămâne un singur exchange | ✅ | count/attempt history locală; nu se creează `API n` duplicat |
| `UI-NET-13` | observațiile pot fi consumate plug-and-play de alt consumer | ✅ | `UiNetworkInspector` este independent de Extent, business routes și assertions |
| `UI-NET-14` | network inspectorul nu se confundă cu UI/API validation | ✅ | inspectorul aggregate este technical evidence; verdictul rămâne la checkpoint/validator |

## Fluxul normativ

```text
Browser response
  → host allowlist
  → typed/redacted observation
  → consumer adapter
  → checkpoint UI + API validation
  → Request / Response / Execution evidence
  → collapsed journey aggregate
```

Regula de prezentare este compactă: aceeași propoziție Gherkin apare o singură
dată; copiii pot fi UI și API, iar mai multe request-uri sunt exchange-uri
nested. O repetare consecutivă este colapsată numai în stream-ul de prezentare;
trace-ul raw rămâne disponibil pentru audit.

## Configurația recomandată

```yaml
networkInspector:
  enabled: true
  allowedHosts: [api.practicesoftwaretesting.com]
  maxObservations: 256
  maxBodyPreviewCharacters: 12000
  collapseConsecutiveSignatures: true
  settlement:
    enabled: false
    timeout: PT10S
    pollInterval: PT0.1S
```

`settlement.enabled` rămâne `false` până când consumerul definește read-back-ul
și dovedește idempotency-ul mutației. `timeout` este durata maximă de așteptare,
nu o instrucțiune de replay.

## Ce nu trebuie adăugat

- nu se pune un card separat pentru fiecare retry;
- nu se repetă titlul Gherkin pentru UI și API;
- nu se publică URL complet cu token, cookie, query secret sau body nebounded;
- nu se mută semantica business în `atf-ui` sau `atf-reporting`;
- nu se tratează `Runtime diagnostic` ori aggregate-ul de rețea ca `UI validation`;
- nu se declară AI evaluation doar pentru că există o hartă de request-uri;
- nu se activează settlement/replay printr-un default global.

## Dovadă curentă

- `UiNetworkInspectionConfigurationTest`: 4 teste verzi;
- `ReportNetworkInspectorTest`: 6 teste verzi;
- Practice Shop: 105 teste, 0 failure/error, 5 skip-uri explicite;
- raport fresh verificat prin Playwright: readiness/performance true,
  `scrollWidth == clientWidth`, zero `<hr>`, exchange-uri nested, disclosures
  `Request details → Response details → Execution evidence`, auth și transport
  redacted/bounded.

## Următorul proof obligatoriu

1. fixture generic cu două response-uri consecutive cu aceeași signature și un
   request distinct între ele;
2. fixture consumer-owned pentru o mutație idempotentă și read-back bounded;
3. caz negativ în care read-back-ul expiră fără replay și fără verdict verde;
4. component proof pentru corelarea observației cu checkpoint-ul UI;
5. render proof la lățime standard și narrow, fără overflow sau duplicate nodes.

Până la acest proof, network observation este reutilizabilă și sigură pentru
evidence, dar retry-ul activ al mutațiilor rămâne neimplementat intenționat.
