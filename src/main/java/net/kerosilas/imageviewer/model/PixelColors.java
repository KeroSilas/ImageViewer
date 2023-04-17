package net.kerosilas.imageviewer.model;

public record PixelColors(int redCount, int greenCount, int blueCount, int mixedCount) {

    public int getTotalCount() {
        return redCount + greenCount + blueCount + mixedCount;
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

    public double getMixedPercentage() {
        return (double) mixedCount / getTotalCount();
    }
}
