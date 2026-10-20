package atf.ui.tests.extent.support.audit;

import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.ui.AtfUiActionRecord;
import java.util.Collection;
import java.util.List;

/** Test-composition adapter: Playwright/core action records become renderer evidence records. */
public final class ReportUiActionAdapters {
    private ReportUiActionAdapters() { }

    public static List<AtfUiActionRecord> from(Collection<? extends atf.api.core.ui.AtfUiActionRecord> actions) {
        if (actions == null || actions.isEmpty()) return List.of();
        return actions.stream().map(action -> new AtfUiActionRecord(
                action.sequence(), action.operation(), action.target(), action.expected(),
                action.observed(), action.durationMillis(), status(action.status()),
                atf.api.reporting.ui.AtfUiEvidenceSource.valueOf(action.source().name()))).toList();
    }

    private static AtfReportStatus status(atf.api.core.reporting.contract.AtfReportStatus status) {
        return AtfReportStatus.valueOf(status.name());
    }
}
