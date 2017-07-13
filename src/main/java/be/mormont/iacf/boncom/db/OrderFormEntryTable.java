package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.OrderForm;
import be.mormont.iacf.boncom.data.OrderFormEntry;
import com.sun.org.apache.regexp.internal.RE;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Date: 01-07-17
 * By  : Mormont Romain
 */
public class OrderFormEntryTable extends BaseTable<OrderFormEntry> {
    static String FIELD_ID = "id";
    static String FIELD_ORDER_FORM = "order_form";
    static String FIELD_REFERENCE = "reference";
    static String FIELD_DESIGNATION = "designation";
    static String FIELD_QUANTITY = "quantity";
    static String FIELD_UNIT_PRICE = "unit_price";

    static final String NAME = "order_form_entry";


    @Override
    String insertQuery() {
        return "INSERT INTO " + NAME + "(" +
                FIELD_ORDER_FORM + ", " + FIELD_REFERENCE + ", " + FIELD_DESIGNATION + ", " +
                FIELD_QUANTITY + ", " + FIELD_UNIT_PRICE +
                ") VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    PreparedStatement insertStatement(Connection conn, OrderFormEntry object) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(insertQuery());
        statement.setLong(1, object.getOrderFormId());
        statement.setString(2, object.getReference());
        statement.setString(3, object.getDesignation());
        statement.setLong(4, object.getQuantity());
        statement.setBigDecimal(5, object.getUnitPrice());
        return statement;
    }

    @Override
    String selectQuery() {
        return "SELECT " +
                    FIELD_ID + ", " + FIELD_ORDER_FORM + ", " + FIELD_REFERENCE + ", " +
                    FIELD_DESIGNATION + ", " + FIELD_QUANTITY + ", " + FIELD_UNIT_PRICE +
                " FROM " + NAME + " WHERE " + FIELD_ID + "=?";
    }

    @Override
    PreparedStatement selectStatement(Connection conn, long id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(selectQuery());
        statement.setLong(1, id);
        return statement;
    }

    @Override
    String selectAllQuery() {
        return "SELECT " +
                    FIELD_ID + ", " + FIELD_ORDER_FORM + ", " + FIELD_REFERENCE + ", " +
                    FIELD_DESIGNATION + ", " + FIELD_QUANTITY + ", " + FIELD_UNIT_PRICE +
                " FROM " + NAME;
    }

    @Override
    PreparedStatement selectAllStatement(Connection conn) throws SQLException {
        return conn.prepareStatement(selectAllQuery());
    }

    String addBatchQuery(int n) {
        return "INSERT INTO " + NAME + "(" +
                FIELD_ORDER_FORM + ", " + FIELD_REFERENCE + ", " + FIELD_DESIGNATION + ", " +
                FIELD_QUANTITY + ", " + FIELD_UNIT_PRICE +
                ") VALUES " + String.join(", ", Collections.nCopies(n, "(?, ?, ?, ?, ?)"));
    }

    PreparedStatement addBatchStatement(Connection conn, List<OrderFormEntry> entries) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(addBatchQuery(entries.size()));
        int nParams = 5;
        for (int i = 0; i < entries.size(); ++i) {
            OrderFormEntry entry = entries.get(i);
            statement.setLong(i * nParams + 1, entry.getOrderFormId());
            statement.setString(i * nParams + 2, entry.getReference());
            statement.setString(i * nParams + 3, entry.getDesignation());
            statement.setInt(i * nParams + 4, entry.getQuantity());
            statement.setBigDecimal(i * nParams + 5, entry.getUnitPrice());
        }
        return statement;
    }

    void addOrderFormEntries(ArrayList<OrderFormEntry> entries, Callback<ArrayList<OrderFormEntry>> callback, boolean commit) {
        try {
            Database.getDatabase().executeUpdatePreparedStatement(new Database.PreparedStatementBuilder<ArrayList<OrderFormEntry>>() {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return addBatchStatement(conn, entries);
                }

                @Override
                public ArrayList<OrderFormEntry> success(ResultSet resultSet, PreparedStatement statement) {
                    callback.success(entries);
                    return entries;
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

    private OrderFormEntry makeEntry(ResultSet set) throws SQLException {
        //long id, long orderFormId, String reference, String designation, int quantity, BigDecimal unitPrice
        return new OrderFormEntry(
            set.getLong(1),
            set.getLong(2),
            set.getString(3),
            set.getString(4),
            set.getInt(5),
            set.getBigDecimal(6)
        );
    }

    void getOrderFormEntries(OrderForm orderForm, Callback<ArrayList<OrderFormEntry>> callback) {
        try {
            Database.getDatabase().executePreparedStatement(new Database.PreparedStatementBuilder<ArrayList<OrderFormEntry>>() {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return selectStatement(conn, orderForm.getId());
                }

                @Override
                public ArrayList<OrderFormEntry> success(ResultSet resultSet, PreparedStatement statement) {
                    ArrayList<OrderFormEntry> entries = new ArrayList<>();
                    try {
                        while(resultSet.next()) {
                            entries.add(makeEntry(resultSet));
                        }
                        callback.success(entries);
                    } catch (SQLException e) {
                        callback.failure(e);
                        e.printStackTrace();
                    }
                    return entries;
                }

                @Override
                public void failure(Exception e) {
                    callback.failure(e);
                }
            });
        } catch (SQLException e) {
            callback.failure(e);
        }
    }

    private String selectFormEntriesQuery() {
        return "SELECT " +
                    FIELD_ID + ", " + FIELD_ORDER_FORM + ", " + FIELD_REFERENCE + ", " +
                    FIELD_DESIGNATION + ", " + FIELD_QUANTITY + ", " + FIELD_UNIT_PRICE +
                " FROM " + NAME + " WHERE " + FIELD_ORDER_FORM + "=?";
    }

    private PreparedStatement selectFormEntriesStatement(Connection conn, long orderFormId) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(selectFormEntriesQuery());
        statement.setLong(1, orderFormId);
        return statement;
    }

    ArrayList<OrderFormEntry> getFormEntries(long orderFormid) throws SQLException {
        Connection conn = Database.getDatabase().getConnection();
        try (PreparedStatement statement = selectFormEntriesStatement(conn, orderFormid); ResultSet set = statement.executeQuery()) {
            ArrayList<OrderFormEntry> entries = new ArrayList<>();
            while(set.next()) {
                entries.add(makeEntry(set));
            }
            return entries;
        }
    }
}
