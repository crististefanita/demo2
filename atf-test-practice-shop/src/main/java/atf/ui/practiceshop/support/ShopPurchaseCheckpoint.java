package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.model.ReportExecutionEvidence;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/** Immutable page-level evidence contract emitted by the purchase flow. */
public record ShopPurchaseCheckpoint(
        AtfGherkinKeyword keyword,
        String title,
        Collection<AtfAssertion> assertions,
        Collection<ReportExecutionEvidence> apiEvidence) {

    public ShopPurchaseCheckpoint(AtfGherkinKeyword keyword, String title,
                                          Collection<AtfAssertion> assertions) {
        this(keyword, title, assertions, List.of());
    }

    public ShopPurchaseCheckpoint {
        keyword = keyword == null ? AtfGherkinKeyword.THEN : keyword;
        title = title == null || title.isBlank() ? "Practice Shop page is valid" : title.trim();
        assertions = assertions == null ? List.of() : List.copyOf(assertions);
        apiEvidence = apiEvidence == null ? List.of() : List.copyOf(apiEvidence);
    }

    public ShopPurchaseCheckpoint withAdditionalAssertions(Collection<AtfAssertion> additional) {
        List<AtfAssertion> merged = new ArrayList<>(assertions);
        if (additional != null) merged.addAll(additional);
        return new ShopPurchaseCheckpoint(keyword, title, merged, apiEvidence);
    }

    public ShopPurchaseCheckpoint withApiEvidence(
            Collection<ReportExecutionEvidence> additional) {
        List<ReportExecutionEvidence> merged = new ArrayList<>(apiEvidence);
        if (additional != null) merged.addAll(additional);
        return new ShopPurchaseCheckpoint(keyword, title, assertions, merged);
    }

    public boolean passed() {
        return assertions.stream().allMatch(AtfAssertion::passed);
    }
}
