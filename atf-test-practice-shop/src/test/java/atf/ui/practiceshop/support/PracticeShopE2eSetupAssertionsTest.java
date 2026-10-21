package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopE2eSetupAssertionsTest {
    @Test
    void guestSetupSeparatesFixtureOwnershipFromWhetherTheJourneyConsumesIt() {
        PracticeShopConfiguration configuration = PracticeShopConfigurationLoader.load();

        List<AtfAssertion> assertions = PracticeShopE2eContext.setupAssertions(configuration);
        AtfAssertion fixturePolicy = assertion(assertions, "Account fixture policy");
        AtfAssertion accountSetup = assertion(assertions, "Account setup");

        assertTrue(assertions.stream().allMatch(AtfAssertion::passed));
        assertEquals(configuration.accountPolicy().expectedEvidence(), fixturePolicy.expected());
        assertEquals(fixturePolicy.expected(), fixturePolicy.actual());
        assertTrue(accountSetup.actual().contains("guest journey"));
        assertTrue(accountSetup.actual().contains("not consumed"));
    }

    @Test
    void missingOptionalFixtureDoesNotFailGuestSetupOrItsOwnershipPolicy() {
        PracticeShopConfiguration base = PracticeShopConfigurationLoader.load();
        Path missingFixture = Path.of("atf-test-practice-shop", "src", "test", "resources",
                "missing-optional-account.properties").toAbsolutePath().normalize();
        PracticeShopConfiguration guest = withAccountFile(base, missingFixture);

        List<AtfAssertion> assertions = PracticeShopE2eContext.setupAssertions(guest);

        assertTrue(assertions.stream().allMatch(AtfAssertion::passed));
        assertTrue(assertion(assertions, "Account setup").actual().contains("fixture missing"));
        assertTrue(assertion(assertions, "Account fixture policy").passed());
    }

    @Test
    void fixtureOutsideOwnedTestResourcesFailsOnlyTheOwnershipCheck(@TempDir Path temp) {
        PracticeShopConfiguration base = PracticeShopConfigurationLoader.load();
        PracticeShopConfiguration guest = withAccountFile(base, temp.resolve("account.properties"));

        List<AtfAssertion> assertions = PracticeShopE2eContext.setupAssertions(guest);

        AtfAssertion fixturePolicy = assertion(assertions, "Account fixture policy");
        assertFalse(fixturePolicy.passed());
        assertEquals("fixture path outside module-owned src/test/resources", fixturePolicy.actual());
        assertTrue(assertions.stream().filter(candidate -> !candidate.name().equals(fixturePolicy.name()))
                .allMatch(AtfAssertion::passed));
    }

    private static AtfAssertion assertion(List<AtfAssertion> assertions, String name) {
        return assertions.stream().filter(candidate -> candidate.name().equals(name))
                .findFirst().orElseThrow();
    }

    private static PracticeShopConfiguration withAccountFile(
            PracticeShopConfiguration base, Path accountFile) {
        return new PracticeShopConfiguration(base.baseUrl(), accountFile, base.productName(),
                base.productPath(), base.checkout(), base.browser(), base.uiRetry(),
                base.validationProfile(), base.api(), base.uiReportDirectory(),
                base.apiReportDirectory(), false, base.reporting());
    }
}
