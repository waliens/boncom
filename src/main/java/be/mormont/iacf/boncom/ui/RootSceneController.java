package be.mormont.iacf.boncom.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Romain on 03-07-17.
 * This is a class.
 */
public class RootSceneController implements Initializable {
    @FXML private Label titleLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleLabel.setText("Bon de commandes");
    }
}
