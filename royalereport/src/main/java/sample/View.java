package sample;

import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

class View {

    private Controller controller = new Controller();

    void initialise(Stage window) {

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
        ComboBox<String> dropDown = new ComboBox<String>();
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
            controller.alertResourceError().showAndWait();
        }

        // Window Properties //
        window.setTitle("Manager");
        window.setScene(new Scene(root));
        window.setResizable(false);
        window.setMinHeight(220);
        window.setMaxWidth(240);

        dropDown.setMinWidth(window.getMaxWidth()-20);
        txtField.setMinWidth(window.getMaxWidth()-20);

        window.show();
    }

    private void displayAdminPanel() {

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

        // Submit Button //
        Button button = new Button("Submit Credentials");
        VBox submit = new VBox(button);
        submit.setPadding(new Insets(5,0,10,0));
        submit.setAlignment(Pos.CENTER);

        // Root //
        VBox root = new VBox(15, inputs, submit);
        root.setPadding(new Insets(0,10,0,10));
        root.setAlignment(Pos.CENTER);

        button.setOnAction(e -> {
            if (authenticate(ipTextField, authTextField)) {
                controller.alertCredentialsSaved().showAndWait();
                window.close();
            }
        });

        root.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)  {
                if (authenticate(ipTextField, authTextField)) {
                    controller.alertCredentialsSaved().showAndWait();
                    window.close();
                }
            }
        });

        // Applying Window Icon //
        try {
            Image icon = new Image(String.valueOf(this.getClass().getClassLoader().getResource("admin.png")));
            window.getIcons().add(icon);
        } catch(Exception e) {
            controller.alertResourceError().showAndWait();
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
    }

    private boolean authenticate(TextField ipTextField, TextField authTextField) {
        if (ipTextField.getText().isEmpty() || authTextField.getText().isEmpty()) {
            return false;
        } else {
            controller.setIp(ipTextField.getText().trim());
            controller.setToken("Bearer " + authTextField.getText().trim());
            return true;
        }

    }

    private void buttonAction(TextField txtField, ComboBox<String> dropDown) {
        if (!txtField.getText().isEmpty()) {
            if (txtField.getText().equals("admin")) {
                controller.alertAdminWarning().showAndWait();
                displayAdminPanel();
            } else {
                try {
                    controller.buttonHandler(txtField.getText(), dropDown.getValue());
                } catch (UnirestException ex) {
                    controller.alertServerError().showAndWait();
                } catch (Exception ex) {
                    controller.alertFatalError().showAndWait();
                }
            }
        }
        else {
            controller.alertInputError().showAndWait();
        }
    }

}
