package net.kerosilas.imageviewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
// Ye
public class ImageViewerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ImageViewerApplication.class.getResource("ImageViewerWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Image Viewer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}