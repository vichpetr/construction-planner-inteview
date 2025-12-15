package eu.petrvich.construction.planner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Crew {

    @JsonProperty
    private String name;

    @JsonProperty
    private int assignment;
}
