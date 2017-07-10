package be.mormont.iacf.boncom.data;

import java.math.BigDecimal;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class OrderFormEntry {
    private long id;
    private long orderFormId;
    private String reference;
    private String designation;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderFormEntry(long id, long orderFormId, String reference, String designation, int quantity, BigDecimal unitPrice) {
        this.id = id;
        this.orderFormId = orderFormId;
        this.reference = reference;
        this.designation = designation;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public OrderFormEntry(String reference, String designation, int quantity, BigDecimal unitPrice) {
        this(-1, -1, reference, designation, quantity, unitPrice);
    }

    public BigDecimal getTotal() {
        return unitPrice.multiply(new BigDecimal((quantity)));
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderFormId() {
        return orderFormId;
    }

    public void setOrderFormId(long orderFormId) {
        this.orderFormId = orderFormId;
    }
}
