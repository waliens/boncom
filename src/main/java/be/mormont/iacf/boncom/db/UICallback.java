package be.mormont.iacf.boncom.db;

import javafx.application.Platform;

/**
 * Created by Romain on 03-07-17.
 * Callback for executing code on the UI thread
 */
public abstract class UICallback<T> implements Callback<T> {
    @Override
    public void onFailure(Exception e) {
        Platform.runLater(() -> failure(e));
    }

    @Override
    public void onSuccess(T object) {
        Platform.runLater(() -> success(object));
    }
}
