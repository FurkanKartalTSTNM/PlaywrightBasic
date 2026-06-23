package com.testinium.playwrightjavaproject;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

public class FirstTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeEach
    void createContext() {
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    public void shouldOpenGoogle() {
        page.navigate("https://www.google.com");
        Assertions.assertTrue(page.title().contains("Google"));
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
