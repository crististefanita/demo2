package com.api.action;

import com.api.context.TestContext;
import com.api.helper.api.ApiCall;
import com.api.helper.api.RetrieveUrl;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j;

@Log4j
public class AddEmployee {

    private AddEmployee() {
        throw new IllegalStateException("Utility class");
    }

    public static Response addEmployee(TestContext context, String body) {
        Response response = null;
        try {
            response = ApiCall.post(context.requestSetup(), RetrieveUrl.retrieveUrl("createUrl"), body);
        } catch (Exception e) {
            context.scenario.log("Exception: " + e.getMessage());
            log.error("Exception: " + e.getMessage());
        }
        return response;
    }
}
