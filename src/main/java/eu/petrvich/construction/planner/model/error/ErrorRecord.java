package eu.petrvich.construction.planner.model.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Schema(
        description = "Standardized error response for external API consumers"
)
@JsonInclude(Include.NON_NULL)
public record ErrorRecord(ZonedDateTime timestamp, Integer status, String message, @Nullable String path, String errorCode, @Nullable List<ValidationError> validationErrors, @Nullable String traceId, @Nullable String spanId, @Nullable Map<String, Object> additionalInfo) implements
        Serializable {
    public ErrorRecord(@Schema(description = "Timestamp when the error occurred (ISO 8601 format with timezone)",example = "2023-12-01T10:30:00.000+01:00") @JsonFormat(shape = Shape.STRING,pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") ZonedDateTime timestamp, @Schema(description = "HTTP status code",example = "400") Integer status, @Schema(description = "Human-readable error message describing what went wrong",example = "Product validation failed") String message, @Schema(description = "Request path that caused the error",example = "/external/products") @Nullable String path, @Schema(description = "Application-specific error code for programmatic error handling. Use this code to implement specific error handling logic in your application.",example = "PRODUCT_VALIDATION_FAILED") String errorCode, @Schema(description = "List of detailed validation errors for individual fields. Present when the error is caused by validation failures.") @Nullable List<ValidationError> validationErrors, @Schema(description = "Distributed tracing ID for correlating logs and debugging. Include this ID when reporting issues to support.",example = "1a2b3c4d5e6f7g8h") @Nullable String traceId, @Schema(description = "Span ID for distributed tracing",example = "9i8h7g6f5e4d3c2b") @Nullable String spanId, @Schema(description = "Additional contextual information about the error. Can include IDs of affected entities, configuration values, or other relevant metadata.",example = "{ \"productId\": 345, \"categoryId\": 123, \"storeId\": \"CZ01\" }") @Nullable Map<String, Object> additionalInfo) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.path = path;
        this.errorCode = errorCode;
        this.validationErrors = validationErrors;
        this.traceId = traceId;
        this.spanId = spanId;
        this.additionalInfo = additionalInfo;
    }

    @Schema(
            description = "Timestamp when the error occurred (ISO 8601 format with timezone)",
            example = "2023-12-01T10:30:00.000+01:00"
    )
    @JsonFormat(
            shape = Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    )
    public ZonedDateTime timestamp() {
        return this.timestamp;
    }

    @Schema(
            description = "HTTP status code",
            example = "400"
    )
    public Integer status() {
        return this.status;
    }

    @Schema(
            description = "Human-readable error message describing what went wrong",
            example = "Product validation failed"
    )
    public String message() {
        return this.message;
    }

    @Schema(
            description = "Request path that caused the error",
            example = "/external/products"
    )
    public @Nullable String path() {
        return this.path;
    }

    @Schema(
            description = "Application-specific error code for programmatic error handling. Use this code to implement specific error handling logic in your application.",
            example = "PRODUCT_VALIDATION_FAILED"
    )
    public String errorCode() {
        return this.errorCode;
    }

    @Schema(
            description = "List of detailed validation errors for individual fields. Present when the error is caused by validation failures."
    )
    public @Nullable List<ValidationError> validationErrors() {
        return this.validationErrors;
    }

    @Schema(
            description = "Distributed tracing ID for correlating logs and debugging. Include this ID when reporting issues to support.",
            example = "1a2b3c4d5e6f7g8h"
    )
    public @Nullable String traceId() {
        return this.traceId;
    }

    @Schema(
            description = "Span ID for distributed tracing",
            example = "9i8h7g6f5e4d3c2b"
    )
    public @Nullable String spanId() {
        return this.spanId;
    }

    @Schema(
            description = "Additional contextual information about the error. Can include IDs of affected entities, configuration values, or other relevant metadata.",
            example = "{ \"productId\": 345, \"categoryId\": 123, \"storeId\": \"CZ01\" }"
    )
    public @Nullable Map<String, Object> additionalInfo() {
        return this.additionalInfo;
    }
}
