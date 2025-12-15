package eu.petrvich.construction.planner.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Task {

    @JsonProperty
    @NotBlank(message = "Task code is required and cannot be blank")
    private String taskCode;

    @JsonProperty
    @NotBlank(message = "Operation name is required and cannot be blank")
    private String operationName;

    @JsonProperty
    private String elementName;

    @JsonProperty
    @Positive(message = "Duration must be a positive number")
    private int duration;

    @JsonProperty
    @Valid
    private Crew crew;

    @JsonProperty
    @Valid
    @NotNull(message = "Equipment list cannot be null")
    private List<Equipment> equipment = new ArrayList<>();

    @JsonProperty
    @NotNull(message = "Dependencies list cannot be null")
    private List<String> dependencies = new ArrayList<>();

    @JsonIgnore
    private int earliestStart = 0;

    @JsonIgnore
    private int earliestFinish = 0;

    @JsonIgnore
    private int latestStart = 0;

    @JsonIgnore
    private int latestFinish = 0;

    @JsonIgnore
    private int slack = 0;

    @JsonIgnore
    private boolean isCritical = false;

    @JsonProperty
    private Integer startInterval;

    @JsonProperty
    private Integer endInterval;

    /**
     * Gets the number of crew members assigned to this task.
     * Returns 0 if no crew is assigned.
     */
    public int getCrewCount() {
        return crew != null ? crew.getAssignment() : 0;
    }

    /**
     * Checks if this task has dependencies.
     */
    public boolean hasDependencies() {
        return dependencies != null && !dependencies.isEmpty();
    }
}
