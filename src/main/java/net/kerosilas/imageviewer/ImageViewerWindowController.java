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
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class ImageViewerWindowController {

    @FXML private Button startStopButton, previousButton, nextButton;
    @FXML private Slider slideshowSpeedSlider;
    @FXML private ImageView imageView;
    @FXML private Label sliderValueLabel;
    @FXML private TilePane imagePane;

    private final List<Image> images = new ArrayList<>();
    private final List<File> imageFiles = new ArrayList<>();
    private int currentImageIndex = 0;
    private SlideshowTask slideshowTask;

    @FXML private void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        try {
            imageFiles.addAll(fileChooser.showOpenMultipleDialog(new Stage()));
        } catch (NullPointerException e) {
            System.out.println("No files selected");
        }

        if (!imageFiles.isEmpty()) {
            imageFiles.forEach((File f) -> images.add(new Image(f.toURI().toString())));
            displayCurrentImage();
            startStopButton.setDisable(false);
            previousButton.setDisable(false);
            nextButton.setDisable(false);
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
                imageView.setFitWidth(newValue.doubleValue() - 16);
            });
        });

        slideshowSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> { // Add a listener to the slider that will format the value and display it in a label
            sliderValueLabel.setText(String.format("%ds", newValue.intValue()));
        });
    }

    void displayCurrentImage() {
        if (!images.isEmpty()) {
            imageView.setImage(images.get(currentImageIndex));
        }
    }

    void displayNextImage() {
        if (!images.isEmpty()) {
            if (currentImageIndex == images.size() - 1) {
                currentImageIndex = 0;
            } else {
                currentImageIndex++;
            }
            imageView.setImage(images.get(currentImageIndex));
        }
    }

    void displayPrevImage() {
        if (!images.isEmpty()) {
            if (currentImageIndex == 0) {
                currentImageIndex = images.size() - 1;
            } else {
                currentImageIndex--;
            }
            imageView.setImage(images.get(currentImageIndex));
        }
    }

    private void startSlideshow() {
        if (slideshowTask == null) {
            slideshowTask = new SlideshowTask(imageFiles, (int) slideshowSpeedSlider.getValue(), currentImageIndex);
            slideshowTask.valueProperty().addListener((ov, oldValue, newValue) -> {
                imageView.setImage(newValue);
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
            slideshowTask.valueProperty().removeListener((ov, oldValue, newValue) -> {
                imageView.setImage(newValue);
            });
            slideshowTask = null;

            slideshowSpeedSlider.setDisable(false);
            startStopButton.setText("Start");
        }
    }
}