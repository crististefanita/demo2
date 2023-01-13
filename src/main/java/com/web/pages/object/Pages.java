package com.web.pages.object;

import org.openqa.selenium.WebDriver;

public class Pages {
    private WebDriver driver;

    private HomePageObject homePage;

    public Pages(WebDriver driver) {
        this.driver = driver;
    }

    public HomePageObject homePage() {
        if (homePage == null) {
            homePage = new HomePageObject(driver);
        }
        return homePage;
    }


}
