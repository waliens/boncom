package be.mormont.iacf.boncom.ui;

import be.mormont.iacf.boncom.data.Entity;
import javafx.scene.control.ListCell;

/**
 * Date: 08-07-17
 * By  : Mormont Romain
 */
public class EntityListCell extends ListCell<Entity> {
    @Override
    protected void updateItem(Entity item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            setText(item.getName() + " (" + item.getId() + ")");
        } else {
            setText("_");
        }
    }
}
