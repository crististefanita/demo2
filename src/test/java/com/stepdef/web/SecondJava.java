package com.stepdef.web;

import com.stepdef.basic.BaseTestWeb;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;

public class SecondJava {
    BaseTestWeb base;

    public SecondJava(BaseTestWeb base) {
        this.base = base;
    }

    @When("I click on login link")
    public void i_click_on_login_link() {
        base.getDriver().findElement(By.linkText("LOG IN")).click();
    }
}
