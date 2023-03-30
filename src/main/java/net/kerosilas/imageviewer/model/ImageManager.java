package net.kerosilas.imageviewer.model;

import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ImageManager {

    private final PropertyChangeSupport support;

    private static ImageManager instance = null;
    private final Vector<File> fileList;
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

    public boolean addImage(File file) {
        ImagePane imagePane = new ImagePane(file);
        imagePaneList.add(imagePane);
        fileList.add(file);
        return true;
    }

    public List<HBox> getImagePaneList() {
        List<HBox> hBoxList = new ArrayList<>();
        for (ImagePane imagePane : imagePaneList) {
            hBoxList.add(imagePane.getHBox());
        }
        return hBoxList;
    }

    public Image getCurrentImage() {
        return new Image(fileList.get(currentIndex).toURI().toString());
    }

    public void setCurrentIndex(int newIndex) {
        int oldIndex = this.currentIndex;
        this.currentIndex = newIndex;
        support.firePropertyChange("currentIndex", oldIndex, newIndex);
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

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }
}