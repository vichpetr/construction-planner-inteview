package eu.petrvich.construction.planner.exception;

import eu.petrvich.construction.planner.model.error.ErrorRecord;
import eu.petrvich.construction.planner.utils.ErrorRecordBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    public static final String INVALID_PARAMETER_VALUE = "INVALID_PARAMETER_VALUE";

    @ExceptionHandler(InvalidTaskDependencyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleInvalidTaskDependency(InvalidTaskDependencyException e, HttpServletRequest request) {
        log.error("Invalid task dependency: {}", e.getMessage(), e);
        return ErrorRecordBuilder.create(HttpStatus.BAD_REQUEST, e.getMessage(), "INVALID_TASK_DEPENDENCY", request);
    }

    @ExceptionHandler(CircularDependencyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleCircularDependency(CircularDependencyException e, HttpServletRequest request) {
        log.error("Circular dependency detected: {}", e.getMessage());
        return ErrorRecordBuilder.create(HttpStatus.BAD_REQUEST, e.getMessage(), "CIRCULAR_DEPENDENCY", request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorRecord handleGenericException(Exception e, HttpServletRequest request) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ErrorRecordBuilder.create(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred", "INTERNAL_SERVER_ERROR", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("Validation error: {}", e.getMessage(), e);

        return ErrorRecordBuilder.create(HttpStatus.BAD_REQUEST,
                "Input data is not valid.", "VALIDATION_ERROR", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleIllegalArgumentException(IllegalArgumentException e,
            HttpServletRequest request) {
        log.error("Invalid argument error: {}", e.getMessage(), e);

        return ErrorRecordBuilder.create(HttpStatus.BAD_REQUEST, e.getMessage(), INVALID_PARAMETER_VALUE, request);
    }
}
