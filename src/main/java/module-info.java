module net.kerosilas.imageviewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires MaterialFX;


    opens net.kerosilas.imageviewer to javafx.fxml;
    exports net.kerosilas.imageviewer;
    exports net.kerosilas.imageviewer.model;
    opens net.kerosilas.imageviewer.model to javafx.fxml;
    exports net.kerosilas.imageviewer.controller;
    opens net.kerosilas.imageviewer.controller to javafx.fxml;
}