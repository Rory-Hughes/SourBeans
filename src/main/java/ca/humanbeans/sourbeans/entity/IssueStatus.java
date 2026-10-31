package ca.humanbeans.sourbeans.entity;
public enum IssueStatus {
    // TODO: Review these are the actual enums expected
    Unknown,
    OPEN,
    CLOSED;
    public static IssueStatus fromString(String s) {
        for (IssueStatus is : IssueStatus.values()) {
            if (is.toString().equalsIgnoreCase(s)) {
                return is;
            }
        }
        return null;
    }
    public static IssueStatus fromInteger(int n) {
        switch (n) {
        case 1:
            return OPEN;
        case 2:
            return CLOSED;
        }
        return null;
    }
}
