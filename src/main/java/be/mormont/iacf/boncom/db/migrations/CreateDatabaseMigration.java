package be.mormont.iacf.boncom.db.migrations;

import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.db.EntityTable;
import be.mormont.iacf.boncom.db.OrderFormEntryTable;
import be.mormont.iacf.boncom.db.OrderFormTable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Date: 21-01-18
 * By  : Mormont Romain
 */
public class CreateDatabaseMigration implements Migration {
    @Override
    public void execute() throws IOException, SQLException {
        Connection connection = getConnection();
        try (Statement statement = connection.createStatement()) {

            // create entity table
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + EntityTable.NAME + "(" +
                            EntityTable.FIELD_ID + " INTEGER, " +
                            EntityTable.FIELD_ENTITY_NAME + " VARCHAR(255)," +
                            EntityTable.FIELD_PHONE_NUMBERS + " VARCHAR(255)," +
                            EntityTable.FIELD_STREET + " VARCHAR(511)," +
                            EntityTable.FIELD_HOUSE_NUMBER + " VARCHAR(255)," +
                            EntityTable.FIELD_BOX + " VARCHAR (255)," +
                            EntityTable.FIELD_POST_CODE + " VARCHAR(255)," +
                            EntityTable.FIELD_CITY + " VARCHAR(255)," +
                            EntityTable.FIELD_CUSTOMER_NB + " VARCHAR(255)," +
                            "PRIMARY KEY(" + EntityTable.FIELD_ID + "))"
            );

            // create order form table
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + OrderFormTable.NAME + "(" +
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
                    "CREATE TABLE IF NOT EXISTS " + OrderFormEntryTable.NAME + "(" +
                            OrderFormEntryTable.FIELD_ID + " INTEGER," +
                            OrderFormEntryTable.FIELD_ORDER_FORM + " INTEGER," +
                            OrderFormEntryTable.FIELD_REFERENCE + " VARCHAR(255)," +
                            OrderFormEntryTable.FIELD_DESIGNATION + " VARCHAR(255)," +
                            OrderFormEntryTable.FIELD_QUANTITY + " INTEGER UNSIGNED," +
                            OrderFormEntryTable.FIELD_UNIT_PRICE + " DECIMAL(20, 2)," +
                            "PRIMARY KEY (" + OrderFormEntryTable.FIELD_ID + ")," +
                            "FOREIGN KEY (" + OrderFormEntryTable.FIELD_ORDER_FORM + ") REFERENCES " + OrderFormTable.NAME + "(" + OrderFormTable.FIELD_ID + ") ON DELETE CASCADE )"
            );

            connection.commit();
        }
    }

    @Override
    public void revert() throws SQLException, IOException {
        Connection connection = getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + OrderFormEntryTable.NAME);
            statement.executeUpdate("DROP TABLE IF EXISTS " + OrderFormTable.NAME);
            statement.executeUpdate("DROP TABLE IF EXISTS " + EntityTable.NAME);
            connection.commit();
        }
    }

}
