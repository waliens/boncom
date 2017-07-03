package be.mormont.iacf.boncom.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Romain on 03-07-17.
 * This is a class.
 */
public class RootSceneController implements Initializable {

    private static String EDIT_PROVIDER_FXML = "/be/mormont/iacf/boncom/ui/provider_form.fxml";

    @FXML private Label titleLabel;
    @FXML private VBox createOrderFormBox;
    @FXML private VBox createProviderBox;
    @FXML private Label createOrderFormLabel;
    @FXML private Label createProviderLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleLabel.setText("Bon de commandes");
        createOrderFormLabel.setText("Nouveau bon de commande");
        createProviderLabel.setText("Nouveau fournisseur");

        createOrderFormBox.setOnMouseClicked(event -> System.out.println("Create new order form"));
        createProviderBox.setOnMouseClicked(event -> {
            Pair<Parent, ProviderFormController> nodeCtrl = FXMLModalHelper.popModal(EDIT_PROVIDER_FXML, titleLabel.getScene().getWindow());
            nodeCtrl.getValue().setEntity(null);
        });
    }
}
