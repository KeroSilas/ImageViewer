package net.kerosilas.imageviewer;

import java.io.File;
import java.util.List;

import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.enums.ButtonType;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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

public class ImageViewerWindowController {

    @FXML private MFXButton startStopButton, loadButton, listButton, nextButton, previousButton, fullscreenButton;
    @FXML private MFXScrollPane imageScrollPane;
    @FXML private TilePane imageTilePane;
    @FXML private Slider slideshowSpeedSlider;
    @FXML private ImageView imageView;
    @FXML private Label sliderValueLabel, nameLabel, pathLabel, blueCountLabel, greenCountLabel, redCountLabel;
    @FXML private HBox hBoxTop, hBoxBottom, pane;
    @FXML private BorderPane root;

    private ImageManager imageManager;
    private int currentImageIndex = 0;
    private SlideshowTask slideshowTask;

    @FXML private void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (files != null && !files.isEmpty()) {
            imageManager.addImages(files);
            for (Node node : hBoxTop.getChildren()) {
                node.setVisible(true);
                if (node instanceof MFXButton) {
                    ((MFXButton) node).setButtonType(ButtonType.RAISED);
                }
            }
            imageTilePane.getChildren().clear();
            imageTilePane.getChildren().addAll(imageManager.getImagePaneList());
            hBoxBottom.setVisible(true);
            loadButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
            root.setStyle("-fx-background-color: #131313;");
            setupAnimations();
            updateImage();
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

    @FXML private void handleList() {
        if (imageScrollPane.isVisible()) {
            imageScrollPane.setVisible(false);
            listButton.setStyle("-fx-background-color: #ffffff;");
        } else {
            imageScrollPane.setVisible(true);
            listButton.setStyle("-fx-background-color: #d9d9d9;");
        }
    }

    @FXML private void handleImageClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setFullScreen(!stage.isFullScreen());
        }
    }

    @FXML private void handleImageListClick(MouseEvent event) {
        // doesnt work
        if (event.getSource() instanceof HBox hBox) {
            currentImageIndex = imageManager.getIndex(hBox);
            updateImage();
        }
    }

    public void initialize() {
        imageManager = ImageManager.getInstance();

        Platform.runLater(() -> {
            imageView.setFitHeight(imageView.getScene().getHeight());
            imageView.setFitWidth(imageView.getScene().getWidth());
            pane.setTranslateY((imageView.getScene().getHeight()) / 2);
            pane.setTranslateX((imageView.getScene().getWidth()) / 2);
            imageView.getScene().heightProperty().addListener((observable, oldValue, newValue) -> {
                    imageView.setFitHeight((newValue.doubleValue()));
                    pane.setTranslateY((newValue.doubleValue()) / 2);
            });
            imageView.getScene().widthProperty().addListener((observable, oldValue, newValue) -> {
                    imageView.setFitWidth((newValue.doubleValue()));
                    pane.setTranslateX((newValue.doubleValue()) / 2);
            });
        });
        pane.toBack();

        slideshowSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                sliderValueLabel.setText(String.format("%ds", newValue.intValue())));

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
                    startStopButton.fire();
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

    private void displayNextImage() {
        if (!imageManager.getFileList().isEmpty()) {
            if (currentImageIndex == imageManager.getFileList().size() - 1) {
                currentImageIndex = 0;
            } else {
                currentImageIndex++;
            }
            updateImage();
        }
    }

    private void displayPrevImage() {
        if (!imageManager.getFileList().isEmpty()) {
            if (currentImageIndex == 0) {
                currentImageIndex = imageManager.getFileList().size() - 1;
            } else {
                currentImageIndex--;
            }
            updateImage();
        }
    }

    private void startSlideshow() {
        if (slideshowTask == null) {
            slideshowTask = new SlideshowTask(imageManager.getFileList(), (int) slideshowSpeedSlider.getValue(), currentImageIndex);
            slideshowTask.valueProperty().addListener((ov, oldValue, newValue) -> {
                imageView.setImage(newValue);
                File file = new File(newValue.getUrl());
                nameLabel.setText(file.getName().replace("%20", " "));
                pathLabel.setText(file.getParentFile().getPath().substring(6));
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
            startStopButton.setStyle("-fx-background-color: #bd2323; -fx-text-fill: #ffffff;");
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
        imageView.setImage(new Image(imageManager.getFileList().get(currentImageIndex).toURI().toString()));
        File file = new File(imageManager.getFileList().get(currentImageIndex).toURI().toString());
        nameLabel.setText(file.getName().replace("%20", " "));
        pathLabel.setText(file.getParentFile().getPath().substring(6));
        countPixelColors();
    }

    private void countPixelColors() {
        PixelCounterTask pixelCounterTask = new PixelCounterTask(imageManager.getFileList().get(currentImageIndex));
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
        hBoxTop.setTranslateY(-50);
        hBoxBottom.setTranslateY(50);
        imageScrollPane.setTranslateY(-128);

        TranslateTransition ttBottom = new TranslateTransition(Duration.millis(70), hBoxBottom);
        TranslateTransition ttTop = new TranslateTransition(Duration.millis(70), hBoxTop);
        TranslateTransition ttList = new TranslateTransition(Duration.millis(70), imageScrollPane);

        root.setOnMouseEntered(event -> {
            ttTop.setByY(50);
            ttBottom.setByY(-50);
            ttList.setByY(128);
            ttTop.play();
            ttBottom.play();
            ttList.play();

            ttTop.setOnFinished(e ->
                    hBoxTop.setTranslateY(0));
            ttBottom.setOnFinished(e ->
                    hBoxBottom.setTranslateY(0));
            ttList.setOnFinished(e ->
                    imageScrollPane.setTranslateX(0));
        });

        root.setOnMouseExited(event -> {
            ttTop.setByY(-50);
            ttBottom.setByY(50);
            ttList.setByY(-128);
            ttTop.play();
            ttBottom.play();
            ttList.play();

            ttTop.setOnFinished(e ->
                    hBoxTop.setTranslateY(-50));
            ttBottom.setOnFinished(e ->
                    hBoxBottom.setTranslateY(50));
            ttList.setOnFinished(e ->
                    imageScrollPane.setTranslateY(-128));
        });
    }
}