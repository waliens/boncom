package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;
import be.mormont.iacf.boncom.db.EntityTable;
import be.mormont.iacf.boncom.db.OrderFormTable;
import be.mormont.iacf.boncom.db.UICallback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;


/**
 * Date: 08-07-17
 * By  : Mormont Romain
 */
public class OrderFormFormController implements Initializable {
    @FXML private Label formTitle;
    @FXML private Label numberFieldLabel;
    @FXML private Label numberFieldMessageLabel;
    @FXML private TextField numberField;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;
    @FXML private Label purchaserFieldLabel;
    @FXML private ComboBox<Entity> purchaserField;
    @FXML private Label providerFieldLabel;
    @FXML private ComboBox<Entity> providerField;
    @FXML private Label dateFieldLabel;
    @FXML private DatePicker dateField;

    private ObservableList<Entity> purchasersList;
    private ObservableList<Entity> providersList;
    private OrderForm orderForm = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        numberFieldLabel.setText("Numéro");
        numberFieldMessageLabel.setText("Si vide, déterminé automatiquement");
        purchaserFieldLabel.setText("Acheteur");
        providerFieldLabel.setText("Fournisseur");
        dateFieldLabel.setText("Date");
        cancelButton.setText("Annuler");
        cancelButton.setOnMouseClicked(e -> closeForm());

        purchasersList = FXCollections.observableList(new ArrayList<>());
        purchaserField.setItems(purchasersList);
        purchaserField.setCellFactory(param -> new EntityListCell());
        purchaserField.setButtonCell(new EntityListCell());

        providersList = FXCollections.observableList(new ArrayList<>());
        providerField.setItems(providersList);
        providerField.setCellFactory(param -> new EntityListCell());
        providerField.setButtonCell(new EntityListCell());

        EntityTable entityTable = new EntityTable();
        entityTable.getIacf(new be.mormont.iacf.boncom.db.Callback<Entity>() {
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
                    "Impossible de récupérer les entités 'acheteur'",
                    "L'ajout a échoué à cause de : " + e.getMessage(),
                    true
                );
            }
        });

        entityTable.getAllEntities(new be.mormont.iacf.boncom.db.Callback<ArrayList<Entity>>() {
            @Override
            public void success(ArrayList<Entity> object) {
                providersList.addAll(object);
                if (orderForm != null) {
                    purchaserField.getSelectionModel().select(orderForm.getProvider());
                }
            }

            @Override
            public void failure(Exception e) {
                AlertHelper.popAlert(
                    Alert.AlertType.ERROR,
                    "Erreur",
                    "Impossible de récupérer les entités 'fournisseur'",
                    "L'ajout a échoué à cause de : " + e.getMessage(),
                    true
                );
            }
        });

        if (orderForm != null) { // new order form
            formTitle.setText("Mise à jour d'un bon de commande (" + orderForm.getNumber() + ")");
            numberField.setText(Long.toString(orderForm.getNumber()));
            dateField.setValue(orderForm.getDate());
            submitButton.setText("Update");
            submitButton.setOnMouseClicked(e -> System.out.println("Update..."));
        } else {
            formTitle.setText("Créer un nouveau bon de commande");
            submitButton.setText("Créer");
            submitButton.setOnMouseClicked(e -> {
                OrderForm orderForm = getOrderForm();
                if (orderForm != null) {
                    new OrderFormTable().addOrderForm(orderForm, new UICallback<OrderForm>() {
                        @Override
                        public void success(OrderForm object) {
                            closeForm();
                        }

                        @Override
                        public void failure(Exception e) {
                            AlertHelper.popAlert(
                                    Alert.AlertType.ERROR,
                                    "Erreur",
                                    "Impossible de sauvegarder le bon de commande.",
                                    "L'ajout a échoué à cause de : " + e.getMessage(),
                                    true
                            );
                        }
                    });
                }
            });
        }
    }

    private void closeForm() {
        FXMLModalHelper.closeModal(cancelButton.getParent());
    }

    public synchronized void setOrderForm(OrderForm orderForm) {
        this.orderForm = orderForm;
    }

    private OrderForm getOrderForm() {
        try {
            String numberString = numberField.getText();
            long number = numberString.isEmpty() ? OrderForm.UNDEFINED_NUMBER : Long.parseLong(numberString);
            LocalDate date = dateField.getValue();
            Entity provider = providerField.getValue(),
                purchaser = purchaserField.getValue();

            if (provider == null || purchaser == null) {
                AlertHelper.popAlert(
                    Alert.AlertType.ERROR,
                    "Erreur",
                    "Champ invalide",
                    "Il faut choisir un fournisseur ET un acheteur.",
                    true
                );
                return null;
            }

            return new OrderForm(number, purchaser, provider, date, new ArrayList<>());
        } catch (NumberFormatException e) {
            AlertHelper.popAlert(
                Alert.AlertType.ERROR,
                "Erreur",
                "Champ invalide",
                "Le nombre passé '" + numberField.getText() + "' est invalide.",
                true
            );
            return null;
        }
    }
}
