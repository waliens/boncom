package be.mormont.iacf.boncom.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Date: 01-07-17
 * By  : Mormont Romain
 */
abstract class BaseTable<T> {
    abstract String insertQuery();
    abstract PreparedStatement insertStatement(Connection conn, T object) throws SQLException;
    abstract String selectQuery();
    abstract PreparedStatement selectStatement(Connection conn, long id) throws SQLException;
    abstract String selectAllQuery();
    abstract PreparedStatement selectAllStatement(Connection conn) throws SQLException;
}
