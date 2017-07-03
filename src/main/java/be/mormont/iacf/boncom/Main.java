package be.mormont.iacf.boncom;

import be.mormont.iacf.boncom.db.Database;

import java.sql.SQLException;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class Main {
    public static void main(String[] args) {
        try (Database database = Database.getDatabase()) {
            database.createDatabaseIfNotExist();
        } catch (SQLException e) {
            System.err.println("Cannot connect to the database: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
