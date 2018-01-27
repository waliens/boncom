package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.Lg;
import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class Database implements AutoCloseable {
    // JDBC driver name and database URL
    private static final String DB_URL = "jdbc:sqlite:";
    private static final String APP_FOLDER = ".boncom";
    private static final String DB_FILE = "database.db";

    private static Database database = null;
    private Connection connection ;

    /**
     * from https://stackoverflow.com/questions/11113974/what-is-the-cross-platform-way-of-obtaining-the-path-to-the-local-application-da
     * @return the AppData folder path
     */
    private static String getAppDataFolder() {
        String workingDirectory;
        String OS = (System.getProperty("os.name")).toUpperCase();
        if (OS.contains("WIN")) {
            workingDirectory = System.getenv("AppData");
        } else {
            workingDirectory = System.getProperty("user.home");
        }
        return workingDirectory;
    }

    private Database() throws SQLException, IOException {
        File dir = new File(Paths.get(getAppDataFolder(), APP_FOLDER).toString());
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create database folder '" + dir.getAbsoluteFile() + "'!");
        }
        String url = DB_URL + Paths.get(dir.getAbsolutePath(), DB_FILE);
        connection = DriverManager.getConnection(url);
    }

    /**
     * Get singleton database object
     * @return database
     */
    public synchronized static Database getDatabase() throws SQLException, IOException {
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
        String[] actualNames = getTableNames(connection);

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
     * @param connection The SQL connection
     * @return Array of table names
     * @throws SQLException If connection fails
     */
    public static String[] getTableNames(Connection connection) throws SQLException {
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


    public <T> T executePreparedStatement(PreparedStatementWithReturnBuilder<T> builder) throws SQLException {
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

    public <T> T executePreparedStatement(PreparedStatementNoReturnBuilder<T> builder, boolean commit) throws SQLException {
        try (PreparedStatement statement = builder.getStatement(connection)) {
            try {
                statement.executeUpdate();
                if (commit) {
                    connection.commit();
                }
                builder.success(statement);
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
     * Callbacks for building prepared statement
     */
    public interface PreparedStatementBuilder<T> {
        PreparedStatement getStatement(Connection conn) throws SQLException;
        default void failure(Exception e) {}
    }

    public interface PreparedStatementWithReturnBuilder<T> extends PreparedStatementBuilder<T> {
        T success(ResultSet resultSet, PreparedStatement statement);
    }

    public interface PreparedStatementNoReturnBuilder<T> extends PreparedStatementBuilder<T> {
        void success(PreparedStatement statement);
    }
}
