module com.ethanace.royalereport {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires org.json;

    opens com.ethanace.royalereport to javafx.fxml;
    exports com.ethanace.royalereport;
}
