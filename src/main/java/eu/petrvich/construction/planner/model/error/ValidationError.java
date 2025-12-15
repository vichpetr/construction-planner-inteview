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

    public ValidationError(
            @Schema(description = "Hierarchical list of Spring validation error codes, ordered from most specific to most general. Use errorCodes.get(0) to get the most specific code for programmatic error handling.", example = "[\"NotNull.productName\", \"NotNull.java.lang.String\", \"NotNull\"]") List<String> errorCodes,
            @Schema(description = "Arguments passed to the validation constraint (e.g., min/max values, patterns)") List<ConstraintArgument> constraintArguments,
            @Schema(description = "Default error message in the system's primary language", example = "Product name cannot be null") String defaultMessage,
            @Schema(description = "Name of the object being validated", example = "newProductDTO") String objectName,
            @Schema(description = "Name of the field that failed validation. Null for object-level errors.", example = "productName") @Nullable String field,
            @Schema(description = "The actual value that was rejected by the validation rule. May be null if the value was null or if it's sensitive data.", example = "null") @Nullable String rejectedValue,
            @Schema(description = "Indicates whether this error occurred during data binding (type conversion) rather than validation. True for binding failures, false for validation failures.", example = "false") Boolean bindingFailure) {
        this.errorCodes = errorCodes;
        this.constraintArguments = constraintArguments;
        this.defaultMessage = defaultMessage;
        this.objectName = objectName;
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.bindingFailure = bindingFailure;
    }

    @Schema(
            description = "Hierarchical list of Spring validation error codes, ordered from most specific to most general. Use errorCodes.get(0) to get the most specific code for programmatic error handling.",
            example = "[\"NotNull.productName\", \"NotNull.java.lang.String\", \"NotNull\"]"
    )
    public List<String> errorCodes() {
        return this.errorCodes;
    }

    @Schema(
            description = "Arguments passed to the validation constraint (e.g., min/max values, patterns)"
    )
    public List<ConstraintArgument> constraintArguments() {
        return this.constraintArguments;
    }

    @Schema(
            description = "Default error message in the system's primary language",
            example = "Product name cannot be null"
    )
    public String defaultMessage() {
        return this.defaultMessage;
    }

    @Schema(
            description = "Name of the object being validated",
            example = "newProductDTO"
    )
    public String objectName() {
        return this.objectName;
    }

    @Schema(
            description = "Name of the field that failed validation. Null for object-level errors.",
            example = "productName"
    )
    public @Nullable String field() {
        return this.field;
    }

    @Schema(
            description = "The actual value that was rejected by the validation rule. May be null if the value was null or if it's sensitive data.",
            example = "null"
    )
    public @Nullable String rejectedValue() {
        return this.rejectedValue;
    }

    @Schema(
            description = "Indicates whether this error occurred during data binding (type conversion) rather than validation. True for binding failures, false for validation failures.",
            example = "false"
    )
    public Boolean bindingFailure() {
        return this.bindingFailure;
    }
}
