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
}
