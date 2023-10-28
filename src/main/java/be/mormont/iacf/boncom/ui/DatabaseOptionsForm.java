package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.db.Database;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DatabaseOptionsForm implements Initializable {
    @FXML private Label formTitle;

    @FXML private Label databasePathLabel;
    @FXML private TextField databasePathField;
    @FXML private Button databasePathEditButton;
    @FXML private Button saveButton;

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

        saveButton.setText("Back-up");
        saveButton.setTooltip(new Tooltip("Sauvegarder l'état de la base de données"));
        saveButton.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            LocalDateTime now = LocalDateTime.now();
            String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            fileChooser.setInitialFileName("boncom-" + formattedDate + ".db");
            File file = fileChooser.showSaveDialog(getWindow());

            try {
                Database.getDatabase().copyDatabase(file);
                AlertHelper.popAlert(
                        Alert.AlertType.CONFIRMATION,
                        "Succès",
                        "Base de données sauvegardée avec succès",
                        "",
                        true
                );
            } catch (Exception e) {
                AlertHelper.popAlert(
                        Alert.AlertType.ERROR,
                        "Erreur",
                        "Impossible de faire une sauvegarde de la base de données",
                        "Erreur: " + e.getMessage(),
                        true
                );
            }
        });
    }

    private Window getWindow() {
        return databasePathLabel.getScene().getWindow();
    }

    private void setPathFieldContent() {
        String databasePath = Database.getDatabasePath();
        databasePathField.setText(databasePath);
        databasePathField.setTooltip(new Tooltip(databasePath));
    }
}
