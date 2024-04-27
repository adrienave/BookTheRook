package com.github.adrienave.booktherook.controller;

import com.github.adrienave.booktherook.javafx.CollectionTreeCellImpl;
import com.github.adrienave.booktherook.model.GameRecord;
import com.github.adrienave.booktherook.persistence.FileSystemManager;
import com.github.adrienave.booktherook.util.Piece;
import com.github.adrienave.booktherook.util.Side;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.apache.commons.io.FilenameUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static com.github.adrienave.booktherook.util.Constants.CHESSBOARD_COLUMNS;
import static com.github.adrienave.booktherook.util.Constants.CHESSBOARD_ROWS;
import static org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.*;

public class CollectionController implements Initializable {

    private static int game_index = 1;

    @FXML
    private TreeView<Object> collectionTree;
    @FXML
    private TextField newFolderNameInput;
    @FXML
    private TextArea gameContentArea;
    @FXML
    private GridPane chessboard;

    private TreeItem<Object> collectionRoot;
    private FileSystemManager fileSystemManager;

    private String selectedGameLocation;
    private final StackPane[][] stackGrid = new StackPane[CHESSBOARD_ROWS][CHESSBOARD_COLUMNS];

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileSystemManager = new FileSystemManager();
        try {
            fileSystemManager.initializeDataDirectory();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create data directory", e);
        }

        collectionRoot = new TreeItem<>("Folders");
        loadDataToTree();
        collectionRoot.setExpanded(true);
        collectionTree.setRoot(collectionRoot);
        collectionTree.setCellFactory((TreeView<Object> tree) -> new CollectionTreeCellImpl(this));

        initializeChessboard();
    }

    private void initializeChessboard() {
        for (int row = 0; row < CHESSBOARD_ROWS; row++) {
            for (int column = 0; column < CHESSBOARD_COLUMNS; column++) {
                String squareClass = row % 2 == column % 2 ? "light-square" : "dark-square";
                StackPane stackPane = new StackPane();
                stackPane.getStyleClass().add(squareClass);
                chessboard.add(stackPane, column, row);
                stackGrid[row][column] = stackPane;
            }
        }
    }

    private void setChessboardInitialPosition() {
        for (int row = 0; row < CHESSBOARD_ROWS; row++) {
            for (int column = 0; column < CHESSBOARD_COLUMNS; column++) {
                stackGrid[row][column].getChildren().clear();
            }
        }

        stackGrid[0][0].getChildren().add(createVisualPiece(Piece.ROOK, Side.BLACK));
        stackGrid[0][1].getChildren().add(createVisualPiece(Piece.KNIGHT, Side.BLACK));
        stackGrid[0][2].getChildren().add(createVisualPiece(Piece.BISHOP, Side.BLACK));
        stackGrid[0][3].getChildren().add(createVisualPiece(Piece.QUEEN, Side.BLACK));
        stackGrid[0][4].getChildren().add(createVisualPiece(Piece.KING, Side.BLACK));
        stackGrid[0][5].getChildren().add(createVisualPiece(Piece.BISHOP, Side.BLACK));
        stackGrid[0][6].getChildren().add(createVisualPiece(Piece.KNIGHT, Side.BLACK));
        stackGrid[0][7].getChildren().add(createVisualPiece(Piece.ROOK, Side.BLACK));
        for (int column = 0; column < CHESSBOARD_COLUMNS; column++) {
            stackGrid[1][column].getChildren().add(createVisualPiece(Piece.PAWN, Side.BLACK));
        }

        stackGrid[7][0].getChildren().add(createVisualPiece(Piece.ROOK, Side.WHITE));
        stackGrid[7][1].getChildren().add(createVisualPiece(Piece.KNIGHT, Side.WHITE));
        stackGrid[7][2].getChildren().add(createVisualPiece(Piece.BISHOP, Side.WHITE));
        stackGrid[7][3].getChildren().add(createVisualPiece(Piece.QUEEN, Side.WHITE));
        stackGrid[7][4].getChildren().add(createVisualPiece(Piece.KING, Side.WHITE));
        stackGrid[7][5].getChildren().add(createVisualPiece(Piece.BISHOP, Side.WHITE));
        stackGrid[7][6].getChildren().add(createVisualPiece(Piece.KNIGHT, Side.WHITE));
        stackGrid[7][7].getChildren().add(createVisualPiece(Piece.ROOK, Side.WHITE));
        for (int column = 0; column < CHESSBOARD_COLUMNS; column++) {
            stackGrid[6][column].getChildren().add(createVisualPiece(Piece.PAWN, Side.WHITE));
        }
    }

    private FontIcon createVisualPiece(Piece pieceName, Side side) {
        FontIcon pieceIcon = new FontIcon(QUESTION);
        switch (pieceName) {
            case PAWN: {
                pieceIcon = new FontIcon(CHESS_PAWN);
                break;
            }
            case ROOK: {
                pieceIcon = new FontIcon(CHESS_ROOK);
                break;
            }
            case KNIGHT: {
                pieceIcon = new FontIcon(CHESS_KNIGHT);
                break;
            }
            case BISHOP: {
                pieceIcon = new FontIcon(CHESS_BISHOP);
                break;
            }
            case QUEEN: {
                pieceIcon = new FontIcon(CHESS_QUEEN);
                break;
            }
            case KING: {
                pieceIcon = new FontIcon(CHESS_KING);
                break;
            }
        }
        switch (side) {
            case BLACK -> pieceIcon.setIconColor(Color.BLACK);
            case WHITE -> pieceIcon.setIconColor(Color.WHITE);
        }
        pieceIcon.setIconSize(30);
        pieceIcon.setStroke(Color.BLACK);
        return pieceIcon;
    }

    private void loadDataToTree() {
        List<String> existingFolderNames;
        try {
            existingFolderNames = fileSystemManager.getFolderNames();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read content of data directory", e);
        }
        existingFolderNames.forEach(folderName -> {
            TreeItem<Object> folderItem = new TreeItem<>(folderName);
            try {
                fileSystemManager.getFileNamesInFolder(folderName).forEach(name -> {
                    String formattedGameName = FilenameUtils.removeExtension(name);
                    TreeItem<Object> gameItem = new TreeItem<>(new GameRecord(formattedGameName));
                    folderItem.getChildren().add(gameItem);
                    game_index++;
                });
            } catch (IOException e) {
                throw new RuntimeException(String.format("Cannot read content of %s directory", folderName), e);
            }
            collectionRoot.getChildren().add(folderItem);
        });
        try {
            fileSystemManager.getFileNamesInFolder("").forEach(gameName -> {
                String formattedGameName = FilenameUtils.removeExtension(gameName);
                TreeItem<Object> gameItem = new TreeItem<>(new GameRecord(formattedGameName));
                collectionRoot.getChildren().add(gameItem);
                game_index++;
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot read content of data directory", e);
        }
    }

    @FXML
    public void createFolder() {
        String name = newFolderNameInput.getText();
        if (!name.isBlank()) {
            try {
                fileSystemManager.createFolder(name);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Cannot create folder %s", name), e);
            }
            TreeItem<Object> newFolder = new TreeItem<>(name);
            collectionRoot.getChildren().add(newFolder);
            newFolderNameInput.setText("");
        }
    }

    public void createGameInFolder(TreeItem<Object> folderItem) {
        String gameName = "Game " + game_index++;
        TreeItem<Object> game = new TreeItem<>(new GameRecord(gameName));
        folderItem.getChildren().add(game);
        folderItem.setExpanded(true);
        String folderName = folderItem.getParent() != null ? (String) folderItem.getValue() : "";
        try {
            fileSystemManager.createGame(gameName, folderName);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Cannot create game file %s", gameName), e);
        }
    }

    public void renderGame(String gameLocation) {
        String gameContent;
        try {
            gameContent = fileSystemManager.loadGame(gameLocation);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Cannot read content of game file %s", gameLocation), e);
        }
        selectedGameLocation = gameLocation;

        gameContentArea.setText(gameContent);
        gameContentArea.setVisible(true);

        setChessboardInitialPosition();
        chessboard.setVisible(true);
    }

    @FXML
    public void saveGame() {
        try {
            fileSystemManager.saveGame(selectedGameLocation, gameContentArea.getText());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Cannot save game into file %s", selectedGameLocation), e);
        }
    }
}