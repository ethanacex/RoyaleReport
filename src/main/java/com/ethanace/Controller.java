package com.ethanace;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.tinylog.Logger;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
    @FXML
    private TableView<ObservableList<Object>> tableView;
    @FXML
    private ProgressBar progressBar;

    private IOModel IO_MODEL;
    private NetModel NET_MODEL;
    private ReportModel REPORT_MODEL;

    public enum ActionRequest {
        POPULATE_TABLE("Populate Table", 1),
        BUILD_REPORT("Build Report", 2);

        private final String displayText;
        private final int value;

        ActionRequest(String displayText, int value) {
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

    public enum Report {
        CLAN_PERFORMANCE("Clan Performance", 1),
        PLAYER_PERFORMANCE("Player Performance", 2),
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
        try {
            IO_MODEL.saveToProperties(favouritesList.getItems(), ipField.getText(), authField.getText());
            alertUser(AlertType.INFORMATION, "Preferences saved successfully");
        } catch (IOException e) {
            alertUser(AlertType.ERROR, e.getMessage());
        }
    }

    @FXML
    private void copyToClipboard() {
        IO_MODEL.copyToClipboard(ipLabel.getText());
        alertUser(AlertType.INFORMATION, "IP copied to clipboard");
    }

    @FXML
    private void loadPreferences() {
        List<String> properties = IO_MODEL.getFavourites();
        ObservableList<String> favourites = FXCollections.observableArrayList(properties);
        favouritesList.setItems(favourites);
        ipField.setText(IO_MODEL.getLocalIP());
        authField.setText(IO_MODEL.getAuthToken());
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
    private void getNewToken() {
        try {
            NET_MODEL.openSupercellDevSite();
        } catch (Exception e) {
            alertUser(AlertType.ERROR, e.getMessage());
        }
    }

    private void processRequest(TableData tableData, Report reportType, ActionRequest action) throws Exception {

        switch (action) {
            case POPULATE_TABLE -> {
                Logger.info("Populating table");
                tableView.getColumns().clear();
                tableView.getItems().clear();
        
                List<String> columnHeaders = tableData.getColumnHeaders();
                ObservableList<ObservableList<Object>> rowData = tableData.getRowData();

                Logger.debug("Column headers: " + columnHeaders.toString());
                Logger.debug("Row data: " + tableData.getRowData().toString());

                for (int i = 0; i < columnHeaders.size(); i++) {
                    int columnIndex = i;
                    TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(columnHeaders.get(i));
                
                    Logger.debug("Column index: " + columnIndex);
                    Logger.info("Adding column: " + columnHeaders.get(i));
                    column.setCellValueFactory(cellData -> {
                        Object value = cellData.getValue().get(columnIndex);
                        Logger.info("Value found: " + value);

                        if (value instanceof Integer) {
                            return new SimpleObjectProperty<>(Integer.valueOf(value.toString()));
                        } else if (value instanceof Float || value instanceof Double) {
                            return new SimpleObjectProperty<>(Float.valueOf(value.toString()));
                        } else {
                            return new SimpleObjectProperty<>(cellData.getValue().get(columnIndex));
                        }
                    });
        
                    tableView.getColumns().add(column);
                }
        
                tableView.setItems(rowData);
            }
            case BUILD_REPORT -> {
                IO_MODEL.writeCsv(tableData.getRowData(), tableData.getColumnHeaders(), reportType.toString());
            }
            default ->
                alertUser(AlertType.ERROR, "Alert: Unknown report type");
        }
    }

    private void getTableData(ActionRequest action) {

        try {
            Report reportType = reportList.getSelectionModel().getSelectedItem();
            String clan = clanTagField.getText();
            String auth = authField.getText();

            Task<TableData> task;
            task = new Task<TableData>() {
                @Override
                protected TableData call() throws Exception {
                    switch (reportType) {
                        case CLAN_PERFORMANCE -> {
                            return REPORT_MODEL.getClanReport(clan, auth);
                        }
                        case PLAYER_PERFORMANCE -> {
                            Logger.info(reportType); return null;
                        }
                        case PDK -> {
                            Logger.info(reportType); return null;
                        }
                        default -> throw new Exception("Unknown report type");
                    }
                }
            };

            task.setOnSucceeded(event -> {
                TableData tableData = task.getValue();
                try {
                    processRequest(tableData, reportType, action);
                } catch (Exception e) {
                    alertUser(AlertType.ERROR, e.getMessage());
                }
            });

            task.setOnFailed(event -> alertUser(AlertType.ERROR, task.getException().getMessage()));

            new Thread(task).start();

        } catch (Exception e) {
            alertUser(AlertType.ERROR, e.getMessage());
        }


    }

    @FXML
    private void populateTable() {
        getTableData(ActionRequest.POPULATE_TABLE);
    }

    @FXML
    private void buildReport() {
        getTableData(ActionRequest.BUILD_REPORT);
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
            IO_MODEL = new IOModel();
            NET_MODEL = new NetModel();
            REPORT_MODEL = new ReportModel(NET_MODEL, IO_MODEL);
        } catch (IOException e) {
            alertUser(AlertType.ERROR, e.getMessage());
            return;
        } catch (Exception e) {
            alertUser(AlertType.ERROR, e.getMessage());
            return;
        }

        loadPreferences();

        ObservableList<Report> items = FXCollections.observableArrayList(
                Report.CLAN_PERFORMANCE,
                Report.PLAYER_PERFORMANCE,
                Report.PDK
        );

        reportList.setItems(items);
        reportList.getSelectionModel().selectFirst();

        try {
            ipLabel.setText(NET_MODEL.getPublicIP());
        } catch (Exception e) {
            alertUser(AlertType.ERROR, e.getMessage());
        }
    }
}
