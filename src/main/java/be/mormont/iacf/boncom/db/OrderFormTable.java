package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;
import be.mormont.iacf.boncom.data.OrderFormEntry;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Date: 01-07-17
 * By  : Mormont Romain
 */
public class OrderFormTable extends BaseTable<OrderForm> {
    static final public String FIELD_ID = "id";
    static final public String FIELD_PROVIDER = "provider";
    static final public String FIELD_PURCHASER = "purchaser";
    static final public String FIELD_ISSUE_DATE = "issue_date";
    static final public String FIELD_NUMBER = "number";
    static final public String FIELD_DELIVERY_DATE = "delivery_date";

    static final public String NAME = "order_form"; // table name

    @Override
    String insertQuery() {
        return "INSERT INTO " + NAME + "(" +
                    FIELD_PROVIDER + ", " + FIELD_PURCHASER + ", " +
                    FIELD_ISSUE_DATE + ", " + FIELD_NUMBER + ", " + FIELD_DELIVERY_DATE +
                ") VALUES (?, ?, ?, ?, ?)";
    }

    PreparedStatement insertStatementWithAutoNumber(Connection conn, OrderForm orderForm) throws SQLException {
        // TODO take into account yearly number reset !
        PreparedStatement statement = conn.prepareStatement(
            "INSERT INTO " + NAME + "(" +
                    FIELD_PROVIDER + ", " + FIELD_PURCHASER + ", " +
                    FIELD_ISSUE_DATE + ", " + FIELD_NUMBER + ", " + FIELD_DELIVERY_DATE +
                    ") SELECT ?, ?, ?, IFNULL(MAX(" + FIELD_NUMBER + "), 0) + 1, ? FROM " + NAME
        );
        statement.setLong(1, orderForm.getProvider().getId());
        statement.setLong(2, orderForm.getPurchaser().getId());
        statement.setDate(3, Date.valueOf(orderForm.getDate()));
        statement.setDate(4, orderForm.hasDeliveryDate() ? Date.valueOf(orderForm.getDeliveryDate()) : null);
        return statement;
    }

    @Override
    PreparedStatement insertStatement(Connection conn, OrderForm object) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(insertQuery());
        statement.setLong(1, object.getProvider().getId());
        statement.setLong(2, object.getPurchaser().getId());
        statement.setDate(3, Date.valueOf(object.getDate()));
        statement.setLong(4, object.getNumber());
        statement.setDate(5, object.hasDeliveryDate() ? Date.valueOf(object.getDeliveryDate()) : null);
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
                    EntityTable.FIELD_ID + " AS provider_" + EntityTable.FIELD_ID + " , " +
                    EntityTable.FIELD_ENTITY_NAME + " AS provider_" + EntityTable.FIELD_ENTITY_NAME + " , " +
                    EntityTable.FIELD_STREET + " AS provider_" + EntityTable.FIELD_STREET + " , " +
                    EntityTable.FIELD_HOUSE_NUMBER + " AS provider_" + EntityTable.FIELD_HOUSE_NUMBER + " , " +
                    EntityTable.FIELD_BOX + " AS provider_" + EntityTable.FIELD_BOX + " , " +
                    EntityTable.FIELD_CITY + " AS provider_" + EntityTable.FIELD_CITY + " , " +
                    EntityTable.FIELD_POST_CODE + " AS provider_" + EntityTable.FIELD_POST_CODE + " , " +
                    EntityTable.FIELD_PHONE_NUMBERS + " AS provider_" + EntityTable.FIELD_PHONE_NUMBERS + " , " +
                    EntityTable.FIELD_CUSTOMER_NB + " AS provider_" + EntityTable.FIELD_CUSTOMER_NB + " " +
                " FROM " + EntityTable.NAME;
        String purchaserSelect = "SELECT " +
                EntityTable.FIELD_ID + " AS purchaser_" + EntityTable.FIELD_ID + ", " +
                EntityTable.FIELD_ENTITY_NAME + " AS purchaser_" + EntityTable.FIELD_ENTITY_NAME + ", " +
                EntityTable.FIELD_STREET + " AS purchaser_" + EntityTable.FIELD_STREET + ", " +
                EntityTable.FIELD_HOUSE_NUMBER + " AS purchaser_" + EntityTable.FIELD_HOUSE_NUMBER + ", " +
                EntityTable.FIELD_BOX + " AS purchaser_" + EntityTable.FIELD_BOX + ", " +
                EntityTable.FIELD_CITY + " AS purchaser_" + EntityTable.FIELD_CITY + ", " +
                EntityTable.FIELD_POST_CODE + " AS purchaser_" + EntityTable.FIELD_POST_CODE + ", " +
                EntityTable.FIELD_PHONE_NUMBERS + " AS purchaser_" + EntityTable.FIELD_PHONE_NUMBERS + ", " +
                EntityTable.FIELD_CUSTOMER_NB + " AS purchaser_" + EntityTable.FIELD_CUSTOMER_NB + "" +
                " FROM " + EntityTable.NAME;
        return "SELECT * FROM " + NAME +
                " INNER JOIN (" + providerSelect + ") as provider ON " + NAME + "." + FIELD_PROVIDER + "=provider.provider_id"  +
                " INNER JOIN (" + purchaserSelect + ") as purchaser ON " + NAME + "." + FIELD_PURCHASER  + "=purchaser.purchaser_id";
     }

    private Entity getEntityWithPrefix(ResultSet set, String fieldPrefix) throws SQLException{
        Address address = new Address(
                set.getString(fieldPrefix + EntityTable.FIELD_STREET),
                set.getString(fieldPrefix + EntityTable.FIELD_HOUSE_NUMBER),
                set.getString(fieldPrefix + EntityTable.FIELD_BOX),
                set.getString(fieldPrefix + EntityTable.FIELD_CITY),
                set.getString(fieldPrefix + EntityTable.FIELD_POST_CODE)
        );
        return new Entity(
                set.getLong(fieldPrefix + EntityTable.FIELD_ID),
                set.getString(fieldPrefix + EntityTable.FIELD_ENTITY_NAME),
                address,
                set.getString(fieldPrefix + EntityTable.FIELD_PHONE_NUMBERS).split(","),
                set.getString(fieldPrefix + EntityTable.FIELD_CUSTOMER_NB)
        );
    }

    private OrderForm makeShallowOrderForm(ResultSet set) throws SQLException {
        Date deliveryDate = set.getDate(FIELD_DELIVERY_DATE);
        return new OrderForm(
                set.getLong(FIELD_ID),
                set.getLong(FIELD_NUMBER),
                getEntityWithPrefix(set, "purchaser_"),
                getEntityWithPrefix(set, "provider_"),
                set.getDate(FIELD_ISSUE_DATE).toLocalDate(),
                new ArrayList<>(),
                deliveryDate != null ? deliveryDate.toLocalDate() : null
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
        } catch (SQLException | IOException e) {
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

        } catch (SQLException | IOException e) {
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

        } catch (SQLException | IOException e) {
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

    private String deleteOrderFormQuery() {
        return "DELETE FROM " + NAME + " WHERE " + FIELD_ID + "=?";
    }

    PreparedStatement deleteStatement(Connection conn, long orderFormId) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(deleteQuery());
        statement.setLong(1, orderFormId);
        return statement;
    }

    public void deleteOrderForm(OrderForm orderForm, Callback<OrderForm> callback) {
        try {
            Connection conn = Database.getDatabase().getConnection();
            OrderFormEntryTable orderFormEntryTable = new OrderFormEntryTable();
            try (PreparedStatement orderFormStmt = deleteStatement(conn, orderForm.getId());
                 PreparedStatement entriesStmt = orderFormEntryTable.deleteByOrderFormStatement(conn, orderForm.getId())) {
                entriesStmt.executeUpdate();
                orderFormStmt.executeUpdate();
                conn.commit();
                callback.success(orderForm);
            }
        } catch (SQLException | IOException e) {
            callback.failure(e);
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
                    FIELD_PURCHASER + "=?, " +
                    FIELD_DELIVERY_DATE + "=? " +
                " WHERE " + FIELD_ID + "=?";
    }

    private PreparedStatement getUpdateStatement(Connection conn, OrderForm form) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(getUpdateQuery());
        statement.setDate(1, Date.valueOf(form.getDate()));
        statement.setLong(2, form.getNumber());
        statement.setLong(3, form.getProvider().getId());
        statement.setLong(4, form.getPurchaser().getId());
        statement.setDate(5, form.hasDeliveryDate() ? Date.valueOf(form.getDeliveryDate()) : null);
        statement.setLong(6, form.getId());
        return statement;
    }

    public void updateOrderForm(OrderForm form, Callback<OrderForm> callback) {
        Connection conn;
        try {
            conn = Database.getDatabase().getConnection();
        } catch (SQLException | IOException e) {
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
        } catch (SQLException | IOException e) {
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
        } catch (SQLException | IOException e) {
            callback.failure(e);
        }
    }


}
