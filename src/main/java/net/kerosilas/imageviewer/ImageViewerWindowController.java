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
import javafx.scene.image.PixelReader;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class ImageViewerWindowController {

    @FXML private Button startStopButton, previousButton, nextButton;
    @FXML private Slider slideshowSpeedSlider;
    @FXML private ImageView imageView;
    @FXML private Label sliderValueLabel;
    @FXML private TilePane imagePane;

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
            displayCurrentImage();
            startStopButton.setDisable(false);
            previousButton.setDisable(false);
            nextButton.setDisable(false);
        }
    }

    @FXML private void handlePrevious() {
        stopSlideshow();
        displayPrevImage();

        printPixelColor();
    }

    @FXML private void handleNext() {
        stopSlideshow();
        displayNextImage();

        printPixelColor();
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
        if (!imageFiles.isEmpty()) {
            imageView.setImage(new Image(imageFiles.get(currentImageIndex).toURI().toString()));
            System.out.println(imageFiles.get(currentImageIndex).getName().replace("%20", " "));
        }
    }

    void displayNextImage() {
        if (!imageFiles.isEmpty()) {
            if (currentImageIndex == imageFiles.size() - 1) {
                currentImageIndex = 0;
            } else {
                currentImageIndex++;
            }
            imageView.setImage(new Image(imageFiles.get(currentImageIndex).toURI().toString()));
            System.out.println(imageFiles.get(currentImageIndex).getName().replace("%20", " "));
        }
    }

    void displayPrevImage() {
        if (!imageFiles.isEmpty()) {
            if (currentImageIndex == 0) {
                currentImageIndex = imageFiles.size() - 1;
            } else {
                currentImageIndex--;
            }
            imageView.setImage(new Image(imageFiles.get(currentImageIndex).toURI().toString()));
            System.out.println(imageFiles.get(currentImageIndex).getName().replace("%20", " "));
        }
    }

    private void startSlideshow() {
        if (slideshowTask == null) {
            slideshowTask = new SlideshowTask(imageFiles, (int) slideshowSpeedSlider.getValue(), currentImageIndex);
            slideshowTask.valueProperty().addListener((ov, oldValue, newValue) -> {
                imageView.setImage(newValue);
                File file = new File(newValue.getUrl()); // Get the current image file
                System.out.println(file.getName().replace("%20", " ")); // Print the name of the current image
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

    private void printPixelColor() {
        Image image = new Image(imageFiles.get(currentImageIndex).toURI().toString());
        PixelReader pixelReader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        int redCount = 0;
        int greenCount = 0;
        int blueCount = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = pixelReader.getColor(x, y);

                if (color.getRed() > color.getGreen() && color.getRed() > color.getBlue()) {
                    redCount++;
                } else if (color.getGreen() > color.getRed() && color.getGreen() > color.getBlue()) {
                    greenCount++;
                } else if (color.getBlue() > color.getRed() && color.getBlue() > color.getGreen()) {
                    blueCount++;
                }
            }
        }

        System.out.println("Red pixels: " + redCount);
        System.out.println("Green pixels: " + greenCount);
        System.out.println("Blue pixels: " + blueCount);
    }
}