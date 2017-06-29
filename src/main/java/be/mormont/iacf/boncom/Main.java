package be.mormont.iacf.boncom;

import be.mormont.iacf.boncom.db.Database;

import java.sql.SQLException;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class Main {
    public static void main(String[] args) {
        try {
            Database.createDatabaseIfNotExist();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
