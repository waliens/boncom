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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @FXML private Button orderFormDeleteButton;
    @FXML private Button orderFormRefreshButton;
    @FXML private TableView<OrderForm> orderFormsTable;
    @FXML private TableColumn<OrderForm, Long> orderFormNumberColumn;
    @FXML private TableColumn<OrderForm, LocalDate> orderFormDateColumn;
    @FXML private TableColumn<OrderForm, Integer> orderFormCountColumn;
    @FXML private TableColumn<OrderForm, BigDecimal> orderFormTotalColumn;
    @FXML private TableColumn<OrderForm, String> orderFormProviderColumn;
    @FXML private Label providerFilterLabel;
    @FXML private ComboBox<Entity> providerFilterComboBox;
    @FXML public Label yearFilterLabel;
    @FXML public ComboBox<Integer> yearFilterComboBox;
    @FXML private Button resetFilterButton;

    private ObservableList<Entity> providers;
    private ObservableList<Integer> years;
    private ObservableList<OrderForm> orderForms;
    private FilteredList<OrderForm> providerFilteredOrderForms;
    private FilteredList<OrderForm> yearFilteredOrderForms;

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
        orderFormDeleteButton.setText("Supprimer");
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

        orderFormDeleteButton.setOnMouseClicked(event -> {
            OrderForm selected = orderFormsTable.getSelectionModel().getSelectedItem();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Suppression de bon de commande");
            alert.setHeaderText("Confirmer la suppression ?");
            alert.setContentText("Suppression du bon de commande n°" + selected.getNumber() + ", fournisseur: " + selected.getProvider().getName());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK){
                new OrderFormTable().deleteOrderForm(selected, new Callback<OrderForm>() {
                    @Override
                    public void success(OrderForm object) {
                        refreshHistory();
                    }

                    @Override
                    public void failure(Exception e) {
                        AlertHelper.popException(e);
                    }
                });
            }
        });

        orderFormRefreshButton.setOnMouseClicked(event -> refreshHistory());

        orderFormExportButton.setOnMouseClicked(event -> {
            OrderForm selected = orderFormsTable.getSelectionModel().getSelectedItem();
            FileChooser fileChooser = new FileChooser();
            String providerName = selected.getProvider().getName().toLowerCase().replaceAll("[^a-z0-9]+", "");
            fileChooser.setInitialFileName(providerName + "_bon_commande_" + selected.getId() + ".xls");
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
        providerFilteredOrderForms = new FilteredList<>(orderForms);
        yearFilteredOrderForms = new FilteredList<>(providerFilteredOrderForms);
        SortedList sortedOrderForms = new SortedList<>(yearFilteredOrderForms);
        sortedOrderForms.comparatorProperty().bind(orderFormsTable.comparatorProperty());
        orderFormsTable.setItems(sortedOrderForms);

        // provider filtering
        providers = FXCollections.observableArrayList();
        SortedList<Entity> sortedProviders = new SortedList<>(providers);
        sortedProviders.setComparator(Comparator.comparing(a -> a.getName().toLowerCase()));
        providerFilterLabel.setText("Fournisseurs:");
        providerFilterComboBox.setItems(sortedProviders);
        providerFilterComboBox.setCellFactory(param -> new EntityListCell());
        providerFilterComboBox.setButtonCell(new EntityListCell());
        providerFilterComboBox.valueProperty().addListener(items -> {
            Entity entity = providerFilterComboBox.getSelectionModel().getSelectedItem();
            if(entity == null) {
                providerFilteredOrderForms.setPredicate(s -> true);
            } else {
                providerFilteredOrderForms.setPredicate(s -> s.getProvider().getId() == entity.getId());
            }
        });

        // year filtering
        years = FXCollections.observableArrayList();
        yearFilterLabel.setText("Année:");
        yearFilterComboBox.setItems(years);
        orderForms.addListener((ListChangeListener<OrderForm>) c -> {
            final ObservableList<? extends OrderForm> orderForms = c.getList();
            List<Integer> yearsToSet = orderForms.stream()
                .map(orderform -> orderform.getDate().getYear())
                .distinct().sorted()
                .collect(Collectors.toList());
            yearFilterComboBox.getSelectionModel().clearSelection();
            years.setAll(yearsToSet);
        });
        yearFilterComboBox.valueProperty().addListener(items -> {
            Integer year = yearFilterComboBox.getSelectionModel().getSelectedItem();
            if(year == null) {
                yearFilteredOrderForms.setPredicate(s -> true);
            } else {
                yearFilteredOrderForms.setPredicate(s -> s.getDate().getYear() == year);
            }
        });


        resetFilterButton.setOnMouseClicked(event -> {
            providerFilterComboBox.getSelectionModel().clearSelection();
            yearFilterComboBox.getSelectionModel().clearSelection();
        });

        refreshHistory();
        updateProviders();
    }

    private void updateProviders() {
        new EntityTable().getAllEntities(entities -> providers.setAll(entities));
    }

    private void setTableButtonsDisableProperty(boolean enable) {
        orderFormEditButton.setDisable(!enable);
        orderFormExportButton.setDisable(!enable);
        orderFormDeleteButton.setDisable(!enable);
    }

    private void refreshHistory() {
        new OrderFormTable().getAllOrderForms(new Callback<ArrayList<OrderForm>>() {
            @Override
            public void success(ArrayList<OrderForm> object) {
                orderForms.setAll(object);
                orderFormsTable.getSortOrder().clear();
                orderFormsTable.getSortOrder().add(orderFormDateColumn);
                orderFormsTable.getSortOrder().add(orderFormNumberColumn);
                orderFormNumberColumn.setSortType(TableColumn.SortType.DESCENDING);
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
                setText("");
                setGraphic(null);
            }
        }
    }
}
