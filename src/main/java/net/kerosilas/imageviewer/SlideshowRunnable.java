package net.kerosilas.imageviewer;

import javafx.application.Platform;

// This class implements Runnable and contains the slideshow method
public class SlideshowRunnable implements Runnable {

    // Some fields to store the parameters
    private int sliderValue;
    private boolean isSlideshowRunning;

    // A field to store a reference of SlideshowController
    private final ImageViewerWindowController controller;

    // A constructor that takes in the parameters and the controller reference
    public SlideshowRunnable(int sliderValue, boolean isSlideshowRunning, ImageViewerWindowController controller) {
        this.sliderValue = sliderValue;
        this.isSlideshowRunning = isSlideshowRunning;
        this.controller = controller;
    }

    // The run method that executes on a separate thread
    @Override
    public void run() {
        try {
            while (true) {;
                Thread.sleep(sliderValue * 1000L);

                Platform.runLater(() -> {
                    if (isSlideshowRunning)
                        controller.displayImage(1);
                });
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setSliderValue(int sliderValue) {
        this.sliderValue = sliderValue;
    }

    public void setSlideshowRunning(boolean slideshowRunning) {
        isSlideshowRunning = slideshowRunning;
    }

    public boolean isSlideshowRunning() {
        return isSlideshowRunning;
    }
}