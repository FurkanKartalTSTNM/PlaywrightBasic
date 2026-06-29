package com.testinium.playwrightjavaproject;

import com.microsoft.playwright.Page;
import com.testinium.playwright.screenshot.TestiniumPlaywright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class FirstTest {
    private TestiniumPlaywright testinium;
    private Page page;

    @BeforeEach
    void launchBrowser(TestInfo testInfo) {
        testinium = TestiniumPlaywright.launch(testInfo.getDisplayName());
        page = testinium.page();
    }

    @Test
    public void shouldCaptureActionsWithoutStepNames() {
        page.setContent("<label>E-posta <input id='email'></label>"
                + "<button id='login'>Giriş yap</button>");
        page.getByLabel("E-posta").fill("user@test.com");
        page.locator("#login").click();

        Assertions.assertEquals("user@test.com",
                page.locator("#email").inputValue());
    }

    @AfterEach
    void closeBrowser() {
        if (testinium != null) {
            testinium.close();
        }
    }
}
