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

public class ImageViewerWindowController {

    @FXML private MFXButton toggleSlideshowButton, loadButton, listButton, nextButton, previousButton, fullscreenButton;
    @FXML private MFXScrollPane imageScrollPane;
    @FXML private MFXProgressSpinner progressSpinner;
    @FXML private TilePane imageTilePane;
    @FXML private Slider slideshowSpeedSlider;
    @FXML private ImageView imageView;
    @FXML private Label sliderValueLabel, nameLabel, pathLabel, blueCountLabel, greenCountLabel, redCountLabel;
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
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (files != null && !files.isEmpty()) {
            progressSpinner.setVisible(true);
            loadButton.setDisable(true);
            loadButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

            ImageTask imageTask = new ImageTask(files, 10);
            imageTask.setOnSucceeded(e -> {
                for (Node node : hBoxTop.getChildren()) {
                    node.setDisable(false);
                    if (node instanceof MFXButton) {
                        ((MFXButton) node).setButtonType(ButtonType.RAISED);
                    }
                }
                loadButton.setStyle("-fx-background-color:  #30a14f; -fx-text-fill: #ffffff;");
                root.setStyle("-fx-background-color: #131313;");
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

    @FXML private void handleList() {
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
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setFullScreen(!stage.isFullScreen());
        }
    }

    public void initialize() {
        imageManager = ImageManager.getInstance();
        imageManager.addPropertyChangeListener(e -> updateImage());

        slideshowSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            sliderValueLabel.setText(String.format("%ds", newValue.intValue()));
            if (isSlideshowRunning)
                slideshowTask.setDelay(newValue.intValue());
        });

        imageTilePane.setOnMouseClicked(e -> {
            int index = imageTilePane.getChildren().indexOf(e.getTarget ());
            if(index != -1) {
                imageManager.setCurrentIndex(index);
                updateImage();
            }
        });

        Platform.runLater(() -> {
            imageView.fitWidthProperty().bind(imageView.getScene().widthProperty());
            imageView.fitHeightProperty().bind(imageView.getScene().heightProperty());
            imageHBox.translateXProperty().bind(imageView.getScene().widthProperty().divide(2));
            imageHBox.translateYProperty().bind(imageView.getScene().heightProperty().divide(2));

            initializeAnimations();
            initializeKeybindings();
        });
        imageHBox.toBack();
    }

    private void toggleSlideshow() {
        if (!isSlideshowRunning) {
            slideshowTask = new SlideshowTask((int) slideshowSpeedSlider.getValue());
            Thread thread = new Thread(slideshowTask);
            thread.setDaemon(true);
            thread.start();

            toggleSlideshowButton.setText("Stop slideshow");
            toggleSlideshowButton.setStyle("-fx-background-color: #bd2323; -fx-text-fill: #ffffff;");
            isSlideshowRunning = true;
        } else {
            slideshowTask.cancel();

            toggleSlideshowButton.setText("Start slideshow");
            toggleSlideshowButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
            isSlideshowRunning = false;
        }
    }

    private void updateImage() {
        new Thread(() -> {
            imageView.setImage(imageManager.getCurrentImage());
        }).start();
        File file = imageManager.getCurrentFile();
        Platform.runLater(() -> {
            nameLabel.setText(file.getName().replace("%20", " "));
            pathLabel.setText(file.getParentFile().getPath());
            countPixelColors();
        });
    }

    private void countPixelColors() {
        PixelCounterTask pixelCounterTask = new PixelCounterTask();
        pixelCounterTask.setOnSucceeded(e -> {
            redCountLabel.setText(String.format("%d (%.2f%%)", pixelCounterTask.getValue().redCount(), pixelCounterTask.getValue().getRedPercentage() * 100));
            greenCountLabel.setText(String.format("%d (%.2f%%)", pixelCounterTask.getValue().greenCount(), pixelCounterTask.getValue().getGreenPercentage() * 100));
            blueCountLabel.setText(String.format("%d (%.2f%%)", pixelCounterTask.getValue().blueCount(), pixelCounterTask.getValue().getBluePercentage() * 100));
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
        pause.setOnFinished(e -> {
            ttTop.setByY(-50);
            ttBottom.setByY(50);
            ttList.setByY(-128);
            ttTop.play();
            ttBottom.play();
            ttList.play();

            pause.playFromStart();
        });

        root.getScene().setOnMouseMoved(e -> {
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

        root.getScene().setOnMouseEntered(e -> {
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

        root.getScene().setOnMouseExited(e -> {
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

        slideshowSpeedSlider.setOnMouseDragged(e -> {
            pause.playFromStart();
        });

        for (Node node : hBoxTop.getChildren()) {
            node.setOnMouseEntered(event -> {
                pause.stop();
            });
            node.setOnMouseExited(event -> {
                pause.playFromStart();
            });
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