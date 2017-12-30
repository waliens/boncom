package be.mormont.iacf.boncom.ui.util;

import javafx.scene.control.ListCell;

/**
 * Date: 30-12-17
 * By  : Mormont Romain
 */
public abstract  class StringListCell<T> extends ListCell<T> {
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText("");
            setGraphic(null);
        } else {
            setText(convertItem(item));
        }
    }

    protected abstract String convertItem(T item);
}
