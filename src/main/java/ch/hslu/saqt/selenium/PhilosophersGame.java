package ch.hslu.saqt.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
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

    private final ArrayList<String> startPages = new ArrayList<>();
    private final ArrayList<Integer> maxNumberOfClickes = new ArrayList<>();
    private final ArrayList<String> scoreOgPages = new ArrayList<>();
    private String endPage = "Philosophie – Wikipedia";

    private String pathToFirefoxExe;
    private LinkedList<String> visitedPages = new LinkedList<>();
    private WebDriver driver;
    private FileHandler fileHandler = new FileHandler();

    public static void main(String[] args) {

        PhilosophersGame game = new PhilosophersGame();

        System.out.print("\nSelenium Project - Philosophiespiel\n" +
                "by Stefan Winterberger\n\n");

        game.setUpVariables();
        game.setUpWebDriver();
        game.getUserInput();
        game.run();
    }

    private void setUpVariables() {
        Properties properties = fileHandler.getProperties();
        pathToFirefoxExe = properties.getProperty("pathToFirefoxExe");
        endPage = properties.getProperty("endPage");
    }

    private void setUpWebDriver() {

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

    private void getUserInput() {
        System.out.print("To Start the Game enter a valid Wikipedia-Page or press Enter to read from InputFile.\n" +
                "Example: https://de.wikipedia.org/wiki/Tee\n");

        Scanner scanner = new Scanner(System.in);

        boolean readFromFile = false;
        do {
            readFromFile = false;
            System.out.println("https://de.wikipedia.org/wiki/");
            String userInput = scanner.nextLine();
            if (userInput.equals("")) {
                readFromFile = true;
                startPages.addAll(fileHandler.readStartPagesFromFile());
                maxNumberOfClickes.addAll(fileHandler.readMaxClicksFromFile());
            } else {
                //User Input
                startPages.add(0, "https://de.wikipedia.org/wiki/" + userInput);
                System.out.println("Now enter a maximum Value for Clicks till the Game aborts: ");
                maxNumberOfClickes.add(0, 0);
                while (maxNumberOfClickes.get(0) == 0) {
                    try {
                        maxNumberOfClickes.add(0, scanner.nextInt());
                    } catch (InputMismatchException ex) {
                        System.out.println("\nThe entered Value have to be a valid Integer. Please enter again.");
                        scanner.nextLine();
                        maxNumberOfClickes.add(0);
                    }
                }
            }
            System.out.println();
        } while ((!isPageValid(startPages.get(0))) || !readFromFile);
    }

    private void run() {

        for (int page = 0; page < startPages.size(); page++) {
            driver.navigate().to(startPages.get(page));
            String title = driver.getTitle();
            int clicks = 0;

            while (clicks < maxNumberOfClickes.get(page) && !title.equals(endPage)) {
                WebElement contentText = null;
                try {
                    contentText = driver.findElement(
                            By.xpath("/html/body/div[@id='content']/div[@id='bodyContent']/div[@id='mw-content-text']/p"));
                } catch (NoSuchElementException ex) {
                    System.out.println("The Entered Page seems not to Exist. Enter a Valid Wiki-Page to start.");

                    getUserInput();
                    run();
                    return;
                }

                String paragraphText = filterTextInBrackets(contentText);

                List<WebElement> links = contentText.findElements(By.cssSelector("p > a"));
                //Listen
                //Zweiter, Dritter, Vierter... Paragraph

                if (links.size() == 0) {
                    System.out.println("The Page you entered is not explicit.\n" +
                            "Please enter another Page.\n");
                    getUserInput();
                }

                links.stream()
                        .filter(link -> paragraphText.contains(link.getText()))
                        .findFirst()
                        .ifPresent(WebElement::click);

                title = driver.getTitle();
                System.out.println(title);

                //Loopdetection
                if (isPageVisited(title)) {
                    System.out.println("Page already visited. Loop detected. After " + clicks + " Clicks. Abort.");
                    return;
                }
                visitedPages.add(title);

                clicks++;
            }
            if (clicks > maxNumberOfClickes.get(page)) {
                System.out.println("Maximum Number of Clicks reached without reaching the endPage: " + endPage);
            } else {
                System.out.println("Number of Click needed: " + clicks);
            }
        }
        driver.quit();
    }

    private String filterTextInBrackets(WebElement element) {
        return Arrays.stream(element.getText().split(" \\(((?!\\)).)*\\) ")).collect(Collectors.joining(" "));
    }

    private boolean isPageVisited(final String page) {
        return visitedPages.contains(page);
    }

    private boolean isPageValid(final String page) {
        driver.navigate().to(page);
        try {
            driver.findElement(
                    By.xpath("/html/body/div[@id='content']/div[@id='bodyContent']/div[@id='mw-content-text']/p"));
        } catch (NoSuchElementException ex) {
            System.out.println("The Entered Page seems not to Exist. Enter a Valid Wiki-Page to start.");
            return false;
        }
        return true;
    }
}
