package eu.petrvich.construction.planner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Task with calculated CPM intervals including start/end times")
public class TaskWithIntervals {

    @JsonProperty("taskCode")
    @Schema(description = "Unique identifier for the task", example = "A")
    private String taskCode;

    @JsonProperty("operationName")
    @Schema(description = "Name of the operation/activity", example = "Foundation")
    private String operationName;

    @JsonProperty("elementName")
    @Schema(description = "Name of the construction element", example = "Building A")
    private String elementName;

    @JsonProperty("duration")
    @Schema(description = "Duration of the task in time units", example = "5")
    private int duration;

    @JsonProperty("crew")
    @Schema(description = "Crew assignment for the task")
    private Crew crew;

    @JsonProperty("equipment")
    @Schema(description = "List of equipment needed for the task")
    private List<Equipment> equipment;

    @JsonProperty("dependencies")
    @Schema(description = "List of task codes that must be completed before this task", example = "[\"A\", \"B\"]")
    private List<String> dependencies;

    @JsonProperty("startInterval")
    @Schema(description = "Calculated start time for the task based on CPM", example = "0")
    private int startInterval;

    @JsonProperty("endInterval")
    @Schema(description = "Calculated end time for the task based on CPM", example = "5")
    private int endInterval;
}
