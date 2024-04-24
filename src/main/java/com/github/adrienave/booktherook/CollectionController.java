package com.github.adrienave.booktherook;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.net.URL;
import java.util.ResourceBundle;

public class CollectionController implements Initializable {
    @FXML
    private TreeView<String> collectionTree;
    @FXML
    private TextField newFolderNameInput;

    private TreeItem<String> collectionRoot;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        collectionRoot = new TreeItem<>("Folders");
        collectionTree.setRoot(collectionRoot);
    }

    @FXML
    protected void createFolder() {
        String name = newFolderNameInput.getText();
        if (!name.isBlank()) {
            TreeItem<String> newFolder = new TreeItem<>(name);
            collectionRoot.getChildren().add(newFolder);
            newFolderNameInput.setText("");
        }
    }
}