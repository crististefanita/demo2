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

    @Test
    void registerPerRunCannotSilentlyActivateOnAnotherAuthenticatedClass() {
        var configuration = PracticeShopTestSupport.configurationFor(NonPurchaseAuthenticatedTest.class);

        String reason = PracticeShopTestSupport.authenticatedSetupSkipReason(
                NonPurchaseAuthenticatedTest.class, configuration);

        assertNotNull(reason);
        assertTrue(reason.contains("@AllowsRegistrationPerRun"));
    }

    @Test
    void registrationCapabilityIsAnExplicitClassContract() throws ClassNotFoundException {
        Class<?> purchase = Class.forName(
                "atf.ui.tests.practiceshop.e2e.PracticeShopAuthenticatedPurchaseE2eTest");

        assertTrue(purchase.isAnnotationPresent(PracticeShopTestSupport.AllowsRegistrationPerRun.class));
        Class<?> accountAndCatalog = Class.forName(
                "atf.ui.tests.practiceshop.e2e.PracticeShopAccountAndCatalogE2eTest");
        assertTrue(accountAndCatalog.isAnnotationPresent(
                PracticeShopTestSupport.AllowsRegistrationPerRun.class));
        assertFalse(NonPurchaseAuthenticatedTest.class.isAnnotationPresent(
                PracticeShopTestSupport.AllowsRegistrationPerRun.class));
    }

    @Test
    void registrationProfileMustUseAnIgnoredLocalPropertiesFile(@TempDir Path temp) {
        Path valid = temp.resolve(
                "project/atf-test-practice-shop/src/test/resources/registration.local.properties");
        Path wrongModule = temp.resolve(
                "project/atf-test-kit/src/test/resources/registration.local.properties");
        Path wrongExtension = temp.resolve(
                "project/atf-test-practice-shop/src/test/resources/registration.properties");

        var moduleFailure = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> PracticeShopConfigurationLoader.resolveRegistrationProfileFile(
                        wrongModule.toString()));
        var extensionFailure = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> PracticeShopConfigurationLoader.resolveRegistrationProfileFile(
                        wrongExtension.toString()));

        assertAll(
                () -> assertEquals(valid.toAbsolutePath().normalize(),
                        PracticeShopConfigurationLoader.resolveRegistrationProfileFile(valid.toString())),
                () -> assertTrue(moduleFailure.getMessage().contains("atf-test-practice-shop/src/test/resources")),
                () -> assertTrue(extensionFailure.getMessage().contains(".local.properties")));
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

    @PracticeShopTestSupport.AuthenticatedE2e(
            accountSetup = PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN)
    static class NonPurchaseAuthenticatedTest { }
}
