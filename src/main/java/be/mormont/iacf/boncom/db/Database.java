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
    static final String JDBC_DRIVER = "org.sqlite.JDBC";
    static final String DB_URL = "jdbc:sqlite:database.db";
    static final String DB_NAME

    //  Database credentials
    static final String USER = "access";
    static final String PASS = "";

    public static void createDatabaseIfNotExist() throws SQLException {
        Connection c = DriverManager.getConnection(DB_URL);
        c.setAutoCommit(false);

        Statement statement = c.createStatement();
        statement.executeUpdate("CREATE DATABASE " + DB_NAME);

    }
}
