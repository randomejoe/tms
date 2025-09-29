module joe.davtian.tms {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens joe.davtian.tms to javafx.fxml;
    exports joe.davtian.tms;
}