package com.ethanace.royalereport;

import com.ethanace.royalereport.NetModel.NetworkException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
        ioModel.saveToProperties(favouritesList.getItems(), ipField.getText(), authField.getText());
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
            alertUser(AlertType.ERROR, "Make a valid selection");
        }
    }
    
    @FXML
    private void buildReport() {
        try {

            Report reportType = reportList.getSelectionModel().getSelectedItem();
            ReportModel reportModel = new ReportModel();
            String clan = clanTagField.getText();
            String auth = authField.getText();

            switch (reportType) {
                case Report.WAR_PERFORMANCE -> reportModel.buildPerformanceReport(clan, auth);
                case Report.WAR_READINESS -> System.out.println(reportType);
                case Report.PDK -> System.out.println(reportType);
                default -> throw new IOException("Unknown report type");
            }

        } catch (IOException e) {
            alertUser(AlertType.ERROR, e.getMessage());
        }
    }

    public void alertUser(AlertType type, String message) {

        Alert alert = new Alert(type);

        if (type == AlertType.ERROR) {
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred");
        } else {
            alert.setTitle("Notification");
            alert.setHeaderText("Information");
        }
        
        alert.setContentText(message);
        alert.showAndWait();

    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            ioModel = new IOModel(() -> {
                alertUser(AlertType.INFORMATION, "Load was successful");
            });
        } catch (IOException e) {
            alertUser(AlertType.ERROR, e.getMessage());
        }
        
        netModel = new NetModel();
        
        loadPreferences();
        
        ObservableList<Report> items = FXCollections.observableArrayList(
                Report.WAR_PERFORMANCE,
                Report.WAR_READINESS,
                Report.PDK
        );
        
        reportList.setItems(items);
        reportList.getSelectionModel().selectFirst();
        
        try {
            ipLabel.setText(netModel.getPublicIPAddress());
        } catch (NetworkException e) {
            alertUser(AlertType.ERROR, e.getMessage());
        }
    }    
}
