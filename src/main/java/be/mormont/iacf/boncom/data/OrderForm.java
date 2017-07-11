package be.mormont.iacf.boncom.data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class OrderForm {
    public static long UNDEFINED_NUMBER = -1;

    private long number;
    private Entity purchaser;
    private Entity provider;
    private LocalDate date;
    private ArrayList<OrderFormEntry> entries;

    public OrderForm(long number, Entity purchaser, Entity provider, LocalDate date, ArrayList<OrderFormEntry> entries) {
        this.number = number;
        this.purchaser = purchaser;
        this.provider = provider;
        this.date = date;
        this.entries = entries;
    }

    /**
     * Constructor for order form with undefined number
     */
    public OrderForm(Entity purchaser, Entity provider, LocalDate date, ArrayList<OrderFormEntry> entries) {
        this(UNDEFINED_NUMBER, purchaser, provider, date, entries);
    }

    public Entity getPurchaser() {
        return purchaser;
    }

    public void setPurchaser(Entity purchaser) {
        this.purchaser = purchaser;
    }

    public Entity getProvider() {
        return provider;
    }

    public void setProvider(Entity provider) {
        this.provider = provider;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ArrayList<OrderFormEntry> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<OrderFormEntry> entries) {
        this.entries = entries;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    /**
     * The number is undefined if negative or zero
     * @return true if the number is defined, false otherwise
     */
    public boolean isNumberDefined() {
        return number > 0;
    }
}
