package com.github.adrienave.booktherook;

import com.github.adrienave.booktherook.controller.CollectionController;
import com.github.adrienave.booktherook.persistence.FileSystemManager;
import com.github.adrienave.booktherook.service.GameService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class BookTheRook extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("collection-view.fxml"));
        CollectionController controller = new CollectionController(new FileSystemManager(), new GameService());
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load());
        scene.setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case ENTER: {
                    controller.createFolder();
                    break;
                }
                case ESCAPE: {
                    stage.close();
                }
            }
        });
        scene.setOnScroll(scrollEvent -> {
            if (scrollEvent.getDeltaY() != 0) {
                controller.changeActiveMove(scrollEvent.getDeltaY() < 0);
            }
        });
        scene.getStylesheets().add(Objects.requireNonNull(this.getClass().getResource("collection-view.css")).toExternalForm());
        stage.setTitle("Book The Rook - Chess Game Collection Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}