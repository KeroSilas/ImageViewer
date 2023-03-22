module net.kerosilas.imageviewer {
    requires javafx.controls;
    requires javafx.fxml;


    opens net.kerosilas.imageviewer to javafx.fxml;
    exports net.kerosilas.imageviewer;
}