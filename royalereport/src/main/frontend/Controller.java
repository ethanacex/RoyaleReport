package main.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.client.Model;

import java.io.IOException;

public class Controller {

    @FXML private Label ipLabel;
    @FXML private TextField clanTagField;
    @FXML private TextField ipTextField;
    @FXML private TextArea authTokenField;
    @FXML private ComboBox<String> dropDown;
    @FXML private ListView<String> favourites;

    private Model model = new Model();

    @FXML
    private void initialize() {
        getPublicIP();
        load();
    }

    @FXML
    public void download() {
        model.downloadReport(clanTagField.getText(), dropDown.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void useSelectedItem() {
        clanTagField.setText(favourites.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void load() {
//        model.loadDatabase();
//        ipTextField.setText(model.getSavedIP());
//        authTokenField.setText(model.getSavedToken());
//        favourites.getItems().addAll(model.getFavourites());
    }

    @FXML
    private void save() {
        model.saveUserData(ipTextField.getText().trim(), authTokenField.getText().trim(), favourites.getItems());
    }

    @FXML
    private void addFavourite() {
        if (!clanTagField.getText().isEmpty() && !favourites.getItems().contains(clanTagField.getText())) {
            favourites.getItems().add(clanTagField.getText().trim());
        }
    }

    @FXML
    private void deleteFavourite() {
        favourites.getItems().remove(favourites.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void getPublicIP() {
        ipLabel.setText(model.getPublicIP());
    }

}