package net.kerosilas.imageviewer.tasks;

import javafx.concurrent.Task;
import net.kerosilas.imageviewer.model.ImageManager;

public class SlideshowTask extends Task<Void> {

    private int delay;
    private final ImageManager imageManager;

    public SlideshowTask(int delay) {
        this.delay = delay;
        this.imageManager = ImageManager.getInstance();
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    protected Void call() {
        while (!isCancelled()) { // Keep running until the task is cancelled
            try {
                Thread.sleep(delay * 1000L); // Sleep for the specified delay
            } catch (InterruptedException e) {
                if (isCancelled()) { // If the task is cancelled while the thread is sleeping,
                    break;
                }
            }
            imageManager.nextImage();
        }

        return null; // Return null because the task does not return a value
    }
}