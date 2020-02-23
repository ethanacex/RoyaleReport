package client;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public abstract class AlertStatus {

    private static final Alert.AlertType INFO = Alert.AlertType.INFORMATION;
    private static final Alert.AlertType WARNING = Alert.AlertType.WARNING;

    private static Alert createAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setContentText(content);
        alert.setHeaderText(null);
        alert.setTitle(title);
        return alert;
    }

    public static Alert alertOperationSuccess() {
        return createAlert(INFO, "Success", "Operation completed successfully.");
    }

    public static Alert alertLoadSuccess() {
        return createAlert(INFO, "Success", "Operation completed successfully.");
    }

    public static Alert alertNotFoundError() {
        return createAlert(WARNING,"Error", "No default credentials found.");
    }

    public static Alert alertWriteError() {
        return createAlert(WARNING,"Error", "Write failure, file may be in use.");
    }

    public static Alert alertDirectoryBuildError() {
        return createAlert(WARNING,"Error",
                "Failed to create directory, please check write permissions.");
    }

    public static Alert alertOverwriteWarning() {
        return createAlert(INFO, "Warning",
                "Files in output path with the same name will be overwritten.");
    }

    public static Alert alertCredentialsSaved() {
        return createAlert(INFO, "Authorisation Updated",
                "Security clearance changed, new IP and authorisation saved.");
    }

    public static Alert alertInputError() {
        return createAlert(INFO, "Input required", "Text field must not be empty");
    }

    public static Alert alertFatalError() {
        return createAlert(WARNING, "Error",
                "A fatal error occurred. Please check your connection and access privileges.");
    }

    public static Alert alertServerError() {
        return createAlert(WARNING, "Error",
                "Could not fetch information from server.");
    }

    public static Alert alertResourceError() {
        return(createAlert(WARNING, "Error",
                "Icon resource could not be loaded."));
    }

    public static Alert alertAdminWarning() {
        return(createAlert(WARNING, "Admin",
                "You have activated the admin panel, you will need:\n\n" +
                        "Your Public IP Address\n" +
                        "Your Authorisation Token\n\n" +
                        "Press OK to continue"));
    }

}
