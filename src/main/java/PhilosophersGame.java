import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main-Class for the Philosophers-Game.
 *
 * @author Stefan Winterberger
 * @version 1.0.0
 */
public class PhilosophersGame {

    private final String PATH_TO_FIREFOX_EXE = "P:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe";
    private final String END_PAGE = "Philosophie – Wikipedia";
    private String startUrl = "https://de.wikipedia.org/wiki/Tee";
    //    private String startUrl = "https://de.wikipedia.org/wiki/Chinesische_Schrift";
    private int maxClicks = 20;
    private LinkedList<String> visitedPages = new LinkedList<>();
    private WebDriver driver;

    public static void main(String[] args) {
        PhilosophersGame game = new PhilosophersGame();
        game.setUpWebDriver();
        game.run();
    }

    private void setUpWebDriver() {
        File pathBinary = new File(PATH_TO_FIREFOX_EXE);
        FirefoxBinary Binary = new FirefoxBinary(pathBinary);
        FirefoxProfile firefoxPro = new FirefoxProfile();
        driver = new FirefoxDriver(Binary, firefoxPro);
    }

    private void run() {

        driver.navigate().to(startUrl);

        String title = driver.getTitle();

        visitedPages.add(title);
        int clicks = 0;


        while (clicks < maxClicks && !title.equals(END_PAGE)) {

            WebElement contentText = driver.findElement(
                    By.xpath("/html/body/div[@id='content']/div[@id='bodyContent']/div[@id='mw-content-text']/p"));

            String paragraphText = filterTextInBrackets(contentText);

            List<WebElement> links = contentText.findElements(By.cssSelector("p > a"));

            links.stream()
                    .filter(link -> paragraphText.contains(link.getText()))
                    .findFirst()
                    .ifPresent(WebElement::click);

            title = driver.getTitle();
            System.out.println(title);

            //Loopdetection
            if (isPageVisited(title)) {
                System.out.println("Page already visited. Loop detected. Abort.");
                return;
            }
            visitedPages.add(title);

            clicks++;
        }
        if (clicks >= maxClicks) {
            System.out.println("Maximum Number of Clicks reached without reaching the END_PAGE: " + END_PAGE);
        } else {
            System.out.println("Number of Click needed: " + clicks);
        }
        driver.quit();
    }

    private String filterTextInBrackets(WebElement element) {
        return Arrays.stream(element.getText().split(" \\(((?!\\)).)*\\) ")).collect(Collectors.joining(" "));
    }

    private boolean isPageVisited(final String page) {
        return visitedPages.contains(page);
    }
}
