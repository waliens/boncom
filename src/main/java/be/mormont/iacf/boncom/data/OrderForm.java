package be.mormont.iacf.boncom.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class OrderForm implements Comparable<OrderForm> {
    public static long UNDEFINED_NUMBER = -1;

    private long id;
    private long number;
    private Entity purchaser;
    private Entity provider;
    private LocalDate date;
    private ArrayList<OrderFormEntry> entries;
    private LocalDate deliveryDate;

    public OrderForm(long id, long number, Entity purchaser, Entity provider, LocalDate date, List<OrderFormEntry> entries, LocalDate deliveryDate) {
        this.number = number;
        this.purchaser = purchaser;
        this.provider = provider;
        this.date = date;
        this.entries = new ArrayList<>();
        this.entries.addAll(entries);
        this.id = id;
    }

    public OrderForm(long number, Entity purchaser, Entity provider, LocalDate date, List<OrderFormEntry> entries) {
        this(-1, number, purchaser, provider, date, entries, null);
    }

    public OrderForm(long number, Entity purchaser, Entity provider, LocalDate date, List<OrderFormEntry> entries, LocalDate deliveryDate) {
        this(-1, number, purchaser, provider, date, entries, deliveryDate);
    }

    /**
     * Constructor for order form with undefined number
     */
    public OrderForm(Entity purchaser, Entity provider, LocalDate date, List<OrderFormEntry> entries, LocalDate deliveryDate) {
        this(UNDEFINED_NUMBER, purchaser, provider, date, entries, deliveryDate);
    }

    /**
     * Constructor for order form with undefined number
     */
    public OrderForm(Entity purchaser, Entity provider, LocalDate date, List<OrderFormEntry> entries) {
        this(UNDEFINED_NUMBER, purchaser, provider, date, entries, null);
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

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public boolean hasDeliveryDate() {
        return deliveryDate != null;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * The number is undefined if negative or zero
     * @return true if the number is defined, false otherwise
     */
    public boolean isNumberDefined() {
        return number > 0;
    }

    public BigDecimal getTotal() {
        BigDecimal total = new BigDecimal(0);
        for(OrderFormEntry e : getEntries()) {
            total = total.add(e.getTotal());
        }
        return total;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OrderForm && ((OrderForm) obj).compareTo(this) == 0;
    }

    @Override
    public int compareTo(OrderForm o) {
        return (int) (getId() - o.getId());
    }
}
