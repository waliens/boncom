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

    public static String FXML_BASE_PATH = "/be/mormont/iacf/boncom/ui/";
    private static String EDIT_PROVIDER_FXML = "provider_form.fxml";
    private static String EDIT_ORDER_FORM_FXML = "order_form_form.fxml";

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

        createOrderFormBox.setOnMouseClicked(event -> {
            Pair<Parent, OrderFormFormController> nodeCtrl = FXMLModalHelper.popModal(FXML_BASE_PATH + EDIT_ORDER_FORM_FXML, titleLabel.getScene().getWindow());
            nodeCtrl.getValue().setOrderForm(null);
        });
        createProviderBox.setOnMouseClicked(event -> {
            Pair<Parent, ProviderFormController> nodeCtrl = FXMLModalHelper.popModal(FXML_BASE_PATH + EDIT_PROVIDER_FXML, titleLabel.getScene().getWindow());
            nodeCtrl.getValue().setEntity(null);
        });
    }
}
