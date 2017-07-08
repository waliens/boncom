package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.OrderForm;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;


/**
 * Date: 08-07-17
 * By  : Mormont Romain
 */
public class OrderFormFormController implements Initializable {
    @FXML private Label formTitle;
    @FXML private Label numberFieldLabel;
    @FXML private TextField numberField;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;

    private OrderForm orderForm = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        numberFieldLabel.setText("Numéro");
        cancelButton.setText("Annuler");
        cancelButton.setOnMouseClicked(e -> closeForm());

        if (orderForm != null) { // new order form
            formTitle.setText("Mise à jour d'un bon de commande (" + orderForm.getNumber() + ")");
            numberField.setText(Long.toString(orderForm.getNumber()));
            submitButton.setText("Update");
        } else {
            formTitle.setText("Créer un nouveau bon de commande");
            submitButton.setText("Créer");
        }
    }

    private void closeForm() {
        FXMLModalHelper.closeModal(cancelButton.getParent());
    }

    public synchronized void setOrderForm(OrderForm orderForm) {
        this.orderForm = orderForm;
    }
}
