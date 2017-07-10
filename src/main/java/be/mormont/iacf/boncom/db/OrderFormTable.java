package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;
import be.mormont.iacf.boncom.data.OrderFormEntry;

import java.sql.*;
import java.util.ArrayList;


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

    private void addOrderFormWithAutoNumber(OrderForm orderForm, Callback<OrderForm> callback, boolean commit) {
        try {
            Database db = Database.getDatabase();

            db.executeUpdatePreparedStatement(new AddFormPreparedStatementBuilder(orderForm, callback, commit) {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return insertStatementWithAutoNumber(conn, orderForm);
                }
            }, false);

        } catch (SQLException e) {
            callback.failure(e);
        }
    }

    private void addOrderFormWithAutoNumber(OrderForm orderForm, Callback<OrderForm> callback) {
        addOrderFormWithAutoNumber(orderForm, callback, true);
    }

    private void addOrderFormWithDefinedNumber(OrderForm orderForm, Callback<OrderForm> callback, boolean commit) {
        try {
            Database db = Database.getDatabase();

            db.executeUpdatePreparedStatement(new AddFormPreparedStatementBuilder(orderForm, callback, commit) {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return insertStatement(conn, orderForm);
                }
            }, false);

        } catch (SQLException e) {
            callback.failure(e);
        }
    }

    private void addOrderFormWithDefinedNumber(OrderForm orderForm, Callback<OrderForm> callback) {
        addOrderFormWithDefinedNumber(orderForm, callback, true);
    }

    /**
     * Add an order form to the database
     * @param orderForm The order form to add
     * @param callback The callback
     */
    public void addOrderForm(OrderForm orderForm, Callback<OrderForm> callback) {
        addOrderForm(orderForm, callback, true);
    }

    public void addOrderForm(OrderForm orderForm, Callback<OrderForm> callback, boolean commit) {
        if (orderForm.isNumberDefined()) {
            addOrderFormWithDefinedNumber(orderForm, callback, commit);
        } else {
            addOrderFormWithAutoNumber(orderForm, callback, commit);
        }
    }

    private abstract class AddFormPreparedStatementBuilder implements Database.PreparedStatementBuilder<OrderForm> {
        private OrderForm orderForm;
        private Callback<OrderForm> callback;
        private boolean commit;

        public AddFormPreparedStatementBuilder(OrderForm orderForm, Callback<OrderForm> callback, boolean commit) {
            this.orderForm = orderForm;
            this.callback = callback;
            this.commit = commit;
        }

        @Override
        public OrderForm success(ResultSet resultSet, PreparedStatement statement) {
            try {
                long formId = Database.getLastId(statement);

                for (OrderFormEntry entry: orderForm.getEntries()) {
                    entry.setOrderFormId(formId);
                }

                new OrderFormEntryTable().addOrderFormEntries(orderForm.getEntries(), new Callback<ArrayList<OrderFormEntry>>() {
                    @Override
                    public void success(ArrayList<OrderFormEntry> object) {
                        callback.success(orderForm);
                    }

                    @Override
                    public void failure(Exception e) {
                        callback.failure(e);
                    }
                }, commit);
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
