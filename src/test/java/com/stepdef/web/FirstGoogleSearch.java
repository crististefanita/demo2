package com.stepdef.web;

import com.api.helper.AssertTest;
import com.stepdef.basic.BaseTestWeb;
import com.web.action.flow.HomePageAction;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;

public class FirstGoogleSearch {
    BaseTestWeb base;
    private HomePageAction homePageAction;
    private AssertTest assertTest;

    public FirstGoogleSearch(BaseTestWeb base) {
        this.base = base;
        homePageAction = new HomePageAction(base.getDriver(), base.getScenario());
        assertTest = new AssertTest(base.getScenario());
    }

    @Given("an user opens {string}")
    public void anUserOpensUrl(String url) {
        base.getDriver().get(url);
        homePageAction.tryClosePopUp();
    }

    @Then("the page title opened is {string}")
    public void thePageTitleOpenedIs(String titlePage) {
        assertTest.assertTest("check page title", base.getDriver().getTitle(), Matchers.is(titlePage));
    }


    @When("user search by {string}")
    public void userSearchBy(String text) {
        homePageAction.searchByText(text);
    }


}
