package be.mormont.iacf.boncom.db.migrations;

import be.mormont.iacf.boncom.db.EntityTable;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Date: 21-01-18
 * By  : Mormont Romain
 */
public class InsertIacfProviderMigration implements Migration {
    @Override
    public void execute() throws SQLException, IOException {
        String query =
                "INSERT OR REPLACE INTO " + EntityTable.NAME + " (" +
                    EntityTable.FIELD_ID + ", " +
                    EntityTable.FIELD_ENTITY_NAME + ", " +
                    EntityTable.FIELD_PHONE_NUMBERS + ", " +
                    EntityTable.FIELD_STREET + ", " +
                    EntityTable.FIELD_HOUSE_NUMBER + ", " +
                    EntityTable.FIELD_BOX + ", " +
                    EntityTable.FIELD_POST_CODE + ", " +
                    EntityTable.FIELD_CITY + ", " +
                    EntityTable.FIELD_CUSTOMER_NB + ") VALUES " +
                "(1, 'IACF Cointe', '04/252.92.86,04/254.23.67', 'Rue des Bruyères', " +
                  "'150', NULL, '4000', 'Liège', '')";

        try(PreparedStatement prepStatement = getConnection().prepareStatement(query)) {
            prepStatement.executeUpdate();
            getConnection().commit();
        }
    }

    @Override
    public void revert() throws SQLException, IOException {
        String query = "DELETE FROM " + EntityTable.NAME + " WHERE " + EntityTable.FIELD_ID + "=1";
        try(PreparedStatement preparedStatement = getConnection().prepareStatement(query)) {
            preparedStatement.executeUpdate();
            getConnection().commit();
        }
    }
}
