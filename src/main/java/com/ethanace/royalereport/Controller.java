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
    private Label ipLabel;
    @FXML
    private TextField ipField;
    @FXML
    private TextArea authField;
    @FXML
    private ComboBox<Report> reportList;
    @FXML
    private TextField clanTagField;
    @FXML
    private ListView<String> favouritesList;

    private IOModel ioModel;
    private NetModel netModel;
    
    public enum Report {
        WAR_PERFORMANCE("War Performance", 1),
        WAR_READINESS("War Readiness", 2),
        PDK("PDK Report", 3);
    
        private final String displayText;
        private final int value;
    
        Report(String displayText, int value) {
            this.displayText = displayText;
            this.value = value;
        }
    
        public String getDisplayText() {
            return displayText;
        }
    
        public int getValue() {
            return value;
        }
    
        @Override
        public String toString() {
            return displayText;
        }
    }

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
        ioModel.writeToFile(favouritesList.getItems(), ipField.getText(), authField.getText());
    }
    
    @FXML
    private void loadPreferences() {
        List<String> properties = ioModel.getFavourites();
        ObservableList<String> favourites = FXCollections.observableArrayList(properties);
        favouritesList.setItems(favourites);
        ipField.setText(ioModel.getLocalIP());
        authField.setText(ioModel.getAuthToken());
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
        Report reportType = reportList.getSelectionModel().getSelectedItem();
        ReportModel reportModel = new ReportModel();
        boolean successful = false;
        switch (reportType) {
            case Report.WAR_PERFORMANCE -> {
                System.out.println(reportType);
                successful = reportModel.buildPerformanceReport(clanTagField.getText(), authField.getText());
            }
            case Report.WAR_READINESS -> System.out.println(reportType);
            case Report.PDK -> System.out.println(reportType);
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
        
        loadPreferences();
        
        ObservableList<Report> items = FXCollections.observableArrayList(
                Report.WAR_PERFORMANCE,
                Report.WAR_READINESS,
                Report.PDK
        );
        
        reportList.setItems(items);
        reportList.getSelectionModel().selectFirst();
        
        ipLabel.setText(netModel.getPublicIPAddress());
    }    
}
