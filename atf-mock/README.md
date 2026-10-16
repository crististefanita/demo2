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

Catalogul mock este separat intenționat de configurația generală ATF:
`atf.yaml` păstrează doar API, validation și reporting, iar `atf-mock.yaml`
deține rutele, răspunsurile și fallback-ul fake. `AtfMockConfigurationLoader`
caută implicit `atf-mock.yaml`/`atf-mock.yml` în directorul curent și părinții
lui. Pentru o compoziție explicită, ordinea este:

1. proprietatea JVM `-Datf.mock.config=cale/catre/atf-mock.yaml`;
2. variabila `ATF_MOCK_CONFIG`;
3. descoperirea ascendentă a `atf-mock.yaml`/`atf-mock.yml`;
4. fixture-ul built-in, numai când nu există un fișier configurat.

Calea explicită poate fi relativă la working directory-ul procesului, ceea ce
permite rulări directe din IntelliJ, CI sau workspace-uri paralele. Dacă o cale
explicită există dar este invalidă, încărcarea eșuează fail-fast; nu revine
silențios la fixture-ul built-in și nu pornește HTTP real.

Fișierul implicit din repository este [`../atf-mock.yaml`](../atf-mock.yaml).
Un consumator poate păstra catalogul în altă locație fără să schimbe
`atf.yaml`, folosind override-ul de mai sus.

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
      request-headers:
        Content-Type: application/json
        X-Client: atf-java
      status: 202
      body: '{"accepted":true}'
```

`auth-profile` este metadata de matching, nu un secret. Valorile sensibile nu
se pun în YAML-ul de test; auth-ul este reprezentat prin `AuthType` și prin
headerele redacted din evidence. Dacă profilul sau un parametru nu corespunde,
mock-ul nu acceptă ruta și ajunge la fallback-ul configurat.

`request-headers` adaugă aceeași regulă pentru headerele trimise de client.
Numele sunt comparate case-insensitive, dar valoarea trebuie să corespundă
exact. Headerele din `headers` sunt headerele primite în response și pot
simula `X-Request-Id`, `ETag`, `Retry-After`, rate-limit sau alte metadata de
transport. În raport, headerele trimise și primite apar în rânduri distincte
din `TECHNICAL TRACE`.

Suitea `RequestVariantsApiTests` folosește patru forme distincte: Bearer +
query, API key + body mare, Basic + body nested și Custom + parametri. Fiecare
are o rută YAML proprie, pentru ca testele să poată verifica izolat metoda,
URL-ul complet, query string-ul, tipul de auth și răspunsul nested. Aceasta
rămâne o simulare deterministă; nu transformă accidental testele offline în
apeluri către `https://atf.local`.

Toate rutele `POST` și `PUT` din catalogul proiectului sunt write-contracts
JSON: request-ul trebuie să aibă body JSON non-gol și `Content-Type` JSON, iar
response-ul configurat trebuie să fie un document JSON non-gol și valid. `ApiRoute`
adaugă automat header-ul `Content-Type: application/json` pentru
`requestBody(...)`, iar mock-ul face fail-closed către fallback pentru body lipsă
sau invalid. Publisher-ul și auditorul UI verifică apoi aceleași două canale în
raport, astfel încât mock-ul să fie sursa de adevăr pentru validarea UI.

`duration` este o durată ISO-8601 Java (`java.time.Duration`): 12 ms se scrie
`PT0.012S`, nu `PT12MS`. Forma este intenționată și este verificată de testul
YAML al transportului, pentru ca fixture-urile să nu depindă de un parser
permisiv sau de unități inventate.

Configurația este încărcată o singură dată la composition root. Un fallback
explică rutele uitate fără să transforme un request necunoscut într-un 200.
Scenariile API și `ApiReportAuditSetup` folosesc configurația proiectului;
`ApiReportSetupFlow` este doar fațada de compatibilitate;
testele unitare pot folosi `AtfMockTransport.defaults()` pentru izolare.

`atf-mock` nu este live HTTP. Pentru un endpoint real se folosește separat
`HttpClientTransport` și profilul live opt-in configurat la execuție.
