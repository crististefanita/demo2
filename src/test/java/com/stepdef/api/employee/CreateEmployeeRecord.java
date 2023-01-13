package com.stepdef.api.employee;

import com.api.action.AddEmployee;
import com.api.context.TestContext;
import com.api.helper.json.JsonUtil;
import com.api.model.Employee;
import com.stepdef.basic.BasicTestApi;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.json.simple.JSONObject;

import static com.api.helper.json.JsonUtil.parseJson;

public class CreateEmployeeRecord extends BasicTestApi {
    public CreateEmployeeRecord(TestContext context) {
        super(context);
    }

    @Given("an employee record is added")
    public void insertEmployee() {
        JSONObject employeeJson = parseJson("./src/test/resources/payload/employee.json");
        Response response = AddEmployee.addEmployee(context, employeeJson.toString());
        assertTest.assertTest("Check status code", response.getStatusCode(), Matchers.is(200));
    }

    @Then("a random employee is added and status code {int} is returned")
    public void responseVerify(int code) {
        Response response = AddEmployee.addEmployee(context, JsonUtil.toStringJson(Employee.randomBuilder()));
        assertTest.assertTest("Check status code", response.getStatusCode(), Matchers.is(code));
    }

}

