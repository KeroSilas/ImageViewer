package net.kerosilas.imageviewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
// Ye with some spice
public class ImageViewerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ImageViewerApplication.class.getResource("views/ImageViewerWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Image Viewer");
        stage.getIcons().add(new Image("file:src/main/resources/net/kerosilas/imageviewer/icons/logo.png"));
        stage.setMinWidth(740);
        stage.setMinHeight(240);
        stage.setWidth(1080);
        stage.setHeight(638);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}