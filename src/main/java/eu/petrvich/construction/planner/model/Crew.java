package eu.petrvich.construction.planner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Crew {

    @JsonProperty
    @NotBlank(message = "Crew name is required and cannot be blank")
    private String name;

    @JsonProperty
    @PositiveOrZero(message = "Crew assignment must be zero or a positive number")
    private int assignment;
}
