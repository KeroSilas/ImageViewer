package net.kerosilas.imageviewer.model;

import javafx.concurrent.Task;
import javafx.scene.image.Image;

import java.io.File;
import java.util.List;

public class SlideshowTask extends Task<Image> {

    private final ImageManager imageManager;
    private final List<File> imageFiles; // the image files to display
    private final int delay; // the delay between images in seconds
    private int index; // the index of the current image file

    public SlideshowTask(List<File> imageFiles, int delay, int index) {
        this.imageManager = ImageManager.getInstance();
        this.imageFiles = imageFiles;
        this.delay = delay;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    protected Image call() {
        while (!isCancelled()) { // loop until cancelled

            // load the image from the file and update the value property
            Image image = new Image(imageManager.getFileList().get(index).toURI().toString());
            updateValue(image);

            // sleep for the specified delay or until interrupted
            try {
                Thread.sleep(delay * 1000L);
            } catch (InterruptedException e) {
                if (isCancelled()) {
                    break;
                }
            }

            // increment the index or wrap around if it reaches the end of the list
            index = (index + 1) % imageFiles.size();
        }

        return null; // return null when cancelled or done
    }
}