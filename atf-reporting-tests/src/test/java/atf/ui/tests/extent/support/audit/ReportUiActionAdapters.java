package atf.ui.tests.extent.support.audit;

import atf.api.reporting.api.ReportModelAdapters;
import atf.api.reporting.ui.AtfUiActionRecord;
import java.util.Collection;
import java.util.List;

/** Test-composition adapter: Playwright/core action records become renderer evidence records. */
public final class ReportUiActionAdapters {
    private ReportUiActionAdapters() { }

    public static List<AtfUiActionRecord> from(Collection<? extends atf.api.core.ui.AtfUiActionRecord> actions) {
        return ReportModelAdapters.uiActions(actions);
    }
}
