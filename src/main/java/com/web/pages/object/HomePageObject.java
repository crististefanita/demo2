package com.web.pages.object;

import com.web.PageBase;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class HomePageObject extends PageBase {

    @FindBy(how = How.CSS, using = "input[class='gLFyf']")
    private WebElement inputSearch;

    @FindBy(how = How.XPATH, using = "//*[@id='nav']/li/a[text()=\"Clothing\"]")
    private WebElement topNavManuClothing;


    public HomePageObject(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

}
