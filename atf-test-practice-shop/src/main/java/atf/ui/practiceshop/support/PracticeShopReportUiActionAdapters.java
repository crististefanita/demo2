package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.ui.AtfUiActionRecord;
import atf.api.reporting.ui.AtfUiEvidenceSource;

import java.util.Collection;
import java.util.List;

/** Consumer-owned bridge from core UI actions to the renderer-neutral report contract. */
final class PracticeShopReportUiActionAdapters {
    private PracticeShopReportUiActionAdapters() { }

    static List<AtfUiActionRecord> from(
            Collection<? extends atf.api.core.ui.AtfUiActionRecord> actions) {
        if (actions == null || actions.isEmpty()) return List.of();
        return actions.stream().map(action -> new AtfUiActionRecord(
                action.sequence(), action.operation(), action.target(), action.expected(),
                action.observed(), action.durationMillis(), status(action.status()),
                AtfUiEvidenceSource.valueOf(action.source().name()))).toList();
    }

    private static AtfReportStatus status(atf.api.core.reporting.contract.AtfReportStatus status) {
        return AtfReportStatus.valueOf(status.name());
    }
}
