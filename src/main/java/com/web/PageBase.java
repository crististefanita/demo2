package com.web;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.util.concurrent.TimeUnit;

public class PageBase {

    protected WebDriver driver;

    public PageBase(WebDriver driver) {
        this.driver = driver;
    }

    public void delayFor(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public WebElement waitForElement(final By locator) {
        return waitForElement(locator, 1000);
    }

    public WebElement waitForElement(final By locator, int timeToWait) {
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .withTimeout(timeToWait, TimeUnit.SECONDS)
                .pollingEvery(100, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class);

        return wait.until(driver -> {
            WebElement element;
            element = driver.findElement(locator);
            highlightElement(element);
            return element;
        });
    }

    public WebElement waitForElementDisplayed(final By locator) {
        return waitForElementDisplayed(locator, 1000);
    }

    public WebElement waitForElementDisplayed(final By locator,
                                              final int timeToWaitInSec) {
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .withTimeout(timeToWaitInSec, TimeUnit.SECONDS)
                .pollingEvery(100, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class);

        return wait.until(driver -> {
            WebElement element = driver.findElement(locator);
            if (element.isDisplayed()) {
                highlightElement(element);
                return element;
            }
            return null;
        });
    }

    public WebElement waitForElementText(final By locator, String text) {
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .withTimeout(30, TimeUnit.SECONDS)
                .pollingEvery(100, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class);

        return wait.until(driver -> {
            WebElement element = driver.findElement(locator);
            if (element.getText().contentEquals(text)) {
                highlightElement(element);
                return element;
            }
            return null;
        });
    }

    public WebElement waitForElementNotDisplayed(final By locator) {
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .withTimeout(60, TimeUnit.SECONDS)
                .pollingEvery(100, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class);

        return wait.until(driver -> {
            WebElement element = driver.findElement(locator);
            if (element.isDisplayed()) {
                return null;
            }
            return element;
        });
    }

    public void jsClick(WebElement element) {
        // ((JavascriptExecutor) driver).executeScript("arguments[0].click()",
        // element);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        highlightElement(element);
        js.executeScript("arguments[0].click()", element);
    }

    public JavascriptExecutor jsExecutor() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return js;
    }

    public void highlightElement(WebElement element) {
        for (int i = 0; i < 2; i++) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    "arguments[0].setAttribute('style', arguments[1]);",
                    element, "color: red; border: 2px solid red;");
            delayFor(200);
            js.executeScript(
                    "arguments[0].setAttribute('style', arguments[1]);",
                    element, "");
        }
    }

    public void hoverItem(WebElement element) {
        Actions actions = new Actions(driver);
        actions.moveToElement(element);
        actions.perform();
    }

    public void hoverItem(By by) {
        WebElement element = driver.findElement(by);
        hoverItem(element);
    }

    public void hoverItemEx(WebElement element) {
        Locatable hoverItem = (Locatable) element;
        Mouse mouse = ((HasInputDevices) driver).getMouse();
        mouse.mouseMove(hoverItem.getCoordinates());
    }

    public void hoverItemEx(By by) {
        WebElement element = driver.findElement(by);
        hoverItemEx(element);
    }


}
