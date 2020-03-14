package main.client;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public abstract class Prompt {

    private static final Alert.AlertType INFO = Alert.AlertType.INFORMATION;
    private static final Alert.AlertType WARNING = Alert.AlertType.WARNING;
    private static final Alert.AlertType ERROR = Alert.AlertType.ERROR;

    private static Alert createAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setContentText(content);
        alert.setHeaderText(null);
        alert.setTitle(title);
        return alert;
    }

    public static Alert success(String message) {
        return createAlert(INFO, "Success", message);
    }

    public static Alert error(String message) {
        return createAlert(ERROR, "Error", message);
    }

    public static Alert warning(String message) {
        return createAlert(WARNING, "Warning", message);
    }

}
