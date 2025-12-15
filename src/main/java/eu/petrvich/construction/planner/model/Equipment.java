package eu.petrvich.construction.planner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {

    @JsonProperty
    @NotBlank(message = "Equipment name is required and cannot be blank")
    private String name;

    @JsonProperty
    @Positive(message = "Equipment quantity must be a positive number")
    private int quantity;
}
