package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.Entity;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Date: 04-07-17
 * By  : Mormont Romain
 */
public class ProviderFormController implements Initializable {
    @FXML private Label formTitle;
    @FXML private Label nameFieldLabel;
    @FXML private TextField nameField;
    @FXML private Label streetFieldLabel;
    @FXML private TextField streetField;
    @FXML private Label numberFieldLabel;
    @FXML private TextField numberField;
    @FXML private Label boxFieldLabel;
    @FXML private TextField boxField;
    @FXML private Label cityFieldLabel;
    @FXML private TextField cityField;
    @FXML private Label postCodeFieldLabel;
    @FXML private TextField postCodeField;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;

    private Entity entity = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refresh();
    }

    public synchronized void setEntity(Entity entity) {
        this.entity = entity;
        refresh();
    }

    private synchronized void refresh() {
        nameFieldLabel.setText("Nom");
        streetFieldLabel.setText("Rue");
        numberFieldLabel.setText("Numéro");
        boxFieldLabel.setText("Boîte");
        cityFieldLabel.setText("Ville");
        postCodeFieldLabel.setText("Code postal");
        cancelButton.setText("Annuler");
        cancelButton.setOnMouseClicked(event -> this.closeModal(cancelButton.getParent()));

        if (entity == null) {
            formTitle.setText("Créer un nouveau fournisseur");
            submitButton.setText("Créer");
            submitButton.setOnMouseClicked(event -> System.out.println("Créer"));
        } else {
            formTitle.setText("Mise à jour d'un fournisseur");
            submitButton.setText("Mettre à jour");
            submitButton.setOnMouseClicked(event -> System.out.println("Mettre à jour"));
            // pre-remplissage des champs
            nameField.setText(entity.getName());
            streetField.setText(entity.getAddress().getStreet());
            numberField.setText(entity.getAddress().getNumber());
            boxField.setText(entity.getAddress().getBox());
            cityField.setText(entity.getAddress().getCity());
            postCodeField.setText(entity.getAddress().getPostCode());
        }
    }

    /**
     * Close the modal
     * @param source The parent issuing the closing request
     */
    private void closeModal(Parent source) {
        Stage stage = (Stage)source.getScene().getWindow();
        stage.close();
    }
}
