package com.stepdef.basic;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class HooksUI {
    BaseTestWeb base;

    public HooksUI(BaseTestWeb base) {
        this.base = base;
    }

    @Before("@UI")
    public void setUp(Scenario scenario) {
        if (base.getDriver() == null) {
            base.setDriver();
        }
        base.scenario = scenario;

    }

    @After("@UI")
    public void clean() {
        base.getDriver().close();
        base.getDriver().quit();
    }
}
