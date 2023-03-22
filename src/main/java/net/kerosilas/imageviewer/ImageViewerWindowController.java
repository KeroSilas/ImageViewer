package net.kerosilas.imageviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class ImageViewerWindowController {

    @FXML private Button startStopButton;
    @FXML private Slider slideshowSpeedSlider;
    @FXML private ImageView imageView;
    @FXML private Label sliderValueLabel;

    private final List<Image> images = new ArrayList<>();
    private int currentImageIndex = 0;
    private SlideshowRunnable slideshowRunnable;

    @FXML private void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        try {
            if (!files.isEmpty()) {
                files.forEach((File f) -> images.add(new Image(f.toURI().toString())));
                displayImage(0);
            }
        } catch (NullPointerException e) {
            System.out.println("No files selected");
        }
    }

    @FXML private void handlePrevious() {
        displayImage(-1);
    }

    @FXML private void handleNext() {
        displayImage(1);
    }

    @FXML private void handleStartStopSlideshow() {
        if(slideshowRunnable.isSlideshowRunning()) {
            slideshowRunnable.setSlideshowRunning(false);
            startStopButton.setText("Start");
        } else {
            slideshowRunnable.setSlideshowRunning(true);
            startStopButton.setText("Stop");
        }
    }

    public void initialize() {
        slideshowRunnable = new SlideshowRunnable((int) slideshowSpeedSlider.getValue(), false, this); // The slideshow is off by default. It will be turned on when the user clicks the start button
        Thread slideshowThread = new Thread(slideshowRunnable);
        slideshowThread.setDaemon(true); // This means that the thread will not prevent the application from exiting
        slideshowThread.start();

        slideshowSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> { // Add a listener to the slider that will format the value and display it in a label
            sliderValueLabel.setText(String.format("%ds", newValue.intValue()));
            slideshowRunnable.setSliderValue(newValue.intValue()); // Update the slideshowRunnable object with the new slider value
        });
    }

    // This method will display the image at the currentImageIndex.
    // indexChange can be -1, 0 or 1
    // -1 means previous image
    // 0 means current image
    // 1 means next image
    void displayImage(int indexChange) {
        if (!images.isEmpty()) {
            if (currentImageIndex + indexChange < 0) {
                currentImageIndex = images.size() - 1;
            } else {
                currentImageIndex = (currentImageIndex + indexChange) % images.size();
            }
            imageView.setImage(images.get(currentImageIndex));
        }
    }
}