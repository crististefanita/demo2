# Practice Shop E2E — active consumer gaps

## Scope

This feedback retains only consumer gaps not yet closed by the live code and
owner tests. It is not a dated run log or report archive. Stable contracts remain
in the relevant module READMEs.

## Active gap — action-details selection

### `SHOP-E2E-ACTION-DISCLOSURE-01` — expose the generic action-details mode to E2E consumers

`atf-reporting` supports `INCLUDED_COLLAPSED` and `SUMMARY_ONLY` through
`ReportEvidenceProfile.actionDetailsMode()`. The Shop composition creates that
profile, but `@WithE2eHarness` currently exposes only validation profile, report
flow, and screenshot policy. A Shop test therefore cannot select the generic
rendering mode from its IntelliJ class annotation.

Required acceptance:

- expose a typed annotation option with the existing generic modes and a safe
  default;
- map the selected value into `ReportEvidenceProfile` without changing action
  execution, assertions, screenshots, API evidence, or the Gherkin tree;
- component-test both modes and verify that the disclosure reflects the
  selection while action evidence and verdict remain unchanged;
- keep configuration consumer-owned; do not add Practice Shop conditions to
  generic reporting.

Close this gap when an E2E class can select either mode through its annotation
and owner tests prove both mappings without launching a browser.

## Current code contract

`@AuthenticatedE2e` already models two account sources:

| Mode | Setup behavior | External effect |
|---|---|---|
| `REUSE_FIXTURE` | Load a previously registered account from the local account properties file, then prove login in `SETUP` | No account registration; later purchase may create an order |
| `REGISTER_PER_RUN` | Build a unique account from the local registration profile, submit once, confirm registration, then prove login in `SETUP` | Creates a persistent demo account; it is not deleted or replayed after an uncertain submit |

- Both target classes currently select `REGISTER_PER_RUN` in their class
  annotations; YAML does not override the mode.
- The account-properties source is ignored, and switching to the existing
  account branch currently requires a source-level profile change or a dedicated
  fixture-mode class.
- The selected registration profile is required before either class reaches the
  browser. This is a configuration/preflight gap, not evidence that the account,
  catalog, or purchase business flow failed.

- The in-memory account cache is scoped by test class. It can reuse registration
  between methods of `PracticeShopAccountAndCatalogE2eTest`.
- `PracticeShopAuthenticatedPurchaseE2eTest` has a separate scope and does not
  reuse that class's account.
- The cache component test checks map semantics only; it does not establish the
  registration/login journey or reuse boundary across test classes.
- “Remains reusable across the external suite” overstates the implemented
  class-only scope unless the intended scope is explicitly narrowed.

## Active gap

### `SHOP-E2E-AUTH-LIFECYCLE-01` — selectable account source with proven setup

Make the authenticated examples easy to run with either a registered local
fixture or a newly registered synthetic account, while keeping all account
acquisition and login work in `SETUP`. The selected mode must be explicit and
inspectable from the test declaration/configuration; a missing or invalid source
must produce a concise, actionable preflight result without exposing values.

Required acceptance:

- With `REUSE_FIXTURE`, `SETUP` loads the configured local account and proves
  login through the account page and the selected evidence profile; it does not
  register another account.
- With `REGISTER_PER_RUN`, `SETUP` creates one unique account, submits once,
  verifies registration, and proves login before `TEST BODY`. The intended reuse
  boundary (method, class-run, or broader suite) is explicit and covered by a
  behavioral test; it must not be inferred from a static cache helper alone.
- `PracticeShopAccountAndCatalogE2eTest` proves the account and catalog journeys
  without creating an order. Its account reuse claim matches the actual scope.
- `PracticeShopAuthenticatedPurchaseE2eTest` can use either selected account
  source, then completes the purchase through the explicit final `Confirm` and
  verifies the terminal order confirmation. Payment validation alone is not a
  completed purchase.
- Setup failures stop before `TEST BODY`; uncertain registration is never
  replayed. Reports identify the setup mode and redact credentials, identity,
  address, and contact data.
- For `REGISTER_PER_RUN`, show the new-account evidence specifically in
  `SETUP` → registration `GIVEN` → **Details** → **Setup evidence**, matching
  the area in the reference image. Use its `CHECK / EXPECTED / ACTUAL / RESULT`
  table for the selected creation mode, observed registration outcome, and
  account readiness for login. An API trace or Playwright action list alone
  does not satisfy this summary.
- Keep the subsequent authenticated-session proof in a sibling `AND` under the
  same `SETUP`; neither registration nor login belongs in `TEST BODY`. For
  `REUSE_FIXTURE`, the `GIVEN` evidence describes fixture selection and does not
  imply that a new account was created.
- Each account-source mode must produce a fresh Extent report from its actual
  E2E run. The report must make the `SETUP` sequence auditable before
  `TEST BODY`; a skipped run, a component report, or a guest-purchase report is
  not a substitute.
- Attach the exact generated report path to the run evidence and inspect the
  rendered `Tests` view (including expanded setup evidence) for ordering,
  redaction, and a passing setup verdict. Never retain credentials or generated
  identity/contact values in feedback or documentation.
- Live acceptance does not pass by skip, fixture shape alone, or a successful
  intermediate login. The purchase journey requires its terminal confirmation
  and, when API evidence is selected, a new order exchange correlated to that
  confirmation.

### Closure condition

Close this gap when the fixture and register-on-demand modes are both exercised
through the intended authenticated setup, the account/catalog class demonstrates
its declared reuse scope, and the authenticated purchase reaches the final order
confirmation. Until then, the account-source capability is implemented only in
part: the modes exist, but the target journeys do not currently prove both
branches or a shared account lifecycle.

## Stable contracts promoted to owner documentation

| Contract | Normative source |
|---|---|
| Fixture and registration profile format, redaction, preflight, and non-replay policy | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| SETUP must pass before the business body; account lifecycle is separate from purchase actions | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| API evidence is selected by checkpoint; guest setup does not depend on account credentials | [Practice Shop README](../../atf-test-practice-shop/README.md) |
| Journey summary is informational and terminal; it is not another business step | [Practice Shop README](../../atf-test-practice-shop/README.md), [ATF Reporting README](../../atf-reporting/README.md) |
| Action ordinals remain monotonic across `drain()` calls in one sequence; the E2E reuses that sequence across setup and checkpoints | [ATF UI README](../../atf-ui/README.md), [PlaywrightActionSequenceTest](../../atf-ui/src/test/java/atf/api/ui/playwright/PlaywrightActionSequenceTest.java) |
