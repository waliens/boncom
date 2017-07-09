package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;
import be.mormont.iacf.boncom.data.OrderFormEntry;
import be.mormont.iacf.boncom.db.EntityTable;
import be.mormont.iacf.boncom.db.OrderFormTable;
import be.mormont.iacf.boncom.db.UICallback;
import be.mormont.iacf.boncom.util.StringUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;


/**
 * Date: 08-07-17
 * By  : Mormont Romain
 */
public class OrderFormFormController implements Initializable {
    private static String EDIT_ENTRY_FORM_FXML = "order_form_entry_form.fxml";
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
    @FXML private Label entriesTableLabel;
    @FXML private Button deleteEntryButton;
    @FXML private Button editEntryButton;
    @FXML private Button createEntryButton;
    @FXML private TableView<OrderFormEntry> entriesTable;
    @FXML private TableColumn<OrderFormEntry, String> entriesTabColumnReference;
    @FXML private TableColumn<OrderFormEntry, String> entriesTabColumnDesignation;
    @FXML private TableColumn<OrderFormEntry, Integer> entriesTabColumnQuantity;
    @FXML private TableColumn<OrderFormEntry, BigDecimal> entriesTabColumnUnitPrice;

    private ObservableList<OrderFormEntry> entries;
    private ObservableList<Entity> purchasersList;
    private ObservableList<Entity> providersList;
    private OrderForm orderForm = null;

    private void refresh() {
        numberFieldLabel.setText("Numéro");
        numberFieldMessageLabel.setText("Si vide, déterminé automatiquement");
        purchaserFieldLabel.setText("Acheteur");
        providerFieldLabel.setText("Fournisseur");
        dateFieldLabel.setText("Date");
        entriesTableLabel.setText("Entrées");
        cancelButton.setText("Annuler");
        cancelButton.setOnMouseClicked(e -> closeForm());

        purchasersList = FXCollections.observableArrayList();
        purchaserField.setItems(purchasersList);
        purchaserField.setCellFactory(param -> new EntityListCell());
        purchaserField.setButtonCell(new EntityListCell());

        providersList = FXCollections.observableArrayList();
        providerField.setItems(providersList);
        providerField.setCellFactory(param -> new EntityListCell());
        providerField.setButtonCell(new EntityListCell());

        /* Entries table */
        entries = FXCollections.observableArrayList();
        entriesTable.setItems(entries);
        entriesTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        entriesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setTableButtonsDisableProperty(newValue != null);
        });

        // buttons
        editEntryButton.setText("Modifier");
        createEntryButton.setText("Ajouter");
        deleteEntryButton.setText("Supprimer");
        setTableButtonsDisableProperty(false);

        editEntryButton.setOnMouseClicked(event -> {
            Pair<Parent, OrderFormEntryFormController> nodeCtrl = popEditEntryForm();
            nodeCtrl.getValue().setOrderFormEntry(entriesTable.getSelectionModel().getSelectedItem());
            nodeCtrl.getValue().setOrderFormEntryHandler(entry -> {
                // Assumes that the selection cannot change when the user is in the order form entry form !!
                int index = entriesTable.getSelectionModel().getSelectedIndex();
                entries.remove(index);
                entries.add(index, entry);
            });
        });

        createEntryButton.setOnMouseClicked(event -> {
            Pair<Parent, OrderFormEntryFormController> nodeCtrl = popEditEntryForm();
            nodeCtrl.getValue().setOrderFormEntry(null);
            nodeCtrl.getValue().setOrderFormEntryHandler(entry -> entries.add(entry));
        });

        deleteEntryButton.setOnMouseClicked(event -> {
            entries.remove(entriesTable.getSelectionModel().getSelectedIndex());
        });

        // names
        entriesTabColumnReference.setText("Réf.");
        entriesTabColumnDesignation.setText("Désignation");
        entriesTabColumnQuantity.setText("Quant.");
        entriesTabColumnUnitPrice.setText("Prix unit.");

        // display
        entriesTabColumnReference.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getReference()));
        entriesTabColumnDesignation.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDesignation()));
        entriesTabColumnQuantity.setCellValueFactory(params -> new ReadOnlyObjectWrapper<>(params.getValue().getQuantity()));
        entriesTabColumnUnitPrice.setCellValueFactory(params -> new ReadOnlyObjectWrapper<>(params.getValue().getUnitPrice()));

        /* Columns widths
         *  Ref  : 1/8
         *  Desig: 5/8
         *  Qty  : 1/8
         *  Unit : 1/8
         */
        entriesTabColumnReference.prefWidthProperty().bind(entriesTable.widthProperty().divide(8));
        entriesTabColumnDesignation.prefWidthProperty().bind(entriesTable.widthProperty().divide(8).multiply(5));
        entriesTabColumnQuantity.prefWidthProperty().bind(entriesTable.widthProperty().divide(8));
        entriesTabColumnUnitPrice.prefWidthProperty().bind(entriesTable.widthProperty().divide(8));



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
            dateField.setValue(LocalDate.now());
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refresh();
    }

    private void closeForm() {
        FXMLModalHelper.closeModal(cancelButton.getParent());
    }

    public synchronized void setOrderForm(OrderForm orderForm) {
        this.orderForm = orderForm;
        refresh();

    }

    private OrderForm getOrderForm() {
        String strNumber = StringUtil.getNotEmptyOrNull(numberField.getText());
        long number;
        try {
            number = strNumber == null ? OrderForm.UNDEFINED_NUMBER : Long.parseLong(strNumber);
        } catch (NumberFormatException e) {
            AlertHelper.popInvalidField("numéro", e);
            return null;
        }

        LocalDate date = dateField.getValue();
        Entity provider = providerField.getValue(), purchaser = purchaserField.getValue();
        if (provider == null) {
            AlertHelper.popEmptyField("fournisseur");
            return null;
        }
        if (purchaser == null) {
            AlertHelper.popEmptyField("acheteur");
            return null;
        }

        ArrayList<OrderFormEntry> addedEntries = new ArrayList<>(entries);
        if (addedEntries.size() < 1) {
            AlertHelper.popEmptyField("entrées");
            return null;
        }

        return new OrderForm(number, purchaser, provider, date, addedEntries);
    }

    /**
     * Disable or enable the update button of the order form entries table
     * @param v True for enabling false for disabling
     */
    private void setTableButtonsDisableProperty(boolean v) {
        editEntryButton.setDisable(!v);
        deleteEntryButton.setDisable(!v);
    }

    private Pair<Parent, OrderFormEntryFormController> popEditEntryForm() {
        return FXMLModalHelper.popModal(RootSceneController.FXML_BASE_PATH + EDIT_ENTRY_FORM_FXML, formTitle.getScene().getWindow());
    }
}
