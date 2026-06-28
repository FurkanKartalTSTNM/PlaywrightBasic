package com.testinium.playwrightjavaproject;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ScreenshotAnimations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/** Runs a logical test step and captures its final browser state. */
public final class ScreenshotSteps {
    private static final Path SCREENSHOT_ROOT = Path.of("screenshot");

    private final Page page;
    private final Path testDirectory;
    private final AtomicInteger sequence = new AtomicInteger();

    public ScreenshotSteps(Page page, String testName) {
        this.page = Objects.requireNonNull(page, "page");
        if (testName == null || testName.isBlank()) {
            throw new IllegalArgumentException("testName boş olamaz");
        }
        this.testDirectory = SCREENSHOT_ROOT.resolve(sanitize(testName));
    }

    public void run(String stepName, Runnable action) {
        Objects.requireNonNull(action, "action");
        execute(stepName, () -> {
            action.run();
            return null;
        });
    }

    public <T> T run(String stepName, Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        return execute(stepName, action);
    }

    public Path getTestDirectory() {
        return testDirectory;
    }

    private <T> T execute(String stepName, Supplier<T> action) {
        validateStepName(stepName);
        T result;
        try {
            result = action.get();
        } catch (RuntimeException | Error stepError) {
            try {
                takeScreenshot(stepName, "failed");
            } catch (RuntimeException screenshotError) {
                stepError.addSuppressed(screenshotError);
            }
            throw stepError;
        }

        takeScreenshot(stepName, "passed");
        return result;
    }

    private void takeScreenshot(String stepName, String status) {
        int currentSequence = sequence.incrementAndGet();
        String fileName = String.format(Locale.ROOT, "%03d-%s-%s.png",
                currentSequence, sanitize(stepName), status);
        Path screenshotPath = testDirectory.resolve(fileName);

        try {
            Files.createDirectories(testDirectory);
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true)
                    .setAnimations(ScreenshotAnimations.DISABLED));
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException(
                    "Screenshot alınamadı: " + screenshotPath, exception);
        }
    }

    private static void validateStepName(String stepName) {
        if (stepName == null || stepName.isBlank()) {
            throw new IllegalArgumentException("stepName boş olamaz");
        }
    }

    private static String sanitize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('ı', 'i')
                .replace('İ', 'I');
        String sanitized = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("^-+|-+$", "");
        return sanitized.isEmpty() ? "unnamed" : sanitized;
    }
}
