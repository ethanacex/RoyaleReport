package com.ethanace.royalereport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javafx.collections.ObservableList;

/**
 * @author ethanace
 */

public class IOModel {
    
    private List<String> favourites;
    private String authToken;
    private String localIp;
    private String saveDir;

    
    public IOModel() {
        try {
            String path = new File(
                IOModel.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath())
                    .getParent();
            saveDir = Paths.get(path, "save.properties").toString();
            favourites = new ArrayList<>();
            loadFromFile();
        } catch (Exception e) {
            System.out.println("Exception occurred on IOModel initialisation");
            // needs to be an alert
        }
    }

    private boolean isValidInput(String input) {
        return input != null && !input.isBlank();
    }

    private void loadFromFile() {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(saveDir)) {
            properties.load(in);
            authToken = properties.getProperty("auth");
            localIp = properties.getProperty("ip");
            properties.remove("auth");
            properties.remove("ip");
            for (String key : properties.stringPropertyNames()) {
                favourites.add(properties.getProperty(key));
            }
            System.out.println("File loaded from " + saveDir); // TODO: Alert
        } catch (IOException e) {
            System.out.println("Error when loading file");
            //TODO: Alert failure
        }
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public String getLocalIP() {
        return localIp;
    }
    
    public List<String> getFavourites() {
        return favourites;
    }

    public void writeToFile(ObservableList<String> selectedItems, String ip, String auth) {

        Properties properties = new Properties();

        properties.setProperty("ip", isValidInput(ip) ? ip : "");
        properties.setProperty("auth", isValidInput(auth) ? auth : "");
        
        int index = 0;
        for (Object selectedItem : selectedItems) {
            index++;
            properties.setProperty(Integer.toString(index), selectedItem.toString());
        }

        try (FileOutputStream out = new FileOutputStream(saveDir)) {
            properties.store(out, "Favourites");
            System.out.println("Data written to properties file successfully.");
            //TODO: Alert success
        } catch (IOException e) {
            System.out.println("IO Error when writing to file");
            //TODO: Alert failure
        }
    }
    
}

