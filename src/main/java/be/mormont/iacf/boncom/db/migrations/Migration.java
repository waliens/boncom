package be.mormont.iacf.boncom.db.migrations;

import be.mormont.iacf.boncom.db.Database;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Date: 21-01-18
 * By  : Mormont Romain
 */
public interface Migration {

    void execute() throws SQLException, IOException;
    void revert() throws SQLException, IOException;

    default Connection getConnection() throws IOException, SQLException {
        return Database.getDatabase().getConnection();
    }
}
