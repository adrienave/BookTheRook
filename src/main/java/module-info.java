module com.github.adrienave.booktherook {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.github.adrienave.booktherook to javafx.fxml;
    exports com.github.adrienave.booktherook;
}