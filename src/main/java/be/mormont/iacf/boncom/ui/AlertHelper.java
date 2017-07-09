package be.mormont.iacf.boncom.ui;

import javafx.scene.control.Alert;

/**
 * Date: 05-07-17
 * By  : Mormont Romain
 */
public class AlertHelper {

    /** Pop an alert */
    static void popAlert(Alert.AlertType type, String title, String header, String content, boolean wait) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (wait) {
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

    static void popInvalidField(String field, Exception e) {
        AlertHelper.popAlert(
            Alert.AlertType.ERROR,
            "Erreur",
            "Champ invalide",
            "Le champ '" + field + "' est invalide: " + e.getMessage(),
            true
        );
    }

    static void popEmptyField(String field) {
        AlertHelper.popAlert(
            Alert.AlertType.ERROR,
            "Erreur",
            "Champ invalide",
            "Le champ '" + field + "' ne peut être vide.",
            true
        );
    }
}
