package com.ethanace;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.tinylog.Logger;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author ethanace
 */
public class Controller implements Initializable {

    @FXML
    private Label ipLabel;
    @FXML
    private Label httpStatus;
    @FXML
    private Label warningLabel;
    @FXML
    private ImageView warningIcon;
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

    private final StringProperty ipValue = new SimpleStringProperty();
    private final StringProperty labelValue = new SimpleStringProperty();

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
        WAR_PERFORMANCE("War Performance", 2),
        PLAYER_ACTIVITY("Player Activity", 3);

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

// Create a method to unbind the progress bar
    private void unbindProgressBar() {
        if (progressBar.progressProperty().isBound()) {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        }
    }

// Create a method to reset and bind the progress bar for a new task
    private void bindProgressBar(Task<?> task) {
        progressBar.progressProperty().bind(task.progressProperty());
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
        String selectedItem = favouritesList.getSelectionModel().getSelectedItem();
        if (checkValidSelection(selectedItem)) {
            try {
                clanTagField.setText(favouritesList.getSelectionModel().getSelectedItem());
            } catch (Exception e) {
                alertUser(AlertType.ERROR, e.getMessage());
            }
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
                    Logger.debug("Adding column: " + columnHeaders.get(i));
                    column.setCellValueFactory(cellData -> {
                        Object value = cellData.getValue().get(columnIndex);
                        Logger.trace("Value found: " + value);

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
                alertUser(AlertType.ERROR, "Unknown report type requested");
        }
    }

    private void getTableData(ActionRequest action) {

        try {
            Report reportType = reportList.getSelectionModel().getSelectedItem();
            String clan = clanTagField.getText();
            String auth = authField.getText();

            switch (reportType) {
                case CLAN_PERFORMANCE -> {

                    unbindProgressBar();
                    Task<TableData> task = REPORT_MODEL.getClanReport(clan, auth);
                    bindProgressBar(task);

                    task.setOnSucceeded(event -> {
                        TableData tableData = task.getValue();
                        httpStatus.setVisible(true);
                        httpStatus.setText("Connection OK");
                        try {
                            processRequest(tableData, reportType, action);
                        } catch (Exception e) {
                            alertUser(AlertType.ERROR, e.getMessage());
                        }
                    });

                    task.setOnFailed(event -> {
                        unbindProgressBar();
                        Throwable exception = task.getException();
                        if (exception != null) {
                            httpStatus.setVisible(true);
                            httpStatus.setText(exception.getMessage());
                            alertUser(AlertType.ERROR, exception.getMessage());
                            Logger.error("Task failed with exception: " + exception.getMessage(), exception);
                        } else {
                            Logger.error("Task failed, but no exception was set.");
                        }
                    });
                    new Thread(task).start();
                }
                case WAR_PERFORMANCE -> {
                    unbindProgressBar();
                    Task<TableData> task = REPORT_MODEL.getPlayerReport(clan, auth);
                    bindProgressBar(task);

                    task.setOnSucceeded(event -> {
                        TableData tableData = task.getValue();
                        httpStatus.setVisible(true);
                        httpStatus.setText("Connection OK");
                        try {
                            processRequest(tableData, reportType, action);
                        } catch (Exception e) {
                            alertUser(AlertType.ERROR, e.getMessage());
                        }
                    });

                    task.setOnFailed(event -> {
                        unbindProgressBar();
                        Throwable exception = task.getException();
                        if (exception != null) {
                            httpStatus.setVisible(true);
                            httpStatus.setText(exception.getMessage());
                            alertUser(AlertType.ERROR, exception.getMessage());
                            Logger.error("Task failed with exception: " + exception.getMessage(), exception);
                        } else {
                            Logger.error("Task failed, but no exception was set.");
                        }
                    });
                    new Thread(task).start();
                }
                case PLAYER_ACTIVITY -> {
                    unbindProgressBar();
                    Task<TableData> task = REPORT_MODEL.getPlayerActivityReport(clan, auth);
                    bindProgressBar(task);

                    task.setOnSucceeded(event -> {
                        TableData tableData = task.getValue();
                        httpStatus.setVisible(true);
                        httpStatus.setText("Connection OK");
                        try {
                            processRequest(tableData, reportType, action);
                        } catch (Exception e) {
                            alertUser(AlertType.ERROR, e.getMessage());
                        }
                    });

                    task.setOnFailed(event -> {
                        unbindProgressBar();
                        Throwable exception = task.getException();
                        if (exception != null) {
                            httpStatus.setVisible(true);
                            httpStatus.setText(exception.getMessage());
                            alertUser(AlertType.ERROR, exception.getMessage());
                            Logger.error("Task failed with exception: " + exception.getMessage(), exception);
                        } else {
                            Logger.error("Task failed, but no exception was set.");
                        }
                    });
                    new Thread(task).start();
                }
                default -> {
                    throw new Exception("Unknown report type");
                }
            }

        } catch (Exception e) {
            alertUser(AlertType.ERROR, e.getMessage());
            Logger.error("general error");
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
            Logger.error(message);
        } else {
            alert.setTitle("Notification");
            alert.setHeaderText("Information");
            Logger.info(message);
        }

        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateWarnings() {

        String ipFieldText = (ipField.getText() == null) ? "" : ipField.getText().trim();
        String ipLabelText = (ipLabel.getText() == null) ? "" : ipLabel.getText().trim();

        if (ipFieldText.equalsIgnoreCase(ipLabelText)) {
            warningLabel.setText("Local IP matches corresponding authentication IP");
            warningLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            warningIcon.setImage(new Image(getClass().getResource("/com/ethanace/images/check-94.png").toExternalForm()));
        } else {
            warningLabel.setText("Warning: Mismatch with IP associated with token");
            warningLabel.setTextFill(javafx.scene.paint.Color.RED);
            warningIcon.setImage(new Image(getClass().getResource("/com/ethanace/images/alert-94.png").toExternalForm()));
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        System.setProperty("tinylog.writerFile.file", System.getProperty("user.home") + "/RoyaleReport/System/output.log");

        // Bind the properties to the UI components
        ipValue.bindBidirectional(ipField.textProperty());
        labelValue.bindBidirectional(ipLabel.textProperty());

        // Add listeners to detect changes
        ipValue.addListener((obs, oldVal, newVal) -> updateWarnings());
        labelValue.addListener((obs, oldVal, newVal) -> updateWarnings());

        httpStatus.setVisible(false);

        try {
            IO_MODEL = new IOModel();
            NET_MODEL = new NetModel();
            REPORT_MODEL = new ReportModel(NET_MODEL);
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
                Report.WAR_PERFORMANCE,
                Report.PLAYER_ACTIVITY
        );

        reportList.setItems(items);
        reportList.getSelectionModel().selectFirst();

        try {
            ipLabel.setText(NET_MODEL.getPublicIP());
            updateWarnings();
        } catch (Exception e) {
            alertUser(AlertType.ERROR, e.getMessage());
        }
    }

    private boolean checkValidSelection(Object selectedItem) {
        return selectedItem != null && selectedItem != "";
    }
}
