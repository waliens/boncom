package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.data.OrderFormEntry;
import be.mormont.iacf.boncom.db.Callback;
import be.mormont.iacf.boncom.db.OrderFormEntryTable;
import be.mormont.iacf.boncom.util.StringUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Date: 24-12-17
 * By  : Mormont Romain
 */
public class SearchOrderFormEntryForm implements Initializable {
    @FXML private Label formTitle;
    @FXML private Label referenceFieldLabel;
    @FXML private TextField referenceField;
    @FXML private Label designationFieldLabel;
    @FXML private TextField designationField;
    @FXML private Label duplicateChkboxLabel;
    @FXML private CheckBox duplicateChkbox;
    @FXML private Label filteredListViewTitle;
    @FXML private Label caseSensitiveLabel;
    @FXML private CheckBox caseSensitiveChkbox;
    @FXML private ListView<OrderFormEntry> filteredListView;
    @FXML private Button addEntriesButton;
    @FXML private Button removeEntriesButton;
    @FXML private Label selectedListViewTitle;
    @FXML private ListView<OrderFormEntry> selectedListView;
    @FXML private Button addButton;
    @FXML private Button cancelButton;

    /**
     * Null if there are no provider, otherwise provider is used to filter the list of entries
     */
    private Entity provider = null;
    private OrderFormSearchExitHandler handler = null;

    /**
     * Should be sorted by decreasing order form entry ids so that duplicate are not counted several times
     */
    private ArrayList<OrderFormEntry> all;
    private ObservableList<OrderFormEntry> filtered;
    private ObservableList<OrderFormEntry> selected;
    private OrderFormEntryFilter filter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formTitle.setText("Recherche d'entrées");
        referenceFieldLabel.setText("Référence");
        designationFieldLabel.setText("Désignation");
        duplicateChkboxLabel.setText("Cacher duplicats");
        caseSensitiveLabel.setText("Sensible à la casse");
        filteredListViewTitle.setText("Filtré(s)");
        selectedListViewTitle.setText("Sélectionné(s)");
        filter = new OrderFormEntryFilter(referenceField, designationField, duplicateChkbox, caseSensitiveChkbox);

        // prepare data and list views
        initFiltered();
        selectedListView.setCellFactory(param -> new OrderFormEntryCell());
        selectedListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filteredListView.setCellFactory(param -> new OrderFormEntryCell());
        filteredListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // set refresh events
        referenceField.textProperty().addListener(e -> refreshFiltered());
        designationField.textProperty().addListener(e -> refreshFiltered());
        duplicateChkbox.selectedProperty().addListener(e -> refreshFiltered());
        caseSensitiveChkbox.selectedProperty().addListener(e -> refreshFiltered());

        // set exit buttons behavior
        cancelButton.setText("Annuler");
        cancelButton.setOnMouseClicked(e -> closeForm());
        addButton.setText("Ajouter");
        addButton.setOnMouseClicked(e -> {
            if (handler != null) {
                handler.handle(selected);
            }
            closeForm();
        });

        // set select of unselect buttons behavior
        addEntriesButton.setOnMouseClicked(e -> addItemsToSelected());
        removeEntriesButton.setOnMouseClicked(e -> selectedListView.getItems().removeAll(selectedListView.getSelectionModel().getSelectedItems()));
}

    public void setProvider(Entity provider) {
        this.provider = provider;
        initFiltered();
    }

    public void setHandler(OrderFormSearchExitHandler handler) {
        this.handler = handler;
    }

    private void closeForm() {
        FXMLModalHelper.closeModal(formTitle.getParent());
    }

    /**
     * Add items selected in the filtered list to the selected list (if they haven't been added yet)
     */
    private void addItemsToSelected() {
        ObservableList<OrderFormEntry> entries = filteredListView.getSelectionModel().getSelectedItems();

        // check which one were already added
        HashSet<Long> added = new HashSet<>();
        for (OrderFormEntry entry: selected) {
            added.add(entry.getId());
        }

        // filter already added
        selectedListView.getItems().addAll(entries.stream()
                    .filter(entry -> entry != null && !added.contains(entry.getId()))
                    .collect(Collectors.toCollection(ArrayList::new)));
    }
    /**
     * Initialize structures containing the content of the list views
     */
    private void initFiltered() {
        all = new ArrayList<>();
        filtered = FXCollections.observableArrayList();
        filteredListView.setItems(filtered);
        selected = FXCollections.observableArrayList();
        selectedListView.setItems(selected);
        new OrderFormEntryTable().getOrderFormEntriesByProvider(provider, new Callback<ArrayList<OrderFormEntry>>() {
            @Override
            public void success(ArrayList<OrderFormEntry> list) {
                list.sort((o1, o2) -> (int) (o2.getId() - o1.getId())); // order by decreasing id
                all.clear();
                all.addAll(list);
                refreshFiltered();
            }

            @Override
            public void failure(Exception e) {
                AlertHelper.popException(e);
            }
        });
    }

    private void refreshFiltered() {
        filteredListView.getItems().clear();
        filteredListView.getItems().setAll(filter.filter(all));
    }


    /**
     * by reference: loosely match the reference string
     * by designation: loosely match the designation string
     * hide duplicate: only keep the most recent item among the items having the same designation (by ids)
     * case sensitivity: control the effect of case for the reference and designation matches
     */
    private static class OrderFormEntryFilter {
        private SimpleStringProperty referenceFilter;
        private SimpleStringProperty designationFilter;
        private SimpleBooleanProperty hideDuplicate;
        private SimpleBooleanProperty caseSensitive;

        public OrderFormEntryFilter(TextField referenceField, TextField designationField, CheckBox duplicateBox, CheckBox caseSensitivityBox) {
            referenceFilter = new SimpleStringProperty();
            referenceFilter.bind(referenceField.textProperty());
            designationFilter = new SimpleStringProperty();
            designationFilter.bind(designationField.textProperty());
            hideDuplicate = new SimpleBooleanProperty();
            hideDuplicate.bind(duplicateBox.selectedProperty());
            caseSensitive = new SimpleBooleanProperty();
            caseSensitive.bind(caseSensitivityBox.selectedProperty());
        }

        /**
         * Filter the input list using the defined criterion
         * @param in Input list (should be sorted by decreasing order form entry id
         * @return A filtered list
         */
        ArrayList<OrderFormEntry> filter(ArrayList<OrderFormEntry> in) {
            ArrayList<OrderFormEntry> out = new ArrayList<>();
            HashSet<String> usedAlready = new HashSet<>(); // for duplicates

            // extract current filtering rules
            boolean hideDup = hideDuplicate.get(), caseSen = caseSensitive.get();
            Predicate<String> referencePred = getPatternFromQuery(referenceFilter.get(), caseSen).asPredicate();
            Predicate<String> designationPred = getPatternFromQuery(designationFilter.get(), caseSen).asPredicate();

            // filter
            for (OrderFormEntry entry: in) {
                String designation = entry.getDesignation();
                // check for duplicates (with case consideration) if needed
                if (hideDup) {
                    String key = caseSen ? designation : designation.toLowerCase();
                    if (usedAlready.contains(key)) {
                        continue;
                    } else {
                        usedAlready.add(key);
                    }
                }

                // check names
                if (referencePred.test(entry.getReference()) && designationPred.test(designation)) {
                    out.add(entry);
                }
            }

            return out;
        }

        /**
         * Build a pattern loosely matching the query.
         * Loosely matching means that the characters in the query do not have to be adjacent in the
         * filtered string to have a match. Case sensitivity is set or not according to the object configuration.
         * @param query The query string
         * @param caseSensitive True for the final regex to be case sensitive
         * @return A compiled pattern
         */
        private Pattern getPatternFromQuery(String query, boolean caseSensitive) {
            StringBuilder regex = new StringBuilder();
            final String WILDCARD = ".*";
            regex.append("^");
            for (int i = 0; i < query.length(); ++i) {
                regex.append(WILDCARD);
                regex.append(query.charAt(i));
            }
            regex.append(WILDCARD);
            regex.append("$");
            return Pattern.compile(regex.toString(), caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        }
    }

    /**
     * A listview cell displaying basing information
     */
    public class OrderFormEntryCell extends ListCell<OrderFormEntry> {
        @Override
        protected void updateItem(OrderFormEntry item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                setText("[" + item.getReference() + "] " + item.getDesignation() + " (" + StringUtil.formatCurrency(item.getUnitPrice()) + ")");
            } else {
                setText("");
                setGraphic(null);
            }
        }
    }

    /**
     * An interface for defining the action to perform with the list when the search windows is exited on success
     */
    interface OrderFormSearchExitHandler {
        void handle(List<OrderFormEntry> entries);
    }
}
