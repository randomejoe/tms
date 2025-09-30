package joe.davtian.tms;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onViewTickets() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("tickets-view.fxml"));
            Scene scene = new Scene(loader.load(), 950, 600);

            Stage stage = new Stage();
            stage.setTitle("Tickets");
            stage.initModality(Modality.NONE);
            stage.initOwner(welcomeText.getScene().getWindow());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Unable to open tickets view", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Unexpected error" : message);
        alert.showAndWait();
    }
}
