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
    public ConstraintArgument(@Schema(description = "Hierarchical list of constraint-specific error codes, ordered from most specific to most general. Use constraintCodes.get(0) to get the most specific code.",example = "[\"Size.productName\", \"Size.java.lang.String\", \"Size\"]") List<String> constraintCodes, @Schema(description = "List of constraint parameter values in string format. For @Size(min=2, max=50), this would be [\"2\", \"50\"]. For @Pattern(regexp=\"[A-Z]{3}\"), this would be [\"[A-Z]{3}\"].",example = "[\"2\", \"50\"]") List<String> parameterValues, @Schema(description = "Default error message for this constraint argument",example = "Product name must be between 2 and 50 characters") @Nullable String defaultMessage) {
        this.constraintCodes = constraintCodes;
        this.parameterValues = parameterValues;
        this.defaultMessage = defaultMessage;
    }

    @Schema(
            description = "Hierarchical list of constraint-specific error codes, ordered from most specific to most general. Use constraintCodes.get(0) to get the most specific code.",
            example = "[\"Size.productName\", \"Size.java.lang.String\", \"Size\"]"
    )
    public List<String> constraintCodes() {
        return this.constraintCodes;
    }

    @Schema(
            description = "List of constraint parameter values in string format. For @Size(min=2, max=50), this would be [\"2\", \"50\"]. For @Pattern(regexp=\"[A-Z]{3}\"), this would be [\"[A-Z]{3}\"].",
            example = "[\"2\", \"50\"]"
    )
    public List<String> parameterValues() {
        return this.parameterValues;
    }

    @Schema(
            description = "Default error message for this constraint argument",
            example = "Product name must be between 2 and 50 characters"
    )
    public @Nullable String defaultMessage() {
        return this.defaultMessage;
    }
}
