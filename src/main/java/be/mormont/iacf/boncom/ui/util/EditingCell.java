package be.mormont.iacf.boncom.ui.util;

import be.mormont.iacf.boncom.ui.util.ObservableOrderFormEntry;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import java.util.List;

/** Editing cell
 * - commit on focus loss
 * @param <D> Data type associated with the cell
 * @param <T> Type of the stored element
 */
public abstract class EditingCell<D, T> extends TableCell<D, T> {

    private TextField field = null;

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(field);
            field.requestFocus();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getString());
        setGraphic(null);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (field != null) {
                    field.setText(getEditableString());
                }
                setText(null);
                setGraphic(field);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    /**
     * Create the text field to edit the table
     */
    private void createTextField() {
        field = new TextField(getEditableString());
        field.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                commit();
            }
        });

        EventHandler<? super KeyEvent> enterKeyHandler = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                commit();
                event.consume();
                return;
            }

            // current cell info
            TableView<D> table = getTableView();
            int nbColumns = table.getColumns().size(), nbRows = table.getItems().size();
            TablePosition<D, ?> currCell = table.getEditingCell();
            int currRow = currCell.getRow(), currCol = currCell.getColumn();

            // check next cell, nextCellIdx should be equal to currCellIdx if the movement is illegal
            KeyCodeCombination backTab = new KeyCodeCombination(KeyCode.TAB, KeyCodeCombination.SHIFT_DOWN);
            KeyCode code = event.getCode();

            // if next column is not editable don't move
            List<TableColumn<D, ?>> columns = table.getColumns();
            boolean goUp = code == KeyCode.UP && currRow != 0,
                    goDown = code == KeyCode.DOWN && currRow != (nbRows - 1),
                    goLeft = backTab.match(event) && currCol % nbColumns != 0 && columns.get(currCol - 1).isEditable(),
                    goRight = !backTab.match(event)
                                && code == KeyCode.TAB
                                && currCol % nbColumns != (nbColumns - 1)
                                && columns.get(currCol + 1).isEditable();

            if (!goLeft && !goRight && !goUp && !goDown) {
                if (code == KeyCode.UP || code == KeyCode.DOWN || backTab.match(event) || code == KeyCode.TAB) {
                    event.consume();
                }
                return;
            }

            commit();

            if (goLeft) {
                table.getSelectionModel().selectLeftCell();
            } else if (goRight) {
                table.getSelectionModel().selectRightCell();
            } else if (goUp) {
                table.getSelectionModel().selectAboveCell();
            } else {
                table.getSelectionModel().selectBelowCell();
            }

            TablePosition newCell = table.getFocusModel().getFocusedCell();
            table.edit(newCell.getRow(), newCell.getTableColumn());
            event.consume();
        };
        field.setOnKeyPressed(enterKeyHandler);
    }

    /**
     * @return Value to display when cell is not in editable mode
     */
    protected String getString() {
        return getItem() == null ? "" : getItem().toString();
    }

    /**
     * @return Value to return when the cell gets in editable mode
     */
    protected String getEditableString() { return getString(); }

    /**
     * Commit the current field value
     */
    private void commit() {
        commitEdit(fromString(field.getText()));
    }

    abstract protected T fromString(String v);
}
