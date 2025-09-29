package joe.davtian.tms;

public class User {
    // Fields
    private int id;
    private int employeeId;
    private String email;
    private String password;
    private String role;
    private String branch;
    private String phoneNumber;
    private Name name;

    // Constructor
    public User(int employeeId, int id, String email, String password,
                String phoneNumber, String branch, String role, Name name) {
        this.employeeId = employeeId;
        this.id = id;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.branch = branch;
        this.role = role;
        this.name = name;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getRole() {
        return role;
    }

    public String getBranch() {
        return branch;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Name getName() {
        return name;
    }

}