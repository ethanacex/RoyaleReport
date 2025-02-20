module com.ethanace {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive org.json;
    requires org.tinylog.api;
    requires java.desktop;
    requires java.net.http;
    requires java.datatransfer;

    opens com.ethanace to javafx.fxml;
    exports com.ethanace;
}
