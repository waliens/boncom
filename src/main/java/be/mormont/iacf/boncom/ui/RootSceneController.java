package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;
import be.mormont.iacf.boncom.data.OrderFormEntry;
import be.mormont.iacf.boncom.db.Callback;
import be.mormont.iacf.boncom.db.OrderFormTable;
import be.mormont.iacf.boncom.export.OrderFormXlsExporter;
import be.mormont.iacf.boncom.util.StringUtil;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by Romain on 03-07-17.
 * This is a class.
 */
public class RootSceneController implements Initializable {

    public static String FXML_BASE_PATH = "/be/mormont/iacf/boncom/ui/";
    private static String EDIT_PROVIDER_FXML = "provider_form.fxml";
    private static String EDIT_ORDER_FORM_FXML = "order_form_form.fxml";

    @FXML private Label titleLabel;
    @FXML private VBox createOrderFormBox;
    @FXML private VBox createProviderBox;
    @FXML private Label createOrderFormLabel;
    @FXML private Label createProviderLabel;

    // order form
    @FXML private Label orderFormsLabel;
    @FXML private Button orderFormEditButton;
    @FXML private Button orderFormExportButton;
    @FXML private TableView<OrderForm> orderFormsTable;
    @FXML private TableColumn<OrderForm, Long> orderFormNumberColumn;
    @FXML private TableColumn<OrderForm, LocalDate> orderFormDateColumn;
    @FXML private TableColumn<OrderForm, Integer> orderFormCountColumn;
    @FXML private TableColumn<OrderForm, String> orderFormTotalColumn;
    @FXML private TableColumn<OrderForm, String> orderFormProviderColumn;

    private ObservableList<OrderForm> orderForms;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleLabel.setText("Bon de commandes");
        createOrderFormLabel.setText("Nouveau bon de commande");
        createProviderLabel.setText("Nouveau fournisseur");

        createOrderFormBox.setOnMouseClicked(event -> {
            Pair<Parent, OrderFormFormController> nodeCtrl = FXMLModalHelper.popModal(FXML_BASE_PATH + EDIT_ORDER_FORM_FXML, titleLabel.getScene().getWindow());
            nodeCtrl.getValue().setOrderForm(null, null);
        });
        createProviderBox.setOnMouseClicked(event -> {
            Pair<Parent, ProviderFormController> nodeCtrl = FXMLModalHelper.popModal(FXML_BASE_PATH + EDIT_PROVIDER_FXML, titleLabel.getScene().getWindow());
            nodeCtrl.getValue().setEntity(null);
        });

        // order forms
        orderFormsLabel.setText("Historique");
        orderFormEditButton.setText("Mettre à jour");
        orderFormExportButton.setText("Exporter");

        // initialize button and add listener to enable them on selection
        setTableButtonsDisableProperty(false);
        orderFormsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                setTableButtonsDisableProperty(newValue != null)
        );

        orderFormEditButton.setOnMouseClicked(event -> {
            Pair<Parent, OrderFormFormController> nodeCtrl = FXMLModalHelper.popModal(FXML_BASE_PATH + EDIT_ORDER_FORM_FXML, titleLabel.getScene().getWindow());
            OrderForm selected = orderFormsTable.getSelectionModel().getSelectedItem();
            nodeCtrl.getValue().setOrderForm(selected, form -> {

            });
        });

        orderFormExportButton.setOnMouseClicked(event -> {
            OrderForm selected = orderFormsTable.getSelectionModel().getSelectedItem();
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(titleLabel.getScene().getWindow());
            try {
                if (file != null) {
                    new OrderFormXlsExporter().export(file.getAbsolutePath(), selected);
                }
            } catch (IOException e) {
                AlertHelper.popException(e);
            }
        });

        // Columns widths
        orderFormNumberColumn.prefWidthProperty().bind(orderFormsTable.widthProperty().divide(6));
        orderFormDateColumn.prefWidthProperty().bind(orderFormsTable.widthProperty().divide(6));
        orderFormCountColumn.prefWidthProperty().bind(orderFormsTable.widthProperty().divide(6));
        orderFormTotalColumn.prefWidthProperty().bind(orderFormsTable.widthProperty().divide(6));
        orderFormProviderColumn.prefWidthProperty().bind(orderFormsTable.widthProperty().divide(3));

        // display
        orderFormNumberColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getNumber()));
        orderFormDateColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getDate()));
        orderFormCountColumn.setCellValueFactory(params -> new SimpleObjectProperty<>(params.getValue().getEntries().size()));
        orderFormTotalColumn.setCellValueFactory(params -> new SimpleStringProperty(StringUtil.formatCurrency(params.getValue().getTotal())));
        orderFormProviderColumn.setCellValueFactory(params -> new SimpleStringProperty(params.getValue().getProvider().getName()));

        // columns
        orderFormNumberColumn.setText("Numéro");
        orderFormDateColumn.setText("Date");
        orderFormCountColumn.setText("Quantité");
        orderFormTotalColumn.setText("Total");
        orderFormProviderColumn.setText("Fournisseur");

        // data
        orderForms = FXCollections.observableArrayList();
        orderFormsTable.setItems(orderForms);
        //orderForms.addAll(DEBUG_getOrderForm());
        new OrderFormTable().getAllOrderForms(new Callback<ArrayList<OrderForm>>() {
            @Override
            public void success(ArrayList<OrderForm> object) {
                orderForms.addAll(object);
            }

            @Override
            public void failure(Exception e) {
                AlertHelper.popException(e);
            }
        });
    }

    private void setTableButtonsDisableProperty(boolean enable) {
        orderFormEditButton.setDisable(!enable);
        orderFormExportButton.setDisable(!enable);
    }

    private OrderForm DEBUG_getOrderForm() {
        ArrayList<OrderFormEntry> entries = new ArrayList<>();
        entries.add(new OrderFormEntry("ref1", "desig1", 3, new BigDecimal(25.35)));
        entries.add(new OrderFormEntry("ref2", "desig2", 2, new BigDecimal(5.10)));
        Entity entity = new Entity("NAME", new Address("street", "number", "box", "postcode", "city"), new String[] {"04/225"});
        return new OrderForm(25, entity, entity, LocalDate.now(), entries);
    }
}
