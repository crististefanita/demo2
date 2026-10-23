package atf.ui.practiceshop.support.reporting;

import atf.api.reporting.api.ReportModelAdapters;
import atf.api.reporting.ui.model.AtfUiActionRecord;

import java.util.Collection;
import java.util.List;

/** Consumer-owned bridge from core UI actions to the renderer-neutral report contract. */
public final class ShopReportUiActionAdapters {
    private ShopReportUiActionAdapters() { }

    public static List<AtfUiActionRecord> from(
            Collection<? extends atf.api.core.ui.AtfUiActionRecord> actions) {
        return ReportModelAdapters.uiActions(actions);
    }
}
