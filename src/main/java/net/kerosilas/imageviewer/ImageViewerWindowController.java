package net.kerosilas.imageviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class ImageViewerWindowController
{
    @FXML
    private Slider slideshowSpeedSlider;

    @FXML
    private TextField slideshowSpeedTextField;

    private final List<Image> images = new ArrayList<>();
    private int currentImageIndex = 0;
    ExecutorService es = Executors.newFixedThreadPool(5);
    private boolean isSlideshowRunning = false;
    private int delay = 1000;
    private final Thread slideshowThread = new Thread(() -> slideshow(delay));

    @FXML
    private ImageView imageView;

    @FXML
    private void handleBtnLoadAction()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (!files.isEmpty())
        {
            files.forEach((File f) ->
            {
                images.add(new Image(f.toURI().toString()));
            });
            displayImage();
        }
    }

    @FXML
    private void handleBtnPreviousAction()
    {
        if (!images.isEmpty())
        {
            currentImageIndex =
                    (currentImageIndex - 1 + images.size()) % images.size();
            displayImage();
        }
    }

    @FXML
    private void handleBtnNextAction()
    {
        if (!images.isEmpty())
        {
            currentImageIndex = (currentImageIndex + 1) % images.size();
            displayImage();
        }
    }

    @FXML
    private void handleStartSlideshow() {
        //es.submit(() -> slideshow(delay));
        if (!isSlideshowRunning)
            slideshowThread.start();
        isSlideshowRunning = true;
    }

    @FXML
    private void handleStopSlideshow() {
        //slideshowThread = null;
        //es.shutdownNow();
        isSlideshowRunning = false;
    }

    public void initialize() {
        slideshowSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // change the speed of the slideshow
            if (isSlideshowRunning) {
                handleStopSlideshow();
                delay = newValue.intValue() * 1000;
                System.out.println(delay);
                handleStartSlideshow();
            }
        });

        slideshowSpeedTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // change the speed of the slideshow
            if (isSlideshowRunning) {
                handleStopSlideshow();
                delay = Integer.parseInt(newValue) * 1000;
                System.out.println(delay);
                handleStartSlideshow();
            }
        });
    }

    private void displayImage()
    {
        if (!images.isEmpty())
        {
            imageView.setImage(images.get(currentImageIndex));
        }
    }

    private void slideshow(int delay) {
        if (!images.isEmpty()) {
            displayImage();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.out.println("Slideshow interrupted");
            }
            currentImageIndex = (currentImageIndex + 1) % images.size();
            if (isSlideshowRunning)
                slideshow(delay);
        }
    }
}