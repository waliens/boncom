package be.mormont.iacf.boncom.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class Database {
    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "org.sqlite.JDBC";
    private static final String DB_URL = "jdbc:sqlite:database.db";
    private static final String DB_NAME = "iacf-orderform";

    private static final String DB_TABLE_PROVIDER = "provider";

    //  Database credentials
    private static final String USER = "access";
    private static final String PASS = "";

    public static void createDatabaseIfNotExist() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection c = DriverManager.getConnection(DB_URL); Statement statement = c.createStatement()) {
            c.setAutoCommit(false);

            statement.executeUpdate("CREATE DATABASE " + DB_NAME);

            // create provider table
            statement.executeUpdate(
                    "CREATE TABLE " + DB_TABLE_PROVIDER + " IF NOT EXISTS " +
                            "(id INTEGER PRIMARY KEY, " +
                            " name VARCHAR(255)," +
                            " phoneNumbers VARCHAR(255)," +
                            "street VARCHAR(511)," +
                            "house_number VARCHAR(255)," +
                            "box VARCHAR (255)," +
                            "post_code VARCHAR(255)," +
                            "city VARCHAR(255))");
        }


    }
}
