package com.ethanace;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.tinylog.Logger;

import javafx.collections.ObservableList;

public class IOModel {

    private List<String> favourites;
    private String authToken;
    private String localIp;
    private String sysDir;
    private String homePath;

    public IOModel() throws IOException {
        init();
    }

    private void init() throws IOException {
        setDirectories();
        createDirectoriesIfNotExists(homePath);
        createDirectoriesIfNotExists(Paths.get(homePath, "System").toString());
        loadProperties();
        Logger.info("IOModel initialised");
    }

    private void setDirectories() {
        homePath = Paths.get(System.getProperty("user.home"), "RoyaleReport").toString();
        sysDir = Paths.get(homePath, "System", "save.properties").toString();
        Logger.info("Home path set to " + homePath);
        Logger.info("Properties directory set to " + sysDir);
    }

    private void createDirectoriesIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            boolean dirCreated = directory.mkdirs();
            Logger.info("Directory created (" + path + "): " + dirCreated);
        } else {
            Logger.info("Directory already exists: " + path);
        }
    }

    @SuppressWarnings("deprecation")
    private void openDirectory(String path) throws IOException {
        try {
            Logger.debug("Platform: " + getPlatform());
            if (isMac()) {
                Runtime.getRuntime().exec("open " + path);
            } else if (isWindows()) {
                Desktop.getDesktop().open(new File(path));
            } else {
                Logger.error("Unsupported OS");
                throw new IOException("Unsupported OS");
            }
        } catch (IOException e) {
            Logger.error("An error occurred when locating file", e);
            throw new IOException("An error occurred when locating file", e);
        }
    }

    private FileWriter initFileTemplate(String fileName, List<String> columns) throws Exception {
        String path = Paths.get(homePath, "Reports").toString();
        createDirectoriesIfNotExists(path); // Ensure Reports directory exists

        File file = new File(path, fileName + ".csv");

        FileWriter csv = new FileWriter(file);
        for (String header : columns) {
            csv.append(header).append(",");
        }
        csv.append("\n");
        Logger.info("File template initialised at " + file.getAbsolutePath());
        return csv;
    }

    private boolean isValidInput(String input) {
        return input != null && !input.isBlank();
    }

    private void loadProperties() throws IOException {
        Properties properties = new Properties();
        File file = new File(sysDir);

        favourites = new ArrayList<>();

        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                properties.load(in);
                authToken = properties.getProperty("auth");
                localIp = properties.getProperty("ip");
                Logger.debug("Loaded auth token: " + authToken);
                Logger.debug("Loaded local IP: " + localIp);

                properties.remove("auth");
                properties.remove("ip");

                for (String key : properties.stringPropertyNames()) {
                    favourites.add(properties.getProperty(key));
                    Logger.debug("Loaded favourite: " + properties.getProperty(key));
                }
            } catch (IOException e) {
                Logger.error("Error when loading file", e);
                throw new IOException("Error when loading file", e);
            }
            Logger.info("Properties loaded successfully");
        } else {
            createDirectoriesIfNotExists(new File(sysDir).getParent());

            try (FileOutputStream out = new FileOutputStream(file)) {
                Logger.info("No properties file found, creating new file");
                properties.store(out, "Favourites");
            } catch (IOException e) {
                Logger.error("Failed to create an empty properties file at " + sysDir, e);
                throw new IOException("Failed to create an empty properties file at " + sysDir, e);
            }
            Logger.info("Properties file created at " + sysDir);
        }
    }

    private String getPlatform() {
        return System.getProperty("os.name");
    }

    private boolean isWindows() {
        return getPlatform().toLowerCase().contains("win");
    }

    private boolean isMac() {
        return getPlatform().toLowerCase().contains("mac");
    }

    public void saveToProperties(ObservableList<String> selectedItems, String ip, String auth) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("ip", isValidInput(ip) ? ip : "");
        properties.setProperty("auth", isValidInput(auth) ? auth : "");

        int index = 0;
        for (String selectedItem : selectedItems) {
            index++;
            properties.setProperty(Integer.toString(index), selectedItem);
        }

        File file = new File(sysDir);
        if (!file.exists()) {
            createDirectoriesIfNotExists(file.getParent());
        }

        try (FileOutputStream out = new FileOutputStream(sysDir)) {
            properties.store(out, "Favourites");
        } catch (IOException e) {
            Logger.error("I/O Error when writing to file", e);
            throw new IOException("I/O Error when writing to file", e);
        }
        Logger.info("Properties saved successfully");
    }

    public void writeCsv(ObservableList<ObservableList<Object>> data, List<String> headers, String filename) throws Exception {
        try (FileWriter csv = initFileTemplate(filename, headers)) {
            for (ObservableList<Object> row : data) {
                for (Object cell : row) {
                    csv.append(cell.toString()).append(",");
                }
                csv.append("\n");
            }
            csv.flush();
        } catch (IOException e) {
            Logger.error("Alert: Some error occurred when writing to file.", e);
            throw new IOException("Alert: Some error occurred when writing to file.", e);
        }
        Logger.info("CSV file written to " + homePath);
        openDirectory(homePath);
    }

    public List<String> getFavourites() {
        return favourites;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getLocalIP() {
        return localIp;
    }

    public void copyToClipboard(String text) {
        Logger.debug("Copied text to clipboard: " + text);
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);
    }
}
