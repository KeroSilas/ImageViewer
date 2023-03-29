package net.kerosilas.imageviewer.model;

import javafx.scene.layout.HBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ImageManager {

    private static ImageManager instance = null;
    private final Vector<File> fileList;
    private final Vector<ImagePane> imagePaneList;

    private ImageManager() {
        imagePaneList = new Vector<>();
        fileList = new Vector<>();
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

    public List<File> getFileList() {
        return fileList;
    }

    public List<HBox> getImagePaneList() {
        List<HBox> hBoxList = new ArrayList<>();
        for (ImagePane imagePane : imagePaneList) {
            hBoxList.add(imagePane.getHBox());
        }
        return hBoxList;
    }
}