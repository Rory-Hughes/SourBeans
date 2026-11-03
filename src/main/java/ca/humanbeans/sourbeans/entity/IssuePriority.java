package ca.humanbeans.sourbeans.entity;
public enum IssuePriority {
    LOW,
    MEDIUM,
    HIGH;
    public static IssuePriority fromString(String s) {
        for (IssuePriority ip : IssuePriority.values()) {
            if (ip.toString().equalsIgnoreCase(s)) {
                return ip;
            }
        }
        return null;
    }
    public static IssuePriority fromInteger(int n) {
        switch (n) {
        case 1:
            return LOW;
        case 2:
            return MEDIUM;
        case 3:
            return HIGH;
        }
        return null;
    }
}
