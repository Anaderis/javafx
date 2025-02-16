module org.annuaire.annuairejavafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;


    opens main to javafx.fxml, com.fasterxml.jackson.databind;
    opens model to com.fasterxml.jackson.databind; // 🔹 Ouvre le package "model" pour Jackson

    exports main;
    exports model;
    exports main.CRUD;
    opens main.CRUD to com.fasterxml.jackson.databind, javafx.fxml;
}