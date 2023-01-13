package com.web.action.basic;

import com.aventstack.extentreports.ExtentTest;
import io.cucumber.java.Scenario;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class FindElementAction {

    public static WebElement waitForElement(Scenario scenario, WebDriver driver, String locatorCSS) {
        return waitForElement(scenario, driver, locatorCSS, 1000);
    }

    public static WebElement waitForElement(Scenario scenario, WebDriver driver, String locatorCSS, int timeToWait) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(timeToWait, TimeUnit.SECONDS)
                .pollingEvery(100, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class);

        scenario.log("[DEBUG] waitForElement => " + locatorCSS);

        return wait.until(driver1 -> {
            WebElement element;
            element = driver1.findElement(By.cssSelector(locatorCSS));
            ClickElementAction.highlightElement(driver1, element);
            return element;
        });

    }


}
