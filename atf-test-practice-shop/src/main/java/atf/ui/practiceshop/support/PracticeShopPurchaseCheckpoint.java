package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.model.ReportExecutionEvidence;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/** Immutable page-level evidence contract emitted by the purchase flow. */
public record PracticeShopPurchaseCheckpoint(
        AtfGherkinKeyword keyword,
        String title,
        Collection<AtfAssertion> assertions,
        Collection<ReportExecutionEvidence> apiEvidence) {

    public PracticeShopPurchaseCheckpoint(AtfGherkinKeyword keyword, String title,
                                          Collection<AtfAssertion> assertions) {
        this(keyword, title, assertions, List.of());
    }

    public PracticeShopPurchaseCheckpoint {
        keyword = keyword == null ? AtfGherkinKeyword.THEN : keyword;
        title = title == null || title.isBlank() ? "Practice Shop page is valid" : title.trim();
        assertions = assertions == null ? List.of() : List.copyOf(assertions);
        apiEvidence = apiEvidence == null ? List.of() : List.copyOf(apiEvidence);
    }

    public PracticeShopPurchaseCheckpoint withAdditionalAssertions(Collection<AtfAssertion> additional) {
        List<AtfAssertion> merged = new ArrayList<>(assertions);
        if (additional != null) merged.addAll(additional);
        return new PracticeShopPurchaseCheckpoint(keyword, title, merged, apiEvidence);
    }

    public PracticeShopPurchaseCheckpoint withApiEvidence(
            Collection<ReportExecutionEvidence> additional) {
        List<ReportExecutionEvidence> merged = new ArrayList<>(apiEvidence);
        if (additional != null) merged.addAll(additional);
        return new PracticeShopPurchaseCheckpoint(keyword, title, assertions, merged);
    }

    public boolean passed() {
        return assertions.stream().allMatch(AtfAssertion::passed);
    }
}
