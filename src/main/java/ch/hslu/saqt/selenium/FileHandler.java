package ch.hslu.saqt.selenium;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * FileHandler for I/O of the Philosophersgame.
 *
 * @author Stefan Winterberger
 * @version 1.0.0
 */
public class FileHandler {

    private final String settingsFilename = "settings.properties";
    private final String filePath = System.getProperty("user.dir");
    private final String INPUT_FILENAME = "Wikipages Input.csv";
    private final String OUTPUT_FILENAME = "Results.csv";

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

    public ArrayList<String> readStartPagesFromFile() {

        ArrayList<String> pages = new ArrayList<>();

        try {
            FileReader fr = new FileReader(INPUT_FILENAME);
            BufferedReader in = new BufferedReader(fr);
            String line = in.readLine();
            while (line != null) {
                String page = (line.split(","))[0];
                pages.add("https://de.wikipedia.org/wiki/" + page);
                line = in.readLine();
            }

        } catch (FileNotFoundException e1) {
            createDefaultInputFile();
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return pages;
    }

    public ArrayList<Integer> readMaxClicksFromFile() {
        ArrayList<Integer> clicks = new ArrayList<>();

        try {
            FileReader fr = new FileReader(INPUT_FILENAME);
            BufferedReader in = new BufferedReader(fr);
            String line = in.readLine();
            while (line != null) {
                int maxClicks = Integer.parseInt((line.split(","))[1].trim());
                clicks.add(maxClicks);
                line = in.readLine();
            }
        } catch (FileNotFoundException e1) {
            createDefaultInputFile();
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return clicks;
    }

    private void createDefaultSettingsFile() {
        try {
            File settingsFile = new File(filePath + File.separator + settingsFilename);
            settingsFile.createNewFile();

            FileWriter writer = new FileWriter(settingsFile);
            writer.append("pathToFirefoxExe=C:\\\\Program Files (x86)\\\\Mozilla Firefox\\\\firefox.exe\n");
            writer.append("endPage=Philosophie – Wikipedia");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void createDefaultInputFile() {

    }
}
