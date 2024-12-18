package org.example.onlineegyetemirendszer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PerformanceTest {

    private final int THREAD_COUNT = 10;
    private final int ITERATIONS_PER_THREAD = 10;
    private final String BASE_URL = "http://localhost:8080/index.html";
    private WebDriver driver;

    @BeforeClass
    public void setUp() {
        System.setProperty("webdriver.edge.driver", "drivers/msedgedriver.exe");
    }

    @Test
    public void testPerformanceUnderLoad() {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                WebDriver localDriver = new EdgeDriver();
                try {
                    for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                        executeTestScenario(localDriver);
                    }
                } finally {
                    localDriver.quit();
                }
            });
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(15, TimeUnit.MINUTES)) {
                System.err.println("A teljesítményteszt nem fejeződött be időben.");
            }
        } catch (InterruptedException e) {
            System.err.println("A teljesítményteszt megszakadt: " + e.getMessage());
        }
    }

    private void executeTestScenario(WebDriver localDriver) {
        localDriver.get(BASE_URL);
        System.out.println("Oldal betöltve: " + localDriver.getCurrentUrl());

        WebElement usernameInput = localDriver.findElement(By.id("username"));
        WebElement passwordInput = localDriver.findElement(By.id("password"));
        WebElement loginButton = localDriver.findElement(By.tagName("button"));

        usernameInput.sendKeys("diak1");
        passwordInput.sendKeys("jelszo1");
        loginButton.click();
        System.out.println("Bejelentkezés megtörtént.");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement logoutButton = localDriver.findElement(By.id("logoutButton"));
        logoutButton.click();
        System.out.println("Kijelentkezés megtörtént.");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
