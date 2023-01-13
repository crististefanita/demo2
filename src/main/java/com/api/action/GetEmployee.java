package com.api.action;

import com.api.context.TestContext;
import com.api.helper.api.ApiCall;
import com.api.helper.api.RetrieveUrl;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j;

import java.util.concurrent.TimeUnit;

@Log4j
public class GetEmployee {
    private GetEmployee() {
        throw new IllegalStateException("Utility class");
    }

    public static Response callGetAnEmployee(TestContext context, String employeeId) {
        Response response = ApiCall.get(context.requestSetup(), RetrieveUrl.retrieveUrl("getEmployeeUrl") + employeeId);

        context.scenario.log("time milliseconds: " + response.timeIn(TimeUnit.MILLISECONDS));
        context.scenario.log("---");

        return response;
    }

    public static Response callGetAllEmployees(TestContext context) {
        Response response = ApiCall.get(context.requestSetup(), RetrieveUrl.retrieveUrl("getAllEmployee"));

        context.scenario.log("time milliseconds: " + response.timeIn(TimeUnit.MILLISECONDS));
        context.scenario.log("---");
        return response;
    }
}
