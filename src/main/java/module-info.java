module com.github.adrienave.booktherook {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires org.apache.commons.io;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires static chesslib;
    requires org.fxmisc.richtext;


    opens com.github.adrienave.booktherook to javafx.fxml;
    exports com.github.adrienave.booktherook;
    exports com.github.adrienave.booktherook.model;
    opens com.github.adrienave.booktherook.model to javafx.fxml;
    exports com.github.adrienave.booktherook.controller;
    opens com.github.adrienave.booktherook.controller to javafx.fxml;
    exports com.github.adrienave.booktherook.javafx;
    opens com.github.adrienave.booktherook.javafx to javafx.fxml;
}