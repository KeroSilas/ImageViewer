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

        setAnimation();
    }

    public HBox getHBox() {
        return hBox;
    }

    private void setAnimation() {
        hBox.setOnMouseEntered(event -> {
            hBox.setStyle("-fx-background-radius: 5; " +
                          "-fx-background-color: rgba(0,0,0,0.1);");
        });
        hBox.setOnMouseExited(event -> {
            hBox.setStyle("-fx-background-radius: 5; " +
                          "-fx-background-color: rgba(0,0,0,0.05);");
        });
        hBox.setOnMousePressed(event -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(70), hBox);
            st.setToX(0.9);
            st.setToY(0.9);
            st.setCycleCount(2);
            st.setAutoReverse(true);
            st.play();
        });
    }
}
