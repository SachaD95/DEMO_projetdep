module com.example.demo_lignedroite {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.demo_lignedroite to javafx.fxml;
    exports com.example.demo_lignedroite;
}