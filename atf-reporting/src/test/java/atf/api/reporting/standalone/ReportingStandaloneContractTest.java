package atf.api.reporting.standalone;

import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.duration.AtfDurationFormatter;
import atf.api.reporting.security.SensitiveDataRedactor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Proves the reporting module can test its public contracts without ATF modules. */
class ReportingStandaloneContractTest {
    @Test
    void typedStatusAndDurationContractsAreAvailableStandalone() {
        assertEquals(AtfReportStatus.PASS, AtfReportStatus.valueOf("PASS"));
        assertEquals("12 ms", AtfDurationFormatter.format(java.time.Duration.ofMillis(12)));
    }

    @Test
    void sensitiveValuesAreRedactedWithoutAnAtfAdapter() {
        String safe = new SensitiveDataRedactor().redactJson("{\"token\":\"secret-value\",\"ok\":true}");
        assertFalse(safe.contains("secret-value"));
        assertEquals("{\"token\":\"***REDACTED***\",\"ok\":true}", safe);
    }
}
