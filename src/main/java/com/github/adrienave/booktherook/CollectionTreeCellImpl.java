package com.github.adrienave.booktherook;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

public class CollectionTreeCellImpl extends TreeCell<String> {

    static int game_index = 1;

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setContextMenu(generateContextMenu());
            setText(item);
        }
    }

    private ContextMenu generateContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem createNewGame = new MenuItem("Add new game");
        createNewGame.setOnAction(event -> {
            TreeItem<String> game = new TreeItem<>("Game " + game_index++);
            getTreeItem().getChildren().add(game);
            getTreeItem().setExpanded(true);
        });
        contextMenu.getItems().add(createNewGame);
        return contextMenu;
    }

}
