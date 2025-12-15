package eu.petrvich.construction.planner.utils;

import eu.petrvich.construction.planner.model.error.ErrorRecord;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Map;

@UtilityClass
public class ErrorRecordBuilder {

    private static final String TRACE_ID = "traceId";
    private static final String SPAN_ID = "spanId";

    public static ErrorRecord create(HttpStatus httpStatus, String message, String errorCode, HttpServletRequest request) {
        return create(httpStatus, message, errorCode, request.getRequestURI());
    }

    public static ErrorRecord createFull(ZonedDateTime timestamp, HttpStatus httpStatus, String message, String path, String errorCode,
             String traceId, String spanId) {
        return new ErrorRecord(timestamp, httpStatus.value(), message, path, errorCode, traceId, spanId);
    }

    private static ErrorRecord create(HttpStatus httpStatus, String message, String errorCode, String path) {
        return createFull(ZonedDateTime.now(), httpStatus, message, path, errorCode, MDC.get(TRACE_ID), MDC.get(SPAN_ID));
    }
}
