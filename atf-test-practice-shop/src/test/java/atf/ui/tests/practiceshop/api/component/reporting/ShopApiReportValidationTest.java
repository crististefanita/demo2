package atf.ui.tests.practiceshop.api.component.reporting;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.ShopApiReportValidation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ShopApiReportValidationTest {
    @Test
    void businessValidationAggregatesEveryAssertion() {
        var passing = new ShopApiReportValidation("live business contract", List.of(
                new AtfAssertion("status", "200", "200", true),
                new AtfAssertion("identity", "Pliers", "Pliers", true)));
        var failing = new ShopApiReportValidation("live business contract", List.of(
                new AtfAssertion("status", "200", "401", false)));

        assertTrue(passing.passed());
        assertFalse(failing.passed());
    }
}
