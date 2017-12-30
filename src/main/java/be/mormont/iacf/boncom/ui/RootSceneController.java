package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderForm;
import be.mormont.iacf.boncom.db.Callback;
import be.mormont.iacf.boncom.db.EntityTable;
import be.mormont.iacf.boncom.db.OrderFormTable;
import be.mormont.iacf.boncom.export.OrderFormXlsExporter;
import be.mormont.iacf.boncom.util.StringUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    private static String PROVIDER_PANEL_FXML = "provider_panel.fxml";
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
    @FXML private Button orderFormRefreshButton;
    @FXML private TableView<OrderForm> orderFormsTable;
    @FXML private TableColumn<OrderForm, Long> orderFormNumberColumn;
    @FXML private TableColumn<OrderForm, LocalDate> orderFormDateColumn;
    @FXML private TableColumn<OrderForm, Integer> orderFormCountColumn;
    @FXML private TableColumn<OrderForm, BigDecimal> orderFormTotalColumn;
    @FXML private TableColumn<OrderForm, String> orderFormProviderColumn;
    @FXML private Label providerFilterLabel;
    @FXML private ComboBox<Entity> providerFilterComboBox;
    @FXML private Button resetFilterButton;

    private ObservableList<Entity> providers;
    private ObservableList<OrderForm> orderForms;
    private FilteredList<OrderForm> filteredOrderForms;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleLabel.setText("Bon de commandes");
        createOrderFormLabel.setText("Nouveau bon de commande");
        createProviderLabel.setText("Gestion des fournisseurs");

        createOrderFormBox.setOnMouseClicked(event -> {
            Pair<Parent, OrderFormFormController> nodeCtrl = FXMLModalHelper.popModal(FXML_BASE_PATH + EDIT_ORDER_FORM_FXML, titleLabel.getScene().getWindow());
            nodeCtrl.getValue().setOrderForm(null);
            nodeCtrl.getValue().setHandler(form -> refreshHistory());
        });
        createProviderBox.setOnMouseClicked(event -> {
            Pair<Parent, ProviderPanelController> nodeCtrl = FXMLModalHelper.popModal(FXML_BASE_PATH + PROVIDER_PANEL_FXML, titleLabel.getScene().getWindow());
            nodeCtrl.getKey().getScene().getWindow().setOnCloseRequest(e -> updateProviders());
        });

        // order forms
        orderFormsLabel.setText("Historique");
        orderFormEditButton.setText("Mettre à jour");
        orderFormExportButton.setText("Exporter");
        orderFormRefreshButton.setText("Rafraîchir");
        resetFilterButton.setText("Reset");

        // initialize button and add listener to enable them on selection
        setTableButtonsDisableProperty(false);
        orderFormsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                setTableButtonsDisableProperty(newValue != null)
        );

        orderFormEditButton.setOnMouseClicked(event -> {
            Pair<Parent, OrderFormFormController> nodeCtrl = FXMLModalHelper.popModal(FXML_BASE_PATH + EDIT_ORDER_FORM_FXML, titleLabel.getScene().getWindow());
            OrderForm selected = orderFormsTable.getSelectionModel().getSelectedItem();
            nodeCtrl.getValue().setOrderForm(selected);
            nodeCtrl.getValue().setHandler(form -> refreshHistory());
        });

        orderFormRefreshButton.setOnMouseClicked(event -> refreshHistory());

        orderFormExportButton.setOnMouseClicked(event -> {
            OrderForm selected = orderFormsTable.getSelectionModel().getSelectedItem();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("bon_commande_" + selected.getId() + ".xls");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", ".xls"));
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
        orderFormTotalColumn.setCellValueFactory(params -> new SimpleObjectProperty<>(params.getValue().getTotal()));
        orderFormProviderColumn.setCellValueFactory(params -> new SimpleStringProperty(params.getValue().getProvider().getName()));
        orderFormTotalColumn.setCellFactory(param -> new TotalCellFactory());

        // columns
        orderFormNumberColumn.setText("Numéro");
        orderFormDateColumn.setText("Date");
        orderFormCountColumn.setText("Quantité");
        orderFormTotalColumn.setText("Total");
        orderFormProviderColumn.setText("Fournisseur");

        // sort
        orderFormDateColumn.setComparator(LocalDate::compareTo);

        // data
        orderForms = FXCollections.observableArrayList();
        filteredOrderForms = new FilteredList<>(orderForms);
        orderFormsTable.setItems(filteredOrderForms);
        refreshHistory();

        // provider filtering
        providers = FXCollections.observableArrayList();
        providerFilterLabel.setText("Fournisseurs:");
        providerFilterComboBox.setItems(providers);
        providerFilterComboBox.setCellFactory(param -> new EntityListCell());
        providerFilterComboBox.setButtonCell(new EntityListCell());
        providerFilterComboBox.valueProperty().addListener(items -> {
            Entity entity = providerFilterComboBox.getSelectionModel().getSelectedItem();
            if(entity == null) {
                filteredOrderForms.setPredicate(s -> true);
            } else {
                filteredOrderForms.setPredicate(s -> s.getProvider().getId() == entity.getId());
            }
        });

        resetFilterButton.setOnMouseClicked(event -> providerFilterComboBox.getSelectionModel().clearSelection());

        updateProviders();
    }

    private void updateProviders() {
        new EntityTable().getAllEntities(entities -> providers.setAll(entities));
    }

    private void setTableButtonsDisableProperty(boolean enable) {
        orderFormEditButton.setDisable(!enable);
        orderFormExportButton.setDisable(!enable);
    }

    private void refreshHistory() {
        new OrderFormTable().getAllOrderForms(new Callback<ArrayList<OrderForm>>() {
            @Override
            public void success(ArrayList<OrderForm> object) {
                orderForms.setAll(object);
                orderFormsTable.getSortOrder().clear();
                orderFormsTable.getSortOrder().add(orderFormDateColumn);
                orderFormsTable.getSortOrder().add(orderFormNumberColumn);
                orderFormNumberColumn.setSortType(TableColumn.SortType.ASCENDING);
                orderFormDateColumn.setSortType(TableColumn.SortType.DESCENDING);
                orderFormDateColumn.setSortable(true);
            }

            @Override
            public void failure(Exception e) {
                AlertHelper.popException(e);
            }
        });
    }

    private static class TotalCellFactory extends TableCell<OrderForm, BigDecimal> {
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                setText(StringUtil.formatCurrency(item));
            } else {
                setGraphic(null);
            }
        }
    }
}
