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

import javafx.collections.ObservableList;

/**
 * @author ethanace
 */

public class IOModel {

    public interface IOModelCallback {
        void onLoadSuccess();
    }

    private IOModelCallback callback;
    private List<String> favourites;
    private String authToken;
    private String localIp;
    private String saveDir;


    public IOModel(IOModelCallback callback) throws IOException {
        this.callback = callback;

        String path = new File(
            IOModel.class.getProtectionDomain()
            .getCodeSource().getLocation().getPath())
                .getParent();
        saveDir = Paths.get(path, "save.properties").toString();
        favourites = new ArrayList<>();
        try {
            loadFromFile();
            if (callback != null) {
                callback.onLoadSuccess();
            }
        } catch (IOException e) {
            throw e;
        }
        
    }

    private boolean isValidInput(String input) {
        return input != null && !input.isBlank();
    }

    private void loadFromFile() throws IOException {
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
            throw new IOException("Error when loading file", e);
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

    public void saveToProperties(ObservableList<String> selectedItems, String ip, String auth) {

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
    
    private FileWriter initFileTemplate(String fileName, String[] columns) throws IOException {
        String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
        File file = new File(path, fileName + ".csv");
        try {
            boolean dirCreated = file.getParentFile().mkdirs();
            if (!dirCreated) {
                System.out.println("Operation will overwrite existing files");
            }
        } catch (SecurityException e) {
            System.out.println("Could not create save report directory");
        }
        FileWriter csv = new FileWriter(file);
        for (String header : columns) {
            csv.append(header);
            csv.append(",");
        }
        csv.append("\n");
        return csv;
    }

    public void writeToFile(ArrayList<String> data, String[] headers, String filename) {

        try (FileWriter csv = initFileTemplate(filename, headers)) {

            for (String row : data) {
                csv.append(row);
                csv.append("\n");
            }

            csv.flush();
            csv.close();
        } catch (IOException e) {
            System.out.println("Alert: Some error occurred when writing to file, this needs to be an alert");
        }

        System.out.println("Alert success");
        locateFile();
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("win"));
    }

    private boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("mac"));
    }


    @SuppressWarnings("deprecation")
    private void locateFile() {
        if (isWindows()) {
            try {
                String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
                Desktop.getDesktop().open(new File(path));
            } catch (IOException e) {
                System.out.println("Alert Operation successful");
            }

        } else if (isMac()) {
            try {
                String path = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "RoyaleReport";
                Runtime.getRuntime().exec("open " + path);
            } catch (IOException e) {
                System.out.println("Alert Directory cannot be located");
            }

        }
    }

}

