package atf.api.core.ui;

import atf.api.core.reporting.AtfReportStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtfUiActionRecordTest {
    @Test
    void keepsOrderedActionContractAndUsesCanonicalPassLabel() {
        var action = new AtfUiActionRecord(1, "Click", "role=button[name=Save]",
                "save dialog opens", "click completed", 547, AtfReportStatus.PASS);

        assertAll(
                () -> assertEquals("547 ms", action.durationText()),
                () -> assertEquals("Pass", action.resultLabel()),
                () -> assertEquals(AtfReportStatus.PASS, action.status()));
    }

    @Test
    void rejectsInvalidActionIdentityAndDuration() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AtfUiActionRecord(0, "Click", "target", "expected", "observed", 1, AtfReportStatus.PASS)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AtfUiActionRecord(1, "", "target", "expected", "observed", 1, AtfReportStatus.PASS)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AtfUiActionRecord(1, "Click", "target", "expected", "observed", -1, AtfReportStatus.PASS)));
    }
}
