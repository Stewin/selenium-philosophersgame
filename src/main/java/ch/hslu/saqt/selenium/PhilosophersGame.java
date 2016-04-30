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

    private final FileHandler fileHandler = new FileHandler();
    private final LinkedList<GameTurn> turns = new LinkedList<>();
    private final LinkedList<String> visitedPages = new LinkedList<>();
    private String pathToFirefoxExe;
    private WebDriver driver;

    public static void main(String[] args) {

        PhilosophersGame game = new PhilosophersGame();

        System.out.print("\nSelenium Project - Philosophiespiel\n" +
                "by Stefan Winterberger\n\n");

        game.setUpVariables();
        game.setUpWebDriver();
        game.getUserInput();
        game.run();
        game.printScores();
    }

    private void setUpVariables() {
        Properties properties = fileHandler.getProperties();
        pathToFirefoxExe = properties.getProperty("pathToFirefoxExe");
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

        System.out.println("https://de.wikipedia.org/wiki/");
        String userInput = scanner.nextLine();
        if (userInput.equals("")) {
            turns.addAll(fileHandler.readGameturnsFromFile());
        } else {
            //User Input
            String page = "https://de.wikipedia.org/wiki/" + userInput;
            System.out.println("Now enter a maximum Value for Clicks till the Game aborts: ");
            int maxNumberOfClicks = 0;

            while (maxNumberOfClicks == 0) {
                try {
                    maxNumberOfClicks = scanner.nextInt();
                } catch (InputMismatchException ex) {
                    System.out.println("\nThe entered Value have to be a valid Integer. Please enter again.");
                    scanner.nextLine();
                    maxNumberOfClicks = 0;
                }
            }
            turns.add(new GameTurn(page, maxNumberOfClicks));
        }
        System.out.println();
    }

    private void run() {

        for (GameTurn turn : turns) {
            driver.navigate().to(turn.getStartPageUrl());
            String title = driver.getTitle();
            visitedPages.clear();

            while (turn.getClicksNeeded() < turn.getMaxClicks() && !title.equals(turn.getEndPageTitle())) {
                try {
                    clickFirstValidLink();
                } catch (NullPointerException npe) {
                    turn.setErrorDetected();
                    break;
                }

                title = driver.getTitle();
                if (isLoopDetected()) {
                    turn.setLoopDetected();
                    break;
                }
                turn.increaseClicksNeeded();
            }
        }
        driver.quit();
    }

    private void clickFirstValidLink() {

        WebElement contentText = null;
        try {
            contentText = driver.findElement(
                    By.xpath("/html/body/div[@id='content']/div[@id='bodyContent']/div[@id='mw-content-text']/p[1]"));
        } catch (NoSuchElementException ex) {
            System.out.println("The Entered Page seems not to Exist. Enter a Valid Wiki-Page to start.");
        }

        List<WebElement> webLinks = contentText.findElements(By.cssSelector("p > a"));
        if (webLinks.size() == 0) {
            webLinks = contentText.findElements(By.cssSelector("li > a"));
        }

        String paragraphText = filterTextInBrackets(contentText);

        webLinks.stream()
                .filter(link -> paragraphText.contains(link.getText()))
                .findFirst()
                .ifPresent(WebElement::click);
    }

    private boolean isLoopDetected() {
        String title = driver.getTitle();
        if (isPageVisited(title)) {
            return true;
        }
        visitedPages.add(title);
        return false;
    }

    private String filterTextInBrackets(final WebElement element) {
        return Arrays.stream(element.getText().split(" \\(((?!\\)).)*\\) ")).collect(Collectors.joining(" "));
    }

    private boolean isPageVisited(final String page) {
        return visitedPages.contains(page);
    }

    private void printScores() {

        StringBuilder stringBuilder = new StringBuilder();

        for (GameTurn turn : turns) {
            stringBuilder.append(turn.toString()).append(System.lineSeparator());
        }
        System.out.println(stringBuilder.toString());
        fileHandler.writeToDefaultOutputFile(stringBuilder.toString());
    }
}
