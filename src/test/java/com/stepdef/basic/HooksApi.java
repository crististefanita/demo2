package com.stepdef.basic;

import com.api.context.TestContext;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class HooksApi {

    private static TestContext context;

    public HooksApi(TestContext context) {
        this.context = context;
    }


    @Before("@Api")
    public void setUp(Scenario scenario) {
        if (context == null) {
            context = new TestContext();
        }
        context.setScenario(scenario);
    }

}
