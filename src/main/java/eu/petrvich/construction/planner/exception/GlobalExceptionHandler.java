package eu.petrvich.construction.planner.exception;

import eu.petrvich.construction.planner.config.AppProperties;
import eu.petrvich.construction.planner.model.error.ErrorRecord;
import eu.petrvich.construction.planner.utils.ErrorRecordBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Construction Planner REST API.
 *
 * Provides centralized exception handling for all controllers, converting
 * exceptions into standardized ErrorRecord objects with appropriate HTTP status codes.
 *
 * Handled exceptions:
 * - InvalidTaskDependencyException (400): Task references non-existent dependency
 * - CircularDependencyException (400): Circular reference in task graph
 * - IllegalArgumentException (400): Invalid input parameters
 * - MethodArgumentNotValidException (400): Bean validation failures
 * - Exception (500): All other unexpected errors
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    public static final String INVALID_PARAMETER_VALUE = "INVALID_PARAMETER_VALUE";

    @ExceptionHandler(InvalidTaskDependencyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleInvalidTaskDependency(InvalidTaskDependencyException e, HttpServletRequest request) {
        logErrorWithContext("Invalid task dependency", e, request);
        return ErrorRecordBuilder.create(HttpStatus.BAD_REQUEST, e.getMessage(), "INVALID_TASK_DEPENDENCY", request);
    }

    @ExceptionHandler(CircularDependencyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleCircularDependency(CircularDependencyException e, HttpServletRequest request) {
        logErrorWithContext("Circular dependency detected", e, request);
        return ErrorRecordBuilder.create(HttpStatus.BAD_REQUEST, e.getMessage(), "CIRCULAR_DEPENDENCY", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorDetails = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        logErrorWithContext("Validation failed: " + errorDetails, e, request);

        return ErrorRecordBuilder.create(HttpStatus.BAD_REQUEST,
                "Input data is not valid. " + errorDetails, "VALIDATION_ERROR", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logErrorWithContext("Invalid argument", e, request);
        return ErrorRecordBuilder.create(HttpStatus.BAD_REQUEST, e.getMessage(), INVALID_PARAMETER_VALUE, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorRecord handleGenericException(Exception e, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        logUnexpectedError(e, request, errorId);

        String message = "An unexpected error occurred. Error ID: " + errorId;
        return ErrorRecordBuilder.create(HttpStatus.INTERNAL_SERVER_ERROR, message, "INTERNAL_SERVER_ERROR", request);
    }

    /**
     * Logs error with full request context including method, URI, and query parameters.
     */
    private void logErrorWithContext(String message, Exception e, HttpServletRequest request) {
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        log.error("{} at {} {}{}: {}",
                message,
                request.getMethod(),
                request.getRequestURI(),
                queryString,
                e.getMessage(),
                e);
    }

    /**
     * Logs unexpected errors with error tracking ID when enabled.
     */
    private void logUnexpectedError(Exception e, HttpServletRequest request, String errorId) {
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        if (errorId != null) {
            log.error("UNEXPECTED ERROR [{}] at {} {}{}: {} - {}",
                    errorId,
                    request.getMethod(),
                    request.getRequestURI(),
                    queryString,
                    e.getClass().getName(),
                    e.getMessage(),
                    e);
        } else {
            log.error("Unexpected error at {} {}{}: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    queryString,
                    e.getMessage(),
                    e);
        }
    }
}
