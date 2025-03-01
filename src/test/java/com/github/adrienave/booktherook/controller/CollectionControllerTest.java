package com.github.adrienave.booktherook.controller;

import com.github.adrienave.booktherook.BookTheRook;
import com.github.adrienave.booktherook.model.GameRecord;
import com.github.adrienave.booktherook.persistence.FileSystemManager;
import com.github.adrienave.booktherook.service.GameService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.fxmisc.richtext.InlineCssTextArea;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.assertions.api.Assertions;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class CollectionControllerTest {

    private final FileSystemManager fileSystemManager = mock(FileSystemManager.class);
    private final GameService gameService = mock(GameService.class);
    private final CollectionController collectionController = new CollectionController(fileSystemManager, gameService);

    @Start
    @SuppressWarnings("unused")
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
        TreeItem<String> collectionTree = robot.lookup("#collectionTree").queryAs(TreeView.class).getRoot();
        Assertions.assertThat(collectionTree.getValue()).isEqualTo("Folders");
        Assertions.assertThat(collectionTree.isExpanded()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void load_into_tree_structure_top_level_games_when_initializing(FxRobot robot) throws IOException {
        String firstGameName = "First_game";
        String secondGameName = "Second_game";
        when(fileSystemManager.getFileNamesInFolder("")).thenReturn(List.of(firstGameName + ".pgn", secondGameName + ".pgn"));

        runLaterButNotTooLate(() -> collectionController.initialize(null, null));

        TreeItem<Object> collectionTree = robot.lookup("#collectionTree").queryAs(TreeView.class).getRoot();
        Assertions.assertThat(collectionTree.getChildren())
               .hasSize(2)
               .extracting(TreeItem::getValue)
               .extracting(Object::toString)
               .containsExactlyInAnyOrder(firstGameName, secondGameName);
    }

    @Test
    @SuppressWarnings("unchecked")
    void load_into_tree_structure_sub_folder_games_when_initializing(FxRobot robot) throws IOException {
        List<String> folderNames = List.of("Folder A", "Folder B");
        String gameInFirstFolder = "First_game_in_folder_A";
        String gameInSecondFolder = "Second_game_in_folder_B";
        String otherGameInSecondFolder = "Third_game_in_folder_B";
        when(fileSystemManager.getFolderNames()).thenReturn(folderNames);
        when(fileSystemManager.getFileNamesInFolder(folderNames.get(0))).thenReturn(List.of(gameInFirstFolder + ".pgn"));
        when(fileSystemManager.getFileNamesInFolder(folderNames.get(1))).thenReturn(List.of(gameInSecondFolder + ".pgn", otherGameInSecondFolder + ".pgn"));

        runLaterButNotTooLate(() -> collectionController.initialize(null, null));

        TreeItem<Object> collectionTree = robot.lookup("#collectionTree").queryAs(TreeView.class).getRoot();
        Assertions.assertThat(collectionTree.getChildren()).hasSize(2);
        List<TreeItem<Object>> subFolderGames = collectionTree.getChildren().stream().map((treeItem) -> (List<TreeItem<Object>>) treeItem.getChildren()).flatMap(Collection::stream).toList();
        Assertions.assertThat(subFolderGames).hasSize(3).extracting(TreeItem::getValue).extracting(Object::toString).containsExactlyInAnyOrder(gameInFirstFolder, gameInSecondFolder, otherGameInSecondFolder);
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

        runLaterButNotTooLate(collectionController::createFolder);

        TreeItem<String> root = robot.lookup("#collectionTree").queryAs(TreeView.class).getRoot();
        Assertions.assertThat(root.getChildren()).isEmpty();
        Label label = robot.lookup("#inputErrorMessage").queryAs(Label.class);
        Assertions.assertThat(label).isVisible().hasText("Folder with such name already exists.");
    }

    @Test
    void render_game_moves_into_game_area_when_game_is_valid(FxRobot robot) throws Exception {
        String gameLocation = "/myGame";
        String formattedMoves = "1. c4 c5 2. g3 Nc6";
        GameRecord game = mock(GameRecord.class);
        when(gameService.parsePGN(any())).thenReturn(game);
        when(game.formattedContent()).thenReturn(formattedMoves);

        runLaterButNotTooLate(() -> collectionController.renderGame(gameLocation));

        String renderedContent = robot.lookup("#gameContentArea").queryAs(InlineCssTextArea.class).getText();
        Assertions.assertThat(renderedContent).isEqualTo(formattedMoves);
    }

    @Test
    void render_meta_game_fields_when_game_is_valid(FxRobot robot) throws Exception {
        String gameLocation = "/myGame";
        String eventName = "Cat Kings Tournament";
        String whitePlayerName = "Oswald Lagrouse";
        String blackPlayerName = "Le Chat Potté";
        String result = "1-0";
        GameRecord game = GameRecord.builder().name(eventName).whitePlayerName(whitePlayerName).blackPlayerName(blackPlayerName).result(result).build();
        when(gameService.parsePGN(any())).thenReturn(game);

        runLaterButNotTooLate(() -> collectionController.renderGame(gameLocation));

        String renderedWhitePlayerName = robot.lookup("#whitePlayerNameField").queryAs(TextField.class).getText();
        Assertions.assertThat(renderedWhitePlayerName).isEqualTo(whitePlayerName);
        String renderedBlackPlayerName = robot.lookup("#blackPlayerNameField").queryAs(TextField.class).getText();
        Assertions.assertThat(renderedBlackPlayerName).isEqualTo(blackPlayerName);
        String renderedEventName = robot.lookup("#eventNameField").queryAs(TextField.class).getText();
        Assertions.assertThat(renderedEventName).isEqualTo(eventName);
        String renderedResult = robot.lookup("#resultField").queryAs(TextField.class).getText();
        Assertions.assertThat(renderedResult).isEqualTo(result);
    }

    @Test
    void render_error_message_into_game_area_when_game_format_is_invalid(FxRobot robot) throws Exception {
        String gameLocation = "/myInvalidGame";
        when(gameService.parsePGN(any())).thenThrow(IndexOutOfBoundsException.class);

        runLaterButNotTooLate(() -> collectionController.renderGame(gameLocation));

        String renderedContent = robot.lookup("#gameContentArea").queryAs(InlineCssTextArea.class).getText();
        Assertions.assertThat(renderedContent).isEqualTo("Error while parsing game");
    }

    @Test
    void save_active_game_to_its_location_when_game_is_selected() throws IOException {
        String location = "/path/to/the/game";
        GameRecord activeGame = GameRecord.builder().location(location).build();
        when(gameService.getActiveGame()).thenReturn(activeGame);

        collectionController.saveGame();

        verify(fileSystemManager).saveGame(eq(location), any());
    }

    @Test
    void save_active_game_with_content_from_fields_when_game_is_selected(FxRobot robot) throws IOException {
        String gameContent = "1. c4 e5 2. Nc3 Nf6";
        String eventName = "Cat Kings Tournament";
        String whitePlayerName = "Oswald Lagrouse";
        String blackPlayerName = "Le Chat Potté";
        String result = "1-0";
        String expectedGameFileContent = "[Event \"" + eventName + "\"]\n" +
                "[White \"" + whitePlayerName + "\"]\n" +
                "[Black \"" + blackPlayerName +"\"]\n" +
                "[Result \"" + result + "\"]\n" +
                "\n" +
                gameContent + " " + result;

        TextField eventNameField = robot.lookup("#eventNameField").queryAs(TextField.class);
        eventNameField.setText(eventName);
        TextField whitePlayerNameField = robot.lookup("#whitePlayerNameField").queryAs(TextField.class);
        whitePlayerNameField.setText(whitePlayerName);
        TextField blackPlayerNameField = robot.lookup("#blackPlayerNameField").queryAs(TextField.class);
        blackPlayerNameField.setText(blackPlayerName);
        TextField resultField = robot.lookup("#resultField").queryAs(TextField.class);
        resultField.setText(result);
        InlineCssTextArea inlineCssTextArea = robot.lookup("#gameContentArea").queryAs(InlineCssTextArea.class);
        runLaterButNotTooLate(() -> inlineCssTextArea.replaceText(gameContent));

        GameRecord activeGame = GameRecord.builder().location("/path/to/the/game").build();
        when(gameService.getActiveGame()).thenReturn(activeGame);

        collectionController.saveGame();

        verify(fileSystemManager).saveGame(any(), eq(expectedGameFileContent));
    }

    @Test
    void removes_line_breaks_from_game_content_when_game_is_saved(FxRobot robot) throws IOException {
        String gameContent = "1. c4 e5 \n" +
                "2. Nc3 Nf6";
        String expectedSavedContent = "1. c4 e5 2. Nc3 Nf6";
        InlineCssTextArea inlineCssTextArea = robot.lookup("#gameContentArea").queryAs(InlineCssTextArea.class);
        runLaterButNotTooLate(() -> inlineCssTextArea.replaceText(gameContent));

        GameRecord activeGame = GameRecord.builder().location("/path/to/the/game").build();
        when(gameService.getActiveGame()).thenReturn(activeGame);

        collectionController.saveGame();

        verify(fileSystemManager).saveGame(any(), contains(expectedSavedContent));
    }

    @Test
    void enable_save_button_when_switch_to_edit_mode(FxRobot robot) {
        runLaterButNotTooLate(collectionController::switchToEditMode);

        Button saveButton = robot.lookup("#saveButton").queryAs(Button.class);
        Assertions.assertThat(saveButton).isVisible().isEnabled();
    }

    @Test
    void disable_save_button_when_switch_to_play_mode(FxRobot robot) {
        runLaterButNotTooLate(collectionController::switchToPlayMode);

        Button saveButton = robot.lookup("#saveButton").queryAs(Button.class);
        Assertions.assertThat(saveButton).isVisible().isDisabled();
    }

    @Test
    void toggle_play_edit_buttons_when_switch_to_edit_mode(FxRobot robot) {
        runLaterButNotTooLate(collectionController::switchToEditMode);

        Button editModeButton = robot.lookup("#editModeButton").queryAs(Button.class);
        Assertions.assertThat(editModeButton).isInvisible();
        Button playModeButton = robot.lookup("#playModeButton").queryAs(Button.class);
        Assertions.assertThat(playModeButton).isVisible();
    }

    @Test
    void toggle_play_edit_buttons_when_switch_to_play_mode(FxRobot robot) {
        runLaterButNotTooLate(collectionController::switchToPlayMode);

        Button editModeButton = robot.lookup("#editModeButton").queryAs(Button.class);
        Assertions.assertThat(editModeButton).isVisible();
        Button playModeButton = robot.lookup("#playModeButton").queryAs(Button.class);
        Assertions.assertThat(playModeButton).isInvisible();
    }

    @Test
    void enable_game_metadata_inputs_when_switch_to_edit_mode(FxRobot robot) {
        runLaterButNotTooLate(collectionController::switchToEditMode);

        TextField whitePlayerNameInput = robot.lookup("#whitePlayerNameField").queryAs(TextField.class);
        Assertions.assertThat(whitePlayerNameInput.isEditable()).isTrue();
        TextField blackPlayerNameInput = robot.lookup("#blackPlayerNameField").queryAs(TextField.class);
        Assertions.assertThat(blackPlayerNameInput.isEditable()).isTrue();
        TextField eventNameInput = robot.lookup("#eventNameField").queryAs(TextField.class);
        Assertions.assertThat(eventNameInput.isEditable()).isTrue();
        TextField resultInput = robot.lookup("#resultField").queryAs(TextField.class);
        Assertions.assertThat(resultInput.isEditable()).isTrue();
    }

    @Test
    void disable_game_metadata_inputs_when_switch_to_play_mode(FxRobot robot) {
        runLaterButNotTooLate(collectionController::switchToPlayMode);

        TextField whitePlayerNameInput = robot.lookup("#whitePlayerNameField").queryAs(TextField.class);
        Assertions.assertThat(whitePlayerNameInput.isEditable()).isFalse();
        TextField blackPlayerNameInput = robot.lookup("#blackPlayerNameField").queryAs(TextField.class);
        Assertions.assertThat(blackPlayerNameInput.isEditable()).isFalse();
        TextField eventNameInput = robot.lookup("#eventNameField").queryAs(TextField.class);
        Assertions.assertThat(eventNameInput.isEditable()).isFalse();
        TextField resultInput = robot.lookup("#resultField").queryAs(TextField.class);
        Assertions.assertThat(resultInput.isEditable()).isFalse();
    }

    @SneakyThrows
    private static void runLaterButNotTooLate(Runnable codeToExecute) {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> {
            codeToExecute.run();
            semaphore.release();
        });
        semaphore.acquire();
    }
}