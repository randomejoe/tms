package joe.davtian.tms;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class TicketDetailController {

    private final TicketRepository ticketRepository = new TicketRepository();
    private Ticket ticket;
    private Runnable onTicketChanged;
    private Stage stage;
    private boolean createMode;

    @FXML
    private TextField idField;

    @FXML
    private TextField statusField;

    @FXML
    private DatePicker submissionPicker;

    @FXML
    private DatePicker deadlinePicker;

    @FXML
    private TextField priorityField;

    @FXML
    private Spinner<Integer> employeeSpinner;

    @FXML
    private TextField typeField;

    @FXML
    private TextField subjectField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    public void initialize() {
        employeeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        idField.setEditable(false);
        idField.setPromptText("Generated after save");
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        if (ticket == null) {
            clearForm();
        } else {
            populateForm(ticket);
        }
    }

    public void setOnTicketChanged(Runnable onTicketChanged) {
        this.onTicketChanged = onTicketChanged;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCreateMode(boolean createMode) {
        this.createMode = createMode;
        if (updateButton != null) {
            updateButton.setText(createMode ? "Create" : "Update");
        }
        if (deleteButton != null) {
            deleteButton.setDisable(createMode);
        }
    }

    public void prepareNewTicket() {
        this.ticket = new Ticket();
        clearForm();
    }

    @FXML
    private void onUpdate() {
        try {
            Ticket formTicket = collectTicketFromForm();
            if (createMode) {
                Ticket created = ticketRepository.createTicket(formTicket);
                this.ticket = created;
                populateForm(created);
                setCreateMode(false);
                if (stage != null) {
                    stage.setTitle("Ticket Details");
                }
                notifyTicketChanged();
                showInfo("Ticket created", "The ticket was created successfully.");
            } else {
                if (ticket == null) {
                    showError("No ticket selected", "There is no ticket to update.");
                    return;
                }

                Ticket persisted = ticketRepository.updateTicket(formTicket);
                if (persisted == null) {
                    showError("Ticket not found", "The ticket could not be updated because it no longer exists.");
                    return;
                }

                this.ticket = persisted;
                populateForm(persisted);
                notifyTicketChanged();
                showInfo("Ticket updated", "Changes were saved successfully.");
            }
        } catch (IllegalArgumentException e) {
            showError("Invalid data", e.getMessage());
        } catch (Exception e) {
            showError("Update failed", e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        if (ticket == null) {
            return;
        }

        try {
            boolean deleted = ticketRepository.deleteTicket(ticket.getId());
            if (!deleted) {
                showError("Delete failed", "The ticket could not be deleted. It may have been removed already.");
                return;
            }

            notifyTicketChanged();
            closeStage();
        } catch (Exception e) {
            showError("Delete failed", e.getMessage());
        }
    }

    @FXML
    private void onClose() {
        closeStage();
    }

    private void populateForm(Ticket existing) {
        idField.setText(existing.getId());
        statusField.setText(existing.getStatus());
        submissionPicker.setValue(toLocalDate(existing.getDateOfSubmission()));
        deadlinePicker.setValue(toLocalDate(existing.getDeadline()));
        priorityField.setText(existing.getPriority());
        if (employeeSpinner.getValueFactory() != null) {
            employeeSpinner.getValueFactory().setValue(existing.getEmployeeID());
        }
        typeField.setText(existing.getType());
        subjectField.setText(existing.getSubject());
        descriptionArea.setText(existing.getDescription());
    }

    private Ticket collectTicketFromForm() {
        Ticket result = new Ticket();

        String id = trimOrNull(idField.getText());
        if (!createMode && (id == null || id.isBlank())) {
            if (ticket != null && ticket.getId() != null && !ticket.getId().isBlank()) {
                id = ticket.getId();
            } else {
                throw new IllegalArgumentException("Ticket id is required");
            }
        }

        result.setId(id);
        result.setStatus(trimOrNull(statusField.getText()));
        result.setDateOfSubmission(toDate(submissionPicker.getValue()));
        result.setDeadline(toDate(deadlinePicker.getValue()));
        result.setPriority(trimOrNull(priorityField.getText()));

        Integer employeeId = employeeSpinner.getValue();
        if (employeeId == null) {
            employeeId = 0;
        }
        result.setEmployeeID(employeeId);
        result.setType(trimOrNull(typeField.getText()));
        result.setSubject(trimOrNull(subjectField.getText()));
        result.setDescription(trimOrNull(descriptionArea.getText()));

        return result;
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void notifyTicketChanged() {
        if (onTicketChanged != null) {
            onTicketChanged.run();
        }
    }

    private void closeStage() {
        if (stage != null) {
            stage.close();
        }
    }

    private void clearForm() {
        idField.setText("");
        statusField.setText("");
        submissionPicker.setValue(null);
        deadlinePicker.setValue(null);
        priorityField.setText("");
        if (employeeSpinner.getValueFactory() != null) {
            employeeSpinner.getValueFactory().setValue(0);
        }
        typeField.setText("");
        subjectField.setText("");
        descriptionArea.setText("");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Unexpected error" : message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
