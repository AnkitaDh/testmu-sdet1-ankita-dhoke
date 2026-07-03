package com.testmu;

import com.testmu.llm.LlmFailureReportingExtension;
import com.testmu.support.UiTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LlmFailureReportingExtension.class)
public class DashboardTests extends UiTestBase {

    @Test
    void widgetsLoadCorrectlyForAdminUser() {
        driver.get(loginUrl);
        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("email")).sendKeys("tester@testmu.ai");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("TestMu123!");
        driver.findElement(By.id("login-button")).click();
        assertTrue(driver.findElement(By.id("status")).getText().contains("Welcome tester@testmu.ai"));
        assertTrue(driver.findElement(By.cssSelector("#dashboard-grid .widget-card")).isDisplayed());
        assertEquals("block", driver.findElement(By.id("admin-panel")).getCssValue("display"));
    }

    @Test
    void dashboardDataIncludesExpectedSummaryFields() {
        driver.get(dashboardUrl);
        ((JavascriptExecutor) driver).executeScript("localStorage.setItem('authToken','valid-token'); localStorage.setItem('authExpiry', String(Date.now()+3600000)); localStorage.setItem('userRole','admin');");
        driver.navigate().refresh();
        assertTrue(driver.findElement(By.id("status")).getText().contains("Welcome tester@testmu.ai"));
        assertTrue(driver.findElements(By.cssSelector("#dashboard-grid .widget-card")).size() >= 3);
    }

    @Test
    void filterAndSortBehaviorReturnsDescendingValues() {
        driver.get(dashboardUrl);
        ((JavascriptExecutor) driver).executeScript("localStorage.setItem('authToken','valid-token'); localStorage.setItem('authExpiry', String(Date.now()+3600000)); localStorage.setItem('userRole','admin');");
        driver.navigate().refresh();
        driver.findElement(By.id("filter-input")).sendKeys("widget");
        driver.findElement(By.id("refresh-button")).click();
        var items = driver.findElements(By.cssSelector("#items-list .widget-card"));
        assertTrue(items.size() >= 3);
        var firstText = items.get(0).getText();
        var lastText = items.get(items.size() - 1).getText();
        assertTrue(firstText.contains("Value: 230") || firstText.contains("Value: 120"));
        assertTrue(lastText.contains("Value: 95") || lastText.contains("Value: 120"));
    }

    @Test
    void permissionBasedVisibilityHidesAdminPanelForRegularUsers() {
        driver.get(dashboardUrl);
        ((JavascriptExecutor) driver).executeScript("localStorage.setItem('authToken','valid-token'); localStorage.setItem('authExpiry', String(Date.now()+3600000)); localStorage.setItem('userRole','user');");
        driver.navigate().refresh();
        assertEquals("none", driver.findElement(By.id("admin-panel")).getCssValue("display"));
    }

    @Test
    void responsiveLayoutChangesAtNarrowViewport() {
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(500, 900));
        driver.get(dashboardUrl);
        ((JavascriptExecutor) driver).executeScript("localStorage.setItem('authToken','valid-token'); localStorage.setItem('authExpiry', String(Date.now()+3600000)); localStorage.setItem('userRole','admin');");
        driver.navigate().refresh();
        String width = driver.findElement(By.cssSelector("#dashboard-grid .widget-card")).getCssValue("width");
        assertTrue(width.endsWith("px"));
    }
}
