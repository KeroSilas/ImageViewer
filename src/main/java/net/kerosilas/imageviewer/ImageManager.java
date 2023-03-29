package net.kerosilas.imageviewer;

import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Factory class for ImagePane objects

public class ImageManager {
    private static ImageManager instance = null;
    private final List<File> fileList;
    private ImagePane imagePane;
    private final List<ImagePane> imagePaneList;

    private ImageManager() {
        imagePaneList = new ArrayList<>();
        fileList = new ArrayList<>();
    }

    public static ImageManager getInstance() {
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }

    public void addImages(List<File> files) {
        for (File file : files) {
            imagePane = new ImagePane(file);
            imagePaneList.add(imagePane);
            fileList.add(file);
        }
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

    public int getIndex(HBox hBox) {
        for (int i = 0; i < imagePaneList.size(); i++) {
            if (imagePaneList.get(i).getHBox().equals(hBox)) {
                return i;
            }
        }
        return -1;
    }
}