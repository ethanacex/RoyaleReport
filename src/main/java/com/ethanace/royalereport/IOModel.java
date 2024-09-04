package com.ethanace.royalereport;

import java.awt.Desktop;
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

/**
 * @author ethanace
 */

public class IOModel {

    private List<String> favourites;
    private String authToken;
    private String localIp;
    private String saveDir;

    
    public IOModel() throws IOException {

        String path = new File(
            IOModel.class.getProtectionDomain()
            .getCodeSource().getLocation().getPath())
                .getParent();
        saveDir = Paths.get(path, "save.properties").toString();
        favourites = new ArrayList<>();

        Logger.info("IOModel initialised successfully");

        try {
            loadFromFile();
        } catch (IOException e) {
            throw e;
        }
        
    }

    @SuppressWarnings("deprecation")
    private void locateFile() throws IOException {
        if (isWindows()) {
            try {
                String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
                Desktop.getDesktop().open(new File(path));
            } catch (IOException e) {
                throw new IOException("An error occurred when locating file", e);
            }

        } else if (isMac()) {
            try {
                String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
                Runtime.getRuntime().exec("open " + path);
            } catch (IOException e) {
                throw new IOException("An error occurred when locating file", e);
            }

        }
    }

    private FileWriter initFileTemplate(String fileName, String[] columns) throws Exception {
        String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
        File file = new File(path, fileName + ".csv");
        try {
            boolean dirCreated = file.getParentFile().mkdirs();
            if (!dirCreated) {
                System.out.println("Operation will overwrite existing files");
            }
        } catch (SecurityException e) {
            throw new Exception("Could not create save report directory", e);
        }
        FileWriter csv = new FileWriter(file);
        for (String header : columns) {
            csv.append(header);
            csv.append(",");
        }
        csv.append("\n");
        return csv;
    }

    private boolean isValidInput(String input) {
        return input != null && !input.isBlank();
    }

    private void loadFromFile() throws IOException {
        Properties properties = new Properties();
        File file = new File(saveDir);
        
        if (!file.exists()) {
            try (FileOutputStream out = new FileOutputStream(file)) {
                properties.store(out, "Favourites");
            } catch (IOException e) {
                throw new IOException("Failed to create an empty properties file at " + saveDir, e);
            }
        }
    
        try (FileInputStream in = new FileInputStream(file)) {
            properties.load(in);
            authToken = properties.getProperty("auth");
            localIp = properties.getProperty("ip");
            properties.remove("auth");
            properties.remove("ip");
            for (String key : properties.stringPropertyNames()) {
                favourites.add(properties.getProperty(key));
            }
        } catch (IOException e) {
            throw new IOException("Error when loading file", e);
        }
    }
    
    private boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("win"));
    }

    private boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("mac"));
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

        try (FileOutputStream out = new FileOutputStream(saveDir)) {
            properties.store(out, "Favourites");
        } catch (IOException e) {
            throw new IOException("I/O Error when writing to file", e);
        }
    }
    
    public void writeToFile(ArrayList<String> data, String[] headers, String filename) throws Exception {

        try (FileWriter csv = initFileTemplate(filename, headers)) {

            for (String row : data) {
                csv.append(row);
                csv.append("\n");
            }

            csv.flush();
            csv.close();
        } catch (IOException e) {
            throw new IOException("Alert: Some error occurred when writing to file.", e);
        }
        locateFile();
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
 
}

