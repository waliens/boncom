package be.mormont.iacf.boncom.data;

import java.math.BigDecimal;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class OrderFormEntry {
    private String reference;
    private String designation;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderFormEntry(String reference, String designation, int quantity, BigDecimal unitPrice) {
        this.reference = reference;
        this.designation = designation;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
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
}
