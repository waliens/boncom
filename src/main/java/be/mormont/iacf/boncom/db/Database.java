package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;

import java.sql.*;
import java.util.ArrayList;


/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class Database implements AutoCloseable {
    // JDBC driver name and database URL
    private static final String DB_URL = "jdbc:sqlite:database.db";
    private static final String DB_NAME = "iacf-orderform";

    //  Database credentials
    private static final String USER = "access";
    private static final String PASS = "";

    private static Database database = null;
    private Connection connection ;

    private Database() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
    }

    /**
     * Get singleton database object
     * @return database
     */
    public synchronized static Database getDatabase() {
        if (database == null) {
            try {
                database = new Database();
                database.connection.setAutoCommit(false);
            } catch (SQLException e) {
                // will return null
            }
        }
        return database;
    }

    /**
     * Get connection
     * @return connection
     */
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    public synchronized boolean ready() throws SQLException {
        return false;
    }

    public synchronized void createDatabaseIfNotExist() throws SQLException {
        if (!ready()) {
            createDatabase();
        }
    }

    /**
     * Create tables if they don't exist.
     * @throws SQLException
     */
    private void createDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + OrderFormEntryTable.NAME);
            statement.executeUpdate("DROP TABLE IF EXISTS " + OrderFormTable.NAME);
            statement.executeUpdate("DROP TABLE IF EXISTS " + EntityTable.NAME);

            // create entity table
            statement.executeUpdate(
            "CREATE TABLE " + EntityTable.NAME + "(" +
                    EntityTable.FIELD_ID + " INTEGER, " +
                    EntityTable.FIELD_ENTITY_NAME + " VARCHAR(255)," +
                    EntityTable.FIELD_PHONE_NUMBERS + " VARCHAR(255)," +
                    EntityTable.FIELD_STREET + " VARCHAR(511)," +
                    EntityTable.FIELD_HOUSE_NUMBER + " VARCHAR(255)," +
                    EntityTable.FIELD_BOX + " VARCHAR (255)," +
                    EntityTable.FIELD_POST_CODE + " VARCHAR(255)," +
                    EntityTable.FIELD_CITY + " VARCHAR(255)," +
                    "PRIMARY KEY(" + EntityTable.FIELD_ID + "))"
            );

            // create order form table
            statement.executeUpdate(
            "CREATE TABLE " + OrderFormTable.NAME + "(" +
                    OrderFormTable.FIELD_ID + " INTEGER, " +
                    OrderFormTable.FIELD_PROVIDER + " INTEGER, " +
                    OrderFormTable.FIELD_PURCHASER + " INTEGER, " +
                    OrderFormTable.FIELD_ISSUE_DATE + " DATE," +
                    OrderFormTable.FIELD_NUMBER + " INTEGER UNIQUE," +
                    "PRIMARY KEY (" + OrderFormTable.FIELD_ID + ")," +
                    "FOREIGN KEY (" + OrderFormTable.FIELD_PROVIDER + ") REFERENCES " + EntityTable.NAME + "(" + EntityTable.FIELD_ID + ")," +
                    "FOREIGN KEY (" + OrderFormTable.FIELD_PURCHASER + ") REFERENCES " + EntityTable.NAME + "(" + EntityTable.FIELD_ID + "))"
            );

            // create order form entry table
            statement.executeUpdate(
                "CREATE TABLE " + OrderFormEntryTable.NAME + "(" +
                    OrderFormEntryTable.FIELD_ID + " INTEGER," +
                    OrderFormEntryTable.FIELD_ORDER_FORM + " INTEGER," +
                    OrderFormEntryTable.FIELD_REFERENCE + " VARCHAR(255)," +
                    OrderFormEntryTable.FIELD_DESIGNATION + " VARCHAR(255)," +
                    OrderFormEntryTable.FIELD_QUANTITY + " INTEGER UNSIGNED," +
                    OrderFormEntryTable.FIELD_UNIT_PRICE + " INTEGER," +
                    "PRIMARY KEY (" + OrderFormEntryTable.FIELD_ID + ", " + OrderFormEntryTable.FIELD_ORDER_FORM + ")," +
                    "FOREIGN KEY (" + OrderFormEntryTable.FIELD_ID + ") REFERENCES " + OrderFormTable.NAME + "(" + OrderFormTable.FIELD_ID + "))"
            );

            // insert default data
            Address iacfAddress = new Address("Rue des Bruyères", "150", null, "4000", "Liège");
            ArrayList<String> iacfPhones = new ArrayList<>();
            iacfPhones.add("04/252.92.86");
            iacfPhones.add("04/254.23.67");
            Entity iacf = new Entity("IACF Cointe", iacfAddress, iacfPhones);

            try(PreparedStatement prepStatement = new EntityTable().insertStatement(connection, iacf)) {
                prepStatement.executeUpdate();
            }

            connection.commit();
        }
    }
}
