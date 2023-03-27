package net.kerosilas.imageviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class ImageViewerWindowController {

    @FXML private Button startStopButton, previousButton, nextButton;
    @FXML private Slider slideshowSpeedSlider;
    @FXML private ImageView imageView;
    @FXML private Label sliderValueLabel, nameLabel, pathLabel, blueCountLabel, greenCountLabel, redCountLabel, totalCountLabel;
    @FXML private TilePane imagePane;
    @FXML private HBox infoPane;

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
            infoPane.setVisible(true);
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

    public void initialize() {
        imagePane.setAlignment(Pos.CENTER);

        // Add a listener to the window size that will resize the image to fit the window
        Platform.runLater(() -> {
            imageView.getScene().getWindow().heightProperty().addListener((observable, oldValue, newValue) -> {
                imageView.setFitHeight(newValue.doubleValue() - 86);
            });
            imageView.getScene().getWindow().widthProperty().addListener((observable, oldValue, newValue) -> {
                imageView.setFitWidth(newValue.doubleValue() - 265);
            });
        });

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
                nameLabel.setText(String.format("Name: %s", file.getName().replace("%20", " ")));
                pathLabel.setText(String.format("Path: %s", file.getPath()));
                if (newValue != oldValue) {
                    currentImageIndex = slideshowTask.getIndex();
                    countPixelColors();
                }
            });
            Thread thread = new Thread(slideshowTask);
            thread.setDaemon(true);
            thread.start();

            slideshowSpeedSlider.setDisable(true);
            startStopButton.setText("Stop");
        }
    }

    private void stopSlideshow() {
        if (slideshowTask != null) {
            currentImageIndex = slideshowTask.getIndex();
            slideshowTask.cancel();
            slideshowTask = null;

            slideshowSpeedSlider.setDisable(false);
            startStopButton.setText("Start");
        }
    }

    private void updateImage() {
        imageView.setImage(new Image(imageFiles.get(currentImageIndex).toURI().toString()));
        File file = new File(imageFiles.get(currentImageIndex).toURI().toString());
        nameLabel.setText(String.format("Name: %s", file.getName().replace("%20", " ")));
        pathLabel.setText(String.format("Path: %s", file.getAbsolutePath()));
        countPixelColors();
    }

    private void countPixelColors() {
        PixelCounterTask pixelCounterTask = new PixelCounterTask(imageFiles.get(currentImageIndex));
        pixelCounterTask.valueProperty().addListener((ov, oldValue, newValue) -> {
            redCountLabel.setText(String.format("Red pixel count: %d (%.2f%%)", newValue.getRedCount(), newValue.getRedPercentage() * 100));
            greenCountLabel.setText(String.format("Green pixel count: %d (%.2f%%)", newValue.getGreenCount(), newValue.getGreenPercentage() * 100));
            blueCountLabel.setText(String.format("Blue pixel count: %d (%.2f%%)", newValue.getBlueCount(), newValue.getBluePercentage() * 100));
            totalCountLabel.setText(String.format("Total pixel count: %d", newValue.getTotalCount()));
        });
        Thread thread = new Thread(pixelCounterTask);
        thread.setDaemon(true);
        thread.start();
    }
}