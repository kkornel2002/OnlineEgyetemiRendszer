package org.example.onlineegyetemirendszer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.testng.Assert.assertTrue;

public class UITest {

    private WebDriver driver;

    @BeforeClass
    public void setUp() {
        System.setProperty("webdriver.edge.driver", "drivers/msedgedriver.exe");
        driver = new EdgeDriver();
    }

    @Test
    public void testAdminLogin() {
        driver.get("http://localhost:8080/index.html");
        System.out.println("Oldal betöltve: " + driver.getCurrentUrl());

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement usernameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.tagName("button"));

        usernameInput.sendKeys("ugyintezo1");
        System.out.println("Felhasználónév megadva.");
        passwordInput.sendKeys("ugyintezojelszo1");
        System.out.println("Jelszó megadva.");
        loginButton.click();
        System.out.println("Bejelentkezés gombra rákattintva.");

        wait.until(ExpectedConditions.alertIsPresent());
        System.out.println("Figyelmeztető üzenet megjelenése észlelve.");

        driver.switchTo().alert().accept();
        System.out.println("Figyelmeztető üzenet elfogadva.");

        wait.until(ExpectedConditions.urlContains("management.html"));
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Jelenlegi URL a bejelentkezés után: " + currentUrl);
        assertTrue(currentUrl.contains("management.html"), "Admin bejelentkezés sikertelen!");
    }

    @Test
    public void testStudentLoginAndRegisterForExam() {
        driver.get("http://localhost:8080/index.html");
        System.out.println("Oldal betöltve: " + driver.getCurrentUrl());

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement usernameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.tagName("button"));

        usernameInput.sendKeys("diak1");
        System.out.println("Felhasználónév megadva.");
        passwordInput.sendKeys("jelszo1");
        System.out.println("Jelszó megadva.");
        loginButton.click();
        System.out.println("Bejelentkezés gombra rákattintva.");

        wait.until(ExpectedConditions.alertIsPresent());
        System.out.println("Figyelmeztető üzenet megjelenése észlelve.");

        driver.switchTo().alert().accept();
        System.out.println("Figyelmeztető üzenet elfogadva.");

        wait.until(ExpectedConditions.urlContains("student.html"));
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Jelenlegi URL a bejelentkezés után: " + currentUrl);
        assertTrue(currentUrl.contains("student.html"), "Diák bejelentkezés sikertelen!");

        WebElement examsButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(),'Elérhető vizsgák')]")));
        examsButton.click();
        System.out.println("Elérhető vizsgák gomb megnyomva.");

        try {
            WebElement firstExamButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Vizsga felvétele')]")));
            firstExamButton.click();
            System.out.println("Első vizsga felvéve.");
        } catch (TimeoutException e) {
            System.out.println("Nincsenek elérhető vizsgák.");
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
