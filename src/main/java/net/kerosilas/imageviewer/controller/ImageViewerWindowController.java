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
import net.kerosilas.imageviewer.model.ImageManager;
import net.kerosilas.imageviewer.model.ImageTask;
import net.kerosilas.imageviewer.model.PixelCounterTask;
import net.kerosilas.imageviewer.model.SlideshowTask;

public class ImageViewerWindowController {

    @FXML private MFXButton startStopButton, loadButton, listButton, nextButton, previousButton, fullscreenButton;
    @FXML private MFXScrollPane imageScrollPane;
    @FXML private MFXProgressSpinner progressSpinner;
    @FXML private TilePane imageTilePane;
    @FXML private Slider slideshowSpeedSlider;
    @FXML private ImageView imageView;
    @FXML private Label sliderValueLabel, nameLabel, pathLabel, blueCountLabel, greenCountLabel, redCountLabel;
    @FXML private HBox hBoxTop, hBoxBottom, pane;
    @FXML private BorderPane root;

    private ImageManager imageManager;
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
            progressSpinner.progressProperty().bind(imageTask.progressProperty());
            imageTask.setOnSucceeded(e -> {
                for (Node node : hBoxTop.getChildren()) {
                    node.setVisible(true);
                    if (node instanceof MFXButton) {
                        ((MFXButton) node).setButtonType(ButtonType.RAISED);
                    }
                }
                imageTilePane.getChildren().setAll(imageManager.getImagePaneList());
                updateImage();
                hBoxBottom.setVisible(true);
                progressSpinner.setVisible(false);
                loadButton.setDisable(false);
                root.setStyle("-fx-background-color: #131313;");
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

    public void initialize() {
        imageManager = ImageManager.getInstance();
        imageManager.addPropertyChangeListener(e -> updateImage());

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
            setupAnimations();
        });
        pane.toBack();

        slideshowSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            sliderValueLabel.setText(String.format("%ds", newValue.intValue()));
            if (slideshowTask != null)
                slideshowTask.setDelay(newValue.intValue());
        });

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

        imageTilePane.setOnMouseClicked(event -> {
            int index = imageTilePane.getChildren().indexOf(event.getTarget ());
            if(index != -1) {
                imageManager.setCurrentIndex(index);
                updateImage();
            }
        });
    }

    private void startSlideshow() {
        if (slideshowTask == null) {
            slideshowTask = new SlideshowTask((int) slideshowSpeedSlider.getValue());
            Thread thread = new Thread(slideshowTask);
            thread.setDaemon(true);
            thread.start();

            startStopButton.setText("Stop slideshow");
            startStopButton.setStyle("-fx-background-color: #bd2323; -fx-text-fill: #ffffff;");
        }
    }

    private void stopSlideshow() {
        if (slideshowTask != null) {
            slideshowTask.cancel();
            slideshowTask = null;

            startStopButton.setText("Start slideshow");
            startStopButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
        }
    }

    private void updateImage() {
        new Thread(() -> {
            imageView.setImage(imageManager.getCurrentImage());
        }).start();
        File file = imageManager.getCurrentFile();
        Platform.runLater(() -> { // run on JavaFX thread
            nameLabel.setText(file.getName().replace("%20", " "));
            pathLabel.setText(file.getParentFile().getPath().substring(6));
        });
        countPixelColors();
    }

    private void countPixelColors() {
        PixelCounterTask pixelCounterTask = new PixelCounterTask(imageManager.getCurrentFile());
        pixelCounterTask.valueProperty().addListener((ov, oldValue, newValue) -> {
            redCountLabel.setText(String.format("%d (%.2f%%)", newValue.redCount(), newValue.getRedPercentage() * 100));
            greenCountLabel.setText(String.format("%d (%.2f%%)", newValue.greenCount(), newValue.getGreenPercentage() * 100));
            blueCountLabel.setText(String.format("%d (%.2f%%)", newValue.blueCount(), newValue.getBluePercentage() * 100));
        });
        Thread thread = new Thread(pixelCounterTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void setupAnimations() {
        TranslateTransition ttBottom = new TranslateTransition(Duration.millis(70), hBoxBottom);
        TranslateTransition ttTop = new TranslateTransition(Duration.millis(70), hBoxTop);
        TranslateTransition ttList = new TranslateTransition(Duration.millis(70), imageScrollPane);
        PauseTransition pause = new PauseTransition(Duration.millis(3000));

        pause.play();
        pause.setOnFinished(event -> {
            ttTop.setByY(-50);
            ttBottom.setByY(50);
            ttList.setByY(-128);
            ttTop.play();
            ttBottom.play();
            ttList.play();

            pause.playFromStart();
        });

        root.getScene().setOnMouseMoved(event -> {
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

        root.getScene().setOnMouseEntered(event -> {
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

        root.getScene().setOnMouseExited(event -> {
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

        slideshowSpeedSlider.setOnMouseDragged(event -> {
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
}