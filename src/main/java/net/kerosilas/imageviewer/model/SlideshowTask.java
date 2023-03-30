package net.kerosilas.imageviewer.model;

import javafx.concurrent.Task;

public class SlideshowTask extends Task<Void> {

    private final ImageManager imageManager;
    private int delay;

    public SlideshowTask(int delay) {
        this.imageManager = ImageManager.getInstance();
        this.delay = delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    protected Void call() {
        while (!isCancelled()) {
            try {
                Thread.sleep(delay * 1000L);
            } catch (InterruptedException e) {
                if (isCancelled()) {
                    break;
                }
            }
            imageManager.nextImage();
        }

        return null;
    }
}