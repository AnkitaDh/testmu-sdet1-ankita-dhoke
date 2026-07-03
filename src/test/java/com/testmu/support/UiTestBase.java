package com.testmu.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.File;
import java.time.Duration;

public abstract class UiTestBase {
    protected WebDriver driver;
    protected String loginUrl;
    protected String dashboardUrl;

    @BeforeEach
    void setUpBase() {
        driver = new HtmlUnitDriver(true);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        loginUrl = new File("src/test/resources/pages/login.html").getAbsoluteFile().toURI().toString();
        dashboardUrl = new File("src/test/resources/pages/dashboard.html").getAbsoluteFile().toURI().toString();
    }

    @AfterEach
    void tearDownBase() {
        if (driver != null) {
            driver.quit();
        }
    }
}
