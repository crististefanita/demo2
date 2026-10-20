package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.ui.practiceshop.config.PracticeShopScreen;

import java.util.List;

/** Immutable result of a page contract evaluation. */
public record PracticeShopPageValidationResult(PracticeShopScreen screen,
                                               List<AtfAssertion> assertions) {
    public PracticeShopPageValidationResult {
        assertions = assertions == null ? List.of() : List.copyOf(assertions);
    }

    public boolean passed() { return assertions.stream().allMatch(AtfAssertion::passed); }

    public String summary() {
        long passed = assertions.stream().filter(AtfAssertion::passed).count();
        return passed + "/" + assertions.size() + " validations passed for " + screen;
    }
}
