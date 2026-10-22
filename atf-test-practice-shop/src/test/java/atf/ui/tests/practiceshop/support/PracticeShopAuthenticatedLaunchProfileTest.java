package atf.ui.tests.practiceshop.support;

import atf.api.core.config.ApiTransportMode;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopApiPrecondition;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopAuthenticatedLaunchProfileTest {
    private static final String LOCAL_FIXTURE =
            "atf-test-practice-shop/src/test/resources/practice-software-testing-account.local.properties";

    @Test
    void eachAuthenticatedClassResolvesItsOwnIntelliJProfileWithoutVmOverrides() throws Exception {
        assertAuthenticatedProfile("atf.ui.tests.practiceshop.e2e.PracticeShopAccountAndCatalogE2eTest");
        assertAuthenticatedProfile("atf.ui.tests.practiceshop.e2e.PracticeShopAuthenticatedPurchaseE2eTest");
    }

    @Test
    void missingFixtureExplainsHowToPrepareTheReusableLocalAccount(@TempDir Path temp) {
        var configuration = authenticatedConfiguration(temp.resolve(
                "src/test/resources/practice-software-testing-account.local.properties"));

        String reason = PracticeShopTestSupport.authenticatedFixtureSkipReason(configuration);

        assertNotNull(reason);
        assertTrue(reason.contains("fixture is missing"));
        assertTrue(reason.contains("registered account's email and password"));
    }

    @Test
    void malformedOrReservedFixtureSkipsWithoutLeakingCredentialValues(@TempDir Path temp) throws Exception {
        Path fixture = temp.resolve("src/test/resources/account.local.properties");
        Files.createDirectories(fixture.getParent());
        Files.writeString(fixture,
                "email=customer@example.test\npassword=never-print-this\n");

        String reason = PracticeShopTestSupport.authenticatedFixtureSkipReason(
                authenticatedConfiguration(fixture));

        assertAll(
                () -> assertNotNull(reason),
                () -> assertTrue(reason.contains("registered, non-reserved account email")),
                () -> assertFalse(reason.contains("never-print-this")));
    }

    @Test
    void incompleteFixtureExplainsRequiredKeysWithoutPrintingValues(@TempDir Path temp) throws Exception {
        Path fixture = temp.resolve("src/test/resources/account.local.properties");
        Files.createDirectories(fixture.getParent());
        Files.writeString(fixture, "email=customer@practicesoftwaretesting.com\n");

        String reason = PracticeShopTestSupport.authenticatedFixtureSkipReason(
                authenticatedConfiguration(fixture));

        assertNotNull(reason);
        assertTrue(reason.contains("lacks required email/password fields"));
        assertFalse(reason.contains("customer@practicesoftwaretesting.com"));
    }

    private static void assertAuthenticatedProfile(String testClassName) throws ClassNotFoundException {
        Class<?> testClass = Class.forName(testClassName);
        var configuration = PracticeShopTestSupport.configurationFor(testClass);

        assertAll(testClass.getSimpleName(),
                () -> assertTrue(configuration.authenticatedE2eEnabled()),
                () -> assertTrue(configuration.accountFile().endsWith(Path.of(LOCAL_FIXTURE))),
                () -> assertEquals(ApiTransportMode.LIVE, configuration.api().mode()),
                () -> assertEquals(PracticeShopApiPrecondition.AUTHENTICATED,
                        configuration.api().precondition()),
                () -> assertFalse(configuration.browser().headless()),
                () -> assertEquals(70, configuration.browser().zoomPercent()));
    }

    private static PracticeShopConfiguration authenticatedConfiguration(Path fixture) {
        return PracticeShopConfigurationLoader.load().forAuthenticatedE2e(fixture, false, 70);
    }
}
