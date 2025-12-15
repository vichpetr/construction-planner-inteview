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
public record ErrorRecord(@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") ZonedDateTime timestamp,
                          Integer status,
                          String message,
                          @Nullable String path,
                          String errorCode,
                          @Nullable List<ValidationError> validationErrors,
                          @Nullable String traceId,
                          @Nullable String spanId,
                          @Nullable Map<String, Object> additionalInfo) implements Serializable {
}
