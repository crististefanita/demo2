package atf.ui.tests.extent.support;

import atf.api.ui.config.UiConfiguration;

/** Resolves one immutable report input from the active UI configuration. */
@FunctionalInterface
public interface ReportAuditInputSource {
    ReportAuditInput resolve(UiConfiguration configuration);
}
