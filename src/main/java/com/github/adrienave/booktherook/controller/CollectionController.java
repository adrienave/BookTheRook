package com.github.adrienave.booktherook.controller;

import com.github.adrienave.booktherook.exception.InvalidPGNFileException;
import com.github.adrienave.booktherook.exception.MissingGameException;
import com.github.adrienave.booktherook.exception.MissingMandatoryPGNFieldException;
import com.github.adrienave.booktherook.javafx.CollectionTreeCellImpl;
import com.github.adrienave.booktherook.model.GameRecord;
import com.github.adrienave.booktherook.model.HalfMove;
import com.github.adrienave.booktherook.model.Square;
import com.github.adrienave.booktherook.persistence.FileSystemManager;
import com.github.adrienave.booktherook.service.GameService;
import com.github.adrienave.booktherook.util.Piece;
import com.github.adrienave.booktherook.util.Side;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.github.adrienave.booktherook.mapping.PieceFontIcon.convertNodeToPiece;
import static com.github.adrienave.booktherook.mapping.PieceFontIcon.createVisualPiece;
import static com.github.adrienave.booktherook.util.Constants.CHESSBOARD_FILE_COUNT;
import static com.github.adrienave.booktherook.util.Constants.CHESSBOARD_RANK_COUNT;

@RequiredArgsConstructor
public class CollectionController implements Initializable {

    private static final List<Piece> FIRST_RANK_PIECE_SEQUENCE = List.of(
            Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN, Piece.KING, Piece.BISHOP, Piece.KNIGHT, Piece.ROOK);
    private static int game_index = 1;

    @FXML
    private TreeView<Object> collectionTree;
    @FXML
    private Label inputErrorMessage;
    @FXML
    private TextField newFolderNameInput;
    @FXML
    private Button editModeButton;
    @FXML
    private Button playModeButton;
    @FXML
    private Button saveButton;
    @FXML
    private Pane gamePanel;
    @FXML
    private TextField whitePlayerNameField;
    @FXML
    private TextField blackPlayerNameField;
    @FXML
    private TextField eventNameField;
    @FXML
    private TextField resultField;
    @FXML
    private InlineCssTextArea gameContentArea;
    @FXML
    private GridPane chessboard;

    private final FileSystemManager fileSystemManager;
    private final GameService gameService;
    private TreeItem<Object> collectionRoot;
    private boolean isPlayMode;
    private final StackPane[][] stackGrid = new StackPane[CHESSBOARD_RANK_COUNT][CHESSBOARD_FILE_COUNT];

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

        newFolderNameInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                inputErrorMessage.setText("");
                newFolderNameInput.getStyleClass().remove("invalid");
            }
        });

        initializeChessboard();
    }

    @FXML
    public void createFolder() {
        String name = newFolderNameInput.getText();
        if (!name.isBlank()) {
            try {
                fileSystemManager.createFolder(name);
            } catch (IOException e) {
                if (e instanceof FileAlreadyExistsException) {
                    inputErrorMessage.setText("Folder with such name already exists.");
                    newFolderNameInput.getStyleClass().add("invalid");
                    return;
                }
                throw new RuntimeException(String.format("Cannot create folder %s", name), e);
            }
            TreeItem<Object> newFolder = new TreeItem<>(name);
            collectionRoot.getChildren().add(newFolder);
            newFolderNameInput.setText("");
        }
    }

    @FXML
    public void saveGame() {
        try {
            String result = resultField.getText();
            String contentToSave = "[Event \"" + eventNameField.getText() + "\"]\n" +
                    "[White \"" + whitePlayerNameField.getText() + "\"]\n" +
                    "[Black \"" + blackPlayerNameField.getText() + "\"]\n" +
                    "[Result \"" + result + "\"]\n" +
                    "\n" +
                    gameContentArea.getText().replaceAll("\n", "") + " " + result;
            fileSystemManager.saveGame(gameService.getActiveGame().getLocation(), contentToSave);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Cannot save game into file %s", gameService.getActiveGame().getLocation()), e);
        }
    }

    @FXML
    public void swapBoard() {
        if (gameService.getActiveGame() == null) {
            return;
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) {
                ObservableList<Node> startPositionContent = stackGrid[j][i].getChildren();
                ObservableList<Node> endPositionContent = stackGrid[7 - j][7 - i].getChildren();

                swapSquaresContent(startPositionContent, endPositionContent);
            }
        }
    }

    public void createGameInFolder(TreeItem<Object> folderItem) {
        String gameName = "Game " + game_index++;
        TreeItem<Object> game = new TreeItem<>(GameRecord.builder().name(gameName).build());
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
        GameRecord gameRecord;
        try {
            gameRecord = gameService.parsePGN(fileSystemManager.getGamePath(gameLocation));
        } catch (InvalidPGNFileException e) {
            gameRecord = GameRecord.builder().parsingError("File contains improper PGN syntax").build();
        } catch (MissingGameException e) {
            gameRecord = GameRecord.builder().parsingError("File doesn't contain any relevant game data").build();
        } catch (MissingMandatoryPGNFieldException e) {
            gameRecord = GameRecord.builder().parsingError(e.getMessage()).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        gameRecord.setLocation(gameLocation);
        gameService.setActiveGame(gameRecord);

        gamePanel.setVisible(true);
        whitePlayerNameField.setText(gameRecord.getWhitePlayerName());
        blackPlayerNameField.setText(gameRecord.getBlackPlayerName());
        eventNameField.setText(gameRecord.getEventName());
        resultField.setText(gameRecord.getResult());
        gameContentArea.replaceText(gameRecord.formattedContent());
        setChessboardInitialPosition();

        isPlayMode = true;
    }

    public void changeActiveMove(boolean switchToNext) {
        if (gameService.getActiveGame() == null || !isPlayMode) {
            return;
        }

        Optional<HalfMove> moveToProceed = gameService.updateActiveMove(switchToNext);
        if (moveToProceed.isEmpty()) {
            return;
        }
        HalfMove move = moveToProceed.get();
        proceedMove(move, switchToNext);
        highlightActiveMove(switchToNext ? move.getColor() : move.getColor().reverseSide());
    }

    public void switchToEditMode() {
        editModeButton.setVisible(false);
        playModeButton.setVisible(true);
        saveButton.setDisable(false);
        whitePlayerNameField.setEditable(true);
        blackPlayerNameField.setEditable(true);
        eventNameField.setEditable(true);
        resultField.setEditable(true);
        gameContentArea.setEditable(true);
        gameContentArea.clearStyle(0, gameContentArea.getLength());
        isPlayMode = false;
    }

    public void switchToPlayMode() {
        editModeButton.setVisible(true);
        playModeButton.setVisible(false);
        saveButton.setDisable(true);
        whitePlayerNameField.setEditable(false);
        blackPlayerNameField.setEditable(false);
        eventNameField.setEditable(false);
        resultField.setEditable(false);
        gameContentArea.setEditable(false);
        String updatedGameContent = gameContentArea.getText();
        String updatedGameMoves = updatedGameContent.substring(updatedGameContent.indexOf('\n') + 1);
        gameService.setActiveGameUpdatedMoves(gameService.toSAN(updatedGameMoves));
        setChessboardInitialPosition();
        isPlayMode = true;
    }

    private void initializeChessboard() {
        for (int rank = 0; rank < CHESSBOARD_RANK_COUNT; rank++) {
            for (int file = 0; file < CHESSBOARD_FILE_COUNT; file++) {
                String squareClass = rank % 2 == file % 2 ? "light-square" : "dark-square";
                StackPane stackPane = new StackPane();
                stackPane.getStyleClass().add(squareClass);
                chessboard.add(stackPane, file, rank);
                stackGrid[rank][file] = stackPane;
            }
        }
    }

    private void setChessboardInitialPosition() {
        for (int rank = 0; rank < CHESSBOARD_RANK_COUNT; rank++) {
            for (int file = 0; file < CHESSBOARD_FILE_COUNT; file++) {
                stackGrid[rank][file].getChildren().clear();
            }
        }

        for (int file = 0; file < CHESSBOARD_FILE_COUNT; file++) {
            stackGrid[0][file].getChildren().add(createVisualPiece(FIRST_RANK_PIECE_SEQUENCE.get(file), Side.BLACK));
            stackGrid[1][file].getChildren().add(createVisualPiece(Piece.PAWN, Side.BLACK));
        }

        for (int file = 0; file < CHESSBOARD_FILE_COUNT; file++) {
            stackGrid[7][file].getChildren().add(createVisualPiece(FIRST_RANK_PIECE_SEQUENCE.get(file), Side.WHITE));
            stackGrid[6][file].getChildren().add(createVisualPiece(Piece.PAWN, Side.WHITE));
        }
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
            loadGameNamesFromFolderToFolderTree(folderName, folderItem);
            collectionRoot.getChildren().add(folderItem);
        });
        loadGameNamesFromFolderToFolderTree("", collectionRoot);
    }

    private void loadGameNamesFromFolderToFolderTree(String folderName, TreeItem<Object> folderItem) {
        try {
            fileSystemManager.getFileNamesInFolder(folderName).forEach(gameName -> {
                String formattedGameName = FilenameUtils.removeExtension(gameName);
                TreeItem<Object> gameItem = new TreeItem<>(GameRecord.builder().name(formattedGameName).build());
                folderItem.getChildren().add(gameItem);
                game_index++;
            });
        } catch (IOException e) {
            throw new RuntimeException(String.format("Cannot read content of %s directory", folderName.isBlank() ? "data" : folderName), e);
        }
    }

    private void proceedMove(HalfMove move, boolean isPlayed) {
        Pair<Square, Square> boardLocations = move.convertToBoardLocation();
        Square startLocation = boardLocations.getKey();
        Square endLocation = boardLocations.getValue();
        ObservableList<Node> startPositionContent = stackGrid[CHESSBOARD_RANK_COUNT - 1 - startLocation.rank()][startLocation.file()].getChildren();
        ObservableList<Node> endPositionContent = stackGrid[CHESSBOARD_RANK_COUNT - 1 - endLocation.rank()][endLocation.file()].getChildren();
        ObservableList<Node> enPassantPositionContent = stackGrid[CHESSBOARD_RANK_COUNT - 1 - startLocation.rank()][endLocation.file()].getChildren();

        if (isPlayed) {
            if (!endPositionContent.isEmpty()) {
                move.setTakenPiece(convertNodeToPiece(endPositionContent.get(0)));
            } else if (move.isTake()) {
                // Move is taking a piece not located on the end position : must be *en passant*
                move.setTakenPiece(convertNodeToPiece(enPassantPositionContent.get(0)));
                move.setEnPassant(true);
                enPassantPositionContent.clear();
            }
            swapPieceSquare(startPositionContent, endPositionContent);
            if (move.getPromotionPiece() != null) {
                endPositionContent.clear();
                endPositionContent.add(createVisualPiece(move.getPromotionPiece(), move.getColor()));
            }
        } else {
            swapPieceSquare(endPositionContent, startPositionContent);
            if (move.getTakenPiece() != null) {
                ObservableList<Node> takenPositionContent = move.isEnPassant() ? enPassantPositionContent : endPositionContent;
                takenPositionContent.add(createVisualPiece(move.getTakenPiece(), move.getColor().reverseSide()));
            }
            if (move.getPromotionPiece() != null) {
                startPositionContent.clear();
                startPositionContent.add(createVisualPiece(Piece.PAWN, move.getColor()));
            }
        }

        if (move.isCastle()) {
            ObservableList<Node> originalRookPositionContent;
            ObservableList<Node> newRookPositionContent;
            if (move.isKingSideCastle()) {
                originalRookPositionContent = stackGrid[CHESSBOARD_RANK_COUNT - 1 - startLocation.rank()][CHESSBOARD_FILE_COUNT - 1].getChildren();
                newRookPositionContent = stackGrid[CHESSBOARD_RANK_COUNT - 1 - startLocation.rank()][endLocation.file() - 1].getChildren();
            } else {
                originalRookPositionContent = stackGrid[CHESSBOARD_RANK_COUNT - 1 - startLocation.rank()][0].getChildren();
                newRookPositionContent = stackGrid[CHESSBOARD_RANK_COUNT - 1 - startLocation.rank()][endLocation.file() + 1].getChildren();
            }
            if (isPlayed) {
                swapPieceSquare(originalRookPositionContent, newRookPositionContent);
            } else {
                swapPieceSquare(newRookPositionContent, originalRookPositionContent);
            }
        }
    }

    private void highlightActiveMove(Side activeSide) {
        int activeLineIndex = gameService.getActiveGame().getCurrentMoveIndex() / 2;
        String activeLine = gameContentArea.getText(activeLineIndex);
        List<String> lineParts = List.of(activeLine.split(" "));
        int moveStart, moveEnd;
        if (activeSide == Side.WHITE) {
            moveStart = lineParts.get(0).length() + 1;
            moveEnd = moveStart + lineParts.get(1).length() + 1;
        } else {
            moveStart = lineParts.get(0).length() + 1 + lineParts.get(1).length() + 1;
            moveEnd = moveStart + lineParts.get(2).length();
        }

        gameContentArea.clearStyle(0, gameContentArea.getLength());
        gameContentArea.setStyle(activeLineIndex, 0, lineParts.get(0).length(), "-fx-font-weight: bold;");
        gameContentArea.setStyle(activeLineIndex, moveStart, moveEnd, "-fx-font-weight: bold;");
    }

    private static void swapPieceSquare(ObservableList<Node> sourceSquareContent, ObservableList<Node> destinationSquareContent) {
        Node piece = sourceSquareContent.get(0);
        sourceSquareContent.clear();
        destinationSquareContent.clear();
        destinationSquareContent.add(piece);
    }

    private static void swapSquaresContent(ObservableList<Node> firstSquare, ObservableList<Node> secondSquare) {
        Optional<Node> firstPiece = popPiece(firstSquare);
        Optional<Node> secondPiece = popPiece(secondSquare);

        secondPiece.ifPresent(firstSquare::add);
        firstPiece.ifPresent(secondSquare::add);
    }

    private static Optional<Node> popPiece(ObservableList<Node> square) {
        if (!square.isEmpty()) {
            Node piece = square.get(0);
            square.clear();
            return Optional.of(piece);
        }
        return Optional.empty();
    }
}