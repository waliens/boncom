package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.db.Database;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DatabaseOptionsForm implements Initializable {
    @FXML private Label formTitle;

    @FXML private Label databasePathLabel;
    @FXML private TextField databasePathField;
    @FXML private Button databasePathEditButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formTitle.setText("Options > Base de données");
        databasePathLabel.setText("Chemin");
        databasePathLabel.setTooltip(new Tooltip("Chemin de fichier de la base de données"));
        setPathFieldContent();
        databasePathEditButton.setText("Mettre à jour");
        databasePathEditButton.setOnMouseClicked(event -> {
            try {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Nouveau chemin pour la base de données");
                File selectedDirectory = directoryChooser.showDialog(databasePathLabel.getScene().getWindow());
                Database.getDatabase().moveDatabase(selectedDirectory);
                setPathFieldContent();
                AlertHelper.popAlert(
                        Alert.AlertType.CONFIRMATION,
                        "Succès",
                        "Base de données déplacée avec succès",
                        "",
                        true
                );

            } catch (Exception e) {
                AlertHelper.popAlert(
                        Alert.AlertType.ERROR,
                        "Erreur",
                        "Impossible de déplacer la base de données",
                        "Erreur: " + e.getMessage(),
                        true
                );
            }
        });
    }

    private void setPathFieldContent() {
        String databasePath = Database.getDatabasePath();
        databasePathField.setText(databasePath);
        databasePathField.setTooltip(new Tooltip(databasePath));
    }
}
