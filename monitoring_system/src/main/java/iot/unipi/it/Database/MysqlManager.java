package iot.unipi.it.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MysqlManager {
    private final static String databaseIP = "localhost";
    private final static String databasePort = "3306";
    private final static String databaseUsername = "root";
    private final static String databasePassword = "PASSWORD";
    private final static String databaseName = "smart_agricolture";

    private static Connection makeConnection() {
        Connection databaseConnection = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");// checks if the Driver class exists (correctly available)
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            // DriverManager: The basic service for managing a set of JDBC drivers.
            databaseConnection = DriverManager.getConnection(
                    "jdbc:mysql://" + databaseIP + ":" + databasePort +
                            "/" + databaseName + "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=CET",
                    databaseUsername,
                    databasePassword);
            // The Driver Manager provides the connection specified in the parameter string
            if (databaseConnection == null) {
                System.err.println("Connection to Db failed");
            }
        } catch (SQLException e) {
            System.err.println("MySQL Connection Failed!\n");
            e.printStackTrace();
        }
        return databaseConnection;
    }

    public static void insertTemperatureAndUmidity(String nodeId, int temperature, int umidity) {
        String insertQueryStatementTemperature = "INSERT INTO temperature (nodeId, value) VALUES (?, ?)";
        String insertQueryStatementUmidity = "INSERT INTO humidity (nodeId,value) VALUES (?, ?)";

        try (Connection AgricoltureConnection = makeConnection();
                PreparedStatement preparedStatementTemp = AgricoltureConnection
                        .prepareStatement(insertQueryStatementTemperature);
                PreparedStatement preparedStatementUmid = AgricoltureConnection
                        .prepareStatement(insertQueryStatementUmidity);) {
            preparedStatementTemp.setString(1, nodeId);
            preparedStatementTemp.setInt(2, temperature);
            preparedStatementTemp.executeUpdate();
            preparedStatementUmid.setString(1, nodeId);
            preparedStatementUmid.setInt(2, umidity);
            preparedStatementUmid.executeUpdate();
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }

    public static void insertSoilMoistureValue(String nodeId, int soilValue) {
        String insertQueryStatement = "INSERT INTO soilHumidity (nodeId, value) VALUES (?, ?);";

        try (Connection AgricoltureConnection = makeConnection();
                PreparedStatement preparedStatement = AgricoltureConnection.prepareStatement(insertQueryStatement);) {
            preparedStatement.setString(1, nodeId);
            preparedStatement.setInt(2, soilValue);
            preparedStatement.executeUpdate();

        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }

    public static void selectSoilHumidity(int seconds, ArrayList<Integer> nodeId, ArrayList<Double> averageHumidity) {
        String selectQueryStatement = "SELECT nodeId, AVG(value) AS average_humidity FROM soilHumidity WHERE timestamp >= NOW() - INTERVAL ? SECOND GROUP BY nodeId";
        nodeId.clear();
        averageHumidity.clear();
        try (Connection AgricoltureConnection = makeConnection();
                PreparedStatement preparedStatement = AgricoltureConnection.prepareStatement(selectQueryStatement);) {
            preparedStatement.setInt(1, seconds);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    nodeId.add(resultSet.getInt("nodeId"));
                    averageHumidity.add(resultSet.getDouble("average_humidity"));
                    System.out.println("Node ID: " + resultSet.getInt("nodeId") + ", Average Humidity: "
                            + resultSet.getDouble("average_humidity"));
                }
            }
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
        return;
    }

    public static double selectTemperature(int seconds) {
        String selectQueryStatement = "SELECT AVG(value) AS average_temperature FROM temperature WHERE timestamp >= NOW() - INTERVAL ? SECOND;";
        double temperature = 0;

        try (Connection agricultureConnection = makeConnection();
                PreparedStatement preparedStatement = agricultureConnection.prepareStatement(selectQueryStatement)) {
            preparedStatement.setInt(1, seconds);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    temperature = resultSet.getDouble("average_temperature");
                } else {
                    // Il ResultSet è vuoto, gestisci la situazione di conseguenza
                    System.out.println("Nessun risultato trovato per la query.");
                }
            }
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
        return temperature;
    }

    public static double selectHumidity(int seconds) {
        String selectQueryStatement = "SELECT AVG(value) AS average_humidity FROM humidity WHERE timestamp >= NOW() - INTERVAL ? SECOND;";
        double humidity = 0;

        try (Connection agricultureConnection = makeConnection();
                PreparedStatement preparedStatement = agricultureConnection.prepareStatement(selectQueryStatement)) {
            preparedStatement.setInt(1, seconds);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    humidity = resultSet.getDouble("average_humidity");
                } else {
                    // Il ResultSet è vuoto, gestisci la situazione di conseguenza
                    System.out.println("Nessun risultato trovato per la query.");
                }
            }
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
        return humidity;
    }

    public static void deleteAllRecords(String tableName) {
        String deleteQueryStatement = "DELETE FROM " + tableName + ";";
        try (Connection agricultureConnection = makeConnection();
                PreparedStatement preparedStatement = agricultureConnection.prepareStatement(deleteQueryStatement)) {
            preparedStatement.executeUpdate();
            System.out.println("Tutti i record della tabella " + tableName + " sono stati eliminati con successo.");
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }

    }

    public static int countRecords(String tableName) {
        String countQueryStatement = "";
        countQueryStatement = "SELECT COUNT(*) AS count FROM " + tableName + ";";

        int count = 0;

        try (Connection agricultureConnection = makeConnection();
                PreparedStatement preparedStatement = agricultureConnection.prepareStatement(countQueryStatement)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt("count");
                }
            }
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
        return count;
    }

}