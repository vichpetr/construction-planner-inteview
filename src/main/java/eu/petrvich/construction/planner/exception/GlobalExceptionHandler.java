package eu.petrvich.construction.planner.exception;

import eu.petrvich.construction.planner.model.error.ErrorRecord;
import eu.petrvich.construction.planner.model.error.ValidationError;
import eu.petrvich.construction.planner.utils.ErrorRecordBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;

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

        List<ValidationError> errors = e.getBindingResult().getAllErrors().stream().map(error -> {
            if (error instanceof FieldError fieldError) {
                return new ValidationError(
                        List.of(Objects.requireNonNull(fieldError.getCode())),
                        List.of(),
                        fieldError.getDefaultMessage(),
                        fieldError.getObjectName(),
                        fieldError.getField(),
                        fieldError.getRejectedValue() != null ? fieldError.getRejectedValue().toString()
                                : null,
                        false
                );
            } else {
                return new ValidationError(
                        List.of(Objects.requireNonNull(error.getCode())),
                        List.of(),
                        error.getDefaultMessage(),
                        error.getObjectName(),
                        null,
                        null,
                        false
                );
            }
        }).toList();

        return ErrorRecordBuilder.createWithValidation(HttpStatus.BAD_REQUEST,
                "Input data is not valid.", "VALIDATION_ERROR", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.error("Constraint violation error: {}", e.getMessage(), e);

        List<ValidationError> errors = e.getConstraintViolations().stream().map(violation -> {
            String propertyPath = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : null;
            String invalidValue = violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : null;

            return new ValidationError(
                    List.of("ConstraintViolation"),
                    List.of(),
                    violation.getMessage(),
                    violation.getRootBeanClass() != null ? violation.getRootBeanClass().getSimpleName() : null,
                    propertyPath,
                    invalidValue,
                    false
            );
        }).toList();

        return ErrorRecordBuilder.createWithValidation(HttpStatus.BAD_REQUEST,
                "Constraint validation failed.", "VALIDATION_ERROR", request, errors);
    }

    @ExceptionHandler(ConversionFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleConversionFailedException(ConversionFailedException e,
            HttpServletRequest request) {
        log.error("Conversion error: {}", e.getMessage(), e);

        String invalidValue = e.getValue() != null ? e.getValue().toString() : "null";
        String targetType = e.getTargetType().getType().getSimpleName();

        String message = String.format("Invalid value '%s' for parameter type '%s'", invalidValue, targetType);
        return ErrorRecordBuilder.createWithValidation(HttpStatus.BAD_REQUEST, message, INVALID_PARAMETER_VALUE, request, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleIllegalArgumentException(IllegalArgumentException e,
            HttpServletRequest request) {
        log.error("Invalid argument error: {}", e.getMessage(), e);

        return ErrorRecordBuilder.createWithValidation(HttpStatus.BAD_REQUEST, e.getMessage(), INVALID_PARAMETER_VALUE, request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRecord handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {
        log.error("Method argument type mismatch error: {}", e.getMessage(), e);

        String parameterName = e.getName();
        String invalidValue = e.getValue() != null ? e.getValue().toString() : "null";

        // Extract meaningful error message from the cause (our custom converter)
        String errorMessage = e.getMessage();
        if (e.getCause() instanceof IllegalArgumentException) {
            errorMessage = e.getCause().getMessage();
        }

        List<ValidationError> errors = List.of(new ValidationError(
                List.of("TypeMismatch"),
                null,
                errorMessage,
                "listExternalOrders",
                parameterName,
                invalidValue,
                true
        ));

        String message = String.format("Parameter validation failed for '%s'", parameterName);
        return ErrorRecordBuilder.createWithValidation(HttpStatus.BAD_REQUEST, message, INVALID_PARAMETER_VALUE, request, errors);
    }
}
