package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Date: 01-07-17
 * By  : Mormont Romain
 */
public class EntityTable extends BaseTable<Entity> {
    static String FIELD_ID = "id";
    static String FIELD_ENTITY_NAME = "entity_name";
    static String FIELD_PHONE_NUMBERS = "phone_numbers";
    static String FIELD_STREET = "street";
    static String FIELD_HOUSE_NUMBER = "house_number";
    static String FIELD_BOX = "box";
    static String FIELD_POST_CODE = "post_code";
    static String FIELD_CITY = "city";

    static String NAME = "entity";

    static long IACF_ENTITY_ID = 1;

    @Override
    String insertQuery() {
        return "INSERT INTO " + NAME + "(" +
                 EntityTable.FIELD_ENTITY_NAME + ", " + EntityTable.FIELD_PHONE_NUMBERS + ", " +
                 EntityTable.FIELD_STREET + ", " + EntityTable.FIELD_HOUSE_NUMBER + ", " +
                 EntityTable.FIELD_BOX + ", " + EntityTable.FIELD_POST_CODE + ", " +
                 EntityTable.FIELD_CITY  +
                ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    PreparedStatement insertStatement(Connection conn, Entity object) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(insertQuery());
        statement.setString(1, object.getName());
        statement.setString(2, String.join(",", object.getPhoneNumbers()));
        statement.setString(3, object.getAddress().getStreet());
        statement.setString(4, object.getAddress().getNumber());
        statement.setString(5, object.getAddress().getBox());
        statement.setString(6, object.getAddress().getPostCode());
        statement.setString(7, object.getAddress().getCity());
        return statement;
    }

    @Override
    String selectQuery() {
        return  "SELECT * FROM " + NAME + " WHERE " + FIELD_ID + "=?";
    }

    @Override
    PreparedStatement selectStatement(Connection conn, long id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(selectQuery());
        statement.setLong(1, id);
        return statement;
    }

    public void getEntity(long id, Callback<Entity> callback) {
        try {
            Database.getDatabase().executePreparedStatement(new Database.PreparedStatementBuilder<Entity>() {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return selectStatement(conn, id);
                }

                @Override
                public Entity success(ResultSet resultSet) {
                    try {
                        if (resultSet.next()) {
                            Address entityAddress = new Address(
                                resultSet.getString(3), // Street
                                resultSet.getString(4), // Number
                                resultSet.getString(5), // Box
                                resultSet.getString(6), // PostCode
                                resultSet.getString(7)  // City
                            );

                            Entity entity = new Entity(
                                resultSet.getString(1),
                                entityAddress,
                                resultSet.getString(8).split(",")
                            );
                            callback.success(entity);
                            return entity;
                        }
                    } catch (Exception e) {
                        callback.failure(e);
                    }
                    return null;
                }

                @Override
                public void failure(Exception e) { callback.failure(e); }
            });
        } catch (SQLException e) {
            callback.failure(e);
        }
    }

    /**
     * Get IACF entity from database
     * @param callback The callback
     */
    public void getIacf(Callback<Entity> callback) {
        getEntity(IACF_ENTITY_ID, callback);
    }
}
