package net.kerosilas.imageviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
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
    private boolean isSlideshowRunning = false;

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
        if(isSlideshowRunning) {
            isSlideshowRunning = false;
            startStopButton.setText("Start");
        } else {
            isSlideshowRunning = true;
            startStopButton.setText("Stop");
        }
    }

    public void initialize() {
        // Create a new thread to run the slideshow.
        // The slideshow is off by default. It will be turned on when the user clicks the start button
        Thread slideshowThread = new Thread(() -> slideshow());

        // Set the thread as a daemon thread
        // This means that the thread will not prevent the application from exiting
        slideshowThread.setDaemon(true);

        // Start the thread
        slideshowThread.start();

        // Add a listener to the slider that will format the value and display it in a label
        slideshowSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            sliderValueLabel.setText(String.format("%ds", newValue.intValue()));
        });
    }

    // This method will display the image at the currentImageIndex.
    // indexChange can be -1, 0 or 1
    // -1 means previous image
    // 0 means current image
    // 1 means next image
    private void displayImage(int indexChange) {
        if (!images.isEmpty()) {
            if (currentImageIndex + indexChange < 0) {
                currentImageIndex = images.size() - 1;
            } else {
                currentImageIndex = (currentImageIndex + indexChange) % images.size();
            }
            imageView.setImage(images.get(currentImageIndex));
        }
    }

    // This method will be run in a separate thread
    private void slideshow() {
        try {
            while (true) {
                // Get the current delay value from the slider. Slider shows in seconds, so it needs to be multiplied by 1000
                int delay = (int) slideshowSpeedSlider.getValue() * 1000;

                // Sleep for that amount of milliseconds
                Thread.sleep(delay);

                // Update the UI from the JavaFX Application thread using runLater()
                Platform.runLater(() -> {
                    if (isSlideshowRunning)
                        displayImage(1);
                });
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}