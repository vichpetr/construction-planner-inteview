package eu.petrvich.construction.planner.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.List;

@Schema(
        description = "Arguments and parameters used in validation constraints"
)
@JsonInclude(Include.NON_NULL)
public record ConstraintArgument(List<String> constraintCodes, List<String> parameterValues, @Nullable String defaultMessage) implements Serializable {
}
