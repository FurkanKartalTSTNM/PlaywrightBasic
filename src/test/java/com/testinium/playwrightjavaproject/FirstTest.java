package com.testinium.playwrightjavaproject;

import com.microsoft.playwright.*;
import com.testinium.playwright.screenshot.ScreenshotConfig;
import com.testinium.playwright.screenshot.ScreenshotSession;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FirstTest {

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    private Page page;
    private ScreenshotSession screenshots;
    private Path tracePath;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true)
        );
    }

    @BeforeEach
    void createContext(TestInfo testInfo) throws IOException {
        String testName = sanitize(testInfo.getDisplayName());

        Files.createDirectories(Path.of("trace"));
        tracePath = Path.of("trace", testName + ".zip");

        context = browser.newContext();

        // Page oluşturulmadan önce tracing başlatılmalı.
        context.tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
        );

        page = context.newPage();

        screenshots = ScreenshotSession.builder(page, testName)
                .config(
                        ScreenshotConfig.builder()
                                .outputDirectory(Path.of("screenshot"))
                                .fullPage(true)
                                .build()
                )
                .build();
    }

    @Test
    public void shouldOpenGoogle() {
        step("Google sayfasını aç", () ->
                page.navigate("https://www.google.com"));

        step("Sayfa başlığını doğrula", () ->
                Assertions.assertTrue(page.title().contains("Google")));
    }

    private void step(String stepName, Runnable action) {
        context.tracing().group(stepName);

        try {
            screenshots.step(stepName, action);
        } finally {
            context.tracing().groupEnd();
        }
    }

    @AfterEach
    void closeContext() {
        if (context == null) {
            return;
        }

        try {
            // Context kapanmadan önce trace kaydedilmeli.
            context.tracing().stop(
                    new Tracing.StopOptions()
                            .setPath(tracePath)
            );
        } finally {
            context.close();
        }
    }

    @AfterAll
    static void closeBrowser() {
        try {
            if (browser != null) {
                browser.close();
            }
        } finally {
            if (playwright != null) {
                playwright.close();
            }
        }
    }

    private static String sanitize(String value) {
        return value.toLowerCase()
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
