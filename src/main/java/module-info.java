module joe.davtian.tms {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires org.mongodb.driver.sync.client;

    opens joe.davtian.tms to javafx.fxml;
    exports joe.davtian.tms;
}
