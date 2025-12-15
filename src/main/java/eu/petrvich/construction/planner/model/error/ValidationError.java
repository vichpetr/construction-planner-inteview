package eu.petrvich.construction.planner.model.error;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.List;

@Schema(
        description = "Validation error details for a specific field or object property"
)
public record ValidationError(List<String> errorCodes, List<ConstraintArgument> constraintArguments, String defaultMessage, String objectName,
                              @Nullable String field, @Nullable String rejectedValue, Boolean bindingFailure) implements
        Serializable {
}
