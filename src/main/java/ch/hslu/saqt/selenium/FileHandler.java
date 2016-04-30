package ch.hslu.saqt.selenium;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;

/**
 * FileHandler for I/O of the Philosophersgame.
 *
 * @author Stefan Winterberger
 * @version 1.0.0
 */
public final class FileHandler {

    private final String settingsFilename = "settings.properties";
    private final String filePath = System.getProperty("user.dir");
    private final String INPUT_FILENAME = "Wikipages Input.csv";

    public Properties getProperties() {
        Properties properties = new Properties();

        if (new File(filePath + File.separator + settingsFilename).exists()) {
            try {
                properties.load(new FileInputStream(filePath + File.separator + settingsFilename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            createDefaultSettingsFile();
            try {
                properties.load(new FileInputStream(filePath + File.separator + settingsFilename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    public LinkedList<GameTurn> readGameturnsFromFile() {
        final LinkedList<GameTurn> turns = new LinkedList<>();
        try (FileReader fr = new FileReader(INPUT_FILENAME);
             BufferedReader in = new BufferedReader(fr)) {
            String line = in.readLine();
            while (line != null) {
                ArrayList<String> pages = readEvenValues(line);
                ArrayList<Integer> maxClicks = readOddValues(line);
                for (int i = 0; i < pages.size(); i++) {
                    turns.add(new GameTurn(pages.get(i), maxClicks.get(i)));
                }
                line = in.readLine();
            }
        } catch (FileNotFoundException e1) {
            createDefaultInputFile();
            System.out.println("File " + INPUT_FILENAME + " wasn't found: ");
        } catch (IOException e1) {
            System.out.println("IO Exception while read File. The Message is: " + e1.getMessage());
        }
        return turns;
    }

    public void writeToDefaultOutputFile(final String stringToWrite) {
        String OUTPUT_FILENAME = "Results.csv";
        try (FileWriter fileWriter = new FileWriter(OUTPUT_FILENAME);
             BufferedWriter out = new BufferedWriter(fileWriter)) {
            out.write(stringToWrite);
            out.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private ArrayList<String> readEvenValues(final String line) {
        ArrayList<String> pages = new ArrayList<>();

        String[] tokens = line.split(",");

        for (int i = 0; i < tokens.length; i++) {
            if (i % 2 == 0) {
                String page = tokens[i];
                pages.add("https://de.wikipedia.org/wiki/" + page.trim());
            }
        }
        return pages;
    }

    private ArrayList<Integer> readOddValues(final String line) {
        ArrayList<Integer> maxClicks = new ArrayList<>();

        String[] tokens = line.split(",");

        for (int i = 0; i < tokens.length; i++) {
            if (i % 2 != 0) {
                int maxClick = 0;
                try {
                    maxClick = Integer.parseInt(tokens[i].trim());
                } catch (NumberFormatException nfex) {
                    System.out.println("Invalid Value for MaxClicks");
                }
                maxClicks.add(maxClick);
            }
        }
        return maxClicks;
    }

    private void createDefaultSettingsFile() {
        try {
            File settingsFile = new File(filePath + File.separator + settingsFilename);
            settingsFile.createNewFile();
            FileWriter writer = new FileWriter(settingsFile);
            writer.append("pathToFirefoxExe=C:\\\\Program Files (x86)\\\\Mozilla Firefox\\\\firefox.exe\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultInputFile() {
        File inputFile = new File(filePath + File.separator + INPUT_FILENAME);
        try (FileWriter fileWriter = new FileWriter(inputFile);
             BufferedWriter out = new BufferedWriter(fileWriter)) {
            inputFile.createNewFile();
            out.write("Tee, 10, Hanf, 4, Kuh, 20,\nSchule, 15,\nSteam, 55,");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
