# ATF Mock Transport

`atf-mock` este modulul decuplat pentru răspunsuri API deterministe. Depinde
doar de `atf-core` și implementează `ApiTransport`; nu cunoaște Extent, JUnit,
serviciile Users/Posts sau validarea de business.

## Contract

```text
test composition root -> AtfMockTransport -> ApiTransport -> ApiExecutor
                                             -> ApiResponse determinist
```

Mock-ul nu face DNS, TLS sau conexiuni HTTP. URL-ul primit de transport este
păstrat în `ApiResponse` pentru evidence, astfel încât raportul poate afișa
ruta completă fără să pretindă că serverul a fost contactat.

## Configurație YAML

`AtfMockConfigurationLoader` caută `atf.mock.config`, `ATF_MOCK_CONFIG` sau
`atf.yaml` în directorul curent și părinții lui. Citește numai secțiunea
`mock`; dacă aceasta lipsește, folosește fixture-ul built-in compatibil.

```yaml
mock:
  routes:
    - method: GET
      path: /public/v2/users
      status: 200
      duration: PT0.012S
      headers:
        Content-Type: application/json
      body: '[{"id":1,"name":"ATF User"}]'
  fallback:
    status: 500
    body: '{"message":"Unexpected mock route"}'
```

## Matching pentru request-uri variate

O rută mock poate restrânge potrivirea la un profil de autentificare și la
parametri query. Aceste constrângeri sunt utile pentru testele API care trebuie
să dovedească faptul că raportul păstrează forma reală a request-ului:

```yaml
mock:
  routes:
    - method: POST
      path: /public/v2/bulk-import
      auth-profile: api-key
      query:
        mode: validate
      status: 202
      body: '{"accepted":true}'
```

`auth-profile` este metadata de matching, nu un secret. Valorile sensibile nu
se pun în YAML-ul de test; auth-ul este reprezentat prin `AuthType` și prin
headerele redacted din evidence. Dacă profilul sau un parametru nu corespunde,
mock-ul nu acceptă ruta și ajunge la fallback-ul configurat.

Suitea `RequestVariantsApiTests` folosește patru forme distincte: Bearer +
query, API key + body mare, Basic + body nested și Custom + parametri. Fiecare
are o rută YAML proprie, pentru ca testele să poată verifica izolat metoda,
URL-ul complet, query string-ul, tipul de auth și răspunsul nested. Aceasta
rămâne o simulare deterministă; nu transformă accidental testele offline în
apeluri către `https://atf.local`.

`duration` este o durată ISO-8601 Java (`java.time.Duration`): 12 ms se scrie
`PT0.012S`, nu `PT12MS`. Forma este intenționată și este verificată de testul
YAML al transportului, pentru ca fixture-urile să nu depindă de un parser
permisiv sau de unități inventate.

Configurația este încărcată o singură dată la composition root. Un fallback
explică rutele uitate fără să transforme un request necunoscut într-un 200.
`ApiAcceptanceReportTest` și `ApiTestFixture` folosesc configurația proiectului;
testele unitare pot folosi `AtfMockTransport.defaults()` pentru izolare.

`atf-mock` nu este live HTTP. Pentru un endpoint real se folosește separat
`HttpClientTransport` și profilul live opt-in din `ApiLiveReportTest`.
