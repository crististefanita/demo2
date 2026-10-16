# ATF Debug UI

`atf-debug-ui` este un modul de infrastructură pentru investigarea manuală a
unui HTML deja generat. Nu generează raportul, nu îl modifică și nu conține
teste executabile. Publică numai pe `127.0.0.1` directorul care conține
artifactul ales și tipărește URL-ul care trebuie deschis în Playwright/TAB MCP.

## Fluxul de investigare

```mermaid
sequenceDiagram
    participant A as Agent / IntelliJ
    participant D as atf-debug-ui
    participant P as Playwright / TAB MCP
    participant H as HTML artifact

    A->>D: pornește launcher-ul cu --html report.html
    D->>D: validează fișierul și root-ul
    D-->>A: afișează URL loopback
    A->>P: navighează la URL-ul afișat
    P->>H: GET HTML/CSS/JS/read-only
    P-->>A: snapshot, click, screenshot, console, network
```

Responsabilitățile rămân separate:

| Componentă | Responsabilitate |
|---|---|
| `atf-reporting` | generează și publică HTML-ul Extent |
| `atf-debug-ui` | servește read-only artifactul local pe loopback |
| `atf-ui` | deține configurația și adaptorul Java Playwright |
| `atf-tests` / `atf.ui.tests` | verificări automate UI |
| agentul + TAB MCP | investigație manuală interactivă |

## Pornire

Din IntelliJ rulează `atf.api.debugui.AtfDebugUiLauncher` cu argumentele:

```text
--html artifacts/reports/api-atf-extent-report-TIMESTAMP.html
--port 0
```

Launcher-ul afișează URL-ul efectiv, de exemplu:

```text
[ATF DEBUG UI] Target for Playwright/TAB MCP: http://127.0.0.1:PORT/report.html
```

Agentul deschide exact acel URL cu Playwright MCP și poate investiga manual
taburile, filtrele, nodurile expandabile, DOM-ul, console errors, network și
randarea. Serverul rămâne activ până la `Ctrl+C`; `--port 0` alege automat un
port liber și permite rulări paralele în directoare diferite.

Pentru compilare și testul de contract:

```text
mvn -pl atf-debug-ui -am test
```

Testul verifică publicarea HTML și blocarea traversal-ului către fișiere din
afara root-ului. Nu se adaugă un modul separat pentru teste: testele automate
UI rămân în `atf-tests/src/test/java/atf/ui/tests`.
