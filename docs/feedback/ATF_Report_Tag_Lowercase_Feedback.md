# ATF reporting — tag/annotation lowercase feedback

## Finding

The report screenshot exposed uppercase fixture categories such as `CATEGORIES`,
`FAILS`, `OVERVIEW` and `EVIDENCE` in the visible annotation chips. That made
the report vocabulary inconsistent with the lowercase CSS/filter contract.

## Resolution

`ReportTagNormalizer` is now the single reporting boundary for tags. It trims,
drops null/blank values, lowercases with `Locale.ROOT`, and deduplicates while
preserving first-seen order. Extent categories and the semantic fallback both
consume the canonical list. Runtime `readTagValues` and `testTags` also
lowercase values read from existing markup, so filters remain coherent for
legacy artifacts.

## Verification ToDo

- [x] Unit-test null/blank handling, trim, lowercase, deduplication and order.
- [x] Verify semantic fallback receives the same canonical tags.
- [x] Verify Extent receives canonical categories before HTML publication.
- [x] Verify runtime tag extraction/filter inputs are lowercase.
- [ ] Fresh UI artifact must be opened in ATF Debug/Playwright MCP and checked
      visually after the next reporting E2E run; existing live runs are not
      stopped for this change.
