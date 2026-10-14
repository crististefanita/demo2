# ATF Java — backlog tehnic activ

Acest document păstrează matricea compactă de execuție pentru ToDo-urile din
[README-ul feedback](README.md). Nu este sursă de adevăr pentru contracte; când
un punct devine stabil, regula se mută în `docs/` și punctul se elimină aici.

## Ordine recomandată

```text
F-01 UI/theme/runtime controllers
  → F-02 semantic snapshot comun
  → F-03 immutable runtime snapshot
  → F-04 failure cause/redaction propagation
  → F-05 DOM/component verification
  → F-06 architecture boundaries
```

## Matrice de închidere

| ID | Owner | Test minim obligatoriu | Dovadă finală |
| --- | --- | --- | --- |
| F-01 | reporting theme + publication pipeline | composition order, filters, collapse, computed styles, parallel sessions | TAB MCP pe Overview/Tests/Category/Fails |
| F-02 | semantic document + Extent adapter | aceeași fixture în Extent și fallback, aceeași ierarhie/count/status/redaction | artifact autonom și deep-links |
| F-03 | configuration/execution composition root | două roots/config-uri concurente, precedence și snapshot immutable | build staged fără citiri globale |
| F-04 | lifecycle + reporting evidence | retry/body/teardown failure, cause primary/secondary și redaction | Fails cu request/validation/cause concrete |
| F-05 | report artifact runtime | click tabs, filters, hash, disclosure, request → validation siblings | comportament DOM verificat în TAB MCP |
| F-06 | architecture tests | import/POM forbidden, package ownership și fixture invalid | diagnostic boundary explicit |

Ultimul increment F-03 a introdus `AtfReportConfigurationContext`: rezolvarea
configurației poate fi executată cu două roots independente, fără citiri
ulterioare din `user.dir`, proprietăți de sistem sau environment. Rămân pentru
F-03 snapshot-ul complet de runtime și injectarea lui în composition root.

## Gate comun

Pentru fiecare increment: recitește live, copiază într-un staging nou, adaugă
testul înaintea implementării, rulează reactorul Maven, verifică artifactul
autonom în TAB MCP și compară staging-ul cu live după build. Un raport vechi
sau un build pe checkout-ul live nu este dovadă de închidere.

