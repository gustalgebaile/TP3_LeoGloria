package org.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;

public class LojaTests {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void teardown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void registerUserTest() {
        String registerPath = Paths.get("src/main/resources/register.html").toAbsolutePath().toUri().toString();
        driver.get(registerPath);
        driver.findElement(By.id("username")).sendKeys("usuarioTeste");
        driver.findElement(By.id("email")).sendKeys("teste@email.com");
        driver.findElement(By.id("password")).sendKeys("123456");
        driver.findElement(By.id("registerButton")).click();
        WebElement successMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("successMessage")));
        Assertions.assertEquals("Cadastro realizado com sucesso!", successMessage.getText());
    }

    @Test
    public void addProductToCartTest() {
        String productPath = Paths.get("src/main/resources/product.html").toAbsolutePath().toUri().toString();
        driver.get(productPath);
        driver.findElement(By.id("addToCart")).click();
        String message = driver.findElement(By.id("cartMessage")).getText();
        Assertions.assertEquals("Produto adicionado!", message);
    }

    @Test
    public void loginUserWithCorrectCredentialsTest() {
        String loginPath = Paths.get("src/main/resources/login.html").toAbsolutePath().toUri().toString();
        driver.get(loginPath);

        driver.findElement(By.id("email")).sendKeys("user@test.com");
        driver.findElement(By.id("password")).sendKeys("1234");
        driver.findElement(By.id("loginButton")).click();

        wait.until(ExpectedConditions.urlContains("dashboard.html"));

        Assertions.assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("dashboard.html"),
                "O login não redirecionou para o dashboard");
    }

    @Test
    public void contactUsFormTest() {
        String formPath = Paths.get("src/main/resources/contact.html").toAbsolutePath().toUri().toString();
        driver.get(formPath);

        driver.findElement(By.id("name")).sendKeys("João Teste");
        driver.findElement(By.id("email")).sendKeys("joao@email.com");
        driver.findElement(By.id("message")).sendKeys("Mensagem de teste");

        driver.findElement(By.id("acceptTerms")).click();

        WebElement subjectDropdown = driver.findElement(By.id("subject"));
        Select select = new Select(subjectDropdown);
        select.selectByVisibleText("Suporte");

        driver.findElement(By.id("submitButton")).click();

        WebElement successMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("successMessage"))
        );
        Assertions.assertEquals("Mensagem enviada com sucesso!", successMessage.getText());
    }

    @Test
    public void loginAndSaveCookiesTest() {
        String loginPath = Paths.get("src/main/resources/login.html").toAbsolutePath().toUri().toString();
        driver.get(loginPath);

        driver.findElement(By.id("email")).sendKeys("user@test.com");
        driver.findElement(By.id("password")).sendKeys("1234");
        driver.findElement(By.id("loginButton")).click();

        wait.until(ExpectedConditions.urlContains("dashboard.html"));

        Set<Cookie> cookies = driver.manage().getCookies();

        for (Cookie cookie : cookies) {
            System.out.println("Salvo cookie: " + cookie.getName() + " = " + cookie.getValue());
        }

        Assertions.assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("dashboard.html"));
    }

    @Test
    public void reuseLocalStorageToSkipLoginTest() {
        String loginPath = Paths.get("src/main/resources/login.html").toAbsolutePath().toUri().toString();
        driver.get(loginPath);

        // Grava sessão direto no localStorage simulando usuário logado
        ((JavascriptExecutor) driver).executeScript("localStorage.setItem('sessionId', '12345');");

        driver.navigate().refresh();

        String dashboardPath = Paths.get("src/main/resources/dashboard.html").toAbsolutePath().toUri().toString();
        driver.get(dashboardPath);

        Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard.html"),
                "Não foi possível reutilizar a sessão pelo localStorage");
    }

    @Test
    public void captureFullPageScreenshotTest() throws IOException {
        String productPath = Paths.get("src/main/resources/product.html").toAbsolutePath().toUri().toString();
        driver.get(productPath);

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        Path destination = Paths.get("target/screenshots/productPage.png");
        Files.createDirectories(destination.getParent());
        Files.copy(screenshot.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        Assertions.assertTrue(Files.exists(destination), "Screenshot não foi salva corretamente!");
    }

    @Test
    public void addProductAndVerifyCartQuantityTest() {
        String productPath = Paths.get("src/main/resources/product.html").toAbsolutePath().toUri().toString();
        driver.get(productPath);

        driver.findElement(By.id("addToCart")).click();

        String message = driver.findElement(By.id("cartMessage")).getText();
        Assertions.assertEquals("Produto adicionado!", message);

        String cartPath = Paths.get("src/main/resources/cart.html").toAbsolutePath().toUri().toString();
        driver.get(cartPath);

        WebElement quantityElement = driver.findElement(By.id("productQuantity"));
        Assertions.assertEquals("1", quantityElement.getText(), "Quantidade no carrinho incorreta!");
    }

    @Test
    public void scrollToElementAndClickTest() {
        String pagePath = Paths.get("src/main/resources/longPage.html").toAbsolutePath().toUri().toString();
        driver.get(pagePath);

        WebElement hiddenButton = driver.findElement(By.id("scrollButton"));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", hiddenButton);

        wait.until(ExpectedConditions.elementToBeClickable(hiddenButton));

        hiddenButton.click();

        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successMessage")));
        Assertions.assertEquals("Botão clicado com sucesso!", message.getText());
    }

}
