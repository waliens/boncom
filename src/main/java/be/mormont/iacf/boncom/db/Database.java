package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.Lg;
import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class Database implements AutoCloseable {
    // JDBC driver name and database URL
    private static final String DB_URL = "jdbc:sqlite:database.db";

    private static Database database = null;
    private Connection connection ;

    private Database() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
    }

    /**
     * Get singleton database object
     * @return database
     */
    public synchronized static Database getDatabase() throws SQLException {
        if (database == null) {
            database = new Database();
            database.connection.setAutoCommit(false);
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

    /**
     * Check whether the database contains valid tables for the application to run.
     * @return True if the database is ready, false otherwise
     * @throws SQLException If connection fails
     */
    private synchronized boolean ready() throws SQLException {
        String[] expectedNames = new String[] { EntityTable.NAME, OrderFormTable.NAME, OrderFormEntryTable.NAME };
        String[] actualNames = getTableNames();

        if (expectedNames.length != actualNames.length) {
            return false;
        }

        Arrays.sort(expectedNames);
        Arrays.sort(actualNames);

        for (int i = 0; i < expectedNames.length; ++i) {
            if (!actualNames[i].equals(expectedNames[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return names of all tables currently in the database
     * @return Array of table names
     * @throws SQLException If connection fails
     */
    private String[] getTableNames() throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try(ResultSet tables = metadata.getTables(null, null, "", new String[]{"table"})) {
            ArrayList<String> names = new ArrayList<>();
            while (tables.next()) {
                final int TABLE_NAME = 3; // table name idx in result set
                names.add(tables.getString(TABLE_NAME));
            }
            return names.toArray(new String[names.size()]);
        }
    }

    public synchronized void createDatabaseIfNotExist() throws SQLException {
        if (!ready()) {
            Lg.getLogger(Database.class).info("Database is not ready... start creation!");
            createDatabase();
            Lg.getLogger(Database.class).info("Database created...");
        }
    }

    /**
     * Create tables if they don't exist.
     * @throws SQLException If database connection fails
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
                    OrderFormTable.FIELD_NUMBER + " INTEGER," +
                    "PRIMARY KEY (" + OrderFormTable.FIELD_ID + ")," +
                    "FOREIGN KEY (" + OrderFormTable.FIELD_PROVIDER + ") REFERENCES " + EntityTable.NAME + "(" + EntityTable.FIELD_ID + ") ON DELETE RESTRICT," +
                    "FOREIGN KEY (" + OrderFormTable.FIELD_PURCHASER + ") REFERENCES " + EntityTable.NAME + "(" + EntityTable.FIELD_ID + ") ON DELETE RESTRICT )"
            );

            // create order form entry table
            statement.executeUpdate(
                "CREATE TABLE " + OrderFormEntryTable.NAME + "(" +
                    OrderFormEntryTable.FIELD_ID + " INTEGER," +
                    OrderFormEntryTable.FIELD_ORDER_FORM + " INTEGER," +
                    OrderFormEntryTable.FIELD_REFERENCE + " VARCHAR(255)," +
                    OrderFormEntryTable.FIELD_DESIGNATION + " VARCHAR(255)," +
                    OrderFormEntryTable.FIELD_QUANTITY + " INTEGER UNSIGNED," +
                    OrderFormEntryTable.FIELD_UNIT_PRICE + " DECIMAL(20, 2)," +
                    "PRIMARY KEY (" + OrderFormEntryTable.FIELD_ID + ")," +
                    "FOREIGN KEY (" + OrderFormEntryTable.FIELD_ORDER_FORM + ") REFERENCES " + OrderFormTable.NAME + "(" + OrderFormTable.FIELD_ID + ") ON DELETE CASCADE )"
            );

            // insert default data
            Address iacfAddress = new Address("Rue des Bruyères", "150", null, "4000", "Liège");
            String[] iacfPhones = new String[]{"04/252.92.86", "04/254.23.67"};
            Entity iacf = new Entity("IACF Cointe", iacfAddress, iacfPhones);

            try(PreparedStatement prepStatement = new EntityTable().insertStatement(connection, iacf)) {
                prepStatement.executeUpdate();
            }

            connection.commit();
        }
    }

    public <T> T executeUpdatePreparedStatement(PreparedStatementBuilder<T> builder, boolean commit) throws SQLException {
        try (PreparedStatement statement = builder.getStatement(connection)) {
            try {
                statement.executeUpdate();
                if (commit) {
                    connection.commit();
                }
                builder.success(null, statement);
            } catch (Exception e) {
                connection.rollback();
                builder.failure(e);
            }
        } catch (Exception e) {
            connection.rollback();
            builder.failure(e);
        }
        return null;
    }

    public <T> T executePreparedStatement(PreparedStatementBuilder<T> builder) throws SQLException {
        try (PreparedStatement statement = builder.getStatement(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                return builder.success(resultSet, statement);
            } catch (Exception e) {
                builder.failure(e);
            }
        } catch (Exception e) {
            builder.failure(e);
        }
        return null;
    }

    /**
     * Return the id of the element last inserted in the database
     * @param statement The statement which has yielded the key
     * @return The key, -1 if there no key was created
     * @throws SQLException exception occurs
     */
    static long getLastId(PreparedStatement statement) throws SQLException {
        ResultSet keys = statement.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        return -1;
    }

    /**
     * Callback for building prepared statement
     */
    public interface PreparedStatementBuilder<T> {
        PreparedStatement getStatement(Connection conn) throws SQLException;
        T success(ResultSet resultSet, PreparedStatement statement);
        default void failure(Exception e) {}
    }
}
