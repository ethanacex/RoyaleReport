module com.ethanace {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires org.tinylog.api;
    requires java.desktop;
    requires java.net.http;

    opens com.ethanace to javafx.fxml;
    exports com.ethanace;
}
