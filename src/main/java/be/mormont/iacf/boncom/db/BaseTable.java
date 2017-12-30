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

    /**
     * @return A query selecting one item based on its ids
     */
    abstract String selectQuery();

    PreparedStatement selectStatement(Connection conn, long id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(selectQuery());
        statement.setLong(1, id);
        return statement;
    }

    /**
     * @return query deleting one item based on its ids
     */
    abstract String deleteQuery();

    PreparedStatement deleteStatement(Connection conn, long id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(deleteQuery());
        statement.setLong(1, id);
        return statement;
    }

    /**
     * @return query selecting all entries of the table
     */
    abstract String selectAllQuery();

    PreparedStatement selectAllStatement(Connection conn) throws SQLException {
        return conn.prepareStatement(selectAllQuery());
    }
}
