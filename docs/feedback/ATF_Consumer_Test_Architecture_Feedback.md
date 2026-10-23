# ATF — feedback activ pentru arhitectura consumerilor

Boundary-ul `main`/`test`, ownership-ul consumerilor, coeziunea pachetelor și
contractul comun al adnotărilor sunt descrise în README-urile ownerilor,
[arhitectura ATF](../ATF_Architecture.md),
[ghidul de testare](../ATF_Testing_Guide.md) și
[contractul adnotărilor](../ATF_Test_Infrastructure_Annotations.md). Din acest
document am eliminat cerințele de package split AutoEx și de creare a adnotării
comune: codul și proba nominală există. Rămâne un singur decalaj de integrare.

## CONSUMER-ANNOTATIONS-01 — precedență uniformă pentru UI evidence

**Stare: cod rezolvat și probat; documentație owner încă divergentă.**
`@AtfUiEvidence` există în `atf-test-kit`, iar AutoEx și Practice Shop declară
aceeași intenție typed. Configurația YAML oferă valorile implicite; valoarea
explicită selectată de consumator ajunge acum nemodificată la sesiunea Extent.
Conform instrucțiunii de a modifica numai feedback-urile, README-urile ownerilor
nu au fost rescrise în această intervenție; descrierile lor despre precedența
capturilor trebuie aduse la zi separat.

### Ce este deja implementat

- `AtfUiEvidence` declară separat `flow` și `screenshots`, are target `TYPE` și
  retenție `RUNTIME`; `CONFIGURED` lasă axa la profilul consumerului.
- `AtfUiEvidenceContractTest` verifică target-ul, retenția și valorile implicite.
- adaptorul AutoEx citește adnotarea clasei E2E și mapează ambele axe; testele
  componente acoperă fallback-ul configurat și override-urile independente.
- adaptorul Shop citește adnotarea clasei concrete, iar `reportProfile()` mapează
  ambele axe. `ShopE2eContext.open(...)` și `openReport(...)` nu mai rescriu
  profilul ales cu valoarea YAML. `ShopE2eContextEvidenceProfileTest` deschide
  sesiunea reală de raport într-un director temporar și verifică flow-ul
  unificat și politica explicită `NONE`, diferită de YAML. `@WithE2eHarness` a
  rămas locală pentru harness și `actionDetails`.
- AutoEx este inclus în inventarul surselor de producție, iar suportul său a fost
  împărțit pe pachete semantice. `AtfPackageCohesionGovernanceTest` verifică
  inventory-ul explicit al celor patru pachete `support`; această cerință este
  închisă și nu mai are backlog separat.

### Regresia de precedență în Shop — remediată

Mapperul Shop păstra override-ul explicit, dar `ShopE2eContext` îl suprascria
la deschiderea raportului standalone sau partajat. Am eliminat rescrierea din
ambele boundary-uri: profilul complet compus este acum autoritar, iar overload-ul
fără profil continuă să derive profilul YAML prin `ShopScreenshotPolicy`.
Testul verifică profilul observabil pe sesiunea Extent, nu numai mapperul.

### Contractul cerut

1. Aceeași precedență în ambii consumatori: configurația/YAML furnizează baza;
   `CONFIGURED` o păstrează; valoarea explicită din adnotare suprascrie numai
   axa declarată pentru clasa selectată.
2. Aplică selecția la ultimul boundary înainte de creare/publicare. Nicio
   normalizare ulterioară nu trebuie să reimpună YAML peste intenția explicită.
3. Păstrează axele independente: schimbarea `flow` nu schimbă capturile,
   schimbarea `screenshots` nu schimbă flow-ul, iar harness-ul și
   `actionDetails` rămân responsabilități locale Shop.
4. Nu adăuga resolver generic, lifecycle sau dependență Reporting în Test Kit.
   Adnotarea nu este `@Inherited`; clasele E2E declară intenția direct. Nu
   extinde cerința la meta-adnotări sau moștenire fără un consumer concret.
5. Păstrează izolarea profilurilor în raportul partajat al suitei. Aplicarea
   profilului per scenariu și între faze este urmărită separat în
   [`SHOP-CAPTURE-01`](ATF_Reporting_Feedback.md#shop-capture-01--profil-per-scenariu-și-flow-unificat-peste-faze),
   ca să nu dublăm același gap aici.

### Probe și criterii de închidere — îndeplinite

- Test Kit continuă să verifice exclusiv forma neutră a declarației, fără
  renderer, browser sau cod consumer.
- Testele componente AutoEx și Shop verifică mapping-ul axelor; testul de
  compoziție Shop suplimentar inspectează profilul efectiv din sesiunea Extent.
- Politica YAML rămâne default când adnotarea lipsește sau folosește
  `CONFIGURED`; politica explicită trece prin `open/openReport` fără a fi
  rescrisă.
- O probă de regresie confirmă că YAML-ul rămâne default când adnotarea lipsește
  sau folosește `CONFIGURED`, iar override-urile nu schimbă acțiunile,
  aserțiunile, verdictul ori journey-ul E2E.
- Rămâne sincronizarea descrierii de precedență din README-urile ownerilor;
  feedback-ul nu le modifică implicit.

Profilul selectat este verificat la sesiunea de raport pentru path-ul
`openReport`, folosit atât direct, cât și de proprietarul raportului partajat al
suitei. Testele E2E de suită rămân responsabile pentru izolarea per-scenariu a
profilurilor într-un singur raport; acel lifecycle este urmărit separat în
`SHOP-CAPTURE-01`. Așadar, regresia de cod este închisă, dar sincronizarea
documentară nu este declarată închisă.
