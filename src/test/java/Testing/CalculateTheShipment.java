package Testing;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import java.time.Duration;
import java.util.List;

public class CalculateTheShipment {
    WebDriver driver;

    @BeforeTest
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        // Configure Headless Mode
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run Chrome in headless mode
        options.addArguments("--disable-gpu"); // Required for headless mode in some systems
        options.addArguments("--window-size=1920,1080"); // Set screen size
        options.addArguments("--no-sandbox"); // Bypass OS security model
        options.addArguments("--disable-dev-shm-usage"); // Overcome limited resource issues

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.get("https://www.pos.com.my/send/ratecalculator");
    }

    @Test
    public void calculate() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Locate postcode input field
        WebElement textField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@formcontrolname='postcodeFrom']")));

        // Scroll and enter postcode
        js.executeScript("arguments[0].scrollIntoView(true);", textField);
        textField.clear();
        textField.sendKeys("50100");

        // Ensure Postcode is entered correctly
        Assert.assertEquals(textField.getAttribute("value"), "50100");

        // Locate the country input field
        WebElement countryInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Select country']")));

        // Scroll into view before clicking
        js.executeScript("arguments[0].scrollIntoView(true);", countryInput);

        // Click using JavaScript if normal click fails
        try {
            countryInput.click();
        } catch (ElementClickInterceptedException e) {
            System.out.println("Click intercepted, using JavaScript Click.");
            js.executeScript("arguments[0].click();", countryInput);
        }

        // Clear default value in the country input field
        countryInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        countryInput.sendKeys(Keys.DELETE);

        // If still not cleared, use backspace multiple times
        for (int i = 0; i < 10; i++) {
            countryInput.sendKeys(Keys.BACK_SPACE);
        }

        // If still not cleared, use JavaScript
        js.executeScript("arguments[0].value = '';", countryInput);

        Actions actions = new Actions(driver);

        // Send "India" as input
        actions.sendKeys("India").perform();

        // Wait for dropdown to appear before clicking the option
        WebElement countryOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//mat-option[contains(@id, 'mat-option') and contains(., 'India - IN')]")));

        // Click the dropdown option after waiting
        wait.until(ExpectedConditions.elementToBeClickable(countryOption)).click();

        // Verify that "India" is selected
        Assert.assertTrue(countryInput.getAttribute("value").contains("India"));

        // Locate and update the weight field
        WebElement weightInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@formcontrolname='itemWeight']")));
        js.executeScript("arguments[0].scrollIntoView(true);", weightInput);
        weightInput.clear();
        weightInput.sendKeys("1");

        // Locate the "Calculate" button
        WebElement calculateButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'Calculate')]")));

        // Scroll into view
        js.executeScript("arguments[0].scrollIntoView(true);", calculateButton);

        // Try clicking normally
        try {
            calculateButton.click();
        } catch (ElementClickInterceptedException e) {
            System.out.println("Click intercepted, using JavaScript Click.");
            js.executeScript("arguments[0].click();", calculateButton);
        }

        // Wait for at least one result to appear
        List<WebElement> results = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.xpath("//div[contains(@class,'border-gray-300') and contains(@class,'ng-star-inserted')]")
        ));

        // Assert that at least one result is displayed
        Assert.assertTrue(results.size() >= 1, "No results found after clicking Calculate!");

        // Print the number of results found
        System.out.println("Number of results displayed: " + results.size());
    }

    @AfterTest
    public void tearDown() {
        driver.quit();
    }
}
