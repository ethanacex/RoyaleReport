package com.ethanace.royalereport;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * @author ethanace
 */
public class Controller implements Initializable {
    
    @FXML
    private Label ip;
    @FXML
    private TextArea auth;
    @FXML
    private ComboBox<String> reportList;
    @FXML
    private TextField clanTagField;
    @FXML
    private TextField ipField;
    @FXML
    private ListView<String> favouritesList;

    private IOModel ioModel;
    private NetModel netModel;
    
    @FXML
    private void addToFavourites() {
        if (!favouritesList.getItems().contains(clanTagField.getText())) {
            favouritesList.getItems().add(clanTagField.getText());
        }
    }
    
    @FXML
    private void removeFromFavourites() {
        favouritesList.getItems().remove(favouritesList.getSelectionModel().getSelectedItem());
    }
    
    @FXML
    private void savePreferences() {
        ioModel.writeToFile(favouritesList.getItems(), ipField.getText(), auth.getText());
    }
    
    @FXML
    private void loadFavourites() {
        List<String> properties = ioModel.getFavourites();
        ObservableList<String> favourites = FXCollections.observableArrayList(properties);
        favouritesList.setItems(favourites);
    }
    
    @FXML
    private void loadSettings() {
        ipField.setText(ioModel.getLocalIP());
        auth.setText(ioModel.getAuthToken());
    }
    
    @FXML
    private void populateClanTag() {
        try {
            clanTagField.setText(favouritesList.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            // TODO: create an alert window
            System.out.println("Make a valid selection");
        }
    }
    
    @FXML
    private void buildReport() {
        String reportType = reportList.getSelectionModel().getSelectedItem();
        ReportModel reportModel = new ReportModel();
        boolean successful = false;
        switch (reportType) {
            case "War Performance" -> {
                System.out.println(reportType);
                successful = reportModel.buildPerformanceReport(clanTagField.getText(), auth.getText());
            }
            case "War Readiness" -> System.out.println(reportType);
            case "PDK Report" -> System.out.println(reportType);
            default -> System.out.println("Error"); //TODO: Alert error
        }
        if (successful) {
            // Alert success
        } else {
            // Alert failure
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        ioModel = new IOModel();
        netModel = new NetModel();
        
        loadFavourites();
        loadSettings();
        
        ObservableList<String> items = FXCollections.observableArrayList(
                "War Performance",
                "War Readiness",
                "PDK Report"
        );
        
        reportList.setItems(items);
        reportList.getSelectionModel().selectFirst();
        
        ip.setText(netModel.getPublicIPAddress());
    }    
}
