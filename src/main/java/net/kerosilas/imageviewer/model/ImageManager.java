package net.kerosilas.imageviewer.model;

import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

// Keeps track of a list of images and the current index of the image that is being displayed
// This class is a singleton, so that it can be accessed from anywhere in the application
// It also implements the PropertyChangeSupport class to notify the ImageViewerWindowController class when the current index changes

public class ImageManager {

    private final PropertyChangeSupport support; // Used to notify the ImageViewerWindowController class when the current index changes

    private static ImageManager instance = null;
    private final Vector<File> fileList; // Vector is used instead of ArrayList because it is thread-safe
    private final Vector<ImagePane> imagePaneList;

    private int currentIndex = 0;

    private ImageManager() {
        imagePaneList = new Vector<>();
        fileList = new Vector<>();
        support = new PropertyChangeSupport(this);
    }

    public static ImageManager getInstance() {
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }

    public void addImage(File file) {
        ImagePane imagePane = new ImagePane(file);
        imagePaneList.add(imagePane);
        fileList.add(file);
    }

    public List<HBox> getImagePaneList() { // Returns a list of HBoxes to be put in the imageTilePane of the ImageViewerWindowController class
        List<HBox> hBoxList = new ArrayList<>();
        for (ImagePane imagePane : imagePaneList) {
            hBoxList.add(imagePane.getHBox());
        }
        return hBoxList;
    }

    public Image getCurrentImage() {
        return new Image(fileList.get(currentIndex).toURI().toString());
    }

    public synchronized void setCurrentIndex(int newIndex) {
        int oldIndex = this.currentIndex;
        this.currentIndex = newIndex;
        support.firePropertyChange("currentIndex", oldIndex, newIndex); // Notifies the ImageViewerWindowController class when the current index changes
    }

    public void nextImage() {
        if (currentIndex < fileList.size() - 1) {
            setCurrentIndex(currentIndex + 1);
        } else {
            setCurrentIndex(0);
        }
    }

    public void previousImage() {
        if (currentIndex > 0) {
            setCurrentIndex(currentIndex - 1);
        } else {
            setCurrentIndex(fileList.size() - 1);
        }
    }

    public File getCurrentFile() {
        return fileList.get(currentIndex);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }
}