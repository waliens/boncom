package be.mormont.iacf.boncom.ui.util;

import be.mormont.iacf.boncom.data.OrderFormEntry;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;

import java.math.BigDecimal;

/**
 * Date: 23-12-17
 * By  : Mormont Romain
 */
public class ObservableOrderFormEntry {

    private SimpleLongProperty id;
    private SimpleLongProperty orderFormId;
    private SimpleStringProperty reference;
    private SimpleStringProperty designation;
    private SimpleIntegerProperty quantity;
    private SimpleObjectProperty<BigDecimal> unitPrice;
    private SimpleObjectProperty<BigDecimal> totalPrice;


    public ObservableOrderFormEntry(long id, long orderFormId, String reference, String designation, int quantity, BigDecimal unitPrice) {
        this.id = new SimpleLongProperty(id);
        this.orderFormId = new SimpleLongProperty(orderFormId);
        this.reference = new SimpleStringProperty(reference);
        this.designation = new SimpleStringProperty(designation);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = new SimpleObjectProperty<>(unitPrice);
        ObjectBinding<BigDecimal> objectBinding = new ObjectBinding<BigDecimal>() {
            {
                bind(ObservableOrderFormEntry.this.quantity, ObservableOrderFormEntry.this.unitPrice);
            }

            @Override
            protected BigDecimal computeValue() {
                BigDecimal quantity = new BigDecimal(ObservableOrderFormEntry.this.quantity.get());
                return ObservableOrderFormEntry.this.unitPrice.get().multiply(quantity);
            }
        };
        this.totalPrice = new SimpleObjectProperty<>(new BigDecimal(0));
        this.totalPrice.bind(objectBinding);
    }

    public ObservableOrderFormEntry(String reference, String designation, int quantity, BigDecimal unitPrice) {
        this(-1, -1, reference, designation, quantity, unitPrice);
    }

    public ObservableOrderFormEntry(OrderFormEntry orderFormEntry) {
        this(orderFormEntry.getId(),
                orderFormEntry.getOrderFormId(),
                orderFormEntry.getReference(),
                orderFormEntry.getDesignation(),
                orderFormEntry.getQuantity(),
                orderFormEntry.getUnitPrice());
    }

    public OrderFormEntry toOrderFormEntry() {
        return new OrderFormEntry(
                id.get(), orderFormId.get(),
                reference.get(),
                designation.get(),
                quantity.get(),
                unitPrice.get()
        );
    }

    public long getId() {
        return id.get();
    }

    public SimpleLongProperty idProperty() {
        return id;
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public long getOrderFormId() {
        return orderFormId.get();
    }

    public SimpleLongProperty orderFormIdProperty() {
        return orderFormId;
    }

    public void setOrderFormId(long orderFormId) {
        this.orderFormId.set(orderFormId);
    }

    public String getReference() {
        return reference.get();
    }

    public SimpleStringProperty referenceProperty() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference.set(reference);
    }

    public String getDesignation() {
        return designation.get();
    }

    public SimpleStringProperty designationProperty() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation.set(designation);
    }

    public int getQuantity() {
        return quantity.get();
    }

    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public BigDecimal getUnitPrice() {
        return unitPrice.get();
    }

    public SimpleObjectProperty<BigDecimal> unitPriceProperty() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice.set(unitPrice);
    }

    public BigDecimal getTotalPrice() {
        return totalPrice.get();
    }

    public SimpleObjectProperty<BigDecimal> totalPriceProperty() {
        return totalPrice;
    }
}
