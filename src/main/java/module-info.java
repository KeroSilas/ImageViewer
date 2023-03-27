module net.kerosilas.imageviewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires MaterialFX;


    opens net.kerosilas.imageviewer to javafx.fxml;
    exports net.kerosilas.imageviewer;
}