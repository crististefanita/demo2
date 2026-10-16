# ATF UI/Extent — failure diagnostic audit

**Audit date:** 2026-10-16 23:00–23:16 (Europe/Bucharest)  
**Scope:** API failures rendered in the Extent `Tests`/`Fails` views and the UI
audit that proves the display is readable, actionable and quiet by default.

This is a focused companion to the evidence-style and section-architecture
feedback. Those documents define the common geometry and module boundaries;
this document owns the failure-specific questions: *which phase failed, why,
what was expected, what was observed, what was retried, and how do I get back to
the exact failing node?*

## Evidence used

The build was made from the live source into the isolated staging workspace
`C:\work\ex\java\.tmp\failure-diagnostic-hardening-20261017`. Existing processes
were not stopped. The validated artifacts were then copied selectively to the
configured live report folders.

| Artifact | Result | SHA-256 |
|---|---:|---|
| [`api-atf-extent-report_2026-10-16_23-15-22-774.html`](../../artifacts/reports/api-atf-extent-report_2026-10-16_23-15-22-774.html) | 7 intentional API scenario failures; setup + POST + PUT fixture included | `99EC7D5F04E5925EAF5FF87D90EE219952E76D6AF922101AD03490A27DD5AB70` |
| [`ui-atf-extent-report_lifecycle-failures-report-ui-test_2026-10-16_23-15-26-907.html`](../../artifacts/reports/ui/ui-atf-extent-report_lifecycle-failures-report-ui-test_2026-10-16_23-15-26-907.html) | 15/15 full lifecycle UI rules green | `F47DE220D14E426128915846F4661744C38725B1162A492C959C8ADEAC8D5F40` |
| [`ui-atf-extent-report_lifecycle-failure-details-report-ui-test_2026-10-16_23-11-34-337.html`](../../artifacts/reports/ui/ui-atf-extent-report_lifecycle-failure-details-report-ui-test_2026-10-16_23-11-34-337.html) | 3/3 focused keyboard/focus/transport UI rules green | `47E764BF713CECF7926F732E354D437BBB75CBDF9F26E4E5A94DE24FF1ED7EEB` |
| [`api-atf-extent-report_2026-10-16_22-57-02-631.html`](../../artifacts/reports/api-atf-extent-report_2026-10-16_22-57-02-631.html) | failure-lifecycle matrix input | `188FEC3315878DD3D02D7E005CDCC5878D848C921CFBC36868E4F69B5A1D352C` |
| [`ui-atf-extent-report_failure-lifecycle-narrow_2026-10-16_22-57-05-124.html`](../../artifacts/reports/ui/ui-atf-extent-report_failure-lifecycle-narrow_2026-10-16_22-57-05-124.html) | 4 failure rows, 1 primary cause, 1 deep-link | `BA49E017DCC2997BF10A527FB6BE9727B5C9A56D68479D7EA6FCF7C9D98FC886` |
| [`ui-atf-extent-report_write-methods-all-api-normal_2026-10-16_22-57-33-769.html`](../../artifacts/reports/ui/ui-atf-extent-report_write-methods-all-api-normal_2026-10-16_22-57-33-769.html) | 39 requests, GET/POST/PUT/PATCH | `D7F641080B3181AA9A1BDC3F5F13E2371751B1C7E25CE42C07B18F2FBC9564A5` |
| [`ui-atf-extent-report_write-methods-all-api-narrow_2026-10-16_22-59-53-735.html`](../../artifacts/reports/ui/ui-atf-extent-report_write-methods-all-api-narrow_2026-10-16_22-59-53-735.html) | narrow layout | `C58755B7EC3EDAB29B24BBC146574408C5D4D7A38E6DE5442F1C0F1539F58942` |

The exact API report was opened through the official ATF Debug loopback server
and inspected with browser MCP. A real Windows browser was not used.

## Failure matrix actually inspected

| Scenario | Failing stage | Diagnostic contract observed |
|---|---|---|
| Users API — setup → test → teardown | `TEST BODY` + `TEARDOWN` | HTTP expected `201`, actual `200`; teardown cause preserves the cleanup exception. |
| Users API — setup failure — body skipped | `SETUP` | HTTP expected `201`, actual `200`; `TEST BODY` is skipped and `TEARDOWN` still runs as `Pass`. |
| Users API — retry → body fail → teardown fail | `TEST BODY` + `TEARDOWN` | Attempts `503 → 200`, `retry.events=1`, final status `200`; body failure remains visible and teardown is separate. |
| Users API — JSON assertion mismatch | `TEST BODY` | `JSON path equals: $.name`, expected `Expected Contract Name`, actual `ATF User`. |
| Users API — header assertion mismatch | `TEST BODY` | `Header X-Required-Header`, expected `present`, actual `<missing>`. |
| Users API — failed POST keeps transport metadata | `TEST BODY` | Failed status assertion with request/response JSON, request/response headers, auth type/profile and attempt history. |
| Users API — failed PUT keeps transport metadata | `TEST BODY` | Failed status assertion with query, request/response JSON, request/response headers, auth type/profile and attempt history. |

The report also keeps the failed request's URL, auth profile/type, request JSON
(`null` for body-less GET), response status/content type/JSON, duration,
attempt count and attempt history. Failed POST/PUT nodes now prove the same
contract on red requests, including request/response headers and body payloads.

## Display verdict

The current display is audit-friendly for the failure contract:

- Scenario, phase and request status badges are visibly marked `Fail` while
  successful sibling phases remain `Pass`.
- A failed phase arrives collapsed; the user sees a compact `Failure details`
  disclosure, not the technical trace wall.
- Expanding the disclosure exposes a small `FIELD / VALUE` trace with
  `FAILURE CAUSE`, `ASSERTION`, `EXPECTED` and `ACTUAL` where applicable.
- Retry evidence remains a separate execution table, so “first attempt failed”
  is not confused with “final response failed”.
- Keyboard `Enter` opens and closes every failure disclosure; focus remains on
  the summary and the final state is collapsed/idempotent.
- The retry validator reconciles `attempts`, `retry.events`, the complete
  attempt-history table and the final response status as one typed snapshot.
- Failed POST and PUT nodes retain the complete request/response transport
  envelope: JSON bodies, headers, URL, auth profile/type, attempts and history.
- `Fails → Open in Tests` is a real deep-link; the selected scenario may be
  open, but descendants and evidence disclosures remain collapsed.
- The inspected render had no decorative `<hr>` separators, no visible blank
  sibling gaps in the evidence stack, and no console errors. The screenshot
  shows the shared compact card contour and two-column diagnostic tables.
- After the manual clicks, details were closed again; the audit did not leave a
  polluted report state.

## Existing guards that are already sufficient

`FailureDetailsDisplayInspector` now proves the most important reusable
invariants: failed scenario/phase discovery, collapsed arrival state, exactly
one failure disclosure per failed phase, semantic cause, TEST BODY assertion
evidence, expected/actual pair, hidden trace before click, and restored final
collapsed state. `FailureFlowInspector` covers the Fails list, primary cause,
deep-link and Overview failure metric. `FailureTransportEvidenceInspector`
owns failed POST/PUT transport facts, while `RetryEvidenceValidator` owns the
renderer-neutral reconciliation. The browser matrix adds empty/failure,
normal/narrow and GET/POST/PUT/PATCH coverage.

The manual Fails-tab audit additionally confirmed that root-cause precedence is
already rendered as `PRIMARY` plus `SECONDARY`: for the first scenario the HTTP
status contract failure is primary and the teardown cleanup failure remains
secondary. `Open in Tests` points to the exact scenario and phase.

## Recommended UI validations / ToDo

These are the remaining high-value checks discovered by the audit. They are
intentionally listed as follow-up work rather than hidden inside a generic
boolean:

- [x] **FD-AUDIT-01 — setup-failure fixture.** Added the deterministic
  `Users API — setup failure — body skipped` scenario. The full UI audit proves
  `SETUP=Fail`, body skip/absence, `TEARDOWN=Pass`, scenario rollup and Fails
  navigation.
- [x] **FD-AUDIT-02 — root-cause precedence.** The Fails detail pane renders
  the body contract failure as `PRIMARY` and the teardown cleanup failure as
  `SECONDARY`, with a working `Open in Tests` link. Keep this as a regression
  guard when the Fails projection is refactored.
- [x] **FD-AUDIT-03 — retry reconciliation.** `RetryEvidenceSnapshot` plus the
  pure `RetryEvidenceValidator` prove `attempts = 1 + retry.events`, complete
  history and last-attempt/response-status equality.
- [x] **FD-AUDIT-04 — failure-kind-specific fields.** The lifecycle fixture keeps
  status, JSON-path and header mismatches distinct; the inspector requires
  `ASSERTION / EXPECTED / ACTUAL` for every failed TEST BODY and the full audit
  passes all three semantic shapes.
- [x] **FD-AUDIT-05 — disclosure interaction hygiene.** The locator/flow boundary
  now drives `Enter` open/close, verifies focus return, `＋/−` transitions,
  idempotent close and collapsed final state in normal and narrow coverage.
- [x] **FD-AUDIT-06 — transport metadata on failures.** Deliberate failed POST
  and PUT scenarios are audited in their red request nodes; JSON bodies,
  request/response headers, URL, auth profile/type, attempts and history remain
  reachable after failure details are expanded.

## Closure rule

Do not mark a future item done from a source-only assertion. The closure gate is:

```text
isolated .tmp build
→ intentional failure fixture
→ focused UI audit green
→ exact API and UI HTML generated
→ exact HTML opened through ATF Debug/browser MCP
→ click each relevant disclosure and deep-link
→ verify status, expected/actual, retry/history, collapsed return, console=0
→ copy only validated artifacts to artifacts/reports[/ui]
```

The failure audit is closed for setup, body, teardown, retry, JSON, header,
keyboard/focus and failed-write transport cases. The full lifecycle audit passed
15/15 rules in the isolated build. The validated final artifacts are the three
links at the top of this document; no new README is needed because this focused
feedback file remains the owner document.
