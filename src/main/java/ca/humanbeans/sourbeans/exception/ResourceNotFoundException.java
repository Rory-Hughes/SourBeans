package ca.humanbeans.sourbeans.exception;

/** A requested domain resource does not exist. No internal details are sent to clients. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
        super("The requested resource was not found.");
    }
}
