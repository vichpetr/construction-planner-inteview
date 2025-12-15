package eu.petrvich.construction.planner.exception;

public class InvalidTaskDependencyException extends RuntimeException {
    public InvalidTaskDependencyException(String message) {
        super(message);
    }
}
