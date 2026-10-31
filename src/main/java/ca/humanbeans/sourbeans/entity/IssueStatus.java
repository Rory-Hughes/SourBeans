package ca.humanbeans.sourbeans.entity;
public enum IssueStatus {
    // TODO: Review these are the actual enums expected
    Unknown,
    TODO,
    IN_PROGRESS,
    DONE;
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
            return TODO;
        case 2:
            return IN_PROGRESS;
        case 3:
            return DONE;
        }
        return null;
    }
}
