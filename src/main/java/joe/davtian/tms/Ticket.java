package joe.davtian.tms;
import java.util.Date;

public class Ticket {
    private String id;
    private String Status;
    private Date dateOfSubmission;
    private Date deadline;
    private String priority;
    private int employeeID;
    private String type;
    private String subject;
    private String description;

    public Ticket(String id, String status, Date dateOfSubmission, Date deadline, String priority, int employeeID, String type, String subject, String description) {
        this.id = id;
        this.Status = status;
        this.dateOfSubmission = dateOfSubmission;
        this.deadline = deadline;
        this.priority = priority;
        this.employeeID = employeeID;
        this.type = type;
        this.subject = subject;
        this.description = description;
    }

    public Ticket() {}

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status = status;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public Date getDateOfSubmission() {
        return dateOfSubmission;
    }

    public void setDateOfSubmission(Date dateOfSubmission) {
        this.dateOfSubmission = dateOfSubmission;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Ticket:\n");
        builder.append("  id: ").append(id).append('\n');
        builder.append("  status: ").append(Status).append('\n');
        builder.append("  dateOfSubmission: ").append(dateOfSubmission).append('\n');
        builder.append("  deadline: ").append(deadline).append('\n');
        builder.append("  priority: ").append(priority).append('\n');
        builder.append("  employeeID: ").append(employeeID).append('\n');
        builder.append("  type: ").append(type).append('\n');
        builder.append("  subject: ").append(subject).append('\n');
        builder.append("  description: ").append(description);
        return builder.toString();
    }
}
