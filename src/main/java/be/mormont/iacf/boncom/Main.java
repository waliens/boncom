package be.mormont.iacf.boncom;

import be.mormont.iacf.boncom.db.Database;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Created by Romain on 28-06-17.
 * This is THE (main) class.
 */
public class Main {
    public static void main(String[] args) {
        try (Database database = Database.getDatabase()) {
            Lg.getLogger(Main.class).log(Level.INFO, "Create database (if it does not exist)...");
            database.createDatabaseIfNotExist();
        } catch (SQLException e) {
            Lg.getLogger(Main.class).log(Level.SEVERE, "Issue with database", e);
        } catch (Exception e) {
            Lg.getLogger(Main.class).log(Level.SEVERE, "Unhandled exception", e);
        }
    }
}
