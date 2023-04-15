package net.kerosilas.imageviewer.model;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.io.File;

// Creates a HBox with an ImageView inside it to be used in the imagePaneList of the ImageManager class
// This list is used to display the images in a row at the top of the application

public class ImagePane {

    private final HBox hBox;

    public ImagePane(File file) {
        hBox = new HBox();
        hBox.setPrefSize(50, 50);
        hBox.setPadding(new Insets(4, 4, 4, 4));
        hBox.setStyle("-fx-background-radius: 5; " +
                "-fx-background-color: rgba(0,0,0,0.05);");
        hBox.setAlignment(Pos.CENTER);
        hBox.setCursor(Cursor.HAND);

        ImageView imageView = new ImageView();
        Image image = new Image(file.toURI().toString(), 38, 38, true, true); // Better to define these values for the image itself rather than the ImageView, saves memory and processing power
        imageView.setImage(image);
        imageView.setMouseTransparent(true);

        hBox.getChildren().add(imageView);

        initializeAnimations();
    }

    public HBox getHBox() {
        return hBox;
    }

    private void initializeAnimations() {
        hBox.setOnMouseEntered(e ->
                hBox.setStyle("-fx-background-radius: 5; " +
                              "-fx-background-color: rgba(0,0,0,0.1);"));
        hBox.setOnMouseExited(e ->
                hBox.setStyle("-fx-background-radius: 5; " +
                              "-fx-background-color: rgba(0,0,0,0.05);"));
        hBox.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(70), hBox);
            st.setToX(0.9);
            st.setToY(0.9);
            st.setCycleCount(2);
            st.setAutoReverse(true); // Makes the animation go back to the original size after it finishes
            st.play();
        });
    }
}
