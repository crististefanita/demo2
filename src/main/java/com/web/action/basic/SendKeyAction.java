package com.web.action.basic;

import io.cucumber.java.Scenario;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SendKeyAction {
    public static WebElement sendKey(Scenario scenario, WebDriver driver, String locatorCSS, String text) {
        WebElement element = FindElementAction.waitForElement(scenario, driver, locatorCSS);
        element.sendKeys(text);
        return element;
    }

    public static WebElement sendKeyWithEnter(Scenario scenario, WebDriver driver, String locatorCSS, String text) {
        WebElement element = FindElementAction.waitForElement(scenario, driver, locatorCSS);
        scenario.log("[DEBUG] sendKeys to " + locatorCSS + " => " + text);
        element.sendKeys(text);
        scenario.log("[DEBUG] sendKeys to " + locatorCSS + " => Keys.ENTER");
        element.sendKeys(Keys.ENTER);
        return element;
    }
}
