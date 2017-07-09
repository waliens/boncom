package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.OrderForm;
import be.mormont.iacf.boncom.data.OrderFormEntry;
import be.mormont.iacf.boncom.util.StringUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Date: 09-07-17
 * By  : Mormont Romain
 */
public class OrderFormEntryFormController implements Initializable {
    @FXML private Label formTitle;
    @FXML private Label referenceFieldLabel;
    @FXML private TextField referenceField;
    @FXML private Label designationFieldLabel;
    @FXML private TextField designationField;
    @FXML private Label quantityFieldLabel;
    @FXML private TextField quantityField;
    @FXML private Label unitPriceFieldLabel;
    @FXML private TextField unitPriceField;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;

    private OrderFormEntry orderFormEntry;
    private OrderFormEntryHandler handler = entry -> {};  // do nothing by default

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        referenceFieldLabel.setText("Référence");
        designationFieldLabel.setText("Désignation");
        quantityFieldLabel.setText("Quantité");
        unitPriceFieldLabel.setText("Prix unitaire");
        cancelButton.setText("Annuler");
        cancelButton.setOnMouseClicked(event -> closeForm());
        submitButton.setOnMouseClicked(event -> {
            OrderFormEntry entry = createOrderFormEntry();
            if (entry != null) {
                handler.handle(entry);
                closeForm();
            }
        });

        if (orderFormEntry == null) {
            formTitle.setText("Créer une nouvelle entrée");
            submitButton.setText("Créer");
            quantityField.setText(Integer.toString(1));

        } else {
            formTitle.setText("Mise à jour d'une entrée");
            submitButton.setText("Mettre à jour");
            submitButton.setOnMouseClicked(event -> System.out.println("Mettre à jour"));
            // pre-filling fields
            referenceField.setText(orderFormEntry.getReference());
            designationField.setText(orderFormEntry.getDesignation());
            quantityField.setText(orderFormEntry.getDesignation());
            unitPriceField.setText(orderFormEntry.getUnitPrice().toString());

        }
    }

    public synchronized void setSetOrderFormEntry(OrderFormEntry orderFormEntry) {
        this.orderFormEntry = orderFormEntry;
    }

    public synchronized void setOrderFormEntryHandler(OrderFormEntryHandler handler) {
        this.handler = handler;
    }

    private void closeForm() {
        FXMLModalHelper.closeModal(cancelButton.getParent());
    }

    /** A handler for enabling third-party components managing the entry generated in this form */
    public interface OrderFormEntryHandler {
        void handle(OrderFormEntry entry);
    }

    private OrderFormEntry createOrderFormEntry() {
        String reference = StringUtil.getNotEmptyOrNull(referenceField.getText());
        String designation = StringUtil.getNotEmptyOrNull(designationField.getText());
        if (designation == null) {
            AlertHelper.popEmptyField("désignation");
            return null;
        }
        String strQuantity = StringUtil.getNotEmptyOrNull(quantityField.getText());
        if (strQuantity == null) {
            AlertHelper.popEmptyField("quantité");
            return null;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(strQuantity);
        } catch (NumberFormatException e) {
            AlertHelper.popInvalidField("quantité", e);
            return null;
        }

        String strUnitPrice = StringUtil.getNotEmptyOrNull(unitPriceField.getText());
        if (strUnitPrice == null) {
            AlertHelper.popEmptyField("prix unitaire");
            return null;
        }
        BigDecimal unitPrice;
        try {
            unitPrice = new BigDecimal(strUnitPrice);
        } catch (NumberFormatException e) {
            AlertHelper.popInvalidField("prix unitaire", e);
            return null;
        }

        return new OrderFormEntry(reference, designation, quantity, unitPrice);
    }
}
