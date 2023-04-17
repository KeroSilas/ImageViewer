package net.kerosilas.imageviewer.controller;

import java.io.File;
import java.util.List;

import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.enums.ButtonType;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.util.Duration;
import net.kerosilas.imageviewer.model.*;
import net.kerosilas.imageviewer.tasks.LoadImageTask;
import net.kerosilas.imageviewer.tasks.PixelCounterTask;
import net.kerosilas.imageviewer.tasks.SlideshowTask;

public class ImageViewerWindowController {

    @FXML private MFXButton toggleSlideshowButton, loadButton, listButton, nextButton, previousButton, fullscreenButton;
    @FXML private MFXScrollPane imageScrollPane;
    @FXML private MFXProgressSpinner progressSpinner;
    @FXML private TilePane imageTilePane;
    @FXML private Slider slideshowSpeedSlider;
    @FXML private ImageView imageView;
    @FXML private Label sliderValueLabel, nameLabel, pathLabel, blueCountLabel, greenCountLabel, redCountLabel, mixedCountLabel;
    @FXML private HBox hBoxTop, hBoxBottom, imageHBox;
    @FXML private BorderPane root;

    private ImageManager imageManager;
    private boolean isSlideshowRunning = false;
    private SlideshowTask slideshowTask;

    @FXML private void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        fileChooser.setInitialDirectory(new File("src/main/resources/net/kerosilas/imageviewer/images/")); // For testing purposes
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (files != null && !files.isEmpty()) {
            progressSpinner.setVisible(true);
            loadButton.setDisable(true);
            loadButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

            LoadImageTask imageTask = new LoadImageTask(files, 16); // 16 threads for loading images in parallel (you can change this)
            imageTask.setOnSucceeded(e -> {
                for (Node node : hBoxTop.getChildren()) { // Enable all buttons, only relevant when you load images the first time
                    node.setDisable(false);
                    if (node instanceof MFXButton) {
                        ((MFXButton) node).setButtonType(ButtonType.RAISED);
                    }
                }
                loadButton.setStyle("-fx-background-color:  #30a14f; -fx-text-fill: #ffffff;"); // Set the load button to green
                root.setStyle("-fx-background-color: #131313;"); // Set the background to black
                hBoxBottom.setVisible(true);
                progressSpinner.setVisible(false);

                imageTilePane.getChildren().setAll(imageManager.getImagePaneList()); // For the custom list view of images
                updateImage();
            });
            new Thread(imageTask).start();
        }
    }

    @FXML private void handlePrevious() {
        imageManager.previousImage();
    }

    @FXML private void handleNext() {
        imageManager.nextImage();
    }

    @FXML private void handleToggleSlideshow() {
        toggleSlideshow();
    }

    @FXML private void handleFullscreen() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML private void handleList() { // Toggle the list view
        if (imageScrollPane.isVisible()) {
            imageScrollPane.setVisible(false);
            listButton.setStyle("-fx-background-color: #ffffff;");
        } else {
            imageScrollPane.setVisible(true);
            listButton.setStyle("-fx-background-color: #d9d9d9;");
        }
    }

    @FXML private void handleImageClick(MouseEvent e) {
        if (e.getClickCount() == 2) {
            fullscreenButton.fire(); // Double click to toggle fullscreen
        }
    }

    public void initialize() {
        imageManager = ImageManager.getInstance();
        imageManager.addPropertyChangeListener(e -> { // Listen for changes in the current index
            if (e.getPropertyName().equals("currentIndex"))
                updateImage();
        });

        slideshowSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            sliderValueLabel.setText(String.format("%ds", newValue.intValue())); // Update the slider value label
            if (isSlideshowRunning)
                slideshowTask.setDelay(newValue.intValue()); // Update the delay of the slideshow task
        });

        imageTilePane.setOnMouseClicked(e -> {
            int index = imageTilePane.getChildren().indexOf(e.getTarget());
            if(index != -1) { // If index is -1 then the click was not on an image
                imageManager.setCurrentIndex(index);
            }
        });

        Platform.runLater(() -> {
            // Scales the image to fit the window
            imageView.fitWidthProperty().bind(imageView.getScene().widthProperty());
            imageView.fitHeightProperty().bind(imageView.getScene().heightProperty());
            // Centers the image (necessary when imageHBox is sent to back)
            imageHBox.translateXProperty().bind(imageView.getScene().widthProperty().divide(2));
            imageHBox.translateYProperty().bind(imageView.getScene().heightProperty().divide(2));

            initializeAnimations();
            initializeKeybindings();
        });
        imageHBox.toBack(); // The top and bottom hBoxes will now be on top of the image
    }

    private void toggleSlideshow() {
        if (!isSlideshowRunning) {
            slideshowTask = new SlideshowTask((int) slideshowSpeedSlider.getValue());
            Thread thread = new Thread(slideshowTask);
            thread.setDaemon(true);
            thread.start();

            toggleSlideshowButton.setText("Stop slideshow");
            toggleSlideshowButton.setStyle("-fx-background-color: #bd2323; -fx-text-fill: #ffffff;"); // Set the button to red
            isSlideshowRunning = true;
        } else {
            slideshowTask.cancel();

            toggleSlideshowButton.setText("Start slideshow");
            toggleSlideshowButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;"); // Set the button to white
            isSlideshowRunning = false;
        }
    }

    private void updateImage() {
        new Thread(() ->
                imageView.setImage(imageManager.getCurrentImage())).start(); // Load the image in a new thread to prevent the UI from freezing when loading large images
        File file = imageManager.getCurrentFile();
        Platform.runLater(() -> { // Update the labels in the UI thread to prevent errors
            nameLabel.setText(file.getName().replace("%20", " "));
            pathLabel.setText(file.getParentFile().getPath());
            countPixelColors();
        });
    }

    private void countPixelColors() {
        PixelCounterTask pixelCounterTask = new PixelCounterTask();
        pixelCounterTask.setOnSucceeded(e -> {
            redCountLabel.setText(String.format("%d (%.2f%%)",
                    pixelCounterTask.getValue().redCount(),
                    pixelCounterTask.getValue().getRedPercentage() * 100));
            greenCountLabel.setText(String.format("%d (%.2f%%)",
                    pixelCounterTask.getValue().greenCount(),
                    pixelCounterTask.getValue().getGreenPercentage() * 100));
            blueCountLabel.setText(String.format("%d (%.2f%%)",
                    pixelCounterTask.getValue().blueCount(),
                    pixelCounterTask.getValue().getBluePercentage() * 100));
            mixedCountLabel.setText(String.format("%d (%.2f%%)",
                    pixelCounterTask.getValue().mixedCount(),
                    pixelCounterTask.getValue().getMixedPercentage() * 100));
        });
        Thread thread = new Thread(pixelCounterTask);
        thread.setDaemon(true);
        thread.start();
    }

    // Animations for the top and bottom bars
    private void initializeAnimations() {
        TranslateTransition ttBottom = new TranslateTransition(Duration.millis(70), hBoxBottom);
        TranslateTransition ttTop = new TranslateTransition(Duration.millis(70), hBoxTop);
        TranslateTransition ttList = new TranslateTransition(Duration.millis(70), imageScrollPane);
        PauseTransition pause = new PauseTransition(Duration.millis(3000));

        pause.play();
        pause.setOnFinished(e -> { // If the mouse is not moved for 3 seconds, hide the bars
            ttTop.setByY(-50);
            ttBottom.setByY(50);
            ttList.setByY(-128);
            ttTop.play();
            ttBottom.play();
            ttList.play();
        });

        root.getScene().setOnMouseMoved(e -> { // If the mouse moves, show the bars
            if (imageScrollPane.getTranslateY() != 0) {
                ttTop.setByY(50);
                ttBottom.setByY(-50);
                ttList.setByY(128);
                ttTop.play();
                ttBottom.play();
                ttList.play();
            }
            pause.playFromStart();
        });

        root.getScene().setOnMouseExited(e -> { // If the mouse leaves the window, hide the bars
            if (imageScrollPane.getTranslateY() == 0) {
                ttTop.setByY(-50);
                ttBottom.setByY(50);
                ttList.setByY(-128);
                ttTop.play();
                ttBottom.play();
                ttList.play();
            }
            pause.playFromStart();
        });

        slideshowSpeedSlider.setOnMouseDragged(e -> pause.playFromStart()); // If the user is dragging the slider, don't hide the bars

        for (Node node : hBoxTop.getChildren()) { // If the mouse is over a button, don't hide the bars
            node.setOnMouseEntered(event -> pause.stop());
            node.setOnMouseExited(event -> pause.playFromStart());
        }
    }

    private void initializeKeybindings() {
        root.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case D -> {
                    nextButton.fire();
                    e.consume();
                }
                case A -> {
                    previousButton.fire();
                    e.consume();
                }
                case S -> {
                    toggleSlideshowButton.fire();
                    e.consume();
                }
                case F -> {
                    fullscreenButton.fire();
                    e.consume();
                }
                case L -> {
                    listButton.fire();
                    e.consume();
                }
            }
        });
    }
}