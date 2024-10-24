package com.github.adrienave.booktherook.controller;

import com.github.adrienave.booktherook.BookTheRook;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.net.URL;

@ExtendWith(ApplicationExtension.class)
class CollectionControllerTest {

    CollectionController collectionController = new CollectionController();

    @Start
    void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BookTheRook.class.getResource("collection-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Book The Rook - Chess Game Collection Manager");
        stage.setScene(scene);
        stage.show();
        collectionController = fxmlLoader.getController();
        URL url = new URL("http://localhost");
        collectionController.initialize(url, null);
    }

    @Test
    void creates_expanded_collection_structure_root_when_initializing(FxRobot robot) {
        TreeItem root = robot.lookup("#collectionTree").queryAs(TreeView.class).getRoot();
        Assertions.assertThat(root.getValue()).isEqualTo("Folders");
        Assertions.assertThat(root.isExpanded()).isTrue();
    }
}