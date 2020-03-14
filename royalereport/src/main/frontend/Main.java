package main.frontend;

import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.client.UserNotify;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage window) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("../frontend/view.fxml"));

        window.setTitle("Royale Report");
        window.setScene(new Scene(root));
        window.setResizable(false);

        try {
            window.getIcons().add(new Image(Main.class.getResourceAsStream("../resources/crown.png")));
        } catch(Exception e) {
            UserNotify.alertResourceError().showAndWait();
        }

        window.show();

    }


}
