# ATF Java — backlog activ de refactorizare

Acest document este matricea de execuție pentru ToDo-urile rămase din
[feedback-ul activ](README.md). Nu repetă contractele stabile, nu păstrează
istoric și nu reintroduce lucrurile deja închise.

## Ordine recomandată

```text
F-06 hotspot decomposition
  → F-01 reporting orchestration
  → F-02 semantic snapshot comun
  → F-03 configuration composition root
  → F-04 failure chain cu retry
  → F-05 verificare comportamentală artifact
  → D-01..D-04 durate și badge-uri
```

F-06 și F-01 trebuie începute primele deoarece definesc limitele în care pot fi
extrase componentele fără a crea package-uri artificiale. Inventarul și regula
de boundary există deja; backlog-ul urmărește doar reducerea hotspot-urilor și
menținerea verificărilor.

## Matrice de închidere

| ID | Owner | Test minim obligatoriu | Dovadă finală |
|---|---|---|---|
| F-01 | `atf-reporting` theme/runtime | composition order, filters, collapse, CSS tokens, parallel sessions | artifact staged verificat în TAB MCP |
| F-02 | semantic document + Extent adapter | aceeași fixture și aceleași count/status în ambele proiecții | snapshot inspectabil și deep-links |
| F-03 | composition root + config loaders | roots concurente, precedence, schema strictă, snapshot immutable | build staged fără `user.dir` implicit |
| F-04 | execution lifecycle + reporting evidence | retry + body failure + teardown failure + redaction | `Fails` și `Tests` cu chain ordonat |
| F-05 | report artifact runtime | click tabs, filters, hash, disclosures, siblings, computed styles | test component + TAB MCP |
| F-06 | architecture tests | import/POM forbidden, static state, package inventory, allow-list | diagnostic boundary reproductibil |
| D-01 | duration runtime | `data-duration-ms` și `data-duration-source` pe noduri | aceeași valoare în Tests/Fails/Category |
| D-02 | duration aggregation | suma copiilor direcți, fără vendor double-count | clasa = suma scenariilor vizibile |
| D-03 | duration formatter | absent/malformed/zero/leaf | `—` sau `N ms`, niciodată count/timestamp |
| D-04 | theme tokens | badge geometry, status/duration separation, computed styles | token unic în toate tab-urile |

## Gate comun

Pentru fiecare increment: recitește live, creează staging nou, scrie testul
contractului, implementează refactorizarea, rulează reactorul relevant și
compară staging-ul cu live după build. Pentru schimbări observabile,
generează raportul în staging, deschide-l în TAB MCP și verifică toate
suprafețele relevante. Un raport vechi, un build pe checkout-ul live sau un
test relaxat nu este dovadă de închidere.
