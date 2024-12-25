package org.example.weatherapp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseService {

    private static final String URL = "jdbc:sqlite:my.db";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    public DatabaseService() {
        createTable();
    }

    private Connection getConnection() throws SQLException {
        logger.debug("Attempting to connect to database: {}", URL);
        return DriverManager.getConnection(URL);
    }

    public void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS CITIES ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "country TEXT NOT NULL "
                + ");";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL)) {
            preparedStatement.execute();
            logger.info("Table 'CITIES' created or already exists.");
        } catch (SQLException e) {
            logger.error("Error creating table 'CITIES': {}", e.getMessage(), e);
        }
    }

    public void insertCity(String city, String country) {
        String insertCitySQL = "INSERT INTO CITIES (name, country) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertCitySQL)) {
            preparedStatement.setString(1, city);
            preparedStatement.setString(2, country);
            preparedStatement.executeUpdate();
            logger.info("Inserted city: {}, country: {}", city, country);
        } catch (SQLException e) {
            logger.error("Error inserting city: {}, country: {}: {}", city, country, e.getMessage(), e);
        }
    }

    public void deleteAllRows() {
        String deleteAllRowsSQL = "DELETE FROM CITIES;";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteAllRowsSQL)) {
            preparedStatement.executeUpdate();
            logger.info("All rows deleted from 'CITIES' table.");
        } catch (SQLException e) {
            logger.error("Error deleting rows from 'CITIES' table: {}", e.getMessage(), e);
        }
    }

    public String getCityName() {
        String query = "SELECT name FROM CITIES LIMIT 1";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                String cityName = resultSet.getString("name");
                logger.info("Fetched city name: {}", cityName);
                return cityName;
            }
        } catch (Exception e) {
            logger.error("Error fetching city name: {}", e.getMessage(), e);
        }

        logger.warn("No city name found in 'CITIES' table.");
        return null;
    }

    public boolean hasCityRecord() {
        String query = "SELECT COUNT(*) FROM CITIES";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                boolean hasRecords = resultSet.getInt(1) > 0;
                logger.info("City records exist: {}", hasRecords);
                return hasRecords;
            }
        } catch (Exception e) {
            logger.error("Error checking city records: {}", e.getMessage(), e);
        }

        logger.warn("No records found in 'CITIES' table.");
        return false;
    }
}