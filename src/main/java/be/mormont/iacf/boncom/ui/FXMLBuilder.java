package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.Lg;
import be.mormont.iacf.boncom.exceptions.FxmlLoadingException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class FXMLBuilder {
    /**
     * Build a JavaFX component from a FXML file.
     * @param <C> The controller class
     * @param resource The path to the FXML resource file
     * @return The component and its controller
     */
    public static <C> Pair<Parent, C> build(String resource) {
        try {
            URL url = FXMLBuilder.class.getResource(resource);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            C controller = loader.getController();
            return new Pair<>(root, controller);
        } catch (IOException e) {
            Lg.getLogger(FXMLBuilder.class).severe("Cannot initialize scene '" + resource + "' (cause: " + e.getMessage() + ")");
            throw new FxmlLoadingException(e);
        }
    }

    /**
     * Build a JavaFX component from a FXML file
     * @param resource The path to the FXML resource file
     * @return The component
     */
    public static Parent buildParent(String resource) {
        try {
            URL url = FXMLBuilder.class.getResource(resource);
            FXMLLoader loader = new FXMLLoader(url);
            return loader.load();
        } catch (IOException e) {
            Lg.getLogger(FXMLBuilder.class).severe("Cannot initialize scene '" + resource + "' (cause: " + e.getMessage() + ")");
            throw new FxmlLoadingException(e);
        }
    }

}
