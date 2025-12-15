package eu.petrvich.construction.planner.utils;

import eu.petrvich.construction.planner.model.error.ErrorRecord;
import eu.petrvich.construction.planner.model.error.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@UtilityClass
public class ErrorRecordBuilder {

    private static final String TRACE_ID = "traceId";
    private static final String SPAN_ID = "spanId";

    public static ErrorRecord create(HttpStatus httpStatus, String message, String errorCode, HttpServletRequest request) {
        return create(httpStatus, message, errorCode, request.getRequestURI(), null, null);
    }

    public static ErrorRecord create(HttpStatus httpStatus, String message, String errorCode, String path) {
        return create(httpStatus, message, errorCode, path, null, null);
    }

    public static ErrorRecord createWithValidation(HttpStatus httpStatus, String message, String errorCode, HttpServletRequest request,
            List<ValidationError> validationErrors) {
        return create(httpStatus, message, errorCode, request.getRequestURI(), validationErrors, null);
    }

    public static ErrorRecord createWithValidation(HttpStatus httpStatus, String message, String errorCode, String path,
            List<ValidationError> validationErrors) {
        return create(httpStatus, message, errorCode, path, validationErrors, null);
    }

    public static ErrorRecord createWithAdditionalInfo(HttpStatus httpStatus, String message, String errorCode, HttpServletRequest request,
            Map<String, Object> additionalInfo) {
        return create(httpStatus, message, errorCode, request.getRequestURI(), null, additionalInfo);
    }

    public static ErrorRecord createWithAdditionalInfo(HttpStatus httpStatus, String message, String errorCode, String path,
            Map<String, Object> additionalInfo) {
        return create(httpStatus, message, errorCode, path, null, additionalInfo);
    }

    public static ErrorRecord createWithValidationAndAdditionalInfo(HttpStatus httpStatus, String message, String errorCode, HttpServletRequest request,
            List<ValidationError> validationErrors, Map<String, Object> additionalInfo) {
        return create(httpStatus, message, errorCode, request.getRequestURI(), validationErrors, additionalInfo);
    }

    public static ErrorRecord createWithValidationAndAdditionalInfo(HttpStatus httpStatus, String message, String errorCode, String path,
            List<ValidationError> validationErrors, Map<String, Object> additionalInfo) {
        return create(httpStatus, message, errorCode, path, validationErrors, additionalInfo);
    }

    public static ErrorRecord createFull(ZonedDateTime timestamp, HttpStatus httpStatus, String message, String path, String errorCode,
            List<ValidationError> validationErrors, String traceId, String spanId, Map<String, Object> additionalInfo) {
        return new ErrorRecord(timestamp, httpStatus.value(), message, path, errorCode, validationErrors, traceId, spanId, additionalInfo);
    }

    private static ErrorRecord create(HttpStatus httpStatus, String message, String errorCode, String path, List<ValidationError> validationErrors,
            Map<String, Object> additionalInfo) {
        return createFull(ZonedDateTime.now(), httpStatus, message, path, errorCode, validationErrors, MDC.get(TRACE_ID), MDC.get(SPAN_ID), additionalInfo);
    }
}
