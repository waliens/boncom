package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.db.Callback;
import be.mormont.iacf.boncom.db.EntityTable;
import be.mormont.iacf.boncom.ui.util.StringListCell;
import be.mormont.iacf.boncom.util.StringUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Date: 30-12-17
 * By  : Mormont Romain
 */
public class ProviderPanelController implements Initializable {
    private static String EDIT_PROVIDER_FXML = "provider_form.fxml";

    @FXML private Label titleLabel;
    @FXML private Label searchFieldLabel;
    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private ListView<Entity> providersListView;

    private ObservableList<Entity> providers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleLabel.setText("Gestion des fournisseurs");
        searchFieldLabel.setText("Recherche");

        // button
        addButton.setText("Nouveau");
        updateButton.setText("Mettre à jour");
        deleteButton.setText("Supprimer");

        addButton.setOnMouseClicked(event -> {
            Pair<Parent, ProviderFormController> nodeCtrl = FXMLModalHelper.popModal(getFXMLFormAbsolutePath(), titleLabel.getScene().getWindow());
            nodeCtrl.getValue().setEntity(null);
            nodeCtrl.getKey().getScene().getWindow().setOnCloseRequest(e -> refreshProviders());
        });

        updateButton.setOnMouseClicked(event -> {
            if (providersListView.getSelectionModel().isEmpty()) {
                AlertHelper.popInvalidField("fournisseur","le fournisseur à mettre à jour n'est pas sélectionné.");
                return;
            }
            Pair<Parent, ProviderFormController> nodeCtrl = FXMLModalHelper.popModal(getFXMLFormAbsolutePath(), titleLabel.getScene().getWindow());
            nodeCtrl.getValue().setEntity(providersListView.getSelectionModel().getSelectedItem());
            nodeCtrl.getKey().getScene().getWindow().setOnCloseRequest(e -> refreshProviders());
        });

        deleteButton.setOnMouseClicked(event -> {
            Entity selected = providersListView.getSelectionModel().getSelectedItem();
            new EntityTable().deleteEntity(selected.getId(), new Callback<Entity>() {
                @Override
                public void success(Entity object) {
                    refreshProviders();
                }

                @Override
                public void failure(Exception e) {
                    AlertHelper.popException(e);
                }
            });
        });

        // list view
        providers = FXCollections.observableArrayList();
        FilteredList<Entity> filteredEntities = new FilteredList<>(providers);
        providersListView.setItems(filteredEntities);
        providersListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        providersListView.setCellFactory(param -> new EntityCell());

        // search field binding
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                filteredEntities.setPredicate(entity -> true);
            } else {
                Pattern regex = StringUtil.getSearchPatternFromQuery(newValue, false);
                Predicate<String> regexPred = regex.asPredicate();
                filteredEntities.setPredicate(entity -> regexPred.test(entity.getName()));
            }
        });

        // populate list view
        refreshProviders();
    }

    public void refreshProviders() {
        new EntityTable().getAllEntities(list -> {
            list.removeIf(entity -> entity.getId() == 1); // remove IAF cointe from the list
            providers.setAll(list);
        });
    }


    private static String getFXMLFormAbsolutePath() {
        return RootSceneController.FXML_BASE_PATH + EDIT_PROVIDER_FXML;
    }

    /**
     * A listview cell displaying basing information
     */
    public class EntityCell extends StringListCell<Entity> {
        @Override
        protected String convertItem(Entity item) {
            return item.getName(); // TODO add address information + " (" + item.getAddress().toString() + ")";
        }
    }
}
