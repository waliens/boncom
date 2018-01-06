package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.Address;
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
    String deleteQuery() {
        return "DELETE FROM " + OrderFormTable.NAME + " WHERE " + OrderFormTable.FIELD_ID + "=?";
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

    private void addOrderFormWithAutoNumber(OrderForm orderForm, Callback<OrderForm> callback, boolean commit) {
        try {
            Database db = Database.getDatabase();

            db.executePreparedStatement(new AddFormPreparedStatementBuilder(orderForm, callback, commit) {
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

            db.executePreparedStatement(new AddFormPreparedStatementBuilder(orderForm, callback, commit) {
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

    private abstract class AddFormPreparedStatementBuilder implements Database.PreparedStatementNoReturnBuilder<OrderForm> {
        private OrderForm orderForm;
        private Callback<OrderForm> callback;
        private boolean commit;

        public AddFormPreparedStatementBuilder(OrderForm orderForm, Callback<OrderForm> callback, boolean commit) {
            this.orderForm = orderForm;
            this.callback = callback;
            this.commit = commit;
        }

        @Override
        public void success(PreparedStatement statement) {
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
        }

        @Override
        public void failure(Exception e) {
            callback.failure(e);
        }
    }

    private String getUpdateQuery() {
        return "UPDATE " + NAME + " SET " +
                    FIELD_ISSUE_DATE + "=?, " +
                    FIELD_NUMBER + "=?, " +
                    FIELD_PROVIDER + "=?, " +
                    FIELD_PURCHASER + "=? " +
                " WHERE " + FIELD_ID + "=?";
    }

    private PreparedStatement getUpdateStatement(Connection conn, OrderForm form) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(getUpdateQuery());
        statement.setDate(1, Date.valueOf(form.getDate()));
        statement.setLong(2, form.getNumber());
        statement.setLong(3, form.getProvider().getId());
        statement.setLong(4, form.getPurchaser().getId());
        statement.setLong(5, form.getId());
        return statement;
    }

    public void updateOrderForm(OrderForm form, Callback<OrderForm> callback) {
        Connection conn;
        try {
            conn = Database.getDatabase().getConnection();
        } catch (SQLException e) {
            callback.failure(e);
            return;
        }

        try {
            try (PreparedStatement stmt = getUpdateStatement(conn, form)) {
                stmt.executeUpdate();
            }

            // delete removed entries
            OrderFormEntryTable orderFormEntryTable = new OrderFormEntryTable();
            ArrayList<Long> remainingIds = new ArrayList<>();
            for (OrderFormEntry entry : form.getEntries()) {
                if (entry.getId() != -1) {
                    remainingIds.add(entry.getId());
                }
            }
            long[] ids = new long[remainingIds.size()];
            for (int i = 0; i < ids.length; ++i) {
                ids[i] = remainingIds.get(i);
            }
            try (PreparedStatement stmt = orderFormEntryTable.getRemoveMissingEntriesStatement(conn, ids, form.getId())) {
                stmt.executeUpdate();
            }

            // update remaining/new entries
            for (OrderFormEntry entry : form.getEntries()) {
                orderFormEntryTable.updateEntry(entry);
            }
            conn.commit();
            callback.success(form);
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) { }
            callback.failure(e);
        }
    }

    private String countOrderFormsQuery() {
        return "SELECT COUNT(*) FROM " + NAME + " WHERE " + FIELD_PROVIDER + "=? OR " + FIELD_PURCHASER + "=?";
    }

    private PreparedStatement countOrderFormStatement(Connection conn, long entityId) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(countOrderFormsQuery());
        statement.setLong(1, entityId);
        statement.setLong(2, entityId);
        return statement;
    }

    public void countOrderFormOfEntity(long entityId, Callback<Long> callback) {
        try {
            Connection conn = Database.getDatabase().getConnection();
            try (PreparedStatement orderFormsStatement = countOrderFormStatement(conn, entityId);
                ResultSet countSet = orderFormsStatement.executeQuery()) {
                if (countSet.next()) {
                    callback.success(countSet.getLong(1));
                } else {
                    callback.success(0L);
                }
            }
        } catch (SQLException e) {
            callback.failure(e);
        }
    }


}
