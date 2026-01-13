package common.dto.UserAccount;

import common.enums.EmployeeRole;

public class EmployeeLogInResult {
    private final boolean success;
    private final String message;
    private final Integer employeeId;
    private final EmployeeRole role;
    private final String fullName;

    private EmployeeLogInResult(boolean success, String message, Integer employeeId, EmployeeRole role, String fullName) {
        this.success = success;
        this.message = message;
        this.employeeId = employeeId;
        this.role = role;
        this.fullName = fullName;
    }

    public static EmployeeLogInResult ok(int id, EmployeeRole role, String fullName, String msg) {
        return new EmployeeLogInResult(true, msg, id, role, fullName);
    }
    public static EmployeeLogInResult fail(String msg) {
        return new EmployeeLogInResult(false, msg, null, null, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getEmployeeId() { return employeeId; }
    public EmployeeRole getRole() { return role; }
    public String getFullName() { return fullName; }
}
