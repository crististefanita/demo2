package atf.ui.practiceshop.api;

import atf.api.core.http.response.ApiExecutionResult;

/** Authenticated API precondition produced by the login screen boundary. */
public record PracticeShopApiSession(String bearerToken, ApiExecutionResult loginResult) {
    public PracticeShopApiSession {
        bearerToken = bearerToken == null ? "" : bearerToken;
    }

    public boolean authenticated() { return !bearerToken.isBlank(); }
}
