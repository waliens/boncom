package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;
import be.mormont.iacf.boncom.data.OrderFormEntry;
import com.sun.org.apache.xpath.internal.operations.Or;

import javax.xml.crypto.Data;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


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
        String providerSelect = "SELECT " +
                    EntityTable.FIELD_ID + " AS provider_id, " +
                    EntityTable.FIELD_ENTITY_NAME + " AS provider_name, " +
                    EntityTable.FIELD_STREET + " AS provider_street, " +
                    EntityTable.FIELD_HOUSE_NUMBER + " AS provider_number, " +
                    EntityTable.FIELD_BOX + " AS provider_box, " +
                    EntityTable.FIELD_CITY + " AS provider_city, " +
                    EntityTable.FIELD_POST_CODE + " AS provider_post_code, " +
                    EntityTable.FIELD_PHONE_NUMBERS + " AS provider_phone " +
                " FROM " + EntityTable.NAME;
        String purchaserSelect = "SELECT " +
                EntityTable.FIELD_ID + " AS purchaser_id, " +
                EntityTable.FIELD_ENTITY_NAME + " AS purchaser_name, " +
                EntityTable.FIELD_STREET + " AS purchaser_street, " +
                EntityTable.FIELD_HOUSE_NUMBER + " AS purchaser_number, " +
                EntityTable.FIELD_BOX + " AS purchaser_box, " +
                EntityTable.FIELD_CITY + " AS purchaser_city, " +
                EntityTable.FIELD_POST_CODE + " AS purchaser_post_code, " +
                EntityTable.FIELD_PHONE_NUMBERS + " AS purchaser_phone " +
                " FROM " + EntityTable.NAME;
        return "SELECT * FROM " + NAME +
                " INNER JOIN (" + providerSelect + ") as provider ON " + NAME + "." + FIELD_PROVIDER + "=provider.provider_id"  +
                " INNER JOIN (" + purchaserSelect + ") as purchaser ON " + NAME + "." + FIELD_PURCHASER  + "=purchaser.purchaser_id";
     }

    @Override
    PreparedStatement selectAllStatement(Connection conn) throws SQLException {
        return conn.prepareStatement(selectAllQuery());
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

    private Entity getEntityWithOffset(ResultSet set, int offset) throws SQLException{
        Address address = new Address(
            set.getString(offset + 3),
            set.getString(offset + 4),
            set.getString(offset + 5),
            set.getString(offset + 6),
            set.getString(offset + 7)
        );
        return new Entity(
            set.getLong(offset + 1),
            set.getString(offset + 2),
            address,
            set.getString(offset + 8).split(",")
        );
    }

    private OrderForm makeShallowOrderForm(ResultSet set) throws SQLException {
        final int OFFSET_PROVIDER = 5, OFFSET_PURCHASER = OFFSET_PROVIDER + 8;
        Date date = set.getDate(4);
        return new OrderForm(
            set.getLong(1),
            set.getLong(5),
            getEntityWithOffset(set, OFFSET_PURCHASER),
            getEntityWithOffset(set, OFFSET_PROVIDER),
            date.toLocalDate(),
            new ArrayList<>()
        );
    }

    public void getAllOrderForms(Callback<ArrayList<OrderForm>> callback) {
        try {
            Connection conn = Database.getDatabase().getConnection();
            try (PreparedStatement orderFormsStatement = selectAllStatement(conn); ResultSet orderFormsSet = orderFormsStatement.executeQuery()) {
                ArrayList<OrderForm> orderForms = new ArrayList<>();
                OrderFormEntryTable orderFormEntryTable = new OrderFormEntryTable();
                while (orderFormsSet.next()) {
                    OrderForm form = makeShallowOrderForm(orderFormsSet);
                    form.setEntries(orderFormEntryTable.getFormEntries(form.getId()));
                    orderForms.add(form);
                }
                callback.success(orderForms);
            }
        } catch (SQLException e) {
            callback.failure(e);
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
