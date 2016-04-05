package ch.hslu.saqt.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main-Class for the Philosophers-Game.
 *
 * @author Stefan Winterberger
 * @version 1.0.1
 */
public class PhilosophersGame {

    private final String END_PAGE = "Philosophie – Wikipedia";
    private final String SETTINGS_FILENAME = "settings.properties";
    private String pathToFirefoxExe;
    private String startUrl = "https://de.wikipedia.org/wiki/Tee";
    private int maxClicks = 0;
    private LinkedList<String> visitedPages = new LinkedList<>();
    private WebDriver driver;
    private String settingsPath = System.getProperty("user.dir");

    public static void main(String[] args) {

        PhilosophersGame game = new PhilosophersGame();

        System.out.print("Selenium Project - Philosophiespiel\n" +
                "by Stefan Winterberger\n\n");
        game.setUpWebDriver();
        game.getUserInput();
        game.run();
    }

    private void setUpWebDriver() {

        setupProperties();

        File pathBinary = new File(pathToFirefoxExe);
        FirefoxProfile firefoxPro = new FirefoxProfile();
        FirefoxBinary binary;
        try {
            binary = new FirefoxBinary(pathBinary);
        } catch (WebDriverException wdex) {
            System.out.println("It seems that " + pathToFirefoxExe + " isn't your installation path for firefox.exe");
            System.out.println("Please edit de settings.properties File to a valid Path for firefox.exe and restart the Program.");
            try {
                System.out.println("Press a Key to Exit...");
                System.in.read();
                System.exit(-1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            binary = null;
        }
        driver = new FirefoxDriver(binary, firefoxPro);
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

    private void setupProperties() {
        Properties properties = new Properties();


        File jarPath = new File(settingsPath + File.separator + SETTINGS_FILENAME);

        if (new File(settingsPath + File.separator + SETTINGS_FILENAME).exists()) {
            try {
                properties.load(new FileInputStream(settingsPath + File.separator + SETTINGS_FILENAME));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            createDefaultSettingsFile();
            try {
                properties.load(new FileInputStream(settingsPath + File.separator + SETTINGS_FILENAME));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        pathToFirefoxExe = properties.getProperty("pathToFirefoxExe");
    }

    private void createDefaultSettingsFile() {
        try {
            File settingsFile = new File(settingsPath + File.separator + SETTINGS_FILENAME);
            settingsFile.createNewFile();

            FileWriter writer = new FileWriter(settingsFile);
            writer.append("pathToFirefoxExe=C:\\\\Program Files (x86)\\\\Mozilla Firefox\\\\firefox.exe");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
