package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopCredentialsTest {
    @Test
    void loadsCredentialsAndNeverRedactsPasswordIntoToString(@TempDir Path temp) throws Exception {
        Path file = temp.resolve("account.properties");
        Files.writeString(file, "email=customer@example.test\npassword=Secret!123\nfirstName=ATF\nlastName=E2E\n");

        PracticeShopCredentials credentials = PracticeShopCredentials.load(file);

        assertTrue(credentials.email().contains("@"));
        assertTrue(credentials.displayName().equals("ATF E2E"));
        assertFalse(credentials.toString().contains("Secret!123"));
    }

    @Test
    void rejectsMissingPasswordInsteadOfStartingAnAmbiguousExternalRun(@TempDir Path temp) throws Exception {
        Path file = temp.resolve("account.properties");
        Files.writeString(file, "email=customer@example.test\nfirstName=ATF\nlastName=E2E\n");

        assertThrows(IllegalArgumentException.class, () -> PracticeShopCredentials.load(file));
    }
}


