module com.ethanace.royalereport {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires org.tinylog.api;
    requires java.desktop;
    requires java.net.http;

    opens com.ethanace.royalereport to javafx.fxml;
    exports com.ethanace.royalereport;
}
