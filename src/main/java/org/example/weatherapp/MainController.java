package org.example.weatherapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.weatherapp.services.DatabaseService;
import org.example.weatherapp.services.WeatherForecastService;
import org.example.weatherapp.services.WeatherService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static java.lang.Math.round;

public class MainController {

    @Setter
    private Stage stage;

    @FXML
    private Button cityNameButton;

    @FXML
    private Label temperatureLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label minTempLabel;

    @FXML
    private Label windSpeedLabel;

    @FXML
    private Label windDirectionLabel;

    @FXML
    private Label humidityLabel;

    @FXML
    private Label visibilityLabel;

    @FXML
    private Label pressureLabel;

    @FXML
    private Label feelsLikeLabel;

    @FXML
    private Label dateLabel1;

    @FXML
    private Label dateLabel2;

    @FXML
    private Label dateLabel3;

    @FXML
    private Label dateLabel4;

    @FXML
    private Label forecastTemperatureLabel1;

    @FXML
    private Label forecastTemperatureLabel2;

    @FXML
    private Label forecastTemperatureLabel3;

    @FXML
    private Label forecastTemperatureLabel4;

    @FXML
    private ImageView weatherIcon;

    @FXML
    private ImageView weatherIcon1;

    @FXML
    private ImageView weatherIcon2;

    @FXML
    private ImageView weatherIcon3;

    @FXML
    private ImageView weatherIcon4;

    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM");

    private final WeatherService weatherService = new WeatherService();
    private final DatabaseService databaseService = new DatabaseService();
    private final WeatherForecastService weatherForecastService = new WeatherForecastService();

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    public void initialize() {
        logger.info("Initializing MainController");
        fetchWeather();
    }

    private void fetchWeather() {
        new Thread(() -> {
            String cityName = databaseService.getCityName().trim();
            logger.info("Fetching weather data for city: {}", cityName);

            JSONObject weatherData = weatherService.getWeatherData(cityName);
            JSONArray forecastData = weatherForecastService.getForecastData(cityName);

            if (weatherData != null && forecastData != null) {
                Platform.runLater(() -> {
                    logger.info("Successfully fetched weather and forecast data for city: {}", cityName);
                    updateUI(weatherData);
                    updateForecastUI(forecastData);
                });
            } else {
                Platform.runLater(() -> {
                    logger.error("Failed to fetch weather data for city: {}", cityName);
                    cityNameButton.setText("Город не найден!");
                });
            }
        }).start();
    }

    private void updateUI(JSONObject weatherData) {
        try {
            String cityName = weatherData.getString("name");
            int temperature = (int) weatherData.getJSONObject("main").getDouble("temp");
            int minTemp = (int) weatherData.getJSONObject("main").getDouble("temp_min");
            String description = weatherData.getJSONArray("weather").getJSONObject(0).getString("description");
            int windSpeed = (int) weatherData.getJSONObject("wind").getDouble("speed");
            String windDirection = getWindDirection(weatherData.getJSONObject("wind").getInt("deg"));
            int humidity = (int) weatherData.getJSONObject("main").getDouble("humidity");
            int visibility = (int) weatherData.getDouble("visibility") / 1000;
            double pressure = round(weatherData.getJSONObject("main").getDouble("pressure") * 0.7500638);
            int feelsLike = (int) weatherData.getJSONObject("main").getDouble("feels_like");

            cityNameButton.setText(cityName);
            minTempLabel.setText(minTemp+ " °C");
            temperatureLabel.setText(temperature + " °C");
            descriptionLabel.setText(description);
            windSpeedLabel.setText(windSpeed + " м/с");
            windDirectionLabel.setText(windDirection);
            humidityLabel.setText(humidity + "%");
            visibilityLabel.setText(visibility + " км");
            pressureLabel.setText(pressure + " мм рт. ст.");
            feelsLikeLabel.setText(feelsLike + " °C");
            weatherIcon.setImage(getWeatherIcon(description));

        } catch (Exception e) {
            logger.error("Error occurred while updating UI with weather data", e);
        }
    }

    private void updateForecastUI(JSONArray forecastData) {
        StringBuilder forecastText = new StringBuilder();

        Label[] dateLabels = {dateLabel1, dateLabel2, dateLabel3, dateLabel4};
        Label[] tempLabels = {forecastTemperatureLabel1, forecastTemperatureLabel2, forecastTemperatureLabel3, forecastTemperatureLabel4};
        ImageView[] iconLabels = {weatherIcon1, weatherIcon2, weatherIcon3, weatherIcon4};

        try {
            for (int i = 1; i < 5; i++) {
                JSONObject dailyForecast = forecastData.getJSONObject(i * 8);
                LocalDate date = LocalDate.parse(dailyForecast.getString("dt_txt").split(" ")[0]);
                int temp = (int) dailyForecast.getJSONObject("main").getDouble("temp");
                String description = dailyForecast.getJSONArray("weather").getJSONObject(0).getString("description");

                forecastText.append(String.format("%s: %d°C, %s%n", date, temp, description));

                dateLabels[i-1].setText(date.format(outputFormatter));
                iconLabels[i-1].setImage(getWeatherIcon(description));
                tempLabels[i-1].setText(String.valueOf(temp));
            }
        } catch (Exception e) {
            logger.error("Error occurred while updating forecast UI", e);
        }
    }

    @FXML
    private void changeCity() throws IOException {
        logger.info("Changing city...");
        databaseService.deleteAllRows();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("CitySearchScene.fxml")));
        stage = (Stage) cityNameButton.getScene().getWindow();
        stage.setScene(new Scene(root, 800, 400));
    }

    private String getWindDirection(int degree) {
        if (degree >= 337.5 || degree < 22.5) {
            return "Север";
        } else if (degree >= 22.5 && degree < 67.5) {
            return "Северо-Восток";
        } else if (degree >= 67.5 && degree < 112.5) {
            return "Восток";
        } else if (degree >= 112.5 && degree < 157.5) {
            return "Юго-Восток";
        } else if (degree >= 157.5 && degree < 202.5) {
            return "Юг";
        } else if (degree >= 202.5 && degree < 247.5) {
            return "Юго-Запад";
        } else if (degree >= 247.5 && degree < 292.5) {
            return "Запад";
        } else {
            return "Северо-Запад";
        }
    }

    private Image getWeatherIcon(String description) {
        return switch (description.toLowerCase()) {
            case "ясно" -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/sunny.png")));
            case "пасмурно" -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/cloudy.png")));
            case "переменная облачность" -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/cloudy_at_sometimes.png")));
            case "облачно с прояснениями" -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/partly_cloudy.png")));
            case "небольшой дождь" -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/light_rainy.png")));
            case "дождь" -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/rainy.png")));
            case "небольшой снег" -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/snowy.png")));
            case "туман" -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/fog.png")));
            default -> new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/default.jpg")));
        };
    }
}