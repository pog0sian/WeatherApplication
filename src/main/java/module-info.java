module org.example.weatherapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires static lombok;
    requires java.sql;
    requires java.net.http;
    requires org.slf4j;


    opens org.example.weatherapp to javafx.fxml;
    exports org.example.weatherapp;
    exports org.example.weatherapp.services;
    opens org.example.weatherapp.services to javafx.fxml;

}