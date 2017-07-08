package be.mormont.iacf.boncom.data;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class OrderForm {
    private long number;
    private Entity purchaser;
    private Entity provider;
    private LocalDate date;
    private ArrayList<OrderFormEntry> entries;

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
}
