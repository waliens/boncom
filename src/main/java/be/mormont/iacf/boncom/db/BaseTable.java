package be.mormont.iacf.boncom.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Date: 01-07-17
 * By  : Mormont Romain
 */
public interface BaseTable<T> {
    String insertQuery();
    PreparedStatement insertStatement(Connection conn, T object) throws SQLException;
}
