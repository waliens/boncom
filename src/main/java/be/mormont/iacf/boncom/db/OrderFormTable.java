package be.mormont.iacf.boncom.db;

import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;


/**
 * Date: 01-07-17
 * By  : Mormont Romain
 */
public class OrderFormTable {
    static String FIELD_ID = "id";
    static String FIELD_PROVIDER = "provider";
    static String FIELD_PURCHASER = "purchaser";
    static String FIELD_ISSUE_DATE = "issue_date";
    static String FIELD_NUMBER = "number";

    static final String NAME = "order_form"; // table name

    public String insertQuery(OrderForm orderForm) {
        return "INSERT INTO " + NAME + "(" +
                 EntityTable.FIELD_ENTITY_NAME + ", " + EntityTable.FIELD_PHONE_NUMBERS + ", " +
                 EntityTable.FIELD_STREET + ", " + EntityTable.FIELD_HOUSE_NUMBER + ", " +
                 EntityTable.FIELD_BOX + ", " + EntityTable.FIELD_POST_CODE + ", " +
                 EntityTable.FIELD_CITY  +
                ") VALUES (" +
                "" +
                ")";
    }
}
