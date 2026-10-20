package atf.ui.practiceshop.api.screen;

import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopApiSession;
import atf.ui.practiceshop.config.PracticeShopCredentials;

/** Login-screen API precondition; it is executed once and reused by later screens. */
public final class PracticeShopLoginApi {
    private final PracticeShopApiClient client;

    public PracticeShopLoginApi(PracticeShopApiClient client) {
        this.client = client;
    }

    public PracticeShopApiSession establish(PracticeShopCredentials credentials) {
        PracticeShopApiSession session = client.login(credentials);
        if (!session.authenticated()) {
            var loginResult = session.loginResult();
            var loginResponse = loginResult.response();
            throw new IllegalStateException("Practice Shop login did not return a bearer token; status="
                    + loginResponse.status());
        }
        return session;
    }
}
