package be.mormont.iacf.boncom.db.migrations;

import be.mormont.iacf.boncom.db.OrderFormTable;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Date: 21-01-18
 * By  : Mormont Romain
 */
public class AddDeliveryDateFieldMigration implements Migration {
    @Override
    public void execute() throws IOException, SQLException {
        String query = "ALTER TABLE " + OrderFormTable.NAME +
                        " ADD " + OrderFormTable.FIELD_DELIVERY_DATE +
                        " DATE NULL " +
                        " CONSTRAINT " + OrderFormTable.FIELD_DELIVERY_DATE + "_default DEFAULT NULL";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.executeUpdate();
            getConnection().commit();
        }
    }

    @Override
    public void revert() throws SQLException, IOException {
        throw new RuntimeException("No column dropping !");
    }
}
