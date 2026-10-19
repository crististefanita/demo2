# ATF Tests — guvernanță la nivel de proiect

`atf-tests` este owner-ul testelor care guvernează întregul ATF, nu owner-ul
testelor API/UI sau al infrastructurii de reporting. Pachetul dedicat este:

```text
src/test/java/atf/api/tests/governance/
└── ArchitectureModuleBoundaryTest.java
```

Acest test verifică global catalogul reactorului din root POM, direcția Maven,
importurile interzise, ownership-ul output-ului de proces și starea statică a modulelor. El nu verifică un singur
artifact și nu trebuie mutat într-un modul de producție doar pentru că acel
modul îl execută convenabil.

## Ce rămâne în `atf-reporting`

`atf-reporting` păstrează numai contractele proprii reporting-ului și testele
care îi verifică production/test boundary-ul:

```text
atf-reporting/src/test/java/atf/api/tests/architecture/
├── ArchitectureOwnershipBoundaryTest.java
├── ArchitectureCohesionBoundaryTest.java
├── ApiTestBoundaryArchitectureTest.java
└── ...
```

`ArchitectureOwnershipBoundaryTest` nu guvernează reactorul. El verifică faptul
că modelele, publisher-ele, audit kernel-ul, test-kit-ul UI și configurația de
reporting rămân în ownerii corecți.

## Constantele modulelor

Identificatorii Maven nu se repetă în testele de guvernanță. Owner-ul neutru este
[`AtfModuleIds`](../atf-core/src/main/java/atf/api/core/config/module/AtfModuleIds.java),
din `atf-core`:

```java
AtfModuleIds.CORE       // atf-core
AtfModuleIds.REPORTING  // atf-reporting
AtfModuleIds.TESTS      // atf-tests
```

Acest vocabular este folosit și de testele de arhitectură din reporting. Nu se
introduc copii locale ale listelor de module sau string-uri Maven repetate.
`REACTOR_MODULES` este lista completă, în ordinea root POM. `SOURCE_GOVERNED_MODULES`
exclude explicit `atf-test-kit` doar din scanarea globală a surselor, deoarece
test-kit-ul își verifică propriul layer de teste prin
`AtfTestLayerArchitectureTest`; nu este exclus din reactor și nu devine
neguvernat.
`AtfModule` rămâne enum-ul pentru selecția logică runtime (`core`, `reporting`,
`ui`); `AtfModuleIds` este vocabularul fizic al reactorului.

## Rulare

Guvernanța globală se rulează explicit în staging extern proiectului:

```text
mvn -pl atf-tests -am -Dtest=ArchitectureModuleBoundaryTest test
```

Testele API/UI și contractele Extent rămân în:

```text
mvn -pl atf-reporting -am test
```

Build-ul de lucru se face într-un staging extern, fără oprirea rulărilor
existente. Pentru schimbări de reporting, artifactele se promovează numai după
verificarea prin ATF Debug și browser MCP; browserul Windows real nu face parte
din protocol.

## Regula de ownership

```text
ATF întreg / reactor / import graph / Maven direction → atf-tests
core models, ports și vocabulary fizic → atf-core
Extent, HTML, audit și UI report contracts → atf-reporting
API/UI executable scenarios și browser flows → atf-reporting/src/test
```

Nu se creează un al doilea registru de guvernanță în reporting și nu se mută
fixture-uri, locatori sau publisher-e în `atf-tests`.

Detaliile de layering și criteriile de promovare sunt în
[`docs/feedback/ATF_Test_Layering_Feedback.md`](../docs/feedback/ATF_Test_Layering_Feedback.md),
iar harta arhitecturală este în
[`docs/ATF_Architecture.md`](../docs/ATF_Architecture.md).
