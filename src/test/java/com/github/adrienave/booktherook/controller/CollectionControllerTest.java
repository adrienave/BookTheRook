package com.github.adrienave.booktherook.controller;

import com.github.adrienave.booktherook.BookTheRook;
import com.github.adrienave.booktherook.persistence.FileSystemManager;
import com.github.adrienave.booktherook.service.GameService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import java.nio.file.FileAlreadyExistsException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class CollectionControllerTest {

    private final FileSystemManager fileSystemManager = mock(FileSystemManager.class);
    private final GameService gameService = mock(GameService.class);
    private final CollectionController collectionController = new CollectionController(fileSystemManager, gameService);

    @Start
    void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BookTheRook.class.getResource("collection-view.fxml"));
        fxmlLoader.setController(collectionController);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    @SuppressWarnings("unchecked")
    void creates_expanded_collection_structure_root_when_initializing(FxRobot robot) {
        TreeItem<String> root = robot.lookup("#collectionTree").queryAs(TreeView.class).getRoot();
        Assertions.assertThat(root.getValue()).isEqualTo("Folders");
        Assertions.assertThat(root.isExpanded()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void creates_folder_when_name_is_not_blank(FxRobot robot) throws IOException {
        String folderName = "My first folder";
        TextField textField = robot.lookup("#newFolderNameInput").queryAs(TextField.class);
        textField.setText(folderName);

        collectionController.createFolder();

        verify(fileSystemManager).createFolder(folderName);
        TreeItem<String> root = robot.lookup("#collectionTree").queryAs(TreeView.class).getRoot();
        Assertions.assertThat(root.getChildren()).hasSize(1).extracting(TreeItem::getValue).contains(folderName);
    }

    @Test
    void resets_input_when_folder_is_created(FxRobot robot) {
        String folderName = "My first folder";
        TextField textField = robot.lookup("#newFolderNameInput").queryAs(TextField.class);
        textField.setText(folderName);

        collectionController.createFolder();

        Assertions.assertThat(textField.getText()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void does_not_create_folder_when_name_is_blank(FxRobot robot) throws IOException {
        TextField textField = robot.lookup("#newFolderNameInput").queryAs(TextField.class);
        textField.setText("");

        collectionController.createFolder();

        verify(fileSystemManager, never()).createFolder(any());
        TreeItem<String> root = robot.lookup("#collectionTree").queryAs(TreeView.class).getRoot();
        Assertions.assertThat(root.getChildren()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void display_proper_error_when_folder_with_name_already_exists(FxRobot robot) throws IOException {
        String folderName = "My first folder";
        TextField textField = robot.lookup("#newFolderNameInput").queryAs(TextField.class);
        textField.setText(folderName);
        doThrow(FileAlreadyExistsException.class).when(fileSystemManager).createFolder(any());

        Platform.runLater(collectionController::createFolder);

        TreeItem<String> root = robot.lookup("#collectionTree").queryAs(TreeView.class).getRoot();
        Assertions.assertThat(root.getChildren()).isEmpty();
        Label label = robot.lookup("#inputErrorMessage").queryAs(Label.class);
        Assertions.assertThat(label).isVisible().hasText("Folder with such name already exists.");
    }
}