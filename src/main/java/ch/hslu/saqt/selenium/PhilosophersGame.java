package ch.hslu.saqt.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main-Class for the Philosophers-Game.
 *
 * @author Stefan Winterberger
 * @version 1.0.0
 */
public class PhilosophersGame {

    private final String END_PAGE = "Philosophie – Wikipedia";
    private String pathToFirefoxExe;
    private String startUrl = "https://de.wikipedia.org/wiki/Tee";
    private int maxClicks = 0;
    private LinkedList<String> visitedPages = new LinkedList<>();
    private WebDriver driver;

    public static void main(String[] args) {
        PhilosophersGame game = new PhilosophersGame();
        System.out.print("Selenium Project - Philosophiespiel\n" +
                "by Stefan Winterberger\n");
        game.setUpWebDriver();
        game.getUserInput();
        game.run();
    }

    private void setUpWebDriver() {

        setupProperties();

        File pathBinary = new File(pathToFirefoxExe);
        FirefoxBinary binary = new FirefoxBinary(pathBinary);
        FirefoxProfile firefoxPro = new FirefoxProfile();
        driver = new FirefoxDriver(binary, firefoxPro);
    }

    private void setupProperties() {
        Properties properties = new Properties();
        String settingsPath = new File("src/main/resources/settings.properties").getAbsolutePath();

        try {
            properties.load(new FileInputStream(settingsPath));
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        pathToFirefoxExe = properties.getProperty("pathToFirefoxExe");
    }

    private void run() {

        driver.navigate().to(startUrl);

        String title = driver.getTitle();

        visitedPages.add(title);
        int clicks = 0;

        while (clicks <= maxClicks && !title.equals(END_PAGE)) {
            WebElement contentText = null;
            try {
                contentText = driver.findElement(
                        By.xpath("/html/body/div[@id='content']/div[@id='bodyContent']/div[@id='mw-content-text']/p"));
            } catch (NoSuchElementException ex) {
                System.out.println("The Entered Page seems not to Exist. Enter a Valid Wiki-Page to start.");
                getUserInput();
                run();
            }

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
        if (clicks > maxClicks) {
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

    private void getUserInput() {
        System.out.print("To Start the Game enter a valid Wikipedia-Page.\n" +
                "Example: https://de.wikipedia.org/wiki/Tee\n");

        Scanner scanner = new Scanner(System.in);

        do {
            System.out.print("https://de.wikipedia.org/wiki/");
            startUrl = "https://de.wikipedia.org/wiki/" + scanner.nextLine();
            System.out.println();
        } while ((!isPageValid(startUrl)));

        System.out.println("Now enter a maximum Value for Clicks till the Game aborts: ");

        while (maxClicks == 0) {
            try {
                maxClicks = scanner.nextInt();
            } catch (InputMismatchException ex) {
                System.out.println("\nThe entered Value have to be a valid Integer. Please enter again.");
                scanner.nextLine();
                maxClicks = 0;
            }
        }
    }

    private boolean isPageValid(final String page) {
        driver.navigate().to(page);
        WebElement contentText = null;
        try {
            contentText = driver.findElement(
                    By.xpath("/html/body/div[@id='content']/div[@id='bodyContent']/div[@id='mw-content-text']/p"));
        } catch (NoSuchElementException ex) {
            System.out.println("The Entered Page seems not to Exist. Enter a Valid Wiki-Page to start.");
            return false;
        }
        return true;
    }
}
