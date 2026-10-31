package ca.humanbeans.sourbeans.exception;

/** A domain operation conflicts with the resource's current state. */
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException() {
        super("The operation conflicts with the current resource state.");
    }
}
