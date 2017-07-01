package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.Entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Date: 01-07-17
 * By  : Mormont Romain
 */
public class EntityTable implements BaseTable<Entity> {
    static String FIELD_ID = "id";
    static String FIELD_ENTITY_NAME = "entity_name";
    static String FIELD_PHONE_NUMBERS = "phone_numbers";
    static String FIELD_STREET = "street";
    static String FIELD_HOUSE_NUMBER = "house_number";
    static String FIELD_BOX = "box";
    static String FIELD_POST_CODE = "post_code";
    static String FIELD_CITY = "city";

    static String NAME = "entity";

    @Override
    public String insertQuery() {
        return "INSERT INTO " + NAME + "(" +
                 EntityTable.FIELD_ENTITY_NAME + ", " + EntityTable.FIELD_PHONE_NUMBERS + ", " +
                 EntityTable.FIELD_STREET + ", " + EntityTable.FIELD_HOUSE_NUMBER + ", " +
                 EntityTable.FIELD_BOX + ", " + EntityTable.FIELD_POST_CODE + ", " +
                 EntityTable.FIELD_CITY  +
                ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public PreparedStatement insertStatement(Connection conn, Entity object) throws SQLException {
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
}
