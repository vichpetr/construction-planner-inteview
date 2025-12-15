package eu.petrvich.construction.planner.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "Error response containing error details")
public class ErrorResponse {

    @Schema(description = "Error code identifying the type of error", example = "INVALID_TASK_DEPENDENCY")
    private String errorCode;

    @Schema(description = "Human-readable error message", example = "Task A depends on non-existent task: Z")
    private String message;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Timestamp when the error occurred", example = "2025-12-15T16:30:00")
    private LocalDateTime timestamp;

    public ErrorResponse(String errorCode, String message, int status) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
