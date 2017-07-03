package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.Main;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;

/**
 * Date: 13-01-17
 * By  : Mormont Romain
 */
public class FXMLModalHelper {
    /**
     * Pop a modal containing the JavaFX component represented in the
     * FXML resource.
     * @param resource The resource defining the content of the modal
     * @param parent The window owning of the modal
     * @return The node loaded in the modal and its controller
     */
    public static <T> Pair<Parent, T> popModal(String resource, Window parent) {
        final Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(parent);
        Pair<Parent, T> modalNode = FXMLBuilder.build(resource);
        Scene scene = new Scene(modalNode.getKey());
        scene.getStylesheets().add(Main.getCssPath());
        modal.setScene(scene);
        modal.show();
        return modalNode;
    }
}