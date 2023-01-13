package com.stepdef.basic;

import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.concurrent.TimeUnit;

public class BaseTestWeb {
    private WebDriver driver;

    private Scenario scenario;

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\work\\new\\driver\\chromedriver.exe");
        if (driver == null) {
            driver = new ChromeDriver();
        }
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }
}
