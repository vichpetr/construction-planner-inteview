package eu.petrvich.construction.planner.dto;

import eu.petrvich.construction.planner.model.Crew;
import eu.petrvich.construction.planner.model.Equipment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskWithIntervals {

    @JsonProperty("taskCode")
    private String taskCode;

    @JsonProperty("operationName")
    private String operationName;

    @JsonProperty("elementName")
    private String elementName;

    @JsonProperty("duration")
    private int duration;

    @JsonProperty("crew")
    private Crew crew;

    @JsonProperty("equipment")
    private List<Equipment> equipment;

    @JsonProperty("dependencies")
    private List<String> dependencies;

    @JsonProperty("startInterval")
    private int startInterval;

    @JsonProperty("endInterval")
    private int endInterval;
}
