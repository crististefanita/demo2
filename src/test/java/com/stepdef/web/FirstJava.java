package com.stepdef.web;

import com.stepdef.basic.BaseTestWeb;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;

public class FirstJava {
    BaseTestWeb base;

    public FirstJava(BaseTestWeb base) {
        this.base = base;
    }

    @When("I visit the homepage")
    public void i_visit_the_homepage() {
        base.getDriver().get("https://www.programsbuzz.com");
    }

}
