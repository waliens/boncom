package be.mormont.iacf.boncom.data;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class OrderForm {
    private Entity purchaser;
    private Entity provider;
    private Date date;
    private ArrayList<OrderFormEntry> entries;
}
