package com.github.adrienave.booktherook.javafx;

import com.github.adrienave.booktherook.controller.CollectionController;
import com.github.adrienave.booktherook.model.GameRecord;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import org.kordamp.ikonli.javafx.FontIcon;
import lombok.RequiredArgsConstructor;

import static org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHESS_ROOK;

@RequiredArgsConstructor
public class CollectionTreeCellImpl extends TreeCell<Object> {

    private final CollectionController collectionController;

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setContextMenu(null);
        } else {
            setGraphic(computeIcon(item));
            getStyleClass().removeAll("folder-item", "game-item");
            getStyleClass().add(computeStyleClass(item));
            setContextMenu(generateContextMenu(item));
            setOnMouseClicked(mouseEvent -> handleClick(item));
            setText(item.toString());
        }
    }

    private Node computeIcon(Object item) {
        if (item instanceof GameRecord) {
            return new FontIcon(CHESS_ROOK);
        }
        return null;
    }

    private String computeStyleClass(Object item) {
        if (item instanceof String) {
            return "folder-item";
        } else if (item instanceof GameRecord) {
            return "game-item";
        }
        return "";
    }

    private ContextMenu generateContextMenu(Object item) {
        ContextMenu contextMenu = null;
        if (item instanceof String) {
            contextMenu = new ContextMenu();
            MenuItem createNewGame = new MenuItem("Add new game");
            createNewGame.setOnAction(event -> collectionController.createGameInFolder(getTreeItem()));
            contextMenu.getItems().add(createNewGame);
        }
        return contextMenu;
    }

    private void handleClick(Object item) {
        if (item instanceof GameRecord) {
            String gameLocation = item.toString();
            if (getTreeItem().getParent() != getTreeView().getRoot()) {
                String folderName = (String) getTreeItem().getParent().getValue();
                gameLocation = String.format("%s/%s", folderName, gameLocation);
            }
            collectionController.renderGame(gameLocation);
        }
    }
}
