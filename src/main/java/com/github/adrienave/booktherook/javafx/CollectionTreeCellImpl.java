package com.github.adrienave.booktherook.javafx;

import com.github.adrienave.booktherook.model.GameRecord;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

public class CollectionTreeCellImpl extends TreeCell<Object> {

    private static int game_index = 1;

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setContextMenu(null);
        } else {
            setContextMenu(generateContextMenu(item));
            setText(item.toString());
        }
    }

    private ContextMenu generateContextMenu(Object item) {
        ContextMenu contextMenu = null;
        if (item instanceof String) {
            contextMenu = new ContextMenu();
            MenuItem createNewGame = new MenuItem("Add new game");
            createNewGame.setOnAction(event -> {
                TreeItem<Object> game = new TreeItem<>(new GameRecord("Game " + game_index++));
                getTreeItem().getChildren().add(game);
                getTreeItem().setExpanded(true);
            });
            contextMenu.getItems().add(createNewGame);
        }
        return contextMenu;
    }

}
