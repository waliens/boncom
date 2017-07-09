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

    PreparedStatement insertStatementWithAutoNumber(Connection conn, OrderForm orderForm) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(
            "INSERT INTO " + NAME + "(" +
                    FIELD_PROVIDER + ", " + FIELD_PURCHASER + ", " +
                    FIELD_ISSUE_DATE + ", " + FIELD_NUMBER +
                    ") SELECT ?, ?, ?, IFNULL(MAX(" + FIELD_NUMBER + "), 0) + 1 FROM " + NAME
        );
        statement.setLong(1, orderForm.getProvider().getId());
        statement.setLong(2, orderForm.getPurchaser().getId());
        statement.setDate(3, Date.valueOf(orderForm.getDate()));
        return statement;
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

    private void addOrderFormWithAutoNumber(OrderForm orderForm, Callback<OrderForm> callback) {
        try {
            Database db = Database.getDatabase();

            db.executeUpdatePreparedStatement(new AddFormPreparedStatementBuilder(orderForm, callback) {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return insertStatementWithAutoNumber(conn, orderForm);
                }
            }, false);

        } catch (SQLException e) {
            callback.failure(e);
        }
    }

    private void addOrderFormWithDefinedNumber(OrderForm orderForm, Callback<OrderForm> callback) {
        try {
            Database db = Database.getDatabase();

            db.executeUpdatePreparedStatement(new AddFormPreparedStatementBuilder(orderForm, callback) {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return insertStatement(conn, orderForm);
                }
            }, false);

        } catch (SQLException e) {
            callback.failure(e);
        }
    }

    /**
     * Add an order form to the database
     * @param orderForm The order form to add
     * @param callback The callback
     */
    public void addOrderForm(OrderForm orderForm, Callback<OrderForm> callback) {
        if (orderForm.isNumberDefined()) {
            addOrderFormWithDefinedNumber(orderForm, callback);
        } else {
            addOrderFormWithAutoNumber(orderForm, callback);
        }
    }

    private abstract class AddFormPreparedStatementBuilder implements Database.PreparedStatementBuilder<OrderForm> {
        private OrderForm orderForm;
        private Callback<OrderForm> callback;

        public AddFormPreparedStatementBuilder(OrderForm orderForm, Callback<OrderForm> callback) {
            this.orderForm = orderForm;
            this.callback = callback;
        }

        @Override
        public OrderForm success(ResultSet resultSet, PreparedStatement statement) {
            try {
                long formId = Database.getLastId(statement);
                Database.getDatabase().getConnection().commit();
                callback.success(orderForm);
            } catch (SQLException e) {
                callback.failure(e);
            }
            return orderForm;
        }

        @Override
        public void failure(Exception e) {
            callback.failure(e);
        }
    }
}
