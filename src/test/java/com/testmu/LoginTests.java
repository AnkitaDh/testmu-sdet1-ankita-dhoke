package com.testmu;

import com.testmu.llm.LlmFailureReportingExtension;
import com.testmu.support.UiTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LlmFailureReportingExtension.class)
public class LoginTests extends UiTestBase {

    @Test
    void validLoginRedirectsToDashboard() {
        driver.get(loginUrl);
        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("email")).sendKeys("tester@testmu.ai");
        driver.findElement(By.id("password")).sendKeys("TestMu123!");
        driver.findElement(By.id("login-button")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("dashboard.html"));
        assertTrue(driver.getCurrentUrl().endsWith("dashboard.html"));
        assertTrue(driver.findElement(By.id("status")).getText().contains("Welcome tester@testmu.ai"));
    }

    @Test
    void invalidCredentialsShowError() {
        driver.get(loginUrl);
        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("email")).sendKeys("bad@testmu.ai");
        driver.findElement(By.id("password")).sendKeys("wrongpass");
        driver.findElement(By.id("login-button")).click();
        assertEquals("Invalid email or password.", driver.findElement(By.id("message")).getText());
    }

    @Test
    void forgotPasswordReturnsConfirmation() {
        driver.get(loginUrl);
        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("email")).sendKeys("tester@testmu.ai");
        driver.findElement(By.id("forgot-password-button")).click();
        assertEquals("Password reset email simulated.", driver.findElement(By.id("message")).getText());
    }

    @Test
    void sessionExpiryInvalidatesStoredToken() {
        driver.get(loginUrl);
        ((JavascriptExecutor) driver).executeScript("localStorage.setItem('authToken', 'expired-token'); localStorage.setItem('authExpiry', '0'); localStorage.setItem('userRole', 'admin');");
        driver.get(new File("src/test/resources/pages/dashboard.html").getAbsoluteFile().toURI().toString());
        assertEquals("Session expired or invalid. Please login again.", driver.findElement(By.id("status")).getText());
    }

    @Test
    void bruteForceLockoutBlocksRepeatedFailures() {
        driver.get(loginUrl);
        for (int i = 0; i < 5; i++) {
            driver.findElement(By.id("email")).clear();
            driver.findElement(By.id("password")).clear();
            driver.findElement(By.id("email")).sendKeys("tester@testmu.ai");
            driver.findElement(By.id("password")).sendKeys("wrongpass");
            driver.findElement(By.id("login-button")).click();
        }
        assertTrue(driver.findElement(By.id("message")).getText().contains("Too many failed login attempts"));
    }
}
