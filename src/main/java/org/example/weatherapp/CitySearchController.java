package org.example.weatherapp;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.weatherapp.services.DatabaseService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CitySearchController {

    @Setter
    private Stage stage;

    private final DatabaseService databaseService = new DatabaseService();

    private final Alert alert = new Alert(Alert.AlertType.NONE);

    private boolean selectionMade = false;

    @FXML
    private ComboBox<String> cityComboBox;

    private static final String API_URL = "https://api.openweathermap.org/geo/1.0/direct";
    private static final String API_KEY = "026645cb62724c6ded894022fa28e190";

    private static final Logger logger = LoggerFactory.getLogger(CitySearchController.class);

    @FXML
    public void initialize() {
        cityComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 2) {
                logger.info("User entered search query: {}", newValue);
                searchCity(newValue);
            }
        });

        cityComboBox.setOnAction(event -> {
            if (cityComboBox.getSelectionModel().getSelectedItem() != null) {
                selectionMade = true;
                cityComboBox.hide();
                logger.info("User selected city: {}", cityComboBox.getSelectionModel().getSelectedItem());
            }
        });

        cityComboBox.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (selectionMade) {
                cityComboBox.hide();
                selectionMade = false;
                logger.debug("City selection was canceled");
            }
        });
    }

    public void searchCity(String query) {
        new Thread(() -> {
            try {
                String urlString = String.format("%s?q=%s&limit=5&appid=%s", API_URL, query.replace(" ", "%20"), API_KEY);

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Safari/537.36");

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    List<String> cityList = parseCities(response.toString());

                    Platform.runLater(() -> {
                        ObservableList<String> cityObservableList = FXCollections.observableArrayList(cityList);
                        cityComboBox.setItems(cityObservableList);
                        if (!cityObservableList.isEmpty() && !selectionMade) {
                            cityComboBox.show();
                        }
                    });

                } else {
                    logger.error("Error fetching city data, Response Code: {}", responseCode);
                }

            } catch (Exception e) {
                logger.error("Error occurred while searching for cities with query: {}", query, e);
            }
        }).start();
    }

    private List<String> parseCities(String jsonResponse) {
        List<String> cityList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject cityObject = jsonArray.getJSONObject(i);
                String cityName = cityObject.getString("name");
                String country = cityObject.getString("country");
                String displayName = cityName + ", " + country;
                cityList.add(displayName);
            }
        } catch (Exception e) {
            logger.error("Error occurred while parsing city data", e);
        }
        return cityList;
    }

    @FXML
    private void switchToMainScene () throws IOException {
        String cityName = cityComboBox.getEditor().getText().trim();
        logger.info("Attempting to switch to main scene with city: {}", cityName);

        if (cityName.isEmpty()) {
            logger.warn("City name is empty");
            showError("Ошибка", "Введите корректное название города.");
        } else {
            String[] parts = cityName.split(", ");
            if (parts.length == 2) {
                String city = parts[0];
                String country = parts[1];

                logger.info("Inserting city: {} into database", city);
                databaseService.insertCity(city, country);
            }

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("MainScene.fxml")));
            stage = (Stage) cityComboBox.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 400));
            logger.info("Successfully switched to main scene");
        }
    }

    private void showError(String title, String message) {
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}