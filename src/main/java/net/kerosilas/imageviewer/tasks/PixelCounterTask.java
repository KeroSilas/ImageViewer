package net.kerosilas.imageviewer.tasks;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import net.kerosilas.imageviewer.model.ImageManager;
import net.kerosilas.imageviewer.model.PixelColors;

import java.io.File;

public class PixelCounterTask extends Task<PixelColors> {

    private final ImageManager imageManager;

    public PixelCounterTask() {
        imageManager = ImageManager.getInstance();
    }

    @Override
    protected PixelColors call() {
        Image image = imageManager.getCurrentImage();
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

        return new PixelColors(redCount, greenCount, blueCount);
    }
}
