package org.example.weatherapp.services;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {

    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather";
    private static final String API_KEY = "026645cb62724c6ded894022fa28e190";

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    public JSONObject getWeatherData(String cityName) {
        logger.info("Fetching weather data for city: {}", cityName);

        try {
            String urlString = String.format("%s?q=%s&appid=%s&units=metric&lang=ru", API_URL, cityName.replace(" ", "%20"), API_KEY);
            logger.debug("Request URL: {}", urlString);

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Safari/537.36");

            int responseCode = connection.getResponseCode();
            logger.debug("Response Code: {}", responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                logger.info("Successfully fetched weather data for city: {}", cityName);
                return new JSONObject(response.toString());
            } else {
                logger.error("Error fetching weather data. Response code: {}", responseCode);
            }
        } catch (Exception e) {
            logger.error("Error occurred while fetching weather data for city: {}", cityName, e);
        }

        return null;
    }
}