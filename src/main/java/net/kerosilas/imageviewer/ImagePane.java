package net.kerosilas.imageviewer;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.io.File;

public class ImagePane {

    private final HBox hBox;
    private final ImageView imageView;
    private final File file;

    public ImagePane(File file) {
        hBox = new HBox();
        hBox.setPrefSize(50, 50);
        hBox.setPadding(new Insets(4, 4, 4, 4));
        hBox.setStyle("-fx-background-radius: 5; " +
                "-fx-background-color: rgba(0,0,0,0.05);");
        hBox.setAlignment(Pos.CENTER);
        imageView = new ImageView();
        imageView.setFitWidth(38);
        imageView.setFitHeight(38);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setImage(new Image(file.toURI().toString()));
        hBox.getChildren().add(imageView);

        setAnimation();

        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public HBox getHBox() {
        return hBox;
    }

    public ImageView getImageView() {
        return imageView;
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
