package Steps;

import com.thoughtworks.gauge.Step;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.Method;

import java.io.FileReader;
import java.time.Duration;

public class StepImplementation {

    private WebDriver driver;
    private Method method;

    @Step("Open the browser and navigate to SecilStore homepage")
    public void openBrowser() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://www.secilstore.com/");
        method = new Method(driver);
    }

    private JSONObject getElementFromJson(String elementName) {
        try (FileReader reader = new FileReader("src/test/resources/elements.json")) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);

            System.out.println("JSON İçeriği: " + jsonObject.toString(4));

            if (!jsonObject.has(elementName)) {
                throw new RuntimeException("JSON içinde böyle bir element bulunamadı: " + elementName);
            }

            return jsonObject.getJSONObject(elementName);
        } catch (Exception e) {
            throw new RuntimeException("JSON dosyası okunamadı veya element bulunamadı: " + elementName, e);
        }
    }


    @Step("Click on element <elementName>")
    public void clickElement(String elementName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        JSONObject elementData = getElementFromJson(elementName);

        String id = elementData.optString("id", "");
        String xpath = elementData.optString("xpath", "");

        System.out.println("Element Adı: " + elementName);
        System.out.println("ID: " + id);
        System.out.println("XPath: " + xpath);

        WebElement element = null;

        try {
            if (!id.isEmpty()) {
                System.out.println("ID ile element bulunmaya çalışılıyor...");
                element = wait.until(ExpectedConditions.elementToBeClickable(By.id(id)));
            }
        } catch (Exception e) {
            System.out.println("ID ile bulunamadı, XPath deneniyor...");
        }

        if (element == null && !xpath.isEmpty()) {
            try {
                System.out.println("XPath ile element bulunmaya çalışılıyor...");
                element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            } catch (Exception e) {
                throw new RuntimeException("Ne ID ne de XPath ile element bulunamadı: " + elementName);
            }
        }

        if (element != null) {
            System.out.println("Element bulundu, tıklanıyor...");
            element.click();
        } else {
            throw new RuntimeException("Element bulunamadı: " + elementName);
        }
    }

    @Step("Click on element <elementName>, clear it and type <text>")
    public void clearAndTypeElement(String elementName, String text) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JSONObject elementData = getElementFromJson(elementName);

        String id = elementData.optString("id", "");
        String xpath = elementData.optString("xpath", "");

        WebElement element = null;
        Thread.sleep(2000);

        if (!id.isEmpty()) {
            element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
        }

        if (element == null && !xpath.isEmpty()) {
            element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        }

        if (element != null) {
            element.clear();
            element.sendKeys(text);
        } else {
            throw new RuntimeException("Element bulunamadı: " + elementName);
        }
    }

    @Step("Wait for <seconds> seconds")
    public void waitForSeconds(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000);
    }

    @Step("Close the browser")
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}