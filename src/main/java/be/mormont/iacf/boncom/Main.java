package be.mormont.iacf.boncom;

import be.mormont.iacf.boncom.db.Database;
import be.mormont.iacf.boncom.db.migrations.MigrationHandler;
import be.mormont.iacf.boncom.ui.FXMLBuilder;
import be.mormont.iacf.boncom.ui.RootSceneController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Created by Romain on 28-06-17.
 * This is THE (main) class.
 */
public class Main extends Application {
    private static String ROOT_FXML = "/be/mormont/iacf/boncom/ui/root_scene.fxml";
    private static String STYLE_CSS = "/be/mormont/iacf/boncom/ui/style.css";

    public static void main(String[] args) {
        try (Database database = Database.getDatabase()) {
            Lg.getLogger(Main.class).info("Check migrations");
            MigrationHandler handler = new MigrationHandler();
            handler.checkAndUpdate();
            Lg.getLogger(Main.class).info("Launch UI...");
            launch(args);
        } catch (SQLException e) {
            Lg.getLogger(Main.class).log(Level.SEVERE, "Issue with database", e);
        } catch (Exception e) {
            Lg.getLogger(Main.class).log(Level.SEVERE, "Unhandled exception", e);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // register on close event
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        Pair<Parent, RootSceneController> rootScene = FXMLBuilder.build(ROOT_FXML);
        Scene scene = new Scene(rootScene.getKey());
        scene.getStylesheets().add(STYLE_CSS);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static String getCssPath() {
        return STYLE_CSS;
    }
}


