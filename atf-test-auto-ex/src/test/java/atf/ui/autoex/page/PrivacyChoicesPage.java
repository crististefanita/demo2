package atf.ui.autoex.page;

import atf.ui.autoex.support.AutoExE2eRun;

/** Explicitly declines optional site tracking when a fresh browser is prompted. */
public final class PrivacyChoicesPage extends AutoExPage {
    public PrivacyChoicesPage(AutoExE2eRun run) { super(run); }

    public void rejectOptionalProcessingIfPrompted() {
        run.dismissPrivacyPromptIfPresent();
    }
}
