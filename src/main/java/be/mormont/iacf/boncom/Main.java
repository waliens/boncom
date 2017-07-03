package be.mormont.iacf.boncom;

import be.mormont.iacf.boncom.db.Database;
import be.mormont.iacf.boncom.ui.FXMLBuilder;
import be.mormont.iacf.boncom.ui.RootSceneController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Created by Romain on 28-06-17.
 * This is THE (main) class.
 */
public class Main extends Application {
    public static void main(String[] args) {
        try (Database database = Database.getDatabase()) {
            Lg.getLogger(Main.class).info("Create database (if it does not exist)...");
            database.createDatabaseIfNotExist();
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
        Pair<Parent, RootSceneController> rootScene = FXMLBuilder.build("/be/mormont/iacf/boncom/ui/root_scene.fxml");
        primaryStage.setScene(new Scene(rootScene.getKey()));
        primaryStage.show();
    }
}
