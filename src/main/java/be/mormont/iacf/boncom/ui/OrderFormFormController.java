package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;
import be.mormont.iacf.boncom.db.EntityTable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
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
    @FXML private Label purchaserFieldLabel;
    @FXML private ComboBox<Entity> purchaserField;

    private ObservableList<Entity> purchasersList;
    private OrderForm orderForm = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        numberFieldLabel.setText("Numéro");
        purchaserFieldLabel.setText("Acheteur");
        cancelButton.setText("Annuler");
        cancelButton.setOnMouseClicked(e -> closeForm());

        purchasersList = FXCollections.observableList(new ArrayList<>());
        purchaserField.setItems(purchasersList);
        purchaserField.setCellFactory(param -> new EntityListCell());
        purchaserField.setButtonCell(new EntityListCell());
        new EntityTable().getIacf(new be.mormont.iacf.boncom.db.Callback<Entity>() {
            @Override
            public void success(Entity object) {
                purchasersList.add(object);
                purchaserField.getSelectionModel().select(object);
            }

            @Override
            public void failure(Exception e) {
                AlertHelper.popAlert(
                    Alert.AlertType.ERROR,
                    "Erreur",
                    "Impossible de récupérer l'entité 'acheteur'",
                    "L'ajout a échoué à cause de : " + e.getMessage(),
                    true
                );
            }
        });


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
