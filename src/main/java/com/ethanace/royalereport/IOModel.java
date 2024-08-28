package com.ethanace.royalereport;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javafx.collections.ObservableList;

/**
 * @author ethanace
 */

public class IOModel {
    
    private final String SAVE_DIR = getClass().getResource("save.properties").getPath();
    
    private final List<String> favourites;
    private String authToken;
    private String localIp;
    
    public IOModel() {
        favourites = new ArrayList<>();
        loadFromFile();
    }

    private boolean isValidInput(String input) {
        return input != null && !input.isBlank();
    }

    private void loadFromFile() {
        Properties properties = new Properties();
        
        try (FileInputStream in = new FileInputStream(SAVE_DIR)) {
            System.out.println(SAVE_DIR);
            properties.load(in);
            authToken = properties.getProperty("auth");
            localIp = properties.getProperty("ip");
            properties.remove("auth");
            properties.remove("ip");
            for (String key : properties.stringPropertyNames()) {
                favourites.add(properties.getProperty(key));
            }
        } catch (IOException e) {
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

        try (FileOutputStream out = new FileOutputStream(SAVE_DIR)) {
            properties.store(out, "Favourites");
            System.out.println("Data written to properties file successfully.");
        } catch (IOException e) {
            System.out.println("IO Error");
            //TODO: Alert failure
        }
    }
    
}

