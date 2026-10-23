package atf.ui.autoex.page;

import atf.ui.autoex.support.journey.AutoExPurchaseSession;

/** Explicitly declines optional site tracking when a fresh browser is prompted. */
public final class PrivacyChoicesPage extends AutoExPage {
    public PrivacyChoicesPage(AutoExPurchaseSession run) { super(run); }

    public void rejectOptionalProcessingIfPrompted() {
        run.dismissPrivacyPromptIfPresent();
    }
}
