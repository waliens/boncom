package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;
import be.mormont.iacf.boncom.data.OrderFormEntry;
import be.mormont.iacf.boncom.db.Callback;
import be.mormont.iacf.boncom.db.EntityTable;
import be.mormont.iacf.boncom.db.OrderFormTable;
import be.mormont.iacf.boncom.db.UICallback;
import be.mormont.iacf.boncom.util.StringUtil;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    @FXML private Button addEntryButton;
    @FXML private TableView<OrderFormEntry> entriesTable;
    @FXML private TableColumn<OrderFormEntry, String> entriesTabColumnReference;
    @FXML private TableColumn<OrderFormEntry, String> entriesTabColumnDesignation;
    @FXML private TableColumn<OrderFormEntry, Integer> entriesTabColumnQuantity;
    @FXML private TableColumn<OrderFormEntry, BigDecimal> entriesTabColumnUnitPrice;
    @FXML private TableColumn<OrderFormEntry, String> entriesTabColumnTotal;
    @FXML private Label totalFieldLabel;
    @FXML private Label totalField;

    private ObservableList<OrderFormEntry> entries;
    private ObservableList<Entity> purchasersList;
    private ObservableList<Entity> providersList;
    private OrderForm orderForm = null;
    private OrderFormHandler handler = orderForm -> {};

    private void refresh() {
        numberFieldLabel.setText("Numéro");
        numberFieldMessageLabel.setText("Si vide, déterminé automatiquement");
        purchaserFieldLabel.setText("Acheteur");
        providerFieldLabel.setText("Fournisseur");
        dateFieldLabel.setText("Date");
        entriesTableLabel.setText("Entrées");
        totalFieldLabel.setText("Total :");
        setTotal(new BigDecimal(0));
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
        entries.addListener((ListChangeListener<OrderFormEntry>) c -> updateTotal());
        entriesTable.setItems(entries);
        entriesTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        entriesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setTableButtonsDisableProperty(newValue != null);
        });

        // buttons
        addEntryButton.setText("Ajouter");
        deleteEntryButton.setText("Supprimer");
        setTableButtonsDisableProperty(false);

        addEntryButton.setOnMouseClicked(event -> {
            entries.add(getEmptyOrderFormEntry());
        });

        deleteEntryButton.setOnMouseClicked(event -> entries.remove(entriesTable.getSelectionModel().getSelectedIndex()));

        // make cells editable
        entriesTable.setEditable(true);
        entriesTable.getSelectionModel().cellSelectionEnabledProperty().set(true);
        entriesTabColumnReference.setCellFactory(col -> new StringEditingCell());
        entriesTabColumnDesignation.setCellFactory(col -> new StringEditingCell());
        entriesTabColumnQuantity.setCellFactory(col -> new IntegerEditingCell());
        entriesTabColumnUnitPrice.setCellFactory(col -> new BigDecimalEditingCell());
        entriesTabColumnTotal.setEditable(false);

        // value commit
        entriesTabColumnReference.setOnEditCommit(event -> getSelectedItem().setReference(event.getNewValue()));
        entriesTabColumnDesignation.setOnEditCommit(event -> getSelectedItem().setDesignation(event.getNewValue()));
        entriesTabColumnQuantity.setOnEditCommit(event -> {
            getSelectedItem().setQuantity(event.getNewValue());
            refreshAllTotals();
        });
        entriesTabColumnUnitPrice.setOnEditCommit(event -> {
            getSelectedItem().setUnitPrice(event.getNewValue());
            refreshAllTotals();
        });

        // names
        entriesTabColumnReference.setText("Réf.");
        entriesTabColumnDesignation.setText("Désignation");
        entriesTabColumnQuantity.setText("Quant.");
        entriesTabColumnUnitPrice.setText("Prix unit.");
        entriesTabColumnTotal.setText("Total");

        // display
        entriesTabColumnReference.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getReference()));
        entriesTabColumnDesignation.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDesignation()));
        entriesTabColumnQuantity.setCellValueFactory(params -> new ReadOnlyObjectWrapper<>(params.getValue().getQuantity()));
        entriesTabColumnUnitPrice.setCellValueFactory(params -> new ReadOnlyObjectWrapper<>(params.getValue().getUnitPrice()));
        entriesTabColumnTotal.setCellValueFactory(params -> new SimpleStringProperty(StringUtil.formatCurrency(params.getValue().getTotal())));

        /* Columns widths
         *  Ref  : 1/9
         *  Desig: 5/9
         *  Qty  : 1/9
         *  Unit : 1/9
         *  Total: 1/9
         */
        entriesTabColumnReference.prefWidthProperty().bind(entriesTable.widthProperty().divide(9));
        entriesTabColumnDesignation.prefWidthProperty().bind(entriesTable.widthProperty().divide(9).multiply(5));
        entriesTabColumnQuantity.prefWidthProperty().bind(entriesTable.widthProperty().divide(9));
        entriesTabColumnUnitPrice.prefWidthProperty().bind(entriesTable.widthProperty().divide(9));
        entriesTabColumnTotal.prefWidthProperty().bind(entriesTable.widthProperty().divide(9));


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
                    providerField.getSelectionModel().select(orderForm.getProvider());
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
            entries.addAll(orderForm.getEntries());
            submitButton.setText("Mettre à jour");
            submitButton.setOnMouseClicked(e -> {
                OrderForm orderForm = getOrderForm();
                if (orderForm != null) {
                    new OrderFormTable().updateOrderForm(orderForm, new Callback<OrderForm>() {
                        @Override
                        public void success(OrderForm object) {
                            handler.handle(orderForm);
                            closeForm();
                        }

                        @Override
                        public void failure(Exception e) {
                            AlertHelper.popException(e);
                        }
                    });
                }
            });
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
                            handler.handle(orderForm);
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

    public synchronized void setHandler(OrderFormHandler handler) {
        this.handler = handler;
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

        OrderForm newOrderForm = new OrderForm(number, purchaser, provider, date, addedEntries);

        // add ids if update
        if (orderForm != null) {
            newOrderForm.setId(orderForm.getId());
            for (OrderFormEntry entry: entries) {
                entry.setOrderFormId(orderForm.getId());
            }
        }

        return newOrderForm;
    }

    /**
     * Disable or enable the update button of the order form entries table
     * @param v True for enabling false for disabling
     */
    private void setTableButtonsDisableProperty(boolean v) {
        deleteEntryButton.setDisable(!v);
    }

    private Pair<Parent, OrderFormEntryFormController> popEditEntryForm() {
        return FXMLModalHelper.popModal(RootSceneController.FXML_BASE_PATH + EDIT_ENTRY_FORM_FXML, formTitle.getScene().getWindow());
    }


    /**
     * Update the content of the total field with content of entries
     */
    private void updateTotal() {
        BigDecimal total = new BigDecimal(0);
        for (OrderFormEntry entry : entries) {
            total = total.add(entry.getTotal());
        }
        setTotal(total);
    }

    /**
     * Set the formatted total field
     * @param total The total amount of money to display
     */
    private void setTotal(BigDecimal total) {
        totalField.setText(StringUtil.formatCurrency(total));
    }

    /**
     * Refresh all the totals (column + field)
     */
    private void refreshAllTotals() {
        entriesTable.refresh();
        updateTotal();
    }

    /**
     * @return Element that currently has focus
     */
    private OrderFormEntry getSelectedItem() {
        return entriesTable.getSelectionModel().getSelectedItem();
    }

    /**
     * @return An order form entry with pre-filled values to be changed
     */
    private static OrderFormEntry getEmptyOrderFormEntry() {
        return new OrderFormEntry("XX", "XX", 0, new BigDecimal(0));
    }

    // callback called when object is update when
    public interface OrderFormHandler {
        void handle(OrderForm form);
    }

    /** Editing cell
     * - commit on focus loss
     * @param <T> Type of the stored element
     */
    abstract class EditingCell<T> extends TableCell<OrderFormEntry, T> {

        private TextField field = null;

        public EditingCell() { }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(field);
                field.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getString());
            setGraphic(null);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (field != null) {
                        field.setText(getString());
                    }
                    setText(null);
                    setGraphic(field);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        /**
         * Create the text field to edit the table
         */
        private void createTextField() {
            field = new TextField(getString());
            field.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            field.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    commit();
                }
            });
            EventHandler<? super KeyEvent> enterKeyHandler = event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    commit();
                    event.consume();
                }
            };
            field.setOnKeyPressed(enterKeyHandler);
            field.setOnKeyReleased(enterKeyHandler);
        }

        /**
         * @return stored value as a string
         */
        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }

        /**
         * Commit the current field value
         */
        private void commit() {
            commitEdit(fromString(field.getText()));
        }

        abstract T fromString(String v);
    }

    class BigDecimalEditingCell extends EditingCell<BigDecimal> {
        @Override
        BigDecimal fromString(String v) {
            return new BigDecimal(v);
        }
    }

    class StringEditingCell extends EditingCell<String> {
        @Override
        String fromString(String v) {
            return v;
        }
    }

    class IntegerEditingCell extends EditingCell<Integer> {
        @Override
        Integer fromString(String v) {
            return Integer.parseInt(v);
        }
    }
}
