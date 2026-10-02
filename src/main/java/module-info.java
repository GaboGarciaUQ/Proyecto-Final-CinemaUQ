module com.uniquindio.proyectofinalp2.cinemauq {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.uniquindio.proyectofinalp2.cinemauq to javafx.fxml;
    exports com.uniquindio.proyectofinalp2.cinemauq;
}
