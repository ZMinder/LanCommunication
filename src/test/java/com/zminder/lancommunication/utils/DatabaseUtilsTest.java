package com.zminder.lancommunication.utils;

import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtilsTest {
    @Test
    public void testGetDataSource() {
        DataSource dataSource = DatabaseUtils.getDataSource();
        Assert.assertNotNull("DataSource should not be null", dataSource);
    }

    @Test
    public void testGetConnection() {
        DataSource dataSource = DatabaseUtils.getDataSource();
        try (Connection connection = dataSource.getConnection()) {
            Assert.assertNotNull("Connection should not be null", connection);
            Assert.assertFalse("Connection should not be closed", connection.isClosed());
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail("Failed to get connection: " + e.getMessage());
        }
    }

    @Test
    public void testConnection() {
        String url = "jdbc:mysql://localhost:3306/lan_communicate?serverTimezone=UTC";
        String username = "root";
        String password = "040712";

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected!");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the database.");
        }
    }
}
