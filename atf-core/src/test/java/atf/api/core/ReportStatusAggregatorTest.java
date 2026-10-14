package atf.api.core;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.reporting.ReportStatusAggregator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportStatusAggregatorTest {
    @Test
    void failureWinsOverSkipWarningAndPassRegardlessOfObservationOrder() {
        var aggregator = new ReportStatusAggregator();
        aggregator.record(AtfReportStatus.WARNING);
        aggregator.record(AtfReportStatus.PASS);
        aggregator.record(AtfReportStatus.SKIP);
        aggregator.record(AtfReportStatus.FAIL);

        assertEquals(AtfReportStatus.FAIL, aggregator.aggregate());
        assertEquals(1, aggregator.count(AtfReportStatus.WARNING));
        assertTrue(aggregator.hasWarnings());
    }

    @Test
    void skippedLifecycleRemainsStrongerThanWarningButWeakerThanFailure() {
        var aggregator = new ReportStatusAggregator();
        aggregator.record(AtfReportStatus.WARNING);
        aggregator.record(AtfReportStatus.SKIP);
        assertEquals(AtfReportStatus.SKIP, aggregator.aggregate());
    }
}
