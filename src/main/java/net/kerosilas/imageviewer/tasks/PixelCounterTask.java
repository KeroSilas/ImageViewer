package net.kerosilas.imageviewer.tasks;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import net.kerosilas.imageviewer.model.ImageManager;
import net.kerosilas.imageviewer.model.PixelColors;

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

        for (int x = 0; x < width; x++) { // Iterate over all pixels
            for (int y = 0; y < height; y++) {
                Color color = pixelReader.getColor(x, y); // Get the color of the pixel

                if (color.getRed() > color.getGreen() && color.getRed() > color.getBlue()) { // If the red component is the largest, increment the red counter
                    redCount++;
                } else if (color.getGreen() > color.getRed() && color.getGreen() > color.getBlue()) { // If the green component is the largest, increment the green counter
                    greenCount++;
                } else if (color.getBlue() > color.getRed() && color.getBlue() > color.getGreen()) { // If the blue component is the largest, increment the blue counter
                    blueCount++;
                }
            }
        }

        return new PixelColors(redCount, greenCount, blueCount); // Store the results in a PixelColors object
    }
}
