package com.github.adrienave.booktherook.controller;

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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.apache.commons.io.FilenameUtils;
import org.fxmisc.richtext.InlineCssTextArea;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.github.adrienave.booktherook.util.Constants.CHESSBOARD_FILE_COUNT;
import static com.github.adrienave.booktherook.util.Constants.CHESSBOARD_RANK_COUNT;
import static org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.*;

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
    private InlineCssTextArea gameContentArea;
    @FXML
    private GridPane chessboard;

    private TreeItem<Object> collectionRoot;
    private FileSystemManager fileSystemManager;
    private boolean isPlayMode;
    private final StackPane[][] stackGrid = new StackPane[CHESSBOARD_RANK_COUNT][CHESSBOARD_FILE_COUNT];
    private final GameService gameService = new GameService();

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
            fileSystemManager.saveGame(gameService.getActiveGame().getLocation(), gameContentArea.getText());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Cannot save game into file %s", gameService.getActiveGame().getLocation()), e);
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
        GameRecord gameRecord = GameRecord.builder().name("Error while parsing game").build();
        try {
            gameRecord = GameService.parsePGN(fileSystemManager.getGamePath(gameLocation));
        } catch (Exception e) {
            // TODO: render error message when game cannot be parsed (#14)
            System.err.println(e);
        }
        gameRecord.setLocation(gameLocation);
        gameService.setActiveGame(gameRecord);

        String gameText = readRecord(gameRecord);
        gameContentArea.replaceText(gameText);
        gameContentArea.setVisible(true);

        setChessboardInitialPosition();
        chessboard.setVisible(true);
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
        gameContentArea.setEditable(true);
        gameContentArea.clearStyle(0, gameContentArea.getLength());
        isPlayMode = false;
    }

    public void switchToPlayMode() {
        editModeButton.setVisible(true);
        playModeButton.setVisible(false);
        saveButton.setDisable(true);
        gameContentArea.setEditable(false);
        String updatedGameContent = gameContentArea.getText();
        String updatedGameMoves = updatedGameContent.substring(updatedGameContent.indexOf('\n') + 1);
        gameService.setActiveGameUpdatedMoves(GameService.toSAN(updatedGameMoves));
        setChessboardInitialPosition();
        isPlayMode = true;
    }

    private static String readRecord(GameRecord gameRecord) {
        StringBuilder gameText = new StringBuilder(gameRecord.getName() + "\n");
        int currentMoveIndex = 1;
        boolean isWhiteMove = true;
        for (HalfMove move : gameRecord.getMoves()) {
            if (isWhiteMove) {
                gameText.append(String.format("%d. %s", currentMoveIndex, move.getAlgebraicNotation()));
            } else {
                gameText.append(String.format(" %s \n", move.getAlgebraicNotation()));
                currentMoveIndex++;
            }
            isWhiteMove = !isWhiteMove;
        }
        return gameText.toString();
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

    private FontIcon createVisualPiece(Piece pieceName, Side side) {
        FontIcon pieceIcon = new FontIcon(QUESTION);
        switch (pieceName) {
            case PAWN -> pieceIcon = new FontIcon(CHESS_PAWN);
            case ROOK -> pieceIcon = new FontIcon(CHESS_ROOK);
            case KNIGHT -> pieceIcon = new FontIcon(CHESS_KNIGHT);
            case BISHOP -> pieceIcon = new FontIcon(CHESS_BISHOP);
            case QUEEN -> pieceIcon = new FontIcon(CHESS_QUEEN);
            case KING -> pieceIcon = new FontIcon(CHESS_KING);
        }
        switch (side) {
            case BLACK -> pieceIcon.setIconColor(Color.BLACK);
            case WHITE -> pieceIcon.setIconColor(Color.WHITE);
        }
        pieceIcon.setIconSize(30);
        pieceIcon.setStroke(Color.BLACK);
        return pieceIcon;
    }

    private Piece convertNodeToPiece(Node node) {
        if (!(node instanceof FontIcon pieceRepresentation)) {
            throw new RuntimeException(String.format("Node %s is not a valid Piece representation", node));
        }
        if (!(pieceRepresentation.getIconCode() instanceof FontAwesomeSolid pieceIcon)) {
            throw new RuntimeException(String.format("Node %s is not a valid Piece representation", node));
        }
        return switch (pieceIcon) {
            case CHESS_PAWN -> Piece.PAWN;
            case CHESS_ROOK -> Piece.ROOK;
            case CHESS_KNIGHT -> Piece.KNIGHT;
            case CHESS_BISHOP -> Piece.BISHOP;
            case CHESS_QUEEN -> Piece.QUEEN;
            case CHESS_KING -> Piece.KING;
            default ->
                    throw new RuntimeException(String.format("Icon %s is not a valid Piece representation", pieceRepresentation.getIconLiteral()));
        };
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
                    TreeItem<Object> gameItem = new TreeItem<>(GameRecord.builder().name(formattedGameName).build());
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
                TreeItem<Object> gameItem = new TreeItem<>(GameRecord.builder().name(formattedGameName).build());
                collectionRoot.getChildren().add(gameItem);
                game_index++;
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot read content of data directory", e);
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
        int activeLineIndex = (gameService.getActiveGame().getCurrentMoveIndex() / 2) + 1;
        String activeLine = gameContentArea.getText(activeLineIndex);
        List<String> lineParts = List.of(activeLine.split(" "));
        int moveStart, moveEnd;
        if (activeSide == Side.WHITE) {
                moveStart = lineParts.get(0).length() + 1;
            moveEnd = moveStart + lineParts.get(1).length() + 1;
        } else {
            moveStart = lineParts.get(0).length() + 1 + lineParts.get(1).length() + 1;
            moveEnd = moveStart + lineParts.get(2).length() + 1;
        }

        gameContentArea.clearStyle(0, gameContentArea.getLength());
        gameContentArea.setStyle(activeLineIndex, 0, lineParts.get(0).length(), "-fx-font-weight: bold;");
        gameContentArea.setStyle(activeLineIndex, moveStart, moveEnd, "-fx-font-weight: bold;");
    }

    private void swapPieceSquare(ObservableList<Node> sourceSquareContent, ObservableList<Node> destinationSquareContent) {
        Node piece = sourceSquareContent.get(0);
        sourceSquareContent.clear();
        destinationSquareContent.clear();
        destinationSquareContent.add(piece);
    }
}