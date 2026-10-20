package atf.ui.tests.practiceshop.api.component;

import atf.ui.practiceshop.support.PracticeShopApiAssertions;
import atf.ui.practiceshop.support.PracticeShopApiNetworkJournal;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component contract for a JSON login response whose browser body is deferred. */
class PracticeShopApiLoginAssertionsTest {
    @Test
    void acceptsJsonTransportWhenPlaywrightCannotExposeCompressedBody() {
        var login = new PracticeShopApiNetworkJournal.Exchange(
                "POST", "/users/login", "https://api.practicesoftwaretesting.com/users/login", 200,
                "{\"email\":\"<redacted>\",\"password\":\"<redacted>\"}", "",
                Map.of("content-type", "application/json"), Map.of(
                "content-type", "application/json", "content-length", "384"));

        var assertions = PracticeShopApiAssertions.login(login, null, "ATF PracticeShop");

        assertTrue(assertions.stream().allMatch(assertion -> assertion.passed()),
                () -> assertions.toString());
    }
}
