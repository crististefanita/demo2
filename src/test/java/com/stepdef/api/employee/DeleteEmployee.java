package com.stepdef.api.employee;

import com.api.context.TestContext;
import com.api.helper.AssertTest;
import com.stepdef.basic.BasicTestApi;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.hamcrest.Matchers;

public class DeleteEmployee extends BasicTestApi {
    public DeleteEmployee(TestContext context) {
        super(context);
    }
    @Then("an employee id {string} is deleted and response status code {int} is returned")
    public void verifyStatus(String id, int statusCode) {
        Response response = com.api.action.DeleteEmployee.deleteEmployee(context, id);
        assertTest.assertTest("Check status code", response.getStatusCode(), Matchers.is(statusCode));
    }

    @Given("an employee record is not added")
    public void noUserAdded() {
    }

}
