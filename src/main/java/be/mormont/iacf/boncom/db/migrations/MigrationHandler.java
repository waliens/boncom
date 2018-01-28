package be.mormont.iacf.boncom.db.migrations;


import be.mormont.iacf.boncom.db.BaseTable;
import be.mormont.iacf.boncom.db.Database;
import javafx.util.Pair;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static be.mormont.iacf.boncom.db.Database.*;

/**
 * Date: 21-01-18
 * By  : Mormont Romain
 */
public class MigrationHandler
{
    public static final String NAME = "migrations";
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_MIGRATION_DATETIME = "migration_datetime";

    private Connection connection;
    private ArrayList<Pair<String, Migration>> migrations;

    /**
     * Create the migration table object
     * @throws IOException
     * @throws SQLException
     */
    public MigrationHandler() throws IOException, SQLException {
        connection = getDatabase().getConnection();
        createTableIfNotExists();
        migrations = new ArrayList<>();
        addMigration("create_database", new CreateDatabaseMigration());
        addMigration("add_iacf", new InsertIacfProviderMigration());
        addMigration("add_delivery_date_col", new AddDeliveryDateFieldMigration());
    }

    /**
     * Add a migartion to the migration to thable
     * @param name Name of the migration
     * @param migration The migration
     */
    private void addMigration(String name, Migration migration) {
        migrations.add(new Pair<>(name, migration));
    }

    /**
     * Createt migration table it does not exist
     * @throws SQLException SQL exception
     */
    private void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS " + NAME + "(" +
                FIELD_ID + " INTEGER, " +
                FIELD_NAME + " VARCHAR(255)," +
                FIELD_MIGRATION_DATETIME + " DATETIME(255)," +
                "PRIMARY KEY(" + FIELD_ID + "))";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
            connection.commit();
        }
    }

    /**
     * Check which migrations were already added, and adds the new ones
     */
    public void checkAndUpdate() throws SQLException, IOException {
        HashSet<String> executedMigrations = getExecutedMigrations();
        // check if no migration was skipped
        String lastMigrationNotDone = null;
        for (Pair<String, Migration> migrationPair: migrations) {
            String currentName = migrationPair.getKey();
            boolean currentDone = executedMigrations.contains(currentName);
            if (currentDone && lastMigrationNotDone != null) {
                throw new RuntimeException(
                        "Migration system in invalid mode. " +
                        "Migration '" + currentName + "' " +
                        "seems to have been executed while the anterior " +
                        "migration '" + lastMigrationNotDone + "' has not been.");
            }
            if (!currentDone) {
                lastMigrationNotDone = currentName;
            }
        }

        // execute not previously executed migrations
        for (Pair<String, Migration> migrationPair: migrations) {
            String currentName = migrationPair.getKey();
            Migration migration = migrationPair.getValue();
            boolean currentDone = executedMigrations.contains(currentName);
            if (!currentDone) {
                executeMigration(currentName, migration);
            }
        }
    }

    /**
     * Get already executed migrations names
     * @return Set of added migrations
     * @throws SQLException
     */
    private HashSet<String> getExecutedMigrations() throws SQLException {
        String query = "SELECT * FROM " + NAME;
        HashSet<String> executedMigrations = new HashSet<>();
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
            ResultSet resultSet = statement.getResultSet();
            while(resultSet.next()) {
                executedMigrations.add(resultSet.getString(FIELD_NAME));
            }
            return executedMigrations;
        }
    }

    private void executeMigration(String name, Migration migration) throws IOException, SQLException {
        migration.execute();
        String query = "INSERT INTO " + NAME + "("+ FIELD_NAME + ", " + FIELD_MIGRATION_DATETIME + ") VALUES (?, datetime('now'))";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.executeUpdate();
            connection.commit();
        }
    }
}
