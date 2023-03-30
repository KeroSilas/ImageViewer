package net.kerosilas.imageviewer.model;

public record PixelColors(int redCount, int greenCount, int blueCount) {

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
