package joe.davtian.tms;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class TicketListController {

    private final TicketRepository ticketRepository = new TicketRepository();
    private final ObservableList<Ticket> tickets = FXCollections.observableArrayList();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @FXML
    private TableView<Ticket> ticketsTable;

    @FXML
    private TableColumn<Ticket, String> idColumn;

    @FXML
    private TableColumn<Ticket, String> statusColumn;

    @FXML
    private TableColumn<Ticket, String> submissionColumn;

    @FXML
    private TableColumn<Ticket, String> deadlineColumn;

    @FXML
    private TableColumn<Ticket, String> priorityColumn;

    @FXML
    private TableColumn<Ticket, Number> employeeColumn;

    @FXML
    private TableColumn<Ticket, String> typeColumn;

    @FXML
    private TableColumn<Ticket, String> subjectColumn;

    @FXML
    private TableColumn<Ticket, String> descriptionColumn;

    @FXML
    public void initialize() {
        configureColumns();
        loadTickets();
    }

    private void configureColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        employeeColumn.setCellValueFactory(new PropertyValueFactory<>("employeeID"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        submissionColumn.setCellValueFactory(cell -> new SimpleStringProperty(
            formatDate(cell.getValue().getDateOfSubmission())));
        deadlineColumn.setCellValueFactory(cell -> new SimpleStringProperty(
            formatDate(cell.getValue().getDeadline())));

        ticketsTable.setItems(tickets);
        ticketsTable.setRowFactory(table -> {
            TableRow<Ticket> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    openTicketDetail(row.getItem());
                }
            });
            return row;
        });
    }

    private String formatDate(java.util.Date date) {
        if (date == null) {
            return "";
        }
        synchronized (dateFormat) {
            return dateFormat.format(date);
        }
    }

    @FXML
    private void onRefresh() {
        loadTickets();
    }

    @FXML
    private void onAddTicket() {
        openTicketDetail(null, true);
    }

    private void loadTickets() {
        try {
            List<Ticket> results = ticketRepository.readTickets();
            tickets.setAll(results);
        } catch (Exception e) {
            showError("Unable to load tickets", e.getMessage());
        }
    }

    private void openTicketDetail(Ticket ticket) {
        openTicketDetail(ticket, false);
    }

    private void openTicketDetail(Ticket ticket, boolean createMode) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("ticket-detail-view.fxml"));
            Scene scene = new Scene(loader.load(), 600, 500);

            TicketDetailController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle(createMode ? "New Ticket" : "Ticket Details");
            stage.setScene(scene);
            if (ticketsTable != null && ticketsTable.getScene() != null) {
                stage.initOwner(ticketsTable.getScene().getWindow());
            }

            controller.setStage(stage);
            controller.setOnTicketChanged(() -> {
                loadTickets();
                ticketsTable.refresh();
            });
            controller.setCreateMode(createMode);
            if (createMode) {
                controller.prepareNewTicket();
            } else {
                controller.setTicket(ticket);
            }

            stage.show();
        } catch (Exception e) {
            showError("Unable to open ticket", e.getMessage());
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
