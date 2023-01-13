package com.stepdef.api.employee;

import com.api.action.AddEmployee;
import com.api.action.PutEmployee;
import com.api.context.TestContext;
import com.api.model.Employee;
import com.stepdef.basic.BasicTestApi;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.hamcrest.Matchers;

public class UpdateEmployee extends BasicTestApi {
    public UpdateEmployee(TestContext context) {
        super(context);
    }

    @Given("an employee record is already added")
    public void createRecord() {
        Response response = AddEmployee.addEmployee(context, Employee.randomBuilder().toString());
        assertTest.assertTest("Check status code", response.getStatusCode(), Matchers.is(200));
    }

    @Then("an employee with id {string} is updated and response status code {int} is returned")
    public void verifyUpdate(String id, int code) {
        Response response = PutEmployee.putCallMethod(context, id, Employee.randomBuilder().toString());
        assertTest.assertTest("Check status code", response.getStatusCode(), Matchers.is(code));
    }
}
