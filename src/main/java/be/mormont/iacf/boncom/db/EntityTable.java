package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Date: 01-07-17
 * By  : Mormont Romain
 */
public class EntityTable extends BaseTable<Entity> {
    static public String FIELD_ID = "id";
    static public String FIELD_ENTITY_NAME = "entity_name";
    static public String FIELD_PHONE_NUMBERS = "phone_numbers";
    static public String FIELD_STREET = "street";
    static public String FIELD_HOUSE_NUMBER = "house_number";
    static public String FIELD_BOX = "box";
    static public String FIELD_POST_CODE = "post_code";
    static public String FIELD_CITY = "city";
    static public String FIELD_CUSTOMER_NB = "customer_nb";

    static public String NAME = "entity";

    static long IACF_ENTITY_ID = 1;

    private static Entity makeEntity(ResultSet resultSet) throws SQLException {
        Address entityAddress = new Address(
                resultSet.getString(FIELD_STREET), // Street
                resultSet.getString(FIELD_HOUSE_NUMBER), // Number
                resultSet.getString(FIELD_BOX), // Box
                resultSet.getString(FIELD_POST_CODE), // PostCode
                resultSet.getString(FIELD_CITY) // City
        );

        return new Entity(
                resultSet.getLong(FIELD_ID),
                resultSet.getString(FIELD_ENTITY_NAME),
                entityAddress,
                resultSet.getString(FIELD_PHONE_NUMBERS).split(","),
                resultSet.getString(FIELD_CUSTOMER_NB)  // Customer number
        );
    }

    @Override
    String insertQuery() {
        return "INSERT INTO " + NAME + "(" +
                 FIELD_ENTITY_NAME + ", " + FIELD_PHONE_NUMBERS + ", " + FIELD_STREET + ", " +
                 FIELD_HOUSE_NUMBER + ", " + FIELD_BOX + ", " + FIELD_POST_CODE + ", " +
                 FIELD_CITY + ", " + FIELD_CUSTOMER_NB +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public PreparedStatement insertStatement(Connection conn, Entity object) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(insertQuery());
        statement.setString(1, object.getName());
        statement.setString(2, object.getPhonesAsString());
        statement.setString(3, object.getAddress().getStreet());
        statement.setString(4, object.getAddress().getNumber());
        statement.setString(5, object.getAddress().getBox());
        statement.setString(6, object.getAddress().getPostCode());
        statement.setString(7, object.getAddress().getCity());
        statement.setString(8, object.getCustomerNb());
        return statement;
    }

    @Override
    String selectQuery() {
        return  "SELECT * FROM " + NAME + " WHERE " + FIELD_ID + "=?";
    }

    @Override
    String deleteQuery() {
        return "DELETE FROM " + NAME + " WHERE " + FIELD_ID + "=?";
    }

    @Override
    String selectAllQuery() {
        return "SELECT * FROM " + NAME;
    }

    public void insertEntity(Entity entity, Callback<Entity> callback) {
        try {
            Database.getDatabase().executePreparedStatement(new Database.PreparedStatementNoReturnBuilder<Entity>() {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return insertStatement(conn, entity);
                }

                @Override
                public void success(PreparedStatement statement) {
                    callback.success(entity);
                }

                @Override
                public void failure(Exception e) { callback.failure(e); }
            }, true);
        } catch (SQLException | IOException e) {
            callback.failure(e);
        }
    }


    public void getEntity(long id, Callback<Entity> callback) {
        try {
            Database.getDatabase().executePreparedStatement(new Database.PreparedStatementWithReturnBuilder<Entity>() {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return selectStatement(conn, id);
                }

                @Override
                public Entity success(ResultSet resultSet, PreparedStatement statement) {
                    try {
                        if (resultSet.next()) {
                            Entity entity = makeEntity(resultSet);
                            callback.success(entity);
                            return entity;
                        } else {
                            throw new NoSuchElementException("There is no entity with identifier " + id);
                        }
                    } catch (Exception e) {
                        callback.failure(e);
                    }
                    return null;
                }

                @Override
                public void failure(Exception e) { callback.failure(e); }
            });
        } catch (SQLException | IOException e) {
            callback.failure(e);
        }
    }

    public void getAllEntities(Callback<ArrayList<Entity>> callback) {
        try {
            Database.getDatabase().executePreparedStatement(new Database.PreparedStatementWithReturnBuilder<ArrayList<Entity>>() {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return selectAllStatement(conn);
                }

                @Override
                public ArrayList<Entity> success(ResultSet resultSet, PreparedStatement statement) {
                    try {
                        ArrayList<Entity> data = new ArrayList<>();
                        while (resultSet.next()) {
                            data.add(makeEntity(resultSet));
                        }
                        callback.success(data);
                        return data;
                    } catch (Exception e) {
                        callback.failure(e);
                    }
                    return null;
                }

                @Override
                public void failure(Exception e) { callback.failure(e); }
            });
        } catch (SQLException | IOException e) {
            callback.failure(e);
        }
    }

    public void deleteEntity(long id, Callback<Entity> callback) {
        try {
            Database.getDatabase().executePreparedStatement(new Database.PreparedStatementNoReturnBuilder<Entity>() {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return deleteStatement(conn, id);
                }

                @Override
                public void success(PreparedStatement statement) {
                    // TODO return something else then null ??
                    callback.success(null);
                }

                @Override
                public void failure(Exception e) { callback.failure(e); }
            }, true);
        } catch (SQLException | IOException e) {
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

    public void updateEntity(Entity entity, Callback<Entity> callback) {
        try {
            Database.getDatabase().executePreparedStatement(new Database.PreparedStatementNoReturnBuilder<Entity>() {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return updateStatement(entity, conn);
                }

                @Override
                public void success(PreparedStatement statement) {
                    callback.success(entity);
                }

                @Override
                public void failure(Exception e) { callback.failure(e); }
            }, true);
        } catch (SQLException | IOException e) {
            callback.failure(e);
        }
    }

    private String updateQuery() {
        return "UPDATE " + NAME +
                " SET "
                 + FIELD_ENTITY_NAME + "=?, "
                 + FIELD_PHONE_NUMBERS + "=?, "
                 + FIELD_STREET + "=?, "
                 + FIELD_HOUSE_NUMBER + "=?, "
                 + FIELD_BOX + "=?, "
                 + FIELD_POST_CODE + "=?, "
                 + FIELD_CITY + "=?, "
                 + FIELD_CUSTOMER_NB + "=? " +
                " WHERE " + EntityTable.FIELD_ID + "=?";
    }

    private PreparedStatement updateStatement(Entity entity, Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(updateQuery());
        statement.setString(1, entity.getName());
        statement.setString(2, entity.getPhonesAsString());
        statement.setString(3, entity.getAddress().getStreet());
        statement.setString(4, entity.getAddress().getNumber());
        statement.setString(5, entity.getAddress().getBox());
        statement.setString(6, entity.getAddress().getPostCode());
        statement.setString(7, entity.getAddress().getCity());
        statement.setString(8, entity.getCustomerNb());
        statement.setLong(9, entity.getId());
        return statement;
    }
}
