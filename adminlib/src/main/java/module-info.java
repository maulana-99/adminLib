module lenderjava {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;
    requires javafx.base;
    requires javafx.graphics;
    requires org.apache.fontbox;
    requires org.apache.pdfbox;
    requires transitive java.sql;

    opens lenderjava to javafx.fxml;
    exports lenderjava;
}
