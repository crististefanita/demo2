# AutoEx — compatibilitatea business rămasă

Contractele stabile pentru vocabularul UI/API, validarea căutării și ownership-ul
fazelor sunt în [README-ul AutoEx](../../atf-test-auto-ex/README.md#vocabular-și-adrese).
Feedback-ul păstrează numai lacunele business încă neînchise.

Activarea Close opt-in la 70% și contractul său de evidence sunt documentate în
[README AutoEx](../../atf-test-auto-ex/README.md#vignette-și-activare-javascript).
Limita pointerului rămâne în [feedback-ul generic UI](ATF_UI_Browser_Geometry_Feedback.md).

## AUTOEX-DELETE-AFTER-AD-01 — confirmarea ștergerii după reclamă

**Prioritate:** 88/100 = impact 36 + lacună 26 + lucrabil 20 + documentare 6,
conform [registrului comun](README.md#top-feedback-uri-active).

O rulare UI_API la 100% a trecut checkout-ul și factura, dar după Close la
ștergerea contului a ajuns pe Home. `accountDeletionResult()` a găsit două
headings `h2.title.text-center`, iar testul a rămas FAIL. Cleanup-ul de
recuperare a revenit la `/delete_account` și a confirmat ștergerea.

Rulările UI și UI_API la 70% trec cu activarea DOM Close opt-in, dar nu au
reprodus reclama exact în tranziția Delete account. Nu închid acest gap pe
baza succesului unei alte tranziții sau a dispariției overlay-ului.

- [ ] Corelați rezultatul ștergerii cu ruta și confirmarea business după Close,
      fără `first()` care ar accepta un heading Home și fără mascarea erorii.
- [ ] Probați tranziția în componenta business AutoEx și pe site-ul real;
      păstrați failure evidence înainte de recuperare.

### Increment în verificare — continuare fără replay de ștergere

- Observarea pasivă începe înainte de click și numără request-urile către
  endpoint-ul de ștergere; un request pornit interzice reluarea, indiferent
  de răspuns sau de dispariția reclamei.
- Numai reclama închisă fără request și fără destinație permite o singură
  continuare; înainte se reîncarcă Home și se verifică identitatea exactă și
  disponibilitatea Delete account, fără a reexecuta plata sau crearea contului.
- Confirmarea cere ruta exactă, heading-ul dedicat `account-deleted` vizibil
  și un singur request de ștergere; Home nu poate satisface acest contract.
- Proba de componentă este o decizie business pură, fără browser sau reclamă
  fabricată; proba integrată trebuie să observe întreruperea naturală pe site.
- Guvernanța păstrează inventarul exact al pachetului account: cele trei clase
  existente plus observatorul scoped și regula de progres adăugate pentru
  această cerință. Se actualizează numai setul așteptat, fără relaxarea pragului
  de opt clase, wildcard-uri sau excepții noi.

```yaml
testChanges:
  - id: AUTOEX-DELETE-AFTER-AD-01
    status: ACTIVE
    base: 7b365028ac8356960614e35023556e757311e633
    expires: 2026-11-23
    reason: Verifica decizia business de continuare a navigarii numai dupa reclama inchisa fara request de stergere pornit; nu fabrica reclame si nu permite replay dupa un request inceput.
    proof: mvn "-pl=atf-test-auto-ex,atf-tests" -am "-Dtest=AutoExComponentSuite,AutoExAccountUiE2eTest,AutoExBusinessSuite,AutoExPurchaseUiApiE2eTest,WholeAtfGovernanceSuiteTest" "-Datf.autoex.e2e.enabled=true" "-Datf.governance.testChanges.baseRef=7b365028ac8356960614e35023556e757311e633" "-Dsurefire.failIfNoSpecifiedTests=false" test
    paths:
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/component/journey/AutoExAccountDeletionSafetyComponentTest.java
      - atf-test-auto-ex/src/test/java/atf/ui/tests/autoex/e2e/AutoExAccountUiE2eTest.java
      - atf-tests/src/test/java/atf/api/tests/governance/AtfPackageCohesionGovernanceTest.java
```
