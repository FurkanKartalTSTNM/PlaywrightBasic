package com.testinium.playwrightjavaproject;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

public class FirstTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;
    ScreenshotSteps steps;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @BeforeEach
    void createContext(TestInfo testInfo) {
        context = browser.newContext();
        page = context.newPage();
        steps = new ScreenshotSteps(page, testInfo.getDisplayName());
    }

    @Test
    public void shouldOpenGoogle() {
        steps.run("Google sayfasını aç", () ->
                page.navigate("https://www.google.com"));

        steps.run("Sayfa başlığını doğrula", () ->
                Assertions.assertTrue(page.title().contains("Google")));
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }
}
