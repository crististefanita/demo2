# Ghid pentru adăugarea unui test API în ATF Java

Acest document descrie numai pașii necesari pentru un test nou. Reguli generale
de arhitectură sunt în [ATF_Architecture.md](ATF_Architecture.md), iar
contractul raportului în [ATF_Reporting_Contract.md](ATF_Reporting_Contract.md).

## Pașii obligatorii

1. Definește comportamentul API și criteriul de acceptare.
2. Verifică ruta, modelul, serviciul și validatorul existente înainte să creezi tipuri noi.
3. Adaugă calea în RoutePaths și metadatele în catalogul de rute; nu copia endpoint-ul în test.
4. Adaugă sau extinde un serviciu semantic numai dacă operația are un sens de domeniu clar.
5. Execută cererea prin executorul ATF și transportul injectat; testul nu construiește direct clientul HTTP.
6. Grupează toate verificările response-ului într-un singur tabel de assertions.
7. Înregistrează cleanup-ul imediat după ce testul dobândește ownership-ul unei resurse.
8. Adaugă întâi testul unitar sau de componentă pentru regula nouă.
9. Adaugă scenariul cu nume și tag-uri care descriu intenția.
10. Generează artifactul și verifică raportul în ordinea definită de contract.

Pentru un flow cu mai multe request-uri, nu copia contractul între pași. Definește
separat contractul de mutație și contractul read-model-ului, apoi adaugă o
verificare relațională între răspunsuri (id/self-link, versiune sau lineage).
Verificarea relațională se atașează request-ului de read-back ca evidence, nu ca
un request HTTP inventat.

## Structura minimă a unui scenariu

```mermaid
flowchart TD
    Test[Test API] --> Plan[Scenario plan]
    Plan --> Setup[SETUP opțional]
    Setup --> SetupRequest[Request(s)]
    Plan --> Body[TEST BODY]
    Body --> BodyRequest[Request]
    BodyRequest --> Trace[Trace + response evidence]
    Body --> BodyValidation[Validation sibling]
    BodyValidation --> Assertions[Aggregate assertion table]
    Plan --> Teardown[TEARDOWN]
    Teardown --> Cleanup[Cleanup/read-back]
```

Pentru un test simplu, TEST BODY trebuie să conțină request-ul și validarea
lui. Pentru lifecycle, fazele sunt create numai când au conținut real. Un nod
TEST BODY nu repetă failure-urile care apar deja în tabelul de assertions; faza
arată doar sumarul, iar diagnosticul detaliat rămâne sub validation.

## Ce verifică un test API

Un validator poate grupa în același tabel:

| Assertion | Expected | Actual | Result |
|---|---|---|---|
| HTTP status | codul și textul așteptat | răspunsul primit | PASS/FAIL/WARNING |
| Content-Type | tipul acceptat | header-ul primit | PASS/FAIL |
| JSON schema | schema contractului | schema evaluată | PASS/FAIL |
| câmp obligatoriu | valoare sau tip | valoarea primită | PASS/FAIL/WARNING |
| relație | resursa asociată | rezultatul verificării | PASS/FAIL |

Nu se creează câte un nod pentru fiecare assertion. Tabelul este agregat pentru
response-ul curent, astfel încât toate abaterile să fie vizibile împreună.

## Durată și status în raport

Durata este afișată cu unitatea explicită (`1 ms`, `16 ms`, `25 ms`). Statusul
și durata sunt tokens diferite: `Pass` + `16 ms` nu reprezintă un count de
assertions sau pași. O frunză folosește durata măsurată; un părinte folosește
durata proprie exclusivă, dacă există, plus totalurile copiilor structurali
direcți. O durată Extent deja inclusivă nu se adună din nou cu descendenții.

Badge-urile `Pass`, `Warning` și `Fail` au aceeași geometrie în fiecare tab și
proiecție a raportului: `18.2px` înălțime, `0 5px` padding, `4px` radius,
`9.52px` font, `18.2px` line-height, `400` weight, fără border. Timestamp-ul nu este
durată și nu apare ca o coloană în tab-ul Tests.

## Separarea responsabilităților

```text
RoutePaths           -> căi relative
ApiRoutes             -> metadate operaționale
Service               -> acțiune semantică
ApiExecutor           -> transport, retry și durată
Validator             -> assertions și tabel agregat
Report session        -> arbore și evidence
Scenario              -> ordine și intenție
```

Dacă testul începe să conțină JSON de infrastructură, CSS, markup Extent,
politici de retry sau endpoint-uri literale, responsabilitatea este în locul
greșit.

## Replay manual și request-uri reale

Raportul API este sursa de pași reproductibili: `METHOD`, `URL`, headers
efective, `REQUEST JSON`, `RESPONSE JSON`, `TRANSPORT PROVENANCE` și
`MANUAL REPLAY`. În acceptance, un URL real configurat este numai metadata a
mock-ului; raportul îl marchează `ACCEPTANCE / MOCK` și `NOT EXECUTABLE`. Nu se
copiează un cURL dintr-un raport mock ca și cum ar fi fost executat.

Request-urile reale sunt separate și opt-in:

```yaml
api:
  transport: live
  baseUri: https://<real-api-host>/<base-path>
```

Rularea mai cere `-Datf.api.live.optIn=true` sau
`ATF_API_LIVE_OPT_IN=true`, apoi deschiderea unui endpoint prin
`ApiLiveTransportCatalog`. După execuție, raportul poate genera cURL-ul de
replay pentru schimbul live; token-urile și header-ele sensibile rămân
redacted. Un test care nu declară explicit `live` nu are voie să deschidă
network-ul doar pentru că `baseUri` arată ca un endpoint public. Catalogul
respinge placeholder-ul acceptance și limitează request-urile la originea
endpoint-ului selectat.

## Cleanup și failure

- resursa creată se înregistrează imediat în registry-ul scenariului;
- teardown-ul se execută și când body-ul eșuează, dacă există ownership;
- failure-ul din cleanup nu ascunde failure-ul primar;
- statusul final respectă aceeași precedence peste tot;
- un test skip nu simulează un pass și nu publică assertions inventate.

## Verificarea raportului

După testele offline și rularea scenariului:

1. deschide artifactul HTML în TAB MCP;
2. verifică tab-urile în ordinea Tests → Category → Fails → Overview, cu
   Overview deschis implicit;
3. selectează un test pozitiv și unul cu warning/fail/skip;
4. expandează request-ul, trace-ul, JSON-ul și validation-ul;
5. confirmă că validation-ul este fratele request-ului și că assertions apar
   într-un singur tabel;
6. confirmă că JSON-ul este bloc de cod cu scroll intern și fără overflow al
   paginii;
7. verifică tag-urile în detaliile scenariului, nu ca zgomot în lista claselor;
8. verifică tab-ul Fails: o singură zonă de fails, sumar schematic și link către
   testul original;
9. confirmă că nu apar resurse externe sau folderul auxiliar extent.

Validarea vizuală este parte din testul de raportare. Build-ul verde singur nu
este suficient când schimbarea atinge DOM-ul sau CSS-ul.

