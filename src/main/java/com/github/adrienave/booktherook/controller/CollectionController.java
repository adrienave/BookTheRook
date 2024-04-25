package com.github.adrienave.booktherook.controller;

import com.github.adrienave.booktherook.javafx.CollectionTreeCellImpl;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.net.URL;
import java.util.ResourceBundle;

public class CollectionController implements Initializable {
    @FXML
    private TreeView<Object> collectionTree;
    @FXML
    private TextField newFolderNameInput;

    private TreeItem<Object> collectionRoot;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        collectionRoot = new TreeItem<>("Folders");
        collectionRoot.setExpanded(true);
        collectionTree.setRoot(collectionRoot);
        collectionTree.setCellFactory((TreeView<Object> tree) -> new CollectionTreeCellImpl());
    }

    @FXML
    public void createFolder() {
        String name = newFolderNameInput.getText();
        if (!name.isBlank()) {
            TreeItem<Object> newFolder = new TreeItem<>(name);
            collectionRoot.getChildren().add(newFolder);
            newFolderNameInput.setText("");
        }
    }
}