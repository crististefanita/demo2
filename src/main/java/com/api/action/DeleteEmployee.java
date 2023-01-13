package com.api.action;

import com.api.context.TestContext;
import com.api.helper.api.RetrieveUrl;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;

public class DeleteEmployee {
    private DeleteEmployee() {
        throw new IllegalStateException("Utility class");
    }

    public static Response deleteEmployee(TestContext context, String id) {
        return context.requestSetup()
                .delete(((RetrieveUrl.retrieveUrl("deleteUrl")) + id));
    }

}
