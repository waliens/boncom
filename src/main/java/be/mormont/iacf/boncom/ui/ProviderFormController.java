package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.Lg;
import be.mormont.iacf.boncom.data.Address;
import be.mormont.iacf.boncom.data.Entity;
import be.mormont.iacf.boncom.db.Callback;
import be.mormont.iacf.boncom.db.EntityTable;
import be.mormont.iacf.boncom.exceptions.FormContentException;
import be.mormont.iacf.boncom.util.StringUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

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
    @FXML private Label phonesFieldLabel;
    @FXML private TextField phonesField;
    @FXML private Label customerNbFieldLabel;
    @FXML private TextField customerNbField;

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
        phonesFieldLabel.setText("Téléphone(s)");
        customerNbFieldLabel.setText("Numéro client");
        cancelButton.setText("Annuler");
        cancelButton.setOnMouseClicked(event -> closeForm());

        if (entity == null) {
            formTitle.setText("Créer un nouveau fournisseur");
            submitButton.setText("Créer");
            submitButton.setOnMouseClicked(event -> {
                Entity entity;
                try {
                    entity = getEntityFromFields();
                } catch (FormContentException e) {
                    AlertHelper.popAlert(
                            Alert.AlertType.ERROR,
                            "Erreur",
                            "Impossible d'ajouter l'entité",
                            "Le formulaire contient des données incorrectes: " + e.getMessage(),
                            true
                    );
                    return;
                }
                new EntityTable().insertEntity(entity, new Callback<Entity>() {
                    @Override
                    public void success(Entity object) {
                        Lg.getLogger(ProviderFormController.class).info("Added new entity '" + entity.getName() + "'");
                        closeForm();
                    }

                    @Override
                    public void failure(Exception e) {
                        AlertHelper.popAlert(
                            Alert.AlertType.ERROR,
                            "Erreur",
                            "Impossible d'ajouter l'entité",
                            "L'ajout a échoué à cause de : " + e.getMessage(),
                            true
                        );
                        Lg.getLogger(ProviderFormController.class).log(Level.WARNING, "Couldn't add the entity", e);
                    }
                });
            });
        } else {
            formTitle.setText("Mise à jour d'un fournisseur");
            submitButton.setText("Mettre à jour");
            submitButton.setOnMouseClicked(event -> {
                Entity updatedEntity;
                try {
                    updatedEntity = getEntityFromFields();
                } catch (FormContentException e) {
                    AlertHelper.popAlert(
                            Alert.AlertType.ERROR,
                            "Erreur",
                            "Impossible d'ajouter l'entité",
                            "Le formulaire contient des données incorrectes: " + e.getMessage(),
                            true
                    );
                    return;
                }
                updatedEntity.setId(entity.getId());
                new EntityTable().updateEntity(updatedEntity, new Callback<Entity>() {
                    @Override
                    public void success(Entity object) {
                        Lg.getLogger(ProviderFormController.class).info("Updated entity '" + updatedEntity.getName() + "' with id:" + updatedEntity.getId());
                        closeForm();
                    }

                    @Override
                    public void failure(Exception e) {
                        AlertHelper.popAlert(
                                Alert.AlertType.ERROR,
                                "Erreur",
                                "Impossible d'ajouter l'entité",
                                "L'ajout a échoué à cause de : " + e.getMessage(),
                                true
                        );
                        Lg.getLogger(ProviderFormController.class).log(Level.WARNING, "Couldn't add the entity", e);
                    }
                });
            });
            // pre-remplissage des champs
            nameField.setText(entity.getName());
            streetField.setText(entity.getAddress().getStreet());
            numberField.setText(entity.getAddress().getNumber());
            boxField.setText(entity.getAddress().getBox());
            cityField.setText(entity.getAddress().getCity());
            postCodeField.setText(entity.getAddress().getPostCode());
            phonesField.setText(entity.getPhonesAsString());
            customerNbField.setText(entity.getCustomerNb());
        }
    }

    private void closeForm() {
        FXMLModalHelper.closeModal(cancelButton.getParent());
    }

    private String[] getPhones() {
        String[] phones = phonesField.getText().trim().split(",");
        for (int i = 0; i < phones.length; ++i) {
            phones[i] = phones[i].trim();
        }
        return phones;
    }

    private Entity getEntityFromFields() {
        FieldErrorChecker checker = new FieldErrorChecker();
        String street = StringUtil.trimOrNull(streetField.getText());
        String number = StringUtil.trimOrNull(numberField.getText());
        String box = StringUtil.trimOrNull(boxField.getText());
        String postCode = StringUtil.trimOrNull(postCodeField.getText());
        String city = StringUtil.trimOrNull(cityField.getText());
        String name = StringUtil.trimOrNull(nameField.getText());
        String customerNb = StringUtil.trimOrNull(customerNbField.getText());

        checker.put("rue", street);
        checker.put("code postal", postCode);
        checker.put("ville", city);
        checker.put("name", name);

        String empty = checker.whichEmpty();
        if (empty != null) {
            throw new FormContentException("Le champ '" + empty + "' ne peut pas être vide.");
        }

        if (number != null && number.isEmpty()) {
            number = null;
        }

        if (box != null && box.isEmpty()) {
            box = null;
        }

        String[] phones = getPhones();
        Address address = new Address(street, number, box, postCode, city);
        return new Entity(name, address, phones.length == 0 ? null : phones, customerNb);
    }


}
