package net.kerosilas.imageviewer;

public class PixelColors {
    private final int redCount;
    private final int greenCount;
    private final int blueCount;

    public PixelColors(int redCount, int greenCount, int blueCount) {
        this.redCount = redCount;
        this.greenCount = greenCount;
        this.blueCount = blueCount;
    }

    public int getRedCount() {
        return redCount;
    }

    public int getGreenCount() {
        return greenCount;
    }

    public int getBlueCount() {
        return blueCount;
    }

    public int getTotalCount() {
        return redCount + greenCount + blueCount;
    }

    public double getRedPercentage() {
        return (double) redCount / getTotalCount();
    }

    public double getGreenPercentage() {
        return (double) greenCount / getTotalCount();
    }

    public double getBluePercentage() {
        return (double) blueCount / getTotalCount();
    }

    public String toString() {
        return String.format("Red: %d (%.2f%%), Green: %d (%.2f%%), Blue: %d (%.2f%%)",
                redCount, getRedPercentage() * 100,
                greenCount, getGreenPercentage() * 100,
                blueCount, getBluePercentage() * 100);
    }

}
