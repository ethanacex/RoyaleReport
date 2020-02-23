package client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

class View {

    private Controller controller = new Controller();

    void initialise(Stage window) {

        // region UI Initalization

        // Menu bar and items //
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem newToken = new MenuItem("New Credentials");
        MenuItem loadToken = new MenuItem("Load Credentials");
        MenuItem defaultToken = new MenuItem("Set default access");
        MenuItem quit = new MenuItem("Quit");
        fileMenu.getItems().addAll(newToken, loadToken, defaultToken, quit);
        menuBar.getMenus().add(fileMenu);

        // Disclaimer //
        Label disclaimer = new Label("Icons courtesy of https://icons8.com");
        disclaimer.setTextFill(Color.LIGHTGREY);
        disclaimer.setFont(new Font(10));

        // Clan Name TextField //
        TextField txtField = new TextField();
        txtField.setPromptText("eg. #V8Q998J");
        Label clan = new Label("Clan Tag:");
        VBox clanBox = new VBox(5, clan, txtField);

        // Report Type ComboBox //
        ComboBox<String> dropDown = new ComboBox<>();
        dropDown.getItems().addAll("War Performance", "War Readiness", "PDK Report");
        dropDown.getSelectionModel().selectFirst();
        Label report = new Label("Report Type:");
        VBox reportBox = new VBox(5, report, dropDown);

        // Input Boxes //
        VBox inputs = new VBox(10, clanBox, reportBox);

        // Generate Button //
        Button button = new Button("Generate Report");
        VBox submit = new VBox(button);
        submit.setPadding(new Insets(5,0,0,0));
        submit.setAlignment(Pos.CENTER);

        // Root //
        VBox root = new VBox(15, inputs, submit, disclaimer);
        root.setPadding(new Insets(0,10,0,10));
        root.setAlignment(Pos.CENTER);

        button.setOnAction(e -> buttonAction(txtField, dropDown));

        root.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)  {
                buttonAction(txtField, dropDown);
            }
        });

        // Applying Window Icon //
        try {
            Image icon = new Image(String.valueOf(this.getClass().getClassLoader().getResource("crown.png")));
            window.getIcons().add(icon);
        } catch(Exception e) {
            AlertStatus.alertResourceError().showAndWait();
        }

        // Attempt to load default data //
        controller.loadCredentials();

        // Window Properties //
        window.setTitle("Manager");
        window.setScene(new Scene(root));
        window.setResizable(false);
        window.setMinHeight(220);
        window.setMaxWidth(240);

        dropDown.setMinWidth(window.getMaxWidth()-20);
        txtField.setMinWidth(window.getMaxWidth()-20);

        window.show();
        //endregion
    }

    public void displayAdminPanel() {

        // region Admin Panel Initilization

        // Create Stage //
        Stage window = new Stage();

        // IP Address TextField //
        TextField ipTextField = new TextField();
        ipTextField.setPromptText("e.g. XX.XX.XX.XX");
        Label ipLabel = new Label("Public IP Address:");
        VBox ipBox = new VBox(5, ipLabel, ipTextField);

        // Authorisation Token TextField //
        TextField authTextField = new TextField();
        authTextField.setPromptText("e.g. eyJ0eXAiOiJKV1QiLCJhbGciOiJr...");
        Label authLabel = new Label("Authorisation Token:");
        VBox authBox = new VBox(5, authLabel, authTextField);

        // Input Boxes //
        VBox inputs = new VBox(10, ipBox, authBox);

        // Save & Load //
        Button save = new Button("Save Credentials");
        Button load = new Button("Load Credentials");
        HBox buttons = new HBox(5, save, load);
        buttons.setPadding(new Insets(5,0,10,0));
        buttons.setAlignment(Pos.CENTER);

        // Root //
        VBox root = new VBox(15, inputs, buttons);
        root.setPadding(new Insets(0,10,0,10));
        root.setAlignment(Pos.CENTER);

        save.setOnAction(e -> {
            if (authenticate(ipTextField, authTextField)) {
                controller.saveCredentials();
                window.close();
            }
        });

        load.setOnAction(e -> {
            if (controller.loadCredentials()) {
                window.close();
            }
        });

        // Applying Window Icon //
        try {
            Image icon = new Image(String.valueOf(this.getClass().getClassLoader().getResource("admin.png")));
            window.getIcons().add(icon);
        } catch(Exception e) {
            AlertStatus.alertResourceError().showAndWait();
        }

        // Window Properties //
        window.setTitle("Admin Panel");
        window.setScene(new Scene(root));
        window.setResizable(false);
        window.setMinHeight(180);
        window.setMaxWidth(240);

        ipTextField.setMinWidth(window.getMaxWidth()-20);
        authTextField.setMinWidth(window.getMaxWidth()-20);

        window.show();
        //endregion
    }

    private boolean authenticate(TextField ipTextField, TextField authTextField) {
        return controller.authenticate(ipTextField, authTextField);
    }

    private void buttonAction(TextField txtField, ComboBox<String> dropDown) {
        if (controller.unlockAdmin(txtField, dropDown)) {
            displayAdminPanel();
        }
    }

}
