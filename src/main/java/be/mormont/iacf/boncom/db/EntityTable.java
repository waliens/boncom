package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;

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

    private static Entity makeEntity(ResultSet resultSet) throws SQLException {
        Address entityAddress = new Address(
                resultSet.getString(4), // Street
                resultSet.getString(5), // Number
                resultSet.getString(6), // Box
                resultSet.getString(7), // PostCode
                resultSet.getString(8)  // City
        );

        return new Entity(
                resultSet.getLong(1),
                resultSet.getString(2),
                entityAddress,
                resultSet.getString(3).split(",")
        );
    }

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
        statement.setString(2, object.getPhonesAsString());
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

    @Override
    String selectAllQuery() {
        return "SELECT * FROM " + NAME;
    }

    @Override
    PreparedStatement selectAllStatement(Connection conn) throws SQLException {
        return conn.prepareStatement(selectAllQuery());
    }

    public void insertEntity(Entity entity, Callback<Entity> callback) {
        try {
            Database.getDatabase().executeUpdatePreparedStatement(new Database.PreparedStatementBuilder<Entity>() {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return insertStatement(conn, entity);
                }

                @Override
                public Entity success(ResultSet resultSet) {
                    callback.success(entity);
                    return entity;
                }

                @Override
                public void failure(Exception e) { callback.failure(e); }
            });
        } catch (SQLException e) {
            callback.failure(e);
        }
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
        } catch (SQLException e) {
            callback.failure(e);
        }
    }

    public void getAllEntities(Callback<ArrayList<Entity>> callback) {
        try {
            Database.getDatabase().executePreparedStatement(new Database.PreparedStatementBuilder<ArrayList<Entity>>() {
                @Override
                public PreparedStatement getStatement(Connection conn) throws SQLException {
                    return selectAllStatement(conn);
                }

                @Override
                public ArrayList<Entity> success(ResultSet resultSet) {
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
