package atf.ui.autoex.page;

import atf.ui.autoex.locator.AutoExSelectors;
import atf.ui.autoex.support.AutoExE2eRun;

import java.util.Locale;
import java.util.regex.Pattern;

/** Explicitly declines optional site tracking when a fresh browser is prompted. */
public final class PrivacyChoicesPage extends AutoExPage {
    private static final Pattern SAFE_CONTROL_ID = Pattern.compile("[A-Za-z][A-Za-z0-9_-]*");

    public PrivacyChoicesPage(AutoExE2eRun run) { super(run); }

    public void rejectOptionalProcessingIfPrompted() {
        if (!run.visible(AutoExSelectors.PRIVACY_DIALOG)) return;

        run.click(AutoExSelectors.PRIVACY_MANAGE_OPTIONS,
                "optional privacy purposes are reviewed");
        int selectedOptions = run.count(AutoExSelectors.PRIVACY_SELECTED_OPTIONS);
        for (int option = 0; option < selectedOptions; option++) {
            String controlId = run.firstAttribute(AutoExSelectors.PRIVACY_SELECTED_OPTIONS, "id");
            if (controlId == null || !SAFE_CONTROL_ID.matcher(controlId).matches()) {
                throw new IllegalStateException("Consent option has no safe associated label");
            }
            String label = String.format(Locale.ROOT, AutoExSelectors.PRIVACY_OPTION_LABEL, controlId);
            run.click(label,
                    "a preselected optional privacy purpose is declined");
        }
        if (run.count(AutoExSelectors.PRIVACY_SELECTED_OPTIONS) != 0) {
            throw new IllegalStateException("Optional privacy purposes remain selected");
        }

        run.click(AutoExSelectors.PRIVACY_CONFIRM_CHOICES,
                "required-only privacy choices are confirmed");
    }
}
