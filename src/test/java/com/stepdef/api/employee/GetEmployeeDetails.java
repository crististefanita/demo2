package com.stepdef.api.employee;

import com.api.action.GetEmployee;
import com.api.context.TestContext;
import com.stepdef.basic.BasicTestApi;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j;
import org.hamcrest.Matchers;


@Log4j
public class GetEmployeeDetails extends BasicTestApi {
    public GetEmployeeDetails(TestContext context) {
        super(context);
    }

    @Then("details of employees with status code {int} is received")
    public void fetchDetails(int code) {
        Response response = GetEmployee.callGetAllEmployees(context);
        assertTest.assertTest("Check status code", response.getStatusCode(), Matchers.is(code));
    }

    @Then("details of employee with id {string} and status code {int} is received")
    public void singleEmployee(String id, int code) {
        Response response = GetEmployee.callGetAnEmployee(context, id);
        assertTest.assertTest("Check status code", response.getStatusCode(), Matchers.is(code));
    }

    @Then("status code {int} is received")
    public void noRecord(int code) {
        Response response = GetEmployee.callGetAnEmployee(context, "");
        assertTest.assertTest("Check status code", response.getStatusCode(), Matchers.is(code));
    }
}

