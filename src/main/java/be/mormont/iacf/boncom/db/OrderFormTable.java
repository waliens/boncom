package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;

import java.sql.*;


/**
 * Date: 01-07-17
 * By  : Mormont Romain
 */
public class OrderFormTable extends BaseTable<OrderForm> {
    static String FIELD_ID = "id";
    static String FIELD_PROVIDER = "provider";
    static String FIELD_PURCHASER = "purchaser";
    static String FIELD_ISSUE_DATE = "issue_date";
    static String FIELD_NUMBER = "number";

    static final String NAME = "order_form"; // table name

    @Override
    String insertQuery() {
        return "INSERT INTO " + NAME + "(" +
                    FIELD_PROVIDER + ", " + FIELD_PURCHASER + ", " +
                    FIELD_ISSUE_DATE + ", " + FIELD_NUMBER +
                ") VALUES (?, ?, ?, ?)";
    }

    @Override
    PreparedStatement insertStatement(Connection conn, OrderForm object) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(insertQuery());
        statement.setLong(1, object.getProvider().getId());
        statement.setLong(2, object.getPurchaser().getId());
        statement.setDate(3, Date.valueOf(object.getDate()));
        statement.setLong(4, object.getNumber());
        return statement;
    }

    @Override
    String selectQuery() {
        return null;
    }

    @Override
    PreparedStatement selectStatement(Connection conn, long id) throws SQLException {
        return null;
    }

    @Override
    String selectAllQuery() {
        return null;
    }

    @Override
    PreparedStatement selectAllStatement(Connection conn) throws SQLException {
        return null;
    }

    void addOrderForm(OrderForm orderForm, Callback<OrderForm> callback) {
        try {
            Database db = Database.getDatabase();

            db.executeUpdatePreparedStatement(new Database.PreparedStatementBuilder<OrderForm>() {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return insertStatement(conn, orderForm);
                }

                @Override
                public OrderForm success(ResultSet resultSet, PreparedStatement statement) {
                    try {
                        long formId = Database.getLastId(statement);
                        callback.success(orderForm);
                        db.getConnection().commit();
                        return orderForm;
                    } catch (SQLException e) {
                        callback.failure(e);
                    }
                    return orderForm;
                }

                @Override
                public void failure(Exception e) {
                    callback.failure(e);
                }
            }, false);

        } catch (SQLException e) {
            callback.failure(e);
        }

    }
}
