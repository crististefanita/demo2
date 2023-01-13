package com.web.action.flow;

import com.web.action.basic.FindElementAction;
import com.web.action.basic.SendKeyAction;
import com.web.pages.locators.HomePage;
import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HomePageAction {
    private WebDriver driver;
    private Scenario scenario;

    public HomePageAction(WebDriver driver, Scenario scenario) {
        this.driver = driver;
        this.scenario = scenario;
    }

    public void searchByText(String text) {
        scenario.log("searchByText: " + text);
        SendKeyAction.sendKeyWithEnter(scenario, driver, HomePage.INPUT_SEARCH, text);
    }

    public void tryClosePopUp() {
        WebElement element = FindElementAction.waitForElement(scenario, driver, HomePage.BUTTON_ACCEPT_ALL);
        if (element.isDisplayed()) {
            scenario.log("closePopUp");
            element.click();
        }
    }

}
