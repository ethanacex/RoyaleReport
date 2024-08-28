module com.ethanace.royalereport {
    requires transitive javafx.controls;
    requires javafx.fxml;

    opens com.ethanace.royalereport to javafx.fxml;
    exports com.ethanace.royalereport;
}
