package net.kerosilas.imageviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.palexdev.materialfx.enums.ButtonType;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.util.Duration;

public class ImageViewerWindowController {

    @FXML private MFXButton startStopButton, previousButton, nextButton;
    @FXML private Slider slideshowSpeedSlider;
    @FXML private ImageView imageView;
    @FXML private Label sliderValueLabel, nameLabel, pathLabel, blueCountLabel, greenCountLabel, redCountLabel;
    @FXML private HBox hBoxTop, hBoxBottom, pane;
    @FXML private BorderPane root;

    private final List<File> imageFiles = new ArrayList<>();
    private int currentImageIndex = 0;
    private SlideshowTask slideshowTask;

    @FXML private void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (files != null && !files.isEmpty()) {
            imageFiles.addAll(files);
            updateImage();
            startStopButton.setDisable(false);
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            startStopButton.setButtonType(ButtonType.RAISED);
            previousButton.setButtonType(ButtonType.RAISED);
            nextButton.setButtonType(ButtonType.RAISED);
            hBoxBottom.setVisible(true);
            setupAnimations();
        }
    }

    @FXML private void handlePrevious() {
        stopSlideshow();
        displayPrevImage();
    }

    @FXML private void handleNext() {
        stopSlideshow();
        displayNextImage();
    }

    @FXML private void handleStartStopSlideshow() {
        if (slideshowTask == null) {
            startSlideshow();
        } else {
            stopSlideshow();
        }
    }

    @FXML private void handleFullscreen() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    public void initialize() {
        // Add a listener to the window size that will resize the image to fit the window
        Platform.runLater(() -> {
            imageView.setFitHeight(imageView.getScene().getWindow().getHeight() - 39);
            imageView.setFitWidth(imageView.getScene().getWindow().getWidth() - 16);
            pane.setTranslateY((imageView.getScene().getWindow().getHeight() - 39) / 2);
            pane.setTranslateX((imageView.getScene().getWindow().getWidth() - 16) / 2);
            imageView.getScene().getWindow().heightProperty().addListener((observable, oldValue, newValue) -> {
                    imageView.setFitHeight((newValue.doubleValue() - 39));
                    pane.setTranslateY((newValue.doubleValue() - 39) / 2);
            });
            imageView.getScene().getWindow().widthProperty().addListener((observable, oldValue, newValue) -> {
                    imageView.setFitWidth((newValue.doubleValue() - 16));
                    pane.setTranslateX((newValue.doubleValue() - 16) / 2);
            });
        });
        pane.toBack();

        slideshowSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> { // Add a listener to the slider that will format the value and display it in a label
            sliderValueLabel.setText(String.format("%ds", newValue.intValue()));
        });
    }

    private void displayNextImage() {
        if (!imageFiles.isEmpty()) {
            if (currentImageIndex == imageFiles.size() - 1) {
                currentImageIndex = 0;
            } else {
                currentImageIndex++;
            }
            updateImage();
        }
    }

    private void displayPrevImage() {
        if (!imageFiles.isEmpty()) {
            if (currentImageIndex == 0) {
                currentImageIndex = imageFiles.size() - 1;
            } else {
                currentImageIndex--;
            }
            updateImage();
        }
    }

    private void startSlideshow() {
        if (slideshowTask == null) {
            slideshowTask = new SlideshowTask(imageFiles, (int) slideshowSpeedSlider.getValue(), currentImageIndex);
            slideshowTask.valueProperty().addListener((ov, oldValue, newValue) -> {
                imageView.setImage(newValue);
                File file = new File(newValue.getUrl());
                nameLabel.setText(String.format("%s", file.getName().replace("%20", " ")));
                pathLabel.setText(String.format("%s", file.getParentFile().getPath().substring(6)));
                if (newValue != oldValue) {
                    currentImageIndex = slideshowTask.getIndex();
                    countPixelColors();
                }
            });
            Thread thread = new Thread(slideshowTask);
            thread.setDaemon(true);
            thread.start();

            slideshowSpeedSlider.setDisable(true);
            startStopButton.setText("Stop slideshow");
            startStopButton.setStyle("-fx-background-color: #cc0000; -fx-text-fill: #ffffff;");
        }
    }

    private void stopSlideshow() {
        if (slideshowTask != null) {
            currentImageIndex = slideshowTask.getIndex();
            slideshowTask.cancel();
            slideshowTask = null;

            slideshowSpeedSlider.setDisable(false);
            startStopButton.setText("Start slideshow");
            startStopButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
        }
    }

    private void updateImage() {
        imageView.setImage(new Image(imageFiles.get(currentImageIndex).toURI().toString()));
        File file = new File(imageFiles.get(currentImageIndex).toURI().toString());
        nameLabel.setText(String.format("%s", file.getName().replace("%20", " ")));
        pathLabel.setText(String.format("%s", file.getParentFile().getPath().substring(6)));
        countPixelColors();
    }

    private void countPixelColors() {
        PixelCounterTask pixelCounterTask = new PixelCounterTask(imageFiles.get(currentImageIndex));
        pixelCounterTask.valueProperty().addListener((ov, oldValue, newValue) -> {
            redCountLabel.setText(String.format("%d (%.2f%%)", newValue.getRedCount(), newValue.getRedPercentage() * 100));
            greenCountLabel.setText(String.format("%d (%.2f%%)", newValue.getGreenCount(), newValue.getGreenPercentage() * 100));
            blueCountLabel.setText(String.format("%d (%.2f%%)", newValue.getBlueCount(), newValue.getBluePercentage() * 100));
        });
        Thread thread = new Thread(pixelCounterTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void setupAnimations() {
        // Setups the animation for the top and bottom HBoxes
        hBoxTop.setTranslateY(-55);
        hBoxBottom.setTranslateY(55);

        TranslateTransition ttBottom = new TranslateTransition(Duration.millis(100), hBoxBottom);
        TranslateTransition ttTop = new TranslateTransition(Duration.millis(100), hBoxTop);

        root.setOnMouseEntered(event -> {
            ttTop.setByY(55);
            ttTop.setCycleCount(1);
            ttTop.setAutoReverse(false);
            ttTop.setDelay(Duration.millis(0));
            ttBottom.setByY(-55);
            ttBottom.setCycleCount(1);
            ttBottom.setAutoReverse(false);
            ttBottom.setDelay(Duration.millis(0));
            ttTop.play();
            ttBottom.play();

            ttTop.setOnFinished(e ->
                    hBoxTop.setTranslateY(0));
            ttBottom.setOnFinished(e ->
                    hBoxBottom.setTranslateY(0));
        });

        root.setOnMouseExited(event -> {
            ttTop.setByY(-55);
            ttTop.setCycleCount(1);
            ttTop.setAutoReverse(false);
            ttTop.setDelay(Duration.millis(1100));
            ttBottom.setByY(55);
            ttBottom.setCycleCount(1);
            ttBottom.setAutoReverse(false);
            ttBottom.setDelay(Duration.millis(1100));
            ttTop.play();
            ttBottom.play();

            ttTop.setOnFinished(e ->
                    hBoxTop.setTranslateY(-55));
            ttBottom.setOnFinished(e ->
                    hBoxBottom.setTranslateY(55));
        });
    }
}