package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.Lg;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import static java.nio.file.StandardCopyOption.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

import static java.nio.file.Files.move;


/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class Database implements AutoCloseable {
    // JDBC driver name and database URL
    private static final String DB_URL = "jdbc:sqlite:";
    private static final String APP_FOLDER = ".boncom";
    private static final String DB_FILE = "database.db";

    private static final String CONFIG_FILE = "config.txt";

    private static Database database = null;
    private Connection connection;

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

    private static String getConfigPath() {
        Path appFolderPath = Paths.get(getAppDataFolder(), APP_FOLDER);
        // look for config file
        return Paths.get(appFolderPath.toString(), CONFIG_FILE).toString();
    }

    private static String readConfigFile() throws IOException {
        File config_file = new File(getConfigPath());
        FileReader reader = new FileReader(config_file);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static String getDatabasePath() {
        Path appFolderPath = Paths.get(getAppDataFolder(), APP_FOLDER);
        String path;
        // look for config file
        try {
            String configContent = readConfigFile();
            return Paths.get(configContent, DB_FILE).toString();
        } catch (IOException e) {
            // no config file so database must be in App Data
            File dir = new File(appFolderPath.toString());
            return Paths.get(dir.getAbsolutePath(), DB_FILE).toString();
        }
    }

    private Database() throws SQLException, IOException {
        connect();
    }

    public void connect() throws SQLException, IOException {
        if (this.isConnected()) {
            return;
        }
        String path = getDatabasePath();
        File databaseFile = new File(path);
        File parentOfDatabaseFile = new File(databaseFile.getParent());
        if (!databaseFile.exists() && !parentOfDatabaseFile.exists()){
            Lg.getLogger(Database.class).info("creating folder for database file in '" + parentOfDatabaseFile.toString() + "'");
            if(!parentOfDatabaseFile.mkdirs()) {
                throw new IOException("cannot create database folder '" + parentOfDatabaseFile.getAbsolutePath() + "'!");
            }
        }
        Lg.getLogger(Database.class).info("open database from '" + path + "'");
        String url = DB_URL + path;
        this.connection = DriverManager.getConnection(url);
        this.connection.setAutoCommit(false);
    }

    public boolean isConnected() {
        try {
            return connection != null && connection.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Get singleton database object
     * @return database
     */
    public synchronized static Database getDatabase() throws SQLException, IOException {
        if (database == null) {
            database = new Database();
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
        connection = null;
    }

    public void disconnect() throws Exception {
        close();
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

    static void writeDatabasePathConfig(String newPath) throws IOException {
        // Create a new File object for the file to replace
        File file = new File(getConfigPath());
        // Check if the file exists
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IOException("cannot create config folder in '" + file.getParentFile().getAbsolutePath() + "'");
        }
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("cannot create configuration file in '" + file.getAbsolutePath() + "'");
        }
        // Open the file for writing
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath())));
        // Write the new content to the file
        pw.println(newPath);
        // Close the file
        pw.close();
    }

    public void moveDatabase(File selectedDirectory) throws Exception, IOException {
        if (selectedDirectory.exists() && !selectedDirectory.isDirectory()) {
            throw new IOException("selected item is not a directory");
        }
        File targetFile = new File(Paths.get(selectedDirectory.toString(), DB_FILE).toString());
        if (targetFile.exists()) {
            throw new FileAlreadyExistsException("database file already exists in '" + targetFile.getAbsolutePath() + "'");
        }
        try {
            this.disconnect();
        } catch (Exception e) {
            Lg.getLogger(Database.class).warning("cannot move database because failed to disconnect: " + e.getMessage());
            throw e;
        }

        String sourceFile = getDatabasePath();
        Path sourcePath = Paths.get(sourceFile),
            targetPath = Paths.get(targetFile.getAbsolutePath());

        try {
            Files.move(sourcePath, targetPath);
        } catch (Exception e) {
            this.connect();
            Lg.getLogger(Database.class).warning("cannot move database: " + e.getMessage());
            throw e;
        }

        try {
            writeDatabasePathConfig(targetFile.getParentFile().getAbsolutePath());
        } catch (Exception e) {
            Files.move(targetPath, sourcePath);
            this.connect();
            Lg.getLogger(Database.class).warning("cannot move database: " + e.getMessage());
            throw e;
        }

        this.connect();
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
