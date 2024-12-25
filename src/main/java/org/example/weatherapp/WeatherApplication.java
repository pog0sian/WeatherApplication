package org.example.weatherapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.weatherapp.services.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WeatherApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    DatabaseService databaseService = new DatabaseService();
    String fxmlFile;

    @Override
    public void start(Stage stage) throws IOException {


        Font customFont = Font.loadFont(getClass().getResourceAsStream("fonts/Lato-Light.ttf"), 18);

        fxmlFile = databaseService.hasCityRecord() ? "MainScene.fxml" : "CitySearchScene.fxml";
        logger.info("Loading FXML file: {}", fxmlFile);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
        Scene scene = new Scene(fxmlLoader.load(), 800, 400);

        stage.setTitle("Погода");
        stage.setScene(scene);
        stage.show();
        logger.info("Weather application started.");
    }

    public static void main(String[] args) {
        launch();
    }
}